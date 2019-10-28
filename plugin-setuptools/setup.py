from setuptools import setup, find_packages
import os
import sys

# 'setup.py publish' shortcut.
if sys.argv[-1] == 'publish':
    os.system('rm dist/*')
    os.system('python setup.py bdist_wheel')
    os.system('twine upload -r pypi dist/*')
    sys.exit()

setup(
    name="vulnerability-assessment-tool-plugin-setuptools",
    version="3.1.7",
    packages=find_packages(),

    # Make sure to include the vulnerability-assessment-tool java CLI
    package_data={
        # If any package contains *.txt or *.rst files, include them:
        '': ['*.jar', '*.rst', '*.txt']
    },

    # Starts the wrapper
    entry_points={
        'console_scripts': [
            'vulas = vulas.wrapper:main'
        ],
        "distutils.commands": [
            "clean = vulas.clean_command:clean",
            "cleanSpace = vulas.cleanSpace_command:cleanSpace",
            "app = vulas.bom_command:bom",
            "report = vulas.report_command:report"
        ],
         "distutils.setup_keywords": [
             "debug = vulas.bom_command:assert_bool"
        ],
    },

    test_suite="vulas.tests.test_all"
)
