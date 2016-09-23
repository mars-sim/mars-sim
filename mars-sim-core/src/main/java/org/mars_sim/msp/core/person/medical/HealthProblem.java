/**
 * Mars Simulation Project
 * HealthProblem.java
 * @version 3.07 2014-11-10
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.medical;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

/**
 * This class represents a Health problem being suffered by a Person.
 * The class references a fixed Complaint that defines the
 * characteristics of this problem.
 */
public class HealthProblem implements Serializable {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = Logger.getLogger(HealthProblem.class.getName());

    private static final int DEGRADING = 0;
    private static final int TREATMENT = 1;
    private static final int RECOVERING = 2;
    private static final int CURED = 3;
    private static final int DEAD = 4;

    private Complaint       illness;        // Illness
    private Person          sufferer;       // Person
    private int             state;          // State of problem
    private double          timePassed;     // Current time of state
    private double          duration;       // Length of the current state
    private MedicalAid      usedAid;        // Any aid being used
    private boolean         requiresBedRest; // Does recovery require bed rest? 

    /**
     * Create a new Health Problem that relates to a single Physical
     * Condition object. It also references a complaint that defines
     * the behaviour. If the Complaint has no degrade period then self-recovery
     * starts immediately.
     *
     * @param complaint Medical complaint being suffered.
     * @param person The Physical condition being effected.
     */
    public HealthProblem(Complaint complaint, Person person) {
        illness = complaint;
        sufferer = person;
        timePassed = 0D;
        setState(DEGRADING);
        duration = illness.getDegradePeriod();
        usedAid = null;
        requiresBedRest = false;
        
        // Create medical event for health problem.
		MedicalEvent newEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_STARTS);
		Simulation.instance().getEventManager().registerNewEvent(newEvent);
        
