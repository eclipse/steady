/**
 *
 */
package com.sap.psr.vulas.java.decompiler;

import java.io.File;

/**
 * <p>IDecompiler interface.</p>
 *
 */
public interface IDecompiler {

	/**
	 * <p>decompileClassFile.</p>
	 *
	 * @param _classFile - the *.class file to be decompiled containing bytecode
	 * @return the corresponding decompiled _javaFile
	 */
	public File decompileClassFile(File _classFile);
}
