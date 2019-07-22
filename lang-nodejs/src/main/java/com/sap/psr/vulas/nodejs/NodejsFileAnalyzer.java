package com.sap.psr.vulas.nodejs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.Construct;
import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.nodejs.antlr.javascript.JavaScriptParserBaseListener;
import com.sap.psr.vulas.nodejs.antlr.javascript.JavaScriptLexer;
import com.sap.psr.vulas.nodejs.antlr.javascript.JavaScriptParser;
import com.sap.psr.vulas.shared.util.DirUtil;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.StringUtil;

public class NodejsFileAnalyzer extends JavaScriptParserBaseListener implements FileAnalyzer {

    private final static Log log = LogFactory.getLog(NodejsFileAnalyzer.class);

    protected Map<ConstructId, Construct> constructs = null;

    /** Nested definitions. */
    protected final Stack<NodejsId> context = new Stack<NodejsId>();

    /** Scripting statements, i.e., all statements outside of functions and classes. */
    protected final List<String> stmts = new ArrayList<>();

    /** Number of times a given function has been defined in the given context. */
    protected final Map<NodejsId, Map<String, Integer>> countPerContext = new HashMap<NodejsId, Map<String, Integer>>();

    private final ConstructIdBuilder constructIdBuilder = new ConstructIdBuilder();

    private NodejsId module = null;

    private File file = null;


    @Override
    public String[] getSupportedFileExtensions() {
        return new String[] { "js" };
    }

    public NodejsFileAnalyzer() {
        super();
    }

    @Override
    public boolean canAnalyze(File _file) {
        final String ext = FileUtil.getFileExtension(_file);
        if(!FileUtil.isAccessibleFile(_file.toString()) || FileUtil.isAccessibleDirectory(_file))
            return false;
        if(ext == null || ext.equals(""))
            return false;
        for(String supported_ext: this.getSupportedFileExtensions()) {
            if(supported_ext.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }

    @Override
    public void analyze(File _file) throws FileAnalysisException {
        if(!FileUtil.isAccessibleFile(_file.toPath()))
            throw new IllegalArgumentException("[" + _file + "] does not exist or is not readable");
        this.file = _file;
    }

    /**
     * Returns true if the top-most element of the stack is of type {@link ConstructType#MODU}, false otherwise.
     */
    static boolean isTopOfType(Stack<NodejsId> _context, NodejsId.Type _type) {
        if(_context==null)
            throw new IllegalArgumentException("Stack argument is null");
        if(_context.isEmpty())
            return false;
        final NodejsId id = _context.peek();
        return id.getType().equals(_type);
    }

    /**
     * Returns true if the top-most element of the stack is of any of the given {@link NodejsId#Type}s, false otherwise.
     */
    static boolean isTopOfType(Stack<NodejsId> _context, NodejsId.Type[] _types) {
        if(_context==null)
            throw new IllegalArgumentException("Stack argument is null");
        if(_context.isEmpty())
            return false;
        final NodejsId id = _context.peek();
        for(NodejsId.Type t: _types)
            if(id.getType().equals(t))
                return true;
        return false;
    }

    /**
     * Enter a parse tree produced by {@link JavaScriptParser#statement}.
     * @param ctx the parse tree
     */
    @Override
    public void enterSourceElement(JavaScriptParser.SourceElementContext ctx) {
        // Get non-construct nodes.
        final String stmt = ctx.getText();
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.MODULE)
            || stmt.startsWith("function")
            || stmt.startsWith("class"))
            return ;
        stmts.add(stmt);
    }

