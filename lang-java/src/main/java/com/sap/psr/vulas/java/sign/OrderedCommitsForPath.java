package com.sap.psr.vulas.java.sign;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sap.psr.vulas.core.util.SignatureConfiguration;
import com.sap.psr.vulas.java.JarAnalyzer;
import com.sap.psr.vulas.java.JavaId;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureChangeDeserializer;
import com.sap.psr.vulas.java.sign.gson.ASTSignatureDeserializer;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.shared.json.model.ConstructId;
import com.sap.psr.vulas.shared.util.VulasConfiguration;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;
import com.sap.psr.vulas.sign.SignatureComparator;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;

/**
 * Contains a serious of {@link ConstructChange}s for a given {@link ConstructId}, whereby all of the changes
 * concern the same software repository path (e.g., /trunk/src/main/java/A.java). The class
 * offers a couple of convenience methods to understand the overall change introduced by the
 * single changes.
 * More importantly, the method {@link OrderedCommitsForPath#containsFix()} analyzes whether the construct under analysis
 * ({@link OrderedCommitsForPath#constructId}) contains the fixes or not.
 */
public class OrderedCommitsForPath {

	private static final Log log = LogFactory.getLog(OrderedCommitsForPath.class);

	public static enum Version { BEFORE, AFTER };

	private final JavaSignatureFactory signatureFactory = new JavaSignatureFactory();
	private final SignatureComparator signComparator = new ASTSignatureComparator();
	private final ASTSignatureDeserializer astSignDeserializer = new ASTSignatureDeserializer();
	private final ASTSignatureChangeDeserializer astSignChangeDeserializer = new ASTSignatureChangeDeserializer();

	private SortedSet<ConstructChange> changes = new TreeSet<ConstructChange>();

	private JarAnalyzer ja = null;

	/**
	 * The ID of the constuct under analysis.
	 */
	private ConstructId constructId = null;

	private String path = null;

	final Gson gson = GsonHelper.getCustomGsonBuilder().create();

	private Boolean isFixed = null;

	private boolean relaxedEntityComparison = false;

	/**
	 * 
	 * @param _c
	 * @param _ja
	 */
	public OrderedCommitsForPath(ConstructId _c, JarAnalyzer _ja, String _p) {
		this.constructId = _c;
		this.ja = _ja;
		this.path = _p;
	}	

	public void addConstructChange(ConstructChange _construct_change) {
		this.changes.add(_construct_change);
	}

	public boolean isConstructExistedBeforeFirstCommit() {
		if(this.changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.first().getConstructChangeType().equals(ConstructChangeType.ADD);
	}

	public boolean isConstructExistsAfterLastCommit() {
		if(this.changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.last().getConstructChangeType().equals(ConstructChangeType.DEL);
	}

	/**
	 * Returns the type of change considering all commits.
	 * @return
	 */
	public ConstructChangeType getOverallChangeType() {
		if(this.isConstructExistedBeforeFirstCommit() && this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.MOD;
		}
		else if(this.isConstructExistedBeforeFirstCommit() && !this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.DEL;
		}
		else if(!this.isConstructExistedBeforeFirstCommit() && this.isConstructExistsAfterLastCommit()) {
			return ConstructChangeType.ADD;
		}
		// Strange case: A construct has been added as part of a commit, but removed later on.
		// Example: CVE-2012-2098, repo http://svn.apache.org/repos/asf/commons/proper/compress
		// 			Method org.apache.commons.compress.compressors.bzip2.BlockSort.randomiseBlock(Data,int)
		// 			Added as part of commit 1332540, deleted as part of commit 1340790
		else {
			OrderedCommitsForPath.log.info("Construct [" + this.constructId + "] only existed temporarily (during fix development) and will be ignored for the fix containement check");
			return ConstructChangeType.NUL;
		}
	}

	/**
	 * If the overall change type is MOD(ified), the method returns the signature change considering
	 * all commits. In the other cases, it returns null.
	 * @return
	 */
	public ASTSignatureChange getOverallChange() {
		ASTSignatureChange chg = null;
		if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
			if(this.changes.first().getBuggyBody()!=null && this.changes.last().getFixedBody()!=null) {
				final Signature before = this.getBefore();
				final Signature after  = this.getAfter();

				//String bef_str = before.toJson();
				//String aft_str = after.toJson();

				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = (ASTSignatureChange)comparator.computeChange(before, after);
			}
		}
		return chg;        		
	}

	/**
	 * If the overall change type is MOD(ified), the method returns the signature change
	 * to go from the defective signature to the given one.
	 * @param _to
	 * @return
	 */
	public SignatureChange fromDefective(Signature _to) {
		SignatureChange chg = null;
		if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
			if(this.changes.first().getBuggyBody()!=null) {
				final Signature before = this.getBefore();
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = comparator.computeChange(before, _to);
			}
		}
		return chg;		
	}

