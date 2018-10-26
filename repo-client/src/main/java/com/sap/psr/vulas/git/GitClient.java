package com.sap.psr.vulas.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.vcs.FileChange;
import com.sap.psr.vulas.vcs.IVCSClient;
import com.sap.psr.vulas.vcs.RepoMismatchException;

/*
 * JGit examples taken from:
 * https://github.com/centic9/jgit-cookbook
 */

public class GitClient implements IVCSClient {

	private static final int RANDOM_ID_LENGTH = 8;
	private static final Log log = LogFactory.getLog( GitClient.class );
	private static final String TYPE = "GIT";

	private String id;
	//    private final String id = new Double( Math.random() )
	//    .toString()
	//    .substring( 0, RANDOM_ID_LENGTH );
	private Path workDir = null;
	private String url = null;
	private Repository repository = null;

	public String getType() { return GitClient.TYPE; }

	/*
	 * setup, almost a constructor
	 *
	 * @param _repoUrl the url from which the repository is to be cloned
	 * @param _repoPath where to put the clone in the local filesystem
	 */
	private Repository setup( String _repoUrl, Path _repoPath ) throws RepoMismatchException {
		Repository repository = null;
		try {
			FileRepositoryBuilder builder = new FileRepositoryBuilder();
			builder.addCeilingDirectory( new File( "/tmp/" ) ); // TODO: restrict this
			builder.findGitDir( _repoPath.toFile() );

			if ( builder.getGitDir() == null ) {
				GitClient.log.info( "Create dir [" + _repoPath + "] for GIT repo [" + _repoUrl + "]");
				repository = builder.setGitDir(
						_repoPath.toFile() ).readEnvironment().findGitDir().build();

			}
			else{
				repository = getRepositoryFromPath(_repoPath.toString());
			}
			Git git = new Git( repository );
			// no previous repository found
			// only clone if this has not been done yet
			if ( repository.getRef( "HEAD" ) == null ) {
				git.cloneRepository()
				.setURI( _repoUrl )
				.setDirectory( _repoPath.toFile() )
				.call();
			
			}
			else {
				GitClient.log.info( "Found existing dir [" + builder.getGitDir().toPath().toAbsolutePath() + "] for GIT repo [" + _repoUrl + "], trying to fetch to update it");
				git.fetch().call();
				git.pull().call();
			}
		}
		// read this:
		// http://www.codeaffine.com/2014/09/22/access-git-repository-with-jgit/
		catch ( org.eclipse.jgit.api.errors.TransportException e ) {
			GitClient.log.error("Invalid GIT repository at " + _repoUrl);
			this.cleanup();
			throw new RepoMismatchException( this, this.url, e );
		}
		catch ( org.eclipse.jgit.api.errors.InvalidRemoteException e ) {
			GitClient.log.error("Invalid remote for GIT repository at " + _repoUrl);
			this.cleanup();
			throw new RepoMismatchException( this, this.url, e );
		}
		catch ( IOException e ) {
			GitClient.log.error("IO exception while cloning GIT repository [" + _repoUrl + "]: " + e.getMessage(), e);
			throw new RepoMismatchException( this, this.url, e );
		}
		catch ( NoHeadException e ) {
			GitClient.log.error("NoHeadException while cloning GIT repository [" + _repoUrl + "]: " + e.getMessage(), e);
			throw new RepoMismatchException( this, this.url, e );
		}
		catch ( GitAPIException e ) {
			GitClient.log.error("GitAPIException while cloning GIT repository [" + _repoUrl + "]: " + e.getMessage(), e);
			//GitClient.log.error( "Could not connect to GIT repository at " + _repoUrl );
			this.cleanup();
			throw new RepoMismatchException( this, this.url, e );
		}
		catch ( Exception e ) {
			GitClient.log.error("Unknown exception while cloning GIT repository [" + _repoUrl + "]: " + e.getMessage(), e);
			throw new RepoMismatchException( this, this.url, e );
		}

		return ( repository );
	}

