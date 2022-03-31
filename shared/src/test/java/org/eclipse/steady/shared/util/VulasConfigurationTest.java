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
package org.eclipse.steady.shared.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.steady.shared.connectivity.Service;
import org.eclipse.steady.shared.json.model.KeyValue;
import org.junit.Test;

public class VulasConfigurationTest {

  @Test
  public void testConfigurationChange() {

    // Should be empty by default (see steady-shared.properties)
    assertTrue(VulasConfiguration.getGlobal().isEmpty(VulasConfiguration.TMP_DIR));

    // Should not result in an update, since this class is not part of a JAR
    boolean added =
        VulasConfiguration.getGlobal().appendConfigurationsFromJar(VulasConfigurationTest.class);
    assertFalse(added);

    // Should not result in an update, since the JAR from the class was loaded does not contain
    // Vulas configuration files
    added = VulasConfiguration.getGlobal().appendConfigurationsFromJar(Configuration.class);
    assertFalse(added);

    // Get the mutable configuration object and change its value
    final Configuration cfg_1 = VulasConfiguration.getGlobal().getConfiguration();
    assertTrue(VulasConfiguration.getGlobal().isEmpty(VulasConfiguration.TMP_DIR));
    // cfg_1.setProperty(VulasConfiguration.TMP_DIR, "test");
    VulasConfiguration.getGlobal().setProperty(VulasConfiguration.TMP_DIR, "test", "-1", false);
    assertFalse(VulasConfiguration.getGlobal().isEmpty(VulasConfiguration.TMP_DIR));

    // Get the mutable object another time and check it has been changed
    final Configuration cfg_2 = VulasConfiguration.getGlobal().getConfiguration();
    assertFalse(VulasConfiguration.getGlobal().isEmpty(VulasConfiguration.TMP_DIR));
    assertEquals("test", cfg_2.getString(VulasConfiguration.TMP_DIR));
    assertEquals(
        cfg_1.getString(VulasConfiguration.TMP_DIR), cfg_2.getString(VulasConfiguration.TMP_DIR));

    // Squeeze and rebuild
    final Map<Integer, String> map1 = new HashMap<Integer, String>();
    map1.put(1, "test-value");
    map1.put(2, "-1");
    VulasConfiguration.getGlobal().addLayerAfterSysProps("foo", map1, "-1", false);
    assertEquals("test-value", VulasConfiguration.getGlobal().getConfiguration().getString("1"));
    assertTrue(VulasConfiguration.getGlobal().isEmpty("2")); // Must not have been added

    // Squeeze and rebuild
    final Map<String, String> map2 = new HashMap<String, String>();
    map2.put("vulas.test-key-1", "test-value");
    map2.put("vulas.test-key-2", null);
    VulasConfiguration.getGlobal().addLayerAfterSysProps("foo", map2, null, true);
    assertEquals(
        "test-value",
        VulasConfiguration.getGlobal().getConfiguration().getString("vulas.test-key-1"));
  }

  @Test
  public void testStringArray() {
    final String[] default_array = new String[] {"foo"};

    // Test with a key that does not exist
    String[] a1 = VulasConfiguration.getGlobal().getStringArray("does-not-exist", default_array);
    assertTrue(default_array.equals(a1));

    // Test with a key that contains one value
    VulasConfiguration.getGlobal().setProperty("single", "bar");
    String[] a2 = VulasConfiguration.getGlobal().getStringArray("single", default_array);
    assertFalse(default_array.equals(a2));

    // Test with a key that contains multiple values
    VulasConfiguration.getGlobal().setProperty("multiple", "foo , bar");
    String[] a3 = VulasConfiguration.getGlobal().getStringArray("multiple", default_array);
    assertFalse(default_array.equals(a3));
  }

  @Test
  public void testNestedJars() {
    final Path p = Paths.get("src", "test", "resources", "Outer.jar");
    VulasConfiguration.getGlobal().getConfiguration();
    VulasConfiguration.getGlobal().appendConfigurationsFromJarPath(p.toString());
    VulasConfiguration.getGlobal().log(new String[] {"vulas"}, "    ");
    assertEquals(
        "123", VulasConfiguration.getGlobal().getConfiguration().getString("vulas.test-key-3"));
    assertEquals(
        "abc", VulasConfiguration.getGlobal().getConfiguration().getString("vulas.test-key-4"));
  }

