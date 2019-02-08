
:: This script ensures that the correct vcvarsall.bat has been run before cmake runs
:: otherwise it won't find the Visual Studio toolchain
@echo off
@setlocal enableextensions enabledelayedexpansion

pushd %~dp0

if not exist mvn-build\cmake.properties (
    echo "mvn-build\cmake.properties does not exist, please make sure find_vs_cmake_generator has run"
    goto :error
)

:: Read generator and Visual Studio version from cmake.properties
for /f "tokens=2 delims==" %%A in ('type mvn-build\cmake.properties ^| findstr /C:"cmake.generator"') do (
    set GENERATOR=%%A
)
for /f "tokens=2 delims==" %%A in ('type mvn-build\cmake.properties ^| findstr /C:"vs.version"') do (
    set VS_VERSION=%%A
) 
for /f "tokens=2 delims==" %%A in ('type mvn-build\cmake.properties ^| findstr /C:"vs.vcvarsall"') do (
    set VCVARSALL=%%A
)

if not exist !VCVARSALL! (
    echo "No appropriate vcvarsall.bat could be found for Visual Studio, existing environment will be used"
    goto :cmake
)

:: If the generator ends with Win64, do a 64 bit build, else 32 bit
set ISWIN64=!GENERATOR:"=!
set ISWIN64=!ISWIN64:~-5!
if [%ISWIN64%] == [Win64] (
    set ARCH=amd64
) else (
    set ARCH=x86
)

call !VCVARSALL! !ARCH!

:cmake
popd
@echo on
cmake %*
@echo off

@endlocal
goto :EOF

:error
popd
@endlocal
