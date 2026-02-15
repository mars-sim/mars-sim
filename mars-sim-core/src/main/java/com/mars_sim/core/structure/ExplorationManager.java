/*
 * Mars Simulation Project
 * ExplorationManager.java
 * @date 2025-07-06
 * @author Barry Evans
 */
package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.environment.MineralSite;
import com.mars_sim.core.environment.SurfaceFeatures;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.mineral.MineralMap;
import com.mars_sim.core.person.ai.mission.Mining;
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
	 * Represents a prospective site
	 */
	private static class Prospect implements Serializable {
		private static final long serialVersionUID = 1L;
		Coordinates locn;
		double distance;
		MineralSite site;

		Prospect(Coordinates locn, double distance) {
			this.locn = locn;
			this.distance = distance;
		}
	}

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

	/** A collection of Prospective locations. */
	private Set<Prospect> interestingLocns = new HashSet<>();

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
		
		var existing = getNearbyMineralLocations();

		var pair = surfaceFeatures.getMineralMap().
				findRandomMineralLocation(base.getCoordinates(), limit + extraKM, existing);
		
		if (pair == null) {
			logger.warning(base, "No nearby mineral locations found within " + Math.round(limit + extraKM) + " km.");
			extraKM = extraKM + 0.3;
			return null;
		}
		
		interestingLocns.add(new Prospect(pair.getKey(), pair.getValue()));
		
		return pair.getKey();
	}
	
    
	/**
	 * Gets the next closest mineral location that is not a declared site.
	 * 
	 * @param limit
	 * @return
	 */
	public Coordinates getNextClosestMineralLoc(double limit) {
		
		acquireNearbyMineralLocation(limit);
		
		var closest = interestingLocns.stream()
					.filter(c -> c.site == null)
					.min(Comparator.comparing(p -> p.distance))
					.orElse(null);
		return (closest != null ? closest.locn : null);
	}
	
	/**
	 * Gets a set of nearby potential mineral locations.
	 */
	public Set<Coordinates> getNearbyMineralLocations() {
		return interestingLocns.stream().map(m -> m.locn).collect(Collectors.toSet());

	}

	/**
	 * Gets one of the existing nearby mineral location that has NOT been put on the declared ROIs list.
	 *
	 * @return
	 */
	public Coordinates getExistingNearbyMineralLocNotFromROIs() {
		var unDeclared = interestingLocns.stream()
							.filter(p -> p.site == null)
							.toList();
		var selected = RandomUtil.getRandomElement(unDeclared);
		
		return (selected != null ? selected.locn : null);
	}
    
	
	/**
	 * Gets a random nearby mineral location that can be reached by any rover.
	 * 
	 * @param closest is selecting to return one of the closest locations
	 * @param limit
	 * @param skill
	 * @return
	 */
	public Coordinates getUnexploredLocalSites(boolean closest, double limit) {
		
		Map<Coordinates, Double> weightedMap = new HashMap<>();
		for (var c : interestingLocns) {
			// If an undeclared location
			if ((c.site != null) && c.site.isExplored()) {
				continue;
			}
			
			double distance = c.distance;
			double prob = 0;
			double delta = limit - distance + 100;
				
			if (closest) {		
				prob = delta * delta / limit / limit;
			}
			else {
				prob = delta / limit;
			}
			
			if (distance >= MineralMap.MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c.locn, prob);
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
	public int numDeclaredROIs(boolean isClaimed) {
		return (int) interestingLocns.stream()
				.filter(i -> (i.site != null) && (i.site.isClaimed() == isClaimed))
				.count();
	}

	/**
	 * Returns list of declared locations of Region Of Interest (ROI).
	 * 
	 * @return
	 */
	public Set<MineralSite> getDeclaredROIs() {	
		return interestingLocns.stream()
					.filter(f -> f.site != null)
					.map(p -> p.site)
					.collect(Collectors.toSet());
	}

    
	/**
	 * Computes the mineral sites statistics.
	 * 
	 * @param status The type of statistics to request
	 * @return
	 */
	public ExploredStats getStatistics(int status) {
		List<Double> list = new ArrayList<>();
		for (var e : interestingLocns) {
			if (e.site != null) {
				switch(status) {
					case SITE_STAT: 
						list.add(e.distance);
						break;
					
					case CLAIMED_STAT:
						if (e.site.isClaimed() && e.site.getOwner().equals(base.getReportingAuthority())) {
							list.add(e.distance);
						}
						break;
					
					case UNCLAIMED_STAT:
						if (!e.site.isClaimed()) {
							list.add(e.distance);
						}
						break;
					
					default:
						break;
				}
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
	public MineralSite createROI(Coordinates siteLocation, int skill) {
		var el = surfaceFeatures.createROI(siteLocation, skill);
		if (el != null) {
			// Record this site locally
			var p = findProspect(siteLocation);

			p.site = el;
		}
		return el;
	}
	
	private Prospect findProspect(Coordinates siteLocation) {
		var p = interestingLocns.stream()
					.filter(s -> s.locn.equals(siteLocation))
					.findAny().orElse(null);
		if (p == null) {
			// Odd as all potential Coordinates shoudl be known
			var distance = base.getCoordinates().getDistance(siteLocation);
			p = new Prospect(siteLocation, distance);
			interestingLocns.add(p);
		}

		return p;
	}

	/**
	 * Checks if there are any mineral locations within rover/mission range.
	 * Note: Called by getTotalMineralValue()
	 *
	 * @param rover          the rover to use.
	 * @return true if mineral locations.
	 * @throws Exception if error determining mineral locations.
	 */
	private Map<Integer, Integer> getNearbyMineral(Rover rover) {
				
		double roverRange = rover.getEstimatedRange();
		double tripTimeLimit = rover.getTotalTripTimeLimit(true);
		double tripRange = getTripTimeRange(tripTimeLimit, rover.getBaseSpeed() / 1.25D);
		double range = roverRange;
		if (tripRange < range)
			range = tripRange;

		Map<Coordinates, Double> weightedMap = new HashMap<>();
		
		for (var c : interestingLocns) {
			double distance = c.distance;
			double prob = 0;
			double delta = range - distance + 100;
			if (delta > 0) {
				prob = delta * delta / range / range;
			}
			
			if (distance >= MineralMap.MIN_DISTANCE && prob > 0) {
				// Fill up the weight map
				weightedMap.put(c.locn, prob);
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
	private static int getTotalMineralValue(Settlement settlement, Map<Integer, Integer> minerals) {

		double result = 0D;

		for (var entry : minerals.entrySet()) {
		    int mineralId = entry.getKey();
		    double concentration = entry.getValue();
			double mineralValue = settlement.getGoodsManager().getGoodValuePoint(mineralId);
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

	/**
	 * Claim an existing site to be owner by this settlement
	 * @param newSite
	 */
    public void claimSite(MineralSite newSite) {
		newSite.setClaimed(base.getReportingAuthority());
		var p = findProspect(newSite.getCoordinates());
		p.site = newSite;
    }	
}
