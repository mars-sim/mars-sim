# coding=utf-8
#
# Mars Simulation Project
# script for fixing messed up utf8 characters in internationalized messages
# @author st.pa.
#
# This script requires that the internationalized msg-files are in their usual place.
# Please avoid messing up the unicode encoding of the internationalized properties,
# see the msp wiki page on translation issues for more details.

import os
import os.path

# get absolute path to the .properties-files
folder = os.path.abspath("../../mars-sim/mars-sim-core/src/main/resources/")
print "absolute folder: " + folder
if not os.path.isdir(folder):
        print "invalid directory " + folder
else:
        os.chdir(folder)
# loop over all files under that path
for f in os.listdir(folder):
        # only care for .properties-files
        if f.endswith(".properties"):
                print "processing file " + f
                # read contents
                fi = open(f,"r")
                text = fi.read()
                fi.close()
                # replace what needs replacing
                # replace common characters
                text = text.replace('\\u00C2\\u00B0','°')
                text = text.replace('\\u00E2\\u0096\\u00BC','▼')
                # replace german characters
                text = text.replace('\\u00C3\\u00A4','ä')
                text = text.replace('\\u00C3\\u0084','Ä')
                text = text.replace('\\u00C3\\u00B6','ö')
                text = text.replace('\\u00C3\\u0096','Ö')
                text = text.replace('\\u00C3\\u00BC','ü')
                text = text.replace('\\u00C3\\u009C','Ü')
                text = text.replace('\\u00C3\\u009F','ß')
                # replace danish characters
                text = text.replace('\\u00C3\\u00A6','æ')
                text = text.replace('\\u00C3\\u0098','Ø')
                text = text.replace('\\u00C3\\u0086','Æ')
                text = text.replace('\\u00C3\\u0085','Å')
                text = text.replace('\\u00C3\\u00A5','å')
                text = text.replace('\\u00C3\\u00B8','ø')
                # replace esperanto characters
                text = text.replace('\\u00C4\\u0089','ĉ')
                text = text.replace('\\u00C4\\u0088','Ĉ')
                # text = text.replace('','ĥ')
                # text = text.replace('','Ĥ')
                text = text.replace('\\u00C4\\u00B5','ĵ')
                # text = text.replace('','Ĵ')
                text = text.replace('\\u00C5\\u009D','ŝ')
                # text = text.replace('','Ŝ')
                text = text.replace('\\u00C4\\u009D','ĝ')
                # text = text.replace('','Ĝ')
                text = text.replace('\\u00C5\\u00AD','ŭ')
                # text = text.replace('','Ŭ')
                # text = text.replace('','')
                # text = text.replace('','')
                # write changed contents back to file
                fo = open(f,"w")
                fo.write(text)
                fo.close()
print "all done"
