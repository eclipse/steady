package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.psr.vulas.backend.model.AffectedLibrary;
import com.sap.psr.vulas.backend.model.Application;
import com.sap.psr.vulas.backend.model.Bug;
import com.sap.psr.vulas.backend.model.ConstructChange;
import com.sap.psr.vulas.backend.model.ConstructChangeType;
import com.sap.psr.vulas.backend.model.ConstructId;
import com.sap.psr.vulas.backend.model.Dependency;
import com.sap.psr.vulas.backend.model.GoalExecution;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.backend.model.Path;
import com.sap.psr.vulas.backend.model.PathNode;
import com.sap.psr.vulas.backend.model.Property;
import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.model.VulnerableDependency;
import com.sap.psr.vulas.backend.repo.AffectedLibraryRepository;
import com.sap.psr.vulas.backend.repo.ApplicationRepository;
import com.sap.psr.vulas.backend.repo.BugRepository;
import com.sap.psr.vulas.backend.repo.ConstructIdRepository;
import com.sap.psr.vulas.backend.repo.GoalExecutionRepository;
import com.sap.psr.vulas.backend.repo.LibraryRepository;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.categories.RequiresNetwork;
import com.sap.psr.vulas.shared.enums.BugOrigin;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ContentMaturityLevel;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.enums.GoalType;
import com.sap.psr.vulas.shared.enums.PathSource;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.enums.PropertySource;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.Constants;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StopWatch;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class,webEnvironment= SpringBootTest.WebEnvironment.MOCK)
//SpringApplicationConfiguration was deprecated in spring 1.4 and removed in 1.5
//in favour of a more fine grained test configurations. Right now we are using SpringBootTest which
// starts the entire spring framework for testing, but we could use the test slicing capabilities 
// with @MvcTest that would only start the required slice of application (Controller) 
//@SpringApplicationConfiguration(classes = MainController.class) 
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationControllerTest {
	
	private MediaType contentTypeJson = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

	private MediaType contentTypeCsv = new MediaType("text", "csv", Charset.forName("utf8"));
	
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
    	this.affLibRepository.deleteAll();
        this.libRepository.deleteAll();
        this.bugRepository.deleteAll();
        this.cidRepository.deleteAll();
       
    }
    
    /**
     * Rest-read non-existing app.
     * @throws Exception
     */
    @Test
    public void testGetNotFound() throws Exception {
        mockMvc.perform(get("/apps/group/artifact/version"))
                .andExpect(status().isNotFound());
    }

    /**
     * Repo-save and rest-get.
     * @throws Exception
     */
    @Test
    public void testGetApp() throws Exception {
    	libRepository.customSave(this.createExampleLibrary());
       	final Application app = this.createExampleApplication();
    	System.out.println("App as JSON: " + JacksonUtil.asJsonString(app));
    	this.appRepository.customSave(app);
     
    	
       	final MockHttpServletRequestBuilder get_builder = get(getAppUri(app));
    	mockMvc.perform(get_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.group", is(APP_GROUP)))
                .andExpect(jsonPath("$.artifact", is(APP_ARTIFACT)))
                .andExpect(jsonPath("$.lastVulnChange", is(String.class)))
                .andExpect(jsonPath("$.lastChange", is(String.class)))
                .andExpect(jsonPath("$.version", is(app.getVersion())));
    	
       	
    	assertEquals(1, this.appRepository.count());
    }
    
    /**
     * Repo-save and JSON export.
     * @throws Exception
     */
    @Test
    public void testExportApps() throws Exception {
    	libRepository.customSave(this.createExampleLibrary());
    	
    	// App 1
       	final Application app = this.createExampleApplication();
    	this.appRepository.customSave(app);
    	this.gexeRepository.customSave(app, this.createExampleGoalExecution(app, GoalType.CLEAN));
    	this.gexeRepository.customSave(app, this.createExampleGoalExecution(app, GoalType.APP));
    	
    	// App 2
    	final Application app2 = this.createExampleApplication();
    	this.appRepository.customSave(app2);
    	
    	String f = "jsOn";
    	MockHttpServletRequestBuilder get_builder = get(getAppsExportUri(f));
    	MvcResult result = mockMvc.perform(get_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andReturn();
    	
    	System.out.println(result.getResponse().getContentAsString());
    	
    	f = "csv";
    	get_builder = get(getAppsExportUri(f));
    	result = mockMvc.perform(get_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeCsv))
                .andReturn();
    	
    	System.out.println(result.getResponse().getContentAsString());
       	
    	f = "foo";
    	get_builder = get(getAppsExportUri(f));
    	result = mockMvc.perform(get_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeCsv))
                .andReturn();
    	
    	System.out.println(result.getResponse().getContentAsString());
    	
    	assertEquals(2, this.appRepository.count());
    }
    
    /**
     * Rest-post and rest-get.
     * @throws Exception
     */
    @Test
    public void testPost() throws Exception {
    	
    	//final Library lib = (Library)TestUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/lib.json")), Library.class);
    	final Library lib = this.createExampleLibrary();
    	libRepository.customSave(lib);
   // 	final Application app = this.createExampleApplication();
    	final Application app = (Application)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/app.json")), Application.class);
    	
    	// test post for vulas2.x (w/o space header) 
    	// Rest-post
    	final MockHttpServletRequestBuilder post_builder = post("/apps/")
    			.content(asJsonString(app).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.group", is(APP_GROUP)))
                .andExpect(jsonPath("$.artifact", is(APP_ARTIFACT)))
                .andExpect(jsonPath("$.version", is("0.0.1"))); // Value used in the JSON file
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    	
    	Application app1 = (Application)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/app.json")), Application.class);
    	app1.setVersion("0.0.2-SNAPSHOT");
    	
    	// test post for vulas3.x (w/o space header) 
    	// Rest-post
    	final MockHttpServletRequestBuilder post_builder2 = post("/apps/")
    			.content(asJsonString(app1).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.HTTP_SPACE_HEADER, TEST_DEFAULT_SPACE);
    	mockMvc.perform(post_builder2)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson));
    	
    	// Repo must contain 2
    	assertEquals(2, this.appRepository.count());
    	
    	// Rest-get without header
    	final MockHttpServletRequestBuilder get_builder = get("/apps/" + app.getMvnGroup()+ "/" + app.getArtifact()+"/"+app.getVersion());
    	mockMvc.perform(get_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.group", is(APP_GROUP)))
                .andExpect(jsonPath("$.artifact", is(APP_ARTIFACT)))
                .andExpect(jsonPath("$.version", is(app.getVersion() )))
                //.andExpect(jsonPath("$.dependencies[0].lib.sha1", is("sha1")))
                ;
    	
    }
    
    /**
     * Duplicate rest-post.
     * @throws Exception
     */
    @Test
    public void testDuplicatePost() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	libRepository.customSave(lib);
    	//final Application app = this.createExampleApplication();
    	final Application app = (Application)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/app.json")), Application.class);
    	
    	// Rest-post
    	final MockHttpServletRequestBuilder post_builder = post("/apps/")
    			.content(asJsonString(app).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.group", is(APP_GROUP)))
                .andExpect(jsonPath("$.artifact", is(APP_ARTIFACT)))
                .andExpect(jsonPath("$.version", is("0.0.1"))); // Version used in the JSON file
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    	
    	// Rest-post
    	mockMvc.perform(post_builder)	
                .andExpect(status().isConflict());
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    }
    
    @Test
    @Category(RequiresNetwork.class)
    public void testPostVulasTestapp() throws Exception {
    	// poi-ooxml takes ~8 min to upload ~43600 constructs
     	//Library lib = (Library)TestUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/libs-poi-ooxml-schemas-3.11-beta1.json")), Library.class);
     	Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_commons-fileupload-1.2.2.json")), Library.class);
    	MockHttpServletRequestBuilder post_builder = post("/libs/")
    			.content(JacksonUtil.asJsonString(lib).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.digest", is("1E48256A2341047E7D729217ADEEC8217F6E3A1A")));
    
    	Application app = (Application)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/apps-testapp-fileupload-1.2.2.json")), Application.class);
    	post_builder = post("/apps/")
    			.content(JacksonUtil.asJsonString(app).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$.group", is("com.acme")))
                .andExpect(jsonPath("$.artifact", is("vulas-testapp")));
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    	
    	//TODO add asserts on constructs and dependencies
    }
    
    @Test
    public void readAllApplications() throws Exception {
    	//libRepository.customSave(this.createExampleLibrary());
    	//appRepository.customSave(this.createExampleApplication());
    	//TODO perform check on the returned value
    	    	
    	// Read all public apps
    	mockMvc.perform(get("/apps")
    	.header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT)
    	.header(Constants.HTTP_SPACE_HEADER, TEST_DEFAULT_SPACE))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentTypeJson));
    	
    	// Read all apps for a non-existing token
    	mockMvc.perform(get("/apps")
     	    	  .header(Constants.HTTP_SPACE_HEADER, "does-not-exist"))
                  .andExpect(status().isNotFound())
                  .andExpect(content().contentType(contentTypeJson));
    	
    	// Read all apps for a non-existing token
    	mockMvc.perform(get("/apps"))
                  .andExpect(status().isOk())
                  .andExpect(content().contentType(contentTypeJson));
    }
    
    /**
     * Rest-post goal execution.
     * @param obj
     * @return
     */
    @Test
    public void testPostGoalExe() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	this.libRepository.customSave(lib);
    	final Application app = this.createExampleApplication();
    	this.appRepository.customSave(app);    	
    	
    	// Get latest goal execution (type APP)
    	GoalExecution latest_gexe = this.gexeRepository.findLatestGoalExecution(app, GoalType.APP);
    	assertEquals(null, latest_gexe);
    	
    	// Get latest goal execution (any type)
    	latest_gexe = this.gexeRepository.findLatestGoalExecution(app, null);
    	assertEquals(null, latest_gexe);
    	
    	final GoalExecution gexe = this.createExampleGoalExecution(app, GoalType.APP);
    	
    	// Rest-post
    	final MockHttpServletRequestBuilder post_builder = post(getAppUri(app) + "/goals")
    			.content(JacksonUtil.asJsonString(gexe).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	System.out.println("Gexe: " + JacksonUtil.asJsonString(gexe));
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson));
    	
    	// Repo must contain 1
    	assertEquals(1, this.gexeRepository.count());
    	
    	// Get latest goal execution (type APP)
    	latest_gexe = this.gexeRepository.findLatestGoalExecution(app, GoalType.APP);
    	assertEquals(gexe, latest_gexe);
    	
    	// Get latest goal execution (any type)
    	latest_gexe = this.gexeRepository.findLatestGoalExecution(app, null);
    	assertEquals(gexe, latest_gexe);
    	
    	// Goal exe of type REPORT has not been created, hence, should be null
    	latest_gexe = this.gexeRepository.findLatestGoalExecution(app, GoalType.REPORT);
    	assertEquals(null, latest_gexe);
    }
    
    /**
     * Repo-save and rest-clean
     * @param obj
     * @return
     */
    @Test
    @Transactional
    public void testCleanApp() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	this.libRepository.customSave(lib);
    	Application app = this.createExampleApplication();
    	app = this.appRepository.customSave(app);
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    	
    	// Rest-post
    	final MockHttpServletRequestBuilder post_builder = post(getAppUri(app))
    			.param("clean", "true")
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson));
    	
    	// Repo must contain 1
    	assertEquals(1, this.appRepository.count());
    	
    	// Check its number of constructs and dependencies
    	final Application managed_app = this.appRepository.findOne(app.getId());
    	Boolean isEmpty=(managed_app.getConstructs()==null || managed_app.getConstructs().isEmpty()) && (managed_app.getDependencies()==null || managed_app.getDependencies().isEmpty());
    	assertEquals(true, isEmpty);
    }
    
    @Test
    public void testPostPath() throws Exception {
    	final Application app = this.createExampleApplication();
    	final Bug bug = (Bug)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/dummy_app/bug_bar.json")), Bug.class);
    	final Path p = this.createExamplePath(app, bug);
    	System.out.println(p.toString(true));
    	System.out.println(JacksonUtil.asJsonString(p));
    }
    
    @Test
    public void testGetAppVulnerabilities() throws Exception {
    	// Rest-post http-client 4.1.3
    	Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_http-client-4.1.3.json")), Library.class);
    	this.libRepository.customSave(lib);
    	/*MockHttpServletRequestBuilder post_builder = post("/libs/")
    			.content(JacksonUtil.asJsonString(lib).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.sha1", is("16CF5A6B78951F50713D29BFAE3230A611DC01F0")));*/
    	
    	//Rest-post bug 
    	final Bug bug = (Bug)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/bug_2015-5262.json")), Bug.class);
    	this.bugRepository.customSave(bug,true);
    	// Rest-post
    	/*post_builder = post("/bugs/")
    			.content(JacksonUtil.asJsonString(bug).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.bugId", is("CVE-2015-5262")));*/
    	
    	//Rest-post app using http-client
    	final Application app = new Application(APP_GROUP, APP_ARTIFACT, "0.0." + APP_VERSION);

		//Dependencies
		final Set<Dependency> app_dependency = new HashSet<Dependency>(); 
		app_dependency.add(new Dependency(app,lib, Scope.COMPILE, false, "httpclient-4.1.3.jar"));
		app.setSpace(spaceRepository.getDefaultSpace(null));
		app.setDependencies(app_dependency);
    	this.appRepository.customSave(app);
    	
    	// Repo must not contain vulnerableDependencies
    	final StopWatch sw = new StopWatch("Query vulnerable dependencies " + app).start();
   // 	Application app_1 = ApplicationRepository.FILTER.findOne(appRepository.findByGAV(APP_GROUP, APP_ARTIFACT,APP_VERSION));
    	TreeSet<VulnerableDependency> vd = this.appRepository.findJPQLVulnerableDependenciesByGAV(app.getMvnGroup(), app.getArtifact(), app.getVersion(), app.getSpace());
    //	List<VulnerableDependency> vd = this.appRepository.findVulnerableDependenciesByApp(app_1);
    	sw.stop();
    	System.out.println("====================================");
    	System.out.println("Vulnerable Dependency list size: "+vd.size());
       	System.out.println("====================================");
    	assertEquals(0, vd.size());
    }
    
    /**
     * Tests application lastVulnChange update when bug construct changes are saved 
     * @return
     */
    @Test
    public void testRefreshAppsByCC() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	Library managed_lib = this.libRepository.customSave(lib);
    	final Application app = this.createExampleApplication();
    	Application managed_app = this.appRepository.customSave(app);    	
    	
    	Calendar originalLastVulnChange = managed_app.getLastVulnChange();
    	Calendar originalModifiedAt = managed_app.getModifiedAt();
    	Calendar originalCreatedAt = managed_app.getCreatedAt();
    	assertTrue(originalModifiedAt.getTimeInMillis()==originalCreatedAt.getTimeInMillis());
    	    	
    	//Get the application by CC
    	List<Application> appFromJPQL = this.appRepository.findAppsByCC((List<ConstructId>)managed_lib.getConstructs());
    	assertTrue(appFromJPQL.contains(managed_app));
    	
    	//create Construct change for the already existing construct
    	ConstructId cid =null;
    	for(ConstructId c: managed_lib.getConstructs()){
    		cid = c;
    	}
    	final ConstructChange cc1 = new ConstructChange("svn.apache.org", "123456", "/trunk/src/main/java/com/acme/Bar.java", cid, Calendar.getInstance(), ConstructChangeType.MOD);
    	List<ConstructChange> listOfConstructChanges = new ArrayList<ConstructChange>();
    	listOfConstructChanges.add(cc1);
    	
    	this.appRepository.refreshVulnChangebyChangeList(listOfConstructChanges);
    	
    	managed_app = this.appRepository.findOne(managed_app.getId());
    	System.out.println("Modified at before update is [" + originalLastVulnChange.getTimeInMillis() + "], after update is [" + managed_app.getLastVulnChange().getTimeInMillis() + "]");
    	assertTrue(managed_app.getLastVulnChange().getTimeInMillis()>originalLastVulnChange.getTimeInMillis());
    	assertTrue(managed_app.getModifiedAt().getTimeInMillis()>originalModifiedAt.getTimeInMillis());
    	assertTrue(managed_app.getCreatedAt().getTimeInMillis()==originalCreatedAt.getTimeInMillis());
    	
    }
    
    /**
     * Tests application lastVulnChange update when affected Library is saved 
     * @return
     */
    @Test
    public void testRefreshAppsByAffLib() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	Library managed_lib = this.libRepository.customSave(lib);
    	final Application app = this.createExampleApplication();
    	Application managed_app = this.appRepository.customSave(app);    
    	final Bug bug = this.createBugWithOutCC();
    	Bug managed_bug = this.bugRepository.customSave(bug, false);
    	
    	Calendar originalLastVulnChange = managed_app.getLastVulnChange();
    	Calendar originalModifiedAt = managed_app.getModifiedAt();
    	Calendar originalCreatedAt = managed_app.getCreatedAt();
    	assertTrue(originalModifiedAt.getTimeInMillis()==originalCreatedAt.getTimeInMillis());
    	
    	
    	//Application app = (Application)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/apps-testapp-fileupload-1.2.2.json")), Application.class);
    	AffectedLibrary affLib = (AffectedLibrary)JacksonUtil.asObject(AFF_LIB_JSON, AffectedLibrary.class);
    	AffectedLibrary[] affArray = new AffectedLibrary[1];
    	affArray[0]=affLib;
    	final MockHttpServletRequestBuilder post_builder = post("/bugs/"+bug.getBugId()+"/affectedLibIds?source=MANUAL")
    			.content(JacksonUtil.asJsonString(affArray).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson))
                .andExpect(jsonPath("$[0].libraryId.group", is("com.acme")))
                .andExpect(jsonPath("$[0].affected", is(true)));
    	
    	AffectedLibrary managed_afflib = AffectedLibraryRepository.FILTER.findOne(this.affLibRepository.findByBug(managed_bug));
    	
     	    	
    	//Get the application by AffLib
    	List<Application> appFromJPQL = this.appRepository.findAppsByAffLib(managed_afflib.getLibraryId());
    	assertTrue(appFromJPQL.contains(managed_app));
    	
    	//create Construct change for the already existing construct  	
    	this.appRepository.refreshVulnChangebyAffLib(managed_afflib);
    	
    	managed_app = this.appRepository.findOne(managed_app.getId());
    	System.out.println("Modified at before update is [" + originalLastVulnChange.getTimeInMillis() + "], after update is [" + managed_app.getLastVulnChange().getTimeInMillis() + "]");
    	assertTrue(managed_app.getLastVulnChange().getTimeInMillis()>originalLastVulnChange.getTimeInMillis());
    	assertTrue(managed_app.getModifiedAt().getTimeInMillis()>originalModifiedAt.getTimeInMillis());
    	assertTrue(managed_app.getCreatedAt().getTimeInMillis()==originalCreatedAt.getTimeInMillis());
    	
    }
    
    /**
     * Tests application lastScan update from rest api 
     * @return
     */
    @Test
    public void testRefreshAppsByLastScan() throws Exception {
    	final Library lib = this.createExampleLibrary();
    	this.libRepository.customSave(lib);
    	final Application app = this.createExampleApplication();
    	Application managed_app = this.appRepository.customSave(app);    
    	
    	Calendar originalLastVulnChange = managed_app.getLastVulnChange();
    	Calendar originalLastScan = managed_app.getLastScan();
    	Calendar originalModifiedAt = managed_app.getModifiedAt();
    	Calendar originalCreatedAt = managed_app.getCreatedAt();
    	assertTrue(originalModifiedAt.getTimeInMillis()==originalCreatedAt.getTimeInMillis());
    	
    	final GoalExecution gexe = this.createExampleGoalExecution(app, GoalType.APP);
    	
    	// post
    	final MockHttpServletRequestBuilder post_builder = post(getAppUri(app) + "/goals")
    			.content(JacksonUtil.asJsonString(gexe).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	System.out.println("Gexe: " + JacksonUtil.asJsonString(gexe));
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentTypeJson));
    	
    	Application after_update = ApplicationRepository.FILTER.findOne(this.appRepository.findById(managed_app.getId()));
    	Calendar lastScanAfterPost = managed_app.getLastScan();
    	assertTrue(originalLastScan.getTimeInMillis()<after_update.getLastScan().getTimeInMillis());
    	assertTrue(after_update.getLastScan().getTimeInMillis()==after_update.getLastChange().getTimeInMillis());
    	assertTrue(after_update.getLastScan().getTimeInMillis()>after_update.getLastVulnChange().getTimeInMillis());
    	assertTrue(originalLastVulnChange.getTimeInMillis()==after_update.getLastVulnChange().getTimeInMillis());
    	
    	//re-post
    	final MockHttpServletRequestBuilder put_builder = put(getAppUri(app) + "/goals/"+ gexe.getExecutionId())
    			.content(JacksonUtil.asJsonString(gexe).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(put_builder)	
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentTypeJson));
    	
