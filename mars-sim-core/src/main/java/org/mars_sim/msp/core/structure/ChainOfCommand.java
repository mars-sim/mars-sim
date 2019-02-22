/**
 * Mars Simulation Project
 * ChainOfCommand.java
 * @version 3.1.0 2017-03-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.NaturalAttributeType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.Role;
import org.mars_sim.msp.core.person.RoleType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
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
import org.mars_sim.msp.core.person.ai.job.Reporter;
import org.mars_sim.msp.core.person.ai.job.Technician;
import org.mars_sim.msp.core.person.ai.job.Trader;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * The ChainOfCommand class creates and assigns a person a role type based on
 * one's job type and the size of the settlement
 */
public class ChainOfCommand implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ChainOfCommand.class.getName());

	public static final int POPULATION_WITH_COMMANDER = 4;
	public static final int POPULATION_WITH_SUB_COMMANDER = 9;
	public static final int POPULATION_WITH_CHIEFS = 17;
	public static final int POPULATION_WITH_MAYOR = 51;
	
	private boolean has7Divisions = false;
	private boolean has3Divisions = false;
    /** Stores the number for each role type . */
	private Map<RoleType, Integer> roleRegistry;

	private Settlement settlement;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
	
	private static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

//	private RoleType[] CHIEFS_3 = new RoleType[] { 
//			RoleType.CHIEF_OF_ENGINEERING,  
//			RoleType.CHIEF_OF_MISSION_PLANNING, 
//			RoleType.CHIEF_OF_SUPPLY_N_RESOURCES,
//		};
	
	private RoleType[] CHIEFS_7 = new RoleType[] {
			RoleType.CHIEF_OF_ENGINEERING,  
			RoleType.CHIEF_OF_MISSION_PLANNING, 
			RoleType.CHIEF_OF_SUPPLY_N_RESOURCES,
			
			RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS,
			RoleType.CHIEF_OF_SAFETY_N_HEALTH, 
			RoleType.CHIEF_OF_AGRICULTURE,
			RoleType.CHIEF_OF_SCIENCE
		};
	
	/**
	 * This class creates a chain of command structure for a settlement. A
	 * settlement can have either 3 divisions or 7 divisions organizational
	 * structure
	 * 
	 * @param settlement {@link Settlement}
	 */
	public ChainOfCommand(Settlement settlement) {
		this.settlement = settlement;
		roleRegistry = new ConcurrentHashMap<>();
	}

	/**
	 * Assigns a person with one of the three specialist role types
	 * 
	 * @param job {@link Job}
	 * @param person {@link Person}
	 * @param num
	 */
	public void assignRole(Job job, Person person, int num) {
		int safe = getNumFilled(RoleType.SAFETY_SPECIALIST);
		int r = getNumFilled(RoleType.RESOURCE_SPECIALIST);
		int e = getNumFilled(RoleType.ENGINEERING_SPECIALIST);

		// fill up a particular role in sequence without considering one's job type
		if (safe < num - 1) {
//			assignSpecialiststo3Divisions(person);
			person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
		} else if (e < num - 1) {
//			assignSpecialiststo3Divisions(person);
			person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
		} else if (r < num - 1) {
//			assignSpecialiststo3Divisions(person);
			person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
		}
		else {
			if (num < 4) {
				
				RoleType type = this.getMissingSpecialistRole(1);
				if (type != null) {
					person.getRole().setNewRoleType(type);
				}
				
				else {
					int least = Math.max(safe, Math.max(r, e));
					if (least == safe) {
						person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
					}
					else if (least == r) {
						person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
					}
					else if (least == e) {
						person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
					}
				}
			}
			else {
				
				RoleType type = this.getMissingSpecialistRole(1);
				if (type != null) {
					person.getRole().setNewRoleType(type);
				}
				
				else {
				
					int m = getNumFilled(RoleType.MISSION_SPECIALIST);
					int a = getNumFilled(RoleType.AGRICULTURE_SPECIALIST);
					int sci = getNumFilled(RoleType.SCIENCE_SPECIALIST);
					int l = getNumFilled(RoleType.LOGISTIC_SPECIALIST);
					
					int least = Math.max(safe, Math.max(r, Math.max(e, Math.max(m, Math.max(a, Math.max(sci, l))))));
					if (least == m) {
						person.getRole().setNewRoleType(RoleType.MISSION_SPECIALIST);
					}
					else if (least == a) {
						person.getRole().setNewRoleType(RoleType.AGRICULTURE_SPECIALIST);
					}
					else if (least == sci) {
						person.getRole().setNewRoleType(RoleType.SCIENCE_SPECIALIST);
					}
					else if (least == l) {
						person.getRole().setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
					}
					else if (least == safe) {
						person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
					}
					else if (least == r) {
						person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
					}
					else if (least == e) {
						person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
					}
				}
			}
			
//			int rand = RandomUtil.getRandomInt(2);
//			if (rand == 0) {
//				person.getRole().setNewRoleType(RoleType.SAFETY_SPECIALIST);
//			} else if (rand == 1) {
//				person.getRole().setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
//			} else  {
//				person.getRole().setNewRoleType(RoleType.RESOURCE_SPECIALIST);
//			}
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
				logger.warning("Impossible to have no associated people in " + person.getName() + "'s settlement.");
			}
		}

		RoleType type = null;

