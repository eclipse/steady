package com.sap.psr.vulas.cia.rest;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Set;



import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import com.sap.psr.vulas.cia.util.MavenCentralWrapper;
import com.sap.psr.vulas.cia.util.NexusWrapper;
import com.sap.psr.vulas.cia.util.PypiWrapper;
import com.sap.psr.vulas.cia.util.RepositoryDispatcher;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.Version;

public class IT01_ArtifactControllerTest {

	
	
	@Test
	public void getAllVersionsExistingTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Set<Artifact> response = r.getAllArtifactVersions("commons-fileupload", "commons-fileupload", null, null);
			System.out.println(response.size());
			// the number of returned version is not stable (13/14..) assert modified to checked greater to avoid failures due to external services
			assertTrue(response.size()>=10);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllVersionsNotFoundTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Set<Artifact> response = r.getAllArtifactVersions("commons-fileupload", "commons-", null, null);
			
			assertTrue(response.size()==0);
		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getAllVersionsPythonTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Set<Artifact> response = r.getAllArtifactVersions("django", "django", null, null);
			
			assertTrue(response.size()>=156);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getLatestVersionPythonTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Artifact response = r.getLatestArtifactVersion("django", "django", null, null);
			
			assertTrue(new Version(response.getLibId().getVersion()).compareTo(new Version("2.0.4"))>0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getGreaterVersionPythonTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Set<Artifact> response = r.getGreaterArtifactVersions("django", "django", "2.0.2", null, null);
			
			assertTrue(response.size()>=6);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void getArtifactVersionTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.2.2", null, null, null);
			System.out.println(response.toString());
			assertTrue(response.getLibId().getVersion().equals("1.2.2"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getArtifactVersionNexusTest(){
		
		NexusWrapper r = new NexusWrapper();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.2.2", null, null, ProgrammingLanguage.JAVA);
			System.out.println(response.toString());
			assertTrue(response.getLibId().getVersion().equals("1.2.2"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getArtifactVersionMavenTest(){
		
		MavenCentralWrapper r = new MavenCentralWrapper();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.2.2", null, null, ProgrammingLanguage.JAVA);
			System.out.println(response.toString());
			assertTrue(response.getLibId().getVersion().equals("1.2.2"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getArtifactVersionPythonTest(){
		
		PypiWrapper r = new PypiWrapper();
		try {
			Artifact response = r.getArtifactVersion("django", "django", "1.2.2", null, null, ProgrammingLanguage.PY);

			assertTrue(response.getLibId().getVersion().equals("1.2.2"));	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getArtifactVersionPythonTest1(){
		
		PypiWrapper r = new PypiWrapper();
		try {
			Artifact response = r.getArtifactVersion("django", "django", "1.2.2", null, null,null);

			assertTrue(response.getLibId().getVersion().equals("1.2.2"));	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getGreaterVersionTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Set<Artifact> response = r.getGreaterArtifactVersions("commons-fileupload", "commons-fileupload", "1.2.2", null, null);
			for(Artifact c : response)
				System.out.println(c.toString());
			assertTrue(response.size()>3);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getGreaterVersionNexusTest(){
		
		NexusWrapper r = new NexusWrapper();
		try {
			Set<Artifact> response = r.getGreaterArtifactVersions("commons-fileupload", "commons-fileupload", "1.2.2", null, null);
			for(Artifact c : response)
				System.out.println(c.toString());
			assertTrue(response.size()>0);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getGreaterVersionMavenTest(){
		
		MavenCentralWrapper r = new MavenCentralWrapper();
		try {
			Set<Artifact> response = r.getGreaterArtifactVersions("commons-fileupload", "commons-fileupload", "1.2.2", null, null);
			for(Artifact c : response)
				System.out.println(c.toString());
			assertTrue(response.size()>3);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getLatestVersionTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Artifact response = r.getLatestArtifactVersion("commons-fileupload", "commons-fileupload", null, null);
					
			System.out.println(response.getLibId());
			Version v = new Version(response.getLibId().getVersion());
			Version v1 = new Version("1.3.3");
			assertTrue(v.compareTo(v1)>=0);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getLatestVersionNexusTest(){
		
		NexusWrapper n = new NexusWrapper();
		try {
			Artifact nexus_response = n.getLatestArtifactVersion("com.fasterxml.jackson.core", "jackson-databind", null, null);
			
			System.out.println("Nexus" + nexus_response.getLibId());
			//assertTrue(nexus_response.getLibId().getVersion().equals("2.9.5"));
			Version v = new Version(nexus_response.getLibId().getVersion());
			Version v1 = new Version("2.9.5");
			assertTrue(v.compareTo(v1)>=0);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void getLatestVersionMavenTest(){
		
		MavenCentralWrapper m = new MavenCentralWrapper();
		try {
			
			Artifact maven_response = m.getLatestArtifactVersion("com.fasterxml.jackson.core", "jackson-databind", null, null);
			
			System.out.println("Maven" + maven_response.getLibId());
			//assertTrue(maven_response.getLibId().getVersion().equals("2.9.5"));
			Version v = new Version(maven_response.getLibId().getVersion());
			Version v1 = new Version("2.9.6");
			assertTrue(v.compareTo(v1)>=0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void doesVersionSourceExistTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.3.3", "sources", null, ProgrammingLanguage.JAVA);
			System.out.println(response.getLibId());
			assertTrue(response.getLibId().getVersion().equals("1.3.3"));
			
			Artifact response1 = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.1.1", "sources", null, ProgrammingLanguage.JAVA);
			
			assertTrue(response1==null);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void doesVersionSourceExistNexusTest(){
		
		NexusWrapper r = new NexusWrapper();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.3.3", "sources", null, ProgrammingLanguage.JAVA);
			System.out.println(response.getLibId());
			assertTrue(response.getLibId().getVersion().equals("1.3.3"));
			
			Artifact response1 = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.1.1", "sources", null, ProgrammingLanguage.JAVA);
			
			assertTrue(response1==null);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void doesVersionSourceExistMavenTest(){
		
		MavenCentralWrapper r = new MavenCentralWrapper();
		try {
			Artifact response = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.3.3", "sources", null, ProgrammingLanguage.JAVA);
			System.out.println(response.getLibId());
			assertTrue(response.getLibId().getVersion().equals("1.3.3"));
			
			Artifact response1 = r.getArtifactVersion("commons-fileupload", "commons-fileupload", "1.1.1", "sources", null, ProgrammingLanguage.JAVA);
			
			assertTrue(response1==null);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void downloadArtifactTest(){
		
		RepositoryDispatcher r = new RepositoryDispatcher();
		try {
			Artifact toDownload = new Artifact("commons-fileupload", "commons-fileupload", "1.3.3");
			toDownload.setClassifier("sources");
			toDownload.setPackaging("jar");
			toDownload.setProgrammingLanguage(ProgrammingLanguage.JAVA);
			
			boolean existing = false;
			//delete artifact if already existing
			if(toDownload.getAbsM2Path().toFile().exists()){
				existing = true;
				toDownload.getAbsM2Path().toFile().delete();
				}
			
			Path p = r.downloadArtifact(toDownload);
			System.out.println("Artifact downloaded to [" + toDownload.getAbsM2Path()+"]");
			assertTrue(p.toFile().exists());
			
			if(!existing && p.toFile().exists())
				p.toFile().delete();
			
			
			toDownload = new Artifact("commons-fileupload", "commons-", "1.3.3");
			toDownload.setClassifier("sources");
			toDownload.setPackaging("jar");
			//TODO: add assertion that exception is thrown (unless they publish sources for 1.3.3) 
			try{
				p = r.downloadArtifact(toDownload);
			} catch (FileNotFoundException e) {
				System.out.println("File [" + toDownload.toString() + "] not found");
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
		
}
