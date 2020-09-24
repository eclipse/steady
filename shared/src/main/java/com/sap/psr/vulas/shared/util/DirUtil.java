    package com.sap.psr.vulas.shared.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.enums.DigestAlgorithm;

/**
 * <p>DirUtil class.</p>
 *
 */
public class DirUtil {

	private static final Log log = LogFactory.getLog(DirUtil.class);

	/**
	 * Returns true if the given directory contains a file with the given name, false otherwise.
	 *
	 * @param _dir a {@link java.io.File} object.
	 * @param _filename a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static final boolean containsFile(final File _dir, final String _filename) {
		if(_dir.isDirectory()) {
			final Path file = Paths.get(_dir.toPath().toString(), _filename);
			return file.toFile().exists();
		} else {
			return false;
		}
	}

	/**
	 * Searches recursively in the given directory and returns all the contained files and directories (excluding the given directory).
	 *
	 * @param _dir a {@link java.io.File} object.
	 * @param _ignore an array of {@link java.lang.String} objects.
	 * @return an array of {@link java.io.File} objects.
	 */
	public static File[] getAllFiles(final File _dir, final String[] _ignore) {
		if(!_dir.isDirectory())
			throw new IllegalArgumentException("[" + _dir + "] is not a directory");
		
		final List<File> list = new ArrayList<File>();

		final File[] files = _dir.listFiles();
		if(files!=null) {
			Arrays.sort(files);
			outer:
				for (final File file: files) {
					if (file.isDirectory()) {
						list.addAll(Arrays.asList(DirUtil.getAllFiles(file, _ignore)));
						list.add(file);
					} else {
						if(_ignore!=null) {
							for(String ext: _ignore) {
								if(file.getName().endsWith(ext)) {
									continue outer;
								}
							}
						}
						list.add(file);
					}
				}
		}
		return list.toArray(new File[list.size()]);
	}

