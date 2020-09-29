/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.backend.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.steady.backend.cve.Cve;
import org.eclipse.steady.backend.cve.CveReader2;
import org.eclipse.steady.backend.cve.NvdRestServiceMockup;
import org.eclipse.steady.backend.model.AffectedConstructChange;
import org.eclipse.steady.backend.model.AffectedLibrary;
import org.eclipse.steady.backend.model.Bug;
import org.eclipse.steady.backend.model.ConstructChange;
import org.eclipse.steady.backend.model.ConstructChangeType;
import org.eclipse.steady.backend.model.ConstructId;
import org.eclipse.steady.backend.model.Library;
import org.eclipse.steady.backend.model.LibraryId;
import org.eclipse.steady.backend.repo.AffectedLibraryRepository;
import org.eclipse.steady.backend.repo.BugRepository;
import org.eclipse.steady.backend.repo.ConstructChangeRepository;
import org.eclipse.steady.backend.repo.ConstructIdRepository;
import org.eclipse.steady.backend.repo.LibraryIdRepository;
import org.eclipse.steady.backend.repo.LibraryRepository;
import org.eclipse.steady.shared.enums.AffectedVersionSource;
import org.eclipse.steady.shared.enums.BugOrigin;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ContentMaturityLevel;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.util.FileUtil;
import org.hamcrest.core.IsNull;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = MainController.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BugControllerTest {

  private MediaType contentType =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));

  private MockMvc mockMvc;
  private HttpMessageConverter<?> mappingJackson2HttpMessageConverter;

  @Autowired private BugRepository bugRepository;

  @Autowired private ConstructIdRepository cidRepository;

  @Autowired private ConstructChangeRepository ccRepository;

  @Autowired private LibraryRepository libRepository;

  @Autowired private LibraryIdRepository libIdRepository;

  @Autowired private AffectedLibraryRepository afflibRepository;

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter =
        Arrays.asList(converters).stream()
            .filter(
                new Predicate<HttpMessageConverter<?>>() {
                  @Override
                  public boolean test(HttpMessageConverter<?> hmc) {
                    return hmc instanceof MappingJackson2HttpMessageConverter;
                  }
                })
            .findAny()
            .get();

    Assert.assertNotNull(
        "the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
    this.afflibRepository.deleteAll();
    this.bugRepository.deleteAll();
    this.libRepository.deleteAll();
    this.libIdRepository.deleteAll();
  }

  /**
   * Rest-read non-existing bug.
   * @throws Exception
   */
  @Test
  public void testGetNotFound() throws Exception {
    mockMvc.perform(get("/bugs/CVE-xxxx-yyyy")).andExpect(status().isNotFound());
  }

  /**
   * Repo-save and rest-get.
   * @throws Exception
   */
  @Test
  public void testGetBug() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);
    this.bugRepository.customSave(bug, true);
    mockMvc
        .perform(get("/bugs/" + bug.getBugId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());
  }

  /**
   * Save bug and update its description and score.
   * @throws Exception
   */
  @Test
  public void testUpdateCachedCveData() throws Exception {
    final String id = "CVE-2019-17531";

    // Create mockup service and read correct CVE data
    NvdRestServiceMockup.create();
    final CveReader2 reader = new CveReader2();
    final Cve cve = reader.read(id);

    // Create bug without wrong data
    final Bug b1 = this.createExampleBug(id, "Lorem ipsum");
    this.bugRepository.customSave(b1, true);
    assertFalse(cve.getSummary().equals(b1.getDescription()));

    // Update cache with correct data from mockup service
    // this.bugRepository.updateCachedCveData(b1, true);
    mockMvc.perform(post("/cves/refreshCache/" + id)).andExpect(status().isOk());

    // Check whether the descr has been updated
    final Bug b2 = this.bugRepository.findByBugId("CVE-2019-17531").get(0);
    assertTrue(cve.getSummary().equals(b2.getDescription()));
  }

  /**
   * Rest-post and rest-get.
   * @throws Exception
   */
  @Test
  public void testPost() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    // Rest-get
    final MockHttpServletRequestBuilder get_builder = get("/bugs/" + bug.getBugId());
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));
  }

  /**
   * Rest-post and rest-get.
   * @throws Exception
   */
  @Test
  public void testPostCVE20140050() throws Exception {
    final Bug bug =
        (Bug)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get("./src/test/resources/real_examples/bug_CVE-2014-0050.json")),
                Bug.class);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is("CVE-2014-0050")));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    // Rest-get
    final MockHttpServletRequestBuilder get_builder = get("/bugs/" + bug.getBugId());
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is("CVE-2014-0050")));
  }

  /**
   * Duplicate rest-post.
   * @throws Exception
   */
  @Test
  public void testDuplicatePost() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    // Rest-post
    mockMvc.perform(post_builder).andExpect(status().isConflict());

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());
  }

  /**
   * Rest-post and rest-delete.
   * @throws Exception
   */
  @Test
  public void testDelete() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    // Rest-delete
    final MockHttpServletRequestBuilder get_builder = delete("/bugs/" + bug.getBugId());
    mockMvc.perform(get_builder).andExpect(status().isOk());

    // Repo must be empty
    assertEquals(0, this.bugRepository.count());
  }

  /**
   * Rest-post and rest-put.
   * @throws Exception
   */
  @Test
  public void testPostPut() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    // Rest-put
    final MockHttpServletRequestBuilder put_builder =
        put("/bugs/" + bug.getBugId())
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(put_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());
  }

  /**
   * Rest-post, change and rest-put.
   * @throws Exception
   */
  @Test
  public void testPostChangePut() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-post
    final MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());
    assertEquals(2, this.ccRepository.count());

    bug.getConstructChanges().clear();
    final ConstructId cid =
        new ConstructId(ProgrammingLanguage.JAVA, ConstructType.CLAS, "com.acme.Foo");
    final ConstructChange cc3 =
        new ConstructChange(
            "svn.apache.org",
            "123456",
            "/branch/1.x/src/main/java/com/acme/FooTestBar.java",
            cid,
            Calendar.getInstance(),
            ConstructChangeType.MOD);
    bug.getConstructChanges().add(cc3);

    // Rest-put
    final MockHttpServletRequestBuilder put_builder =
        put("/bugs/" + bug.getBugId())
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(put_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());
    assertEquals(1, this.ccRepository.count());
  }

  /**
   * Rest-put non-existing bug.
   * @throws Exception
   */
  @Test
  public void testPutNotFound() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    // Rest-put
    final MockHttpServletRequestBuilder put_builder =
        put("/bugs/" + bug.getBugId())
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc.perform(put_builder).andExpect(status().isNotFound());

    // Repo must contain 1
    assertEquals(0, this.bugRepository.count());
  }

  @Test
  public void testAffectedLibrary() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);

    MockHttpServletRequestBuilder post_builder =
        post("/bugs/")
            .content(JacksonUtil.asJsonString(bug).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bugId", is(BUG_ID)))
        .andExpect(jsonPath("$.reference[0]", is(BUG_URL1)))
        .andExpect(jsonPath("$.reference[1]", is(BUG_URL2)))
        .andExpect(jsonPath("$.description", is(BUG_DESCR)));

    // Repo must contain 1
    assertEquals(1, this.bugRepository.count());

    Library lib =
        (Library)
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get(
                        "./src/test/resources/real_examples/lib_commons-fileupload-1.2.2.json")),
                Library.class);
    post_builder =
        post("/libs/")
            .content(JacksonUtil.asJsonString(lib).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isCreated())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.wellknownDigest", is(true)))
        .andExpect(jsonPath("$.digest", is("1E48256A2341047E7D729217ADEEC8217F6E3A1A")));

    // Repo must contain 1
    assertEquals(1, this.libRepository.count());

    LibraryId lid = new LibraryId("com.foo", "bar", "0.0.1");

    libIdRepository.save(lid);

    ConstructId cid =
        cidRepository.FILTER.findOne(
            cidRepository.findConstructId(
                ProgrammingLanguage.JAVA, ConstructType.CLAS, "com.acme.Foo"));
    ConstructChange cc =
        ccRepository.FILTER.findOne(
            ccRepository.findByRepoPathCommitCidBug(
                "svn.apache.org", "/trunk/src/main/java/com/acme/Foo.java", "123456", cid, bug));

    AffectedLibrary[] afl =
        (AffectedLibrary[])
            JacksonUtil.asObject(
                FileUtil.readFile(
                    Paths.get("./src/test/resources/real_examples/affectedLib-propagate.json")),
                AffectedLibrary[].class);

    AffectedConstructChange acc = new AffectedConstructChange(cc, afl[0], true, true, true, null);
    Collection<AffectedConstructChange> accList = new ArrayList<AffectedConstructChange>();
    accList.add(acc);
    afl[0].setAffectedcc(accList);

    // Post affected libraries
    post_builder =
        post("/bugs/CVE-2014-0050/affectedLibIds?source=PROPAGATE_MANUAL")
            .content(JacksonUtil.asJsonString(afl).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(post_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(2)));

    AffectedLibrary createdAffLib =
        AffectedLibraryRepository.FILTER.findOne(
            this.afflibRepository.findByLibraryId("bar", "bar", "0.0.1"));

    // Put the same affected libraries, no saving backend side
    MockHttpServletRequestBuilder put_builder =
        put("/bugs/CVE-2014-0050/affectedLibIds?source=PROPAGATE_MANUAL")
            .content(JacksonUtil.asJsonString(afl).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(put_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType));

    AffectedLibrary afterPutAffLib =
        AffectedLibraryRepository.FILTER.findOne(
            this.afflibRepository.findByLibraryId("bar", "bar", "0.0.1"));

    assertTrue(
        createdAffLib.getModifiedAt().getTimeInMillis()
            == afterPutAffLib.getModifiedAt().getTimeInMillis());

    // Get affLib by GA
    MockHttpServletRequestBuilder get_builder = get("/bugs/CVE-2014-0050/affectedLibIds/bar/bar");
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(1)));

    // Get affected library with affectedcc
    get_builder = get("/bugs/CVE-2014-0050/affectedLibIds/bar/bar/0.0.1?source=PROPAGATE_MANUAL");
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.[0].affectedcc.length()", is(1)));

    AffectedLibrary afterGetAffLib =
        AffectedLibraryRepository.FILTER.findOne(
            this.afflibRepository.findByLibraryId("bar", "bar", "0.0.1"));

    assertTrue(
        afterGetAffLib.getModifiedAt().getTimeInMillis()
            == afterPutAffLib.getModifiedAt().getTimeInMillis());

    afl[0].setAffected(null);

    // put affected library removing affected cc
    put_builder =
        put("/bugs/CVE-2014-0050/affectedLibIds?source=PROPAGATE_MANUAL")
            .content(JacksonUtil.asJsonString(afl).getBytes())
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);
    mockMvc
        .perform(put_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.[0].affected").value(IsNull.nullValue()));

    AffectedLibrary afterLastPutAffLib =
        AffectedLibraryRepository.FILTER.findOne(
            this.afflibRepository.findByLibraryId("bar", "bar", "0.0.1"));

    assertTrue(
        afterGetAffLib.getModifiedAt().getTimeInMillis()
            < afterLastPutAffLib.getModifiedAt().getTimeInMillis());
  }

  @Test
  public void testGetWellKnownAffectedLibrary() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);
    this.bugRepository.customSave(bug, true);

    LibraryId lid1 = new LibraryId("com.foo", "bar", "1.0");
    libIdRepository.save(lid1);

    LibraryId lid2 = new LibraryId("com.foo", "bar", "1.0-copy");
    libIdRepository.save(lid2);

    Library l1 = new Library("1E48256A2341047E7D729217ADEEC8217F6E3A1A");
    l1.setLibraryId(lid1);
    l1.setWellknownDigest(true);
    libRepository.customSave(l1);

    Library l2 = new Library("123FD");
    l2.setLibraryId(lid2);
    l2.setWellknownDigest(false);
    libRepository.customSave(l2);

    // the libraryId for l1 must have been replaced with the official one (from
    // Maven Central) during the digest verification, thus we create the affected
    // library for the latter
    AffectedLibrary afflib1 =
        new AffectedLibrary(
            bug,
            new LibraryId("commons-fileupload", "commons-fileupload", "1.2.2"),
            true,
            null,
            null,
            null);
    afflib1.setSource(AffectedVersionSource.MANUAL);

    AffectedLibrary afflib2 = new AffectedLibrary(bug, lid2, true, null, null, null);
    afflib2.setSource(AffectedVersionSource.MANUAL);

    AffectedLibrary[] afflibs = new AffectedLibrary[2];
    afflibs[0] = afflib1;
    afflibs[1] = afflib2;
    afflibRepository.customSave(bug, afflibs);

    final MockHttpServletRequestBuilder get_builder =
        get("/bugs/" + bug.getBugId() + "/affectedLibIds?onlyWellKnown=true");
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(1)));

    mockMvc
        .perform(get("/bugs/" + bug.getBugId() + "/affectedLibIds"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(2)));

    mockMvc
        .perform(
            get("/bugs/" + bug.getBugId() + "/affectedLibIds?onlyWellKnown=true&source=MANUAL"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(1)));

    mockMvc
        .perform(get("/bugs/" + bug.getBugId() + "/affectedLibIds?source=MANUAL"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(2)));
  }

  @Test
  public void testGetResolvedAffectedLibrary() throws Exception {
    final Bug bug = this.createExampleBug(BUG_ID, BUG_DESCR);
    this.bugRepository.customSave(bug, true);

    LibraryId lid1 = new LibraryId("com.foo", "bar", "1.0");
    libIdRepository.save(lid1);

    LibraryId lid2 = new LibraryId("com.foo", "bar", "1.0-copy");
    libIdRepository.save(lid2);

    LibraryId lid3 = new LibraryId("com.bar", "foo", "1.1");
    libIdRepository.save(lid3);

    AffectedLibrary afflib1 = new AffectedLibrary(bug, lid1, true, null, null, null);
    afflib1.setSource(AffectedVersionSource.AST_EQUALITY);

    AffectedLibrary afflib2 = new AffectedLibrary(bug, lid2, true, null, null, null);
    afflib2.setSource(AffectedVersionSource.MANUAL);

    AffectedLibrary afflib2_ast = new AffectedLibrary(bug, lid2, false, null, null, null);
    afflib2_ast.setSource(AffectedVersionSource.AST_EQUALITY);

    AffectedLibrary afflib3 = new AffectedLibrary(bug, lid3, null, null, null, null);
    afflib3.setSource(AffectedVersionSource.TO_REVIEW);

    AffectedLibrary[] afflibs = new AffectedLibrary[4];
    afflibs[0] = afflib1;
    afflibs[1] = afflib2;
    afflibs[2] = afflib2_ast;
    afflibs[3] = afflib3;
    afflibRepository.customSave(bug, afflibs);

    final MockHttpServletRequestBuilder get_builder =
        get("/bugs/" + bug.getBugId() + "/affectedLibIds?resolved=true");
    mockMvc
        .perform(get_builder)
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(2)));

    mockMvc
        .perform(get("/bugs/" + bug.getBugId() + "/affectedLibIds"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.length()", is(4)));
  }

  /*@Test
  public void postSingleBug() throws Exception {
  	//https://shdhumale.wordpress.com/2011/07/07/code-to-compress-and-decompress-json-object/
  }*/

  private static final String BUG_JSON =
      "{\"id\":1,\"bugId\":\"CVE-2014-0050\",\"source\":\"NVD\",\"description\":\"MultipartStream.java"
          + " in Apache Commons FileUpload before 1.3.1, as used in Apache"
          + " [...]\",\"url\":\"https://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2014-0050\",\"constructChanges\":[{\"repo\":\"svn.apache.org\",\"commit\":\"123456\",\"path\":\"/branch/1.x/src/main/java/com/acme/Foo.java\",\"constructId\":{\"lang\":\"JAVA\",\"type\":\"CLAS\",\"qname\":\"com.acme.Foo\"},\"committedAt\":\"2016-05-13T14:35:50.274+0000\",\"changeType\":\"MOD\"},{\"repo\":\"svn.apache.org\",\"commit\":\"123456\",\"path\":\"/trunk/src/main/java/com/acme/Foo.java\",\"constructId\":{\"lang\":\"JAVA\",\"type\":\"CLAS\",\"qname\":\"com.acme.Foo\"},\"committedAt\":\"2016-05-13T14:35:50.274+0000\",\"changeType\":\"MOD\"}],\"countConstructChanges\":2}";
  private static final String BUG_ID = "CVE-2014-0050";
  private static final String BUG_URL1 =
      "https://web.nvd.nist.gov/view/vuln/detail?vulnId=CVE-2014-0050";
  private static final String BUG_URL2 = "http://svn.apache.org/r1565143";
  private static final String BUG_DESCR =
      "MultipartStream.java in Apache Commons FileUpload before 1.3.1, as used in Apache Tomcat,"
          + " JBoss Web, and other products, allows remote attackers to cause a denial of service"
          + " (infinite loop and CPU consumption) via a crafted Content-Type header that bypasses"
          + " a loop&#039;s intended exit conditions.";

  /**
   * Creates a transient bug.
   * @param _id TODO
   * @param _descr TODO
   * @return
   */
  private final Bug createExampleBug(String _id, String _descr) {
    final Collection<String> references = new ArrayList<String>();
    references.add(BUG_URL1);
    references.add(BUG_URL2);

    final Bug b = new Bug(_id, _descr, references);
    b.setOrigin(BugOrigin.PUBLIC);
    b.setMaturity(ContentMaturityLevel.READY);
    b.setCvssScore(0.1f);
    b.setCvssVersion("1.9"); // Note: This version does not exist
    b.setCvssVector("bla");

    final ConstructId cid =
        new ConstructId(ProgrammingLanguage.JAVA, ConstructType.CLAS, "com.acme.Foo");

    final Set<ConstructChange> ccs = new HashSet<ConstructChange>();
    final ConstructChange cc1 =
        new ConstructChange(
            "svn.apache.org",
            "123456",
            "/trunk/src/main/java/com/acme/Foo.java",
            cid,
            Calendar.getInstance(),
            ConstructChangeType.MOD);
    final ConstructChange cc2 =
        new ConstructChange(
            "svn.apache.org",
            "123456",
            "/branch/1.x/src/main/java/com/acme/Foo.java",
            cid,
            Calendar.getInstance(),
            ConstructChangeType.MOD);
    cc1.setBug(b);
    cc2.setBug(b);
    ccs.add(cc1);
    ccs.add(cc2);
    b.setConstructChanges(ccs);

    return b;
  }
}