  @Test
  public void testToKeyValue() {
    KeyValue[] values =
        KeyValue.toKeyValue(VulasConfiguration.getGlobal().getConfiguration().subset("vulas"));
    assertTrue(values.length > 0);
    values = KeyValue.toKeyValue(VulasConfiguration.getGlobal().getConfiguration());
    assertTrue(values.length > 0);
  }

  @Test
  public void testIndependentConfigChanges() {
    final VulasConfiguration c1 = new VulasConfiguration();
    c1.setProperty("foo", "bar");

    final VulasConfiguration c2 = new VulasConfiguration();
    c2.setProperty("foo", "baz");

    assertTrue(
        !c1.getConfiguration().getString("foo").equals(c2.getConfiguration().getString("foo")));
  }

  @Test
  public void testCheckSettings() {
    final VulasConfiguration c1 = new VulasConfiguration();

    // Add a setting as mandatory
    c1.setProperty(VulasConfiguration.MAND_SETTINGS, "bar");

    // And check that an exception is thrown
    Exception ce = null;
    try {
      c1.checkSettings();
    } catch (ConfigurationException e) {
      ce = e;
    }
    assertFalse(ce == null);
    assertEquals(
        "The following mandatory settings are not specified: [bar], the following settings do not"
            + " comply with the required format: []",
        ce.getMessage());
  }

  @Test
  public void testCheckSettingsFormatWrong() {
    final VulasConfiguration c1 = new VulasConfiguration();

    // Add a setting as mandatory
    c1.setProperty("vulas.bar", "1234, abcd, 56789");
    c1.setProperty("vulas.bar.format", "\\d{4}");

    // And check that an exception is thrown
    Exception ce = null;
    try {
      c1.checkSettings();
    } catch (ConfigurationException e) {
      ce = e;
    }
    assertFalse(ce == null);
    assertEquals(
        "The following mandatory settings are not specified: [], the following settings do not"
            + " comply with the required format: [vulas.bar]",
        ce.getMessage());
  }

  @Test
  public void testCheckSettingsFormatOk() {
    final VulasConfiguration c1 = new VulasConfiguration();

    // Add a setting as mandatory
    c1.setProperty("vulas.bar", "1234, 5678");
    c1.setProperty("vulas.bar.format", "\\d{4}");

    // And check that an exception is thrown
    Exception ce = null;
    try {
      c1.checkSettings();
    } catch (ConfigurationException e) {
      ce = e;
    }
    assertTrue(ce == null);
  }

  @Test
  public void testEscapedUrl() {
    final VulasConfiguration c1 = new VulasConfiguration();
    final Properties props = new Properties();
    try {
      props.load(new FileInputStream(new File("./src/test/resources/steady-test.properties")));
      assertEquals("https://foo.com/bar", props.getProperty("vulas.bar"));
    } catch (IOException e) {
      assertTrue(false);
    }
  }

  @Test
  public void testGetServiceUrl() {
    final String url = "http://localhost:8000/backend";
    VulasConfiguration c1 = new VulasConfiguration();
    c1.setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), url + "////");
    assertEquals(url, c1.getServiceUrl(Service.BACKEND));
    c1.setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), url + "/");
    assertEquals(url, c1.getServiceUrl(Service.BACKEND));
    c1.setProperty(VulasConfiguration.getServiceUrlKey(Service.BACKEND), url);
    assertEquals(url, c1.getServiceUrl(Service.BACKEND));
  }

  @Test
  public void testGetServiceHeaders() {
    final String url = "http://localhost:8000/backend";
    VulasConfiguration c1 = new VulasConfiguration();
    c1.setProperty("vulas.shared.backend.header.foo", "bar");
    c1.setProperty("vulas.shared.backend.header.baz", 1);
    c1.setProperty("vulas.shared.backend.header.X-Vulas-Client-Token", "AJDEY@HEX@EWX@XEH@I*QA");
    // c1.setProperty("vulas.shared.backend.header.test", "123, 456");
    final Map<String, String> headers = c1.getServiceHeaders(Service.BACKEND);
    assertEquals("bar", headers.get("foo"));
    assertEquals("1", headers.get("baz"));
    assertEquals("AJDEY@HEX@EWX@XEH@I*QA", headers.get("X-Vulas-Client-Token"));
    // assertEquals("123, 456", headers.get("test"));
  }

  @Test
  public void testSanitize() {
    VulasConfiguration c1 = new VulasConfiguration();
    Configuration pc = new PropertiesConfiguration();
    pc.setProperty("abc.123", "foo");
    pc.setProperty("!@&^!@", "bar");
    c1.sanitize(pc);
    assertTrue(!pc.containsKey("!@&^!@"));
  }
}
