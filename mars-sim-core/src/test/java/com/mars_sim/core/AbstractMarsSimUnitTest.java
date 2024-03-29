package com.mars_sim.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;

import com.mars_sim.core.environment.MarsSurface;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.mission.TravelToSettlement;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.PersonTaskManager;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.MockSettlement;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.structure.building.BuildingManager;
import com.mars_sim.core.structure.building.MockBuilding;
import com.mars_sim.core.structure.building.function.EVA;
import com.mars_sim.core.structure.building.function.Function;
import com.mars_sim.core.structure.building.function.FunctionType;
import com.mars_sim.core.structure.building.function.LivingAccommodation;
import com.mars_sim.core.structure.building.function.Recreation;
import com.mars_sim.core.structure.building.function.VehicleGarage;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.mapdata.location.LocalPosition;

import junit.framework.TestCase;

public abstract class AbstractMarsSimUnitTest extends TestCase {

	protected static final double BUILDING_LENGTH = 9D;
	protected static final double BUILDING_WIDTH = 9D;

	private static final double MSOLS_PER_EXECUTE = 0.1D;
	
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

	@Before
	public void setUp() {
	    // Create new simulation instance.
	    simConfig = SimulationConfig.instance();
	    simConfig.loadConfig();
	    
	    sim = Simulation.instance();
	    sim.testRun();
	    
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

	
	protected Rover buildRover(Settlement settlement, String name, LocalPosition parked) {
	    Rover rover1 = new Rover(name, simConfig.getVehicleConfiguration().getVehicleSpec("explorer rover"),
								settlement);
		if (parked != null) {
	    	rover1.setParkedLocation(parked, 0D);
		}
	    unitManager.addUnit(rover1);
	    return rover1;
	}

	protected VehicleGarage buildGarage(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
	
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);
	
		// TODO this should be built off a FunctionSpec
	    LocalPosition parkingLocation = LocalPosition.DEFAULT_POSITION;
	    VehicleGarage garage = new VehicleGarage(building0,
	            new LocalPosition[] { parkingLocation });
	    building0.addFunction(garage);

		buildingManager.addNewBuildingtoBFMap(building0);
	    
	    return garage;
	}

	protected MockBuilding buildBuilding(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		String name = "B" + id;
		
	    MockBuilding building0 = new MockBuilding(buildingManager, name);
	    building0.setTemplateID("" + id);
	    building0.setName(name);
	    building0.setWidth(BUILDING_WIDTH);
	    building0.setLength(BUILDING_LENGTH);
	    building0.setLocation(pos);
	    building0.setFacing(facing);
	    buildingManager.addMockBuilding(building0);	
	    
	    unitManager.addUnit(building0);
	
	    return building0;
	}

	protected Building buildRecreation(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);

		var spec = simConfig.getBuildingConfiguration().getFunctionSpec("Lander Hab", FunctionType.LIVING_ACCOMMODATION);

	    building0.addFunction(new Recreation(building0, spec));
	    return building0;
	}

	protected Building buildEVA(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);

		var evaSpec = simConfig.getBuildingConfiguration().getFunctionSpec("EVA Airlock", FunctionType.EVA);
	    building0.addFunction(new EVA(building0, evaSpec));
	    return building0;
	}

	protected Building buildAccommodation(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);
		// Need to rework to allow maven to test this
		var quartersSpec = simConfig.getBuildingConfiguration().getFunctionSpec("Residential Quarters", FunctionType.LIVING_ACCOMMODATION);

	    building0.addFunction(new LivingAccommodation(building0, quartersSpec));
	    return building0;
	}
	
	protected Settlement buildSettlement() {
		return buildSettlement(MockSettlement.DEFAULT_NAME);
	}

	protected Settlement buildSettlement(String name) {
		Settlement settlement = new MockSettlement(name);
		unitManager.addUnit(settlement);

		return settlement;
	}

	protected Person buildPerson(String name, Settlement settlement) {
		Person person = Person.create(name, settlement, GenderType.MALE)
				.build();
		person.setJob(JobType.ENGINEER, "Test");
		
		unitManager.addUnit(person);
		return person;
	}

	/**
	 * Builds a pseudo vehicle mission that can be used in tests.
	 */
	protected VehicleMission buildMission(Settlement starting, Person leader) {
		// Need a vehicle
		Rover rover = buildRover(starting, "loader", null);

		Building garage = buildBuilding(starting.getBuildingManager(), new LocalPosition(0,0), 0D, 1);
		BuildingManager.addPersonToActivitySpot(leader, garage, FunctionType.VEHICLE_MAINTENANCE);

		List<Worker> members = new ArrayList<>();
		members.add(leader);

		Settlement destination = buildSettlement();
		return new TravelToSettlement(members, destination, rover);
	}

	/**
	 * Executes a Task for a number of steps or until it completes.
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
			tm.executeTask(MSOLS_PER_EXECUTE, 1D);
			callsLeft--;
		}
		
		return maxCalls - callsLeft;
	}

	/**
     * Creates a Clock pulse that just contains a MarsClock at a specific time.
     * 
     * @param missionSol Sol in the current mission
     * @param mSol MSol throught the day
     * @param newSol Is the new Sol flag set
     * @return
     */
    protected ClockPulse createPulse(int missionSol, int mSol, boolean newSol, boolean newHalfSol) {
        MarsTime marsTime = new MarsTime(1, 1, missionSol, mSol, missionSol);
		return createPulse(marsTime, newSol, newHalfSol);
	}

	protected ClockPulse createPulse(MarsTime marsTime, boolean newSol, boolean newHalfSol) {
		sim.getMasterClock().setMarsTime(marsTime);
        return new ClockPulse(pulseID++, 1D, marsTime, null, newSol, newHalfSol, true);
    }
}