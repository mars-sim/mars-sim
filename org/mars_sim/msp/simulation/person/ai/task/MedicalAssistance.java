/**
 * Mars Simulation Project
 * MedicalHelp.java
 * @version 2.77 2004-09-09
 * @author Barry Evans
 */

package org.mars_sim.msp.simulation.person.ai.task;

import java.io.Serializable;
import java.util.*;
import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.job.Doctor;
import org.mars_sim.msp.simulation.person.medical.HealthProblem;
import org.mars_sim.msp.simulation.person.medical.MedicalAid;
import org.mars_sim.msp.simulation.person.medical.Treatment;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.simulation.structure.building.function.MedicalCare;
import org.mars_sim.msp.simulation.vehicle.Medical;
import org.mars_sim.msp.simulation.vehicle.SickBay;
import org.mars_sim.msp.simulation.vehicle.*;

/**
 * This class represents a task that requires a person to provide medical
 * help to someone else. 
 */
public class MedicalAssistance extends Task implements Serializable {

	private static final double STRESS_MODIFIER = 1D; // The stress modified per millisol.

    private MedicalAid medical;    // The medical station the person is at.
    private double duration;       // How long for treatment
    private HealthProblem problem; // Health problem to treat.

    /** 
     * Constructor
     *
     * @param person the person to perform the task
     */
    public MedicalAssistance(Person person) {
        super("Medical Assistance", person, true, true, STRESS_MODIFIER);
        
        // Sets this task to create historical events.
        setCreateEvents(true);

        // Get a local medical aid that needs work.
        List localAids = getNeedyMedicalAids(person);
        if (localAids.size() > 0) {
            int rand = RandomUtil.getRandomInt(localAids.size() - 1);
            medical = (MedicalAid) localAids.get(rand);
        
            // Get a curable medical problem waiting for treatment at the medical aid.
            problem = (HealthProblem) medical.getProblemsAwaitingTreatment().get(0);

            // Get the person's medical skill.
            int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.MEDICAL);
            
            // Treat medical problem.
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
	        description = "Apply " + treatment.getName();
            duration = treatment.getAdjustedDuration(skill);
            setStressModifier(STRESS_MODIFIER * treatment.getSkill());
            
            // Start the treatment
            try {
                medical.startTreatment(problem, duration);
				// System.out.println(person.getName() + " treating " + problem.getIllness().getName());
                
                // Add person to medical care building if necessary.
				if (medical instanceof MedicalCare) {
        			MedicalCare medicalCare = (MedicalCare) medical;
        			Building building = medicalCare.getBuilding();
					BuildingManager.addPersonToBuilding(person, building);
				}
                
				// Create starting task event if needed.
			    if (getCreateEvents()) {
					TaskEvent startingEvent = new TaskEvent(person, this, TaskEvent.START, "");
					Simulation.instance().getEventManager().registerNewEvent(startingEvent);
				}
            }
            catch (Exception e) {
                System.err.println("MedicalAssistance: " + e.getMessage());
                endTask();
            }
        }
        else endTask();
    }

    /** Returns the weighted probability that a person might perform this task.
     *  It should return a 0 if there is no chance to perform this task given the person and his/her situation.
     *  @param person the person to perform the task
     *  @return the weighted probability that a person might perform this task
     */
    public static double getProbability(Person person) {
        double result = 0D;

        // Get the local medical aids to use.
        if (getNeedyMedicalAids(person).size() > 0) result = 150D;
        
        // Crowding task modifier.
        if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
        	try {
				Building building = getMedicalAidBuilding(person);
				if (building != null) {
					result *= Task.getCrowdingProbabilityModifier(person, building);
					result *= Task.getRelationshipModifier(person, building);
				} 
				else result = 0D;
        	}
        	catch (Exception e) {
        		System.err.println("MedicalAssistance.getProbability(): " + e.getMessage());
        	}
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier if there is a nearby doctor.
		if (isThereADoctorInTheHouse(person)) {
			result *= person.getMind().getJob().getStartTaskProbabilityModifier(MedicalAssistance.class);
		}        

        return result;
    }

    /**
     * Gets the local medical aids that have patients waiting.
     * 
     * @return List of medical aids
     */
    private static List getNeedyMedicalAids(Person person) {
        List result = new ArrayList();
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
        	try {
        		Building building = getMedicalAidBuilding(person);
        		if (building != null) result.add((MedicalCare) building.getFunction(MedicalCare.NAME));
        	}
        	catch (Exception e) {
        		System.err.println("MedicalAssistance.getNeedyMedicalAids(): " + e.getMessage());
        	}
        }
        else if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = person.getVehicle();
            if (vehicle instanceof Medical) {
                MedicalAid aid = ((Medical) vehicle).getSickBay();
                if (isNeedyMedicalAid(aid)) result.add(aid);
            }
        }

        return result;
    }
    
    /**
     * Checks if a medical aid needs work.
     *
     * @return true if medical aid has patients waiting and is not malfunctioning.
     */
    private static boolean isNeedyMedicalAid(MedicalAid aid) {
        boolean waitingProblems = (aid.getProblemsAwaitingTreatment().size() > 0);
        boolean malfunction = getMalfunctionable(aid).getMalfunctionManager().hasMalfunction();
        if (waitingProblems && !malfunction) return true;
        else return false;
    }
    
    /**
     * Gets the malfunctionable associated with the medical aid.
     *
     * @param aid The medical aid
     * @return the associated Malfunctionable
     */
    private static Malfunctionable getMalfunctionable(MedicalAid aid) {
        Malfunctionable result = null;
        
        if (aid instanceof SickBay) result = ((SickBay) aid).getVehicle();
        else if (aid instanceof MedicalCare) result = ((MedicalCare) aid).getBuilding();
        else result = (Malfunctionable) aid;
        
        return result;
    }

    /** 
     * This task simply waits until the set duration of the task is complete, then ends the task.
     * @param time the amount of time to perform this task (in millisols)
     * @return amount of time remaining after finishing with task (in millisols)
     * @throws Exception if error performing task.
     */
    double performTask(double time) throws Exception {
        double timeLeft = super.performTask(time);
        if (subTask != null) return timeLeft;

        // If person is incompacitated, end task.
        if (person.getPerformanceRating() == 0D) endTask();

        // If sickbay owner has malfunction, end task.
        if (getMalfunctionable(medical).getMalfunctionManager().hasMalfunction()) endTask();

        if (isDone()) return timeLeft;

        // Check for accident in infirmary.
        checkForAccident(time);

        timeCompleted += time;
        if (timeCompleted > duration) {
            // Add experience points for 'Medical' skill.
            // Add one point for every 100 millisols.
            double newPoints = (duration / 100D);
            
            // Modify experience by skill level required by treatment.
			newPoints *= problem.getIllness().getRecoveryTreatment().getSkill();
            
            // Modify experience by experience aptitude.
            int experienceAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeManager.EXPERIENCE_APTITUDE);
            newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
            person.getSkillManager().addExperience(Skill.MEDICAL, newPoints);

            problem.startRecovery();
            endTask();
            return timeCompleted - duration;
        }
        else return 0;
    }

    /**
     * Check for accident in infirmary.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        Malfunctionable entity = getMalfunctionable(medical);

        double chance = .001D;

        // Medical skill modification.
        int skill = person.getSkillManager().getEffectiveSkillLevel(Skill.MEDICAL);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // System.out.println(person.getName() + " has accident during medical assistance.");
            entity.getMalfunctionManager().accident();
        }
    }
    
    /**
     * Ends the task and performs any final actions.
     */
    public void endTask() {
        super.endTask();
        
        // Stop treatment.
        try {
            medical.stopTreatment(problem);
        }
        catch (Exception e) {
            // System.out.println("MedicalAssistance.endTask(): " + e.getMessage());
        }
    }
    
    /**
     * Gets the medical aid the person is using for this task.
     *
     * @return medical aid or null.
     */
    public MedicalAid getMedicalAid() {
        return medical;
    }
    
    /**
     * Gets the least crowded medical care building with a patient that needs treatment.
     * @param person the person looking for a medical care building.
     * @return medical care building or null if none found.
     * @throws Exception if person is not in a settlement.
     */
    private static Building getMedicalAidBuilding(Person person) throws Exception {
    	Building result = null;
    	
    	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List medicalBuildings = manager.getBuildings(MedicalCare.NAME);
			
			List needyMedicalBuildings = new ArrayList();
			Iterator i = medicalBuildings.iterator();
			while (i.hasNext()) {
				Building building = (Building) i.next();
				MedicalCare medical = (MedicalCare) building.getFunction(MedicalCare.NAME);
				if (isNeedyMedicalAid(medical)) needyMedicalBuildings.add(building);
			}
			
			List bestMedicalBuildings = BuildingManager.getNonMalfunctioningBuildings(needyMedicalBuildings);
			bestMedicalBuildings = BuildingManager.getLeastCrowdedBuildings(bestMedicalBuildings);
			bestMedicalBuildings = BuildingManager.getBestRelationshipBuildings(person, bestMedicalBuildings);
		
			if (bestMedicalBuildings.size() > 0) result = (Building) bestMedicalBuildings.get(0);
    	}
    	else throw new Exception("MedicalAssistance.getMedicalAidBuilding(): Person is not in settlement.");
    	
    	return result;
    }
    
    /**
     * Checks to see if there is a doctor in the settlement or vehicle the person is in.
     * @param person the person checking.
     * @return true if a doctor nearby.
     */
    private static boolean isThereADoctorInTheHouse(Person person) {
    	boolean result = false;
    	
    	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
    		PersonIterator i = person.getSettlement().getInhabitants().iterator();
    		while (i.hasNext()) {
    			Person inhabitant = i.next();
    			if ((inhabitant != person) && (inhabitant.getMind().getJob()) 
    				instanceof Doctor) result = true;
    		}
    	}
    	else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
    		if (person.getVehicle() instanceof Rover) {
    			Rover rover = (Rover) person.getVehicle();
    			PersonIterator i = rover.getCrew().iterator();
    			while (i.hasNext()) {
    				Person crewmember = i.next(); 
    				if ((crewmember != person) && (crewmember.getMind().getJob() instanceof Doctor))
    					result = true;
    			}
    		}
    	}
    	
    	return result;
    }
    
	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MEDICAL);
	}    
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List getAssociatedSkills() {
		List results = new ArrayList();
		results.add(Skill.MEDICAL);
		return results;
	}
}