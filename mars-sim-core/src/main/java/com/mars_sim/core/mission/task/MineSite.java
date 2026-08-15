/*
 * Mars Simulation Project
 * MineSite.java
 * @date 2023-09-17
 * @author Scott Davis
 */
package com.mars_sim.core.mission.task;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.task.EVAOperation;
import com.mars_sim.core.person.ai.task.ExitAirlock;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.core.vehicle.Rover;

/**
 * Task for mining minerals at a site.
 */
public class MineSite extends EVAOperation {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(MineSite.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.mineSite"); //$NON-NLS-1$

	/** Simple Task name */
	static final String SIMPLE_NAME = MineSite.class.getSimpleName();
	
	/** Task phases. */
	private static final TaskPhase MINING = new TaskPhase(Msg.getString("Task.phase.mining"),
					createPhaseImpact(SkillType.PROSPECTING));


	/** Excavation rates (kg/millisol). */
	private static final double HAND_EXCAVATION_RATE = 5D;
	/** Excavation rates (kg/millisol). */
	private static final double LUV_EXCAVATION_RATE = 25D;

	/** The base chance of an accident while operating LUV per millisol. */
	public static final double BASE_LUV_ACCIDENT_CHANCE = .001;

	/** Level of light needed for mining */
	public static final LightLevel LIGHT_LEVEL = LightLevel.LOW;

	// Data members
	private MiningObjective objectives;
	private boolean operatingLUV;

	private Mission eventSource;

	/**
	 * Constructor.
	 *
	 * @param person the person performing the task.
	 * @param objective   the objectives of the mining
	 * @param rover  the rover used for the EVA operation.
	 * @param eventSource The source of the mission event.
	 */
	public MineSite(Person person, MiningObjective objective, Rover rover, Mission eventSource) {

		// Use EVAOperation parent constructor.
		super(NAME, person, RandomUtil.getRandomDouble(50D) + 50D, MINING);
		setMinimumSunlight(LIGHT_LEVEL);

		// Initialize data members.
		this.objectives = objective;
		this.eventSource = eventSource;		
		operatingLUV = false;

		if (EVAOperation.isSuperUnfit(person)){
			endEVA("Super Unfit.");
        	return;
		}

		// Determine location for mining site.
		setRandomOutsideLocation(rover);
	}

	/**
	 * Checks if a person can mine a site.
	 *
	 * @param member the member
	 * @param rover  the rover
	 * @return true if person can mine a site.
	 */
	public static boolean canMineSite(Worker member, Rover rover) {

		if (member instanceof Person person) {

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			// Check if person's medical condition will not allow task.
            return (person.getPerformanceRating() >= .2D);
		}

		return true;
	}

	@Override
	protected double performMappedPhase(double time) {

		time = super.performMappedPhase(time);
		if (!isDone()) {
			if (getPhase() == null) {
				throw new IllegalArgumentException("Task phase is null");
			}
			else if (MINING.equals(getPhase())) {
				time = miningPhase(time);
			}
		}
		return time;
	}

	@Override
	public void endEVA(String reason) {
		// End operating light utility vehicle.
		if (operatingLUV) {
			var luv = objectives.getLUV();
			logger.info(person, "Release " + luv.getName());
			luv.removeOccupant(person);
			operatingLUV = false;
		}

		super.endEVA(reason);
	}

	/**
	 * Perform the mining phase of the task.
	 *
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double miningPhase(double time) {
		double remainingTime = 0;

		// Note: need to call addTimeOnSite() ahead of checkReadiness() since
		// checkReadiness's addTimeOnSite() lacks the details of handling LUV
		if (checkReadiness(time) > 0) {
			return time;
		}

		
		// Operate light utility vehicle if no one else is operating it.
		var luv = objectives.getLUV();
		if (!luv.getMalfunctionManager().hasMalfunction()
				&& !luv.isFull() && luv.getOperator() == null) {

			// Warning: do not call addOccupant directly
			boolean canTransfer = person.transfer(luv);	
			if (canTransfer) {
				logger.info(person, "Operating " + luv.getName());

				operatingLUV = true;
				setDescription(Msg.getString("Task.description.mineSite.detail", luv.getName())); // -NLS-1$
			}
			else {
				
			}
		}

		// Excavate minerals.
		excavateMinerals(time);

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);
		
		if (person.isSuperUnfit()) {
			endEVA("Super Unfit.");
			return time;
		}
		
		return remainingTime;
	}

	/**
	 * Excavating minerals from the mining site.
	 *
	 * @param time the time to excavate minerals.
	 */
	private void excavateMinerals(double time) {
		double totalToBeExtracted = (operatingLUV ? LUV_EXCAVATION_RATE 
									: HAND_EXCAVATION_RATE) * time;
		int skill = getEffectiveSkillLevel();
		if (skill > 1)
			totalToBeExtracted += (.2D * skill);

		var totalExtracted = 0D;
		var site = objectives.getSite();
		var siteMinerals = site.getMinerals();
		for(var e : siteMinerals.entrySet()) {

			int mineralId = e.getKey();
			double concentration = e.getValue().concentration()/100D;
			double certainty = e.getValue().certainty()/100D;
			double mineralExcavated = totalToBeExtracted * certainty * concentration;
			mineralExcavated = Math.min(mineralExcavated, site.getEstimatedMineralAmount(mineralId));
			objectives.extractedMineral(mineralId, mineralExcavated);
			
        	totalExtracted += mineralExcavated;
		}

		site.excavateMass(totalExtracted);
	}

	@Override
	protected void clearDown() {
		// Just fire one event at the end of the task
		eventSource.fireMissionUpdate(MissionObjective.CHANGE_EVENT, "extracted");

		super.clearDown();
	}

	@Override
	protected void addExperience(double time) {
		super.addExperience(time);

		// If person is driving the light utility vehicle, add experience to driving
		// skill.
		// 1 base experience point per 10 millisols of mining time spent.
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		if (MINING.equals(getPhase()) && operatingLUV) {
			int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
			double experienceAptitudeModifier = ((experienceAptitude) - 50D) / 100D;
			double drivingExperience = time / 10D;
			drivingExperience += drivingExperience * experienceAptitudeModifier;
			worker.getSkillManager().addExperience(SkillType.PILOTING, drivingExperience, time);
		}
	}

	@Override
	protected void checkForAccident(double time) {
		super.checkForAccident(time);

		// Check for light utility vehicle accident if operating one.
		if (operatingLUV) {

			// Driving skill modification.
			int skill = worker.getSkillManager().getEffectiveSkillLevel(SkillType.PILOTING);

			var luv = objectives.getLUV();
			checkForAccident(luv, time, BASE_LUV_ACCIDENT_CHANCE, skill, luv.getName());
		}
	}

	@Override
	public boolean shouldEndEVAOperation() {
		boolean result = super.shouldEndEVAOperation();

		// If operating LUV, check if LUV has malfunction.
		if (operatingLUV && objectives.getLUV().getMalfunctionManager().hasMalfunction()) {
			result = true;
		}

		return result;
	}
}
