import os
import sys
import unittest
import subprocess
from subprocess import PIPE, STDOUT

in_directory = os.path.join(os.getcwd(), 'tinypy/tests')
proper_tests_dir  = in_directory
failing_tests_dir = os.path.join(in_directory, 'fail')
shell_tests_dir   = os.path.join(in_directory, 'shell')

python_binary = sys.executable
tinypy_binary = sys.executable + ' ' + os.path.join(os.getcwd(), 'test_entryp.py')

envs = dict(os.environ)
envs['COVERAGE_PROCESS_START'] = '.coveragerc'


class FileTestCase(unittest.TestCase):

    def __init__(self, file_path):
        super().__init__()
        self.file_path = file_path

    def __str__(self):
        return self.__class__.__name__ + ":" + os.path.basename(self.file_path)


class ProperParserTest(FileTestCase):

    def runTest(self):
        result = subprocess.run(
            tinypy_binary + " --parse " + self.file_path, stdout=PIPE, stderr=STDOUT,
            shell=True, universal_newlines=True, env=envs,
        )

        self.assertEqual(result.returncode, 0)
        self.assertTrue(not result.stdout)
        self.assertTrue(not result.stderr)


class FailingParserTest(FileTestCase):

    def runTest(self):
        result = subprocess.run(
            tinypy_binary + " --parse " + self.file_path, stdout=PIPE, stderr=STDOUT,
            shell=True, universal_newlines=True, env=envs,
        )

        self.assertNotEqual(result.returncode, 0)
        self.assertTrue(result.stdout != '')


class SemanticTest(FileTestCase):

    def runTest(self):
        tinypy_result = subprocess.run(
            tinypy_binary + ' -q ' + self.file_path, stdout=PIPE, stderr=STDOUT,
            shell=True, universal_newlines=True, env=envs
        )
        python_result = subprocess.run(
            python_binary + ' -q ' + self.file_path, stdout=PIPE, stderr=STDOUT,
            shell=True, universal_newlines=True
        )

        self.assertEqual(tinypy_result.returncode, python_result.returncode)
        self.assertEqual(tinypy_result.stdout, python_result.stdout)


class ShellTest(FileTestCase):

    def runTest(self):
        # Note that each process should have its own file handle
        with open(self.file_path) as fstdin:
            tinypy_result = subprocess.run(
                tinypy_binary + ' -q -i ', stdin=fstdin, stdout=PIPE, stderr=STDOUT,
                shell=True,  env=envs,
            )

        with open(self.file_path) as fstdin:
            python_result = subprocess.run(
                python_binary + ' -q -i ', stdin=fstdin, stdout=PIPE, stderr=STDOUT,
                shell=True,  env=envs,
            )

        self.assertEqual(tinypy_result.returncode, python_result.returncode)
        self.assertEqual(tinypy_result.stdout, python_result.stdout)


class EvalTest(unittest.TestCase):

    def __init__(self, expression):
        super().__init__()
        self.expression = expression

    def runTest(self):
        from tinypy.tinypyapp import tinypy_eval
        expected = eval(self.expression)
        actual = tinypy_eval(self.expression)
        self.assertEqual(expected, actual, msg=self.expression)

    def __str__(self):
        return self.__class__.__name__ + ":" + self.expression


class RedirectTest(SemanticTest):
    def __init__(self, filepath):
        super().__init__(' < ' + filepath)


def get_suite():
    suite = unittest.TestSuite()

    for file in os.listdir(proper_tests_dir):
        file_path = os.path.join(proper_tests_dir, file)

        if file.startswith("unicode") and os.name == 'nt':
            continue

        if file.endswith(".txt"):
            suite.addTest(ProperParserTest(file_path))

        elif file.endswith(".py"):
            suite.addTest(SemanticTest(file_path))

    for file in os.listdir(failing_tests_dir):
        if file.endswith(".txt"):
            file_path = os.path.join(failing_tests_dir, file)
            suite.addTest(FailingParserTest(file_path))

    for file in os.listdir(shell_tests_dir):
        file_path = os.path.join(shell_tests_dir, file)
        suite.addTest(ShellTest(file_path))

    suite.addTest(RedirectTest(os.path.join(proper_tests_dir, 'factorial.py')))

    evalTests = [
        EvalTest("0xDEADC0DE"),
        EvalTest("float( 1+3*(7-2) / min(3.0, 1.5) )"),
        EvalTest("'hey'"),
        EvalTest("print"),
        EvalTest("'hello' + 'world'"),
        EvalTest('max(1,10,3.0,4)'),
    ]
    suite.addTests(evalTests)

    return suite

if __name__ == '__main__':
    unittest.TextTestRunner().run(get_suite())
