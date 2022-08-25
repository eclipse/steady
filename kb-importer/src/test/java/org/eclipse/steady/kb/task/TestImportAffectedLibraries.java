package org.eclipse.steady.kb.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.steady.backend.BackendConnectionException;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.kb.ImportCommand;
import org.eclipse.steady.kb.TestImportCommand;
import org.eclipse.steady.kb.model.Artifact;
import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.json.model.AffectedLibrary;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.packageurl.MalformedPackageURLException;
import com.google.gson.JsonSyntaxException;

public class TestImportAffectedLibraries {
  @Test
  public void testImportAffectedLibs()
      throws MalformedPackageURLException, BackendConnectionException, JsonSyntaxException,
          IOException {
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT,
            (CoreConfiguration.ConnectType.READ_WRITE.toString()));
    ImportAffectedLibraries importAffectedLibs = new ImportAffectedLibraries();
    Vulnerability vuln =
        Metadata.getFromMetadata(
            TestImportCommand.class.getClassLoader().getResource("CVE-2011-4343").getPath());
    MockBackConnector mockBackendConnector = new MockBackConnector();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.VERBOSE_OPTION, false);
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    importAffectedLibs.execute(vuln, args, mockBackendConnector);
    ObjectMapper mapper = new ObjectMapper();
    List<AffectedLibrary> listAffectedLibUpserted =
        mapper.readValue(
            mockBackendConnector.getUploadJson(),
            mapper.getTypeFactory().constructCollectionType(List.class, AffectedLibrary.class));
    org.junit.Assert.assertEquals(listAffectedLibUpserted.size(), 3);
  }

  @Test
  public void testImportAffectedLibsOverwrite()
      throws MalformedPackageURLException, BackendConnectionException, JsonSyntaxException,
          IOException {
    VulasConfiguration.getGlobal()
        .setProperty(
            CoreConfiguration.BACKEND_CONNECT,
            (CoreConfiguration.ConnectType.READ_WRITE.toString()));
    ImportAffectedLibraries importAffectedLibs = new ImportAffectedLibraries();
    Vulnerability vuln =
        Metadata.getFromMetadata(
            TestImportCommand.class.getClassLoader().getResource("CVE-2011-4343").getPath());
    MockBackConnector mockBackendConnector = new MockBackConnector();
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, true);
    args.put(ImportCommand.VERBOSE_OPTION, false);
    importAffectedLibs.execute(vuln, args, mockBackendConnector);
    ObjectMapper mapper = new ObjectMapper();
    List<AffectedLibrary> listAffectedLibUpserted =
        mapper.readValue(
            mockBackendConnector.getUploadJson(),
            mapper.getTypeFactory().constructCollectionType(List.class, AffectedLibrary.class));
    org.junit.Assert.assertEquals(listAffectedLibUpserted.size(), 4);
  }

  @Test
  public void testImportAffectedLibsNullData()
      throws MalformedPackageURLException, BackendConnectionException, JsonSyntaxException,
          IOException {
    ImportAffectedLibraries importAffectedLibs = new ImportAffectedLibraries();
    Vulnerability vuln =
        Metadata.getFromMetadata(
            TestImportCommand.class.getClassLoader().getResource("CVE-2011-4343").getPath());
    vuln.setArtifacts(new ArrayList<Artifact>());
    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put(ImportCommand.OVERWRITE_OPTION, false);
    importAffectedLibs.execute(vuln, args, null);
  }
}
