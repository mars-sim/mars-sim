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

import org.mars_sim.msp.core.RandomUtil;
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

/**
 * The ChainOfCommand class creates and assigns a person a role type based on
 * one's job type and the size of the settlement
 */
public class ChainOfCommand implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChainOfCommand.class.getName());

//    int safetySlot = 0;
//    int engrSlot = 0;
//    int resourceSlot = 0;
//	int missionSlot = 0;
//	int agriSlot = 0;
//	int scienceSlot = 0;
//	int logSlot = 0;

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

//		initializeRoleType();

	}

//	public void initializeRoleType() {
//
//	}

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
		// System.out.println(person.getName());

		// fill up a particular role in sequence without considering one's job type
		if (safety == num - 1) {
			person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
			// System.out.println(person.getRole().toString());
		} else if (engr == num - 1) {
			person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			// System.out.println(person.getRole().toString());
		} else if (resource == num - 1) {
			person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			// System.out.println(person.getRole().toString());
		}

//        if (job.equals(JobManager.getJob("Architect")))
//        	person.getRole().setRoleType(RoleType.SAFETY_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Areologist")))
//        	person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Astronomer")))
//        	person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Biologist")))
//       		person.getRole().setRoleType(RoleType.RESOURCE_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Botanist")))
//        	person.getRole().setRoleType(RoleType.RESOURCE_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Chef")))
//        	person.getRole().setRoleType(RoleType.RESOURCE_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Chemist")))
//        	person.getRole().setRoleType(RoleType.RESOURCE_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Doctor")))
//        	person.getRole().setRoleType(RoleType.SAFETY_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Driver")))
//            person.getRole().setRoleType(RoleType.SAFETY_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Engineer")))
//        	person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Mathematician")))
//           	person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Meteorologist")))
//        	person.getRole().setRoleType(RoleType.SAFETY_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Physicist")))
//            person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Technician")))
//           	person.getRole().setRoleType(RoleType.ENGINEERING_SPECIALIST);
//        else if (job.equals(JobManager.getJob("Trader")))
//           	person.getRole().setRoleType(RoleType.SAFETY_SPECIALIST);

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
		if (person.getSettlement() == null)
			logger.warning("person.getSettlement() = null");
		else if (person.getSettlement().getAllAssociatedPeople() == null)
			logger.warning("person.getSettlement().getAllAssociatedPeople() = null");
		else {
			pop = person.getSettlement().getAllAssociatedPeople().size();
			if (pop == 0) {
				// logger.warning("person.getSettlement().getAllAssociatedPeople().size() = 0");
				pop = person.getSettlement().getIndoorPeopleCount();
				if (pop == 0)
					logger.warning("person.getSettlement().getCurrentPopulationNum() = 0");
			}
		}
		// if (pop == 0)
		// pop = person.getSettlement().getCurrentPopulationNum();
		// int slot = (int) ((pop - 2 - 3 )/ 3);

		boolean allSlotsFilledOnce = areAllFilled(1);

		boolean allSlotsFilledTwice = true;
		if (pop >= 4)
			allSlotsFilledTwice = areAllFilled(2);

		boolean allSlotsFilledTriple = true;
		if (pop > 8)
			allSlotsFilledTriple = areAllFilled(3);

