/**
 * Mars Simulation Project
 * CollectResources.java
 * @version 2.84 2008-05-17
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.vehicle.Rover;

/** 
 * The CollectResources class is a task for collecting resources at a site with an EVA from a rover.
 */
public class CollectResources extends EVAOperation implements Serializable {
	
	private static final String COLLECT_RESOURCES = "Collecting Resources";
	
	// Data members
	protected Rover rover; // Rover used.
	protected double collectionRate; // Collection rate for resource (kg/millisol)
	protected double targettedAmount; // Targeted amount of resource to collect at site. (kg)
	protected double startingCargo; // Amount of resource already in rover cargo at start of task. (kg)
	protected AmountResource resourceType; // The resource type 
	protected Class containerType; // The container type to use to collect resource.
	
	/**
	 * Constructor
	 * @param taskName The name of the task.
	 * @param person The person performing the task.
	 * @param rover The rover used in the task.
	 * @param resourceType The resource type to collect.
	 * @param collectionRate The rate (kg/millisol) of collection.
	 * @param targettedAmount The amount (kg) desired to collect.
	 * @param startingCargo The starting amount (kg) of resource in the rover cargo.
	 * @param containerType the type of container to use to collect resource.
	 * @throws Exception if error constructing this task.
	 */
	public CollectResources(String taskName, Person person, Rover rover, AmountResource resourceType, 
			double collectionRate, double targettedAmount, double startingCargo, Class containerType) throws Exception {
		
		// Use EVAOperation parent constructor.
		super(taskName, person);
		
		// Initialize data members.
		this.rover = rover;
		this.collectionRate = collectionRate;
		this.targettedAmount = targettedAmount;
		this.startingCargo = startingCargo;
		this.resourceType = resourceType;
		this.containerType = containerType;
		
		// Add task phase
		addPhase(COLLECT_RESOURCES);
	}
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time the phase is to be performed.
     * @return the remaining time after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitRover(time);
    	if (COLLECT_RESOURCES.equals(getPhase())) return collectResources(time);
    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterRover(time);
    	else return time;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
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
		
