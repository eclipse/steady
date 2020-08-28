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
package org.eclipse.steady.java.sign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.Construct;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.FileAnalyzerFactory;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.JavaConstructorId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.sign.gson.ASTConstructBodySignatureDeserializer;
import org.eclipse.steady.java.sign.gson.ASTSignatureChangeDeserializer;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.sign.Signature;
import org.eclipse.steady.sign.SignatureChange;
import org.eclipse.steady.sign.SignatureComparator;
import org.eclipse.steady.sign.SignatureFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 *  UPDATE THE CONFIGUARATION PARAMETERS BEFORE TESTING
 */
public class ASTSignatureComparatorTest {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  Map<Class<?>, StdDeserializer<?>> custom_deserializers =
      new HashMap<Class<?>, StdDeserializer<?>>();

  private static final String TEST_DATA = "./src/test/resources/methodBody/";

  private String _constructQName =
      "org.apache.commons.fileupload.MultipartStream(InputStream,byte[],int,ProgressNotifier)";
  private String _fixedJavaFile = "MultipartStreamFix.java";
  private String _defectiveJavaFile = "MultipartStreamDef.java";
  private String _underTestJavaFile = "MultipartStream121.java";

  // Def and Fix Signatures
  private Signature signatureFix = null;
  private Signature signatureDef = null;
  private Signature signatureUnderTest = null;

  // "Diff" patch - Signature Change
  SignatureChange astChange = null;

  private void setupDefectiveConstruct() throws FileAnalysisException {

    FileAnalyzer jfa2 =
        FileAnalyzerFactory.buildFileAnalyzer(new File(TEST_DATA + _defectiveJavaFile));
    // Create a construct for testing and check that it exists in the set
    JavaConstructorId cDefid = JavaId.parseConstructorQName(_constructQName);
    assertEquals(jfa2.containsConstruct(cDefid), true);

    final Construct _cDef = jfa2.getConstruct(cDefid);
    // log.info("DEF CONSTRUCT CONTENT\n"+_cDef.getContent());
    SignatureFactory signFactory =
        CoreConfiguration.getSignatureFactory(ConstructId.toSharedType(cDefid));
    signatureDef = signFactory.createSignature(_cDef);
    log.info("AST of FIXED construct: [" + signatureDef.toJson() + "]");
  }

  private void setupFixedConstruct() throws FileAnalysisException {
    FileAnalyzer jfa2 = FileAnalyzerFactory.buildFileAnalyzer(new File(TEST_DATA + _fixedJavaFile));

    // Create a construct for testing and check that it exists in the set
    JavaConstructorId cFixid = JavaId.parseConstructorQName(_constructQName);
    assertEquals(jfa2.containsConstruct(cFixid), true);

    final Construct _cFix = jfa2.getConstruct(cFixid);
    SignatureFactory signFactory =
        CoreConfiguration.getSignatureFactory(ConstructId.toSharedType(cFixid));
    signatureFix = signFactory.createSignature(_cFix);
    log.info("AST of DEFECTIVE construct: [" + signatureFix.toJson() + "]");
  }

  private void setupConstructUnderTest() throws FileAnalysisException {

    FileAnalyzer jfa2 =
        FileAnalyzerFactory.buildFileAnalyzer(new File(TEST_DATA + _underTestJavaFile));

    // Create a construct for testing and check that it exists in the set
    JavaConstructorId cFixid = JavaId.parseConstructorQName(_constructQName);
    assertEquals(jfa2.containsConstruct(cFixid), true);

    final Construct _cFix = jfa2.getConstruct(cFixid);
    SignatureFactory signFactory =
        CoreConfiguration.getSignatureFactory(ConstructId.toSharedType(cFixid));
    signatureUnderTest = signFactory.createSignature(_cFix);
    log.info("AST of TESTED construct: [" + signatureUnderTest.toJson() + "]");
  }

  @Before
  public void setup() throws FileAnalysisException {
    this.setupFixedConstruct();
    this.setupDefectiveConstruct();
    this.setupConstructUnderTest();
    custom_deserializers.put(ASTSignatureChange.class, new ASTSignatureChangeDeserializer());
    custom_deserializers.put(
        ASTConstructBodySignature.class, new ASTConstructBodySignatureDeserializer());
  }

