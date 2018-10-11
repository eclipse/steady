package com.sap.psr.vulas.python.pip;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.python.ProcessWrapper;
import com.sap.psr.vulas.python.ProcessWrapperException;
import com.sap.psr.vulas.python.utils.PythonConfiguration;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;
import com.sap.psr.vulas.shared.util.ThreadUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class PipWrapper {

	private final static Log log = LogFactory.getLog(PipWrapper.class);

	static final Pattern DOWNLOAD_PATTERN = Pattern.compile("^\\s*Downloading\\s*(http\\S*).*$");

	static final Pattern SAVED_PATTERN_1 = Pattern.compile("^\\s*File was already downloaded\\s*(.*)$");
	static final Pattern SAVED_PATTERN_2 = Pattern.compile("^\\s*Saved\\s*(.*)$");

	static final String PACK_PATTERN_REGEX = ".*<pack>-<version>.*";

	private Path pathToPip = null;

	private Path logDir = null;

	private StringList ignorePacks = new StringList();

	/**
	 * Assumes that the pip executable is part of the PATH environment variable.
	 */
	public PipWrapper() throws ProcessWrapperException {
		this(Paths.get("pip"), null);
	}

	/**
	 * Creates a new wrapper for the pip executable at the given path.
	 * @param _path_to_pip
	 */
	public PipWrapper(Path _path_to_pip, Path _log_dir) throws ProcessWrapperException {
		this.pathToPip = _path_to_pip;
		if(_log_dir!=null)
			this.logDir = _log_dir;
		else {
			try {
				this.logDir = FileUtil.createTmpDir("vulas-pip-");
				log.info("Created tmp directory [" + this.logDir + "]");
			} catch (IOException e) {
				throw new ProcessWrapperException("Cannot create tmp directory: " + e.getMessage());
			}
		}

		// Not to be added as dependencies
		final String[] ignore_packs = VulasConfiguration.getGlobal().getStringArray(PythonConfiguration.PY_BOM_IGNORE_PACKS, new String[] {});
		this.ignorePacks.addAll(ignore_packs, true);
	}

	private boolean ignorePackage(String _p) {
		return this.ignorePacks.contains(_p, ComparisonMode.EQUALS, CaseSensitivity.CASE_SENSITIVE);
	}

	public boolean isAvailable() {
		boolean exists = false;
		try {
			final Process p = new ProcessBuilder(this.pathToPip.toString()).start();
			final int exit_code = p.waitFor();
			exists = exit_code==0;
		} catch(IOException ioe) {
			log.error("Error calling [pip]: " + ioe.getMessage(), ioe);
		}
		catch(InterruptedException ie) {
			log.error("Error calling [pip]: " + ie.getMessage(), ie);
		}
		return exists;
	}

	/**
	 * Calls pip install on the given project {@link Path} and the returns a list of all installed packages (including the dependencies).
	 * @return
	 */
	public Set<PipInstalledPackage> installPackages(Path _project) {
		Set<PipInstalledPackage> packages = null;
		try {
			// Make download dir
			final Path download_dir = Paths.get(logDir.toString(), "pip-download");
			FileUtil.createDirectory(download_dir);

			// Download all deps
			ProcessWrapper pw = new ProcessWrapper();
			pw.setCommand(this.pathToPip, "download", "-d", download_dir.toString(), "--no-cache-dir", _project.toString());
			pw.setPath(logDir);
			Thread t = new Thread(pw);
			t.start();
			t.join();
			final Path download_info = pw.getOutFile();		

			// Install all deps
			pw = new ProcessWrapper();
			pw.setCommand(this.pathToPip, "install", _project.toString());
			pw.setPath(logDir);
			t = new Thread(pw);
			t.start();
			t.join();

			// Get all deps
			packages = this.getListPackages();

			// Enrich with download info
			this.searchDownloadInfo(packages, FileUtil.readFile(download_info));
		}
		catch (ProcessWrapperException e) {
			log.error("Error calling installing packages: " + e.getMessage(), e);
		}
		catch (IOException e) {
			log.error("Error calling installing packages: " + e.getMessage(), e);
		}
		catch (InterruptedException e) {
			log.error("Error calling installing packages: " + e.getMessage(), e);
		}
		return packages;
	}

	/**
	 * Calls pip freeze and pip show <package> in order to create and return all {@link PipInstalledPackages} of the Python environment.
	 * @return
	 */
	public Set<PipInstalledPackage> getFreezePackages() throws ProcessWrapperException, IOException, InterruptedException {
		final StopWatch sw = new StopWatch("pip freeze").start();
		Set<PipInstalledPackage> packages = null;
		ProcessWrapper pw = new ProcessWrapper().setCommand(this.pathToPip, "freeze").setPath(logDir);
		final Thread t = new Thread(pw, "pip");
		t.start();
		t.join();
		if(pw.terminatedWithSuccess()) {
			// Create packages
			packages = this.parsePipFreezeOutput(pw.getOutFile());

			// Log
			log.info("Found [" + packages.size() + "] pip packages:");
			for(PipInstalledPackage pack: packages)
				log.info("    " + pack);

			// Call pip show and pip download in separate threads
			final ExecutorService pool = Executors.newFixedThreadPool(ThreadUtil.getNoThreads(2)); //newSingleThreadExecutor();
			final Set<Future<PipInstalledPackage>> futures = new HashSet<Future<PipInstalledPackage>>();
			for(PipInstalledPackage pack: packages) {
				futures.add(pool.submit(new PipShow(pack)));
				futures.add(pool.submit(new PipDownload(pack)));
			}
			pool.shutdown();
			try {
				while (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
					int done = 0;
					for(Future<PipInstalledPackage> f: futures) {
						if(f.isDone())
							done++;
					}
					sw.lap("[" + done + "/" + futures.size() + "] pip jobs are done");
				}
			} catch (InterruptedException e) {
				PipWrapper.log.error("Interrupt exception");
			}			
		}
		sw.stop();
		return packages;
	}

	/**
	 * Parses the output of pip list, and instantiates {@link PipInstalledPackage} for every installed pip package.
	 * @param _file
	 * @return
	 * @throws IOException
	 */
	private Set<PipInstalledPackage> parsePipFreezeOutput(Path _file) throws IOException {
		final Set<PipInstalledPackage> packs = new TreeSet<PipInstalledPackage>();

		// The pattern to search for
		final Pattern pattern = Pattern.compile("^(.*)==(.*)$");

		// Read line by line
		final BufferedReader reader = new BufferedReader(new FileReader(_file.toFile()));
		String line;
		while( (line=reader.readLine())!=null ) {
			final Matcher m = pattern.matcher(line);
			if(m.matches()) {
				if(this.ignorePackage(m.group(1))) {
					log.warn("Package [" + m.group(1) + "] not added as installed package");
				} else {
					packs.add(new PipInstalledPackage(m.group(1), m.group(2)));
				}
			}
		}
		reader.close();

		return packs;
	}

	/**
	 * Calls pip list and pip show <package> in order to create and return all {@link PipInstalledPackages} of the Python environment.
	 * @return
	 */
	public Set<PipInstalledPackage> getListPackages() throws ProcessWrapperException, IOException, InterruptedException {
		final StopWatch sw = new StopWatch("pip list").start();

		// Try legacy format
		ProcessWrapper pw = new ProcessWrapper().setCommand(this.pathToPip, "list", "--format", "legacy").setPath(logDir);
		Thread t = new Thread(pw, "pip");
		t.start();
		t.join();
		
		Set<PipInstalledPackage> packages = null;
		if(pw.terminatedWithSuccess()) {
			packages = this.parsePipListOutput(pw.getOutFile());
		}
		// Try JSON format
		else {
			log.info("Legacy format did not work, trying JSON format...");
			pw = new ProcessWrapper().setCommand(this.pathToPip, "list", "--format", "json").setPath(logDir);
			t = new Thread(pw, "pip");
			t.start();
			t.join();
			if(pw.terminatedWithSuccess())
				packages = this.deserializePipListOutput(pw.getOutFile());
		}
		
		// Collect package details
		if(packages!=null) {
			log.info("Found [" + packages.size() + "] pip packages:");
			for(PipInstalledPackage pack: packages)
				log.info("    " + pack);

			// Call pip show in separate threads
			final ExecutorService pool = Executors.newFixedThreadPool(ThreadUtil.getNoThreads(2)); //newSingleThreadExecutor();
			final Set<Future<PipInstalledPackage>> futures = new HashSet<Future<PipInstalledPackage>>();
			for(PipInstalledPackage pack: packages) {
				futures.add(pool.submit(new PipShow(pack)));
			}
			pool.shutdown();
			try {
				while (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
					int done = 0;
					for(Future<PipInstalledPackage> f: futures) {
						if(f.isDone())
							done++;
					}
					sw.lap("[" + done + "/" + futures.size() + "] pip jobs are done");
				}
			} catch (InterruptedException e) {
				PipWrapper.log.error("Interrupt exception");
			}
		} else {
			log.warn("No pip packages found with pip list");
		}
		
		sw.stop();
		return packages;
	}
	
	/**
	 * Parses the output of pip list, and instantiates {@link PipInstalledPackage} for every installed pip package.
	 * @param _file
	 * @return
	 * @throws IOException
	 */
	private Set<PipInstalledPackage> parsePipListOutput(Path _file) throws IOException {
		final Set<PipInstalledPackage> packs = new TreeSet<PipInstalledPackage>();

		// The pattern to search for
		// HP, 17.04.2018: There seem to be versions of PIP with and without using brackets
		final Pattern pattern_brackets = Pattern.compile("^(.*)\\s+\\((.*)\\)$");
		//final Pattern pattern = Pattern.compile("^(.*)\\s+(.*)$");

		// Read line by line
		final BufferedReader reader = new BufferedReader(new FileReader(_file.toFile()));
		String line;
		while( (line=reader.readLine())!=null ) {
			final Matcher mb = pattern_brackets.matcher(line);
			if(mb.matches()) {
				if(this.ignorePackage(mb.group(1))) {
					log.warn("Package [" + mb.group(1) + "] not added as installed package");
				} else {
					packs.add(new PipInstalledPackage(mb.group(1), mb.group(2)));
				}				
			}
		}
		reader.close();

		return packs;
	}

	/**
	 * Parses the output of pip list, and instantiates {@link PipInstalledPackage} for every installed pip package.
	 * @param _file
	 * @return
	 * @throws IOException
	 */
	private Set<PipInstalledPackage> deserializePipListOutput(Path _file) throws IOException {
		// Deserialize
		final String json = FileUtil.readFile(_file);
		final PipPackageJson[] packs = (PipPackageJson[])JacksonUtil.asObject(json, PipPackageJson[].class);
		// Create set
		final Set<PipInstalledPackage> set = new HashSet<PipInstalledPackage>();
		for(PipPackageJson p: packs) {
			if(this.ignorePackage(p.getName())) {
				log.warn("Package [" + p.getName() + "] not added as installed package");
			} else {
				set.add(new PipInstalledPackage(p.getName(), p.getVersion()));
			}	
		}
		return set;
	}

	/**
	 * Calls pip show <package> in order to set the properties of the given {@link PipInstalledPackage}.
	 * @param _p
	 * @param _pool TODO
	 */
	final class PipShow implements Callable<PipInstalledPackage> {
		private PipInstalledPackage pack = null;

		PipShow(PipInstalledPackage _pack) {
			this.pack = _pack;
		}

		public PipInstalledPackage call() throws ProcessWrapperException, IOException {
			final ProcessWrapper pw = new ProcessWrapper(this.pack.getName());
			pw.setCommand(pathToPip, "show", this.pack.getName()).setPath(logDir);
			pw.run();
			if(pw.terminatedWithSuccess()) {
				this.pack.addProperties(parsePipShowOutput(pw.getOutFile()));
				log.info("Added properties to " + this.pack);
			}
			else {
				log.info("Properties for " + this.pack + " are missing");
			}
			return this.pack;
		}
	}

	private Map<String,String> parsePipShowOutput(Path _file) throws IOException {
		final Map<String,String> props = new HashMap<String,String>();

		// The pattern to search for
		final Pattern pattern = Pattern.compile("^([^:]*):(.*)$");

		// Read line by line
		final BufferedReader reader = new BufferedReader(new FileReader(_file.toFile()));
		String line;
		while( (line=reader.readLine())!=null ) {
			final Matcher m = pattern.matcher(line);
			if(m.matches())
				props.put(m.group(1).trim(), m.group(2).trim());
		}
		reader.close();

		return props;		
	}

	/**
	 * Calls pip download <package> in order to add download path and url of the given {@link PipInstalledPackage}.
	 * @param _p
	 */
	final class PipDownload implements Callable<PipInstalledPackage> {
		private PipInstalledPackage pack = null;

		PipDownload(PipInstalledPackage _pack) {
			this.pack = _pack;
		}

		public PipInstalledPackage call() throws ProcessWrapperException, IOException {
			// Make download dir
			//final Path download_dir = Paths.get(logDir.toString(), "pip-download");
			final Path download_dir = FileUtil.createTmpDir(this.pack.getName() + "-");

			// Download all deps
			ProcessWrapper pw = new ProcessWrapper();
			pw.setCommand(pathToPip, "download", "-d", download_dir.toString(), "--no-cache-dir", this.pack.getName()+"=="+this.pack.getVersion());
			pw.setPath(logDir);
			pw.run();
			final Path download_info = pw.getOutFile();

			// Enrich with download info
			searchDownloadInfo(this.pack, FileUtil.readFile(download_info));
			return this.pack;
		}
	}

	/**
	 * Searches the provided output of 'pip download' for download URL and download path of the given packages.
	 * @param _packs
	 * @param _out standard out of pip download
	 */
	void searchDownloadInfo(Set<PipInstalledPackage> _packs, String _out) throws IOException {
		if(_packs!=null)
			for(PipInstalledPackage pack: _packs)
				this.searchDownloadInfo(pack, _out);
	}

	/**
	 * Searches the provided output of 'pip download' for download URL and download path of the given package.
	 * @param _p
	 * @param _out standard out of pip download
	 */
	private void searchDownloadInfo(PipInstalledPackage _p, String _out) throws IOException {
		// Find all download URLs and files in pip output
		final Set<String> urls  = new HashSet<String>();
		final Set<String> files = new HashSet<String>();
		final BufferedReader r = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(_out.getBytes())));
		String line = null;
		while( (line=r.readLine())!=null ) {
			final Matcher d  = PipWrapper.DOWNLOAD_PATTERN.matcher(line);
			final Matcher s1 = PipWrapper.SAVED_PATTERN_1.matcher(line);
			final Matcher s2 = PipWrapper.SAVED_PATTERN_2.matcher(line);
			if(d.matches())
				urls.add(d.group(1));
			else if(s1.matches())
				files.add(s1.group(1));
			else if(s2.matches())
				files.add(s2.group(1));
		}

		// Regex to search for the file name
		String regex = PACK_PATTERN_REGEX.replace("<pack>", _p.getName().toLowerCase());
		regex = regex.replace("<version>", _p.getVersion().toLowerCase());
		regex = regex.replace("-", "[_-]{1,1}"); // For some reason, the file names of some packages use _ instead of -

		final  Pattern p = Pattern.compile(regex);
		for(String url: urls) {
			final Matcher m = p.matcher(url.toLowerCase());
			if(m.matches()) {
				_p.setDownloadUrl(url);
				break;
			}
		}
		for(String file: files) {
			final Matcher m = p.matcher(file.toLowerCase());
			if(m.matches()) {
				_p.setDownloadPath(Paths.get(file));
				break;
			}
		}

		// Warn if no info could be found
		if(_p.getDownloadPath()==null && _p.getDownloadUrl()==null)
			log.warn("Download path and URL for " + _p + " missing");
		else if(_p.getDownloadPath()==null)
			log.warn("Download path for " + _p + " missing");
		else if(_p.getDownloadUrl()==null)
			log.warn("Download URL for " + _p + " missing");
		else
			log.info("Found download info for " + _p);
	}
	
	/**
	 * Helper class for deserializing the output of pip list --format json.
	 */
	static class PipPackageJson {
		String name;
		String version;
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public String getVersion() { return version; }
		public void setVersion(String version) { this.version = version; }
	}
}
