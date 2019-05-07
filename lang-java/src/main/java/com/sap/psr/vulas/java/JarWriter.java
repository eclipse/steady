package com.sap.psr.vulas.java;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.util.DigestUtil;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Utility class to extract and rewrite JAR files, offering the following possibilities to modify the JAR: Skip original manifest file entries, add new manifest file entries, add new JAR entries, replace the content of existing JAR entries (using JarEntryWriter).
 *
 */
public class JarWriter {

	private static final Log log = LogFactory.getLog(JarWriter.class);

	public final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

	/**
	 * Included in the manifest file of every JAR rewritten by Vulas.
	 */
	public static final String MANIFEST_ENTRY_VULAS_MODIF = "VULAS-modifiedAt";

	public static final String MANIFEST_ENTRY_ORIG_SHA1 = "VULAS-originalSHA1";

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private JarFile originalJar = null;

	private long originalFileSize = 0;

	private Manifest originalManifest = null;

	private String sha1 = null;

	/** Original manifest entries to be skipped when rewriting the JAR. */
	private Set<String> mfEntriesToSkip = new HashSet<String>();

	/** New manifest entries to be added when rewriting the JAR. */
	private Map<String,String> mfEntriesToAdd = new HashMap<String,String>();

	/** Appended to the file name of the rewritten JAR. */
	private String classifier = null;

	/** JarEntryWriters to be called during rewrite, in case the entry name matches the provided pattern. */
	private Map<Pattern,JarEntryWriter> callbacks = new HashMap<Pattern,JarEntryWriter>();

	/** The rewritten JAR archive (null if rewrite has not yet been called). */
	private File rewrittenFile = null;

	/** Additional files to be written in the JAR (entryname:path). */
	private Map<String,Path> additionalFiles = new HashMap<String,Path>();

	public JarWriter(Path _jar) throws IOException {
		final File file = _jar.toFile();
		this.originalJar = new JarFile(file, VulasConfiguration.getGlobal().getConfiguration().getBoolean(CoreConfiguration.VERIFY_JARS, true), JarFile.OPEN_READ);
		this.originalFileSize = file.length();
		this.originalManifest = this.originalJar.getManifest();
		if(this.originalManifest==null)
			JarWriter.log.warn("Manifest file is missing in JAR [" + this.originalJar.getName() + "]");
	}

	/**
	 * Returns the size of the original JAR file.
	 * @see #getInstrumentedFileSize()
	 * @return
	 */
	public long getFileSize() { return this.originalFileSize; }

	/**
	 * Returns the size of the instrumented JAR file or -1 if no instrumentation took place.
	 * @see #getFileSize()
	 * @return
	 */
	public long getInstrumentedFileSize() {
		if(this.rewrittenFile!=null)
			return this.rewrittenFile.length();
		else
			return -1;
	}

	/**
	 * Returns the original manifest.
	 */
	public Manifest getOriginalManifest() { return this.originalManifest; }

	/**
	 * Extract the JAR to a given directory, or to a new temporary directory if null.
	 * @param _todir
	 * @return
	 */
	public Path extract(Path _todir) throws IOException {
		// Target directory, to be returned
		Path to = _todir;
		if(to==null)
			to = java.nio.file.Files.createTempDirectory("extracted_jar_");

		// Reading and writing the JAR entry to the FS
		Path path = null;
		File dir = null;
		byte[] bytes = new byte[1024];
		int bytes_read = 0;

		// Loop all entries
		final Enumeration<JarEntry> enumeration =  this.originalJar.entries();
		JarEntry entry = null;
		while(enumeration.hasMoreElements()) {
			entry = enumeration.nextElement();
			
			// ZipSlip: Do not extract
			if(!DirUtil.isBelowDestinationPath(to, entry.getName())) {
				log.warn("Entry [" + entry + "] of archive [" + Paths.get(this.originalJar.getName()).toAbsolutePath() + "] will not be extracted, as it would be outside of destination directory");
			}
			// Extract
			else {
				path  = Paths.get(to.toString(), entry.getName());
	
				try {
					if(entry.isDirectory()) {
						if(!path.toFile().exists())
							Files.createDirectories(path);
					}
					else {
						// If the entry is a file, check whether we have already created the directory it is contained in.
						// According to the JAR spec [?], this should always be the case, but tests showed non-compliant JAR files.
						dir = path.getParent().toFile();
						if(!dir.exists()) {
							Files.createDirectories(path.getParent());
							JarWriter.log.warn(this.toString() + ": Invalid JAR file: No directory entry for file entry [" + path + "]");
						}
	
						try (final FileOutputStream fos = new FileOutputStream(path.toFile()); final InputStream is = this.originalJar.getInputStream(entry)) {
							while((bytes_read = is.read(bytes)) != -1) fos.write(bytes, 0, bytes_read);
						}
					}
				}
				catch(Exception ioe) {
					JarWriter.log.error("Error while extracting JAR entry [" + entry.getName() + "]: " + ioe.getMessage(), ioe);
				}
			}
		}

		// Return the path to which the JAR was extracted
		JarWriter.log.info("Extracted [" + this.getOriginalJarFileName() + "] to [" + to + "]");
		return to;
	}

