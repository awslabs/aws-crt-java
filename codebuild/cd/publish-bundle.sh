#!/bin/sh

set -euo pipefail

# Send all diagnostic output to stderr
exec 3>&1  # Save stdout
exec 1>&2  # Redirect stdout to stderr for messages

# Configuration
BUNDLE_FILE="bundle.zip"
SONATYPE_API_URL="https://central.sonatype.com/api/v1/publisher/upload"

# Validate prerequisites
if [ -z "${AUTH_HEADER_VAL:-}" ]; then
    echo "Error: AUTH_HEADER_VAL environment variable is not set"
    exit 1
fi

if [ ! -f "${BUNDLE_FILE}" ]; then
    echo "Error: Bundle file '${BUNDLE_FILE}' not found"
    exit 1
fi

echo "Uploading bundle to Sonatype Central..."

# Upload bundle (verbose output goes to stderr)
DEPLOY_ID=$(curl \
  --fail \
  --verbose \
  --show-error \
  --request POST \
  --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
  --form "bundle=@${BUNDLE_FILE}" \
  "${SONATYPE_API_URL}" 2>&1) || {
    echo "Error: Failed to upload bundle to Sonatype Central"
    exit 1
}

echo "Successfully uploaded. Deployment ID: ${DEPLOY_ID}"

# Output only the DEPLOY_ID to original stdout for the caller
echo "${DEPLOY_ID}" | tee /tmp/deploy-id.txt
