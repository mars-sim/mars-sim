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

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class is a factory for Malfunction objects.
 */
public final class MalfunctionFactory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

//	private static final Logger logger = Logger.getLogger(MalfunctionFactory.class.getName());

	public static final String METEORITE_IMPACT_DAMAGE = "Meteorite Impact Damage";

	// Data members
	private int newIncidentNum = 0;

	/** The possible malfunctions in the simulation. */
	private Collection<Malfunction> malfunctions;

	private static MalfunctionConfig config;
	
	private static Malfunction meteoriteImpactMalfunction;

	private static MissionManager missionManager;

	/**
	 * Constructs a MalfunctionFactory object.
	 * 
	 * @param config malfunction configuration DOM document.
	 * @throws Exception when malfunction list could not be found.
	 */
	public MalfunctionFactory() {
		config = SimulationConfig.instance().getMalfunctionConfiguration();
		malfunctions = config.getMalfunctionList();
		missionManager = Simulation.instance().getMissionManager();
	}

	/**
	 * Picks a malfunction from a given unit scope.
	 * 
	 * @param scopes a collection of scope strings defining the unit.
	 * @return a randomly-picked malfunction or null if there are none available.
	 */
	public Malfunction pickAMalfunction(Collection<String> scopes) {
		Malfunction mal = null;

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
			mal = determineRepairParts(mal);
		}
		else
			return null;
	
		return mal;

	}

	/**
	 * Determines the repair parts
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
			entities = getMalfunctionables(person.getVehicle());
		}

		Collection<Unit> inventoryUnits = person.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && !entities.contains(unit)) {
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
			entities = getMalfunctionables(robot.getVehicle());
		}

		Collection<Unit> inventoryUnits = robot.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && !entities.contains(unit)) {
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
		/*
		 * Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();
		 * 
		 * // Add all buildings within the settlement. Iterator<Building> i =
		 * settlement.getBuildingManager().getBuildings().iterator();//
		 * getACopyOfBuildings().iterator(); while (i.hasNext()) {
		 * entities.add(i.next()); }
		 * 
		 * for (Building b : settlement.getBuildingManager().getBuildings()) {
		 * entities.add(b); }
		 */
		Collection<Malfunctionable> entities = new ArrayList<>(settlement.getBuildingManager().getBuildings());

		// Add all malfunctionable entities in settlement inventory.
		Collection<Unit> inventoryUnits = settlement.getInventory().getContainedUnits();
		if (inventoryUnits.size() > 0) {
			for (Unit unit : inventoryUnits) {
				if ((unit instanceof Malfunctionable) && (!entities.contains(unit))) {
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
		Map<Integer, Double> result = new HashMap<Integer, Double>();

		for (Malfunction m : malfunctions) {
			if (m.isMatched(scope)) {
				double malfunctionProbability = m.getProbability() / 100D;

				String[] partNames = config.getRepairPartNamesForMalfunction(m.getName());
				for (String partName : partNames) {
					double partProbability = config.getRepairPartProbability(m.getName(), partName) / 100D;
					int partNumber = config.getRepairPartNumber(m.getName(), partName);
					double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
					double totalNumber = averageNumber * partProbability * malfunctionProbability;
//					Part part = (Part) ItemResource.findItemResource(partName);
//					int id = part.getID();
					Integer id = ItemResourceUtil.findIDbyItemResourceName(partName);
					if (result.containsKey(id))
						totalNumber += result.get(id);
					result.put(id, totalNumber);
				}
			}
		}

		return result;
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
		Map<Integer, Double> result = new HashMap<Integer, Double>();

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
					Integer id = ItemResourceUtil.findIDbyItemResourceName(part.getName());
					if (result.containsKey(id))
						totalNumber += result.get(id);
					result.put(id, totalNumber);

				}
			}
		}

		return result;
	}

	/**
	 * Obtains the malfunction representing the meteorite impact
	 * 
	 * @param malfunctionName
	 * @return {@link Malfunction}
	 */
	public static Malfunction getMeteoriteImpactMalfunction(String malfunctionName) {
		if (meteoriteImpactMalfunction == null) {
			for (Malfunction m : config.getMalfunctionList()) {
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
	 * Prepares the object for garbage collection.
	 */
	public void destroy() {
		malfunctions = null;
		config = null;
		meteoriteImpactMalfunction = null;
		missionManager = null;
	}
}