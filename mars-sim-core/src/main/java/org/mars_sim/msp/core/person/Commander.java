/**
 * Mars Simulation Project
 * Commander.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;

import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

public class Commander implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	private int age = -1;
    private int phase;
    
    private int leadershipPoint = 0;

	private String firstName;
    private String lastName;
    private String gender;
    private String job;
    private String countryStr = "";
    private ReportingAuthorityType sponsorStr = ReportingAuthorityType.MS;    
    
    public String getFullName() {
    	if (firstName == null && lastName == null)
    		return null;
    	else {
    		return firstName + " " + lastName;
    	}
    }

    public String getLastName() {
    	return lastName;
    }

    public void setLastName(String l) {
    	lastName = l;
    }

    public String getFirstName() {
    	return firstName;
    }

    public void setFirstName(String f) {
    	firstName = f;
    }

    public String getGender() {
    	return gender;
    }

    public void setGender(String g) {
    	gender = g;
    }

    public void setCountryStr(String c) {
    	countryStr = c;
    }
    
    public String getCountryStr() {
    	return countryStr;
    }
    
    public void setSponsorStr(ReportingAuthorityType c) {
    	sponsorStr = c;
    }
    
    public ReportingAuthorityType getSponsorStr() {
    	return sponsorStr;
    }
    
    public int getAge() {
    	return age;
    }

    public void setAge(int a) {
    	age = a;
    }

    public void setJob(String j) {
    	job = j;
    }

    public String getJob() {
    	return job;
    }
    
    public int getPhase() {
    	return phase;
    }
    
    public void setInitialLeadershipPoint(int value) {   
    	leadershipPoint = value;
    }
    
    public int getLeadershipPoint() {
//    	if (initialLeadershipPoint == 0) {
//    		determineLeadershipPoints
//    	}
    		
    	return leadershipPoint;
    }
    
    public void setLeadershipPoint(int value) {
    	leadershipPoint = value;
    }

    public void addLeadershipPoint(int value) {
    	leadershipPoint += value;
    }

    public void subtractLeadershipPoint(int value) {
    	leadershipPoint -= value;
    }
    
    public void outputDetails(StringBuilder dest) {
    	dest.append("Full Name: ").append(firstName + ' ' + lastName).append(System.lineSeparator())
    	    .append("Age: ").append(age).append(System.lineSeparator())
    	    .append("Gender: ").append(gender).append(System.lineSeparator())
    	    .append("Job: ").append(job).append(System.lineSeparator())
    	    .append("Country: ").append(countryStr).append(System.lineSeparator())
    	    .append("Sponsor: ").append(sponsorStr).append(System.lineSeparator());
    }
    
    
    @Override
    public String toString() {
        return "Commander " + firstName + ' ' + lastName;
    }
}
