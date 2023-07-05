/*
 * Mars Simulation Project
 * ExploredLocation.java
 * @date 2023-06-30
 * @author Scott Davis
 */

package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.structure.Settlement;

/**
 * A class representing an Region of Interest for further exploration. It contains information on
 * estimated mineral concentrations and if it's still minable or not. When reserve runs out, it's no longer minable. 
 * 
 * @Note Later we may further model it in looking for signature of life. 
 */
public class ExploredLocation implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(ExploredLocation.class.getName());
	
	private static final int AVERAGE_RESERVE_MASS = 10_000;
	
	public static final int IMPROVEMENT_THRESHOLD = 1000;
	
	// Private members.
	private boolean minable;
	private boolean claimed;
	private boolean explored;
	private boolean reserved;
	private int numEstimationImprovement;
	private double totalMass;
	private double remainingMass;
	private double distance;
	
	private Settlement settlement;
	private Coordinates location;
	
	private Map<String, Double> estimatedMineralConcentrations;
	
	/**
	 * A map for the degree of certainty in estimating mineral concentration
	 */
	private Map<String, Double> degreeCertainty = new HashMap<>();

	/**
	 * Constructor.
	 *
	 * @param location                       the location coordinates.
	 * @param estimationImprovement			 The number times the estimates have been improved
	 * @param estimatedMineralConcentrations a map of all mineral types and their
	 *                                       estimated concentrations (0% -100%)
	 * @param the                            settlement the exploring mission is
	 *                                       from.
	 */
	ExploredLocation(Coordinates location, int estimationImprovement, Map<String, Double> estimatedMineralConcentrations, Settlement settlement, double distance) {
		this.location = location;
		this.estimatedMineralConcentrations = estimatedMineralConcentrations;
		this.settlement = settlement;
		minable = true;
		explored = false;
		reserved = false;
		this.numEstimationImprovement = estimationImprovement;
		this.distance = distance;
		
		// Future: Need to find better algorithm to estimate the reserve amount of each mineral 
		double reserve = 0;
		for (String s: estimatedMineralConcentrations.keySet()) {
			double concentration = estimatedMineralConcentrations.get(s);
			reserve += AVERAGE_RESERVE_MASS * concentration * RandomUtil.getRandomDouble(.5, 5);
		}

		totalMass = reserve; //RandomUtil.computeGaussianWithLimit(reserve, .5, reserve * .1);
		remainingMass = totalMass;
		
		logger.info(settlement, location.getFormattedString() 
			+ " has estimated reserve of " + (int)totalMass + " kg.  % Minerals: "
			+  estimatedMineralConcentrations);
	}

	public boolean isEmpty() {
		if (remainingMass == 0.0)
			return true;
		
		return false;
	}
	
	public double excavateMass(double amount) {
		if (remainingMass < amount) {
			remainingMass = 0;
			return amount - remainingMass;
		}
		remainingMass -= amount;
		return 0;
	}
	
	public double getRemainingMass() {
		return remainingMass;
	}
	
	/**
	 * Gets the location coordinates.
	 *
	 * @return coordinates.
	 */
	public Coordinates getLocation() {
		return location;
	}

	/**
	 * Gets a map of estimated mineral concentrations at the location.
	 *
	 * @return a map of all mineral types and their estimated concentrations (0%
	 *         -100%)
	 */
	public Map<String, Double> getEstimatedMineralConcentrations() {
		return estimatedMineralConcentrations;
	}

	/**
	 * Gets the number of times the mineral concentration estimation has been
	 * improved.
	 *
	 * @return number of times.
	 */
	public int getNumEstimationImprovement() {
		return numEstimationImprovement;
	}

	/**
	 * Improves the certainty of mineral concentration estimation.
	 * 
	 * @param skill
	 */
	public void improveCertainty(double skill) {
			
		List<String> minerals = new ArrayList<>(estimatedMineralConcentrations.keySet());
		
		Collections.shuffle(minerals);
		
		int size = minerals.size();
		
		for (int i = 0; i < size; i++) {
			// Pick this mineral
			String aMineral = minerals.get(i);
			
			double conc = estimatedMineralConcentrations.get(aMineral);
			
			if (conc <= 0) {
				continue;
			}
	
			if (degreeCertainty != null && !degreeCertainty.isEmpty()
					&& degreeCertainty.containsKey(aMineral)) {
				
				double certainty = degreeCertainty.get(aMineral);
				
				if (certainty >= 100) {
					continue;
				}
				
				// Improvement is skill based
				double rand = RandomUtil.getRandomDouble(.97, 1.03);
				
				double newCertainty = rand * certainty * (1.03 + skill / 100);
				
				if (newCertainty > 100) {
					newCertainty = 100;
				}
				
				logger.info(settlement, 20_00L,
						aMineral + " at " 
						+ location.getFormattedString()
						+ " Degree of certainty improved: " 
						+ Math.round(certainty * 100.0)/100.0 + " %"
						+ " -> "
						+ Math.round(newCertainty * 100.0)/100.0 + " %"
						);
				
				degreeCertainty.put(aMineral, newCertainty);
						
				break;
			}
			
			else {
				degreeCertainty = new HashMap<>();			

				// Improvement is skill based
				double newCertainty = RandomUtil.getRandomDouble(1, 5) * (1 + skill);
				
				logger.info(settlement, location.getFormattedString() + " Degree of estimation certainty improved on " 
						+ aMineral + ": " + Math.round(newCertainty * 10.0)/10.0 + " %");
				
				degreeCertainty.put(aMineral, newCertainty);
				
				break;
			}
		}
	}
	
	/**
	 * Returns the degree of certainty map.
	 * 
	 * @return
	 */
	public Map<String, Double> getDegreeCertainty() {
		return degreeCertainty;
	}
	
	/**
	 * Returns the degree of certainty of a mineral.
	 * 
	 * @param mineral
	 * @return
	 */
	public double getDegreeCertainty(String mineral) {
		if (degreeCertainty != null 
				&& !degreeCertainty.isEmpty()
				&& degreeCertainty.containsKey(mineral)) {
			return degreeCertainty.get(mineral);
		}
		return 0;
	}
	
	/**
	 * Gets the average degree of certainty of all minerals.
	 * 
	 * @return
	 */
	public double getAverageCertainty() {
		double sum = 0;
		int numMinerals = 0;
		for (double percent : degreeCertainty.values()) {
			if (percent > 0) {
				sum += percent;
				numMinerals++;
			}
		}
		if (numMinerals > 0)
			return sum / numMinerals;
		return 0;
	}
	
	/**
	 * Checks if the average certainty is above 50 %.
	 * 
	 * @return
	 */
	public boolean isCertaintyAverageOver50() {
		double sum = 0;
		double numMinerals = 0;
		for (Entry<String, Double> i : degreeCertainty.entrySet()) {
//			String mineral = i.getKey();
			numMinerals ++;
			double certainty = i.getValue();
			sum += certainty;
		}
		
		double average = sum / numMinerals;
		if (average > 50) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Increments the estimation improvement.
	 */
	public void incrementNumImprovement() {
		numEstimationImprovement++;
	}

	/**
	 * Sets if this site is still minable or not .
	 *
	 * @param value true if minable.
	 */
	public void setMinable(boolean value) {
		this.minable = value;
	}

	/**
	 * Checks if this site is minable or not.
	 *
	 * @return true if minable.
	 */
	public boolean isMinable() {
		return minable;
	}

	/**
	 * Sets if this site is claimed or not .
	 *
	 * @param value true if claimed.
	 */
	public void setClaimed(boolean value) {
		this.claimed = value;
	}

	/**
	 * Checks if this site is claimed or not.
	 *
	 * @return true if claimed.
	 */
	public boolean isClaimed() {
		return claimed;
	}
	
	/**
	 * Sets if the location has been explored or not.
	 *
	 * @param explored true if explored.
	 */
	public void setExplored(boolean explored) {
		this.explored = explored;
	}

	/**
	 * Checks if the location has been explored of not.
	 *
	 * @return true if explored.
	 */
	public boolean isExplored() {
		return explored;
	}

	/**
	 * Sets if the location has been staked and reserved by a Mining mission.
	 *
	 * @param reserved true if reserved.
	 */
	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	/**
	 * Checks if the location has been staked and reserved by a Mining mission
	 *
	 * @return true if reserved.
	 */
	public boolean isReserved() {
		return reserved;
	}

	/**
	 * The settlement that explored this site. This may be null if it is 
	 * an unclaimed location.
	 *
	 * @return settlement
	 */
	public Settlement getSettlement() {
		return settlement;
	}


	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		location = null;
		estimatedMineralConcentrations.clear();
		estimatedMineralConcentrations = null;
		settlement = null;
	}
}