//    	final MockHttpServletRequestBuilder put_builder = put(getAppUri(managed_app)+"/lastscan");
//    			//.content(JacksonUtil.asJsonString(affArray).getBytes())
//				//.contentType(MediaType.APPLICATION_JSON)
//				//.accept(MediaType.APPLICATION_JSON);
//    	mockMvc.perform(put_builder)	
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentTypeJson)) ;
//              //  .andExpect(jsonPath("$.lastChange", is(String.class)));
    	
    	after_update = ApplicationRepository.FILTER.findOne(this.appRepository.findById(managed_app.getId()));
    	assertTrue(lastScanAfterPost.getTimeInMillis()<after_update.getLastScan().getTimeInMillis());
    	assertTrue(after_update.getLastScan().getTimeInMillis()==after_update.getLastChange().getTimeInMillis());
    	assertTrue(after_update.getLastScan().getTimeInMillis()>after_update.getLastVulnChange().getTimeInMillis());
    	assertTrue(originalLastVulnChange.getTimeInMillis()==after_update.getLastVulnChange().getTimeInMillis());
    }
    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }  
    
    private static final String APP_GROUP = "com.acme";
    private static final String APP_ARTIFACT = "vulas";
    private static int APP_VERSION = 1; // Used to create unique apps
    
	private final Application createExampleApplication() {		
		final Application app = new Application(APP_GROUP, APP_ARTIFACT, "0.0." + APP_VERSION++);
		app.setSpace(spaceRepository.getDefaultSpace(null));
		
		//Dependencies
		final Set<Dependency> app_dependency = new HashSet<Dependency>(); 
		//app_dependency.add(new Dependency(LibraryRepository.FILTER.findOne(libRepository.findBySha1("sha1")), "compile", false, "MAVEN", "common-filename.jar"));
		app_dependency.add(new Dependency(app,new Library("sha1"), Scope.COMPILE, false, "common-filename.jar"));
		app.setDependencies(app_dependency);
		
    	// Constructs
    	final Set<ConstructId> app_constructs = new HashSet<ConstructId>();
    	app_constructs.add(new ConstructId(ProgrammingLanguage.JAVA, ConstructType.CLAS, "com.acme.Vulas.method"));
    	app.setConstructs(app_constructs);    	
     	
    	return app;
	}
	
    /**
     * Creates a transient {@link Library}.
     * @return
     */
    private final Library createExampleLibrary() {
    	final Library lib = new Library("sha1");
    	lib.setLibraryId(new LibraryId("com.acme", "Foo", "1.0.0"));
    	lib.setDigestAlgorithm(DigestAlgorithm.SHA1);
    	
    	// Constructs
    	final Set<ConstructId> lib_constructs = new HashSet<ConstructId>();
    	lib_constructs.add(new ConstructId(ProgrammingLanguage.JAVA, ConstructType.CLAS, "com.acme.Bar"));
    	lib.setConstructs(lib_constructs);
    	lib.setProperties(this.createExampleProperties(PropertySource.JAVA_MANIFEST, "entry", "value"));
    	
    	return lib;
    }
    
    private final GoalExecution createExampleGoalExecution(Application _app, GoalType _goal_type) {
    	final GoalExecution gexe = new GoalExecution(_app, _goal_type, Calendar.getInstance());
    	gexe.setConfiguration(this.createExampleProperties(PropertySource.GOAL_CONFIG, "entry", "value"));
    	gexe.setSystemInfo(this.createExampleProperties(PropertySource.GOAL_CONFIG, "entry", "value"));
    	gexe.setExecutionException("ExampleException");
    	gexe.setMemMax(11111L);
    	gexe.setMemUsedAvg(22222L);
    	gexe.setMemUsedMax(33333L);
    	gexe.setRuntimeNano(44444L);
    	gexe.setExecutionId("ClientID-1234");
    	Map<String, Long> stats = new HashMap<String,Long>();
    	stats.put("abc", 123L);
    	gexe.setStatistics(stats);
    	gexe.setClientVersion("1.1.0-SNAPSHOT");
    	return gexe;
    }
    
    private final Collection<Property> createExampleProperties(PropertySource source, String name, String value) {
    	final Collection<Property> props = new HashSet<Property>();
    	props.add(new Property(source, name, value));
    	return props;
    }
    
    private final Path createExamplePath(Application _app, Bug _bug) {
    	final Path p = new Path(_app, _bug, PathSource.A2C);
    	p.setExecutionId("A2C-1234");

    	// Start and end
    	final ConstructId start = new ConstructId(ProgrammingLanguage.JAVA, ConstructType.METH, "com.acme.Vulas.method()");
    	final ConstructId end   = new ConstructId(ProgrammingLanguage.JAVA, ConstructType.METH, "org.lib.Foo.test()");
    	
    	// Complete path
    	List<PathNode> path = new LinkedList<PathNode>();
    	path.add(new PathNode(start));
    	path.add(new PathNode(new ConstructId(ProgrammingLanguage.JAVA, ConstructType.METH, "org.lib.Foo.bar()")));
    	path.add(new PathNode(end));
    	p.setPath(path);
    	
    	return p;    	
    }
	
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
			default_space.setExportConfiguration(ExportConfiguration.OFF);
			default_space.setSpaceDescription("bar");
			default_space.setSpaceOwners(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
			default_space.setTenant(default_tenant);
			spaceRepository.save(default_space);
			
			default_space = SpaceRepository.FILTER.findOne(spaceRepository.findBySecondaryKey(TEST_DEFAULT_SPACE));
		}
	}
	
	
	private static final String AFF_LIB_JSON = "{\"libraryId\": { \"group\":\"com.acme\",\"artifact\":\"Foo\",\"version\":\"1.0.0\" },\"source\":\"MANUAL\",\"affected\":\"true\" }";

        
    /**
     * Creates a transient bug.
     * @return
     */
    private final Bug createBugWithOutCC() {
    	final Bug b = new Bug("CVE-2014-0050");
    	b.setDescription("MultipartStream.java in Apache Commons FileUpload before 1.3.1, as used in Apache Tomcat, JBoss Web, and other products, allows remote attackers to cause a denial of service (infinite loop and CPU consumption) via a crafted Content-Type header that bypasses a loop&#039;s intended exit conditions.");
    	b.setOrigin(BugOrigin.PUBLIC);
    	b.setMaturity(ContentMaturityLevel.READY);
    	return b;
    }

}
