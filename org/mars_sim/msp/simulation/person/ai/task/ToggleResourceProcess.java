/**
 * Mars Simulation Project
 * ToggleResourceProcess.java
 * @version 2.78 2007-05-06
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.Airlock;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.Skill;
import org.mars_sim.msp.simulation.person.ai.SkillManager;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.Building;
import org.mars_sim.msp.simulation.structure.building.BuildingManager;
import org.mars_sim.msp.simulation.structure.building.function.LifeSupport;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcess;
import org.mars_sim.msp.simulation.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;

/**
 *
 */
public class ToggleResourceProcess extends EVAOperation implements Serializable {

	// Task phase
	private static final String TOGGLE_PROCESS = "toggle process";
	
	// Data members
	private boolean isEVA; // True if toggling process is EVA operation.
	private Airlock airlock; // Airlock to be used for EVA.
	private ResourceProcess process; // The resource process to toggle.
	private Building building; // The building the resource process is in.
	private boolean toggleOn; // True if process is to be turned on, false if turned off.
	
	/**
	 * Constructor
	 * @param person the person performing the task.
	 * @throws Exception if error constructing the task.
	 */
	public ToggleResourceProcess(Person person) throws Exception {
		super("Turning on resource process", person);
		
		building = getResourceProcessingBuilding(person);
		if (building != null) {
			process = getResourceProcess(building);
			toggleOn = !process.isProcessRunning();
			if (!toggleOn) {
				setName("Turning off resource process");
				setDescription("Turning off resource process");
			}
			isEVA = !building.hasFunction(LifeSupport.NAME);
			
			// If habitable building, add person to building.
			if (!isEVA) BuildingManager.addPersonToBuilding(person, building);
		}
		else endTask();
		
		if (isEVA) {
			// Get an available airlock.
			airlock = getAvailableAirlock(person);
			if (airlock == null) endTask();
		}
		
		addPhase(TOGGLE_PROCESS);
		if (!isEVA) setPhase(TOGGLE_PROCESS);
	}
	