//		boolean allSlotsFilledOnce = false;
//		boolean allSlotsFilledTwice = false;
//		boolean allSlotsFilledTriple = false;
		
//		if (pop <= 4) {
//			boolean allSlotsFilledOnce = metMinimiumFilled(1);
//			if (!allSlotsFilledOnce) {
//				RoleType type = this.getMissingSpecialistRole(1);
//				person.getRole().setNewRoleType(type);	
//			}
//			else
//				logger.warning("With 4 or less people in " + person.getSettlement() 
//					+ ". All roles have been filled once.");
//		
//		}
//		
//		else if (pop <= 7) {
//			boolean allSlotsFilledTwice = metMinimiumFilled(2);
//			if (!allSlotsFilledTwice) {
//				RoleType type = this.getMissingSpecialistRole(2);
//				person.getRole().setNewRoleType(type);	
//			}
//			else
//				logger.warning("With 7 or less people in " + person.getSettlement() 
//					+ ". All roles have been filled twice.");
//		}
//		
//		else if (pop <= 10) {
//			boolean allSlotsFilledTriple = metMinimiumFilled(3);
//			if (!allSlotsFilledTriple) {
//				RoleType type = this.getMissingSpecialistRole(3);
//				person.getRole().setNewRoleType(type);	
//			}
//			else
//				logger.warning("With 10 or less people in " + person.getSettlement() 
//					+ ". All roles have been filled 3 times.");
//		}
//
//		else if (pop <= 14) {
//			// will have a sub-commander if pop > 12
//			boolean allSlotsFilledQuad = metMinimiumFilled(4);
//			if (!allSlotsFilledQuad) {
//				RoleType type = this.getMissingSpecialistRole(4);
//				person.getRole().setNewRoleType(type);	
//			}
//			else
//				logger.warning("With 10 or less people in " + person.getSettlement() 
//					+ ". All roles have been filled 3 times.");
//		}
		
