#!/bin/bash

# Any errors should make this fail
set -e

mvn -f java/pom.xml test

pushd python && py.test && popd
pushd ruby && rake test && popd

