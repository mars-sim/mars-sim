# coding=utf-8
# Mars Simulation Project
# Script for output of progress with UI transition
# transition.py
# @version 3.08 2015-08-14
# @author Lars Næsbye Christensen
#
# A very simple script to tell us how much Swing and JavaFX we use
# Note that it doesn't discern between packages and imports
#
# Usage : 'python transition.py [path]' and the path you wish to traverse, e.g. '/home/user/mars-sim'


import io
import os
import sys

num_of_java_files = 0;
num_of_files_with_swing = 0;
num_of_files_with_javafx = 0;
num_of_files_with_both = 0;

rootdir = sys.argv[1]; // get the starting path from the command line
carry =0;

for subdir, dirs, files in os.walk(rootdir):
	for file in files:
		if file.endswith('.java'):
				num_of_java_files =num_of_java_files +1;
				if '.swing' in open(subdir+"/"+ file).read():
					num_of_files_with_swing = num_of_files_with_swing + 1;
					carry = 1;
					print 'Swing: ' + file;
				if 'javafx.' in open(subdir+"/"+ file).read():
					num_of_files_with_javafx = num_of_files_with_javafx + 1;
					print 'Javafx: ' + file;
					if carry == 1:
						num_of_files_with_both = num_of_files_with_both +1;
				carry = 0;
print "\n";
print "Files with Java code  : "+ str(num_of_java_files);
print "Files importing Swing : "+ str(num_of_files_with_swing) + " ("+str(100*(float(num_of_files_with_swing)/float(num_of_java_files))) +"%)";
print "Files importing JavaFX: "+ str(num_of_files_with_javafx) + " ("+str(100*(float(num_of_files_with_javafx)/float(num_of_java_files))) +"%)";
print "Files importing both  : "+ str(num_of_files_with_both) + " ("+str(100*(float(num_of_files_with_both)/float(num_of_java_files))) +"%)" ;
