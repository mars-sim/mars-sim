package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;

import org.junit.Before;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.util.PersonTaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Task;
import org.mars_sim.msp.core.structure.MockSettlement;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.FunctionSpec;
import org.mars_sim.msp.core.structure.building.MockBuilding;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.VehicleGarage;
import org.mars_sim.msp.core.vehicle.Rover;

import junit.framework.TestCase;

public abstract class AbstractMarsSimUnitTest extends TestCase {

	protected static final double BUILDING_LENGTH = 9D;
	protected static final double BUILDING_WIDTH = 9D;

	private static final double MSOLS_PER_EXECUTE = 0.1D;
	
	protected UnitManager unitManager;
	protected MarsSurface surface;
	protected Simulation sim;
	private FunctionSpec evaFunction;

	public AbstractMarsSimUnitTest() {
		super();
	}

	public AbstractMarsSimUnitTest(String name) {
		super(name);
	}

	@Before
	public void setUp() {
	    // Create new simulation instance.
	    SimulationConfig simConfig = SimulationConfig.instance();
	    simConfig.loadConfig();
	    
	    sim = Simulation.instance();
	    sim.testRun();
	    
	    // Clear out existing settlements in simulation.
	    unitManager = sim.getUnitManager();
	    Iterator<Settlement> i = unitManager.getSettlements().iterator();
	    while (i.hasNext()) {
	        unitManager.removeUnit(i.next());
	    }
		
	    Function.initializeInstances(simConfig.getBuildingConfiguration(), sim.getMasterClock().getMarsClock(),
	    							 simConfig.getPersonConfig(), simConfig.getCropConfiguration(), sim.getSurfaceFeatures(),
	    							 sim.getWeather(), unitManager);
	    
	    surface = unitManager.getMarsSurface();
		evaFunction = simConfig.getBuildingConfiguration().getFunctionSpec("EVA Airlock", FunctionType.EVA);
	}

	
	protected Rover buildRover(Settlement settlement, String name, LocalPosition parked) {
	    Rover rover1 = new Rover(name, "Explorer Rover", settlement);
	    rover1.setParkedLocation(parked, 0D);
	    unitManager.addUnit(rover1);
	    return rover1;
	}

	protected VehicleGarage buildGarage(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
	
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);

	    building0.addFunction(new EVA(building0, evaFunction));
	
	    LocalPosition parkingLocation = LocalPosition.DEFAULT_POSITION;
	    VehicleGarage garage = new VehicleGarage(building0,
	            new LocalPosition[] { parkingLocation });
	    building0.addFunction(garage);
	    
	    return garage;
	}

	protected MockBuilding buildBuilding(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		String name = "B" + id;
		
	    MockBuilding building0 = new MockBuilding(buildingManager, name);
	    building0.setTemplateID(id);
	    building0.setName(name);
	    building0.setWidth(BUILDING_WIDTH);
	    building0.setLength(BUILDING_LENGTH);
	    building0.setLocation(pos);
	    building0.setFacing(facing);
	    buildingManager.addMockBuilding(building0);	
	    
	    unitManager.addUnit(building0);
	
	    return building0;
	}

	protected Building buildEVA(BuildingManager buildingManager, LocalPosition pos, double facing, int id) {
		MockBuilding building0 = buildBuilding(buildingManager, pos, facing, id);

	    building0.addFunction(new EVA(building0,  evaFunction));
	    return building0;
	}

	protected Settlement buildSettlement() {
		Settlement settlement = new MockSettlement();
		unitManager.addUnit(settlement);
		return settlement;
	}

	protected Person buildPerson(String name, Settlement settlement) {
		Person person = Person.create(name, settlement)
				.setGender(GenderType.MALE)
				.setCountry(null)
				.setSponsor(null)
				.setSkill(null)
				.setPersonality(null, null)
				.setAttribute(null)
				.build();
		person.initialize();
		
		unitManager.addUnit(person);
		return person;
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
		tm.startTask(task);
		
		int callsLeft = maxCalls;
		while ((callsLeft > 0) && !task.isDone()) {
			tm.executeTask(MSOLS_PER_EXECUTE, 1D);
			callsLeft--;
		}
		
		return maxCalls - callsLeft;
	}

}