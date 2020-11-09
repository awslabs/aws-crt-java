#!/usr/bin/env bash

# Examples:
# ./dockcross-build.sh build -p aws-crt-java --target=linux-armv6
# ./dockcross-build.sh build -p aws-crt-java --target=linux-armv7
# ./dockcross-build.sh build -p aws-crt-java --target=linux-aarch64

set -ex

args=("$@")

version=LATEST
if [[ "${args[0]}" == "--version="* ]]; then
    version=${args[0]}
    version=$(echo $version | cut -f2 -d=)
    args=${args[@]:1}
fi

if [ $(echo $version | grep -E '^v[0-9\.]+$') ]; then
    version=releases/$version
elif [[ $version != 'channels/'* ]] && [[ $version != 'LATEST' ]]; then
    version=channels/$version
fi

# download the version of builder requested
builder=/tmp/builder.pyz
curl -sSL -o $builder --retry 3 https://d19elf31gohf1l.cloudfront.net/${version}/builder.pyz?date=`date +%s`
chmod a+x $builder

# on manylinux, use the latest python3 via symlink
if [ -x /opt/python/cp39-cp39/bin/python ] && [ ! -e /usr/local/bin/python3 ]; then
    ln -s /opt/python/cp39-cp39/bin/python /usr/local/bin/python3
fi

# Launch the builder with whatever args were passed to this script
$builder ${args[@]}
