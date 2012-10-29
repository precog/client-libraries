#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

from distribute_setup import use_setuptools
use_setuptools()
from setuptools import setup

import precog


setup(
    name = precog.__app_name__.lower(),
    version = precog.__version__,

    author = precog.__author__,
    author_email = precog.__author_email__,
    description = precog.__description__,
    long_description = open(os.path.abspath(os.path.join(
                            os.path.dirname(__file__), 'README.rst'))).read(),
    url = precog.__url__,

    keywords = 'precog reporting api',
    classifiers = [
        "Development Status :: 3 - Alpha",
        "Environment :: Web Environment",
        "Intended Audience :: Developers",
        "License :: Free To Use But Restricted",
        "Natural Language :: English",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 2.7",
        "Topic :: Software Development :: Libraries :: Python Modules"
    ],
)