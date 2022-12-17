
@setlocal enableextensions enabledelayedexpansion

echo PATH=%PATH%
echo JAVA_HOME=%JAVA_HOME%

set AWS_CMAKE_GENERATOR=Visual Studio 14 2015 Win64

git submodule update --init

for /f %%A in ('git describe --tags') do (
    set GIT_TAG=%%A
)

set PKG_VERSION=%GIT_TAG:~1,100%

mvn -X versions:set -DnewVersion=%PKG_VERSION%-SNAPSHOT

mvn -X install -DskipTests -P windows-64 || goto error

aws s3 cp --recursive --exclude "*" --include "*.dll" .\target\cmake-build\lib s3://aws-crt-java-pipeline/%GIT_TAG%/lib
aws s3 cp --recursive --exclude "*" --include "*.jar" .\target s3://aws-crt-java-pipeline/%GIT_TAG%/jar


@endlocal
goto :EOF

:error
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
