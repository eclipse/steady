import antlr4
from antlr4.tree.Tree import ParseTree

from tinypy.parser.TinyPyParser import TinyPyParser
from tinypy.parser.Utils import nameFor

useless_tokens = [
    TinyPyParser.NEWLINE,
    TinyPyParser.EOF,
    TinyPyParser.COLON,
    TinyPyParser.COLON,
    TinyPyParser.OPEN_PAREN,
    TinyPyParser.CLOSE_PAREN,
    TinyPyParser.IF,
    TinyPyParser.ELIF,
    TinyPyParser.ELSE,
]

useless_tokens = []

class CstFlattened:
    def __init__(self, parent=None, tree:ParseTree=None, children=None):
        self.payload = self.getPayload(tree)

        if children is None:
            children = []

        self.children = children

        if parent is None:
            self.walk(tree, self)
        else:
            parent.addToChildren(self)

    def addToChildren(self, what):
        self.children.append(what)

    # Determines the payload of this CST: a string in case it's an inner node (which
    # is the name of the parser rule), or a Token in case it is a leaf node.
    def getPayload(self, tree:ParseTree):
        if tree.getChildCount() == 0:
            # A leaf node: return the tree's payload, which is a Token.
            return tree.getPayload()
        else:
            # The name for parser rule `foo` will be `FooContext`. Strip `Context` and
            # lower case the first character.
            ruleNume = tree.__class__.__name__.replace('Context', '')
            return ruleNume

    # Fills this AST based on the parse tree.
    def walk(self, tree:ParseTree, ast):
        if tree.getChildCount() == 0:
            # We've reached a leaf. We must create a new instance of an AST because
            # the constructor will make sure this new instance is added to its parent's
            # child nodes.
            CstFlattened(ast, tree)
        elif tree.getChildCount() == 1:
            # We've reached an inner node with a single child: we don't include this in
            # our AST.
            self.walk(tree.getChild(0), ast)
        elif tree.getChildCount() > 1:
            for child in tree.getChildren():
                temp = CstFlattened(ast, child)
                if not isinstance(temp.payload, antlr4.Token):
                    # Only traverse down if the payload is not a Token.
                    self.walk(child, temp)

    #
    # Look for box-drawing characters here:
    # https://en.wikipedia.org/wiki/Box-drawing_character
    #
    def __str__(self, print_pos=False):
        result = ""
        firstStack = [self]
        childListStack = [firstStack]

        while len(childListStack) != 0:
            childStack = childListStack[-1]

            if len(childStack) == 0:
                childListStack.pop(-1)
            else:
                ast = childStack.pop(0)

                if isinstance(ast.payload, antlr4.Token):
                    token = ast.payload

                    position = ", at (%d, %d) " % (token.line, token.column) if print_pos else ""
                    caption = 'TOKEN[type: %s, text: %s%s]' % (
                        nameFor(token.type), token.text.replace('\n', '\\n'), position
                    )
                else:
                    caption = str(ast.payload)

                indent = ''

                for i in range(0, len(childListStack) - 1):
                    indent += '│       ' if len(childListStack[i]) > 0 else '        '

                result += indent
                result += '└──── ' if len(childStack) == 0 else '├──── '
                result += caption
                result += '\n'

                if len(ast.children) > 0:
                    children = []
                    for i in range(0, len(ast.children)):
                        children.append(ast.children[i])
                    childListStack.append(children)

        return result

class CstFiltered(CstFlattened):

    # Don't include NEWLINEs and EOFs in the Tree
    def walk(self, tree:ParseTree, ast):
        if tree.getChildCount() > 1:
            for child in tree.getChildren():
                # Don't add useless token to tree
                if isinstance(child.getPayload(), antlr4.Token) and child.symbol.type in useless_tokens:
                    continue
                temp = CstFlattened(ast, child)
                if not isinstance(temp.payload, antlr4.Token):
                    # Only traverse down if the payload is not a Token.
                    self.walk(child, temp)
        else:
            super().walk(tree, ast)
