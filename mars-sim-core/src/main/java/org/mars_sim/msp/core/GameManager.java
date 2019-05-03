/**
 * Mars Simulation Project
 * GameManager.java
 * @version 3.1.0 2019-02-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.person.Person;

public class GameManager {
	
    public static int id;
    
    public static String mode;
    
    public static String choice;

    public static Person commanderPerson;

    public static UnitManager unitManager;
    
    public static void setCommander(Person cc) {
    	commanderPerson = cc;
    	id = cc.getIdentifier();
    }
    
//    public String getMode() {
//    	return mode;
//    }
//
//    public void setMode(String mode) {
//    	this.mode = mode;
//    }
//    
    public static void initializeInstances(UnitManager u) {
    	unitManager = u;
	
//    	if (mode == null) {
    		if (unitManager.getCommanderMode()) {
	    		mode = "1";
		    	id = unitManager.getCommanderID();
		    	commanderPerson = unitManager.getPersonByID(id);
    		}
    		else {
    			mode = "0";
    		}
//    	}
    	
//    	if (mode == "1") {
//    		unitManager.setCommanderMode(true);
//	    	id = unitManager.getCommanderID();
//	    	commander = unitManager.getPersonByID(id);
//    	}
    	
    }
    
    @Override
    public String toString() {
        return System.lineSeparator() +">" + mode;
    }
}