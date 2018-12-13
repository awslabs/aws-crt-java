
@echo off

@setlocal enableextensions enabledelayedexpansion
set CMAKE_ARGS=%*

pushd %~dp0\..\

"%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -InputFormat None -ExecutionPolicy Bypass -Command "iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))" && SET "PATH=%PATH%;%ALLUSERSPROFILE%\chocolatey\bin"
:: this will also install jdk8
choco install maven vswhere -y
call RefreshEnv.cmd
echo JAVA_HOME=%JAVA_HOME%

:: See if a generator was provided
echo.%CMAKE_ARGS% | findstr /C:"-G" >NUL && (
    :: strip everything before -G
    set GENERATOR_ET_AL=%CMAKE_ARGS:*-G=%
    :: strip leading whitepace that was between -G and the generator in quotes
    set GENERATOR_ET_AL=!GENERATOR_ET_AL: ^"=!
    for /F delims^=^"^ tokens^=1 %%A in ("!GENERATOR_ET_AL!") do @(
        set CMAKE_VS_GENERATOR=%%A
        goto :found_generator
    )
    :found_generator
    echo Forcing generator to !CMAKE_VS_GENERATOR!
    :: force this generator into the maven config so find_vs_cmake_generator will find it
    :: when run from maven during the build
    if not exist mvn-build (
        mkdir mvn-build
    )
    echo cmake.generator=!CMAKE_VS_GENERATOR!>mvn-build\cmake.properties
    goto :build_deps
) || (
    echo No generator specified on command line, getting cmake generator from cmake
    call find_vs_cmake_generator.bat
    for /F "tokens=2 delims==" %%A in (mvn-build\cmake.properties) do (
        set CMAKE_VS_GENERATOR=%%A
    )
    echo Using generator "!CMAKE_VS_GENERATOR!"
    set CMAKE_ARGS=-G"!CMAKE_VS_GENERATOR!" %CMAKE_ARGS%
)

:build_deps
call build_deps.bat !CMAKE_ARGS!

mvn compile -X || goto error

popd
@endlocal
goto :EOF

:error
popd
@endlocal
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