        logger.finest(person.getName() + " has new health problem : " + complaint.getType().toString());
    }
    
    /**
     * Sets the health problem state.
     * @param newState the new state of the health problem.
     */
    private void setState(int newState) {
    	state = newState;
    	sufferer.fireUnitUpdate(UnitEventType.ILLNESS_EVENT, illness);
		logger.finer(getSufferer().getName() + " " + toString() + " setState(" + getStateString() + ")");
    }
    
    /**
     * Is the problem in a degrading state.
     * @return true if degrading
     */
    public boolean getDegrading() {
    	return (state == DEGRADING);
    }

    /**
     * Has the problem been cured.
     */
    public boolean getCured() {
        return (state == CURED);
    }

    /**
     * Get a rating of the current health situation. This is a percentage value
     * and may either represent the recovering or degradation of the current
     * illness.
     * @return Percentage value.
     */
    public int getHealthRating() {
        if (duration > 0) return (int)((timePassed * 100D) / duration);
        else return 100;
    }

    /**
     * Return the illness that this problem has.
     *
     * @return Complaint defining problem.
     */
    public Complaint getIllness() {
        return illness;
    }

    /**
     * Sufferer of problem
     */
    public Person getSufferer() {
        return sufferer;
    }

    /**
     * The performance rating for this Problem. If there is an aid in used, then
     * the factor is zero otherwise it is the illness rating.
     */
    public double getPerformanceFactor() {
        if (usedAid != null) return 0D;
        return illness.getPerformanceFactor();
    }

    /**
     * Has the problem been cured.
     */
    public boolean getRecovering() {
        return (state == RECOVERING);
    }
    
    /**
     * Checks if the recovery requires bed rest.
     * @return true if requires bed rest.
     */
    public boolean requiresBedRest() {
        return requiresBedRest;
    }
    
    /**
     * Adds time to bed rest recovery.
     * @param bedRestTime the time resting in bed. (millisols)
     */
    public void addBedRestRecoveryTime(double bedRestTime) {
        if ((state == RECOVERING) && requiresBedRest) {
            timePassed += bedRestTime;
            
            // If fully recovered, set health problem as cured.
            if (timePassed >= duration) {
                setCured();
            }
        }
    }

    /**
     * Awaiting treatment
     */
    public boolean getAwaitingTreatment() {
        return (state == DEGRADING);
    }

    /**
     * Generates a situation string that represents the current status of this
     * problem.
     * @return Name of the complaint prefixed by the status.
     */
    public String getSituation() {
    	return toString();
    }
    
    /**
     * Gets a string representing this illness's current state.
     *
     * @return illness state as string
     */
    public String getStateString() {
        switch(state) {
            case DEGRADING: 
                return "degrading";
            case TREATMENT:
                return "treatment";
            case RECOVERING:
                return "recovering";
            case CURED:
                return "cured";
            case DEAD:
                return "dead";
            default:
                return "";
        }
    }

    /**
     * Start the required treatment. It will take the specified
     * duration.
     *
     * @param treatmentLength Length of treatment.
     */
    public void startTreatment(double treatmentLength, MedicalAid medicalAid) {
        usedAid = medicalAid;
        duration = treatmentLength;
        timePassed = 0D;
        setState(TREATMENT);
        
        logger.info("Starting treatment: " + getSufferer().getName() + " - " + toString());
        
        // Create medical event for treatment.
		MedicalEvent treatedEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_TREATED);
		Simulation.instance().getEventManager().registerNewEvent(treatedEvent);
    }
    
    /**
     * Stops the treatment for now.
     */
    public void stopTreatment() {
        if (state == TREATMENT) {
            if (duration > timePassed) {
                startDegrading();
            }
            else {
                startRecovery();
            }
        }
    }
    
    /**
     * Start degrading the health problem.
     */
    private void startDegrading() {
    	setState(DEGRADING);
    	
    	// Create medical event for degrading.
		MedicalEvent degradingEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_DEGRADES);
		Simulation.instance().getEventManager().registerNewEvent(degradingEvent);
    }

    /**
     * This is now moving to a recovery state.
     */
    public void startRecovery() {
        
        if ((state == DEGRADING) || (state == TREATMENT)) {
            // If no recovery period, then it's done.
            duration = illness.getRecoveryPeriod();
            
            // 2016-09-22 Randomized the duration and varied it according to the complaint type
            if (illness.getType() == ComplaintType.COLD
            		|| illness.getType() == ComplaintType.FEVER)
            	duration = duration + duration * RandomUtil.getRandomDouble(.5)
            				- duration * RandomUtil.getRandomDouble(.5);
            else if (illness.getType() == ComplaintType.HEARTBURN)
            	duration = duration + duration * RandomUtil.getRandomDouble(.4)
							- duration * RandomUtil.getRandomDouble(.4);
            else if (illness.getType() == ComplaintType.FLU)
            	duration = duration + duration * RandomUtil.getRandomDouble(.3)
							- duration * RandomUtil.getRandomDouble(.3);
            else
            	duration = duration + duration * RandomUtil.getRandomDouble(.2)
							- duration * RandomUtil.getRandomDouble(.2);
            
            timePassed = 0D;
            if (duration > 0D) {
            	setState(RECOVERING);
            	
				if ((usedAid != null)) {
					if (usedAid.getProblemsBeingTreated().contains(this)) {
						usedAid.stopTreatment(this);
					}
					usedAid = null;
				}
				
				// Check if recovery requires bed rest.
				requiresBedRest = illness.requiresBedRestRecovery();
            	
            	// Create medical event for recovering.
				MedicalEvent recoveringEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_RECOVERY);
				Simulation.instance().getEventManager().registerNewEvent(recoveringEvent);
            } 
            else setCured();
        }
    }
    
    /**
     * Sets the state of the health problem to cured.
     */
    private void setCured() {
    	setState(CURED);
    	
    	// Create medical event for cured.
		MedicalEvent curedEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_CURED);
		Simulation.instance().getEventManager().registerNewEvent(curedEvent);
    }

    /**
     * A time period has expired for this problem.
     *
     * @param time The time period this problem has passed.
     * @param condition Physical condition being effected.
     * @return Return a replacement Medical complaint.
     */
    public Complaint timePassing(double time, PhysicalCondition condition) {
        Complaint result = null;

        if ((state == DEGRADING) && !isEnvironmentalProblem()) {
            // If no required treatment, 
            Treatment treatment = getIllness().getRecoveryTreatment();
            if (treatment == null) {
                startRecovery();
            }
        }
        
        if (!(requiresBedRest && (state == RECOVERING))) {
            timePassed += time;
        }

        if (timePassed >= duration) {

            // Recovering so has the recovery period expired
            if (state == RECOVERING) {
                setCured();

                // If person is cured or treatment person has expired, then
                // release the aid.
                if (usedAid != null) {
                    if (usedAid.getProblemsBeingTreated().contains(this)) {
                        usedAid.stopTreatment(this);
                    }
                    usedAid = null;
                }
            }
            else if (state == DEGRADING) {
                if (duration != 0D) {
                    // Illness has moved to next phase, if null then dead
                    Complaint nextPhase = illness.getNextPhase();
                    if (usedAid != null) {
                        if (usedAid.getProblemsBeingTreated().contains(this)) {
                            usedAid.stopTreatment(this);
                        }
                        usedAid = null;
                    }
                    logger.info(sufferer + " illness " + illness + " degrading to " + nextPhase);

                    if (nextPhase == null) {
                        setState(DEAD);
                        condition.setDead(this);
                    }
                    else result = nextPhase;
                }
            }
        }

        return result;
    }

    /**
     * This method generates a string representation of this problem.
     * It contains the illness and the health rating.
     * @return String description.
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (state == RECOVERING) {
            buffer.append("Recovering from ");
            buffer.append(illness.getType().toString());
        }
        else if (state == TREATMENT) {
            buffer.append("Treatment (");
            Treatment treatment = illness.getRecoveryTreatment();
            if (treatment != null) {
                buffer.append(treatment.getName());
            }
            buffer.append(") ");
            buffer.append(illness.getType());
        }
        else buffer.append(illness.getType());

        buffer.append(' ');
        buffer.append(getHealthRating());
        buffer.append('%');

        return buffer.toString();
    }
    
    /**
     * Checks if this problem is an environmental problem.
     * 
     * @return true if environmental problem.
     */
    public boolean isEnvironmentalProblem() {
        return Simulation.instance().getMedicalManager().isEnvironmentalComplaint(illness);
    }
}