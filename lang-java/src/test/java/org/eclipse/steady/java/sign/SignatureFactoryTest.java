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
package org.eclipse.steady.java.sign;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.steady.java.sign.ASTConstructBodySignature;
import org.eclipse.steady.java.sign.JavaSignatureFactory;
import org.eclipse.steady.shared.enums.ConstructType;
import org.eclipse.steady.shared.enums.ProgrammingLanguage;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.sign.SignatureFactory;
import org.junit.Test;

public class SignatureFactoryTest {

  @Test
  public void createSignature() throws Exception {
    final SignatureFactory f = new JavaSignatureFactory();
    final ASTConstructBodySignature s =
        (ASTConstructBodySignature)
            f.createSignature(
                new ConstructId(
                    ProgrammingLanguage.JAVA,
                    ConstructType.METH,
                    "org.foo.Filter.doFilter(Boolean,String,String)"),
                new File("./src/test/resources/Filter.java"));
    assertTrue(s.toJson().length() > 0);
  }
}
