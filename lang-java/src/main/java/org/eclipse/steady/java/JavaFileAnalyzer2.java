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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.validation.constraints.NotNull;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.logging.log4j.Logger;
import org.eclipse.steady.Construct;
import org.eclipse.steady.ConstructId;
import org.eclipse.steady.FileAnalysisException;
import org.eclipse.steady.FileAnalyzer;
import org.eclipse.steady.java.antlr.JavaLexer;
import org.eclipse.steady.java.antlr.JavaParser;
import org.eclipse.steady.java.antlr.JavaParser.CompilationUnitContext;
import org.eclipse.steady.java.antlr.JavaParser.FormalParameterContext;
import org.eclipse.steady.java.antlr.JavaParser.TypeTypeContext;
import org.eclipse.steady.java.antlr.JavaParserBaseListener;
import org.eclipse.steady.shared.util.FileUtil;

/**
 * Analyzes java source files using ANTLR.
 */
public class JavaFileAnalyzer2 extends JavaParserBaseListener implements FileAnalyzer {

  private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

  /**
   * All Java constructs found in the given Java file, created through visiting relevant nodes of the ANTLR parse tree.
   * The values are return by {@link #getConstructs()}. Java enums and interfaces are ignored, as they rarely contain
   * executable code.
   */
  private Map<ConstructId, Construct> constructs = null;

  private CharStream input = null;

  /** The file to be analyzed. */
  private File file = null;

  /**
   * Package, class, enum and interface declarations found while parsing a Java source file.
   * The topmost element will be used as declaration context of methods and constructors.
   */
  private final ContextStack contextStack = new ContextStack();

  /** Used for the construction of nested named and anonynous classes. */
  private final ConstructIdBuilder constructIdBuilder = new ConstructIdBuilder();

  /** {@inheritDoc} */
  @Override
  public String[] getSupportedFileExtensions() {
    return new String[] {"java"};
  }

