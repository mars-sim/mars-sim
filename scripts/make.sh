export CLASSPATH='.:jars/jfreechart.jar:jars/jcommon.jar:jars/junit.jar:jars/plexus-core.jar:jars/commons-collections-3.1.jar:jars/log4j-1.2.8.jar'

cd ../

# Compile all classes.
javac -classpath $CLASSPATH -deprecation `find . -name \*.java -printf "%p "`

