package com.sap.psr.vulas.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.backend.BackendConnector;
import com.sap.psr.vulas.patcha.PatchAnalyzer;
import com.sap.psr.vulas.shared.json.JsonBuilder;
import com.sap.psr.vulas.shared.util.VulasConfiguration;


/**
 * http://localhost:8080/patchaWeb/pa?r=http%3A%2F%2Fsvn.apache.org%2Frepos%2Fasf%2Fcxf%2F&s=CVE-2013-0239
 *
 */
public class PatchaWeb extends HttpServlet {

	private static final Log log = LogFactory.getLog(PatchaWeb.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		try {
			// The URL of the VCS repository
			final String repo_url = request.getParameter("u");
			if(repo_url==null || repo_url.equals("")) throw new IllegalArgumentException("Repository URL not provided");
			
			final String collector_usr = request.getParameter("usr");
			final String collector_pwd = request.getParameter("pwd");			
			
			// The bug id must be provided in any case, it will be stored in the context
			final String bugid = request.getParameter("b");
			if(bugid==null || bugid.equals("")) throw new IllegalArgumentException("Bug Id not provided");
			
			// Action (search, identify or upload)
			String action = request.getParameter("a");
			if(action==null || (!action.equalsIgnoreCase("search")
							&& !action.equalsIgnoreCase("identify")
							&& !action.equalsIgnoreCase("upload")) ) throw new IllegalArgumentException("Unknown action: " + action);
			action = action.toLowerCase();
			
			// Revision number and search string
			final String[] revisions = request.getParameterValues("r");
			final String search_string = request.getParameter("s");
			
			// Delete tmp files
			final String del_temp_files_string = request.getParameter("t");
			final boolean del_temp_files = (del_temp_files_string==null?false:Boolean.parseBoolean(del_temp_files_string));
			
			// Search as of given date (if any)
			final String as_of_string = request.getParameter("d");
			Date as_of = null;
			if(as_of_string!=null && !as_of_string.equals("")) as_of = PatchaWeb.dateFormat.parse(as_of_string);
			
			// To where the results are uploaded
			final String coll_url = request.getParameter("c");
			
			// Get Patch Analyzer from session and update repo URL
			final HttpSession session = request.getSession(true);
			PatchAnalyzer pa = (PatchAnalyzer)session.getAttribute("pa");
			if(pa==null) {
				pa = new PatchAnalyzer(repo_url, bugid);
				session.setAttribute("pa", pa);
			}
			else {
				pa.setRepoURL(repo_url);
				pa.setBugId(bugid);
			}
			
//			// Classloader experiments
//			ClassLoader cl = this.getClass().getClassLoader(), parent_cl=cl;
//			System.out.println("Leaf class loader: " + cl.toString());
//			while( (parent_cl=parent_cl.getParent())!=null ) {
//				System.out.println("Parent class loader: " + parent_cl.toString());
//			}
			
			// The revisions from the VCS
			Map<String,String> revs = null;
			
			// Used for producing the output (JSON)
			StringBuilder b = null;
			int i = 0;
			
			// Search for revisions
			if(action.equals("search")) {
				
				// Check parameters (defined revisions and/or a search string)
				if( (revisions==null || revisions.length==0 || (revisions.length==1 && revisions[0].equals(""))) && (search_string==null || search_string.equals("")) )
					throw new IllegalArgumentException("Either revisions or search string must be provided");
				
				// Search for a given string (if provided)
				if(search_string!=null && !search_string.equals("")) {
					PatchaWeb.log.info("Starting the search for '" + search_string + "'");
					revs = pa.searchCommitLog(search_string, as_of);
				}
				
				// Complement search result with given revisions (if provided)
				if(revisions!=null) {
					final Set<String> revs2 = new HashSet<String>();
					for(String r : revisions)
						if(!r.equals("") && !revs.containsKey(r))
							revs2.add(r);
					if(revs2.size()>0) {
						PatchaWeb.log.info("Starting the retrieval of " + revs2.size() + " named revisions.");
						revs.putAll(pa.getCommitLogEntries(revs2));
					}
				}
					
				// Log the information
				b = new StringBuilder();
				i = 0;
				for(String rev: revs.keySet()) {
					b.append(rev);
					if(++i<revs.size()) b.append(", ");
				}
				PatchaWeb.log.info("Found " + revs.size() + " revision(s) for search string '" + search_string + "': " + b.toString());

				// Build JSON string to be returned to the client
				b = new StringBuilder();
				i = 0;
				b.append("{ \"revisions\" : [ ");
				for(String rev: revs.keySet()) {
					b.append("{ \"id\" : \"").append(rev).append("\", \"msg\" : ").append(JsonBuilder.escape(revs.get(rev))).append(" }");
					if(++i<revs.keySet().size()) b.append(", ");
				}
				b.append("] }");
				
				// Write response
				final String json = b.toString();
				response.setContentType("application/json");
				response.setContentLength(json.getBytes().length);
				out.print(json);
			}
			// Identify construct changes for given revisions
			else if(action.equals("identify")) {
				
				// Identify changes for all search hits
				Set<ConstructChange> changes = null;
				for(String rev: revisions) {
					if(rev!=null && !rev.equals("")) {
						PatchaWeb.log.info("Starting analysis of revision " + rev);
						changes = pa.identifyConstructChanges(rev);
					}
				}
				
				// Print JSON to output stream
				final String json = pa.toJSON(revisions);
				response.setContentType("application/json");
				response.setContentLength(json.getBytes().length);
				out.print(json);
			}
			else if(action.equals("upload")) {
				// Check and set upload Url
				if(coll_url==null || coll_url.equals(""))
					throw new IllegalArgumentException("Upload URL not provided");
				VulasConfiguration.getGlobal().setProperty("collector.url", coll_url);
				
				// Set credentials (if provided)
		    	//if(collector_usr!=null && collector_pwd!=null)
		    		//Collector.getInstance().setHttpCredentials(collector_usr, collector_pwd);
				
				// Upload the construct changes identified previously
				final String json = pa.toJSON(revisions);
				BackendConnector.getInstance().uploadChangeList(bugid, json);
			
				final String msg = "Upload successful";
				response.setContentType("text/plain");
				response.setContentLength(msg.getBytes().length);
				out.println(msg);
				
				// Identify all constucts that have not been touched and upload them as well
				//json = pa.prepareJSON4SourceConstructs(revisions[0]);
				//pa.storeAnalysisResults(coll_url, json);
				
				// Del. all temporary files created by the VCS checkout
				if(del_temp_files) pa.cleanup();
			}
		}
		catch(Exception e) {
			PatchaWeb.log.error("Exception during HTTP request processing: " + e.getMessage());
			final String msg = e.getMessage();
			response.setStatus(500); // This triggers the fail handler of the AJAX call
			response.setContentType("text/plain");
			response.setContentLength(msg.getBytes().length);
			out.println(msg);
		}
		out.flush();
		out.close();
	}
}
