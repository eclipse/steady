package com.sap.psr.vulas.svn;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.vcs.FileChange;
import com.sap.psr.vulas.vcs.IVCSClient;
import com.sap.psr.vulas.vcs.RepoMismatchException;



public class SvnClient implements IVCSClient {

	private static final Log log = LogFactory.getLog(SvnClient.class);

	private static final String TYPE = "SVN";

	private final String id = new Double(Math.random()).toString();
	private Path workDir = null;

	private URL url = null;

	//private SVNURL repoUrl = null;
	private SVNRepository rootRepo = null;
	private Collection<SVNLogEntry> logEntries = null;
	private BasicAuthenticationManager authManager = null;
	private Date asOf = null;

	private Configuration cfg = null;

	public String getType() { return SvnClient.TYPE; }

	public SvnClient() {
		this.cfg = VulasConfiguration.getGlobal().getConfiguration();
	}

	public void setRepoUrl(URL _u) throws RepoMismatchException {
		if(_u==null) throw new IllegalArgumentException("Invalid url: " + _u);
		this.url = _u;

		// Prepare repository setup (authentication and HTTP proxy)
		authManager = new BasicAuthenticationManager( "login" , "password" );
		final String phost = this.cfg.getString("http.proxyHost", null);
		final String pport = this.cfg.getString("http.proxyPort", null);
		if(phost!=null && pport!=null) {
			authManager.setProxy(phost, new Integer(pport).intValue(), "", "");
			SvnClient.log.info("Using proxy " + phost + ":" + pport);
		}

		// Set up repo for trunk (used for searching log entries)
		this.rootRepo = this.setupRepo(null);
	}

	public void setWorkDir(Path _dir) {
		if(_dir!=null) this.workDir = _dir;
		else {
			try {
				this.workDir = java.nio.file.Files.createTempDirectory("patcha_" + this.id);
			} catch (IOException e) {
				throw new IllegalStateException("Unable to create work directory", e);
			}
		}
	}

	private SVNRepository setupRepo(String _path) throws RepoMismatchException {

		// Set repository for given URL (+ trunk or tag suffix)
		String tmp = null;

		// Build repository URL (incl. path)
		if(_path==null)
			tmp = this.url.toString();
		else
			tmp = this.url.toString() + (this.url.toString().endsWith("/") ? "" : "/") + _path;

		int count = 0;
		int maxTries = 5;
		SVNRepository repo = null;
		while(true) {
			try {
				SVNURL url = SVNURL.parseURIEncoded(tmp);

				//SVN2Client.log.debug("Environment proxy (host:port): " + System.getProperty("http.proxyHost") + ":" + System.getProperty("http.proxyPort"));
				SvnClient.log.info("SVNKIT proxy configuration (host:port): " + authManager.getProxyManager(url).getProxyHost() + ":" + authManager.getProxyManager(url).getProxyPort());

				repo =  SVNRepositoryFactory.create(url);
				repo.setAuthenticationManager(authManager);
				repo.testConnection();
			}
			catch(Exception e) {
				Thread.sleep(2000);
				if (++count == maxTries) {
					e.printStackTrace();
					throw new RepoMismatchException(this, tmp, e); // "Cannot create SVN repository from URL '" + tmp + "': " + e.getMessage());
				}
				SvnClient.log.info("Couldn't connect to " + url + ". Retrying now")
			}
		}
		return repo;
	}

	private void updateCommitLog(Date _as_of) throws SVNException {
		// In all of the following cases, no further log entries must be collected
		if(this.logEntries!=null && (this.asOf==null || (_as_of!=null && this.asOf.before(_as_of))))
			return;

		// Retrieve the log
		this.asOf = _as_of;
		final long latest_rev = this.rootRepo.getLatestRevision();

		// Search as of ...
		long first_rev = 0;
		if(this.asOf!=null) first_rev = this.rootRepo.getDatedRevision(this.asOf);

		this.logEntries = rootRepo.log(new String[] { "" }, null, first_rev, latest_rev, true, true);
	}

	/**
	 * Performs a search in the repository root.
	 *
	 */
	public Map<String, String> searchCommitLog(String _str, Date _asOf) {
		final Map<String,String> hits = new HashMap<String,String>();
		try {
			// Update revision log
			this.updateCommitLog(_asOf);

			for (SVNLogEntry logEntry : this.logEntries) {
				if (logEntry.getMessage()!=null && logEntry.getMessage().contains(_str)) {
					SvnClient.log.info("Revision '" + logEntry.getRevision() + "' : " + logEntry.getMessage().trim());
					hits.put(String.valueOf(logEntry.getRevision()), logEntry.getMessage().trim());
				}
			}

		} catch (SVNException e) {
			SvnClient.log.error("Error while searching commit log: " + e.getMessage());
		}
		return hits;
	}

