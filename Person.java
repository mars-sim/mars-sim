/**
 * Mars Simulation Project
 * Person.java
 * @version 2.70 2000-09-01
 * @author Scott Davis
 */

import java.awt.*;
import javax.swing.*;

/** The Person class represents a person on the virtual Mars. It keeps
 *  track of everything related to that person and provides
 *  information about him/her.
 */
public class Person extends Unit {

    private Settlement settlement;     // Person's current settlement
    private Vehicle vehicle;           // Vehicle person is riding in
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private SkillManager skills;      // Manager for Person's skills
    private TaskManager tasks;        // Manager for Person's tasks
    private String locationSituation; // Where person is ("In Settlement", "In Vehicle", "Outside")

    public Person(String name, Coordinates location, VirtualMars mars, UnitManager manager) {
		
	// Use Unit constructor
	super(name, location, mars, manager);
		
	// Initialize data members
	settlement = null;
	vehicle = null;
	attributes = new NaturalAttributeManager();
	skills = new SkillManager();
	tasks = new TaskManager(this, mars);
	locationSituation = new String("In Settlement");
    }

    /** Returns a string for the person's relative location "In
     *  Settlement", "In Vehicle" or "Outside"
     */
    public String getLocationSituation() {
	return locationSituation;
    }
	
    /** Sets the person's relative location "In Settlement", "In
     *  Vehicle" or "Outside"
     */
    public void setLocationSituation(String newLocation) {
	locationSituation = newLocation;
    }

    /** Get settlement person is at, null if person is not at
     *  settlement
     */
    public Settlement getSettlement() {
	return settlement;
    }

    /** Get vehicle person is in, null if person is not in vehicle */
    public Vehicle getVehicle() {
	return vehicle;
    }

    /** Makes the person an inhabitant of a given settlement */
    public void setSettlement(Settlement settlement) {
	this.settlement = settlement;
	location.setCoords(settlement.getCoordinates());
	settlement.addPerson(this);
	vehicle = null;
    }
	
    /** Makes the person a passenger in a vehicle */
    public void setVehicle(Vehicle vehicle) { 
	this.vehicle = vehicle; 
	settlement = null;	
    }

    /** Action taken by person during unit turn */
    public void timePasses(int seconds) {
	tasks.takeAction(seconds);
    }

    /** Returns a reference to the Person's natural attribute manager */
    public NaturalAttributeManager getNaturalAttributeManager() {
	return attributes;
    }
	
    /** Returns a reference to the Person's skill manager */
    public SkillManager getSkillManager() {
	return skills;
    }
	
    /** Returns a reference to the Person's task manager */
    public TaskManager getTaskManager() {
	return tasks;
    }

    /** Returns a detail window for the unit */
    public UnitDialog getDetailWindow(MainDesktopPane parentDesktop) {
	return new PersonDialog(parentDesktop, this);
    }
}
