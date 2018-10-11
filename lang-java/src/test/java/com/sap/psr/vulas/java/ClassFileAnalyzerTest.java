package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzerFactory;
public class ClassFileAnalyzerTest {
	
	/**
	 * Test that certain constructs are correctly found in a give class file.
	 */
	@Test 
	public void classFileAnalyzerTest () {
		try {
			// Analyze a test class file
			ClassFileAnalyzer cfa = (ClassFileAnalyzer)FileAnalyzerFactory.buildFileAnalyzer(new File("./src/test/resources/Callgraph.class"));
			final Map<ConstructId,Construct> c = cfa.getConstructs();
						
			// Check that certain constructs have been found
			JavaConstructorId cid = JavaId.parseConstructorQName("com.sap.psr.vulas.cg.Callgraph(Graph)");
			assertEquals(true, cfa.containsConstruct(cid));
			
			JavaMethodId mid = JavaId.parseMethodQName("com.sap.psr.vulas.cg.Callgraph.getAllEdges(String)");
			assertEquals(true, cfa.containsConstruct(cid));
			
			JavaMethodId not_existing_method = JavaId.parseMethodQName("com.sap.psr.vulas.cg.Callgraph.1234(String)");
			assertEquals(false, cfa.containsConstruct(not_existing_method));
		} catch (FileAnalysisException e) {
			System.err.println(e.getMessage());
		}
	}
}
