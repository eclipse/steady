package com.sap.psr.vulas.cg.spi;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.cg.ReachabilityConfiguration;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class CallgraphConstructorFactory {

    public static ClassLoader classLoaderToFindPlugins = Thread.currentThread().getContextClassLoader();

    private static final Log log = LogFactory.getLog(CallgraphConstructorFactory.class);

    /**
     * Build a call graph constructor, searching the service registry for a service implementation that registers itself using the given string
     *
     * @param analysisFramework the framework to use, e.g., wala, soot
     * @param appContext        the application for the call graph construction
     * @return the build call graph constructor
     */
    public static ICallgraphConstructor buildCallgraphConstructor(String analysisFramework, Application appContext, boolean useURLClassloader) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        if (useURLClassloader) {
            classloader = searchInPluginFolder();
        }

        final ServiceLoader<ICallgraphConstructor> loader = ServiceLoader.load(ICallgraphConstructor.class, classloader);
        ICallgraphConstructor cgConstructor = null;

        for (ICallgraphConstructor constructor : loader) {
            if (constructor.getFramework().equals(analysisFramework)) {
                cgConstructor = constructor;
                break;
            }
        }

        if (cgConstructor != null) {
            cgConstructor.setAppContext(appContext);

        } else {

            log.error("No Callgraph Constructor found for requested framework  [" + analysisFramework + "]");
        }

        return cgConstructor;

    }

    /**
     * Searches for plugins in the configured plugin directory (only command-line interface)
     *
     * @return
     */
    private static ClassLoader searchInPluginFolder() {
        String pluginFolder = VulasConfiguration.getGlobal().getConfiguration().getString(ReachabilityConfiguration.CLI_PLUGIN_DIR);
        Path loc = Paths.get(pluginFolder);
        List<URL> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(loc)) {
            for (Path path : directoryStream) {
                URL fileUrl = path.toUri().toURL();
                if (fileUrl.getFile().endsWith(".jar")) {
                    fileNames.add(fileUrl);
                    log.debug("Found jar file [" + fileUrl.toString() + "] in service folder [" + pluginFolder + "]");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        URL[] urls = new URL[fileNames.size()];
        urls = fileNames.toArray(urls);
        URLClassLoader ucl = new URLClassLoader(urls);

        return ucl;
    }


}
