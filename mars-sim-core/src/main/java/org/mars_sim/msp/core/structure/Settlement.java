/**
 * Mars Simulation Project
 * Settlement.java
 * @version 3.06 2013-11-08
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Repair;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.science.Science;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.connection.BuildingConnectorManager;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.goods.GoodsManager;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * The Settlement class represents a settlement unit on virtual Mars. It contains information related to the state of
 * the settlement.
 */
public class Settlement extends Structure implements org.mars_sim.msp.core.LifeSupport {

    private static Logger logger = Logger.getLogger(Settlement.class.getName());

    // Unit update events.
    public static final String ADD_ASSOCIATED_PERSON_EVENT = "add associated person";
    public static final String REMOVE_ASSOCIATED_PERSON_EVENT = "remove associated person";

    private static final double NORMAL_AIR_PRESSURE = 1D; // Normal air pressure (atm.)
    private static final double NORMAL_TEMP = 25D; // Normal temperature (celsius)

    private static final double MAINTENANCE_TIME = 1000D; // Amount of time (millisols) required for periodic
                                                          // maintenance.

    // Data members
    protected BuildingManager buildingManager; // The settlement's building manager.
    protected BuildingConnectorManager buildingConnectorManager; // The settlement's building connector manager.
    protected GoodsManager goodsManager; // The settlement's goods manager.
    protected ConstructionManager constructionManager; // The settlement's construction manager.
    protected PowerGrid powerGrid; // The settlement's building power grid.
    private String template; // The settlement template name.
    private boolean missionCreationOverride; // Override flag for mission creation at settlement.
    private boolean manufactureOverride; // Override flag for manufacturing at settlement.
    private boolean resourceProcessOverride; // Override flag for resource process at settlement.
    private boolean constructionOverride; // Override flag for construction/salvage mission creation at settlement.
    private Map<Science, Double> scientificAchievement; // The settlement's achievement in scientific fields.
    private double zeroPopulationTime;  // Amount of time (millisols) that the settlement has had zero population.
    private int initialPopulation; // The initial population of the settlement.

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
     * @param populationNumber the settlement's initial population.
     */
    public Settlement(String name, String template, Coordinates location, int populationNumber) {
        // Use Structure constructor
        super(name, location);

        this.template = template;

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

        // Initialize scientific achievement.
        scientificAchievement = new HashMap<Science, Double>(0);
        
        // Initialize the initial population.
        initialPopulation = populationNumber;
    }

    /**
     * Gets the population capacity of the settlement
     * @return the population capacity
     */
    public int getPopulationCapacity() {
        int result = 0;
        Iterator<Building> i = buildingManager.getBuildings(
                LivingAccommodations.NAME).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            LivingAccommodations livingAccommodations = (LivingAccommodations) building
                    .getFunction(LivingAccommodations.NAME);
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

        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
        if (getInventory().getAmountResourceStored(oxygen, false) <= 0D)
            result = false;
        AmountResource water = AmountResource.findAmountResource("water");
        if (getInventory().getAmountResourceStored(water, false) <= 0D)
            result = false;
        if (getAirPressure() != NORMAL_AIR_PRESSURE)
            result = false;
        if (getTemperature() != NORMAL_TEMP)
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
     * @return the amount of oxgyen actually received from system (kg)
     * @throws Exception if error providing oxygen.
     */
    public double provideOxygen(double amountRequested) {
        AmountResource oxygen = AmountResource.findAmountResource("oxygen");
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
        AmountResource water = AmountResource.findAmountResource("water");
        double waterTaken = amountRequested;
        double waterLeft = getInventory().getAmountResourceStored(water, false);
        if (waterTaken > waterLeft)
            waterTaken = waterLeft;
        getInventory().retrieveAmountResource(water, waterTaken);

        return waterTaken;
    }

    /**
     * Gets the air pressure of the life support system.
     * @return air pressure (atm)
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
    public double getTemperature() {
        double result = NORMAL_TEMP;
        double ambient = Simulation.instance().getMars().getWeather()
                .getTemperature(getCoordinates());
        if (result < ambient)
            return ambient;
        else
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

        // If no current population at settlement for one sol, power down buildings.
        if (getCurrentPopulationNum() == 0) {
            zeroPopulationTime += time;
            if (zeroPopulationTime > 1000D) {
                powerGrid.setPowerMode(PowerGrid.POWER_DOWN_MODE);
            }
        } else {
            zeroPopulationTime = 0D;
            powerGrid.setPowerMode(PowerGrid.POWER_UP_MODE);
        }

        powerGrid.timePassing(time);

        buildingManager.timePassing(time);

        updateGoodsManager(time);
    }

    private void updateGoodsManager(double time) {

        // Randomly update goods manager 1 time per Sol.
        if (!goodsManager.isInitialized()
                || (time >= RandomUtil.getRandomDouble(1000D))) {
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
     * Gets an available airlock for the settlement.
     * @return airlock or null if none available.
     */
    public Airlock getAvailableAirlock() {
        Airlock result = null;

        BuildingManager manager = buildingManager;
        List<Building> evaBuildings = manager.getBuildings(EVA.NAME);
        // Note - I don't think least crowded is useful for picking EVA building. - Scott.
        //evaBuildings = BuildingManager.getLeastCrowdedBuildings(evaBuildings);

        if (evaBuildings.size() > 0) {
            // Pick random EVA building from list.
            int rand = RandomUtil.getRandomInt(evaBuildings.size() - 1);
            Building building = evaBuildings.get(rand);
            EVA eva = (EVA) building.getFunction(EVA.NAME);
            result = eva.getAirlock();
        }

        return result;
    }

    /**
     * Gets the number of airlocks at the settlement.
     * @return number of airlocks.
     */
    public int getAirlockNum() {
        return buildingManager.getBuildings(EVA.NAME).size();
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
        while (i.hasNext())
            result += i.next();

        return result;
    }

    /**
     * Add achievement credit to the settlement in a scientific field.
     * @param achievementCredit the achievement credit.
     * @param science the scientific field.
     */
    public void addScientificAchievement(double achievementCredit,
            Science science) {
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
        
        buildingManager.destroy();
        buildingManager = null;
        buildingConnectorManager.destroy();
        buildingConnectorManager = null;
        goodsManager.destroy();
        goodsManager = null;
        constructionManager.destroy();
        constructionManager = null;
        powerGrid.destroy();
        powerGrid = null;
        template = null;
        scientificAchievement.clear();
        scientificAchievement = null;
    }
}