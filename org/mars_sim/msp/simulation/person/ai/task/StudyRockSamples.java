/**
 * Mars Simulation Project
 * StudyRockSamples.java
 * @version 2.76 2004-05-12
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.Lab;
import org.mars_sim.msp.simulation.Mars;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Resource;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.*;
import org.mars_sim.msp.simulation.vehicle.*;

/** 
 * The StudyRockSamples class is a task for scientific research on 
 * collected rock samples. 
 */
public class StudyRockSamples extends Task implements Serializable {

    // Rate of rock sample research (kg / millisol)
    private static final double RESEARCH_RATE = .01D;
	private static final double STRESS_MODIFIER = 0D; // The stress modified per millisol.
	
    // Data members
    private Inventory inv;   // The inventory containing the rock samples.
    private MalfunctionManager malfunctions; // The labs associated malfunction manager.
    private Lab lab;         // The laboratory the person is working in.
    private double duration; // The duration (in millisols) the person will perform this task.

    /** 
     * Constructor for StudyRockSamples task.  
     * This is an effort driven task.
     * @param person the person to perform the task
     * @param mars the virtual Mars
     */
    public StudyRockSamples(Person person, Mars mars) {
        super("Studying Rock Samples", person, true, false, STRESS_MODIFIER, mars);
        
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	
        	Settlement settlement = person.getSettlement();
        	
			// Check if any rock samples to research.
			Inventory inv = settlement.getInventory();
			if (inv.getResourceMass(Resource.ROCK_SAMPLES) > 0D) {
				try {
					Building labBuilding = getSettlementLab(person);
					if (labBuilding != null) {
						BuildingManager.addPersonToBuilding(person, labBuilding); 
						lab = (Research) labBuilding.getFunction(Research.NAME);
						lab.addResearcher();
						malfunctions = labBuilding.getMalfunctionManager();
						inv = labBuilding.getInventory();
					}
					else endTask();
				}
				catch (Exception e) {
					System.err.println("StudyRockSamples.constructor(): " + e.getMessage());
					endTask();
				}
			}
        }
        else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
        	
        	// Check if any rock samples to research.
        	Inventory inv = person.getVehicle().getInventory();
        	if (inv.getResourceMass(Resource.ROCK_SAMPLES) > 0D) {
        		Vehicle vehicle = person.getVehicle();
        		lab = getVehicleLab(vehicle);
        		if (lab != null) {
        			try {
        				lab.addResearcher();
        				malfunctions = vehicle.getMalfunctionManager();
        				inv = vehicle.getInventory();
        			}
        			catch (Exception e) {
        				endTask();
        			}
        		}
        		else endTask();
        	}
        }
        else endTask();

        // Randomly determine duration from 0 - 500 millisols.
        duration = RandomUtil.getRandomDouble(500D);
    }

    /** Returns the weighted probability that a person might perform this task.
     *  @param person the person to perform the task
     *  @param mars the virtual Mars
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person, Mars mars) {
        double result = 0D;

		String location = person.getLocationSituation();
		if (location.equals(Person.INSETTLEMENT)) {
			Inventory inv = person.getSettlement().getInventory();
			if (inv.getResourceMass(Resource.ROCK_SAMPLES) > 0D) {
				try {
					Building labBuilding = getSettlementLab(person);
					if (labBuilding != null) {
						result = 25D;
					
						// Check for crowding modifier.
						result *= Task.getCrowdingProbabilityModifier(person, labBuilding);
					}
				}
				catch (BuildingException e) {
					System.err.println("StudyRockSamples.getProbability(): " + e.getMessage());
				}
			}
		}
		else if (location.equals(Person.INVEHICLE)) {
			Inventory inv = person.getVehicle().getInventory();
			if (inv.getResourceMass(Resource.ROCK_SAMPLES) > 0D) {
				if (getVehicleLab(person.getVehicle()) != null) result = 25D;
			}
		}
	    
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();

        return result;
    }

    /** 
     * Performs the mechanic task for a given amount of time.
     * @param time amount of time to perform the task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() < 5D) endTask();

        // Check for laboratory malfunction.
        if (malfunctions.hasMalfunction()) endTask();

        if (isDone()) return timeLeft;
	
        // Determine effective research time based on "Areology" skill.
        double researchTime = timeLeft;
        int areologySkill = person.getSkillManager().getEffectiveSkillLevel(Skill.AREOLOGY);
        if (areologySkill == 0) researchTime /= 2D;
        if (areologySkill > 1) researchTime += researchTime * (.2D * areologySkill);

        // Remove studied rock samples.
        double studied = RESEARCH_RATE * researchTime;
        inv.removeResource(Resource.ROCK_SAMPLES, studied);
	
        // Add experience to "Areology" skill.
        // (1 base experience point per 100 millisols of research time spent)
        // Experience points adjusted by person's "Academic Aptitude" attribute.
        double experience = timeLeft / 100D;
        experience += experience *
                (((double) person.getNaturalAttributeManager().getAttribute("Academic Aptitude") - 50D) / 100D);
        person.getSkillManager().addExperience(Skill.AREOLOGY, experience);

        // Keep track of the duration of this task.
        timeCompleted += time;
        if (timeCompleted >= duration) {
            endTask();
            try {
                lab.removeResearcher();
            }
            catch (Exception e) {
                System.out.println("StudyRockSamples.performTask(): " + e.getMessage());
            }
        }

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
     * Check for accident in laboratory.
     * @param time the amount of time researching (in millisols)
     */
    private void checkForAccident(double time) {

        double chance = .001D;

        // Areology skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.AREOLOGY);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has a lab accident while researching rock samples.");
            if (person.getLocationSituation().equals(Person.INSETTLEMENT)) 
                ((Research) lab).getBuilding().getMalfunctionManager().accident();
            else if (person.getLocationSituation().equals(Person.INVEHICLE)) 
                person.getVehicle().getMalfunctionManager().accident(); 
        }
    }
    
    /**
     * Gets an available lab building in a settlement.
     * Returns null if no lab is currently available.
     *
     * @param person the person
     * @return available lab building
   	 * @throws BuildingException if error in finding lab building.
     */
    private static Building getSettlementLab(Person person) throws BuildingException {
        
        Building result = null;
        
        BuildingManager manager = person.getSettlement().getBuildingManager();
        List labBuildings = manager.getBuildings(Research.NAME);
        labBuildings = BuildingManager.getNonMalfunctioningBuildings(labBuildings);
        labBuildings = getSettlementLabsWithAvailableSpace(labBuildings);
        labBuildings = BuildingManager.getLeastCrowdedBuildings(labBuildings);
        
        if (labBuildings.size() > 0) {
            // Pick random lab from list.
            int rand = RandomUtil.getRandomInt(labBuildings.size() - 1);
            result = (Building) labBuildings.get(rand);
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
     * Gets an available lab in a vehicle.
     * Returns null if no lab is currently available.
     *
     * @param vehicle the vehicle
     * @return available lab
     */
    private static Lab getVehicleLab(Vehicle vehicle) {
        
        Lab result = null;
        
        if (vehicle instanceof Rover) {
            Rover rover = (Rover) vehicle;
            if (rover.hasLab()) {
            	Lab lab = rover.getLab();
            	boolean availableSpace = (lab.getResearcherNum() < lab.getLaboratorySize());
            	boolean malfunction = (rover.getMalfunctionManager().hasMalfunction());
            	if (availableSpace && !malfunction) result = lab;
            }
        }
        
        return result;
    }
}