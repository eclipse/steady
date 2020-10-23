from setuptools import setup, find_packages

setup(
    name='Vulas Python Test App',
    version='0.2',
    author='Grayden Hormes',
    author_email='grayden.hormes@sap.com',
    description='VULAS Test App for Python',
    #long_description=open('README.md').read(),
    classifiers=[
        "Development Status :: 1 - Alpha",
        'Intended Audience :: Developers',
        'Programming Language :: Python :: 2.7',
    ],
    install_requires = [ 'django==1.11.29' ],
    include_package_data = True,
    packages=find_packages(),
    entry_points = {
        #'console_scripts' : [ 'testapp = testapp:main']
    },
    #test_suite = 'testapp.run_tests.get_suite',

)
