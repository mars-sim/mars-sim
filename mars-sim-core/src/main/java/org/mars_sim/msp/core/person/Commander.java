/**
 * Mars Simulation Project
 * Commander.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;

public class Commander implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;


	private int age = -1;
    private int phase;
    private int countryInt = -1;
    private int sponsorInt = -1;
    
    private int leadershipPoint = 0;

	private String firstName;
    private String lastName;
    private String gender;
    private JobType job;
    private String countryStr = "";
    private String sponsorStr = "";    
    
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

    public int getCountryInt() {
    	if (countryInt == -1)
    		countryInt = SimulationConfig.instance().getPersonConfig().getCountryNum(countryStr) + 1;
    	return countryInt - 1;
    }
    
    public void setCountryInt(int c) {
    	countryInt = c;
    }
    
    public void setCountryStr(String c) {
    	countryStr = c;
    }
    
    public String getCountryStr() {
    	if (countryStr.equals(""))
    		countryStr = SimulationConfig.instance().getPersonConfig().getCountry(countryInt-1);
    	return countryStr;
    }
    
    public int getSponsorInt() {
    	if (sponsorInt == -1)
    		sponsorInt = ReportingAuthorityType.getSponsorID(sponsorStr) + 1;
    	return sponsorInt - 1;
    }
    
    public void setSponsorInt(int c) {
    	sponsorInt = c;
    }
    
    public void setSponsorStr(String c) {
    	sponsorStr = c;
    }
    
    public String getSponsorStr() {
    	if (sponsorStr.equals(""))
    		sponsorStr = UnitManager.getSponsorByID(sponsorInt-1);
    	return sponsorStr;
    }
    
    public int getAge() {
    	return age;
    }

    public void setAge(int a) {
    	age = a;
    }

    public void setJob(JobType j) {
    	job = j;
    }

    public JobType getJob() {
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
    
    @Override
    public String toString() {
        return "Commander " + firstName + ' ' + lastName;
    }
}
