/*
 * Mars Simulation Project
 * GroupIDChange.java
 * @date 2023-10-17
 * @author Manny Kung
 */

package com.mars_sim.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

/*
 * This class automatically generates or updates the maven package path name
 * to match up with the maven group id in pom.xml.
 */
public class GroupIDChange {
	
	// CAUTION: Please test the setting on a set of demo files first and never
	// run it the first time on files without a proper backup.
		
	private static String COM_GITHUB_MARS_SIM = "com.github.mars-sim";
	
	private static String ORG_MARS_SIM = "org.mars_sim";

	private static String ORG_MARS_SIM_MSP = "com.mars_sim";
	
//	private static String ORG_SLASH_MARS_SIM = "org/mars_sim";

//	private static String ORG_SLASH_MARS_SIM_MSP = "org/mars_sim/msp";

	private static String COM_MARS_SIM = "com.mars_sim";

	// The affected path for the git clone repo on your machine 
	private static String TOP_DIR = "D:/eclipse/java-2023-09/git/mars-sim/mars-sim";
//			
//	private static String LINE0 = "/**";
//	private static String LINE1 = " * Mars Simulation Project";
//	private static String LINE2 = " * ";
	private static String DOT_JAVA = ".java";
//	// Update the author name. Note this would affect only new files without header
//	private static String LINE4 = " * @author Manny Kung";
//	private static String LINE5 = " */";
	
	private static int level = 15;
	// level = 5 has a list of module-info.java
	// level = 15 contains the last level of class files /food
	
	private static List<String> changedGroupIdList = new ArrayList<>();
	private static List<String> untouchedList = new ArrayList<>();
	private static List<String> noGroupIdList = new ArrayList<>();
	
	// ref : https://www.codevscolor.com/java-replace-string-in-file/
	
	// ref : http://zetcode.com/java/fileswalk/	

    public static void main(String[] args) {

    	Path dir = Paths.get(TOP_DIR);
    	
        try (Stream<Path> paths = Files.walk(dir, level)) {
        	
        	
            paths.map(path -> path.toString())
            
            .filter(f -> 
//            	f.endsWith(DOT_JAVA)
//            	&& 
            	(f.contains(ORG_MARS_SIM_MSP)
            		|| f.contains(ORG_MARS_SIM)
//            		|| f.contains(COM_GITHUB_MARS_SIM)
            		)
            	)
            
//          .forEach(System.out::println);
            .forEach(s -> {
//            	System.out.println(path);
//            	replaceALine(path);
            	
//            	Charset charset = StandardCharsets.UTF_8;
//
//            	String content = null;
//				try {
//					content = new String(Files.readAllBytes(path), charset);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
////            	String content = FileUtils.readFileToString(f, charset);
//            	content = content
//            			.replaceAll(ORG_MARS_SIM_MSP, COM_MARS_SIM)
//            			.replaceAll(ORG_MARS_SIM, COM_MARS_SIM);
//            	try {
//					Files.write(path, content.getBytes(charset));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
            	
            });

        	
        } catch (IOException e) {
			System.out.println(e.getMessage());
		}
        
        System.out.println();
        System.out.println("> # of java files being replaced with the correct group id: " 
        		+ changedGroupIdList.size());
        
        if (changedGroupIdList.size() > 0)
        	changedGroupIdList.forEach(System.out::println);

//        System.out.println("The old group id is " + ORG_MARS_SIM);
        System.out.println("The new group id is " + COM_MARS_SIM);

        
        System.out.println("> # of java files already having the correct group id: " 
        		+ untouchedList.size());
        
        if (untouchedList.size() > 0)
        	untouchedList.forEach(System.out::println);

        System.out.println();

        System.out.println("> # of java files untouched: "
        		+ noGroupIdList.size());
        
        if (noGroupIdList.size() > 0)
        	noGroupIdList.forEach(System.out::println);
              
        // TODO: Should show java files having possible issues with version tag       
    }
	
    public static void replaceALine(String f) {
   	 //1
        String originalFileContent = "";

        boolean hasGroupId = false;
        boolean hasCorrectGroupId = false;
        
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
            while (!hasCorrectGroupId && line != null) {	

            	if (line.contains(ORG_MARS_SIM_MSP)
            			|| line.contains(ORG_MARS_SIM)
                		|| line.contains(COM_GITHUB_MARS_SIM)) {
            	
            		hasGroupId = true;          		
            	}

            	else if (line.contains(COM_MARS_SIM)) {
            	
            		hasCorrectGroupId = true;
            	}
            	
                originalFileContent += line;
                line = reader.readLine();
            }

            if (hasGroupId) {
            	
            	if (hasCorrectGroupId) {
            		untouchedList.add(f);
            	}
            	else 
            		changedGroupIdList.add(f);
            }
            else {
            	noGroupIdList.add(f);
            }
                    
            if (!hasCorrectGroupId) {
            	
            	String modifiedFileContent = null;
            	
                if (!hasGroupId) {
//    	            System.out.println("> issue : " + f);            
    	            modifiedFileContent = COM_MARS_SIM + originalFileContent;
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
