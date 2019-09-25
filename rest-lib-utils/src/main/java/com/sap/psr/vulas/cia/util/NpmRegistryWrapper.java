package com.sap.psr.vulas.cia.util;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.sap.psr.vulas.cia.model.npmRegistry.NpmRegistryResponse;
import com.sap.psr.vulas.cia.model.npmRegistry.NpmRegistryVersion;
import com.sap.psr.vulas.cia.model.pypi.PypiRelease;
import com.sap.psr.vulas.cia.model.pypi.PypiResponse;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * <p>NpmRegistryWrapper class.</p>
 *
 */
public class NpmRegistryWrapper  implements RepositoryWrapper  {
	
	private static Logger log = LoggerFactory.getLogger(NpmRegistryWrapper.class);
	
	private static String searchAVUrl ;
	
	private static String searchAUrl ;
	
	private static boolean CONFIGURED = false;
	
	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();
	
	static {
		SUPP_LANG.add(ProgrammingLanguage.JS);
		searchAUrl = VulasConfiguration.getGlobal().getConfiguration().getString("vulas.lib-utils.npmregistry.search", null).concat("{artifact}");
		searchAVUrl = VulasConfiguration.getGlobal().getConfiguration().getString("vulas.lib-utils.npmregistry.search", null).concat("{artifact}/{version}");
		if(searchAUrl!=null)
			CONFIGURED=true;
	}

	/** {@inheritDoc} */
	@Override
	public Set<ProgrammingLanguage> getSupportedLanguages() {
		return SUPP_LANG;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isConfigured() {
		return CONFIGURED;
	}
	
	private NpmRegistryResponse searchInNpmRegistry(String _artifact, String _version) throws InterruptedException{		
		
		final Map<String,String> params = new HashMap<String,String>();
		
		String url = null;
		params.put("artifact", _artifact);
		if(_version!=null) {
			params.put("version", _version); 
			url=searchAVUrl;
		}else
			url=searchAUrl;
		
		final RestTemplate rest_template = new RestTemplate();
		ResponseEntity<NpmRegistryResponse> responseEntity = null;
		
		NpmRegistryResponse result = null;
		try{
			responseEntity = rest_template.getForEntity(url, NpmRegistryResponse.class, params);
			if(responseEntity!=null)
				result = responseEntity.getBody();
		}
		catch(HttpServerErrorException he){
			NpmRegistryWrapper.log.error("HttpServerErrorException: Received status ["+he.getStatusCode()+"] calling url ["+url+"] with artifact ["+params.get("artifact")+"]");
		}
		catch(HttpClientErrorException he){
			NpmRegistryWrapper.log.warn("HttpClientErrorException: Received status ["+he.getStatusCode()+"] calling url ["+url+"] with artifact ["+params.get("artifact")+"]");
		}
		catch(Exception e){
			NpmRegistryWrapper.log.warn("Exception while searching in Npm registry for url ["+url+"] with artifact ["+params.get("artifact")+"]", e);
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Artifact> getAllArtifactVersions(String group, String artifact, String classifier, String packaging)
			throws Exception {
		Set<Artifact> result = new TreeSet<Artifact>();
		final NpmRegistryResponse response = this.searchInNpmRegistry(artifact,null);
		
		
		if(response!=null){
			for(String k:response.getVersions().keySet()){
				//create artifacts if the key (e.g., "1.0.1") contains something
				
				Artifact a = new Artifact(artifact,artifact,k);
				a.setProgrammingLanguage(ProgrammingLanguage.JS);
				//TODO: we will have to find a way to set the timestamp
//						if(release.getUpload_time()!=null && !release.getUpload_time().equals("")){
//							String upload_time = release.getUpload_time().replace("T"," ");
//							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//							Date parsedDate = dateFormat.parse(upload_time);
//							a.setTimestamp(parsedDate.getTime());
//						}
				result.add(a);
			
			}
		}
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public Set<Artifact> getGreaterArtifactVersions(String group, String artifact, String greaterThanVersion,
			String classifier, String packaging) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Artifact getLatestArtifactVersion(String group, String artifact, String classifier, String packaging)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Artifact getArtifactVersion(String group, String artifact, String version, String classifier,
			String packaging, ProgrammingLanguage lang) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Path downloadArtifact(Artifact a) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Artifact getArtifactForDigest(String digest) throws RepoException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}
