# coding=utf-8

# Mars Simulation Project
# Tab-separated landmark file importer script thingie
# tsv2xml.py
# @version 3.06 2014-02-05
# @author Lars Næsbye Christensen [lechimp]
#
# This script requires Python 2.3 or later and the 'SearchResults.tsv' file to be in the same
# directory as the script file.
# Generate TSV file at http://planetarynames.wr.usgs.gov/SearchResults?target=MARS
# Scroll down and click 'TSV (tab separated values) for importing into other spread sheets', and save the file as SearchResults.tsv
# Usage : 'python tsv2xml.py'

# TODO: 
# Insert doctype - <!DOCTYPE landmark-list SYSTEM "conf/dtd/landmarks.dtd"> - doesn't seem to be necessary, though
# Parse into MSP's native format
# Sort according to name or type

from xml.dom.minidom import Document

# Preferences - should maybe be cmd-line parameters
diameter_threshold = 1000.0 #skip landmarks smaller than this size in km

# Initialize types 
xmldoc = Document()
# in case we don't have a key line, assume some defaults
index_feature = 0
index_target = 1
index_diameter = 2
index_lat = 3
index_long = 4
index_approval = 7
index_origin = 8

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

# Open and parse the search results file (TSV), get the data key and data
f = open('SearchResults.tsv')

tsvlinelist = f.readlines() # fill a list with TSV lines

# Main parsing loop
for tsvline in tsvlinelist: 
	if tsvline == "\n":
		print "Skipped empty line"  # ignore empty lines
	elif "Feature_Name" in tsvline:
		print "Found key line, parsing..." # first line of TSV, names of keys
		paramlist = tsvline.split("\t") # explode into a list of keys
		index_feature = paramlist.index('Feature_Name')
		index_diameter = paramlist.index('Diameter')
		index_lat = paramlist.index('Center_Latitude')
		index_long = paramlist.index('Center_Longitude')
		index_approval = paramlist.index('Approval_Date')
		index_origin = paramlist.index('Origin')
	elif "Dropped" in tsvline:
		print "Skipped dropped name" # ignore dropped (unapproved) names
	elif "Mars" not in tsvline:
		print "Skipped non-Mars target" # ignore features not on Mars
	else:
		print "Found data line, parsing..." #ready to move data into XML DOM
		valuelist = tsvline.split("\t") # explode into a list of values
		
		if float(valuelist[index_diameter]) > float(diameter_threshold): # threshold
			landmark = xmldoc.createElement("landmark")
		
			namestring = valuelist[index_feature]
			namestring = namestring.replace('[', '')
			namestring = namestring.replace(']', '')
			landmark.setAttribute("name", namestring) # Feature_Name
		
			landmark.setAttribute("diameter", valuelist[index_diameter]) # Diameter of feature

			landmark.setAttribute("longitude", valuelist[index_long]+" E") # Center_Longitude
			landmark.setAttribute("latitude", valuelist[index_lat]+" N") # Center_Latitude

			landmark.setAttribute("approvaldate", valuelist[index_approval]) # Approval Date string
			landmark.setAttribute("origin", valuelist[index_origin]) # Origin of name
			landmarks.appendChild(landmark)


# Add data for artificial objects (from Google Mars and Wikipedia)
artobjcomment = xmldoc.createComment("Artificial Objects")
landmarks.appendChild(artobjcomment)

artobjarray =[["Beagle 2 Lander", "90.0 E", "10.6 N", "0.1"], 
             ["Mars 2 Lander", "47.0 E", "45.0 S", "0.1"],
             ["Mars 3 Lander", "158.0 W", "45.0 S", "0.1"],
             ["Mars 6 Lander", "19.5 W", "23.9 S", "0.1"],
             ["Mars Pathfinder Rover", "33.3 W", "19.3 N", "0.1"],
             ["Mars Polar Lander", "164.7 E", "76.7 S", "0.1"],
             ["MSL Curiosity Rover", "137.2 E", "4.3 S", "0.1"],
             ["MER Spirit Rover", "175.47 E", "14.57 S", "0.1"],
             ["MER Opportunity Rover", "5.53 W", "1.95 S", "0.1"],
             ["Phoenix Mars Lander", "125.7 W", "68.22 N", "0.1"],
             ["Viking Lander 1", "48.0 W", "22.5 N", "0.1"],
             ["Viking Lander 2", "133.7 E", "47.9 N", "0.1"]]
             
for artobj in artobjarray: 
	landmark = xmldoc.createElement("landmark")
	landmark.setAttribute("name", artobj[0]) 
	landmark.setAttribute("longitude", artobj[1]) 
	landmark.setAttribute("latitude", artobj[2]) 
	landmark.setAttribute("diameter", artobj[3]) #absurd?
	landmark.setAttribute("approvaldate", "N/A") #absurd?
	landmark.setAttribute("origin", "N/A") #absurd?

	landmarks.appendChild(landmark)

f.close() # close our TSV file stream nicely
 
# Write the final XML data to file landmarks.xml
f = open('landmarks.xml', 'w')
xmldoc.writexml(f, encoding= 'utf-8', indent="  ",addindent="	",newl="\n")
f.close()
