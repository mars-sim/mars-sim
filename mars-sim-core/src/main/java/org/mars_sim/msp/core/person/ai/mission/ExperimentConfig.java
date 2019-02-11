/**
 * Mars Simulation Project
 * ExperimentConfig.java
 * @version 3.1.0 2019-02-08
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
 
public class ExperimentConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
 
    public static final String JSON_FILE = "/json/experiments.json";
    
    private Package p;
    
    public static void main(String[] args) {
			new ExperimentConfig(JSON_FILE);
    }
    
    public ExperimentConfig(String fileName){
        InputStream fis = null;
        JsonReader jsonReader = null;
        fis = this.getClass().getResourceAsStream(fileName);//JSON_FILE);
        jsonReader = Json.createReader(fis);
         
        //get JsonObject from JsonReader
        JsonObject jsonObject = jsonReader.readObject();
         
        //we can close IO resource and JsonReader now
        jsonReader.close();
        try {
			fis.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
         
        // Retrieve package from JsonObject
        p = new Package();
         
        p.setName(jsonObject.getString("package"));
        
        // Read the json array of experiments
        JsonArray jsonArray = jsonObject.getJsonArray("experiments");
        
        int size = jsonArray.size();
        
        p.setNum(size);
        
        try {
	        for (int i = 0; i< size; i++) {
                JsonObject child = jsonArray.getJsonObject(i);
                String exp = child.getString("experiment");
                String num = child.getString("number");
	        	p.createExperiment(exp, num);
//	            System.out.println(exp + " : " + num);
	        }
        } catch (Exception e1) {
			e1.printStackTrace();
		}
     
    }
 
    public Package getPackage() {
    	return p;
    }
	
	class Package {
	    
		int num;
		
		String name;

		List<Experiment> exps = new ArrayList<>();
		
		Package() {}

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
		
	   	void createExperiment(String name, String id) {
	   		Experiment e = new Experiment(name, id);
	   		exps.add(e);
    	}
	}
	
    class Experiment {
    
    	String name;
    	String id;
    	
    	Experiment(String name, String id) {
    		this.name = name;
    		this.id = id;
    	}
    	
    	String getName() {
    		return name;
    	}
    	
    	String getID() {
    		return id;
    	}
    }
}
