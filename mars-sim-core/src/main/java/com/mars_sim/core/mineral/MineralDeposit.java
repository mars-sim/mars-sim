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
	private Map<Integer,Integer> concentration;

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
	 * @param mineralId Resource id of the mineral
	 * @param newConc New concentration to adjust to
	 */
	void adjustMineral(int mineralId, int newConc) {
		concentration.merge(mineralId, newConc, (o,n) -> ((o + n)/2));
	}

	/**
	 * Add to the concentration of a mineral. If it exits then take the sum of the old and new.
	 * @param mineralId Resource id of the mineral
	 * @param newConc New concentration to add
	 */
	void addMineral(int mineralId, int newConc) {
		var existing = concentration.get(mineralId);
		if (existing != null) {
			newConc += existing.intValue();
			if (newConc > 100) {
				newConc = 100;
			}
		}
		concentration.put(mineralId, newConc);
	}


	/**
	 * Get the the concentration of a single mineral at this location
	 * @param mineralId Resource id of the mineral
	 * @return
	 */
	public int getConcentration(int mineralId) {
		return concentration.getOrDefault(mineralId, 0);
	}

	/**
	 * Get the concentrations at this location
	 * @return
	 */
    public Map<Integer, Integer> getConcentrations() {
        return concentration;
    }
}
