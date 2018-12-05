package com.sap.psr.vulas.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.function.Predicate;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.repo.LibraryRepository;
import com.sap.psr.vulas.shared.categories.RequiresNetwork;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.util.FileUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainController.class,webEnvironment= SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
public class LibraryControllerTest {

	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;
    private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

    @Autowired
    private LibraryRepository libRepository;

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
        this.libRepository.deleteAll();
    }
    
    /**
     * 2x rest-post and rest-get (using different versions of Apache Commons-Fileupload)
     * @throws Exception
     */
    @Test
    @Category(RequiresNetwork.class)
    public void testPostJinja2() throws Exception {
    	// Rest-post 1.2.2
    	Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_Jinja2-2.9.6.json")), Library.class);
    	MockHttpServletRequestBuilder post_builder = post("/libs/")
    			.content(JacksonUtil.asJsonString(lib).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.wellknownDigest", is(true)))
                .andExpect(jsonPath("$.digest", is("61A215BCDB0F7939C70582BC00B293F1")));
    	

    	// Repo must contain 1
    	assertEquals(1, this.libRepository.count());
    }
    
    /**
     * 2x rest-post and rest-get (using different versions of Apache Commons-Fileupload)
     * @throws Exception
     */
    @Test
    @Category(RequiresNetwork.class)
    public void testPostCommonsFileUpload() throws Exception {
    	// Rest-post 1.2.2
    	Library lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_commons-fileupload-1.2.2.json")), Library.class);
    	MockHttpServletRequestBuilder post_builder = post("/libs/")
    			.content(JacksonUtil.asJsonString(lib).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.wellknownDigest", is(true)))
                .andExpect(jsonPath("$.digest", is("1E48256A2341047E7D729217ADEEC8217F6E3A1A")));
    	

    	// Repo must contain 1
    	assertEquals(1, this.libRepository.count());
    	

    	lib = (Library)JacksonUtil.asObject(FileUtil.readFile(Paths.get("./src/test/resources/real_examples/lib_commons-fileupload-1.3.1.json")), Library.class);
    	post_builder = post("/libs/")
    			.content(JacksonUtil.asJsonString(lib).getBytes())
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON);
    	mockMvc.perform(post_builder)	
                .andExpect(status().isCreated())
                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.digest", is("C621B54583719AC0310404463D6D99DB27E1052C")));
    	

    	assertEquals(2, this.libRepository.count());
    	
    	//check that modifiedAt does not get updated on GET
    	Library managed_lib = LibraryRepository.FILTER.findOne(libRepository.findByDigest("1E48256A2341047E7D729217ADEEC8217F6E3A1A"));
    	Calendar modifiedAt = managed_lib.getModifiedAt();
    	managed_lib = LibraryRepository.FILTER.findOne(libRepository.findByDigest("1E48256A2341047E7D729217ADEEC8217F6E3A1A"));
    	assertTrue(modifiedAt.getTimeInMillis()==managed_lib.getModifiedAt().getTimeInMillis());
    }
}
