
set CMAKE_ARGS=%*

"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
REM this will also install jdk8
choco install maven -y && refreshenv
set JAVA_HOME=%~$PATH:javac
echo JAVA_HOME=%JAVA_HOME%

mkdir build\deps\install
set AWS_C_INSTALL=%cd%\build\deps\install

CALL :install_library aws-c-common
CALL :install_library aws-c-io
CALL :install_library aws-c-mqtt

cd aws-crt-java
mvn test || goto error

goto :EOF

:install_library
git clone https://github.com/awslabs/%~1.git
cd %~1

if [%~2] == [] GOTO do_build
git checkout %~2

:do_build
cmake %CMAKE_ARGS% -DCMAKE_INSTALL_PREFIX=%AWS_C_INSTALL%
cmake --build . --target ALL_BUILD
cmake --build . --target INSTALL
exit /b %errorlevel%

:error
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
