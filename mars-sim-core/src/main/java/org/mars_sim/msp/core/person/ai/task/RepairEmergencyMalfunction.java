/**
 * Mars Simulation Project
 * RepairEmergencyMalfunction.java
 * @version 3.1.0 2017-08-30
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.malfunction.MalfunctionFactory;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.meta.RepairMalfunctionMeta;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskEvent;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RoboticAttributeType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The RepairEmergencyMalfunction class is a task to repair an emergency
 * malfunction.
 */
public class RepairEmergencyMalfunction extends Task implements Repair, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(RepairEmergencyMalfunction.class.getName());

	private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1,
			logger.getName().length());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.repairEmergencyMalfunction"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase REPAIRING = new TaskPhase(Msg.getString("Task.phase.repairing")); //$NON-NLS-1$

	// Static members
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = 1D;

	// Data members
	/** The entity being repaired. */
	private Malfunctionable entity;
	/** Problem being fixed. */
	private Malfunction malfunction;
	private Person person = null;
	private Robot robot = null;

	/**
	 * Constructs a RepairEmergencyMalfunction object.
	 * 
	 * @param person the person to perform the task
	 */
	public RepairEmergencyMalfunction(Unit unit) {
		super(NAME, unit, true, true, STRESS_MODIFIER, false, 25D);

		if (unit instanceof Person) {
			this.person = (Person) unit;

			int score = person.getPreference().getPreferenceScore(new RepairMalfunctionMeta());
			// Factor in a person's preference for the new stress modifier
			super.setStressModifier(score / 10D + STRESS_MODIFIER);
		}
        else if (unit instanceof Robot) {
        	this.robot = (Robot) unit;
//        	endTask();
        }

		claimMalfunction();

		if (entity != null) {
			addPersonOrRobotToMalfunctionLocation(entity);
		} else {
			endTask();
			return;
		}

		// Create starting task event if needed.
		TaskEvent startingEvent = null;
		if (getCreateEvents() && !isDone()) {
			if (person != null)
				startingEvent = new TaskEvent(person, this, entity, EventType.TASK_START,
						person.getLocationTag().getImmediateLocation(), "Repair Emergency Malfunction");
        	else if (robot != null)
                startingEvent = new TaskEvent(robot, 
                		this, 
                		entity,
                		EventType.TASK_START, 
                		robot.getLocationTag().getImmediateLocation(), 
                		"Repair Emergency Malfunction");

			Simulation.instance().getEventManager().registerNewEvent(startingEvent);
		}

		// Initialize task phase
		addPhase(REPAIRING);
		setPhase(REPAIRING);

		if (malfunction != null) {
			if (person != null) {
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						"[" + person.getLocationTag().getLocale() + "] " + person.getName() + " started repairing on emergency malfunction: " 
				+ malfunction.getName() + " in "+ entity + ".");
//				+ "@"+ Integer.toHexString(malfunction.hashCode()));
			}
        	else if (robot != null) {
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() + " started repairing on emergency malfunction: " 
				+ malfunction.getName() + " in "+ entity + ".");
//				+ "@" + Integer.toHexString(malfunction.hashCode()));
        	}
		}
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (REPAIRING.equals(getPhase())) {
			return repairingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Performs the repairing phase of the task.
	 * 
	 * @param time the amount of time (millisol) to perform the phase.
	 * @return the amount of time (millisol) left after performing the phase.
	 */
	private double repairingPhase(double time) {
		String name = null;

		if (isDone()) {
			return time;
		}

		double workTime = 0;

		if (person != null) {
			name = person.getName();
			workTime = time;
		} else if (robot != null) {
			name = robot.getName();
			// A robot moves slower than a person and incurs penalty on workTime
			workTime = time / 2;
		}

		// Determine effective work time based on "Mechanic" skill.
		int mechanicSkill = getEffectiveSkillLevel();
		if (mechanicSkill == 0) {
			workTime /= 2;
		} else if (mechanicSkill > 1) {
			workTime += (workTime * (.2D * mechanicSkill));
		}

		// Add work to emergency malfunction.
		double remainingWorkTime = malfunction.addEmergencyWorkTime(workTime, name);

		// Add experience points
		addExperience(time);

		// Check if an accident happens during repair.
//		checkForAccident(time);

		// Check if the emergency malfunction work is fixed.
		if (malfunction.needEmergencyRepair() && malfunction.isEmergencyRepairDone()) {
			if (person != null) {
			LogConsolidated.log(Level.INFO, 10_000, sourceName,
					"[" + person.getLocationTag().getLocale() + "] " + person.getName() 
					+ " wrapped up the emergency repair of " + malfunction.getName() 
					+ " in "+ entity + " (" + Math.round(malfunction.getCompletedEmergencyWorkTime()*10.0)/10.0 + " millisols spent).");
			}
			else {
				LogConsolidated.log(Level.INFO, 10_000, sourceName,
						"[" + robot.getLocationTag().getLocale() + "] " + robot.getName() 
						+ " wrapped up the emergency repair of " + malfunction.getName() 
						+ " in "+ entity + " (" + Math.round(malfunction.getCompletedEmergencyWorkTime()*10.0)/10.0 + " millisols spent).");
			}
			endTask();
		}
		
		return remainingWorkTime;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience to "Mechanics" skill
		// (1 base experience point per 20 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 20D;
		if (person != null) {
			int experienceAptitude = person.getNaturalAttributeManager()
					.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
			newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			newPoints *= getTeachingExperienceModifier();
			person.getSkillManager().addExperience(SkillType.MECHANICS, newPoints, time);
		} 
		else if (robot != null) {
			 int experienceAptitude = robot.getRoboticAttributeManager().getAttribute(
					 RoboticAttributeType.EXPERIENCE_APTITUDE);
			 newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
			 newPoints *= getTeachingExperienceModifier();
			 robot.getSkillManager().addExperience(SkillType.MECHANICS,
			 newPoints, time);
		}

	}

	/**
	 * Checks if the person has a local emergency malfunction.
	 * 
	 * @return true if emergency, false if none.
	 */
	public static boolean hasEmergencyMalfunction(Person person) {

		boolean result = false;

		Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
		while (i.hasNext()) {
			Malfunctionable entity = i.next();
			MalfunctionManager manager = entity.getMalfunctionManager();
			if (manager.hasEmergencyMalfunction()) {
				result = true;
			}
		}

		return result;
	}

	public static boolean hasEmergencyMalfunction(Robot robot) {

		boolean result = false;

        Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
        while (i.hasNext()) {
            Malfunctionable entity = i.next();
            MalfunctionManager manager = entity.getMalfunctionManager();
            if (manager.hasEmergencyMalfunction()) {
                result = true;
            }
        }

		return result;
	}

	/**
	 * Gets a local emergency malfunction.
	 */
	private void claimMalfunction() {
		malfunction = null;
		if (person != null) {
			Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(person).iterator();
			while (i.hasNext() && (malfunction == null)) {
				Malfunctionable e = i.next();
				MalfunctionManager manager = e.getMalfunctionManager();
				if (manager.hasEmergencyMalfunction()) {
					malfunction = manager.getMostSeriousEmergencyMalfunction();
					entity = e;
					setDescription(Msg.getString("Task.description.repairEmergencyMalfunction.detail",
							malfunction.getName(), entity.getNickName())); // $NON-NLS-1$
				}
			}
		} 
		
		else if (robot != null) {
            Iterator<Malfunctionable> i = MalfunctionFactory.getMalfunctionables(robot).iterator();
            while (i.hasNext() && (malfunction == null)) {
                Malfunctionable e = i.next();
                MalfunctionManager manager = e.getMalfunctionManager();
                if (manager.hasEmergencyMalfunction()) {
                    malfunction = manager.getMostSeriousEmergencyMalfunction();
                    entity = e;
                    setDescription(Msg.getString("Task.description.repairEmergencyMalfunction.detail",
                            malfunction.getName(), entity.getNickName())); //$NON-NLS-1$
                }
            }
		}
	}

	/**
	 * Gets the malfunctionable entity the person is currently repairing or null if
	 * none.
	 * 
	 * @return entity
	 */
	public Malfunctionable getEntity() {
		return entity;
	}

	/**
	 * Adds the person or robot to building if malfunctionable is a building with
	 * life support. Otherwise walk to random location.
	 * 
	 * @param malfunctionable the malfunctionable the person or robot is repairing.
	 */
	private void addPersonOrRobotToMalfunctionLocation(Malfunctionable malfunctionable) {

		boolean isWalk = false;
		if (malfunctionable instanceof Building) {
			Building building = (Building) malfunctionable;
			if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {

				// Walk to malfunctioning building.
				walkToRandomLocInBuilding(building, true);
				isWalk = true;
			}
		} else if (malfunctionable instanceof Rover) {
			// Walk to malfunctioning rover.
			walkToRandomLocInRover((Rover) malfunctionable, true);
			isWalk = true;
		}

		if (!isWalk) {
			walkToRandomLocation(true);
		}
	}

	@Override
	public int getEffectiveSkillLevel() {
		SkillManager manager = null;
		if (person != null)
			manager = person.getSkillManager();
		else if (robot != null)
			manager = robot.getSkillManager();
		return manager.getEffectiveSkillLevel(SkillType.MECHANICS);
	}

	@Override
	public List<SkillType> getAssociatedSkills() {
		List<SkillType> results = new ArrayList<SkillType>(1);
		results.add(SkillType.MECHANICS);
		return results;
	}

	@Override
	public void destroy() {
		super.destroy();

		person = null;
		robot = null;
		entity = null;
		malfunction = null;
	}
}