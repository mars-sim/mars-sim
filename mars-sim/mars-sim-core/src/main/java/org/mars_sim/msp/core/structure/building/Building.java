/**
 * Mars Simulation Project
 * Building.java
 * @version 3.00 2010-08-25
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.structure.building;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.building.function.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The Building class is a settlement's building.
 */
public class Building implements Malfunctionable, Serializable {
    
    // Power Modes
    public static final String FULL_POWER = "Full Power";
    public static final String POWER_DOWN = "Power Down";
    public static final String NO_POWER = "No Power";
    
    // Maintenance info
    private static final double WEAR_LIFETIME = 3340000D; // 3340 Sols (5 orbits)
    private static final double MAINTENANCE_TIME = 1000D; // 1 Sol
    
    // Data members
    protected BuildingManager manager; 
    protected String name;
    protected double width;
    protected double length;
    protected double xLoc;
    protected double yLoc;
    protected double facing;
    protected String powerMode;
    protected MalfunctionManager malfunctionManager;
    protected List<Function> functions;
    protected double basePowerRequirement;
    protected double basePowerDownPowerRequirement;
    
    /**
     * Constructs a Building object.
     * @param template the building template.
     * @param manager the building's building manager.
     * @throws BuildingException if building can not be created.
     */
    public Building(BuildingTemplate template, BuildingManager manager) {
        this(template.getType(), template.getXLoc(), template.getYLoc(), 
                template.getFacing(), manager);
    }
    
    /**
     * Constructs a Building object.
     * @param name the building's name.
     * @param xLoc the x location of the building in the settlement.
     * @param yLoc the y location of the building in the settlement.
     * @param facing the facing of the building (degrees clockwise from North).
     * @param manager the building's building manager.
     * @throws BuildingException if building can not be created.
     */
    public Building(String name, double xLoc, double yLoc, double facing, 
            BuildingManager manager) {
        
        this.name = name;
        this.manager = manager;
        powerMode = FULL_POWER;
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.facing = facing;
        
//        try {
        	// Get the building's functions
        	functions = determineFunctions();
        	
        	// Get base power requirements.
			BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
			
			basePowerRequirement = config.getBasePowerRequirement(name);
			basePowerDownPowerRequirement = config.getBasePowerDownPowerRequirement(name);
			
			// Get building's dimensions.
			width = config.getWidth(name);
			length = config.getLength(name);
//        }
//        catch (Exception e) {
//        	throw new BuildingException("Building " + name + " cannot be constructed: " + e.getMessage());
//        }
        
		// Set up malfunction manager.
		malfunctionManager = new MalfunctionManager(this, WEAR_LIFETIME, MAINTENANCE_TIME);
		malfunctionManager.addScopeString("Building");
		
		// Add each function to the malfunction scope.
		Iterator<Function> i = functions.iterator();
		while (i.hasNext()) {
            Function function = i.next();
            for (int x = 0; x < function.getMalfunctionScopeStrings().length; x++) {
                malfunctionManager.addScopeString(function.getMalfunctionScopeStrings()[x]);
            }
        }
    }
    
    /**
     * Empty constructor.
     */
    protected Building() {
    	
    }
    
    /**
     * Determines the building functions.
     * @return list of building functions.
     * @throws Exception if error in functions.
     */
    private List<Function> determineFunctions() {
    	List<Function> buildingFunctions = new ArrayList<Function>();
    	
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
    	
    	// Set power generation function.
    	if (config.hasPowerGeneration(name)) buildingFunctions.add(new PowerGeneration(this));
    	
    	// Set life support function.
    	if (config.hasLifeSupport(name)) buildingFunctions.add(new LifeSupport(this));
    	
    	// Set living accommodations function.
    	if (config.hasLivingAccommodations(name)) buildingFunctions.add(new LivingAccommodations(this));
    	
    	// Set research function.
    	if (config.hasResearchLab(name)) buildingFunctions.add(new Research(this));
    	
    	// Set communication function.
    	if (config.hasCommunication(name)) buildingFunctions.add(new Communication(this));
    	
    	// Set EVA function.
    	if (config.hasEVA(name)) buildingFunctions.add(new EVA(this));
    	
    	// Set recreation function.
    	if (config.hasRecreation(name)) buildingFunctions.add(new Recreation(this));
    	
    	// Set dining function.
    	if (config.hasDining(name)) buildingFunctions.add(new Dining(this));
    	
    	// Set resource processing function.
    	if (config.hasResourceProcessing(name)) buildingFunctions.add(new ResourceProcessing(this));
    	
    	// Set storage function.
    	if (config.hasStorage(name)) buildingFunctions.add(new Storage(this));
    	
    	// Set medical care function.
    	if (config.hasMedicalCare(name)) buildingFunctions.add(new MedicalCare(this));
    	
    	// Set farming function.
    	if (config.hasFarming(name)) buildingFunctions.add(new Farming(this));
    	
    	// Set exercise function.
    	if (config.hasExercise(name)) buildingFunctions.add(new Exercise(this));
    	
    	// Set ground vehicle maintenance function.
    	if (config.hasGroundVehicleMaintenance(name)) buildingFunctions.add(new GroundVehicleMaintenance(this));
    	
    	// Set cooking function.
    	if (config.hasCooking(name)) buildingFunctions.add(new Cooking(this));
    	
    	// Set manufacture function.
    	if (config.hasManufacture(name)) buildingFunctions.add(new Manufacture(this));
        
        // Set power storage function.
        if (config.hasPowerStorage(name)) buildingFunctions.add(new PowerStorage(this));
        
        //set astronomical observation function
        if(config.hasAstronomicalObservation(name)) buildingFunctions.add(new AstronomicalObservation(this));
    	
    	return buildingFunctions;
    }
    
