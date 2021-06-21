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
package org.eclipse.steady.backend.util;

import org.springframework.stereotype.Component;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

// Component duplicated in
// rest-lib-utils/src/main/java/com/sap/psr/vulas/backend/util/CacheFilter.java

/**
 * <p>CacheFilter class.</p>
 */
@Component
public class CacheFilter implements Filter {

  /**
   * {@inheritDoc}
   *
   * Default destroy
   */
  @Override
  public void destroy() {
    // Nothing
  }

  /**
   * {@inheritDoc}
   *
   * Appends to the response a X-Accel-Expires header equal to two hours if cache=true is present in the querystring of the request
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String cache = request.getParameter("cache");
    if (cache != null && cache.equals("true")) {
      // Instructs Nginx to cache the response for 2 hours
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      httpServletResponse.setHeader("X-Accel-Expires", "7200");
    }
    chain.doFilter(request, response);
  }

  /**
   * {@inheritDoc}
   *
   * Default init
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing
  }
}
