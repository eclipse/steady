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
package org.eclipse.steady.java.monitor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.core.util.CoreConfiguration;
import org.eclipse.steady.java.JavaClassId;
import org.eclipse.steady.java.JavaId;
import org.eclipse.steady.java.JavaId.Type;
import org.eclipse.steady.shared.json.model.Application;
import org.eclipse.steady.shared.util.FileUtil;
import org.eclipse.steady.shared.util.VulasConfiguration;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * Identifies all methods and constructors in a given Java class (using Javassist).
 * Can inject instrumentation code into constructors and methods in order to collect
 * trace information during application tests.
 */
public class ClassVisitor {

  // ====================================== STATIC MEMBERS

  private static Logger log = null;

  private static final Logger getLog() {
    if (ClassVisitor.log == null)
      ClassVisitor.log = org.apache.logging.log4j.LogManager.getLogger();
    return ClassVisitor.log;
  }

  // ====================================== INSTANCE MEMBERS

  private JavaId javaId = null;
  private String qname = null;
  private CtClass c = null;

  /** The outer class of nested classes. */
  private CtClass declaringClass = null;

  /** True if the class is already instrumented. */
  private Boolean isInstrumented = null;

  private String originalArchiveDigest = null;
  private Application appContext = null;
  private byte[] bytes = null;

  /** Major version of the class file format (see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html). */
  private int major = 0;

  /** Minor version of the class file format (see https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html). */
  private int minor = 0;

  /** The constructs found in the given class. */
  private Set<ConstructId> constructs = null;

  private boolean writeCodeToTmp = false;

  private String[] fieldAnnotations = null;

  /**
   * <p>Constructor for ClassVisitor.</p>
   *
   * @param _c a {@link javassist.CtClass} object.
   */
  public ClassVisitor(CtClass _c) {
    // Build the JavaId
    if (_c.isInterface())
      throw new IllegalArgumentException("[" + _c.getName() + "]: Interfaces are not supported");
    else if (_c.isEnum()) this.javaId = JavaId.parseEnumQName(_c.getName());
    else this.javaId = JavaId.parseClassQName(_c.getName());

    this.qname = this.javaId.getQualifiedName();
    this.c = _c;

    // Remember major/minor
    final ClassFile cf = _c.getClassFile();
    this.major = cf.getMajorVersion();
    this.minor = cf.getMinorVersion();

    // For nested classes, get the declaring (outer) class: It is used to skip the first argument in
    // non-static inner classes
    try {
      this.declaringClass = _c.getDeclaringClass();
    } catch (NotFoundException e) {
      // Only a problem in case of non-static inner classes, because in that case the 1st argument
      // of the constructor cannot be removed, cf. method visitConstructors(boolean)
      if (!Modifier.isStatic(this.c.getModifiers()))
        ClassVisitor.getLog()
            .warn(
                "No declaring class found for non-static inner class ["
                    + this.javaId.getQualifiedName()
                    + "]"); // : " + e.getMessage());
    }

    this.writeCodeToTmp =
        VulasConfiguration.getGlobal()
            .getConfiguration()
            .getBoolean(CoreConfiguration.INSTR_WRITE_CODE, false);

    this.fieldAnnotations =
        VulasConfiguration.getGlobal()
            .getStringArray(CoreConfiguration.INSTR_FLD_ANNOS, new String[] {});
  }

  /**
   * Returns a set with the {@link ConstructId}s of all constructs contained in the given Java class, i.e.,
   * all methods, all constructors, the class or enumeration and the package (unless it is the default package,
   * i.e., no package).
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<ConstructId> getConstructs() {
    if (this.constructs == null) {
      this.constructs = new TreeSet<ConstructId>();
      this.constructs.add(this.javaId);

      // Do not add the default package (qualified name=="")
      // This makes the constructs obtained from java and class files more comparable
      if (!this.javaId.getJavaPackageId().getSimpleName().equals(""))
        this.constructs.add(this.javaId.getJavaPackageId());

      try {
        this.constructs.addAll(this.visitConstructors(false));
        this.constructs.addAll(this.visitMethods(false));
      } catch (CannotCompileException e) {
        // Should never happen since we do not instrument in this case (argument is false)
      }
    }
    return this.constructs;
  }

  /**
   * <p>isInstrumented.</p>
   *
   * @return a boolean.
   */
  public synchronized boolean isInstrumented() {
    if (this.isInstrumented == null) {
      try {
        this.c.getDeclaredField("VUL_CLS_INS");
        this.isInstrumented = Boolean.valueOf(true);
      } catch (NotFoundException e) {
        this.isInstrumented = Boolean.valueOf(false);
      }
    }
    return this.isInstrumented.booleanValue();
  }