  /** {@inheritDoc} */
  @Override
  public boolean canAnalyze(File _file) {
    final String ext = FileUtil.getFileExtension(_file);
    if (ext == null || ext.equals("")) return false;
    for (String supported_ext : this.getSupportedFileExtensions()) {
      if (supported_ext.equalsIgnoreCase(ext)) return true;
    }
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void analyze(final File _file) throws FileAnalysisException {
    this.setFile(_file);
  }

  /**
   * <p>Setter for the field <code>file</code>.</p>
   *
   * @param _file a {@link java.io.File} object.
   * @throws java.lang.IllegalArgumentException if any.
   */
  public void setFile(File _file) throws IllegalArgumentException {
    final String ext = FileUtil.getFileExtension(_file);
    if (!ext.equals("java"))
      throw new IllegalArgumentException("Expected a java file but got [" + _file + "]");
    if (!FileUtil.isAccessibleFile(_file.toPath()))
      throw new IllegalArgumentException("Cannot open file [" + _file + "]");
    this.file = _file;
  }

  /**
   * Creates and adds a new {@link Construct} to the set of constructs found in the analyzed file.
   * This method is called during the various visitor methods inherited from {@link JavaBaseListener}.
   * @param _id
   * @param _body
   */
  private void saveConstruct(ConstructId _id, String _body) {
    try {
      final Construct c = new Construct(_id, _body);
      this.constructs.put(_id, c);
      JavaFileAnalyzer2.log.debug("Added " + c.getId());
    } catch (IllegalArgumentException e) {
      JavaFileAnalyzer2.log.error(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void enterPackageDeclaration(@NotNull JavaParser.PackageDeclarationContext ctx) {
    // Create JavaId
    final JavaPackageId id = new JavaPackageId(ctx.getChild(1).getText());

    // Add to the stack
    this.contextStack.push(id);

    // Create the construct
    this.saveConstruct(id, ctx.getParent().getText());
  }

  /**
   * {@inheritDoc}
   *
   * Enums are added to {@link #constructs}.
   */
  @Override
  public void enterEnumDeclaration(@NotNull JavaParser.EnumDeclarationContext ctx) {
    // Create JavaId and push to the stack
    final ContextStackEntry cse = this.contextStack.peek();
    final JavaId decl_ctx =
        (cse == null ? JavaPackageId.DEFAULT_PACKAGE : (JavaId) cse.getConstructId());
    final JavaId id = new JavaEnumId(decl_ctx, ctx.IDENTIFIER().getText());
    this.contextStack.push(id);
    this.saveConstruct(id, this.getConstructContent(ctx));
  }

  /** {@inheritDoc} */
  @Override
  public void exitEnumDeclaration(@NotNull JavaParser.EnumDeclarationContext ctx) {
    final JavaId id = (JavaId) this.contextStack.pop().getConstructId();
    this.isOfExpectedType(id, new JavaId.Type[] {JavaId.Type.ENUM}, true);
  }

  /**
   * {@inheritDoc}
   *
   * Interfaces are not added to {@link #constructs}.
   */
  @Override
  public void enterInterfaceDeclaration(@NotNull JavaParser.InterfaceDeclarationContext ctx) {
    // Create JavaId and push to the stack
    final ContextStackEntry cse = this.contextStack.peek();
    final JavaId decl_ctx =
        (cse == null ? JavaPackageId.DEFAULT_PACKAGE : (JavaId) cse.getConstructId());
    final JavaId id = new JavaInterfaceId(decl_ctx, ctx.IDENTIFIER().getText());
    this.contextStack.push(id);
  }

  /** {@inheritDoc} */
  @Override
  public void exitInterfaceDeclaration(@NotNull JavaParser.InterfaceDeclarationContext ctx) {
    final JavaId id = (JavaId) this.contextStack.pop().getConstructId();
    this.isOfExpectedType(id, new JavaId.Type[] {JavaId.Type.INTERFACE}, true);
  }

  /** {@inheritDoc} */
  @Override
  public void enterClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx) {
    // Remember the declaration context (needed for the handling of anon. classes in enterClassBody)
    this.constructIdBuilder.setCurrentDeclarationContext(ctx.IDENTIFIER().getText());
  }

  /** {@inheritDoc} */
  @Override
  public void exitClassDeclaration(@NotNull JavaParser.ClassDeclarationContext ctx) {}

  /** {@inheritDoc} */
  @Override
  public void enterClassBody(@NotNull JavaParser.ClassBodyContext ctx) {
    // Create JavaId and push to the stack
    final JavaId id = (JavaClassId) this.constructIdBuilder.buildJavaClassId();
    this.contextStack.push(id);
    this.saveConstruct(id, this.getConstructContent(ctx));

    // Log anon classes
    if (this.constructIdBuilder.isAnonymousClass())
      JavaFileAnalyzer2.log.debug(
          this.indent(this.contextStack.size())
              + "Enter anon class body "
              + id.toString()
              + " "
              + this.printDeclarationStack());

    this.constructIdBuilder.resetCurrentDeclarationContext();
  }

  /** {@inheritDoc} */
  @Override
  public void exitClassBody(@NotNull JavaParser.ClassBodyContext ctx) {
    final JavaId id = (JavaId) this.contextStack.pop().getConstructId();
    this.isOfExpectedType(id, new JavaId.Type[] {JavaId.Type.CLASS}, true);
    this.constructIdBuilder.resetCurrentDeclarationContext();
  }

  /** {@inheritDoc} */
  @Override
  public void enterMethodDeclaration(@NotNull JavaParser.MethodDeclarationContext ctx) {
    // Peek JavaId and ensure it is a class or enum
    final JavaId class_ctx = (JavaId) this.contextStack.peek().getConstructId();
    this.isOfExpectedType(class_ctx, new JavaId.Type[] {JavaId.Type.CLASS, JavaId.Type.ENUM}, true);

    // Build the identifier
    final JavaMethodId id =
        new JavaMethodId(
            (JavaId) class_ctx,
            ctx.IDENTIFIER().getText(),
            this.getParameters(ctx.formalParameters().formalParameterList()));

    this.contextStack.push(id);
    this.saveConstruct(id, this.getConstructContent(ctx));
  }

  /** {@inheritDoc} */
  @Override
  public void exitMethodDeclaration(
      org.eclipse.steady.java.antlr.JavaParser.MethodDeclarationContext ctx) {
    this.contextStack.pop().getConstructId();
  }

  /** {@inheritDoc} */
  @Override
  public void enterConstructorDeclaration(@NotNull JavaParser.ConstructorDeclarationContext ctx) {
    // Peek JavaId and ensure it is a class or enum
    final JavaId class_ctx = (JavaId) this.contextStack.peek().getConstructId();
    this.isOfExpectedType(class_ctx, new JavaId.Type[] {JavaId.Type.CLASS, JavaId.Type.ENUM}, true);

    // Build the identifier
    final JavaId id =
        new JavaConstructorId(
            (JavaId) class_ctx, this.getParameters(ctx.formalParameters().formalParameterList()));

    this.contextStack.push(id);
    this.saveConstruct(id, this.getConstructContent(ctx));
  }

  /** {@inheritDoc} */
  @Override
  public void exitConstructorDeclaration(
      org.eclipse.steady.java.antlr.JavaParser.ConstructorDeclarationContext ctx) {
    this.contextStack.pop().getConstructId();
  }

  /**
   * Retrieves content for constructs of type Method, Constructor and Class.
   * @param ctx - ParseRuleContex
   * @return Extracted construct Body
   */
  private final String getConstructContent(ParserRuleContext ctx) {
    final int a = ctx.start.getStartIndex();
    final int b = ctx.stop.getStopIndex();
    final Interval interval = new Interval(a, b);
    final String text = this.input.getText(interval);
    return text;
  }

  private boolean isOfExpectedType(JavaId _jid, JavaId.Type[] _types, boolean _throw_exception) {
    boolean is = true;
    if (_jid == null || !Arrays.asList(_types).contains(_jid.getType())) {
      is = false;
      if (_throw_exception) {
        JavaFileAnalyzer2.log.error("Expected [" + _types[0] + "], got " + _jid);
        throw new IllegalStateException("Expected [" + _types[0] + "], got " + _jid);
      } else {
        JavaFileAnalyzer2.log.warn("Expected [" + _types[0] + "], got " + _jid);
      }
      is = false;
    }
    return is;
  }

  /**
   * Returns true if the construct stack only consists of classes. It allows skipping
   * all declarations happening in enums and interfaces, nested or not.
   */
  /*private boolean isClassDeclarationsOnly(ParserRuleContext _context) {
  		boolean is = true;
  		for(ContextStackEntry jid : this.contextStack.all()) {
  			if (       !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.PACKAGE)
  					&& !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.CLASS)
  					&& !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.METHOD) ) {

  				// Get the name of the current declaration
  				String item = null;
  				if (_context instanceof JavaParser.ClassDeclarationContext)
  					item = "class [" + ((JavaParser.ClassDeclarationContext) _context).Identifier().getText() + "]";
  				// else if (_context instanceof
  				// JavaParser.MethodDeclarationContext)
  				// item = "method [" + ((JavaParser.MethodDeclarationContext)
  				// _context).Identifier().getText() + "]";
  				else if (_context instanceof JavaParser.ConstructorDeclarationContext)
  					item = "constructor ["
  							+ ((JavaParser.ConstructorDeclarationContext) _context).Identifier().getText() + "]";
  				else if (_context instanceof JavaParser.ClassBodyContext)
  					item = "classBody";

  //				if(_context instanceof JavaParser.ClassDeclarationContext)
  //				item = "class [" + ((JavaParser.ClassDeclarationContext)_context).Identifier().getText() + "]";
  //			else if(_context instanceof JavaParser.MethodDeclarationContext)
  //				item = "method [" + ((JavaParser.MethodDeclarationContext)_context).Identifier().getText() + "]";
  //			else if(_context instanceof JavaParser.ConstructorDeclarationContext)
  //				item = "constructor [" +  ((JavaParser.ConstructorDeclarationContext)_context).Identifier().getText() + "]";

  				JavaFileAnalyzer2.log.info("Declaration of " + item + " will be skipped, it is inside a nested declarations including enums and/or interfaces");

  				is = false;
  				break;
  			}
  		}
  		return is;
  	}*/

  /*private boolean isClassDeclarationsOnly(ParserRuleContext _context) {
  		boolean is = true;
  		for(ContextStackEntry jid : this.contextStack.all()) {
  			if (       !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.PACKAGE)
  					&& !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.CLASS)
  					&& !((JavaId)jid.getConstructId()).getType().equals(JavaId.Type.METHOD) ) {

  				// Get the name of the current declaration
  				String item = null;
  				if (_context instanceof JavaParser.ClassDeclarationContext)
  					item = "class [" + ((JavaParser.ClassDeclarationContext) _context).Identifier().getText() + "]";
  				// else if (_context instanceof
  				// JavaParser.MethodDeclarationContext)
  				// item = "method [" + ((JavaParser.MethodDeclarationContext)
  				// _context).Identifier().getText() + "]";
  				else if (_context instanceof JavaParser.ConstructorDeclarationContext)
  					item = "constructor ["
  							+ ((JavaParser.ConstructorDeclarationContext) _context).Identifier().getText() + "]";
  				else if (_context instanceof JavaParser.ClassBodyContext)
  					item = "classBody";

  //				if(_context instanceof JavaParser.ClassDeclarationContext)
  //				item = "class [" + ((JavaParser.ClassDeclarationContext)_context).Identifier().getText() + "]";
  //			else if(_context instanceof JavaParser.MethodDeclarationContext)
  //				item = "method [" + ((JavaParser.MethodDeclarationContext)_context).Identifier().getText() + "]";
  //			else if(_context instanceof JavaParser.ConstructorDeclarationContext)
  //				item = "constructor [" +  ((JavaParser.ConstructorDeclarationContext)_context).Identifier().getText() + "]";

  				JavaFileAnalyzer2.log.info("Declaration of " + item + " will be skipped, it is inside a nested declarations including enums and/or interfaces");

  				is = false;
  				break;
  			}
  		}
  		return is;
  	}*/
  private List<String> getParameters(JavaParser.FormalParameterListContext _ctx) {
    if (_ctx == null) return null;
    else {
      List<String> l = new ArrayList<String>();
      List<FormalParameterContext> list = _ctx.formalParameter();
      for (FormalParameterContext par_ctx : list) {
        TypeTypeContext type_ctx = par_ctx.typeType();

        String t = type_ctx.getText();

        // Simply remove the parameterization of generic classes
        // This is possible, as they do not matter in terms of method overloading
        // Example: to methods foo(Set<String>) and foo(Set<Object>) are not possible within one
        // class
        if ((t.contains("<") || t.contains(">")) && t.indexOf("<") != -1) {
          t = t.substring(0, t.indexOf("<"));
        }

        // Parameters of simple types (boolean, int, etc.) can be added as is
        if (type_ctx.primitiveType() != null)
          l.add(t); // l.add(type_ctx.primitiveType().getText());
        // Parameters of complex types (class or interface) may or may not be specified using its
        // qualified name (i.e., with its package). Since we cannot (easily) add the package
        // information
        // for those where the package is missing, we simply remove it for all of them (and do the
        // same in the instrumentation).
        else if (type_ctx.classOrInterfaceType() != null)
          l.add(
              JavaId.removePackageContext(
                  t)); // l.add(JavaId.removePackageContext(type_ctx.classOrInterfaceType().getText()));
        else
          JavaFileAnalyzer2.log.error(
              "Parameter " + par_ctx.variableDeclaratorId().getText() + " has unknown type");
      }
      return l;
    }
  }

  /** {@inheritDoc} */
  @Override
  public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
    if (this.constructs == null) {
      try {
        this.constructs = new TreeMap<ConstructId, Construct>();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final InputStream is2 = new BufferedInputStream(new FileInputStream(this.file))) {
          int cc = -1;
          while ((cc = is2.read()) >= 0) baos.write(cc);
        }
        baos.flush();
        this.input = CharStreams.fromStream(new ByteArrayInputStream(baos.toByteArray()));
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, ctx);
      } catch (FileNotFoundException e) {
        throw new FileAnalysisException(e.getMessage(), e);
      } catch (RecognitionException e) {
        throw new FileAnalysisException(
            "ANTLR exception while analysing class file ["
                + this.file.getName()
                + "]: "
                + e.getMessage(),
            e);
      } catch (IOException e) {
        throw new FileAnalysisException(
            "I/O exception while analysing class file ["
                + this.file.getName()
                + "]: "
                + e.getMessage(),
            e);
      } catch (Exception e) {
        throw new FileAnalysisException(
            "Exception of type ["
                + e.getClass().getSimpleName()
                + "] while analyzing file ["
                + this.file.toPath().toAbsolutePath()
                + "]: "
                + e.getMessage(),
            e);
      }
    }
    return this.constructs;
  }

