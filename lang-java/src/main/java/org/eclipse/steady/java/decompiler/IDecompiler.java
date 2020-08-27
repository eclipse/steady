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
/**
 *
 */
package org.eclipse.steady.java.decompiler;

import java.io.File;

/**
 * <p>IDecompiler interface.</p>
 *
 */
public interface IDecompiler {

  /**
   * <p>decompileClassFile.</p>
   *
   * @param _classFile - the *.class file to be decompiled containing bytecode
   * @return the corresponding decompiled _javaFile
   */
  public File decompileClassFile(File _classFile);
}
