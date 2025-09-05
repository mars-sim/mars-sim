/*
 * Mars Simulation Project
 * RepairHelper.java
 * @date 2021-11-16
 * @author Barry Evans
 */
package com.mars_sim.core.malfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.mars_sim.core.equipment.EquipmentOwner;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.resource.MaintenanceScope;
import com.mars_sim.core.resource.Part;

/**
 * This is a helper class for completing Repair activities on a Malfunction
 */
public final class RepairHelper {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairHelper.class.getName());

	private RepairHelper() {
		// nothing
	}

	/**
	 * Prepares working on a Malfunction. This selects Deputy or Chief if available and registers for work.
	 * 
	 * @param malfunction
	 * @param repairer
	 * @param type
	 * @param entity
	 */
	public static void prepareRepair(Malfunction malfunction, Worker repairer,
									MalfunctionRepairWork type, Malfunctionable entity) {

		String myName = repairer.getName();

		if (malfunction.getChiefRepairer(type) == null) {
			malfunction.setChiefRepairer(type, myName);
			logger.info(repairer, 10_000L, "Appointed as the chief repairer handling the "
					+ type.getName() + " work for '"
					+ malfunction.getName() + "' on "
					+ entity.getName() + ".");
		}

		else if (malfunction.getDeputyRepairer(type) == null) {
			malfunction.setDeputyRepairer(type, myName);
			logger.info(repairer, 10_000L, "Appointed as the deputy repairer handling the "
					+ type.getName() + " work for '"
					+ malfunction.getName() + "' on "
					+ entity.getName() + ".");
		}

		logger.log(repairer, Level.FINEST, 10_000, "About to repair malfunction.");

		// Add name to reserved a slot
		malfunction.addWorkTime(type, 0D, myName);
	}


	/**
	 * Does a container have the parts required in stock ?
	 * 
	 * @param containerUnit
	 * @param malfunction
	 * @return
	 */
	public static boolean hasRepairParts(EquipmentOwner containerUnit, Malfunction malfunction) {

		boolean result = true;

		if (containerUnit == null)
			throw new IllegalArgumentException("containerUnit is null");

		if (malfunction == null)
			throw new IllegalArgumentException("malfunction is null");

		for (Entry<MaintenanceScope, Integer> item : malfunction.getRepairParts().entrySet()) {
			MaintenanceScope ms = item.getKey();
			Part part = ms.getPart();
			int id = part.getID();
			int number = item.getValue();
			if (containerUnit.getItemResourceStored(id) < number) {
				result = false;
			}
		}

		return result;
	}

	/**
	 * Claim any needed parts from the Inventory of the container. Valid that none may be needed.
	 *
	 * @param containerUnit
	 * @param malfunction
	 * @return 
	 */
	public static Map<MaintenanceScope, Integer>  claimRepairParts(EquipmentOwner containerUnit, Malfunction malfunction) {
		Map<MaintenanceScope, Integer> replaced = new HashMap<>();
		Map<MaintenanceScope, Integer> needed = malfunction.getRepairParts();
		if (!needed.isEmpty()) {
			// Take a copy because source will get modified
			Map<MaintenanceScope, Integer> parts = new HashMap<>(needed);

			// Add repair parts if necessary.
			for (Entry<MaintenanceScope, Integer> entry : parts.entrySet()) {
				MaintenanceScope ms = entry.getKey(); 
				Part part = ms.getPart();
				int id = part.getID();
				int number = entry.getValue();
				int shortfall = containerUnit.retrieveItemResource(id, number);
				// Add in the repair part
				malfunction.repairWithParts(ms, number - shortfall, containerUnit);
				
				replaced.put(ms, number - shortfall);
			}
		}
		return replaced;
	}

	/**
	 * Find the closest store for Repair parts to a Worker.
	 * @param repairer
	 * @return
	 */
	public static EquipmentOwner getClosestRepairStore(Worker repairer) {
		EquipmentOwner partStore;
		if (repairer.isInVehicle()) {
			partStore = repairer.getVehicle();
		}
		else {
			partStore = repairer.getSettlement();
		}

		return partStore;
	}
}
