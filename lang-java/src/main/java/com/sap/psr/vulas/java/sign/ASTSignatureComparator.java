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
/**
 *
 */
package com.sap.psr.vulas.java.sign;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;
import com.sap.psr.vulas.sign.SignatureComparator;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.NodePair;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.TreeMatcher;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.MatchingFactory;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.LevenshteinSimilarityCalculator;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.NGramsCalculator;

/**
 * <p>ASTSignatureComparator class.</p>
 *
 */
public class ASTSignatureComparator implements SignatureComparator {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  // If a construct under test contains 50% of the fixes, it is said to contain the Security Fixes
  // TODO : (A more robust scheme than a simple percentage might be better)
  private static final double NUM_OF_FIXES_THRESHOLD = 100.0;

  private int totalNumFixes;
  private int matchedNumFixes;

  // String Similarity Schemes for comparing Node Values
  private LevenshteinSimilarityCalculator fLevenshtein = new LevenshteinSimilarityCalculator();
  // private TokenBasedCalculator fTokenBased = new TokenBasedCalculator();
  private NGramsCalculator fNgrams = new NGramsCalculator(2); // Using bi-grams, n = 2

  // Different (Dynamic) string similarity threshold for the different size of SourceCode Changes
  private static final double STRING_SIMILARITY_THRESHOLD_LESS_THAN_TWO_CHANGES = 0.7;
  private static final double STRING_SIMILARITY_THRESHOLD_BETWEEN_TWO_AND_FIVE_CHANGES = 0.6;
  private static final double STRING_SIMILARITY_THRESHOLD_MORE_THAN__FIVE_CHANGES = 0.5;

  // private Map<Node, Double> matchingNodes = new HashMap<Node,Double>();
  // private Node bestMatchNode = null;
  // private Signature mSignVuln = null;
  // private Signature mSignFixed = null;
  // private SignatureChange mSignChange = null;
  private double mStringSimilarity;

  /**
   * <p>Constructor for ASTSignatureComparator.</p>
   */
  public ASTSignatureComparator() {}

  /**
   * <p>Constructor for ASTSignatureComparator.</p>
   *
   * @param stringSimilarity a double.
   */
  public ASTSignatureComparator(double stringSimilarity) {
    this.mStringSimilarity = stringSimilarity;
  }

  /**
   * <p>Constructor for ASTSignatureComparator.</p>
   *
   * @param _sign a {@link com.sap.psr.vulas.sign.Signature} object.
   * @param _signChg a {@link com.sap.psr.vulas.sign.SignatureChange} object.
   */
  /*public ASTSignatureComparator(Signature _sign, SignatureChange _signChg){
  	this.mSignFixed = _sign;
  	this.mSignChange = _signChg;
  }*/

  /**
   * <p>Constructor for ASTSignatureComparator.</p>
   *
   * @param _vuln a {@link com.sap.psr.vulas.sign.Signature} object.
   * @param _fixed a {@link com.sap.psr.vulas.sign.Signature} object.
   * @param _signChg a {@link com.sap.psr.vulas.sign.SignatureChange} object.
   */
  /*public ASTSignatureComparator(Signature _vuln, Signature _fixed, SignatureChange _signChg){
  	this.mSignVuln = _vuln;
  	this.mSignFixed = _fixed;
  	this.mSignChange = _signChg;
  }*/

  /*
   * @see com.sap.psr.vulas.sign.SignatureComparator#computeSimilarity(com.sap.psr.vulas.sign.Signature, com.sap.psr.vulas.sign.Signature)
   */
  /** {@inheritDoc} */
  @Override
  public float computeSimilarity(Signature _a, Signature _b) {
    Node _vulnerableConstruct = ((ASTSignature) _a).getRoot();
    Node _fixedConstruct = ((ASTSignature) _b).getRoot();
    float similarity = computeSimilarity(_vulnerableConstruct, _fixedConstruct);
    return similarity;
  }

  /**
   *  Compute the similarity of two ASTs
   * @param _vulnerable
   * @param _fixed
   * @return similarity value
   */
  private float computeSimilarity(Node _vulnerable, Node _fixed) {
    float similarity = 0;
    // Baxter Scheme of AST similarity
    return similarity;
  }

  /*
   * @see com.sap.psr.vulas.sign.SignatureComparator#computeChange(com.sap.psr.vulas.sign.Signature, com.sap.psr.vulas.sign.Signature)
   *  _a is the defective construct ( this is used for the patch analyzer)
   *  _b is the fixed construct
   */
  /** {@inheritDoc} */
  @Override
  public SignatureChange computeChange(Signature _a, Signature _b) {
    SignatureChange astChange = new ASTSignatureChange(_a, _b);
    astChange.getModifications();
    return astChange;
  }

