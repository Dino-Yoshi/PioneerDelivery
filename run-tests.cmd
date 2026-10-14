@echo off
setlocal
cd /d "%~dp0"
if defined JAVA_HOME goto useHome
java tools\TestSuite.java
exit /b %errorlevel%
:useHome
"%JAVA_HOME%\bin\java.exe" tools\TestSuite.java
exit /b %errorlevel%
