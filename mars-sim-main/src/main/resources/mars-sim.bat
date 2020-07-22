:: Please replace the [$CURRENT_VERSION] with the correct build version in your jarfile 
:: e.g. [$CURRENT_VERSION] as 3.1.1 or a build number such as 5283
@echo off
call java -jar ./[$CURRENT_VERSION]_swing_java14.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1
