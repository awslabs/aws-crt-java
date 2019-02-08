
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
    echo Using cached cmake generator from mvn-build\cmake.properties
)
for /f "tokens=2 delims==" %%A in ('type mvn-build\cmake.properties ^| findstr /C:"vs.version"') do (
    set VS_VERSION=%%A
) 

:: Get the right version of Visual Studio
if [!VS_VERSION!] EQU [14] (
    if exist "C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\vcvarsall.bat" (
        set VCVARSALL="C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\vcvarsall.bat"
    )
)

if [!VS_VERSION!] EQU [15] (
    if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvarsall.bat" (
        set VCVARSALL="C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvarsall.bat"
    )
    if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\VC\Auxiliary\Build\vcvarsall.bat" (
        set VCVARSALL="C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\VC\Auxiliary\Build\vcvarsall.bat"
    ) 
    if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Enterprise\VC\Auxiliary\Build\vcvarsall.bat" (
        set VCVARSALL="C:\Program Files (x86)\Microsoft Visual Studio\2017\Enterprise\VC\Auxiliary\Build\vcvarsall.bat"
    )
)

if not exist !VCVARSALL! (
    echo "No appropriate vcvarsall.bat could be found for Visual Studio, existing environment will be used"
    goto :cmake
)

:: If the generator ends with Win64, do a 64 bit build, else 32 bit
set ISWIN64=!GENERATOR:~-5!

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
