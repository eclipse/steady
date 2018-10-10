package com.sap.psr.vulas.mvn;


import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.shared.utils.ReaderFactory;

import java.io.File;
import java.util.*;

public class TestProjectStub
        extends MavenProjectStub {
    private final String projectPath;
    private Map/*<String, Artifact>*/pluginArtifactMap = new HashMap/*<String, Artifact>*/();

    /**
     * Default constructor
     */
    public TestProjectStub(String projectPath, String pomFile) {
        this.projectPath = projectPath;

        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;
        try {
            model = pomReader.read(ReaderFactory.newXmlReader(new File(getBasedir(), pomFile)));
            setModel(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setGroupId(model.getGroupId());
        setArtifactId(model.getArtifactId());
        setVersion(model.getVersion());
        setName(model.getName());
        setUrl(model.getUrl());
        setPackaging(model.getPackaging());


        Build build = new Build();
        build.setFinalName(model.getArtifactId());
        build.setDirectory(getBasedir() + "/target");
        build.setSourceDirectory(getBasedir() + "/src/main/java");
        build.setOutputDirectory(getBasedir() + "/target/classes");
        build.setTestSourceDirectory(getBasedir() + "/src/test/java");
        build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
        build.setPlugins(model.getBuild().getPlugins());
        build.setExtensions(model.getBuild().getExtensions());
        setBuild(build);


        List compileSourceRoots = new ArrayList();
        compileSourceRoots.add(getBasedir() + "/src/main/java");
        setCompileSourceRoots(compileSourceRoots);

        List testCompileSourceRoots = new ArrayList();
        testCompileSourceRoots.add(getBasedir() + "/src/test/java");
        setTestCompileSourceRoots(testCompileSourceRoots);



    }


    /**
     * {@inheritDoc}
     */
    public File getBasedir() {
        return new File(super.getBasedir() + this.projectPath);
    }


    @Override
    public Properties getProperties() {
        return this.getModel().getProperties();
    }

    public Map/*<String, Artifact>*/getPluginArtifactMap() {
        return pluginArtifactMap;
    }

    public void setPluginArtifactMap(Map/*<String, Artifact>*/pluginArtifactMap) {
        this.pluginArtifactMap = pluginArtifactMap;
    }
}

