/**
 * Mars Simulation Project
 * Part.java
 * @version 3.1.2 2020-09-02
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.util.*;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The Part class represents a type of unit resource that is used for
 * maintenance and repairs.
 */
public class Part extends ItemResource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The assumed # of years for calculating MTBF. */
	public static final int NUM_YEARS = 3;
	/** The maximum possible mean time between failure rate on Mars (Note that Mars has 669 sols in a year). */
	public static final double MAX_MTBF = 669 * NUM_YEARS;
	/** The maximum possible reliability percentage. */
	public static final double MAX_RELIABILITY = 99.999;
	
	// Domain members
	//private List<MaintenanceScope> maintenanceEntities;

	// Number of failures
	private int numFailures = 0;

	private double mtbf = MAX_MTBF;

	private double percentReliability = MAX_RELIABILITY;

	/**
	 * Constructor.
	 * 
	 * @param name        the name of the part.
	 * @param id          the id# of the part
	 * @param description {@link String}
	 * @param mass        the mass of the part (kg)
	 * @param the         sol when this part is put to use
	 */
	public Part(String name, int id, String description, double mass, int solsUsed) {
		// Use ItemResource constructor.
		super(name, id, description, mass, solsUsed);

		//maintenanceEntities = new ArrayList<MaintenanceScope>();
	}
	
	/**
	 * Gets a set of all parts.
	 * 
	 * @return set of parts.
	 */
	public static Set<Part> getParts() {
		return ItemResourceUtil.getItemResources();
	}

	/**
	 * Gets a set of all parts.
	 * 
	 * @return set of parts.
	 */
	public static Set<Integer> getItemIDs() {
		return ItemResourceUtil.getItemIDs();
	}

	public void computeReliability() {

		// TODO need to resolve this later
		MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

		int sol = marsClock.getMissionSol();
		int numSols = sol - getStartSol();

		if (numFailures  == 0)
			mtbf = MAX_MTBF;
		else {
			numSols = Math.max(1, numSols);
			mtbf = computeMTBF(numSols);
		}

		if (mtbf == 0) {
			percentReliability = MAX_RELIABILITY;		
		}
		else {
			percentReliability = Math.exp(-numSols / mtbf) * 100;

//		 LogConsolidated.log(logger, Level.INFO, 0, sourceName,
//		 "The 3-year reliability rating of " + p.getName() + " is now "
//		 + Math.round(percent_reliability*100.0)/100.0 + " %", null);

			percentReliability = Math.min(MAX_RELIABILITY, percentReliability);	
		}
	}
	
	private double computeMTBF(double numSols) {
		int numItem = 0;
		UnitManager unitManager = Simulation.instance().getUnitManager();
		
		// Obtain the total # of this part in used from all settlements
		Collection<Settlement> ss = unitManager.getSettlements();
		for (Settlement s : ss) {
			Inventory inv = s.getInventory();
			int num = inv.getItemResourceNum(this);
			numItem += num;
		}

		// Take the average between the factory mtbf and the field measured mtbf
		return (numItem * numSols / numFailures + MAX_MTBF) / 2D;
	}

	public double getReliability() {
		return percentReliability;
	}

	public double getMTBF() {
		return mtbf;
	}

	public void setFailure(int num) {
		numFailures += num;
		computeReliability();
	}
}
