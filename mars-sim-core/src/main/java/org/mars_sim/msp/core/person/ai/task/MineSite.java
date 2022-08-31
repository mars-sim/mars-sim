/*
 * Mars Simulation Project
 * MineSite.java
 * @date 2021-10-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.util.Iterator;
import java.util.Map;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.LightUtilityVehicle;
import org.mars_sim.msp.core.vehicle.Rover;

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

	/** Task phases. */
	private static final TaskPhase MINING = new TaskPhase(Msg.getString("Task.phase.mining")); //$NON-NLS-1$

	/** Excavation rates (kg/millisol). */
	private static final double HAND_EXCAVATION_RATE = .1D;
	/** Excavation rates (kg/millisol). */
	private static final double LUV_EXCAVATION_RATE = 1D;

	/** The base chance of an accident while operating LUV per millisol. */
	public static final double BASE_LUV_ACCIDENT_CHANCE = .001;

	// Data members
	private Coordinates site;
	private LightUtilityVehicle luv;
	private boolean operatingLUV;

	/**
	 * Constructor
	 *
	 * @param person the person performing the task.
	 * @param site   the explored site to mine.
	 * @param rover  the rover used for the EVA operation.
	 * @param luv    the light utility vehicle used for mining.
	 */
	public MineSite(Person person, Coordinates site, Rover rover, LightUtilityVehicle luv) {

		// Use EVAOperation parent constructor.
		super(NAME, person, true, RandomUtil.getRandomDouble(50D) + 10D, SkillType.PROSPECTING);

		// Initialize data members.
		this.site = site;
		this.luv = luv;
		operatingLUV = false;

		if (shouldEndEVAOperation()) {
			checkLocation();
        	return;
        }

		if (!person.isBarelyFit()) {
			checkLocation();
        	return;
		}

		// Determine location for mining site.
		setRandomOutsideLocation(rover);

		// Add task phase
		addPhase(MINING);
	}

	/**
	 * Checks if a person can mine a site.
	 *
	 * @param member the member
	 * @param rover  the rover
	 * @return true if person can mine a site.
	 */
	public static boolean canMineSite(Worker member, Rover rover) {

		if (member instanceof Person) {
			Person person = (Person) member;

			// Check if person can exit the rover.
			if (!ExitAirlock.canExitAirlock(person, rover.getAirlock()))
				return false;

			if (EVAOperation.isGettingDark(person))
				return false;

			// Check if person's medical condition will not allow task.
            return !(person.getPerformanceRating() < .2D);
		}

		return true;
	}

	@Override
	protected TaskPhase getOutsideSitePhase() {
		return MINING;
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

	/**
	 * Perform the mining phase of the task.
	 *
	 * @param time the time available (millisols).
	 * @return remaining time after performing phase (millisols).
	 * @throws Exception if error performing phase.
	 */
	private double miningPhase(double time) {
		double remainingTime = 0;
		
		if (checkReadiness(time) > 0)
			return time;

		// Check if there is reason to cut the mining phase short and return
		// to the rover.
		if (addTimeOnSite(time)) {
			// End operating light utility vehicle.
			if (person != null && ((Crewable)luv).isCrewmember(person)) {
				luv.removePerson(person);
				luv.setOperator(null);
				operatingLUV = false;

			} else if (robot != null && ((Crewable)luv).isRobotCrewmember(robot)) {
				luv.removeRobot(robot);
				luv.setOperator(null);
				operatingLUV = false;
			}

			checkLocation();
		}

		// Operate light utility vehicle if no one else is operating it.
		if (person != null && !luv.getMalfunctionManager().hasMalfunction()
				&& (luv.getCrewNum() == 0) && (luv.getRobotCrewNum() == 0)) {

			if (luv.addPerson(person)) {

				LocalPosition settlementLoc = LocalAreaUtil.getRandomLocalRelativePosition(luv);

				person.setPosition(settlementLoc);
				luv.setOperator(person);

				operatingLUV = true;
				setDescription(Msg.getString("Task.description.mineSite.detail", luv.getName())); // $NON-NLS-1$
			} else {
				logger.info(person, " could not operate " + luv.getName());
			}
		}

		// Excavate minerals.
		excavateMinerals(time);

		// Add experience points
		addExperience(time);

		// Check for an accident during the EVA operation.
		checkForAccident(time);

		return remainingTime;
	}

	/**
	 * Excavating minerals from the mining site.
	 *
	 * @param time the time to excavate minerals.
	 * @throws Exception if error excavating minerals.
	 */
	private void excavateMinerals(double time) {

		Map<String, Double> minerals = surfaceFeatures.getMineralMap()
				.getAllMineralConcentrations(site);
		Iterator<String> i = minerals.keySet().iterator();
		while (i.hasNext()) {
			String mineralName = i.next();
			double amountExcavated = 0D;
			if (operatingLUV) {
				amountExcavated = LUV_EXCAVATION_RATE * time;
			} else {
				amountExcavated = HAND_EXCAVATION_RATE * time;
			}
			double mineralConcentration = minerals.get(mineralName);
			amountExcavated *= mineralConcentration / 100D;
			amountExcavated *= getEffectiveSkillLevel();

			((Mining) worker.getMission()).excavateMineral(
					ResourceUtil.findAmountResource(mineralName), amountExcavated);
		}
	}

	@Override
	protected void addExperience(double time) {
		super.addExperience(time);

		// If person is driving the light utility vehicle, add experience to driving
		// skill.
		// 1 base experience point per 10 millisols of mining time spent.
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		if (getOutsideSitePhase().equals(getPhase()) && operatingLUV) {
			int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
			double experienceAptitudeModifier = (((double) experienceAptitude) - 50D) / 100D;
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
			checkForAccident(luv, time, BASE_LUV_ACCIDENT_CHANCE, skill, luv.getName());
		}
	}

	@Override
	protected boolean shouldEndEVAOperation() {
		boolean result = super.shouldEndEVAOperation();

		// If operating LUV, check if LUV has malfunction.
		if (operatingLUV && luv.getMalfunctionManager().hasMalfunction()) {
			result = true;
		}

		return result;
	}
}
