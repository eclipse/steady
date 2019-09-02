package com.sap.psr.vulas.nodejs.npm;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.util.DigestUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.nodejs.ProcessWrapper;
import com.sap.psr.vulas.nodejs.ProcessWrapperException;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;

public class NpmWrapper {

    private final static Log log = LogFactory.getLog(NpmWrapper.class);

    private static final String PACKAGE_JSON = "package.json";
    private static final boolean IS_WIN = System.getProperty("os.name").contains("Windows");

    private Path pathToVirtualenv = null;
    private Path pathToNpmExecutable = null;
    private Path pathToNodejsProject = null;

    private String projectName = null;

    private Set<NpmInstalledPackage> installedPackages = null;
    private Set<String> devDependenciesPath = null;

    /**
     * Assumes that the npm executable is part of the PATH environment variable.
     */
    public NpmWrapper(Path _path_to_nodejs_project) throws ProcessWrapperException {
        this(Paths.get(IS_WIN ? "npm.cmd": "npm"), _path_to_nodejs_project);
    }

    /**
     * Creates a new wrapper for the npm executable at the given path.
     * @param _path_to_npm
     */
    public NpmWrapper(Path _path_to_npm, Path _path_to_nodejs_project) throws ProcessWrapperException {

        // Check it is a directory with a file "package.json"
        if(!FileUtil.isAccessibleDirectory(_path_to_nodejs_project)) {
            throw new IllegalArgumentException("Project path [" + _path_to_nodejs_project + "] does not point to an accessible directory");
        }
        if(!DirUtil.containsFile(_path_to_nodejs_project.toFile(), PACKAGE_JSON)) {
            throw new IllegalArgumentException("Project path [" + _path_to_nodejs_project + "] does not contain the file [" + PACKAGE_JSON + "]");
        }

        this.pathToNpmExecutable = _path_to_npm;
        this.pathToNodejsProject = _path_to_nodejs_project.toAbsolutePath();
        this.projectName = this.pathToNodejsProject.getName(this.pathToNodejsProject.getNameCount()-1).toString();

        // Create the virtual environment for project and dependencies
        try {
            this.pathToVirtualenv = FileUtil.createTmpDir("vulas-npm-virtualenv-" + this.projectName + "-").toAbsolutePath();
        } catch (IOException e) {
            throw new ProcessWrapperException("Cannot create tmp directory: " + e.getMessage());
        }

        // Copy the project folder
        this.copyProjectDirectory();

        // Call npm install
        final Path project_path = Paths.get(this.pathToVirtualenv.toString(), this.projectName);
        this.installedPackages = installPackages(project_path, 0);
    }

    public String getProjectName() {
        return this.projectName;
    }

    public Path getPathToVirtualenv() {
        return this.pathToVirtualenv;
    }

    public Set<NpmInstalledPackage> getInstalledPackages() {
        return this.installedPackages;
    }

    private Path copyProjectDirectory() throws ProcessWrapperException {
        Path new_dir = null;
        try {
            Files.walkFileTree(this.pathToNodejsProject, new HashSet<FileVisitOption>(), Integer.MAX_VALUE, new CopyFileVisitor(this.pathToNodejsProject, this.pathToVirtualenv));
        } catch (Exception e) {
            log.error("Cannot copy project dir [" + this.pathToNodejsProject + "] to virtual env [" + this.pathToNpmExecutable + "]:" + e.getMessage());
            throw new ProcessWrapperException("Cannot copy project dir [" + this.pathToNodejsProject + "] to virtual env [" + this.pathToNpmExecutable + "]:" + e.getMessage());
        }
        return new_dir;
    }

    private static class CopyFileVisitor extends SimpleFileVisitor<Path> {

        private final static Log log = LogFactory.getLog(CopyFileVisitor.class);

        private Path src = null;
        private Path tgt = null;

