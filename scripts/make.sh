export CLASSPATH='.:jars/jfreechart.jar:jars/jcommon.jar:jars/junit.jar'

cd ../

# Compile all classes.
javac -classpath $CLASSPATH -deprecation `find . -name \*.java -printf "%p "`

