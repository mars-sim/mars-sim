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
    
    public enum GameMode {
        SANDBOX, COMMAND; 
    }
    
    public static GameMode mode;

    // The input variable captures the first player text input
    public static String input;

    // The choice variable captures the second player text input
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
	
		if (unitManager.getCommanderMode()) {
			mode = GameMode.COMMAND;
	    	id = unitManager.getCommanderID();
	    	commanderPerson = unitManager.getPersonByID(id);
		}
		else {
			mode = GameMode.SANDBOX;
		}

    }
    
    @Override
    public String toString() {
        return System.lineSeparator() +">" + input;
    }
}