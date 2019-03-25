package com.sap.psr.vulas.java.sign;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaClassId;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.JavaMethodId;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.sign.SignatureChange;

import javassist.ClassPool;
import javassist.NotFoundException;

public class ArchiveFixContainmentCheck {

	private static final Log log = LogFactory.getLog(ArchiveFixContainmentCheck.class);

	private String bugId = null;
	//private Archive archive = null;
	private JarAnalyzer ja = null;
	public static final ClassPool cp = new ClassPool();

	/**
	 * Caches the result of method {@link ArchiveFixContainmentCheck#containsFix()}.
	 */
	private Boolean isFixedArchive = null;

	private int constructsFixedCount = 0;
	private int constructsVulnerableCount = 0;

	private Map<ConstructId,ConstructFixContainmentCheck> constructFixes = new HashMap<ConstructId,ConstructFixContainmentCheck>();

	public ArchiveFixContainmentCheck(String _b, Path _p) throws IOException, IllegalArgumentException {
		if(_p==null)
			throw new IllegalArgumentException("Filesystem path to JAR is required");
		try {
			this.ja = new JarAnalyzer();
			this.ja.analyze(_p.toFile());
			ArchiveFixContainmentCheck.cp.appendClassPath(this.ja.getPath().toString());
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("JAR cannot be added to classpath: " + e.getMessage());
		} catch (FileAnalysisException e) {
			throw new IllegalArgumentException("JAR cannot be added to classpath: " + e.getMessage());
		}
		this.bugId   = _b;
	}
	/*
	public void addConstructChanges(Set<ConstructChange> _changes) {
		for(ConstructChange change: _changes) {
			this.addConstructChange(change);
		}
	}*/
	public void addConstructChanges(Set<ConstructChange> _changes) {
		for(ConstructChange change: _changes) {
			this.addConstructChange(change);
		}
	}

	/**
	 * Adds the given {@link ConstructChange} to the list of changes considered for the fix containment check.
	 * Changes of tests, however, will not be considered. Whether a construct is part of a test is determined
	 * based on the construct Id and the path.
	 * 
	 * //return this.isConstructInArchive() || this.isConstructClassInArchive() && !this.isNestedClassConstruct();
			return !this.isNestedClassConstruct();

	 * @param _construct_change
	 */
	public void addConstructChange(ConstructChange _construct_change) {
		final ConstructId cid = _construct_change.getConstructId();

		//
		if(this.isTestConstruct(cid) || this.isBelowTestDir(_construct_change.getRepoPath()) 
				|| this.isInTestClass(cid)){
			//if(this.isBelowTestDir(_construct_change.getRepoPath())) {
			ArchiveFixContainmentCheck.log.info("Change of construct [" +  cid.getQname() + "] ignored for fix containment check (assumed test)");
		}
		else {
			ConstructFixContainmentCheck check = this.constructFixes.get(cid);

			// Add check if not yet existing
			/*if(check==null) {
				check = new ConstructFixContainmentCheck(cid, this.ja);
				this.constructFixes.put(cid, check);
			}*/

			if(check==null) {
				check = new ConstructFixContainmentCheck(cid, this.ja, _construct_change);
				this.constructFixes.put(cid, check);
			}

			// Add change
			try {
				check.addConstructChange(_construct_change);
			} catch (IllegalArgumentException e) {
				ArchiveFixContainmentCheck.log.error("Construct change for [" + cid.getQname() + "]: " + e.getMessage());
			}	
		}
	}/* old
	public void addConstructChange(ConstructChange _construct_change) {
		final ConstructId cid = _construct_change.getConstruct();

		//
		if(this.isTestConstruct(cid) || this.isBelowTestDir(_construct_change.getPath()) || this.isInTestClass(cid)) {
			ArchiveFixContainmentCheck.log.info("Change of construct [" +  cid.getQualifiedName() + "] ignored for fix containment check (assumed test)");
		}
		else {
			ConstructFixContainmentCheck check = this.constructFixes.get(cid);

			// Add check if not yet existing
			if(check==null) {
				check = new ConstructFixContainmentCheck(cid, this.ja);
				this.constructFixes.put(cid, check);
			}

			// Add change
			try {
				check.addConstructChange(_construct_change);
			} catch (IllegalArgumentException e) {
				ArchiveFixContainmentCheck.log.error("Construct change for [" + cid.getQualifiedName() + "]: " + e.getMessage());
			}	
		}
	}*/

