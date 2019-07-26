package com.sap.psr.vulas.nodejs.npm;

import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.nodejs.ProcessWrapperException;
import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringList;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class NpmWrapperTest {

    @Test
    @Category(Slow.class)
    public void testCreateVirtualenv() throws IllegalArgumentException, ProcessWrapperException, FileAnalysisException {

        // Create virtualenv
        final Path project = Paths.get("src", "test", "resources", "test-helloworld");
        final NpmWrapper vew = new NpmWrapper(project);
        final Path ve_path = vew.getPathToVirtualenv();
        assertTrue(FileUtil.isAccessibleDirectory(ve_path));

        // Get packages
        final Set<NpmInstalledPackage> packages = vew.getInstalledPackages();
        assertEquals(10, packages.size());

        // Get rid of the project itself
        final Set<NpmInstalledPackage> filtered_packages = NpmInstalledPackage.filterUsingArtifact(packages, new StringList().add("test-helloworld"), false);
        assertEquals(9, filtered_packages.size());

        /*
         * TODO: Get digest from packages outside npm registry
         */
        // Get Digest for every package
        for(NpmInstalledPackage p: filtered_packages) {
            final String digest = p.getDigest();
            final DigestAlgorithm algo = p.getDigestAlgorithm();
            assertTrue(digest != null && !digest.equals(""));
            assertNotNull(algo);
        }

        // Get constructs for every packages
        for(NpmInstalledPackage p: filtered_packages) {
            final Collection<ConstructId> constructs = p.getLibrary().getConstructs();
            assertTrue(constructs != null && constructs.size() > 0);
        }
    }

    @Test
    @Category(Slow.class)
    public void testMessyDependencies() throws IllegalArgumentException, ProcessWrapperException, FileAnalysisException, IOException {
        final Path project = Paths.get("src", "test", "resources", "test-anon-constructs");
        final NpmWrapper vew = new NpmWrapper(project);

        final Set<NpmInstalledPackage> packages = vew.getInstalledPackages();
        final Set<NpmInstalledPackage> filtered_packages = NpmInstalledPackage.filterUsingArtifact(packages, new StringList().add("test-anon-constructs"), false);

        assertEquals(91, packages.size());
        assertEquals(90, filtered_packages.size());
    }

    @Test
    @Category(Slow.class)
    public void testGitDependencies() throws IllegalArgumentException, ProcessWrapperException {
        final Path project = Paths.get("src", "test", "resources", "test-non-registry-project");
        final NpmWrapper vew = new NpmWrapper(project);

        final Set<NpmInstalledPackage> packages = vew.getInstalledPackages();
        final Set<NpmInstalledPackage> filtered_packages = NpmInstalledPackage.filterUsingArtifact(packages, new StringList().add("test-non-registry-project"), false);

        assertEquals(59, packages.size());
        assertEquals(58, filtered_packages.size());

        for(NpmInstalledPackage p: filtered_packages) {
            final String digest = p.getDigest();
            final DigestAlgorithm algo = p.getDigestAlgorithm();
            assertTrue(digest != null && !digest.equals(""));
            assertNotNull(algo);
        }
    }
}