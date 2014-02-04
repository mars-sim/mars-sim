# coding=utf-8

# Mars Simulation Project
# csv2xml.py
# @version 3.06 2014-02-04
# @author Lars Næsbye Christensen [lechimp]
#
# This script requires Python 2.3 or later and the 'SearchResults.csv' file to be in the same
# directory as the script file.
# The CSV file can be generated at http://planetarynames.wr.usgs.gov/SearchResults?target=MARS
# Scroll down and click 'CSV (comma separated values) for importing into Excel', and save the file
# Usage : 'python csv2xml.py'
#
# TODO: Insert the doctype - <!DOCTYPE landmark-list SYSTEM "conf/dtd/landmarks.dtd"> - doesn't seem to be necessary, though

from xml.dom.minidom import Document

# Preferences - should maybe be cmd line parameters
diameter_threshold = 50.0 #skip landmarks smaller than this size in km

# Initialize types 
xmldoc = Document()
csvdata = []
# in case we don't have a key line, assume defaults
index_feature = 0
index_target = 1
index_diameter = 2
index_lat = 3
index_long = 4

# Add introductory comments 
introcomment1 = xmldoc.createComment("Landmark coordinates from USGS Astrogeology Research Program")
introcomment2 = xmldoc.createComment("http://planetarynames.wr.usgs.gov/SearchResults?target=MARS")
introcomment3 = xmldoc.createComment("Landmarks to be displayed in the user interface. ")
xmldoc.appendChild(introcomment1)
xmldoc.appendChild(introcomment2)
xmldoc.appendChild(introcomment3)
 
# Create the base XML element
landmarks = xmldoc.createElement("landmark-list")
landmarks.setAttribute("xmlns", "http://mars-sim.sourceforge.net/landmarks")
landmarks.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
landmarks.setAttribute("xsi:schemaLocation", "http://mars-sim.sourceforge.net/landmarks schema/landmarks.xsd")

xmldoc.appendChild(landmarks)

# Open and parse the search results file (CSV), get the data key and data
f = open('SearchResults.csv')

csvlinelist = f.readlines() # fill a list with CSV lines

# Main parsing loop
for csvline in csvlinelist: 
	if csvline == "\n":
		print "Skipped empty line"  # ignore empty lines
	elif "Feature_Name" in csvline:
		print "Found key line, parsing..." # first line of CSV, names of keys
		paramlist = csvline.split(",") # explode into a list of keys
		index_feature = paramlist.index('Feature_Name')
		index_diameter = paramlist.index('Diameter')
		index_lat = paramlist.index('Center_Latitude')
		index_long = paramlist.index('Center_Longitude')
	elif "Dropped" in csvline:
		print "Skipped dropped name" # ignore dropped (unapproved) names
	elif "Mars" not in csvline:
		print "Skipped non-Mars target" # ignore features not on Mars
	else:
		print "Found data line, parsing..." #ready to move data into XML DOM
		valuelist = csvline.split(",") # explode into a list of values
		
		if float(valuelist[index_diameter]) > float(diameter_threshold):
			landmark = xmldoc.createElement("landmark")
		
			namestring = valuelist[index_feature]
			clean_string = namestring.replace('\"', '')
			landmark.setAttribute("name", clean_string) # Feature_Name
		
			landmark.setAttribute("diameter", valuelist[index_diameter]) # Diameter of feature
			landmark.setAttribute("longitude", valuelist[index_long]+" E") # Center_Longitude
			landmark.setAttribute("latitude", valuelist[index_lat]+" N") # Center_Latitude
			landmarks.appendChild(landmark)

# Add data for artificial objects (from Google Mars and Wikipedia)
artobjcomment = xmldoc.createComment("Martian Landers and Rovers")
landmarks.appendChild(artobjcomment)

artobjarray =[["Beagle 2 Lander", "90.0 E", "10.6 N"], 
             ["Mars 2 Lander", "47.0 E", "45.0 S"],
             ["Mars 3 Lander", "158.0 W", "45.0 S"],
             ["Mars 6 Lander", "19.5 W", "23.9 S"],
             ["Mars Pathfinder Rover", "33.3 W", "19.3 N"],
             ["Mars Polar Lander", "164.7 E", "76.7 S"],
             ["MSL Curiosity Rover", "137.2 E", "4.3 S"],
             ["MER Spirit Rover", "175.47 E", "14.57 S"],
             ["MER Opportunity Rover", "5.53 W", "1.95 S"],
             ["Phoenix Mars Lander", "125.7 W", "68.22 N"],
             ["Viking Lander 1", "48.0 W", "22.5 N"],
             ["Viking Lander 2", "133.7 E", "47.9 N"]]
             
for artobj in artobjarray: 
	landmark = xmldoc.createElement("landmark")
	landmark.setAttribute("name", artobj[0]) 
	landmark.setAttribute("longitude", artobj[1]) 
	landmark.setAttribute("latitude", artobj[2]) 
	landmarks.appendChild(landmark)

f.close() # close our CSV file stream nicely
 
# Write the final XML data to file landmarks.xml
f = open('landmarks.xml', 'w')
xmldoc.writexml(f, encoding= 'utf-8', indent="  ",addindent="	",newl="\n")
f.close()
