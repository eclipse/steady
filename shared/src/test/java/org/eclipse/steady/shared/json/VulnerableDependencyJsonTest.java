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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eclipse.steady.shared.json;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.ConstructChangeInDependency;
import org.eclipse.steady.shared.json.model.VulnerableDependency;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class VulnerableDependencyJsonTest {
  VulnerableDependency inputNew;

  @Test
  public void testVulnerableDependecyDeserialization() {

    final Logger log = org.apache.logging.log4j.LogManager.getLogger();
    String vulndepstring = this.getFile("vulndepJsonExpected.json");
    inputNew =
        (VulnerableDependency) JacksonUtil.asObject(vulndepstring, VulnerableDependency.class);
    Assert.assertNotEquals(inputNew, null);
    Assert.assertEquals("CVE-2011-1498", this.inputNew.getBug().getBugId());
    Assert.assertEquals(
        "894B77B74AC06206075BCCC22868EF83D69E383B", inputNew.getDep().getLib().getDigest());
    List<ConstructChangeInDependency> constructList = inputNew.getConstructList();
    Assert.assertEquals(
        constructList.get(0).getConstructChange().getBuggyBody(),
        "{\"ast\":[ {\"Value\" : \"process\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 19,\"End\" : 1754}},\"EntityType\" :"
            + " \"METHOD\",\"C\" : [{\"Value\" : \"(request == null)\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 144,\"End\" :"
            + " 257}},\"EntityType\" : \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"(request =="
            + " null)\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\""
            + " : 165,\"End\" : 257}},\"EntityType\" : \"THEN_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"new IllegalArgumentException(\"HTTP request may not be"
            + " null\");\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 180,\"End\" : 246}},\"EntityType\" :"
            + " \"THROW_STATEMENT\"}]}]},{\"Value\" : \"(context == null)\",\"SourceCodeEntity\""
            + " :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 268,\"End\" :"
            + " 381}},\"EntityType\" : \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"(context =="
            + " null)\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\""
            + " : 289,\"End\" : 381}},\"EntityType\" : \"THEN_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"new IllegalArgumentException(\"HTTP context may not be"
            + " null\");\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 304,\"End\" : 370}},\"EntityType\" :"
            + " \"THROW_STATEMENT\"}]}]},{\"Value\" :"
            + " \"request.containsHeader(AUTH.PROXY_AUTH_RESP)\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 394,\"End\" :"
            + " 476}},\"EntityType\" : \"IF_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"request.containsHeader(AUTH.PROXY_AUTH_RESP)\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 444,\"End\" :"
            + " 476}},\"EntityType\" : \"THEN_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
            + " 459,\"End\" : 465}},\"EntityType\" : \"RETURN_STATEMENT\"}]}]},{\"Value\" :"
            + " \"AuthState authState = (AuthState)"
            + " context.getAttribute(ClientContext.PROXY_AUTH_STATE);\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 529,\"End\" :"
            + " 633}},\"EntityType\" : \"VARIABLE_DECLARATION_STATEMENT\"},{\"Value\" :"
            + " \"(authState == null)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 644,\"End\" : 699}},\"EntityType\" :"
            + " \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"(authState =="
            + " null)\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\""
            + " : 667,\"End\" : 699}},\"EntityType\" : \"THEN_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
            + " 682,\"End\" : 688}},\"EntityType\" : \"RETURN_STATEMENT\"}]}]},{\"Value\" :"
            + " \"AuthScheme authScheme = authState.getAuthScheme();\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 712,\"End\" :"
            + " 761}},\"EntityType\" : \"VARIABLE_DECLARATION_STATEMENT\"},{\"Value\" :"
            + " \"(authScheme == null)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 772,\"End\" : 828}},\"EntityType\" :"
            + " \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"(authScheme =="
            + " null)\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\""
            + " : 796,\"End\" : 828}},\"EntityType\" : \"THEN_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
            + " 811,\"End\" : 817}},\"EntityType\" : \"RETURN_STATEMENT\"}]}]},{\"Value\" :"
            + " \"Credentials creds = authState.getCredentials();\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 841,\"End\" :"
            + " 887}},\"EntityType\" : \"VARIABLE_DECLARATION_STATEMENT\"},{\"Value\" : \"(creds"
            + " == null)\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 898,\"End\" : 1012}},\"EntityType\" : \"IF_STATEMENT\",\"C\" :"
            + " [{\"Value\" : \"(creds == null)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 917,\"End\" : 1012}},\"EntityType\" :"
            + " \"THEN_STATEMENT\",\"C\" : [{\"Value\" : \"this.log.debug(\"User credentials not"
            + " available\");\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 932,\"End\" : 980}},\"EntityType\" :"
            + " \"METHOD_INVOCATION\"},{\"Value\" : \"\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 995,\"End\" : 1001}},\"EntityType\" :"
            + " \"RETURN_STATEMENT\"}]}]},{\"Value\" : \"((authState.getAuthScope() != null) || (!"
            + " authScheme.isConnectionBased()))\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1023,\"End\" : 1747}},\"EntityType\" :"
            + " \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"((authState.getAuthScope() != null) || (!"
            + " authScheme.isConnectionBased()))\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1096,\"End\" : 1747}},\"EntityType\" :"
            + " \"THEN_STATEMENT\",\"C\" : [{\"Value\" : \"\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 1111,\"End\" :"
            + " 1736}},\"EntityType\" : \"TRY_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" :"
            + " 1115,\"End\" : 1528}},\"EntityType\" : \"BODY\",\"C\" : [{\"Value\" : \"Header"
            + " header;\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 1134,\"End\" : 1147}},\"EntityType\" :"
            + " \"VARIABLE_DECLARATION_STATEMENT\"},{\"Value\" : \"(authScheme instanceof"
            + " ContextAwareAuthScheme)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1166,\"End\" : 1469}},\"EntityType\" :"
            + " \"IF_STATEMENT\",\"C\" : [{\"Value\" : \"(authScheme instanceof"
            + " ContextAwareAuthScheme)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1216,\"End\" : 1372}},\"EntityType\" :"
            + " \"THEN_STATEMENT\",\"C\" : [{\"Value\" : \"header = ((ContextAwareAuthScheme)"
            + " authScheme).authenticate(creds, request, context);\",\"SourceCodeEntity\" :{"
            + " \"Modifiers\" : \"0\",\"SourceRange\" : {\"Start\" : 1239,\"End\" :"
            + " 1353}},\"EntityType\" : \"ASSIGNMENT\"}]},{\"Value\" : \"(authScheme instanceof"
            + " ContextAwareAuthScheme)\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1379,\"End\" : 1469}},\"EntityType\" :"
            + " \"ELSE_STATEMENT\",\"C\" : [{\"Value\" : \"header = authScheme.authenticate(creds,"
            + " request);\",\"SourceCodeEntity\" :{ \"Modifiers\" : \"0\",\"SourceRange\" :"
            + " {\"Start\" : 1402,\"End\" : 1450}},\"EntityType\" : \"ASSIGNMENT\"}]}]},{\"Value\""
            + " : \"request.addHeader(header);\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1488,\"End\" : 1513}},\"EntityType\" :"
            + " \"METHOD_INVOCATION\"}]},{\"Value\" : \"\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1529,\"End\" : 1736}},\"EntityType\" :"
            + " \"CATCH_CLAUSES\",\"C\" : [{\"Value\" :"
            + " \"AuthenticationException\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1530,\"End\" : 1736}},\"EntityType\" :"
            + " \"CATCH_CLAUSE\",\"C\" : [{\"Value\" :"
            + " \"this.log.isErrorEnabled()\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1584,\"End\" : 1721}},\"EntityType\" :"
            + " \"IF_STATEMENT\",\"C\" : [{\"Value\" :"
            + " \"this.log.isErrorEnabled()\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1615,\"End\" : 1721}},\"EntityType\" :"
            + " \"THEN_STATEMENT\",\"C\" : [{\"Value\" : \"this.log.error((\"Proxy authentication"
            + " error: \" + ex.getMessage()));\",\"SourceCodeEntity\" :{ \"Modifiers\" :"
            + " \"0\",\"SourceRange\" : {\"Start\" : 1638,\"End\" : 1702}},\"EntityType\" :"
            + " \"METHOD_INVOCATION\"}]}]}]}]}]}]}]}]}]}");
  }

  private String getFile(String fileName) {

    StringBuilder result = new StringBuilder("");

    // Get file from resources folder
    ClassLoader classLoader = getClass().getClassLoader();
    String filePath = null;
    try {
      filePath = URLDecoder.decode(classLoader.getResource(fileName).getFile(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    File file = new File(filePath);

    try {
      final Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line).append("\n");
      }

      scanner.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

    return result.toString();
  }
}