	public ASTSignatureChange toFixed(Signature _from) {
		ASTSignatureChange chg = null;
		if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
			if(this.changes.last().getFixedBody()!=null) {
				final Signature after  = this.getAfter();
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = (ASTSignatureChange)comparator.computeChange(_from, after);
			}
		}
		return chg;		
	}

	public ASTSignatureChange getLastChange() {
		ASTSignatureChange change = null;
		// Deserialize string
		final String change_as_string = this.changes.last().getBodyChange();  //Get the SignatureChange , String Representation
		if(change_as_string != null){
			change = this.gson.fromJson(change_as_string, ASTSignatureChange.class);
		}                
		return change;
	}

	private Signature getBefore() {
		Signature s = null;
		if(this.changes.first().getBuggyBody()!=null){
			s = this.gson.fromJson(this.changes.first().getBuggyBody(), ASTConstructBodySignature.class);
		}
		return s;
	}

	private Signature getAfter() {
		Signature s = null;
		if(this.changes.last().getFixedBody()!=null){
			s = this.gson.fromJson(this.changes.last().getFixedBody(), ASTConstructBodySignature.class);
		}
		return s;
	}

	public boolean isEqual(ASTUtil.NODE_COMPARE_MODE _mode, OrderedCommitsForPath.Version _version) {
		boolean equal = false;
		final ASTSignature construct_signature = (ASTSignature)this.getConstructSignature();

		// The version to compare to (before or after)
		ASTSignature version = null;
		if(_version==Version.BEFORE)
			version = (ASTSignature)this.getBefore();
		else
			version = (ASTSignature)this.getAfter();

		if(construct_signature!=null && version!=null) {
			equal = ASTUtil.isEqual(construct_signature.getRoot(), version.getRoot(), _mode);
		}
		return equal;
	}

	public boolean isEqualToVersionBeforeOrAfter(ASTUtil.NODE_COMPARE_MODE _mode) {
		return this.isEqual(_mode, Version.BEFORE) || this.isEqual(_mode, Version.AFTER);
	}

	public Version getEqualVersion(ASTUtil.NODE_COMPARE_MODE _mode) {
		if(this.isEqualToVersionBeforeOrAfter(_mode)) {
			if(this.isEqual(_mode, Version.BEFORE))
				return Version.BEFORE;
			else if(this.isEqual(_mode, Version.AFTER))
				return Version.AFTER;
			else
				throw new IllegalStateException("Signature supposed to be equal to BEFORE or AFTER versions of the overall change");
		}
		else {
			return null;
		}
	}

	public String getCommitsAsString() {
		final StringBuffer b = new StringBuffer();
		int i = 0;
		for(ConstructChange chg: this.changes) {
			if(i++>0) b.append(",");
			b.append(chg.getCommit());
		}
		return b.toString();
	}

	public Signature getConstructSignature() {
		return signatureFactory.createFromCtClass(this.constructId, ArchiveFixContainmentCheck.cp);
	}

	/**
	 * 
	 * @return
	 */
	public boolean containsFix() {
		if(this.isFixed==null) {
			boolean fixed = false;

			// Not supported by SignatureFactory
			if(!JavaSignatureFactory.isSupported(this.constructId, false)) {
				this.isFixed = new Boolean(true);
			} 
			else {
				String className = JavaId.toCoreType(constructId).getDefinitionContext().getName();
				// Last and overall change to be compared to the signature under analysis
				final ASTSignatureChange last_change    = this.getLastChange();
				final ASTSignatureChange overall_change;
				overall_change = this.getOverallChange();

				if ( !isFollowingCommit(this.changes.first(), this.changes.last()) ){
					log.info("Not subsequent commits. There is ( at least ) one commit in between!");
					//getActualOverallChange(overall_change);
					removeIntermediateCommits(overall_change);
				}

				final String last_change_json = last_change.toJSON();
				final String overall_change_json = overall_change.toJSON();

				// Are last and overall changes equals
				boolean is_equal = last_change.equals(overall_change);

				// Structural changes
				boolean last_structural_change    = last_change.isStructuralChange();
				boolean overall_structural_change = overall_change.isStructuralChange();

				// Last and overall changes contained
				//final boolean last_fixed    = false; //signComparator.containsChange(construct_signature, last_change);	
				//final boolean overall_fixed = signComparator.containsChange(construct_signature, overall_change);

				// Get and compare edit scripts Vulnerable->Test and Test->Fixed
				final SignatureChange ddt = this.fromDefective(this.getConstructSignature());
				final SignatureChange dtf = this.toFixed(this.getConstructSignature());

				/* decompiled construct */
				/*if ( VulasConfiguration.getSingleton().getConfiguration().getBoolean(SignatureConfiguration.SHOW_DECOMPILED_CONSTRUCT) ){
                        Construct c = signatureFactory.getConstructFromCache(this.constructId);
                        log.info("Decompiled construct: \n"+c.getContent());
                    }*/
				/* 
                    save to disk in case the value -DsaveEditScripts in CL has been set
				 */
				if ( VulasConfiguration.getGlobal().getConfiguration().getBoolean(SignatureConfiguration.SAVE_EDIT_SCRIPTS) ){
					writeToFile(overall_change_json, "overallChange");
					writeToFile(ddt.toJSON(), "ddt");
					writeToFile(dtf.toJSON(), "dtf");
				}

				if ( VulasConfiguration.getGlobal().getConfiguration().getBoolean(SignatureConfiguration.RELAXED_BY_DEFAULT)) {
					this.relaxedEntityComparison = true;
				}

				// Compare 1 to 1
				OrderedCommitsForPath.log.info(" Edit script intersection (defective -> test):");
				// split getmodifications to have an easier debug
				Set<SourceCodeChange> ddtModifications = ASTSignatureChange.toSourceCodeChanges(ddt.getModifications());
				Set<SourceCodeChange> overallChangeModifications = ASTSignatureChange.toSourceCodeChanges(overall_change.getModifications());
				//Set<Object> i_dt = ASTUtil.intersectSourceCodeChanges(ddtModifications, overallChangeModifications, this.relaxedEntityComparison);
				Set<Object> i_dt = ASTUtil.intersectSourceCodeChanges(ddtModifications, overallChangeModifications, this.relaxedEntityComparison, className);

				writeMissingIntersectionToConsole(i_dt, ASTSignatureChange.toSourceCodeChanges(overall_change.getModifications()));

				log.info("Confidence(#i_dt/#oc): " + ((float)i_dt.size()/overallChangeModifications.size())*100 + "%");

				OrderedCommitsForPath.log.info(" Edit script intersection (test -> fixed):");
				//Set<Object> i_tf = ASTUtil.intersectSourceCodeChanges(dtf.getModifications(), overall_change.getModifications(), this.relaxedEntityComparison);
				Set<Object> i_tf = ASTUtil.intersectSourceCodeChanges(dtf.getModifications(), overall_change.getModifications(), this.relaxedEntityComparison, className);
				if ( i_tf.size() != 0 ){
					log.info("Confidence(#i_tf/#oc): " + ((float)i_tf.size()/overallChangeModifications.size())*100 + "%");
				}
				// Compare relaxed (only if both are empty)
				if(i_tf.size()==0 && i_dt.size()==0 && overallChangeModifications.size()!=0 ) { 
					this.relaxedEntityComparison = true;
					OrderedCommitsForPath.log.info(" Relaxed edit script intersection (defective -> test):");
					i_dt = ASTUtil.intersectSourceCodeChanges(ddt.getModifications(), overall_change.getModifications(), this.relaxedEntityComparison);
					OrderedCommitsForPath.log.info(" Relaxed edit script intersection (test -> fixed):");
					i_tf = ASTUtil.intersectSourceCodeChanges(dtf.getModifications(), overall_change.getModifications(), this.relaxedEntityComparison);
				}

				//save edit script comparisons to file, if necessary
				if ( isSaveEditScriptIntersection() ){
					writeIntersectionToFile(i_dt, i_tf, this.path);
				}

				// patch CVE-2012-0838
				boolean fixed_script_comparison = overall_change.getModifications().size() == 0 || (i_dt.size()>0 && i_tf.size()==0);

				//boolean fixed_script_comparison = i_dt.size()>0 && i_tf.size()==0;
				boolean vuln_script_comparison = i_dt.size()==0 && i_tf.size()>0;

				OrderedCommitsForPath.log.info("                 Number of commits: [" + this.changes.size() + "], Last change and overall change equal: [" + is_equal + "]");
				OrderedCommitsForPath.log.info("                 Contain structural changes: Last change: [" + last_structural_change + "], Overall change: [" + overall_structural_change + "]");
				//OrderedCommitsForPath.log.info("                 Changes contained: Last change [" + last_fixed + "], Overall change: [" + overall_fixed + "]");
				OrderedCommitsForPath.log.info("                 Edit script comparison: Vuln [" + vuln_script_comparison + "], Fixed: [" + fixed_script_comparison + "]");

				// Attention: Move is also considered as structural change. However, after the move of two src code entities of the same type, the structure is still the same!
				if(overall_structural_change) {
					final boolean eq_structure_before = this.isEqual(ASTUtil.NODE_COMPARE_MODE.ENTITY_TYPE, Version.BEFORE);
					final boolean eq_structure_after  = this.isEqual(ASTUtil.NODE_COMPARE_MODE.ENTITY_TYPE, Version.AFTER);
					OrderedCommitsForPath.log.info("                 Equal tree structure: Before [" + eq_structure_before + "], After: [" + eq_structure_after + "]");
				}
				else {
					final boolean eq_before = this.isEqual(ASTUtil.NODE_COMPARE_MODE.VALUE, Version.BEFORE);
					final boolean eq_after  = this.isEqual(ASTUtil.NODE_COMPARE_MODE.VALUE, Version.AFTER);
					OrderedCommitsForPath.log.info("                 Equal node values: Before [" + eq_before + "], After: [" + eq_after + "]");
				}

				this.isFixed = new Boolean(fixed_script_comparison);
			}
		}
		return this.isFixed;
	}

	public JsonElement toJson() {
		final JsonObject json =  new JsonObject();
		json.addProperty("path", this.path);
		final JsonArray revs = new JsonArray();
		for(ConstructChange c: this.changes) {
			revs.add(new JsonPrimitive(c.getCommit()));
		}
		json.add("revisions", revs);
		final boolean is_mod = this.getOverallChangeType().equals(ConstructChangeType.MOD);
		json.addProperty("is_modification", (is_mod ? 1 : 0) );

		if(is_mod) {
			json.addProperty("fixed", (this.containsFix() ? 1 : 0) );
			json.addProperty("relaxed_comparison", (this.relaxedEntityComparison ? 1 : 0) );
		}
		return json;
	}

	private void writeToFile(String _et, String _etName){
		final String path = VulasConfiguration.getGlobal().getTmpDir().toString();
		final Date dnow = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
		// save to path\construct_etname_date_time.json
		final String fname = getFileNameFromPath(this.path) + "_" + _etName + "_" + simpleDateFormat.format(dnow);
		final String pname = getFullPathName(this.path);
		final File file = new File(path, fname + ".json" );
		try (final Writer output = new BufferedWriter(new FileWriter(file))){
			output.write(pname);
			output.write("\n");
			output.write(_et);
			log.info("File " + file.getAbsolutePath() + " correctly written");
		} catch (IOException ex) {
			log.error(ex);
		}
	}

	private void writeIntersectionToFile(Set<Object> _idt, Set<Object> _itf, String _etName){
		final String path = VulasConfiguration.getGlobal().getTmpDir().toString();
		final Date dnow = new Date();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
		// save to path\construct_etname_date_time.json
		final String fname = getFileNameFromPath(this.path) + "_"+ _etName + "_" + simpleDateFormat.format(dnow);
		final String pname = getFullPathName(this.path);
		final File file = new File(path, pname + ".json" );
		try (final Writer output = new BufferedWriter(new FileWriter(file))) {
			output.write(pname);
			output.write("\n" + "### Defective->Test\n");
			for(Object o1: _idt) {
				output.write(o1.toString()+"\n");    
			}                
			output.write("\n" + "### Test->Fixed\n");
			for(Object o1: _itf) {
				output.write(o1.toString()+"\n");    
			}  
			output.write("\n");
			log.info("File " + file.getAbsolutePath() + " correctly written");
		} catch (IOException ex) {
			log.error(ex);
		}
	}

	/**
	 * Logs to console the missing intersection ( usually to be used between overall_change and i_dt )
	 * @param _idt
	 * @param _oc 
	 */
	private void writeMissingIntersectionToConsole(Set<Object> _idt, Set<SourceCodeChange> _oc){
		StringBuilder sb = new StringBuilder();
		boolean contained;
		for ( Object o1 : _oc ){
			contained=false;
			for(Object o2 : _idt ){
				if ( o1.equals(o2) ){
					contained=true;
				}
			}
			if(!contained){
				sb.append(o1.toString());
				sb.append("\n");
			}
		}
		log.info("Missing changes: \n" + sb.toString());
	}

	/**
	 * Checks and logs if there is more than one commit fixing this construct 
	 */
	private void checkIfManyCommits(){
		if ( this.changes.size() != 1 ){
			log.info("more than one commit found fixing this construct");
			checkFollowingCommits();
			log.info(getCommitsAsString());
			for ( ConstructChange cc1 : this.changes ){
				log.info("Commit: " + cc1.getCommit());
				SignatureChange diffChange = getDiffChange(cc1);
				log.info("diffchange . get modifications");
				for (  SourceCodeChange o1 : ASTSignatureChange.toSourceCodeChanges(diffChange.getModifications()) ) {
					log.info(o1.toString());
				}
			}
		}
	}

	/**
	 * Returns the difference between the versions before and after for a construct change (commit)
	 */
	private SignatureChange getDiffChange(ConstructChange _cc1) {
		SignatureChange chg = null;
		if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
			if(_cc1.getBuggyBody()!=null && _cc1.getFixedBody()!=null) {
				Signature s1 = this.gson.fromJson(_cc1.getBuggyBody(), ASTConstructBodySignature.class);
				Signature s2 = this.gson.fromJson(_cc1.getFixedBody(), ASTConstructBodySignature.class);
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = comparator.computeChange(s1, s2);
			}
		}
		return chg;		
	}

	/**
	 * Checks if two commits have no intermediate commits between them and logs it to console 
	 */
	private void checkFollowingCommits(){
		for(int i=1; i<this.changes.size(); i++){
			boolean isFollowing = isFollowingCommit(Iterables.get(changes, i-1), Iterables.get(changes, i));
			log.info("Commits " + Iterables.get(changes, i-1).getCommit() + 
					" and " + Iterables.get(changes, i).getCommit() + " are " + (isFollowing ? "" : "not")
					+ " sequential");
		}
	}

	/**
	 * Returns true if 2 commits have no intermediate commit between them, false otherwise 
	 */
	private boolean isFollowingCommit(ConstructChange _cc1, ConstructChange _cc2){
		if ( _cc1.equals(_cc2) ){
			return true;
		}
		if ( _cc1.getFixedBody() != null && _cc2.getBuggyBody() != null ){
			SignatureChange chg = null;
			Signature s1 = this.gson.fromJson(_cc1.getFixedBody(), ASTConstructBodySignature.class);
			Signature s2 = this.gson.fromJson(_cc2.getBuggyBody(), ASTConstructBodySignature.class);
			final SignatureComparator comparator = new ASTSignatureComparator();
			chg = comparator.computeChange(s1, s2);
			return chg.getModifications().isEmpty();
		}
		return false;
	}

	/**
	 * Returns the real overall change if there is more than one commit
	 */
	@Deprecated
	private void getActualOverallChange(ASTSignatureChange _oc){
		if ( this.changes.size() != 1 && this.getOverallChangeType().equals(ConstructChangeType.MOD)){
			Set<SourceCodeChange> commons=new HashSet<SourceCodeChange>();
			Set<SourceCodeChange> toRemove = ASTSignatureChange.toSourceCodeChanges(_oc.getModifications());
			log.info("getActualOverallChange()");
			log.info(getCommitsAsString());

			for ( int i=1; i<this.changes.size(); i++ ){
				ConstructChange ccBefore = Iterables.get(changes, i-1);
				ConstructChange ccAfter = Iterables.get(changes, i);
				commons.addAll(unionCommits(ccBefore, ccAfter));
			}  

			for (SourceCodeChange scc : toRemove ){
				_oc.removeChange(scc);
			}

			for(SourceCodeChange scc : commons){
				_oc.addChange(scc);
			}

			log.info("New overall change \n" + commons.toString());
		}
	}

	/**
	 * Used in the @Deprecated getActualOverallChange
	 * Returns the set with the union of the changes added by two different commits.
	 */
	private Set<SourceCodeChange> unionCommits(ConstructChange ccBefore, ConstructChange ccAfter){
		Set<SourceCodeChange> commons = new HashSet<SourceCodeChange>();
		if ( ! isFollowingCommit(ccBefore, ccAfter) ){
			SignatureChange before = getDiffChange(ccBefore);
			SignatureChange after = getDiffChange(ccAfter);
			for ( SourceCodeChange fromBefore : ASTSignatureChange.toSourceCodeChanges(before.getModifications()) ){
				commons.add(fromBefore);
			}
			for (  SourceCodeChange fromAfter : ASTSignatureChange.toSourceCodeChanges(after.getModifications()) ) {
				// if it is an update, check if the other commit is fixing the same line
				if (fromAfter.getClass().getSimpleName().compareToIgnoreCase("update")==0){
					for ( SourceCodeChange fromBefore : ASTSignatureChange.toSourceCodeChanges(before.getModifications()) ){
						if ( (fromBefore.getClass().getSimpleName().compareToIgnoreCase("update")==0) &&
								((Update)fromAfter).getChangedEntity().equals(((Update)fromBefore).getNewEntity()) ){
							log.info("updated: " + 
									((Update)fromBefore).getChangedEntity() + " --> " +  ((Update)fromBefore).getNewEntity() +
									" --> " + ((Update)fromAfter).getNewEntity());
							// signatureChange.addChange(fromAfter);
							// keep the most recent one
							commons.remove(fromBefore);
							commons.add(fromAfter); 
						}
					}
				}
				// for now, if it is not an update, simply add it to the list of changes
				//signatureChange.addChange(fromAfter);
				commons.add(fromAfter);
			}  
		}
		return commons;
	}

	/**
	 * Modifies overall change.
	 * Given a series of commits for a method, aims at finding only the changes necessary for the patch ( and so remove 
	 * the ones not interested by the patch ).
	 * @param _oc 
	 */
	private void removeIntermediateCommits(ASTSignatureChange _oc ){
		Set<SourceCodeChange> finalSet = new HashSet<SourceCodeChange>();
		Set<SourceCodeChange> toRemove = ASTSignatureChange.toSourceCodeChanges(_oc.getModifications());
		// important to ingore source code range!!
		SourceCodeEntity.setIgnoreSourceRange(true);
		finalSet.addAll(toRemove);
		for( int i=1; i<this.changes.size(); i++){
			ConstructChange precedent = Iterables.get(this.changes, i-1);
			ConstructChange following = Iterables.get(this.changes, i);
			if ( ! isFollowingCommit(precedent, following) ){ 
				// !isFollowing means that intersection precedent.after and following.before is empty
				// compute difference between precedent.after and following.before
				SignatureChange chg = this.getDiffChange(precedent, following);
				Set<SourceCodeChange> deltaNonFix = ASTSignatureChange.toSourceCodeChanges(chg.getModifications());
				// Overall change = overall change - delta non fix
				for ( SourceCodeChange contained : ASTSignatureChange.toSourceCodeChanges(_oc.getModifications()) ){ 
					for ( SourceCodeChange notfix : deltaNonFix ) {
						if ( contained.equals(notfix) ){
							finalSet.remove(contained);
							continue;
						}
					}
				}
			}
		}
		SourceCodeEntity.setIgnoreSourceRange(false);
		for (SourceCodeChange scc : toRemove ){
			_oc.removeChange(scc);
		}
		for ( SourceCodeChange scc : finalSet ){
			_oc.addChange(scc);
		}
		log.info("New Overall change ( size: " + _oc.getModifications().size()+" ) after removals: ");
		logEditScriptToConsole(ASTSignatureChange.toSourceCodeChanges(_oc.getModifications()));
	}

	/* return the difference between the versions precedent.after and following.before for 2 construct changes (commits)*/
	private SignatureChange getDiffChange(ConstructChange _prec, ConstructChange _follow) {
		SignatureChange chg = null;
		if(this.getOverallChangeType().equals(ConstructChangeType.MOD)) {
			if(_follow.getBuggyBody()!=null && _prec.getFixedBody()!=null) {
				Signature sp = this.gson.fromJson(_prec.getFixedBody(), ASTConstructBodySignature.class);
				Signature sf = this.gson.fromJson(_follow.getBuggyBody(), ASTConstructBodySignature.class);
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = comparator.computeChange(sp, sf);
			}
		}
		return chg;		
	}

	private void logEditScriptToConsole(Set<SourceCodeChange> _sscc){
		StringBuilder sb = new StringBuilder();
		for(SourceCodeChange scc : _sscc){
			sb.append(scc.toString()+"\n");
		}
		log.info(sb.toString());
	}

	private String getFileNameFromPath(String _path){
		String fname = _path.subSequence(path.lastIndexOf("/")+1, path.indexOf(".java")).toString();
		return fname;
	}

	private String getFullPathName(String _path){
		String pname = _path.replace('/', '_');
		return pname;
	}

	private boolean isSaveEditScriptIntersection(){
		return VulasConfiguration.getGlobal().getConfiguration().getBoolean(SignatureConfiguration.SAVE_EDIT_SCRIPT_INTERSECTION);
	}

	public SortedSet<ConstructChange> getChanges() {
		return changes;
	}

}
