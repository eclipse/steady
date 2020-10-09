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
package org.eclipse.steady.java;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class PomParserTest {

  @Test
  public void testParsePom() {
    try {
      final PomParser pp = new PomParser();
      final SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setNamespaceAware(true);
      final SAXParser saxParser = spf.newSAXParser();
      final XMLReader xmlReader = saxParser.getXMLReader();
      xmlReader.setContentHandler(pp);
      xmlReader.parse(new InputSource(new FileReader(new File("pom.xml"))));
      assertEquals("org.eclipse.steady", pp.getLibraryId().getMvnGroup());
      assertEquals("lang-java", pp.getLibraryId().getArtifact());
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
}
