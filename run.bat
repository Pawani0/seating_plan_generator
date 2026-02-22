@echo off
REM ============================================
REM Seating Plan Generator - Build and Run Script
REM ============================================

REM Set Java Home (modify this path if different on your system)
REM set JAVA_HOME=C:\Program Files\Java\jdk-21

REM Set paths
set PROJECT_DIR=%~dp0
set SRC_DIR=%PROJECT_DIR%src
set OUT_DIR=%PROJECT_DIR%out
set LIB_DIR=%PROJECT_DIR%lib

REM JavaFX path
set PATH_TO_FX=%LIB_DIR%\javafx

REM Classpath for external libraries
set CLASSPATH=%OUT_DIR%;%LIB_DIR%\poi\*;%LIB_DIR%\pdfbox\*

echo.
echo ============================================
echo  Seating Plan Generator
echo ============================================
echo.

REM Check if lib folders have JARs
if not exist "%PATH_TO_FX%\javafx.base.jar" (
    if not exist "%PATH_TO_FX%\javafx-base-*.jar" (
        echo [WARNING] JavaFX JARs not found in lib\javafx\
        echo Please download JavaFX SDK from https://gluonhq.com/products/javafx/
        echo and extract the JARs to the lib\javafx\ folder.
        echo.
    )
)

REM Create output directory if not exists
if not exist "%OUT_DIR%" (
    mkdir "%OUT_DIR%"
)

echo [Step 1/2] Compiling Java files...
echo.

REM Compile all Java files
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

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! 
    echo Make sure all required JAR files are in the lib\ folders.
    echo.
    pause
    exit /b 1
)

echo [Step 2/2] Running application...
echo.

REM Run the application
java --module-path "%PATH_TO_FX%" ^
     --add-modules javafx.controls,javafx.fxml ^
     --enable-native-access=javafx.graphics ^
     -Djava.library.path="%PATH_TO_FX%" ^
     -cp "%CLASSPATH%" ^
     com.seatingplan.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application failed to start!
    pause
)
