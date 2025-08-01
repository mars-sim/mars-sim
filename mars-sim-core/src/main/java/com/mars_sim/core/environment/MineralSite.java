/*
 * Mars Simulation Project
 * MineralSite.java
 * @date 2025-07-06
 * @author Scott Davis
 */

package com.mars_sim.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.tool.RandomUtil;

/**
 * A class representing an Region of Interest for further exploration. It contains information on
 * estimated mineral concentrations and if it's still minable or not. When reserve runs out, it's no longer minable. 
 * 
 * @Note Later we may further model it in looking for signature of life. 
 */
public class MineralSite implements Serializable, SurfacePOI {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MineralSite.class.getName());
	
	private static final int AVERAGE_RESERVE_MASS = 10_000;
	
	public static final int IMPROVEMENT_THRESHOLD = 1000;

	private static final double MINING_THRESHOLD = 100D;
	
	// Private members.
	private boolean explored;
	private boolean reserved;
	private int numEstimationImprovement;
	private double totalMass;
	private double remainingMass;
	
	private Authority owner;
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
	 */
	MineralSite(Coordinates location, int estimationImprovement,
					Map<String, Double> estimatedMineralConcentrations) {
		this.location = location;
		this.estimatedMineralConcentrations = estimatedMineralConcentrations;
		explored = false;
		reserved = false;
		this.numEstimationImprovement = estimationImprovement;
		
		// Future: Need to find better algorithm to estimate the reserve amount of each mineral 
		double reserve = 0;
		for (var concentration: estimatedMineralConcentrations.values()) {
			reserve += AVERAGE_RESERVE_MASS * concentration * RandomUtil.getRandomDouble(.5, 5);
		}

		totalMass = reserve;
		remainingMass = totalMass;
		
		logger.info(location.getFormattedString() 
			+ " has estimated reserve of " + (int)totalMass + " kg. % Minerals: "
			+  estimatedMineralConcentrations);
	}

	public boolean isEmpty() {
		return remainingMass == 0.0;
	}
	
	/**
	 * Compute the remaining mass after excavation.
	 * 
	 * @param amount
	 * @return
	 */
	public double excavateMass(double amount) {
		if (remainingMass < amount) {
			remainingMass = 0;
			return amount - remainingMass;
		}
		remainingMass -= amount;
		return 0;
	}
	
	/**
	 * Gets the remaining mass.
	 * 
	 * @return
	 */
	public double getRemainingMass() {
		return remainingMass;
	}
	
	/**
	 * Gets the location coordinates.
	 *
	 * @return coordinates.
	 */
	@Override
	public Coordinates getCoordinates() {
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
	 * Get teh estimated amount of each mineral at the site based on the Mass and mineral concentration
	 * @return
	 */
	public Map<Integer,Double> getEstimatedMineralAmounts() {
		return estimatedMineralConcentrations.entrySet().stream()
				.collect(Collectors.toMap(e -> ResourceUtil.findIDbyAmountResourceName(e.getKey()),
									v -> (remainingMass * v.getValue())/100D));
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
				
		for (var aMineral : minerals) {			
			double conc = estimatedMineralConcentrations.get(aMineral);
			
			if (conc > 0) {
				double newCertainty = 0;
				if (degreeCertainty.containsKey(aMineral)) {
					// Existing mineral certainty so increase it	
					double certainty = degreeCertainty.get(aMineral);
					if (certainty < 100) {
						// Improvement is skill based
						double rand = RandomUtil.getRandomDouble(.97, 1.03);
						newCertainty = rand * certainty * (1.03 + skill / 100);
						if (newCertainty > 100) {
							newCertainty = 100;
						}
					}
				}
				else {
					// Improvement is skill based
					newCertainty = RandomUtil.getRandomDouble(1, 5) * (1 + skill);
				}

				// Make a change
				if (newCertainty > 0) {
					logger.info(owner, location.getFormattedString()
								+ " Degree of estimation certainty improved on " 
								+ aMineral + ": " + Math.round(newCertainty * 10.0)/10.0 + " %");
			
					degreeCertainty.put(aMineral, newCertainty);
					return;
				}
			}
		}
	}
	
	/**
	 * Returns the degree of certainty of a mineral.
	 * 
	 * @param mineral
	 * @return
	 */
	public double getDegreeCertainty(String mineral) {
		return degreeCertainty.getOrDefault(mineral, 0D);
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
	 * Checks if the average certainty is above a certain percentage.
	 * 
	 * @return
	 */
	public boolean isCertaintyAverageOver(int percent) {
		if (!degreeCertainty.isEmpty()) {
			double sum = 0;
			double numMinerals = 0;
			for (Entry<String, Double> i : degreeCertainty.entrySet()) {
				numMinerals ++;
				double certainty = i.getValue();
				sum += certainty;
			}
			
			double average = 0; 
			if (numMinerals > 0)
				average = sum / numMinerals;
			if (average > percent) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Increments the estimation improvement.
	 * 
	 * @param delta the Amount to increase the estimates
	 */
	public void incrementNumImprovement(int delta) {
		numEstimationImprovement += delta;
	}

	/**
	 * Checks if this site is minable or not.
	 *
	 * @return true if minable.
	 */
	public boolean isMinable() {
		return remainingMass > MINING_THRESHOLD;
	}

	/**
	 * Sets if this site is claimed or not .
	 *
	 * @param value true if claimed.
	 */
	public void setClaimed(Authority owner) {
		this.owner = owner;
	}

	/**
	 * Checks if this site is claimed or not.
	 *
	 * @return true if claimed.
	 */
	public boolean isClaimed() {
		return owner != null;
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
	 * The owner that explored this site. This may be null if it is 
	 * an unclaimed location.
	 *
	 * @return settlement
	 */
	public Authority getOwner() {
		return owner;
	}
}