  /**
   * <p>getTotalNumChanges.</p>
   *
   * @return a int.
   */
  public int getTotalNumChanges() {
    return this.totalNumFixes;
  }

  /**
   * <p>getMatchedNumChanges.</p>
   *
   * @return a int.
   */
  public int getMatchedNumChanges() {
    return this.matchedNumFixes;
  }

  /**
   * {@inheritDoc}
   *
   * Returns true of the given signature contains the given change, false otherwise.
   */
  @Override
  public boolean containsChange(Signature _s, SignatureChange _change) {
    // Do a couple of casts before calling the worker method
    final Node root_of_signature_under_test = ((ASTSignature) _s).getRoot();
    final ASTSignatureChange sign_change = (ASTSignatureChange) _change;
    final Set<SourceCodeChange> list_of_modifications = sign_change.getListOfChanges();

    // Return the result of the worker method
    return containsChange(root_of_signature_under_test, list_of_modifications);
  }

  /**
   * <p>getStringSimilarityThreshold.</p>
   *
   * @return a double.
   */
  public double getStringSimilarityThreshold() {
    return this.mStringSimilarity;
  }

  /**
   * <p>setStringSimilarityThreshold.</p>
   *
   * @param _threshold a double.
   */
  public void setStringSimilarityThreshold(double _threshold) {
    this.mStringSimilarity = _threshold;
  }

  /**
   * Depending on the number of source code changes, the method sets a suitable threshold for string similarity.
   * @param sizeOfSourceCodeChange
   * @return Similarity Threshold
   *
   */
  private void assignSimlarityScheme(int _change_count) {
    if (_change_count <= 2)
      this.setStringSimilarityThreshold(STRING_SIMILARITY_THRESHOLD_LESS_THAN_TWO_CHANGES);
    else if (_change_count <= 5)
      this.setStringSimilarityThreshold(STRING_SIMILARITY_THRESHOLD_BETWEEN_TWO_AND_FIVE_CHANGES);
    else this.setStringSimilarityThreshold(STRING_SIMILARITY_THRESHOLD_MORE_THAN__FIVE_CHANGES);
  }

  /**
   * Returns the class name of the given SourceCodeChange (Delete, Insert, Move or Update).
   * @param _change
   * @return
   */
  private String getSimpleChangeName(SourceCodeChange _change) {
    return _change.getClass().getSimpleName();
  }

  /**
   * Returns true if the tree given by _root contains the entity changed by _change.
   * @param _root
   * @param _change
   * @return
   */
  private Node getBestMatch(Node _root, SourceCodeChange _change) {
    Node best_match = null;

    final Set<NodePair> fMatch = new HashSet<NodePair>();
    final TreeMatcher dnm = MatchingFactory.getMatcher(fMatch);

    final EntityType change_type = _change.getChangedEntity().getType();
    final String change_value = _change.getChangedEntity().getUniqueName();
    final Node changed_node = new Node(change_type, change_value);

    dnm.match(_root, changed_node);

    // ASTSignatureComparator.log.info("");
    for (NodePair pair : fMatch) {
      if (pair.getRight().equals(changed_node)) {
        best_match = pair.getLeft();
      }
    }

    return best_match;
  }