	private boolean isInTestClass(ConstructId _cid){
		return (_cid.getQname().indexOf("test")!=-1 || _cid.getQname().indexOf("Test")!=-1);
	}

	private boolean isTestConstruct(ConstructId _cid) {
		com.sap.psr.vulas.ConstructId c = JavaId.toCoreType(_cid);
		return c instanceof JavaMethodId &&  ((JavaMethodId)c).isTestMethod();
	}

	private boolean isBelowTestDir(String _p) {
		return _p!=null && (_p.indexOf("/testcases/")!=-1 || _p.indexOf("src/test/")!=-1);
	}


	/**
	 * An archive is considered to contain a security patch if all programming constructs affected by the patch
	 * do exist in the fixed revision (unless they are not contained in the archive). 
	 * @return
	 */
	public boolean containsFix() {
		if(this.isFixedArchive==null) {
			boolean archive_fixed = true, construct_fixed;
			int i=0; 
			for(ConstructId cid: this.constructFixes.keySet()) {
				ArchiveFixContainmentCheck.log.info("    -------- #" + String.valueOf(i++)); 
				ArchiveFixContainmentCheck.log.info("    Checking construct [" + cid.getQname() + "]");
				construct_fixed = this.constructFixes.get(cid).containsFix();

				// Update counters
				if(construct_fixed)
					this.constructsFixedCount++;
				else
					this.constructsVulnerableCount++;

				// Update overall result
				archive_fixed = archive_fixed && construct_fixed;
				ArchiveFixContainmentCheck.log.info("    Construct [" + cid.getQname() + "] fixed: [" + construct_fixed + "]");
			}
			this.isFixedArchive = new Boolean(archive_fixed);
		}
		return this.isFixedArchive;
	}

	public boolean containsAtLeastOneConstruct(){
		return ! ( this.constructFixes.isEmpty() );
	}

	public int getConstructsFixedCount() { return this.constructsFixedCount; }
	public int getConstructsVulnerableCount() { return this.constructsVulnerableCount; }

	/**
	 * Returns a JSON array representing the check results.
	 * @return
	 */
	public final JsonArray toJson() {
		final JsonArray affectedcc = new JsonArray();

		for ( Map.Entry<ConstructId, ConstructFixContainmentCheck> entry : constructFixes.entrySet()){
			JsonArray jsa = entry.getValue().toJson();
			for ( JsonElement e : jsa ){
				final JsonObject json = new JsonObject();
				json.add("cc", e); // cc
				json.addProperty("affected", !entry.getValue().isFixedConstruct);
				json.addProperty("inArchive", entry.getValue().isConstructInArchive());
				json.addProperty("classInArchive", entry.getValue().isConstructClassInArchive());
				json.addProperty("equalChangeType", entry.getValue().isEqualChangeTypeAccrossPaths());
				if(entry.getValue().getEqualChangeTypeAccrossPaths()!=null){
					ConstructChangeType a = entry.getValue().getEqualChangeTypeAccrossPaths();
					System.out.println(a);
					json.addProperty("overall_chg", entry.getValue().getEqualChangeTypeAccrossPaths().toString());
				}
				// add testedBody for each construct analyzed
				if(entry.getValue().getTestedAST()!=null)
					json.addProperty("testedBody", entry.getValue().getTestedAST());
				affectedcc.add(json);
			}
		}
		return affectedcc;
	}

	/**
	 * Given a construct and a fix (which consists of a set of commits for that construct,
	 * potentially on multiple different paths in the VCS), the class evaluates whether the
	 * construct body contains the fix or not.
	 *
	 */
	private static class ConstructFixContainmentCheck {

		private ConstructId constructId = null;
		private JarAnalyzer ja = null;
		private Boolean isFixedConstruct = null;

		// for debug/ final json
		private String repo = null;
		private String commit = null; 
		private String repoPath = null; 
		public String testedAST = null;        

		final Gson gson = GsonHelper.getCustomGsonBuilder().create();

		/**
		 * Commits (changes) per repository path.
		 */
		private Map<String, OrderedCommitsForPath> commits = new HashMap<String, OrderedCommitsForPath>();

