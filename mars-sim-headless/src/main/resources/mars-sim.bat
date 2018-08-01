@echo off
call java -jar jars/mars-sim-headless-3.1.0-b2.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
