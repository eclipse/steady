package com.sap.psr.vulas.kb.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

public class ZipUtil {
  public static void unzip(String zipPath, String destPathToUnzip) {
    try (java.util.zip.ZipFile zipFile = new ZipFile(zipPath)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File(destPathToUnzip, entry.getName());
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try (InputStream in = zipFile.getInputStream(entry);
              OutputStream out = new FileOutputStream(entryDestination)) {
            IOUtils.copy(in, out);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("unzip failed " + e.getMessage());
    }
  }
}
