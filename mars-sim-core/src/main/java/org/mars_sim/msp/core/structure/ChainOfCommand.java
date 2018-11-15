/**
 * Mars Simulation Project
 * ChainOfCommand.java
 * @version 3.1.0 2017-03-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Role;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.job.Architect;
import org.mars_sim.msp.core.person.ai.job.Areologist;
import org.mars_sim.msp.core.person.ai.job.Astronomer;
import org.mars_sim.msp.core.person.ai.job.Biologist;
import org.mars_sim.msp.core.person.ai.job.Botanist;
import org.mars_sim.msp.core.person.ai.job.Chef;
import org.mars_sim.msp.core.person.ai.job.Chemist;
import org.mars_sim.msp.core.person.ai.job.Doctor;
import org.mars_sim.msp.core.person.ai.job.Driver;
import org.mars_sim.msp.core.person.ai.job.Engineer;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.job.Mathematician;
import org.mars_sim.msp.core.person.ai.job.Meteorologist;
import org.mars_sim.msp.core.person.ai.job.Physicist;
import org.mars_sim.msp.core.person.ai.job.Technician;
import org.mars_sim.msp.core.person.ai.job.Trader;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The ChainOfCommand class creates and assigns a person a role type based on
 * one's job type and the size of the settlement
 */
