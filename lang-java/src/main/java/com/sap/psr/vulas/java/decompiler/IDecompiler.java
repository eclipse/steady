/** */
package com.sap.psr.vulas.java.decompiler;

import java.io.File;

/** IDecompiler interface. */
public interface IDecompiler {

  /**
   * decompileClassFile.
   *
   * @param _classFile - the *.class file to be decompiled containing bytecode
   * @return the corresponding decompiled _javaFile
   */
  public File decompileClassFile(File _classFile);
}
