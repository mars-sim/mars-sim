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
import java.util.Arrays;
import java.util.List;

/**
 * MarsProjectHeadlessStarter is the main class for running the main executable
 * JAR in purely headless mode. It creates a new virtual machine with 1GB memory
 * and logging properties. It isn't used in the webstart release.
 */
public class MarsProjectHeadlessStarter {

//	private final static String ERROR_PREFIX = "? ";

	public static void main(String[] args) {

		StringBuilder command = new StringBuilder();

		String javaHome = System.getenv("JAVA_HOME");
		
		if (javaHome != null) {
			if (javaHome.contains(" "))
				javaHome = "\"" + javaHome;

			command.append(javaHome).append(File.separator).append("bin").append(File.separator).append("java");

			if (javaHome.contains(" "))
				command.append("\"");
		} else
			command.append("java");

		// command.append(" -Dswing.aatext=true");
		// command.append(" -Dswing.plaf.metal.controlFont=Tahoma"); // the compiled jar
		// won't run
		// command.append(" -Dswing.plaf.metal.userFont=Tahoma"); // the compiled jar
		// won't run
		// command.append(" -generateHelp");
		// command.append(" -new");

		command.append(" -Djava.util.logging.config.file=logging.properties").append(" -cp .")
				.append(File.pathSeparator).append("*").append(File.pathSeparator).append("jars").append(File.separator)
				.append("*").append(" org.mars_sim.headless.MarsProjectHeadless");

		// Add checking for input args
		List<String> argList = Arrays.asList(args);

		if (argList.isEmpty()) {
			// by default, use gui and 1GB
			command.append(" -Xms256m");
			command.append(" -Xmx1024m");
			command.append(" -new");
		}

		else {
			// Check for the memory switch
			if (argList.contains("5") || argList.contains("-5")) {// || argList.contains("5 ")) {
				command.append(" -Xms256m");
				command.append(" -Xmx2048m");
			} else if (argList.contains("4") || argList.contains("-4")) {
				command.append(" -Xms256m");
				command.append(" -Xmx1536m");
			} else if (argList.contains("3") || argList.contains("-3")) {
				command.append(" -Xms256m");
				command.append(" -Xmx1024m");
			} else if (argList.contains("2") || argList.contains("-2")) {
				command.append(" -Xms256m");
				command.append(" -Xmx768m");
			} else if (argList.contains("1") || argList.contains("-1")) {
				command.append(" -Xms256m");
				command.append(" -Xmx512m");
			} else if (argList.contains("0") || argList.contains("-0")) {
				command.append(" -Xms256m");
				command.append(" -Xmx1024m");
			} else {
				// Use 1GB by default
				command.append(" -Xms256m");
				command.append(" -Xmx1024m");
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
				if (argList.contains("new") || argList.contains("-new"))
					command.append(" -new");

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
					command.append(" -new");
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
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
}