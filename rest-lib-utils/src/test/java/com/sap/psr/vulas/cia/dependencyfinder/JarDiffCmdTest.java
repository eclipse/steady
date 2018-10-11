package com.sap.psr.vulas.cia.dependencyfinder;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.diff.JarDiffResult;

public class JarDiffCmdTest {
	
	@Test
	public void testDoProcessing() {
		Artifact old_doc = new Artifact("commons-fileupload","commons-fileupload","1.1.1");
		
		
		Artifact new_doc = new Artifact("commons-fileupload","commons-fileupload","1.3.1");
		
		Path old_jar = Paths.get("./src/test/resources/commons-fileupload-1.1.1.jar");
		Path new_jar = Paths.get("./src/test/resources/commons-fileupload-1.3.1.jar");
		
		JarDiffCmd cmd = new JarDiffCmd(old_doc, old_jar, new_doc, new_jar);
		
		try {
			final String[] args = new String[] { "-old", old_jar.toString(), "-new", new_jar.toString(), "-name", "xyz", "-code" };
			cmd.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JarDiffResult result = cmd.getResult();
		
		
		String json = JacksonUtil.asJsonString(result);
		System.out.println(json);
		assertTrue(result!=null);
	}
}
