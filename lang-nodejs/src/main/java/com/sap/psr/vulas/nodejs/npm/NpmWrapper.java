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
import java.util.HashSet;
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
        this.installedPackages = installPackages(project_path);
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

    public Set<NpmInstalledPackage> installPackages(Path _project_path) {
        Set<NpmInstalledPackage> packages = null;
        try {
            // Download and install all dependencies
            ProcessWrapper pw = new ProcessWrapper();
//            pw.setWorkingDir(Paths.get("C:\\Users\\I517033\\AppData\\Local\\Temp\\vulas-npm-virtualenv-test-helloworld-8744455940834443910\\test-helloworld"));
            pw.setWorkingDir(_project_path);
            pw.setCommand(this.pathToNpmExecutable,"install", "-ddd", "--force", "--no-audit");
//            pw.setPath(Paths.get("C:\\Users\\I517033\\AppData\\Local\\Temp\\vulas-npm-virtualenv-test-helloworld-8744455940834443910"));
            pw.setPath(this.pathToVirtualenv);
            Thread t = new Thread(pw);
            t.start();
            t.join();

            // Get all dependencies
            packages = this.getListPackages();
        } catch(ProcessWrapperException e) {
            log.error("Error calling installing packages: " + e.getMessage(), e);
        } catch(IOException e) {
            log.error("Error calling installing packages: " + e.getMessage(), e);
        } catch(InterruptedException e) {
            log.error("Error calling installing packages: " + e.getMessage(), e);
        }
        return packages;
    }

    public Set<NpmInstalledPackage> getListPackages() throws ProcessWrapperException, IOException, InterruptedException {
        Set<NpmInstalledPackage> packages = null;

        // List paths of installed dependencies
        ProcessWrapper pw = new ProcessWrapper();
        final Path project_path = Paths.get(this.pathToVirtualenv.toString(), this.projectName);
        pw.setCommand(this.pathToNpmExecutable, "list", "--parseable");
        pw.setWorkingDir(project_path);
        pw.setPath(this.pathToVirtualenv);
        Thread t = new Thread(pw, "npm-list");
        t.start();
        t.join();

        packages = this.parseNpmListParseableOutput(pw.getOutFile());

        return packages;
    }

    private Set<NpmInstalledPackage> parseNpmListParseableOutput(Path _file) throws IOException {
        final Set<NpmInstalledPackage> set = new HashSet<>();

        // Read the list of installed dependencies/project paths
        final String file = FileUtil.readFile(_file);
        String[] pack_path_list = file.split("\n");

        // Read package.json for each package in node_modules
        for(String pack_path: pack_path_list) {

            // Get package information from package.json
            final Path pack_json_file = Paths.get(pack_path, "package.json");
            final JsonObject pack_json = new Gson().fromJson(FileUtil.readFile(pack_json_file), JsonObject.class);

            final Map<String, String> pack_props = new HashMap<>();

            final String pack_name = pack_json.get("name").getAsString();
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
                        required_by_list.add(dep.getAsString());
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
                        tarball_dest = Paths.get(pack_path.substring(0, pack_path.lastIndexOf(File.separator)), pack_name + ".tgz").toFile();

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