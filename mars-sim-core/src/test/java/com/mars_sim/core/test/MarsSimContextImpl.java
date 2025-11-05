package com.mars_sim.core.test;

import com.mars_sim.core.MarsSimContext;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * Implementation of MarsSimContext that provides utility methods for creating
 * simulation entities in unit tests.
 */
public class MarsSimContextImpl implements MarsSimContext {

    protected static final double BUILDING_LENGTH = 9D;
    protected static final double BUILDING_WIDTH = 9D;

    private UnitManager unitManager;
    private Simulation sim;
    private SimulationConfig simConfig;
    private int pulseID = 1;
    private MarsSurface surface;

    /**
     * Constructor that initializes the context with simulation components.
     */
    public MarsSimContextImpl() {
        // Create new simulation instance.
	    simConfig = SimulationConfig.loadConfig();
	    
	    sim = Simulation.instance();
	    sim.testRun();

        unitManager = sim.getUnitManager();
        surface = unitManager.getMarsSurface();

		// No random failures or accidents during normal unit tests
		MalfunctionManager.setNoFailures(true); 
	    
	    Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock(),
	    							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
	    							 sim.getWeather(), unitManager);
    }

    @Override
    public Person buildPerson(String name, Settlement settlement) {
        return buildPerson(name, settlement, JobType.ENGINEER, null, null);
    }

    public Person buildPerson(String name, Settlement settlement, JobType job,
                    Building place, FunctionType activity) {
        
        GenderType gender = GenderType.MALE;
        int rand = RandomUtil.getRandomInt(1);
        if (rand == 1)
            gender = GenderType.FEMALE;

        Person person = Person.create(name, settlement, gender)
                .build();
        
        person.setJob(job, "Test");
        
        person.getNaturalAttributeManager().adjustAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE, 100);
        
        unitManager.addUnit(person);

        if (place != null) {
            boolean success = BuildingManager.addToActivitySpot(person, place, activity);
            if (!success) {
                throw new RuntimeException("Failed to add " + person + " to a " + activity.getName() + " activity spot");
            }
        }
        
        return person;
    }

    @Override
    public Building buildResearch(BuildingManager buildingManager, LocalPosition pos, double facing) {
        return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LABORATORY,
                            FunctionType.RESEARCH, pos, facing, true);
    }

    @Override
    public Building buildEVA(BuildingManager buildingManager, LocalPosition pos, double facing) {
        var building0 = buildFunction(buildingManager, "EVA Airlock", BuildingCategory.EVA,
                        FunctionType.EVA, pos, facing, true);
        
        var spec = simConfig.getBuildingConfiguration().getFunctionSpec("Lander Hab", FunctionType.LIVING_ACCOMMODATION);
        building0.addFunction(spec);
        return building0;
    }

    @Override
    public Simulation getSim() {
        return sim;
    }
    
    @Override
    public SimulationConfig getConfig() {
        return simConfig;
    }

    @Override
    public ClockPulse createPulse(MarsTime marsTime, boolean newSol, boolean newHalfSol) {
        return createPulse(marsTime, 1D, newSol, newHalfSol);
    }

    /**
     * Creates a Clock pulse that advances the current clock a duration
     * 
     * @param elapsed
     * @return
     */
    public ClockPulse createPulse(double elapsed) {
        var currentTime = sim.getMasterClock().getMarsTime();
        var newTime = currentTime.addTime(elapsed);
        var newSol = !currentTime.getDate().equals(newTime.getDate());
        return createPulse(newTime, elapsed, newSol, false);
    }

    /**
     * Creates a Clock pulse that just contains a MarsClock at a specific time.
     * 
     * @param marsTime
     * @param elapsed
     * @param newSol Is it a new sol ?
     * @param newHalfSol Has half a sol just passed ? 
     * @return
     */
    private ClockPulse createPulse(MarsTime marsTime, double elapsed, boolean newSol, boolean newHalfSol) {
        var master = sim.getMasterClock();
        master.setMarsTime(marsTime);
        return new ClockPulse(pulseID++, elapsed, marsTime, master, newSol, newHalfSol, true, false);
    }

    @Override
    public Building buildFunction(BuildingManager buildingManager, String type, BuildingCategory cat,
                            FunctionType fType, LocalPosition pos, double facing, boolean lifesupport) {
        MockBuilding building0 = buildBuilding(buildingManager, type, cat, pos, facing, lifesupport);

        building0.getMalfunctionManager().addScopeString(fType.getName());
        
        var spec = simConfig.getBuildingConfiguration().getFunctionSpec(type, fType);

        building0.addFunction(spec);
        buildingManager.refreshFunctionMapForBuilding(building0);

        return building0;
    }

    public MockBuilding buildBuilding(BuildingManager buildingManager, String type, BuildingCategory cat,
                                LocalPosition pos, double facing, boolean lifeSupport) {

        int id = buildingManager.getNumBuildings();
        String name = "B" + (id + 1);
        var building0 = new MockBuilding(buildingManager.getSettlement(), name, Integer.toString(id),
                                        new BoundedObject(pos, BUILDING_WIDTH, BUILDING_LENGTH, facing),
                                        type, cat, lifeSupport);
        buildingManager.addMockBuilding(building0);    
        
        building0.getMalfunctionManager().addScopeString(building0.getBuildingType());
        
        unitManager.addUnit(building0);

        return building0;
    }

    /**
     * Build a rover and add it to the unit manager.
     * @param settlement
     * @param name
     * @param parked
     * @param spec If null a default spec will be used
     * @return
     */
    public Rover buildRover(Settlement settlement, String name, LocalPosition parked, String spec) {
	    Rover rover1 = new Rover(name, simConfig.getVehicleConfiguration().getVehicleSpec(spec),
								settlement);
		if (parked != null) {			
			// Note: since settlement.addOwnedVehicle(this) was called in Vehicle's constructor
	    	rover1.setParkedLocation(parked, 0D);
		}
	    unitManager.addUnit(rover1);
	    
	    return rover1;
	}

    /**
     * Creates a test settlement at a random location.
     * @param name
     * @param needGoods
     * @param locn
     * @return
     */
	public Settlement buildSettlement(String name, boolean needGoods, Coordinates locn) {
		var auth = getConfig().getReportingAuthorityFactory().getItem("NASA");
		Settlement settlement = new MockSettlement(name, needGoods, locn, auth);
		unitManager.addUnit(settlement);

		return settlement;
	}
    
    /**
     * Creates a Clock pulse that contains a MarsClock at a specific time.
     * 
     * @param missionSol Sol in the current mission
     * @param mSol MSol throughout the day
     * @param newSol Is the new Sol flag set
     * @param newHalfSol Has half a sol just passed
     * @return
     */
    public ClockPulse createPulse(int missionSol, int mSol, boolean newSol, boolean newHalfSol) {
        MarsTime marsTime = new MarsTime(1, 1, missionSol, mSol, missionSol);
        return createPulse(marsTime, newSol, newHalfSol);
    }

    public MarsSurface getSurface() {
        return surface;
    }
}