/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class is a factory for Malfunction objects.
 */
public final class MalfunctionFactory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final Logger logger = Logger.getLogger(MalfunctionFactory.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	public static final String METEORITE_IMPACT_DAMAGE = "Meteorite Impact Damage";
	
	// Data members
	private int newIncidentNum = 0;

	/** The repair part probabilities per malfunction for a set of entity scope strings. */
	private static Map<Integer, Double> repairPartProbabilities;
	/** The probabilities of parts per maintenance for a set of entity scope strings. */
	private static Map<Integer, Double> maintenancePartProbabilities;
	
	private static MalfunctionConfig malfunctionConfig;
	
	private static Simulation sim = Simulation.instance();
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static Malfunction meteoriteImpactMalfunction;
	private static MissionManager missionManager;
	
	/**
	 * Constructs a MalfunctionFactory object.
	 * 
	 * @param malfunctionConfig malfunction configuration DOM document.
	 * @throws Exception when malfunction list could not be found.
	 */
	public MalfunctionFactory() {
		malfunctionConfig = simulationConfig.getMalfunctionConfiguration();
		missionManager = sim.getMissionManager();
	}

	/**
	 * Picks a malfunction from a given unit scope.
	 * 
	 * @param scopes a collection of scope strings defining the unit.
	 * @return a randomly-picked malfunction or null if there are none available.
	 */
	public Malfunction pickAMalfunction(Collection<String> scopes) {
		Malfunction mal = null;

		Collection<Malfunction> malfunctions = MalfunctionConfig.getMalfunctionList();
		double totalProbability = 0D;
		if (malfunctions.size() > 0) {
			for (Malfunction m : malfunctions) {
				if (m.isMatched(scopes)) {
					totalProbability += m.getProbability();
				}
			}
		}

		double r = RandomUtil.getRandomDouble(totalProbability);
		for (Malfunction m : malfunctions) {
			double probability = m.getProbability();
			// will only pick one malfunction at a time (if mal == null, quit)
			if (m.isMatched(scopes) && (mal == null)) {
				if (r < probability) {
					try {
						mal = m;
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				} else
					r -= probability;
			}
		}

		double failure_rate = mal.getProbability();
		// Note : the composite probability of a malfunction is dynamically updated as
		// the field reliability data trickles in
		if (RandomUtil.lessThanRandPercent(failure_rate)) {
			// Clones a malfunction and determines repair parts
			mal = determineRepairParts(mal);
		}
		else
			return null;
	
		return mal;

	}

	/**
	 * Clones a malfunction and determines repair parts
	 * 
	 * @param mal
	 * @return {@link Malfunction}
	 */
	public Malfunction determineRepairParts(Malfunction mal) {
		mal = mal.getClone();
		mal.determineRepairParts();
		return mal;
	}
	
	/**
	 * Gets a collection of malfunctionable entities local to the given person.
	 * 
	 * @return collection collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getMalfunctionables(Person person) {

		Collection<Malfunctionable> entities = new CopyOnWriteArrayList<Malfunctionable>();

		if (person.isInSettlement()) {
			entities = getMalfunctionables(person.getSettlement());
		}

		if (person.isInVehicle()) {
			entities.addAll(getMalfunctionables(person.getVehicle()));
		}

		Collection<Unit> inventoryUnits = person.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && !entities.contains((Malfunctionable)unit)) {
					entities.add((Malfunctionable) unit);
				}
			}
		}

		return entities;
	}

	public static Collection<Malfunctionable> getMalfunctionables(Robot robot) {

		Collection<Malfunctionable> entities = new CopyOnWriteArrayList<Malfunctionable>();

		if (robot.isInSettlement()) {
			entities = getMalfunctionables(robot.getSettlement());
		}

		if (robot.isInVehicle()) {
			entities.addAll(getMalfunctionables(robot.getVehicle()));
		}

		Collection<Unit> inventoryUnits = robot.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && !entities.contains((Malfunctionable) unit)) {
					entities.add((Malfunctionable) unit);
				}
			}
		}

		return entities;
	}

	/**
	 * Gets a collection of malfunctionable entities local to a given settlement.
	 * 
	 * @param settlement the settlement.
	 * @return collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getMalfunctionables(Settlement settlement) {
		Collection<Malfunctionable> entities = new CopyOnWriteArrayList<>(settlement.getBuildingManager().getBuildings());

		// Add all malfunctionable entities in settlement inventory.
		// TODO: need to separate the malfunctionable in a vehicle ?
		Collection<Unit> inventoryUnits = settlement.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && (!entities.contains((Malfunctionable)unit))) {
					entities.add((Malfunctionable) unit);
				}
			}
		}

		return entities;
	}

	/**
	 * Gets a collection of malfunctionable entities local to the given
	 * malfunctionable entity.
	 * 
	 * @return collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getMalfunctionables(Malfunctionable entity) {

		Collection<Malfunctionable> entities = new CopyOnWriteArrayList<Malfunctionable>();

		entities.add(entity);

		Collection<Unit> inventoryUnits = entity.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if (unit instanceof Malfunctionable) {
					entities.add((Malfunctionable) unit);
				}
			}
		}

		return entities;
	}

	/**
	 * Gets all malfunctionables associated with a settlement.
	 * 
	 * @param settlement the settlement.
	 * @return collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getAssociatedMalfunctionables(Settlement settlement) {

		// Add settlement, buildings and all other malfunctionables in settlement
		// inventory.
		Collection<Malfunctionable> entities = getMalfunctionables(settlement);

		if (missionManager == null)
			missionManager = Simulation.instance().getMissionManager();
		// Add all associated rovers out on missions and their inventories.
		for (Mission mission : missionManager.getMissionsForSettlement(settlement)) {
			if (mission instanceof VehicleMission) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if ((vehicle != null) && !settlement.equals(vehicle.getSettlement()))
					entities.addAll(getMalfunctionables(vehicle));
			}
		}

		// Get entities carried by robots
		for (Robot robot : settlement.getAllAssociatedRobots()) {
			if (robot.isOutside()) // .getLocationSituation() == LocationSituation.OUTSIDE)
				entities.addAll(getMalfunctionables(robot));
		}

		// TODO: how to ask robots first and only ask people if robots are not available
		// so that the tasks are not duplicated ?
		// Get entities carried by people on EVA.
		for (Person person : settlement.getAllAssociatedPeople()) {
			if (person.isOutside()) // getLocationSituation() == LocationSituation.OUTSIDE)
				entities.addAll(getMalfunctionables(person));
		}

		return entities;
	}

	/**
	 * Gets the repair part probabilities per malfunction for a set of entity scope
	 * strings.
	 * 
	 * @param scope a collection of entity scope strings.
	 * @return map of repair parts and probable number of parts needed per
	 *         malfunction.
	 * @throws Exception if error finding repair part probabilities.
	 */
	Map<Integer, Double> getRepairPartProbabilities(Collection<String> scope) {
		if (repairPartProbabilities == null) {
			repairPartProbabilities = new ConcurrentHashMap<Integer, Double>();
	
			for (Malfunction m : MalfunctionConfig.getMalfunctionList()) {
				if (m.isMatched(scope)) {
					double malfunctionProbability = m.getProbability() / 100D;
	
					String[] partNames = malfunctionConfig.getRepairPartNamesForMalfunction(m.getName());
					for (String partName : partNames) {
						double partProbability = malfunctionConfig.getRepairPartProbability(m.getName(), partName) / 100D;
						int partNumber = malfunctionConfig.getRepairPartNumber(m.getName(), partName);
						double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
						double totalNumber = averageNumber * partProbability * malfunctionProbability;

						Integer id = ItemResourceUtil.findIDbyItemResourceName(partName);
						if (repairPartProbabilities.containsKey(id))
							totalNumber += repairPartProbabilities.get(id);
						repairPartProbabilities.put(id, totalNumber);
					}
				}
			}
		}
		return repairPartProbabilities;
	}

	/**
	 * Gets the probabilities of parts per maintenance for a set of entity scope
	 * strings.
	 * 
	 * @param scope a collection of entity scope strings.
	 * @return map of maintenance parts and probable number of parts needed per
	 *         maintenance.
	 * @throws Exception if error finding maintenance part probabilities.
	 */
	Map<Integer, Double> getMaintenancePartProbabilities(Collection<String> scope) {
		if (maintenancePartProbabilities == null) {
			maintenancePartProbabilities = new ConcurrentHashMap<Integer, Double>();
	
			for (String entity : scope) {
				for (Part part : ItemResourceUtil.getItemResources()) {
					if (part.hasMaintenanceEntity(entity)) {
						double prob = part.getMaintenanceProbability(entity) / 100D;
						int partNumber = part.getMaintenanceMaximumNumber(entity);
						double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
						double totalNumber = averageNumber * prob;
	//					if (result.containsKey(part)) 
	//						totalNumber += result.get(part);
	//					result.put(part, totalNumber);
						Integer id = part.getID();//ItemResourceUtil.findIDbyItemResourceName(part.getName());
						if (maintenancePartProbabilities.containsKey(id))
							totalNumber += maintenancePartProbabilities.get(id);
						maintenancePartProbabilities.put(id, totalNumber);
	
					}
				}
			}
		}
		return maintenancePartProbabilities;
	}

	/**
	 * Obtains the malfunction representing the meteorite impact
	 * 
	 * @param malfunctionName
	 * @return {@link Malfunction}
	 */
	public static Malfunction getMeteoriteImpactMalfunction(String malfunctionName) {
		if (meteoriteImpactMalfunction == null) {
			for (Malfunction m : MalfunctionConfig.getMalfunctionList()) {
				if (m.getName().equals(malfunctionName))
					meteoriteImpactMalfunction = m;
			}
		}
		return meteoriteImpactMalfunction;
	}

	/**
	 * Gets the next incident number for the simulation 
	 * 
	 * @return
	 */
	public int getNewIncidentNum() {
		return ++newIncidentNum;
	}

	/**
	 * Computes the reliability of a part
	 */
	public void computeReliability() {
		for (Part p : Part.getParts()) {
			p.computeReliability();
		}
	}

	/**
	 * Set instances
	 * 
	 * @param clock
	 */
	public static void initializeInstances(Simulation s, MarsClock c, UnitManager u) {
		sim = s;
		simulationConfig = SimulationConfig.instance();
		malfunctionConfig = simulationConfig.getMalfunctionConfiguration();
		missionManager = sim.getMissionManager();
	}
	
	
	/**
	 * Prepares the object for garbage collection.
	 */
	public void destroy() {
		
		if (repairPartProbabilities != null)
			repairPartProbabilities.clear();
		if (maintenancePartProbabilities != null)	
			maintenancePartProbabilities.clear();
		
		repairPartProbabilities = null;
		maintenancePartProbabilities = null;
		
		sim = null;
		simulationConfig = null;
		meteoriteImpactMalfunction = null;
		missionManager = null;
		
		malfunctionConfig = null;
		meteoriteImpactMalfunction = null;
		missionManager = null;
	}
}
