@echo off

:: Get the latest version of Visual Studio
for /F "delims=" %%A in ('vswhere -legacy -latest -property installationVersion') do @(
    set VS_VERSION=%%A
)

:: strip minor version info
for /F "tokens=1 delims=." %%A in ("%VS_VERSION%") do @(
    set VS_VERSION=%%A
)

:: Get a list of Visual Studio generators from cmake, match against the version installed
for /F "delims=" %%A in ('cmake --help ^| findstr /R "Visual Studio %VS_VERSION%.+\[arch\]"') do @(
    set GENERATOR=%%A
)

:: Grab the generator name from the the name = description pair
for /F "tokens=1 delims==" %%A in ("%GENERATOR%") do (
    set GENERATOR_NAME=%%A
)

:: Trim leading whitespace
for /F "tokens=*" %%A in ("%GENERATOR_NAME%") do set TRIMMED=%%A
set CMAKE_VS_GENERATOR=%TRIMMED:[arch] =Win64%
:: write out maven properties file
echo cmake.generator=%CMAKE_VS_GENERATOR%>mvn-build\cmake.properties
