/**
 * Mars Simulation Project
 * EquipmentFactory.java
 * @version 2.79 2006-01-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.equipment;

import org.mars_sim.msp.simulation.Coordinates;

/**
 * A factory for equipment units.
 */
public final class EquipmentFactory {
	
	/**
	 * Private constructor for static factory class.
	 */
	private EquipmentFactory() {}
	
	public static Equipment getEquipment(String type, Coordinates location) throws Exception {
		
		if (Bag.TYPE.equals(type)) return new Bag(location, 100D);
		else if (Barrel.TYPE.equals(type)) return new Barrel(location, 200D);
		else if (EVASuit.TYPE.equals(type)) return new EVASuit(location);
		else if (GasCanister.TYPE.equals(type)) return new GasCanister(location, 50D);
		else if (SpecimenContainer.TYPE.equals(type)) return new SpecimenContainer(location);
		else throw new Exception("Equipment: " + type + " could not be constructed.");
	}
}