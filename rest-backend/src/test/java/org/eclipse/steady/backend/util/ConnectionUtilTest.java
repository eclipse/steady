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
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.backend.util;

import org.apache.http.client.config.RequestConfig;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConnectionUtilTest {

  @Test
  public void testGetProxy() {

    String host = System.getProperty("http.proxyHost");
    String port = System.getProperty("http.proxyPort");

    RequestConfig g = ConnectionUtil.getProxyConfig();

    System.setProperty("http.proxyHost", "proxy");
    System.setProperty("http.proxyPort", "80");
    g = ConnectionUtil.getProxyConfig();
    assertEquals(g.getProxy().toString(), "http://proxy:80");

    System.setProperty("http.proxyPort", "");
    g = ConnectionUtil.getProxyConfig();
    assertEquals(g, null);
    // System.out.println(g.getProxy());

    if (host != null) System.setProperty("http.proxyHost", host);
    if (port != null) System.setProperty("http.proxyPort", port);
  }
}
