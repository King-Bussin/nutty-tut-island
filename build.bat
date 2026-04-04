@echo off
REM Auto-compile for Bussin Tut DreamBot script using Java 11

REM Set workspace root (adjust if needed).
set ROOT_DIR=%~dp0

REM Set Java 11 JDK path (update to your installed Java 11 location).
set JDK11="C:\Program Files\Eclipse Adoptium\jdk-11.0.22.7-hotspot\bin"

REM Compiler classpath to DreamBot libs (update path if your BotData location differs).
set CP="C:\Users\laver\DreamBot\BotData\repository2\*"

cd /d "%ROOT_DIR%"

REM Compile
echo Compiling Main.java with Java 11...
%JDK11%\javac -cp %CP% -d out src\Main.java

IF %ERRORLEVEL% NEQ 0 (
    echo Compilation failed with errorlevel %ERRORLEVEL%.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation succeeded.

REM Build JAR directly in script root (overwrite existing)
if not exist out\Main.class (
    echo Compiled classes not found under out\\. Skipping JAR creation.
) else (
    echo Creating BussinTut.jar in %ROOT_DIR%...
    %JDK11%\jar cfm "%ROOT_DIR%BussinTut.jar" manifest.txt -C out .
    echo JAR created at %ROOT_DIR%BussinTut.jar
)

echo Done.
pause
