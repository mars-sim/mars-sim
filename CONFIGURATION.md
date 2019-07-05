# Configuration


## Utility Tool

In order to manipulate these xml files inside the jar file, 
we recommend installing the [7-Zip File Manager](https://www.7-zip.org/)
that allows users to manipulate files insides a jar file 
on the fly without having to manually compressing, uncompressing,
 cutting and pasting of files.


## Location

The Mars Simulation Project's configuration files are primarily 
written in XML format. They are located inside the '/conf' 
sub-directory of the mars-sim jar file. Specifically, they 
are inside the resources folder of the mars-sim-core 
maven submodule.
 
 
## Editing
  
Hover the mouse cursor over the mars-sim jarfile.
Right click on the jarfile to bring up a menu.
Choose the option '7-Zip' and 'Open archive'.
In the 7-zip FIle Manager, go to the directory '\conf'
Right click on a xml file of your interest
Choose 'Edit' to open up that xml
 
 
## Backup

It is recommended that you make a backup of the original configuration
file before editing it as user-created XML errors can cause mars-sim
fail to start.


## List of xml files

buildings.xml:	    Modify or create new buildings based on functional 
                  	components.
                  
construction.xml: 	Modify or create new construction sites for constructing
                  	settlement buildings.
               
crops.xml:        	Modify or add new crops that settlers can grow in 
                  	greenhouses.

foodProduction.xml: Modify or add food technology related processes.
           
landmarks.xml:    	Modify or add landmarks.
               
malfunctions.xml: 	Modify or create new malfunctions that can occur
                  	in the simulation.
                  
manufacturing.xml: 	Modify or create new manufacturing processes.  

meals.xml:			Modify or create new meal recipes.                  
                  
medical.xml:      	Modify or create new illnesses or treatments.

minerals.xml:     	Modify or create new mineral types.

part_packages.xml: 	Modify or create new part packages that can be used for
                  	initial settlements or resupplies from Earth.

parts.xml:        	Modify or create new parts.

people.xml:       	Modify properties related to people. May add new 
                  	names to the person name list.
                  
resources.xml:    	Modify or create new resources.

resupplies.xml:   	Modify or create settlement resupplies from Earth.
                  
settlements.xml:  	Modify or create new settlement templates an
                  	define individual settlements. May add new settlement
                  	names to the names list.
                  
simulation.xml:   	Modify simulation properties.

vehicles.xml:    	Modify or create new rovers based on components. May add
					new rover names to the names list.