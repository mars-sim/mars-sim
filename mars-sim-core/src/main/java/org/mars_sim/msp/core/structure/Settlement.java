/**
 * Mars Simulation Project
 * Settlement.java
 * @version 3.07 2014-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LifeSupport;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.HeatMode;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.PowerMode;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It contains information related to the state of
 * the settlement.
 */
public class Settlement
extends Structure
implements LifeSupport {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /* default logger.*/
	//private static Logger logger = Logger.getLogger(Settlement.class.getName());
     
    /** Normal air pressure (Pa) */
    private static final double NORMAL_AIR_PRESSURE = 101325D;
    /** Normal temperature (celsius) */
    private static final double NORMAL_TEMP = 22.5D;
    // maximum & minimal acceptable temperature for living space (arbitrary)
    // TODO: where are these two values from people.xml saved into by PersonConfig.java?
    private static final double MIN_TEMP = 0.0D;
    private static final double MAX_TEMP = 48.0D;

    /* Amount of time (millisols) required for periodic maintenance.
    private static final double MAINTENANCE_TIME = 1000D;
     */

    // Data members
    /** The settlement's building manager. */
    protected BuildingManager buildingManager;
    /** The settlement's building connector manager. */
    protected BuildingConnectorManager buildingConnectorManager;
    /** The settlement's goods manager. */
    protected GoodsManager goodsManager;
    /** The settlement's construction manager. */
    protected ConstructionManager constructionManager;
    /** The settlement's building power grid. */
    protected PowerGrid powerGrid;
    //2014-10-17 Added heating system
    /** The settlement's heating system. */
    protected ThermalSystem thermalSystem;  
    /** The settlement template name. */
    private String template;
    /** Override flag for mission creation at settlement. */
    private boolean missionCreationOverride;
    /** Override flag for manufacturing at settlement. */
    private boolean manufactureOverride;
    /** Override flag for resource process at settlement. */
    private boolean resourceProcessOverride;
    /** Override flag for construction/salvage mission creation at settlement. */
    private boolean constructionOverride;
    /** The settlement's achievement in scientific fields. */
    private Map<ScienceType, Double> scientificAchievement;
    /** Amount of time (millisols) that the settlement has had zero population. */
    private double zeroPopulationTime;
    /** The initial population of the settlement. */
    private int initialPopulation;
    // 2014-10-27 Added settlement id
    // TODO: implement settlement id
    private int id;
    private static int count;
    //2014-11-23 Added foodProductionOverride
    private boolean foodProductionOverride;
    
    /**
     * Constructor for subclass extension.
     * @param name the settlement's name
     * @param location the settlement's location
     */
    // constructor 1
    // TODO: pending for deletion (use constructor 2 instead)
    protected Settlement(String name, Coordinates location) {
        // Use Structure constructor.
        super(name, location);
        count++;
        //logger.info("constructor 1 : count is " + count);
    }

    // constructor 2
    // 2014-10-28 Added settlement id
    protected Settlement(String name, int id, Coordinates location) {
        // Use Structure constructor.
        super(name, location);
        count++;
        //logger.info("constructor 2 : count is " + count);
    }

    // constructor 3
    // 2014-10-29 Added settlement id
    // Called by UnitManager.java when users create the initial settlement
    // Called by ArrivingSettlement.java when users create a brand new settlement
    public Settlement(String name, int id, String template, Coordinates location, int populationNumber) {
        // Use Structure constructor
        super(name, location);

        this.template = template;
        
        count++;
        //logger.info("constructor 3 : count is " + count);
        
        // Set inventory total mass capacity.
        getInventory().addGeneralCapacity(Double.MAX_VALUE);

        // Initialize building manager
        buildingManager = new BuildingManager(this);

        // Initialize building connector manager.
        buildingConnectorManager = new BuildingConnectorManager(this);

        // Initialize goods manager.
        goodsManager = new GoodsManager(this);

        // Initialize construction manager.
        constructionManager = new ConstructionManager(this);

        // Initialize power grid
        powerGrid = new PowerGrid(this);
        
        //2014-10-17 Added thermal control system
        thermalSystem = new ThermalSystem(this);       

        // Initialize scientific achievement.
        scientificAchievement = new HashMap<ScienceType, Double>(0);

        // Initialize the initial population.
        initialPopulation = populationNumber;
        

    }


	/**
	 * Gets the settlement template's unique ID.
	 * @return ID number.
	 */
    // 2014-10-29 Added settlement id
	public int getID() {
		return id;
	}
	
	/**
	 * Gets the how many times the settlement class has been called .
	 * @return count.
	 */
    // 2014-10-29 Added count
	public int getCount() {
		return count;
	}
    /**
     * Gets the population capacity of the settlement
     * @return the population capacity
     */
    public int getPopulationCapacity() {
        int result = 0;
        Iterator<Building> i = buildingManager.getBuildings(BuildingFunction.LIVING_ACCOMODATIONS).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            LivingAccommodations livingAccommodations = (LivingAccommodations) building.getFunction(BuildingFunction.LIVING_ACCOMODATIONS);
            result += livingAccommodations.getBeds();
        }

        return result;
    }

    /**
     * Gets the current population number of the settlement
     * @return the number of inhabitants
     */
    public int getCurrentPopulationNum() {
        return getInhabitants().size();
    }

    /**
     * Gets a collection of the inhabitants of the settlement.
     * @return Collection of inhabitants
     */
    public Collection<Person> getInhabitants() {
        return CollectionUtils.getPerson(getInventory().getContainedUnits());
    }

    /**
     * Gets the current available population capacity of the settlement
     * @return the available population capacity
     */
    public int getAvailablePopulationCapacity() {
        return getPopulationCapacity() - getCurrentPopulationNum();
    }

    /**
     * Gets an array of current inhabitants of the settlement
     * @return array of inhabitants
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

    /**
     * Gets a collection of vehicles parked at the settlement.
     * @return Collection of parked vehicles
     */
    public Collection<Vehicle> getParkedVehicles() {
        return CollectionUtils.getVehicle(getInventory().getContainedUnits());
    }

    /**
     * Gets the number of vehicles parked at the settlement.
     * @return parked vehicles number
     */
    public int getParkedVehicleNum() {
        return getParkedVehicles().size();
    }

    /**
     * Returns true if life support is working properly and is not out of oxygen or water.
     * @return true if life support is OK
     * @throws Exception if error checking life support.
     */
    public boolean lifeSupportCheck() {
        boolean result = true;

        AmountResource oxygen = AmountResource.findAmountResource(LifeSupport.OXYGEN);
        if (getInventory().getAmountResourceStored(oxygen, false) <= 0D)
            result = false;
        AmountResource water = AmountResource.findAmountResource(LifeSupport.WATER);
        if (getInventory().getAmountResourceStored(water, false) <= 0D)
            result = false;
        // TODO: check against indoor air pressure
        if (getAirPressure() != NORMAL_AIR_PRESSURE)
            result = false;
        // TODO: check if this is working
        // 2014-11-28 Added MAX_TEMP
        if (getTemperature() < MIN_TEMP || getTemperature() > MAX_TEMP)
            result = false;

        return result;
    }

    /**
     * Gets the number of people the life support can provide for.
     * @return the capacity of the life support system
     */
    public int getLifeSupportCapacity() {
        return getPopulationCapacity();
    }

    /**
     * Gets oxygen from system.
     * @param amountRequested the amount of oxygen requested from system (kg)
     * @return the amount of oxygen actually received from system (kg)
     * @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) {
        AmountResource oxygen = AmountResource.findAmountResource(LifeSupport.OXYGEN);
        double oxygenTaken = amountRequested;
        double oxygenLeft = getInventory().getAmountResourceStored(oxygen, false);
        if (oxygenTaken > oxygenLeft)
            oxygenTaken = oxygenLeft;
        AmountResource carbonDioxide = AmountResource
                .findAmountResource("carbon dioxide");
        double carbonDioxideProvided = oxygenTaken;
        double carbonDioxideCapacity = getInventory()
                .getAmountResourceRemainingCapacity(carbonDioxide, true, false);
        if (carbonDioxideProvided > carbonDioxideCapacity)
            carbonDioxideProvided = carbonDioxideCapacity;

        getInventory().retrieveAmountResource(oxygen, oxygenTaken);
        getInventory().storeAmountResource(carbonDioxide,
                carbonDioxideProvided, true);

        return oxygenTaken;
    }

    /**
     * Gets water from system.
     * @param amountRequested the amount of water requested from system (kg)
     * @return the amount of water actually received from system (kg)
     * @throws Exception if error providing water.
     */
    public double provideWater(double amountRequested) {
        AmountResource water = AmountResource.findAmountResource(LifeSupport.WATER);
        double waterTaken = amountRequested;
        double waterLeft = getInventory().getAmountResourceStored(water, false);
        if (waterTaken > waterLeft)
            waterTaken = waterLeft;
        getInventory().retrieveAmountResource(water, waterTaken);

        return waterTaken;
    }

    /**
     * Gets the air pressure of the life support system.
     * @return air pressure (Pa)
     */
    public double getAirPressure() {
        double result = NORMAL_AIR_PRESSURE;
        double ambient = Simulation.instance().getMars().getWeather()
                .getAirPressure(getCoordinates());
        if (result < ambient)
            return ambient;
        else
            return result;
    }

    /**
     * Gets the temperature of the life support system.
     * @return temperature (degrees C)
     */
    //TODO: what is the use of this method
    public double getTemperature() {
        double result = NORMAL_TEMP;
        //double result = getLifeSupport().getTemperature();
        
        /*
        double ambient = Simulation.instance().getMars().getWeather()
                .getTemperature(getCoordinates());
        if (result < ambient)
            return ambient;
        else
            return result;
        */
        return result;
    }
     
    /**
     * Perform time-related processes
     * @param time the amount of time passing (in millisols)
     * @throws Exception if error during time passing.
     */
    public void timePassing(double time) {

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

        // If no current population at settlement for one sol, power down the building and turn the heat off.
        if (getCurrentPopulationNum() == 0) {
            zeroPopulationTime += time;
            if (zeroPopulationTime > 1000D) {
                powerGrid.setPowerMode(PowerMode.POWER_DOWN);
                thermalSystem.setHeatMode(HeatMode.POWER_DOWN);
            }
        } else {
            zeroPopulationTime = 0D;
            powerGrid.setPowerMode(PowerMode.POWER_UP);
            // TODO: check if POWER_UP is necessary
            // Question: is POWER_UP a prerequisite of FULL_POWER ?
            //thermalSystem.setHeatMode(HeatMode.POWER_UP); 
        }

        powerGrid.timePassing(time);
        
        thermalSystem.timePassing(time);

        buildingManager.timePassing(time);

        updateGoodsManager(time);
    }

    private void updateGoodsManager(double time) {

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
        Collection<Person> people = new ConcurrentLinkedQueue<Person>(
                getInhabitants());

        // Check all people.
        Iterator<Person> i = Simulation.instance().getUnitManager().getPeople()
                .iterator();
        while (i.hasNext()) {
            Person person = i.next();
            Task task = person.getMind().getTaskManager().getTask();

            // Add all people maintaining this settlement.
            if (task instanceof Maintenance) {
                if (((Maintenance) task).getEntity() == this) {
                    if (!people.contains(person))
                        people.add(person);
                }
            }

            // Add all people repairing this settlement.
            if (task instanceof Repair) {
                if (((Repair) task).getEntity() == this) {
                    if (!people.contains(person))
                        people.add(person);
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
     * Gets the settlement's building connector manager.
     * @return building connector manager.
     */
    public BuildingConnectorManager getBuildingConnectorManager() {
        return buildingConnectorManager;
    }

    /**
     * Gets the settlement's goods manager.
     * @return goods manager
     */
    public GoodsManager getGoodsManager() {
        return goodsManager;
    }

    /**
     * Gets the closest available airlock to a person.
     * @param person the person.
     * @return airlock or null if none available.
     */
    public Airlock getClosestAvailableAirlock(Person person) {
        Airlock result = null;

        double leastDistance = Double.MAX_VALUE;
        BuildingManager manager = buildingManager;
        Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
        while (i.hasNext()) {
            Building building = i.next();

            double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), 
                    person.getXLocation(), person.getYLocation());
            if (distance < leastDistance) {
                EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
                result = eva.getAirlock();
                leastDistance = distance;
            }
        }

        return result;
    }

    /**
     * Gets the closest available airlock at the settlement to the given location.
     * The airlock must have a valid walkable interior path from the person's current location.
     * @param person the person.
     * @param xLocation the X location.
     * @param yLocation the Y location.
     * @return airlock or null if none available.
     */
    public Airlock getClosestWalkableAvailableAirlock(Person person, double xLocation, double yLocation) {
        Airlock result = null;

        Building currentBuilding = BuildingManager.getBuilding(person);
        if (currentBuilding == null) {
            throw new IllegalStateException(person.getName() + " is not currently in a building.");
        }

        double leastDistance = Double.MAX_VALUE;
        BuildingManager manager = buildingManager;
        Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
        while (i.hasNext()) {
            Building building = i.next();

            if (buildingConnectorManager.hasValidPath(currentBuilding, building)) {

                double distance = Point2D.distance(building.getXLocation(), building.getYLocation(), 
                        xLocation, yLocation);
                if (distance < leastDistance) {
                    EVA eva = (EVA) building.getFunction(BuildingFunction.EVA);
                    result = eva.getAirlock();
                    leastDistance = distance;
                }
            }
        }

        return result;
    }

    /**
     * Gets the closest available airlock at the settlement to the given location.
     * The airlock must have a valid walkable interior path from the given building's current location.
     * @param building the building in the walkable interior path.
     * @param xLocation the X location.
     * @param yLocation the Y location.
     * @return airlock or null if none available.
     */
    public Airlock getClosestWalkableAvailableAirlock(Building building, double xLocation, double yLocation) {
        Airlock result = null;

        double leastDistance = Double.MAX_VALUE;
        BuildingManager manager = buildingManager;
        Iterator<Building> i = manager.getBuildings(BuildingFunction.EVA).iterator();
        while (i.hasNext()) {
            Building nextBuilding = i.next();

            if (buildingConnectorManager.hasValidPath(building, nextBuilding)) {

                double distance = Point2D.distance(nextBuilding.getXLocation(), nextBuilding.getYLocation(), 
                        xLocation, yLocation);
                if (distance < leastDistance) {
                    EVA eva = (EVA) nextBuilding.getFunction(BuildingFunction.EVA);
                    result = eva.getAirlock();
                    leastDistance = distance;
                }
            }
        }

        return result;
    }

    /**
     * Checks if a building has a walkable path from it to an airlock.
     * @param building the building.
     * @return true if an airlock is walkable from the building.
     */
    public boolean hasWalkableAvailableAirlock(Building building) {
        return (getClosestWalkableAvailableAirlock(building, 0D, 0D) != null);
    }

    /**
     * Gets the number of airlocks at the settlement.
     * @return number of airlocks.
     */
    public int getAirlockNum() {
        return buildingManager.getBuildings(BuildingFunction.EVA).size();
    }

    /**
     * Gets the settlement's power grid.
     * @return the power grid.
     */
    public PowerGrid getPowerGrid() {
        return powerGrid;
    }

    /**
     * Gets the settlement's heating system.
     * @return thermalSystem.
     */
    public ThermalSystem getThermalSystem() {
        return thermalSystem;
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

        Iterator<Person> i = Simulation.instance().getUnitManager().getPeople()
                .iterator();
        while (i.hasNext()) {
            Person person = i.next();
            if (person.getAssociatedSettlement() == this)
                result.add(person);
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
        Iterator<Mission> i = Simulation.instance().getMissionManager()
                .getMissionsForSettlement(this).iterator();
        while (i.hasNext()) {
            Mission mission = i.next();
            if (mission instanceof VehicleMission) {
                Vehicle vehicle = ((VehicleMission) mission).getVehicle();
                if ((vehicle != null) && !this.equals(vehicle.getSettlement()))
                    result.add(vehicle);
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
     * Sets the construction override flag.
     * @param constructionOverride override for settlement construction/salvage 
     * mission creation.
     */
    public void setConstructionOverride(boolean constructionOverride) {
        this.constructionOverride = constructionOverride;
    }

    /**
     * Gets the construction override flag.
     * @return override for settlement construction mission creation.
     */
    public boolean getConstructionOverride() {
        return constructionOverride;
    }

    /**
     * Sets the FoodProduction override flag.
     * @param FoodProduction override for FoodProduction.
     */
    public void setFoodProductionOverride(boolean foodProductionOverride) {
        this.foodProductionOverride = foodProductionOverride;
    }

    /**
     * Gets the FoodProduction override flag.
     * @return override for settlement FoodProduction.
     */
    public boolean getFoodProductionOverride() {
        return foodProductionOverride;
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
    public double getScientificAchievement(ScienceType science) {
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
        while (i.hasNext())
            result += i.next();

        return result;
    }

    /**
     * Add achievement credit to the settlement in a scientific field.
     * @param achievementCredit the achievement credit.
     * @param science the scientific field.
     */
    public void addScientificAchievement(double achievementCredit, ScienceType science) {
        if (scientificAchievement.containsKey(science))
            achievementCredit += scientificAchievement.get(science);

        scientificAchievement.put(science, achievementCredit);
    }

    /**
     * Gets the initial population of the settlement.
     * @return initial population number.
     */
    public int getInitialPopulation() {
        return initialPopulation;
    }

    @Override
    public void destroy() {
        super.destroy();

        if (buildingManager != null) {
            buildingManager.destroy();
        }
        buildingManager = null;
        if (buildingConnectorManager != null) {
            buildingConnectorManager.destroy();
        }
        buildingConnectorManager = null;
        if (goodsManager != null) {
            goodsManager.destroy();
        }
        goodsManager = null;
        if (constructionManager != null) {
            constructionManager.destroy();
        }
        constructionManager = null;
        if (powerGrid != null) {
            powerGrid.destroy();
        }
        powerGrid = null;
        
        if (thermalSystem != null) {
        	thermalSystem.destroy();
        }
        thermalSystem = null;
        
        template = null;
        if (scientificAchievement != null) {
            scientificAchievement.clear();
        }
        scientificAchievement = null;
    }

}