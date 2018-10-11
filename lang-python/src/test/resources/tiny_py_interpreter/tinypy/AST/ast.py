#
# Some useful stuff here:
# http://greentreesnakes.readthedocs.org/en/latest/index.html
# https://docs.python.org/3/reference/expressions.html#calls
# https://docs.python.org/3/reference/executionmodel.html#naming
#
from enum import Enum


class AST(object):
    def eval(self):
        raise NotImplementedError()


""" Input types """

class Module(AST):
    def __init__(self, body:[]):
        super().__init__()
        self.body = body

    def eval(self):
        if type(self.body) is not list:
            self.body.eval()
        for stmt in self.body:
            stmt.eval()


class Interactive(AST):
    def __init__(self, body:[]):
        super().__init__()
        self.body = body

    def eval(self):
        if type(self.body) is not list:
            return self.body.eval()
        else:
            return [stmt.eval() for stmt in self.body]


class EvalExpression(AST):
    def __init__(self, body):
        super().__init__()
        self.body = body

    def eval(self):
        return self.body.eval()


""" Base node types """

class Expression(AST):
    def __init__(self):
        super().__init__()


class Statement(AST):
    def __init__(self):
        super().__init__()


""" Memory context for names, attributes, indexes, et.c. """
class MemoryContext(Enum):
    Load = 1
    Store = 2
    Del = 3
