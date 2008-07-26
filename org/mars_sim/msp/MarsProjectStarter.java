/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 2.85 2008-07-26
 * @author Scott Davis
 */

package org.mars_sim.msp;

import java.io.File;
import java.io.IOException;

/**
 * MarsProjectStarter is the default main class for the MarsProject.jar executable JAR.
 * It creates a new virtual machine with 256MB memory and logging properties.
 */
public class MarsProjectStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			StringBuffer command = new StringBuffer();
			
			String javaHome = System.getenv("JAVA_HOME");
			if (javaHome != null) {
				if (javaHome.contains(" ")) javaHome = "\"" + javaHome + "\"";
				command.append(javaHome + File.separator + "bin" + File.separator + "java ");
			}
			else command.append("java ");
			
			command.append("-Xms256m ");
			command.append("-Xmx256m ");
			command.append("-Djava.util.logging.config.file=logging.properties ");
			command.append("-cp MarsProject.jar ");
			command.append("org.mars_sim.msp.MarsProject");
			
			String commandStr = command.toString();
			System.out.println("Command: " + commandStr);
			Runtime.getRuntime().exec(commandStr);
		}
		catch(IOException e) {
			System.out.println("Error starting MarsProject");
			e.printStackTrace(System.err);
		}
	}
}