
:: This script ensures that the correct vcvarsall.bat has been run before cmake runs
:: otherwise it won't find the Visual Studio toolchain
@echo on
@setlocal enableextensions enabledelayedexpansion

set CMAKE_BINARIES=.

if not exist %CMAKE_BINARIES%\cmake.properties (
    echo "%CMAKE_BINARIES%\cmake.properties does not exist, please make sure vs_config.bat has run"
    goto :error
)

:: Read generator and Visual Studio version from cmake.properties
for /f "tokens=2 delims==" %%A in ('type %CMAKE_BINARIES%\cmake.properties ^| findstr /C:"cmake.generator"') do (
    set GENERATOR=%%A
)
for /f "tokens=2 delims==" %%A in ('type %CMAKE_BINARIES%\cmake.properties ^| findstr /C:"vs.version"') do (
    set VS_VERSION=%%A
) 
for /f "tokens=2 delims==" %%A in ('type %CMAKE_BINARIES%\cmake.properties ^| findstr /C:"vs.vcvarsall"') do (
    set VCVARSALL=%%A
)

if not exist !VCVARSALL! (
    echo "No appropriate vcvarsall.bat could be found for Visual Studio, existing environment will be used"
    goto :cmake
)

:: If the generator ends with Win64, do a 64 bit build, else 32 bit
set ISWIN64=!GENERATOR:"=!
set ISWIN64=!ISWIN64:~-5!
if [!ISWIN64!] == [Win64] (
    set ARCH=amd64
) else (
    set ARCH=x86
)

call !VCVARSALL! !ARCH!

:cmake
@echo on
cmake %* || goto :error
@echo off

@endlocal
goto :EOF

:error
@endlocal
exit /b 1
