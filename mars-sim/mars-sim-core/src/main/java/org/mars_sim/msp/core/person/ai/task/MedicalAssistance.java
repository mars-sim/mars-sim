/**
 * Mars Simulation Project
 * MedicalHelp.java
 * @version 3.00 2010-08-10
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.person.medical.Treatment;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.SickBay;
import org.mars_sim.msp.core.vehicle.Vehicle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a task that requires a person to provide medical
 * help to someone else. 
 */
public class MedicalAssistance extends Task implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.person.ai.task.MedicalAssistance";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	private static final String TREATMENT = "Treatment";

	private static final double STRESS_MODIFIER = 1D; // The stress modified per millisol.

    private MedicalAid medical;    // The medical station the person is at.
    private double duration;       // How long for treatment
    private HealthProblem problem; // Health problem to treat.

    /** 
     * Constructor
     * @param person the person to perform the task
     * @throws Exception if error constructing task.
     */
    public MedicalAssistance(Person person) {
        super("Medical Assistance", person, true, true, STRESS_MODIFIER, true, 0D);

        // Get a local medical aid that needs work.
        List<MedicalAid> localAids = getNeedyMedicalAids(person);
        if (localAids.size() > 0) {
            int rand = RandomUtil.getRandomInt(localAids.size() - 1);
            medical = localAids.get(rand);
        
            // Get a curable medical problem waiting for treatment at the medical aid.
            problem = (HealthProblem) medical.getProblemsAwaitingTreatment().get(0);

            // Get the person's medical skill.
            int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MEDICAL);
            
            // Treat medical problem.
            Treatment treatment = problem.getIllness().getRecoveryTreatment();
	        setDescription("Apply " + treatment.getName());
            setDuration(treatment.getAdjustedDuration(skill));
            setStressModifier(STRESS_MODIFIER * treatment.getSkill());
            
            // Start the treatment
            try {
                medical.startTreatment(problem, duration);
				// logger.info(person.getName() + " treating " + problem.getIllness().getName());
                
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
                logger.log(Level.SEVERE,"MedicalAssistance: " + e.getMessage());
                endTask();
            }
        }
        else endTask();
        
        // Initialize phase.
        addPhase(TREATMENT);
        setPhase(TREATMENT);
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
        		logger.log(Level.SEVERE,"MedicalAssistance.getProbability(): " + e.getMessage());
        	}
        }
        
        // Effort-driven task modifier.
        result *= person.getPerformanceRating();
        
		// Job modifier if there is a nearby doctor.
		if (isThereADoctorInTheHouse(person)) {
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartTaskProbabilityModifier(MedicalAssistance.class);
		}        

        return result;
    }
    
    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
    protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (TREATMENT.equals(getPhase())) return treatmentPhase(time);
    	else return time;
    }
    
    /**
     * Performs the treatment phase of the task.
     * @param time the amount of time (millisol) to perform the phase.
     * @return the amount of time (millisol) left over after performing the phase.
     * @throws Exception if error performing the phase.
     */
    private double treatmentPhase(double time) {
    	
        // If sickbay owner has malfunction, end task.
        if (getMalfunctionable(medical).getMalfunctionManager().hasMalfunction()) endTask();

        if (isDone()) return time;

        // Check for accident in infirmary.
        checkForAccident(time);

        if (getDuration() < (getTimeCompleted() + time)) {
            problem.startRecovery();
            endTask();
        }
        
        // Add experience.
        addExperience(time);
        
        return 0D;
    }
    
	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience to "Medical" skill
		// (1 base experience point per 25 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 25D;
        int experienceAptitude = person.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
        person.getMind().getSkillManager().addExperience(Skill.MEDICAL, newPoints);
	}

    /**
     * Gets the local medical aids that have patients waiting.
     * 
     * @return List of medical aids
     */
    private static List<MedicalAid> getNeedyMedicalAids(Person person) {
        List<MedicalAid> result = new ArrayList<MedicalAid>();
        
        String location = person.getLocationSituation();
        if (location.equals(Person.INSETTLEMENT)) {
        	try {
        		Building building = getMedicalAidBuilding(person);
        		if (building != null) result.add((MedicalCare) building.getFunction(MedicalCare.NAME));
        	}
        	catch (Exception e) {
        		logger.log(Level.SEVERE,"MedicalAssistance.getNeedyMedicalAids(): " + e.getMessage());
        	}
        }
        else if (location.equals(Person.INVEHICLE)) {
            Vehicle vehicle = person.getVehicle();
            if (vehicle instanceof Medical) {
                MedicalAid aid = ((Medical) vehicle).getSickBay();
                if ((aid != null) && isNeedyMedicalAid(aid)) result.add(aid);
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
    	if (aid == null) throw new IllegalArgumentException("aid is null");
        boolean waitingProblems = (aid.getProblemsAwaitingTreatment().size() > 0);
        boolean malfunction = getMalfunctionable(aid).getMalfunctionManager().hasMalfunction();
        return waitingProblems && !malfunction;
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
     * Check for accident in infirmary.
     * @param time the amount of time working (in millisols)
     */
    private void checkForAccident(double time) {

        Malfunctionable entity = getMalfunctionable(medical);

        double chance = .001D;

        // Medical skill modification.
        int skill = person.getMind().getSkillManager().getEffectiveSkillLevel(Skill.MEDICAL);
        if (skill <= 3) chance *= (4 - skill);
        else chance /= (skill - 2);

        // Modify based on the entity's wear condition.
        chance *= entity.getMalfunctionManager().getWearConditionAccidentModifier();
        
        if (RandomUtil.lessThanRandPercent(chance * time)) {
            // logger.info(person.getName() + " has accident during medical assistance.");
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
            // logger.info("MedicalAssistance.endTask(): " + e.getMessage());
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
    private static Building getMedicalAidBuilding(Person person) {
    	Building result = null;
    	
    	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
			BuildingManager manager = settlement.getBuildingManager();
			List<Building> medicalBuildings = manager.getBuildings(MedicalCare.NAME);
			
			List<Building> needyMedicalBuildings = new ArrayList<Building>();
			Iterator<Building> i = medicalBuildings.iterator();
			while (i.hasNext()) {
				Building building = i.next();
				MedicalCare medical = (MedicalCare) building.getFunction(MedicalCare.NAME);
				if (isNeedyMedicalAid(medical)) needyMedicalBuildings.add(building);
			}
			
			List<Building> bestMedicalBuildings = BuildingManager.getNonMalfunctioningBuildings(needyMedicalBuildings);
			bestMedicalBuildings = BuildingManager.getLeastCrowdedBuildings(bestMedicalBuildings);
			bestMedicalBuildings = BuildingManager.getBestRelationshipBuildings(person, bestMedicalBuildings);
		
			if (bestMedicalBuildings.size() > 0) result = bestMedicalBuildings.get(0);
    	}
    	else throw new IllegalStateException("MedicalAssistance.getMedicalAidBuilding(): Person is not in settlement.");
    	
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
    		Iterator<Person> i = person.getSettlement().getInhabitants().iterator();
    		while (i.hasNext()) {
    			Person inhabitant = i.next();
    			if ((inhabitant != person) && (inhabitant.getMind().getJob()) 
    				instanceof Doctor) result = true;
    		}
    	}
    	else if (person.getLocationSituation().equals(Person.INVEHICLE)) {
    		if (person.getVehicle() instanceof Rover) {
    			Rover rover = (Rover) person.getVehicle();
    			Iterator<Person> i = rover.getCrew().iterator();
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
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.MEDICAL);
	}    
	
	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> results = new ArrayList<String>(1);
		results.add(Skill.MEDICAL);
		return results;
	}
}