//		TODO: make use of assignRole(job, person, 1);

			if (job.equals(JobManager.getJob(Architect.class.getSimpleName()))) {
//				if (type != null) { 
//					if (type == RoleType.SAFETY_SPECIALIST
//							|| type == RoleType.ENGINEERING_SPECIALIST) {
//						person.getRole().setNewRoleType(type);						
//					}
//					person.getRole().setNewRoleType(type);
//				}
				
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				}
			} 
			else if (job.equals(JobManager.getJob(Areologist.class.getSimpleName())))
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			else if (job.equals(JobManager.getJob(Astronomer.class.getSimpleName())))
				role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
			else if (job.equals(JobManager.getJob(Biologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} 
			
			else if (job.equals(JobManager.getJob(Botanist.class.getSimpleName())))
				role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			else if (job.equals(JobManager.getJob(Chef.class.getSimpleName())))
				role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			else if (job.equals(JobManager.getJob(Chemist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			} 
			
			else if (job.equals(JobManager.getJob(Doctor.class.getSimpleName())))
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
			} 
			
			else if (job.equals(JobManager.getJob(Meteorologist.class.getSimpleName())))
				role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			else if (job.equals(JobManager.getJob(Physicist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				else
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} 
			
			else if (job.equals(JobManager.getJob(Reporter.class.getSimpleName()))) {
				int num = RandomUtil.getRandomInt(1, 3);
				if (num == 1) {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				} else if (num == 2) {
						role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);	
				} else {
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				}
			} 
			
			else if (job.equals(JobManager.getJob(Technician.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.ENGINEERING_SPECIALIST);
				else
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
			} 
			
			else if (job.equals(JobManager.getJob(Trader.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1)
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				else
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
			}
			
			if (marsClock.getMissionSol() > 1)
				logger.config("[" + person.getLocationTag().getLocale() + "] Selecting " + person.getName()
				+ " the " + job.getClass().getSimpleName() + " to be " + person.getRole() + ".");
//		}
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
//		int pop = person.getSettlement().getNumCitizens();
		// int slot = (int) ((pop - 2 - 7 )/ 7);

//		boolean allSlotsFilledOnce = metMinimiumFilled(1);
//
//		boolean allSlotsFilledTwice = true;
//		if (pop >= 4)
//			allSlotsFilledTwice = metMinimiumFilled(2);
//
//		boolean allSlotsFilledTriple = true;
//		if (pop > 8)
//			allSlotsFilledTriple = metMinimiumFilled(3);
//
//		boolean allSlotsFilledQuad = true;
//		if (pop > 12)
//			allSlotsFilledQuad = metMinimiumFilled(4);
//
//		boolean allSlotsFilledPenta = true;
//		if (pop > 24)
//			allSlotsFilledPenta = metMinimiumFilled(5);
//
//		if (!allSlotsFilledOnce) {
//			assignRole(job, person, 1);
//		} else if (!allSlotsFilledTwice) {
//			assignRole(job, person, 2);
//		} else if (!allSlotsFilledTriple) {
//			assignRole(job, person, 3);
//		} else if (!allSlotsFilledQuad) {
//			assignRole(job, person, 4);
//		} else if (!allSlotsFilledPenta) {
//			assignRole(job, person, 5);
//		} 
//		
//		else {

			if (job.equals(JobManager.getJob(Architect.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.SAFETY_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Areologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Astronomer.class.getSimpleName()))) {
				role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
			} else if (job.equals(JobManager.getJob(Biologist.class.getSimpleName()))) {
				if (RandomUtil.getRandomInt(1, 2) == 1) {
					role.setNewRoleType(RoleType.AGRICULTURE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				}
			} else if (job.equals(JobManager.getJob(Botanist.class.getSimpleName()))) {
				int num = RandomUtil.getRandomInt(1, 3);
				if (num == 1) {
					role.setNewRoleType(RoleType.AGRICULTURE_SPECIALIST);
				} else if (num == 2) {
					role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
				} else {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
				}
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
				int num = RandomUtil.getRandomInt(1, 4);
				if (num == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else if (num == 2) {
					role.setNewRoleType(RoleType.SCIENCE_SPECIALIST);
				} else if (num == 3) {
					role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);
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
			} else if (job.equals(JobManager.getJob(Reporter.class.getSimpleName()))) {
				int num = RandomUtil.getRandomInt(1, 3);
				if (num == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else if (num == 2) {
						role.setNewRoleType(RoleType.SAFETY_SPECIALIST);	
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
				int num = RandomUtil.getRandomInt(1, 3);
				if (num == 1) {
					role.setNewRoleType(RoleType.MISSION_SPECIALIST);
				} else if (num == 2) {
						role.setNewRoleType(RoleType.RESOURCE_SPECIALIST);	
				} else {
					role.setNewRoleType(RoleType.LOGISTIC_SPECIALIST);
				}
			}
			if (marsClock.getMissionSol() > 1)
				logger.config("[" + person.getLocationTag().getLocale() + "] Selecting " + person.getName()
					+ " the " + job.getClass().getSimpleName() + " to be " + person.getRole() + ".");
//		}
	}

	/**
	 * Increments the number of the target role type in the map
	 * 
	 * @param key {@link RoleType}
	 */
	public void registerRole(RoleType key) {
		int value = getNumFilled(key);
		roleRegistry.put(key, value + 1);
	}

	/**
	 * Decrements the number of the target role type from the map
	 * 
	 * @param key {@link RoleType}
	 */
	public void releaseRole(RoleType key) {
		int value = getNumFilled(key);
		if (value != 0)
			roleRegistry.put(key, value - 1);
	}

	/**
	 * Elects a new person for leadership in a settlement if a mayor,
	 * commander, sub-commander, or chiefs vacates his/her position.
	 * 
	 * @param key {@link RoleType}
	 */
	public void reelectLeadership(RoleType key) {
		if (getNumFilled(key) == 0) {
			
			int popSize = settlement.getNumCitizens();
		
			if (popSize >= POPULATION_WITH_MAYOR) {	
				if (key == RoleType.CHIEF_OF_AGRICULTURE
						|| key == RoleType.CHIEF_OF_ENGINEERING
						|| key == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS
						|| key == RoleType.CHIEF_OF_MISSION_PLANNING
						|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH
						|| key == RoleType.CHIEF_OF_SCIENCE
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(settlement, key);
				} else if (key == RoleType.COMMANDER 
						|| key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				} else if (key == RoleType.MAYOR) {
					electMayor(settlement, key);
				}
			}
		
			else if (popSize >= POPULATION_WITH_SUB_COMMANDER) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE
						|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(settlement, key);
				} else if (key == RoleType.COMMANDER 
						|| key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				}
			}
		
			else {
				if (key == RoleType.COMMANDER 
						|| key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				}
			}
		}

	}

	/**
	 * Finds out the number of people already fill this roleType.
	 * 
	 * @param key
	 */
	public int getNumFilled(RoleType key) {
		int value = 0;
		if (roleRegistry.containsKey(key))
			value = roleRegistry.get(key);
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
	public boolean metMinimiumFilled(int minimum) {
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

	/**
	 * Checks if all the roleTypes in a settlement have been filled
	 * 
	 * @param minimum
	 */
	public RoleType getMissingSpecialistRole(int minimum) {
		if (has3Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) < minimum)
				return RoleType.SAFETY_SPECIALIST;
			else if (getNumFilled(RoleType.ENGINEERING_SPECIALIST) < minimum)
				return RoleType.ENGINEERING_SPECIALIST;
			else if (getNumFilled(RoleType.RESOURCE_SPECIALIST) < minimum)
				return RoleType.RESOURCE_SPECIALIST;
		} 
		
		else if (has7Divisions) {
			if (getNumFilled(RoleType.SAFETY_SPECIALIST) < minimum)
				return RoleType.SAFETY_SPECIALIST;
			else if (getNumFilled(RoleType.ENGINEERING_SPECIALIST) < minimum)
				return RoleType.ENGINEERING_SPECIALIST;
			else if (getNumFilled(RoleType.RESOURCE_SPECIALIST) < minimum)
				return RoleType.RESOURCE_SPECIALIST;
			else if (getNumFilled(RoleType.MISSION_SPECIALIST) < minimum)
				return RoleType.MISSION_SPECIALIST;
			else if (getNumFilled(RoleType.AGRICULTURE_SPECIALIST) < minimum)
				return RoleType.AGRICULTURE_SPECIALIST;
			else if (getNumFilled(RoleType.SCIENCE_SPECIALIST) < minimum)
				return RoleType.SCIENCE_SPECIALIST;
			else if (getNumFilled(RoleType.LOGISTIC_SPECIALIST) < minimum)
				return RoleType.LOGISTIC_SPECIALIST;
		}
		
		return null;
	}
	
	/**
	 * Elect the commanders
	 * 
	 * @param settlement
	 * @param role
	 * @param pop
	 */
	public void electCommanders(Settlement settlement, int pop) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person cc = null;
		int cc_leadership = 0;
		int cc_combined = 0;

		Person cv = null;
		int cv_leadership = 0;
		int cv_combined = 0;
		// compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttributeType.LEADERSHIP);
			int p_combined = 3 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
					+ 2 * mgr.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY)
					+ mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
					+ mgr.getAttribute(NaturalAttributeType.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > cc_leadership) {
				if (pop >= POPULATION_WITH_SUB_COMMANDER) {
					cv_leadership = cc_leadership;
					cv = cc;
					cv_combined = cc_combined;
				}
				cc = p;
				cc_leadership = p_leadership;
				cc_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == cc_leadership) {
				// if this person p has a higher combined score than the
				// previous cc
				if (p_combined > cc_combined) {
					// this person becomes the cc
					if (pop >= POPULATION_WITH_SUB_COMMANDER) {
						cv = cc;
						cv_leadership = cc_leadership;
						cv_combined = cc_combined;
					}
					cc = p;
					cc_leadership = p_leadership;
					cc_combined = p_combined;
				}
//				 else { 
					 // if this person p has a lower combined score than previous cc 
					 // but have a higher leadership score than the previous cv 
//					 if (pop >= POPULATION_WITH_SUB_COMMANDER) {
//						 if ( p_leadership > cv_leadership) { 
//							// this person p becomes the sub-commander 
//							cv = p; 
//							cv_leadership = p_leadership;
//							cv_combined = p_combined; 
//						 }
//						 	
//						 else if ( p_leadership == cv_leadership) {
//						 	if (p_combined > cv_combined) {
//								cv = p; 
//								cv_leadership = p_leadership; 
//								cv_combined = p_combined; 
//							} 
//						}
//					}
//				 }
			}
			else if (pop >= POPULATION_WITH_SUB_COMMANDER) {

				if (p_leadership > cv_leadership) {
					// this person p becomes the sub-commander
					cv = p;
					cv_leadership = p_leadership;
					cv_combined = p_combined;
				} 
				else if (p_leadership == cv_leadership) {
					// compare person p's combined score with the cv's combined
					// score
					if (p_combined > cv_combined) {
						cv = p;
						cv_leadership = p_leadership;
						cv_combined = p_combined;
					}
				}

			}
		}
		// TODO: look at other attributes and/or skills when comparing
		// individuals
		
		// Check if this settlement is the designated one for the user proposed commander
		if (settlement.hasDesignatedCommander()) {
			unitManager.updateCommander(cc);
			cc.setAssociatedSettlement(settlement.getIdentifier());
			logger.config("[" + cc.getLocationTag().getLocale() + "] " + cc + " will be assigned as the settlement's commander.");
		}
		
		else {
			cc.setRole(RoleType.COMMANDER);
			logger.config("[" + cc.getLocationTag().getLocale() + "] " + cc + " got elected as the " + RoleType.COMMANDER.getName() + ".");
		}
		
//		if (isProfileRetrieved) {
//			cc.setRole(RoleType.COMMANDER);
//		}
//		
//		else {
//			
//			String newCountry = personConfig.getCountry(getCountry()); 
//			String newSponsor = personConfig.getSponsorFromCountry(newCountry);
//			
//			// If the user's commander has the sponsor that match this settlement's sponsor
//			if (settlement.getSponsor().equals(newSponsor) || settlement.goCommander()) {
////				String oldName = cc.getName();
////				GenderType oldGender = cc.getGender();			
//				String newName = getFullname();
//				String newGender = getGender();
//
//				// Replace the commander 
//				cc.setName(newName);
//				cc.setGender(newGender);
//				cc.changeAge(getAge());
//				cc.setRole(RoleType.COMMANDER);
//				setJob(cc, getJob());
//				cc.setCountry(newCountry);
//				cc.setSponsor(newSponsor);		
//				isProfileRetrieved = true;
//				
//			}
//		}
		
		if (pop >= POPULATION_WITH_SUB_COMMANDER) {
			cv.setRole(RoleType.SUB_COMMANDER);
			logger.config("[" + cv.getLocationTag().getLocale() + "] " + cv + " got elected as the " + RoleType.SUB_COMMANDER.getName() + ".");
		}
	}
	
	/**
	 * Establish or reset the system of governance at a settlement.
	 * 
	 * @param settlement the settlement.
	 */
	public void establishSettlementGovernance(Settlement settlement) {

		int popSize = settlement.getNumCitizens();
		
		if (popSize >= POPULATION_WITH_MAYOR) {
			// Elect a mayor
			electMayor(settlement, RoleType.MAYOR);
			// Elect commander and sub-commander
			electCommanders(settlement, popSize);
			// Elect chiefs
			for (int i = 0; i < popSize - POPULATION_WITH_CHIEFS + 1; i++) {
				if (i <= 6 && getNumFilled(CHIEFS_7[i]) == 0)
					electChief(settlement, CHIEFS_7[i]);
			}

//			establishGovernment(settlement);
		}
		
		else if (popSize >= POPULATION_WITH_COMMANDER) {
			// Elect commander and sub-commander
			electCommanders(settlement, popSize);
			// pop < POPULATION_WITH_MAYOR
			if (popSize >= POPULATION_WITH_CHIEFS) {
				// Elect chiefs
				for (int i = 0; i < popSize - POPULATION_WITH_CHIEFS + 1; i++) {
					if (i <= 6 && getNumFilled(CHIEFS_7[i]) == 0)
						electChief(settlement, CHIEFS_7[i]);
				}
			}
		}
	}
	

	/**
	 * Establish the mayor in a settlement
	 * 
	 * @param settlement
	 * @param role
	 */
	public void electMayor(Settlement settlement, RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person mayorCandidate = null;
		int m_leadership = 0;
		int m_combined = 0;
		// Compare their leadership scores
		for (Person p : people) {
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			int p_leadership = mgr.getAttribute(NaturalAttributeType.LEADERSHIP);
			int p_tradeSkill = 5 * p.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
			p_leadership = p_leadership + p_tradeSkill;
			int p_combined = mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
					+ 3 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
					+ mgr.getAttribute(NaturalAttributeType.CONVERSATION);
			// if this person p has a higher leadership score than the previous
			// cc
			if (p_leadership > m_leadership) {
				m_leadership = p_leadership;
				mayorCandidate = p;
				m_combined = p_combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (p_leadership == m_leadership) {
				// if this person p has a higher combined score in those 4
				// categories than the previous cc
				if (p_combined > m_combined) {
					// this person becomes the cc
					m_leadership = p_leadership;
					mayorCandidate = p;
					m_combined = p_combined;
				}
			}
		}

		if (mayorCandidate != null) {
			mayorCandidate.setRole(RoleType.MAYOR);
			logger.config("[" + mayorCandidate.getLocationTag().getLocale() + "] " + mayorCandidate + " got elected as the " + role.getName() + ".");
		}
	}

	/**
	 * Establish the chiefs in a settlement
	 * 
	 * @param settlement
	 * @param role
	 */
	public void electChief(Settlement settlement, RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();

		RoleType specialty = null;
		Person winner = null;
		int c_skills = 0;
		int c_combined = 0;

		SkillType skill_1 = null;
		SkillType skill_2 = null;
		SkillType skill_3 = null;
		SkillType skill_4 = null;
		if (role == RoleType.CHIEF_OF_ENGINEERING) {
			skill_1 = SkillType.MATERIALS_SCIENCE;
			skill_2 = SkillType.CONSTRUCTION;
			skill_3 = SkillType.PHYSICS;
			skill_4 = SkillType.MECHANICS;
			specialty = RoleType.ENGINEERING_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_AGRICULTURE) {
			skill_1 = SkillType.BOTANY;
			skill_2 = SkillType.BIOLOGY;
			skill_3 = SkillType.CHEMISTRY;
			skill_4 = SkillType.TRADING;
			specialty = RoleType.AGRICULTURE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SAFETY_N_HEALTH) {
			skill_1 = SkillType.EVA_OPERATIONS;
			skill_2 = SkillType.MEDICINE;
			skill_3 = SkillType.COOKING;
			skill_4 = SkillType.CONSTRUCTION;
			specialty = RoleType.SAFETY_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SCIENCE) {
			skill_1 = SkillType.AREOLOGY;
			skill_2 = SkillType.CHEMISTRY;
			skill_3 = SkillType.PHYSICS;
			skill_4 = SkillType.MATHEMATICS;
			specialty = RoleType.SCIENCE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_MISSION_PLANNING) {
			skill_1 = SkillType.MATHEMATICS;
			skill_2 = SkillType.DRIVING;
			skill_3 = SkillType.CONSTRUCTION;
			skill_4 = SkillType.EVA_OPERATIONS;
			specialty = RoleType.MISSION_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
			skill_1 = SkillType.TRADING;
			skill_2 = SkillType.MATHEMATICS;
			skill_3 = SkillType.BOTANY;
			skill_4 = SkillType.COOKING;
			specialty = RoleType.RESOURCE_SPECIALIST;
		} else if (role == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS) {
			skill_1 = SkillType.DRIVING;
			skill_2 = SkillType.METEOROLOGY;
			skill_3 = SkillType.AREOLOGY;
			skill_4 = SkillType.MATHEMATICS;
			specialty = RoleType.LOGISTIC_SPECIALIST;
		}

		// compare their scores
		for (Person p : people) {
			SkillManager skillMgr = p.getMind().getSkillManager();
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			if (p.getRole().getType() == specialty) {
				// && (p.getRole().getType() != RoleType.COMMANDER)
				// && (p.getRole().getType() != RoleType.SUB_COMMANDER)) {

				int p_skills = 6 * skillMgr.getEffectiveSkillLevel(skill_1)
						+ 5 * skillMgr.getEffectiveSkillLevel(skill_2) + 4 * skillMgr.getEffectiveSkillLevel(skill_3)
						+ 3 * skillMgr.getEffectiveSkillLevel(skill_4);

				int p_combined = mgr.getAttribute(NaturalAttributeType.LEADERSHIP)
						+ mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
						+ skillMgr.getEffectiveSkillLevel(SkillType.MANAGEMENT);
				// if this person p has a higher experience score than the
				// previous cc
				if (p_skills > c_skills) {
					c_skills = p_skills;
					winner = p;
					c_combined = p_combined;
				}
				// if this person p has the same experience score as the
				// previous chief
				else if (p_skills == c_skills) {
					// if this person p has a higher combined score in those 4
					// categories than the previous chief
					if (p_combined > c_combined) {
						// this person becomes the chief
						c_skills = p_skills;
						winner = p;
						c_combined = p_combined;
					}
				}
			}
		}
		
		if (winner != null) {
			winner.setRole(role);
			logger.config("[" + winner.getLocationTag().getLocale() + "] " + winner + " got elected as the " + role.getName() + ".");
		}
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 * @param um
	 */
	public static void initializeInstances(MarsClock clock, UnitManager um) {
		marsClock = clock;
		unitManager = um;
	}
	
	public void destroy() {
		roleRegistry.clear();
		roleRegistry = null;
		settlement = null;
	}

}
