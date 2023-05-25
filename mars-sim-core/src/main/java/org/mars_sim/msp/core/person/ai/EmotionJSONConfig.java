/*
 * Mars Simulation Project
 * EmotionJSONConfig.java
 * @date 2023-05-24
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.mars_sim.msp.core.logging.SimLogger;

public class EmotionJSONConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
 
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(EmotionJSONConfig.class.getName());

    public static final String JSON_FILE = "/json/emotions.json";
    
    private String[] emotionalStates = {
		"joy",
		"distress",
		"happy",
		"pity",
		"gloating",
		"resentment",
		"hope",
		"fear",
		"satisfaction",
		"fears-confirmed",
		"relief",
		"disappointment",
		"pride",
		"shame",
		"admiration",
		"reproach",
		"gratification",
		"remorse",
		"gratitude",
		"anger",
		"love",
		"hate",
		"disgust",
		"surprise"
	};
    
    private Emotion e;
    
    private Settler settler;
    
    public static void main(String[] args) {
		new EmotionJSONConfig();
    }
    
    public EmotionJSONConfig(){
    	
        InputStream fis = null;
        JsonReader jsonReader = null;
        fis = this.getClass().getResourceAsStream(JSON_FILE);
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
          	logger.log(Level.SEVERE, "Cannot close json file :" + e1.getMessage());
		}
         
        // Retrieve data from JsonObject and create Employee bean
        settler = new Settler();
         
        settler.setName(jsonObject.getString("name"));
     
        // Read inner object from json object
        JsonObject innerJsonObject = jsonObject.getJsonObject("emotions");
        
        e = new Emotion();
        int size = innerJsonObject.size();
        System.out.println("innerJsonObject size: " + size);

        int size1 = emotionalStates.length;
        System.out.println("emotionalStates size: " + size1);
        
        try {
	        for (int i = 0; i< size; i++) {
//	        	String s = (String)(innerJsonObject.asJsonObject()); //.getJsonNumber(s).toString();
//	        	System.out.println(innerJsonObject.getJsonNumber(s).intValue());
	        	e.setAnEmotionLevel(innerJsonObject.getInt(emotionalStates[i]), i);
	        }
        } catch (Exception e1) {
          	logger.log(Level.SEVERE, "Cannot get json int objects: " + e1.getMessage());
		}
        
        settler.setEmotion(e);
        //System.out.println(settler);     
    }
 
	public int [] getEmotionalStates() {
		return e.states;
	}
	
    class Settler {
    	String name = null;
    	Emotion e = null;
    	
    	Settler() {}
    	void setName(String name) {
    		this.name = name;
    	}
    	void setEmotion(Emotion e) {
    		this.e = e;
    	}
    	Emotion getEmotion() {
    		return e;
    	}
    	@Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("***** Settler's Preconfigured Emotional States *****\n");
            sb.append("Name = " + name);
            sb.append("\nEmotions : " + e);//toString());//+ getEmotion());
            //sb.append(emotionString[i]);
            sb.append("\n*****************************************************");
             
            return sb.toString();
        }
    }
    
    
    class Emotion {
    
    	int [] states = new int[22]; 
    	// range 1 to 10

    	Emotion() {}
    	
    	void setAnEmotionLevel(int value, int i) {
    		states[i] = value;
    	}
    	
    	public int [] getEmotionalStates() {
    		return states;
    	}
    	
    	public String toString() {
    		String result = "{ ";
    		
    		for (int e : states) {
    			result += e + " ";
    		}
    		
    		return result +"}";
    	}
    	
    	public int[] toPrint() {
    		return states;
    	}
    }
    
}
