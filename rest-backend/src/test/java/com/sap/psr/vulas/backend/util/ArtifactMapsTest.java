package com.sap.psr.vulas.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.connectivity.ServiceConnectionException;

public class ArtifactMapsTest {

	/**
	 * Checks whether the configuration setting ARTIFACT_MAPS contains valid JSON.
	 */
	@Test
	public void testsBuildMaps() {
		// Read the JSON as String from the configuration and deserialize it
		final ArtifactMaps maps = ArtifactMaps.buildMaps();

		// Must not be empty
		assertTrue(!maps.getMaps().isEmpty());

		// For Tomcat, there should be 2 other artifacts after tomcat:catalina
		final LibraryId tomcat = new LibraryId();
		tomcat.setMvnGroup("tomcat");
		tomcat.setArtifact("catalina");
		final List<LibraryId> gt_tomcat_catalina = maps.getGreaterArtifacts(tomcat);
		assertEquals(2, gt_tomcat_catalina.size());

		// Get latest versions of synonyms from Maven Central
		/*try {
			final Collection<LibraryId> all_libids = ServiceWrapper.getInstance().getAllArtifactVersions(tomcat.getMvnGroup(), tomcat.getArtifact(), true, null);
			for(LibraryId syn: gt_tomcat_catalina) {
				all_libids.addAll(ServiceWrapper.getInstance().getAllArtifactVersions(syn.getMvnGroup(), syn.getArtifact(), true, null));
			}
		} catch (ServiceConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
