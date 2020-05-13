#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""The setup script."""

from setuptools import setup, find_packages

with open('README.md') as readme_file:
    readme = readme_file.read()

with open('HISTORY.md') as history_file:
    history = history_file.read()

with open('VERSION') as version_file:
    version = version_file.read()

requirements = [ 'plac' ,'tqdm', 'flask', 'requests' ]

setup_requirements = ['pytest-runner', ]

test_requirements = ['pytest', ]

setup(
    author="Antonino Sabetta",
    author_email='antonino.sabetta@sap.com',
    classifiers=[
        'Development Status :: 2 - Pre-Alpha',
        'Intended Audience :: Developers',
        'Natural Language :: English',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.4',
        'Programming Language :: Python :: 3.5',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: 3.7',
    ],
    description="RESTful API to serve CVE data from the NVD",
    install_requires=requirements,
    long_description=readme + '\n\n' + history,
    include_package_data=True,
    keywords='nvd',
    name='rest-nvd',
    packages=find_packages(include=['nvd_rest']),
    setup_requires=setup_requirements,
    test_suite='tests',
    tests_require=test_requirements,
    url='https://github.com/eclipse/steady/rest-nvd',
    version=version,
    zip_safe=False,
    entry_points = {
        'console_scripts': [
            'update = rest_nvd.app:main',
        ]
    }
)
