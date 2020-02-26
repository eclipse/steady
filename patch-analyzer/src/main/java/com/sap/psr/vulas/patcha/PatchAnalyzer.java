/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.patcha;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.FileSearch;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StopWatch;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.vcs.FileChange;
import com.sap.psr.vulas.vcs.IVCSClient;
import com.sap.psr.vulas.vcs.NoRepoClientException;
import com.sap.psr.vulas.vcs.RepoMismatchException;


/**
 * <p>PatchAnalyzer class.</p>
 *
 */
public class PatchAnalyzer {

	private static final Log log = LogFactory.getLog(PatchAnalyzer.class);

	private String id = Double.toString(Math.random());
	private String bugid = null;
	private String bugDescription = null;
	private String bugLinks = null;
	private String search = null;
	private URL url = null;
	private IVCSClient vcs = null;
	private Map<String,Set<ConstructChange>> changes = new HashMap<String,Set<ConstructChange>>();
	private FileAnalyzer sourceConstructs = null;
	private String sourceRev = null;

	/**
	 * <p>Constructor for PatchAnalyzer.</p>
	 *
	 * @param _url a {@link java.lang.String} object.
	 * @param _bugid a {@link java.lang.String} object.
	 * @throws java.lang.IllegalArgumentException if any.
	 * @throws com.sap.psr.vulas.vcs.NoRepoClientException if any.
	 */
	public PatchAnalyzer(String _url, String _bugid) throws IllegalArgumentException, NoRepoClientException {
		// Check parameters
		try {
			this.setRepoURL(_url);
			this.setBugId(_bugid);
		} catch(MalformedURLException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	/**
	 * <p>setMetaInfo.</p>
	 *
	 * @param _descr a {@link java.lang.String} object.
	 * @param _link a {@link java.lang.String} object.
	 */
	public void setMetaInfo(String _descr, String _link) {
		this.bugDescription = _descr;
		this.bugLinks = _link;
	}

	/**
	 * Switches to a new VCS. Upon success, previous analysis results are lost.
	 *
	 * @param _url a {@link java.lang.String} object.
	 * @throws java.net.MalformedURLException if the given URL is invalid (previous results are kept)
	 * @throws com.sap.psr.vulas.vcs.NoRepoClientException if no VCS client can be created for the given URL (previous results are kept)
	 */
	public void setRepoURL(String _url) throws MalformedURLException, NoRepoClientException {
		final URL u = new URL(_url);
		if(!u.equals(this.url)) {
			final IVCSClient c = PatchAnalyzer.createVCSClient(u);
			// Previous state will be dropped (only if above instantiation worked)
			this.vcs = c;
			this.url = u;
			this.changes.clear();
			this.sourceConstructs = null;
			this.sourceRev = null;
		}
	}

	/**
	 * Sets the bug ID, which is associated to the changes when storing the results. In other words, the changes identified are supposed
	 * to fix the bug with the given identifier.
	 *
	 * @param _b a {@link java.lang.String} object.
	 */
	public void setBugId(String _b) { this.bugid = _b; }

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() { return this.id; }

	/**
	 * Creates and initializes a VCS client of a certain type (e.g., SVN or GIT) for a given URL.
	 *
	 * @param _url a {@link java.net.URL} object.
	 * @throws com.sap.psr.vulas.vcs.NoRepoClientException if client creation and initialization failed
	 * @return a {@link com.sap.psr.vulas.vcs.IVCSClient} object.
	 */
	public static IVCSClient createVCSClient(URL _url) throws NoRepoClientException {
		IVCSClient client = null;

		// Test all VCS clients listed in the configuration, if none works throw an exception.
		final String[] clients = VulasConfiguration.getGlobal().getConfiguration().getStringArray(PatchaConfiguration.PATCHA_VCS_CLIENTS);
		for(int i=0; i<clients.length; i++) {
			try {
				// Set the proxy default before every instantiation (SvnKit seems to alter the default and later calls to JGit fail)
				VulasProxySelector.registerAsDefault();
				final Class<?> c = Class.forName(clients[i]);
				client = (IVCSClient)c.newInstance();
				client.setRepoUrl(_url);
				client.setWorkDir(null);
			} catch (RepoMismatchException rme) {
				PatchAnalyzer.log.error(rme.getMessage());
				PatchAnalyzer.log.error("Root cause: " + rme.getCause().getMessage());
				client = null;
			} catch (Exception e) {
				PatchAnalyzer.log.error("Error when instantiating VCS client from class [" + clients[i] + "]: " + e.getMessage());
				client = null;
			}
			// Break if we succeeded
			if(client!=null) break;
		}

		// Throw exception if we did not find a client
		if(client==null)
			throw new NoRepoClientException(_url.toString());
		else
			return client;
	}

	/**
	 * <p>searchCommitLog.</p>
	 *
	 * @param _s a {@link java.lang.String} object.
	 * @param _asOf a {@link java.util.Date} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,String> searchCommitLog(String _s, Date _asOf) {
		this.search = _s;
		return this.vcs.searchCommitLog(_s, _asOf);
	}

	/**
	 * <p>getCommitLogEntries.</p>
	 *
	 * @param _revs a {@link java.util.Set} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String,String> getCommitLogEntries(Set<String> _revs) { //String[] _s) {
		return this.vcs.getCommitLogEntries(_revs);
	}

	/**
	 * <p>cleanup.</p>
	 */
	public void cleanup() { this.vcs.cleanup(); }

	/**
	 * Identifies all constructs that have been changed in the given revision. To that end, all files touched are downloaded from the remote repository,
	 * and afterwards compared according to the syntax of the respective programming language.
	 *
	 * @param _rev a {@link java.lang.String} object.
	 * @return a {@link java.util.Set} object.
	 */
	public Set<ConstructChange> identifyConstructChanges(String _rev) {
		Set<ConstructChange> ch = this.changes.get(_rev);
		if(ch==null) {
			ch = new TreeSet<ConstructChange>();

			//Get the time stamp information
			final String timeStamp = Long.toString(this.vcs.getRevisionTimeStamp(_rev));
			
			// Get and loop over all changed files
			final Set<FileChange> file_changes = this.vcs.getFileChanges(_rev);
			for(FileChange c: file_changes) {
				try {
					// Check if the file ext is supported
					if(FileAnalyzerFactory.isSupportedFileExtension(c.getFileExtension())) {
						final FileComparator comparator = new FileComparator(c, _rev,timeStamp);
						ch.addAll(comparator.identifyChanges());
					}
				} catch (Exception e) {
					PatchAnalyzer.log.error("Error while analyzing " + c + ": " + e.getMessage());
				}
			}

			//remove MOD classes if no MOD method is included (excluding modification in inner classes)
			//	ConstructChange[] cc = (ConstructChange[]) ch.toArray();
			ConstructChange[] ch_array = ch.toArray(new ConstructChange[ch.size()]);
			for (ConstructChange c: ch_array){
				ConstructId tocheck = c.getConstruct().getId();
				boolean toDelete = true;
				if(tocheck.getSharedType()==ConstructType.CLAS) {
					for(ConstructChange in : ch_array) {
						if(tocheck.equals(in.getConstruct().getId().getDefinitionContext())) {
							toDelete = false;
							break;
						}
					}
					if(toDelete) {
						ch.remove(c);
						PatchAnalyzer.log.info("Class [" + tocheck.toString() + "] removed from changeList as no METH/CONS included" );
					}
				}
			}		
			this.changes.put(_rev, ch);
		}
		return ch;
	}

	/**
	 * Identifies all programming constructs of a given revision number.
	 *
	 * @throws java.lang.Exception
	 * @param _rev a {@link java.lang.String} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<ConstructId,Construct> identifySourceConstructs(String _rev) throws Exception {
		if(!(this.sourceRev.equals(_rev)) || this.sourceConstructs==null) {
			// The revision for which the sources are analyzed
			this.sourceRev = _rev;

			// Check out the entire SVN
			final Path p = this.vcs.checkout(_rev);

			// Instantiate SrcAnalyzer (using the dir to which the checkout happened) to crawl all files
			this.sourceConstructs = FileAnalyzerFactory.buildFileAnalyzer(p.toFile());
		}
		return this.sourceConstructs.getConstructs();
	}

	/**
	 * Returns the union of changes for the given revisions. If null is passed as argument, all revisions will be returned.
	 * If an empty array is passed, an empty set will be returned.
	 * @return
	 */
	private Set<ConstructChange> getConsolidatedChanges(String [] _revs) {
		final Set<ConstructChange> ch = new TreeSet<ConstructChange>();
		for(Entry<String, Set<ConstructChange>> entry: this.changes.entrySet()) {
			if(_revs==null)
				ch.addAll(this.changes.get(entry.getKey()));
			else {
				for(String rev: _revs) {
					if(entry.getKey().equals(rev))
						ch.addAll(this.changes.get(entry.getKey()));
				}
			}
		}
		return ch;
	}

	/**
	 * <p>toJSON.</p>
	 *
	 * @param _revs an array of {@link java.lang.String} objects.
	 * @return a {@link java.lang.String} object.
	 * @throws java.util.ConcurrentModificationException if any.
	 */
	public String toJSON(String[] _revs) throws ConcurrentModificationException {
		final StringBuilder b = new StringBuilder();
		b.append(" { ");

		String repo = this.url.toString();
		if(repo.endsWith("/")) repo = repo.substring(0, repo.length()-1);
		b.append(" \"vcs\" : \"").append(repo).append("\", ");

		b.append(" \"bugId\" : \"").append(this.bugid).append("\", ");
		b.append(" \"maturity\" : \"" + ContentMaturityLevel.DRAFT.toString() + "\", ");
		b.append(" \"origin\" : \"" + BugOrigin.PUBLIC.toString() + "\", ");
		if(this.search!=null) b.append(" \"search\" : \"").append(this.search).append("\", ");

		// Add description and link if not null
		if(this.bugDescription!=null) {
			b.append(" \"descriptionAlt\" : ").append(JsonBuilder.escape(this.bugDescription)).append(", ");
		}

		if(this.bugLinks!=null){
			b.append(" \"reference\" : [");
			int i=0;

			// Take the link(s) provided
			final StringTokenizer t = new StringTokenizer(this.bugLinks, ",");
			while(t.hasMoreTokens()){
				if(i!=0){	b.append(", ");	}else	i++;
				b.append(JsonBuilder.escape(t.nextToken()));
			}

			//			for(String l: this.bugLinks){
			//				b.append(JsonBuilder.escape(l));
			//				if(++i<this.bugLinks.size()) b.append(", ");
			//			}
			b.append("], ");
		}

		b.append(" \"constructChanges\" : [ ");
		int i=0;
		final Set<ConstructChange> consol_ch = this.getConsolidatedChanges(_revs);
		for(ConstructChange c: consol_ch) {
			b.append(c.toJSON());
			if(++i<consol_ch.size()) b.append(", ");
		}
		b.append(" ] } ");
		return b.toString();
	}

	/**
	 * <p>main.</p>
	 *
	 * @param _args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] _args) {
		// Prepare parsing of cmd line arguments
		final Options options = new Options();
		options.addOption("r", "repo", true, "VCS repository URL");
		options.addOption("s", "search string", true, "Search string (optional, if not provided, the bugid will be used for searching the commit log)");
		options.addOption("b", "bug", true, "Bug identifier");
		options.addOption("e", "revision", true, "One or multiple revisions (optional, multiple ones must be comma-separated w/o blanks). In the case of Git repositories, the revision can be optionally concatened with ':' with the branch information.");
		options.addOption("links", "links", true, "Comma-separated list of links to comprehensive bug information (optional, only required for non-NVD vulnerabilities)");
		options.addOption("descr", "description", true, "Textual bug description (optional, only required for non-NVD vulnerabilities)");
		options.addOption("mr", "max-rev", true, "Maximum number of search results (revisions) analyzed (optional, default 5)");
		options.addOption("sie", "skip-if-existing", false, "Skips the analysis of a bug if it already exists in the backend");
		

		//options.addOption("f", "file", true, "File or directory with JSON files to be uploaded");

		// Boolean flags
		options.addOption("u", "upload", false, "Upload construct changes");
		options.addOption("v", "verbose", false, "Verbose mode");
		options.addOption("d", "delete", false, "Delete temporary files");

		// Parse exception
		try {
			// Parse cmd line arguments
			final CommandLineParser parser = new DefaultParser();
			final CommandLine cmd = parser.parse(options, _args);
			
			// Whether to upload JSON to the backend or save to the disk
			final boolean upload  = cmd.hasOption("u");
			VulasConfiguration.getGlobal().setProperty(CoreConfiguration.BACKEND_CONNECT, (upload ? CoreConfiguration.ConnectType.READ_WRITE.toString() : CoreConfiguration.ConnectType.READ_ONLY.toString()) );

			// Upload JSON files found on the disk
			if(cmd.hasOption("f")) {
				String file = cmd.getOptionValue("f");
				if(FileUtil.isAccessibleDirectory(file)) {
					final FileSearch fs = new FileSearch(new String[] {"json"});
					final Set<Path> files = fs.search(Paths.get(file));
					for(Path path: files) {
						PatchAnalyzer.uploadFile(path);
					}
				}
				else if(FileUtil.isAccessibleFile(file)) {
					PatchAnalyzer.uploadFile(Paths.get(file));
				}
				else {
					PatchAnalyzer.log.error("-f is required to refer to an accessible JSON file or directory (with JSON files): " + file);
				}
			}
			// Perform patch analysis and upload
			else {
				try {

					String url = null, str = null, bugid = null, rev = null, refsList=null;
					int max_rev = 5;
					if(cmd.hasOption("r")) url 	 = cmd.getOptionValue("r");
					if(cmd.hasOption("s")) str 	 = cmd.getOptionValue("s");
					if(cmd.hasOption("b")) bugid = cmd.getOptionValue("b");
					if(cmd.hasOption("e")) rev   = cmd.getOptionValue("e");
					if(cmd.hasOption("mr")) max_rev = Integer.parseInt(cmd.getOptionValue("mr"));

					// Boolean flags
					final boolean delete  = cmd.hasOption("d");
					final boolean verbose = cmd.hasOption("v");

					if(url==null || bugid==null)
						throw new IllegalArgumentException("The following options are mandatory: (r)epo and (b)ug");

					// Just stop if the bug already exists
					final boolean skip_if_existing = cmd.hasOption("sie");
					if(skip_if_existing && BackendConnector.getInstance().isBugExisting(bugid)) {
						PatchAnalyzer.log.info("Bug [" + bugid + "] already exists in backend, analysis will be skipped");
						return;
					}
					
					// Create instance of PatchAnalyzer
					final PatchAnalyzer pa = new PatchAnalyzer(url, bugid);
					pa.setMetaInfo(cmd.getOptionValue("descr"), cmd.getOptionValue("links"));

					// Revisions to be analyzed (commit no->commit msg)
					Map<String,String> revisions = null;

					// Take the revision(s) provided
					if(rev!=null) {
						revisions = new HashMap<String,String>();
						final StringTokenizer t = new StringTokenizer(rev, ",");
						while(t.hasMoreTokens())
							revisions.put(t.nextToken(), "");
					}
					// Search for revisions
					else {
						final String search_string = (str==null||str.equals("")?bugid:str);

						PatchAnalyzer.log.info("Starting the search for '" + search_string + "'");
						revisions = pa.searchCommitLog(search_string, null);
						if(revisions.size()==0) {
							PatchAnalyzer.log.info("No revision found for search string '" + search_string + "'");
							return;
						}
						else if(revisions.size()>0 && revisions.size()<max_rev) {
							final StringBuilder b = new StringBuilder();
							int i = 0;
							for(String r: revisions.keySet()) {
								b.append(r);
								if(++i<revisions.size()) b.append(", ");
							}
							PatchAnalyzer.log.info("Found " + revisions.size() + " revision(s) for search string '" + search_string + "': " + b.toString());
						}
						else {
							PatchAnalyzer.log.info("Found more than " + max_rev + " revisions matching the search string '" + search_string + "': " + revisions.size() + ". Please refine...");
							return;
						}
					}
					
					// Identify changes for all search hits
					final StopWatch sw = new StopWatch("Analysis of [" + revisions.size() + "] revision(s)");
					sw.setTotal(revisions.size()).start();

					Set<ConstructChange> changes = null;
					for(String r: revisions.keySet()) {
						changes = pa.identifyConstructChanges(r);
						sw.progress();
						// If verbose mode, print all changes
						if(verbose) {
							for(ConstructChange chg : changes) {
								PatchAnalyzer.log.info(chg.toString());
							}
						}
					}
					sw.stop();

					// Upload or store construct changes
					final String[] rev_array = revisions.keySet().toArray(new String[revisions.keySet().size()]);
					final String json = pa.toJSON(rev_array);

					// Upload analysis results
					BackendConnector.getInstance().uploadChangeList(bugid, json);

					// Cleanup temp. files
					if(delete) pa.cleanup();
				} catch (Exception e) {
					PatchAnalyzer.log.error(e.getMessage());
					System.exit(127);
				}
			}
		}
		catch(ParseException pe) {
			PatchAnalyzer.log.error(pe.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "PatchAnalyzer", options );
			System.exit(125);
		}
	}

	private static void uploadFile(Path _path) {
		/*try {
			PatchAnalyzer.log.info("Found file [" + _path + "]");
			final String json = FileUtil.readFile(_path);
			Map querystring = new HashMap();
			querystring.put("source","PRE_COMMIT_POM");
			BackendConnector.getInstance().upload(HttpMethod.POST, BackendConnector.filenameToPath(_path), querystring, json, true);
		} catch (Exception e) {
			PatchAnalyzer.log.error("Error uploading file [" + _path + "]: " + e.getMessage(), e);
		}*/
	}
}
