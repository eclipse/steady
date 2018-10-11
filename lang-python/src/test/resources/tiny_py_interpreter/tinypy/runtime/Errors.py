

class BaseRuntimeException(BaseException):
    def __init__(self, message):
        super().__init__(message)

class MemoryError(BaseRuntimeException):
    pass

class NameError(MemoryError):
    pass

class TypeError(BaseRuntimeException):
    pass

class ArithmeticError(BaseRuntimeException):
    pass

class ZeroDivisionError(ArithmeticError):
    def __init__(self):
        super().__init__("division by zero")

class SyntaxError(BaseRuntimeException):
    pass


class AttributeError(BaseRuntimeException):
    pass


class KeyError(BaseRuntimeException):
    pass


class IndexError(BaseRuntimeException):
    pass


class NotImplementedError(BaseRuntimeException):
    pass
