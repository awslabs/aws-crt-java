
set CMAKE_ARGS=%*

"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
REM this will also install jdk8
choco install maven vswhere -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

call build_deps.bat %CMAKE_ARGS%

mvn test || goto error

goto :EOF

:install_library
git clone https://github.com/awslabs/%~1.git
pushd %~1

if [%~2] == [] GOTO do_build
git checkout %~2

:do_build
cmake %CMAKE_ARGS% -DCMAKE_INSTALL_PREFIX=%AWS_C_INSTALL%
cmake --build . --target ALL_BUILD
cmake --build . --target INSTALL
popd
exit /b %errorlevel%

:error
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
