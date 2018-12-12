@echo off

:: Get a list of Visual Studio generators from cmake
for /F "delims=" %%A in ('cmake --help ^| findstr /R "Visual Studio .+\[arch\]"') do @(
    set GENERATOR=%%A
    goto :found
)

:found
:: Grab first Visual Studio generator
for /F "tokens=1 delims==" %%A in ("%GENERATOR%") do (
    set GENERATOR_NAME=%%A
)

:: Trim leading whitespace
for /F "tokens=*" %%A in ("%GENERATOR_NAME%") do set TRIMMED=%%A
set CMAKE_VS_GENERATOR=%TRIMMED:[arch] =Win64%
:: If being run from maven, write out maven properties file
echo cmake.generator=%CMAKE_VS_GENERATOR%>mvn-build\cmake.properties
