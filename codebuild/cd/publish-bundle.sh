#!/bin/bash

set -ex
set -o pipefail # Make sure one process in pipe fail gets bubble up

# Configuration
BUNDLE_FILE="bundle.zip"
SONATYPE_API_UPLOAD_URL="https://central.sonatype.com/api/v1/publisher/upload"
SONATYPE_API_STATUS_URL="https://central.sonatype.com/api/v1/publisher/status"
MAX_WAIT_TIME=300  # Maximum wait time in seconds (5 minutes)
CHECK_INTERVAL=10   # Check status every 10 seconds

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

# Upload bundle and extract deployment ID from response
UPLOAD_RESPONSE=$(curl \
  --fail \
  --verbose \
  --show-error \
  --request POST \
  --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
  --form "bundle=@${BUNDLE_FILE}" \
  "${SONATYPE_API_UPLOAD_URL}") || {
    echo "Error: Failed to upload bundle to Sonatype Central"
    exit 1
}

# Extract deployment ID from response (assuming it returns just the ID)
DEPLOY_ID="${UPLOAD_RESPONSE}"

echo "Successfully uploaded. Deployment ID: ${DEPLOY_ID}"

# Wait for the deployment to be validated
echo "Waiting for deployment to be validated..."

elapsed_time=0
while [ $elapsed_time -lt $MAX_WAIT_TIME ]; do
    # Check deployment status
    STATUS_RESPONSE=$(curl \
        --silent \
        --request POST \
        --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
        "${SONATYPE_API_STATUS_URL}?id=${DEPLOY_ID}") || {
        echo "Error: Failed to check deployment status"
        exit 1
    }

    # Extract deploymentState from JSON response
    # Using grep and sed as a simple JSON parser
    DEPLOYMENT_STATE=$(echo "$STATUS_RESPONSE" | grep -o '"deploymentState":"[^"]*"' | sed 's/"deploymentState":"\([^"]*\)"/\1/')

    echo "Current deployment state: ${DEPLOYMENT_STATE}"

    if [ "$DEPLOYMENT_STATE" = "VALIDATED" ]; then
        echo "Deployment has been validated successfully!"
        break
    elif [ "$DEPLOYMENT_STATE" = "FAILED" ]; then
        echo "Error: Deployment validation failed"
        echo "Full response: $STATUS_RESPONSE"
        exit 1
    fi

    sleep $CHECK_INTERVAL
    elapsed_time=$((elapsed_time + CHECK_INTERVAL))
done

if [ $elapsed_time -ge $MAX_WAIT_TIME ]; then
    echo "Error: Timeout waiting for deployment validation"
    exit 1
fi


# promote the release if in promote mode, otherwise just drop the uploaded bundle
if [ "$PROMOTE_RELEASE" = "true" ]; then
    # Construct the publish URL with the deployment ID
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
    echo "Skipping publish step (testing mode). Drop the uploaded bundle."

    # Construct the publish URL with the deployment ID
    SONATYPE_API_PUBLISH_URL="https://central.sonatype.com/api/v1/publisher/deployment/${DEPLOY_ID}"

    echo "Publishing deployment ${DEPLOY_ID}..."

    curl \
      --fail \
      --verbose \
      --show-error \
      --request DELETE \
      --header "Authorization: Bearer ${AUTH_HEADER_VAL}" \
      "${SONATYPE_API_PUBLISH_URL}" || {
        echo "Error: Failed to publish deployment"
        exit 1
    }

    echo "Successfully published deployment"
fi
