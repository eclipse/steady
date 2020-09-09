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
package org.eclipse.steady.kb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

public class ZipUtil {
  public static void unzip(String zipPath, String destPathToUnzip) {
    try (java.util.zip.ZipFile zipFile = new ZipFile(zipPath)) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        File entryDestination = new File(destPathToUnzip, entry.getName());
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try (InputStream in = zipFile.getInputStream(entry);
              OutputStream out = new FileOutputStream(entryDestination)) {
            IOUtils.copy(in, out);
          }
        }
      }
    } catch (IOException e) {
      System.out.println("unzip failed " + e.getMessage());
    }
  }
}
