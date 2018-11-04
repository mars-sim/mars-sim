package org.mars_sim.msp.core.terminal;

import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.ai.job.JobType;

public class Commander {
	
    private String firstName;
    private String lastName;
    private String gender;
    private int age;
    private int job;
    private int phase;
    private int country;
    
    public Commander() {
//    	System.out.println("Commander is ready");
    }
    
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
    
    @Override
    public String toString() {
        return System.lineSeparator() + "   First Name: " + firstName +
        	   System.lineSeparator() + "   Last Name: " + lastName +
        	   System.lineSeparator() + "   Gender: " + gender +
        	   System.lineSeparator() + "   Age: " + age +
        	   System.lineSeparator() + "   Job: " + JobType.getEditedJobString(job-1) +
        	   System.lineSeparator() + "   Country: " + UnitManager.getCountryByID(country-1) + 
        	   System.lineSeparator() + "   Space Agency: " + UnitManager.getSponsorByCountryID(country-1) + "" 
//        	   System.lineSeparator() + "   Settlement Phase: " + phase
        	   ;
        
    }
}