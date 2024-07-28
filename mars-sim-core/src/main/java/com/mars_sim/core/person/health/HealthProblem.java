/*
 * Mars Simulation Project
 * HealthProblem.java
 * @date 2022-07-29
 * @author Barry Evans
 */
package com.mars_sim.core.person.health;

import java.io.Serializable;

import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.events.HistoricalEventManager;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.EventType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalCondition;
import com.mars_sim.core.time.MarsTime;

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

	private HealthProblemState state; // State of problem
	private MarsTime started;

	private double timePassed; // Current time of state
	private double duration; // Length of the current state

	private boolean requiresBedRest; // Does recovery require bed rest?

	private Person sufferer; // Person
	private MedicalAid usedAid; // Any aid being used

	private ComplaintReference complaint;
	private static HistoricalEventManager eventManager;
	
	
	/**
	 * Constructor. Creates a new Health Problem that relates to a single Physical Condition
	 * object. It also references a complaint that defines the behaviour. If the
	 * Complaint has no degrade period then self-recovery starts immediately.
	 *
	 * @param complaintType Medical complaint enum being suffered.
	 * @param person    The person whose physical condition being effected.
	 */
	public HealthProblem(ComplaintType complaintType, Person person) {
		sufferer = person;
		timePassed = 0D;
		state = HealthProblemState.DEGRADING;
		complaint = new ComplaintReference(complaintType);
		duration = getComplaint().getDegradePeriod();
		started = eventManager.getClock().getMarsTime();
		usedAid = null;
		requiresBedRest = false;

		// Create medical event for health problem.
		MedicalEvent newEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_STARTS);
		eventManager.registerNewEvent(newEvent);

		logger.fine(person, " had a new health problem of " + complaintType.getName());
	}

	/**
	 * Sets the health problem state.
	 * 
	 * @param newState the new state of the health problem.
	 */
	public void setState(HealthProblemState newState) {
		state = newState;

		var c = getComplaint();
		sufferer.fireUnitUpdate(UnitEventType.ILLNESS_EVENT, c);
		logger.fine(getSufferer(), "Problem " + c.getName() + " now " + newState);
	}

	/**
	 * What is the state of this health problem
	 */
	public HealthProblemState getState() {
		return state;
	}

	/**
	 * Gets a rating of the current health situation. This is a percentage value and
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
	 * Returns the complaint.
	 *
	 * @return Complaint.
	 */
	public Complaint getComplaint() {
		return complaint.getComplaint();
	}

	/**
	 * Returns the complaint type.
	 *
	 * @return Complaint Type.
	 */
	public ComplaintType getType() {
		return complaint.getType();
	}
	
	/**
	 * Returns the sufferer of this problem.
	 */
	public Person getSufferer() {
		return sufferer;
	}

	/**
	 * Gets the performance rating for this Problem. If there is an aid in used, then the
	 * factor is zero otherwise it is the illness rating.
	 */
	public double getPerformanceFactor() {
		if (usedAid != null)
			return 0D;
		return getComplaint().getPerformanceFactor();
	}

	/**
	 * What is the current recovery period
	 * @return
	 */
	public double getRemainingRecovery() {
		return duration - timePassed;
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
		if ((state == HealthProblemState.RECOVERING) && requiresBedRest) {
			timePassed += bedRestTime;

			// If fully recovered, set health problem as cured.
			if (timePassed >= duration) {
				setCured();
			}
		}
	}

	/**
	 * Starts the required treatment. It will take the specified duration.
	 *
	 * @param treatmentLength Length of treatment.
	 */
	public void startTreatment(double treatmentLength, MedicalAid medicalAid) {
		usedAid = medicalAid;
		duration = treatmentLength;
		timePassed = 0D;
		setState(HealthProblemState.BEING_TREATED);

		// Create medical event for treatment.
		MedicalEvent treatedEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_TREATED);
		eventManager.registerNewEvent(treatedEvent);

		logger.info(getSufferer(), "Began to receive treatment for " + getComplaint().getName() + ".");
	}

	/**
	 * Stops the treatment for now.
	 */
	public void stopTreatment() {
		if (state == HealthProblemState.BEING_TREATED) {
			if (duration > timePassed) {
				startDegrading();
			} else {
				startRecovery();
			}
		}
	}

	/**
	 * Starts degrading the health problem.
	 */
	private void startDegrading() {
		setState(HealthProblemState.DEGRADING);

		// Create medical event for degrading.
		MedicalEvent degradingEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_DEGRADES);
		eventManager.registerNewEvent(degradingEvent);
	}

	/**
	 * Starts the recovery process and moving to a recovery state.
	 */
	public void startRecovery() {

		if ((state == HealthProblemState.DEGRADING) || (state == HealthProblemState.BEING_TREATED)) {
			// If no recovery period, then it's done.
			duration = getComplaint().getRecoveryPeriod();
			
			timePassed = 0D;

			if (duration > 0D) {
				setState(HealthProblemState.RECOVERING);

				if ((usedAid != null)) {
					if (usedAid.getProblemsBeingTreated().contains(this)) {
						usedAid.stopTreatment(this);
					}
					usedAid = null;
				}

				// Check if recovery requires bed rest.
				requiresBedRest = getComplaint().requiresBedRestRecovery();
				
				// Create medical event for recovering.
				MedicalEvent recoveringEvent = new MedicalEvent(sufferer, this, EventType.MEDICAL_RECOVERY);
				// Register event
				eventManager.registerNewEvent(recoveringEvent);
			} else {
				setCured();
			}
		}
	}

	/**
	 * Sets the state of the health problem to cured.
	 */
	public void setCured() {
		setState(HealthProblemState.CURED);

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

		if ((state == HealthProblemState.DEGRADING) && !getComplaint().isEnvironmental()) {
			// If no required treatment,
			Treatment treatment = getComplaint().getRecoveryTreatment();
			if (treatment == null) {
				startRecovery();
			}
		}

		if (!(requiresBedRest && (state == HealthProblemState.RECOVERING))) {
			timePassed += time;
		}

		if (timePassed >= duration) {

			// Recovering so has the recovery period expired
			if (state == HealthProblemState.RECOVERING) {
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
			
			else if ((state == HealthProblemState.DEGRADING) && (duration != 0D)) {
				// Illness has moved to next phase, if null then dead
				Complaint nextPhase = getComplaint().getNextPhase();
				if (usedAid != null) {
					if (usedAid.getProblemsBeingTreated().contains(this)) {
						usedAid.stopTreatment(this);
					}
					usedAid = null;
				}

				var name = getComplaint().getName();
				if (nextPhase == null) {
					logger.info(sufferer, "Suffered from '" 
								+ name + "' too long and was dead.");
					setState(HealthProblemState.DEAD);
					condition.recordDead(this, false, "My suffering is over. Good bye!");
				} else {
					logger.info(sufferer, "Suffered from '" 
							+ name + "', which was just degraded to " + nextPhase + ".");
					result = nextPhase;
				}
			}
		}

		return result;
	}

	/**
	 * Convert this problem into a cured report
	 * @param now
	 * @return
	 */
	public CuredProblem toCured(MarsTime now) {
		return new CuredProblem(started, now, complaint);
	}

	/**
	 * This method generates a string representation of this problem. It contains
	 * the illness and the health rating.
	 * 
	 * @return String description.
	 */
	public String outputInfoString() {
		return "Problem: " + getComplaint().getName()
				+ ". State: " + state.toString().toLowerCase();
	}
	
	/**
	 * This method generates a string representation of this problem. It contains
	 * the illness and the health rating.
	 * 
	 * @return String description.
	 */
	@Override
	public String toString() {
		return "Sufferer=" + getSufferer().getName()
				+ " Problem=" + getComplaint().getName()
				+ " State=" + state;
	}
	
	/**
	 * Initializes instances after loading from a saved sim
	 * 
	 * @param m {@link medicalManager}
	 * @param h {@link HistoricalEventManager}
	 */
	public static void initializeInstances(MedicalManager m, HistoricalEventManager h) {
		ComplaintReference.initializeInstances(m);
		eventManager = h;
	}

	public void destroy() {
		complaint = null;
		sufferer = null;
		usedAid = null;
	}
}
