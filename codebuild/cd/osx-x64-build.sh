#!/bin/sh

set -ex

cd `dirname $0`/../..

mvn -B compile
