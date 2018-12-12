
set CMAKE_ARGS=%*

"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
REM this will also install jdk8
choco install maven vswhere -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

:: See if a generator was provided
echo.%CMAKE_ARGS% | findstr /C:"-G" >NUL || (
    echo No generator specified on command line, getting cmake generator from cmake
    call find_vs_cmake_generator.bat
    for /F "tokens=2 delims==" %%A in (mvn-build\cmake.properties) do (
        set CMAKE_VS_GENERATOR=%%A
    )
    echo Using generator "%CMAKE_VS_GENERATOR%"
    set CMAKE_ARGS=-G"%CMAKE_VS_GENERATOR%" %CMAKE_ARGS%
)

call build_deps.bat %CMAKE_ARGS%

mvn compile -X || goto error

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
