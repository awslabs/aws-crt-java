import os
import boto3
from botocore.exceptions import ClientError

cwd = os.getcwd()

def getSecretAndSaveToFile(client, secretName, fileName):
    try:
        secret_value_response = client.get_secret_value(
            SecretId=secretName
            )
    except ClientError as e:
        print("Error encountered")
        if e.response['Error']['Code'] == 'ResourceNotFoundException':
            print("The requested secret " + secretName + " was not found")
        elif e.response['Error']['Code'] == 'InvalidRequestException':
            print("The request was invalid due to:", e)
        elif e.response['Error']['Code'] == 'InvalidParameterException':
            print("The request had invalid params:", e)
        elif e.response['Error']['Code'] == 'DecryptionFailure':
            print("The requested secret can't be decrypted using the provided KMS key:", e)
        elif e.response['Error']['Code'] == 'InternalServiceError':
            print("An error occurred on service side:", e)
    else:
        if 'SecretString' in secret_value_response:
            secret_file = open(cwd + "/" + fileName + ".txt", "w")
            secret_file.write(secret_value_response['SecretString'])
            secret_file.close()
            print(fileName + ".txt file created")
        else:
            print("SecretString not found in response")

def main():
    print("Setting up Android test assets")

    session = boto3.session.Session()

    try:
        client = session.client(
            service_name='secretsmanager',
            region_name='us-east-1'
        )
    except Exception:
        print("Error - could not make Boto3 client.")

    print("Boto3 client created")

    getSecretAndSaveToFile(client, "unit-test/endpoint", "AWS_TEST_MQTT5_IOT_CORE_HOST")
    getSecretAndSaveToFile(client, "aws-c-auth-testing/cognito-identity", "AWS_TEST_MQTT5_COGNITO_IDENTITY")
    getSecretAndSaveToFile(client, "ci/mqtt5/us/x509/endpoint", "AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT")
    getSecretAndSaveToFile(client, "ci/mqtt5/us/Mqtt5Prod/cert", "AWS_TEST_MQTT5_IOT_CORE_RSA_CERT")
    getSecretAndSaveToFile(client, "ci/mqtt5/us/Mqtt5Prod/key", "AWS_TEST_MQTT5_IOT_CORE_RSA_KEY")
    getSecretAndSaveToFile(client, "unit-test/certificate", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT")
    getSecretAndSaveToFile(client, "unit-test/privatekey-p8", "AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY")
    getSecretAndSaveToFile(client, "ecc-test/certificate", "AWS_TEST_MQTT311_IOT_CORE_ECC_CERT")
    getSecretAndSaveToFile(client, "ecc-test/privatekey", "AWS_TEST_MQTT311_IOT_CORE_ECC_KEY")
    getSecretAndSaveToFile(client, "unit-test/rootca", "AWS_TEST_MQTT311_ROOT_CA")

    getSecretAndSaveToFile(client, "X509IntegrationTestRootCA", "AWS_TEST_MQTT5_IOT_CORE_X509_CA")
    getSecretAndSaveToFile(client, "X509IntegrationTestPrivateKey", "AWS_TEST_MQTT5_IOT_CORE_X509_KEY")
    getSecretAndSaveToFile(client, "X509IntegrationTestCertificate", "AWS_TEST_MQTT5_IOT_CORE_X509_CERT")

    # AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY
    # AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY
    # AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN

    print("Android test asset creation complete")


if __name__ == "__main__":
    main()