		// If phase is collect resource, add experience to areology skill.
		if (COLLECT_RESOURCES.equals(getPhase())) {
			// 1 base experience point per 10 millisols of collection time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double areologyExperience = time / 10D;
			areologyExperience += areologyExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.AREOLOGY, areologyExperience);
		}
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
			// Take container for collecting resource.
			if (!hasContainers()) takeContainer();
			
			if (hasContainers()) setPhase(COLLECT_RESOURCES);
			else {
				setPhase(ENTER_AIRLOCK);
			}
		}
		return time;
	}
	
	/**
	 * Checks if the person is carrying any containers.
	 * @return true if carrying containers.
	 */
	private boolean hasContainers() {
		return person.getInventory().containsUnitClass(containerType);
	}
	
	/**
	 * Takes the least full container from the rover.
	 * @throws Exception if error taking container.
	 */
	private void takeContainer() throws Exception {
		Unit container = findLeastFullContainer(rover.getInventory(), containerType, resourceType);
		if (container != null) {
			if (person.getInventory().canStoreUnit(container)) {
				rover.getInventory().retrieveUnit(container);
				person.getInventory().storeUnit(container);
			}
		}
	}
	
	/**
	 * Gets the least full container in the rover.
	 * @param inv the inventory to look in.
	 * @param containerType the container class to look for.
	 * @param resourceType the resource for capacity.
	 * @return container.
	 */
	private static Unit findLeastFullContainer(Inventory inv, Class containerType, AmountResource resource) {
		Unit result = null;
		double mostCapacity = 0D;
		
		Iterator<Unit> i = inv.findAllUnitsOfClass(containerType).iterator();
		while (i.hasNext()) {
			Unit container = i.next();
			try {
				double remainingCapacity = container.getInventory().getAmountResourceRemainingCapacity(resource, true);
			
				if (remainingCapacity > mostCapacity) {
					result = container;
					mostCapacity = remainingCapacity;
				}
			}
			catch (InventoryException e) {
				e.printStackTrace(System.err);
			}
		}
		
		return result;
	}
	
	/**
	 * Perform the collect resources phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error collecting resources.
	 */
	private double collectResources(double time) throws Exception {

		// Check for an accident during the EVA operation.
		checkForAccident(time);
	    
		// Check if there is reason to cut the collection phase short and return
		// to the rover.
		if (shouldEndEVAOperation()) {
			setPhase(EVAOperation.ENTER_AIRLOCK);
			return time;
		}

		double remainingPersonCapacity = person.getInventory().getAmountResourceRemainingCapacity(resourceType, true);
		double currentSamplesCollected = rover.getInventory().getAmountResourceStored(resourceType) - startingCargo; 
		double remainingSamplesNeeded = targettedAmount - currentSamplesCollected;
		double sampleLimit = remainingPersonCapacity;
		if (remainingSamplesNeeded < remainingPersonCapacity) sampleLimit = remainingSamplesNeeded;

		double samplesCollected = time * collectionRate;

		// Modify collection rate by "Areology" skill.
		int areologySkill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.AREOLOGY);
		if (areologySkill == 0) samplesCollected /= 2D;
		if (areologySkill > 1) samplesCollected += samplesCollected * (.2D * areologySkill);

		// Modify collection rate by polar region if ice collecting.
		if (resourceType.equals(AmountResource.findAmountResource("ice"))) {
			if (Simulation.instance().getMars().getSurfaceFeatures().inPolarRegion(person.getCoordinates()))
				samplesCollected *= 3D;
		}
	
        // Add experience points
        addExperience(time);
		
		// Collect resources.
		if (samplesCollected <= sampleLimit) {
			person.getInventory().storeAmountResource(resourceType, samplesCollected, true);
			return 0D;
		}
		else {
			if (sampleLimit >= 0D) person.getInventory().storeAmountResource(resourceType, sampleLimit, true);
			setPhase(ENTER_AIRLOCK);
			return time - (sampleLimit / collectionRate);
		}
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
			
			if (pInv.containsUnitClass(containerType)) {
				// Load containers in rover.
				Iterator<Unit> i = pInv.findAllUnitsOfClass(containerType).iterator();
				while (i.hasNext()) {
					Unit container = i.next();
					pInv.retrieveUnit(container);
					rover.getInventory().storeUnit(container);
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
	 * Checks if a person can perform an CollectResources task.
	 * @param person the person to perform the task
	 * @param rover the rover the person will EVA from
	 * @param containerType the container class to collect resources in.
	 * @param resourceType the resource to collect.
	 * @return true if person can perform the task.
	 */
	public static boolean canCollectResources(Person person, Rover rover, Class containerType, 
			AmountResource resourceType) {

		// Check if person can exit the rover.
		boolean exitable = ExitAirlock.canExitAirlock(person, rover.getAirlock());

		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();

		// Check if it is night time outside.
		boolean sunlight = surface.getSurfaceSunlight(rover.getCoordinates()) > 0;
		
		// Check if in dark polar region.
		boolean darkRegion = surface.inDarkPolarRegion(rover.getCoordinates());

		// Check if person's medical condition will not allow task.
		boolean medical = person.getPerformanceRating() < .5D;
		
		// Checks if available container with remaining capacity for resource.
		boolean containerAvailable = 
			(findLeastFullContainer(rover.getInventory(), containerType, resourceType) != null);
	
		return (exitable && (sunlight || darkRegion) && !medical && containerAvailable);
	}
	
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int areologySkill = manager.getEffectiveSkillLevel(Skill.AREOLOGY);
		return (int) Math.round((double)(EVAOperationsSkill + areologySkill) / 2D); 
	}
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(2);
		results.add(Skill.EVA_OPERATIONS);
		results.add(Skill.AREOLOGY);
		return results;
	}
}