  /** {@inheritDoc} */
  @Override
  public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
    return this.getConstructs().containsKey(_id);
  }

  /** {@inheritDoc} */
  @Override
  public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
    return this.getConstructs().get(_id);
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasChilds() {
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public Set<FileAnalyzer> getChilds(boolean _recursive) {
    return null;
  }

  private final String indent(int _i) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < _i; i++) b.append(":   ");
    return b.toString();
  }

  private String printDeclarationStack() {
    final StringBuilder out = new StringBuilder().append("\t\t\t");
    for (ContextStackEntry jid : this.contextStack.all()) {
      out.append(((JavaId) jid.getConstructId()).getType().toString() + " ");
    }
    return out.toString();
  }

  class ConstructIdBuilder {

    // class name as obtained from the enterClassDeclaration callback
    // that callback is not called for anonymous classes, for which the
    // member below stays 'null'
    // (note: it's reset to null in enterClassBody)
    private String declaredName = null;

    /**
     * Used to give numeric names to anonymous inner classes.
     */
    private Map<ConstructId, Integer> anonymousClassCounters = new HashMap<ConstructId, Integer>();

    /**
     * Used to prepend numeric values to named classes declared in methods.
     */
    private Map<ConstructId, Map<String, Integer>> namedClassesCounter =
        new HashMap<ConstructId, Map<String, Integer>>();

    public void setCurrentDeclarationContext(String name) {
      this.declaredName = name;
    }

    public String getDeclaredName() {
      return this.declaredName;
    }

    public void resetCurrentDeclarationContext() {
      this.declaredName = null;
    }

    /**
     * Returns true if the class for which the name is about to be build is
     * anonymous
     *
     * @return
     */
    private boolean isAnonymousClass() {
      return this.declaredName == null;
    }

    /**
     * Returns the current counter value for anonymous classes and increases the counter by one.
     * @param id
     * @return
     */
    private Integer incrementAnonymousCounter(ConstructId id) {
      Integer count = null;

      // Initialize if not done already
      if (!this.anonymousClassCounters.containsKey(id)) this.anonymousClassCounters.put(id, 1);

      // Current value
      count = this.anonymousClassCounters.get(id);

      // Increase by one
      this.anonymousClassCounters.put(id, count + 1);

      return count;
    }

    /**
     * Returns the current counter value for named classes and increases the counter by one.
     * @param id
     * @return
     */
    private Integer incrementNamedCounter(ConstructId id, String _class_name) {
      Integer count = null;
      Map<String, Integer> name_counter = this.namedClassesCounter.get(id);

      // Initialize if necessary
      if (name_counter == null) {
        name_counter = new HashMap<String, Integer>();
        this.namedClassesCounter.put(id, name_counter);
      }

      //
      if (!name_counter.containsKey(_class_name)) name_counter.put(_class_name, 1);

      // Current value
      count = name_counter.get(_class_name);

      // Increase by one
      name_counter.put(_class_name, count + 1);

      return count;
    }

    /**
     * Create a name for the construct at hand considering what containers
     * are currently on the stack
     *
     * @param spaceId
     *            of the construct (the suffix to add to the container
     *            construct)
     * @return
     */
    public ConstructId buildJavaClassId() {

      final JavaId.Type[] cie =
          new JavaId.Type[] {JavaId.Type.CLASS, JavaId.Type.INTERFACE, JavaId.Type.ENUM};
      final JavaId.Type[] pcie =
          new JavaId.Type[] {
            JavaId.Type.PACKAGE, JavaId.Type.CLASS, JavaId.Type.INTERFACE, JavaId.Type.ENUM
          };

      // Class name of the new class
      final StringBuilder class_name = new StringBuilder();

      // The context of the to-be-created class (can be PACK, CLASS, INTERFACE or ENUM), depending
      // on the case
      JavaId context = null;
      final ContextStackEntry topmost_stack_entry = contextStack.peek();

      // Named class
      if (!this.isAnonymousClass()) {

        // In method
        if (topmost_stack_entry != null
            && ((JavaId) topmost_stack_entry.getConstructId()).type.equals(JavaId.Type.METHOD)) {
          // Get the context (class, interface or enum)
          final ContextStackEntry cse = contextStack.peek(cie);
          if (cse == null) {
            throw new IllegalStateException(
                "Named class [" + this.declaredName + "] w/o appropriate context");
          } else {
            // Get and increment name counter
            context = (JavaId) cse.getConstructId();
            class_name.append(
                this.incrementNamedCounter(context, this.declaredName).toString()
                    + this.declaredName);
          }
        }

        // In class
        else {
          // Get the context (pack, class, interface or enum)
          final ContextStackEntry cse = contextStack.peek(pcie);
          if (cse != null) context = (JavaId) cse.getConstructId();
          // Name is what we found in enterClassDeclaration
          class_name.append(this.declaredName);
        }
      }
      // Anon class
      else {
        // Fetch the parent (class, enum or interface)
        final ContextStackEntry cse = contextStack.peek(cie);
        if (cse == null) {
          throw new IllegalStateException(
              "Anonnymous class [" + this.declaredName + "] w/o appropriate context");
        } else {
          // Get and increment anon counter
          context = (JavaId) cse.getConstructId();
          class_name.append(this.incrementAnonymousCounter(context).toString());
        }
      }

      final ConstructId id = new JavaClassId(context, class_name.toString());
      return id;
    }
  }

  static class ContextStackEntry {
    private ConstructId constructId = null;
    private Map<Object, Object> attributes = new HashMap<Object, Object>();

    public ContextStackEntry(ConstructId id) {
      this.constructId = id;
    }

    public ContextStackEntry(ConstructId id, Object key, Object value) {
      this.constructId = id;
      this.setAttribute(key, value);
    }

    public ConstructId getConstructId() {
      return this.constructId;
    }

    public Object getAttribute(Object key) {
      return this.attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
      this.attributes.put(key, value);
    }
  }

  static class ContextStack {

    /**
     * Nested package, class, enum and interface declarations found while
     * parsing a Java source file. The topmost element will be used as
     * declaration context of methods and constructors.
     */
    private Deque<ContextStackEntry> nestedDeclarationContexts =
        new ArrayDeque<ContextStackEntry>();

    /**
     * Return the topmost element (without removing it)
     *
     * @return
     */
    public ContextStackEntry peek() {
      return this.nestedDeclarationContexts.peek();
    }

    /**
     * Gets the top-most element <a>of a given types</a> (without removing it).
     *
     * @param _t
     *            the type of context elements to consider
     * @return
     */
    public ContextStackEntry peek(JavaId.Type[] _t) {
      final Iterator<ContextStackEntry> iterator = this.nestedDeclarationContexts.iterator();
      ContextStackEntry entry = null, result = null;
      search:
      while (iterator.hasNext()) {
        entry = iterator.next();
        for (int i = 0; i < _t.length; i++) {
          if (((JavaId) entry.getConstructId()).getType().equals(_t[i])) {
            result = entry;
            break search;
          }
        }
      }
      return result;
    }

    public void push(ConstructId id) {
      this.nestedDeclarationContexts.push(new ContextStackEntry(id));
    }

    public void push(ContextStackEntry id) {
      this.nestedDeclarationContexts.push(id);
    }

    public ContextStackEntry pop() {
      return this.nestedDeclarationContexts.pop();
    }

    // note: returns true if either the stack or the pattern is empty!
    @Deprecated
    public boolean headMatches(JavaId.Type[] pattern) {
      boolean result = true;
      int patternElementIndex = 0;
      ContextStackEntry construct = null;
      Iterator<ContextStackEntry> itr = this.iterator();

      while (itr.hasNext() && patternElementIndex < pattern.length) {

        construct = itr.next();

        if (!pattern[patternElementIndex].equals(((JavaId) construct.getConstructId()).type)) {
          result = false;
          break;
        } else {
          patternElementIndex++;
        }
      }

      return result;
    }

    public int size() {
      return this.nestedDeclarationContexts.size();
    }

    public Deque<ContextStackEntry> all() {
      return this.nestedDeclarationContexts;
    }

    public Iterator<ContextStackEntry> iterator() {
      return this.nestedDeclarationContexts.iterator();
    }
  }
}
