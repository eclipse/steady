/**
 *
 */
//package ch.uzh.ifi.seal.changedistiller.distilling;
package com.sap.psr.vulas.java.sign.gson;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;
import com.sap.psr.vulas.java.sign.ASTConstructBodySignature;
import com.sap.psr.vulas.java.sign.ASTSignatureChange;
import com.sap.psr.vulas.java.sign.ASTSignatureComparator;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.sign.Signature;
import com.sap.psr.vulas.sign.SignatureChange;

public class ASTDeserializeSignComparatorTest {

	private static final Log log = LogFactory.getLog(ASTDeserializeSignComparatorTest.class);
	
	final Gson gson = GsonHelper.getCustomGsonBuilder().create();

    //JSON objects representing the SignatureChange, Defective Signature, Fixed Signature and Unknown Version Signature
	// Current content of the files have the CVE-2014-0050, MultipartStream  - Constructor
	private static final String SIGNATURE_CHANGE_JSON_OBJECT = "./src/test/resources/methodBody/deserialize/signatureChange.json";
	private final static String SIGNATURE_JSON_OBJECT_DEF=  "./src/test/resources/methodBody/deserialize/signatureDef.json";
	private final static String SIGNATURE_JSON_OBJECT_FIXED =  "./src/test/resources/methodBody/deserialize/signatureFix.json";
	private final static String SIGNATURE_JSON_OBJECT_UNKNOWN =  "./src/test/resources/methodBody/deserialize/signatureUnknown.json";

	//Signature Deserializer
	//private ASTSignatureDeserializer astSignDeserializer = null;

	//SignatureChage Deserializer
	//private ASTSignatureChangeDeserializer astSignChangeDeserializer = null;

	//SignatureComparator for fix containment check
	private ASTSignatureComparator astSignComparator = null;


	//Fixed and Defective JSON Strings
	private String JsonSignDef = null;
	private String JsonSignFix = null;

	//Read from the DB, here hardcoded for testing purposes
	private String JsonSignUnderTest = null;
	private Signature astSignUnderTest = null;    //Random Signature of a construct found in an archive

	private String JsonSignChange = null;
	private SignatureChange astSignChange = null;

	//Know versions of a construct
	private Signature astSignFix = null;     //Fixed version of a construct
	private Signature astSignDef = null;    //Vulnerable version of a construct

	@Before
	public void setup() throws IOException{

		//Create instance of Deserializers and ASTSignatureCompartor
		//astSignDeserializer = new ASTSignatureDeserializer();
		//astSignChangeDeserializer = new ASTSignatureChangeDeserializer();
		astSignComparator = new ASTSignatureComparator();

		//Read in the JSON string into Strings (TODO : Check the validity of json objects)
		JsonSignDef = FileUtil.readFile(SIGNATURE_JSON_OBJECT_DEF);
		JsonSignFix = FileUtil.readFile(SIGNATURE_JSON_OBJECT_FIXED);
		JsonSignChange = FileUtil.readFile(SIGNATURE_CHANGE_JSON_OBJECT);
		JsonSignUnderTest = FileUtil.readFile(SIGNATURE_JSON_OBJECT_UNKNOWN);
	}

	@Test
	@Ignore
	public void testDeserializeAndFixContainmentCheck(){

		//Deserialize the "Signature"s and the set of "SignatureChange"s (JSON - to -Java)
		//astSignUnderTest = astSignDeserializer.fromJson(JsonSignUnderTest);
		
		astSignFix = this.gson.fromJson(JsonSignFix, ASTConstructBodySignature.class);
		astSignDef = this.gson.fromJson(JsonSignDef, ASTConstructBodySignature.class);
		//astSignFix = astSignDeserializer.fromJson(JsonSignFix);
		//astSignDef = astSignDeserializer.fromJson(JsonSignDef);

		astSignChange = this.gson.fromJson(JsonSignChange, ASTSignatureChange.class);
		//astSignChange = astSignChangeDeserializer.fromJson(JsonSignChange);

		//Fixed Version Must have the SourceCode change Elements
		log.info("Signature Under Test : Known to be a  Fixed Version \n");
		boolean status = astSignComparator.containsChange(astSignFix, astSignChange);
		log.info("(Found/Total) Fixes : " + astSignComparator.getMatchedNumChanges()+ "/" + astSignComparator.getTotalNumChanges());
		assertTrue(status);
		log.info("YES, Signature contains Fix " + "\n" );

		//Defective Version Must NOT have the SourceCode change Elements
		log.info("Signature Under Test : Known to be a Defective Version \n");
		assertFalse(astSignComparator.containsChange(astSignDef, astSignChange));
		log.info("(Found/Total) Fixes : " + astSignComparator.getMatchedNumChanges()+ "/" + astSignComparator.getTotalNumChanges());
	    log.info("NO, Signature Doesn't contain Fix" );

	    //Random Signature Under Test (Test should pass if the signature under test is close to the fixed version)
	    /*log.info("Random Signature Under Test \n");
		assertTrue(astSignComparator.containsChange(astSignUnderTest, astSignChange));
		log.info("YES, Signature contains Fix " + "\n" );*/

	}

}
