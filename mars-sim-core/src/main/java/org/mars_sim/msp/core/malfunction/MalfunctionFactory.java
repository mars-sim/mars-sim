/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 3.1.0 2017-09-06
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.Inventory;
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
import org.mars_sim.msp.core.resource.PartConfig;
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

	/** The assumed # of years for calculating MTBF. */
	public static final int NUM_YEARS = 3;
	/** The maximum possible mean time between failure rate on Mars (Note that Mars has 669 sols in a year). */
	public static final double MAX_MTBF = 669 * NUM_YEARS;
	/** The maximum possible reliability percentage. */
	public static final double MAX_RELIABILITY = 99.999;
	
	// Data members
	private int newIncidentNum = 0;

	/** The possible malfunctions in the simulation. */
//	private Collection<Malfunction> malfunctions;
	/** The map for storing Part name and its instance. */
	private Map<String, Part> namePartMap;
	/** The map for storing the MTBF of Parts. */
	private Map<Integer, Double> MTBF_map;
	/** The map for storing the reliability of Parts. */
	private Map<Integer, Double> reliability_map;
	/** The map for storing the failure rate of Parts. */
	private Map<Integer, Integer> failure_map;
	/** The repair part probabilities per malfunction for a set of entity scope strings. */
	private static Map<Integer, Double> repairPartProbabilities;
	/** The probabilities of parts per maintenance for a set of entity scope strings. */
	private static Map<Integer, Double> maintenancePartProbabilities;
	
	private static MalfunctionConfig malfunctionConfig;
	private static PartConfig partConfig;
	
	private static Simulation sim = Simulation.instance();
	private static SimulationConfig simulationConfig = SimulationConfig.instance();
	private static Malfunction meteoriteImpactMalfunction;
	private static MissionManager missionManager;
	private static MarsClock marsClock;
	private static UnitManager unitManager;
	
	/**
	 * Constructs a MalfunctionFactory object.
	 * 
	 * @param malfunctionConfig malfunction configuration DOM document.
	 * @throws Exception when malfunction list could not be found.
	 */
	public MalfunctionFactory() {
		malfunctionConfig = simulationConfig.getMalfunctionConfiguration();
		partConfig = simulationConfig.getPartConfiguration();
//		malfunctions = malfunctionConfig.getMalfunctionList();
		missionManager = sim.getMissionManager();
	
		// Initialize maps 
		namePartMap = new HashMap<String, Part>();

		for (Part p : partConfig.getPartSet()) {
			namePartMap.put(p.getName(), p);
		}

		setupReliability();
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

		Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();

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

		Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();

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
		Collection<Malfunctionable> entities = new ArrayList<>(settlement.getBuildingManager().getBuildings());

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

		Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();

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
			repairPartProbabilities = new HashMap<Integer, Double>();
	
			for (Malfunction m : MalfunctionConfig.getMalfunctionList()) {
				if (m.isMatched(scope)) {
					double malfunctionProbability = m.getProbability() / 100D;
	
					String[] partNames = malfunctionConfig.getRepairPartNamesForMalfunction(m.getName());
					for (String partName : partNames) {
						double partProbability = malfunctionConfig.getRepairPartProbability(m.getName(), partName) / 100D;
						int partNumber = malfunctionConfig.getRepairPartNumber(m.getName(), partName);
						double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
						double totalNumber = averageNumber * partProbability * malfunctionProbability;
	//					Part part = (Part) ItemResource.findItemResource(partName);
	//					int id = part.getID();
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
			maintenancePartProbabilities = new HashMap<Integer, Double>();
	
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
	 * Sets up the reliability, MTBF and failure map
	 * 
	 */
	public void setupReliability() {
		MTBF_map = new HashMap<Integer, Double>();
		reliability_map = new HashMap<Integer, Double>();
		failure_map = new HashMap<Integer, Integer>();

		for (Part p : partConfig.getPartSet()) {
			int id = p.getID();
			MTBF_map.put(id, MAX_MTBF);
			failure_map.put(id, 0);
			reliability_map.put(id, 100.0);
		}
	}

	public Map<Integer, Double> getMTBFs() {
		return MTBF_map;
	}

	/**
	 * Computes reliability for a given part 
	 * 
	 * @param p
	 */
	public void computeReliability(Part p) {

		int id = p.getID();
		// double old_mtbf = MTBF_map.get(id);
		double new_mtbf = 0;

		if (marsClock == null)
			marsClock = Simulation.instance().getMasterClock().getMarsClock();

		int sol = marsClock.getMissionSol();
		int numSols = sol - p.getStartSol();
		int numFailures = failure_map.get(id);

		if (numFailures == 0)
			new_mtbf = MAX_MTBF;
		else {
			if (numSols == 0) {
				numSols = 1;

				new_mtbf = computeMTBF(numSols, numFailures, p);
			} else
				new_mtbf = computeMTBF(numSols, numFailures, p);
		}

		MTBF_map.put(id, new_mtbf);

		double percent_reliability = Math.exp(-numSols / new_mtbf) * 100;

//		 LogConsolidated.log(logger, Level.INFO, 0, sourceName,
//		 "The 3-year reliability rating of " + p.getName() + " is now "
//		 + Math.round(percent_reliability*100.0)/100.0 + " %", null);

		if (percent_reliability >= 100)
			percent_reliability = MAX_RELIABILITY;

		reliability_map.put(id, percent_reliability);

	}

	/**
	 * Computes the MTBF 
	 * 
	 * @param numSols
	 * @param numFailures
	 * @param p
	 * @return
	 */
	public double computeMTBF(double numSols, int numFailures, Part p) {
		int numItem = 0;
		// Obtain the total # of this part in used from all settlements
		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			Inventory inv = s.getInventory();
			int num = inv.getItemResourceNum(p);
			numItem += num;
		}

		// Take the average between the factory mtbf and the field measured mtbf
		return (numItem * numSols / numFailures + MAX_MTBF) / 2D;
	}

	/**
	 * Computes the reliability of a part
	 */
	public void computeReliability() {
		for (Part p : partConfig.getPartSet()) {
			computeReliability(p);
		}
	}

	/**
	 * Gets the failure rate of a part
	 * 
	 * @param id
	 * @return
	 */
	public int getFailure(int id) {
		return failure_map.get(id);
	}

	/**
	 * Gets the reliability of a part
	 * 
	 * @param id
	 * @return
	 */
	public double getReliability(int id) {
		return reliability_map.get(id);
	}

	/**
	 * Sets the failure rate for a given part
	 * 
	 * @param p
	 * @param num
	 */
	public void setFailure(Integer p, int num) {
		int old_failures = failure_map.get(p);// .getID());
		failure_map.put(p, old_failures + num);
	}

	/**
	 * Gets a map of part names and corresponding objects
	 * 
	 * @return a map of part names and objects
	 */
	public Map<String, Part> getNamePartMap() {
		return namePartMap;
	}
	

	/**
	 * Set instances
	 * 
	 * @param clock
	 */
	public static void initializeInstances(Simulation s, MarsClock c, UnitManager u) {
		marsClock = c;
		sim = s;
		simulationConfig = SimulationConfig.instance();
		malfunctionConfig = simulationConfig.getMalfunctionConfiguration();
		partConfig = simulationConfig.getPartConfiguration();
		missionManager = sim.getMissionManager();
		unitManager = u;
	}
	
	
	/**
	 * Prepares the object for garbage collection.
	 */
	public void destroy() {
//		malfunctions = null;
		
		namePartMap.clear();
		MTBF_map.clear();
		reliability_map.clear();
		failure_map.clear();
		if (repairPartProbabilities != null)
			repairPartProbabilities.clear();
		if (maintenancePartProbabilities != null)	
			maintenancePartProbabilities.clear();
		
		namePartMap = null;
		MTBF_map = null;
		reliability_map = null;
		failure_map = null;
		repairPartProbabilities = null;
		maintenancePartProbabilities = null;
		
		partConfig = null;
		
		sim = null;
		simulationConfig = null;
		meteoriteImpactMalfunction = null;
		missionManager = null;
		marsClock = null;
		unitManager = null;
		
		malfunctionConfig = null;
		meteoriteImpactMalfunction = null;
		missionManager = null;
	}
}