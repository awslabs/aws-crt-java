@echo off

@setlocal enableextensions enabledelayedexpansion

if exist mvn-build\cmake.properties (
    echo Using cached cmake generator from mvn-build\cmake.properties
    goto :EOF
)

:: Get the latest version of Visual Studio
vswhere -legacy -latest -property installationVersion
for /F "delims=" %%A in ('vswhere -legacy -latest -property installationVersion') do @(
    set VS_VERSION=%%A
)

:: strip minor version info
for /F "tokens=1 delims=." %%A in ("!VS_VERSION!") do @(
    set VS_VERSION=%%A
)

echo VS_VERSION=!VS_VERSION!

:: Get a list of Visual Studio generators from cmake, match against the version installed
for /F "delims=" %%A in ('cmake --help ^| findstr /C:"Visual Studio !VS_VERSION!"') do @(
    set GENERATOR=%%A
    goto :generator_found
)

:generator_found
echo GENERATOR=!GENERATOR!
:: Grab the generator name from the the name = description pair
for /F "tokens=1 delims==" %%A in ("!GENERATOR!") do (
    set GENERATOR_NAME=%%A
)

:: Trim leading whitespace
for /F "tokens=*" %%A in ("%GENERATOR_NAME%") do set TRIMMED=%%A
set CMAKE_VS_GENERATOR=%TRIMMED:[arch] =Win64%
:: write out maven properties file
if not exist mvn-build (
    mkdir mvn-build
)
echo cmake.generator=!CMAKE_VS_GENERATOR!>mvn-build\cmake.properties

@endlocal
