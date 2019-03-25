package com.sap.psr.vulas;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.FileUtil;

/**
 * Creates instances of {@link FileAnalyzer}.
 */
public class FileAnalyzerFactory {

	private static final Log log = LogFactory.getLog(FileAnalyzerFactory.class);

	private static String[] supportedFileExtensions = null;

	/**
	 * Loops over all {@link FileAnalyzer}s and returns an array of all supported file extensions.
	 * @return
	 */
	public static synchronized String[] getSupportedFileExtensions() {
		if(supportedFileExtensions==null) {
			final Set<String> exts = new HashSet<String>();
			final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
			for(FileAnalyzer l: loader) {
				final String[] ana_exts = l.getSupportedFileExtensions();
				for(String ana_ext: ana_exts) {
					if(exts.contains(ana_ext))
						log.warn("File extension [" + ana_ext + "] supported multiple times");
					else
						exts.add(ana_ext);
				}
			}
			supportedFileExtensions = new String[exts.size()];
			new ArrayList<String>(exts).toArray(supportedFileExtensions);
		}
		return supportedFileExtensions.clone();
	}
	

	public static boolean isSupportedFileExtension(String _ext) {
		return Arrays.asList(getSupportedFileExtensions()).contains(_ext);
	}

	/**
	 * Creates instances of {@link FileAnalyzer} depending on the file.
	 * @param _file
	 * @return
	 */
	public final static FileAnalyzer buildFileAnalyzer(File _file) throws IllegalArgumentException {
		return FileAnalyzerFactory.buildFileAnalyzer(_file, null);
	}

	/**
	 * Creates instances of {@link FileAnalyzer} depending on the file.
	 * @param _file
	 * @return
	 */
	public final static FileAnalyzer buildFileAnalyzer(File _file, String[] _exts) throws IllegalArgumentException {
		FileAnalyzer fa = null; 

		// Check implementations of FileAnalyzer service
		if(FileUtil.isAccessibleDirectory(_file) || FileUtil.isAccessibleFile(_file.toPath())) {
			final ServiceLoader<FileAnalyzer> loader = ServiceLoader.load(FileAnalyzer.class);
			for(FileAnalyzer l: loader) {
				if(l.canAnalyze(_file)) {
					try {
						fa = l;
						
						// This is to limit the search of the DirAnalyzer to given extensions
						if(_exts!=null && fa instanceof DirAnalyzer)
							((DirAnalyzer)fa).setExtensionFilter(_exts);
						
						fa.analyze(_file);
					} catch (FileAnalysisException e) {
						log.error("Error when creating file analyzer for [" + _file.toString() + "]: " + e.getMessage(), e);
					}
				}

				if(fa!=null)
					break;
			}			
		}
		// Not accessible
		else
			throw new IllegalArgumentException("File [" + _file.toString() + "] not found or not readable");

		if(fa==null)
			log.warn("No analyzer found for file  [" + _file.toString() + "]");

		return fa;
	}
}
