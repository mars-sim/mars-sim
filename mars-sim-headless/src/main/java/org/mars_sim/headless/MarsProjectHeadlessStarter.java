/**
 * Mars Simulation Project
 * MarsProjectHeadlessStarter.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.headless;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * MarsProjectHeadlessStarter is the main class for running the executable
 * JAR in pure console mode. It creates a new virtual machine with logging properties. 
 * It isn't used in the webstart release.
 */
public class MarsProjectHeadlessStarter {

//	private final static String ERROR_PREFIX = "? ";

	private static final String JAVA = "java";
	private static final String JAVA_HOME = "JAVA_HOME";
	private static final String BIN = "bin";
	private static final String ONE_WHITESPACE = " ";
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static void main(String[] args) {
	    // Add checking for input args
		List<String> argList = Arrays.asList(args);
		
		StringBuilder command = new StringBuilder();

		String javaHome = System.getenv(JAVA_HOME);
		
	    System.out.println("      JAVA_HOME : " + javaHome);
        
        System.out.println(" File.separator : " + File.separator);
        
 		if (javaHome != null) {
 			if (javaHome.contains(ONE_WHITESPACE))
 				javaHome = "\"" + javaHome;

 			String lastChar = javaHome.substring(javaHome.length() - 1);
 			
 			if (lastChar.equalsIgnoreCase(File.separator)) {
 				if (javaHome.contains(BIN)) {
 					command
 					.append(javaHome)
 					.append(JAVA);
 				}
 				else {
 					command
 					.append(javaHome)
 					.append(BIN)
 					.append(File.separator)
 					.append(JAVA);
 				}	
 			}
 			else {
 				if (javaHome.contains(BIN)) {
 					command
 					.append(javaHome)
 					.append(File.separator)
 					.append(JAVA);
 				}
 				else {
 					command
 					.append(javaHome)
 					.append(File.separator)
 					.append(BIN)
 					.append(File.separator)
 					.append(JAVA);
 				}		
 			}
 			
 			if (javaHome.contains(ONE_WHITESPACE))
 				command.append("\"");

// 		    System.out.println("      JAVA_HOME : " + javaHome);
 	        System.out.println("   Java Command : " + command.toString());
 	        
 		}
		else {
			command.append(JAVA);
		}
		
//		command.append(" --illegal-access=deny");
        
        // Check OS
        if (OS.indexOf("win") >= 0)
        	command.append(" --add-opens java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED");
        else if (OS.indexOf("mac") >= 0)
        	command.append(" --add-opens java.desktop/com.apple.laf=ALL-UNNAMED");
        else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || OS.indexOf("sunos") >= 0)
            command.append(" --add-opens java.desktop/com.sun.java.swing.plaf.gtk=ALL-UNNAMED");
		
		// command.append(" -Dswing.aatext=true");
		// command.append(" -Dswing.plaf.metal.controlFont=Tahoma"); // the compiled jar
		// won't run
		// command.append(" -Dswing.plaf.metal.userFont=Tahoma"); // the compiled jar
		// won't run
		// command.append(" -generateHelp");
		// command.append(" -new");

        // Use new Shenandoah Garbage Collector from Java 12 
//        command.append(" -XX:+UnlockExperimentalVMOptions")
//        	.append(" -XX:+UseShenandoahGC");
//        	.append(" -Xlog:gc*");
        
        // Set up logging
		command.append(" -Djava.util.logging.config.file=logging.properties").append(" -cp .")
				.append(File.pathSeparator)
				.append("*")
				.append(File.pathSeparator)
				.append("jars")
				.append(File.separator)
				.append("*")
				.append(" org.mars_sim.headless.MarsProjectHeadless");

		
		boolean isNew = false;
		
        if (argList.isEmpty()) {
        	// by default, use gui and 1.5 GB
//            command.append(" -Xms256m");
            command.append(" -Xmx1536m");
        	command.append(" -new");
        }

        else {
        	
        	if (argList.contains("-noremote")) {
 	            command.append(" -noremote");
 	        }
        	
        	if (argList.contains("-resetadmin")) {
 	            command.append(" -resetadmin");
 	        }

	        if (argList.contains("-3")) {
//	            command.append(" -Xms256m");
	            command.append(" -Xmx3072m");
	        }
	        else if (argList.contains("-2.5")) {
//	            command.append(" -Xms256m");
	            command.append(" -Xmx2560m");
	        }
	        else if (argList.contains("-2")) {
//	            command.append(" -Xms256m");
	            command.append(" -Xmx2048m");
	        }
	        else if (argList.contains("-1.5")) {
//	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }
	        else if (argList.contains("-1")) {
//	            command.append(" -Xms256m");
	            command.append(" -Xmx1024m");
	        }
	        else {
	        	//  use 1.5 GB by default
//	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }

	        if (argList.contains("-help")) {
	        	command.append(" -help");
	        }

	        else if (argList.contains("-html")) {
	        	command.append(" -html");
	        }

	        else {
				// Check for the headless switch
		        if (argList.contains("-headless"))
		        	command.append(" -headless");

				// Check for the new switch
				if (argList.contains("-new")) {
					isNew = true;	
				}
				
		        else if (argList.contains("-load")) {
		        	command.append(" -load");

		        	// Appended the name of loadFile to the end of the command stream so that MarsProjectFX can read it.
		        	int index = argList.indexOf("load");
		        	int size = argList.size();
		        	String fileName = null;
		        	if (size > index + 1) { // TODO : will it help to catch IndexOutOfBoundsException
		            // Get the next argument as the filename.
		        		fileName = argList.get(index + 1);
		        		command.append(" " + fileName);
		        	}
		        }

		        else {
					// System.out.println("Note: it's missing 'new' or 'load'. Assume you want to
					// start a new sim now.");
						
					isNew = true;
				}
			}
        }

		if (isNew) {
			command.append(" -new");
			
			for (String s: argList) {

				if (StringUtils.containsIgnoreCase(s, "-remote ")) {
					command.append(" " + s);
				}
				
				if (StringUtils.containsIgnoreCase(s, "-sponsor ")) {
					command.append(" " + s);
				}
						
				if (StringUtils.containsIgnoreCase(s, "-template ")) {
					command.append(" " + s);
				}
				
				if (StringUtils.containsIgnoreCase(s, "-lat ")) {
					command.append(" " + s);
				}
				
				if (StringUtils.containsIgnoreCase(s, "-lon ")) {
					command.append(" " + s);
				}	
				
				if (StringUtils.containsIgnoreCase(s, "-timeratio ")) {
					command.append(" " + s);
				}	
				
				if (StringUtils.containsIgnoreCase(s, "-datadir ")) {
					command.append(" " + s);
				}	
			}		
		}

        if (argList.contains("-noaudio")) 
        	command.append(" -noaudio");
        
        if (argList.contains("-nogui")) 
        	command.append(" -nogui");

        String commandStr = command.toString();
        System.out.println("Command: " + commandStr);

        try {
            Process process = Runtime.getRuntime().exec(commandStr);

            // Creating stream consumers for processes.
            StreamConsumer errorConsumer = new StreamConsumer(process.getErrorStream(), "");
            StreamConsumer outputConsumer = new StreamConsumer(process.getInputStream(), "");

            // Starting the stream consumers.
            errorConsumer.start();
            outputConsumer.start();

            process.waitFor();

            // Close stream consumers.
            errorConsumer.join();
            outputConsumer.join();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e1) {
        	e1.printStackTrace();
        } catch (Exception e2) {
        	e2.printStackTrace();        	
        }
    }
}
