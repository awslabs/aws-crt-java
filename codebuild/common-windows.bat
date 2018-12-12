
set CMAKE_ARGS=%*

"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
REM this will also install jdk8
choco install maven -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

:: See if a generator was provided
echo.%CMAKE_ARGS% | findstr /C:"-G" >NUL && (
    echo "Using cmake generator from command line"
) || (
    echo "Getting cmake generator from cmake"
    call ..\find_vs_cmake_generator.bat
    CMAKE_ARGS=%CMAKE_ARGS% -G"%CMAKE_VS_GENERATOR%
)

mkdir build\deps\install
set AWS_C_INSTALL=%cd%\build\deps\install

CALL :install_library aws-c-common
CALL :install_library aws-c-io
CALL :install_library aws-c-mqtt

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
