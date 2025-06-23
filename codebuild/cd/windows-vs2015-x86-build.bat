
@setlocal enableextensions enabledelayedexpansion

echo PATH=%PATH%
echo JAVA_HOME=%JAVA_HOME%

set AWS_CRT_WINDOWS_SDK_VERSION=10.0.17763.0
@REM The last Windows 10 SDK version that VS 2015 can target is 10.0.14393.0. 
@REM To target 10.0.17763.0, we need to use the VS 2017 and later generator.
@REM "VS 2015 Users: The Windows 10 SDK (15063, 16299, 17134, 17763) is
@REM officially only supported for VS 2017." From:
@REM https://blogs.msdn.microsoft.com/chuckw/2018/10/02/windows-10-october-2018-update/
set AWS_CMAKE_GENERATOR=Visual Studio 15 2017

git submodule update --init

for /f %%A in ('git describe --tags') do (
    set GIT_TAG=%%A
)

mvn -X install -DskipTests -Dcrt.classifier=windows-x86_32 || goto error

aws s3 cp --recursive --exclude "*" --include "*.dll" .\target\cmake-build\lib s3://aws-crt-java-pipeline/%GIT_TAG%/lib
aws s3 cp --recursive --exclude "*" --include "*.jar" .\target s3://aws-crt-java-pipeline/%GIT_TAG%/jar


@endlocal
goto :EOF

:error
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
