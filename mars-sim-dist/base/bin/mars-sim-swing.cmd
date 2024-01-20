@echo off

REM Location installation home
set "MARS_SIM_DIR=%~dp0"
set "MARS_SIM_DIR=%MARS_SIM_DIR:~0,-5%"
set "MARS_SIM_TARGET=%MARS_SIM_DIR%\lib\mars-sim-swing.jar"

REM Load any JVM options
set "JVM_ARGS="
set "JVM_OPTIONS=%MARS_SIM_DIR%\conf\jvm.options"
if exist %JVM_OPTIONS% (
	for /F "usebackq delims=" %%a in ("%JVM_OPTIONS%") do set JVM_ARGS=%JVM_ARGS% %%a
)

REM Run it
java %JVM_ARGS% -jar %MARS_SIM_TARGET% %*