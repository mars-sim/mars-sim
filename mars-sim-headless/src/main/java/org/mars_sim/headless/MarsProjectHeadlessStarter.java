/**
 * Mars Simulation Project
 * MarsProjectHeadlessStarter.java
* @version 3.1.0 2018-06-14
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.headless;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * MarsProjectHeadlessStarter is the main class for running the main executable
 * JAR in purely headless mode. It creates a new virtual machine with 1GB memory
 * and logging properties. It isn't used in the webstart release.
 */
public class MarsProjectHeadlessStarter {

//	private final static String ERROR_PREFIX = "? ";

	private static final String JAVA = "java";
	private static final String JAVA_HOME = "JAVA_HOME";
//	private static final String BIN = "bin";
	private static final String ONE_WHITESPACE = " ";
	
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

		StringBuilder command = new StringBuilder();

		String javaHome = System.getenv(JAVA_HOME);
		
		if (javaHome != null) {
			if (javaHome.contains(ONE_WHITESPACE))
				javaHome = "\"" + javaHome;

			command
			.append(javaHome)
			.append(File.separator)
//			.append(BIN)
//			.append(File.separator)
			.append(JAVA);

			if (javaHome.contains(ONE_WHITESPACE))
				command.append("\"");
		} 
		else {
			command.append(JAVA);
		}
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
        
		command.append(" -Djava.util.logging.config.file=logging.properties").append(" -cp .")
				.append(File.pathSeparator)
				.append("*")
				.append(File.pathSeparator)
				.append("jars")
				.append(File.separator)
				.append("*")
				.append(" org.mars_sim.headless.MarsProjectHeadless");

		// Add checking for input args
		List<String> argList = Arrays.asList(args);

		boolean isNew = false;
		
		if (argList.isEmpty()) {
			// by default, use gui and 1.5 GB
			command.append(" -Xms256m");
            command.append(" -Xmx1536m");
			command.append(" -new");
		}

		else {
			// Check for the memory switch
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
			} else {
				// Use 1.5 GB by default
				command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
			}

			// Check for the help switch
			if (argList.contains("help") || argList.contains("-help")) {
				command.append(" -help");
				// System.out.println(manpage);
			}

			// Check for the html switch			
			else if (argList.contains("html") || argList.contains("-html")) {
				// command.append(" -new");
				command.append(" -html");
			}

			else {

				// Check for the headless switch
//				if (argList.contains("headless") || argList.contains("-headless"))
//					command.append(" -headless");

				// Check for the new switch
				if (argList.contains("new") || argList.contains("-new")) {
					isNew = true;	
				}
				
				// Check for the load switch
				else if (argList.contains("load") || argList.contains("-load")) {
					command.append(" -load");

					// Append the name of loadFile to the end of the command stream so
					// that MarsProjectFX can read it.
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
		
		// Check for noaudio switch
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