    /**
     * Checks if a building has a particular function.
     * @param functionName the name of the function.
     * @return true if function.
     */
    public boolean hasFunction(String functionName) {
    	boolean result = false;
    	Iterator<Function> i = functions.iterator();
    	while (i.hasNext()) {
    		if (i.next().getName().equals(functionName)) result = true;
    	}
    	return result;
    }
    
    /**
     * Gets a function if the building has it.
     * @param functionName the name of the function.
     * @return function.
     * @throws BuildingException if building doesn't have the function.
     */
    public Function getFunction(String functionName)  {
    	Function result = null;
    	Iterator<Function> i = functions.iterator();
    	while (i.hasNext()) {
    		Function function = i.next();
    		if (function.getName().equals(functionName)) result = function;
    	}
    	if (result != null) return result;
    	else throw new IllegalStateException(name + " does not have " + functionName);
    }
    
    /**
     * Gets the building's building manager.
     *
     * @return building manager
     */
    public BuildingManager getBuildingManager() {
        return manager;
    }
    
    /**
     * Gets the building's name.
     *
     * @return building's name as a String.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the building's width.
     * @return width (meters).
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * Gets the building's length.
     * @return length (meters).
     */
    public double getLength() {
        return length;
    }
    
    /**
     * Gets the x location of the building in the settlement.
     * @return x location (meters from settlement center - West: positive, East: negative).
     */
    public double getXLocation() {
        return xLoc;
    }
    
    /**
     * Gets the y location of the building in the settlement.
     * @return y location (meters from settlement center - North: positive, South: negative).
     */
    public double getYLocation() {
        return yLoc;
    }
    
    /**
     * Gets the facing of the building.
     * @return facing (degrees from North clockwise).
     */
    public double getFacing() {
        return facing;
    }
    
    /**
     * Time passing for building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
        
        // Check for valid argument.
        if (time < 0D) throw new IllegalArgumentException("Time must be > 0D");
        
//        try {
        	// Send time to each building function.
        	Iterator<Function> i = functions.iterator();
        	while (i.hasNext()) i.next().timePassing(time);
        	
        	// Update malfunction manager.
        	malfunctionManager.timePassing(time);
        	
        	// If powered up, active time passing.
        	if (powerMode.equals(FULL_POWER)) malfunctionManager.activeTimePassing(time);
//        }
//        catch (Exception e) {
//        	throw new BuildingException("Building " + getName() + " timePassing(): " + e.getMessage());
//        }
    }   
    
    /**
     * Gets the power this building currently requires for full-power mode.
     * @return power in kW.
     */
    public double getFullPowerRequired()  {
		double result = basePowerRequirement;
    	
    	// Determine power required for each function.
    	Iterator<Function> i = functions.iterator();
    	while (i.hasNext()) result += i.next().getFullPowerRequired();
    	
    	return result;
    }
    
    /**
     * Gets the power the building requires for power-down mode.
     * @return power in kW.
     */
    public double getPoweredDownPowerRequired() {
		double result = basePowerDownPowerRequirement;
		
		// Determine power required for each function.
		Iterator<Function> i = functions.iterator();
		while (i.hasNext()) result += i.next().getPowerDownPowerRequired();
		
		return result;
    }
     
    /**
     * Gets the building's power mode.
     */
    public String getPowerMode() {
        return powerMode;
    }
    
    /**
     * Sets the building's power mode.
     */
    public void setPowerMode(String powerMode) {
        this.powerMode = powerMode;
    }
    
    /**
     * Gets the entity's malfunction manager.
     * @return malfunction manager
     */
    public MalfunctionManager getMalfunctionManager() {
        return malfunctionManager;
    }
    
    /**
     * Gets a collection of people affected by this entity.
     * Children buildings should add additional people as necessary.
     * @return person collection
     */
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = new ConcurrentLinkedQueue<Person>();

		// If building has life support, add all occupants of the building.
		if (hasFunction(LifeSupport.NAME)) {
//			try {
				LifeSupport lifeSupport = (LifeSupport) getFunction(LifeSupport.NAME);
				Iterator<Person> i = lifeSupport.getOccupants().iterator();
				while (i.hasNext()) {
					Person occupant = i.next();
					if (!people.contains(occupant)) people.add(occupant);
				}
//			}
//			catch (BuildingException e) {}
		}

        // Check all people in settlement.
        Iterator<Person> i = manager.getSettlement().getInhabitants().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this building. 
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }

            // Add all people repairing this facility.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }
        }

        return people;
    }
    
    /**
     * Gets the inventory associated with this entity.
     * @return inventory
     */
    public Inventory getInventory() {
        return manager.getSettlement().getInventory();
    }
    
    /**
     * String representation of this building.
     * @return The settlement and building name.
     */
    public String toString() {
        return name;
    }
}