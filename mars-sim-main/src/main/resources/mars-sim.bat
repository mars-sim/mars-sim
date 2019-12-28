:: Please replace the $CURRENT_VERSION with the correct build version in your jarfile 
:: e.g. $CURRENT_VERSION could be 5184
@echo off
call java -jar ./[$CURRENT_VERSION]_swing_java11.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