  /**
   * <p>visitMethods.</p>
   *
   * @param _instrument a boolean.
   * @return a {@link java.util.Set} object.
   * @throws javassist.CannotCompileException if any.
   */
  public synchronized Set<ConstructId> visitMethods(boolean _instrument)
      throws CannotCompileException {
    final Set<ConstructId> constructs = new HashSet<ConstructId>();
    final CtMethod[] methods = this.c.getDeclaredMethods();

    // Loop all methods
    JavaId jid = null;
    for (CtMethod meth : methods) {
      jid =
          JavaId.parseMethodQName(
              this.javaId.getType(), ClassVisitor.removeParameterQualification(meth.getLongName()));
      constructs.add(jid);

      // Instrument if requested, not yet done and possible (= not empty, as for abstract methods)
      final boolean is_native = Modifier.isNative(meth.getModifiers());
      if (_instrument
          && !isInstrumented()
          && !meth.isEmpty()
          && !is_native
          && meth.getLongName().startsWith(this.qname)) {
        try {
          this.instrument(jid, meth);
        }
        // Can happen if dependencies of the class are not available in the ClassPool
        catch (CannotCompileException ex) {
          // ClassVisitor.getLog().info("Exception while instrumenting " + jid + ": " +
          // ex.getMessage());
          throw ex;
        }
      }
    }
    ClassVisitor.getLog().debug("Class '" + this.qname + "': " + methods.length + " methods");
    return constructs;
  }

  /**
   * <p>visitConstructors.</p>
   *
   * @param _instrument a boolean.
   * @return a {@link java.util.Set} object.
   * @throws javassist.CannotCompileException if any.
   */
  public synchronized Set<ConstructId> visitConstructors(boolean _instrument)
      throws CannotCompileException {
    final Set<ConstructId> constructs = new HashSet<ConstructId>();
    final CtConstructor[] constructors = this.c.getDeclaredConstructors();
    String constr_name = null;
    JavaId jid = null;

    // Skip the first constructor parameter (added by the compiler for non-static classes)?
    final String param_to_skip =
        (this.declaringClass != null && !Modifier.isStatic(this.c.getModifiers())
            ? ClassVisitor.removePackageContext(this.declaringClass.getName())
            : null);

    // Static initializer <clinit> exists: Add it to the constructs and instrument (if requested)
    final CtConstructor initializer = this.c.getClassInitializer();
    if (initializer != null && this.javaId.getType() == Type.CLASS) {
      jid = ((JavaClassId) this.javaId).getClassInit();
      constructs.add(jid);

      // Instrument if requested
      if (_instrument
          && !isInstrumented()
          && !initializer.isEmpty()
          && initializer.getLongName().startsWith(this.qname)) {
        try {
          constr_name = this.removeParameterQualification(initializer.getLongName());
          this.instrument(jid, initializer);
        }
        // Can happen if dependencies of the class are not available in the ClassPool
        catch (CannotCompileException ex) {
          // ClassVisitor.getLog().error("Exception while instrumenting initializer [" + constr_name
          // + "]: " + ex.getMessage());
          throw ex;
        }
      }
    }

    // Loop all constructors and instrument (if requested)
    for (CtConstructor constr : constructors) {
      jid =
          JavaId.parseConstructorQName(
              this.javaId.getType(),
              ClassVisitor.removeParameterQualification(constr.getLongName()),
              param_to_skip);
      constructs.add(jid);

      // Instrument if requested
      if (_instrument
          && !isInstrumented()
          && !constr.isEmpty()
          && constr.getLongName().startsWith(this.qname)) {
        try {
          constr_name = this.removeParameterQualification(constr.getLongName());
          this.instrument(jid, constr);
        }
        // Can happen if dependencies of the class are not available in the ClassPool
        catch (CannotCompileException ex) {
          // ClassVisitor.getLog().error("Exception while instrumenting constructor [" + constr_name
          // + "]: " + ex.getMessage());
          throw ex;
        }
      }
    }
    ClassVisitor.getLog()
        .debug("Class '" + this.qname + "': " + constructors.length + " constructors");
    return constructs;
  }

