import sys, os, re, stat
import subprocess
import shutil, errno
import traceback

from pathlib import Path

import git
import fire

from mkdocs import config
from mkdocs.commands import build, serve, gh_deploy

CWD=os.getcwd()
MERGED_DOCS_FOLDER=os.path.join(CWD, '.merged')
ENTERPRISE_PREFIX='enterprise'
ENTERPRISE_ROOT=os.path.join(CWD, '.tmp')
MKDOCS_SITE_ROOT=os.path.join(CWD, 'site')
ENTERPRISE_DOCS_ROOT=os.path.join(ENTERPRISE_ROOT, 'content')
PUBLIC_DOCS_ROOT=os.path.join(CWD, 'public', 'content')
EXTENSIONS_TO_COPY=('.md','.jpg','.png', '.css', '.js', '.gif')
KEYWORD_DELIMITER = '@@'

def pretty_path(path):
    short = path.replace(CWD, '')
    unix = Path(short).as_posix()
    return '.' + unix

def execute(cmd, cwd=None, timeout=None):
    try:
        p = subprocess.Popen(cmd, stdout=sys.stdout, stderr=sys.stderr, cwd=None)
        p.communicate()
        return p.returncode
    except Exception as e:
        print('Exception happened while running ' + str(cmd))
        traceback.print_exc()
        return None

def get_keywords(filename):
    with open(filename) as f:
        return dict([ l.strip().split('=', 1) for l in f.readlines() ])

def del_rw(action, name, exc):
    os.chmod(name, stat.S_IWRITE)
    os.remove(name)

def fetch_enterprise_docs(local_repo, url):
    if local_repo:
        global ENTERPRISE_ROOT
        global ENTERPRISE_DOCS_ROOT
        ENTERPRISE_ROOT = str(Path(local_repo))
        ENTERPRISE_DOCS_ROOT = str(Path(local_repo, 'content'))
    else:
        clone_enterprise_docs(url)

def clone_enterprise_docs(url):
    print('Cloning {} in {}'.format(url, pretty_path(ENTERPRISE_ROOT)))
    if os.path.isdir(ENTERPRISE_ROOT):
        shutil.rmtree(ENTERPRISE_ROOT, onerror=del_rw)
    Path(ENTERPRISE_ROOT).mkdir(parents=True, exist_ok=True)
    git.Git(ENTERPRISE_ROOT).clone(url, '.')
    
def move_enterprise_files():
    shutil.copy2(Path(ENTERPRISE_ROOT, '{}.properties'.format(ENTERPRISE_PREFIX)), Path('./{}.properties'.format(ENTERPRISE_PREFIX)))
    shutil.copy2(Path(ENTERPRISE_ROOT, 'mkdocs.yml'), Path('./mkdocs-enterprise.yml'))

def prepare_generated_docs_tree(base_docs_path, generated_docs_path):
    print('Copying {} in {}'.format(pretty_path(base_docs_path), pretty_path(generated_docs_path)))
    if os.path.isdir(generated_docs_path):
        shutil.rmtree(generated_docs_path)
    try:
        shutil.copytree(base_docs_path, generated_docs_path)
    except:
        print('Failed to copy the baseline docs')
        traceback.print_exc()
        sys.exit(-2)

def merge_docs(dir_1, dir_2, merged_docs, extensions):
    '''
    Copy all the content of dir_2 to merged_docs to form the baseline content.
    Then iterate over the content of dir_1 and:
    - overwrite a file f in merged_docs if a file f1 exists in dir_1 with same name
    - add a file f in merged_docs if f ends with '_{ENTERPRISE_PREFIX}.md'
    - through a warning otherwise
    '''
    prepare_generated_docs_tree(dir_2, merged_docs)
    allowed_suffixes = tuple([ '_{}'.format(ENTERPRISE_PREFIX) + ex  for ex in extensions ])
    try:
        print('Copying {} in {}'.format(pretty_path(dir_1), pretty_path(merged_docs)))
        for root, _, files in os.walk(dir_1):
            for f in [ f for f in files if f.endswith(extensions) ]:  
                src_file_path = os.path.join(root, f)
                dest_file_path = src_file_path.replace(dir_1, merged_docs)
                if os.path.isfile(dest_file_path):
                    #print('Replacing file {} in merged docs'.format(src_file_path))
                    shutil.copyfile(str(src_file_path), str(dest_file_path))
                elif str(src_file_path).endswith(allowed_suffixes):
                    #print('Adding file    {} to merged docs'.format(src_file_path))
                    try:
                        shutil.copyfile(str(src_file_path), str(dest_file_path))
                    except:
                        print('Ignoring file {}'.format(dest_file_path))
                        traceback.print_exc()
                else:
                    print('WARNING: File {} is not present in the base docs'.format(pretty_path(src_file_path)))
                    print('         Please add the \'_{}\' to its name (before the extension) and try again. This file will be skipped'.format(ENTERPRISE_PREFIX))
                    continue
    except:
        traceback.print_exc()
        sys.exit(-1)   