		public ConstructFixContainmentCheck(ConstructId _c, JarAnalyzer _ja) {
			this.constructId = _c;
			this.ja = _ja;
		}
		public ConstructFixContainmentCheck(ConstructId _c, JarAnalyzer _ja, ConstructChange _cc) {
			this.constructId = _c;
			this.ja = _ja;
			this.repo = _cc.getRepo();
			this.repoPath = _cc.getRepoPath();
			this.commit = _cc.getCommit();
		}

		/**
		 * Returns true if the construct was only changed in one code line, e.g., trunk.
		 * @return
		 */
		private boolean isConstructFixedOnSinglePath() { return commits.size()==1; }

		/**
		 * Returns true if the construct was changed in multiple code lines, e.g., trunk and a given branch.
		 * @return
		 * @see #isConstructFixedOnSinglePath()
		 */
		private boolean isConstructFixedOnMultiplePaths() { return commits.size()>1; }

		/**
		 * Returns true if the construct is part of the archive, false otherwise.
		 * @return
		 */
		private boolean isConstructInArchive() {
			return this.ja.getConstructIds().contains(JavaId.toCoreType(this.constructId));
		}

		/**
		 * Returns true if the class of the construct is part of the archive, false otherwise.
		 * @return
		 */
		private boolean isConstructClassInArchive() {
			return this.ja.getConstructIds().contains(JavaId.toCoreType(this.constructId).getDefinitionContext());
		}

		private boolean isNestedClassConstruct() {
			final com.sap.psr.vulas.ConstructId c = JavaId.toCoreType(this.constructId).getDefinitionContext();
			if(c instanceof JavaClassId)
				return ((JavaClassId)c).isNestedClass();
			else
				return false;
		}

		/**
		 * Returns true if the construct or its class is contained in the archive. Only in that
		 * case the fix containment check makes sense. In other cases, e.g., test classes, the containment
		 * check must not succeed.
		 */
		/*		private boolean isCheckable() {
			//return this.isConstructInArchive() || this.isConstructClassInArchive() && !this.isNestedClassConstruct();
			return !this.isNestedClassConstruct();
		}*/

		public boolean isEqualChangeTypeAccrossPaths() {
			boolean is_equal = true;
			ConstructChangeType last_chg_type = null;
			for(OrderedCommitsForPath commits: this.commits.values()) {
				is_equal = last_chg_type==null || last_chg_type.equals(commits.getOverallChangeType());
				last_chg_type = commits.getOverallChangeType();
				if(!is_equal) break;
			}
			return is_equal;
		}

		public ConstructChangeType getEqualChangeTypeAccrossPaths() {
			if(!this.isEqualChangeTypeAccrossPaths())
				return null;
			else
				return this.commits.values().iterator().next().getOverallChangeType();
		}

		/**
		 * Returns true if the change type is MODified across all paths and the actual modifications are
		 * the same. Returns false otherwise.
		 * @return
		 */
		public boolean isEqualModAccrossPaths() {
			// Return false if the change type is not the same accross all the paths OR it is different from MOD
			if(!this.isEqualChangeTypeAccrossPaths() || !this.getEqualChangeTypeAccrossPaths().equals(ConstructChangeType.MOD)) {
				ArchiveFixContainmentCheck.log.warn("Change type not equal across paths or different from MOD");
				return false;
			}

			boolean is_equal = true;
			SignatureChange last_chg = null;
			for(OrderedCommitsForPath commits: this.commits.values()) {
				is_equal = last_chg==null || last_chg.equals(commits.getOverallChange());
				last_chg = commits.getOverallChange();
				if(!is_equal) break;
			}
			return is_equal;
		}

