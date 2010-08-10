/**
 * Mars Simulation Project
 * CollectMinedMinerals.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.InventoryException;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Bag;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task for collecting minerals that have been mined at a site.
 */
public class CollectMinedMinerals extends EVAOperation implements Serializable {

	// Task phases
	private static final String COLLECT_MINERALS = "Collecting Minerals";
	
	// Rate of mineral collection (kg/millisol)
	private static final double MINERAL_COLLECTION_RATE = 10D;
	
	// Data members
	private Rover rover; // Rover used.
	protected AmountResource mineralType; 
	
	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @param rover the rover used for the EVA operation.
	 * @param mineralType the type of mineral to collect.
	 * @throws Exception if error creating task.
	 */
	public CollectMinedMinerals(Person person, Rover rover, AmountResource mineralType) 
			throws Exception {
		
		// Use EVAOperation parent constructor.
		super("Collect Minerals", person);
		
		// Initialize data members.
		this.rover = rover;
		this.mineralType = mineralType;
		
		// Add task phase
		addPhase(COLLECT_MINERALS);
	}
	
	/**
	 * Perform the exit rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting rover.
	 */
	private double exitRover(double time) throws Exception {
		
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
			// Take bag for collecting mineral.
			if (!hasBags()) takeBag();
			
			if (hasBags()) setPhase(COLLECT_MINERALS);
			else {
				setPhase(ENTER_AIRLOCK);
			}
		}
		return time;
	}
	
	/**
	 * Checks if the person is carrying any bags.
	 * @return true if carrying bags.
	 */
	private boolean hasBags() {
		return person.getInventory().containsUnitClass(Bag.class);
	}
	
	/**
	 * Takes the most full bag from the rover.
	 * @throws Exception if error taking bag.
	 */
	private void takeBag() throws Exception {
		Bag bag = findMostFullBag(rover.getInventory(), mineralType);
		if (bag != null) {
			if (person.getInventory().canStoreUnit(bag)) {
				rover.getInventory().retrieveUnit(bag);
				person.getInventory().storeUnit(bag);
			}
		}
	}
	
	/**
	 * Gets the most but not completely full bag of the resource in the rover.
	 * @param inv the inventory to look in.
	 * @param resourceType the resource for capacity.
	 * @return container.
	 */
	private static Bag findMostFullBag(Inventory inv, AmountResource resource) {
		Bag result = null;
		double leastCapacity = Double.MAX_VALUE;
		
		Iterator<Unit> i = inv.findAllUnitsOfClass(Bag.class).iterator();
		while (i.hasNext()) {
			Bag bag = (Bag) i.next();
			try {
				double remainingCapacity = bag.getInventory().getAmountResourceRemainingCapacity(resource, true);
			
				if ((remainingCapacity > 0D) && (remainingCapacity < leastCapacity)) {
					result = bag;
					leastCapacity = remainingCapacity;
				}
			}
			catch (InventoryException e) {
				e.printStackTrace(System.err);
			}
		}
		
		return result;
	}
	
	/**
	 * Perform the collect minerals phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting minerals.
	 */
	private double collectMinerals(double time) throws Exception {

		// Check for an accident during the EVA operation.
		checkForAccident(time);
	    
		// Check if there is reason to cut the collection phase short and return
		// to the rover.
		if (shouldEndEVAOperation()) {
			setPhase(EVAOperation.ENTER_AIRLOCK);
			return time;
		}

		Mining mission = (Mining) person.getMind().getMission();
		
		double mineralsExcavated = mission.getMineralExcavationAmount(mineralType);
		double remainingPersonCapacity = 
			person.getInventory().getAmountResourceRemainingCapacity(mineralType, true);

		double mineralsCollected = time * MINERAL_COLLECTION_RATE;

		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.AREOLOGY);
		if (areologySkill == 0) mineralsCollected /= 2D;
		if (areologySkill > 1) mineralsCollected += mineralsCollected * (.2D * areologySkill);
	
		if (mineralsCollected > remainingPersonCapacity) mineralsCollected = remainingPersonCapacity;
		if (mineralsCollected > mineralsExcavated) mineralsCollected = mineralsExcavated;
		
        // Add experience points
        addExperience(time);
		
		// Collect minerals.
        person.getInventory().storeAmountResource(mineralType, mineralsCollected, true);
        mission.collectMineral(mineralType, mineralsCollected);
        if (((mineralsExcavated - mineralsCollected) <= 0D) || 
        		(mineralsCollected >= remainingPersonCapacity)) setPhase(ENTER_AIRLOCK);
        
        return 0D;
	}
	
	/**
	 * Perform the enter rover phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error entering rover.
	 */
	private double enterRover(double time) throws Exception {

		time = enterAirlock(time, rover.getAirlock());

        // Add experience points
        addExperience(time);
		
		if (enteredAirlock) {
			Inventory pInv = person.getInventory();
			
			if (pInv.containsUnitClass(Bag.class)) {
				// Load bags in rover.
				Iterator<Unit> i = pInv.findAllUnitsOfClass(Bag.class).iterator();
				while (i.hasNext()) {
					Bag bag = (Bag) i.next();
					pInv.retrieveUnit(bag);
					rover.getInventory().storeUnit(bag);
				}
			}
			else {
				endTask();
				return time;
			}
		}
        
		return 0D;
	}
	
	/**
	 * Checks if a person can perform a CollectMinedMinerals task.
	 * @param person the person to perform the task
	 * @param rover the rover the person will EVA from
	 * @param mineralType the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectMinerals(Person person, Rover rover, AmountResource mineralType) {

		// Check if person can exit the rover.
		boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

		// Check if it is night time outside.
		boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
		
		// Check if in dark polar region.
		boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

		// Check if person's medical condition will not allow task.
		boolean medical = person.getPerformanceRating() < .5D;
		
		// Checks if available bags with remaining capacity for resource.
		boolean bagsAvailable = (findMostFullBag(rover.getInventory(), mineralType) != null);
	
		return (exitable && (sunlight || darkRegion) && !medical && bagsAvailable);
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
		
		// If phase is collect minerals, add experience to areology skill.
		if (COLLECT_MINERALS.equals(getPhase())) {
			// 1 base experience point per 10 millisols of collection time spent.
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
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitRover(time);
    	if (COLLECT_MINERALS.equals(getPhase())) return collectMinerals(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterRover(time);
    	else return time;
    }
}