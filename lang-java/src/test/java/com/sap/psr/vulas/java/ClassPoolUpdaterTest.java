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
package com.sap.psr.vulas.java;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;

import org.junit.Test;

import com.sap.psr.vulas.monitor.ClassPoolUpdater;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

public class ClassPoolUpdaterTest {

  @Test
  public void testClassPoolUpdater() {
    try {
      // Relative path: Should not be appended, since the package structure does not exist in the
      // file path
      final File f = new File("./src/test/resources/classpath/OuterClass$InnerClass.class");
      final CtClass ctclass = ClassPool.getDefault().makeClass(new FileInputStream(f));
      boolean appended1 = ClassPoolUpdater.getInstance().updateClasspath(ctclass, f);
      assertFalse(appended1);

      // Relative path: Should be appended
      final File f2 =
          new File("./src/test/resources/com/sap/psr/vulas/java/test/OuterClass$InnerClass.class");
      final CtClass ctclass2 = ClassPool.getDefault().makeClass(new FileInputStream(f2));
      boolean appended2 = ClassPoolUpdater.getInstance().updateClasspath(ctclass2, f2);
      assertTrue(appended2);

      // Relative path: Should not be appended, since already done
      boolean appended3 = ClassPoolUpdater.getInstance().updateClasspath(ctclass2, f2);
      assertFalse(appended3);

      // Absolute path
      final File f3 =
          new File("./src/test/resources/com/sap/psr/vulas/java/test/OuterClass$InnerClass.class");
      final Path abs_path = f3.toPath().toAbsolutePath().normalize();
      final Path classpath = ClassPoolUpdater.getInstance().getClasspath(abs_path.toFile());
      assertTrue(classpath.toFile().exists());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void checkForClint() {
    try {
      // Analyze normal class without anything static
      final File basic = new File("./src/test/resources/classpath/BasicClass.class");
      final CtClass basicCtclass = ClassPool.getDefault().makeClass(new FileInputStream(basic));
      System.out.println("---------------- Basic Class -----------------");
      System.out.println(basicCtclass.getClassInitializer());
      for (CtConstructor ctc : basicCtclass.getConstructors()) {
        System.out.println(ctc.getMethodInfo());
      }

      // Analyze class with static fields
      final File staticFields = new File("./src/test/resources/classpath/StaticFields.class");
      final CtClass staticFieldsCtclass =
          ClassPool.getDefault().makeClass(new FileInputStream(staticFields));
      System.out.println("---------------- Static Fields -----------------");
      System.out.println(staticFieldsCtclass.getClassInitializer());
      for (CtConstructor ctc : staticFieldsCtclass.getConstructors()) {
        System.out.println(ctc.getMethodInfo());
      }

      // Analyze class with no static fields but with a static block
      final File staticBlock = new File("./src/test/resources/classpath/StaticBlock.class");
      final CtClass staticBlockCtclass =
          ClassPool.getDefault().makeClass(new FileInputStream(staticBlock));
      System.out.println("---------------- Static Block -----------------");
      System.out.println(staticBlockCtclass.getClassInitializer());
      for (CtConstructor ctc : staticBlockCtclass.getConstructors()) {
        System.out.println(ctc.getMethodInfo());
      }

      // Analyze class with static final fields
      final File staticFinal = new File("./src/test/resources/classpath/StaticFinal.class");
      final CtClass staticFinalCtclass =
          ClassPool.getDefault().makeClass(new FileInputStream(staticFinal));
      System.out.println("---------------- static final fields -----------------");
      System.out.println(staticFinalCtclass.getClassInitializer());
      for (CtConstructor ctc : staticFinalCtclass.getConstructors()) {
        System.out.println(ctc.getMethodInfo());
      }

      // Analyze class with static method
      final File staticMethod = new File("./src/test/resources/classpath/StaticMethod.class");
      final CtClass staticMethodCtclass =
          ClassPool.getDefault().makeClass(new FileInputStream(staticMethod));
      System.out.println("---------------- static final fields -----------------");
      System.out.println(staticMethodCtclass.getClassInitializer());
      for (CtConstructor ctc : staticMethodCtclass.getConstructors()) {
        System.out.println(ctc.getMethodInfo());
      }

      assertFalse(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
