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
	elif "Dropped" in csvline:
		print "Skipped dropped name" # ignore dropped names
	elif "Feature_Name" in csvline:
		print "Found key line, parsing..." # first line of CSV, names of keys
		paramlist = csvline.split(",") # explode into a list of keys
		index_feature = paramlist.index('Feature_Name')
		index_lat = paramlist.index('Center_Latitude')
		index_long = paramlist.index('Center_Longitude')
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

# Add the rovers and other craft
rovercomment = xmldoc.createComment("Martian Landers and Rovers")
landmarks.appendChild(rovercomment)


landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Beagle 2 Lander") 
landmark.setAttribute("longitude", "90.0 E") 
landmark.setAttribute("latitude", "10.6 N") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars 2 Lander") 
landmark.setAttribute("longitude", "47.0 E") 
landmark.setAttribute("latitude", "45.0 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars 3 Lander") 
landmark.setAttribute("longitude", "158.0 W") 
landmark.setAttribute("latitude", "45.0 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars 6 Lander") 
landmark.setAttribute("longitude", "19.5 W") 
landmark.setAttribute("latitude", "23.9 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars Pathfinder Rover") 
landmark.setAttribute("longitude", "33.3 W") 
landmark.setAttribute("latitude", "19.3 N") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars Polar Lander") 
landmark.setAttribute("longitude", "164.7 E") 
landmark.setAttribute("latitude", "76.7 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Mars Science Laboratory") 
landmark.setAttribute("longitude", "137.44 E") 
landmark.setAttribute("latitude", "4.59 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "MER Spirit Rover") 
landmark.setAttribute("longitude", "175.5 E") 
landmark.setAttribute("latitude", "14.6 S") 
landmarks.appendChild(landmark)
	
landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "MER Opportunity Rover") 
landmark.setAttribute("longitude", "5.5 W") 
landmark.setAttribute("latitude", "2.0 S") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Phoenix Mars Lander") 
landmark.setAttribute("longitude", "123.0 W") 
landmark.setAttribute("latitude", "68.0 N") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Viking Lander 1") 
landmark.setAttribute("longitude", "48.0 W") 
landmark.setAttribute("latitude", "22.5 N") 
landmarks.appendChild(landmark)

landmark = xmldoc.createElement("landmark")
landmark.setAttribute("name", "Viking Lander 2") 
landmark.setAttribute("longitude", "133.7 E") 
landmark.setAttribute("latitude", "47.9 N") 
landmarks.appendChild(landmark)

f.close() # close our CSV file stream nicely
 
# Write the final XML data to file landmarks.xml
f = open('landmarks.xml', 'w')
xmldoc.writexml(f, encoding= 'utf-8', indent="  ",addindent="	",newl="\n")
f.close()
