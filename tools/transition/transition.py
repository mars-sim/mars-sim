# coding=utf-8
# Mars Simulation Project
# Script for output of transitional progress
# transition.py
# @version 3.08 2015-05-27
# @author Lars Næsbye Christensen [lechimp]

# Very simple script to tell us how much Swing and JavaFX we use


import io
import os
num_of_java_files = 0;
num_of_files_with_swing = 0;
num_of_files_with_javafx = 0;
num_of_files_with_both = 0;

rootdir = '/home/lanac/workspace/'

for subdir, dirs, files in os.walk(rootdir):
	for file in files:
		if file.endswith('.java'):
				num_of_java_files =num_of_java_files +1;
				if '.swing' in open(subdir+"/"+ file).read():
					num_of_files_with_swing = num_of_files_with_swing + 1;
				if 'javafx.' in open(subdir+"/"+ file).read():
					num_of_files_with_javafx = num_of_files_with_javafx + 1;

print "Java files: "+ str(num_of_java_files);
print "Swing imports: "+ str(num_of_files_with_swing);
print "JavaFX imports: "+ str(num_of_files_with_javafx);
