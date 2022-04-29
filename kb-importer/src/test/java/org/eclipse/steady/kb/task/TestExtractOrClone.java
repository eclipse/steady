package org.eclipse.steady.kb.task;

import java.io.IOException;

import org.eclipse.steady.kb.model.Vulnerability;
import org.eclipse.steady.kb.model.Import;
import org.eclipse.steady.kb.util.Metadata;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.junit.Test;
import com.github.packageurl.MalformedPackageURLException;
import com.google.gson.JsonSyntaxException;

public class TestExtractOrClone {
  @Test
  public void testExtract() {

    Manager manager = new Manager();

    Vulnerability vuln = Metadata.getFromYaml();

    HashMap<String, Object> args = new HashMap<String, Object>();
    args.put("v", false);
    args.put(Import.OVERWRITE_OPTION, false);
    args.put(Import.DIRECTORY_OPTION, "");

    ExtractOrClone extractOrClone = new ExtractOrClone(manager, vuln, dir)
    extractOrClone.execute();

    // TODO : check extracted files

  }

}