package com.sap.psr.vulas.mvn;

import java.io.Serializable;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import com.sap.psr.vulas.goals.CheckverGoal;


/**
 * This Mojo ...
 *
 * The plugin can be invoked as follows:
 * 1) From Eclipse: Project > 'Run As' > 'Maven build...' > Goal 'vulas:version-check'.
 * 2) From command line: mvn vulas:version-check(if the plugin is configured in the pom.xml, or the pluginGroup has been setup in settings.xml)
 * 3) From command line: mvn com.sap.research.security.vulas:vulas-maven-plugin:version-check
 */
@Mojo( name = "checkver", defaultPhase = LifecyclePhase.TEST, requiresDependencyResolution = ResolutionScope.TEST, requiresOnline = true )
public class MvnPluginCheckver extends AbstractVulasMojo implements Serializable {

	// Not needed anymore, replaced by CheckverGoal.getClassLoader()
	/*private URLClassLoader getClassLoader() {
		final List<URL> urls = new ArrayList<URL>();
		for(Artifact a: this.project.getArtifacts()) {
			try {
				urls.add(a.getFile().toURL());
			} catch (MalformedURLException e) {
				getLog().error("No URL for dependency [" + a + "]");
			}
		}
		return new URLClassLoader(urls.toArray(new URL[urls.size()]));
	}/*


	/*private final Artifact getDependency(String _filename, MavenId _gav) {
		MavenId dep = null;
		Artifact artifact = null;
		for(Artifact a: this.project.getArtifacts()) {

			if(_gav!=null) {
				// Need to be constructed in the same manner than in vulas:app
				dep = new MavenId(a.getGroupId(), a.getArtifactId(), a.getVersion(), a.getClassifier(), null);
				if(dep.equals(_gav) && a.getFile().toString().endsWith(_filename)) {
					artifact = a;
					break;
				}
			}
			else {
				if(a.getFile().toString().endsWith(_filename)) {
					artifact = a;
					break;
				}
			}
		}
		return artifact;
	}*/

	/** {@inheritDoc} */
	@Override
	protected void createGoal() {
		this.goal = new CheckverGoal();
	}
	
	/** {@inheritDoc} */
	@Override
	protected void executeGoal() throws Exception {
		/*final CheckverGoal goal = new CheckverGoal(app);
		goal.addAppPaths();
		goal.addDepPaths();
		goal.execute();
		
		final SignatureAnalysis signatureAnalysis = SignatureAnalysis.getInstance();
		signatureAnalysis.setUrlClassLoader(this.getClassLoader());
		signatureAnalysis.setAllArtifacts(this.project.getArtifacts());
		signatureAnalysis.setIsCli(false);
		signatureAnalysis.setApp(app);
		signatureAnalysis.setBug(VulasConfiguration.getSingleton().getConfiguration().getString(CoreConfiguration.SIGN_BUGS, null));
		signatureAnalysis.execute();*/
	}
}
