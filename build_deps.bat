
@echo off

:: extensions allow mkdir to make intermediate directories
@setlocal enableextensions enabledelayedexpansion

:: Everything is relative to this script's directory
set HOME_DIR=%~dp0

:: where to have cmake put its binaries
set DEPS_DIR=%HOME_DIR%build\deps

:: where deps will be installed
set INSTALL_PREFIX=%DEPS_DIR%\install

:: whether or not to look for local sources for deps, they should be in
:: the same parent directory as this repo
:: TODO
::set PREFER_LOCAL_DEPS=0

:: all args go to cmake
set CMAKE_ARGS=%*
echo.%CMAKE_ARGS% | findstr /I /C:"/C" > NUL && (
    echo Cleaning deps directory
    rmdir /s /q %DEPS_DIR%
    :: strip /C out of the args
    set CMAKE_ARGS=%CMAKE_ARGS:/C=%
)

if not exist %DEPS_DIR% (
    mkdir %DEPS_DIR%
)

:: See if a generator was provided
echo.!CMAKE_ARGS! | findstr /C:"-G" >NUL && (
    echo Using supplied generator from command line: !CMAKE_ARGS!
) || (
    echo Getting cmake generator from cmake
    call find_vs_cmake_generator.bat
    for /F "tokens=2 delims==" %%A in (mvn-build\cmake.properties) do (
        set CMAKE_VS_GENERATOR=%%A
    )
    echo Using generator "!CMAKE_VS_GENERATOR!"
    set CMAKE_ARGS=-G"!CMAKE_VS_GENERATOR!" !CMAKE_ARGS!
)

call :install_dep aws-c-common
call :install_dep aws-c-io
call :install_dep aws-c-mqtt

@endlocal
goto :EOF

:install_dep
pushd %DEPS_DIR%
if not exist %~1 (
    git clone https://github.com/awslabs/%~1.git
)
cd %~1

if [%~2] == [] goto do_build
git checkout %~2

:do_build
cmake %CMAKE_ARGS% -DCMAKE_INSTALL_PREFIX=%INSTALL_PREFIX%
cmake --build . --target ALL_BUILD
cmake --build . --target INSTALL

popd
exit /b %errorlevel%
