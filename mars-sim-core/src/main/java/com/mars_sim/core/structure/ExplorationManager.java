/*
 * Mars Simulation Project
 * ExplorationManager.java
 * @date 2024-12-07
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.environment.ExploredLocation;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mineral.MineralMap;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.resource.ResourceUtil;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * This class managers Exploration sites that are local to a Settlement.
 * It interacts with the central SurfaceFeatures.
 */
public class ExplorationManager implements Serializable {
	
    private static final long serialVersionUID = 1L;

	/**
     * This is the statistics output for a category of explored locations.
     */
    public record ExploredStats(double mean, double sd) {}

    private static SurfaceFeatures surfaceFeatures;

    /** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(ExplorationManager.class.getName());

    private static final double AVERAGE_SITE_TIME = 250D;
    private static final int AVERAGE_SITE_VISITS = 3;

    public static final int CLAIMED_STAT = 0;
    public static final int UNCLAIMED_STAT = 1;
    public static final int SITE_STAT = 2;

	/** A set of nearby mineral locations. */
	private Map<Coordinates, Double> nearbyMineralLocations = new HashMap<>();
	/** A list of nearby mineral locations. */
	private Set<ExploredLocation> declaredMineralLocations = new HashSet<>();

    private Settlement base;
    
    /** The extra search radius [in km] to be added. */
	private double extraKM = 0D;

    ExplorationManager(Settlement base) {
        this.base = base;
    }

    /**
	 * Adds a nearby mineral location in random.
	 * 
	 * @param limit
	 * @param sol
	 */
	public Coordinates acquireNearbyMineralLocation(double limit) {
		
		var pair = surfaceFeatures.getMineralMap().
				findRandomMineralLocation(base.getCoordinates(), limit + extraKM, nearbyMineralLocations.keySet());
		
		if (pair == null) {
			logger.warning(base, "No nearby mineral locations found within " + Math.round(limit + extraKM) + " km.");
			extraKM = extraKM + 0.3;
			return null;
		}
		
		nearbyMineralLocations.put(pair.getKey(), pair.getValue());
		
		return pair.getKey();
	}
	
    
	/**
	 * Gets the next closest mineral location.
	 * 
	 * @param limit
	 * @return
	 */
	public Coordinates getNextClosestMineralLoc(double limit) {
		
		acquireNearbyMineralLocation(limit);
				
		double shortestDist = 1000;
		Coordinates chosen = null;

		for(var c : nearbyMineralLocations.entrySet()) {
			double dist = c.getValue();
			if ((surfaceFeatures.isDeclaredLocation(c.getKey()) == null)
			    && (shortestDist >= dist)) {
				shortestDist = dist;
				chosen = c.getKey();
			}
		}	
		
		return chosen;
	}
	
	/**
	 * Gets a set of nearby potential mineral locations.
	 */
	public Set<Coordinates> getNearbyMineralLocations() {
		return nearbyMineralLocations.keySet();
	}

	/**
	 * Gets one of the existing nearby mineral location that has not been declared yet.
	 *
	 * @return
	 */
	public Coordinates getExistingNearbyMineralLocation() {

		for (Coordinates c : nearbyMineralLocations.keySet()) {
			for (ExploredLocation el : declaredMineralLocations) {
				if (!c.equals(el.getLocation())) {
					return c;
				}
			}
		}
		
		return null;
	}
    
	
	/**
	 * Gets a random nearby mineral location that can be reached by any rover.
	 * 
	 * @param closest is selecting to return one of the closest locations
	 * @param limit
	 * @param skill
	 * @return
	 */
	public Coordinates getUnexploredDeclaredSite(boolean closest, double limit) {
		
		double newRange = limit;
		
		if (limit == -1) {		
			return getNextClosestMineralLoc(limit);
		}
		
		logger.info(base, "nearbyMineralLocations: " + nearbyMineralLocations.size());
		
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		for (var c : nearbyMineralLocations.entrySet()) {
			// If an undeclared location
			if (declaredMineralLocations.stream().noneMatch(e -> e.getCoordinates().equals(c.getKey()))) {
				double distance = c.getValue();
				double prob = 0;
				double delta = newRange - distance + 100;
				if (delta > 0) {
					continue;
				}
					
				if (closest) {		
					prob = delta * delta / newRange / newRange;
				}
				else {
					prob = delta / newRange;
				}
				
				if (distance >= MineralMap.MIN_DISTANCE && prob > 0) {
					// Fill up the weight map
					weightedMap.put(c.getKey(), prob);
				}
			}
		}

		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			logger.info(base, 30_000, "No ROIs found within " + Math.round(limit + extraKM) + " km.");
			extraKM = extraKM + 0.3;
			return null;
		}
		
