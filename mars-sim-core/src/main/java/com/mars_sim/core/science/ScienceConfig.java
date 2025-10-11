/*
 * Mars Simulation Project
 * ScienceConfig.java
 * @date 2021-08-28
 * @author Manny Kung
 */
package com.mars_sim.core.science;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.mars_sim.core.configuration.ConfigHelper;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.tool.RandomUtil;
 
/**
 * ScienceConfig class - loads and stores the science configuration parameters.
 */
public class ScienceConfig {
 
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ScienceConfig.class.getName());

	private static final String SCIENTIFIC_STUDY = "scientific_study";
	private static final String JSON = "json";
	private static final String DIR = "/";
	private static final String SUBJECT = "subject";
	private static final String TOPIC = "topic";
	private static final String TOPICS = TOPIC + "s";
	private static final String DOT = ".";
	private static final String UNDERSCORE = "_";
	private static final String GENERAL = "General";
	
	private static final String JSON_DIR = DIR + JSON + DIR;

	private static final String TOPICS_JSON_FILE_EXT = UNDERSCORE + TOPICS + DOT + JSON;
	private static final String SCIENTIFIC_STUDY_JSON = SCIENTIFIC_STUDY + DOT + JSON;
    
    private int[] averageTime = null; 
    
    private int aveNumCollaborators;

	private static int maxStudiesPerPerson = 2;
        
    private Map<ScienceType, List<String>> scienceTopics = new EnumMap<>(ScienceType.class);

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
 
    private List<String> parseScienceJSON(ScienceType sType) throws IOException {
    	String fileName = JSON_DIR + sType.name().toLowerCase() + TOPICS_JSON_FILE_EXT;
		List<String> results = null;
    	
        // Load the topic json files
    	try (InputStream fis = this.getClass().getResourceAsStream(fileName);
			
			JsonReader jsonReader = Json.createReader(fis)) {
	    	
	        var jsonObject = jsonReader.readObject();

	        // Retrieve a subject from JsonObject
	        String subject = jsonObject.getString(SUBJECT);
			subject = ConfigHelper.convertToEnumName(subject);
			if (!sType.name().equals(subject)) {
				throw new IllegalArgumentException("Science type " + sType.getName() + " is not defiend in file " + fileName);
			}	        
	        // Read the json array of topics
	        try {
	            results = jsonObject.getJsonArray(TOPICS).stream()
	                    .map(j -> j.asJsonObject().getString(TOPIC))
	                    .toList();
	        } catch (ClassCastException | NullPointerException | IllegalStateException e) {
	            logger.severe("Malformed JSON structure in file: " + fileName, e);
	            throw new IOException("Malformed JSON structure in file: " + fileName, e);
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
		var topics = getTopics(type);
		if (!topics.isEmpty()) {
			return RandomUtil.getRandomElement(topics);
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
	 * Get the topics assigned to a science type.
	 * @param science
	 * @return
	 */
	public List<String> getTopics(ScienceType science) {
		return scienceTopics.getOrDefault(science, Collections.emptyList());
	}
}