  private void instrument(JavaId _jid, CtBehavior _behavior) throws CannotCompileException {
    // Loop all instrumentors to build the to-be-injected source code
    final List<IInstrumentor> instrumentorList = InstrumentorFactory.getInstrumentors();
    final Iterator<IInstrumentor> iter = instrumentorList.iterator();
    final StringBuffer instrumentation_code = new StringBuffer();
    while (iter.hasNext()) {
      IInstrumentor i = iter.next();
      if (i.acceptToInstrument(_jid, _behavior, this)) {
        i.instrument(instrumentation_code, _jid, _behavior, this);
      }
    }

    // Return if there's nothing to inject
    if (instrumentation_code.length() == 0) return;

    // If there's something to inject, surround it by a try clause
    final StringBuffer source_code = new StringBuffer();
    source_code.append("try {");
    source_code.append(instrumentation_code.toString());
    source_code.append("}");
    source_code.append("catch(IllegalStateException ise) { throw ise; }");
    source_code.append(
        "catch(Throwable e) { System.err.println(e.getClass().getName() + \" occurred during"
            + " execution of instrumentation code in "
            + _jid.toString()
            + ": \" + e.getMessage()); }");

    // Remember an exception (if any) and throw it at the end
    CannotCompileException cce = null;

    // Inject the code
    try {
      if (_jid.getType().equals(JavaId.Type.CONSTRUCTOR)
          || _jid.getType().equals(JavaId.Type.CLASSINIT))
        _behavior.insertAfter(source_code.toString());
      else _behavior.insertBefore(source_code.toString());
    } catch (CannotCompileException e) {
      cce = e;
    }

    // Write source and byte code to tmp
    if (source_code.length() > 0 && (this.writeCodeToTmp || cce != null)) {
      Path p = null;
      try {
        p =
            Paths.get(
                VulasConfiguration.getGlobal().getTmpDir().toString(),
                _jid.getJavaPackageId().getQualifiedName().replace('.', '/'),
                _jid.getDefinitionContext().getName()
                    + "."
                    + _jid.getName().replace('<', '_').replace('>', '_')
                    + ".java");
        FileUtil.createDirectory(p.getParent());
        FileUtil.writeToFile(p.toFile(), ClassVisitor.prettyPrint(source_code.toString()));

        // Only log the writing of the source code in case of a previous exception
        if (cce != null)
          ClassVisitor.getLog()
              .warn(
                  "Compile exception when adding code to "
                      + _jid
                      + ": Instrumentation code written to file ["
                      + p
                      + "]");
      } catch (IOException e) {
        ClassVisitor.getLog()
            .warn(
                "Cannot write instrumentation code of "
                    + _jid
                    + " to file ["
                    + p
                    + "]: "
                    + e.getMessage());
      } catch (InvalidPathException ipe) {
        ClassVisitor.getLog()
            .warn(
                "Cannot write instrumentation code of "
                    + _jid
                    + " to file ["
                    + p
                    + "]: "
                    + ipe.getMessage());
      }
    }

    // Throw exception (if any)
    if (cce != null) throw cce;
  }

  /**
   * If called, the given SHA1 digest will be included in the instrumented code (as literal argument in the callback).
   * Like this, the digest of the original JAR is preserved even if the instrumented class is written back to the
   * archive.
   *
   * This method must be called prior to the invocation of visitMethods and visitConstructors, as the instrumentation
   * code varies depending on whether the digest is known or not.
   *
   * @param _sha1 a {@link java.lang.String} object.
   */
  public synchronized void setOriginalArchiveDigest(String _sha1) {
    this.originalArchiveDigest = _sha1;
  }

  /**
   * If called, the application context (Maven groupId, artifactId and version) will be included in the instrumented code (as literal argument in the callback).
   * Like this, the trace collector already knows the application context and does not need to determine it itself.
   * *
   * This method must be called prior to the invocation of visitMethods and visitConstructors, as the instrumentation
   * code varies depending on whether the app context is known or not.
   *
   * @param _ctx a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public synchronized void setAppContext(Application _ctx) {
    this.appContext = _ctx;
  }

  /**
   * Adds one additional member to the class: A boolean to indicate that the class has been instrumented
   * (so that later processes and threads do not need to do it again).
   *
   * @throws javassist.CannotCompileException if any.
   * @throws java.io.IOException if any.
   */
  public synchronized void finalizeInstrumentation() throws CannotCompileException, IOException {
    // Add member to indicate that the class has been instrumented
    if (!this.isInstrumented()) {
      this.addBooleanMember("VUL_CLS_INS", true, true);
      this.isInstrumented = Boolean.valueOf(true);
    }

    //		this.bytes = this.c.toBytecode();

    // Alternatively, create the byte array from the classfile
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(bos);
    final ClassFile cf = c.getClassFile();

    // Set major and minor version of the new class file (max. JAVA 7), preferably from the original
    // class file (as read in the constructor)
    // Todo: Make max. version configurable
    cf.setMajorVersion(Math.min(this.major, ClassFile.JAVA_7));
    cf.setMinorVersion(this.minor);

    cf.write(dos);
    dos.flush();
    this.bytes = bos.toByteArray();

    this.c.detach(); // Removes the CtClass from the ClassPool

    if (this.writeCodeToTmp) {
      Path p = null;
      try {
        p =
            Paths.get(
                VulasConfiguration.getGlobal().getTmpDir().toString(),
                this.javaId.getJavaPackageId().getQualifiedName().replace('.', '/'),
                this.javaId.getName().replace('<', '_').replace('>', '_') + ".class");
        FileUtil.createDirectory(p.getParent());
        FileUtil.writeToFile(p.toFile(), this.bytes);
      } catch (IOException e) {
        ClassVisitor.getLog()
            .warn(
                "Cannot write bytecode of "
                    + this.javaId
                    + " to file ["
                    + p
                    + "]: "
                    + e.getMessage());
      } catch (InvalidPathException ipe) {
        ClassVisitor.getLog()
            .warn(
                "Cannot write bytecode of "
                    + this.javaId
                    + " to file ["
                    + p
                    + "]: "
                    + ipe.getMessage());
      }
    }
  }

