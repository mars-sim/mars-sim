/**
 * Mars Simulation Project
 * Unit.java
 * @version 2.70 2000-02-29
 * @author Scott Davis
 */

import java.awt.*;
import javax.swing.*;

/** The Unit class is the abstract parent class to all units on the
 *  virtual Mars.  Units include people, vehicles and settlements.
 *  This class provides data members and methods common to all units.
 */
public abstract class Unit {

    private static int idCount = 0;    // a unique id for each Unit instance

    protected Coordinates location;    // Unit location coordinates
    protected int unitID;              // Unit ID assigned by UnitManager
    protected String name;             // Unit name
    protected VirtualMars mars;        // The virtual Mars
    protected UnitManager manager;     // Primary unit manager
    protected UnitInfo info;

    public Unit(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

	this.unitID = idCount++;

	// Initialize data members from parameters
	this.name = name;
	this.location = location;
	this.mars = mars;
	this.manager = manager;
	info = new UnitInfo(this, unitID, name);
    }
	
    /** unit identification */
    public UnitInfo getUnitInfo() {
	return info;
    }

    /** Returns unit's ID */
    public int getID() {
	return unitID;
    }
	
    /** Returns unit's UnitManager */
    public UnitManager getUnitManager() {
	return manager;
    }

    /** Returns unit's name */
    public String getName() {
	return name;
    }

    /** Returns unit's location */
    public Coordinates getCoordinates() {
	return location;
    }

    /** Sets unit's location coordinates */
    public void setCoordinates(Coordinates newLocation) {
	location.setCoords(newLocation);
    }

    /** Returns a detail window for the unit */
    public abstract UnitDialog getDetailWindow(MainDesktopPane parentDesktop); 

    /*
    public abstract Image getSurfIcon();
    public abstract Image getTopoIcon();
    */

}
