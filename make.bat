echo off

set CLASSPATH=.;jars\jfreechart.jar;jars\jcommon.jar;jars\aelfred.jar;jars\junit.jar

set SOURCE=*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\equipment\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\malfunction\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\person\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\person\ai\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\person\medical\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\structure\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\structure\building\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\structure\building\function\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\structure\building\function\impl\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\structure\template\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\simulation\vehicle\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\tool\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\tool\navigator\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\tool\monitor\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\tool\search\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\tool\time\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\equipment\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\person\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\vehicle\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\structure\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_window\structure\building\*.java
set SOURCE=%SOURCE% org\mars_sim\msp\ui\standard\unit_display_info\*.java

javac -classpath %CLASSPATH% %SOURCE%

