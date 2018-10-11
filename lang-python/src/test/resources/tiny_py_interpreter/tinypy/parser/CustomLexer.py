import re

from antlr4.Token import CommonToken
from antlr4 import *

from tinypy.parser.Errors import IndentationErr
from tinypy.parser.TinyPyLexer import TinyPyLexer
from tinypy.parser.TinyPyParser import TinyPyParser

#
# https://docs.python.org/3/reference/lexical_analysis.html#indentation
# The indentation levels of consecutive lines are used to generate INDENT and DEDENT tokens, using a stack, as follows.
#
# Before the first line of the file is read, a single zero is pushed on the stack;
# this will never be popped off again. The numbers pushed on the stack will always be
# strictly increasing from bottom to top. At the beginning of each logical line, the line’s
# indentation level is compared to the top of the stack. If it is equal, nothing happens.
#
# If it is larger, it is pushed on the stack, and one INDENT token is generated.
#
# If it is smaller, it must be one of the numbers occurring on the stack;  all numbers on the stack
# that are larger are popped off, and for each number popped off a DEDENT token is generated.
#
# At the end of the file, a DEDENT token is generated for each number remaining on the stack
# that is larger than zero.
#
# The idea is the following:
#
# Whenever you match a line break in your lexer, optionally match one or more spaces.
# If there are spaces after the line break, compare the length of these spaces with the current indent-size.
# If it's more than the current indent size, emit an Indent token, if it's less than the current indent-size,
# emit a Dedent token and if it's the same, don't do anything.
#
# You'll also want to emit a number of Dedent tokens at the end of the file
# to let every Indent have a matching Dedent token.

class CustomLexer(TinyPyLexer):
    def __init__(self, input=None):
        super().__init__(input)
        self.tokens = []       # A queue where extra tokens are pushed on (see the NEWLINE lexer rule).
        self.indents = []      # The stack that keeps track of the indentation level.
        self.opened = 0        # The amount of opened braces, brackets and parenthesis.
        self.lastToken = None  # The most recently produced token.

    def emitToken(self, token:Token):
        self._token = token
        self.tokens.append(token)
        self.lastToken = token
        # import AST; print("%s at [%d, %d]" % (AST.nameFor(token.type), token.line, token.column))
        # super().emitToken(token)

    def nextToken(self):
        # return super().nextToken()
        # Check if the end-of-file is ahead and there are still some DEDENTS expected
        if self._input.LA(1) == Token.EOF and len(self.indents) != 0:
            # Remove any trailing EOF tokens from our buffer
            i = len(self.tokens) - 1
            while i >= 0:
                if self.tokens[i].type == TinyPyParser.EOF:
                    del self.tokens[i]
                i -= 1

            # First emit an extra line break that serves as the end of the statement
            self.emitToken(self.commonToken(TinyPyParser.NEWLINE, '\n'))

            # Now emit as much DEDENT tokens as needed
            while len(self.indents) != 0:
                self.emitToken(self.createDedent())
                self.indents.pop()

            # Put the EOF back on the token stream
            self.emitToken(self.commonToken(TinyPyParser.EOF, "<EOF>"))

        nextToken = super().nextToken()

        if nextToken.channel == Token.DEFAULT_CHANNEL:
            # Keep track of the last token on the default channel
            self.lastToken = nextToken

        return nextToken if len(self.tokens) == 0 else self.tokens.pop(0)

    def createDedent(self):
        dedent = self.commonToken(TinyPyParser.DEDENT, "")
        dedent.line = self.lastToken.line
        return dedent

    def commonToken(self, _type, text):
        stop = self.getCharIndex() - 1
        start = stop if text == "" else stop - len(text) + 1
        return CommonToken(self._tokenFactorySourcePair, _type, start=start, stop=stop)

    # Calculates the indentation of the provided spaces, taking the
    # following rules into account:
    #
    # "Tabs are replaced (from left to right) by one to eight spaces
    #  such that the total number of characters up to and including
    #  the replacement is a multiple of eight [...]"
    #
    #  -- https://docs.python.org/3.1/reference/lexical_analysis.html#indentation
    def getIndentationCount(self, spaces):
        count = 0
        for ch in spaces:
           if ch == '\t':
               count += 8 - (count % 8)
           else:
               count += 1
        return count


    def newLineAction(self):
        newLine = re.sub("[^\r\n]+", "", self.text)
        spaces  = re.sub("[\r\n]+",  "", self.text)

        next_codepoint    = self._input.LA(1)
        _next = chr(next_codepoint if next_codepoint >= 0 else 0)

        if self.opened > 0 or _next == '#' or _next == '\n' or _next == '\r':
            # If we're inside a list or an a blank line, ignore all indents,
            # dedents and line breaks.
            self.skip()
            return

        self.emitToken(self.commonToken(self.NEWLINE, newLine))

        indent = self.getIndentationCount(spaces)
        previous = self.indents[-1] if len(self.indents) != 0 else 0

        if indent == previous:
            # Skip indents of the same size as the present indent-size
            self.skip()
        elif indent > previous:
            self.indents.append(indent)
            self.emitToken(self.commonToken(TinyPyParser.INDENT, spaces))
        else:
            # Possibly emit more than 1 DEDENT token.
            while len(self.indents) != 0 and self.indents[-1] > indent:
                if indent not in self.indents and indent != 0:
                    raise IndentationErr(line=self.line)
                self.emitToken(self.createDedent())
                self.indents.pop()

        # Emit an additional newline (which we could have skipped) just before the EOF,
        # as it is needed for the shell mode (see single_input rule definition)
        prev_codepoint = self._input.LA(-1)
        pre_prev_codepoint = self._input.LA(-2)

        prev = chr(prev_codepoint if prev_codepoint >= 0 else 0)
        pre_prev = chr(pre_prev_codepoint if pre_prev_codepoint >= 0 else 0)

        if next_codepoint == Token.EOF:
            if (prev == '\n' or prev == '\r') and (pre_prev == '\n' or pre_prev =='\r'):
                self.emitToken(self.commonToken(self.NEWLINE, newLine))

    def atStartOfInput(self):
        return self.column == 0 and self.line == 1

