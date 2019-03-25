package com.sap.psr.vulas.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;

/**
 * Helper methods for checking the presence of files and directories, and for dealing with Java class path.
 * See VULAS-204 in case we run into problems related to special characters in paths.
 */
public class FileUtil {

	private static final Log log = LogFactory.getLog(FileUtil.class);

	/**
	 * Returns the file extension of the given {@link File} or null if the file does not have an extension.
	 * @param the file whose extension is to be returned
	 * @return the file extension of the given file (converted to lower case)
	 * @throws IllegalArgumentException if the file argument is null or the file does not exist
	 */
	public static String getFileExtension(File _file) throws IllegalArgumentException {
		String ext = null;
		if(_file==null || !_file.exists())
			throw new IllegalArgumentException("File [" + _file + "] not found");

		final int ext_index = _file.getAbsolutePath().lastIndexOf(".");
		if(ext_index!=-1)
			ext = _file.getAbsolutePath().substring(ext_index+1).toLowerCase();

		return ext;
	}

	/**
	 * Checks whether the given {@link Path} points to a file that has one of the given file extensions.
	 * @param _p
	 * @param _extensions
	 * @return
	 */
	public static boolean hasFileExtension(Path _p, String[]  _extensions) {
		if(_p.toFile().isFile()) {
			final String ext = FileUtil.getFileExtension(_p.toFile());
			if(ext!=null) {
				for(int i=0; i<_extensions.length; i++) {
					if(ext.equalsIgnoreCase(_extensions[i])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static String toString(Set<Path> _paths, String _sep) {
		final StringBuilder b = new StringBuilder();
		int i = 0;
		for(Path p: _paths) {
			if(i++>0) b.append(_sep);
			b.append(p.toString());
		}
		return b.toString();
	}

	/** 
	 * Creates a new temporary directory with the given prefix inside the directory returned by {@link #getVulasTmpDir()}.  
	 * @return
	 * @throws IOException
	 */
	public static Path createTmpDir(String _prefix) throws IOException {
		return Files.createTempDirectory(VulasConfiguration.getGlobal().getTmpDir(), _prefix);
	}

	public static boolean isAccessibleFile(String _arg) {
		if(_arg==null || _arg.equals(""))
			return false;
		else
			return FileUtil.isAccessibleFile(Paths.get(_arg));
	}

	public static boolean isAccessibleFile(Path _arg) {
		boolean result = false;
		if(_arg!=null) {
			final File f = _arg.toFile();
			if(f.isFile() && f.exists() && f.canRead()) result = true;
		}
		return result;
	}

	public final static boolean isAccessibleDirectory(Path _dir) {
		if(_dir==null)
			return false;
		else {
			return FileUtil.isAccessibleDirectory(_dir.toFile());
		}
	}

	public final static boolean  isAccessibleDirectory(File _dir) {
		if(_dir==null)
			return false;
		else {
			return _dir.exists() && _dir.isDirectory() && _dir.canRead();
		}
	}

	public final static boolean isAccessibleDirectory(String _dir) {
		if(_dir==null || _dir.equals(""))
			return false;
		else
			return FileUtil.isAccessibleDirectory(Paths.get(_dir));
	}

	public final static void createDirectory(Path _p) {
		if(!_p.toFile().exists()) {
			try {
				Files.createDirectories(_p);
			} catch (IOException e) {
				FileUtil.log.error("Error while creating directory [" + _p + "]: " + e.getMessage());
			}
		}
	}

	/**
	 * Removes path information from the argument as to return the file name.
	 * @param complete file path
	 * @return file name
	 */
	public static String getFileName(String _file_path) {
		return FileUtil.getFileName(_file_path, true);
	}

	/**
	 * Removes path information and, if requested, the file extension from the given file name.
	 * @param complete file path
	 * @return file name
	 */
	public static String getFileName(String _file_path, boolean _keep_ext) {
		String n = _file_path;

		// Remove path
		final int idx = (n==null ? -1 : n.lastIndexOf(File.separator));
		if(idx!=-1) n = n.substring(idx+1);

		// Remove file extension
		if(!_keep_ext && n.indexOf(".")!=-1)
			n = n.substring(0, n.indexOf("."));

		return n;
	}

	/**
	 * Returns the name of the {@link Charset} configured via configuration parameter {@link VulasConfiguration#CHARSET}.
	 * 
	 * @see {@link #readFile(Path)}, {@link #writeToFile(File, byte[])}, etc.
	 * @return
	 */
	public static String getCharsetName() {
		return FileUtil.getCharset().name();
	}

	/**
	 * Returns the {@link Charset} configured via configuration parameter {@link VulasConfiguration#CHARSET}.
	 * 
	 * @see {@link #readFile(Path)}, {@link #writeToFile(File, byte[])}, etc.
	 * @return
	 */
	public static Charset getCharset() {
		final String cs = VulasConfiguration.getGlobal().getConfiguration().getString(VulasConfiguration.CHARSET, "UTF-8");
		try {
			return Charset.forName(cs);
		} catch (IllegalCharsetNameException|UnsupportedCharsetException unused) {
			log.error("Defaulting to UTF-8 since charset with name [" + cs + "] cannot be created: " + unused.getMessage());
			return StandardCharsets.UTF_8;
		}
	}
	
	public static Path copyFile(Path _source_file, Path _target_dir) throws IOException {
		final Path to = _target_dir.resolve(_source_file.getFileName());
		try (final InputStream is = new FileInputStream(_source_file.toFile());
				final OutputStream os = new FileOutputStream(to.toFile())) {
			final byte[] byte_buffer = new byte[1024];
			int len = 0;
			while((len = is.read(byte_buffer)) != -1)
				os.write(byte_buffer,0,len);
		}
		return to;
	}

	// Reading files

	public static String readFile(String _p) throws IOException {
		return FileUtil.readFile(Paths.get(_p));
	}

	/**
	 * Preserves the line breaks of the original file.
	 * As such, it can be used for calculating digests.
	 * 
	 * @see {@link DigestUtil#getDigestAsString(String,java.nio.charset.Charset, DigestAlgorithm)}
	 * 
	 * @param _p
	 * @return
	 * @throws IOException
	 */
	public static String readFile(Path _p) throws IOException {
		try (final InputStream is = new FileInputStream(_p.toFile())) {
			return FileUtil.readInputStream(is, FileUtil.getCharset());
		}
	}
	
	/**
	 * Reads the given {@link InputStream} into a {@link String}.
	 * 
	 * @param _p
	 * @return
	 * @throws IOException
	 */
	public static String readInputStream(InputStream _is, Charset _cs) throws IOException {
		return new String(FileUtil.readInputStream(_is), _cs);		
	}

	/**
	 * Reads the given {@link InputStream} into a byte array.
	 * 
	 * @param _p
	 * @return
	 * @throws IOException
	 */
	public static byte[] readInputStream(InputStream _is) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final byte[] byte_buffer = new byte[1024];
		int len = 0;
		while((len = _is.read(byte_buffer)) != -1)
			bos.write(byte_buffer,0,len);
		bos.flush();
		return bos.toByteArray();		
	}

	// Writing files

	/**
	 * Writes a {@link String} to the given {@link File}.
	 */
	public static final void writeToFile(File _f, String _content) throws IOException {
		if(_content!=null)
			FileUtil.writeToFile(_f, _content.getBytes(FileUtil.getCharset()));
	}

	/**
	 * Writes the given content to a file that is in
	 * the temporary directory returned by {@link VulasConfiguration#getTmpDir()}.
	 * @return the path of the file
	 */
	public static final Path writeToTmpFile(String _filename, String _suffix, String _content) throws IOException {
		final Path dir = VulasConfiguration.getGlobal().getTmpDir();
		final String prefix = (_filename!=null ? _filename + "-" : "vulas-tmp-");
		final File f = File.createTempFile(prefix, "." + _suffix, dir.toFile());
		FileUtil.writeToFile(f, _content.getBytes(FileUtil.getCharset()));
		return f.toPath();
	}

	/**
	 * Writes a byte array to the given {@link File}.
	 */
	public static final void writeToFile(File _f, byte[] _content) throws IOException {
		try {
			try (final FileOutputStream fos = new FileOutputStream(_f)) {
				fos.write(_content);
			}
		} catch(Exception e) {
			throw new IOException("Error while writing to file [" + _f + "]: " + e.getMessage(), e);
		}
	}

	/**
	 * Expects a {@link String} array with multiple paths and returns a set for all those paths that represent
	 * accessible files or directories in the file system.
	 * @param _p
	 */
	public static Set<Path> getPaths(String[] _paths) {
		final Set<Path> r = new HashSet<Path>();
		if(_paths!=null) {
			for(int i=0; i<_paths.length; i++) {
				final Path p = FileUtil.getPath(_paths[i]);
				if(p!=null)
					r.add(p);
			}
		}
		return r;
	}

	public static Path getPath(String _path, boolean _create) {
		if(_path==null || _path.equals(""))
			return null;
		else {
			final Path p = Paths.get(_path).toAbsolutePath();
			if(FileUtil.isAccessibleFile(p) || FileUtil.isAccessibleDirectory(p)) {
				return p;
			} else if(!p.toFile().exists() && _create) {
				FileUtil.createDirectory(p);
				return p;
			} else {
				FileUtil.log.warn("Path [" + _path + "] is neither an accessible file nor an accessible directory, hence, will be ignored");
				return null;
			}			
		}
	}

	/**
	 * Returns a {@link Path} object for the given {@link String} in case the path exists in the file system, null otherwise.
	 * @param _path
	 * @return
	 */
	public static Path getPath(String _path) {
		return FileUtil.getPath(_path, false);
	}

	/**
	 * Returns the SHA1 digest for the given file.
	 * @param _file
	 * @return
	 */
	public static final String getSHA1(File _file) { return FileUtil.getDigest(_file, DigestAlgorithm.SHA1); }

	/**
	 * Returns a digest for the given file, using the given algorithm.
	 * @param _file
	 * @param _alg
	 * @return
	 */
	public static final String getDigest(File _file, DigestAlgorithm _alg) {
		try {
			if(!_file.canRead())
				throw new IOException("Cannot read file");

			// Digest preparation
			final MessageDigest md = MessageDigest.getInstance(_alg.toString());				
			try (final InputStream is = new FileInputStream(_file);
				 final DigestInputStream dis = new DigestInputStream(is, md);) {
				byte[] bytes = new byte[1024];
				while(dis.read(bytes, 0, 1024)!=-1) {;} //read()
				byte[] digest = md.digest();
				return DigestUtil.bytesToHex(digest);
			}
		}
		catch (NoSuchAlgorithmException e) {
			FileUtil.log.error("Error while instantiating [" + _alg + "] digest: " + e.getMessage());
		}
		catch(IOException e) {
			FileUtil.log.error("IO Error while computing [" + _alg + "] digest: " + e.getMessage());
		}
		catch(ArrayIndexOutOfBoundsException be) {
			FileUtil.log.error("Error while computing [" + _alg + "] digest: " + be.getMessage());
		}
		return null;
	}

	public static String getJarFilePath(Class<?> _clazz) {
		final ClassLoader cl = _clazz.getClassLoader();
		final URL res_url = cl.getResource(_clazz.getName().replace('.', '/') + ".class");
		return FileUtil.getJARFilePath(res_url.toString());
	}

	/**
	 * Provided that the given URL points to a JAR file (or classes contained therein),
	 * the method transforms the string representation of the given URL
	 * into a string presentation of the local file system path of the JAR file.
	 * If the URL does not point to a JAR, the method returns null.
	 * Examples of URLs are as follows:
	 * url:file:/a/b/c.jar!123.class
	 * file:/a/b/c.jar
	 */
	public static String getJARFilePath(String _url) {
		String file_url = null, file_path = null;

		// (1) Bring _url into form "file:/<abc>.jar"
		if(_url!=null && _url.startsWith("file:") && _url.endsWith(".jar")) {
			file_url = _url;
		}
		else if(_url!=null && _url.startsWith("jar:file:")) {
			file_url = _url.substring(4); //new String("jar:").length());
			final int idx = file_url.indexOf('!');
			if(idx!=-1)
				file_url = file_url.substring(0, idx);
		}

		// 2) If that worked, transform into FS path
		if(file_url!=null) {
			URI uri = null;
			try {
				uri = new URI(file_url);
				file_path = Paths.get(uri).toString();
			} catch (URISyntaxException e) {
				log.error("Cannot create URI from [" + file_url + "]");
			}
		}

		return file_path;
	}
}
