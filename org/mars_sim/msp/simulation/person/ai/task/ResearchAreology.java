/**
 * Mars Simulation Project
 * ResearchAreology.java
 * @version 2.84 2008-06-07
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.mars.MineralMap;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.mission.Exploration;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;

/** 
 * The ResearchAreology class is a task for scientific research in the field of areology.
 */
public class ResearchAreology extends ResearchScience implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.ResearchAreology";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    // Does the researcher have a rock sample to study?
    private boolean hasRockSample;

    /** 
     * Constructor 
     * This is an effort driven task.
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public ResearchAreology(Person person) throws Exception {
    	// Use ResearchScience constructor.
		super(Skill.AREOLOGY, person);
		
		// Check if researcher has a rock sample to study.
		Unit container = person.getContainerUnit();
		if (container != null) {
			AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
			Inventory inv = container.getInventory();
			double totalRockSampleMass = inv.getAmountResourceStored(rockSamples);
			if (totalRockSampleMass > 0D) {
				hasRockSample = true;
				double rockSampleMass = RandomUtil.getRandomDouble(ExploreSite.AVERAGE_ROCK_SAMPLE_MASS * 2D);
				if (rockSampleMass > totalRockSampleMass) rockSampleMass = totalRockSampleMass;
				inv.retrieveAmountResource(rockSamples, rockSampleMass);
			}
		}
    }

    /** 
     * Returns the weighted probability that a person might perform this task.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
		double result = 0D;

		try {
			Lab lab = getLocalLab(person, Skill.AREOLOGY);
			if (lab != null) {
				result = 25D; 
		
				// Check for crowding modifier.
				if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
					try {
						Building labBuilding = ((Research) lab).getBuilding();	
						if (labBuilding != null) {
							result *= Task.getCrowdingProbabilityModifier(person, labBuilding);		
							result *= Task.getRelationshipModifier(person, labBuilding);
						}
						else result = 0D;		
					}
					catch (BuildingException e) {
						logger.log(Level.SEVERE,"StudyRockSamples.getProbability(): " + e.getMessage());
					}
				}
			}
			
			// Check if rock samples are available.
			Unit container = person.getContainerUnit();
			if (container != null) {
				Inventory inv = container.getInventory();
				AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
				if (inv.getAmountResourceStored(rockSamples) > 0D) result*= 10D;
			}
			
			// Check if on exploration mission.
			Mission mission = person.getMind().getMission();
			if ((mission != null) && (mission instanceof Exploration)) result *= 10D;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	    
		// Effort-driven task modifier.
		result *= person.getPerformanceRating();
		
		// Job modifier.
		Job job = person.getMind().getJob();
		if (job != null) result *= job.getStartTaskProbabilityModifier(ResearchAreology.class);		

		return result;
    }
    
    @Override
    protected double researchingPhase(double time) throws Exception {
    	
    	double remainingTime = super.researchingPhase(time);
		
		// Improve mineral concentration estimates.
		improveMineralConcentrationEstimates(time);
    	
    	// Study rock samples if they are available.
    	// Rock sample study double experience and increase chance of 
    	// mineral concentration estimation improvement.
    	if (hasRockSample) {
    		addExperience(time);
    	}
    	
    	return remainingTime;
    }
    
	/**
	 * Improve the mineral concentration estimates of an explored site.
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {
		double probability = (time / 1000D) * getEffectiveSkillLevel();
		if (hasRockSample) probability*= 2D;
		if (RandomUtil.getRandomDouble(1.0D) <= probability) {
			
			// Determine explored site to improve estimations.
			ExploredLocation site = determineExplorationSite();
			if (site != null) {
				MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
				Map<String, Double> estimatedMineralConcentrations = site.getEstimatedMineralConcentrations();
				Iterator<String> i = estimatedMineralConcentrations.keySet().iterator();
				while (i.hasNext()) {
					String mineralType = i.next();
					double actualConcentration = mineralMap.getMineralConcentration(mineralType, site.getLocation());
					double estimatedConcentration = estimatedMineralConcentrations.get(mineralType);
					double estimationDiff = Math.abs(actualConcentration - estimatedConcentration);
					double estimationImprovement = RandomUtil.getRandomDouble(1D * getEffectiveSkillLevel());
					if (estimationImprovement > estimationDiff) estimationImprovement = estimationDiff;
					if (estimatedConcentration < actualConcentration) estimatedConcentration += estimationImprovement;
					else estimatedConcentration -= estimationImprovement;
					estimatedMineralConcentrations.put(mineralType, estimatedConcentration);
				}
			}
		}
	}
	
	/**
	 * Determines an exploration site to improve mineral concentration estimates.
	 * @return exploration site or null if none.
	 */
	private ExploredLocation determineExplorationSite() {
		
		// Try to use an exploration mission site.
		ExploredLocation result = getExplorationMissionSite();
		
		// Try to use a site explored previously by the settlement.
		if (result == null) result = getSettlementExploredSite();
		
		return result;
	}
	
	/**
	 * Gets an exploration site that's been explored by the person's current 
	 * exploration mission (if any).
	 * @return exploration site or null if none.
	 */
	private ExploredLocation getExplorationMissionSite() {
		ExploredLocation result = null;
		
		Mission mission = person.getMind().getMission();
		if ((mission != null) && (mission instanceof Exploration)) {
			Exploration explorationMission = (Exploration) mission;
			List<ExploredLocation> exploredSites = explorationMission.getExploredSites();
			if (exploredSites.size() > 0) {
				int siteIndex = RandomUtil.getRandomInt(exploredSites.size() - 1);
				ExploredLocation location = exploredSites.get(siteIndex);
				if (!location.isMined() && !location.isReserved())
					result = location;
			}
		}
		
		return result;
	}
	
	/**
	 * Gets an exploration site that was previously explored by the person's settlement.
	 * @return exploration site or null if none.
	 */
	private ExploredLocation getSettlementExploredSite() {
		ExploredLocation result = null;
		
		Settlement settlement = person.getAssociatedSettlement();
		if (settlement != null) {
			List<ExploredLocation> settlementExploredLocations = new ArrayList<ExploredLocation>();
			List<ExploredLocation> allExploredLocations = Simulation.instance().getMars().
					getSurfaceFeatures().getExploredLocations();
			Iterator<ExploredLocation> i = allExploredLocations.iterator();
			while (i.hasNext()) {
				ExploredLocation location = i.next();
				if (settlement.equals(location.getSettlement()) && !location.isMined() && 
						!location.isReserved())
					settlementExploredLocations.add(location);
			}
			
			if (settlementExploredLocations.size() > 0) {
				int siteIndex = RandomUtil.getRandomInt(settlementExploredLocations.size() - 1);
				result = settlementExploredLocations.get(siteIndex);
			}
		}
		
		return result;
	}
}