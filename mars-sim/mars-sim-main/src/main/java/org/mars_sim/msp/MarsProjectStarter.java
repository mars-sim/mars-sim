/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp;

import java.io.File;
import java.io.IOException;

/**
 * MarsProjectStarter is the default main class for the main executable JAR.
 * It creates a new virtual machine with 256MB memory and logging properties.
 * It isn't used in the webstart release.
 */
public class MarsProjectStarter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
            StringBuilder command = new StringBuilder();
			
			String javaHome = System.getenv("JAVA_HOME");
			if (javaHome != null) {
				if (javaHome.contains(" ")) javaHome = "\"" + javaHome;
                command.append(javaHome).append(File.separator).append("bin").append(File.separator).append("java");
				if (javaHome.contains(" ")) command.append("\"");
			}
			else command.append("java");
			
			command.append(" -Xms256m");
			command.append(" -Xmx256m");
			command.append(" -Djava.util.logging.config.file=logging.properties");
            command.append(" -cp .").append(File.pathSeparator);
            command.append("*").append(File.pathSeparator);
            command.append("jars").append(File.separator).append("*");
			command.append(" org.mars_sim.msp.MarsProject");
			
			String commandStr = command.toString();
			System.out.println("Command: " + commandStr);
        try {
            Process process = Runtime.getRuntime().exec(commandStr);

            process.waitFor();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}