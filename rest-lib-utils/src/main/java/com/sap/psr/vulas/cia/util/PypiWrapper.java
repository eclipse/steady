package com.sap.psr.vulas.cia.util;

import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.sap.psr.vulas.cia.model.pypi.PypiRelease;
import com.sap.psr.vulas.cia.model.pypi.PypiResponse;
import com.sap.psr.vulas.cia.util.ArtifactDownloader.DefaultRequestCallback;
import com.sap.psr.vulas.cia.util.ArtifactDownloader.FileResponseExtractor;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class PypiWrapper implements RepositoryWrapper {

	private static Logger log = LoggerFactory.getLogger(PypiWrapper.class);
	
	private static String searchAVUrl ;
	
	private static String searchAUrl ;
	
	private static boolean CONFIGURED = false;
	
	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();
	
	static {
		SUPP_LANG.add(ProgrammingLanguage.PY);
		searchAUrl = VulasConfiguration.getGlobal().getConfiguration().getString("vulas.lib-utils.pypi.search", null).concat("{artifact}/json");
		searchAVUrl = VulasConfiguration.getGlobal().getConfiguration().getString("vulas.lib-utils.pypi.search", null).concat("{artifact}/{version}/json");
		if(searchAUrl!=null)
			CONFIGURED=true;
	}
		
	@Override
	public Set<ProgrammingLanguage> getSupportedLanguages() {
		return SUPP_LANG;
	}
	
	private PypiResponse searchInPypi(String _artifact, String _version) throws InterruptedException{		
		
		final Map<String,String> params = new HashMap<String,String>();
		
		String url = null;
		params.put("artifact", _artifact);
		if(_version!=null) {
			params.put("version", _version); 
			url=searchAVUrl;
		}else
			url=searchAUrl;
		
		final RestTemplate rest_template = new RestTemplate();
		ResponseEntity<PypiResponse> responseEntity = null;
		
		PypiResponse result = null;
		try{
			responseEntity = rest_template.getForEntity(url, PypiResponse.class, params);
			if(responseEntity!=null)
				result = responseEntity.getBody();
		}
		catch(HttpServerErrorException he){
			PypiWrapper.log.error("HttpServerErrorException: Received status ["+he.getStatusCode()+"] calling url ["+url+"] with artifact ["+params.get("artifact")+"]");
		}
		catch(HttpClientErrorException he){
			PypiWrapper.log.warn("HttpClientErrorException: Received status ["+he.getStatusCode()+"] calling url ["+url+"] with artifact ["+params.get("artifact")+"]");
		}
		catch(Exception e){
			PypiWrapper.log.warn("Exception while searching in Pypy for url ["+url+"] with artifact ["+params.get("artifact")+"]", e);
		}
		return result;
	}
	
	@Override
	public Set<Artifact> getAllArtifactVersions(String group, String artifact, String classifier, String packaging)
			throws Exception {


		Set<Artifact> result = new TreeSet<Artifact>();
		final PypiResponse response = this.searchInPypi(artifact,null);
		
		
		if(response!=null){
			for(String k:response.getReleases().keySet()){
				//the inner loop only creates artifacts if the key (e.g., "1.0.1") contains something
				// and it creates an artifact with the timestamp of the first element found
				// TODO: should we pick wheel, tar.gz, or what?
				for(PypiRelease release : response.getReleases().get(k)){
						Artifact a = new Artifact(artifact,artifact,k);
						a.setProgrammingLanguage(ProgrammingLanguage.PY);
						if(release.getUpload_time()!=null && !release.getUpload_time().equals("")){
							String upload_time = release.getUpload_time().replace("T"," ");
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							Date parsedDate = dateFormat.parse(upload_time);
							a.setTimestamp(parsedDate.getTime());
						}
						result.add(a);
						break;
					}
			}
		}
		return result;
	}

	@Override
	public Set<Artifact> getGreaterArtifactVersions(String group, String artifact, String greaterThanVersion,
			String classifier, String packaging) throws Exception {
		
		Set<Artifact> all = this.getAllArtifactVersions(group, artifact, null, null);
		Artifact toCompare=null;
		Set<Artifact> greater = new TreeSet<Artifact>();
		
		for(Artifact a : all){
			if(a.getLibId().getVersion().equals(greaterThanVersion)){
				toCompare=a;
				break;
			}
		}
		// Return bad request if version is not found
		if(toCompare==null){
			log.error("Version [" + greaterThanVersion + "] not found in Pypi for group [" + group + "] and artifact [" + artifact + "]");
			return null;
		}
		for (Artifact t: all){
			if (t.compareTo(toCompare)>0)
				greater.add(t);
		}
		
		return greater;
		
	}

	@Override
	public Artifact getLatestArtifactVersion(String group, String artifact, String classifier, String packaging)
			throws Exception {

		//by default the search return the info for the latest version
		final PypiResponse response = this.searchInPypi(artifact, null);
		
		// we ignore the packaging when looking for the latest version
		return this.getArtifactFromResponse(response, null);
	}

	private Artifact getArtifactFromResponse(PypiResponse response, String packaging) throws ParseException{
		Artifact result = null;
		if(response!=null){
			String name = response.getInfo().getName();
			String version = response.getInfo().getVersion();
			
			//loop over all releases to identify the requested release
			for(String k:response.getReleases().keySet()){
				if(k.equals(version)){
					//loop over the artifact for the given release to check for packaging (if any) and get timestamp
					for(PypiRelease release : response.getReleases().get(k)){
						if((packaging==null || packaging.equals("")) && release.getUpload_time()!=null && !release.getUpload_time().equals("")){
							result = new Artifact(name.toLowerCase(),name.toLowerCase(),version);
							String upload_time = release.getUpload_time().replace("T"," ");
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
							result.setTimestamp(dateFormat.parse(upload_time).getTime());
							result.setProgrammingLanguage(ProgrammingLanguage.PY);
							break;
						}
						else if(packaging!=null && !packaging.equals("") && packaging.equals(release.getPackagetype())){
							result = new Artifact(name.toLowerCase(),name.toLowerCase(),version);
							result.setPackaging(packaging);
							if(release.getUpload_time()!=null && !release.getUpload_time().equals("")){
								String upload_time = release.getUpload_time().replace("T"," ");
								SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
								result.setTimestamp(dateFormat.parse(upload_time).getTime());
							}
							result.setProgrammingLanguage(ProgrammingLanguage.PY);
							break;
						}
					}
					break;
				}
			}
		}
		return result;
	}
	
	@Override
	public Artifact getArtifactVersion(String group, String artifact, String version, String classifier,
			String packaging,ProgrammingLanguage lang) throws Exception {

		final PypiResponse response = this.searchInPypi(artifact,version);
		
		return this.getArtifactFromResponse(response, packaging);

	}

	@Override
	public Path downloadArtifact(Artifact a) throws Exception {
		String downloadUrl = null;
		Path result = null;
		try{
			final PypiResponse response = this.searchInPypi(a.getLibId().getArtifact(),a.getLibId().getVersion());	

			if(response!=null){
				for(String k:response.getReleases().keySet()){
					if(k.equals(a.getLibId().getVersion())){
						for(PypiRelease release : response.getReleases().get(k)){
							if(release.getPackagetype().equals(a.getPackaging())){
								downloadUrl = release.getUrl();
								//if we found the matching packaging, we stop looping over the artifacts for the given release
								break;
							}
						}
						//if we found the release matching the one requested, we stop looping no matter whether the packaging was there or not 
						break;
					}
				}
				// Download with the received url!
				
				if(downloadUrl!=null){
					final RestTemplate rest_template = new RestTemplate();
					
					
					rest_template.execute(downloadUrl, HttpMethod.GET, new DefaultRequestCallback(), new FileResponseExtractor(a, a.getAbsM2Path()));
					result = a.getAbsM2Path();
				}
			}
		}catch(HttpClientErrorException e){
			PypiWrapper.log.error(a + " not available at [" +downloadUrl+ "]");
		}
		return result;
	}

	
	@Override
	public Artifact getArtifactForDigest(String digest) throws RepoException {
		throw new NotImplementedException();
	}

	@Override
	public boolean isConfigured() {
		return CONFIGURED;
	}

}
