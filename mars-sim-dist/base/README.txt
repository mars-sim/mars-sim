The distribution supports both Windows and Linux.
It also provides 2 varients of the mars-sim application.
1. Swing based UI
2. Console based engine that is accessed via SSH

Start commands are:
* bin/mars-sim-swing 		- Linux start script for the Swing varient
* bin/mars-sim-swing.cmd 	- Window start script for the Swing varient
* bin/mars-sim-console 		- Linux start script for the Console headless varient
* bin/mars-sim-console.cmd 	- Window start script for the Console headless varient

The mars-sim application requires Java 17 or higher to be installed. 
See https://jdk.java.net/java-se-ri/17

Common command line arguments are:
 -baseurl <URL to remote content>    URL to the remote content repository
                                     (defaults to master in GitHub)
 -crew <true|false>                  Enable or disable use of the crews
 -datadir <path to data directory>   Path to the data directory for
                                     simulation files (defaults to user.home)
 -diags <<module>,<module>.....>     Enable diagnositics modules
 -help                               Display help options
 -lat <latitude>                     Set the latitude of the new template Settlement
 -load <path to simulation file>     Load the a previously saved sim. No argument open file
                                     selection dialog. 'default' will use default
 -lon <longitude>                    Set the longitude of the new template Settlement
 -new                                Enable quick start
 -scenario <scenario name>           New simulation from a scenario

 -sponsor <sponsor>                  Set the sponsor for the settlement template
 -template <template name>           New simulation from a template
 -timeratio <Ratio (power of 2)>     Define the time ratio of the
                                     simulation

Swing varient arguments
 -cleanui                            Disable loading stored UI configurations
 -noaudio                            Disable the audio
 -nogui                              Disable the main UI
 -profile                            Set up the Commander Profile
 -sandbox                            Start in Sandbox Mode
 -site                               Start the Scenario Editor
 
Console varient arguments
 -noremote                           Do not start a remote console service
 -remote <port number>               Run the remote console service [default]
 -resetadmin                         Reset the internal admin password