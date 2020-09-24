package com.sap.psr.vulas.nodejs.tasks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.goals.GoalConfigurationException;
import com.sap.psr.vulas.goals.GoalExecutionException;
import com.sap.psr.vulas.nodejs.ProcessWrapperException;
import com.sap.psr.vulas.nodejs.NodejsPackageAnalyzer;
import com.sap.psr.vulas.nodejs.npm.NpmInstalledPackage;
import com.sap.psr.vulas.nodejs.npm.NpmWrapper;
import com.sap.psr.vulas.nodejs.utils.NodejsConfiguration;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.json.model.Dependency;
import com.sap.psr.vulas.shared.util.DirWithFileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.tasks.AbstractBomTask;

public class NodejsBomTask extends AbstractBomTask {

    private static final Log log = LogFactory.getLog(NodejsBomTask.class);

    private static final String[] EXT_FILTER = new String[] {"js"};

    @Override
    public Set<ProgrammingLanguage> getLanguage() {
        return new HashSet<ProgrammingLanguage>(Arrays.asList(new ProgrammingLanguage[] { ProgrammingLanguage.JS }));
    }

    @Override
    public void configure(VulasConfiguration _cfg) throws GoalConfigurationException {
        super.configure(_cfg);
    }

    @Override
    public void execute() throws GoalExecutionException {

        // The app to be completed
        final Application a = this.getApplication();

        // 1) App dependencies
        final Set<NpmInstalledPackage> app_npm_packs = new HashSet<NpmInstalledPackage>();

        final Set<Path> prj_paths = new HashSet<Path>();
        final DirWithFileSearch search = new DirWithFileSearch("package.json");
        for(Path p : this.getSearchPath()) {
            log.info("Searching for Node.js projects in search path [" + p + "]");
            search.clear();
            Path root_p = null;
            for(Path sub_p : search.search(p)) {
                if(root_p == null)
                    root_p = sub_p;
                else if(root_p.toString().length() > sub_p.toString().length())
                    root_p = sub_p;
            }
            prj_paths.add(root_p);
        }

        log.info("Found [" + prj_paths.size() + "] Node.js projects in search path(s)");

        NpmWrapper npm;
        for(Path p: prj_paths) {
            log.info("Analyzing Node.js project in [" + p + "]");
            try {
                // Npm installation path provided: Call npm to get installed packages
                if (!this.vulasConfiguration.isEmpty(NodejsConfiguration.JS_NPM_PATH)) {
                    final String npm_path = this.vulasConfiguration.getConfiguration().getString(NodejsConfiguration.JS_NPM_PATH);
                    log.info("Determine app dependencies using [" + npm_path + "]");
                    npm = new NpmWrapper(Paths.get(npm_path), p);
                }
                // No npm installation path: Call default npm in system path
                else {
                    log.info("Determine app dependencies using [npm]");
                    npm = new NpmWrapper(p);
                }
                app_npm_packs.addAll(npm.getInstalledPackages());
            } catch (ProcessWrapperException e) {
                throw new GoalExecutionException("Error create npm wrapper: " + e.getMessage(), e);
            }
        }

        if(app_npm_packs.size() == 0)
            log.warn("No dependencies found");

        // Find a relationship between each package and construct a dependency tree
        final Map<String, NpmInstalledPackage> app_npm_map = new HashMap<>();
        final Map<String, Set<String>> dep_tree = new HashMap<>();
        for(NpmInstalledPackage pack : app_npm_packs) {
            String location = pack.getProperties().get("dep_location");
            // Map location with NpmInstalledPackage for querying
            app_npm_map.put(location, pack);

            String[] required_by = pack.getProperties().get("required_by").split(",");
            for(String parent_location: required_by) {
                Set<String> dependencies = new HashSet<>();
                if(dep_tree.containsKey(parent_location)) {
                    dependencies = dep_tree.get(parent_location);
                }
                dependencies.add(location);
                dep_tree.put(parent_location, dependencies);
            }
        }

        a.addDependencies(this.toDependencies(app_npm_map, dep_tree));

        // 2) App constructs
        final Set<ConstructId> app_constructs = new HashSet<ConstructId>();
        for(Path p: prj_paths) {
            try {
                // Make sure to not accidentally add other than Node.js constructs
                if(FileUtil.isAccessibleDirectory(p) || FileUtil.hasFileExtension(p, EXT_FILTER)) {
                    log.info("Searching for Node.js constructs in search path [" + p + "] with filter [" + StringUtil.join(EXT_FILTER, ", ") + "]");
                    final NodejsPackageAnalyzer da = new NodejsPackageAnalyzer();
                    da.analyze(p.toFile());
                    app_constructs.addAll(da.getConstructs().keySet());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        a.addConstructs(ConstructId.getSharedType(app_constructs));

        // Set the one to be returned
        this.setCompletedApplication(a);
    }

    private Set<Dependency> toDependencies(Map<String, NpmInstalledPackage> _packs, Map<String, Set<String>> _dep_tree) {
        // Init queue for dependency tree traversal and add direct dependencies as the starting package
        final Queue<String> pack_queue = new LinkedList<>();
        pack_queue.addAll(_dep_tree.get("/"));

        // Create Map of Dependency Object for querying parent
        final Map<String, Dependency> deps_map = new HashMap<>();
        final Set<String> visited = new HashSet<>();

        // Set project package
        NpmInstalledPackage prj_package = _packs.get("/");

        // BFS tree traversal
        while(!pack_queue.isEmpty()) {
            // Split the location + parent location string by ","
            String[] pack_loc_parent = pack_queue.remove().split(",");
            String pack_loc = pack_loc_parent[0];
            String pack_parent = pack_loc_parent.length > 1 ? pack_loc_parent[1] : null;

            NpmInstalledPackage pack = _packs.get(pack_loc);
            try {
                // Do not add the project package itself as dependency
                if((prj_package == null || !prj_package.equals(pack)) && pack.getLibrary().hasValidDigest() || !visited.contains(pack_loc)) {
                    // Set visited package to
                    visited.add(pack_loc);
                    if(_dep_tree.containsKey(pack_loc)) {
                        for (String next_pack : _dep_tree.get(pack_loc)) {
                            // Skip visited package
                            if (!visited.contains(next_pack))
                                // Enqueue to-be-visited package
                                // Join the location of to-be-visited package and current package location with ","
                                pack_queue.add(next_pack + "," + pack_loc);
                        }
                    }
                    final Dependency dep = new Dependency();
                    dep.setLib(pack.getLibrary());
                    dep.setApp(this.getApplication());
                    final Path download_path = pack.getDownloadPath();
                    if(download_path != null) {
                        dep.setFilename(pack.getName()+"@"+pack.getVersion()+pack.getProperties().get("git_hash"));
                        dep.setPath(download_path.toString());
                    }
                    if(pack.getProperties().containsKey("dep_location")) {
                        dep.setRelativePath(pack.getProperties().get("dep_location"));
                    }
                    dep.setDeclared(true);
                    if(pack.getProperties().containsKey("dev"))
                        dep.setScope(Scope.TEST);
                    else
                        dep.setScope(Scope.RUNTIME);
                    dep.setTransitive(prj_package != null && prj_package.requires(pack) ? false : true);
                    if(pack_parent != null)
                        dep.setParent(deps_map.get(pack_parent));
                    deps_map.put(pack_loc, dep);
                }
            } catch(FileAnalysisException e) {
                log.error(e.getMessage(), e);
            }
        }
        return new HashSet<>(deps_map.values());
    }
}
