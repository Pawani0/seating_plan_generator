@echo off
REM ============================================
REM Compile Only Script
REM ============================================

set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set OUT_DIR=%PROJECT_DIR%out
set LIB_DIR=%PROJECT_DIR%lib
set PATH_TO_FX=%LIB_DIR%\javafx
set CLASSPATH=%OUT_DIR%;%LIB_DIR%\poi\*;%LIB_DIR%\pdfbox\*

echo Compiling...

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

javac --module-path "%PATH_TO_FX%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "%CLASSPATH%" ^
      -d "%OUT_DIR%" ^
      -sourcepath "%SRC_DIR%" ^
      "%SRC_DIR%\com\seatingplan\Main.java" ^
      "%SRC_DIR%\com\seatingplan\model\*.java" ^
      "%SRC_DIR%\com\seatingplan\ui\*.java" ^
      "%SRC_DIR%\com\seatingplan\service\*.java" ^
      "%SRC_DIR%\com\seatingplan\excel\*.java" ^
      "%SRC_DIR%\com\seatingplan\pdf\*.java"

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
) else (
    echo Compilation failed!
)

pause
