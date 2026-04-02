@echo off
title BPD TriggerBot - Builder
color 0A
echo.
echo  ================================
echo   BPD TriggerBot - Auto Builder
echo  ================================
echo.

:: Find Java
if not "%JAVA_HOME%"=="" goto :java_ok
for /d %%i in (
    "C:\Program Files\Java\jdk-21"
    "C:\Program Files\Java\jdk-17"
    "C:\Program Files\Java\jdk*"
    "C:\Program Files\Eclipse Adoptium\jdk*"
    "C:\Program Files\Zulu\zulu*"
) do (
    if exist "%%~i\bin\java.exe" (
        set "JAVA_HOME=%%~i" & goto :java_ok
    )
)
echo [!] Java not found - install JDK 17 or 21
pause & exit /b 1
:java_ok
echo [*] Java: %JAVA_HOME%
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [*] Building...
call gradlew.bat clean build --no-daemon -q
if %errorlevel% neq 0 (
    echo.
    echo [!] BUILD FAILED - run with: gradlew clean build
    pause & exit /b 1
)

:: Copy JAR to Desktop
for /f "delims=" %%i in ('dir /b /s build\libs\BetterPingDisplay-Fabric-*.jar 2^>nul ^| findstr /v "sources" ^| findstr /v "dev"') do set JAR=%%i
if not defined JAR (echo [!] JAR not found & pause & exit /b 1)

echo.
echo  ================================
echo   BUILD SUCCESS!
echo  ================================
echo.
echo  JAR: %JAR%
echo.
echo  Copy to mods folder? (Y/N)
set /p copy="Choice: "
if /i "%copy%"=="Y" (
    :: Try common launcher paths
    set "MODS="
    for %%p in (
        "%APPDATA%\ModrinthApp\profiles\PvP*\mods"
        "%APPDATA%\.minecraft\mods"
        "%APPDATA%\PrismLauncher\instances\*\minecraft\mods"
    ) do (
        for /d %%d in (%%p) do (
            if exist "%%d" (set "MODS=%%d" & goto :copy_jar)
        )
    )
    :copy_jar
    if defined MODS (
        copy /y "%JAR%" "%MODS%\" >nul
        echo [+] Copied to: %MODS%
    ) else (
        echo [!] Mods folder not found - copy manually
        explorer /select,"%JAR%"
    )
)

echo.
pause
