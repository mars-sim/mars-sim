/*
 * Mars Simulation Project
 * VersionTag.java
 * @date 2022-07-17
 * @author Manny Kung
 */

package org.mars_sim.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/*
 * This class automatically generates or updates the header of your files 
 * with a proper date tag. 
 */
public class VersionTag {
	
	// CAUTION: Please test the setting on a set of demo files first and never
	// run it the first time on files without a proper backup.
		
	private static String versionTagID = "* @version";
	// Please update the following to the next official release date
	private static String dateTagString = " * @date 2022-07-17"; 
	// The affected path for the git clone repo on your machine 
	private static String TOP_DIR = "D:/eclipse/java-2021-03/git/mars-sim/mars-sim";
			
	private static String LINE0 = "/**";
	private static String LINE1 = " * Mars Simulation Project";
	private static String LINE2 = " * ";
	private static String DOT_JAVA = ".java";
	// Update the author name. Note this would affect only new files without header
	private static String LINE4 = " * @author Manny Kung";
	private static String LINE5 = " */";
	
	private static int level = 15;
	// level = 5 has a list of module-info.java
	// level = 15 contains the last level of class files /food
	
	private static List<String> changedVersionTagList = new ArrayList<>();
	private static List<String> unChangedVersionTagList = new ArrayList<>();
	private static List<String> noVersionTagList = new ArrayList<>();
	
	// ref : https://www.codevscolor.com/java-replace-string-in-file/
	// ref : http://zetcode.com/java/fileswalk/
	
	public static StringBuffer insertVersionTag(String pathName) {
		StringBuffer inputBuffer = new StringBuffer();
		
		Path path = Paths.get(pathName);
		Path fileName = path.getFileName();
				
//		System.out.println(fileName.toString());
		
		inputBuffer.append(LINE0);
        inputBuffer.append(System.lineSeparator());
		inputBuffer.append(LINE1);
        inputBuffer.append(System.lineSeparator());
		inputBuffer.append(LINE2);
        inputBuffer.append(fileName.toString());
        inputBuffer.append(System.lineSeparator());
    	inputBuffer.append(dateTagString);
        inputBuffer.append(System.lineSeparator());	
		inputBuffer.append(LINE4);
        inputBuffer.append(System.lineSeparator());	
		inputBuffer.append(LINE5);
        inputBuffer.append(System.lineSeparator());
        inputBuffer.append(System.lineSeparator());
        
        return inputBuffer;
		
	}
	
    public static void main(String[] args) {

        try (Stream<Path> paths = Files.walk(Paths.get(TOP_DIR), level)) {
            paths.map(path -> path.toString()).filter(f -> 
            	f.endsWith(DOT_JAVA)
            	&& f.contains("src")
            	&& f.contains("main")
            	&& f.contains("java")
            	
            	&& f.contains("org")
            	&& f.contains("mars_sim")
            	
            	&& !f.contains("android")
            	&& !f.contains("javafx")
            	&& !f.contains("libgdx")
            	&& !f.contains("lwjgl")
            	&& !f.contains("network")
            	&& !f.contains("service")
            	)
            

//          .forEach(System.out::println);
            .forEach(path -> {
//            	System.out.println(path);
            	replaceALine(path);
            });

        	
        } catch (IOException e) {
			System.out.println(e.getMessage());
		}
        
        System.out.println();
        System.out.println("> # of java files updated with new version tag : " + changedVersionTagList.size());
        
        if (changedVersionTagList.size() > 0)
        	changedVersionTagList.forEach(System.out::println);

        System.out.println("The new VersionTag is " + dateTagString);

        System.out.println("> # of java files already having the correct version tag : " + unChangedVersionTagList.size());
        
        if (unChangedVersionTagList.size() > 0)
        	unChangedVersionTagList.forEach(System.out::println);

        System.out.println();

        System.out.println("> # of java files being added with a new version tag (please check for correctness) : " + noVersionTagList.size());
        
        if (noVersionTagList.size() > 0)
        	noVersionTagList.forEach(System.out::println);
              
        // TODO: Should show java files having possible issues with version tag       
    }
	
    public static void replaceALine(String f) {
   	 //1
        String originalFileContent = "";

        boolean hasVersionTag = false;
        boolean hasCorrectVersionTag = false;
        
        //2
        BufferedReader reader = null;
        BufferedWriter writer = null;

        //3
        try {

            //4
            reader = new BufferedReader(new FileReader(f));

            //5
            String line = reader.readLine();

            //6
            while (!hasCorrectVersionTag && line != null) {	
                
            	if (line.contains(versionTagID)
            			&& !line.contains("public")
            			&& !line.contains("private")
            			&& !line.contains("int")
            			&& !line.contains("String")        
            			&& !line.contains("boolean")
            			&& !line.contains("double")
            			&& !line.contains("static")  
            			&& !line.contains("=")  
                    	&& !line.contains("//")
            			) {
            	
            		hasVersionTag = true;
            		// Check if this line equals the versionTagString
            		if (line.equals(dateTagString)) {
            			hasCorrectVersionTag = true;
            		}
            		else
            			line = dateTagString;        			
            		
            	}
            	            	
                originalFileContent += line + System.lineSeparator();
                line = reader.readLine();
            }

            if (hasVersionTag) {
            	
            	if (hasCorrectVersionTag) {
            		unChangedVersionTagList.add(f);
            	}
            	else 
            		changedVersionTagList.add(f);
            }
            else {
            	noVersionTagList.add(f);
            }
                    
            if (!hasCorrectVersionTag) {
            	
            	String modifiedFileContent = null;
            	
                if (!hasVersionTag) {
//    	            System.out.println("> issue : " + f);            
    	            modifiedFileContent = insertVersionTag(f).toString() + originalFileContent; //.replaceAll(currentReadingLine, versionTagString);
                }
                else 
                	modifiedFileContent = originalFileContent;
                
                try (FileWriter fr = new FileWriter(f)) {
		            writer = new BufferedWriter(fr);
		
		            writer.write(modifiedFileContent);
                }
            }

        } catch (IOException e) {
            //handle exception
        } finally {
            //10
            try {
                if (reader != null) {
                    reader.close();
                }

                if (writer != null) {
                    writer.close();
                }

            } catch (IOException e) {
                //handle exception
            }
        }
    }
}
