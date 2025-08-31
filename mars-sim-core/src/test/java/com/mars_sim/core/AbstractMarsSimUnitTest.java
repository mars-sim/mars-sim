package com.mars_sim.core;

import java.util.Iterator;

import org.junit.Before;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.BuildingCategory;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.building.MockBuilding;
import com.mars_sim.core.building.function.Function;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.building.function.VehicleGarage;
import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.malfunction.MalfunctionManager;
import com.mars_sim.core.map.location.BoundedObject;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.science.task.MarsSimContext;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;

import junit.framework.TestCase;

public abstract class AbstractMarsSimUnitTest extends TestCase
			implements MarsSimContext {

    // This test assumes certain characteristics of the Alpha Base 1 template
    public static final String ALPHA_BASE_1 = "Alpha Base 1";

	protected static final String EXPLORER_ROVER = "explorer rover";
	protected static final String TRANSPORT_ROVER = "transport rover";
	protected static final String CARGO_ROVER = "cargo rover";

	protected static final double BUILDING_LENGTH = 9D;
	protected static final double BUILDING_WIDTH = 9D;

	protected static final double MSOLS_PER_EXECUTE = 0.1D;
	   
	protected UnitManager unitManager;
	protected MarsSurface surface;
	protected Simulation sim;
	protected SimulationConfig simConfig;
	private int pulseID = 1;

	
	public AbstractMarsSimUnitTest() {
		super();
	}

	public AbstractMarsSimUnitTest(String name) {
		super(name);
	}

	@Override
	@Before
	public void setUp() {
	    // Create new simulation instance.
	    simConfig = SimulationConfig.loadConfig();
	    
	    
	    sim = Simulation.instance();
	    sim.testRun();

		// No random failures or accidents during normal unit tests
		MalfunctionManager.setNoFailures(true); 
	    
	    // Clear out existing settlements in simulation.
	    unitManager = sim.getUnitManager();
	    Iterator<Settlement> i = unitManager.getSettlements().iterator();
	    while (i.hasNext()) {
	        unitManager.removeUnit(i.next());
	    }
		
	    Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock(),
	    							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
	    							 sim.getWeather(), unitManager);
	    
	    surface = unitManager.getMarsSurface();			   
	}

	public Simulation getSim() {
		return sim;
	}

	protected SimulationConfig getConfig() {
		return simConfig;
	}
	
	protected Rover buildRover(Settlement settlement, String name, LocalPosition parked) {
		return buildRover(settlement, name, parked, EXPLORER_ROVER);
	}

	protected Rover buildRover(Settlement settlement, String name, LocalPosition parked, String spec) {
	    Rover rover1 = new Rover(name, simConfig.getVehicleConfiguration().getVehicleSpec(spec),
								settlement);
		if (parked != null) {			
			// Note: since settlement.addOwnedVehicle(this) was called in Vehicle's constructor
	    	rover1.setParkedLocation(parked, 0D);
		}
	    unitManager.addUnit(rover1);
	    
	    return rover1;
	}
	
	protected LightUtilityVehicle buildLUV(Settlement settlement, String name, LocalPosition parked) {
	    var rover1 = new LightUtilityVehicle(name, simConfig.getVehicleConfiguration().getVehicleSpec("Light Utility Vehicle"),
								settlement);
		if (parked != null) {			
			// Note: since settlement.addOwnedVehicle(this) was called in Vehicle's constructor
	    	rover1.setParkedLocation(parked, 0D);
		}
	    unitManager.addUnit(rover1);
	    
	    return rover1;
	}

	protected VehicleGarage buildGarage(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		var building0 = buildFunction(buildingManager, "Garage", BuildingCategory.VEHICLE,
									FunctionType.VEHICLE_MAINTENANCE,  pos, facing, true);
	    
	    return building0.getVehicleParking();
	}

	protected MockBuilding buildBuilding(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		return buildBuilding(buildingManager, "Mock", BuildingCategory.COMMAND, pos, facing, true);
	}

	protected MockBuilding buildBuilding(BuildingManager buildingManager, String type, BuildingCategory cat,
								LocalPosition pos, double facing, boolean lifeSupport) {

		int id = buildingManager.getNumBuildings();
		String name = "B" + (id + 1);
		var building0 = new MockBuilding(buildingManager.getSettlement(), name, Integer.toString(id),
										new BoundedObject(pos, BUILDING_WIDTH, BUILDING_LENGTH, facing),
										type, cat, lifeSupport);
	    buildingManager.addMockBuilding(building0);	
	    
	    unitManager.addUnit(building0);
	
	    return building0;
	}

	@Override
	public Building buildFunction(BuildingManager buildingManager, String type, BuildingCategory cat,
							FunctionType fType, LocalPosition pos, double facing, boolean lifesupport) {
		MockBuilding building0 = buildBuilding(buildingManager, type, cat,  pos, facing, lifesupport);

		var spec = simConfig.getBuildingConfiguration().getFunctionSpec(type, fType);

	    building0.addFunction(spec);
		buildingManager.refreshFunctionMapForBuilding(building0);

	    return building0;
	}

	public Building buildResearch(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LABORATORY,
							FunctionType.RESEARCH,  pos, facing, true);
	}

	protected Building buildRecreation(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		return buildFunction(buildingManager, "Lander Hab", BuildingCategory.LIVING,
								FunctionType.RECREATION,  pos, facing, true);
	}

	@Override
	public Building buildEVA(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		var building0 = buildFunction(buildingManager, "EVA Airlock", BuildingCategory.EVA,
						FunctionType.EVA,  pos, facing, true);
		
		var spec = simConfig.getBuildingConfiguration().getFunctionSpec("Lander Hab", FunctionType.LIVING_ACCOMMODATION);
	    building0.addFunction(spec);
	    return building0;
	}

	protected Building buildAccommodation(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		return buildFunction(buildingManager, "Residential Quarters", BuildingCategory.LIVING,
					FunctionType.LIVING_ACCOMMODATION,  pos, facing, true);
	}
	
	protected Settlement buildSettlement() {
		return buildSettlement(MockSettlement.DEFAULT_NAME);
	}

	protected Settlement buildSettlement(String name) {
		return buildSettlement(name, false);
	}

	protected Settlement buildSettlement(String name, boolean needGoods) {
		return buildSettlement(name, needGoods, MockSettlement.DEFAULT_COORDINATES);
	}
	
	protected Settlement buildSettlement(String name, boolean needGoods, Coordinates locn) {
		var auth = getConfig().getReportingAuthorityFactory().getItem("NASA");
		Settlement settlement = new MockSettlement(name, needGoods, locn, auth);
		unitManager.addUnit(settlement);

		return settlement;
	}

	
    protected Robot buildRobot(String name, Settlement s, RobotType type, Building place, FunctionType activity) {
        var spec = simConfig.getRobotConfiguration().getRobotSpec(type, "Standard");
        var robot = new Robot(name, s, spec);
		s.addOwnedRobot(robot);  // This should not be needed as the constructor should add the Robot

        unitManager.addUnit(robot);

        if (place != null) {
            BuildingManager.addToActivitySpot(robot, place, activity);
        }
        return robot;
    }

	public Person buildPerson(String name, Settlement settlement) {
		return buildPerson(name, settlement, JobType.ENGINEER, null, null);
	}

	public Person buildPerson(String name, Settlement settlement, JobType job) {
		return buildPerson(name, settlement, job, null, null);
	}

	public Person buildPatient(String name, Settlement settlement, JobType job,
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
			boolean success = BuildingManager.addPatientToMedicalBed(person, settlement);
			assertTrue("Successful in adding " + person + " to a " + activity.getName() + " activity spot", success);
		}
		
		return person;
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
			assertTrue("Successful in adding " + person + " to a " + activity.getName() + " activity spot", success);
		}
		
		return person;
	}

	

	/**
	 * Executes a Task for a duration or until it completes.
	 * 
	 * @param person
	 * @param task
	 * @param duration
	 * @return The number of calls taken
	 */
	protected double executeTaskForDuration(Person person, Task task, double duration) {
		int maxCalls = (int)(duration/MSOLS_PER_EXECUTE) + 1;
		return executeTask(person, task, maxCalls) * MSOLS_PER_EXECUTE;
	}

	/**
	 * Executes a Task for a number of steps or until it completes.
	 * Note: for maven testing.
	 * 
	 * @param person
	 * @param task
	 * @param maxSteps
	 * @return The number of calls taken
	 */
	protected int executeTask(Person person, Task task, int maxCalls) {
		PersonTaskManager tm = person.getMind().getTaskManager();
		tm.replaceTask(task);
		
		int callsLeft = maxCalls;
		while ((callsLeft > 0) && !task.isDone()) {
			tm.executeTask(MSOLS_PER_EXECUTE);
			callsLeft--;
		}
		
		return maxCalls - callsLeft;
	}

	/**
	 * Executes a Task for a number of steps or phase changes.
	 * Note: for maven testing.
	 * 
	 * @param person
	 * @param task
	 * @param maxSteps
	 * @return The number of calls taken
	 */
	protected int executeTaskUntilPhase(Person person, Task task, int maxCalls) {
		PersonTaskManager tm = person.getMind().getTaskManager();
		tm.replaceTask(task);
		
		var phase = task.getPhase();
		int callsLeft = maxCalls;
		while ((callsLeft > 0) && !task.isDone() && phase.equals(task.getPhase())) {
			tm.executeTask(MSOLS_PER_EXECUTE);
			callsLeft--;
		}
		
		return maxCalls - callsLeft;
	}

	/**
	 * Executes a Task for a number of steps or subtask is Done.
	 * 
	 * @param person
	 * @param task
	 * @param maxSteps
	 * @return The number of calls taken
	 */
	protected int executeTaskUntilSubTask(Person person, Task task, int maxCalls) {
		PersonTaskManager tm = person.getMind().getTaskManager();
		tm.replaceTask(task);
		
		int callsLeft = maxCalls;
		while ((callsLeft > 0) && !task.isDone() && (task.getSubTask() != null) && !task.getSubTask().isDone()) {
			tm.executeTask(MSOLS_PER_EXECUTE);
			callsLeft--;
		}
		
		return maxCalls - callsLeft;
	}

	/**
     * Creates a Clock pulse that just contains a MarsClock at a specific time.
     * 
     * @param missionSol Sol in the current mission
     * @param mSol MSol throughout the day
     * @param newSol Is the new Sol flag set
     * @return
     */
    protected ClockPulse createPulse(int missionSol, int mSol, boolean newSol, boolean newHalfSol) {
        MarsTime marsTime = new MarsTime(1, 1, missionSol, mSol, missionSol);
		return createPulse(marsTime, newSol, newHalfSol);
	}

	/**
     * Creates a Clock pulse that just contains a MarsClock at a specific time.
     * 
     * @param marsTime
     * @param newSol Is it a new sol ?
     * @param newHalfSol Has half a sol just passed ? 
     * @return
     */
	public ClockPulse createPulse(MarsTime marsTime, boolean newSol, boolean newHalfSol) {
		return createPulse(marsTime, 1D, newSol, newHalfSol);
	}

	/**
     * Creates a Clock pulse that advanced the current clock a duration
     * 
     * @param elapsed
     * @param newSol Is it a new sol ?
     * @param newHalfSol Has half a sol just passed ? 
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
     * @param newSol Is it a new sol ?
     * @param newHalfSol Has half a sol just passed ? 
     * @return
     */
	private ClockPulse createPulse(MarsTime marsTime, double elapsed, boolean newSol, boolean newHalfSol) {
		var master = sim.getMasterClock();
		master.setMarsTime(marsTime);
        return new ClockPulse(pulseID++, elapsed, marsTime, master, newSol, newHalfSol, true, false);
    }

	/**
	 * Better Assert method.
	 */
	public static void assertGreaterThan(String message, double minValue, double actual) {
		if (actual <= minValue) {
			fail(message + " ==> " +
					"Expected: a value greater than <" + minValue + "> " +
					"Actual was <" + actual + ">");
		}
	}

	public static void assertLessThan(String message, double maxValue, double actual) {
		if (actual >= maxValue) {
			fail(message + " ==> " +
					"Expected: a value less than <" + maxValue + "> " +
					"Actual was <" + actual + ">");
		}
	}
	
	public static void assertEqualLessThan(String message, double maxValue, double actual) {
		if (actual > maxValue) {
			fail(message + " ==> " +
					"Expected: a value less than <" + maxValue + ">\n" +
					"Actual was <" + actual + ">");
		}
	}
}