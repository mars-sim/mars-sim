echo off

set CLASSPATH=../;../jars/jfreechart.jar;../jars/jcommon.jar;../jars/junit.jar

set SOURCE=%SOURCE% ..\org\mars_sim\msp\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\equipment\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\events\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\malfunction\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\person\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\person\ai\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\person\ai\mission\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\person\ai\task\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\person\medical\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\structure\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\structure\building\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\structure\building\function\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\simulation\vehicle\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\tool\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\tool\navigator\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\tool\monitor\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\tool\search\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\tool\time\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\equipment\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\person\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\vehicle\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\structure\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_window\structure\building\*.java
set SOURCE=%SOURCE% ..\org\mars_sim\msp\ui\standard\unit_display_info\*.java

echo on

javac -deprecation -classpath %CLASSPATH% %SOURCE%

pause

REM Create msp-simulation.jar file.

echo off

cd ..

set CLASSES=org\mars_sim\msp\simulation\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\equipment\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\events\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\malfunction\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\person\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\person\ai\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\person\ai\mission\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\person\ai\task\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\person\medical\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\structure\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\structure\building\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\structure\building\function\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\simulation\vehicle\*.class

echo on

jar -cvf jars/msp-simulation.jar %CLASSES%

pause

REM Create msp-standard-ui.jar file.

echo off

set CLASSES=org\mars_sim\msp\ui\standard\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\tool\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\tool\navigator\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\tool\monitor\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\tool\search\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\tool\time\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\equipment\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\person\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\vehicle\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\structure\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_window\structure\building\*.class
set CLASSES=%CLASSES% org\mars_sim\msp\ui\standard\unit_display_info\*.class

echo on

jar -cvf jars/msp-standard-ui.jar %CLASSES% images\*.*

pause

REM Create MarsProject.jar file.

jar -cvmf scripts\main-manifest.txt MarsProject.jar org\mars_sim\msp\MarsProject.class

cd scripts

pause
