/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
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

        List<String> argList = Arrays.asList(args);
        

        if (argList.isEmpty()) {
        	// by default, use gui and 1GB
            command.append(" -Xms256m");
            command.append(" -Xmx1024m");
        	command.append(" -new");
        }
        
        else { 
        	
	        if (argList.contains("5")) {
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
	        
	        
	        if (argList.contains("html"))
	        	command.append(" -generateHelp");
	        
	        else {
	        
		        if (argList.contains("headless"))
		        	command.append(" -headless");
		        
		        if (!argList.contains("load") && argList.contains("new"))
		        	command.append(" -new");
	        
	        }

        }
/*        
    	if (args == null) {
        	String[] defaultArray = {"0", "gui", "new"};
    		args = defaultArray;
    	}
    	
    	else {
    	 	
        	int x0 = 0;

        	if (args[0] != null || !args[0].isEmpty())
        		x0 = Integer.parseInt(args[0]);

            switch(x0) {
    	        
    	        // 0. 1.5 GB // if no arg
    	    	// 1. 1.5 GB
    	    	// 2. 1 GB
    	    	// 3. 768 MB
    	    	// 4. 512 MB
    	        
    	        case 0: 
    	            command.append(" -Xms256m");
    	            command.append(" -Xmx1536m");
    	            //command.append(" -Xmx2048m");
    	            break;    	
    	        
    	        case 1: 
    	            command.append(" -Xms256m");
    	            //command.append(" -Xmx1024m");
    	            command.append(" -Xmx1536m");
    	            break;
    	        case 2: 
    	            command.append(" -Xms256m");
    	            //command.append(" -Xmx1024m");
    	            command.append(" -Xmx1024m");
    	            break;
    	        case 3: 
    	            command.append(" -Xms256m");
    	            //command.append(" -Xmx1024m");
    	            command.append(" -Xmx768m");
    	            break;
    	        case 4: 
    	            command.append(" -Xms256m");
    	            //command.append(" -Xmx1024m");
    	            command.append(" -Xmx512m");
    	            break;
    	 
    	        default :System.out.println("The 1st argument is invalid!");
    	    }  

            String x1 = null;
           	if (args[1] != null || !args[1].isEmpty())
        		x1 = args[1];

            switch(x1) {
            
    	        case "null": 
    	            break;    	
    	        
    	        case "headless": 
    	            command.append(" -headless");
    	            break;
    	        case "gui": 
    	            //command.append(" -gui");
    	            break;
    	        case "html": 
    	            command.append(" -generateHelp");
    	            break;            
    	            
    	        default : System.out.println("The 2nd argument is invalid");
            }
            
            String x2 = null;
           	if (args[2] != null || !args[2].isEmpty())
        		x2 = args[2];

            switch(x2) {
            
    	        case "null": 
    	            command.append(" -new");
    	            break;    	
    	        
    	        case "new": 
    	            command.append(" -new");
    	            break;
    	        case "load": 
    	            //command.append(" -load");
    	            break;

    	        default : System.out.println("The 3rd argument is invalid");
            }             		
    	} 	
*/   
        
        
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