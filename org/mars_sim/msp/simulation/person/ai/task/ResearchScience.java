/**
 * Mars Simulation Project
 * ResearchScience.java
 * @version 2.76 2004-06-02
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
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.Research;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The ResearchScience class is an abstract task for scientific research.
 */
public abstract class ResearchScience extends Task implements Serializable {

	// The stress modified per millisol.
	private static final double STRESS_MODIFIER = 0D; 

	// Data members
	private MalfunctionManager malfunctions; // The labs associated malfunction manager.
	private Lab lab;         // The laboratory the person is working in.
	private double duration; // The duration (in millisols) the person will perform this task.
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
	 */
	public ResearchScience(String science, Person person, boolean consumesResources, 
			String resourceType, double resourceRate) {
		// Use task constructor.
		super("Research " + science, person, true, false, STRESS_MODIFIER);
		
		// Initialize local members.
		this.science = science;
		this.consumesResources = consumesResources;
		this.resourceType = resourceType;
		this.resourceRate = resourceRate;
		
		// Get a local lab to research in.
		lab = getLocalLab(person, science, consumesResources, resourceType);
		
		if (lab != null) {
			// Add the person to the lab.
			addPersonToLab();
			
			// Randomly determine duration from 0 - 500 millisols.
			duration = RandomUtil.getRandomDouble(500D);
		}
		else endTask();
	}
	
	/** 
	 * Performs the task for a given amount of time.
	 * @param time amount of time to perform the task (in millisols)
	 * @return amount of time remaining after finishing with task (in millisols)
	 * @throws Exception if error performing task.
	 */
	protected double performTask(double time) throws Exception {
		double timeLeft = super.performTask(time);
		if (subTask != null) return timeLeft;

		// If person is incompacitated, end task.
		if (person.getPerformanceRating() == 0D) endTask();

		// Check for laboratory malfunction.
		if (malfunctions.hasMalfunction()) endTask();

		if (isDone()) return timeLeft;
	
		// Determine effective research time based on the science skill.
		double researchTime = timeLeft;
		int scienceSkill = person.getSkillManager().getEffectiveSkillLevel(science);
		if (scienceSkill == 0) researchTime /= 2D;
		if (scienceSkill > 1) researchTime += researchTime * (.2D * scienceSkill);

		// Remove any used resources.
		if (consumesResources) inv.removeResource(resourceType, researchTime * resourceRate);
	
		// Add experience to science skill.
		// (1 base experience point per 25 millisols of research time spent)
		double experience = researchTime / 25D;
		
		// Experience points adjusted by person's "Academic Aptitude" attribute.
		int academicAptitude = person.getNaturalAttributeManager().getAttribute("Academic Aptitude");
		experience += experience * (((double) academicAptitude - 50D) / 100D);
		person.getSkillManager().addExperience(science, experience);

		// Keep track of the duration of this task.
		timeCompleted += time;
		if (timeCompleted >= duration) endTask();

		// Check for lab accident.
		checkForAccident(time);
	
		return 0D;
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
        
			if (labBuildings.size() > 0) {
				// Pick random lab from list.
				int rand = RandomUtil.getRandomInt(labBuildings.size() - 1);
				Building building = (Building) labBuildings.get(rand);
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
		int skill = person.getSkillManager().getEffectiveSkillLevel(science);
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