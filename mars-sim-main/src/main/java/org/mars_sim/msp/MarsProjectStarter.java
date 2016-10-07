/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 3.07 2014-12-06
 * @author Scott Davis
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * MarsProjectStarter is the default main class for the main executable JAR.
 * It creates a new virtual machine with 1GB memory and logging properties.
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

        //command.append(" -Dswing.aatext=true");
        //command.append(" -Dswing.plaf.metal.controlFont=Tahoma"); // the compiled jar won't run
        //command.append(" -Dswing.plaf.metal.userFont=Tahoma"); // the compiled jar won't run
        //command.append(" -generateHelp");
        //command.append(" -new");   
        
        command.append(" -Djava.util.logging.config.file=logging.properties");
        command.append(" -cp .").append(File.pathSeparator);
        command.append("*").append(File.pathSeparator);
        command.append("jars").append(File.separator).append("*");
        //command.append(" org.mars_sim.msp.MarsProject");
        command.append(" org.mars_sim.msp.javafx.MarsProjectFX");

        // 2016-05-28 Added checking for input args
        List<String> argList = Arrays.asList(args);
        
        if (argList.isEmpty()) {
        	// by default, use gui and 1GB
            command.append(" -Xms256m");
            command.append(" -Xmx1024m");
        	command.append(" -new");
        }
        
        else { 
        	
	        if (argList.contains("5")) {// || argList.contains("5 ")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx2048m");
	        }
	        else if (argList.contains("4")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1536m");
	        }
	        else if (argList.contains("3")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1024m");
	        }        	
	        else if (argList.contains("2")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx768m");
	        }   
	        else if (argList.contains("1")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx512m");
	        }    
	        else if (argList.contains("0")) {
	            command.append(" -Xms256m");
	            command.append(" -Xmx1024m");
	        }    
	        else {
	        	//  use 1GB by default
	            command.append(" -Xms256m");
	            command.append(" -Xmx1024m");
	        }
	        
	        
	        if (argList.contains("html")) {
	        	//command.append(" -new");	        	
	        	command.append(" -html");
	        }
	        else {
	        
		        if (argList.contains("headless"))
		        	command.append(" -headless");
		        
		        if (argList.contains("new"))
		        	command.append(" -new");
		        else if (argList.contains("load")) {
		        	command.append(" -load");
		        
		        	// 2016-10-06 Appended the name of loadFile to the end of the command stream so that MarsProjectFX can read it.
		        	int index = argList.indexOf("-load");
		        	int size = argList.size();
		        	String fileName = null;
		        	if (size > index + 1) { // TODO : will it help to catch IndexOutOfBoundsException
		            // Get the next argument as the filename.
		        		fileName = argList.get(index + 1);
		        		command.append(" " + fileName);
		        	}
		        }
		        else {
		        	//System.out.println("Note: it's missing 'new' or 'load'. Assume you want to start a new sim now.");
		        	command.append(" -new");
		        }
	        
	        }

        }     
        
        String commandStr = command.toString();
        System.out.println("Command: " + commandStr);
        
        try {
            Process process = Runtime.getRuntime().exec(commandStr);

            // Creating stream consumers for processes.
            StreamConsumer errorConsumer = new StreamConsumer(process.getErrorStream(), "OUTPUT");
            StreamConsumer outputConsumer = new StreamConsumer(process.getInputStream(), "OUTPUT");

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