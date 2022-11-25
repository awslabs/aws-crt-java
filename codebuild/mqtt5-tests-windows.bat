
@echo off

@setlocal enableextensions enabledelayedexpansion

pushd %~dp0\..\

 :: install chocolatey
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
:: this will also install jdk8
choco install adoptopenjdk8 maven -y
:: Install Cmake
choco install cmake --installargs 'ADD_CMAKE_TO_PATH=System' -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

cd %CODEBUILD_SRC_DIR%
mvn install -DskipTests
call ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/CodeBuildIotProdMQTT5EnvironmentVariables.txt us-east-1
mvn test -Dtest=Mqtt5ClientTest -DfailIfNoTests=false
call ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/CodeBuildIotProdMQTT5EnvironmentVariables.txt cleanup

