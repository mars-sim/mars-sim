#
# Mars Simulation Project
# Java makefile by jpatokal
# $Id: Makefile,v 1.2 2002-03-12 21:46:16 jpatokal Exp $
#

VERSION = 2.73

JAVAROOT = /usr/local/java/jdk1.3.1/j2sdk1.3.1/
JAVABIN = $(JAVAROOT)/bin

JARS = ./jars

CLASSPATH = .
CLASSPATH := $(CLASSPATH):$(JARS)/jfreechart.jar
CLASSPATH := $(CLASSPATH):$(JARS)/jcommon.jar

JAVA_OPTIONS = -classpath $(CLASSPATH)
JAVAC_OPTIONS = -deprecation -classpath $(CLASSPATH)

###########################################################################
# The programs to do all the tricks.
###########################################################################

JAR = $(JAVABIN)/jar
JAVA = $(JAVABIN)/java 
JAVAC = $(JAVABIN)/javac $(JAVAC_OPTIONS) -g

JAVADOC = $(JAVABIN)/javadoc
JAVADOC_DIR = docs/javadoc
JAVADOC_OPTIONS = -overview overview.html -use -author\
  -windowtitle "Mars Simulation Project v$(VERSION) API Specification"\
  -doctitle "Mars Simulation Project v$(VERSION)<BR>API Specification" \
  -header "<b>Mars Simulation Project</b><br><font size=\"-1\">v$(VERSION)</font>"
JAVADOC_PACKAGES = $(shell find org \! -path "*/test/*" -name "*.java" -printf "%h \n" | sort | uniq | tr / .) MarsProject
JAVADOC_FILES = $(shell find $(JAVADOC_DIR) -name '*' \! -path '*CVS*' \! -path $(JAVADOC_DIR) -depth)

###########################################################################
# file locations
###########################################################################

# all the .java files - other makes choke on this 
SRC = $(shell find . -name \*\.java)

# all the .class files
CLASSES = $(SRC:.java=.class)

# .class files depend on .java files
.SUFFIXES: .java .class

# how to build .class files from .java files
.java.class:
	$(JAVAC) -classpath $(CLASSPATH) $<

###########################################################################
# targets
###########################################################################

# make all the .class files
all: $(CLASSES)

# Removes all the class files.
clean:
	find . -name \*\.class -exec rm {} \;
	find . -name \*\~ -exec rm {} \;
	find . -name \#\*\# -exec rm {} \;

javadoc: cleanjavadoc
	@echo "Making JavaDoc documentation"
	@mkdir -p $(JAVADOC_DIR)
	$(JAVADOC) -d $(JAVADOC_DIR) $(JAVADOC_OPTIONS) $(JAVADOC_PACKAGES)

cleanjavadoc:
	@echo "Removing old javadocs"
	rm -rf $(JAVADOC_FILES)

classpath:
	@echo $(CLASSPATH)