		return chosen;
	}

	/**
	 * Returns number of locations being claimed or not being claimed as a Region Of Interest (ROI).
	 * 
	 * @param isClaimed
	 * @return
	 */
	public int numDeclaredLocation(boolean isClaimed) {	
		int num = 0;
		Settlement match = (isClaimed ? base : null);
		for (Coordinates c: nearbyMineralLocations.keySet()) {
			if (surfaceFeatures.isDeclaredARegionOfInterest(c, match))
				num++;
		}
		return num;
	}

	/**
	 * Returns list of declared locations of Region Of Interest (ROI).
	 * 
	 * @return
	 */
	public Set<ExploredLocation> getDeclaredLocations() {	
		return declaredMineralLocations;
	}

    
	/**
	 * Computes the mineral sites statistics.
	 * 
	 * @param status The type of statistics to requet
	 * @return
	 */
	public ExploredStats getStatistics(int status) {
		List<Double> list = new ArrayList<>();
		for (var e : nearbyMineralLocations.entrySet()) {
			Coordinates c = e.getKey();	
            var locn = e.getValue();

            switch(status) {
                case SITE_STAT: 
                    list.add(locn);
                    break;
                
                case CLAIMED_STAT:
                    if (surfaceFeatures.isDeclaredARegionOfInterest(c, base)) {
                        list.add(locn);
                    }
                    break;
                
                case UNCLAIMED_STAT:
                    if (surfaceFeatures.isDeclaredARegionOfInterest(c, null)) {
                        list.add(locn);
                    }
                    break;
                
                default:
                    break;
            }	
		}
		
	    double mean = 0.0;
	    double sd = 0.0;
	    
        double sum = list.stream().mapToDouble(Double::doubleValue).sum();
        int size = list.size();
		if (size != 0) {
			mean = sum / size;
 
            final double m = mean;
            double total = list.stream()
                        .mapToDouble(d -> Math.pow((d - m), 2))
                        .sum();
	        var variance = total / size;
	        sd = Math.sqrt(variance);
		}
		
        return new ExploredStats(mean, sd);
	}
	
	/**
	 * Creates a Region of Interest (ROI) at a given location and
	 * estimate its mineral concentrations.
	 * 
	 * @param siteLocation
	 * @param skill
	 * @return ExploredLocation
	 */
	public ExploredLocation createARegionOfInterest(Coordinates siteLocation, int skill) {
		ExploredLocation el = surfaceFeatures.createARegionOfInterest(siteLocation, skill);
		if (el != null) {
			declaredMineralLocations.add(el);
			return el;
		}
		return null;
	}
	
	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 * Note: Called by getTotalMineralValue()
	 *
	 * @param rover          the rover to use.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	private Map<String, Integer> getNearbyMineral(Rover rover) {
				
		double roverRange = rover.getEstimatedRange();
		double tripTimeLimit = rover.getTotalTripTimeLimit(true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (var c : nearbyMineralLocations.entrySet()) {
			double distance = c.getValue();
			double prob = 0;
			double delta = range - distance + 100;
			if (delta > 0) {
				prob = delta * delta / range / range;
			}
			
			if (distance >= MineralMap.MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c.getKey(), prob);
			}
		}
		
		// Choose one with weighted randomness 
		Coordinates chosen = RandomUtil.getWeightedRandomObject(weightedMap);

		if (weightedMap.isEmpty() || chosen == null) {
			logger.info(base, 30_000, "No mineral site of interest found within " + Math.round(range + extraKM) + " km.");
			return Collections.emptyMap();
		}
		
		return surfaceFeatures.getMineralMap().getAllMineralConcentrations(chosen);
	}
	
	
	/**
	 * Gets the total mineral value, based on the range of a given rover.
	 * 
	 * @param rover
	 * @return
	 */
	public int getTotalMineralValue(Rover rover) {
        // Check if any mineral locations within rover range and obtain their
        // concentration
        var minerals = getNearbyMineral(rover);
        if (!minerals.isEmpty()) {
            return getTotalMineralValue(base, minerals);
        }
	
		return 0;
	}

    /**
	 * Gets the estimated total mineral value of a mining site.
	 *
	 * @param site       the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	private static int getTotalMineralValue(Settlement settlement, Map<String, Integer> minerals) {

		double result = 0D;

		for (var entry : minerals.entrySet()) {
		    String mineralType = entry.getKey();
		    double concentration = entry.getValue();
			int mineralResource = ResourceUtil.findIDbyAmountResourceName(mineralType);
			double mineralValue = settlement.getGoodsManager().getGoodValuePoint(mineralResource);
			double mineralAmount = (concentration / 100) * Mining.MINERAL_GOOD_VALUE_FACTOR;
			result += mineralValue * mineralAmount;
		}
		
		result = Math.round(result * 100.0)/100.0;
		
		return (int)result;
	}
    	
	public static void initialise(SurfaceFeatures sf) {
		surfaceFeatures = sf;
	}

	/**
	 * Gets the range of a trip based on its time limit and exploration sites.
	 *
	 * @param tripTimeLimit time (millisols) limit of trip.
	 * @param averageSpeed  the average speed of the vehicle.
	 * @return range (km) limit.
	 */
	private static double getTripTimeRange(double tripTimeLimit, double averageSpeed) {
		
		double tripTimeTravellingLimit = tripTimeLimit - (AVERAGE_SITE_VISITS * AVERAGE_SITE_TIME);
		double millisolsInHour = MarsTime.convertSecondsToMillisols(60D * 60D);
		double averageSpeedMillisol = averageSpeed / millisolsInHour;
		return tripTimeTravellingLimit * averageSpeedMillisol;
	}
	
}
