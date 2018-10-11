from tinypy.parser.TinyPyParser import TinyPyParser

#
# Converts Lisp-style s-expression string to python dictionary.
# Pretty-printing this helps to get a more meaningful representation of a parse tree
#
def sExprToDict(string):
    sexp = [[]]
    word = ''
    in_str = False
    for c in string:
        if c == '(' and not in_str:
            sexp.append([])
        elif c == ')' and not in_str:
            if(len(word) > 0):
                sexp[-1].append(word)
                word = ''
            temp = sexp.pop()
            sexp[-1].append(temp)
        elif c in (' ', '\n', '\t') and not in_str:
            sexp[-1].append(word)
            word = ''
        elif c == '\"':
            in_str = not in_str
        else:
            word = word + c
    return sexp[0]


def nameFor(tokenType:int):
    if tokenType == -1:
        return 'EOF'
    return TinyPyParser.symbolicNames[tokenType]
