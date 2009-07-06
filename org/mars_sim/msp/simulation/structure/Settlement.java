/**
 * Mars Simulation Project
 * Settlement.java
 * @version 2.87 2009-07-05
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.structure;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.CollectionUtils;
import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PhysicalCondition;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.person.ai.task.Maintenance;
import org.mars_sim.msp.simulation.person.ai.task.Repair;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.science.Science;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingException;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.EVA;
import org.mars_sim.msp.simulation.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.simulation.structure.construction.ConstructionManager;
import org.mars_sim.msp.simulation.structure.goods.GoodsManager;
import org.mars_sim.msp.simulation.vehicle.Vehicle;


/** 
 * The Settlement class represents a settlement unit on virtual Mars.
 * It contains information related to the state of the settlement.
 */
public class Settlement extends Structure implements org.mars_sim.msp.simulation.LifeSupport {
    
    private static String CLASS_NAME = 
	    "org.mars_sim.msp.simulation.structure.Settlement";
	
   private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Unit update events.
	public static final String ADD_ASSOCIATED_PERSON_EVENT = "add associated person";
	public static final String REMOVE_ASSOCIATED_PERSON_EVENT = "remove associated person";
	
    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D;        // Normal temperature (celsius)

    // Data members
    protected BuildingManager buildingManager; // The settlement's building manager.
    protected ResupplyManager resupplyManager; // The settlement's resupply manager.
    protected GoodsManager goodsManager; // The settlement's goods manager.
    protected ConstructionManager constructionManager; // The settlement's construction manager.
    protected PowerGrid powerGrid; // The settlement's building power grid.
    private String template; // The settlement template name.
    private boolean missionCreationOverride; // Override flag for mission creation at settlement.
    private boolean manufactureOverride; // Override flag for manufacturing at settlement.
    private boolean resourceProcessOverride; // Override flag for resource process at settlement.
    private Map<Science, Double> scientificAchievement; // The settlement's achievement in scientific fields.
    
    /**
     * Constructor for subclass extension.
     * @param name the settlement's name
     * @param location the settlement's location
     */
    protected Settlement(String name, Coordinates location) {
    	// Use Structure constructor.
    	super(name, location);
    }
    
    /** 
     * Constructs a Settlement object at a given location
     * @param name the settlement's name
     * @param template for the settlement
     * @param location the settlement's location
     * @throws Exception if settlement cannot be constructed.
     */
    public Settlement(String name, String template, Coordinates location) throws Exception { 
        // Use Structure constructor
        super(name, location);
        
        this.template = template;
        
		// Set inventory total mass capacity.
		getInventory().addGeneralCapacity(Double.MAX_VALUE);
        
        // Initialize building manager
        buildingManager = new BuildingManager(this);
        
        // Initialize resupply manager
        resupplyManager = new ResupplyManager(this);
        
        // Initialize goods manager.
        goodsManager = new GoodsManager(this);
        
        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);
       
        // Initialize power grid
        powerGrid = new PowerGrid(this);
       
