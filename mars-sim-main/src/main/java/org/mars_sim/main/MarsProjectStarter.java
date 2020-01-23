/**
 * Mars Simulation Project
 * MarsProjectStarter.java
 * @version 3.1.0 2017-01-31
 * @author Scott Davis
 */

package org.mars_sim.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.mars_sim.headless.StreamConsumer;

/**
 * MarsProjectStarter is the default main class for the main executable JAR.
 * It creates a new virtual machine with 1GB memory and logging properties.
 * It isn't used in the webstart release.
 */
public class MarsProjectStarter {

//	private final static String ERROR_PREFIX = "? ";

    public static void main(String[] args) {

        StringBuilder command = new StringBuilder();

        String javaHome = System.getenv("JAVA_HOME");
        if (javaHome != null) {
            if (javaHome.contains(" "))
            	javaHome = "\"" + javaHome;

            command.append(javaHome)
            .append(File.separator)
//            .append("bin")
//            .append(File.separator)
            .append("java");

            if (javaHome.contains(" "))
            	command.append("\"");
        }
        else 
        	command.append("java");

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
        command.append(" --add-opens java.base/java.util=ALL-UNNAMED")
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
        .append(" --add-opens java.desktop/javax.swing.plaf.synth=ALL-UNNAMED")
        
        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED")
        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.gtk=ALL-UNNAMED")
        .append(" --add-opens java.desktop/com.apple.laf=ALL-UNNAMED")
        
//        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.windows")
//        .append(" --add-opens java.desktop/com.sun.java.swing.plaf.gtk")
//        .append(" --add-opens java.desktop/com.apple.laf");
        .append(" --illegal-access=deny");
        
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

		        if (argList.contains("headless") || argList.contains("-headless"))
		        	command.append(" -headless");

		        if (argList.contains("new") || argList.contains("-new"))
		        	command.append(" -new");

		        else if (argList.contains("load") || argList.contains("-load")) {
		        	command.append(" -load");

		        	// 2016-10-06 Appended the name of loadFile to the end of the command stream so that MarsProjectFX can read it.
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
		        	//System.out.println("Note: it's missing 'new' or 'load'. Assume you want to start a new sim now.");
		        	command.append(" -new");
		        }

	        }

	        if (argList.contains("noaudio") || argList.contains("-noaudio")) 
	        	command.append(" -noaudio");
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