
@setlocal enableextensions enabledelayedexpansion

 :: install chocolatey
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
choco install jdk8 -y -params "installdir=c:\\jdk8"
choco install maven -y
call RefreshEnv.cmd
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
