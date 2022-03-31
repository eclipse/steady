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
package org.eclipse.steady.java.monitor;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.charset;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.status;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.composite;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.uri;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.eclipse.steady.ConstructId;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.JavaClassId;
import org.eclipse.steady.java.JavaConstructorId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.shared.connectivity.PathBuilder;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Test;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class ClassVisitorTest extends AbstractGoalTest {

  /**
   * Calls performed by the instrumentators used in {@link #testVisitMethodsInstr()}.
   */
  private void setupMockServices(Application _a) throws IOException {
    // GET app constructs
    whenHttp(server)
        .match(get("/backend" + PathBuilder.appConstructIds(_a)))
        .then(
            stringContent(FileUtil.readFile("./src/test/resources/constructIds.json")),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    // GET app bugs
    whenHttp(server)
        .match(get("/backend" + PathBuilder.appBugs(_a)))
        .then(
            stringContent(FileUtil.readFile("./src/test/resources/bugs.json")),
            contentType("application/json"),
            charset("UTF-8"),
            status(HttpStatus.OK_200));

    // OPTIONS app
    whenHttp(server)
        .match(composite(method(Method.OPTIONS), uri("/backend" + PathBuilder.app(_a))))
        .then(contentType("application/json"), charset("UTF-8"), status(HttpStatus.OK_200));
  }

  /**
   * Test class required for {@link ClassVisitorTest#testNonStaticInnerClassConstructor()}.
   */
  private class NonStaticInner {
    NonStaticInner(Map<String, Object> _arg) {}
  }
  ;

  @Test
  public void testPrettyPrint() {
    final String src =
        "try {if(!VUL_TRC_XGETALIGNMENT_635905){Class vul_cls = null;if(vul_cls==null) { try {"
            + " vul_cls=$0.getClass(); } catch(Exception e) {} }if(vul_cls==null) { try {"
            + " vul_cls=java.lang.invoke.MethodHandles.lookup().lookupClass(); } catch(Exception e)"
            + " {} }if(vul_cls==null) { try { vul_cls=$class; } catch(Exception e) {} }final"
            + " ClassLoader vul_cls_ldr=vul_cls.getClassLoader();java.net.URL vul_cls_res ="
            + " null;if(vul_cls_ldr!=null)vul_cls_res=vul_cls_ldr.getResource(vul_cls.getName().replace('.',"
            + " '/') + \".class\");java.util.Map params = new"
            + " java.util.HashMap();params.put(\"junit\", \"false\");params.put(\"counter\", new"
            + " Integer(1));VUL_TRC_XGETALIGNMENT_635905=org.eclipse.steady.java.monitor.trace.TraceCollector.callback(\"METHOD\",\"org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPTabImpl.xgetAlignment()\",vul_cls_ldr,vul_cls_res,\"BF0D37E25A643FD4527731790F174BE26AB74A07\",\"com.acme\",\"vulas-testapp\",\"2.1.0-SNAPSHOT\",params);}}"
            + " catch(Throwable e) { System.err.println(e.getClass().getName() + \" occured during"
            + " execution of instrumentation code in JAVA METH"
            + " [org.openxmlformats.schemas.wordprocessingml.x2006.main.impl.CTPTabImpl.xgetAlignment()]:"
            + " \" + e.getMessage()); }";
    final String pretty = ClassVisitor.prettyPrint(src);
  }

  @Test
  public void testVisitMethodsInstr() {
    try {
      // Mock REST services
      this.configureBackendServiceUrl(server);
      this.setupMockServices(this.testApp);

      // The instrumentors to be used
      System.setProperty(
          CoreConfiguration.INSTR_CHOOSEN_INSTR,
          "org.eclipse.steady.java.monitor.trace.SingleStackTraceInstrumentor,org.eclipse.steady.java.monitor.touch.TouchPointInstrumentor");

      // Set before static block in ClassVisitor
      System.setProperty(CoreConfiguration.INSTR_WRITE_CODE, "true");

      // Test field annotations
      System.setProperty(
          CoreConfiguration.INSTR_FLD_ANNOS,
          "javax.persistence.Transient, com.fasterxml.jackson.annotation.JsonIgnore");

      // Directory with instr code
      System.setProperty(VulasConfiguration.TMP_DIR, "./target/tmp");

      // The test class
      final JavaClassId cid = JavaId.parseClassQName("org.eclipse.steady.java.test.Vanilla");

      // Get a CtClass and identify its constructors
      final ClassPool cp = ClassPool.getDefault();
      final CtClass ctclass = cp.get(cid.getQualifiedName());
      final ClassVisitor cv = new ClassVisitor(ctclass);

      // Instrument the methods
      final Set<ConstructId> methods = cv.visitMethods(true);

      // Check that the methods have been instrumented
      final Path tmp = VulasConfiguration.getGlobal().getTmpDir();
      final Path p1 =
          Paths.get("./target/tmp/org/eclipse/steady/java/test/Vanilla.foo(String).java");
      final Path p2 =
          Paths.get("./target/tmp/org/eclipse/steady/java/test/Vanilla.vuln(String).java");
      System.out.println("Expecting files [" + p1 + "] and [" + p2 + "]");
      assertTrue(FileUtil.isAccessibleFile(p1));
      assertTrue(FileUtil.isAccessibleFile(p2));

      // Write class
      cv.finalizeInstrumentation();
      new File("./target/tmp/org/eclipse/steady/java/test").mkdirs();
      final File vanilla_class_file =
          new File("./target/tmp/org/eclipse/steady/java/test/Vanilla.class");
      FileUtil.writeToFile(vanilla_class_file, cv.getBytecode());
      assertTrue(FileUtil.isAccessibleFile(vanilla_class_file.toPath()));

      // Check the HTTP calls made
      /*verifyHttp(server).times(1,
      		method(Method.GET),
      		uri("/backend" + PathBuilder.appBugs(this.testApp)));
      verifyHttp(server).times(1,
      		method(Method.GET),
      		uri("/backend" + PathBuilder.appConstructIds(this.testApp)));
      verifyHttp(server).times(1,
      		method(Method.OPTIONS),
      		uri("/backend" + PathBuilder.app(this.testApp)));*/
    } catch (NotFoundException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    } catch (CannotCompileException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }

  /**
   * Test the handling of (1) the first constructor of non-static inner classes, (2) the handling of generics as method parameters.
   */
  @Test
  public void testNonStaticInnerClassConstructor() {
    try {
      // The test class
      final JavaClassId cid =
          JavaId.parseClassQName("org.eclipse.steady.java.monitor.ClassVisitorTest$NonStaticInner");

      // Get a CtClass and identify its constructors
      final ClassPool cp = ClassPool.getDefault();
      final CtClass ctclass = cp.get(cid.getQualifiedName());
      final ClassVisitor cv = new ClassVisitor(ctclass);
      final Set<ConstructId> constructors = cv.visitConstructors(false);

      // Expected result (no first parameter of type outer class, no diamond for type parameters)
      final JavaConstructorId expected =
          JavaId.parseConstructorQName(
              "org.eclipse.steady.java.monitor.ClassVisitorTest$NonStaticInner(Map)");

      // Check that certain constructs have been found
      assertEquals(expected, constructors.iterator().next());
    } catch (NotFoundException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    } catch (CannotCompileException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }

  @Test
  public void testFixQName() {
    assertTrue(ClassVisitor.removeParameterQualification("a.b.c.Class()").equals("a.b.c.Class()"));
    assertTrue(
        ClassVisitor.removeParameterQualification("a.b.c.Class(int)").equals("a.b.c.Class(int)"));
    assertTrue(
        ClassVisitor.removeParameterQualification("a.b.c.Class(int,a.b.C)")
            .equals("a.b.c.Class(int,C)"));
    assertTrue(
        ClassVisitor.removeParameterQualification("a.b.c.Class(int,C,boolean,a.b.c.ddd.Test)")
            .equals("a.b.c.Class(int,C,boolean,Test)"));
  }
}