  @Test
  public void testChangeComparison() throws FileAnalysisException, IOException {

    // Read previous change from disk and check equality
    final String sig_chg_json =
        FileUtil.readFile(
            Paths.get("./src/test/resources/methodBody/deserialize/signatureChange.json"));
    final SignatureChange ddf =
        (ASTSignatureChange)
            JacksonUtil.asObject(sig_chg_json, custom_deserializers, ASTSignatureChange.class);

    SignatureComparator signComparator = new ASTSignatureComparator();
    SignatureChange ddt = signComparator.computeChange(signatureDef, signatureUnderTest);

    this.setupDefectiveConstruct();
    this.setupFixedConstruct();
    this.setupConstructUnderTest();
    SignatureChange dtf = signComparator.computeChange(signatureUnderTest, signatureFix);

    final Set<Object> i_dt =
        ASTUtil.intersectSourceCodeChanges(ddt.getModifications(), ddf.getModifications(), false);
    final Set<Object> i_tf =
        ASTUtil.intersectSourceCodeChanges(dtf.getModifications(), ddf.getModifications(), false);

    this.setupConstructUnderTest();
  }

  @Test
  @Ignore
  public void testSignComparatorContainsChange2() throws FileAnalysisException {

    SignatureComparator signComparator = new ASTSignatureComparator();
    SignatureChange astSignChange = signComparator.computeChange(signatureDef, signatureFix);

    /*//Fixed Version Must have the SourceCode change Elements
    log.info("Signature Under Test : Known to be a  Fixed Version \n");
    assertTrue(signComparator.containsChange(signatureFix, astSignChange));
    log.info("YES, Signature contains Fix " + "\n" );

    */
    /**
     *  LAZY SOLUTION : The call in class ASTSignatureChange :getModifications(){ mDistiller.extractClassifiedSourceCodeChanges(defSignatureNode, fixSignatureNode);}
     *  Changes the defective version into the fixed one, (No pass by Reference), Until I find a better solution, compute Signature of the defective construct again
     */
    /*
    this.setupDefectiveConstruct();

    //Defective Version Must NOT have the SourceCode change Elements
    log.info("Signature Under Test : Known to be a Defective Version \n");
    assertFalse(signComparator.containsChange(signatureDef, astSignChange));
       log.info("NO, Signature Doesn't contain Fix" );
     */
    log.info("DIFF JSON");
    System.out.println(astSignChange.toJSON());
    // System.out.println(astSignChange.toString());

    // Random Signature Under Test (Test should pass if the signature under test is close to the
    // fixed version)
    log.info("Random Signature Under Test \n");
    // assertTrue(signComparator.containsChange(signatureUnderTest, astSignChange));
    assertTrue(signComparator.containsChange(signatureFix, astSignChange));
    log.info("YES, Signature contains Fix " + "\n");
  }

  @Test
  @Ignore
  public void testSignComparatorContainsChange() throws FileAnalysisException {

    // Compute signature change
    final SignatureComparator comparator = new ASTSignatureComparator();
    final SignatureChange change = comparator.computeChange(signatureDef, signatureFix);
    log.info("Signature change: [" + change.toJSON() + "]");

    // Check change containment for FIXED construct: should be TRUE
    assertTrue(comparator.containsChange(signatureFix, change));

    /**
     *  LAZY SOLUTION : The call in class ASTSignatureChange :getModifications(){ mDistiller.extractClassifiedSourceCodeChanges(defSignatureNode, fixSignatureNode);}
     *  Changes the defective version into the fixed one, (No pass by Reference), Until I find a better solution, compute Signature of the defective construct again
     */
    this.setupDefectiveConstruct();

    // Check change containment for VULNERABLE construct: should be FALSE
    assertFalse(comparator.containsChange(signatureDef, change));

    // Random Signature Under Test (Test should pass if the signature under test is close to the
    // fixed version)
    assertFalse(comparator.containsChange(signatureUnderTest, change));
  }

  @Test
  @Ignore
  public void testSearchForEntity() {
    final ASTSignatureComparator signComparator = new ASTSignatureComparator();
    final boolean found =
        signComparator.searchForEntity(
            "this.boundary = new byte[(boundary.length + BOUNDARY_PREFIX.length)];",
            ((ASTSignature) signatureDef).getRoot());
    assertTrue(found);
  }

