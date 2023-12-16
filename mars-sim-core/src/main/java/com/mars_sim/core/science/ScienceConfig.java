/*
 * Mars Simulation Project
 * ScienceConfig.java
 * @date 2021-08-28
 * @author Manny Kung
 */
package com.mars_sim.core.science;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.tools.util.RandomUtil;
 
public class ScienceConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
 
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ScienceConfig.class.getName());

	private final String SCIENTIFIC_STUDY = "scientific_study";
	private final String JSON = "json";
	private final String DIR = "/";
	private final String SUBJECT = "subject";
	private final String TOPIC = "topic";
	private final String TOPICS = TOPIC + "s";
	private final String DOT = ".";
	private final String UNDERSCORE = "_";
	private final String GENERAL = "General";
	
	private final String JSON_DIR = DIR + JSON + DIR;

	private final String TOPICS_JSON_FILE_EXT = UNDERSCORE + TOPICS + DOT + JSON;
	private final String SCIENTIFIC_STUDY_JSON = SCIENTIFIC_STUDY + DOT + JSON;
    
    private int[] averageTime = null; 
    
    private int aveNumCollaborators;

	private static int maxStudiesPerPerson = 2;
        
    private Map<ScienceType, List<Topic>> scienceTopics = new EnumMap<>(ScienceType.class);

    /**
     * Constructor.
     */
    public ScienceConfig() {
		JsonObject jsonObject = null;

        // Load the scientific study param json files
        try(InputStream fis = this.getClass().getResourceAsStream(JSON_DIR + SCIENTIFIC_STUDY_JSON);
			JsonReader jsonReader = Json.createReader(fis)) {
        	// Get JsonObject from JsonReader
        	jsonObject = jsonReader.readObject();
		} catch (IOException e1) {
          	logger.log(Level.SEVERE, "Cannot open json file: "+ e1.getMessage());
			throw new IllegalStateException("Cannot open science json file", e1);
		}
         
        aveNumCollaborators = jsonObject.getInt("average_num_collaborators");
        maxStudiesPerPerson = jsonObject.getInt("max_studies_per_person");

		averageTime = new int[SciencePhaseTime.values().length];
		for(SciencePhaseTime pt : SciencePhaseTime.values()) {
			averageTime[pt.ordinal()] = jsonObject.getInt(pt.name().toLowerCase() + "_time");
		}
    	     
    	// Create a list of science topic filenames
		for(ScienceType sType : ScienceType.values()) {
			try {
				var topics = parseScienceJSON(sType);
				scienceTopics.put(sType, topics);
			} catch (IOException e) {
				throw new IllegalStateException("Cannot open science json file", e);
			}
		}
    }
 
    private List<Topic> parseScienceJSON(ScienceType sType) throws IOException {
    	String fileName = JSON_DIR + sType.name().toLowerCase() + TOPICS_JSON_FILE_EXT;
		List<Topic> results = new ArrayList<>();
    	
        // Load the topic json files
    	try(InputStream fis = this.getClass().getResourceAsStream(fileName);
			JsonReader jsonReader = Json.createReader(fis)) {
	    	
	        var jsonObject = jsonReader.readObject();

	        // Retrieve a subject from JsonObject
	        String subject = jsonObject.getString(SUBJECT);
			subject = ConfigHelper.convertToEnumName(subject);
			if (!sType.name().equals(subject)) {
				throw new IllegalArgumentException("Science type " + sType.getName() + " is not defiend in file " + fileName);
			}
	        
	        // Read the json array of topics
	        JsonArray jsonArray = jsonObject.getJsonArray(TOPICS);
			for (int i = 0; i< jsonArray.size(); i++) {
				JsonObject child = jsonArray.getJsonObject(i);
				Topic e = new Topic(child.getString(TOPIC));
				results.add(e);
			}
    	}

		return results;
	}

	/**
	 * Finds a random topic for a science type.

	 * @param type
	 * @return
	 */
    public String getATopic(ScienceType type) {
    	if (scienceTopics.containsKey(type)) {
    		List<Topic> topics = scienceTopics.get(type);
    		int size = topics.size();
    		if (size > 0) {
	    		int num = RandomUtil.getRandomInt(size-1);
	    		return topics.get(num).getName();
    		}
    	}
    	return GENERAL;	
    }
    
    public int getAverageTime(SciencePhaseTime time) {
    	return averageTime[time.ordinal()];
    }
    
    int getAveNumCollaborators() {
    	return aveNumCollaborators;
    }

	public int getMaxStudies() {
		return maxStudiesPerPerson ;
	}
	
    /**
     * The Topic of a Subject.
     */
    private static class Topic {
    
    	String name;
    	
    	Topic(String name) {
    		this.name = name;
    	}
    	
    	String getName() {
    		return name;
    	}
    }
}
