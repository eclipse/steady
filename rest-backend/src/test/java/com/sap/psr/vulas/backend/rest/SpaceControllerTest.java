package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

import javax.persistence.EntityNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.sap.psr.vulas.backend.model.Space;
import com.sap.psr.vulas.backend.model.Tenant;
import com.sap.psr.vulas.backend.repo.SpaceRepository;
import com.sap.psr.vulas.backend.repo.TenantRepository;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.Constants;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class,webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SpaceControllerTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(),
			Charset.forName("utf8"));

	private MockMvc mockMvc;
	private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;
	
	public static final String TEST_DEFAULT_SPACE = "public";
    public static final String TEST_DEFAULT_TENANT = "default";

	@Autowired
	private SpaceRepository spaceRepository;

	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private WebApplicationContext webApplicationContext;

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
	}

	

	/**
	 * Check space creation.
	 * @throws Exception
	 */
	@Test
	public void testSpaceCreation() throws Exception {

		Tenant t = createDefaultTenant();
		
		createDefaultTenantandSpace();
		
		// Create space
		final String space_name = "foo";
		com.sap.psr.vulas.shared.json.model.Space new_shared_space = new com.sap.psr.vulas.shared.json.model.Space();
		
		
		//default space
		new_shared_space.setSpaceName(TEST_DEFAULT_SPACE);
		new_shared_space.setDefault(true);
		new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
		new_shared_space.setSpaceDescription("bar");
		new_shared_space.setOwnerEmails(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
		
		
		// Post w/o tenant header, returns 400
		MockHttpServletRequestBuilder post_builder = post("/spaces")
				.content(JacksonUtil.asJsonString(new_shared_space))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(post_builder).andExpect(status().isBadRequest());
//		MvcResult result = mockMvc.perform(post_builder).andExpect(status().isCreated())
//				.andExpect(content().contentType(contentType))
//				.andExpect(jsonPath("$.spaceName", is(space_name))).andReturn();
		

		// Post w/ tenant header to try to recreate default space
		post_builder = post("/spaces")
				.header(Constants.HTTP_TENANT_HEADER, t.getTenantToken())
				.content(JacksonUtil.asJsonString(new_shared_space))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(post_builder).andExpect(status().isBadRequest());

		//try to create another default space
		new_shared_space.setSpaceName(space_name);
		new_shared_space.setDefault(false);
		
		post_builder = post("/spaces")
				.header(Constants.HTTP_TENANT_HEADER,  t.getTenantToken())
				.content(JacksonUtil.asJsonString(new_shared_space))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(post_builder).andExpect(status().isCreated())
			.andExpect(content().contentType(contentType))
			.andExpect(jsonPath("$.spaceName", is(space_name)));
		
		
		// Post with non-existing tenant token, defaults in controller method
		MockHttpServletRequestBuilder post_builder2 = post("/spaces")
				.content(JacksonUtil.asJsonString(new_shared_space))
				.contentType(MediaType.APPLICATION_JSON)
				.header(Constants.HTTP_TENANT_HEADER, "does-not-exist")
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(post_builder2).andExpect(status().isNotFound());
	}
	
	/**
	 * Check presence of spaces.
	 * @throws Exception
	 */
	@Test
	public void testGetSpace() throws Exception {

		createDefaultTenantandSpace();
		// Default tenant exists
		MockHttpServletRequestBuilder opts_builder = options("/spaces/" + TEST_DEFAULT_SPACE)
				.header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT);
		mockMvc.perform(opts_builder).andExpect(status().isOk());

		// Default tenant members
		MockHttpServletRequestBuilder get_builder = get("/spaces/" + TEST_DEFAULT_SPACE)
				.header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(get_builder).andExpect(status().isOk())
		.andExpect(content().contentType(contentType))
		.andExpect(jsonPath("$.spaceToken", is(TEST_DEFAULT_SPACE)))
		.andExpect(jsonPath("$.spaceName", is(TEST_DEFAULT_SPACE)));
	}
	
	/**
	 * Check space creation and deletion.
	 * @throws Exception
	 */
	@Test
	public void testSpaceCreationAndDeletion() throws Exception {

		
		final String space_name = "foo";
		com.sap.psr.vulas.shared.json.model.Space new_shared_space = new com.sap.psr.vulas.shared.json.model.Space();
		new_shared_space.setSpaceName("foo");
		new_shared_space.setExportConfiguration(ExportConfiguration.OFF);
		new_shared_space.setSpaceDescription("bar");
		new_shared_space.setOwnerEmails(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
		
		// Post w tenant header (should create a NON default space as it's not provided)
		Tenant t = createDefaultTenant();
		MockHttpServletRequestBuilder post_builder = post("/spaces")
				.content(JacksonUtil.asJsonString(new_shared_space))
				.contentType(MediaType.APPLICATION_JSON)
				.header(Constants.HTTP_TENANT_HEADER, t.getTenantToken())
				.accept(MediaType.APPLICATION_JSON);
		MvcResult result = mockMvc.perform(post_builder).andExpect(status().isCreated())
				.andExpect(content().contentType(contentType))
				.andExpect(jsonPath("$.default", is(false)))
				.andExpect(jsonPath("$.spaceName", is(space_name))).andReturn();
		
		// Get the generated token
		com.sap.psr.vulas.shared.json.model.Space created_shared_space = (com.sap.psr.vulas.shared.json.model.Space)JacksonUtil.asObject(result.getResponse().getContentAsString(), com.sap.psr.vulas.shared.json.model.Space.class);

		// Get the number of spaces
		final int before = this.spaceRepository.findAllTenantSpaces(TEST_DEFAULT_TENANT).size();
		
		assertEquals(3, before);
		
		// Post with non-existing tenant token, defaults in controller method
		MockHttpServletRequestBuilder del_builder = delete("/spaces/" + created_shared_space.getSpaceToken())
				.header(Constants.HTTP_TENANT_HEADER, TEST_DEFAULT_TENANT)
				.accept(MediaType.APPLICATION_JSON);
		mockMvc.perform(del_builder).andExpect(status().isOk());
		
		final int after = this.spaceRepository.findAllTenantSpaces(TEST_DEFAULT_TENANT).size();
		
		//assertEquals(before-1, after);
	}
	
	
	public Tenant createDefaultTenant() {
		
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
		return default_tenant;
	}

	public void createDefaultTenantandSpace() {
		
	
		Tenant t = createDefaultTenant();
		
		Space default_space = null;
		
		try{
			SpaceRepository.FILTER.findOne(spaceRepository.findBySecondaryKey(TEST_DEFAULT_SPACE));
		
		}catch(EntityNotFoundException e){
			//default space
			default_space = new Space();
			default_space.setSpaceName(TEST_DEFAULT_SPACE);
			default_space.setSpaceToken(TEST_DEFAULT_SPACE);
			default_space.setDefault(true);
			default_space.setExportConfiguration(ExportConfiguration.OFF);
			default_space.setSpaceDescription("bar");
			default_space.setSpaceOwners(new HashSet<String>(Arrays.asList(new String[] {"foo@bar.com"})));
			default_space.setTenant(t);
			spaceRepository.save(default_space);
		}
	}
	
}
