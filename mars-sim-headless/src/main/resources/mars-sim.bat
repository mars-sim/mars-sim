:: This batch file will run the command "java -jar ./[$VERSION_OR_BUILD]_headless_java11.jar new" 
:: Ensure you replace the [$VERSION_OR_BUILD] with the correct version (such as 3.4.0) or build number (such as 7553) in the jarfile
@echo off
call java -jar ./3.4.0_headless_java11.jar new
echo Exit Code = %ERRORLEVEL%
if "%ERRORLEVEL%" == "1" exit /B 1