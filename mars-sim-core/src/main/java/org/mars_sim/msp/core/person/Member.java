/*
 * Mars Simulation Project
 * Member.java
 * @date 2021-09-04
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.util.Map;

/**
 * The Member class is for storing the alpha team roster
 */
public class Member {

	private String name; 
	private GenderType gender;
	private String age; 
	private String mbti; 
	private String job;
	private String country; 
	private String sponsorCode;
	
	private String mainDish;
	private String sideDish;
	private String dessert;
	private String activity;

	private Map<String, Integer> skills;

	private Map<String, Integer> relationships;

	
	public Member() {
	}

	public void setName(String value) {
		name = value;
	}
	
	public void setGender(GenderType value) {
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
	
	public String getName() {
		return name;
	} 
	
	public GenderType getGender() {
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
	
	/**
	 * Checks if the personality is introvert.
	 * 
	 * @param index the crew index
	 * @return true if introvert
	 */
	public boolean isIntrovert(int index) {
		return mbti.substring(0, 1).equals("I");
	}

	/**
	 * Checks if the personality is extrovert.
	 * 
	 * @param index the crew index
	 * @return true if extrovert
	 */
	public boolean isExtrovert() {
		return mbti.substring(0, 1).equals("E");
	}

	/**
	 * Checks if the personality is sensor.
	 * 
	 * @param index the crew index
	 * @return true if sensor
	 */
	public boolean isSensor() {
		return mbti.substring(1, 2).equals("S");
	}

	/**
	 * Checks if the personality is intuitive.
	 * 
	 * @param index the crew index
	 * @return true if intuitive
	 */
	public boolean isIntuitive() {
		return mbti.substring(1, 2).equals("N");
	}

	/**
	 * Checks if the personality is thinker.
	 * 
	 * @param index the crew index
	 * @return true if thinker
	 */
	public boolean isThinker() {
		return mbti.substring(2, 3).equals("T");
	}

	/**
	 * Checks if the personality is feeler.
	 * 
	 * @param index the crew index
	 * @return true if feeler
	 */
	public boolean isFeeler() {
		return mbti.substring(2, 3).equals("F");
	}

	/**
	 * Checks if the personality is judger.
	 * 
	 * @param index the crew index
	 * @return true if judger
	 */
	public boolean isJudger() {
		return mbti.substring(3, 4).equals("J");
	}

	/**
	 * Checks if the personality is perceiver.
	 * 
	 * @param index the crew index
	 * @return true if perceiver
	 */
	public boolean isPerceiver() {
		return mbti.substring(3, 4).equals("P");
	}


		
}
