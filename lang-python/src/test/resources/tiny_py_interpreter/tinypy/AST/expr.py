from enum import Enum
import operator

from tinypy.AST.ast import Expression, MemoryContext
from tinypy import runtime


"""
# Binary arithmetic, bitwise and logic operations
"""
class BinOp(Expression):
    def __init__(self, left:Expression, right:Expression):
        super().__init__()
        self.left = left
        self.right = right

class AddOp(BinOp):
    def eval(self):
        return self.left.eval() + self.right.eval()

class SubOp(BinOp):
    def eval(self):
        return self.left.eval() - self.right.eval()

class MultOp(BinOp):
    def eval(self):
        return self.left.eval() * self.right.eval()

class DivOp(BinOp):
    def eval(self):
        left  = self.left.eval()
        right = self.right.eval()

        if right == 0:
            raise runtime.Errors.ZeroDivisionError()

        return left / right

class ModOp(BinOp):
    def eval(self):
        left  = self.left.eval()
        right = self.right.eval()

        if right == 0:
            raise runtime.Errors.ZeroDivisionError()

        return left % right

class LshiftOp(BinOp):
    def eval(self):
        return self.left.eval() << self.right.eval()

class RshiftOp(BinOp):
    def eval(self):
        return self.left.eval() >> self.right.eval()

class BitAndOp(BinOp):
    def eval(self):
        return self.left.eval() & self.right.eval()

class BitXorOp(BinOp):
    def eval(self):
        return self.left.eval() ^ self.right.eval()

class BitOrOp(BinOp):
    def eval(self):
        return self.left.eval() | self.right.eval()


"""
# Unary arithmetic operations
"""
class UnaryOp(Expression):
    def __init__(self, op, operand:Expression):
        super().__init__()
        self.op = op
        self.operand = operand

    def eval(self):
        if self.op == '+':
            return self.operand.eval()
        elif self.op == '-':
            return -(self.operand.eval())
        else:
            raise ValueError('Unsupported unary operation!')


"""
# Base class for comparisons.
"""
class Compare(Expression):

    class Op(Enum):
        AND = 1
        OR  = 2
        NOT = 3
        IN  = 4
        IS  = 5
        NOT_IN = 6
        IS_NOT = 7

    opTable = {
        '<'  : operator.lt,
        '>'  : operator.gt,
        '==' : operator.eq,
        '>=' : operator.ge,
        '<=' : operator.le,
        '!=' : operator.ne,
        Op.AND : operator.__and__,
        Op.OR  : operator.__or__,
        Op.NOT : operator.__not__,
        Op.IS  : operator.is_,
        Op.IS_NOT : operator.is_not,
    }

    def __init__(self, op):
        super().__init__()
        self.op = op

class BinaryComp(Compare):
    def __init__(self, left, right, op):
        super().__init__(op=op)
        self.left = left
        self.right = right

    def eval(self):
        left = self.left.eval()
        right = self.right.eval()

        if self.op == Compare.Op.IN:
            return left in right
        elif self.op == Compare.Op.NOT_IN:
            return left not in right

        return Compare.opTable[self.op](left, right)

class UnaryComp(Compare):
    def __init__(self, operand, op):
        super().__init__(op=op)
        self.operand = operand

    def eval(self):
        operand = self.operand.eval()
        return Compare.opTable[self.op](operand)


"""
# Represents None, False and True literals.
"""
class NameConstant(Expression):
    nameTable = { 'None' : None, 'True': True, 'False': False }

    def __init__(self, name):
        super().__init__()
        self.name = name

    def eval(self):
        try:
            return NameConstant.nameTable[self.name]
        except KeyError:
            raise ValueError("Wrong name constant")

"""
# A variable name.
#     @id holds the name as a string
#     @ctx is one of the following types: @Load / @Store / @Del
"""
class Name(Expression):

    def __init__(self, id, ctx:MemoryContext):
        super().__init__()
        self.id = id
        self.ctx = ctx

    def eval(self):
        if self.ctx == MemoryContext.Load:
            return self.getNamespace().get(name=self.id)
        elif self.ctx == MemoryContext.Store:
            return self.id
        else:
            raise NotImplementedError()

    def getNamespace(self):
        # Problem: we're very loosely coupled.
        return runtime.Memory.CurrentNamespace


"""
# Function call
#     @param func is the function, which will often be a Name object.
#     @args holds a list of the arguments passed by position.
"""
class CallExpr(Expression):
    def __init__(self, func, args):
        super().__init__()
        self.func = func   # name
        self.args = args

    def eval(self):
        func = self.func.eval()
        evalArgs = [ arg.eval() for arg in self.args ]
        return func(*evalArgs)


"""
# Base class for collections.
#   @value holds a collection, such as a list or a dict.
#
# This class delegates common collection methods  to the contained value.
"""
class CollectionContainer(Expression):
    def __init__(self, value):
        super().__init__()
        self.value = value

    def __repr__(self):
        return self.value.__repr__()

    def __iter__(self):
        return iter(self.value)

    def __getitem__(self, item):
        return self.value.__getitem__(item)

    def __setitem__(self, key, value):
        return self.value.__setitem__(key, value)

    def __len__(self):
        return self.value.__len__()


class ListContainer(CollectionContainer):
    def __init__(self, value:list):
        super().__init__(value)

    def eval(self):
        return ListContainer([value.eval() for value in self.value])

    def __add__(self, other):
        if type(other) is not ListContainer:
            msg = 'can only concatenate list to list'
            raise runtime.Errors.TypeError(msg)
        return ListContainer(self.value + other.value)

    def __mul__(self, other):
        return ListContainer(self.value.__mul__(other))

    def append(self, what):
        return self.value.append(what)


class TupleContainer(CollectionContainer):
    def __init__(self, value):
        super().__init__(value)

    def eval(self):
        return TupleContainer(tuple(value.eval() for value in self.value))

    def __add__(self, other):
        if type(other) is not TupleContainer:
            msg = 'can only concatenate tuple to tuple'
            raise runtime.Errors.TypeError(msg)
        return TupleContainer(self.value + other.value)

    def __mul__(self, other):
        return TupleContainer(self.value.__mul__(other))

class DictContainer(CollectionContainer):
    def __init__(self, value:dict):
        super().__init__(value)

    def copy(self):
        return DictContainer(self.value.copy())

    def update(self, right):
        return DictContainer(self.value.update(right.value))

    def eval(self):
        result = {}

        for key in self.value.keys():
            newKey = key.eval()
            newVal = self.value[key].eval()
            result[newKey] = newVal

        return result


class SetContainer(CollectionContainer):
    def __init__(self, value:set):
        super().__init__(value)

    def eval(self):
        result = set({})
        for item in self.value:
            result.add(item.eval())
        return SetContainer(result)

    def update(self, right):
        SetContainer(self.value.update(right))

"""
# Number literal
"""
class Num(Expression):

    def __init__(self, value):
        super().__init__()
        self.value = value

    def eval(self):
        return self.value

"""
# String literal
"""
class Str(Expression):
    def __init__(self, value):
        super().__init__()
        self.value = value

    def eval(self):
        return self.value