	/**
	 * Deletes temporary folders
	 */
	public void cleanup()  {

		//Path path = this.workDir;
		//GitClient.log.info( "Deleting recursivey : " + path );

		/**
		 * NOTE: all this can be easily done using Apache's commons-io:
		 *
		 *    FileUtils.deleteDirectory(new File("directory"));
		 */

		try{
			FileUtils.deleteDirectory(this.workDir.toFile());
			GitClient.log.error("Deleted temp. directory [" + this.workDir + "]");
		}
		catch (IOException e) {
			GitClient.log.error("Error while deleting temp. directory '" + this.workDir + "': " + e.getMessage());
		}
	}

	/**
	 *  NOTE: this method does much more than just setting the repository URL!
	 *  It effectively invokes the (private) pseudo-constructor "setup()"
	 *
	 *  It is like this because the API mandates so (IVCSClient interface)
	 */
	public void setRepoUrl( URL _u )  throws RepoMismatchException {
		if ( _u == null ) {
			throw new IllegalArgumentException( "Invalid url: " + _u );
		}

		this.url = _u.toString();
		// fix for: VULAS-67
		// NOTE: this.id is now used to create the folder in which to clone the repository locally
		this.id = _u.getHost() + _u.getPath().replace("/","-");

		if ( this.workDir == null ) {
			this.setWorkDir( null );
		}


		try {
			// all the magic happens in setup (pseudo-constructor)
			// post:
			// - this.repository is created and setup
			// - the git repos is cloned and available in this.workDir
			this.repository = this.setup( this.url, this.workDir );
		}
		catch ( Exception e ) {
			throw new RepoMismatchException( this, this.url, e );
			// "Cannot create GIT repository from URL '" + tmp + "': " + e.getMessage());
			/* e.printStackTrace(); */
		}
	}

	/*
	 *
	 */
	public void setWorkDir( Path _dir ) {
		if ( _dir != null ) {
			this.workDir = _dir;
			return;
		}

		try {
			/* this.workDir = java.nio.file.Files.createTempDirectory( "patcha_" + this.id ); */
			this.workDir = java.nio.file.Files.createDirectories( new
					File( "/tmp/patcha_" + this.id ).toPath() );
		}
		catch ( IOException e ) {
			throw new IllegalStateException("Unable to create work directory", e);
		}
	}


	/**
	 * Performs a search in the repository root.
	 *
	 * This returns ALL the commits whose commit msg contains _str
	 * (only those younger than _asOf)
	 *
	 */
	public Map<String, String> searchCommitLog( String _textToSearch, Date _asOf ) {
		final Map<String, String> hits = new HashMap<String, String>();

		try {

			Repository repository = this.getRepositoryFromPath( null );

			Git git = new Git( repository );
			RevWalk walk = new RevWalk( repository );

			// BROKEN
			/* if ( _asOf != null ) { */
			/* RevFilter filter = CommitTimeRevFilter.after( _asOf ); */
			/* walk.setRevFilter( filter ); */
			/* } */

			RevCommit commit = null;

			Iterable<RevCommit> logs = git.log().call();
			Iterator<RevCommit> i = logs.iterator();

			String commitId = null;
			String commitMsg = null;
			int commitTimeStamp ;
			Date commitDate ;

			if(_asOf!=null) GitClient.log.info( "Search commits after [" + _asOf + "]");

			while ( i.hasNext() ) {
				commit = walk.parseCommit(i.next());
				if(commit!=null) {
					commitId   = commit.getName();
					commitMsg  = commit.getFullMessage();
					commitDate = new Date( ( long )commit.getCommitTime() * 1000l );

					// If no date is given, we only search in the commit message
					if ( commitMsg.contains( _textToSearch ) && ( _asOf==null || commitDate.after( _asOf ) ) ) {
						GitClient.log.info("Found commit [id=" + commitId + ", date=" + commitDate + ", tst=" + commit.getCommitTime() + "]: " + commitMsg );
						hits.put( commitId , commitMsg );
					}
				}
			}

		}
		catch ( Exception e ) {
			GitClient.log.error( "Error while searching commit log: " + e.getMessage() );
			//e.printStackTrace();
		}
		return hits;
	}

