/*
 * Mars Simulation Project
 * RepairHelper.java
 * @date 2021-09-10
 * @author Barry Evans
 */
package org.mars_sim.msp.core.malfunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;

/**
 * This is a helper class for completing Repair activities on a Malfunction
 */
public final class RepairHelper {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(RepairHelper.class.getName());
	
	private RepairHelper() {
		
	}
	
	/**
	 * Start working on a Malfunction. This selects Deputy or Cheif if available and registers for work.
	 * @param malfunction
	 * @param repairer
	 * @param type
	 * @param entity
	 */
	public static void startRepair(Malfunction malfunction, Worker repairer,
									MalfunctionRepairWork type, Malfunctionable entity) {
	
		String chief = malfunction.getChiefRepairer(type);
		String deputy = malfunction.getDeputyRepairer(type);
		String myName = repairer.getName();
		if (chief == null) {
			logger.fine(repairer, "Was appointed as the chief repairer handling the " 
					+ type.getName() + " work for '" 
					+ malfunction.getName() + "' on "
					+ entity.getNickName());
			 malfunction.setChiefRepairer(type, myName);						
		}
		else if ((deputy == null) && !chief.equals(myName)) {
			logger.fine(repairer, "Was appointed as the deputy repairer handling the "
					+ type.getName() + " work for '" 
					+ malfunction.getName() + "' on "
					+ entity.getNickName());
			malfunction.setDeputyRepairer(type, myName);
		}
		
		logger.log(repairer, Level.FINEST, 500, "About to repair malfunction.");
	
		// Add time to reserved a slot
		malfunction.addWorkTime(type, 0D, myName);
	}
	

	/**
	 * Does a container have the parts required in stock ?
	 * @param containerUnit
	 * @param malfunction
	 * @return
	 */
	public static boolean hasRepairParts(Unit containerUnit, Malfunction malfunction) {

		boolean result = true;

		if (containerUnit == null)
			throw new IllegalArgumentException("containerUnit is null");

		if (malfunction == null)
			throw new IllegalArgumentException("malfunction is null");

		Inventory inv = containerUnit.getInventory();
		for (Entry<Integer, Integer> item : malfunction.getRepairParts().entrySet()) {	
			Integer part = item.getKey();
			int number = item.getValue();
			if (inv.getItemResourceNum(part) < number) {
				inv.addItemDemand(part, number);
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
	 */
	public static void claimRepairParts(Unit containerUnit, Malfunction malfunction) {
		Map<Integer, Integer> needed = malfunction.getRepairParts();
		if (!needed.isEmpty()) {
			// Take a copy because source will get modified
			Map<Integer, Integer> parts = new HashMap<>(needed);
			
			// Add repair parts if necessary.
			Inventory inv = containerUnit.getInventory();
			for (Entry<Integer, Integer> part : parts.entrySet()) {
				Integer id = part.getKey();
				int number = part.getValue();
				inv.retrieveItemResources(id, number);
				malfunction.repairWithParts(id, number, inv);
			}
		}
	}

}
