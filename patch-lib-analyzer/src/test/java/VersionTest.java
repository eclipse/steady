import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.junit.Test;

import com.sap.psr.vulas.patcheval.representation.ArtifactResult2;



public class VersionTest {


	@Test
	public void compareVersionTest()  {
		ArtifactResult2 ar = new ArtifactResult2("org.apache.cxf","cxf-rt-rs-extension-providers","3.1.4-sap-05",false);
		ArtifactResult2 ar1 = new ArtifactResult2("org.apache.cxf","cxf-rt-rs-extension-providers","3.1.1",true, Long.valueOf("1433534079000"));
		System.out.println(ar.compareVersion(ar1));
		assertTrue( ar.compareVersion(ar1)>0 );
		
		
		ArtifactResult2 ar3 = new ArtifactResult2("org.apache.httpcomponents","httpclient","4.2.1-atlassian-5",false);
		ArtifactResult2 ar4 = new ArtifactResult2("org.apache.httpcomponents","httpclient","4.2.6",true);
		System.out.println(ar4.compareVersion(ar3));
		assertTrue( ar4.compareVersion(ar3)>0 );
	}
	
	
}
