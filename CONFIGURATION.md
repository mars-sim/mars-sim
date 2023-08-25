# Configuration
`Revised : 24 Aug 2023`

Most of the Mars Simulation Project's configuration files are 
primarily written in XML format. This guide will get you 
started with editing these files.

Most attributes and properties are designed to 
tolerate a range of values. However, they are not all 
created equal to have the same degree of user customization. 

Do file a [ticket](https://github.com/mars-sim/mars-sim/issues) or 
open a [discussion topic](https://github.com/mars-sim/mars-sim/discussions) 
with us with any ideas you have in mind.


## Utility Tool

In order to manipulate these xml files inside the jar file, 
we recommend installing the [7-Zip File Manager](https://www.7-zip.org/)
that allows users to manipulate files located insides a jar file 
on the fly without having to manually compressing, uncompressing,
 copying and deleting files.


## Location

Players may manipulate these XML files in /.mar-sim/xml
folder.


## Backup

It is recommended that you make a backup of the original xml 
configuration file before editing it as the edited XML files 
often contain errors that can cause mars-sim fail to start.

Whenever a new version of mars-sim and is being run in user machine, it will attempts to compare the content of the `version.txt` file in the `/.mars-sim/xml` folder to determine if it matches the core engine's build version. 

If they don't match, mars-sim will attempt to backup the existing XML files on user home into a new directory inside the `backup` folder. e.g. `/backup/${build_version}`, where ${build_version} could be, say, `5004`


## List of xml files

| Filename | Purpose |
| --- | --- |
| buildings.xml | Define new buildings with functions |                  
| construction.xml | Define type of foundations, frames and buildings |

| country_austria.xml | Define settler's names for the country of Austria |
| country_belgium.xml | Define settler's names for the country of Belgium |
| country_canada.xml  | Define settler's names for the country of Canada |
| country_china.xml           | Define settler's names for the country of China |
| country_czech_republic.xml | Define settler's names for the country of Czech Republic |
| country_denmark.xml | Define settler's names for the country of Denmark |
| country_estonia.xml | Define settler's names for the country of Estonia |
| country_finland.xml | Define settler's names for the country of Finland |
| country_france.xml | Define settler's names for the country of France |
| country_germany.xml | Define settler's names for the country of Germany |
| country_greece.xml | Define settler's names for the country of Greece |
| country_hungary.xml | Define settler's names for the country of Hungary |
| country_india.xml | Define settler's names for the country of India |
| country_ireland.xml | Define settler's names for the country of Ireland |
| country_italy.xml  | Define settler's names for the country of Italy |
| country_japan.xml           | Define settler's names for the country of Japan |
| country_luxembourg.xml | Define settler's names for the country of Luxembourg |
| country_norway.xml | Define settler's names for the country of Norway |
| country_poland.xml | Define settler's names for the country of Poland |
| country_portugal.xml | Define settler's names for the country of Portugal |
| country_romania.xml | Define settler's names for the country of Romania |
| country_russia.xml | Define settler's names for the country of Russia |
| country_south_korea.xml | Define settler's names for the country of South Korea |
| country_spain.xml | Define settler's names for the country of Spain |
| country_sweden.xml | Define settler's names for the country of Sweden |
| country_switzerland.xml | Define settler's names for the country of Switzerland |
| country_the_netherlands.xml | Define settler's names for the Netherlands |
| country_uk.xml | Define settler's names for the country of UK |
| country_usa.xml | Define settler's names for the country of USA |
| country.xsd | Define the xsd scheme for country-related xmls |

| crew_alpha.xml | Store the alpha crew roster |
| crew_founders.xml | Store the founders roster |
| crew.xsd | Define the xsd scheme for crew-related xmls |

| scenario_default.xml | Define the default settlement scenario |
| scenario_single_settlement.xml | Define the single settlement scenario |
| scenario.xsd | Define the xsd scheme for scenario-related xmls |

| crops.xml | Define food crops grown in greenhouses |
| foodProduction.xml | Define food technology related processes |
| governance.xml | Define various default mission agenda, as well as sponsors' settlement and rover names |
| landmarks.xml | Define landmarks on the surface of Mars |  
| malfunctions.xml | Define malfunctions that can occur in the sim |
| manufacturing.xml | Define manufacturing processes  |
| meals.xml | Define meal recipes |
| medical.xml | Define illnesses or treatments |
| minerals.xml | Define mineral types |
| part_packages.xml | Define part packages for initial settlements or resupplies from Earth |
| parts.xml | Define parts  |
| people.xml | Define properties related to people |
| quotations.xml | Define a list of space-related quotes |
| resourceprocess.xml | Define resource processes |
| resources.xml | Define resources |
| resupplies.xml | Define initial settlement resupply packages from Earth |
| robots.xml | Define robot types |
| scenarios.xml | Define two simulation scenarios |
| settlements.xml | Store settlement templates and define properties related to settlements |   
| simulation.xml | Define simulation properties |
| vehicles.xml | Define properties related to vehicles and rovers |


## Further Information

You can find out more information about mars-sim in its [Github](
https://github.com/mars-sim/mars-sim) page.


