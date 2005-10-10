/**
 * Mars Simulation Project
 * ResearchScience.java
 * @version 2.78 2005-07-15
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The ResearchScience class is an abstract task for scientific research.
 */
public abstract class ResearchScience extends Task implements Serializable {
	
	// Task phase.
	private static final String RESEARCHING = "Researching";

	// The stress modified per millisol.
	private static final double STRESS_MODIFIER = -.1D; 

	// Data members
	private MalfunctionManager malfunctions; // The labs associated malfunction manager.
	private Lab lab;         // The laboratory the person is working in.
	private Inventory inv;   // The inventory the lab uses.
	private boolean consumesResources; // Does the research consume resources?
	private String resourceType; // The type of resource the research consumes.
	private double resourceRate; // The rate the research consumes resources.
	private String science;  // The science that is being researched.
	
	/**
	 * Constructor
	 * @param science the science the person is researching.
	 * @param person the person doing the research.
	 * @param consumesResources does the research consume a resource?
	 * @param resourceType the type of resource consumed by the research.
	 * @param resourceRate the rate of resource consumption by the research.
	 * @throws Exception if error constructing task.
	 */
	public ResearchScience(String science, Person person, boolean consumesResources, 
			String resourceType, double resourceRate) throws Exception {
		// Use task constructor.
		super("Research " + science, person, true, false, STRESS_MODIFIER, true, RandomUtil.getRandomDouble(100D));
		
		// Initialize local members.
		this.science = science;
		this.consumesResources = consumesResources;
		this.resourceType = resourceType;
		this.resourceRate = resourceRate;
		
		// Get a local lab to research in.
		lab = getLocalLab(person, science, consumesResources, resourceType);
		
		// Add the person to the lab.
		if (lab != null) addPersonToLab();
		else endTask();
		
		// Initialize phase
		addPhase(RESEARCHING);
		setPhase(RESEARCHING);
	}
	
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) throws Exception {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (RESEARCHING.equals(getPhase())) return researchingPhase(time);
    	else return time;
    }
    
    /**
     * Performs the researching phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double researchingPhase(double time) throws Exception {
    	
		// Check for laboratory malfunction.
		if (malfunctions.hasMalfunction()) endTask();

		if (isDone()) return time;

		// Remove any used resources.
		if (consumesResources) inv.removeResource(resourceType, getEffectiveResearchTime(time) * resourceRate);
		
		// Add experience
		addExperience(time);
		
		// Check for lab accident.
		checkForAccident(time);
	
		return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to relevant science skill
		// (1 base experience point per 25 millisols of effective research time)
		// Experience points adjusted by person's "Academic Aptitude" attribute.
        double newPoints = getEffectiveResearchTime(time) / 25D;
        int academicAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.ACADEMIC_APTITUDE);
        newPoints += newPoints * ((double) academicAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(science, newPoints);
	}
    
    /**
     * Gets the effective research time based on the person's science skill.
     * @param time the real amount of time (millisol) for research.
     * @return the effective amount of time (millisol) for research.
     */
    private double getEffectiveResearchTime(double time) {
		// Determine effective research time based on the science skill.
		double researchTime = time;
		int scienceSkill = getEffectiveSkillLevel();
		if (scienceSkill == 0) researchTime /= 2D;
		if (scienceSkill > 1) researchTime += researchTime * (.2D * scienceSkill);
		
		return researchTime;
    }
	
	/**
	 * Ends the task and performs any final actions.
	 */
	public void endTask() {
		super.endTask();
		
		// Remove person from lab so others can use it.
		try {
			if (lab != null) lab.removeResearcher();
		}
		catch(Exception e) {}
	}
	
	/**
	 * Gets a local lab for a particular science research.
	 * @param person the person checking for the lab.
	 * @param science the science to research.
	 * @param consumesResource does the research consume a resource?
	 * @param resourceType the resource that is consumed (or null)
	 * @return laboratory found or null if none.
	 */
	protected static Lab getLocalLab(Person person, String science, boolean consumesResource, String resourceType) {
		Lab result = null;
		
		// If research consumes a resource, determine if the resource is available.
		if (consumesResource && !hasSufficientResource(person, resourceType)) return null;
		
		String location = person.getLocationSituation();
		if (location.equals(Person.INSETTLEMENT)) result = getSettlementLab(person, science);
		else if (location.equals(Person.INVEHICLE)) result = getVehicleLab(person.getVehicle(), science);
		
		return result;
	}
	
	/**
	 * Checks if a person's location has enough of a given resource.
	 * @param person the person checking.
	 * @param resourceType the type of resource.
	 * @return true if sufficient resource.
	 */
	private static boolean hasSufficientResource(Person person, String resourceType) {
		boolean result = false;
		
		Unit container = person.getContainerUnit();
		if (container != null) {
			if (container.getInventory().getResourceMass(resourceType) > 0) result = true;
		}
		
		return result;
	}
	
	/**
	 * Gets a settlement lab to research a particular science.
	 * @param person the person looking for a lab.
	 * @param science the science to research.
	 * @return a valid research lab.
	 */
	private static Lab getSettlementLab(Person person, String science) {
		Lab result = null;
		
		try {
			BuildingManager manager = person.getSettlement().getBuildingManager();
			List labBuildings = manager.getBuildings(Research.NAME);
			labBuildings = getSettlementLabsWithSpeciality(science, labBuildings);
			labBuildings = BuildingManager.getNonMalfunctioningBuildings(labBuildings);
			labBuildings = getSettlementLabsWithAvailableSpace(labBuildings);
			labBuildings = BuildingManager.getLeastCrowdedBuildings(labBuildings);
			labBuildings = BuildingManager.getBestRelationshipBuildings(person, labBuildings);
        
			if (labBuildings.size() > 0) {
				Building building = (Building) labBuildings.get(0);
				result = (Research) building.getFunction(Research.NAME);
			}
		}
		catch (BuildingException e) {
			System.err.println("ResearchScience.getSettlementLab(): " + e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Gets a list of research buildings with available research space from a list of buildings 
	 * with the research function.
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with available lab space.
	 * @throws BuildingException if building list contains buildings without research function.
	 */
	private static List getSettlementLabsWithAvailableSpace(List buildingList) throws BuildingException {
		List result = new ArrayList();
    	
		Iterator i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			Research lab = (Research) building.getFunction(Research.NAME);
			if (lab.getResearcherNum() < lab.getLaboratorySize()) result.add(building);
		}
    	
		return result;
	}
	
	/**
	 * Gets a list of research buildings with a given science speciality from a list of buildings with
	 * the research function.
	 * @param science the science speciality.
	 * @param buildingList list of buildings with research function.
	 * @return research buildings with science speciality.
	 * @throws BuildingException if building list contains buildings without research function.
	 */
	private static List getSettlementLabsWithSpeciality(String science, List buildingList) throws BuildingException {
		List result = new ArrayList();
		
		Iterator i = buildingList.iterator();
		while (i.hasNext()) {
			Building building = (Building) i.next();
			Research lab = (Research) building.getFunction(Research.NAME);
			if (lab.hasSpeciality(science)) result.add(building);
		}
		
		return result;
	}
	
	/**
	 * Gets an available lab in a vehicle.
	 * Returns null if no lab is currently available.
	 * @param vehicle the vehicle
	 * @param science the science to research.
	 * @return available lab
	 */
	private static Lab getVehicleLab(Vehicle vehicle, String science) {
        
		Lab result = null;
        
		if (vehicle instanceof Rover) {
			Rover rover = (Rover) vehicle;
			if (rover.hasLab()) {
				Lab lab = rover.getLab();
				boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
				boolean speciality = lab.hasSpeciality(science);
				boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
				if (availableSpace && speciality && !malfunction) result = lab;
			}
		}
        
		return result;
	}
	
	/**
	 * Check for accident in laboratory.
	 * @param time the amount of time researching (in millisols)
	 */
	private void checkForAccident(double time) {

		double chance = .001D;

		// Areology skill modification.
		int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(science);
		if (skill <= 3) chance *= (4 - skill);
		else chance /= (skill - 2);

		if (RandomUtil.lessThanRandPercent(chance * time)) {
			// System.out.println(person.getName() + " has a lab accident while doing " + science + " research.");
			if (person.getLocationSituation().equals(Person.INSETTLEMENT)) 
				((Research) lab).getBuilding().getMalfunctionManager().accident();
			else if (person.getLocationSituation().equals(Person.INVEHICLE)) 
				person.getVehicle().getMalfunctionManager().accident(); 
		}
	}
	
	/**
	 * Adds a person to a lab.
	 */
	private void addPersonToLab() {
		
		try {
			String location = person.getLocationSituation();
			if (location.equals(Person.INSETTLEMENT)) {
				Building labBuilding = ((Research) lab).getBuilding();
				BuildingManager.addPersonToBuilding(person, labBuilding);
				lab.addResearcher();
				malfunctions = labBuilding.getMalfunctionManager();
				inv = labBuilding.getInventory();
			}
			else if (location.equals(Person.INVEHICLE)) {
				lab.addResearcher();
				malfunctions = person.getVehicle().getMalfunctionManager();
				inv = person.getVehicle().getInventory();
			}
		}
		catch (Exception e) {
			System.err.println("ResearchScience.addPersonToLab(): " + e.getMessage());
		}
	}
}