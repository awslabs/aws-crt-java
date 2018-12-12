@echo off

:: Get a list of Visual Studio generators from cmake
cmake --help | findstr /R "Visual Studio .+\[arch\]" | for /F "delims=" %%A in ('more') do @(
    set GENERATOR=%%A
    goto :found
)

:found
:: Grab first Visual Studio generator
set GENERATOR_NAME=""
for /F "tokens=1 delims==" %%A in ("%GENERATOR%") do (
    set GENERATOR_NAME=%%A
)

:: Trim leading whitespace
for /F "tokens=*" %%A in ("%GENERATOR_NAME%") do set TRIMMED=%%A
set CMAKE_VS_GENERATOR=%TRIMMED:[arch]=Win64%
echo cmake.generator=%CMAKE_VS_GENERATOR% > mvn-build\cmake.properties