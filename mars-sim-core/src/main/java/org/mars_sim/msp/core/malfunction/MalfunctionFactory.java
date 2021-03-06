/**
 * Mars Simulation Project
 * MalfunctionFactory.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.MaintenanceScope;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.resource.PartConfig;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * This class is a factory for Malfunction objects.
 */
public final class MalfunctionFactory implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MalfunctionFactory.class.getName());
	
	public static final String METEORITE_IMPACT_DAMAGE = "Meteorite Impact Damage";
	
	// Data members
	private int newIncidentNum = 0;
		
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
	public Malfunction pickAMalfunction(Collection<String> scopes) {
		MalfunctionMeta choosenMalfunction = null;

		MalfunctionConfig mc = SimulationConfig.instance().getMalfunctionConfiguration();

		List<MalfunctionMeta> malfunctions = mc.getMalfunctionList();
		double totalProbability = 0D;
		// Total probability is fixed
		for (MalfunctionMeta m : malfunctions) {
			if (m.isMatched(scopes)) {
				totalProbability += m.getProbability();
			}
		}

		double r = RandomUtil.getRandomDouble(totalProbability);
		for (MalfunctionMeta m : malfunctions) {
			double probability = m.getProbability();
			// will only pick one malfunction at a time (if mal == null, quit)
			if (m.isMatched(scopes) && (choosenMalfunction == null)) {
				if (r < probability) {
					choosenMalfunction = m;
				} else
					r -= probability;
			}
		}

		// Safety check if probability failed to pick malfuncton
		if (choosenMalfunction == null) {
			logger.warning("Failed to pick Malfunction by probability " + totalProbability);
			choosenMalfunction = malfunctions.get(0);
		}
		
		double failureRate = choosenMalfunction.getProbability();
		Malfunction mal = null;
		// Note : the composite probability of a malfunction is dynamically updated as
		// the field reliability data trickles in
		if (RandomUtil.lessThanRandPercent(failureRate)) {
			// Clones a malfunction and determines repair parts
			mal = new Malfunction(getNewIncidentNum(), choosenMalfunction);
		}
	
		return mal;
	}

	/**
	 * Gets a collection of malfunctionable entities local to the given person.
	 * 
	 * @return collection collection of malfunctionables.
	 */
	public static Collection<Malfunctionable> getLocalMalfunctionables(Worker source) {

		Collection<Malfunctionable> entities = new ArrayList<Malfunctionable>();

		if (source.isInSettlement()) {
			entities = getMalfunctionables(source.getSettlement());
		}

		if (source.isInVehicle()) {
			entities.addAll(getMalfunctionables((Malfunctionable) source.getVehicle()));
		}

		Collection<Unit> inventoryUnits = source.getInventory().getContainedUnits();
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

		// Get all vehicles belong to the Settlement. Vehicles can have a malfunction
		// in the Settlement or outside settlement
		for (Vehicle vehicle : settlement.getAllAssociatedVehicles()) {
			entities.addAll(getMalfunctionables(vehicle));			
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
				entities.addAll(getLocalMalfunctionables(person));
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
		Map<Integer, Double> repairPartProbabilities = new HashMap<>();

		MalfunctionConfig mc = SimulationConfig.instance().getMalfunctionConfiguration();

		for (MalfunctionMeta m : mc.getMalfunctionList()) {
			if (m.isMatched(scope)) {
				double malfunctionProbability = m.getProbability() / 100D;

				for (RepairPart p : m.getParts()) {
					double partProbability = p.getProbability() / 100D;
					double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(p.getNumber());
					double totalNumber = averageNumber * partProbability * malfunctionProbability;

					Integer id = ItemResourceUtil.findIDbyItemResourceName(p.getName());
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
	Map<Integer, Double> getMaintenancePartProbabilities(Set<String> scope) {
		Map<Integer, Double> maintenancePartProbabilities = new HashMap<>();
		PartConfig partConfig = SimulationConfig.instance().getPartConfiguration();
		for (MaintenanceScope maintenance : partConfig.getMaintenance(scope)) {
			double prob = maintenance.getProbability() / 100D;
			int partNumber = maintenance.getMaxNumber();
			double averageNumber = RandomUtil.getRandomRegressionIntegerAverageValue(partNumber);
			double totalNumber = averageNumber * prob;

			Integer id = maintenance.getPart().getID();
			if (maintenancePartProbabilities.containsKey(id))
				totalNumber += maintenancePartProbabilities.get(id);
			maintenancePartProbabilities.put(id, totalNumber);				
		}

		return maintenancePartProbabilities;
	}

	/**
	 * Obtains the malfunction representing the specified name
	 * 
	 * @param malfunctionName
	 * @return {@link Malfunction}
	 */
	public static MalfunctionMeta getMalfunctionByname(String malfunctionName) {
		MalfunctionMeta result = null;
		MalfunctionConfig mc = SimulationConfig.instance().getMalfunctionConfiguration();
		for (MalfunctionMeta m : mc.getMalfunctionList()) {
			if (m.getName().equals(malfunctionName))
				result = m;
		}

		return result;
	}

	/**
	 * Gets the next incident number for the simulation 
	 * 
	 * @return
	 */
	synchronized int getNewIncidentNum() {
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
}