		public void addConstructChange(ConstructChange _construct_change) throws IllegalArgumentException {
			String path = _construct_change.getRepoPath();
			if(path==null) {
				ArchiveFixContainmentCheck.log.warn("Construct change with commit [" + _construct_change.getCommit()+ "] has not path information");
				path = "";
			}
			//throw new IllegalArgumentException("Cannot analyze construct fix containment without path information");

			OrderedCommitsForPath commits_per_path = this.commits.get(path);
			if(commits_per_path==null) {
				commits_per_path = new OrderedCommitsForPath(this.constructId, this.ja, path);
				this.commits.put(path,  commits_per_path);
			}
			commits_per_path.addConstructChange(_construct_change);
		}/* old
		public void addConstructChange(ConstructChange _construct_change) throws IllegalArgumentException {
			String path = _construct_change.getPath();
			if(path==null) {
				ArchiveFixContainmentCheck.log.warn("Construct change [" + _construct_change.getChangeID() + "|" + _construct_change.getRev() + "] has not path information");
				path = "";
			}
			//throw new IllegalArgumentException("Cannot analyze construct fix containment without path information");

			OrderedCommitsForPath commits_per_path = this.commits.get(path);
			if(commits_per_path==null) {
				commits_per_path = new OrderedCommitsForPath(this.constructId, this.ja, path);
				this.commits.put(path,  commits_per_path);
			}
			commits_per_path.addConstructChange(_construct_change);
		}*/

		private boolean containsFix(String _path) {
			boolean fixed = true;
			final OrderedCommitsForPath changes = this.commits.get(_path);

			// Case 1: Added as part of the fix
			if(!changes.isConstructExistedBeforeFirstCommit() && changes.isConstructExistsAfterLastCommit()) {
				// Does it exist now?
				// TODO: Compare entire signature
				fixed = this.isConstructInArchive();
				ArchiveFixContainmentCheck.log.info("        Change type [" + changes.getOverallChangeType() + "]: Construct added by fix [" + changes.getCommitsAsString() + "] and contained in current archive: [" + this.isConstructInArchive() + "], fixed: [" + fixed + "]");
			}			
			// Case 2: Deleted as part of the fix
			else if(changes.isConstructExistedBeforeFirstCommit() && !changes.isConstructExistsAfterLastCommit()) {
				// Does it exist now?
				// TODO: Compare entire signature
				fixed = !this.isConstructInArchive();
				ArchiveFixContainmentCheck.log.info("        Change type [" + changes.getOverallChangeType() + "]: Construct deleted by fix [" + changes.getCommitsAsString() + "] and contained in current archive: [" + this.isConstructInArchive() + "], fixed: [" + fixed + "]");
			}
			// Case 3: Modified as part of the fix
			else if(changes.isConstructExistedBeforeFirstCommit() && changes.isConstructExistsAfterLastCommit()) {
				fixed = this.isConstructInArchive() && (this.isNestedClassConstruct() || changes.containsFix());
				if ( (!this.isNestedClassConstruct()) && this.isConstructInArchive()){
					this.testedAST = changes.getConstructSignature().toJson();
				}
				if(this.isNestedClassConstruct()){
					ArchiveFixContainmentCheck.log.warn("        Bug in Procyon decompiler prevents the containment check for constructs in nested in class");
				}
				ArchiveFixContainmentCheck.log.info("        Change type [" + changes.getOverallChangeType() + "]: Construct modified by fix [" + changes.getCommitsAsString() + "] and archive revision contains fix: [" + fixed + "]");
			}
			// Case 4: Did not exist before, and does not after
			else {
				ArchiveFixContainmentCheck.log.warn("        Construct [" + this.constructId.getQname() + "] does not exist in vulnerable nor in fixed archive version");
			}			
			return fixed;
		}

