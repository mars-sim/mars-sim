/**
 * Mars Simulation Project
 * ScienceConfig.java
 * @version 3.1.0 2019-02-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.science;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
 
public class ScienceConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
 
	public static final String PATH = "/json/";

	public static final String EXTENSION = "_topics.json";
	
    public static String[] jsonFiles = new String[9]; 
    
    private Subject s;
    
    private Map<ScienceType, List<Topic>> scienceTopics; 
    
    public static void main(String[] args) {
			new ScienceConfig();
    }
    
    public void createJsonFiles() {
    	int size = ScienceType.valuesList().size();
    	for (int i=0; i<size; i++) {
    		ScienceType type = ScienceType.valuesList().get(i);
    		jsonFiles[i] = PATH + type.getName().toLowerCase() + EXTENSION;
//    		System.out.println(jsonFiles[i]);
    	}
    }
    
    public ScienceConfig(){
    	
    	scienceTopics = new HashMap<>();
    	
    	// Read contents of a file into a single String
    	//String content = new String(Files.readAllBytes(Paths.get("C:/file.txt")));
        //System.out.println(content);
    	
    	// Create a list of science topic filenames 
    	createJsonFiles();
    	
    	for (String fileName : jsonFiles) {
	        InputStream fis = null;
	        JsonReader jsonReader = null;
	        fis = this.getClass().getResourceAsStream(fileName);//JSON_FILE);
	        jsonReader = Json.createReader(fis);
	             
	//      We can create JsonReader from Factory also
	//      JsonReaderFactory factory = Json.createReaderFactory(null);
	//      jsonReader = factory.createReader(fis);
	         
	        //get JsonObject from JsonReader
	        JsonObject jsonObject = jsonReader.readObject();
	         
	        //we can close IO resource and JsonReader now
	        jsonReader.close();
	        try {
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	         
	        // Retrieve a subject from JsonObject
	        s = new Subject();
	         
	        s.setName(jsonObject.getString("subject"));
	     
	        s.setNum(jsonObject.getInt("numbers"));
	        
	        int size = s.getNum();
	        
	        // Read the json array of experiments
	        JsonArray jsonArray = jsonObject.getJsonArray("topics");
	        
	        try {
//	        	System.out.println(s.getName() + " : ");
		        for (int i = 0; i< size; i++) {
	                JsonObject child = jsonArray.getJsonObject(i);
	                String t = Conversion.capitalize(child.getString("topic"));
		        	s.createTopic(t);
//		            System.out.println("   " + t);
		        }
	        } catch (Exception e1) {
				e1.printStackTrace();
			}
     
	        scienceTopics.put(ScienceType.getType(s.getName()), s.getTopics());
    	}
    }
 
    public Subject getSubject() {
    	return s;
    }
	
    public String getATopic(ScienceType type) {
    	if (scienceTopics.containsKey(type)) {
    		List<Topic> topics = scienceTopics.get(type);
    		int size = topics.size();
    		if (size > 0) {
	    		int num = RandomUtil.getRandomInt(topics.size());
	    		return topics.get(num-1).getName();
    		}
    	}
    	return "General";	
    }
    
	class Subject {
	    
		int num;
		
		String name;

		List<Topic> exps = new ArrayList<>();
		
		Subject() {}

		void setName(String value) {
			this.name = value;
		}
		
		void setNum(int num) {
			this.num = num;
		}
		
		int getNum() {
			return num;
		}
		
		String getName() {
			return name;
		}
		
	   	void createTopic(String name) {
	   		Topic e = new Topic(name);
	   		exps.add(e);
    	}
	   	
	   	List<Topic> getTopics() {
	   		return exps;
	   	}
	}
	
    class Topic {
    
    	String name;
    	String id;
    	
    	Topic(String name) {
    		this.name = name;
    	}
    	
    	String getName() {
    		return name;
    	}
    	
    }
}