        public CopyFileVisitor(Path _src, Path _tgt) {
            this.src = _src;
            this.tgt = _tgt;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            final Path relp = this.src.getParent().relativize(dir.toAbsolutePath());
            final Path newp = Paths.get(tgt.toString(), relp.toString());
            try {
                Files.createDirectories(newp);
            } catch (Exception e) {
                log.error("Cannot copy [" + dir + "] to [" + newp + "]: " + e.getMessage());
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            final Path relp = this.src.getParent().relativize(file);
            final Path newf = Paths.get(this.tgt.toString(), relp.toString());
            try {
                Files.copy(file, newf);
            } catch (Exception e) {
                log.error("Cannot copy [" + file + "] to [" + newf + "]: " + e.getMessage());
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public Set<NpmInstalledPackage> installPackages(Path _project_path, int _attempt) throws ProcessWrapperException{
        Set<NpmInstalledPackage> packages = null;
        try {
            // Clear npm cache before installing
            ProcessWrapper pw_cache = new ProcessWrapper();
            pw_cache.setWorkingDir(_project_path);
            pw_cache.setPath(this.pathToVirtualenv);
            pw_cache.setCommand(this.pathToNpmExecutable,"cache", "clean", "--force");
            pw_cache.setOutErrName("npm-cache-" + _attempt);
            Thread t_cache = new Thread(pw_cache);
            t_cache.start();
            t_cache.join();

            // Remove package-lock.json and node_modules before installing dependencies,
            // only when npm fail to install in the first attempt
            if(_attempt > 0) {
                removeLockModules(_project_path);
            }

            // Download and install all dependencies
            ProcessWrapper pw_install = new ProcessWrapper();
            pw_install.setWorkingDir(_project_path);
            pw_install.setPath(this.pathToVirtualenv);
            pw_install.setCommand(this.pathToNpmExecutable,"install", "--no-audit");
            pw_install.setOutErrName("npm-install-" + _attempt);
            Thread t_install = new Thread(pw_install);
            t_install.start();
            t_install.join();

           // Check installing status
           if(parseNpmInstallErrLog(pw_install.getErrFile())){
               if(_attempt > 0) {
                   throw new ProcessWrapperException("Got error from npm-install");
               }
               else {
                   log.warn("Failed to install dependencies, try the second attempt");
                   return installPackages(_project_path, _attempt + 1);
               }
           }

           // Get devDependencies path
            this.devDependenciesPath = this.callNpmList("--dev");
            // Get all dependencies
            packages = this.getListPackages();
        } catch(ProcessWrapperException e) {
            throw new ProcessWrapperException("Error calling installing packages: " + e.getMessage(), e);
        } catch(IOException e) {
            throw new ProcessWrapperException("Error calling installing packages: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            throw new ProcessWrapperException("Error calling installing packages: " + e.getMessage(), e);
        }
        return packages;
    }

    private void removeLockModules(Path _dir) throws IOException{
        final Path lock_file = Paths.get(_dir.toString(), "package-lock.json");
        final Path modules = Paths.get(_dir.toString(), "node_modules");
        if(FileUtil.isAccessibleFile(lock_file)) {
            log.warn("Found [package-lock.json] in [" + _dir + "], removing");
            Files.delete(lock_file);
        }
        if(FileUtil.isAccessibleDirectory(modules)) {
            log.warn("Found [node_modules] in [" + _dir + "], removing");
            Files.walkFileTree(modules, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private boolean parseNpmInstallErrLog(Path _log) throws IOException{
        // Read the list of installed dependencies/project paths
        final String file = FileUtil.readFile(_log);
        String[] log_lines = file.split("\n");
        for(String line: log_lines)
            if(line.toUpperCase().startsWith("NPM ERR"))
                return true;
        return false;
    }

    private Map<String, String> getPackageType(Path pack_path) throws IOException {
        final Map<String, String> type_obj = new HashMap<>();
        final Path pack_json_file = Paths.get(pack_path.toString(), "package-lock.json");
        final Queue<JsonObject> pack_queue = new LinkedList<>();
        final JsonObject lock_json = new Gson().fromJson(FileUtil.readFile(pack_json_file), JsonObject.class);
        pack_queue.add(lock_json);

        while(!pack_queue.isEmpty()) {
            final JsonObject dep = pack_queue.remove();
            final List<String> dep_type = new ArrayList<>();
            if(dep.has("dev"))
                dep_type.add("dev");
            if(dep.has("optional"))
                dep_type.add("optional");
            if(dep.has("bundled"))
                dep_type.add("bundled");
            if(dep_type.size() != 0) {
                final String hash = dep.get("integrity").getAsString();
                type_obj.put(hash, StringUtil.join(dep_type, ","));
            }
            if(dep.has("dependencies")) {
                for(String dep_name : dep.getAsJsonObject("dependencies").keySet()) {
                    pack_queue.add(dep.getAsJsonObject("dependencies").getAsJsonObject(dep_name));
                }
            }
        }
        return type_obj;
    }

    private Set<NpmInstalledPackage> getListPackages() throws ProcessWrapperException, IOException, InterruptedException {
        Set<NpmInstalledPackage> packages = null;

        // List paths of installed dependencies
        Set<String> installed_paths = callNpmList();
        packages = this.parseNpmListParseableOutput(installed_paths);

        return packages;
    }

    private Set<String> callNpmList() throws ProcessWrapperException, IOException, InterruptedException {
        return callNpmList(null);
    }

    private Set<String> callNpmList(String arg) throws ProcessWrapperException, IOException, InterruptedException{

        // List paths of installed dependencies
        ProcessWrapper pw = new ProcessWrapper();
        final Path project_path = Paths.get(this.pathToVirtualenv.toString(), this.projectName);
        if(arg != null)
            pw.setCommand(this.pathToNpmExecutable, "list", "--parseable", arg);
        else
            pw.setCommand(this.pathToNpmExecutable, "list", "--parseable");
        pw.setWorkingDir(project_path);
        pw.setPath(this.pathToVirtualenv);
        pw.setOutErrName("npm-list");
        Thread t = new Thread(pw, "npm-list");
        t.start();
        t.join();

        // Read the list of installed dependencies/project paths
        final String file = FileUtil.readFile(pw.getOutFile());
        return new HashSet<String>(Arrays.asList(file.split("\n")));
    }

    private Set<NpmInstalledPackage> parseNpmListParseableOutput(Set<String> pack_path_list) throws IOException {
        final Set<NpmInstalledPackage> set = new HashSet<>();

        // Read package.json for each package in node_modules
        for(String pack_path: pack_path_list) {
            // Get package information from package.json
            final Path pack_json_file = Paths.get(pack_path, "package.json");
            final JsonObject pack_json = new Gson().fromJson(FileUtil.readFile(pack_json_file), JsonObject.class);

            final Map<String, String> pack_props = new HashMap<>();

            // Replace / with $ to avoid the problem with the filename or url that refer to this package
            String pack_name = pack_json.get("name").getAsString();
            final String pack_version = pack_json.get("version").getAsString();
            String pack_url = "";
            String pack_dep_location = "/";
            String pack_required_by = "";
            String pack_integrity = "";
            String pack_shasum = "";
            DigestAlgorithm pack_shasum_type = DigestAlgorithm.SHA1;
            String pack_git_hash = "";
            File tarball_dest = null;

            try {
                if(pack_json.has("_resolved")) {
                    pack_url = pack_json.get("_resolved").getAsString();
                }
                else {
                    log.warn("Cannot get \"_resolved\" property of [" + pack_name + "], set the default value instead");
                }

                if(pack_json.has("_location")) {
                    pack_dep_location = pack_json.get("_location").getAsString();
                }
                else {
                    log.warn("Cannot get \"_location\" of [" + pack_name + "], set the default value instead");
                }

                if(pack_json.has("_requiredBy")) {
                    List<String> required_by_list = new ArrayList<>();
                    for (JsonElement dep : pack_json.getAsJsonArray("_requiredBy")) {
                        String dep_name = dep.getAsString();
                        dep_name = dep_name.substring(dep_name.indexOf("/"));
                        required_by_list.add(dep_name);
                    }
                    pack_required_by = StringUtils.join(required_by_list, ",");
                }
                else {
                    log.warn("Cannot get \"_requiredBy\" of [" + pack_name + "], set the default value instead");
                }

                if(pack_json.has("_shasum") && pack_json.has("_integrity")) {
                    pack_shasum = pack_json.get("_shasum").getAsString();
                    pack_integrity = pack_json.get("_integrity").getAsString();
                }
                else if(!pack_json.has("_shasum") && (pack_json.has("_integrity")) && !String.valueOf(pack_json.get("_integrity").getAsString()).equalsIgnoreCase("")) {
                    log.warn("Cannot get \"_shasum\" of [" + pack_name + "], compute from \"_integrity\" instead");
                    pack_integrity = pack_json.get("_integrity").getAsString();
                    String[] hash = pack_integrity.split("-");
                    if(hash[0].equalsIgnoreCase("sha512"))
                        pack_shasum_type = DigestAlgorithm.SHA512;

                    byte[] hashByte = Base64.decodeBase64(hash[1]);
                    pack_shasum = DigestUtil.bytesToHex(hashByte);
                }
                else {
                    log.warn("Cannot get \"_shasum\" and \"_integrity\" of [" + pack_name + "], create a tarball and compute them instead");
                    try {
                        tarball_dest = Paths.get(pack_path + ".tgz").toFile();

                        DirUtil.createTarBall(Paths.get(pack_path).toFile(), tarball_dest, new String[] {"node_modules"}, null);
                        pack_integrity = DigestAlgorithm.SHA512.toString().replace("-", "") +"-"+ FileUtil.getDigest(tarball_dest, DigestAlgorithm.SHA512);
                        pack_shasum = FileUtil.getDigest(tarball_dest, DigestAlgorithm.SHA1);
                    } catch(ArchiveException e) {
                        log.error(e.getMessage());
                    } catch(Exception e) {
                        log.error(e.getMessage());
                    }
                }
                pack_shasum = pack_shasum.toUpperCase();

                if(pack_json.has("_requested")) {
                    if(pack_json.getAsJsonObject("_requested").get("type").getAsString().equalsIgnoreCase("git")) {
                        log.info("[" + pack_name + "] was requested from git repository, extract git hash");
                        pack_git_hash = "#" + pack_url.split("#")[1];
                    }
                }
            } catch(NullPointerException e){
                log.error("Cannot get properties of [" + pack_name + "], set the default value instead");
            }

            if(pack_required_by.equalsIgnoreCase("")) {
                final String old_name = pack_name;
                pack_name = pack_name.replace("/", "$");
                log.info("Replace / in package name with $ [" + old_name + "] -> [" + pack_name + "]");
            }

            pack_props.put("name", pack_name);
            pack_props.put("version", pack_version);
            pack_props.put("location", pack_path);
            pack_props.put("dep_location", pack_dep_location);
            pack_props.put("required_by", pack_required_by);
            pack_props.put("integrity", pack_integrity);
            pack_props.put("shasum", pack_shasum);
            pack_props.put("shasum_type", pack_shasum_type.toString());
            pack_props.put("created_tarball", String.valueOf(tarball_dest));
            pack_props.put("git_hash", pack_git_hash);

            if(this.devDependenciesPath.contains(pack_path)) {
                pack_props.put("dev", "true");
            }

            NpmInstalledPackage pack = new NpmInstalledPackage(pack_name, pack_version);
            pack.setDownloadPath(Paths.get(pack_path));
            pack.setDownloadUrl(pack_url);
            pack.addProperties(pack_props);
            pack.setTarballFile(tarball_dest != null ? tarball_dest.toPath() : null);

            set.add(pack);
        }
        return set;
    }
}