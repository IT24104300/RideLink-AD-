@ECHO OFF
SETLOCAL
SET "SCRIPT_DIR=%~dp0"
SET "MAVEN_VERSION=3.9.9"
SET "DIST_NAME=apache-maven-%MAVEN_VERSION%"
SET "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%DIST_NAME%"
SET "URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/%DIST_NAME%-bin.zip"

IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
  FOR /D %%J IN ("C:\Program Files\Microsoft\jdk-*") DO SET "JAVA_HOME=%%~fJ"
)
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
  FOR /D %%J IN ("C:\Program Files\Eclipse Adoptium\jdk-*") DO SET "JAVA_HOME=%%~fJ"
)
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
  ECHO JAVA_HOME is not set to a JDK. Install JDK 17+ or set JAVA_HOME.
  EXIT /B 1
)

IF EXIST "%MAVEN_HOME%\bin\mvn.cmd" GOTO RUN

ECHO Downloading Maven %MAVEN_VERSION% (first run only)...
SET "TMPDIR=%TEMP%\ridelink-mvn-wrap"
IF EXIST "%TMPDIR%" RMDIR /S /Q "%TMPDIR%"
MKDIR "%TMPDIR%"
powershell -NoProfile -Command "Invoke-WebRequest -Uri '%URL%' -OutFile '%TMPDIR%\%DIST_NAME%-bin.zip'"
IF ERRORLEVEL 1 (
  ECHO Failed to download Maven from Maven Central.
  EXIT /B 1
)
powershell -NoProfile -Command "Expand-Archive -Path '%TMPDIR%\%DIST_NAME%-bin.zip' -DestinationPath '%TMPDIR%\out' -Force"
IF NOT EXIST "%USERPROFILE%\.m2\wrapper\dists" MKDIR "%USERPROFILE%\.m2\wrapper\dists"
MOVE "%TMPDIR%\out\%DIST_NAME%" "%MAVEN_HOME%" >NUL

:RUN
"%MAVEN_HOME%\bin\mvn.cmd" %*
EXIT /B %ERRORLEVEL%
