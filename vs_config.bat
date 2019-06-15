
@echo on
@setlocal enableextensions enabledelayedexpansion

:: Ensure all slashes in the path are windows style
set ARG=%1
set CMAKE_BINARIES=%ARG:\=/%
if ["%CMAKE_BINARIES%"] == [] (
    echo No CMake binaries directory specified
    goto :error
)

:: if the generator is specified, then we can narrow the search
if not ["%AWS_CMAKE_GENERATOR%"] == [] (
    echo Using AWS_CMAKE_GENERATOR from environment
    set GENERATOR=%AWS_CMAKE_GENERATOR%
    :: skip "Visual Studio ", then get the next 2 chars
    set VS_VERSION=%AWS_CMAKE_GENERATOR:"=%
    set VS_VERSION=!VS_VERSION:Visual Studio =!
    set VS_VERSION=!VS_VERSION:~0,2!
)

:: Augment the path to include the path where VS installs vswhere in 2017 and later
set PATH=%PATH%;%ProgramFiles(x86)%\Microsoft Visual Studio\Installer
for /f %%A in ('where vswhere') do (
    set VSWHERE_PATH=%%A
)

if [!VSWHERE_PATH!] == [] (
    :: This should only be possible on VS 2015, or someone with a really out of date 2017, pre 15.2
    echo vswhere could not be found, assuming default installation path for Visual Studio
    
    :: Get the right version of Visual Studio if we know which we want, otherwise try
    :: everything in ascending version order to get the latest installed VS
    if [!VS_VERSION!] EQU [14] set TRY_VS14=1
    if [!VS_VERSION!] == [] set TRY_VS14=1
    if [!TRY_VS14!] EQU [1] (
        if exist "C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\vcvarsall.bat" (
            set VCVARSALL_PATH="C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\vcvarsall.bat"
        )
    )

    if [!VS_VERSION!] EQU [15] set TRY_VS15=1
    if [!VS_VERSION!] EQU [] set TRY_VS15=1
    if [!TRY_VS15!] EQU [1] (
        if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvarsall.bat" (
            set VCVARSALL_PATH="C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvarsall.bat"
        )
        if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\VC\Auxiliary\Build\vcvarsall.bat" (
            set VCVARSALL_PATH="C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\VC\Auxiliary\Build\vcvarsall.bat"
        ) 
        if exist "C:\Program Files (x86)\Microsoft Visual Studio\2017\Enterprise\VC\Auxiliary\Build\vcvarsall.bat" (
            set VCVARSALL_PATH="C:\Program Files (x86)\Microsoft Visual Studio\2017\Enterprise\VC\Auxiliary\Build\vcvarsall.bat"
        )
    )

    if not [!VCVARSALL_PATH!] == [] (
        goto :vs_found
    )
) else (
    :: Just use vswhere
    for /f "tokens=*" %%A in ('vswhere -legacy -latest -property installationPath') do (
        set VS_PATH=%%A
    )
    for /f "tokens=1 delims=." %%A in ('vswhere -legacy -latest -property installationVersion') do (
        set VS_VERSION=%%A
    )

    if [!VS_PATH!] == [] (
        echo No suitable version of Visual Studio could be found by vswhere
        goto :error
    )

    if exist "!VS_PATH!\VC\vcvarsall.bat" (
        set VCVARSALL_PATH="!VS_PATH!\VC\vcvarsall.bat"
        goto :vs_found
    )
    if exist "!VS_PATH!\VC\Auxiliary\Build\vcvarsall.bat" (
        set VCVARSALL_PATH="!VS_PATH!\VC\Auxiliary\Build\vcvarsall.bat"
        goto :vs_found
    )
)

echo No vcvarsall.bat could be found
goto :error

:vs_found
if [!GENERATOR!] == [] (
    :: Get a list of Visual Studio generators from cmake, match against the version installed
    for /F "delims=" %%A in ('cmake --help ^| findstr /C:"Visual Studio !VS_VERSION!"') do (
        set GENERATOR=%%A
        goto :generator_found
    )
)

:generator_found
:: Grab the generator name from the the name = description pair
for /F "tokens=1 delims==" %%A in ("!GENERATOR!") do (
    set GENERATOR_NAME=%%A
)

:: Trim leading whitespace
for /F "tokens=*" %%A in ("%GENERATOR_NAME%") do set TRIMMED=%%A
set CMAKE_VS_GENERATOR=%TRIMMED:[arch] =Win64%
:: strip quotes in case they are there
set CMAKE_VS_GENERATOR=!CMAKE_VS_GENERATOR:"=!
:: write out maven properties file
if not exist %CMAKE_BINARIES% (
    mkdir %CMAKE_BINARIES%
)
echo cmake.generator=!CMAKE_VS_GENERATOR!>"%CMAKE_BINARIES%\cmake.properties"
echo vs.version=!VS_VERSION!>>"%CMAKE_BINARIES%\cmake.properties"
echo vs.vcvarsall=!VCVARSALL_PATH!>>"%CMAKE_BINARIES%\cmake.properties"
echo CMAKE_BINARIES=%CMAKE_BINARIES%
type "%CMAKE_BINARIES%\cmake.properties"

popd
@endlocal
goto :EOF

:error
@endlocal
exit /b 1