    @Override
    public void enterClassDeclaration(JavaScriptParser.ClassDeclarationContext ctx) {
        // Happens if a class is anonymous.
        if(ctx.Identifier() == null)
            throw new IllegalStateException("Parser error: Class declaration without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");

        final String name = ctx.Identifier().toString();
        String parent_classes = "";
        if(ctx.classTail().singleExpression() != null){
            parent_classes = ctx.classTail().singleExpression().getText();
        }

        // Create construct and add to context
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.CLASS, name + "(" + parent_classes + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitClassDeclaration(JavaScriptParser.ClassDeclarationContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.CLASS))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.CLASS + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterClassExpression(JavaScriptParser.ClassExpressionContext ctx) {
        // If cannot find the name after keyword "class".
        String name = "";
        if(ctx.Identifier() == null) {
            // Declare a new variable as a class.
            if(ctx.getParent() instanceof JavaScriptParser.VariableDeclarationContext)
                name = ((JavaScriptParser.VariableDeclarationContext) ctx.getParent()).Identifier().getText();
            // Assign an existing variable as a class.
            else if(ctx.getParent() instanceof JavaScriptParser.AssignmentExpressionContext)
                name = ((JavaScriptParser.AssignmentExpressionContext) ctx.getParent()).singleExpression(0).getText();
            // Anon-class
            else
                name = constructIdBuilder.buildAnonymousName(this.context.peek());
        }
        else {
            name = ctx.Identifier().getText();
        }
        String parent_classes = "";
        if(ctx.classTail().Extends() != null)
            parent_classes = ctx.classTail().singleExpression().getText();

        // Create construct and add to context
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.CLASS, name + "(" + parent_classes + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitClassExpression(JavaScriptParser.ClassExpressionContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.CLASS))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.CLASS + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterObjectLiteralExpression(JavaScriptParser.ObjectLiteralExpressionContext ctx) {
        String name = "";
        // Declare a new variable as an object
        if(ctx.getParent() instanceof JavaScriptParser.VariableDeclarationContext) {
            name = ((JavaScriptParser.VariableDeclarationContext) ctx.getParent()).Identifier().getText();
        }
        // Assign an existing variable as an object
        else if(ctx.getParent() instanceof JavaScriptParser.AssignmentExpressionContext) {
            name = ((JavaScriptParser.AssignmentExpressionContext) ctx.getParent()).singleExpression(0).getText();
        }
        // Anon object
        else {
            name = constructIdBuilder.buildAnonymousName(this.context.peek());
        }

        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.OBJECT, name);
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitObjectLiteralExpression(JavaScriptParser.ObjectLiteralExpressionContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.OBJECT))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.OBJECT + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
        // Happens if a function is anonymous.
        if(ctx.Identifier() == null)
            throw new IllegalStateException("Parser error: Function declaration without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");

        final String name = ctx.Identifier().getText();
        String parameters = "";
        if(ctx.formalParameterList() != null)
            parameters = ctx.formalParameterList().getText();

        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.FUNCTION, name + "(" + parameters + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.FUNCTION))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.FUNCTION + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterFunctionExpression(JavaScriptParser.FunctionExpressionContext ctx) {
        String name = "";
        String parameters = "";

        // If cannot find the name after keyword "function".
        if(ctx.Identifier() == null) {
            // Declare a new variable as a function
            if(ctx.getParent() instanceof JavaScriptParser.VariableDeclarationContext) {
                name = ((JavaScriptParser.VariableDeclarationContext) ctx.getParent()).Identifier().getText();
            }
            // Assign an existing variable as a function
            else if(ctx.getParent() instanceof JavaScriptParser.AssignmentExpressionContext) {
                name = ((JavaScriptParser.AssignmentExpressionContext) ctx.getParent()).singleExpression(0).getText();
            }
            // Assign a property of object as a function
            else if(ctx.getParent() instanceof JavaScriptParser.PropertyExpressionAssignmentContext) {
                name = ((JavaScriptParser.PropertyExpressionAssignmentContext) ctx.getParent()).propertyName().getText();
            }
            // Anon-function
            else {
                name = constructIdBuilder.buildAnonymousName(this.context.peek());
            }
        }
        else {
            name = ctx.Identifier().getText();
        }
        if(ctx.formalParameterList() != null)
            parameters = ctx.formalParameterList().getText();
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.FUNCTION, name + "(" + parameters + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitFunctionExpression(JavaScriptParser.FunctionExpressionContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.FUNCTION))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.FUNCTION + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterArrowFunctionExpression(JavaScriptParser.ArrowFunctionExpressionContext ctx) {
        String name = "";
        String parameters = "";

        // Declare a new variable as a function
        if(ctx.getParent() instanceof JavaScriptParser.VariableDeclarationContext) {
            name = ((JavaScriptParser.VariableDeclarationContext) ctx.getParent()).Identifier().getText();
        }
        // Assign an existing variable as a function
        else if(ctx.getParent() instanceof JavaScriptParser.AssignmentExpressionContext) {
            name = ((JavaScriptParser.AssignmentExpressionContext) ctx.getParent()).singleExpression(0).getText();
        }
        // Assign a property of object as a function
        else if(ctx.getParent() instanceof JavaScriptParser.PropertyExpressionAssignmentContext) {
            name = ((JavaScriptParser.PropertyExpressionAssignmentContext) ctx.getParent()).propertyName().getText();
        }
        // Anon-function
        else {
            name = constructIdBuilder.buildAnonymousName(this.context.peek());
        }

        if(ctx.arrowFunctionParameters().formalParameterList() != null)
            parameters = ctx.arrowFunctionParameters().formalParameterList().getText();
        else if(ctx.arrowFunctionParameters().Identifier() != null) {
            parameters = ctx.arrowFunctionParameters().Identifier().getText();
        }
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.FUNCTION, name + "(" + parameters + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitArrowFunctionExpression(JavaScriptParser.ArrowFunctionExpressionContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.FUNCTION))
            log.error("Top most element in stack is not of type [" + NodejsId.Type.FUNCTION + "], line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterMethodDefinition(JavaScriptParser.MethodDefinitionContext ctx) {
        String name = "";
        if(ctx.getter() != null)
            name = "get@" + ctx.getter().propertyName().getText();
        else if(ctx.setter() != null)
            name = "set@" + ctx.setter().propertyName().getText();
        else if(ctx.propertyName() != null)
            name = ctx.propertyName().getText();
        else
            throw new IllegalStateException("Parser error: Method definition without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");
        String parameters = "";
        if(ctx.formalParameterList() != null)
            parameters = ctx.formalParameterList().getText();

        NodejsId id;
        if(name.equalsIgnoreCase("constructor"))
            id = new NodejsId(this.context.peek(), NodejsId.Type.CONSTRUCTOR, name + "(" + parameters + ")");
        else
            id = new NodejsId(this.context.peek(), NodejsId.Type.METHOD, name + "(" + parameters + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id, c);
        this.context.push(id);
    }

    @Override
    public void exitMethodDefinition(JavaScriptParser.MethodDefinitionContext ctx) {
        final NodejsId.Type[] types = new NodejsId.Type[] { NodejsId.Type.METHOD, NodejsId.Type.CONSTRUCTOR };
        if(!NodejsFileAnalyzer.isTopOfType(this.context, types))
            log.error("Top most element in stack is not of the following types [" + StringUtil.join((Object[])types, ", ") + "]" + ", line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterMethodProperty(JavaScriptParser.MethodPropertyContext ctx) {
        if(ctx.generatorMethod().Identifier() == null)
            throw new IllegalStateException("Parser error: Method property without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");
        final String name = ctx.generatorMethod().Identifier().getText();
        String parameters = "";
        if(ctx.generatorMethod().formalParameterList() != null)
            parameters = ctx.generatorMethod().formalParameterList().getText();
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.METHOD, name + "(" + parameters + ")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id,c);
        this.context.push(id);
    }

    @Override
    public void exitMethodProperty(JavaScriptParser.MethodPropertyContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.METHOD))
            log.error("Top most element in stack is not of the following types [" + NodejsId.Type.METHOD + "]" + ", line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterPropertyGetter(JavaScriptParser.PropertyGetterContext ctx) {
        if(ctx.getter() == null)
            throw new IllegalStateException("Parser error: Property getter without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");
        final String name = "get@" + ctx.getter().propertyName().getText();
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.METHOD, name + "()");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id,c);
        this.context.push(id);
    }

    @Override
    public void exitPropertyGetter(JavaScriptParser.PropertyGetterContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.METHOD))
            log.error("Top most element in stack is not of the following types [" + NodejsId.Type.METHOD + "]" + ", line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    @Override
    public void enterPropertySetter(JavaScriptParser.PropertySetterContext ctx) {
        if(ctx.setter() == null)
            throw new IllegalStateException("Parser error: Property setter without name in context " + this.context + ", line [" + ctx.getStart().getLine() + "]");
        final String name = "set@" + ctx.setter().propertyName().getText();
        final String parameter = ctx.Identifier().getText();
        final NodejsId id = new NodejsId(this.context.peek(), NodejsId.Type.METHOD, name + "(" + parameter +")");
        final Construct c = new Construct(id, ctx.getText());
        this.constructs.put(id,c);
        this.context.push(id);
    }

    @Override
    public void exitPropertySetter(JavaScriptParser.PropertySetterContext ctx) {
        if(!NodejsFileAnalyzer.isTopOfType(this.context, NodejsId.Type.METHOD))
            log.error("Top most element in stack is not of the following types [" + NodejsId.Type.METHOD + "]" + ", line [" + ctx.getStart().getLine() + "]");
        else
            context.pop();
    }

    public Map<ConstructId, Construct> getConstructs(InputStream m) throws FileAnalysisException, IOException, RecognitionException {
        final CharStream input = CharStreams.fromStream(m);
        final JavaScriptLexer lexer = new JavaScriptLexer(input);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final JavaScriptParser parser = new JavaScriptParser(tokens);
        final ParseTree root = parser.program();
        final ParseTreeWalker walker = new ParseTreeWalker();

        try {
            walker.walk(this, root);
        } catch(IllegalStateException ise) {
            throw new FileAnalysisException("Parser error", ise);
        }

        // Update module body after the parsing of the entire file
        if(this.stmts != null && this.stmts.size() > 0) {
            final StringBuffer b = new StringBuffer();
            for(String stmt: this.stmts)
                if(!stmt.trim().equals(""))
                    b.append(stmt);
            this.constructs.get(this.module).setContent(b.toString());
        }

        return this.constructs;
    }

    @Override
    public Map<ConstructId, Construct> getConstructs() throws FileAnalysisException {
        if(this.constructs == null) {
            try {
                this.constructs = new TreeMap<ConstructId, Construct>();

                // Create module and add to constructs
                this.module = NodejsFileAnalyzer.getModule(this.file);
                this.constructs.put(this.module, new Construct(this.module, ""));

                // Get package, if any, and add to constructs
                final NodejsId pack = this.module.getPackage();
                if(pack != null)
                    this.constructs.put(pack, new Construct(pack, ""));

                // User package and module as context
                if(pack != null)
                    this.context.push(pack);
                this.context.push(module);

                // Parse the file
                log.debug("Parsing [" + this.file + "]");
                try(FileInputStream fis = new FileInputStream(this.file)) {
                    this.getConstructs(fis);
                }
            } catch(FileNotFoundException e) {
                throw new FileAnalysisException(e.getMessage(), e);
            } catch(RecognitionException e) {
                throw new FileAnalysisException("ANTLR exception while analyzing class file [" + this.file.getName() + "]");
            } catch(IOException e) {
                throw new FileAnalysisException("IO exception while analyzing class file [" + this.file.getName() + "]");
            } catch(Exception e) {
                if(this.constructs.size() == 0)
                    throw new FileAnalysisException("Unexpected exception while analyzing class file [" + this.file.getName() + "]");
                else
                    log.error("Unable to retrieves every constructs: got [" + this.constructs.size() + "] constructs from [" + this.file.getName() + "]");
            }
        }
        return this.constructs;
    }

    @Override
    public boolean containsConstruct(ConstructId _id) throws FileAnalysisException {
        return this.constructs.containsKey(_id);
    }

    @Override
    public Construct getConstruct(ConstructId _id) throws FileAnalysisException {
        return this.constructs.get(_id);
    }

    /**
     * Maybe promote this method, which uses the shared type as argument, to the interface.
     * Alternatively, make all core-internal interfaces work with core types, not with shared
     * types. Example to be changed: SignatureFactory.
     */
    public Construct getConstruct(com.sap.psr.vulas.shared.json.model.ConstructId _id) throws FileAnalysisException {
        for(ConstructId cid: this.constructs.keySet())
            if(ConstructId.toSharedType(cid).equals(_id))
                return this.constructs.get(cid);
        return null;
    }

    @Override
    public boolean hasChilds() {
        return false;
    }

    @Override
    public Set<FileAnalyzer> getChilds(boolean _recursive) {
        return null;
    }

    @Override
    public String toString() {
        return "NodeJS parser for [" + this.file + "]";
    }

    /**
     * Creates a {@link NodejsId} of type {@link ConstructType#MODU} for the given js file.
     */
    public static NodejsId getModule(File _file) throws IllegalArgumentException {
        if(!FileUtil.hasFileExtension(_file.toPath(), new String[] { "js" })) {
            throw new IllegalArgumentException("Expected file with file extension [js], got [" + _file.toString() + "]");
        }

        final Path p = _file.toPath().toAbsolutePath();

        // Add file name w/o extension to qname components
        final String module_name = FileUtil.getFileName(p.toString(), false);

        // Search upwards until find package.json, and add directory names to the qname components
        final List<String> package_name = new ArrayList<String>();
        Path search_path = p.getParent();
        while(!DirUtil.containsFile(search_path.toFile(), "package.json") && search_path.getNameCount() > 1) {
            package_name.add(0, search_path.getFileName().toString());
            search_path = search_path.getParent();
        }
        // Get the root package's directory name
        package_name.add(0, search_path.getFileName().toString());

        // Create the package (if any), the module and return the latter
        NodejsId pack = null;
        if(!package_name.isEmpty())
            pack = new NodejsId(null, NodejsId.Type.PACKAGE, StringUtil.join(package_name, "."));
        return new NodejsId(pack, NodejsId.Type.MODULE, module_name);
    }

    class ConstructIdBuilder  {
        /**
         * Used to give numeric names to anonymous inner constructs.
         */
        private Map<ConstructId, Integer> anonymousConstructCounters = new HashMap<ConstructId, Integer>();

        private Integer incrementAnonymousCounter(ConstructId id) {
            Integer count = null;

            // Initialize if not done already
            if(!this.anonymousConstructCounters.containsKey(id))
                this.anonymousConstructCounters.put(id, 1);

            // Current value
            count = this.anonymousConstructCounters.get(id);

            // Increase by one
            this.anonymousConstructCounters.put(id, count+1);

            return count;
        }

        public String buildAnonymousName(NodejsId _ctx) {
            // Construct name of new construct
            final StringBuilder construct_name = new StringBuilder();
            return construct_name.append(this.incrementAnonymousCounter(_ctx).toString()).toString();
        }
    }
}
