"""
Wraps the vulnerability-assessment-tool Java CLI.
"""

import sys
import os

CONFIG_FILE = "vulas-python.cfg"
wrapper_path = os.path.dirname(os.path.realpath(__file__))
jar_path = os.path.join(wrapper_path, "cli-scanner-latest-jar-with-dependencies.jar")

""" Checks whether the JAR of the Java CLI is available and executes. """
def is_available():
    exists = os.path.isfile(jar_path)
    exit_code = os.system(" ".join(("java", "-jar", jar_path)))
    return exists and exit_code==0

""" Transforms the given dict into a list of Java system properties"""
def dict_to_java_sys_props(dict):
    props = list()
    for key in dict.keys():
        props.append('-D' + key + '="' + dict.get(key) + '"')
    return props

""" Runs the Java CLI, whereby the entries of the given dictionary are used as Java system properties. """
def run(dict, goal, debug='off'):
    cmd = list()
    cmd.append("java")

    # Uncomment to debug the Java CLI
    if(debug):
        cmd.append("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000")

    for v in dict_to_java_sys_props(dict):
        cmd.append(v)
    for v in ["-jar", jar_path, "-goal", goal]:
        cmd.append(v)
    exit_code = os.system(" ".join(cmd))
    return exit_code==0

def read_vulas_configuration_path():
    cwd = os.getcwd()
    config_path = os.path.join(cwd, CONFIG_FILE)
    exists = os.path.isfile(config_path)
    if not exists:
        raise ValueError('Configuration file [' + CONFIG_FILE + "] not found in working directory [" + cwd + "]")
    return config_path

def read_vulas_configuration(path):
    file = {}
    with open(path) as f:
        for line in f:
            if not line.startswith('#') and not line.strip()== '':
                (key, val) = line.split('=')
                if not val.strip() == '':
                    file[key.strip()] = val.strip()
    return file

def main():  # needed for console script
    os.system(run({}))

if __name__ == "__main__":
    sys.exit(main())