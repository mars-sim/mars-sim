export CLASSPATH='.:jars/jfreechart.jar:jars/jcommon.jar:jars/aelfred.jar:jars/junit.jar'
javac -classpath $CLASSPATH `find . -name \*.java -printf "%p "`

