import sys, os, stat
import argparse, time
from enum import Enum

import antlr4
from antlr4.tree.Trees import Trees

from tinypy.AST.builder.Builder import CustomVisitor
from tinypy.parser.CST import CstFlattened, CstFiltered
from tinypy.parser.Errors import CustomErrorStrategy, CustomErrorListener
from tinypy.parser.CustomLexer import CustomLexer
from tinypy.parser.TinyPyParser import TinyPyParser
from tinypy.shell.shell import InteractiveShell


class InputType(Enum):
    File = 1
    SingleInput = 2
    Expression = 3


parserRuleFor = {
    InputType.File        : TinyPyParser.file_input,
    InputType.SingleInput : TinyPyParser.single_input,
    InputType.Expression  : TinyPyParser.eval_input,
}

visitorRuleFor = {
    InputType.File        : CustomVisitor.visitFile_input,
    InputType.SingleInput : CustomVisitor.visitSingle_input,
    InputType.Expression  : CustomVisitor.visitEval_input,
}


class EvalArguments:
    def __init__(self, cst=False, parse_tree=False, parse_only=False, print_timings=False):
        self.cst = cst
        self.parse_tree = parse_tree
        self.parse_only = parse_only
        self.print_timings = print_timings


def tinypy_eval(sourcecode:str, firstRule=InputType.Expression, args=None):
    if args == None:
        args = EvalArguments()

    totalTime = time.time()
    input_stream = antlr4.InputStream(sourcecode)

    # Instantiate an run generated lexer
    lexer = CustomLexer(input_stream)
    tokens = antlr4.CommonTokenStream(lexer)

    # Instantiate and run generated parser
    parser = TinyPyParser(tokens)
    parser._errHandler = CustomErrorStrategy()

    error_listener = CustomErrorListener()
    parser.addErrorListener(error_listener)

    # Traverse the parse tree
    parseTime = time.time()
    try:
        parse_tree = parserRuleFor[firstRule](parser)
    except Exception as e:
        return -1
    parseTime = time.time() - parseTime

    if error_listener.errors_encountered != 0:
        return -1

    # Print parse trees if need (full or flattened)
    if args.parse_tree:
        parseTreeString = Trees.toStringTree(parse_tree, recog=parser)
        print(parseTreeString)

    if args.cst:
        cst = CstFiltered(tree=parse_tree)
        print(cst)

    # Build an AST
    astBuildTime = time.time()

    visitor = CustomVisitor()
    ast = visitorRuleFor[firstRule](visitor, parse_tree)

    astBuildTime = time.time() - astBuildTime

    if ast == None:
        return -1

    if args.parse_only:
        return 0

    # Evaluate the AST we've built
    evalTime = time.time()
    try:
        evalResult = ast.eval()
    except BaseException as e:
        print(e.__class__.__name__ + ": " + str(e))
        return -1

    evalTime = time.time() - evalTime

    totalTime = time.time() - totalTime

    if args.print_timings:
        timings = [
            ('Parsing',         parseTime),
            ('Building an AST', astBuildTime),
            ('Evaluating',      evalTime),
            ('Total time',      totalTime),
            ('Etc', totalTime-parseTime-astBuildTime-evalTime)
        ]
        print("#"*80)
        for timing in timings:
            print((timing[0]+": %.3f ms") % (timing[1]*1000))

    if firstRule == InputType.Expression:
        return evalResult

    return 0


def main():
    argParser = argparse.ArgumentParser()
    argParser.add_argument('filename', type=str, nargs='?',
                           help='Path to the script file.')
    argParser.add_argument('-c', dest='eval_input', type=str,
                           help='Program passed in as string')
    argParser.add_argument('--cst', dest='cst', action='store_true',
                           help='Show flattened concreted syntax tree for the input (parse tree)')
    argParser.add_argument('--tokens',  dest='parse_tree',  action='store_true',
                           help='Show string representation of a parse tree for the input')
    argParser.add_argument('--parse', dest='parse_only', action='store_true',
                           help='Parse input without evaluating it.')
    argParser.add_argument('--timings', dest='print_timings', action='store_true',
                           help='Print time spend during parsing, building an AST and evaluating.')
    argParser.add_argument('-q', dest='ignore_greeting', action='store_true',
                           help="Don't print version and copyright messages on interactive startup")
    argParser.add_argument('-i', dest='force_promt', action='store_true',
                           help='forces a prompt even if stdin does not appear to be a terminal')
    #
    # Parse arguments
    #
    argParser.set_defaults(cst=False, parse_tree=False, tokens=False, parse=False, timings=False)
    args = argParser.parse_args()

    #
    # Check whether terminal is attached
    #
    isatty = True if sys.stdin.isatty() else False

    if args.filename == None and (isatty or args.force_promt) and not args.eval_input:
        shell = InteractiveShell(args)

        if not args.ignore_greeting:
            shell.print_greeting()

        shell.loop()

    if args.eval_input != None:
        firstRule = InputType.SingleInput
        content = args.eval_input
    else:
        firstRule = InputType.File

        if isatty:
            with open(args.filename) as file_contents:
                content = file_contents.read()
        else:
            content = ''.join(sys.stdin.readlines())

    content += '\n'
    retvalue = tinypy_eval(content, firstRule, args)
    exit(retvalue)
