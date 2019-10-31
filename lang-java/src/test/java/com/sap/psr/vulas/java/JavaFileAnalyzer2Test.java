package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.shared.util.FileSearch;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class JavaFileAnalyzer2Test {

  /** Tests the analysis of java and class files that contain a class w/o package */
  @Test
  public void testClassWithoutPackage() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./src/test/java/ClassWithoutPackage.java"));
      final Map<ConstructId, Construct> constructs_from_java = jfa.getConstructs();

      final ClassFileAnalyzer cfa =
          (ClassFileAnalyzer)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./target/test-classes/ClassWithoutPackage.class"));
      final Map<ConstructId, Construct> constructs_from_class = cfa.getConstructs();

      // The parsing should produce the following 3 elements:
      final JavaClassId cl = JavaId.parseClassQName("ClassWithoutPackage");
      final JavaConstructorId co = JavaId.parseConstructorQName("ClassWithoutPackage()");
      final JavaMethodId m = JavaId.parseMethodQName("ClassWithoutPackage.foo()");

      // Assertions
      assertEquals(3, constructs_from_java.size());
      assertTrue(constructs_from_java.containsKey(cl));
      assertTrue(constructs_from_java.containsKey(co));
      assertTrue(constructs_from_java.containsKey(m));

      assertEquals(3, constructs_from_class.size());
      assertTrue(constructs_from_class.containsKey(cl));
      assertTrue(constructs_from_java.containsKey(co));
      assertTrue(constructs_from_class.containsKey(m));
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /** Two anonymous classes in Interface initializer. */
  @Test
  public void testAnonClassInInterfaceInit() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./src/test/java/com/sap/psr/vulas/java/test/ConfigurationKey.java"));
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");
      final JavaClassId anon1 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigurationKey$1");
      final JavaMethodId anon1_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.ConfigurationKey$1.compare(ConfigurationKey,ConfigurationKey)");
      final JavaClassId anon2 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigurationKey$2");
      final JavaMethodId anon2_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.ConfigurationKey$2.compare(ConfigurationKey,ConfigurationKey)");

      // Assertions
      assertEquals(5, constructs.size());
      assertTrue(constructs.containsKey(p));
      assertTrue(constructs.containsKey(anon1));
      assertTrue(constructs.containsKey(anon1_m));
      assertTrue(constructs.containsKey(anon2));
      assertTrue(constructs.containsKey(anon2_m));
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /** One named inner class declared in an interface, and one anonymous class in method. */
  @Test
  public void testNamedClassInInterfaceAndAnonClassInConstructor() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File(
                      "./src/test/java/com/sap/psr/vulas/java/test/HttpRequestCompletionLog.java"));
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");

      // Named inner class
      final JavaClassId named_class =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder");
      final JavaConstructorId named_class_cons =
          JavaId.parseConstructorQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder(String,String)");
      final JavaMethodId named_class_m1 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.forTarget(String,String)");
      final JavaMethodId named_class_m2 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withStartTimeInMillis(long)");
      final JavaMethodId named_class_m3 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withDurationInMillis(long)");
      final JavaMethodId named_class_m4 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withRequestEntitySizeInBytes(long)");
      final JavaMethodId named_class_m5 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withResponseEntitySizeInBytes(long)");
      final JavaMethodId named_class_m6 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withResponseStatus(int)");
      final JavaMethodId named_class_m7 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withResponseReasonPhrase(String)");
      final JavaMethodId named_class_m8 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.withResponseContentType(String)");
      final JavaMethodId named_class_m9 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder.build()");

      // Anon. class
      final JavaClassId anon_class =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1");
      final JavaMethodId anon_class_m1 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.toString()");
      final JavaMethodId anon_class_m2 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getDestination()");
      final JavaMethodId anon_class_m3 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getTargetPath()");
      final JavaMethodId anon_class_m4 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getStartTimeInMillis()");
      final JavaMethodId anon_class_m5 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getDurationInMillis()");
      final JavaMethodId anon_class_m6 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getRequestEntitySizeInBytes()");
      final JavaMethodId anon_class_m7 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getResponseEntitySizeInBytes()");
      final JavaMethodId anon_class_m8 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getResponseStatus()");
      final JavaMethodId anon_class_m9 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getResponseReasonPhrase()");
      final JavaMethodId anon_class_m10 =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.HttpRequestCompletionLog$Builder$1.getResponseContentType()");

      // Assertions
      assertEquals(23, constructs.size());
      assertTrue(constructs.containsKey(p));

      assertTrue(constructs.containsKey(named_class));
      assertTrue(constructs.containsKey(named_class_cons));
      assertTrue(constructs.containsKey(named_class_m1));
      assertTrue(constructs.containsKey(named_class_m2));
      assertTrue(constructs.containsKey(named_class_m3));
      assertTrue(constructs.containsKey(named_class_m4));
      assertTrue(constructs.containsKey(named_class_m5));
      assertTrue(constructs.containsKey(named_class_m6));
      assertTrue(constructs.containsKey(named_class_m7));
      assertTrue(constructs.containsKey(named_class_m8));
      assertTrue(constructs.containsKey(named_class_m9));

      assertTrue(constructs.containsKey(anon_class));
      assertTrue(constructs.containsKey(anon_class_m1));
      assertTrue(constructs.containsKey(anon_class_m2));
      assertTrue(constructs.containsKey(anon_class_m3));
      assertTrue(constructs.containsKey(anon_class_m4));
      assertTrue(constructs.containsKey(anon_class_m5));
      assertTrue(constructs.containsKey(anon_class_m6));
      assertTrue(constructs.containsKey(anon_class_m7));
      assertTrue(constructs.containsKey(anon_class_m8));
      assertTrue(constructs.containsKey(anon_class_m9));
      assertTrue(constructs.containsKey(anon_class_m10));

    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /**
   * Enum with constructor and methods, an anon class in one method, and 3 named spread accross
   * different other methods.
   */
  @Test
  public void testEnumAndNamedClassesInMethod() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./src/test/java/com/sap/psr/vulas/java/test/ConfigKey.java"));
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");

      // The enum and its methods
      final JavaEnumId e = JavaId.parseEnumQName("com.sap.psr.vulas.java.test.ConfigKey");
      final JavaConstructorId enum_c1 =
          JavaId.parseConstructorQName(
              "com.sap.psr.vulas.java.test.ConfigKey(Class,String,String)");
      final JavaMethodId enum_m1 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey.getType()");
      final JavaMethodId enum_m2 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey.getKey()");
      final JavaMethodId enum_m3 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey.getDefaultValue()");
      final JavaMethodId enum_m4 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey.fromKey(String)");

      // The anon class and its method
      final JavaClassId anon_class =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigKey$1");
      final JavaMethodId anon_class_m1 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey$1.foo()");

      // The named classes
      final JavaClassId nc1 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigKey$1InnerClass1");
      final JavaConstructorId nc1_c =
          JavaId.parseConstructorQName("com.sap.psr.vulas.java.test.ConfigKey$1InnerClass1()");
      final JavaMethodId nc1_m =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey$1InnerClass1.foo()");

      final JavaClassId nc2 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigKey$1InnerClass2");
      final JavaMethodId nc2_m =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey$1InnerClass2.foo()");

      final JavaClassId nc3 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.ConfigKey$2InnerClass1");
      final JavaConstructorId nc3_c =
          JavaId.parseConstructorQName("com.sap.psr.vulas.java.test.ConfigKey$2InnerClass1()");
      final JavaMethodId nc3_m =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.ConfigKey$2InnerClass1.foo()");

      // Assertions
      assertEquals(17, constructs.size());
      assertTrue(constructs.containsKey(p));
      assertTrue(constructs.containsKey(e));
      assertTrue(constructs.containsKey(enum_c1));
      assertTrue(constructs.containsKey(enum_m1));
      assertTrue(constructs.containsKey(enum_m2));
      assertTrue(constructs.containsKey(enum_m3));
      assertTrue(constructs.containsKey(enum_m4));
      assertTrue(constructs.containsKey(anon_class));
      assertTrue(constructs.containsKey(anon_class_m1));
      assertTrue(constructs.containsKey(nc1));
      assertTrue(constructs.containsKey(nc1_c));
      assertTrue(constructs.containsKey(nc1_m));
      assertTrue(constructs.containsKey(nc2));
      assertTrue(constructs.containsKey(nc2_m));
      assertTrue(constructs.containsKey(nc3));
      assertTrue(constructs.containsKey(nc3_c));
      assertTrue(constructs.containsKey(nc3_m));
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /** A plain vanilla class. */
  @Test
  public void testVanilla() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./src/test/java/com/sap/psr/vulas/java/test/Vanilla.java"));
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");
      final JavaClassId cl = JavaId.parseClassQName("com.sap.psr.vulas.java.test.Vanilla");
      final JavaConstructorId cons =
          JavaId.parseConstructorQName("com.sap.psr.vulas.java.test.Vanilla(String)");
      final JavaMethodId meth =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.Vanilla.foo(String)");
      final JavaMethodId meth2 =
          JavaId.parseMethodQName("com.sap.psr.vulas.java.test.Vanilla.vuln(String)");

      // Assertions
      assertEquals(5, constructs.size());
      assertTrue(constructs.containsKey(p));
      assertTrue(constructs.containsKey(cl));
      assertTrue(constructs.containsKey(cons));
      assertTrue(constructs.containsKey(meth));
      assertTrue(constructs.containsKey(meth2));
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /** A bunch of files from Apache CXF. */
  @Test
  public void testCxfClasses() throws FileAnalysisException {
    final FileAnalyzer fv =
        FileAnalyzerFactory.buildFileAnalyzer(
            Paths.get(
                    "./src/test/resources/ws_security_1438423/src/main/java/org/apache/cxf/ws/security/cache")
                .toFile());
    final Map<ConstructId, Construct> constructs = fv.getConstructs();
    assertEquals(35, constructs.size());
  }

  /** A bunch of awfully nested classes (named and anonymous). */
  @Test
  public void testNestedDeclarationMess() {
    try {
      final File file =
          new File("./src/test/java/com/sap/psr/vulas/java/test/NestedDeclarations.java");
      final FileAnalyzer fa = FileAnalyzerFactory.buildFileAnalyzer(file);
      final Map<ConstructId, Construct> constructs = fa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");

      final JavaClassId cl1 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.NestedDeclarations"); // line 5
      final JavaMethodId cl1_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations.doSomething()"); // line 42

      final JavaClassId cl2 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoSomethingElse$DoThis"); // line 11
      final JavaMethodId cl2_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoSomethingElse$DoThis.doThis()"); // line 13

      final JavaClassId cl3 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoSomethingElse$DoThis$1"); // line 16
      final JavaMethodId cl3_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoSomethingElse$DoThis$1.doThat()"); // line 17

      final JavaClassId cl4 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.NestedDeclarations$1"); // line 26
      final JavaMethodId cl4_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$1.doSomethingElse()"); // line 27

      final JavaClassId cl5 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoThis"); // line 31
      final JavaMethodId cl5_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoThis.doThis()"); // line 33

      final JavaClassId cl6 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoThis$1"); // line 36
      final JavaMethodId cl6_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$DoThis$1.doThat()"); // line 37

      final JavaClassId cl7 =
          JavaId.parseClassQName("com.sap.psr.vulas.java.test.NestedDeclarations$2"); // line 45
      final JavaMethodId cl7_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$2.doSomething()"); // line 46

      final JavaClassId cl8 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$1DoThat"); // line 50
      final JavaMethodId cl8_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$1DoThat.doThat()"); // line 52

      final JavaClassId cl9 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$1DoThat$1"); // line 55
      final JavaMethodId cl9_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$1DoThat$1.doThis()"); // line 56

      final JavaEnumId e =
          JavaId.parseEnumQName("com.sap.psr.vulas.java.test.NestedDeclarations$Foo"); // line 62
      final JavaMethodId e_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$Foo.bar()"); // line 64

      final JavaClassId cl10 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$Foo$1DoThis"); // line 66
      final JavaMethodId cl10_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$Foo$1DoThis.doThis()"); // line 68

      final JavaClassId cl11 =
          JavaId.parseClassQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$Foo$1DoThis$1"); // line 71
      final JavaMethodId cl11_m =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.NestedDeclarations$Foo$1DoThis$1.doThat()"); // line 72

      // Assertions
      assertEquals(25, constructs.size());
      assertTrue(constructs.containsKey(p));
      assertTrue(constructs.containsKey(cl1));
      assertTrue(constructs.containsKey(cl1_m));

      assertTrue(constructs.containsKey(cl2));
      assertTrue(constructs.containsKey(cl2_m));

      assertTrue(constructs.containsKey(cl3));
      assertTrue(constructs.containsKey(cl3_m));

      assertTrue(constructs.containsKey(cl4));
      assertTrue(constructs.containsKey(cl4_m));

      assertTrue(constructs.containsKey(cl5));
      assertTrue(constructs.containsKey(cl5_m));

      assertTrue(constructs.containsKey(cl6));
      assertTrue(constructs.containsKey(cl6_m));

      assertTrue(constructs.containsKey(cl7));
      assertTrue(constructs.containsKey(cl7_m));

      assertTrue(constructs.containsKey(cl8));
      assertTrue(constructs.containsKey(cl8_m));

      assertTrue(constructs.containsKey(cl9));
      assertTrue(constructs.containsKey(cl9_m));

      assertTrue(constructs.containsKey(e));
      assertTrue(constructs.containsKey(e_m));

      assertTrue(constructs.containsKey(cl10));
      assertTrue(constructs.containsKey(cl10_m));

      assertTrue(constructs.containsKey(cl11));
      assertTrue(constructs.containsKey(cl11_m));
    } catch (FileAnalysisException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }

  /** A simple class with different parameterized arguments. */
  @Test
  public void testGenerics() {
    try {
      final JavaFileAnalyzer2 jfa =
          (JavaFileAnalyzer2)
              FileAnalyzerFactory.buildFileAnalyzer(
                  new File("./src/test/java/com/sap/psr/vulas/java/test/Generics.java"));
      final Map<ConstructId, Construct> constructs = jfa.getConstructs();

      // The parsing should produce the following 5 elements:
      final JavaPackageId p = new JavaPackageId("com.sap.psr.vulas.java.test");
      final JavaClassId cl = JavaId.parseClassQName("com.sap.psr.vulas.java.test.Generics");
      final JavaConstructorId cons =
          JavaId.parseConstructorQName("com.sap.psr.vulas.java.test.Generics(Map)");
      final JavaMethodId meth =
          JavaId.parseMethodQName(
              "com.sap.psr.vulas.java.test.Generics.foo(String,Collection,List)");

      // Assertions
      assertEquals(4, constructs.size());
      assertTrue(constructs.containsKey(p));
      assertTrue(constructs.containsKey(cl));
      assertTrue(constructs.containsKey(cons));
      assertTrue(constructs.containsKey(meth));
    } catch (FileAnalysisException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }

  /**
   * Attention: The class SAMLUtils overloads the method parseRolesInAssertion using the types
   * "org.opensaml.saml1.core.Assertion" and
   * "parseRolesInAssertion(org.opensaml.saml2.core.Assertion". Since we remove the parameter
   * qualification, the class only has 13 rather than 14 methods. TODO: Discuss the case
   */
  @Test
  public void testCxfClass() {
    try {
      final File file =
          new File(
              "./src/test/resources/ws_security_1438423/src/main/java/org/apache/cxf/ws/security/wss4j/SAMLUtils.java");
      final FileAnalyzer fa = (JavaFileAnalyzer2) FileAnalyzerFactory.buildFileAnalyzer(file);
      final Map<ConstructId, Construct> constructs = fa.getConstructs();
      assertEquals(13, constructs.size());
    } catch (FileAnalysisException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }

  /** See Jira VULAS-739 */
  @Test
  public void testSunClass() {
    try {
      final File file = new File("./src/test/resources/Collector.java");
      final FileAnalyzer fa = (JavaFileAnalyzer2) FileAnalyzerFactory.buildFileAnalyzer(file);
      final Map<ConstructId, Construct> constructs = fa.getConstructs();
      assertEquals(2, constructs.size());
    } catch (FileAnalysisException e) {
      System.err.println(e.getMessage());
      assertTrue(false);
    }
  }

  /**
   * Tests whether the constructs extracted from a Java file correspond to the ones obtained from
   * the compiled file.
   */
  @Test
  public void testCompareConstructCreation() {
    FileSearch fs = new FileSearch(new String[] {"class"});
    Set<Path> class_files =
        fs.search(Paths.get("./target/test-classes/com/sap/psr/vulas/java/test/"), 1);
    Set<ConstructId> c_from_class = new HashSet<ConstructId>();
    String filename = null;
    for (Path p : class_files) {
      filename = p.getFileName().toString();
      if (filename.startsWith("TestAnon")) {
        FileAnalyzer fa;
        try {
          fa = FileAnalyzerFactory.buildFileAnalyzer(p.toFile());
          c_from_class.addAll(fa.getConstructs().keySet());
        } catch (FileAnalysisException e) {
          e.printStackTrace();
          assertTrue(false);
        }
      }
    }

    Set<ConstructId> c_from_java = new HashSet<ConstructId>();
    File p = null;
    try {
      p = Paths.get("./src/test/java/com/sap/psr/vulas/java/test/TestAnon.java").toFile();
      FileAnalyzer fa2 = (JavaFileAnalyzer2) FileAnalyzerFactory.buildFileAnalyzer(p);
      c_from_java.addAll(fa2.getConstructs().keySet());
    } catch (FileAnalysisException e) {
      e.printStackTrace();
    }

    assertTrue(this.compareConstructSets(c_from_class, c_from_java));
  }

  private boolean compareConstructSets(
      Set<ConstructId> _bytecode_constructs, Set<ConstructId> _sourcecode_constructs) {
    final Set<ConstructId> bytecode_constructs = new HashSet<ConstructId>(_bytecode_constructs);
    final Set<ConstructId> sourcecode_constructs = new HashSet<ConstructId>(_sourcecode_constructs);

    System.out.println(
        "Matching constructs from bytecode ("
            + bytecode_constructs.size()
            + ") against source code ("
            + sourcecode_constructs.size()
            + "):\n");
    int matches = 0;

    // Remove constructs found in both sources
    for (ConstructId f : _bytecode_constructs) {
      for (ConstructId s : _sourcecode_constructs) {
        if (f.equals(s)) {
          matches++;
          bytecode_constructs.remove(f);
          sourcecode_constructs.remove(s);
        }
      }
    }

    // Remove constructs created at compile time

    // For enums: constructor(String,int) and values() and valueOf
    JavaConstructorId cons =
        JavaId.parseConstructorQName("com.sap.psr.vulas.java.test.TestAnon$Foo(String,int)");
    JavaMethodId values =
        JavaId.parseMethodQName("com.sap.psr.vulas.java.test.TestAnon$Foo.values()");
    JavaMethodId value_of =
        JavaId.parseMethodQName("com.sap.psr.vulas.java.test.TestAnon$Foo.valueOf(String)");
    bytecode_constructs.remove(cons);
    bytecode_constructs.remove(values);
    bytecode_constructs.remove(value_of);

    // For classes: Remove all default constructors (w/o arguments)
    JavaId jid = null;
    JavaConstructorId jcid = null;
    for (ConstructId c : _bytecode_constructs) {
      jid = (JavaId) c;
      if (bytecode_constructs.contains(c) && jid.getType().equals(JavaId.Type.CONSTRUCTOR)) {
        jcid = (JavaConstructorId) jid;
        if (!jcid.hasParams()) {
          bytecode_constructs.remove(c);
        }
      }
    }

    if (!bytecode_constructs.isEmpty()) {
      System.out.println(
          bytecode_constructs.size() + " unmatched constructs in first set (from bytecode):");
      for (ConstructId c : bytecode_constructs) {
        System.out.println("    " + c);
      }
    }

    if (!sourcecode_constructs.isEmpty()) {
      System.out.println(
          sourcecode_constructs.size() + " unmatched constructs in second set (from source code):");
      for (ConstructId c : sourcecode_constructs) {
        System.out.println("    " + c);
      }
    }

    // Both should be empty by now (all matches and compiler-added constructs have been removed)
    return bytecode_constructs.isEmpty() && sourcecode_constructs.isEmpty();
  }
}