  /**
   *
   * @param _root_node
   * @param _changes
   * @return
   */
  private boolean containsChange(Node _root_node, Set<SourceCodeChange> _changes) {
    this.totalNumFixes = _changes.size();
    this.matchedNumFixes = 0;
    this.assignSimlarityScheme(this.totalNumFixes);

    boolean contains_change = false;

    // Maintains the containment status per change
    final Map<SourceCodeChange, Boolean> change_containment =
        new HashMap<SourceCodeChange, Boolean>();
    int i = 1;

    // Loop over all changes and check each of them
    if (totalNumFixes > 0) {

      EntityType change_type = null;
      String change_value = null;

      Node changed_node = null; // The node to search for
      Node matched_node = null; // The node matching best (if any)

      for (SourceCodeChange change : _changes) {

        // Build the node that results from the change (same type and value)
        change_type = change.getChangedEntity().getType();
        change_value = change.getChangedEntity().getUniqueName();
        changed_node = new Node(change_type, change_value);

        // Case 1: INSERT
        // - The New Inserted Entity : change.getChangedEntity()
        // - Parent Entity : change.getParentEntity()
        if (change instanceof Insert) {

          // Set the parent of the changed node
          EntityType changedParentEntityType = ((Insert) change).getParentEntity().getType();
          String changedParentValue = ((Insert) change).getParentEntity().getUniqueName();
          Node parentNode = new Node(changedParentEntityType, changedParentValue);
          changed_node.setParent(parentNode);

          // Look if the changed entity if found in the AST under test
          // matched_node = searchForBestMatchingNode(changed_node, _root_node);
          matched_node = getBestMatch(_root_node, change);
          contains_change = matched_node != null;

        }
        // Case 2: DELETE
        // 1. The Deleted Entity : change.getChangedEntity()
        // 2. Parent Entity : change.getParentEntity()
        else if (change instanceof Delete) {

          // Set the parent of the changed node
          EntityType changedParentEntityType = ((Delete) change).getParentEntity().getType();
          String changedParentValue = ((Delete) change).getParentEntity().getUniqueName();
          Node parentNode = new Node(changedParentEntityType, changedParentValue);
          changed_node.setParent(parentNode);

          // Look if the changed entity if found in the AST under test
          // matched_node = searchForBestMatchingNode(changed_node, _root_node);
          matched_node = getBestMatch(_root_node, change);
          contains_change = matched_node == null;
        }
        /**
         *  A Update Change Type has
         *  1. The New Entity : ((Update) change).getNewEntity()
         *  2. The (old) Updated Entity : ((Update) change).getChangedEntity()
         *  3. Parent Entity : change.getParentEntity()
         */
        else if (change.getClass().getSimpleName().equals("Update")) {

          SourceCodeEntity srcCodeEntity = ((Update) change).getNewEntity();
          Node updateChangedNode = new Node(srcCodeEntity.getType(), srcCodeEntity.getUniqueName());

          EntityType changedParentEntityType = ((Update) change).getParentEntity().getType();
          String changedParentValue = ((Update) change).getParentEntity().getUniqueName();
          Node parentNode = new Node(changedParentEntityType, changedParentValue);
          updateChangedNode.setParent(parentNode);

          // Look if the changed entity if found in the AST under test
          matched_node = searchForBestMatchingNode(updateChangedNode, _root_node);

          contains_change = matched_node != null;
        }
        /**
         * A Move change type has
         *  1. The Old Parent Entity : change.getParentEntity()
         *  2. The Moved Entity Entity : change.getChangedEntity()
         *  3. New Parent Entity : ((Move) change).getNewParentEntity()
         *  4. New Entity : ((Move) change).getNewEntity()
         */
        // Check first if the fix is present, then look for the appropriate place
        else if (change.getClass().getSimpleName().equals("Move")) {

          SourceCodeEntity srcCodeEntity = ((Move) change).getNewEntity();
          Node moveChangedNode = new Node(srcCodeEntity.getType(), srcCodeEntity.getUniqueName());

          EntityType changedNewParentEntityType = ((Move) change).getNewParentEntity().getType();
          String changedNewParentValue = ((Move) change).getNewParentEntity().getUniqueName();
          Node parentNode = new Node(changedNewParentEntityType, changedNewParentValue);
          moveChangedNode.setParent(parentNode);

          // Look if the changed entity if found in the AST under test
          matched_node = searchForBestMatchingNode(moveChangedNode, _root_node);

          contains_change = matched_node != null;
        }

        // Maintain the status for the current change
        ASTSignatureComparator.log.info(
            "             "
                + i++
                + " Change type ["
                + change.getClass().getSimpleName()
                + "], changed node ["
                + change_type
                + ", \""
                + change_value
                + "\"], change contained: ["
                + contains_change
                + "]");
        if (matched_node != null)
          ASTSignatureComparator.log.info(
              "               Matching node: ["
                  + matched_node.getEntity().getType()
                  + ", \""
                  + matched_node.getValue()
                  + "\"]");
        change_containment.put(change, contains_change);
      }
    }

    // Make the Decision at the end reading through all the content of the map
    boolean unMatchedFlag = false;
    for (Boolean status : change_containment.values()) {
      if (!status) {
        unMatchedFlag = true;
      } else {
        matchedNumFixes++;
      }
    }

    // If there is one security-fix change which is not contained, the "unMatchedFlag" will be set
    if (unMatchedFlag) return false;
    else return true;
  }

  private boolean hasSameParent(Node _n1, Node _n2) {
    boolean same_parent = false;

    Node p1 = (Node) _n1.getParent();
    String p1_type = p1.getLabel().name();
    String p1_value = p1.getValue();

    Node p2 = (Node) _n2.getParent();
    String p2_type = p2.getLabel().name();
    String p2_value = p2.getValue();

    if (p1_type.equals(p2_type)) {
      double similarityScheme = fLevenshtein.calculateSimilarity(p1_value, p2_value);
      same_parent = (similarityScheme > getStringSimilarityThreshold());
    }
    return same_parent;
  }

