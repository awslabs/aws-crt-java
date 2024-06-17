import argparse
import sys
import os
import time
import datetime

import requests # - for uploading files
import boto3

parser = argparse.ArgumentParser(description="Utility script to upload and run Android Device tests on AWS Device Farm for CI")
parser.add_argument('--run_id', required=True, help="A unique number for each workflow run within a repository")
parser.add_argument('--run_attempt', required=True, help="A unique number for each attempt of a particular workflow run in a repository")
parser.add_argument('--project_arn', required=True, help="Arn for the Device Farm Project the apk will be tested on")
parser.add_argument('--device_pool_arn', required=True, help="Arn for device pool of the Device Farm Project the apk will be tested on")
parser.add_argument('--device_pool', required=True, help="Which device pool is being used for this test")

current_working_directory = os.getcwd()
build_file_location = current_working_directory + '/src/test/android/testapp/build/outputs/apk/debug/testapp-debug.apk'
test_file_location = current_working_directory + '/src/test/android/testapp/build/outputs/apk/androidTest/debug/testapp-debug-androidTest.apk'
test_spec_file_location = current_working_directory + '/src/test/android/testapp/instrumentedTestSpec.yml'

def main():
    args = parser.parse_args()
    run_id = args.run_id
    run_attempt = args.run_attempt
    project_arn = args.project_arn
    device_pool_arn = args.device_pool_arn
    device_pool = args.device_pool

    region = os.getenv('AWS_DEVICE_FARM_REGION')

    print("Beginning Android Device Farm Setup \n")

    # Create Boto3 client for Device Farm
    try:
        client = boto3.client('devicefarm', region_name=region)
    except Exception:
        print("Error - could not make Boto3 client. Credentials likely could not be sourced")
        sys.exit(-1)
    print("Boto3 client established")

    # Upload the crt library shell app to Device Farm
    upload_file_name = 'CI-' + run_id + '-' + run_attempt + '-' + device_pool + '.apk'
    print('Upload file name: ' + upload_file_name)

    # Prepare upload to Device Farm project
    create_upload_response = client.create_upload(
        projectArn=project_arn,
        name=upload_file_name,
        type='ANDROID_APP'
    )
    device_farm_upload_arn = create_upload_response['upload']['arn']
    device_farm_upload_url = create_upload_response['upload']['url']

    # Upload crt library shell app apk
    with open(build_file_location, 'rb') as f:
        data = f.read()
    r = requests.put(device_farm_upload_url, data=data)
    print('File upload status code: ' + str(r.status_code) + ' reason: ' + r.reason)
    device_farm_upload_status = client.get_upload(arn=device_farm_upload_arn)
    while device_farm_upload_status['upload']['status'] != 'SUCCEEDED':
        if device_farm_upload_status['upload']['status'] == 'FAILED':
            print('Upload failed to process')
            sys.exit(-1)
        time.sleep(1)
        device_farm_upload_status = client.get_upload(arn=device_farm_upload_arn)

    # Upload the instrumentation test package to Device Farm
    upload_test_file_name = 'CI-' + run_id + '-' + run_attempt + '-' + device_pool + 'tests.apk'
    print('Upload file name: ' + upload_test_file_name)

    # Prepare upload to Device Farm project
    create_upload_response = client.create_upload(
        projectArn=project_arn,
        name=upload_test_file_name,
        type='INSTRUMENTATION_TEST_PACKAGE'
    )
    device_farm_instrumentation_upload_arn = create_upload_response['upload']['arn']
    device_farm_instrumentation_upload_url = create_upload_response['upload']['url']

    # Upload instrumentation test package
    with open(test_file_location, 'rb') as f:
        data_instrumentation = f.read()
    r_instrumentation = requests.put(device_farm_instrumentation_upload_url, data=data_instrumentation)
    print('File upload status code: ' + str(r_instrumentation.status_code) + ' reason: ' + r_instrumentation.reason)
    device_farm_upload_status = client.get_upload(arn=device_farm_instrumentation_upload_arn)
    while device_farm_upload_status['upload']['status'] != 'SUCCEEDED':
        if device_farm_upload_status['upload']['status'] == 'FAILED':
            print('Upload failed to process')
            sys.exit(-1)
        time.sleep(1)
        device_farm_upload_status = client.get_upload(arn=device_farm_instrumentation_upload_arn)

    # Upload the test spec file to Device Farm
    upload_spec_file_name = 'CI-' + run_id + '-' + run_attempt + '-' + device_pool + 'test-spec.yml'
    print('Upload file name: ' + upload_spec_file_name)

    # Prepare upload to Device Farm project
    create_upload_response = client.create_upload(
        projectArn=project_arn,
        name=upload_spec_file_name,
        type='INSTRUMENTATION_TEST_SPEC'
    )
    device_farm_test_spec_upload_arn = create_upload_response['upload']['arn']
    device_farm_test_spec_upload_url = create_upload_response['upload']['url']

    # Default Instrumentation tests run on Device Farm result in detailed individual test breakdowns but comes
    # at the cost of the test suite running for up to two hours before completing. There is limited control for turning
    # off unnecessary features which generates an immense amount of traffic resulting in hitting Device Farm rate limits
    # A bare-bones test spec is used with instrumentation testing which will report a singular fail if any one test fails but
    # the resulting Test spec output file contains information on each unit test, whether they passed, failed, or were skipped.
    # Upload test spec yml
    with open(test_spec_file_location, 'rb') as f:
        data = f.read()
    r = requests.put(device_farm_test_spec_upload_url, data=data)
    print('File upload status code: ' + str(r.status_code) + ' reason: ' + r.reason)
    device_farm_upload_status = client.get_upload(arn=device_farm_test_spec_upload_arn)
    while device_farm_upload_status['upload']['status'] != 'SUCCEEDED':
        if device_farm_upload_status['upload']['status'] == 'FAILED':
            print('Upload failed to process')
            sys.exit(-1)
        time.sleep(1)
        device_farm_upload_status = client.get_upload(arn=device_farm_test_spec_upload_arn)

    print('scheduling run')
    schedule_run_response = client.schedule_run(
        projectArn=project_arn,
        appArn=device_farm_upload_arn,
        devicePoolArn=device_pool_arn,
        name=upload_file_name,
        test={
            'type': 'INSTRUMENTATION',
            'testPackageArn': device_farm_instrumentation_upload_arn,
            'testSpecArn': device_farm_test_spec_upload_arn
        },
        executionConfiguration={
            'jobTimeoutMinutes': 30
        }
    )

    device_farm_run_arn = schedule_run_response['run']['arn']

    run_start_time = schedule_run_response['run']['started']
    run_start_date_time = run_start_time.strftime("%m/%d/%Y, %H:%M:%S")
    print('run scheduled at ' + run_start_date_time)

    get_run_response = client.get_run(arn=device_farm_run_arn)
    while get_run_response['run']['result'] == 'PENDING':
        time.sleep(10)
        get_run_response = client.get_run(arn=device_farm_run_arn)

    run_end_time = datetime.datetime.now()
    run_end_date_time = run_end_time.strftime("%m/%d/%Y, %H:%M:%S")
    print('Run ended at ' + run_end_date_time + ' with result: ' + get_run_response['run']['result'])

    is_success = True
    if get_run_response['run']['result'] != 'PASSED':
        print('run has failed with result ' + get_run_response['run']['result'])
        is_success = False

    # If Clean up is not executed due to the job being cancelled in CI, the uploaded files will not be deleted
    # from the Device Farm project and must be deleted manually.

    # Clean up
    print('Deleting ' + upload_file_name + ' from Device Farm project')
    client.delete_upload(
        arn=device_farm_upload_arn
    )
    print('Deleting ' + upload_test_file_name + ' from Device Farm project')
    client.delete_upload(
        arn=device_farm_instrumentation_upload_arn
    )
    print('Deleting ' + upload_spec_file_name + ' from Device Farm project')
    client.delete_upload(
        arn=device_farm_test_spec_upload_arn
    )

    if is_success == False:
        print('Exiting with fail')
        sys.exit(-1)

    print('Exiting with success')

if __name__ == "__main__":
    main()