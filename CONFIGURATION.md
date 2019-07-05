# Configuration


## Utility Tool

In order to manipulate these xml files inside the jar file, 
we recommend installing the [7-Zip File Manager](https://www.7-zip.org/)
that allows users to manipulate files insides a jar file 
on the fly without having to manually compressing, uncompressing,
 cutting and pasting of files.


## Location

The Mars Simulation Project's configuration files are primarily 
written in XML format. They are located inside the `/conf` 
sub-directory of the mars-sim jar file. Specifically, they 
are inside the resources folder of the mars-sim-core 
maven submodule.
 
 
## Editing
  
- Hover the mouse cursor over the mars-sim jarfile
- Right click on the jarfile to bring up a menu
- Choose the option `7-Zip` and `Open archive`
- In the 7-zip File Manager, go to the directory `\conf`
- Right click on a xml file of your interest
- Choose `Edit` to open up that xml file
 
 
## Backup

It is recommended that you make a backup of the original configuration
file before editing it as user-created XML errors can cause mars-sim
fail to start.


## List of xml files

| Filename | Purpose |
| --- | --- |
| buildings.xml | Define new buildings with functions |
                  
| construction.xml | Define type of  foundations, frames and buildings |
               
| crops.xml | Define food crops grown in greenhouses  |

| foodProduction.xml | Define food technology related processes  |
           
| landmarks.xml | Define landmarks on the surface of Mars |
               
| malfunctions.xml | Define malfunctions that can occur in the sim  |
                  
| manufacturing.xml | Define manufacturing processes  | 

| meals.xml | Define meal recipes  |                 
                  
| medical.xml | Define illnesses or treatments  |

| minerals.xml | Define mineral types  |

| part_packages.xml | Define part packages for initial settlements or resupplies from Earth  |

| parts.xml | Define parts  |

| people.xml | Store the alpha crew roster and define properties related to people |
                  
| resources.xml | Define resources  |

| resupplies.xml | Define initial settlement resupply packages from Earth  |
                  
| settlements.xml | Store settlement templates and define properties related to settlements |
                  
| simulation.xml | Define simulation properties  |

| vehicles.xml | Define properties related to vehicles and rovers |