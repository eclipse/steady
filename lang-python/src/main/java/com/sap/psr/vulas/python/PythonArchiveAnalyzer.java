package com.sap.psr.vulas.python;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.DirAnalyzer;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Library;
import com.sap.psr.vulas.shared.json.model.LibraryId;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

/**
 * Analyzer for the .egg and .whl archive (zip) formats,
 * neither are supposed to contain .pyc files.
 */
public class PythonArchiveAnalyzer implements FileAnalyzer {

	private static final Log log = LogFactory.getLog(PythonArchiveAnalyzer.class);

	Map<ConstructId, Construct> constructs = new TreeMap<ConstructId, Construct>();

	private File archive = null;
	
	private LibraryId libraryId = null;
	
	/** PythonArchiveAnalyzers to deal with nested archives. */
	private Set<FileAnalyzer> nestedAnalyzers = new HashSet<FileAnalyzer>();

	/** {@inheritDoc} */
	@Override
	public String[] getSupportedFileExtensions() {
		return new String[] { "egg", "whl", "gz" };
	}

	/** {@inheritDoc} */
	@Override
	public boolean canAnalyze(File _file) {
		final String ext = FileUtil.getFileExtension(_file);
		if(ext.equals("gz") && !_file.getAbsolutePath().endsWith("tar.gz"))
			return false;
		if(ext == null || ext.equals(""))
			return false;
		for(String supported_ext: this.getSupportedFileExtensions()) {
			if(supported_ext.equalsIgnoreCase(ext))
				return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void analyze(final File _file) {
		this.archive = _file;
	}
	
	/**
	 * <p>getArchivePath.</p>
	 *
	 * @return a {@link java.nio.file.Path} object.
	 */
	public Path getArchivePath() {
		return this.archive!=null ? this.archive.toPath() : null;
	}
	
	/**
	 * <p>getDigest.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDigest() {
		return this.archive!=null ? FileUtil.getDigest(this.archive, DigestAlgorithm.MD5) : null;
	}
	
	private InputStream getArchiveInputStream() throws IOException {
		//final String ext = FileUtil.getFileExtension(this.archive);
		InputStream is = null;
		try {			
			if(FileUtil.isZipped(this.archive)) {
				is = new ZipInputStream(new FileInputStream(this.archive));
			} else {
				final GzipCompressorInputStream gzis = new GzipCompressorInputStream(new FileInputStream(this.archive));
				is =  new TarArchiveInputStream(gzis);
			}
			
		} catch (FileNotFoundException e) {
			log.error("Cannot find Pyhton archive to analyze ["+ this.archive.getAbsolutePath() +"]", e);
		}
		return is;
	}
	
	private String getModuleName(String _name){
		if(_name.lastIndexOf("/")>-1) {
			String module_name_with_ext = _name.substring(_name.lastIndexOf("/") + 1, _name.length());
			return module_name_with_ext.substring(0, module_name_with_ext.indexOf("."));
		}
		else{
			return _name.substring(0, _name.indexOf("."));
		}
	}
	
	private List<String> getPackageName(String _name, List<String> _inits){
		final List<String> package_name = new ArrayList<String>();
		if(_name.lastIndexOf("/")>-1){
			String packToFind = _name.substring(0, _name.lastIndexOf("/"));
			
			while(_inits.contains(packToFind.concat("/__init__.py"))){
				if(packToFind.lastIndexOf("/")>-1){
					package_name.add(0,packToFind.substring(packToFind.lastIndexOf("/")+1, packToFind.length()));
					packToFind = packToFind.substring(0,packToFind.lastIndexOf("/"));	
				}
				else{
					package_name.add(0,packToFind);
					break;
				}
			}
		}
		return package_name;
	}
	
	private Boolean isPackage(String _en){
        return _en.endsWith("__init__.py");
	}

	/** {@inheritDoc} */
	@Override
	public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
		if(this.constructs.isEmpty()) {
			
			// list of __init__.py files (to become packages)
			final List<String> inits = new ArrayList<String>();
			boolean pre_processed = false;
			try(InputStream is = this.getArchiveInputStream()) {
				
				// Read archive to look for __init__.py
				if(is instanceof ZipInputStream){
					ZipEntry en=null;
					while ((en=((ZipInputStream)is).getNextEntry())!=null){
						//loop over the archive to understand which files has an __init__.py
						if (!en.isDirectory()) {
				            if (this.isPackage(en.getName())) {
				                inits.add(en.getName());
				            }
				        }
					}
				}
				else if(is instanceof TarArchiveInputStream) {
					ArchiveEntry en=null;
					while ((en=((TarArchiveInputStream)is).getNextEntry())!=null){
						// Loop over the archive to understand which files has an __init__.py
						if (!en.isDirectory()) {
				            if (this.isPackage(en.getName())) {
				                inits.add(en.getName());
				            }
				        }
					}
				}
				pre_processed = true;
			} catch (IOException e) {
				log.error("IOException analyzing Python archive ["+ this.archive.getAbsolutePath() +"]", e);
			}			
						
			// Re-read archive to create constructs
			try(InputStream is = this.getArchiveInputStream()) {
			
				if(is instanceof ZipInputStream && pre_processed) {
					ZipEntry en = null;
					while ((en=((ZipInputStream)is).getNextEntry())!=null){
				
						if(!en.isDirectory()){
							//check if it is a tar (wheel egg), then extract and analyze it (here or where??) !!!
							if(en.getName().endsWith(".whl") || en.getName().endsWith(".egg")|| en.getName().endsWith(".gz")) {
								final FileAnalyzer fa = DirAnalyzer.createAnalyzerForArchiveEntry(is, en.getName());
								if(fa!=null)
									this.nestedAnalyzers.add(fa);
							}
							else if (en.getName().endsWith(".py")) {

								// Create the package (if any)
								final List<String> package_name = this.getPackageName(en.getName(), inits);
								PythonId pack = null;
								if(!package_name.isEmpty())
									pack = new PythonId(null, PythonId.Type.PACKAGE, StringUtil.join(package_name, "."));
								
								// Create module
								final String module_name = this.getModuleName(en.getName());								
								final PythonId module =  new PythonId(pack, PythonId.Type.MODULE, module_name);
																
								// We put everything into a byte[] to avoid that the stream gets closed when read by antlr
								final byte[] py_bytes = FileUtil.readInputStream(is);
																
								final FileAnalyzer fa = PythonFileAnalyzer.createAnalyzer(new ByteArrayInputStream(py_bytes));
								if(fa instanceof Python3FileAnalyzer) {
									((Python3FileAnalyzer)fa).setContext(module, pack);
									this.constructs.putAll(((Python3FileAnalyzer)fa).getConstructs(new ByteArrayInputStream(py_bytes)));
								}
								else if(fa instanceof Python335FileAnalyzer) {
									((Python335FileAnalyzer)fa).setContext(module, pack);
									this.constructs.putAll(((Python335FileAnalyzer)fa).getConstructs(new ByteArrayInputStream(py_bytes)));
								}
							}		
								
						}
					}
				}
				//almost duplicated code from the alternative above as we have another type for the entry (ArchiveEntry from commons compress instead of ZipEntry from java.util.zip)
				else if (is instanceof TarArchiveInputStream){
					ArchiveEntry en=null;
					while ((en=((TarArchiveInputStream)is).getNextEntry())!=null){
						if(!en.isDirectory()){
							//check if it is a tar (wheel egg), then extract and analyze it (here or where??) !!!
							if(en.getName().endsWith(".whl") || en.getName().endsWith(".egg")|| en.getName().endsWith(".gz")) {
								final FileAnalyzer fa = DirAnalyzer.createAnalyzerForArchiveEntry(is, en.getName());
								if(fa!=null)
									this.nestedAnalyzers.add(fa);
							}
							else if (en.getName().endsWith(".py")) {
								
								// Create the package (if any)
								final List<String> package_name = this.getPackageName(en.getName(), inits);
								PythonId pack = null;
								if(!package_name.isEmpty())
									pack = new PythonId(null, PythonId.Type.PACKAGE, StringUtil.join(package_name, "."));
								
								// Create module
								final String module_name = this.getModuleName(en.getName());								
								final PythonId module =  new PythonId(pack, PythonId.Type.MODULE, module_name);
								
								// We put everything into a byte[] to avoid that the stream gets closed when read by antlr
								final byte[] py_bytes = FileUtil.readInputStream(is);
								
								final FileAnalyzer fa = PythonFileAnalyzer.createAnalyzer(new ByteArrayInputStream(py_bytes));
								if(fa instanceof Python3FileAnalyzer) {
									((Python3FileAnalyzer)fa).setContext(module, pack);
									this.constructs.putAll(((Python3FileAnalyzer)fa).getConstructs(new ByteArrayInputStream(py_bytes)));
								}
								else if(fa instanceof Python335FileAnalyzer) {
									((Python335FileAnalyzer)fa).setContext(module, pack);
									this.constructs.putAll(((Python335FileAnalyzer)fa).getConstructs(new ByteArrayInputStream(py_bytes)));
								}
							}		
								
						}
					}
				}
			}
			catch (IOException e) {
				log.error("IOException analyzing Python archive ["+ this.archive.getAbsolutePath() +"]", e);
			}
		}
		
		return constructs;
	}
	
	/**
	 * Sets the Maven Id for the JAR to be analyzed. The Maven ID is already known in some contexts (e.g., during Maven plugin execution).
	 *
	 * @param _id a {@link com.sap.psr.vulas.shared.json.model.LibraryId} object.
	 */
	public void setLibraryId(LibraryId _id) { this.libraryId = _id; }
	
	/**
	 * Returns a {@link Library} representing the analyzed Java archive.
	 *
	 * @throws com.sap.psr.vulas.FileAnalysisException
	 * @return a {@link com.sap.psr.vulas.shared.json.model.Library} object.
	 */
	public Library getLibrary() throws FileAnalysisException {
		final Library lib = new Library();
		
		if(this.getDigest()!=null) {
			lib.setDigest(this.getDigest());
			lib.setDigestAlgorithm(DigestAlgorithm.MD5);
		}
		
		lib.setConstructs(this.getSharedConstructs());

		// No properties are set

		return lib;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
		return this.constructs.containsKey(_id);
	}

	/** {@inheritDoc} */
	@Override
	public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
		return this.constructs.get(_id);
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean hasChilds() {
		return this.nestedAnalyzers!=null && !this.nestedAnalyzers.isEmpty();
	}
	
	/** {@inheritDoc} */
	@Override
	public Set<FileAnalyzer> getChilds(boolean _recursive) {
		final Set<FileAnalyzer> nested_fa = new HashSet<FileAnalyzer>();
		if(!_recursive) {
			nested_fa.addAll(this.nestedAnalyzers);
		}
		else {
			for(FileAnalyzer fa: this.nestedAnalyzers) {
				nested_fa.add(fa);
				final Set<FileAnalyzer> nfas = fa.getChilds(true);
				if(nfas!=null && !nfas.isEmpty())
					nested_fa.addAll(nfas);
			}
		}
		return nested_fa;
	}

	/*private static File unTarGz(final File _gz, File _out_dir)  {
 		if(_gz.isDirectory())
			return _gz;

		if(!_gz.toString().endsWith("tar.gz"))
			throw new IllegalArgumentException("File [" + _gz + "] does not end with tar.gz");

		try {
			TarArchiveInputStream zis = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(_gz))));
		
			TarArchiveEntry entry = null;
			while((entry=zis.getNextTarEntry())!= null) {
				
				// ZipSlip: Do not extract
				if(!DirUtil.isBelowDestinationPath(_out_dir.toPath(), entry.getName())) {
					log.warn("Entry [" + entry + "] of archive [" + _gz + "] will not be extracted, as it would be outside of destination directory");
				}
				// Extract
				else {
					final File file = new File(_out_dir, entry.getName());
					if (entry.isDirectory()) {
						file.mkdirs();
					}
					else {
						// Create parent if not existing
						if (!file.getParentFile().exists())
							file.getParentFile().mkdirs();
		
						// Extract file
						final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
						final byte[] bytes = new byte[1024];				
						int len = 0;
						while((len = zis.read(bytes)) != -1)
							bos.write(bytes,0,len);
						bos.close();
					}
				}
			}
			zis.close();
		} catch (FileNotFoundException e) {
			log.error("Error extracting tar.gz file ["+_gz+"] not found : " + e);
		} catch (IOException e) {
			log.error("Error extracting tar.gz file ["+_gz+"] : " + e);
		}
		return _out_dir;
	}*/
	
	/**
	 * <p>getSharedConstructs.</p>
	 *
	 * @return a {@link java.util.List} object.
	 * @throws com.sap.psr.vulas.FileAnalysisException if any.
	 */
	public List<com.sap.psr.vulas.shared.json.model.ConstructId> getSharedConstructs() throws FileAnalysisException {
		List<com.sap.psr.vulas.shared.json.model.ConstructId> l= new ArrayList<com.sap.psr.vulas.shared.json.model.ConstructId>();
			for(ConstructId c: this.getConstructs().keySet()) {
				l.add(new com.sap.psr.vulas.shared.json.model.ConstructId(ProgrammingLanguage.PY, c.getSharedType(),c.getQualifiedName()));
			}
		return l;
	}
}