	/*
	 * Returns a Git repository object built from the content of a previously
	 * cloned local Git repository available locally at _path
	 */
	private Repository getRepositoryFromPath( String _path ) {

		Repository repository = null;

		try {
			if ( _path == null ) {
				_path = this.workDir.toString() + "/.git";
			}
			else {
				_path += "/.git";
			}

			RepositoryBuilder builder = new RepositoryBuilder();
			repository = builder.setGitDir( new File( _path ) )
					.readEnvironment()
					.findGitDir()
					.build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

		return repository;
	}

	/*
	 *  The result contains pairs (rev_no, commit_msg)
	 */
	public Map<String, String> getCommitLogEntries( Set<String> _revs ) {
		final Map<String, String> hits = new HashMap<String, String>();
		if (!_revs.isEmpty() ) {
			try {
				Repository repository = this.getRepositoryFromPath( null );

				RevWalk walk = new RevWalk( repository );
				//walk.setRevFilter(RevFilter);
				RevCommit commit = null;

				Git git = new Git( repository );
				Iterable<RevCommit> logs = git.log().call();
				Iterator<RevCommit> i = logs.iterator();

				String commitId = null;
				String commitMsg = null;

				// iterate over all commits
				while ( i.hasNext() ) {
					commit = walk.parseCommit( i.next() );

					commitId = commit.getName();
					commitMsg = commit.getFullMessage();

					// iterate over all revisions to search for
					for ( String sid : _revs ) {
						if(sid.contains(":")){
							sid= sid.substring(0,sid.indexOf(":")-1);
						}
						if ( !sid.equals( "" ) && sid.equals( commitId ) ) {
							hits.put( commitId , commitMsg );
							continue;
						}
					}

				}
			}
			catch ( UnknownHostException e ) {
				GitClient.log.error( "Proxy issues?" );
				e.printStackTrace();
			}
			catch ( IOException ioe ) {
				GitClient.log.error( "Something went wrong with the I/O" );
				ioe.printStackTrace();
			}
			catch ( GitAPIException ge ) {
				GitClient.log.error( "Something went wrong with the GIT API" );
				ge.printStackTrace();
			}
		}
		return hits;
	}
	/*
	 * Important information here:
	 * http://www.eclipse.org/forums/index.php/t/213979/
	 * https://github.com/centic9/jgit-cookbook/blob/master/src/main/java/org/dstadler/jgit/api/ReadFileFromCommit.java
	 */
	public Set<FileChange> getFileChanges( String _rev ) {
		final StopWatch sw = new StopWatch("Get file changes for revision [" + _rev.substring( 0, (_rev.length()>8)?8:_rev.length() ) + "]").start();

		String branch = "";
		if(_rev.contains(":")){
			branch = _rev.substring(_rev.indexOf(":")+1, _rev.length())+":";
			_rev= _rev.substring(0,_rev.indexOf(":")-1);
		}

		// TODO: use this.repository instead
		Repository repository = this.getRepositoryFromPath( null );
		final Set<FileChange> changes = new HashSet<FileChange>();
		try {
			RevWalk rw = new RevWalk( repository );
			ObjectId commitId = repository.resolve( _rev  );
			RevCommit commit = rw.parseCommit( commitId );

			if ( commit.getParentCount() > 1 ) {
				GitClient.log.info( "[WARNING] Found multiple parents, I will only consider one (the first I can get...)." );
			}

			RevCommit parent = null;
			if ( commit.getParent( 0 ) != null ) {
				parent = rw.parseCommit( commit.getParent( 0 ).getId() );
			}

			DiffFormatter df = new DiffFormatter( DisabledOutputStream.INSTANCE );
			df.setRepository( repository );
			df.setDiffComparator( RawTextComparator.DEFAULT );
			df.setDetectRenames( true );
			List<DiffEntry> diffs = df.scan( parent.getTree(), commit.getTree() );

			File oldFile, newFile;
			DiffEntry.ChangeType changeType;
			String newPath = null;
			String oldPath = null;

			for ( DiffEntry entry : diffs ) {

				oldFile = null;
				newFile = null;

				newPath = entry.getNewPath() ;
				oldPath = entry.getOldPath() ;

				String parentRev = parent.getName();

				// Checkout file(s), depending on the modification type
				switch ( entry.getChangeType() ) {
				case MODIFY:
					GitClient.log.info("[Modified] " + branch+newPath);
					oldFile = this.checkoutFile( parentRev, oldPath );
					newFile = this.checkoutFile( _rev, newPath );

					if ( oldFile != null || newFile != null ) {
						changes.add( new FileChange(this.url, branch+newPath, oldFile, newFile ) );
					}
					break;
				case ADD:
					GitClient.log.info("[Created] " + branch+newPath);
					oldFile = this.checkoutFile( parentRev, oldPath );
					newFile = this.checkoutFile( _rev, newPath );

					if ( oldFile != null || newFile != null ) {
						changes.add( new FileChange(this.url, branch+newPath, oldFile, newFile ) );
					}
					break;
				case DELETE:
					GitClient.log.info("[Deleted] " + branch+newPath);
					oldFile = this.checkoutFile( parentRev, oldPath );
					newFile = this.checkoutFile( _rev, newPath );

					if ( oldFile != null || newFile != null ) {
						changes.add( new FileChange(this.url, branch+oldPath, oldFile, newFile ) );
					}
					break;
				case COPY:
					GitClient.log.info("[Copied] " + branch+newPath);
					oldFile = this.checkoutFile( parentRev, oldPath );
					newFile = this.checkoutFile( _rev, newPath );

					if ( oldFile != null || newFile != null ) {
						changes.add( new FileChange(this.url, branch+newPath, oldFile, newFile ) );
					}
					break;
				case RENAME:
					GitClient.log.info("[Moved] " + branch+newPath);
					oldFile = this.checkoutFile( parentRev, oldPath );
					newFile = this.checkoutFile( _rev, newPath );

					if ( oldFile != null || newFile != null ) {
						changes.add( new FileChange(this.url, branch+oldPath, oldFile, null ) );
						changes.add( new FileChange(this.url, branch+newPath, null, newFile ) );
					}
					break;
				default:
					GitClient.log.warn("[UNKNOWN CHANGE TYPE] " + branch+newPath);
					oldFile = null;
					newFile = null;
					break;
				}
				
				// Also try to checkout __init__.py files (which are needed to build the construct ID)
				this.checkoutPyInits(parentRev, oldPath);
				this.checkoutPyInits(_rev, newPath);

				// TODO what to do with directories?
			}
			sw.stop();
		}
		catch ( Exception e ) {
			sw.stop(e);
			GitClient.log.error("Error while checking out files: " + e.getMessage());
		}
		return changes;
	}

	/**
	 * TODO: To be implemented
	 */
	public Map<String, String> listEntries(String path, String _asof, String _until) throws Exception {
		Map<String, String> l = new HashMap<String, String>();
		return l;
	}

	private void checkoutPyInits(String _rev_branch, String _path) {
		// Only check for Python files
		if(!_path.endsWith("py"))
			return;

		// Checkout init in current dir
		Path py_file = Paths.get(_path);
		Path init_file = null;
		if(py_file.getParent()==null)
			init_file = Paths.get("__init__.py");
		else
			init_file = py_file.getParent().resolve("__init__.py");

		boolean exists = false;
		try {
			final File f = checkoutIfNotExists(_rev_branch, toGitPath(init_file));
			exists = f!=null;
		} catch(IllegalStateException ise) {
			log.warn("[" + init_file.toString() + "] does not exist in remote repo");
			exists = false;
		}

		// Checkout init in parent dir
		if(exists && init_file.getParent()!=null) {
			Path parent_init = null;
			if(init_file.getParent().getParent()==null)
				parent_init = Paths.get("__init__.py");
			else
				parent_init = init_file.getParent().getParent().resolve("__init__.py");
			checkoutPyInits(_rev_branch, parent_init.toString());
		}
	}
	
	private String toGitPath(Path _p) {
		final StringBuffer b = new StringBuffer();
		for(int i=0; i<_p.getNameCount(); i++) {
			b.append(_p.getName(i));
			if(i<_p.getNameCount()-1)
				b.append("/");
		}
		return b.toString();
	}

	/**
	 * Checks whether a file with the given path already exists in the local copy of the repo.
	 * If not, it attempts to check it out from the remote repo.
	 * @param _rev_branch
	 * @param _path
	 * @return
	 */
	private File checkoutIfNotExists(String _rev_branch, String _path) throws IllegalStateException {
		final String[] rev_branch = splitRevBranch(_rev_branch);
		File file = Paths.get(this.workDir.toString(), rev_branch[0], _path).toFile();
		if(!file.exists()) {
			log.info("Starting checkout of [" + _path + "]");
			file = this.checkoutFile(_rev_branch, _path);
		} else {
			log.info("[" + _path + "] already exists, no checkout needed");
		}
		return file;
	}

	private static String[] splitRevBranch(String _string) {
		final int idx = _string.indexOf(':'); 
		if(idx==-1) {
			return new String[] { _string, "" };
		} else {
			return new String[] { _string.substring(0, idx-1), _string.substring(idx) };
		}
	}

	/**
	 * Check out the file at path _path (relative to the repository root) at
	 * revision _rev
	 *
	 * @return File file object
	 */
	public File checkoutFile( String _rev_branch, String _path ) throws IllegalStateException,Exception {
		if ( _path.equals( "/dev/null" ) )
			return null;

		try {
			Repository repository = this.getRepositoryFromPath( null );
			//    Repository repository = this.repository;
			//

			// Split revision and potential branch info
			final String[] rev_branch = splitRevBranch(_rev_branch);
			/*if(_rev.contains(":")){
            	_rev= _rev.substring(0,_rev.indexOf(":")-1);
            }*/

			ObjectId commitId = repository.resolve( rev_branch[0] );
			// a RevWalk allows to walk over commits based on some filtering that is defined
			RevWalk revWalk = new RevWalk( repository );
			RevCommit commit = revWalk.parseCommit( commitId );
			// and using commit's tree find the path
			RevTree tree = commit.getTree();
			GitClient.log.info( "Having tree: " + tree );
			// now try to find a specific file
			TreeWalk treeWalk = new TreeWalk( repository );
			treeWalk.addTree( tree );
			treeWalk.setRecursive( true );
			treeWalk.setFilter( PathFilter.create( _path ) );
			if ( !treeWalk.next() ) {
				throw new IllegalStateException( "Did not find expected file '" + _path + "'" );
			}
			ObjectId objectId = treeWalk.getObjectId( 0 );
			ObjectLoader loader = repository.open( objectId );
			// and then one can the loader to read the file

			File targetFile = new File( this.workDir.toString() + "/" + rev_branch[0] + "/" + _path );
			File parentDir = targetFile.getParentFile(); // to get the parent dir
			java.nio.file.Files.createDirectories( parentDir.toPath() ) ;

			try (final OutputStream fos = new FileOutputStream( targetFile )) {
				/* loader.copyTo( System.out ); */
				loader.copyTo( fos );
			}
			revWalk.dispose();
			repository.close();

			return targetFile;

		}

		catch ( MissingObjectException e ) {
			// BUG - FIX: not sure why this happens
			GitClient.log.error( "Object not found for path [" + _path + "]");
			GitClient.log.error( "Maybe this is the content of a submodule? Skipping...." );
		}
		catch ( IllegalStateException e ) {
			throw e;
		}
		catch ( Exception e ) {
			log.error(e);
		}

		return null;
	}

	/*
	 *
	 */
	public Path checkout( String _rev ) {
		// checks out a specific _revision and puts it in a temp folder (Path)
		GitClient.log.error( "GitClient::checkout(String) not implemented yet." );
		return null;
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

		//TODO: To be tested
		return url.toString().replace(repository.toString(), "");
	}

	public long getRevisionTimeStamp(String _rev) throws Exception{
		if(_rev.contains(":")){
			_rev= _rev.substring(0,_rev.indexOf(":")-1);
		}
		long timeStamp = 0;
		RevCommit commit = null;
		try {
			Repository repository = this.getRepositoryFromPath( null );
			//    Repository repository = this.repository;

			ObjectId commitId = repository.resolve( _rev );
			// a RevWalk allows to walk over commits based on some filtering that is defined
			RevWalk revWalk = new RevWalk( repository );
			commit = revWalk.parseCommit( commitId );
			PersonIdent committerIdent = commit.getCommitterIdent();
			timeStamp = committerIdent.getWhen().getTime();

		}
		catch ( MissingObjectException e ) {
			// BUG - FIX: not sure why this happens
			// GitClient.log.error( "Object not found for path:" + _path );
			GitClient.log.error( "Maybe this is the content of a submodule? Skipping...." );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

		//return commit.getCommitTime();
		return timeStamp;
	}
}