	/**
	 * Returns the SHA1 digest of the JAR. Either taken from the manifest (entry VULAS-originalSHA1, in case the original JAR has been instrumented
	 * offline), or by computing it on the fly.  
	 * @return the SHA1 digest of the JAR
	 */
	public synchronized String getSHA1() {
		if(this.sha1==null) {
			if(this.originalManifest!=null && this.originalManifest.getMainAttributes().getValue(MANIFEST_ENTRY_ORIG_SHA1)!=null) {
				this.sha1 = this.originalManifest.getMainAttributes().getValue(MANIFEST_ENTRY_ORIG_SHA1);
			}
			else {
				this.sha1 = FileUtil.getSHA1(new File(this.originalJar.getName()));
			}
		}
		return this.sha1;
	}

	/**
	 * Entries of the original manifest which will not be rewritten.
	 * Must be called before "rewrite".
	 * @param _entry
	 */
	public void skipManifestEntry(String _entry) { this.mfEntriesToSkip.add(_entry); }

	/**
	 * Additional manifest file entries to be included in re-written archives.
	 * Must be called before "rewrite".
	 * @param _key
	 * @param _val
	 */
	public void addManifestEntry(String _key, String _val) { this.mfEntriesToAdd.put(_key,  _val); }

	/**
	 * Returns true if the given manifest file entry exists, false otherwise. 
	 * @param _key
	 * @return
	 */
	public boolean hasManifestEntry(String _key) {
		//log.info(this.originalManifest.getMainAttributes().keySet());
		
		if(this.originalManifest!=null) {
			for(Object key: this.originalManifest.getMainAttributes().keySet()) {
				if(_key.equals(key.toString()))
					return true;
			}
		}
		return false;
		
		// Does not work
		//return this.originalManifest.getMainAttributes().get(_key)!=null;//containsKey(_key);		
	}

	/**
	 * Returns true if the JAR has been rewritten by Vulas. Implemented by checking manifest file entries.
	 * @return
	 */
	public boolean isRewrittenByVulas() {
		// Somehow the containsKey does not work, use getValue instead
		//		final boolean modif = this.originalManifest.getMainAttributes().containsKey(JarWriter.MANIFEST_ENTRY_VULAS_MODIF);
		//		final boolean sha1  = this.originalManifest.getMainAttributes().containsKey(JarWriter.MANIFEST_ENTRY_ORIG_SHA1);
		final boolean modif = this.originalManifest.getMainAttributes().getValue(JarWriter.MANIFEST_ENTRY_VULAS_MODIF)!=null;
		final boolean sha1  = this.originalManifest.getMainAttributes().getValue(JarWriter.MANIFEST_ENTRY_ORIG_SHA1)!=null;
		return  modif && sha1;
	}

	/**
	 * Will be appended to the file name of re-written archives (if any).
	 * Must be called before "rewrite".
	 */
	public void setClassifier(String _string) {
		this.classifier = _string;
	}

	/**
	 * Register a JarEntryWriter for a given pattern. 
	 */
	public void register(String _regex, JarEntryWriter _writer) {
		this.callbacks.put(Pattern.compile(_regex), _writer);
	}

	/**
	 * @return
	 */
	private Manifest createModifiedManifest() {
		final Manifest m = new Manifest();
		final Attributes atts = m.getMainAttributes();

		// Put all the main attributes of the original JAR
		if(this.originalManifest!=null) {
			for(Object key: this.originalManifest.getMainAttributes().keySet()) {
				// Unless it is one to be skipped
				if(!this.mfEntriesToSkip.contains(key.toString()))
					atts.putValue(key.toString(), this.originalManifest.getMainAttributes().getValue(key.toString()));
			}
		}

		// Put all the new entries
		for(String key: this.mfEntriesToAdd.keySet()) {
			atts.putValue(key, this.mfEntriesToAdd.get(key));
		}

		// Add vulas-specific ones
		atts.putValue(JarWriter.MANIFEST_ENTRY_VULAS_MODIF, dateFormat.format(new Date(System.currentTimeMillis())));
		atts.putValue(JarWriter.MANIFEST_ENTRY_ORIG_SHA1, this.getSHA1());

		return m;
	}