    /** 
     * Gets the weighted probability that a person might perform this task.
     * It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     * @param person the person to perform the task
     * @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
    	double result = 0D;
        
    	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
    		boolean isEVA = false;
    		
    		Settlement settlement = person.getSettlement();
    		
    		try {
    			Building building = getResourceProcessingBuilding(person);
    			if (building != null) {
    				ResourceProcess process = getResourceProcess(building);
    				isEVA = !building.hasFunction(LifeSupport.NAME);
    				double diff = getResourcesValueDiff(settlement, process);
    				double baseProb = diff * 10000D;
    				if (baseProb > 100D) baseProb = 100D;
    				result += baseProb;
    				
    				if (!isEVA) {
    					// Factor in building crowding and relationship factors.
    					result *= Task.getCrowdingProbabilityModifier(person, building);
    					result *= Task.getRelationshipModifier(person, building);
    				}
    			}
    		}
    		catch (Exception e) {
    			e.printStackTrace(System.err);
    		}
    		
    		if (isEVA) {
    			// Check if an airlock is available
    			if (getAvailableAirlock(person) == null) result = 0D;
    			
    			// Check if it is night time.
    			SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
    			if (surface.getSurfaceSunlight(person.getCoordinates()) == 0) {
    				if (!surface.inDarkPolarRegion(person.getCoordinates()))
    					result = 0D;
    			} 
    			
    			// Crowded settlement modifier
    			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
    				if (settlement.getCurrentPopulationNum() > settlement.getPopulationCapacity()) result *= 2D;
    			}
    		}
    		
            // Effort-driven task modifier.
            result *= person.getPerformanceRating();
            
    		// Job modifier.
            Job job = person.getMind().getJob();
    		if (job != null) result *= job.getStartTaskProbabilityModifier(ToggleResourceProcess.class);    
    	}
    	
        return result;
    }
	
    /**
     * Gets the building at a person's settlement with the resource process that needs toggling.
     * @param person the person.
     * @return building with resource process to toggle, or null if none.
     * @throws Exception if error getting building.
     */
	private static Building getResourceProcessingBuilding(Person person) throws Exception {
		Building result = null;
		
		Settlement settlement = person.getSettlement();
		if (settlement != null) {
			BuildingManager manager = settlement.getBuildingManager();
			double bestDiff = 0D;
			Iterator i = manager.getBuildings(ResourceProcessing.NAME).iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				ResourceProcess process = getResourceProcess(building);
				if (process != null) {
					double diff = getResourcesValueDiff(settlement, process);
					if (diff > bestDiff) {
						bestDiff = diff;
						result = building;
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the resource process to toggle at a building.
	 * @param building the building
	 * @return the resource process to toggle or null if none.
	 * @throws Exception if error getting resource process.
	 */
	private static ResourceProcess getResourceProcess(Building building) throws Exception {
		ResourceProcess result = null;
		
		Settlement settlement = building.getBuildingManager().getSettlement();
		if (building.hasFunction(ResourceProcessing.NAME)) {
			double bestDiff = 0D;
			ResourceProcessing processing = (ResourceProcessing) building.getFunction(ResourceProcessing.NAME);
			Iterator i = processing.getProcesses().iterator();
			while (i.hasNext()) {
				ResourceProcess process = (ResourceProcess) i.next();
				double diff = getResourcesValueDiff(settlement, process);
				if (diff > bestDiff) {
					bestDiff = diff;
					result = process;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the resources value diff between inputs and outputs for a resource process.
	 * @param settlement the settlement the resource process is at.
	 * @param process the resource process.
	 * @return the resource value diff (value points)
	 * @throws Exception if error getting value diff.
	 */
	private static double getResourcesValueDiff(Settlement settlement, ResourceProcess process) throws Exception {
		double inputValue = getResourcesValue(settlement, process, true);
		double outputValue = getResourcesValue(settlement, process, false);
		double diff = outputValue - inputValue;
		if (process.isProcessRunning()) diff *= -1D;
		
		// Check if settlement doesn't have one or more of the input resources.
		if (isEmptyInputResourceInProcess(settlement, process)) {
			if (process.isProcessRunning()) diff = 1D;
			else diff = 0D;
		}
		return diff;
	}
	
	/**
	 * Gets the total value of a resource process's input or output.
	 * @param settlement the settlement for the resource process.
	 * @param process the resource process.
	 * @param input is the resource value for the input?
	 * @return the total value for the input or output.
	 */
	private static double getResourcesValue(Settlement settlement, ResourceProcess process, boolean input) {
		double result = 0D;
		
		Iterator i = null;
		if (input) i = process.getInputResources().iterator();
		else i = process.getOutputResources().iterator();
		
		while (i.hasNext()) {
			AmountResource resource = (AmountResource) i.next();
			boolean useResource = true;
			if (input && process.isAmbientInputResource(resource)) useResource = false;
			if (!input && process.isWasteOutputResource(resource)) useResource = false;
			if (useResource) {
				double value = settlement.getGoodsManager().getGoodValuePerMass(GoodsUtil.getResourceGood(resource));
				double rate = 0D;
				if (input) rate = process.getMaxInputResourceRate(resource);
				else rate = process.getMaxOutputResourceRate(resource);
				result += (value * rate);
			}
		}
		
		return result;
	}
	
	/**
	 * Checks if a resource process has no input resources.
	 * @param settlement the settlement the resource is at.
	 * @param process the resource process.
	 * @return true if any input resources are empty.
	 * @throws Exception if error checking input resources.
	 */
	private static boolean isEmptyInputResourceInProcess(Settlement settlement, ResourceProcess process) throws Exception {
		boolean result = false;
		
		Iterator i = process.getInputResources().iterator();
		while (i.hasNext()) {
			AmountResource resource = (AmountResource) i.next();
			if (!process.isAmbientInputResource(resource)) {
				double stored = settlement.getInventory().getAmountResourceStored(resource);
				if (stored == 0D) result = true;
			}
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.mars_sim.msp.simulation.person.ai.task.Task#addExperience(double)
	 */
	@Override
	protected void addExperience(double time) {
		
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		NaturalAttributeManager nManager = person.getNaturalAttributeManager();
		int experienceAptitude = nManager.getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
		double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
		
		if (isEVA) {
			// Add experience to "EVA Operations" skill.
			// (1 base experience point per 100 millisols of time spent)
			double evaExperience = time / 100D;
			evaExperience += evaExperience * experienceAptitudeModifier;
			evaExperience *= getTeachingExperienceModifier();
			person.getMind().getSkillManager().addExperience(Skill.EVA_OPERATIONS, evaExperience);
		}
		
		// If phase is maintenance, add experience to mechanics skill.
		if (TOGGLE_PROCESS.equals(getPhase())) {
			// 1 base experience point per 100 millisols of time spent.
			// Experience points adjusted by person's "Experience Aptitude" attribute.
			double mechanicsExperience = time / 100D;
			mechanicsExperience += mechanicsExperience * experienceAptitudeModifier;
			person.getMind().getSkillManager().addExperience(Skill.MECHANICS, mechanicsExperience);
		}
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.simulation.person.ai.task.Task#getAssociatedSkills()
	 */
	@Override
	public List getAssociatedSkills() {
		List result = new ArrayList();
		result.add(Skill.MECHANICS);
		if (isEVA) result.add(Skill.EVA_OPERATIONS);
		return result;
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.simulation.person.ai.task.Task#getEffectiveSkillLevel()
	 */
	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		int EVAOperationsSkill = manager.getEffectiveSkillLevel(Skill.EVA_OPERATIONS);
		int mechanicsSkill = manager.getEffectiveSkillLevel(Skill.MECHANICS);
		if (isEVA) return (int) Math.round((double)(EVAOperationsSkill + mechanicsSkill) / 2D);
		else return (mechanicsSkill);
	}

	/* (non-Javadoc)
	 * @see org.mars_sim.msp.simulation.person.ai.task.Task#performMappedPhase(double)
	 */
	@Override
	protected double performMappedPhase(double time) throws Exception {
		if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
		if (isEVA) {
			if (EVAOperation.EXIT_AIRLOCK.equals(getPhase())) return exitEVA(time);
	    	if (TOGGLE_PROCESS.equals(getPhase())) return toggleProcessPhase(time);
	    	if (EVAOperation.ENTER_AIRLOCK.equals(getPhase())) return enterEVA(time);
	    	else return time;
		}
		else {
			if (TOGGLE_PROCESS.equals(getPhase())) return toggleProcessPhase(time);
	    	else return time;
		}
	}
	
	/**
	 * Perform the exit airlock phase of the task.
	 * @param time the time to perform this phase (in millisols)
	 * @return the time remaining after performing this phase (in millisols)
	 * @throws Exception if error exiting the airlock.
	 */
	private double exitEVA(double time) throws Exception {
		
    	try {
    		time = exitAirlock(time, airlock);
        
    		// Add experience points
    		addExperience(time);
    	}
		catch (Exception e) {
			// Person unable to exit airlock.
			endTask();
		}
        
		if (exitedAirlock) setPhase(TOGGLE_PROCESS);
		return time;
	}
	
	/**
	 * Perform the enter airlock phase of the task.
	 * @param time amount of time to perform the phase
	 * @return time remaining after performing the phase
	 * @throws Exception if error entering airlock.
	 */
	private double enterEVA(double time) throws Exception {
		time = enterAirlock(time, airlock);
		
        // Add experience points
        addExperience(time);
        
		if (enteredAirlock) endTask();
		return time;
	}	
	
    /**
     * Performs the toggle process phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double toggleProcessPhase(double time) throws Exception {
    	
        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // Check if process has already been completed.
        if (process.isProcessRunning() == toggleOn) endTask();
        
        if (isDone()) return time;
    	
        // Determine effective work time based on "Mechanic" skill.
        double workTime = time;
        int mechanicSkill = getEffectiveSkillLevel();
        if (mechanicSkill == 0) workTime /= 2;
        if (mechanicSkill > 1) workTime += workTime * (.2D * mechanicSkill);

        // Add work to the toggle process.
        process.addToggleWorkTime(workTime);
            
        // Add experience points
        addExperience(time);
            
        // Check if process has already been completed.
        if (process.isProcessRunning() == toggleOn) {
        	endTask();
        	// Settlement settlement = building.getBuildingManager().getSettlement();
        	// String toggle = "off";
        	// if (toggleOn) toggle = "on";
        	// System.out.println(person.getName() + " turning " + toggle + " " + process.getProcessName() + " at " + settlement.getName() + ": " + building.getName());
        }
            
        // Check if an accident happens during toggle process.
        checkForAccident(time);
    	
        return 0D;
    }
    
	/**
	 * Check for accident with entity during toggle resource phase.
	 * @param time the amount of time (in millisols)
	 */
	protected void checkForAccident(double time) {

		// Use EVAOperation checkForAccident() method.
		if (isEVA) super.checkForAccident(time);

		double chance = .001D;

		// Mechanic skill modification.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MECHANICS);
		if (skill <= 3) chance *= (4 - skill);
		else chance /= (skill - 2);

		if (RandomUtil.lessThanRandPercent(chance * time)) building.getMalfunctionManager().accident();
	}
}