	public Map<String, String> getCommitLogEntries(Set<String> _revs) {//String[] _ids) {
		final Map<String,String> hits = new HashMap<String,String>();
		try {
			// Update revision log (null indicates to retrieve all log entries as of rev. 0)
			this.updateCommitLog(null);

			String id = null;
			for (SVNLogEntry logEntry : this.logEntries) {
				id = String.valueOf(logEntry.getRevision());
				for(String sid: _revs) {
					if(!sid.equals("") && sid.equals(id)) {
						hits.put(sid, logEntry.getMessage().trim());
						continue;
					}
				}
			}
		} catch (SVNException e) {
			SvnClient.log.error("Error while searching commit log: " + e.getMessage());
		}
		return hits;
	}

	private SVNLogEntry getLogEntry(String _rev) {
		SVNLogEntry e = null;
		final long rev = Long.valueOf(_rev);
		for (SVNLogEntry logEntry : this.logEntries)
			if (logEntry.getRevision() == rev) {
				e = logEntry;
				break;
			}
		return e;
	}

	public Path checkout(String _rev) throws Exception {
		if(_rev.contains(":")){
			throw new Exception("The SVN client does not support manually specified branches. Please remove the branch from the commit definition in -e");
		}
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		Path rev_dir = null;
		try {
			// Create subdir for given rev
			Path dir = getRevisionDirectory(_rev);
			rev_dir = Paths.get(dir.toString(), "_src");
			if(!Files.exists(rev_dir))
				rev_dir = Files.createDirectory(rev_dir);

			// Perform checkout
			SVNRevision revision = SVNRevision.create(Long.valueOf(_rev));
			final SvnCheckout checkout = svnOperationFactory.createCheckout();
			checkout.setSingleTarget(SvnTarget.fromFile(rev_dir.toFile()));
			checkout.setSource(SvnTarget.fromURL(this.setupRepo("trunk/").getLocation()));
			checkout.setDepth(SVNDepth.INFINITY);
			checkout.setRevision(revision);
			// checkout
			checkout.run();
		} catch (Exception e) {
			SvnClient.log.error("Error while checking out revision " + _rev + ": " + e.getMessage());
		} finally {
			svnOperationFactory.dispose();
		}
		return rev_dir;
	}

	public Map<String, String> listEntries(String _path, String _asof, String _until) {
		Map<String, String> l = new HashMap<String, String>();
		//String rel_path = url.toString().replace(rootRepo.getDir("", -1, null, (Collection<SVNDirEntry>)null).iterator().next(), "");
		try {
			Collection<SVNDirEntry> entries = this.rootRepo.getDir(_path, -1, null,(Collection<SVNDirEntry>) null); //this.getDirEntries(path);
			Iterator<SVNDirEntry> iterator = entries.iterator();
			String name = null, path = null;

			long rev = -1;
			// Use an iterator to get all directory entries in a certain path ("tags/")

			int asof  = (_asof==null?-1:Integer.parseInt(_asof));
			int until = (_until==null?-1:Integer.parseInt(_until));

			while (iterator.hasNext()) {
				SVNDirEntry entry = (SVNDirEntry) iterator.next();

				SvnClient.log.debug( "Path mess: " + entry.getPath() + " | " + entry.getRelativePath() + " | " + entry.getRepositoryRoot() + " | " + entry.getExternalParentUrl() + " | "  + entry.getURL());

				name = entry.getName();
				rev = entry.getRevision();
				path = entry.getURL().toString().substring(entry.getRepositoryRoot().toString().length());
				if( asof<rev && (_until==null || rev<until) )
					l.put(path, Long.toString(rev));
			}
		} catch (Exception e) {
			SvnClient.log.error( "Error while getting directory entries: " + e.getMessage() );
		}

		// return the tag name and the corresponding revision number
		return l;
	}

	public File checkoutFile(String _rev, String _rel_path) {
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		File f = null;
		SVNURL url = null;
		try {
			// Create subdir for given rev
			final String rel_dir = _rel_path.substring(0, _rel_path.lastIndexOf('/'));
			final Path rev_dir = Paths.get(this.workDir.toString(), _rev, rel_dir);
			Path p = Files.createDirectories(rev_dir);

			// Create SVNURL for specific file
			url = SVNURL.parseURIEncoded(this.rootRepo.getRepositoryRoot(false) + "/" + rel_dir);

			// Perform checkout
			SVNRevision revision = SVNRevision.create(Long.valueOf(_rev));

			SVNUpdateClient clnt = new SVNUpdateClient((ISVNAuthenticationManager)this.authManager, null);
			clnt.doCheckout(url, p.toFile(), revision, revision, SVNDepth.FILES, false); //IMMEDIATES, FILES, INFINITY

			//
			//			final SvnCheckout checkout = svnOperationFactory.createCheckout();
			//			checkout.setSingleTarget(SvnTarget.fromFile(p.toFile()));
			//			checkout.setSource(SvnTarget.fromURL(url));
			//			checkout.setDepth(SVNDepth.IMMEDIATES); //INFINITY
			//			checkout.setRevision(revision);
			//
			//			// Checkout and get file
			//			checkout.run();
			f = Paths.get(this.workDir.toString(), _rev, _rel_path).toFile();
		} catch (Exception e) {
			SvnClient.log.error("Error while checking out URL '" + url + "', revision "+ _rev + ": " + e.getMessage());
		} finally {
			svnOperationFactory.dispose();
		}
		return f;
	}

