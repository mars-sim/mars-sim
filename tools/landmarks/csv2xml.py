# Mars Simulation Project
# csv2xml.py
# @version 3.03 2012-08-06
# @author Lars Naesbye Christensen [lechimp]
#
# Requires Python 2.3 or later and the 'SearchResults.csv' file in same directory
# CSV file can be generated at http://planetarynames.wr.usgs.gov/SearchResults?target=MARS
# Usage : 'python csv2xml.py'
#
# TODO: Insert the doctype - <!DOCTYPE landmark-list SYSTEM "conf/dtd/landmarks.dtd">
# TODO: Insert the artifacts list (artifacts.txt)
# TODO: (optional) write only positive coordinates (e.g. -5.3 N => 5.3 S )

from xml.dom.minidom import Document

# Initialize types 
xmldoc = Document()
csvdata = []
index_feature = 0
index_lat = 3
index_long = 4

# Add introductory comments 
introcomment = xmldoc.createComment("Landmark coordinates from USGS Astrogeology Research Program")
introcomment2 = xmldoc.createComment("http://planetarynames.wr.usgs.gov/SearchResults?target=MARS")
introcomment3 = xmldoc.createComment("Landmarks to be displayed in the user interface. ")
xmldoc.appendChild(introcomment)
xmldoc.appendChild(introcomment2)
xmldoc.appendChild(introcomment3)
 
# Create the base XML element
landmarks = xmldoc.createElement("landmark-list")
xmldoc.appendChild(landmarks)

# Open and parse the search results file (CSV), get the data key and data
f = open('SearchResults.csv')

csvlinelist = f.readlines() # fill a list with CSV lines

# Main parsing loop
for csvline in csvlinelist: 
	if csvline == "\n":
		print "Skipped empty line"  # ignore empty lines
	elif "Dropped" in csvline:
		print "Skipped dropped name" # ignore dropped names
	elif "Feature_Name" in csvline:
		print "Found key line, parsing..." # first line of CSV, names of keys
		paramlist = csvline.split(",") # explode into a list of keys
		index_feature = paramlist.index('Feature_Name')
		print index_feature
		index_lat = paramlist.index('Center_Latitude')
		print index_lat
		index_long = paramlist.index('Center_Longitude')
		print index_long
	else:
		print "Data line, parsing..." #move data into XML DOM
		valuelist = csvline.split(",") # make into a list of values
		landmark = xmldoc.createElement("landmark")
		
		namestring = valuelist[index_feature]
		clean_string = namestring.replace('\"', '')
		landmark.setAttribute("name", clean_string) # Feature_Name
		
		landmark.setAttribute("longitude", valuelist[index_long]+" E") # Center_Longitude
		landmark.setAttribute("latitude", valuelist[index_lat]+" N") # Center_Latitude
		landmarks.appendChild(landmark)


f.close() # close our CSV file stream nicely
 
# Write the final XML data to file landmarks.xml
f = open('landmarks.xml', 'w')
xmldoc.writexml(f, encoding= 'utf-8', indent="  ",addindent="	",newl="\n")
f.close()