  /**
   * Search for the best matching node; having the same label and with a similarity measure of the changedNode's string value above the given THRESHOLD
   *
   * @param changedNode - The changed node to be searched for
   * @param astRoot - The signature of the construct under analysis
   * @return The best matched node/ The first matched node
   */
  private Node searchForBestMatchingNode(Node changedNode, Node astRoot) {

    // Nodes in the AST under test
    EntityType astRootNodeLabel = astRoot.getLabel();
    String astRootNodeValue = astRoot.getValue();

    // Both Nodes have the same "Label", for instance both are IF_STATEMENTs
    if (astRootNodeLabel.equals(changedNode.getLabel())) {

      // Both have an empty Value, for instance ==== > return; (Label : RETURN_STATEMENT, Value :
      // "")
      if (astRootNodeValue.isEmpty() && changedNode.getValue().isEmpty()) {

        // Check if they have the same parent, otherwise resume searching
        if (this.hasSameParent(changedNode, astRoot)) {
          return astRoot;
        }
      } else {
        // Check the similarity of the string values
        // double similarityScheme = fLevenshtein.calculateSimilarity(astRootNodeValue,
        // changedNode.getValue());
        // double similarityScheme = fTokenBased.calculateSimilarity(astRootNodeValue,
        // changedNode.getValue());

        // N-Grams :
        // STRING_1 :  (value: String tok = st.nextToken().trim();)(label:
        // VARIABLE_DECLARATION_STATEMENT),
        // STRING_2 :  (value: final String tok = st.nextToken();)(label:
        // VARIABLE_DECLARATION_STATEMENT),
        // N-Grams Similarity Value : 0.6285714285714286
        double similarityScheme =
            fNgrams.calculateSimilarity(astRootNodeValue, changedNode.getValue());

        // if(similarityScheme > STRING_SIMILARITY_THRESHOLD)
        if (similarityScheme > this.getStringSimilarityThreshold()) {
          /*matchingNodes.put(astRoot, similarityScheme);   //or we use the "Best Match" Algorithm
          	Node currentBestMatchedNode = astRoot;
          	Node previousBestMatchedNode = astRoot;

          	if (matchingNodes.get(currentBestMatchedNode) >  matchingNodes.get(previousBestMatchedNode))
          	{
          		bestMatchNode = currentBestMatchedNode;   //Found a New best Match
          	}
          */

          // Check if they have the same parent, otherwise resume searching
          if (this.hasSameParent(changedNode, astRoot)) {
            return astRoot;
          }
        }
      }
    }

    Node[] children = new Node[astRoot.getChildCount()];
    for (int i = 0; i < astRoot.getChildCount(); i++) {
      children[i] = (Node) astRoot.getChildAt(i);
    }

    Node firstMatchedNode = null;
    // Breadth First Search
    for (int i = 0; (firstMatchedNode == null) && (i < children.length); i++) {
      // Node node = (Node)astRoot.getChildAt(i);
      firstMatchedNode = searchForBestMatchingNode(changedNode, children[i]);
    }

    // return tmpNode;
    // return bestMatchNode;
    return firstMatchedNode; // Returning the first Match that we have found, might be a good
    // approach
  }

  /**
   * Search the SourceCode Entity Using it name
   * @param _uniqueName
   * @param n
   * @return
   */
  private SourceCodeEntity searchSourceCodeEntityByUniqueName(
      String _entityUniqueName, Node astRoot) {

    // The SourceCodeEntity to be returned
    SourceCodeEntity srcCodeEntity = null;

    String nodeValue = astRoot.getValue().toString();
    if (nodeValue.equals(_entityUniqueName)) {
      // Return SourceCodeEntity
      return astRoot.getEntity();
    }

    // Breadth First Search
    for (int i = 0; (srcCodeEntity == null) && (i < astRoot.getChildCount()); i++) {
      Node node = (Node) astRoot.getChildAt(i);
      srcCodeEntity = searchSourceCodeEntityByUniqueName(_entityUniqueName, node);
      if (srcCodeEntity != null) break;
    }

    return srcCodeEntity;
  }

