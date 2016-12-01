/**
 * Mars Simulation Project
 * PrescribeMedication.java
 * @version 3.07 2015-02-17
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.NaturalAttribute;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.medical.AntiStressMedication;
import org.mars_sim.msp.core.person.medical.Medication;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttribute;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * A task in which a doctor prescribes (and provides) a medication to a patient.
 */
public class PrescribeMedication
extends Task
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(PrescribeMedication.class.getName());

	/** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.prescribeMedication"); //$NON-NLS-1$

	private static final double AVERAGE_MEDICAL_WASTE = .1;
	private static final String TOXIC_WASTE = "toxic waste";
	
    /** Task phases. */
    private static final TaskPhase MEDICATING = new TaskPhase(Msg.getString(
            "Task.phase.medicating")); //$NON-NLS-1$

	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = .2D;

	// Data members.
	private Person patient = null;
	private Medication medication = null;

	/**
	 * Constructor.
	 * @param person the person performing the task.
	 */
	public PrescribeMedication(Person person) {
        // Use task constructor.
        super(NAME, person, true, false, STRESS_MODIFIER, true, 10D);
        
        // Determine patient needing medication.
        patient = determinePatient(person);
        if (patient != null) {
            // Determine medication to prescribe.
            medication = determineMedication(patient);
            
            // If in settlement, move doctor to building patient is in.
            if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                
                // Walk to patient's building.
                walkToRandomLocInBuilding(BuildingManager.getBuilding(patient), false);
            }
            
            logger.info(person.getName() + " is prescribing " + medication.getName()
                    + " to " + patient.getName() + " in " + patient.getBuildingLocation().getNickName() 
                    + " at " + patient.getSettlement());
        }
        else {
            endTask();
        }
        
        // Initialize phase
        addPhase(MEDICATING);
        setPhase(MEDICATING);
    }
    
	public PrescribeMedication(Robot robot) {
        // Use task constructor.
        super(NAME, robot, true, false, STRESS_MODIFIER, true, 10D);
        
        // Determine patient needing medication.
        patient = determinePatient(robot);
        if (patient != null) {
            // Determine medication to prescribe.
            medication = determineMedication(patient);
            
            // If in settlement, move doctor to building patient is in.
            if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
                
                // Walk to patient's building.
                walkToRandomLocInBuilding(BuildingManager.getBuilding(patient), false);
            }
            
            logger.info(robot.getName() + " prescribing " + medication.getName() + 
                    " to " + patient.getName());
        }
        else {
            endTask();
        }
        
        // Initialize phase
        addPhase(MEDICATING);
        setPhase(MEDICATING);
    }
    
    /**
     * Determines if there is a patient nearby needing medication.
     * @param doctor the doctor prescribing the medication.
     * @return patient if one found, null otherwise.
     */
    public static Person determinePatient(Person doctor) {
        Person result = null;
        
        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        LocationSituation loc = doctor.getLocationSituation();
        if (loc == LocationSituation.IN_SETTLEMENT) {
            patientList = doctor.getSettlement().getInhabitants();
        }
        else if (loc == LocationSituation.IN_VEHICLE) {
            Vehicle vehicle = doctor.getVehicle();
            if (vehicle instanceof Crewable) {
                Crewable crewVehicle = (Crewable) vehicle;
                patientList = crewVehicle.getCrew();
            }
        }
        
        // Determine patient.
        if (patientList != null) {
            Iterator<Person> i = patientList.iterator();
            while (i.hasNext() && (result == null)) {
                Person person = i.next();
                PhysicalCondition condition = person.getPhysicalCondition();
                if (!condition.isDead() && (condition.getStress() >= 100D)) {
                    // Only prescribing anti-stress medication at the moment.
                    if (!condition.hasMedication(AntiStressMedication.NAME)) {
                        result = person;
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Person determinePatient(Robot doctor) {
        Person result = null;
        
        // Get possible patient list.
        // Note: Doctor can also prescribe medication for himself.
        Collection<Person> patientList = null;
        LocationSituation loc = doctor.getLocationSituation();
        if (loc == LocationSituation.IN_SETTLEMENT) {
            patientList = doctor.getSettlement().getInhabitants();
        }
        else if (loc == LocationSituation.IN_VEHICLE) {
            Vehicle vehicle = doctor.getVehicle();
            if (vehicle instanceof Crewable) {
                Crewable crewVehicle = (Crewable) vehicle;
                patientList = crewVehicle.getCrew();
            }
        }
        
        // Determine patient.
        if (patientList != null) {
            Iterator<Person> i = patientList.iterator();
            while (i.hasNext() && (result == null)) {
                Person person = i.next();
                PhysicalCondition condition = person.getPhysicalCondition();
                if (!condition.isDead() && (condition.getStress() >= 100D)) {
                    // Only prescribing anti-stress medication at the moment.
                    if (!condition.hasMedication(AntiStressMedication.NAME)) {
                        result = person;
                    }
                }
            }
        }
        
        return result;
    }
    
  
    /**
     * Determines a medication for the patient.
     * @param patient the patient to medicate.
     * @return medication.
     */
    private Medication determineMedication(Person patient) {
        // Only allow anti-stress medication for now.
        return new AntiStressMedication(patient);
    }
    
    /**
     * Performs the medicating phase.
     * @param time the amount of time (millisols) to perform the phase.
     * @return the amount of time (millisols) left over after performing the phase.
     */
    private double medicatingPhase(double time) {
        
        // If duration, provide medication.
        if (getDuration() <= (getTimeCompleted() + time)) {
            if (patient != null) {
                if (medication != null) {
                    PhysicalCondition condition = patient.getPhysicalCondition();
                    
                    // Check if patient already has taken medication.
                    if (!condition.hasMedication(medication.getName())) {
                        // Medicate patient.
                        condition.addMedication(medication);
                        
                        produceMedicalWaste();
                    }
                }
                else throw new IllegalStateException("medication is null");
            }
            else throw new IllegalStateException ("patient is null");
        }
        
        // Add experience.
        addExperience(time);
        
        return 0D;
    }

	
	public void produceMedicalWaste() {
	    Unit containerUnit = null;
		if (person != null) 
		       containerUnit = person.getContainerUnit();
		else if (robot != null)
			containerUnit = robot.getContainerUnit();
        
        if (containerUnit != null) {
            Inventory inv = containerUnit.getInventory();
            storeAnResource(AVERAGE_MEDICAL_WASTE, TOXIC_WASTE, inv);
            //System.out.println("PrescribeMedication.java : adding Toxic Waste : "+ AVERAGE_MEDICAL_WASTE);  
	     }
	}
	
	   
	// 2015-02-06 Added storeAnResource()
	public boolean storeAnResource(double amount, String name, Inventory inv) {
		boolean result = false;
		try {
			AmountResource ar = AmountResource.findAmountResource(name);      
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);
			
			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the amount, set remaining capacity to full
				amount = remainingCapacity;
				result = false;

			}
			else {
				inv.storeAmountResource(ar, amount, true);
				inv.addAmountSupplyAmount(ar, amount);
				result = true;
			}
		} catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
		}
		
		return result;
	}	    
    
    @Override
    protected void addExperience(double time) {
        // Add experience to "Medical" skill
        // (1 base experience point per 10 millisols of work)
        // Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 10D;
        int experienceAptitude = 0;
		if (person != null) 
			experienceAptitude = person.getNaturalAttributeManager().getAttribute(
		            NaturalAttribute.EXPERIENCE_APTITUDE);		       			
		else if (robot != null)
			experienceAptitude = robot.getRoboticAttributeManager().getAttribute(
					RoboticAttribute.EXPERIENCE_APTITUDE);
        
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
        newPoints *= getTeachingExperienceModifier();
		if (person != null) 
			person.getMind().getSkillManager().addExperience(SkillType.MEDICINE, newPoints);			
		else if (robot != null)
			robot.getBotMind().getSkillManager().addExperience(SkillType.MEDICINE, newPoints);
		
    }

    @Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MEDICINE);
        return results;
    }

    @Override
    public int getEffectiveSkillLevel() {
    	SkillManager manager = null;
		if (person != null) 
		    manager = person.getMind().getSkillManager();			
		else if (robot != null)
			manager = robot.getBotMind().getSkillManager();
        
		return manager.getEffectiveSkillLevel(SkillType.MEDICINE);
    }

    @Override
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("Task phase is null");
        }
        else if (MEDICATING.equals(getPhase())) {
            return medicatingPhase(time);
        }
        else {
            return time;
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        patient = null;
        medication = null;
    }
}