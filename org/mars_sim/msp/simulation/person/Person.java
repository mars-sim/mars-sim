/**
 * Mars Simulation Project
 * Person.java
 * @version 2.78 2004-11-16
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.List;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.ai.Mind;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The Person class represents a person on the virtual Mars. It keeps
 * track of everything related to that person and provides
 * information about him/her.
 */
public class Person extends Unit implements Serializable {

    /**
     * Status string used when Person resides in settlement
     */
    public final static String INSETTLEMENT = "In Settlement";

    /**
     * Status string used when Person resides in a vehicle
     */
    public final static String INVEHICLE = "In Vehicle";

    /**
     * Status string used when Person is outside
     */
    public final static String OUTSIDE = "Outside";

    /**
     * Status string used when Person has been buried
     */
    public final static String BURIED = "Buried";

	public final static String MALE = "male";
	public final static String FEMALE = "female";

    // Data members
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private SkillManager skills; // Manager for Person's skills
    private Mind mind; // Person's mind
    private PhysicalCondition health; // Person's physical
    private boolean isBuried; // True if person is dead and buried.
    private String gender; // The gender of the person (male or female).

    /** 
     * Constructs a Person object at a given settlement
     *
     * @param name the person's name
     * @param gender the person's gender ("male" or "female")
     * @param settlement the settlement the person is at
     * @throws Exception if no inhabitable building available at settlement.
     */
    public Person(String name, String gender, Settlement settlement) throws Exception {
        // Use Unit constructor
        super(name, settlement.getCoordinates());
		
		// Initialize data members
		this.gender = gender;
		attributes = new NaturalAttributeManager(this);
		skills = new SkillManager(this);
		mind = new Mind(this);
		isBuried = false;
		health = new PhysicalCondition(this);

		// Set base mass of person.
		baseMass = 70D;

		// Set inventory total mass capacity.
		inventory.setTotalCapacity(100D);
		inventory.setResourceCapacity(Resource.ROCK_SAMPLES, 100D);
		inventory.setResourceCapacity(Resource.ICE, 100D);
		inventory.setResourceCapacity(Resource.FOOD, 100D);
		
		// Put person in proper building.
	    settlement.getInventory().addUnit(this);
        BuildingManager.addToRandomBuilding(this, settlement);
    }

    /** Returns a string for the person's relative location "In
     *  Settlement", "In Vehicle", "Outside" or "Buried"
     *  @return the person's location
     */
    public String getLocationSituation() {
        String location = null;

	    if (isBuried) location = BURIED;
	    else {
	        Unit container = getContainerUnit();
	        if (container == null) location = OUTSIDE;
	        else if (container instanceof Settlement) location = INSETTLEMENT;
	        else if (container instanceof Vehicle) location = INVEHICLE;
	    }

        return location;
    }

    /** Get settlement person is at, null if person is not at
     *  a settlement
     *  @return the person's settlement
     */
    public Settlement getSettlement() {

        Unit topUnit = getTopContainerUnit();
        if ((topUnit != null) && (topUnit instanceof Settlement)) {
	        return (Settlement) topUnit;
        }
        else return null;
    }

    /** Get vehicle person is in, null if person is not in vehicle
     *  @return the person's vehicle
     */
    public Vehicle getVehicle() {

        if ((containerUnit != null) && (containerUnit instanceof Vehicle)) {
	        return (Vehicle) containerUnit;
	    }
	    else return null;
    }

    /** Sets the unit's container unit.
     *  Overridden from Unit class.
     *  @param containerUnit the unit to contain this unit.
     */
    public void setContainerUnit(Unit containerUnit) {
        super.setContainerUnit(containerUnit);

        MedicalAid aid = getAccessibleAid();
        if ((aid != null) && (health.canTreatProblems(aid)))
            health.requestAllTreatments(aid);
    }

    /**
     * Bury the Person at the current location. The person is removed from
     * any containing Settlements or Vehicles. The body is fixed at the last
     * location of the containing unit.
     */
    public void buryBody() {
        if (containerUnit != null) containerUnit.getInventory().dropUnitOutside(this);
	    isBuried = true;
    }

    /**
     * Person has died. Update the status to reflect the change and remove
     * this Person from any Task and remove the associated Mind.
     */
    void setDead() {
        mind.setInactive();
        buryBody();
    }

    /** 
     * Person can take action with time passing
     * @param time amount of time passing (in millisols)
     * throws Exception if error during time.
     */
    public void timePassing(double time) throws Exception {
        
        try {
        	// If Person is dead, then skip
        	if (health.getDeathDetails() == null) {
        		SimulationConfig simConfig = Simulation.instance().getSimConfig();
            	PersonConfig config = simConfig.getPersonConfiguration();
            	LifeSupport support = getLifeSupport();
            
            	// Pass the time in the physical condition first as this may result in death.
            	if (health.timePassing(time, support, config)) {
            		// Mental changes with time passing.
            		mind.timePassing(time);
            	}
            	else {
                	// Person has died as a result of physical condition
                	setDead();
            	}
        	}
        }
        catch (Exception e) {
        	throw new Exception("Person " + getName() + " timePassing(): " + e.getMessage());
        }
    }

