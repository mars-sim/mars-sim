@echo off
call java -jar jars/mars-sim-main-3.1.0-b1.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
