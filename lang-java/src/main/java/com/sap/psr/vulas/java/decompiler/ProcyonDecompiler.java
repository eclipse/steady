package com.sap.psr.vulas.java.decompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class ProcyonDecompiler implements IDecompiler {


	private static final Log log =LogFactory.getLog(ProcyonDecompiler.class);
	/**
	 *
	 * @param _classFile - the *.class file to be decompiled containing bytecode
	 * @return the corresponding decompiled _javaFile
	 */
	@Override
	public File decompileClassFile(File inputClassFile) {

				//Default settings for the decompilers
				final DecompilerSettings settings = new DecompilerSettings();
				settings.setShowSyntheticMembers(true);
				settings.setSimplifyMemberReferences(true);
				settings.setExcludeNestedTypes(true);

				String classFilePath = inputClassFile.getPath();

				String fileNameWithOutExt = FilenameUtils.removeExtension(inputClassFile.getName());

				//Output File
				File outFile = new File(inputClassFile.getParent(), fileNameWithOutExt+".java");

				try {

					 final FileOutputStream stream = new FileOutputStream(outFile.toString());

				    //try {
				        final OutputStreamWriter writer = new OutputStreamWriter(stream);

				        try {
				            Decompiler.decompile(
				            	classFilePath,
				                new PlainTextOutput(writer),
				                settings
				            );
				        }
				        finally {
				            writer.close();
				            stream.close();
				        }
				    //}
				    /*finally {
				        stream.close();
				    }*/
				}
				catch (final IOException e) {
				    log.debug(e.getMessage());
				}

				return outFile;
			}

}