public class ChainOfCommand implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChainOfCommand.class.getName());

	private boolean has7Divisions = false;
	private boolean has3Divisions = false;

	private Map<RoleType, Integer> roleType;

	private Settlement settlement;

	private UnitManager unitManager;

	/**
	 * This class creates a chain of command structure for a settlement. A
	 * settlement can have either 3 divisions or 7 divisions organizational
	 * structure
	 * 
	 * @param settlement {@link Settlement}
	 */
	public ChainOfCommand(Settlement settlement) {
		this.settlement = settlement;
		roleType = new ConcurrentHashMap<>();

		unitManager = Simulation.instance().getUnitManager();
	}

	/**
	 * Assigns a person with one of the three specialist role types
	 * 
	 * @param person {@link Person}
	 * @param num
	 */
	public void assignRole(Job job, Person person, int num) {
		int safety = getNumFilled(RoleType.SAFETY_SPECIALIST);
		int resource = getNumFilled(RoleType.RESOURCE_SPECIALIST);
		int engr = getNumFilled(RoleType.ENGINEERING_SPECIALIST);

		// fill up a particular role in sequence without considering one's job type
		if (safety == num - 1) {
			person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
		} else if (engr == num - 1) {
			person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
		} else if (resource == num - 1) {
			person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
		}
		else {
			int rand = RandomUtil.getRandomInt(2);
			if (rand == 0) {
				person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} else if (rand == 1) {
				person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			} else  {
				person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			}
		}
	}

	/**
	 * Assigns a person with a specialist roleType in 3-division settlement
	 * 
	 * @param person {@link Person}
	 */
	public void assignSpecialiststo3Divisions(Person person) {
		// if a person has not been assigned a role, he/she will be mission specialist
		Job job = person.getMind().getJob();
		Role role = person.getRole();
		int pop = 0;

		if (person.getSettlement() == null) {
			logger.warning(person.getName() + " is not in a settlement.");
		} else {
			pop = person.getSettlement().getIndoorPeopleCount();
			if (pop == 0) {
				logger.warning("No one is inside " + person.getName() + "'s settlement.");
			}
			pop = person.getAssociatedSettlement().updateAllAssociatedPeople().size();
			if (pop == 0) {
				logger.warning("Imppossible to have no associated people in " + person.getName() + "'s settlement.");
			}
		}

		boolean allSlotsFilledOnce = areAllFilled(1);

		boolean allSlotsFilledTwice = true;
		if (pop >= 4)
			allSlotsFilledTwice = areAllFilled(2);

		boolean allSlotsFilledTriple = true;
		if (pop > 8)
			allSlotsFilledTriple = areAllFilled(3);

		if (!allSlotsFilledOnce) {
			assignRole(job, person, 1);
		} else if (!allSlotsFilledTwice) {
			assignRole(job, person, 2);
		} else if (!allSlotsFilledTriple) {
			assignRole(job, person, 3);
		}

		else {

			if (job.equals(JobManager.getJob(Architect.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Areologist.class.getSimpleName())))
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			else if (job.equals(JobManager.getJob(Astronomer.class.getSimpleName())))
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			else if (job.equals(JobManager.getJob(Biologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Botanist.class.getSimpleName())))
				role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			else if (job.equals(JobManager.getJob(Chef.class.getSimpleName())))
				role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			else if (job.equals(JobManager.getJob(Chemist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Doctor.class.getSimpleName())))
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			else if (job.equals(JobManager.getJob(Driver.class.getSimpleName())))
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			else if (job.equals(JobManager.getJob(Engineer.class.getSimpleName())))
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			else if (job.equals(JobManager.getJob(Mathematician.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Meteorologist.class.getSimpleName())))
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			else if (job.equals(JobManager.getJob(Physicist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				else
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Technician.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				else
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Trader.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			}
		}
	}

	/**
	 * Assigns a person with a specialist roleType in 7-division settlement
	 * 
	 * @param person {@link Person}
	 */
	public void assignSpecialiststo7Divisions(Person person) {
		// if a person has not been assigned a role, he/she will be mission specialist

//            int missionSlot = 0;
//            int safetySlot = 0;
//            int agriSlot = 0;
//            int engrSlot = 0;
//            int resourceSlot = 0;
//            int scienceSlot = 0;
//            int logSlot = 0;

		Job job = person.getMind().getJob();
		Role role = person.getRole();
		int pop = person.getSettlement().getNumCitizens();
		// int slot = (int) ((pop - 2 - 7 )/ 7);

		boolean allSlotsFilledOnce = areAllFilled(1);

		boolean allSlotsFilledTwice = true;
		if (pop >= 4)
			allSlotsFilledTwice = areAllFilled(2);

		boolean allSlotsFilledTriple = true;
		if (pop > 8)
			allSlotsFilledTriple = areAllFilled(3);

		boolean allSlotsFilledQuad = true;
		if (pop > 12)
			allSlotsFilledQuad = areAllFilled(4);

		boolean allSlotsFilledPenta = true;
		if (pop > 24)
			allSlotsFilledPenta = areAllFilled(5);

		if (!allSlotsFilledOnce) {
			assignRole(job, person, 1);
		} else if (!allSlotsFilledTwice) {
			assignRole(job, person, 2);
		} else if (!allSlotsFilledTriple) {
			assignRole(job, person, 3);
		} else if (!allSlotsFilledQuad) {
			assignRole(job, person, 4);
		} else if (!allSlotsFilledPenta) {
			assignRole(job, person, 5);
		} else {

			if (job.equals(JobManager.getJob(Architect.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Areologist.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.MISSION_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Astronomer.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Biologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.AGRICULTURE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Botanist.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.AGRICULTURE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Chef.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Chemist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Doctor.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Driver.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Engineer.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Mathematician.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Meteorologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Physicist.class.getSimpleName()))) {
				int num = RandomUtil.getRandomInt(1, 3);
				if (num == 1) {
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				} else if (num == 2) {
					role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Technician.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Trader.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			}
		}
	}

	/**
	 * Increments the number of the target role type in the map
	 * 
	 * @param key {@link RoleType}
	 */
	public void addRoleTypeMap(RoleType key) {
		int value = getNumFilled(key);
		roleType.put(key, value + 1);
	}

	/**
	 * Decrements the number of the target role type from the map
	 * 
	 * @param key {@link RoleType}
	 */
	public void releaseRoleTypeMap(RoleType key) {
		int value = getNumFilled(key);
		if (value != 0)
			roleType.put(key, value - 1);
		// Check if the job Role released is manager/commander/chief
		reelect(key);
	}

	/**
	 * Elects a new person for leadership in a settlement if a mayor,
	 * commander, sub-commander, or chiefs vacates his/her position.
	 * 
	 * @param key {@link RoleType}
	 */
	public void reelect(RoleType key) {

		if (key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES || key == RoleType.CHIEF_OF_ENGINEERING
				|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
			unitManager.electChief(settlement, key);
		} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
			int pop = settlement.getNumCitizens();
			unitManager.electCommanders(settlement, pop);
		} else if (key == RoleType.MAYOR) {
			unitManager.electMayor(settlement, key);
		}
	}

	/**
	 * Finds out the number of people already fill this roleType.
	 * 
	 * @param key
	 */
	public int getNumFilled(RoleType key) {
		int value = 0;
		if (roleType.containsKey(key))
			value = roleType.get(key);
		return value;
	}

	/**
	 * Sets this settlement to have 3 divisions
	 */
	public void set3Divisions(boolean value) {
		has3Divisions = value;
	}

	/**
	 * Sets this settlement to have 7 divisions
	 */
	public void set7Divisions(boolean value) {
		has7Divisions = value;
	}

	/**
	 * Checks if all the roleTypes in a settlement have been filled
	 * 
	 * @param minimum
	 */
	public boolean areAllFilled(int minimum) {
		boolean result = false;
		if (has3Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= minimum)
				result = true;
		} else if (has7Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.MISSION_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.AGRICULTURE_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.SCIENCE_SPECIALIST) >= minimum
					&& getNumFilled(RoleType.LOGISTIC_SPECIALIST) >= minimum)
				result = true;
		}
		return result;
	}

	public void destroy() {
		roleType.clear();
		roleType = null;
		settlement = null;
	}

}