//            //boolean allSlotsFilledQuad = true;
//            //if (pop > 12)
//            //	allSlotsFilledQuad = areAllFilled(4);
//
//            //boolean allSlotsFilledPenta = true;
//			// if (pop > 24)
//            //	allSlotsFilledPenta = areAllFilled(5);

		if (!allSlotsFilledOnce) {
			// System.out.println("inside if (!allSlotsFilledOnce)");
			assignRole(job, person, 1);
		} else if (!allSlotsFilledTwice) {
			// System.out.println("inside if (!allSlotsFilledTwice)");
			assignRole(job, person, 2);
		} else if (!allSlotsFilledTriple) {
			assignRole(job, person, 3);
		}

		// else if (!allSlotsFilledQuad) {
		// assignRole(job, person, 4);
		// }
		// else if (!allSlotsFilledPenta) {
		// assignRole(job, person, 5);
		// }

		else {
			// System.out.println("inside else");
			// System.out.println("job is " + job.toString());
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
		int pop = person.getSettlement().getAllAssociatedPeople().size();
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
			// System.out.println("inside if (!allSlotsFilledOnce)");
			assignRole(job, person, 1);
		} else if (!allSlotsFilledTwice) {
			// System.out.println("inside if (!allSlotsFilledTwice)");
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

//    public int getSafetySlot() {
//    	return safetySlot;
//    }
//    public int getEngrSlot() {
//    	return engrSlot;
//    }
//    public int getResourceSlot() {
//    	return resourceSlot;
//    }
//    public void addSafety() {
//    	safetySlot++;
//       	//System.out.println("safetySlot : "+ safetySlot);
//    }

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
	 * commander,sub-commander, or chiefs vacates his/her position.
	 * 
	 * @param key {@link RoleType}
	 */
	public void reelect(RoleType key) {

		if (key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES || key == RoleType.CHIEF_OF_ENGINEERING
				|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
			unitManager.electChief(settlement, key);
		} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
			int pop = settlement.getAllAssociatedPeople().size();
			unitManager.electCommanders(settlement, key, pop);
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
		// return roleType.getOrDefault(key, 0);
	}

//    public void decrementSafety() {
//    	safetySlot--;
//    }
//    public void setSafetySlot(int value) {
//    	safetySlot = value;
//    }
//    public void addEngr() {
//    	engrSlot++;
//    }
//    public void decrementEngr() {
//    	engrSlot--;
//       	//System.out.println("engrSlot : "+ engrSlot);
//    }
//    public void setEngrSlot(int value) {
//    	engrSlot = value;
//    }
//    public void addResource() {
//    	resourceSlot++;
//       	//System.out.println("resourceSlot : "+ resourceSlot);
//    }
//    public void decrementResource() {
//    	resourceSlot--;
//       	//System.out.println("resourceSlot : "+ resourceSlot);
//    }
//    public void setResourceSlot(int value) {
//    	resourceSlot = value;
//    }
//    public void addScience() {
//    	scienceSlot++;
//    }
//    public void addLogistic() {
//    	logSlot++;
//    }
//    public void addAgri() {
//    	agriSlot++;
//    }
//    public void addMission() {
//    	missionSlot++;
//    }

	/**
	 * Sets this settlement to have 3 divisions
	 */
	public void set3Divisions(boolean value) {
		has3Divisions = value;
		// System.out.println("has3Divisions = " + has3Divisions);
	}

	/**
	 * Sets this settlement to have 7 divisions
	 */
	public void set7Divisions(boolean value) {
		has7Divisions = value;
		// System.out.println("has7Divisions = " + has7Divisions);
	}

	/**
	 * Checks if all the roleTypes in a settlement have been filled
	 */
	public boolean areAllFilled(int value) {
		boolean result = false;
		if (has3Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= value
					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= value
					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= value)
				result = true;
			// System.out.println("result of 3 : "+ result);
		} else if (has7Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= value
					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= value
					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= value
					&& getNumFilled(RoleType.MISSION_SPECIALIST) >= value
					&& getNumFilled(RoleType.AGRICULTURE_SPECIALIST) >= value
					&& getNumFilled(RoleType.SCIENCE_SPECIALIST) >= value
					&& getNumFilled(RoleType.LOGISTIC_SPECIALIST) >= value)
				result = true;
			// System.out.println("result of 7 : : "+ result);
		}
		// System.out.println("areAllFilled : "+ result);
		return result;
	}

	public void destroy() {
		roleType.clear();
		roleType = null;
		settlement = null;
	}

}