	/**
	 * <p>unzip.</p>
	 *
	 * @param _zip a {@link java.io.File} object.
	 * @param _out_dir a {@link java.io.File} object.
	 * @return a {@link java.io.File} object.
	 * @throws java.io.IOException if any.
	 */
	public static File unzip(final File _zip, File _out_dir) throws IOException {
		if (_zip.isDirectory())
			return _zip;

		try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(_zip))) {
			ZipEntry entry = null;
			while((entry=zis.getNextEntry())!= null) {

				// ZipSlip: Do not extract
				if(!DirUtil.isBelowDestinationPath(_out_dir.toPath(), entry.getName())) {
					log.warn("Entry [" + entry + "] of archive [" + _zip + "] will not be extracted, as it would be outside of destination directory");
				}
				// Extract
				else {				
					final File file = new File(_out_dir, entry.getName());
					if (entry.isDirectory()) {
						final boolean created = file.mkdirs();
						if(!created)
							log.warn("Directory [" + file + "] could not be created");
					}
					else {
						boolean exists = file.getParentFile().exists();
						
						// Create parent if not existing
						if (!exists) {
							exists = file.getParentFile().mkdirs();
							log.error("Cannot create directory [" + file.getParentFile() + "]");
						}

						// Extract file
						if(exists) {
							try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
								final byte[] bytes = new byte[1024];
								int len = 0;
								while((len = zis.read(bytes)) != -1)
									bos.write(bytes,0,len);
							}
						}
					}
				}
			}
		}
		return _out_dir;
	}

	/**
	 * Checks whether the given archive entry, e.g., from a {@link ZipEntry} and {@link JarEntry}, when extracted, is below the given {@link Path}.
	 * This method should be used before extracting archive entries, in order to protect against the ZipSlip vulnerability.
	 *
	 * On *nix machines, any occurence of a Windows name separator (\) will be replace by the *nix name separator (/) in order
	 * to detect malicious Windows archives (archive entries) on *nix as well.
	 *
	 * @param _destination_path a {@link java.nio.file.Path} object.
	 * @param _entry_name a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public static boolean isBelowDestinationPath(Path _destination_path, String _entry_name) {
		String entry_name = _entry_name;

		// On *nix, replace name separator of Windows (\) archive entries with the *nix separator (/).
		// Otherwise, malicious Win archive entries would not be detected on *nix machines.
		if(File.separator.equals("/") && entry_name.contains("\\"))
			entry_name = entry_name.replace('\\', '/');

		final Path entry = Paths.get(entry_name);
		final Path dest = _destination_path.resolve(entry).toAbsolutePath();
		final Path dest_normalized = dest.normalize();
		//log.info("Normalized destination and entry path [" + dest_normalized + "], destination path [" + _destination_path + "], entry [" + _entry_name + "]");
		return dest_normalized.startsWith(_destination_path);
	}

	/**
	 * Returns all paths of the first set that are sub-paths of one of the paths of the second set.
	 * The filter can be inverted with help of the boolean argument.
	 *
	 * @param _to_be_filtered a {@link java.util.Set} object.
	 * @param _filter a {@link java.util.Set} object.
	 * @param _keep_subpaths a boolean.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Path> filterSubpaths(Set<Path> _to_be_filtered, Set<Path> _filter, boolean _keep_subpaths) {
		final Set<Path> result = new HashSet<Path>();
		for(Path p1: _to_be_filtered) {
			boolean is_sub = false;
			for(Path p2: _filter) {
				if(p1.startsWith(p2)) {
					is_sub = true;
					if(_keep_subpaths) {
						result.add(p1);
						break;
					}
				}
			}
			if(!_keep_subpaths && !is_sub) {
				result.add(p1);
			}
		}
		return result;
	}

	/**
	 * Computes a digest for the given directory. This digest is computed over the concatenation of the digests contained in the respective directory.
	 *
	 * @param _dir a {@link java.io.File} object.
	 * @param _alg a {@link com.sap.psr.vulas.shared.enums.DigestAlgorithm} object.
	 * @see FileUtil#getDigest(File, DigestAlgorithm)
	 * @param _ignore an array of {@link java.lang.String} objects.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getDigest(final File _dir, final String[] _ignore, DigestAlgorithm _alg) {
		if(!_dir.isDirectory())
			throw new IllegalArgumentException("[" + _dir + "] is not a directory");

		// Concatenate the digests of all files
		final StringBuffer res = new StringBuffer();
		for (File file: DirUtil.getAllFiles(_dir, _ignore)) {
			if (!file.isDirectory()) {
				res.append(FileUtil.getDigest(file, _alg));
			}
		}

		// Create the digest
		return DigestUtil.getDigestAsString(res.toString(), StandardCharsets.UTF_8, DigestAlgorithm.MD5);
	}

    /**
     * Create a tarball for the given directory. Specific directory or file extension can be ignored.
     *
     * @param _src a {@link java.io.File} object.
	 * @param _dst a {@link java.io.File} object.
     * @param _ignore_dir an array of {@link java.lang.String} objects.
	 * @param _ignore_ext an array of {@link java.lang.String} objects.
	 * @return a {@link java.io.File} object.
     */
	public static File createTarBall(final File _src, final File _dst, final String[] _ignore_dir, final String[] _ignore_ext) throws ArchiveException {
		if(_dst.isFile()) {
			throw new IllegalArgumentException("[" + _dst + "] is existed, cannot overwrite a file");
		}

		TarArchiveOutputStream tos = null;
		try {
			// Prepare tarball setting
			FileOutputStream fos = new FileOutputStream(_dst);
			GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
			tos = new TarArchiveOutputStream(gos);
			tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

			Queue<File> fileQueue = new LinkedList<File>();
			fileQueue.add(_src);

			// Iterate over files in directory and add them in a tarball
			outer:
                while(!fileQueue.isEmpty()) {
                    File file = fileQueue.remove();

					// Add files to the queue
                    if(FileUtil.isAccessibleDirectory(file)) {
						if(_ignore_dir != null) {
							for(String dir : _ignore_dir) {
								if (file.getName().equalsIgnoreCase(dir))
									continue outer;
							}
						}
						for(File subfile : file.listFiles())
							fileQueue.add(subfile);
					}
                    // Add file in the tarball
                    else if(FileUtil.isAccessibleFile(file.toString())) {
                        if(_ignore_ext != null) {
							for(String ext : _ignore_ext) {
								if(file.getName().endsWith(ext))
									continue outer;
							}
                        }
                        final String entry_name = _src.toPath().toAbsolutePath().relativize(file.toPath().toAbsolutePath()).toString();
                        tos.putArchiveEntry(new TarArchiveEntry(file, entry_name));

                        FileInputStream fis = new FileInputStream(file);
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        IOUtils.copy(bis, tos);
                        tos.closeArchiveEntry();
                        bis.close();
                        fis.close();
                    }
                    else {
                        log.warn("Cannot add [" + file.getName() + "] in the tarball");
                    }
                }
                tos.finish();
				tos.close();
                gos.close();
                fos.close();
		} catch(Exception e) {
			throw new ArchiveException("Cannot make the tarball [" + _dst.toString() + "]");
		}
		return _dst;
	}
}
