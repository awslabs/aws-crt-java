
@setlocal enableextensions enabledelayedexpansion

pushd %~dp0\..\

 :: install chocolatey
"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
:: this will also install jdk8
choco install maven -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

mvn -B compile || goto error

popd
@endlocal
goto :EOF

:error
popd
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
