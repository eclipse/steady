package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.persistence.EntityNotFoundException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.AffectedLibraryRepository;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.repo.BugRepository;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.backend.repo.GoalExecutionRepository;
import com.sap.psr.vulas.backend.repo.LibraryRepository;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.FileUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class,webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class HubIntegrationControllerTest {
	
	
    private MockMvc mockMvc;
    private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;
    
    public static final String TEST_DEFAULT_SPACE = "public";
    public static final String TEST_DEFAULT_TENANT = "default";

   

    @Autowired
    private ApplicationRepository appRepository;
    
    @Autowired
    private LibraryRepository libRepository;
    
    @Autowired
    private ConstructIdRepository cidRepository;
    
    @Autowired
    private GoalExecutionRepository gexeRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;
    

    @Autowired
    private BugRepository bugRepository;
    

    @Autowired
    private TenantRepository tenantRepository;
    

    @Autowired
    private SpaceRepository spaceRepository;
    

    @Autowired
    private AffectedLibraryRepository affLibRepository;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                new Predicate<HttpMessageConverter<?>>() {
					@Override
					public boolean test(HttpMessageConverter<?> hmc) {
						return hmc instanceof MappingJackson2HttpMessageConverter;
					}
				}).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        this.gexeRepository.deleteAll();
        this.appRepository.deleteAll();
        this.libRepository.deleteAll();
        this.cidRepository.deleteAll();
        
    	createDefaultTenantandSpace();
    }
    
    @After
    public void reset() throws Exception {
    	this.gexeRepository.deleteAll();
    	this.appRepository.deleteAll();
        this.libRepository.deleteAll();
        this.bugRepository.deleteAll();
        this.cidRepository.deleteAll();
       
    }
    
    @Ignore
    @Test
    public void testGetHubApps() throws Exception {
    	// Rest-post http-client 4.1.3
    	final Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_http-client-4.1.3.json")), Library.class);
    	this.libRepository.customSave(lib);
    	    	
    	// Rest-post bug 
    	final Bug bug = (Bug)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/bug_2015-5262.json")), Bug.class);
    	this.bugRepository.customSave(bug,true);
    	
    	// Rest-post app using http-client
    	final Application app = new Application(APP_GROUP, APP_ARTIFACT, "0.0." + APP_VERSION);

		// Dependencies
		final Set<Dependency> app_dependency = new HashSet<Dependency>(); 
		app_dependency.add(new Dependency(app, lib, Scope.COMPILE, false, "httpclient-4.1.3.jar"));
		String token = spaceRepository.getDefaultSpace(null).getSpaceToken();
		app.setSpace(spaceRepository.getDefaultSpace(null));
		
		app.setDependencies(app_dependency);
    	this.appRepository.customSave(app);    	
    	
    	// Read all public apps
    	mockMvc.perform(get("/hubIntegration/apps"))
        .andExpect(status().isOk()).andExpect(content().string("[\""+TEST_DEFAULT_SPACE+" ("+token+") "+app.getMvnGroup()+":"+app.getArtifact()+":"+app.getVersion()+"\"]"));	
    }    
    
    @Test
    public void testGetHubAppVulnerabilities() throws Exception {
    	final Library lib_foo = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/lib.json")), Library.class);
    	this.libRepository.customSave(lib_foo);
    	
    	final Library lib_bar = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/lib_bar.json")), Library.class);
    	this.libRepository.customSave(lib_bar);
    	    	
    	final Bug bug = (Bug)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/bug_foo.json")), Bug.class);
    	this.bugRepository.customSave(bug,true);    	
    	
    	// App with dependencies on foo and bar
    	final Application app = new Application(APP_GROUP, APP_ARTIFACT, "0.0." + APP_VERSION);
		final Set<Dependency> app_dependency = new HashSet<Dependency>(); 
		app_dependency.add(new Dependency(app, lib_foo, Scope.COMPILE, false, "foo.jar"));
		app_dependency.add(new Dependency(app, lib_bar, Scope.COMPILE, false, "bar.jar"));
		app.setSpace(spaceRepository.getDefaultSpace(null));
		app.setDependencies(app_dependency);
    	this.appRepository.customSave(app);

    	// Construct item ID
    	final String token = spaceRepository.getDefaultSpace(null).getSpaceToken();
    	String item = TEST_DEFAULT_SPACE+" ("+token+") "+app.getMvnGroup()+":"+app.getArtifact()+":"+app.getVersion();
    	
    	// Get vuln deps - default treatment of unassessed: IGN_UNASS_KNOWN
    	MvcResult response = mockMvc.perform(get("/hubIntegration/apps/" + item + "/vulndeps"))
	        .andExpect(status().isOk()).andExpect(jsonPath("$[0].spaceToken").exists())
	        .andExpect(jsonPath("$[0].appId").exists())    	
	        .andExpect(jsonPath("$[0].lastScan").exists())
	        .andExpect(jsonPath("$[0].projectId").value("bar.jar")) // Only bar.jar is reported (unknown digest), foo.jar is ignored (well-known digest)
	        .andExpect(jsonPath("$[0].reachable").exists()).andReturn();
    	
    	// Only bar.jar is reported (unknown digest), foo.jar is ignored (well-known digest)
    	Set<HubIntegrationController.VulnerableItemDependency> vuln_deps = (Set<HubIntegrationController.VulnerableItemDependency>)JacksonUtil.asObject(response.getResponse().getContentAsString(), Set.class);
    	assertEquals(1, vuln_deps.size());
    	
    	// Get vuln deps - include all unassessed: IGN_UNASS_OFF
    	response = mockMvc.perform(get("/hubIntegration/apps/" + item + "/vulndeps?ignoreUnassessed=" + HubIntegrationController.IGN_UNASS_OFF))
	        .andExpect(status().isOk()).andExpect(jsonPath("$[0].spaceToken").exists())
	        .andExpect(jsonPath("$[0].appId").exists())    	
	        .andExpect(jsonPath("$[0].lastScan").exists())
	        .andExpect(jsonPath("$[0].reachable").exists()).andReturn();
    	
    	// Both vuln in foo and bar are reported
    	vuln_deps = (Set<HubIntegrationController.VulnerableItemDependency>)JacksonUtil.asObject(response.getResponse().getContentAsString(), Set.class);
    	assertEquals(2, vuln_deps.size());
    }
    
    @Ignore
    @Test
    public void testGetHubAppWithSlashChar() throws Exception {
    	// Rest-post http-client 4.1.3
    	Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/lib.json")), Library.class);
    	this.libRepository.customSave(lib);
    	    	
    	// Rest-post bug 
    	final Bug bug = (Bug)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/bug_foo.json")), Bug.class);
    	this.bugRepository.customSave(bug,true);
    	    	
    	// Rest-post app using http-client
    	final Application app = new Application(APP_GROUP, APP_ARTIFACT, "0.0./r" + APP_VERSION);

		// Dependencies
		final Set<Dependency> app_dependency = new HashSet<Dependency>(); 
		app_dependency.add(new Dependency(app,lib, Scope.COMPILE, false, "foo.jar"));
		String token = spaceRepository.getDefaultSpace(null).getSpaceToken();
		app.setSpace(spaceRepository.getDefaultSpace(null));
		
		app.setDependencies(app_dependency);
    	this.appRepository.customSave(app);
    	
    	// Read all apps
    	MvcResult result = mockMvc.perform(get("/hubIntegration/apps/"))
        .andExpect(status().isOk()).andReturn();
    	
    	String item=result.getResponse().getContentAsString();
    	item = item.substring(2, item.length()-2);
    	
    	// Read all public apps
    	MvcResult vulndeps = mockMvc.perform(get("/hubIntegration/apps/"+item+"/vulndeps"))
        .andExpect(status().isOk()).andExpect(jsonPath("$[0].spaceToken").exists())
        .andExpect(jsonPath("$[0].appId",is(1)))    	
        .andExpect(jsonPath("$[0].lastScan").exists())
        .andExpect(jsonPath("$[0].reachable",is(false))).andReturn();
    }
  
    
     
    private static final String APP_GROUP = "com.acme";
    private static final String APP_ARTIFACT = "vulas";
    private static int APP_VERSION = 1; // Used to create unique apps
    
		
	public static String getAppUri(Application _app) {
		return "/apps/" + _app.getMvnGroup()+ "/" + _app.getArtifact() + "/" + _app.getVersion();
	}
	
	public static String getAppsExportUri(String _format) {
		return "/apps/export?format=" + _format;
	}
	
	
	private void createDefaultTenantandSpace() {
		//default tenant
		Tenant default_tenant = null;
		try{
			default_tenant = TenantRepository.FILTER.findOne(tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));
		}catch(EntityNotFoundException e){
			default_tenant = new Tenant();
			default_tenant.setTenantToken(TEST_DEFAULT_TENANT);
			default_tenant.setTenantName(TEST_DEFAULT_TENANT);
			default_tenant.setDefault(true);
			tenantRepository.save(default_tenant);
			default_tenant = TenantRepository.FILTER.findOne(tenantRepository.findBySecondaryKey(TEST_DEFAULT_TENANT));
			
		}
		
		//default space
		Space default_space = null;
		
		try{
			default_space = SpaceRepository.FILTER.findOne(spaceRepository.findBySecondaryKey(TEST_DEFAULT_SPACE));
		}catch(EntityNotFoundException e){
			default_space = new Space();
			default_space.setSpaceName(TEST_DEFAULT_SPACE);
			default_space.setSpaceToken(TEST_DEFAULT_SPACE);
			default_space.setDefault(true);
			default_space.setExportConfiguration(ExportConfiguration.DETAILED);
			default_space.setSpaceDescription("bar");
			default_space.setSpaceOwners(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
			default_space.setTenant(default_tenant);
			spaceRepository.save(default_space);
			
			default_space = SpaceRepository.FILTER.findOne(spaceRepository.findBySecondaryKey(TEST_DEFAULT_SPACE));
		}
	}
}
