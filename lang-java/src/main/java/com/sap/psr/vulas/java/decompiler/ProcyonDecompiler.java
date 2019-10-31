package com.sap.psr.vulas.java.decompiler;

import com.sap.psr.vulas.shared.util.FileUtil;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** ProcyonDecompiler class. */
public class ProcyonDecompiler implements IDecompiler {

  private static final Log log = LogFactory.getLog(ProcyonDecompiler.class);

  /** {@inheritDoc} */
  @Override
  public File decompileClassFile(File inputClassFile) {

    // Default settings for the decompilers
    final DecompilerSettings settings = new DecompilerSettings();
    settings.setShowSyntheticMembers(true);
    settings.setSimplifyMemberReferences(true);
    settings.setExcludeNestedTypes(true);

    String classFilePath = inputClassFile.getPath();
    String fileNameWithOutExt = FilenameUtils.removeExtension(inputClassFile.getName());
    File outFile = new File(inputClassFile.getParent(), fileNameWithOutExt + ".java");

    try {

      final FileOutputStream stream = new FileOutputStream(outFile.toString());
      final OutputStreamWriter writer = new OutputStreamWriter(stream, FileUtil.getCharset());

      try {
        Decompiler.decompile(classFilePath, new PlainTextOutput(writer), settings);
      } finally {
        writer.close();
        stream.close();
      }
    } catch (final IOException e) {
      log.debug(e.getMessage());
    }

    return outFile;
  }
}
