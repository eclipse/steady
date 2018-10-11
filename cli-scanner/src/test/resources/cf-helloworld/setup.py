from setuptools import setup, find_packages


setup(
    name='cf-helloworld',
    version='1.0',
    description='Minimalistic CF python application',
    author='Roman Kindruk',
    author_email='roman.kindruk@sap.com',
    install_requires=[
        'flask>=0.11',
        'gunicorn>=19.0'],
    packages=find_packages()
)
