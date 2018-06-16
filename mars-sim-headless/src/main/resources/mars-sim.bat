@echo off
call java -jar mars-sim-main-3.1.0-p10.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
