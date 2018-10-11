from setuptools import setup, find_packages

setup(
    name='cf-helloworld',
    version='1.0',
    description='CF Python Hello World',
    install_requires=[
        'flask>=0.11',
        'gunicorn>=19.0'],
    packages=find_packages()
)