def replace_keywords(dir_1, context):
    print('Replacing keywords in {}'.format(pretty_path(dir_1)))
    keywords = get_keywords(context + '.properties')

    # for k, v in keywords.items():
    #     print('Replacing keywords')
    #     print('{}  -->   {}'.format(k,v))

    old_keywords = dict(keywords)
    keywords = dict()
    for k,v in old_keywords.items():
        keywords[KEYWORD_DELIMITER + k + KEYWORD_DELIMITER] = v

    for root, _, files in os.walk(dir_1):
            for f in [ f for f in files if f.endswith('.md') ]:  
                with open(os.path.join(root,f), 'r+', encoding='utf-8') as fp:
                    file_content = fp.read()
                    substrings = sorted(keywords, key=len, reverse=True)
                    regex = re.compile('|'.join(map(re.escape, substrings)))
                    new_file_content = regex.sub(lambda match: keywords[match.group(0)] , file_content)
                    fp.seek(0)
                    fp.write(new_file_content)
                    fp.truncate()

def handleRemoveReadonly(func, path, exc):
    # https://stackoverflow.com/a/1214935/3482533
    excvalue = exc[1]
    if func in (os.rmdir, os.remove) and excvalue.errno == errno.EACCES:
        os.chmod(path, stat.S_IRWXU| stat.S_IRWXG| stat.S_IRWXO) # 0777
        func(path)
    else:
        raise

def clean():
    print('Cleaning up')
    files = ['{}.properties'.format(ENTERPRISE_PREFIX), 'mkdocs-enterprise.yml']
    for file in files:
        os.remove(file) if os.path.exists(file) else None
    shutil.rmtree(ENTERPRISE_ROOT, ignore_errors=True, onerror=handleRemoveReadonly)
    shutil.rmtree(MERGED_DOCS_FOLDER, ignore_errors=True, onerror=handleRemoveReadonly)
    shutil.rmtree(MKDOCS_SITE_ROOT, ignore_errors=True, onerror=handleRemoveReadonly)

def select_config(kind):
    return 'mkdocs.yml' if kind == 'public' else 'mkdocs-enterprise.yml'

def handle_mkdocs_build(to_build, kind):
    print('Building website using MKdocs, will be saved in ./site')
    if to_build:
        build.build(config.load_config(
            config_file=select_config(kind),
        ))

def handle_mkdocs_serve(to_serve, kind, dev_addr):
    if to_serve:
        serve.serve(config_file=select_config(kind), dev_addr=dev_addr)

def delete_branch(branch):
    try:
        out = git.cmd.Git(CWD).execute('git branch -D {}'.format(branch))
    except:
        pass

def handle_mkdocs_ghdeploy(to_ghdeploy, kind, remote):
    if to_ghdeploy:
        delete_branch('gh-pages')
        cfg = config.load_config(
            config_file=os.path.join(CWD, select_config(kind)),
            remote_name=remote
        )
        build.build(cfg)
        print('Deploying {} Github Pages to {}#gh-pages'.format(kind, remote))
        gh_deploy.gh_deploy(cfg, force=True)

class Initiator(object):

    def __init__(self, mkbuild=False, mkserve=False, mkghdeploy=False, dev_addr=None, help=False):
        if help:
            print('docs.py [public|enterprise] [--mkbuild|--mkserve|--mkghdeploy|--local_repo <path>|--dev_addr <address>]')
            exit()
        self.mkbuild = mkbuild
        self.mkserve = mkserve
        self.mkghdeploy = mkghdeploy
        self.dev_addr = dev_addr

    def enterprise(self, url, local_repo=False):
        '''
        Generate enterprise docs
        Submit the git URL of the enterprise repository
        '''
        fetch_enterprise_docs(local_repo, url)
        move_enterprise_files()
        merge_docs(ENTERPRISE_DOCS_ROOT, PUBLIC_DOCS_ROOT, MERGED_DOCS_FOLDER, EXTENSIONS_TO_COPY)
        replace_keywords(MERGED_DOCS_FOLDER, ENTERPRISE_PREFIX)
        handle_mkdocs_build(self.mkbuild, ENTERPRISE_PREFIX)
        handle_mkdocs_serve(self.mkserve, ENTERPRISE_PREFIX, self.dev_addr)
        handle_mkdocs_ghdeploy(self.mkghdeploy, ENTERPRISE_PREFIX, url)
        clean()

    def public(self):
        '''
        Generate public docs
        '''
        prepare_generated_docs_tree(PUBLIC_DOCS_ROOT, MERGED_DOCS_FOLDER)
        replace_keywords(MERGED_DOCS_FOLDER, 'public')
        handle_mkdocs_build(self.mkbuild, 'public')
        handle_mkdocs_serve(self.mkserve, 'public', self.dev_addr)
        handle_mkdocs_ghdeploy(self.mkghdeploy, 'public', 'origin')

if __name__ == '__main__':
    fire.Fire(Initiator)
