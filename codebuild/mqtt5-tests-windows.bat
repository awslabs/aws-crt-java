
@echo off

@setlocal enableextensions enabledelayedexpansion

pushd %~dp0\..\

 :: install chocolatey
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
:: this will also install jdk8
choco install adoptopenjdk8 maven -y
:: Install Cmake
choco install cmake --installargs 'ADD_CMAKE_TO_PATH=System' -y
:: Try installing Visual studio via Chocolatey
choco install visualstudio2019community -y
choco install visualstudio2019-workload-nativedesktop -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%
:: Set the path:
echo VS160COMNTOOLS="/c/Program Files (x86)/Microsoft Visual Studio/2019/Community/Common7/Tools"

cd %CODEBUILD_SRC_DIR%
mvn install -DskipTests -Dmsvc.toolpath="C:/Program Files (x86)/Microsoft Visual Studio/2019/Community/Common7/Tools"
call ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/CodeBuildIotProdMQTT5EnvironmentVariables.txt us-east-1
mvn test -Dtest=Mqtt5ClientTest -DfailIfNoTests=false
call ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/CodeBuildIotProdMQTT5EnvironmentVariables.txt cleanup

