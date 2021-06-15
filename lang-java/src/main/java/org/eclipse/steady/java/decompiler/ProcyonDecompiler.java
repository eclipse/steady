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
package org.eclipse.steady.java.decompiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.shared.util.FileUtil;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

/**
 * <p>ProcyonDecompiler class.</p>
 */
public class ProcyonDecompiler implements IDecompiler {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /** {@inheritDoc} */
  @Override
  public File decompileClassFile(File inputClassFile) {

    // Default settings for the decompilers
    final DecompilerSettings settings = new DecompilerSettings();
    settings.setShowSyntheticMembers(true);
    settings.setSimplifyMemberReferences(true);
    settings.setExcludeNestedTypes(true);

    String classFilePath = inputClassFile.getPath();
    String fileNameWithOutExt = FilenameUtils.removeExtension(inputClassFile.getName());
    File outFile = new File(inputClassFile.getParent(), fileNameWithOutExt + ".java");

    try {

      final FileOutputStream stream = new FileOutputStream(outFile.toString());
      final OutputStreamWriter writer = new OutputStreamWriter(stream, FileUtil.getCharset());

      try {
        Decompiler.decompile(classFilePath, new PlainTextOutput(writer), settings);
      } finally {
        writer.close();
        stream.close();
      }
    } catch (final IOException e) {
      log.debug(e.getMessage());
    }

    return outFile;
  }
}
