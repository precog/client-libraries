#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

from distribute_setup import use_setuptools
use_setuptools()
from setuptools import setup

import reportgrid


setup(
    name = reportgrid.__app_name__.lower(),
    version = reportgrid.__version__,

    author = reportgrid.__author__,
    author_email = reportgrid.__author_email__,
    description = reportgrid.__description__,
    long_description = open(os.path.abspath(os.path.join(
                            os.path.dirname(__file__), 'README.rst'))).read(),
    url = reportgrid.__url__,

    keywords = 'reportgrid reporting api',
    classifiers = [
        "Development Status :: 3 - Alpha",
        "Environment :: Web Environment",
        "Intended Audience :: Developers",
        "License :: Free To Use But Restricted",
        "Natural Language :: English",
        "Operating System :: OS Independent",
        "Programming Language :: Python :: 2.6",
        "Topic :: Software Development :: Libraries :: Python Modules"
    ],
)