    /** Returns a reference to the Person's natural attribute manager
     *  @return the person's natural attribute manager
     */
    public NaturalAttributeManager getNaturalAttributeManager() {
        return attributes;
    }

    /**
     * Get the performance factor that effect Person with the complaint.
     * @return The value is between 0 -> 1.
     */
    public double getPerformanceRating() {
        return health.getPerformanceFactor();
    }

    /** Returns a reference to the Person's physical condition
     *  @return the person's physical condition
     */
    public PhysicalCondition getPhysicalCondition() {
        return health;
    }

    /** Returns a reference to the Person's skill manager
     *  @return the person's skill manager
     */
    public SkillManager getSkillManager() {
        return skills;
    }

    /**
     * Find a medical aid according to the current location.
     * @return Accessible aid.
     */
    MedicalAid getAccessibleAid() {
        MedicalAid found = null;
        
        String location = getLocationSituation();
        if (location.equals(INSETTLEMENT)) {
            Settlement settlement = getSettlement();
            List infirmaries = settlement.getBuildingManager().getBuildings(MedicalCare.NAME);
            if (infirmaries.size() > 0) {
                int rand = RandomUtil.getRandomInt(infirmaries.size() - 1);
                Building foundBuilding = (Building) infirmaries.get(rand);
                try {
                	found = (MedicalAid) foundBuilding.getFunction(MedicalCare.NAME);
                }
                catch (BuildingException e) {}
            }
        }
        if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = getVehicle();
            if (vehicle instanceof Medical) found = ((Medical) vehicle).getSickBay();
        }

        return found;
    }

    /** Returns the person's mind
     *  @return the person's mind
     */
    public Mind getMind() {
        return mind;
    }

    /**
     * Get the LifeSupport system supporting this Person. This may be from
     * the Settlement, Vehicle or Equipment.
     *
     * @return Life support system.
     */
    private LifeSupport getLifeSupport() {

        UnitCollection lifeSupportUnits = new UnitCollection();

	    // Get all container units.
	    Unit container = getContainerUnit();
	    while (container != null) {
            if (container instanceof LifeSupport) lifeSupportUnits.add(container);
	        container = container.getContainerUnit();
	    }

	    // Get all contained units.
        UnitIterator i = inventory.getContainedUnits().iterator();
	    while (i.hasNext()) {
	        Unit contained = i.next();
	        if (contained instanceof LifeSupport) lifeSupportUnits.add(contained);
	    }

	    // Get first life support unit that checks out.
	    i = lifeSupportUnits.iterator();
	    while (i.hasNext()) {
	        LifeSupport goodUnit = (LifeSupport) i.next();
	        if (goodUnit.lifeSupportCheck()) return goodUnit;
	    }

	    // If no good units, just get first life support unit.
	    i = lifeSupportUnits.iterator();
	    if (i.hasNext()) return (LifeSupport) i.next();

	    // If no life support units at all, return null.
	    return null;
    }


    /** 
     * Person consumes given amount of food
     * @param amount amount of food to consume (in kg)
     * @param takeFromInv is food taken from local inventory?
     */
    public void consumeFood(double amount, boolean takeFromInv) {
    	if (takeFromInv) health.consumeFood(amount, getContainerUnit());
    	else health.consumeFood(amount);
    }
    
    /**
     * Gets the gender of the person ("male" or "female")
     * @return the gender
     */
    public String getGender() {
    	return gender;
    }
    
	/**
	 * Gets the person's local group of people (in building or rover)
	 * @return collection of people in person's location.
	 * @throws Exception if error
	 */
	public PersonCollection getLocalGroup() throws Exception {
		PersonCollection localGroup = new PersonCollection();
		
		if (getLocationSituation().equals(Person.INSETTLEMENT)) {
			Building building = BuildingManager.getBuilding(this);
			if (building.hasFunction(org.mars_sim.msp.simulation.structure.building.function.LifeSupport.NAME)) {
				org.mars_sim.msp.simulation.structure.building.function.LifeSupport lifeSupport = 
					(org.mars_sim.msp.simulation.structure.building.function.LifeSupport) 
					building.getFunction(org.mars_sim.msp.simulation.structure.building.function.LifeSupport.NAME);
				localGroup = new PersonCollection(lifeSupport.getOccupants());
			}
		}
		else if (getLocationSituation().equals(Person.INVEHICLE)) {
			Rover rover = (Rover) getVehicle();
			localGroup = new PersonCollection(rover.getCrew());
		}
		
		if (localGroup.contains(this)) localGroup.remove(this);
		
		return localGroup;
	}    
}