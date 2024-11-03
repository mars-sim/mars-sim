/*
 * Mars Simulation Project
 * MineralDeposit.java
 * @date 2022-07-14
 * @author Manny Kung
 */
package com.mars_sim.core.mineral;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;

/**
 * Internal class representing a mineral deposit at a location.
 * It has a coordinate and a collection of minerals at different concentrations.
 */
public class MineralDeposit implements Serializable, SurfacePOI {

	private static final long serialVersionUID = 1L;
	
	private Coordinates location;
	private Map<String,Integer> concentration;

	MineralDeposit(Coordinates location) {
		this.location = location;
		this.concentration = new HashMap<>();
	}

	/**
	 * Where is this mineral concentration located
	 */
	@Override
	public Coordinates getCoordinates() {
		return location;
	}

	/**
	 * Adjust the concentration of a mineral. If it exits then take the average of the old and new.
	 * THis provides a balanced movement.
	 * @param mineral
	 * @param newConc
	 */
	void adjustMineral(String mineral, int newConc) {
		concentration.merge(mineral, newConc, (o,n) -> ((o + n)/2));
	}

	/**
	 * Add to the concentration of a mineral. If it exits then take the sum of the old and new.
	 * @param mineral
	 * @param newConc
	 */
	void addMineral(String mineral, int newConc) {
		var existing = concentration.get(mineral);
		if (existing != null) {
			newConc += existing.intValue();
			if (newConc > 100) {
				newConc = 100;
			}
		}
		concentration.put(mineral, newConc);
	}


	/**
	 * Get the the concentration of a single mineral at this location
	 * @param displayed
	 * @return
	 */
	public int getConcentration(String displayed) {
		return concentration.getOrDefault(displayed, 0);
	}

	/**
	 * Get the concentrations at this location
	 * @return
	 */
    public Map<String, Integer> getConcentrations() {
        return concentration;
    }

	/**
	 * Does this have any minerals at this point
	 * @return
	 */
    public boolean isEmpty() {
        return concentration.isEmpty();
    }	
}
