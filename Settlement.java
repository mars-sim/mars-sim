/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.70 2000-08-31
 * @author Scott Davis
 */

import java.awt.*;
import java.util.*;
import javax.swing.*;

/** The Settlement class represents a settlement unit on virtual Mars.
 *  It contains information related to the state of the settlement.
 */
public class Settlement extends Unit {

	// Data members

	protected Vector people;                    // List of inhabitants
	protected Vector vehicles;                  // List of parked vehicles
	protected FacilityManager facilityManager;  // The facility manager for the settlement.

	// Constructor

	public Settlement(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

		// Use Unit constructor

		super(name, location, mars, manager);
		
		// Initialize data members

		people = new Vector();
		vehicles = new Vector();
		facilityManager = new FacilityManager(this);
	}

	// Returns the facility manager for the settlement
	
	public FacilityManager getFacilityManager() { return facilityManager; }

	// Get number of inhabitants or parked vehicles in settlement

	public int getPeopleNum() { return people.size(); }
	public int getVehicleNum() { return vehicles.size(); }

	// Get an inhabitant at a given vector index

	public Person getPerson(int index) { 
		if (index < people.size()) return (Person)people.elementAt(index); 
		else return null;
	}
	
	// Get a parked vehicle at a given vector index.
	
	public Vehicle getVehicle(int index) { 
		if (index < vehicles.size()) return (Vehicle)vehicles.elementAt(index); 
		else return null;	
	}

	// Bring in a new inhabitant

	public void addPerson(Person newPerson) { people.addElement(newPerson); }

	// Make a given inhabitant leave the settlement

	public void personLeave(Person person) { if (people.contains(person)) people.removeElement(person); }

	// Bring in a new vehicle to be parked

	public void addVehicle(Vehicle newVehicle) { vehicles.addElement(newVehicle); }

	// Make a given vehicle leave the settlement
	
	public void vehicleLeave(Vehicle vehicle) { if (vehicles.contains(vehicle)) vehicles.removeElement(vehicle); }
	
	// Perform time-related processes
	
	public void timePasses(int seconds) { facilityManager.timePasses(seconds); }

	// Returns a detail window for the unit

	public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) { return new SettlementDialog(parentDesktop, this); } 
}