		public boolean containsFix() {
			//try{
			if(this.isFixedConstruct==null) {
				boolean construct_fixed = false, path_fixed;

				ArchiveFixContainmentCheck.log.info("    Construct changed on [" + this.commits.size() + "] paths; " +
						"equal change type across paths: [" + this.isEqualChangeTypeAccrossPaths() + "]; " + 
						"change type: [" + (this.isEqualChangeTypeAccrossPaths() ? this.getEqualChangeTypeAccrossPaths() : "not applicable") + "]");

				// See whether the construct is equal to any of the before or after versions
				/*				OrderedCommitsForPath oc = null;
				for(String p: this.commits.keySet()) {
					ArchiveFixContainmentCheck.log.info("        ---- (1)");
					ArchiveFixContainmentCheck.log.info("        Checking path [" + p + "]");
					oc = this.commits.get(p);
					boolean eq;
					SignatureChange overall_change = oc.getOverallChange();
					if(overall_change!=null && overall_change.isStructuralChange()) {
						eq = oc.isEqualToVersionBeforeOrAfter(ASTUtil.NODE_COMPARE_MODE.ENTITY_TYPE);
						ArchiveFixContainmentCheck.log.info("        Equal tree structure: [" + eq + "]; Equal version: [" + (eq?oc.getEqualVersion(ASTUtil.NODE_COMPARE_MODE.ENTITY_TYPE):"not applicable") + "]");
					}
					else {
						eq = oc.isEqualToVersionBeforeOrAfter(ASTUtil.NODE_COMPARE_MODE.VALUE);
						ArchiveFixContainmentCheck.log.info("        Equal node values: [" + eq + "]; Equal version: [" + (eq?oc.getEqualVersion(ASTUtil.NODE_COMPARE_MODE.VALUE):"not applicable") + "]");
					}
				}
				 */
				if(this.isEqualChangeTypeAccrossPaths() && this.getEqualChangeTypeAccrossPaths().equals(ConstructChangeType.MOD)) {
					ArchiveFixContainmentCheck.log.info("        Equal change across paths: [" + this.isEqualModAccrossPaths() + "]");
				}

				// All change types are identical: If one is fixed it is alright
				if(this.isEqualChangeTypeAccrossPaths()) {			
					// Loop over all paths (in case there are multiple)
					log.info("Paths:\n" + this.commits.keySet().toString());
					for(String p: this.commits.keySet()) {
						ArchiveFixContainmentCheck.log.info("        ---- (2)");
						ArchiveFixContainmentCheck.log.info("        Checking path [" + p + "]");
						path_fixed = this.containsFix(p);
						construct_fixed = construct_fixed || path_fixed;
						ArchiveFixContainmentCheck.log.info("        Fix of path found: [" + path_fixed + "]");
					}
				}
				// Not all change types are identical: Compare signatures
				else {
					ArchiveFixContainmentCheck.log.info("        Different change types not yet supported");				
				}

				this.isFixedConstruct = new Boolean(construct_fixed);
			}
			/*} catch ( Exception mje ) {
                        // Temporary fix for malformed json exception thrown when ' \" ' is removed from some edit scripts
                        log.info(mje.toString());
                        this.isFixedConstruct = new Boolean(true);
                    }*/
			return this.isFixedConstruct; 
		}

		public JsonArray toJson() {
			JsonArray jscommits = new JsonArray();
			for (Entry e : commits.entrySet() ){
				OrderedCommitsForPath o = (OrderedCommitsForPath)e.getValue();
				for ( ConstructChange cc : o.getChanges() ){
					JsonObject json =  new JsonObject();
					json.addProperty("repo", cc.getRepo());
					json.addProperty("commit", cc.getCommit());
					json.addProperty("repoPath", cc.getRepoPath());
					json.add("constructId", gson.toJsonTree(cc.getConstructId()));
					jscommits.add(json);
				}
			}
			/*JsonObject json =  new JsonObject();
                    json.addProperty("repo", this.repo);
                    json.addProperty("commit", this.commit);
                    json.addProperty("repoPath", this.repoPath);
                    json.add("constructId", gson.toJsonTree(this.constructId));*/
			//old
			/*json.add("construct", gson.toJsonTree(this.constructId));
                    json.addProperty("in_archive", (this.isConstructInArchive() ? 1 : 0) );
                    json.addProperty("class_in_archive", (this.isConstructClassInArchive() ? 1 : 0) );
                    //json.addProperty("checkable", (this.isCheckable() ? 1 : 0) );
                    json.addProperty("fixed_construct", (this.containsFix() ? 1 : 0) );
                    json.addProperty("single_path_change", (this.isConstructFixedOnSinglePath() ? 1 : 0) );
                    json.addProperty("path_count", this.commits.size());
                    json.addProperty("equal_change_type", (this.isEqualChangeTypeAccrossPaths() ? 1 : 0) );
                    json.addProperty("overall_chg", ( this.getEqualChangeTypeAccrossPaths()==null ? "" : this.getEqualChangeTypeAccrossPaths().toString()) );

                    final JsonArray commits = new JsonArray();
                    for(OrderedCommitsForPath c: this.commits.values()) {
                            //commits.add(c.toJson());
                    }
                    //json.add("commits", commits);
			 */
			//return json;
			return jscommits;
		}

		public void setTestedAST(String _t){
			this.testedAST = _t;
		}

		public String getTestedAST(){
			return this.testedAST;
		}
	}
}
