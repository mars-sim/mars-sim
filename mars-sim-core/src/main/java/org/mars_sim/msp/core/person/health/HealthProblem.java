/**
 * Mars Simulation Project
 * HealthProblem.java
 * @version 3.2.0 2021-06-20
 * @author Barry Evans
 */

package org.mars_sim.msp.core.person.health;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * This class represents a Health problem being suffered by a Person. The class
 * references a fixed Complaint that defines the characteristics of this
 * problem.
 */
public class HealthProblem implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(HealthProblem.class.getName());

	private static final int DEGRADING = 0;
	private static final int BEING_TREATED = 1;
	private static final int RECOVERING = 2;
	private static final int CURED = 3;
	public static final int DEAD = 4;

	private int state; // State of problem

	private double timePassed; // Current time of state
	private double duration; // Length of the current state

	private boolean requiresBedRest; // Does recovery require bed rest?

	private ComplaintType type;
	private Person sufferer; // Person
	private MedicalAid usedAid; // Any aid being used

	private transient Complaint complaint;
	private static MedicalManager medicalManager = Simulation.instance().getMedicalManager();
	private static HistoricalEventManager eventManager = Simulation.instance().getEventManager();
	
	/**
	 * Create a new Health Problem that relates to a single Physical Condition
	 * object. It also references a complaint that defines the behaviour. If the
	 * Complaint has no degrade period then self-recovery starts immediately.
	 *
	 * @param complainttype Medical complaint enum being suffered.
	 * @param person    The Physical condition being effected.
	 */
	public HealthProblem(ComplaintType complaintType, Person person) {
		type = complaintType;
		sufferer = person;
		timePassed = 0D;
		setState(DEGRADING);
		complaint = medicalManager.getComplaintByName(complaintType);
		duration = complaint.getDegradePeriod();
		usedAid = null;
		requiresBedRest = false;

		// Create medical event for health problem.
		MedicalEvent newEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_STARTS);
		eventManager.registerNewEvent(newEvent);

		logger.fine(person, " had a new health problem of " 
				+ complaintType.toString().toLowerCase());
	}

	/**
	 * Sets the health problem state.
	 * 
	 * @param newState the new state of the health problem.
	 */
	public void setState(int newState) {
		state = newState;
		sufferer.fireUnitUpdate(UnitEventType.ILLNESS_EVENT, medicalManager.getComplaintByName(type));
		logger.fine(getSufferer(), toString() + " setState(" + getStateString() + ")");
	}

	/**
	 * Is the problem in a degrading state.
	 * 
	 * @return true if degrading
	 */
	public boolean isDegrading() {
		return (state == DEGRADING);
	}

	/**
	 * Has the problem been cured.
	 */
	public boolean isCured() {
		return (state == CURED);
	}

	/**
	 * Get a rating of the current health situation. This is a percentage value and
	 * may either represent the recovering or degradation of the current illness.
	 * 
	 * @return Percentage value.
	 */
	public int getHealthRating() {
		int d = (int) ((timePassed * 100D) / duration);
		if (d > 100)
			d = 100;
		if (duration > 0)
			return d;
		else
			return 100;
	}

	/**
	 * Return the illness that this problem has.
	 *
	 * @return Complaint defining problem.
	 */
	public Complaint getIllness() {
		if (complaint == null) {
			complaint = medicalManager.getComplaintByName(type);
		}
		return complaint;
	}

	/**
	 * Return the illness that this problem has.
	 *
	 * @return Complaint defining problem.
	 */
	public ComplaintType getType() {
		return type;
	}
	
	/**
	 * Sufferer of problem
	 */
	public Person getSufferer() {
		return sufferer;
	}

	/**
	 * The performance rating for this Problem. If there is an aid in used, then the
	 * factor is zero otherwise it is the illness rating.
	 */
	public double getPerformanceFactor() {
		if (usedAid != null)
			return 0D;
		return medicalManager.getComplaintByName(type).getPerformanceFactor();
	}

	/**
	 * Has the problem been cured.
	 */
	public boolean getRecovering() {
		return (state == RECOVERING);
	}

	/**
	 * Checks if the recovery requires bed rest.
	 * 
	 * @return true if requires bed rest.
	 */
	public boolean requiresBedRest() {
		return requiresBedRest;
	}

	/**
	 * Adds time to bed rest recovery.
	 * 
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
	 * 
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
		switch (state) {
		case DEGRADING:
			return "Degrading";
		case BEING_TREATED:
			return "Being Treated";
		case RECOVERING:
			return "Recovering";
		case CURED:
			return "Cured";
		case DEAD:
			return "Dead";
		default:
			return "";
		}
	}

	/**
	 * Start the required treatment. It will take the specified duration.
	 *
	 * @param treatmentLength Length of treatment.
	 */
	public void startTreatment(double treatmentLength, MedicalAid medicalAid) {
		usedAid = medicalAid;
		duration = treatmentLength;
		timePassed = 0D;
		setState(BEING_TREATED);

		// Create medical event for treatment.
		MedicalEvent treatedEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_TREATED);
		eventManager.registerNewEvent(treatedEvent);

		logger.info(getSufferer(), "Began to receive treatment for " + toString().toLowerCase() + ".");
	}

	/**
	 * Stops the treatment for now.
	 */
	public void stopTreatment() {
		if (state == BEING_TREATED) {
			if (duration > timePassed) {
				startDegrading();
			} else {
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
		eventManager.registerNewEvent(degradingEvent);
	}

	/**
	 * This is now moving to a recovery state.
	 */
	public void startRecovery() {

		if ((state == DEGRADING) || (state == BEING_TREATED)) {
			// If no recovery period, then it's done.
			duration = getIllness().getRecoveryPeriod();

			// Randomized the duration and varied it according to the complaint type
			if (type == ComplaintType.COLD || type == ComplaintType.FEVER)
				duration = duration + duration * RandomUtil.getRandomDouble(.5)
						- duration * RandomUtil.getRandomDouble(.5);
			else if (type == ComplaintType.HEARTBURN
					|| type == ComplaintType.HIGH_FATIGUE_COLLAPSE
					|| type == ComplaintType.PANIC_ATTACK || type == ComplaintType.DEPRESSION)
				duration = duration + duration * RandomUtil.getRandomDouble(.4)
						- duration * RandomUtil.getRandomDouble(.4);
			else if (type == ComplaintType.FLU)
				duration = duration + duration * RandomUtil.getRandomDouble(.3)
						- duration * RandomUtil.getRandomDouble(.3);
			else if (type == ComplaintType.STARVATION)
				duration = duration * (1 + RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));
			else if (type == ComplaintType.DEHYDRATION)
				duration = duration * (1 + RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));
			else
				duration = duration * (1 + RandomUtil.getRandomDouble(.1) - RandomUtil.getRandomDouble(.1));

			// TODO: what to do with environmentally induced complaints ? do they need to be treated ?
			
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
				requiresBedRest = getIllness().requiresBedRestRecovery();
//				if (requiresBedRest)
//					sufferer.getTaskSchedule().setShiftType(ShiftType.OFF);
				// Create medical event for recovering.
				MedicalEvent recoveringEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_RECOVERY);
				eventManager.registerNewEvent(recoveringEvent);

			} else {
				setCured();
//				sufferer.getTaskSchedule().allocateAWorkShift();
			}
		}
	}

	/**
	 * Sets the state of the health problem to cured.
	 */
	public void setCured() {
		setState(CURED);

		// Create medical event for cured.
		MedicalEvent curedEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_CURED);
		eventManager.registerNewEvent(curedEvent);
	}

	/**
	 * A time period has expired for this problem.
	 *
	 * @param time      The time period this problem has passed.
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
					Complaint nextPhase = getIllness().getNextPhase();
					if (usedAid != null) {
						if (usedAid.getProblemsBeingTreated().contains(this)) {
							usedAid.stopTreatment(this);
						}
						usedAid = null;
					}

					if (nextPhase == null) {
						if (type.toString().equalsIgnoreCase("suffocation")) {
							logger.info(sufferer, " was suffocated for too long and was dead.");
						}
						else {
							logger.info(sufferer, " suffered from '" 
									+ type.toString() + "' too long and was dead.");
						}
						setState(DEAD);
						condition.recordDead(this, false, "");
					} else {
						logger.info(sufferer, " suffered from '" 
								+ type.toString() + "', which was just degraded to " + nextPhase + ".");
						result = nextPhase;
					}
				}
			}
		}

		return result;
	}

	/**
	 * This method generates a string representation of this problem. It contains
	 * the illness and the health rating.
	 * 
	 * @return String description.
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (state == RECOVERING) {
			buffer.append("Recovering from ");
			buffer.append(type.toString());
		} 
		
		else if (state == BEING_TREATED) {
			buffer.append(type);
			buffer.append(" with ");
			Treatment treatment = getIllness().getRecoveryTreatment();
			if (treatment != null) {
				buffer.append(treatment.getName());
			}

		} 
		
		else
			buffer.append(type);

		int rating = getHealthRating();

		if (rating < 100) {
			buffer.append(" (");
			buffer.append(getHealthRating());
			buffer.append("%)");
		}

		return buffer.toString();
	}

	/**
	 * Checks if this problem is an environmental problem.
	 *
	 * @return true if environmental problem.
	 */
	public boolean isEnvironmentalProblem() {
		return medicalManager.isEnvironmentalComplaint(getIllness());
	}
	
	/**
	 * initializes instances after loading from a saved sim
	 * 
	 * @param m {@link medicalManager}
	 * @param h {@link HistoricalEventManager}
	 */
	public static void initializeInstances(MedicalManager m, HistoricalEventManager h) {
		medicalManager = m;
		eventManager = h;
	}

	public void destroy() {
		complaint = null;
		sufferer = null;
		usedAid = null;
	}
}
