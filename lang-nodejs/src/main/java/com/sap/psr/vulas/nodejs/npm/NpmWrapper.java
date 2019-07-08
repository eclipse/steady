package com.sap.psr.vulas.nodejs.npm;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.nodejs.ProcessWrapper;
import com.sap.psr.vulas.nodejs.ProcessWrapperException;
import com.sap.psr.vulas.nodejs.utils.NodejsConfiguration;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;

public class NpmWrapper {

    private final static Log log = LogFactory.getLog(NpmWrapper.class);

    private static final String PACKAGE_JSON = "package.json";

    private static final boolean IS_WIN = System.getProperty("os.name").contains("Windows");

    private Path pathToNpm = null;

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
            new_dir = null;
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
            pw.setCommand(this.pathToNpmExecutable,"--prefix", _project_path.toAbsolutePath().toString(),"install", _project_path.toAbsolutePath().toString(),"--no-audit", "--force");
            pw.setPath(this.pathToVirtualenv);
            Thread t = new Thread(pw);
            t.start();
            t.join();
//            final Path download_info =  pw.getOutFile();
            //final Path download_info = Paths.get(_project_path.toString(), "node_modules");
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
        final String project_path = Paths.get(this.pathToVirtualenv.toString(), this.projectName).toString();
        pw.setCommand(this.pathToNpmExecutable, "--prefix", project_path, "list", "--json", "--long");
        pw.setPath(this.pathToVirtualenv);
        Thread t = new Thread(pw, "npm-list");
        t.start();
        t.join();

        packages = this.parseNpmListOutput(pw.getOutFile());

        return packages;
    }

    private Set<NpmInstalledPackage> parseNpmListOutput(Path _file) throws IOException {
        // Read the object
        String file = FileUtil.readFile(_file);
        final JsonObject npm_list = new Gson().fromJson(FileUtil.readFile(_file), JsonObject.class);
        final Set<NpmInstalledPackage> set = new HashSet<NpmInstalledPackage>();
        final Queue<JsonObject> pack_queue = new LinkedList<JsonObject>();
        npm_list.addProperty("depth", 0);
        if(!npm_list.has("_resolved"))
            npm_list.addProperty("_resolved", "");
        if(!npm_list.has("_requiredBy")) {
            npm_list.addProperty("_requiredBy", "");
        }
        if(!npm_list.has("repository")) {
            JsonObject url = new JsonObject();
            url.addProperty("url", "");
            npm_list.add("repository", url);
        }
        pack_queue.add(npm_list);

        // Traverse in npm-list json object
        while(!pack_queue.isEmpty()) {
            // Get the new package object
            JsonObject pack_json = pack_queue.remove();

            // Get properties of the package
            final String pack_name = pack_json.get("name").getAsString();
            final String pack_version = pack_json.get("version").getAsString();
            final Path pack_path = Paths.get(pack_json.get("path").getAsString());
            final String pack_url = pack_json.get("_resolved").getAsString();

            final Map<String, String> pack_props = new HashMap<>();

            pack_props.put("name", pack_name);
            pack_props.put("version", pack_version);
            pack_props.put("location", pack_path.toAbsolutePath().toString());
            pack_props.put("resolved_url", pack_url);
            pack_props.put("description", safeStringGet(pack_json, "description"));
//            pack_props.put("repository", pack_json.getAsJsonObject("repository").get("url").getAsString());
//            pack_props.put("author", pack_json.get("author").isJsonArray()
//                    ? pack_json.getAsJsonObject("author").get("name").getAsString()
//                    : pack_json.get("author").getAsString());
            pack_props.put("license", safeStringGet(pack_json, "license"));
            pack_props.put("required_by", pack_json.get("_requiredBy").getAsString());
            pack_props.put("depth", pack_json.get("depth").getAsString());

            List<String> dep_names = new ArrayList<>();
            for(String dep_pack: pack_json.getAsJsonObject("dependencies").keySet()) {
                dep_names.add(pack_json.getAsJsonObject("dependencies").getAsJsonObject(dep_pack).get("name").getAsString());
            }
            pack_props.put("dependencies", String.join(",", dep_names));

            NpmInstalledPackage pack = new NpmInstalledPackage(pack_name, pack_version);
            pack.setDownloadPath(pack_path);
            pack.setDownloadUrl(pack_url);
            pack.addProperties(pack_props);

            set.add(pack);

            // Enqueue new dependencies
            for(String dep : pack_json.getAsJsonObject("dependencies").keySet()) {
                JsonObject dep_pack = pack_json.getAsJsonObject("dependencies").getAsJsonObject(dep);
                dep_pack.addProperty("depth", pack_json.get("depth").getAsInt()+1);
                pack_queue.add(dep_pack);
            }
        }

        return set;
    }

    private String safeStringGet(JsonObject _obj, String _key) {
        String result = "";
        try {
            result = _obj.get(_key).getAsString();
        } catch(NullPointerException e) {
            log.debug("NullPointerException in JsonObject [key: "+ _key + "], returning default empty string");
        }
        return result;
    }

//    /**
//	 * Helper class for deserializing the output of pip list --format json.
//     */
//    static class NpmPackageJson {
//        String name;
//        String version;
//        String installer;
//        String location;
//        public String getName() { return name; }
//        public void setName(String name) { this.name = name; }
//        public String getVersion() { return version; }
//        public void setVersion(String version) { this.version = version; }
//        public String getInstaller() { return installer; }
//        public void setInstaller(String installer) { this.installer = installer; }
//        public String getLocation() { return location; }
//        public void setLocation(String location) { this.location = location; }
//    }
}