/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 2.84 2008-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.mars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements Serializable, MineralMap {

	private List<MineralConcentration> mineralConcentrations;
	
	/**
	 * Constructor
	 */
	RandomMineralMap() {
		mineralConcentrations = new ArrayList<MineralConcentration>(1000);
		
		determineMineralConcentrations();
	}
	
	private void determineMineralConcentrations() {
		// Determine hematite concentrations.
		int concentrationNumber = 1000;
		for (int x = 0; x < concentrationNumber; x++) {
			double phi = Coordinates.getRandomLatitude();
			double theta = Coordinates.getRandomLongitude();
			Coordinates location = new Coordinates(phi, theta);
			double concentration = RandomUtil.getRandomDouble(100D);
			mineralConcentrations.add(new MineralConcentration(
					location, concentration, MineralMap.HEMATITE));
		}
	}
	
    /**
     * Gets the mineral concentration at a given location.
     * @param mineralType the mineral type (see MineralMap.java)
     * @param location the coordinate location.
     * @return percentage concentration (0 to 100.0)
     */
	public Map<String, Double> getAllMineralConcentration(Coordinates location) {
		Map<String, Double> result = new HashMap<String, Double>();
		double hematiteConcentration = getMineralConcentration(MineralMap.HEMATITE, location);
		result.put(MineralMap.HEMATITE, hematiteConcentration);
		return result;
	}

	/**
     * Gets all of the mineral concentrations at a given location.
     * @param location the coordinate location.
     * @return map of mineral types and percentage concentration (0 to 100.0)
     */
	public double getMineralConcentration(String mineralType,
			Coordinates location) {
		
		double result = 0D;
		
		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
		while (i.hasNext()) {
			MineralConcentration mineralConcentration = i.next();
			if (mineralConcentration.getMineralType().equals(mineralType)) {
				double phiDiff = Math.abs(location.getPhi() - mineralConcentration.getLocation().getPhi());
				double thetaDiff = Math.abs(location.getTheta() - mineralConcentration.getLocation().getTheta());
				double diffLimit = .01D;
				if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
					double distance = location.getDistance(mineralConcentration.getLocation());
					double concentrationRange = mineralConcentration.getConcentration();
					if (distance < concentrationRange) {
						result += (1D - (distance / concentrationRange)) * mineralConcentration.getConcentration();
						if (result > 100D) result = 100D;
					}
				}
			}
		}
		
		return result;
	}
	
	private class MineralConcentration implements Serializable {
		private Coordinates location;
		private double concentration;
		private String mineralType;
		
		private MineralConcentration(Coordinates location, double concentration, 
				String mineralType) {
			this.location = location;
			this.concentration = concentration;
			this.mineralType = mineralType;
		}
		
		private Coordinates getLocation() {
			return location;
		}
		
		private double getConcentration() {
			return concentration;
		}
		
		private String getMineralType() {
			return mineralType;
		}
	}
}