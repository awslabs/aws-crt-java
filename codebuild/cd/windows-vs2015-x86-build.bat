
@setlocal enableextensions enabledelayedexpansion

echo PATH=%PATH%
echo JAVA_HOME=%JAVA_HOME%

set AWS_CMAKE_GENERATOR=Visual Studio 14 2015

mvn -X compile || goto error

for /f %%A in ('git describe --abbrev=0') do (
    set GIT_TAG=%%A
)

aws s3 cp --recursive .\mvn-build\lib s3://aws-crt-java-pipeline/%GIT_TAG%/lib

@endlocal
goto :EOF

:error
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