  /**
   * Returns the byte code of the visited class.
   *
   * @return the byte code of the visited class
   */
  public byte[] getBytecode() {
    return this.bytes.clone();
  }

  /**
   * <p>addBooleanMember.</p>
   *
   * @param _field_name a {@link java.lang.String} object.
   * @param _value a boolean.
   * @param _final a boolean.
   * @throws javassist.CannotCompileException if any.
   */
  public synchronized void addBooleanMember(String _field_name, boolean _value, boolean _final)
      throws CannotCompileException {
    final CtField f = new CtField(CtClass.booleanType, _field_name, this.c);
    if (!_final) f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT);
    else f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL);

    // Avoid problems with JDO/JPA
    this.addFieldAnnotations(f, this.fieldAnnotations);

    this.c.addField(f, Boolean.toString(_value));
  }

  /**
   * Adds the given annotations to the given field. Can be used to avoid problems with OR mappers by adding
   * an annotation "javax.persistence.Transient".
   * @param _fld
   * @param _annotations
   */
  private void addFieldAnnotations(CtField _fld, String[] _annotations) {
    if (_annotations != null && _annotations.length > 0) {
      final ConstPool cpool = this.c.getClassFile().getConstPool();
      final AnnotationsAttribute attr =
          new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
      for (String anno : _annotations) {
        final Annotation annot = new Annotation(anno, cpool);
        attr.addAnnotation(annot);
      }
      _fld.getFieldInfo().addAttribute(attr);
    }
  }

  /**
   * <p>addIntMember.</p>
   *
   * @param _field_name a {@link java.lang.String} object.
   * @param _final a boolean.
   * @throws javassist.CannotCompileException if any.
   */
  public synchronized void addIntMember(String _field_name, boolean _final)
      throws CannotCompileException {
    final CtField f = new CtField(CtClass.intType, _field_name, this.c);
    if (!_final) f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT);
    else f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL);

    // Avoid problems with JDO/JPA
    this.addFieldAnnotations(f, this.fieldAnnotations);

    this.c.addField(f, "0");
  }

  /**
   * Generates a name for a class member.
   *
   * @param _prefix a {@link java.lang.String} object.
   * @param _construct_name a {@link java.lang.String} object.
   * @param _random_part a boolean.
   * @return a {@link java.lang.String} object.
   */
  public String getUniqueMemberName(String _prefix, String _construct_name, boolean _random_part) {
    final StringBuffer b = new StringBuffer();
    if (_prefix != null) b.append(_prefix);
    if (_construct_name != null) {
      if (_prefix != null) b.append("_");

      // check if is a clinit removing <>
      _construct_name = _construct_name.replace("<", "").replace(">", "");

      b.append(_construct_name.toUpperCase());
    }
    if (_random_part) {
      if (_prefix != null || _construct_name != null) b.append("_");
      final String rnd = String.valueOf(Math.random() * 10000000);
      b.append(rnd.substring(0, rnd.indexOf('.')));
    }
    return b.toString();
  }

  /**
   * From http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8:
   *
   * The "Java letters" include uppercase and lowercase ASCII Latin letters A-Z (\u0041-\u005a),
   * and a-z (\u0061-\u007a), and, for historical reasons, the ASCII underscore (_, or \u005f) and dollar sign ($, or \u0024).
   * The $ character should be used only in mechanically generated source code or, rarely, to access pre-existing names on legacy systems.
   * The "Java digits" include the ASCII digits 0-9 (\u0030-\u0039).
   */
  private static Pattern QUALIFIED_TYPE_PATTERN = null;

  private static synchronized Pattern getClassPattern() {
    if (QUALIFIED_TYPE_PATTERN == null)
      QUALIFIED_TYPE_PATTERN = Pattern.compile("([0-9a-zA-Z_\\.\\$]*\\.)([a-zA-Z0-9_\\$]*)");
    return QUALIFIED_TYPE_PATTERN;
  }

  private static Pattern NESTED_CLASS_PATTERN = null;

  private static synchronized Pattern getNestedClassPattern() {
    if (NESTED_CLASS_PATTERN == null)
      NESTED_CLASS_PATTERN = Pattern.compile("([0-9a-zA-Z_\\$]*\\$)([a-zA-Z0-9_]*)");
    return NESTED_CLASS_PATTERN;
  }

  /**
   * Removes package information (if any) from method and constructor parameters.
   * Previously, a string tokenizer split the given {@link String} at every comma,
   * which led to problems in case of Map parameters (e.g., Map&lt;String,Object&gt;).
   * The current implementation uses the regular expression {@link ClassVisitor#QUALIFIED_TYPE_PATTERN}.
   *
   * @param _string a String with qualified parameter types
   * @return a a String where the package info of qualified parameter types has been removed
   */
  public static String removeParameterQualification(String _string) {
    final StringBuilder b = new StringBuilder();

    // Get everything between the brackets
    final int i = _string.indexOf("(");
    final int j = _string.lastIndexOf(")");
    if (i == -1 || j == -1)
      throw new IllegalArgumentException("Method has no round brackets: [" + _string + "]");

    b.append(_string.substring(0, i + 1));
    b.append(ClassVisitor.removePackageContext(_string.substring(i + 1, j)));
    b.append(_string.substring(j));
    return b.toString();
  }

  /**
   * <p>removePackageContext.</p>
   *
   * @param _string a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static String removePackageContext(String _string) {
    final StringBuilder b = new StringBuilder();

    // Find and replace pattern
    final Matcher m = ClassVisitor.getClassPattern().matcher(_string);
    Matcher nested_class_matcher = null;
    int idx = 0;
    String class_name = null;
    while (m.find()) {
      b.append(_string.substring(idx, m.start()));

      // Found class name, now check if nested
      class_name = m.group(2);
      nested_class_matcher = ClassVisitor.getNestedClassPattern().matcher(class_name);
      if (nested_class_matcher.matches()) {
        b.append(nested_class_matcher.group(2));
      } else {
        b.append(class_name);
      }
      idx = m.end();
    }

    // Append the remainders
    b.append(_string.substring(idx));

    return b.toString();
  }

  /**
   * <p>Getter for the field <code>javaId</code>.</p>
   *
   * @return a {@link org.eclipse.steady.java.JavaId} object.
   */
  public JavaId getJavaId() {
    return this.javaId;
  }

  /**
   * <p>getCtClass.</p>
   *
   * @return a {@link javassist.CtClass} object.
   */
  public CtClass getCtClass() {
    return this.c;
  }

  /**
   * <p>getArchiveDigest.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getArchiveDigest() {
    return this.originalArchiveDigest;
  }

  /**
   * <p>Getter for the field <code>appContext</code>.</p>
   *
   * @return a {@link org.eclipse.steady.shared.json.model.Application} object.
   */
  public synchronized Application getAppContext() {
    return this.appContext;
  }

  /**
   * <p>Getter for the field <code>qname</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public String getQname() {
    return this.qname;
  }

  /**
   * <p>Getter for the field <code>originalArchiveDigest</code>.</p>
   *
   * @return a {@link java.lang.String} object.
   */
  public synchronized String getOriginalArchiveDigest() {
    return this.originalArchiveDigest;
  }

  /**
   * <p>prettyPrint.</p>
   *
   * @param _src a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  public static final String prettyPrint(String _src) {
    final String n = System.getProperty("line.separator");
    final String indent = "  ";
    final StringBuffer b = new StringBuffer();
    int lvl = 0;
    for (int i = 0; i < _src.length(); i++) {
      char c = _src.charAt(i);
      switch (c) {
        case ';':
          b.append(c).append(n).append(getIndent(indent, lvl));
          break;
        case '{':
          b.append(c).append(n).append(getIndent(indent, ++lvl));
          break;
        case '}':
          b.append(c).append(n).append(getIndent(indent, --lvl));
          break;
        default:
          b.append(c);
      }
    }
    return b.toString();
  }

  private static String getIndent(String _c, int _i) {
    final StringBuffer b = new StringBuffer();
    for (int i = 0; i < _i; i++) b.append(_c);
    return b.toString();
  }
}
