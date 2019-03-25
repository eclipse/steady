/*
 * To chane this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.psr.vulas.java.sign;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.sap.psr.vulas.java.sign.gson.GsonHelper;
import com.sap.psr.vulas.shared.enums.ConstructChangeType;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import com.sap.psr.vulas.shared.json.model.ConstructChange;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;
import com.sap.psr.vulas.sign.SignatureComparator;

/**
 * Helper class for computing the overall change given a SortedSet of ConstructChange
 */
public class OverallChangeComputer {

	final Gson gson = GsonHelper.getCustomGsonBuilder().create(); 
	private Log log = LogFactory.getLog(OverallChangeComputer.class);

	/**
	 * Method wrapping the all the logic for the class. 
	 * The only exposed API for this class.
	 * @param changes
	 * @return SignatureChange
	 */
	public SignatureChange execute(SortedSet<ConstructChange> changes){
		final ASTSignatureChange overall_change = getOverallChange(changes);
		if(changes.first().getFixedBody()!=null && changes.last().getBuggyBody()!=null) {
			if(!isFollowingCommit(changes.first(), changes.last())) {
				log.info("Not subsequent commits, there is at least one unknown commit in between");
				removeIntermediateCommits(overall_change, changes);
			}
		}
		return overall_change;
	}

	/**
	 * If the overall change type is MOD(ified), the method returns the signature change considering
	 * all commits. In the other cases, it returns null.
	 * @return
	 */
	private ASTSignatureChange getOverallChange(SortedSet<ConstructChange> changes) {
		ASTSignatureChange chg = null;
		if(getOverallChangeType(changes).equals(ConstructChangeType.MOD)) {
			if(changes.first().getBuggyBody()!=null && changes.last().getFixedBody()!=null) {
				final Signature before = getBeforeFirstCommit(changes);
				final Signature after  = getAfterLatestCommit(changes);
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = (ASTSignatureChange)comparator.computeChange(before, after);
			}
		}
		return chg;        		
	}

	private Signature getBeforeFirstCommit(SortedSet<ConstructChange> changes) {
		Signature s = null;
		if(changes.first().getBuggyBody()!=null){
			s = gson.fromJson(changes.first().getBuggyBody(), Signature.class);
		}
		return s;
	}

	private Signature getAfterLatestCommit(SortedSet<ConstructChange> changes) {
		Signature s = null;
		if(changes.last().getFixedBody()!=null){
			s = gson.fromJson(changes.last().getFixedBody(), Signature.class);
		}
		return s;
	}

	/**
	 * Modifies the overall change passed as first parameter.
	 * Given a series of commits for a method, aims at finding only the changes necessary for the patch ( and so remove 
	 * the ones not interested by the patch ).
	 * @param _oc 
	 * @param changes
	 */
	private void removeIntermediateCommits(ASTSignatureChange _oc, SortedSet<ConstructChange> changes ){
		Set<SourceCodeChange> finalSet = new HashSet<SourceCodeChange>();
		Set<SourceCodeChange> toRemove = ASTSignatureChange.toSourceCodeChanges(_oc.getModifications());
		// important to ingore source code range!!
		SourceCodeEntity.setIgnoreSourceRange(true);
		finalSet.addAll(toRemove);
		for( int i=1; i<changes.size(); i++){
			ConstructChange precedent = Iterables.get(changes, i-1);
			ConstructChange following = Iterables.get(changes, i);
			if ( ! isFollowingCommit(precedent, following) ){ 
				// !isFollowing means that intersection precedent.after and following.before is empty
				// compute difference between precedent.after and following.before
				SignatureChange chg = this.getDiffChange(changes, precedent, following);
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

	/**
	 * Returns true if 2 commits have no intermediate commit between them, false otherwise 
	 * @param _ccBefore
	 * @param _ccAfter
	 */
	private boolean isFollowingCommit(ConstructChange _ccBefore, ConstructChange _ccAfter) {
		if(_ccBefore.equals(_ccAfter))
			throw new IllegalArgumentException("Provided construct changes are equal");

		if(_ccBefore.getFixedBody()==null && _ccAfter.getBuggyBody()==null)
			throw new IllegalArgumentException("First change has no fixed body and/or later change has no buggy body");

		final Signature s1 = gson.fromJson(_ccBefore.getFixedBody(), Signature.class);
		final Signature s2 = gson.fromJson(_ccAfter.getBuggyBody(), Signature.class);
		final SignatureComparator comparator = new ASTSignatureComparator();
		final SignatureChange chg = comparator.computeChange(s1, s2);

		// Return whether the edit script is empty
		return chg.getModifications().isEmpty();
	}

	/**
	 * Returns the difference between the versions precedent.after and following.before for 2 construct changes (commits)
	 * @param changes
	 * @param _prec
	 * @param _follow
	 */
	private SignatureChange getDiffChange(SortedSet<ConstructChange> changes, ConstructChange _prec, ConstructChange _follow) {
		SignatureChange chg = null;
		if(this.getOverallChangeType(changes).equals(ConstructChangeType.MOD)) {
			if(_follow.getBuggyBody()!=null && _prec.getFixedBody()!=null) {
				Signature sp = gson.fromJson(_prec.getFixedBody(), Signature.class);
				Signature sf = gson.fromJson(_follow.getBuggyBody(), Signature.class);
				final SignatureComparator comparator = new ASTSignatureComparator();
				chg = comparator.computeChange(sp, sf);
			}
		}
		return chg;		
	}

	/**
	 * Logs to console the set of construct changes
	 * @param _sscc 
	 */
	private void logEditScriptToConsole(Set<SourceCodeChange> _sscc){
		StringBuilder sb = new StringBuilder();
		for(SourceCodeChange scc : _sscc){
			sb.append(scc.toString()+"\n");
		}
		log.info(sb.toString());
	}

	/**
	 * Given a list of changes, returns true if the the construct existed before the first commit
	 * @param changes
	 * @return 
	 */
	private boolean isConstructExistedBeforeFirstCommit(SortedSet<ConstructChange> changes) {
		if(changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.first().getConstructChangeType().toString().equals("ADD");
	}

	/**
	 * Given a list of changes, returns true if the the construct existed after the last commit
	 * @param changes
	 * @return 
	 */
	private boolean isConstructExistsAfterLastCommit(SortedSet<ConstructChange> changes) {
		if(changes.size()==0) throw new IllegalStateException("No commits exist");
		return !changes.last().getConstructChangeType().toString().equals("DEL");
	}

	/**
	 * Returns the type of change considering all commits.
	 * @param changes
	 * @return
	 */
	private ConstructChangeType getOverallChangeType(SortedSet<ConstructChange> changes) {
		if(this.isConstructExistedBeforeFirstCommit(changes) && this.isConstructExistsAfterLastCommit(changes)) {
			return ConstructChangeType.MOD;
		}
		else if(this.isConstructExistedBeforeFirstCommit(changes) && !this.isConstructExistsAfterLastCommit(changes)) {
			return ConstructChangeType.DEL;
		}
		else if(!this.isConstructExistedBeforeFirstCommit(changes) && this.isConstructExistsAfterLastCommit(changes)) {
			return ConstructChangeType.ADD;
		}
		else {
			return ConstructChangeType.NUL;
		}
	}
}
