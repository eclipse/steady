from antlr4.error.ErrorListener import ErrorListener, ProxyErrorListener
from antlr4.error.Errors import RecognitionException, ParseCancellationException, InputMismatchException
from antlr4.error.ErrorStrategy import DefaultErrorStrategy
from antlr4.Parser import Parser
from antlr4.Token import Token

from tinypy.parser.TinyPyParser import TinyPyParser


class IndentationErr(RecognitionException):
    def __init__(self, line):
        super().__init__(message="unindent does not match any outer indentation level")
        self.line = line


class SingleInputUnfinished(RecognitionException):
    def __init__(self):
        super().__init__()


class CustomErrorStrategy(DefaultErrorStrategy):

    def __init__(self):
        super().__init__()

    def reportError(self, recognizer:Parser, e:RecognitionException):
        if isinstance(e, IndentationErr):
            self.reportIndendationError(recognizer, e)
        elif isinstance(e, SingleInputUnfinished):
            super().reportError(recognizer, e)
        else:
            super().reportError(recognizer, e)

    def reportIndendationError(self, recognizer:Parser, e:IndentationErr):
        offendingToken = Token()
        offendingToken.line = e.line
        offendingToken.column = 0
        recognizer.notifyErrorListeners(e.message, offendingToken, e)


class CustomErrorListener(ProxyErrorListener):

    def __init__(self):
        super().__init__(delegates=[])
        self.errors_encountered = 0
        self.input_unfinished = False
        self.eof_received = False

    def syntaxError(self, recognizer, offendingSymbol, line, column, msg, e):
        self.errors_encountered += 1

        #
        # User just started compound statement (e.g. "if True:\n")
        #
        if offendingSymbol.type == Token.EOF:
            if isinstance(e, InputMismatchException):
                if isinstance(e.ctx, TinyPyParser.SuiteContext):
                    self.input_unfinished = True

        #
        # This happens when user have not entered the second newline in compound statement
        # TODO: handle this by using parser state (based on a rule context)
        #
        if offendingSymbol.type == Token.EOF and msg == "missing <INVALID> at '<EOF>'":
            self.input_unfinished = True

        if offendingSymbol.type == Token.EOF and line == 1 and column == 0:
            if isinstance(e.ctx , TinyPyParser.Single_inputContext):
                self.eof_received = True

        super().syntaxError(recognizer, offendingSymbol, line, column, msg, e)

    def addDelegatee(self, delegatee):
        self.delegates.append(delegatee)


class BufferedErrorListener(ErrorListener):

    def __init__(self):
        self.buffer = ""

    def syntaxError(self, recognizer, offendingSymbol, line, column, msg, e):
        self.buffer += ("line " + str(line) + ":" + str(column) + " " + msg) + '\n'

    def printBuffer(self):
        print(self.buffer)