  private static final String vulSigJson =
      "{\"ast\":[ {\"Value\" : \"MultipartStream\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 19,\"End\" : 861}},\"EntityType\" :"
          + " \"METHOD\",\"C\" : [{\"Value\" : \"this.input = input;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 162,\"End\" :"
          + " 180}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"this.bufSize ="
          + " bufSize;\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
          + " {\"Start\" : 191,\"End\" : 213}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" :"
          + " \"this.buffer = new byte[bufSize];\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 224,\"End\" : 255}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"this.notifier = pNotifier;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 266,\"End\" :"
          + " 291}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"this.boundary = new"
          + " byte[(boundary.length + BOUNDARY_PREFIX.length)];\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 407,\"End\" :"
          + " 473}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"this.boundaryLength ="
          + " (boundary.length + BOUNDARY_PREFIX.length);\",\"SourceCodeEntity\" :{ \"Modifiers\""
          + " : \"0\",\"SourceRange\" : {\"Start\" : 484,\"End\" : 546}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"this.keepRegion ="
          + " this.boundary.length;\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 557,\"End\" : 595}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"System.arraycopy(BOUNDARY_PREFIX, 0, this.boundary,"
          + " 0, BOUNDARY_PREFIX.length);\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 606,\"End\" : 701}},\"EntityType\" :"
          + " \"METHOD_INVOCATION\"},{\"Value\" : \"System.arraycopy(boundary, 0, this.boundary,"
          + " BOUNDARY_PREFIX.length, boundary.length);\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 712,\"End\" : 814}},\"EntityType\" :"
          + " \"METHOD_INVOCATION\"},{\"Value\" : \"head = 0;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 827,\"End\" :"
          + " 835}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"tail ="
          + " 0;\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
          + " 846,\"End\" : 854}},\"EntityType\" : \"ASSIGNMENT\"}]}]}";
  private static final String fixSigJson =
      "{\"ast\":[ {\"Value\" : \"MultipartStream\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 19,\"End\" : 1040}},\"EntityType\" :"
          + " \"METHOD\",\"C\" : [{\"Value\" : \"this.input = input;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 162,\"End\" :"
          + " 180}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"this.bufSize ="
          + " bufSize;\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
          + " {\"Start\" : 191,\"End\" : 213}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" :"
          + " \"this.buffer = new byte[bufSize];\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 224,\"End\" : 255}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"this.notifier = pNotifier;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 266,\"End\" :"
          + " 291}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"this.boundaryLength ="
          + " (boundary.length + BOUNDARY_PREFIX.length);\",\"SourceCodeEntity\" :{ \"Modifiers\""
          + " : \"0\",\"SourceRange\" : {\"Start\" : 407,\"End\" : 469}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"(bufSize < (this.boundaryLength +"
          + " 1))\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
          + " 480,\"End\" : 667}},\"EntityType\" : \"IF_STATEMENT\",\"C\" : [{\"Value\" :"
          + " \"(bufSize < (this.boundaryLength + 1))\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 519,\"End\" : 667}},\"EntityType\" :"
          + " \"THEN_STATEMENT\",\"C\" : [{\"Value\" : \"new IllegalArgumentException(\\\"The"
          + " buffer size specified for the MultipartStream is too"
          + " small\\\");\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
          + " {\"Start\" : 534,\"End\" : 656}},\"EntityType\" :"
          + " \"THROW_STATEMENT\"}]}]},{\"Value\" : \"this.boundary = new"
          + " byte[this.boundaryLength];\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 678,\"End\" : 723}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"this.keepRegion ="
          + " this.boundary.length;\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 734,\"End\" : 772}},\"EntityType\" :"
          + " \"ASSIGNMENT\"},{\"Value\" : \"System.arraycopy(BOUNDARY_PREFIX, 0, this.boundary,"
          + " 0, BOUNDARY_PREFIX.length);\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 785,\"End\" : 880}},\"EntityType\" :"
          + " \"METHOD_INVOCATION\"},{\"Value\" : \"System.arraycopy(boundary, 0, this.boundary,"
          + " BOUNDARY_PREFIX.length, boundary.length);\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
          + " \"0\",\"SourceRange\" : {\"Start\" : 891,\"End\" : 993}},\"EntityType\" :"
          + " \"METHOD_INVOCATION\"},{\"Value\" : \"head = 0;\",\"SourceCodeEntity\" :{"
          + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 1006,\"End\" :"
          + " 1014}},\"EntityType\" : \"ASSIGNMENT\"},{\"Value\" : \"tail ="
          + " 0;\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
          + " 1025,\"End\" : 1033}},\"EntityType\" : \"ASSIGNMENT\"}]}]}";
  private static final String sigChange =
      "{\"StructureEntity\" :{ \"UniqueName\" : \"MultipartStream\",\"EntityType\" :"
          + " \"METHOD\",\"Modifiers\" : \"0\",\"changes\" :[{\"OperationType\" :"
          + " \"Insert\",\"changeType\" : \"STATEMENT_INSERT\",\"InsertedEntity\" :"
          + " {\"UniqueName\" : \"(bufSize < (this.boundaryLength + 1))\",\"EntityType\" :"
          + " \"IF_STATEMENT\",\"Modifiers\" : \"0\",\"SourceCodeRange\" : {\"Start\" :"
          + " \"480\",\"End\" : \"667\"}},\"ParentEntity\" : {\"UniqueName\" :"
          + " \"MultipartStream\",\"EntityType\" : \"METHOD\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"19\",\"End\" :"
          + " \"861\"}}},{\"OperationType\" : \"Move\",\"changeType\" :"
          + " \"STATEMENT_ORDERING_CHANGE\",\"OldParentEntity\" : {\"UniqueName\" :"
          + " \"MultipartStream\",\"EntityType\" : \"METHOD\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"19\",\"End\" : \"861\"}},\"MovedEntity\""
          + " : {\"UniqueName\" : \"this.boundaryLength = (boundary.length +"
          + " BOUNDARY_PREFIX.length);\",\"EntityType\" : \"ASSIGNMENT\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"484\",\"End\" :"
          + " \"546\"}},\"NewParentEntity\" : {\"UniqueName\" : \"MultipartStream\",\"EntityType\""
          + " : \"METHOD\",\"Modifiers\" : \"0\",\"SourceCodeRange\" : {\"Start\" : \"19\",\"End\""
          + " : \"861\"}},\"NewEntity\" : {\"UniqueName\" : \"this.boundaryLength ="
          + " (boundary.length + BOUNDARY_PREFIX.length);\",\"EntityType\" :"
          + " \"ASSIGNMENT\",\"Modifiers\" : \"0\",\"SourceCodeRange\" : {\"Start\" :"
          + " \"407\",\"End\" : \"469\"}}},{\"OperationType\" : \"Update\",\"changeType\" :"
          + " \"STATEMENT_UPDATE\",\"NewEntity\" : {\"UniqueName\" : \"this.boundary = new"
          + " byte[this.boundaryLength];\",\"EntityType\" : \"ASSIGNMENT\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"678\",\"End\" :"
          + " \"723\"}},\"UpdatedEntity\" : {\"UniqueName\" : \"this.boundary = new"
          + " byte[(boundary.length + BOUNDARY_PREFIX.length)];\",\"EntityType\" :"
          + " \"ASSIGNMENT\",\"Modifiers\" : \"0\",\"SourceCodeRange\" : {\"Start\" :"
          + " \"407\",\"End\" : \"473\"}},\"ParentEntity\" : {\"UniqueName\" :"
          + " \"MultipartStream\",\"EntityType\" : \"METHOD\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"19\",\"End\" :"
          + " \"861\"}}},{\"OperationType\" : \"Insert\",\"changeType\" :"
          + " \"STATEMENT_INSERT\",\"InsertedEntity\" : {\"UniqueName\" : \"new"
          + " IllegalArgumentException(\\\"The buffer size specified for the MultipartStream is"
          + " too small\\\");\",\"EntityType\" : \"THROW_STATEMENT\",\"Modifiers\" :"
          + " \"0\",\"SourceCodeRange\" : {\"Start\" : \"534\",\"End\" :"
          + " \"656\"}},\"ParentEntity\" : {\"UniqueName\" : \"(bufSize < (this.boundaryLength +"
          + " 1))\",\"EntityType\" : \"THEN_STATEMENT\",\"Modifiers\" : \"0\",\"SourceCodeRange\""
          + " : {\"Start\" : \"519\",\"End\" : \"667\"}}}]}}";

  /**
   * Deserializes signatures from file and creates a signature change from the those.
   */
  @Test
  public void testCompareFromJson() {
    try {
      // Read JSON
      final String vul_sig_json =
          FileUtil.readFile(
              Paths.get("./src/test/resources/methodBody/deserialize/signatureDef.json"));
      final String fix_sig_json =
          FileUtil.readFile(
              Paths.get("./src/test/resources/methodBody/deserialize/signatureFix.json"));
      assertEquals(vul_sig_json, vulSigJson);
      assertEquals(fix_sig_json, fixSigJson);

      // Deserialize
      final Signature vul_sig =
          (Signature)
              JacksonUtil.asObject(
                  vul_sig_json, custom_deserializers, ASTConstructBodySignature.class);
      final Signature fix_sig =
          (Signature)
              JacksonUtil.asObject(
                  fix_sig_json, custom_deserializers, ASTConstructBodySignature.class);

      // Create signature change
      final SignatureComparator comparator = new ASTSignatureComparator();
      SignatureChange chg = comparator.computeChange(vul_sig, fix_sig);

      // Read previous change from disk and check equality
      final String sig_chg_json =
          FileUtil.readFile(
              Paths.get("./src/test/resources/methodBody/deserialize/signatureChange.json"));
      assertEquals(sig_chg_json, sigChange);
      final SignatureChange sig_chg =
          (ASTSignatureChange)
              JacksonUtil.asObject(sig_chg_json, custom_deserializers, ASTSignatureChange.class);
      assertEquals(chg, sig_chg);

    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
