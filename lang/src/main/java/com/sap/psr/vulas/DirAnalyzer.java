package com.sap.psr.vulas;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;


/**
 * Analyzes all files below a given directory, thereby using other implementations of {@link FileAnalyzer}.
 */
public class DirAnalyzer implements FileAnalyzer {

	private static final Log log = LogFactory.getLog(DirAnalyzer.class);

	/** The dir to be analyzed. */
	private File dir = null;

	/** All Java constructs found in the given class file. */
	private Map<ConstructId, Construct> constructs = null;

	private Set<FileAnalyzer> analyzers = new HashSet<FileAnalyzer>();
	
	private String[] extensionFilter = null;
	
	public void setExtensionFilter(String[] _exts) {
		this.extensionFilter = _exts;
	}
	
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] {};
	}
	
	@Override
	public boolean canAnalyze(File _file) {
		return FileUtil.isAccessibleDirectory(_file);
	}

	@Override
	public void analyze(final File _file) throws FileAnalysisException {
		if(!FileUtil.isAccessibleDirectory(_file))
			throw new IllegalArgumentException("Expected a directory but got [" + _file + "]");
		this.dir = _file;

		// Statistics
		final Map<String, Integer> count_ext = new HashMap<String, Integer>();
		int total = 0, err = 0;

		// Search for files
		Set<Path> files = null;		
		if(this.extensionFilter!=null)
			files = new FileSearch(this.extensionFilter).search(this.dir.toPath().normalize());
		else
			files = new FileSearch(FileAnalyzerFactory.getSupportedFileExtensions()).search(this.dir.toPath().normalize());
		
		// Create corresponding file analyzers
		FileAnalyzer fa = null;
		for(Path file: files) {
			try {
				// Stats
				total++;
				final String ext = FileUtil.getFileExtension(file.toFile());

				// Increment ext counter
				Integer curr = count_ext.get(ext);
				if(curr==null) curr = Integer.valueOf(0);
				count_ext.put(ext, Integer.valueOf(curr+1));
				// Build the analyzer
				fa = FileAnalyzerFactory.buildFileAnalyzer(file.toFile());
				if(fa!=null)
					this.analyzers.add(fa);
			} catch (RuntimeException e) {
				err++;
				DirAnalyzer.log.error("Error while analyzing file [" + file.toAbsolutePath() + "]: " + e, e);
			}
		}
		final StringBuffer b = new StringBuffer();
		b.append("File analyzers created: [" + total + " total, " + err + " with error]");
		for(Map.Entry<String, Integer> entry: count_ext.entrySet())
			b.append(", [" + entry.getValue() + " " + entry.getKey()+ "]");
		DirAnalyzer.log.info(b.toString());
	}

	/**
	 * Returns the union of constructs of all {@link FileAnalyzer}s created when searching recursivly in the directory.
	 */
	@Override
	public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
		if(this.constructs==null) {
			this.constructs = new TreeMap<ConstructId, Construct>();
			for(FileAnalyzer fa: this.analyzers) {
				try {
					this.constructs.putAll(fa.getConstructs());
				}
				catch(RuntimeException e) {
					DirAnalyzer.log.error("Error getting constructs from [" + fa + "]: " + e, e);
				}
			}			
			DirAnalyzer.log.info("Constructs found: [" + this.constructs.size() + "]");
		}
		return this.constructs;
	}
	
	@Override
	public boolean containsConstruct(ConstructId _id) throws FileAnalysisException { return this.getConstructs().containsKey(_id); }

	@Override
	public Construct getConstruct(ConstructId _id) throws FileAnalysisException { return this.getConstructs().get(_id); }
	
	@Override
	public boolean hasChilds() {
		return this.analyzers!=null && !this.analyzers.isEmpty();
	}
	
	@Override
	public Set<FileAnalyzer> getChilds(boolean _recursive) {
		final Set<FileAnalyzer> nested_fa = new HashSet<FileAnalyzer>();
		if(!_recursive) {
			nested_fa.addAll(this.analyzers);
		}
		else {
			for(FileAnalyzer fa: this.analyzers) {
				nested_fa.add(fa);
				final Set<FileAnalyzer> nfas = fa.getChilds(true);
				if(nfas!=null && !nfas.isEmpty())
					nested_fa.addAll(nfas);
			}
		}
		return nested_fa;
	}
	
	/**
	 * The given {@link InputStream} has been created from an archive entry with the given name.
	 * The entry is extracted below the temporary directory, and a {@link FileAnalyzer} is created for it.
	 * 
	 * @param _is
	 * @param _entry
	 * @return
	 */
	public static synchronized FileAnalyzer createAnalyzerForArchiveEntry(InputStream _is, String _entry) {
		final Path tmp_dir = VulasConfiguration.getGlobal().getTmpDir();
		FileAnalyzer fa = null;
		
		// ZipSlip: Do not extract
		if(!DirUtil.isBelowDestinationPath(tmp_dir, _entry)) {
			log.warn("Entry [" + _entry + "] will not be extracted, as it would be outside of destination directory");
		}
		// Extract to temp file and create nested PythonArchiveAnalyzer
		else {
			final File file = new File(tmp_dir.toFile(), _entry);
			if(file.exists()) {
				log.info("Exists already: Entry [" + _entry + "] corresponds to [" + file.toPath().toAbsolutePath() + "]");
				fa = FileAnalyzerFactory.buildFileAnalyzer(file);
			} else {
				// Create parent if not existing
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				
				try(final FileOutputStream fos = new FileOutputStream(file)) {
					final InputStream is2 = new BufferedInputStream(_is);
					int cc = -1;
					while ((cc = is2.read()) >= 0) fos.write(cc);
					fos.flush();
					log.info("Extracted entry [" + _entry + "] to [" + file.toPath().toAbsolutePath() + "]");
					fa = FileAnalyzerFactory.buildFileAnalyzer(file);
				}
				catch(IOException ioe) {
					log.error("Error when extracting entry to [" + file.toPath().toAbsolutePath() + "]: " + ioe.getMessage());
				}
			}
		}
		
		return fa;
	}
}