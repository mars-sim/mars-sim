/**
 * Mars Simulation Project
 * Commander.java
 * @version 3.1.0 2018-11-04
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
	
    private static final String ONE_SPACE = " ";
    
	private boolean isMarsSocietyAffiliated;
    private String firstName;
    private String lastName;
    private String gender;
    private String jobStr = "";
    private String countryStr = "";
    private int age = -1;
    private int jobInt = -1;
    private int phase;
    private int countryInt = -1;
    
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
    		countryInt = SimulationConfig.instance().getPersonConfiguration().getCountryNum(countryStr) + 1;
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
    		countryStr = UnitManager.getCountryByID(countryInt-1);
    	return countryStr;
    }
    
    public int getAge() {
    	return age;
    }

    public void setAge(int a) {
    	age = a;
    }

    public int getJob() {
    	if (jobInt == -1)
    		jobInt = JobType.getJobNum(jobStr) + 1;
    	return jobInt - 1;
    }
    
    public void setJob(int j) {
    	jobInt = j;
    }

    public void setJobStr(String j) {
    	jobStr = j;
    }

    public String getJobStr() {
    	if (jobStr.equals(""))
    		jobStr = JobType.getEditedJobString(jobInt-1);
    	return jobStr;
    }
    
    public int getPhase() {
    	return phase;
    }
    
    public void setMarsSocietyStr(String value) {
    	if (value.equalsIgnoreCase("y")) {
    		isMarsSocietyAffiliated = true;
//    		System.out.println("isMarsSocietyAffiliated : " + isMarsSocietyAffiliated);
    	}
    	else {
        	isMarsSocietyAffiliated = false;   
//    		System.out.println("isMarsSocietyAffiliated : " + isMarsSocietyAffiliated);
    	}
    }
    
    public void setMarsSociety(boolean value) {
    	isMarsSocietyAffiliated = value;   		
    }
    
//    public boolean isMarsSocietyAffiliated() {
//    	return isMarsSocietyAffiliated;
//    }

    public String isMarsSocietyStr() {
    	if (isMarsSocietyAffiliated)
    		return "y";
   		return "n";
    }

    public boolean isMarsSociety() {
    	return isMarsSocietyAffiliated;
    }
    
    public String getSponsor() {
    	String s = null;	
    	if (isMarsSocietyAffiliated) {
    		s = ReportingAuthorityType.MARS_SOCIETY_L.getName();//Msg.getString("ReportingAuthorityType.long.MS");
    	}
    	else {
    		if (!countryStr.equals(""))
    			s = UnitManager.mapCountry2Sponsor(countryStr);
    		else
    			s = UnitManager.getSponsorByCountryID(countryInt);
    	}
    	return s;
    }
    
    public String getFieldName(String field) {
    	StringBuilder s = new StringBuilder();
    	int size = 27 - field.length();
    	for (int i = 0; i < size; i++) {
    		s.append(ONE_SPACE);
    	}
    	s.append(field);
    	return s.toString();
    }
    
    @Override
    public String toString() {
        return System.lineSeparator() + getFieldName("    First Name : ") + firstName +
        	   System.lineSeparator() + getFieldName("     Last Name : ") + lastName +
        	   System.lineSeparator() + getFieldName("        Gender : ") + gender +
        	   System.lineSeparator() + getFieldName("           Age : ") + age +
        	   System.lineSeparator() + getFieldName("           Job : ") + getJobStr() +
        	   System.lineSeparator() + getFieldName("       Country : ") + getCountryStr() + 
        	   System.lineSeparator() + getFieldName("       Sponsor : ") + getSponsor() +
        	   System.lineSeparator() + getFieldName("  Mars Society : ") + isMarsSocietyStr() 
//        	   System.lineSeparator() + "   Settlement Phase: " + phase
        	   ;
        
    }
}