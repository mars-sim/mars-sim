/*
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @date 2025-09-02
 * @author Scott Davis
 */

package com.mars_sim.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.Unit;
import com.mars_sim.core.equipment.EVASuit;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;
import com.mars_sim.core.resource.PartConfig;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * This class is a factory for Malfunction objects.
 */
public final class MalfunctionFactory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MalfunctionFactory.class.getName());
	
	public static final String METEORITE_IMPACT_DAMAGE = "Meteorite Impact Damage";

	// Data members
	private int newIncidentNum = 0;

	public static SimulationConfig simulationConfig = SimulationConfig.instance();
	public static MalfunctionConfig mc = simulationConfig.getMalfunctionConfiguration();
	public static PartConfig partConfig = simulationConfig.getPartConfiguration();

	/**
	 * Constructs a MalfunctionFactory object.
	 *
	 * @param malfunctionConfig malfunction configuration DOM document.
	 * @throws Exception when malfunction list could not be found.
	 */
	public MalfunctionFactory() {
	}

	/**
	 * Picks a malfunction from a given unit scope.
	 *
	 * @param scopes a collection of scope strings defining the unit.
	 * @return a randomly-picked malfunction or null if there are none available.
	 */
	public MalfunctionMeta pickAMalfunction(Collection<String> scopes) {
		MalfunctionMeta choosenMalfunction = null;

		List<MalfunctionMeta> malfunctions = new ArrayList<>(mc.getMalfunctionList());
		double totalProbability = 0D;
		// Total probability is fixed
		for (MalfunctionMeta m : malfunctions) {
			if (m.isMatched(scopes)) {
				totalProbability += m.getProbability();
			}
		}

		double r = RandomUtil.getRandomDouble(totalProbability);
		// Shuffle the malfunction list
		Collections.shuffle(malfunctions);
		for (MalfunctionMeta m : malfunctions) {
			double probability = m.getProbability();
			
			if (m.isMatched(scopes) && (choosenMalfunction == null)) {
				if (r < probability) {
					// will only pick one malfunction at a time 
					choosenMalfunction = m;
					break;
				} else
					r -= probability;
			}
		}

		// Safety check if probability failed to pick malfunction
		if (choosenMalfunction == null) {
			logger.warning("Failed to pick a malfunction by probability " + totalProbability + ".");
			choosenMalfunction = malfunctions.get(0);
		}

		double failureRate = choosenMalfunction.getProbability();
		// Note : the composite probability of a malfunction is dynamically updated as
		// the field reliability data trickles in
		if (!RandomUtil.lessThanRandPercent(failureRate)) {
			choosenMalfunction = null;
		}

		return choosenMalfunction;
	}

	/**
	 * Gets a collection of malfunctionable entities local to the given worker.
	 * NOTE: DO NOT DELTE. RETAIN THIS METHOD FOR FUTURE USE.
	 * 
	 * @param worker
	 * @return collection collection of malfunctionables.
	 */
	private static Collection<Malfunctionable> getLocalMalfunctionables(Worker worker) {

		Collection<Malfunctionable> entities = new ArrayList<>();

		if (worker.isInSettlement()) {
			entities = getBuildingMalfunctionables(worker.getSettlement());
		}

		if (worker.isInVehicle()) {
			entities.addAll(getMalfunctionables((Malfunctionable) worker.getVehicle()));
		}

		Collection<? extends Unit> inventoryUnits = null;

		inventoryUnits = ((EquipmentOwner)worker).getEquipmentSet();

		if (inventoryUnits != null && !inventoryUnits.isEmpty()) {
			for (Unit unit : inventoryUnits) {
				if (unit instanceof Malfunctionable u && !entities.contains(u)) {
					entities.add(u);
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
	private static Collection<Malfunctionable> getBuildingMalfunctionables(Settlement settlement) {
		// Should get a collection of buildings only
		return new ArrayList<>(settlement.getBuildingManager().getBuildingSet());
	}

	/**
	 * Gets a collection of malfunctionable entities local to the given
	 * malfunctionable entity.
	 *
	 * @return collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getMalfunctionables(Malfunctionable entity) {

		Collection<Malfunctionable> entities = new ArrayList<>();

		entities.add(entity);

		if (entity instanceof EquipmentOwner eo) {
			for (Equipment e : eo.getEquipmentSet()) {
				if (e instanceof Malfunctionable m) {
					entities.add(m);
				}
			}
		}
		
		// Note: Must filter out drones
		if (entity instanceof Rover || entity instanceof LightUtilityVehicle) {
			Collection<Robot> inventoryUnits1 = ((Crewable)entity).getRobotCrew();
			for (Unit unit : inventoryUnits1) {
				if (unit instanceof Malfunctionable u) {
					entities.add(u);
				}
			}
		}

		else if (entity instanceof Settlement s) {
			entities.addAll(getBuildingMalfunctionables(s));
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

		// Add buildings in settlement
		Collection<Malfunctionable> entities = getBuildingMalfunctionables(settlement);

		// Get all vehicles belong to the Settlement. Vehicles can have a malfunction
		// in the Settlement or outside settlement
		for (Vehicle vehicle : settlement.getParkedGaragedVehicles()) {
			entities.addAll(getMalfunctionables(vehicle));
		}

		// Get entities carried by robots
		for (Robot robot : settlement.getAllAssociatedRobots()) {
			entities.addAll(getMalfunctionables(robot));
		}

		// Get entities carried by people on EVA.
		// for (Person person : settlement.getAllAssociatedPeople()) {
		// 	if (person.isOutside())
		// 		entities.addAll(getLocalMalfunctionables(person));
		// }

		// Get entities carried by people on EVA.
		for (Equipment e: settlement.getSuitSet()) {
			EVASuit suit = (EVASuit)e;
			if (suit.getMalfunctionManager().hasMalfunction())
				entities.add(suit);
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
	public static Map<Integer, Double> getRepairPartProbabilities(Collection<String> scope) {
		Map<Integer, Double> repairPartProbabilities = new HashMap<>();

		for (MalfunctionMeta m : mc.getMalfunctionList()) {
			if (m.isMatched(scope)) {
				double malfunctionProbability = m.getProbability() / 100D;

				for (RepairPart p : m.getParts()) {
					double partProbability = p.getRepairProbability() / 100D;
					double averageNumber = RandomUtil.getIntegerAverageValue(p.getNumber());
					double totalNumber = averageNumber * partProbability * malfunctionProbability;

					int id = p.getPartID();
					if (repairPartProbabilities.containsKey(id))
						totalNumber += repairPartProbabilities.get(id);
					repairPartProbabilities.put(id, totalNumber);
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
	static Map<Integer, Double> getMaintenancePartProbabilities(Set<String> scope) {
		Map<Integer, Double> maintenancePartProbabilities = new HashMap<>();
		for (MaintenanceScope maintenance : partConfig.getMaintenanceScopeList(scope)) {
			double prob = maintenance.getProbability() / 100D;
			int partNumber = maintenance.getMaxNumber();
			double averageNumber = RandomUtil.getIntegerAverageValue(partNumber);
			double totalNumber = averageNumber * prob;

			Integer id = maintenance.getPart().getID();
			if (maintenancePartProbabilities.containsKey(id))
				totalNumber += maintenancePartProbabilities.get(id);
			maintenancePartProbabilities.put(id, totalNumber);
		}

		return maintenancePartProbabilities;
	}

	/**
	 * Obtains the malfunction representing the specified name.
	 *
	 * @param malfunctionName
	 * @return {@link Malfunction}
	 */
	public static MalfunctionMeta getMalfunctionByName(String malfunctionName) {
		MalfunctionMeta result = null;

		for (MalfunctionMeta m : mc.getMalfunctionList()) {
			if (m.getName().equalsIgnoreCase(malfunctionName))
				result = m;
		}

		return result;
	}

	/**
	 * Gets the next incident number for the simulation.
	 *
	 * @return
	 */
	synchronized int getNewIncidentNum() {
		return ++newIncidentNum;
	}

	/**
	 * Computes the reliability of each part.
	 * 
	 * @param missionSol
	 */
	public void computePartReliability(int missionSol) {
		for (Part p : Part.getParts()) {
			p.computeReliability(missionSol);
		}
	}
}
