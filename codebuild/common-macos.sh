#!/bin/bash

# Until CodeBuild supports macOS, this script is just used by Travis.

set -ex

mvn -B compile
