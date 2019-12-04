/**
 * Mars Simulation Project
 * GameManager.java
 * @version 3.1.0 2019-02-21
 * @author Manny Kung
 */

package org.mars_sim.msp.core;

import org.mars_sim.msp.core.person.Person;

public class GameManager {
	
    public static Integer id;

    /** The GameMode enums. */
    public enum GameMode {
        SANDBOX, COMMAND; 
    }
    
    /** An instance of GameMode. */
    public static GameMode mode;

    /** The menu variable captures the first menu choice in the console main menu. */
    public static String menu = "";
    
    /** The resolution variable captures the choice of the screen resolution in the console main menu. */
    public static String resolution = "";
    
    /** The input variable captures the game mode choice. */
    public static String input = "";

    /** The useSCE variable captures the choice of using useSCE. */
    public static String useSCE = "";

    /** The command0 variable captures the first choice in the Command Mode. */
    public static String commanderProfile = "";
    
    /** The sandbox0 variable captures the first choice in the Sandbox Mode. */
    public static String sandbox0 = "";
    
    /** The Commander instance. */
    public static Person commanderPerson;
    /** The UnitManager instance. */
    public static UnitManager unitManager;
    
    public static void setCommander(Person cc) {
    	commanderPerson = cc;
    	id = cc.getIdentifier();
    }
    
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
        return System.lineSeparator() +"> ";
    }
}