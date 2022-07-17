:: Please replace the [$CURRENT_VERSION] with the correct build version in your jarfile 
:: e.g. [$CURRENT_VERSION] such as 3.4.0 or a build number such as 6970
@echo off
call java -jar ./[$CURRENT_VERSION]_swing_java11.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
