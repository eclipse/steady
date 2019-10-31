package com.sap.psr.vulas.cia.util;

import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Retrieves the source or compiled code of Java classes from Maven artifacts. */
public class ClassDownloader {

  // ------------------------ STATIC

  private static Logger log = LoggerFactory.getLogger(ClassDownloader.class);

  public static enum Format {
    JAVA,
    CLASS
  };

  private static ClassDownloader instance = null;

  /**
   * Getter for the field <code>instance</code>.
   *
   * @return a {@link com.sap.psr.vulas.cia.util.ClassDownloader} object.
   */
  public static synchronized ClassDownloader getInstance() {
    if (ClassDownloader.instance == null) ClassDownloader.instance = new ClassDownloader();
    return ClassDownloader.instance;
  }

  /**
   * getContentType.
   *
   * @param _format a {@link com.sap.psr.vulas.cia.util.ClassDownloader.Format} object.
   * @return a {@link java.lang.String} object.
   */
  public static String getContentType(Format _format) {
    if (Format.JAVA == _format) return "text/x-java-source";
    else if (Format.CLASS == _format) return "application/java-vm";
    else throw new IllegalArgumentException("Unknown format [" + _format + "]");
  }

  /**
   * toFormat.
   *
   * @param _format_string a {@link java.lang.String} object.
   * @return a {@link com.sap.psr.vulas.cia.util.ClassDownloader.Format} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public static Format toFormat(String _format_string) throws IllegalArgumentException {
    if (Format.JAVA.toString().equalsIgnoreCase(_format_string)) return Format.JAVA;
    else if (Format.CLASS.toString().equalsIgnoreCase(_format_string)) return Format.CLASS;
    else throw new IllegalArgumentException("Unknown format [" + _format_string + "]");
  }

  // ------------------------ NON-STATIC

  /**
   * getClass.
   *
   * @param mvnGroup a {@link java.lang.String} object.
   * @param artifact a {@link java.lang.String} object.
   * @param version a {@link java.lang.String} object.
   * @param _qname a {@link java.lang.String} object.
   * @param _format a {@link com.sap.psr.vulas.cia.util.ClassDownloader.Format} object.
   * @return a {@link java.nio.file.Path} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public Path getClass(
      @NotNull String mvnGroup,
      @NotNull String artifact,
      @NotNull String version,
      @NotNull String _qname,
      @NotNull Format _format)
      throws IllegalArgumentException {
    Path classfile = null;
    final String filesuffix = "." + _format.toString().toLowerCase();

    // The artifact whose JAR is to be downloaded first
    final Artifact a = new Artifact(mvnGroup, artifact, version);
    a.setClassifier((ClassDownloader.Format.JAVA == _format ? "sources" : null));
    a.setPackaging("jar");
    a.setProgrammingLanguage(ProgrammingLanguage.JAVA);

    try {
      RepositoryDispatcher r = new RepositoryDispatcher();
      final Path file = r.downloadArtifact(a);

      // JAR download was successful
      if (file != null && file.toFile().exists()) {
        JarFile archive = null;
        try {
          archive = new JarFile(file.toFile());
        } catch (ZipException ze) {
          log.error(
              "Error in opening zip file for artifact ["
                  + a.getLibId().toString()
                  + "], going to delete the existing archive.");
          // if the zip file cannot be opened, delete so that we will download it again next time
          // (perhaps it was corrupted)
          boolean deleted = file.toFile().delete();
          if (!deleted)
            log.warn(
                "Couldn't delete presumibly corrupted archive [" + file.toAbsolutePath() + "]");
          return null;
        }
        final String entry_name = _qname.replace('.', '/') + filesuffix;
        final Enumeration<JarEntry> en = archive.entries();
        JarEntry entry = null;
        while (en.hasMoreElements()) {
          entry = en.nextElement();
          if (entry.getName().equals(entry_name)) {
            break;
          }
        }

        if (entry != null) {
          classfile = Files.createTempFile(_qname, filesuffix);
          log.debug("classfile at " + classfile.toAbsolutePath());
          final InputStream ais = archive.getInputStream(entry);
          final FileOutputStream fos = new FileOutputStream(classfile.toFile());
          IOUtils.copy(ais, fos);
          fos.flush();
          fos.close();
          ais.close();
        } else {
          log.warn(
              "Artifact does not contain entry [" + entry_name + "] for class [" + _qname + "]");
        }
        archive.close();
      } else {
        log.warn("Could not download artifact " + a);
      }

    } catch (FileNotFoundException e) {
      log.warn("Could not download artifact " + a);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return classfile;
  }
}
