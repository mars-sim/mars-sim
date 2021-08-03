/**
 * Mars Simulation Project
 * Member.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Map;

/**
 * The Member class is for storing the alpha team roster
 */
public class Member implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private String crewName;
	private String name; 
	private String gender;
	private String age; 
	private String mbti; 
	private String job;
	private String country; 
	private String sponsorCode;
	private String destination;
	
	private String mainDish;
	private String sideDish;
	private String dessert;
	private String activity;

	private Map<String, Integer> skills;

	private Map<String, Integer> relationships;

	
	public Member() {
	}

	public void setCrewName(String value) {
		crewName = value;
	}
	
	public void setName(String value) {
		name = value;
	}
	
	public void setGender(String value) {
		gender = value;
	}
	
	public void setAge(String value) {
		age = value;
	}
	
	public void setMBTI(String value) {
		mbti = value;
	}
	
	public void setJob(String value) {
		job = value;
	} 

	public void setCountry(String value) {
		country = value;
	} 
	
	public void setSponsorCode(String value) {
		sponsorCode = value;
	} 

	public void setDestination(String value) {
		destination = value;
	}
	
	public void setMainDish(String value) {
		mainDish = value;
	}
	
	public void setSideDish(String value) {
		sideDish = value;
	}
	
	public void setDessert(String value) {
		dessert = value;
	}
	
	public void setActivity(String value) {
		activity = value;
	}

	public String getCrewName() {
		return crewName;
	} 
	
	public String getName() {
		return name;
	} 
	
	public String getGender() {
		return gender;
	}
	
	public String getAge() {
		return age;
	}
	
	public String getMBTI() {
		return mbti;
	}
	
	public String getJob() {
		return job;
	}  

	public String getCountry() {
		return country;
	}  

	public String getSponsorCode() {
		return sponsorCode;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public String getMainDish() {
		return mainDish;
	}
	
	public String getSideDish() {
		return sideDish;
	}
	
	public String getDessert() {
		return dessert;
	}
	
	public String getActivity() {
		return activity;
	}

	public Map<String, Integer> getSkillMap() {
		return skills;
	}

	public void setSkillsMap(Map<String, Integer> skills) {
		this.skills = skills;
		
	}

	public void setRelationshipMap(Map<String, Integer> reals) {
		this.relationships = reals;
	}
	
	public Map<String, Integer> getRelationshipMap() {
		return relationships;
	}
	
}
