#!/bin/sh

set -ex
set -euo pipefail

# Configuration
BUNDLE_FILE="bundle.zip"
SONATYPE_API_UPLOAD_URL="https://central.sonatype.com/api/v1/publisher/upload"

# Check if promote release mode is enabled
PROMOTE_RELEASE="${PROMOTE_RELEASE:-false}"

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

# # Upload bundle
DEPLOY_ID=$(curl \
  --fail \
  --verbose \
  --show-error \
  --request POST \
  --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
  --form "bundle=@${BUNDLE_FILE}" \
  "${SONATYPE_API_UPLOAD_URL}" 2>&1) || {
    echo "Error: Failed to upload bundle to Sonatype Central"
    exit 1
}

echo "Successfully uploaded. Deployment ID: ${DEPLOY_ID}"


if [ "$PROMOTE_RELEASE" == "true" ]; then
    SONATYPE_API_PUBLISH_URL="https://central.sonatype.com/api/v1/publisher/deployment/${DEPLOY_ID}"

    echo "Publishing deployment ${DEPLOY_ID}..."

    curl \
      --fail \
      --verbose \
      --show-error \
      --request POST \
      --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
      "${SONATYPE_API_PUBLISH_URL}" || {
        echo "Error: Failed to publish deployment"
        exit 1
    }

    echo "Successfully published deployment"
else
    echo "Skipping publish step (testing mode). You need to manually drop/publish the deployment in Maven Central."
fi