        // Add scope string to malfunction manager.
        malfunctionManager.addScopeString("Settlement");
    }
    
    /** 
     * Gets the population capacity of the settlement
     * @return the population capacity
     */
    public int getPopulationCapacity() {
        int result = 0;
        Iterator i = buildingManager.getBuildings(LivingAccommodations.NAME).iterator();
        while (i.hasNext()) {
        	try {
        		Building building = (Building) i.next();
        		LivingAccommodations livingAccommodations = 
        			(LivingAccommodations) building.getFunction(LivingAccommodations.NAME);
        		result += livingAccommodations.getBeds();
        	} 
        	catch (BuildingException e) {}
        }
        
        return result;
    }

    /** Gets the current population number of the settlement
     *  @return the number of inhabitants
     */
    public int getCurrentPopulationNum() {
        return getInhabitants().size();
    }

    /** Gets a collection of the inhabitants of the settlement.
     *  @return Collection of inhabitants
     */
    public Collection<Person> getInhabitants() {
        return CollectionUtils.getPerson(getInventory().getContainedUnits());
    }
    
    /** Gets the current available population capacity
     *  of the settlement
     *  @return the available population capacity
     */
    public int getAvailablePopulationCapacity() {
        return getPopulationCapacity() - getCurrentPopulationNum();
    }

    /** Gets an array of current inhabitants of the settlement
     *  @return array of inhabitants
     */
    public Person[] getInhabitantArray() {
        Collection<Person> people = getInhabitants();
        Person[] personArray = new Person[people.size()];
        Iterator<Person> i = people.iterator();
        int count = 0;
        while (i.hasNext()) {
            personArray[count] = i.next();
            count++;
        }
        return personArray;
    }

    /** Gets a collection of vehicles parked at the settlement.
     *  @return Collection of parked vehicles
     */
    public Collection<Vehicle> getParkedVehicles() {
        return CollectionUtils.getVehicle(getInventory().getContainedUnits());
    }

    /** Gets the number of vehicles parked at the settlement.
     *  @return parked vehicles number
     */
    public int getParkedVehicleNum() {
        return getParkedVehicles().size();
    }
    
    /** Returns true if life support is working properly and is not out
     *  of oxygen or water.
     *  @return true if life support is OK
     *  @throws Exception if error checking life support.
     */
    public boolean lifeSupportCheck() throws Exception {
        boolean result = true;

        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        if (getInventory().getAmountResourceStored(oxygen) <= 0D) result = false;
        AmountResource water = AmountResource.findAmountResource("water");
        if (getInventory().getAmountResourceStored(water) <= 0D) result = false;
        if (getOxygenFlowModifier() < 100D) result = false;
        if (getWaterFlowModifier() < 100D) result = false;
        if (getAirPressure() != NORMAL_AIR_PRESSURE) result = false;
        if (getTemperature() != NORMAL_TEMP) result = false;
    
        return result;
    }

    /** Gets the number of people the life support can provide for.
     *  @return the capacity of the life support system
     */
    public int getLifeSupportCapacity() {
        return getPopulationCapacity();
    }
    
    /** Gets oxygen from system.
     *  @param amountRequested the amount of oxygen requested from system (kg)
     *  @return the amount of oxgyen actually received from system (kg)
     *  @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) throws Exception {
    	AmountResource oxygen = AmountResource.findAmountResource("oxygen");
    	double oxygenTaken = amountRequested;
    	double oxygenLeft = getInventory().getAmountResourceStored(oxygen);
    	if (oxygenTaken > oxygenLeft) oxygenTaken = oxygenLeft;
    	AmountResource carbonDioxide = AmountResource.findAmountResource("carbon dioxide");
    	double carbonDioxideProvided = oxygenTaken;
    	double carbonDioxideCapacity = getInventory().getAmountResourceRemainingCapacity(carbonDioxide, true);
    	if (carbonDioxideProvided > carbonDioxideCapacity) carbonDioxideProvided = carbonDioxideCapacity;
    	try {
    		getInventory().retrieveAmountResource(oxygen, oxygenTaken);
    		getInventory().storeAmountResource(carbonDioxide, carbonDioxideProvided, true);
    	}
    	catch (InventoryException e) {};
        return oxygenTaken * (malfunctionManager.getOxygenFlowModifier() / 100D);
    }

    /**
     * Gets the oxygen flow modifier for this settlement.
     * @return oxygen flow modifier
     */
    public double getOxygenFlowModifier() {
        return malfunctionManager.getOxygenFlowModifier();
    }
    
    /** Gets water from system.
     *  @param amountRequested the amount of water requested from system (kg)
     *  @return the amount of water actually received from system (kg)
     *  @throws Exception if error providing water.
     */
    public double provideWater(double amountRequested) throws Exception {
    	AmountResource water = AmountResource.findAmountResource("water");
    	double waterTaken = amountRequested;
    	double waterLeft = getInventory().getAmountResourceStored(water);
    	if (waterTaken > waterLeft) waterTaken = waterLeft;
    	try {
    		getInventory().retrieveAmountResource(water, waterTaken);
    	}
    	catch (InventoryException e) {};
        return waterTaken * (malfunctionManager.getWaterFlowModifier() / 100D);
    }

    /**
     * Gets the water flow modifier for this settlement.
     * @return water flow modifier
     */
    public double getWaterFlowModifier() {
        return malfunctionManager.getWaterFlowModifier();
    }
    
    /** Gets the air pressure of the life support system.
     *  @return air pressure (atm)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE * (getAirPressureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getAirPressure(getCoordinates());
        if (result < ambient) return ambient;
        else return result;
    }

    /**
     * Gets the air pressure modifier for this settlement.
     * @return air pressure flow modifier
     */
    public double getAirPressureModifier() {
        return malfunctionManager.getAirPressureModifier();
    }
    
    /** Gets the temperature of the life support system.
     *  @return temperature (degrees C)
     */
    public double getTemperature() {
        double result = NORMAL_TEMP * (getTemperatureModifier() / 100D);
        double ambient = Simulation.instance().getMars().getWeather().getTemperature(getCoordinates());
        if (result < ambient) return ambient;
        else return result;
    }

    /**
     * Gets the temperature modifier for this settlement.
     * @return temperature flow modifier
     */
    public double getTemperatureModifier() {
        return malfunctionManager.getTemperatureModifier();
    }
    
    /** 
     * Perform time-related processes
     * @param time the amount of time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    public void timePassing(double time) throws Exception {
    	
		// If settlement is overcrowded, increase inhabitant's stress.
		int overCrowding = getCurrentPopulationNum() - getPopulationCapacity();
		if (overCrowding > 0) {
			double stressModifier = .1D * overCrowding * time;
			Iterator<Person> i = getInhabitants().iterator();
			while (i.hasNext()) {
				PhysicalCondition condition = i.next().getPhysicalCondition();
				condition.setStress(condition.getStress() + stressModifier);
			}
		}
        
        try {
        	// Deliver supplies to settlement if they arrive.
        	resupplyManager.timePassing(time);
        	
        	// If no current population at settlement, power down buildings.
        	if (getCurrentPopulationNum() == 0) {
        		getPowerGrid().setPowerMode(PowerGrid.POWER_DOWN_MODE);
        	}
        	else {
        		getPowerGrid().setPowerMode(PowerGrid.POWER_UP_MODE);
        	}
        
        	powerGrid.timePassing(time);
        	
        	buildingManager.timePassing(time);
        	
        	updateGoodsManager(time);
        	
        	if (getCurrentPopulationNum() > 0) malfunctionManager.activeTimePassing(time);
        	malfunctionManager.timePassing(time);
        }
        catch (Exception e) {
        	e.printStackTrace(System.err);
        	throw new Exception("Settlement " + getName() + " timePassing(): " + e.getMessage());
        }
    }
    
    private void updateGoodsManager(double time) throws Exception {
    	
    	// Randomly update goods manager 1 time per Sol.
        if (!goodsManager.isInitialized() || (time >= RandomUtil.getRandomDouble(1000D))) {
            goodsManager.timePassing(time);
        }
    }

    /**
     * Gets a collection of people affected by this entity.
     * @return person collection
     */
    public Collection<Person> getAffectedPeople() {
        Collection<Person> people = new ConcurrentLinkedQueue<Person>(getInhabitants());

        // Check all people.
        Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this settlement. 
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }

            // Add all people repairing this settlement.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person)) people.add(person);
                }
            }
        }

        return people;
    }
    
    /**
     * Gets the settlement's building manager. 
     * @return building manager
     */
    public BuildingManager getBuildingManager() {
        return buildingManager;
    }
    
    /**
     * Gets the settlement's resupply manager.
     * @return resupply manager
     */
    public ResupplyManager getResupplyManager() {
    	return resupplyManager;
    }
    
    /**
     * Gets the settlement's goods manager.
     * @return goods manager
     */
    public GoodsManager getGoodsManager() {
    	return goodsManager;
    }
    
    /**
     * Gets an available airlock for the settlement.
     * @return airlock or null if none available.
     */
    public Airlock getAvailableAirlock() {
        Airlock result = null;
        
        try {
			BuildingManager manager = getBuildingManager();
			List<Building> evaBuildings = manager.getBuildings(EVA.NAME);
			evaBuildings = BuildingManager.getLeastCrowdedBuildings(evaBuildings);
        	
			if (evaBuildings.size() > 0) {
				// Pick random dining building from list.
				int rand = RandomUtil.getRandomInt(evaBuildings.size() - 1);
				Building building = (Building) evaBuildings.get(rand);
				EVA eva = (EVA) building.getFunction(EVA.NAME);
				result = eva.getAirlock();
			}
        }
        catch (BuildingException e) {
        	logger.log(Level.SEVERE,"Settlement.getAvailableAirlock(): " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Gets the settlement's power grid.
     * @return the power grid.
     */
    public PowerGrid getPowerGrid() {   
        return powerGrid;
    }
    
    /**
     * Gets the settlement template.
     * @return template as string.
     */
    public String getTemplate() {
    	return template;
    }
    
    /**
     * Gets all people associated with this settlement, even if they are out on missions.
     * @return collection of associated people.
     */
    public Collection<Person> getAllAssociatedPeople() {
    	Collection<Person> result = new ConcurrentLinkedQueue<Person>();
    	
    	Iterator<Person> i = Simulation.instance().getUnitManager().getPeople().iterator();
    	while (i.hasNext()) {
    		Person person = i.next();
    		if (person.getAssociatedSettlement() == this) result.add(person);
    	}
    	
    	return result;
    }
    
    /**
     * Gets all vehicles associated with this settlement, even if they are out on missions.
     * @return collection of associated vehicles.
     */
    public Collection<Vehicle> getAllAssociatedVehicles() {
    	Collection<Vehicle> result = getParkedVehicles();
    	
    	// Also add vehicle mission vehicles not parked at settlement.
		Iterator i = Simulation.instance().getMissionManager().getMissionsForSettlement(this).iterator();
		while (i.hasNext()) {
			Mission mission = (Mission) i.next();
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !this.equals(vehicle.getSettlement())) result.add(vehicle);
			}
		}
    	
    	return result;
    }

    /**
     * Sets the mission creation override flag.
     * @param missionCreationOverride override for settlement mission creation.
     */
	public void setMissionCreationOverride(boolean missionCreationOverride) {
		this.missionCreationOverride = missionCreationOverride; 
	}

	/**
	 * Gets the mission creation override flag.
	 * @return override for settlement mission creation.
	 */
	public boolean getMissionCreationOverride() {
		return missionCreationOverride;
	}
	
	/**
	 * Sets the manufacture override flag.
	 * @param manufactureOverride override for manufacture.
	 */
	public void setManufactureOverride(boolean manufactureOverride) {
		this.manufactureOverride = manufactureOverride;
	}
	
	/**
	 * Gets the manufacture override flag.
	 * @return override for settlement manufacture.
	 */
	public boolean getManufactureOverride() {
		return manufactureOverride;
	}
	
	/**
	 * Sets the resource process override flag.
	 * @param resourceProcessOverride override for resource processes.
	 */
	public void setResourceProcessOverride(boolean resourceProcessOverride) {
		this.resourceProcessOverride = resourceProcessOverride;
	}
	
	/**
	 * Gets the resource process override flag.
	 * @return override for settlement resource processes.
	 */
	public boolean getResourceProcessOverride() {
		return resourceProcessOverride;
	}
    
    /**
     * Gets the settlement's construction manager.
     * @return construction manager.
     */
    public ConstructionManager getConstructionManager() {
        return constructionManager;
    }
    
    /**
     * Gets the settlement's achievement credit for a given scientific field.
     * @param science the scientific field.
     * @return achievement credit.
     */
    public double getScientificAchievement(Science science) {
        double result = 0D;
        
        if (scientificAchievement.containsKey(science)) 
            result = scientificAchievement.get(science);
        
        return result;
    }
    
    /**
     * Gets the settlement's total scientific achievement credit.
     * @return achievement credit.
     */
    public double getTotalScientificAchievement() {
        double result = 0D;
        
        Iterator<Double> i = scientificAchievement.values().iterator();
        while (i.hasNext()) result += i.next();
        
        return result;
    }
    
    /**
     * Add achievement credit to the settlement in a scientific field.
     * @param achievementCredit the achievement credit.
     * @param science the scientific field.
     */
    public void addScientificAchievement(double achievementCredit, Science science) {
        if (scientificAchievement.containsKey(science)) 
            achievementCredit += scientificAchievement.get(science);
        
        scientificAchievement.put(science, achievementCredit);
    }
}