  /**
   *
   * @param _entityUniqueName ; UniqueName of 'Node' to be searched for
   * @param _s The AST to be searched
   * @return A Node in _s  which has the same name as the parameter, UniqueName , return null is NOT found
   */
  private Node search(String _entityUniqueName, Node _s) {

    if (_s.getValue().toString().equals(_entityUniqueName)) return _s;
    Node resultNode = null;
    for (int i = 0; resultNode == null && i < _s.getChildCount(); i++) {
      resultNode = search(_entityUniqueName, (Node) _s.getChildAt(i));
    }
    return resultNode;
  }

  /**
   * TODO : Enclose this functionality in a generic class, "SignatureSimilarity"
   * ALSO, this simple percentage would fail if we have a small number of changes in the change list
   * (Say if we have only one FIX in the change list , this could be problematic), hence a need for a more robust "Closeness" scheme
   *
   * CVE-2012-2098
   * Case in Point :
   * 1. org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream(OutputStream,int)
   * 2. org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream.init() , Revision : 1332552
   *
   * @param numFoundFixes - Number of found fixes found in
   * @param numTotalFixes -
   * @return
   */
  private boolean simplePercentageClosenessScheme(double numFoundFixes, double numTotalFixes) {
    boolean flag = false;
    double percentage = 0.0;
    try {
      percentage = (numFoundFixes / numTotalFixes) * 100;
    } catch (Exception e) {
      log.info(e.getMessage());
    }

    log.info("Percentage of Fixes : " + percentage + "%");
    if (percentage >= NUM_OF_FIXES_THRESHOLD) flag = true;

    return flag;
  }

  /**
   *  Search for the specified change element inside Signature (Search using the Node Label)
   *
   * @param astRoot The AST to be searched
   * @return the Node matching the change elements EntityType and Value
   *
   * public : for testing purpose, switch to private once done testing
   * @param change a {@link ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange} object.
   */
  public Node searchForNode(SourceCodeChange change, Node astRoot) {

    // This is what we are trying to match
    String changeValue = change.getChangedEntity().getUniqueName();
    EntityType changeLabel = change.getChangedEntity().getType();

    // The Node we are looking for, with EntityType and Value
    Node nodeRoot = new Node(changeLabel, changeValue);

    String nodeValue = astRoot.getValue().toString();
    EntityType nodeLabel = astRoot.getLabel();

    if (nodeValue.equals(changeValue) && nodeLabel.equals(changeLabel)) {
      return astRoot;
    }

    Node tmpNode = null;

    // Breadth First Search
    for (int i = 0; (tmpNode == null) && (i < astRoot.getChildCount()); i++) {
      Node node = (Node) astRoot.getChildAt(i);

      // Recursive call
      tmpNode = searchForNode(change, node);
      if (tmpNode != null) break;
    }

    return tmpNode;
  }

  /**
   *  Search for the _entityUniqueName inside Signature (Search using the Node Label)
   *
   * @param astRoot The AST to be searched
   * @return true if entity is within the AST
   *
   * public : for testing purpose, switch to private once done testing
   * @param change a {@link ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange} object.
   */
  public boolean searchForEntity(SourceCodeChange change, Node astRoot) {

    SourceCodeEntity parentEntity = change.getParentEntity();

    String _entityUniqueName = change.getChangedEntity().getUniqueName();
    String nodeValue = astRoot.getValue().toString();
    if (nodeValue.equals(_entityUniqueName)) {
      return true;
    }

    boolean found = false;
    // Breadth First Search
    for (int i = 0; (found == false) && (i < astRoot.getChildCount()); i++) {
      Node node = (Node) astRoot.getChildAt(i);
      /*if(_entityUniqueName.equals(node.getValue().toString()))
      return true;*/
      // else
      String nodeLbl = node.getValue().toString();
      found = searchForEntity(_entityUniqueName, node);
      if (found) break;
    }

    return found;
  }

  /**
   *  Search for the _entityUniqueName inside Signature (Search using the Node Label)
   *
   * @param _entityUniqueName a {@link java.lang.String} object.
   * @param astRoot The AST to be searched
   * @return true if entity is within the AST
   *
   * public : for testing purpose, switch to private once done testing
   */
  public boolean searchForEntity(String _entityUniqueName, Node astRoot) {

    String nodeValue = astRoot.getValue().toString();
    if (nodeValue.equals(_entityUniqueName)) {
      return true;
    }

    boolean found = false;
    // Breadth First Search
    for (int i = 0; (found == false) && (i < astRoot.getChildCount()); i++) {
      Node node = (Node) astRoot.getChildAt(i);
      /*if(_entityUniqueName.equals(node.getValue().toString()))
      return true;*/
      // else
      String nodeLbl = node.getValue().toString();
      found = searchForEntity(_entityUniqueName, node);
      if (found) break;
    }

    return found;
  }
}
