/**
 * Mars Simulation Project
 * Person.java
 * @version 2.71 2001-01-30
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Person class represents a person on the virtual Mars. It keeps
 *  track of everything related to that person and provides
 *  information about him/her.
 */
public class Person extends Unit {

    // Data members
    private Settlement settlement; // Person's current settlement
    private Vehicle vehicle; // Vehicle person is riding in
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private SkillManager skills; // Manager for Person's skills
    private TaskManager tasks; // Manager for Person's tasks
    private String locationSituation; // Where person is ("In Settlement", "In Vehicle", "Outside")

    /** Constructs a Person object
     *  @param name the person's name
     *  @param location the person's location
     *  @param mars the virtual Mars
     *  @param manager the person's unit manager
     */
    Person(String name, Coordinates location, VirtualMars mars, UnitManager manager) {

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
     *  @return the person's location  
     */
    public String getLocationSituation() {
        return locationSituation;
    }

    /** Sets the person's relative location "In Settlement", "In
     *  Vehicle" or "Outside"
     *  @param newLocation the new location
     */
    void setLocationSituation(String newLocation) {
        locationSituation = newLocation;
    }

    /** Get settlement person is at, null if person is not at
     *  a settlement
     *  @return the person's settlement  
     */
    public Settlement getSettlement() {
        return settlement;
    }

    /** Get vehicle person is in, null if person is not in vehicle 
     *  @return the person's vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /** Makes the person an inhabitant of a given settlement 
     *  @param settlement the person's settlement
     */
    void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        location.setCoords(settlement.getCoordinates());
        settlement.addPerson(this);
        vehicle = null;
    }

    /** Makes the person a passenger in a vehicle 
     *  @param vehicle the person's vehicle
     */
    void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        settlement = null;
    }

    /** Action taken by person during unit turn 
     *  @param seconds for action
     */
    void timePasses(int seconds) {
        consumeOxygen(1D / 24D / 60D / 60D * (double) seconds);
        consumeWater(1D / 24D / 60D / 60D * (double) seconds);
        
        // Later to be replaced with a eat meal task.
        consumeFood(1D / 24D / 60D / 60D * (double) seconds);
        
        tasks.takeAction(seconds);
    }

    /** Returns a reference to the Person's natural attribute manager 
     *  @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }

    /** Returns a reference to the Person's skill manager 
     *  @return the person's skill manager
     */
    public SkillManager getSkillManager() {
        return skills;
    }

    /** Returns a reference to the Person's task manager 
     *  @return the person's task manager
     */
    public TaskManager getTaskManager() {
        return tasks;
    }
    
    /** Person consumes given amount of oxygen
     *  @param amount amount of oxygen to consume (in units)
     */
    void consumeOxygen(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeOxygen(amount);
        }
        else amountRecieved = vehicle.removeOxygen(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs oxygen.");
    }
    
    /** Person consumes given amount of water
     *  @param amount amount of water to consume (in units)
     */
    void consumeWater(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeWater(amount);
        }
        else amountRecieved = vehicle.removeWater(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs water.");
    }
    
    /** Person consumes given amount of food
     *  @param amount amount of food to consume (in units)
     */
    void consumeFood(double amount) {
        double amountRecieved = 0D;
        
        if (locationSituation.equals("In Settlement")) {
            FacilityManager manager = settlement.getFacilityManager();
            StoreroomFacility stores = (StoreroomFacility) manager.getFacility("Storerooms");
            amountRecieved = stores.removeFood(amount);
        }
        else amountRecieved = vehicle.removeFood(amount);
        
        // if (amountRecieved != amount) System.out.println(getName() + " needs food.");
    }
}
