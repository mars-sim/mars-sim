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

    public static Person commander;

    public static UnitManager unitManager = Simulation.instance().getUnitManager();
    
    public static void setCommander(Person cc) {
    	commander = cc;
    	id = cc.getIdentifier();
    }
    
    public static void initializeInstances(UnitManager u) {
    	unitManager = u;
    	id = unitManager.getCommanderID();
    	commander = unitManager.getPersonByID(id);
    }
    
    @Override
    public String toString() {
        return System.lineSeparator() +">" + mode;
    }
}