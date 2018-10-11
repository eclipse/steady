from tinypy.parser.TinyPyParser import TinyPyParser
from tinypy.parser.TinyPyVisitor import TinyPyVisitor

from tinypy import AST
from tinypy import runtime

class StmtVisitorMixin(TinyPyVisitor):

    #
    # Base statements
    #
    def visitSimple_stmt(self, ctx:TinyPyParser.Simple_stmtContext):
        statements = []

        for smallStmt in ctx.small_stmt():
            statement = self.visit(smallStmt)
            if statement != None:
                statements.append(statement)

        return statements

    #
    # Compound statements
    #
    def visitSuite(self, ctx:TinyPyParser.SuiteContext):
        if ctx.simple_stmt() != None:
            return self.visit(ctx.simple_stmt())

        statements = []

        for stmt in ctx.stmt():
            if stmt.simple_stmt() != None:
                statements += self.visit(stmt.simple_stmt())
            else:
                statements.append(self.visit(stmt))

        return statements


    def visitIf_stmt(self, ctx:TinyPyParser.If_stmtContext):
        test = self.visit(ctx.test())
        suite = self.visit(ctx.suite())
        orelse = []

        if ctx.if_else() != None:
            orelse = self.visit(ctx.if_else().suite())

        if ctx.if_elif() != None and len(ctx.if_elif()) >= 1:
            elifNodes = ctx.if_elif().copy()
            elifNodes.reverse()

            for node in elifNodes:
                nodeTest = self.visit(node.test())
                nodeSuite = self.visit(node.suite())

                orelse = [AST.stmt.IfStmt(test=nodeTest, body=nodeSuite, orelse=orelse)]

        return AST.stmt.IfStmt(test=test, body=suite, orelse=orelse)


    def visitWhile_stmt(self, ctx:TinyPyParser.While_stmtContext):
        test = self.visit(ctx.test())
        suite = self.visit(ctx.suite())

        return AST.stmt.WhileStmt(test=test, body=suite, orelse=[])


    def visitFor_stmt(self, ctx:TinyPyParser.For_stmtContext):
        expr = self.visit(ctx.nameaccess())
        test = self.visit(ctx.test())
        suite = self.visit(ctx.suite())

        return AST.stmt.ForStmt(target=expr, iter=test, body=suite)


    def visitFuncdef(self, ctx:TinyPyParser.FuncdefContext):
        name = ctx.NAME().getText()
        suite = self.visit(ctx.suite())

        param_ctx = ctx.parameters().param_argslist()
        params = []

        if param_ctx != None:
            for argName in param_ctx.NAME():
                params.append(argName.getText())

        return AST.stmt.FunctionDef(name=name, args=params, body=suite)

    #
    # Small statements
    #
    def visitExprStmtAssign(self, ctx:TinyPyParser.ExprStmtAssignContext):
        name = self.visit(ctx.nameaccess())
        expr = self.visit(ctx.test())

        return AST.stmt.AssignStmt(target=name, value=expr)


    def visitExprStmtAugmented(self, ctx:TinyPyParser.ExprStmtAugmentedContext):
        name = self.visit(ctx.nameaccess())
        value = self.visit(ctx.test())
        op = ctx.augassign().getText()

        return AST.stmt.AugAssignStmt(name=name, value=value, op=op)

    #
    # Control flow statements
    #
    def visitReturn_stmt(self, ctx:TinyPyParser.Return_stmtContext):
        test = None

        validParents = (TinyPyParser.FuncdefContext, )

        if not self.validContextParents(ctx, validParents):
            raise runtime.Errors.SyntaxError("'return' outside function")

        if ctx.test() != None:
            test = self.visit(ctx.test())

        return AST.stmt.ReturnStmt(expr=test)


    def visitPass_stmt(self, ctx:TinyPyParser.Pass_stmtContext):
        return AST.stmt.PassStmt()


    def visitBreak_stmt(self, ctx:TinyPyParser.Break_stmtContext):
        validParents = TinyPyParser.For_stmtContext, TinyPyParser.While_stmtContext

        if not self.validContextParents(ctx, validParents):
            raise runtime.Errors.SyntaxError("'break' outside loop")

        return AST.stmt.BreakStmt()


    def visitContinue_stmt(self, ctx:TinyPyParser.Continue_stmtContext):
        validParents = TinyPyParser.For_stmtContext, TinyPyParser.While_stmtContext

        if not self.validContextParents(ctx, validParents):
            raise runtime.Errors.SyntaxError("'continue' outside loop")

        return AST.stmt.ContinueStmt()

    #
    # Check whether context has one of the specified proper parents
    #
    def validContextParents(self, context, properParents:tuple):
        context = context.parentCtx

        while context != None:
            context = context.parentCtx
            if isinstance(context, properParents):
                return True

        return False