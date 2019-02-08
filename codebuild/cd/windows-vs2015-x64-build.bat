
@setlocal enableextensions enabledelayedexpansion

echo JAVA_HOME=%JAVA_HOME%

set AWS_CMAKE_GENERATOR="Visual Studio 14 2015 Win64"

mvn -X compile || goto error

mkdir ..\dist
xcopy /S /F .\mvn-build\lib ..\dist\lib\

@endlocal
goto :EOF

:error
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
