#!/usr/bin/env bash
set -e
set -x

# Store the current working directory
original_dir=$(pwd)
cd ./crt/aws-lc || exit 1

# Get the current commit hash
current_commit=$(git rev-parse HEAD)

# Check if the current commit is from the FIPS branch
fips_branch="fips-2024-09-27"
if git merge-base --is-ancestor "$current_commit" "origin/$fips_branch"; then
    echo "Current aws-lc commit is from the '$fips_branch' branch"
    status=0
else
    echo "Error: Current aws-lc commit is not from the '$fips_branch' branch"
    status=1
fi

# Change back to the original working directory
cd "$original_dir" || exit 1

exit $status
