/**
 * Mars Simulation Project
 * Member.java
 * @version 3.1.0 2017-01-24
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person;

import java.io.Serializable;

/**
 * The Member class is for storing the alpha team roster
 */
public class Member implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

//    private static Logger logger = Logger.getLogger(Member.class.getName());

	private String name; 
	private String gender;
	private String mbti; 
	private String job;
	private String country; 
	private String sponsor;
	private String destination;
	private String mainDish;
	private String sideDish;
	private String dessert;
	private String activity;

	
	public Member() {
	}

	public void setName(String value) {
		name = value;
	}
	
	public void setGender(String value) {
		gender = value;
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
	
	public void setSponsor(String value) {
		sponsor = value;
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


	public String getName() {
		return name;
	} 
	
	public String getGender() {
		return gender;
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

	public String getSponsor() {
		return sponsor;
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
	
}