	/**
	 * Returns the file name of the original JAR file.
	 */
	public Path getOriginalJarFileName() {
		final Path complete_path = Paths.get(this.originalJar.getName());
		return complete_path.getFileName(); 
	}

	/**
	 * Returns the file name of the to-be-rewritten JAR.
	 * @return
	 */
	public Path getRewriteJarFileName() {
		final Path complete_path = Paths.get(this.originalJar.getName());
		Path path = null;

		// Classifier exists, i.e., change file name
		if(this.classifier!=null) {
			final String original_filename = this.getOriginalJarFileName().toString();
			String new_filename = null;
			final int idx = original_filename.lastIndexOf('.');
			if(idx!=-1)
				new_filename = original_filename.substring(0, idx) + "-" + this.classifier + original_filename.substring(idx);
			else
				new_filename = original_filename + "-" + this.classifier;
			path = Paths.get(new_filename);
		}
		// Use original file name
		else {
			path = complete_path.getFileName(); 
		}

		return path;
	}

	public void addFiles(String _target_dir, Set<Path> _paths, boolean _overwrite) {
		for(Path p: _paths)
			this.addFile(_target_dir, p, _overwrite);
	}

	public void addFile(String _target_dir, Path _path, boolean _overwrite) {
		String entry_name = null;
		if(_target_dir==null)
			entry_name = _path.getFileName().toString();
		else
			entry_name = _target_dir + (_target_dir.equals("") || _target_dir.endsWith("/") ? "" : "/")  + _path.getFileName();

		if(!this.hasEntry(entry_name) || _overwrite)
			this.additionalFiles.put(entry_name, _path);	
	}

	public boolean hasEntry(String _entry_name) {
		boolean has = false;
		final Enumeration<JarEntry> en =  this.originalJar.entries();
		JarEntry entry = null;
		while(en.hasMoreElements()) {
			entry = en.nextElement();
			if(entry.getName().equals(_entry_name)) {
				has = true;
				break;
			}
		}
		return has;
	}

	/**
	 * The rewritten JAR file. Must be called after "rewrite". If rewrite is called multiple times, this method
	 * only returns the last rewritten JAR file.
	 * @return
	 */
	public File getRewrittenJarFile() {
		return this.rewrittenFile;
	}

