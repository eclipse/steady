from setuptools import setup, find_packages

setup(
    name='TinyPy Interpreter',
    version='0.4',
    author='Max Malysh',
    author_email='iam@maxmalysh.com',
    description='Interpreter of a small Python subset I have made as a coursework. ',
    #long_description=open('README.md').read(),
    classifiers=[
        "Development Status :: 3 - Alpha",
        'Intended Audience :: Developers',
        'Programming Language :: Python :: 3.5',
    ],
    install_requires = [ 'setuptools-git' ],
    include_package_data = True,
    packages=find_packages(),
    entry_points = {
        'console_scripts' : [ 'tinypy = tinypy.tinypyapp:main']
    },
    test_suite = 'tinypy.run_tests.get_suite',

)
