/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mars_sim.headless.StreamConsumer;

/**
 * MarsProjectStarter is the default main class for the main executable JAR.
 * It creates a new virtual machine with logging properties.
 * It isn't used in the webstart release.
 */
public class MarsProjectStarter {

//	private final static String ERROR_PREFIX = "? ";
	private static final String JAVA = "java";
	private static final String JAVA_HOME = "JAVA_HOME";
	private static final String BIN = "bin";
	private static final String ONE_WHITESPACE = " ";
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	private static List<String> templates = new ArrayList<>();
	
	static {
		templates.add("template:1");
		templates.add("template:1A");
		templates.add("template:1B");
		templates.add("template:1C");
		templates.add("template:1D");
		templates.add("template:2");
		templates.add("template:2A");
		templates.add("template:2B");
		templates.add("template:2C");
		templates.add("template:2D");
		templates.add("template:3");
		templates.add("template:3A");
		templates.add("template:3B");
		templates.add("template:3C");
		templates.add("template:3D");
		templates.add("template:4");
	}
	
	public static List<String> getTemplates() {
		return templates;
	}
	
	public static void main(String[] args) {
	   start(args);
	}
	
	public static void start(String[] args) {

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

 		    System.out.println("      JAVA_HOME : " + javaHome);
 	        System.out.println("   Java Command : " + command.toString());
 	        
 		}
		else {
			command.append(JAVA);
		}

        //command.append(" -Dswing.aatext=true");
        //command.append(" -Dswing.plaf.metal.controlFont=Tahoma"); // the compiled jar won't run
        //command.append(" -Dswing.plaf.metal.userFont=Tahoma"); // the compiled jar won't run
        //command.append(" -generateHelp");
        //command.append(" -new");

        // Use new Shenandoah Garbage Collector from Java 12 
//        command.append(" -XX:+UnlockExperimentalVMOptions")
//        	.append(" -XX:+UseShenandoahGC");
//        	.append(" -Xlog:gc*");
        
        // Take care of the illegal reflective access for Java 12+
        command //.append(" --illegal-access=deny")      
        .append(" --add-opens java.base/java.util=ALL-UNNAMED")
        .append(" --add-opens java.base/java.text=ALL-UNNAMED")
        .append(" --add-opens java.base/java.lang.reflect=ALL-UNNAMED")
        .append(" --add-opens java.base/java.net=ALL-UNNAMED")
        .append(" --add-opens java.base/java.lang=ALL-UNNAMED")
        .append(" --add-opens java.base/jdk.internal.loader=ALL-UNNAMED")
        .append(" --add-opens java.desktop/javax.swing=ALL-UNNAMED")
        .append(" --add-opens java.desktop/javax.swing.text=ALL-UNNAMED")
        .append(" --add-opens java.desktop/java.awt.font=ALL-UNNAMED")
        .append(" --add-opens java.desktop/java.awt.geom=ALL-UNNAMED")
        .append(" --add-opens java.desktop/java.awt=ALL-UNNAMED")
        .append(" --add-opens java.desktop/java.beans=ALL-UNNAMED")
        .append(" --add-opens java.desktop/javax.swing.table=ALL-UNNAMED")
        .append(" --add-opens java.desktop/com.sun.awt=ALL-UNNAMED")
        .append(" --add-opens java.desktop/sun.awt=ALL-UNNAMED")
        .append(" --add-opens java.desktop/sun.swing=ALL-UNNAMED")
        .append(" --add-opens java.desktop/sun.font=ALL-UNNAMED")
        .append(" --add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED")
        .append(" --add-opens java.desktop/javax.swing.plaf.synth=ALL-UNNAMED");
        
//        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED")
//        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.gtk=ALL-UNNAMED")
//        .append(" --add-opens java.desktop/com.apple.laf=ALL-UNNAMED");
        
        
        // Check OS
        if (OS.indexOf("win") >= 0)
        	command.append(" --add-opens java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED");
        else if (OS.indexOf("mac") >= 0)
        	command.append(" --add-opens java.desktop/com.apple.laf=ALL-UNNAMED");
        else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 || OS.indexOf("sunos") >= 0)
            command.append(" --add-opens java.desktop/com.sun.java.swing.plaf.gtk=ALL-UNNAMED");
        
        // Set up logging
        command.append(" -Djava.util.logging.config.file=logging.properties")
        	.append(" -cp .")
        	.append(File.pathSeparator)
        	.append("*")
        	.append(File.pathSeparator)
        	.append("jars")
        	.append(File.separator)
        	.append("*")
//        	.append(" org.mars_sim.main.javafx.MarsProjectFX"); 
        // OR 
        .append(" org.mars_sim.main.MarsProject");
        // OR .append(" org.mars_sim.headless.MarsProject");
        
        // Add checking for input args
        List<String> argList = Arrays.asList(args);

		boolean isNew = false;
		
        if (argList.isEmpty()) {
        	// by default, use gui and 1GB
            command.append(" -Xms256m");
//            command.append(" -Xmx1024m");
            command.append(" -Xmx1536m");
        	command.append(" -new");
        }

        else {

	        if (argList.contains("5") || argList.contains("-5")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx3072m");
	        }
	        else if (argList.contains("4") || argList.contains("-4")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx2560m");
	        }
	        else if (argList.contains("3") || argList.contains("-3")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx2048m");
	        }
	        else if (argList.contains("2") || argList.contains("-2")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }
	        else if (argList.contains("1") || argList.contains("-1")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1024m");
	        }
	        else if (argList.contains("0") || argList.contains("-0")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }
	        else {
	        	//  use 1.5 GB by default
	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }

	        if (argList.contains("help") || argList.contains("-help")) {
	        	command.append(" -help");
	        	//System.out.println(manpage);
	        }

	        else if (argList.contains("html") || argList.contains("-html")) {
	        	//command.append(" -new");
	        	command.append(" -html");
	        }

	        else {
				// Check for the headless switch
		        if (argList.contains("headless") || argList.contains("-headless"))
		        	command.append(" -headless");

				// Check for the new switch
				if (argList.contains("new") || argList.contains("-new")) {
					isNew = true;	
				}
				
		        else if (argList.contains("load") || argList.contains("-load")) {
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
				if (StringUtils.containsIgnoreCase(s, "-country:")) {
					command.append(" " + s);
				}
				
				if (StringUtils.containsIgnoreCase(s, "-sponsor:")) {
					command.append(" " + s);
				}
						
				if (StringUtils.containsIgnoreCase(s, "-template:")) {
					command.append(" " + s);
				}
			}		
		}

        if (argList.contains("noaudio") || argList.contains("-noaudio")) 
        	command.append(" -noaudio");

    	// Check for time-ratio switches
		if (argList.contains("512x") || argList.contains("-512x")) {// time ratio is 512x
			command.append(" -512x");
		}

		else if (argList.contains("1024x") || argList.contains("-1024x")) {// time ratio is 1024x
			command.append(" -1024x");
		}

		else if (argList.contains("2048x") || argList.contains("-2048x")) {// time ratio is 2048x
			command.append(" -2048x");
		}

		else if (argList.contains("4096x") || argList.contains("-4096x")) {// time ratio is 4096x
			command.append(" -4096x");
		}

		else if (argList.contains("8192x") || argList.contains("-8192x")) {// time ratio is 8192x
			command.append(" -8192x");
		}
		
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
