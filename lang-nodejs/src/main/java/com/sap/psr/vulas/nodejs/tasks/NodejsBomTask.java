package com.sap.psr.vulas.nodejs.tasks;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
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
            prj_paths.addAll(search.search(p));
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
        a.addDependencies(this.toDependencies(app_npm_packs));

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
    private Set<Dependency> toDependencies(Set<NpmInstalledPackage> _packs) {
        // Get the installed package that corresponds to the project (if any)
        NpmInstalledPackage prj_package = null;
        for(NpmInstalledPackage pack: _packs) {
            if(pack.getName().equals(this.getApplication().getMvnGroup())) {
                prj_package = pack;
                break;
            }
        }

        // Create deps for npm packages
        final Set<Dependency> deps = new HashSet<Dependency>();
        for(NpmInstalledPackage pack: _packs) {
            try {
                // Do not add the project package itself as dependency
                if((prj_package == null || !prj_package.equals(pack)) && pack.getLibrary().hasValidDigest()) {
                    final Dependency dep = new Dependency();
                    dep.setLib(pack.getLibrary());
                    dep.setApp(this.getApplication());
                    final Path download_path = pack.getDownloadPath();
                    if(download_path != null) {
                        dep.setFilename(pack.getName()+"@"+pack.getVersion()+pack.getProperties().get("git_hash"));
                        dep.setPath(download_path.toString());
                    }
                    dep.setDeclared(true);
                    if(pack.getProperties().containsKey("dev"))
                        dep.setScope(Scope.TEST);
                    else
                        dep.setScope(Scope.RUNTIME);
                    dep.setTransitive(prj_package != null && prj_package.requires(pack) ? false : true);

                    deps.add(dep);
                }
            } catch(FileAnalysisException e) {
                log.error(e.getMessage(), e);
            }
        }
        return deps;
    }
}