	/**
	 * Deletes the temp. directory into which the repository files where checked out.
	 */
	public void cleanup() {
		try {
			// Does not work as the directory must be empty. Solution to be implemented: Create a file walker deleting every other file or directory.
			//Files.deleteIfExists(this.workDir);
			//
			FileUtils.deleteDirectory(this.workDir.toFile());
			SvnClient.log.info("Deleted temp. directory [" + this.workDir + "]");
		}
		catch(IOException e) {
			SvnClient.log.error("Error while deleting temp. directory '" + this.workDir + "': " + e.getMessage());
		}
	}

	private Path getRevisionDirectory(String _rev) {
		return Paths.get(this.workDir.toString(), _rev);
	}

	private File getLocalFile(String _rel_path, String _rev) {
		return Paths.get(this.workDir.toString(), _rev, _rel_path).toFile();
	}

	public Set<FileChange> getFileChanges(String _rev) {
		final Set<FileChange> changes = new HashSet<FileChange>();
		try {
			// Update revision log (using this.asOf as argument will result in an actual SVN call only if it has not been done before)
			this.updateCommitLog(this.asOf);

			// Determine prev. revision
			final long l = Long.valueOf(_rev).longValue()-1;
			final String prev_rev = new Long(l).toString();

			// Get changed paths for revision
			SVNLogEntry entry = this.getLogEntry(_rev);
			final Map<String, SVNLogEntryPath> changedPaths = entry.getChangedPaths();

			// Loop all changed path and the check the corresponding files out
			SVNLogEntryPath entryPath = null;
			String rel_path = null;
			File oldf = null, newf = null;
			for (Map.Entry<String, SVNLogEntryPath> p : changedPaths.entrySet()) {
				entryPath = p.getValue();
				rel_path = p.getKey();

				// Checkout file(s), depending on the modification type
				switch(entryPath.getType()) {
				case 'M': oldf = this.checkoutFile(prev_rev, rel_path); newf = this.checkoutFile(_rev, rel_path); break;
				case 'A': oldf = null; newf = this.checkoutFile(_rev, rel_path); break;
				case 'D': oldf = this.checkoutFile(prev_rev, rel_path); newf = null; break;
				default:  oldf = null; newf = null; break;
				}

				// If one of them exists and is not a directory, create a new FileChange
				if( (oldf!=null && !oldf.isDirectory()) || (newf!=null && !newf.isDirectory()) )
					changes.add(new FileChange(this.rootRepo.getLocation().toString(), rel_path, oldf, newf));
			}
		} catch (Exception e) {
			SvnClient.log.error("Error while getting file changes: " + e.getMessage());
		}
		return changes;
	}

	public String getRepoRelativePath(){
		/*
		String rel_path = null;
		Collection<SVNDirEntry> SVNentry = this.rootRepo.getDir("tags/", -1, null,(Collection<SVNDirEntry>) null);
		Iterator<SVNDirEntry> iterator = SVNentry.iterator();
		if (iterator.hasNext()) {
			SVNDirEntry entry = (SVNDirEntry) iterator.next();
			// remove the root repository part from the whole url (object: SVNDirEntry)
			rel_path = entry.getURL().toString().replaceAll(entry.getRepositoryRoot().toString(), "");
		}else{
			SvnClient.log.error("[Error] while getting the relative path for tag directory");
		}
		rel_path = rel_path.substring(0, rel_path.lastIndexOf('/'));
		return rel_path;
		 */
		return url.toString().replace(rootRepo.toString(), "");
	}

	//	public Collection<SVNDirEntry> getDirEntries(String path) throws SVNException{
	//		return rootRepo.getDir(path, -1, null,(Collection<SVNDirEntry>) null);
	//	}

	public long  getRevisionTimeStamp(String revision){
		long revisionTimeStampMilliSecond = 0;

		try {
			//An "svn:date" revision property that is a date & time stamp representing the time when the revision was created.
			SVNPropertyValue propertyValue = rootRepo.getRevisionPropertyValue(Long.parseLong(revision),SVNRevisionProperty.DATE);
			String stringValue = SVNPropertyValue.getPropertyAsString(propertyValue);
			//SVNDate date = SVNDate.parseDate(stringValue);
			revisionTimeStampMilliSecond  = SVNDate.parseDateAsMilliseconds(stringValue);
			//revisionTimeStampMicroSecond = date.getTimeInMicros();
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			SvnClient.log.error("Error when retrieving time stamp for revision : " + revision + " "+ e.getMessage());
		}
		return revisionTimeStampMilliSecond;
	}

}