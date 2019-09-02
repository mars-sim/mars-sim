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
 
	public static final String SCIENTIFIC_STUDY_JSON = "scientific_study.json";
	
	public static final String JSON_DIR = "/json/";

	public static final String TOPICS_JSON_FILE_EXT = "_topics.json";
	
	private static String[] jsonFiles = new String[9]; 
    
    private static List<Integer> averageTime = new ArrayList<>(); 
    
    private static int aveNumCollaborators;
    
    private Subject s;
    
    private Map<ScienceType, List<Topic>> scienceTopics = new HashMap<>();
    
    public static void main(String[] args) {
			new ScienceConfig();
    }
    
    public void createJsonFiles() {
    	int size = ScienceType.valuesList().size();
    	for (int i=0; i<size; i++) {
    		ScienceType type = ScienceType.valuesList().get(i);
    		jsonFiles[i] = JSON_DIR + type.getName().toLowerCase() + TOPICS_JSON_FILE_EXT;
//    		System.out.println(jsonFiles[i]);
    	}
    }
    
    public ScienceConfig() {
        InputStream fis = null;
        JsonReader jsonReader = null;
        JsonObject jsonObject = null;
        
        // Load the scientific study param json files
        fis = this.getClass().getResourceAsStream(JSON_DIR + SCIENTIFIC_STUDY_JSON);
        jsonReader = Json.createReader(fis);

        // Get JsonObject from JsonReader
        jsonObject = jsonReader.readObject();
         
        // Close IO resource and JsonReade
        jsonReader.close();
        try {
			fis.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
         
        aveNumCollaborators = jsonObject.getInt("average_num_collaborators");
        averageTime.add(jsonObject.getInt("base_proposal_time"));
        averageTime.add(jsonObject.getInt("base_primary_research_study_time"));
        averageTime.add(jsonObject.getInt("base_collaborative_research_study_time"));
        averageTime.add(jsonObject.getInt("base_primary_researcher_paper_writing_time"));
        averageTime.add(jsonObject.getInt("base_collaborator_paper_writing_time"));
        averageTime.add(jsonObject.getInt("peer_review_time"));
        averageTime.add(jsonObject.getInt("primary_researcher_work_downtime_allowed"));
        averageTime.add(jsonObject.getInt("collaborator_work_downtime_allowed"));
    	
        
    	// Create a list of science topic filenames 
    	createJsonFiles();
    	
        // Load the topic json files
    	for (String fileName : jsonFiles) {  
	        fis = this.getClass().getResourceAsStream(fileName);
	        jsonReader = Json.createReader(fis);

	        // Alternatively, we can create JsonReader from Factory 
//	      	JsonReaderFactory factory = Json.createReaderFactory(null);
//	      	jsonReader = factory.createReader(fis);
	         
	        // Get JsonObject from JsonReader
	        jsonObject = jsonReader.readObject();
	         
	        // Close IO resource and JsonReader
	        jsonReader.close();
	        try {
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	         
	        s = new Subject();
	        // Retrieve a subject from JsonObject
	        s.setSubject(jsonObject.getString("subject"));
	     
//	        s.setNum(jsonObject.getInt("numbers"));
//	        int size = s.getNum();
	        
	        // Read the json array of experiments
	        JsonArray jsonArray = jsonObject.getJsonArray("topics");
	        
	        int size = jsonArray.size();
	        
	        s.setNum(size);
	        
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
	    		int num = RandomUtil.getRandomInt(size-1);
	    		return topics.get(num).getName();
    		}
    	}
    	return "General";	
    }
    
    
    public static List<Integer> getAverageTime() {
    	return averageTime; 
    }

    public static int getAverageTime(int index) {
    	return averageTime.get(index); 
    }
    
    static int getAveNumCollaborators() {
    	return aveNumCollaborators;
    }

    
	class Subject {
	    
		int num;
		
		String name;

		List<Topic> exps = new ArrayList<>();
		
		Subject() {}

		void setSubject(String value) {
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
