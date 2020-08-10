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
package com.sap.psr.vulas.python;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.logging.log4j.Logger;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.FileAnalyzerFactory;
import com.sap.psr.vulas.python.antlr.python3.Python3BaseListener;
import com.sap.psr.vulas.python.antlr.python3.Python3Lexer;
import com.sap.psr.vulas.python.antlr.python3.Python3Parser;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

// TODO: Decide what to do with default arg values in functions and methods? Right now, they are
// part of the qname, which is probably wrong.
/**
 * <p>Python3FileAnalyzer class.</p>
 *
 */
public class Python3FileAnalyzer extends Python3BaseListener implements FileAnalyzer {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger();

    protected Map<ConstructId, Construct> constructs = null;

    /** Nested definitions. */
    protected final Stack<PythonId> context = new Stack<PythonId>();

    /** Scripting statements, i.e., all statements outside of functions and classes. */
    protected final List<String> stmts = new ArrayList<String>();

    /** Number of times a given function has been defined in the given context. */
    protected final Map<PythonId, Map<String, Integer>> countPerContext =
            new HashMap<PythonId, Map<String, Integer>>();

    private PythonId module = null;

    private File file = null;

    /**
     * {@inheritDoc}
     *
     * Will not be instantiated by the {@link FileAnalyzerFactory}, but by {@link PythonFileAnalyzer}.
     */
    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] {};
    }

    /**
     * <p>Constructor for Python3FileAnalyzer.</p>
     */
    public Python3FileAnalyzer() {
        super();
    }

    /**
     * Sets context information in case an {@link InputStream} is parsed using {@link Python3FileAnalyzer#getConstructs(InputStream)}.
     * In this case, package and module information cannot be obtained from the file and file system.
     * The method is called by {@link PythonArchiveAnalyzer}.
     *
     * @param _module a {@link com.sap.psr.vulas.python.PythonId} object.
     * @param _pack a {@link com.sap.psr.vulas.python.PythonId} object.
     */
    public void setContext(PythonId _module, PythonId _pack) {
        this.constructs = new TreeMap<ConstructId, Construct>();
        if (_pack != null) {
            this.context.push(_pack);
            this.constructs.put(_pack, new Construct(_pack, ""));
        }
        this.context.push(_module);
        this.module = _module;
        this.constructs.put(module, new Construct(module, ""));
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
        if (!FileUtil.isAccessibleFile(_file.toPath()))
            throw new IllegalArgumentException("[" + _file + "] does not exist or is not readable");
        this.file = _file;
    }

    /**
     * {@inheritDoc}
     *
     * Enter a parse tree produced by {@link Python3Parser#stmt}.
     */
    @Override
    public void enterStmt(
            Python3Parser.StmtContext
                    ctx) { // TODO reads stmts before class defs and func defs are entered
        final String stmt = ctx.getText();
        if (!PythonFileAnalyzer.isTopOfType(this.context, PythonId.Type.MODULE)
                || stmt.startsWith("def")
                || stmt.startsWith("class")
        //		|| stmt.startsWith("import")
        //		|| stmt.startsWith("from")
        ) return;
        stmts.add(stmt);
    }

    /** {@inheritDoc} */
    @Override
    public void enterClassdef(Python3Parser.ClassdefContext ctx) {
        // Happens if class name is 'async', due to the Python grammar's problem with the ASYNC
        // keyword, cf. testPythonFileWithAsync
        if (ctx.NAME() == null)
            throw new IllegalStateException(
                    "Parser error: Class without name in context "
                            + this.context
                            + ", line ["
                            + ctx.getStart().getLine()
                            + "]");

        final String name = ctx.NAME().toString();
        String parent_classes = "";
        if (ctx.arglist() != null) parent_classes = ctx.arglist().getText().toString();

        // Create construct and add to context
        final PythonId id =
                new PythonId(
                        this.context.peek(),
                        PythonId.Type.CLASS,
                        name + "(" + parent_classes + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    /** {@inheritDoc} */
    @Override
    public void exitClassdef(Python3Parser.ClassdefContext ctx) {
        if (!PythonFileAnalyzer.isTopOfType(this.context, PythonId.Type.CLASS))
            log.error(
                    "Top most element in stack is not of type ["
                            + PythonId.Type.CLASS
                            + "], line ["
                            + ctx.getStart().getLine()
                            + "]");
        else context.pop();
    }

    /** {@inheritDoc} */
    @Override
    public void enterFuncdef(Python3Parser.FuncdefContext ctx) {
        // Happens if method or function name is 'async', due to the Python grammar's problem with
        // the ASYNC keyword, cf. testPythonFileWithAsync
        if (ctx.NAME() == null)
            throw new IllegalStateException(
                    "Parser error: Construct without name in context "
                            + this.context
                            + ", line ["
                            + ctx.getStart().getLine()
                            + "]");

        PythonId id;
        String name = ctx.NAME().toString();
        String args = ctx.parameters().getText();

        // Identical construct names can be used within a single context (e.g., the same function in
        // a module)
        name = this.getNameForCurrentContext(name);

        // New type depends on context type
        final PythonId.Type ctx_type = this.context.peek().getType();

        if (ctx_type == PythonId.Type.CLASS && name.equals("__init__"))
            id = new PythonId(this.context.peek(), PythonId.Type.CONSTRUCTOR, name + args);
        else if (ctx_type == PythonId.Type.CLASS)
            id = new PythonId(this.context.peek(), PythonId.Type.METHOD, name + args);
        else if (ctx_type == PythonId.Type.MODULE
                || ctx_type == PythonId.Type.FUNCTION
                || ctx_type == PythonId.Type.METHOD
                || ctx_type == PythonId.Type.CONSTRUCTOR)
            id = new PythonId(this.context.peek(), PythonId.Type.FUNCTION, name + args);
        else {
            log.error(
                    "Cannot create method, constructor or class due to wrong context: "
                            + this.context
                            + ", line ["
                            + ctx.getStart().getLine()
                            + "]");
            throw new IllegalStateException(
                    "Error when parsing ["
                            + this.file
                            + "]: Cannot create method or class due to wrong context: "
                            + this.context);
        }

        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    /** {@inheritDoc} */
    @Override
    public void exitFuncdef(Python3Parser.FuncdefContext _ctx) {
        final PythonId.Type[] types =
                new PythonId.Type[] {
                    PythonId.Type.FUNCTION, PythonId.Type.CONSTRUCTOR, PythonId.Type.METHOD
                };
        if (!PythonFileAnalyzer.isTopOfType(this.context, types))
            log.error(
                    "Top most element in stack is not of the following types ["
                            + StringUtil.join((Object[]) types, ", ")
                            + "]"
                            + ", line ["
                            + _ctx.getStart().getLine()
                            + "]");
        else context.pop();
    }

    /**
     *
     * @param _name
     * @return
     */
    private String getNameForCurrentContext(String _name) {
        final PythonId ctx = this.context.peek();

        if (this.countPerContext.get(ctx) == null)
            this.countPerContext.put(ctx, new HashMap<String, Integer>());

        if (this.countPerContext.get(ctx).get(_name) == null)
            this.countPerContext.get(ctx).put(_name, new Integer(0));

        int count = this.countPerContext.get(ctx).get(_name).intValue();
        this.countPerContext.get(ctx).put(_name, new Integer(++count));

        if (count == 1) return _name;
        else return _name + "$" + count;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
        return this.constructs.containsKey(_id);
    }

    /**
     * Maybe promote this method, which uses the shared type as argument, to the interface.
     * Alternatively, make all core-internal interfaces work with core types, not with shared
     * types. Example to be changed: SignatureFactory.
     *
     * @param _id a {@link com.sap.psr.vulas.shared.json.model.ConstructId} object.
     * @return a {@link com.sap.psr.vulas.Construct} object.
     * @throws com.sap.psr.vulas.FileAnalysisException if any.
     */
    public Construct getConstruct(com.sap.psr.vulas.shared.json.model.ConstructId _id)
            throws FileAnalysisException {
        for (ConstructId cid : this.constructs.keySet())
            if (ConstructId.toSharedType(cid).equals(_id)) return this.constructs.get(cid);
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
        return this.constructs.get(_id);
    }

    /**
     * <p>Getter for the field <code>constructs</code>.</p>
     *
     * @param m a {@link java.io.InputStream} object.
     * @return a {@link java.util.Map} object.
     * @throws com.sap.psr.vulas.FileAnalysisException if any.
     * @throws java.io.IOException if any.
     * @throws org.antlr.v4.runtime.RecognitionException if any.
     */
    public Map<ConstructId, Construct> getConstructs(InputStream m)
            throws FileAnalysisException, IOException, RecognitionException {
        final ANTLRInputStream input = new ANTLRInputStream(m);
        final Python3Lexer lexer = new Python3Lexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final Python3Parser parser = new Python3Parser(tokens);
        final ParseTree root = parser.file_input();
        final ParseTreeWalker walker = new ParseTreeWalker();

        try {
            walker.walk(this, root);
        } catch (IllegalStateException ise) {
            throw new FileAnalysisException(ise.getMessage(), ise);
        }

        // Update module body after the parsing of the entire file
        if (this.stmts != null && this.stmts.size() > 0) {
            final StringBuffer b = new StringBuffer();
            for (String stmt : this.stmts) if (!stmt.trim().equals("")) b.append(stmt);
            this.constructs.get(this.module).setContent(b.toString());
        }

        return this.constructs;
    }

    /** {@inheritDoc} */
    @Override
    public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
        if (this.constructs == null) {
            try {
                this.constructs = new TreeMap<ConstructId, Construct>();

                // Create module and add to constructs
                this.module = PythonFileAnalyzer.getModule(this.file);
                this.constructs.put(this.module, new Construct(this.module, ""));

                // Get package, if any, and add to constructs
                final PythonId pack = this.module.getPackage();
                if (pack != null) this.constructs.put(pack, new Construct(pack, ""));

                // Use package and module as context
                if (pack != null) this.context.push(pack);
                this.context.push(module);

                // Parse the file
                log.debug("Parsing [" + this.file + "]");
                try (FileInputStream fis = new FileInputStream(this.file)) {
                    this.getConstructs(fis);
                }
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
                        "IO exception while analysing class file ["
                                + this.file.getName()
                                + "]: "
                                + e.getMessage(),
                        e);
            }
        }
        return this.constructs;
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Python3 parser for [" + this.file + "]";
    }
}
