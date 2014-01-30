/**
 * Mars Simulation Project
 * ExploreSite.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.SpecimenContainer;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.MineralMap;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.Rover;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A task for the EVA operation of exploring a site.
 */
public class ExploreSite extends EVAOperation implements Serializable {

	// Task phases
    private static final String WALK_TO_SITE = "Walk to Site";
	private static final String EXPLORING = "Exploring";
	private static final String WALK_TO_ROVER = "Walk to Rover";
	
	// Static members
	private static final double AVERAGE_ROCK_SAMPLES_COLLECTED_SITE = 10D;
	public static final double AVERAGE_ROCK_SAMPLE_MASS = .5D;
	private static final double ESTIMATE_IMPROVEMENT_FACTOR = 5D;
	
	// Data members
	private ExploredLocation site;
	private Rover rover;
	private double exploreXLoc;
    private double exploreYLoc;
    private double enterAirlockXLoc;
    private double enterAirlockYLoc;
	
	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @param site the site to explore.
	 * @param rover the mission rover.
	 * @throws exception if error creating task.
	 */
	public ExploreSite(Person person, ExploredLocation site, Rover rover) {
		
		// Use EVAOperation parent constructor.
		super("Explore Site", person);
		
		// Initialize data members.
		this.site = site;
		this.rover = rover;
		
        // Determine location for field work.
        Point2D exploreLoc = determineExploreLocation();
        exploreXLoc = exploreLoc.getX();
        exploreYLoc = exploreLoc.getY();
        
        // Determine location for reentering rover airlock.
        Point2D enterAirlockLoc = determineRoverAirlockEnteringLocation();
        enterAirlockXLoc = enterAirlockLoc.getX();
        enterAirlockYLoc = enterAirlockLoc.getY();
		
		// Add task phase
        addPhase(WALK_TO_SITE);
		addPhase(EXPLORING);
		addPhase(WALK_TO_ROVER);
	}
	
	/**
     * Determine location to explore.
     * @return field work X and Y location outside rover.
     */
    private Point2D determineExploreLocation() {
        
        Point2D newLocation = null;
        boolean goodLocation = false;
        for (int x = 0; (x < 5) && !goodLocation; x++) {
            for (int y = 0; (y < 10) && !goodLocation; y++) {

                double distance = RandomUtil.getRandomDouble(100D) + (x * 100D) + 50D;
                double radianDirection = RandomUtil.getRandomDouble(Math.PI * 2D);
                double newXLoc = rover.getXLocation() - (distance * Math.sin(radianDirection));
                double newYLoc = rover.getYLocation() + (distance * Math.cos(radianDirection));
                Point2D boundedLocalPoint = new Point2D.Double(newXLoc, newYLoc);

                newLocation = LocalAreaUtil.getLocalRelativeLocation(boundedLocalPoint.getX(), 
                        boundedLocalPoint.getY(), rover);
                goodLocation = LocalAreaUtil.checkLocationCollision(newLocation.getX(), newLocation.getY(), 
                        person.getCoordinates());
            }
        }

        return newLocation;
    }
    
    /**
     * Determine location for returning to rover airlock.
     * @return X and Y location outside rover.
     */
    private Point2D determineRoverAirlockEnteringLocation() {
        
        Point2D vehicleLoc = LocalAreaUtil.getRandomExteriorLocation(rover, 1D);
        Point2D newLocation = LocalAreaUtil.getLocalRelativeLocation(vehicleLoc.getX(), 
                vehicleLoc.getY(), rover);
        
        return newLocation;
    }
	
	/**
	 * Checks if a person can explore a site.
	 * @param person the person
	 * @param rover the rover
	 * @return true if person can explore a site.
	 */
	public static boolean canExploreSite(Person person, Rover rover) {
		// Check if person can exit the rover.
		boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

		// Check if it is night time outside.
		boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
		
		// Check if in dark polar region.
		boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

		// Check if person's medical condition will not allow task.
		boolean medical = person.getPerformanceRating() < .5D;
	
		return (exitable && (sunlight || darkRegion) && !medical);
	}
	
	/**
	 * Perform the exit rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting rover.
	 */
	private double exitRover(double time) {
		
		try {
			time = exitAirlock(time, rover.getAirlock());
		
			// Add experience points
			addExperience(time);
		}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
		
		if (exitedAirlock) {
			// Take container for collecting rock samples if available.
			if (!hasSpecimenContainer()) takeContainer();
			
			setPhase(WALK_TO_SITE);
		}
		return time;
	}
	
	/**
	 * Perform the enter rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error entering rover.
	 */
	private double enterRover(double time) {

		time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);
		
		if (enteredAirlock) {
			Inventory pInv = person.getInventory();
			
			// Load specimen container in rover.
			if (pInv.containsUnitClass(SpecimenContainer.class)) {
				Unit container = pInv.findUnitOfClass(SpecimenContainer.class);
				pInv.retrieveUnit(container);
				rover.getInventory().storeUnit(container);
			}
			else {
				endTask();
				return time;
			}
		}
        
