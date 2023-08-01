/*
 * Mars Simulation Project
 * AmountResourceBin.java
 * @date 2023-07-30
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.building.function.farming.BinEntity;


public class AmountResourceBin extends BaseBin {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private int lastID = 0;

	private Map<Integer, Bin> binMap = new HashMap<>();
	
	public AmountResourceBin(BinEntity entity, double cap) {
		super(entity, cap);
	}
	
	public Map<Integer, Bin> getBinMap() {
		return binMap;
	}
	
	public Bin getBin(int id) {
		if (binMap.containsKey(id)) {
			return binMap.get(id);
		}
		
		return null;
	}
	
	public int addBin(Bin bin) {
		lastID++;
		binMap.put(lastID, bin);
		return lastID;
	}
	
	public int addBin(BinType type, AmountResource ar, double amount) {
		lastID++;
		binMap.put(lastID, new Bin(type, lastID, ar, amount));
		return lastID;
	}
}