	/**
	 * Rewrites the JAR into the directory specified by argument _todir. If _todir is equal to null,
	 * the JAR will be rewritten to a temporary directory. If the target JAR already exists, it will
	 * not be written (see {@link JarWriter#getRewriteJarFileName()}).
	 * See here: http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
	 * @throws JarAnalysisException
	 */
	public Path rewrite(Path _todir) throws JarAnalysisException {
		// Target dir
		Path dir = _todir;

		// Callback logic
		Matcher matcher = null;

		// Loop all entries of the old JAR
		JarEntry old_entry = null, new_entry = null;

		try {
			if(dir==null)
				dir = java.nio.file.Files.createTempDirectory("rewritten_jar_");
			this.rewrittenFile = Paths.get(dir.toString(), this.getRewriteJarFileName().toString()).toFile();

			if(this.rewrittenFile.exists()) {
				JarWriter.log.info("The target [" + this.rewrittenFile + "] already exists, skip rewriting");
			}
			else {
				final FileOutputStream fos = new FileOutputStream(this.rewrittenFile);
				final JarOutputStream jos  = new JarOutputStream(fos, this.createModifiedManifest());
				InputStream is = null;
				byte[] bytes = new byte[1024];
				int bytes_read = 0;

				final Enumeration<JarEntry> en =  this.originalJar.entries();
				String class_name = null;
				JavaId jid = null;
				Set<JavaId> jids = new HashSet<JavaId>();

				// Remember all JAR entries written to the new JAR, so that we do not create duplicate entries
				// Example of a duplicate entry: Location.class in xmlbeans-2.6.0.jar 
				Set<String> written_jar_entries = new HashSet<String>();

				while(en.hasMoreElements()) {
					old_entry = en.nextElement();

					// The input stream used for writing the entries
					is = null;

					// Check whether we already write an entry with this name
					if(written_jar_entries.contains(old_entry.getName()))
						continue;

					// Ignore the original manifest (we built a new one with certain vulas-specific attributes)
					if(old_entry.getName().equals("META-INF/MANIFEST.MF"))
						continue;

					// Ignore signature related files (-> http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html#Signed_JAR_File)
					if(old_entry.getName().startsWith("META-INF/") && ( old_entry.getName().toLowerCase().endsWith(".sf") ||
							old_entry.getName().toLowerCase().endsWith(".dsa") ||
							old_entry.getName().toLowerCase().endsWith(".rsa") ) )
						continue;

					// Loop registered JarEntryWriters to see if any matches (take the input stream from the first match)
					for(Pattern pattern: this.callbacks.keySet()) {
						matcher = pattern.matcher(old_entry.getName());
						if(matcher.matches()) {
							is = this.callbacks.get(pattern).getInputStream(pattern.toString(), old_entry);
						}
					}

					// If null, take the original file
					if(is==null)
						is = this.originalJar.getInputStream(old_entry);

					// Debug information regarding specific attributes
					if(old_entry.getAttributes()!=null)
						JarWriter.log.debug(this.toString() + ": Entry [" +  old_entry.getName() + "] has specific attributes");

					// Write the entry to the modified JAR
					new_entry = new JarEntry(old_entry.getName());
					jos.putNextEntry(new_entry);
					while((bytes_read = is.read(bytes)) != -1)
						jos.write(bytes, 0, bytes_read);

					//is.close();
					jos.closeEntry();

					// Remember we wrote it
					written_jar_entries.add(new_entry.getName());
				}

				// Add additional files
				for(String key: this.additionalFiles.keySet()) {
					if(this.additionalFiles.get(key).toFile().exists()) {
						new_entry = new JarEntry(key);
						jos.putNextEntry(new_entry);
						is = new FileInputStream(this.additionalFiles.get(key).toFile());
						while((bytes_read = is.read(bytes)) != -1)
							jos.write(bytes, 0, bytes_read);
						is.close();
						jos.closeEntry();
					}
				}

				jos.flush();
				jos.close();
				this.originalJar.close();

				// 
				old_entry = null;
				JarWriter.log.info("[" + this.originalJar.getName() + "] rewritten to [" + this.rewrittenFile + "]");
			}
		}
		catch(Exception ioe) {
			if(old_entry!=null)
				throw new JarAnalysisException("Error while writing JAR entry [" + old_entry.getName() + "] to modified JAR [" + this.rewrittenFile + "]: " + ioe.getMessage(), ioe);
			else
				throw new JarAnalysisException("Error while writing modified JAR: " + ioe.getMessage(), ioe);
		}
		return this.rewrittenFile.toPath();
	}

	public final static void appendToClasspath(Set<Path> _classpath, Set<Path> _to_append, boolean _preprocess) {
		for(Path p: _to_append)
			appendToClasspath(_classpath, p, _preprocess);
	}

	/**
	 * Appends the given {@link Path} to the given set. In case of Java archives, it is checked whether it contains
	 * a manifest entry "Class-Path", in which case the archive is re-written to a temporary file w/o this entry.
	 * The method returns the path that has been appended, which is identical to the given path unless an archive
	 * has been rewritten.
	 * 
	 * TODO: Maybe add a parameter to specify problematic entries, rather than hardcoding "Class-Path" here.
	 * 
	 * @param _classpath
	 * @param _to_append
	 * @param _preprocess
	 * @return
	 */
	public final static Path appendToClasspath(Set<Path> _classpath, Path _to_append, boolean _preprocess) {
		Path appended_path = _to_append;
		
		// Add w/o preprocessing
		if(!_preprocess || _to_append.toFile().isDirectory()) {
			_classpath.add(_to_append);
		}
		// Add after preprocessing (if needed)
		else {
			try {
				// Create a new Jar Writer
				final JarWriter jw = new JarWriter(_to_append);

				// Class-Path manifest entry is present: Rewrite JAR and append
				if(jw.hasManifestEntry("Class-Path")) {
					jw.skipManifestEntry("Class-Path");
					jw.setClassifier(jw.getSHA1());
					appended_path = jw.rewrite(VulasConfiguration.getGlobal().getTmpDir());
					_classpath.add(appended_path);
				}
				// Entry not present: Just add to classpath
				else {
					JarWriter.log.info("Rewriting not necessary, original JAR [" + _to_append + "] appended to classpath");
					_classpath.add(_to_append);
				}
			}
			// Add original JAR in case of exception
			catch (Exception e) {
				_classpath.add(_to_append);
				JarWriter.log.error("Error while preprocessing JAR [" + _to_append + "], original JAR appended to classpath: " + e.getMessage());
			}
		}
		
		return appended_path;
	}
}
