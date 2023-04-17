# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0.

from builder.core.action import Action
import os
import pathlib
import sys
import json
import tempfile
import xml.etree.cElementTree as xml


class SetupCIFromFiles(Action):
    current_folder = None
    env_instance = None
    tmp_file_storage = []  # NOTE: This is needed to keep the tmp files alive

    def _is_same_os_as_string(self, os_string):
        our_platform = ""
        if sys.platform.startswith("freebsd") or sys.platform.startswith("linux"):
            our_platform = "linux"
        elif sys.platform.startswith("windows") or sys.platform.startswith("cygwin"):
            our_platform = "windows"
        elif sys.platform.startswith("darwin"):
            our_platform = "mac"
        return os_string == our_platform

    def _process_environment_variables(self, object_environment_variables):
        print("Starting to process all environment variables in file...")

        for item in object_environment_variables:

            ############################################################
            # PRE-PROCESSING
            ############################################################

            environment_name = None
            environment_value = None
            # If we need to write multiple environment variables
            # in a single environment variable request, then set
            # them in this dictionary
            environment_multi = {}

            if ('name' in item):
                environment_name = str(item['name'])
            else:
                print("[SKIPPED]: Invalid environment variable in file: variable is missing name")
                continue

            try:

                ############################################################
                # INPUT (starts with "input_")
                ############################################################

                # NOTE: These options WILL override each other if multiple are present.
                # Example: 'secret' overrides whatever value is in 'data' because 'secret' is AFTER 'data',
                # so if both are present, 'secret' will be what is used and not 'data'.

                # Puts whatever data is in the file directly into the environment_value.
                # Valid JSON:
                #   'input_data': <whatever input you want>
                if ('input_data' in item):
                    environment_value = str(item["input_data"])

                # Puts whatever data is in the given AWS Secret Name into the environment_value
                # Valid JSON:
                #   { 'input_secret': <AWS Secret Name Here> }
                if ('input_secret' in item):
                    try:
                        environment_value = self.env_instance.shell.get_secret(str(item['input_secret']))
                    except:
                        sys.exit(f"[FAIL] {environment_name} [Input Secret]: Exception ocurred trying to get secret")

                # Calls 'assume_role' on the given IAM role ARN and puts the access key, secret access key, and
                # session token in environment variables with the following names:
                # * <environment name in file file>_ACCCESS_KEY = role access key
                # * <environment name in file file>_SECRET_ACCCESS_KEY = role secret access key
                # * <environment name in file file>_SESSION_TOKEN = role session token
                # It will assign environment_value to "SUCCESS".
                #
                # Valid JSON:
                #    'input_role_arn': <AWS IAM role ARN here>
                if ('input_role_arn' in item):
                    try:
                        arn_credentials = self.get_arn_role_credentials(str(item["input_role_arn"]))
                        environment_value = "SUCCESS"
                        environment_multi[environment_name + "_ACCESS_KEY"] = arn_credentials[0]
                        environment_multi[environment_name + "_SECRET_ACCESS_KEY"] = arn_credentials[1]
                        environment_multi[environment_name + "_SESSION_TOKEN"] = arn_credentials[2]
                    except Exception as ex:
                        sys.exit(
                            f"[FAIL] {environment_name} [Input Role ARN]: Exception ocurred trying to get role ARN credentials")

                # The same as "input_role_arn" but instead of taking the IAM role arn directly, it instead
                # takes a AWS Secret Name and assumes the value in that secret is the IAM Role ARN to use and assume.
                # Valid JSON:
                #    'input_role_arn_secret': <AWS Secret Name containing IAM Role ARN here>
                if ('input_role_arn_secret' in item):
                    try:
                        input_role_arn = self.env_instance.shell.get_secret(str(item['input_role_arn_secret']))
                        arn_credentials = self.get_arn_role_credentials(input_role_arn)
                        environment_value = "SUCCESS"
                        environment_multi[environment_name + "_ACCESS_KEY"] = arn_credentials[0]
                        environment_multi[environment_name + "_SECRET_ACCESS_KEY"] = arn_credentials[1]
                        environment_multi[environment_name + "_SESSION_TOKEN"] = arn_credentials[2]
                    except:
                        sys.exit(
                            f"[FAIL] {environment_name} [Input Role ARN Secret]: Exception ocurred trying to get role ARN credentials")

                # Downloads the S3 file at the given URL and sets environment_value to the downloaded (temporary) file.
                # Valid JSON:
                #   'input_s3': <S3 URL Here>
                if ('input_s3' in item):
                    try:
                        tmp_file = tempfile.NamedTemporaryFile()
                        tmp_file.flush()
                        self.tmp_file_storage.append(tmp_file)
                        tmp_s3_filepath = tmp_file.name
                        self.copy_s3_file(str(item['input_s3']), tmp_s3_filepath)
                        environment_value = str(tmp_s3_filepath)
                    except Exception as ex:
                        sys.exit(f"[FAIL] {environment_name} [Input S3]: Exception ocurred trying to get S3 file")

                ############################################################
                # OS Condition (starts with "os_")
                ############################################################

                # Checks to see if the operating system is the same as the desired operating system(s) of the environment variable.
                # Valid JSON:
                #   'os_only': "windows" or "linux" or "mac" <add ',' for multiple. Example: "windows,linux">
                if ('os_only' in item):
                    try:
                        supported_systems = str(item["os_only"]).split(",")
                        is_supported = False
                        for os in supported_systems:
                            if (self._is_same_os_as_string(os.lower()) == True):
                                is_supported = True
                        if (is_supported == False):
                            print(f"[SKIP] {environment_name} [OS Only]: Not desired OS. Skipping...")
                            continue
                    except Exception as ex:
                        sys.exit(f"[FAIL] {environment_name} [OS Only]: Exception ocurred trying to only process on OS")

                # Checks to see if the platform is ARM. If it is, then it skips
                # Valid JSON:
                #   'os_arm_skip': <any value - the existence is what is checked>
                if ('os_arm_skip' in item):
                    if os.uname()[4][:3] == 'arm':
                        print(f"[SKIP] {environment_name} [OS No ARM]: OS is ARM. Skipping...")
                        continue

                # Checks to see if the platform is Codebuild. If the platform is NOT codebuild, then it skips.
                if ('os_codebuild_only' in item):
                    # List of Codebuild environment variables:
                    # https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html
                    if self.env_instance.shell.getenv("CODEBUILD_BUILD_ID") != None:
                        pass  # Do nothing!
                    else:
                        print(f"[SKIP] {environment_name} [OS Codebuild Only]: NOT running on Codebuild. Skipping...")
                        continue

                ############################################################
                # FILE (starts with "file_")
                ############################################################

                # Writes whatever is in environment_value to a temporary named file.
                # NOTE: The value you pass here doesn't matter, if it is present it WILL be written to a temporary file.
                # Valid JSON:
                #   'file_tmp': <whatever you want - its unused>
                if ('file_tmp' in item):
                    tmp_file = tempfile.NamedTemporaryFile()
                    # lgtm [py/clear-text-storage-sensitive-data]
                    tmp_file.write(str.encode(environment_value))
                    tmp_file.flush()
                    self.tmp_file_storage.append(tmp_file)
                    environment_value = tmp_file.name

            except Exception as ex:
                sys.exit(f"[FAIL] {environment_name}: Something threw an exception! "
                         "This is likely due to an invalid/incorrectly-formatted file. "
                         f"Exception: {ex}")

            ############################################################
            # POST-PROCESSING
            ############################################################
            if (environment_value == None):
                print(
                    f"[SKIPPED] {environment_name}: Invalid environment variable in file: No environment value could not be set")
                continue
            # Set the variable with quiet=true so we do NOT print anything secret to the console
            self.env_instance.shell.setenv(environment_name, environment_value, quiet=True)
            print(f"{environment_name}: Set successfully")
            # Set any additional ones
            for key, value in environment_multi.items():
                self.env_instance.shell.setenv(key, value, quiet=True)
                print(f"{key}: Set successfully")

        print("Finished processing all environment variables in file.")

    def _process_json_file(self, json_filepath):
        # Open the JSON file
        json_filepath_abs = pathlib.Path(json_filepath).resolve()
        json_file_data_raw = ""
        with open(json_filepath_abs, "r") as json_file:
            json_file_data_raw = json_file.read()
        # Load the JSON file
        try:
            json_data = json.loads(json_file_data_raw)
        except Exception as ex:
            sys.exit(f"[FAIL]: Exception ocurred trying parson JSON file with name {json_filepath}. Exception {ex}")

        # Process Environment Variables
        self._process_environment_variables(json_data)

    def _process_xml_file(self, xml_filepath):
        try:
            # Open the XML file
            xml_filepath_abs = pathlib.Path(xml_filepath).resolve()
            xml_file_data_raw = ""
            with open(xml_filepath_abs, "r") as xml_file:
                xml_file_data_raw = xml_file.read()
            # Load the XML file
            xml_data = xml.fromstring(xml_file_data_raw)

            # Convert to a list of dictionaries so it processes like JSON
            convert_list = []
            for element in xml_data:
                item = {}
                for sub_item in element:
                    item[sub_item.tag] = sub_item.text
                convert_list.append(item)

            # Process Environment Variables
            self._process_environment_variables(convert_list)

        except Exception as ex:
            sys.exit(f"[FAIL]: Exception ocurred trying parson XML file with name {xml_filepath}. Exception: {ex}")

    def copy_s3_file(self, s3_url, filename):
        cmd = ['aws', '--region', 'us-east-1', 's3', 'cp',
               s3_url, filename]
        self.env_instance.shell.exec(*cmd, check=True, quiet=True)

    def get_arn_role_credentials(self, role_arn):
        cmd = ["aws", "--region", "us-east-1", "sts", "assume-role",
               "--role-arn", role_arn, "--role-session", "CI_Test_Run"]
        result = self.env_instance.shell.exec(*cmd, check=True, quiet=True)
        result_json = json.loads(result.output)
        return [result_json["Credentials"]["AccessKeyId"], result_json["Credentials"]["SecretAccessKey"], result_json["Credentials"]["SessionToken"]]

    def run(self, env):
        # Get the executing folder
        self.current_folder = os.path.dirname(pathlib.Path(__file__).resolve())
        if sys.platform == "win32" or sys.platform == "cygwin":
            self.current_folder += "\\"
        else:
            self.current_folder += "/"

        # Cache the env
        self.env_instance = env

        # Get the file(s)
        for file in self.env_instance.config.get('CI_ENVIRONMENT_VARIABLE_FILES'):
            # Is this an S3 file? If so, then download it to a temporary file and execute it there
            if (file.startswith("s3://")):
                tmp_file = tempfile.NamedTemporaryFile()
                tmp_file.flush()
                self.tmp_file_storage.append(tmp_file)
                tmp_s3_filepath = tmp_file.name
                self.copy_s3_file(file, tmp_s3_filepath)
                if (os.path.exists(tmp_s3_filepath)):
                    # Is it a JSON file or XML file?
                    if (file.endswith(".json")):
                        print("Processing JSON file...")
                        self._process_json_file(tmp_s3_filepath)
                        print("Processed JSON file.")
                    elif (file.endswith(".xml")):
                        print("Processing XML file...")
                        self._process_xml_file(file)
                        print("Processed XML file.")
                    else:
                        sys.exit(f"Cannot parse file: S3 file given [{file}] has an unknown extension!")
                else:
                    sys.exit(f"Cannot parse JSON file: Error processing temporary file from S3")
            # otherwise it's just a normal file, so execute it
            else:
                if (os.path.exists(file) == False):
                    sys.exit(f"Cannot parse file: file given [{file}] does not point to a valid file")

                # Is it a JSON file or XML file?
                if (file.endswith(".json")):
                    print("Processing JSON file...")
                    self._process_json_file(file)
                    print("Processed JSON file.")
                elif (file.endswith(".xml")):
                    print("Processing XML file...")
                    self._process_xml_file(file)
                    print("Processed XML file.")
                else:
                    sys.exit(f"Cannot parse file: file given [{file}] has an unknown extension!")
