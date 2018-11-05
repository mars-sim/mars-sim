/**
 * Mars Simulation Project
 * Commander.java
 * @version 3.1.0 2018-11-04
 * @author Manny Kung
 */

package org.mars_sim.msp.core.terminal;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.job.JobType;

public class Commander implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
    private static final String ONE_SPACE = " ";
    
	private boolean isMarsSocietyAffiliated;
    private String firstName;
    private String lastName;
    private String gender;
    private int age;
    private int job;
    private int phase;
    private int country;
    
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

    public int getCountry() {
    	return country-1;
    }
    
    public void setCountry(int c) {
    	country = c;
    }
    
    public int getAge() {
    	return age;
    }

    public void setAge(int a) {
    	age = a;
    }

    public int getJob() {
    	return job-1;
    }
    
    public void setJob(int j) {
    	job = j;
    }

    public int getPhase() {
    	return phase;
    }
    
    public void setMarsSocietyAffiliated(String value) {
    	if (value.equalsIgnoreCase("y"))
    		isMarsSocietyAffiliated = true;
    	else
        	isMarsSocietyAffiliated = false;    		
    }
    
//    public boolean isMarsSocietyAffiliated() {
//    	return isMarsSocietyAffiliated;
//    }

    public String isMarsSocietyAffiliated() {
    	if (isMarsSocietyAffiliated)
    		return "y";
   		return "n";
    }

    public String getSponsor() {
    	String s = null;	
    	if (isMarsSocietyAffiliated) {
    		s = Msg.getString("ReportingAuthorityType.long.MarsSociety");
    	}
    	else {
    		s = UnitManager.getSponsorByCountryID(country-1);
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
        return System.lineSeparator() + getFieldName("First Name: ") + firstName +
        	   System.lineSeparator() + getFieldName("Last Name: ") + lastName +
        	   System.lineSeparator() + getFieldName("   Gender: ") + gender +
        	   System.lineSeparator() + getFieldName("   Age: ") + age +
        	   System.lineSeparator() + getFieldName("   Job: ") + JobType.getEditedJobString(job-1) +
        	   System.lineSeparator() + getFieldName("   Country: ") + UnitManager.getCountryByID(country-1) + 
        	   System.lineSeparator() + getFieldName("   Sponsor: ") + getSponsor() 
//        	   System.lineSeparator() + "   Settlement Phase: " + phase
        	   ;
        
    }
}