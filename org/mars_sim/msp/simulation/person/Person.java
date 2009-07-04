/**
 * Mars Simulation Project
 * Person.java
 * @version 2.87 2009-07-03
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.LifeSupport;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.person.ai.Mind;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import org.mars_sim.msp.simulation.time.EarthClock;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.simulation.vehicle.Medical;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.simulation.vehicle.VehicleOperator;

/** 
 * The Person class represents a person on Mars. It keeps
 * track of everything related to that person and provides
 * information about him/her.
 */
public class Person extends Unit implements VehicleOperator, Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.Person";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Unit update events
	public final static String ASSOCIATED_SETTLEMENT_EVENT = "associated settlement";
	
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
	
	// The base carrying capacity (kg) of a person.
	private final static double BASE_CAPACITY = 60D;

    // Data members
    private NaturalAttributeManager attributes; // Manager for Person's natural attributes
    private Mind mind; // Person's mind
    private PhysicalCondition health; // Person's physical
    private boolean isBuried; // True if person is dead and buried.
    private String gender; // The gender of the person (male or female).
    private EarthClock birthTimeStamp; // The birth time of the person.
    private Settlement associatedSettlement; // The settlement the person is currently associated with.

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

		// Set a birth time for the person
		int year = 2003 + RandomUtil.getRandomInt(10)+ RandomUtil.getRandomInt(10);
		int month = RandomUtil.getRandomInt(11)+1;
		int day;		
		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {day = RandomUtil.getRandomInt(28)+1;}
		else {day = RandomUtil.getRandomInt(27)+1;}
				}
		else 	{
			if (month%2 == 1) {day = RandomUtil.getRandomInt(30)+1;}
			else {day = RandomUtil.getRandomInt(29)+1;}
		}

		int hour = RandomUtil.getRandomInt(23);
		int minute = RandomUtil.getRandomInt(59);
		int second = RandomUtil.getRandomInt(59);

		String timeString = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second; // We use this to initialize the stamp
		birthTimeStamp = new EarthClock (timeString);

		attributes = new NaturalAttributeManager(this);
		mind = new Mind(this);
		isBuried = false;
		health = new PhysicalCondition(this);

		// Set base mass of person.
		setBaseMass(70D);

		// Set inventory total mass capacity based on the person's strength.
		int strength = attributes.getAttribute(NaturalAttributeManager.STRENGTH);
		getInventory().addGeneralCapacity(BASE_CAPACITY + strength);
		
		// Put person in proper building.
		settlement.getInventory().storeUnit(this);
        BuildingManager.addToRandomBuilding(this, settlement);
        associatedSettlement = settlement;
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

    /** 
     * Get settlement person is at, null if person is not at
     * a settlement
     * @return the person's settlement
     */
    public Settlement getSettlement() {
    	if (INSETTLEMENT.equals(getLocationSituation())) 
    		return (Settlement) getContainerUnit();
    	else return null;
    }

    /** 
     * Get vehicle person is in, null if person is not in vehicle
     * @return the person's vehicle
     */
    public Vehicle getVehicle() {
    	if (INVEHICLE.equals(getLocationSituation()))
    		return (Vehicle) getContainerUnit();
    	else return null;
    }

    /** 
     * Sets the unit's container unit.
     * Overridden from Unit class.
     * @param containerUnit the unit to contain this unit.
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
    	Unit containerUnit = getContainerUnit();
        if (containerUnit != null) {
        	try {
        		containerUnit.getInventory().retrieveUnit(this);
        	}
        	catch (InventoryException e) {
        		logger.log(Level.SEVERE,"Could not bury " + getName());
        		e.printStackTrace(System.err);
        	}
        }
	    isBuried = true;
	    setAssociatedSettlement(null);
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
        		
            	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
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
        	e.printStackTrace(System.err);
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

    /** Returns the person's age
     *  @return the person's age
     */
    public int getAge() { //FIXME: add stuff for handling leap years
	EarthClock simClock = Simulation.instance().getMasterClock().getEarthClock();
	long simTimeinMillis = simClock.getTimeInMillis();
	long personTimeinMillis = birthTimeStamp.getTimeInMillis();
	int age = (int)((simTimeinMillis - personTimeinMillis)/31536000)/1000; // we need to divide twice due to integer restraints
        return age;
    }
    /** Returns the person's birth date
     *  @return the person's birth date
     */

    public String getBirthDate() { 
	return birthTimeStamp.getDateString();
    }

    /**
     * Get the LifeSupport system supporting this Person. This may be from
     * the Settlement, Vehicle or Equipment.
     *
     * @return Life support system.
     */
    private LifeSupport getLifeSupport() {

        Collection<Unit> lifeSupportUnits = new ConcurrentLinkedQueue<Unit>();

	    // Get all container units.
	    Unit container = getContainerUnit();
	    while (container != null) {
            if (container instanceof LifeSupport) lifeSupportUnits.add(container);
	        container = container.getContainerUnit();
	    }

	    // Get all contained units.
	    Iterator<Unit> i = getInventory().getContainedUnits().iterator();
	    while (i.hasNext()) {
	        Unit contained = i.next();
	        if (contained instanceof LifeSupport) lifeSupportUnits.add(contained);
	    }

	    // Get first life support unit that checks out.
	    i = lifeSupportUnits.iterator();
	    while (i.hasNext()) {
	        LifeSupport goodUnit = (LifeSupport) i.next();
	        try {
	        	if (goodUnit.lifeSupportCheck()) return goodUnit;
	        }
	        catch (Exception e) {}
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
     * @throws Exception if error consuming food.
     */
    public void consumeFood(double amount, boolean takeFromInv) throws Exception {
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
	public Collection<Person> getLocalGroup() throws Exception {
		Collection<Person> localGroup = new ConcurrentLinkedQueue<Person>();
		
		if (getLocationSituation().equals(Person.INSETTLEMENT)) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null) {
				if (building.hasFunction(org.mars_sim.msp.simulation.structure.building.function.LifeSupport.NAME)) {
					org.mars_sim.msp.simulation.structure.building.function.LifeSupport lifeSupport = 
						(org.mars_sim.msp.simulation.structure.building.function.LifeSupport) 
						building.getFunction(org.mars_sim.msp.simulation.structure.building.function.LifeSupport.NAME);
					localGroup = new ConcurrentLinkedQueue<Person>(lifeSupport.getOccupants());
				}
			}
		}
		else if (getLocationSituation().equals(Person.INVEHICLE)) {
			Crewable crewableVehicle = (Crewable) getVehicle();
			localGroup = new ConcurrentLinkedQueue<Person>(crewableVehicle.getCrew());
		}
		
		if (localGroup.contains(this)) localGroup.remove(this);
		
		return localGroup;
	}    
	
	/**
	 * Checks if the vehicle operator is fit for operating the vehicle.
	 * @return true if vehicle operator is fit.
	 */
	public boolean isFitForOperatingVehicle() {
		return !health.hasSeriousMedicalProblems();
	}
	
	/**
	 * Gets the name of the vehicle operator
	 * @return vehicle operator name.
	 */
	public String getOperatorName() {
		return getName();
	}
	
	/**
	 * Gets the settlement the person is currently associated with.
	 * @return associated settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return associatedSettlement;
	}
	
	/**
	 * Sets the associated settlement for a person.
	 * @param newSettlement the new associated settlement or null if none.
	 */
	public void setAssociatedSettlement(Settlement newSettlement) {
		if (associatedSettlement != newSettlement) {
			Settlement oldSettlement = associatedSettlement;
			associatedSettlement = newSettlement;
			fireUnitUpdate(ASSOCIATED_SETTLEMENT_EVENT, associatedSettlement);
			if (oldSettlement != null) 
				oldSettlement.fireUnitUpdate(Settlement.REMOVE_ASSOCIATED_PERSON_EVENT, this);
			if (newSettlement != null) 
				newSettlement.fireUnitUpdate(Settlement.ADD_ASSOCIATED_PERSON_EVENT, this);
		}
	}
}