		return 0D;
	}
	
    /**
     * Perform the walk to explore site phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToExploreSitePhase(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // Check if there is reason to cut the EVA walk phase short and return
        // to the rover.
        if (shouldEndEVAOperation()) {
            setPhase(WALK_TO_ROVER);
            return time;
        }
        
        // If not at explore site location, create walk outside subtask.
        if ((person.getXLocation() != exploreXLoc) || (person.getYLocation() != exploreYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    exploreXLoc, exploreYLoc, false);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EXPLORING);
        }
        
        return time;
    }
    
    /**
     * Perform the walk to rover airlock phase.
     * @param time the time available (millisols).
     * @return remaining time after performing phase (millisols).
     */
    private double walkToRoverAirlock(double time) {
        
        // Check for an accident during the EVA walk.
        checkForAccident(time);
        
        // If not at outside rover airlock location, create walk outside subtask.
        if ((person.getXLocation() != enterAirlockXLoc) || (person.getYLocation() != enterAirlockYLoc)) {
            Task walkingTask = new WalkOutside(person, person.getXLocation(), person.getYLocation(), 
                    enterAirlockXLoc, enterAirlockYLoc, true);
            addSubTask(walkingTask);
        }
        else {
            setPhase(EVAOperation.ENTER_AIRLOCK);
        }
        
        return time;
    }
	
	/**
	 * Perform the exploring phase of the task.
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double exploringPhase(double time) {
		
		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		// Check if there is reason to cut the exploring phase short and return
		// to the rover.
		if (shouldEndEVAOperation()) {
			setPhase(WALK_TO_ROVER);
			return time;
		}
		
		// Collect rock samples.
		collectRockSamples(time);
		
		// Improve mineral concentration estimates.
		improveMineralConcentrationEstimates(time);
		
		// TODO: Add other site exploration activities later.
		
		// Add experience points
        addExperience(time);
        
        return 0D;
	}
	
	/**
	 * Collect rock samples if chosen.
	 * @param time the amount of time available (millisols).
	 * @throws Exception if error collecting rock samples.
	 */
	private void collectRockSamples(double time) {
		if (hasSpecimenContainer()) {
			double numSamplesCollected = AVERAGE_ROCK_SAMPLES_COLLECTED_SITE / AVERAGE_ROCK_SAMPLE_MASS;
			double probability = (time / Exploration.EXPLORING_SITE_TIME) * (numSamplesCollected);
			if (RandomUtil.getRandomDouble(1.0D) <= probability) {
				Inventory inv = person.getInventory();
				double rockSampleMass = RandomUtil.getRandomDouble(AVERAGE_ROCK_SAMPLE_MASS * 2D);
				AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
				double rockSampleCapacity = inv.getAmountResourceRemainingCapacity(
						rockSamples, true, false);
				if (rockSampleMass < rockSampleCapacity) 
					inv.storeAmountResource(rockSamples, rockSampleMass, true);
			}
		}
	}
	
	/**
	 * Improve the mineral concentration estimates of an explored site.
	 * @param time the amount of time available (millisols).
	 */
	private void improveMineralConcentrationEstimates(double time) {
		double probability = (time / Exploration.EXPLORING_SITE_TIME) * getEffectiveSkillLevel() * 
		        ESTIMATE_IMPROVEMENT_FACTOR;
		if (RandomUtil.getRandomDouble(1.0D) <= probability) {
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
	
	/**
	 * Checks if the person is carrying a specimen container.
	 * @return true if carrying container.
	 */
	private boolean hasSpecimenContainer() {
		return person.getInventory().containsUnitClass(SpecimenContainer.class);
	}
	
	/**
	 * Takes the least full specimen container from the rover, if any are available.
	 * @throws Exception if error taking container.
	 */
	private void takeContainer() {
		Unit container = findLeastFullContainer(rover);
		if (container != null) {
			if (person.getInventory().canStoreUnit(container, false)) {
				rover.getInventory().retrieveUnit(container);
				person.getInventory().storeUnit(container);
			}
		}
	}
	
	/**
	 * Gets the least full specimen container in the rover.
	 * @param rover the rover with the inventory to look in.
	 * @return specimen container or null if none.
	 */
	private static SpecimenContainer findLeastFullContainer(Rover rover) {
		SpecimenContainer result = null;
		double mostCapacity = 0D;
		
		Iterator<Unit> i = rover.getInventory().findAllUnitsOfClass(SpecimenContainer.class).iterator();
		while (i.hasNext()) {
			SpecimenContainer container = (SpecimenContainer) i.next();
			try {
				AmountResource rockSamples = AmountResource.findAmountResource("rock samples");
				double remainingCapacity = container.getInventory().getAmountResourceRemainingCapacity(
						rockSamples, false, false);
			
				if (remainingCapacity > mostCapacity) {
					result = container;
					mostCapacity = remainingCapacity;
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		
		return result;
	}
	
	@Override
	protected void addExperience(double time) {
		// Add experience to "EVA Operations" skill.
		// (1 base experience point per 100 millisols of time spent)
		double evaExperience = time / 100D;
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		evaExperience += evaExperience * experienceAptitudeModifier;
		evaExperience *= getTeachingExperienceModifier();
		person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		
		// If phase is exploring, add experience to areology skill.
		if (EXPLORING.equals(getPhase())) {
			// 1 base experience point per 10 millisols of exploration time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.AREOLOGY, areologyExperience);
		}
	}

	@Override
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(2);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.AREOLOGY);
		return results;
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(Skill.AREOLOGY);
		return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
	}

	@Override
	protected double performMappedPhase(double time) {
    	if (getPhase() == null) {
    	    throw new IllegalArgumentException("Task phase is null");
    	}
    	else if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) {
    	    return exitRover(time);
    	}
    	else if (WALK_TO_SITE.equals(getPhase())) {
            return walkToExploreSitePhase(time);
        }
    	else if (EXPLORING.equals(getPhase())) {
    	    return exploringPhase(time);
    	}
    	else if (WALK_TO_ROVER.equals(getPhase())) {
            return walkToRoverAirlock(time);
        }
    	else if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) {
    	    return enterRover(time);
    	}
    	else {
    	    return time;
    	}
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    site = null;
	    rover = null;
	}
}