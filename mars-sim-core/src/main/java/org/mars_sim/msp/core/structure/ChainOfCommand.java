/**
 * Mars Simulation Project
 * ChainOfCommand.java
 * @version 3.1.0 2017-03-11
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.time.MarsClock;

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
	/** Stores the number for each role. */
	private Map<RoleType, Integer> roleRegistry;
	/** Store the availability of each role. */
	private Map<RoleType, Integer> roleAvailability;
	
	private Settlement settlement;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();

//	private static MarsClock marsClock = Simulation.instance().getMasterClock().getMarsClock();

//	private RoleType[] CHIEFS_3 = new RoleType[] { 
//			RoleType.CHIEF_OF_ENGINEERING,  
//			RoleType.CHIEF_OF_MISSION_PLANNING, 
//			RoleType.CHIEF_OF_SUPPLY_N_RESOURCES,
//		};

	public RoleType[] CHIEFS_7 = new RoleType[] { 
			RoleType.CHIEF_OF_AGRICULTURE,
			RoleType.CHIEF_OF_ENGINEERING, 
			RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS, 
			RoleType.CHIEF_OF_MISSION_PLANNING,
			RoleType.CHIEF_OF_SAFETY_N_HEALTH, 
			RoleType.CHIEF_OF_SCIENCE,
			RoleType.CHIEF_OF_SUPPLY_N_RESOURCES};

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
		roleAvailability = new ConcurrentHashMap<>();
		
		has7Divisions = true;
		
		// Initialize roleAvailability array once only
		if (roleAvailability.isEmpty())
			initializeRoleMaps();
	}

	/**
	 * Initialize the role maps
	 */
	public void initializeRoleMaps() {
		int pop = settlement.getInitialPopulation();
//		System.out.println("" + settlement + " : " + pop);
		
		RoleType[] types = RoleUtil.specialistRoles;
		
		// Shuffle the role types randomize
		Collections.shuffle(Arrays.asList(types));	
		
		for (RoleType t : types) {
			if (!roleAvailability.containsKey(t)) {
				roleAvailability.put(t, 0);
			}
			
			if (!roleRegistry.containsKey(t))
				roleRegistry.put(t, 0);
		}
		
		for (int i=0; i < pop; i++) {
			if (i < 7) {
				int num = roleAvailability.get(types[i]);
				roleAvailability.put(types[i], num+1);
//				System.out.println("1. " + types[i] + " : " + (num+1));
			}
			else {
				int num = roleAvailability.get(types[i%7]);
				roleAvailability.put(types[i%7], num+1);
//				System.out.println("2. " + types[i%7] + " : " + (num+1));
			}
		}
	}
	
	/**
	 * Is the given role type available
	 * 
	 * @param type
	 * @return
	 */
	public boolean isRoleAvailable(RoleType type) {
		if (roleAvailability.get(type) > roleRegistry.get(type))
			return true;
		else
			return false;
	}
	
	/**
	 * Gets the role availability map
	 * 
	 * @return
	 */
	public Map<RoleType, Integer> getRoleAvailability() {
		return roleAvailability;
	}

//	/**
//	 * Assigns a person with a specialist roleType in 7-division settlement
//	 * 
//	 * @param person {@link Person}
//	 */
//	public void assignSpecialistRole(Person person) {
//		
//		// Initialize roleAvailability array once only
//		if (roleAvailability.isEmpty())
//			initializeRoleMaps();
//		
//		RoleType roleType = RoleUtil.findBestRole(person);
//				
//		// Finalize setting a person's new role
//		person.getRole().setNewRoleType(roleType);
//		
//	}

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
		if (value <= 0)
			roleRegistry.put(key, value - 1);
	}

	/**
	 * Elects a new person for leadership in a settlement if a mayor, commander,
	 * sub-commander, or chiefs vacates his/her position.
	 * 
	 * @param key {@link RoleType}
	 */
	public void reelectLeadership(RoleType key) {
		if (getNumFilled(key) == 0) {

			int popSize = settlement.getNumCitizens();

			if (popSize >= POPULATION_WITH_MAYOR) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE || key == RoleType.CHIEF_OF_ENGINEERING
						|| key == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS || key == RoleType.CHIEF_OF_MISSION_PLANNING
						|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH || key == RoleType.CHIEF_OF_SCIENCE
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(settlement, key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				} else if (key == RoleType.MAYOR) {
					electMayor(settlement, key);
				}
			}

			else if (popSize >= POPULATION_WITH_CHIEFS) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE || key == RoleType.CHIEF_OF_ENGINEERING
						|| key == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS || key == RoleType.CHIEF_OF_MISSION_PLANNING
						|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH || key == RoleType.CHIEF_OF_SCIENCE
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(settlement, key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				}

			}
			
			else if (popSize >= POPULATION_WITH_SUB_COMMANDER) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE || key == RoleType.CHIEF_OF_SAFETY_N_HEALTH
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(settlement, key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(settlement, pop);
				}
			}

			else {
				if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
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
			} else if (pop >= POPULATION_WITH_SUB_COMMANDER) {

				if (p_leadership > cv_leadership) {
					// this person p becomes the sub-commander
					cv = p;
					cv_leadership = p_leadership;
					cv_combined = p_combined;
				} else if (p_leadership == cv_leadership) {
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

		// Check if this settlement is the designated settlement for 
		// housing the player commander
		if (settlement.hasDesignatedCommander()) {
			unitManager.updateCommander(cc);
			cc.setAssociatedSettlement(settlement.getIdentifier());
			logger.config("[" + cc.getLocationTag().getLocale() + "] " + cc
					+ " had been assigned as the commander of " + settlement + ".");

			// Determine the initial leadership points
			determineLeadershipPoints(cc);
		}

		else {
			cc.setRole(RoleType.COMMANDER);
			logger.config("[" + cc.getLocationTag().getLocale() + "] " 
					+ cc + " got elected as the "
					+ RoleType.COMMANDER.getName() + ".");
		}

		if (pop >= POPULATION_WITH_SUB_COMMANDER) {
			cv.setRole(RoleType.SUB_COMMANDER);
			logger.config("[" + cv.getLocationTag().getLocale() + "] " 
					+ cv + " got elected as the "
					+ RoleType.SUB_COMMANDER.getName() + ".");
		}
	}

	/**
	 * Initialize the leadership points
	 * 
	 * @param person
	 */
	public void determineLeadershipPoints(Person person) {
		int leadershipAptitude = person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
		SimulationConfig.instance().getPersonConfig().getCommander()
				.setInitialLeadershipPoint(leadershipAptitude);
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
			int p_tradeSkill = 5 * p.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING);
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
			logger.config("[" + mayorCandidate.getLocationTag().getLocale() + "] "
					+ mayorCandidate
					+ " got elected as the " + role.getName() + ".");
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
			skill_2 = SkillType.PILOTING;
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
			skill_1 = SkillType.PILOTING;
			skill_2 = SkillType.METEOROLOGY;
			skill_3 = SkillType.AREOLOGY;
			skill_4 = SkillType.MATHEMATICS;
			specialty = RoleType.LOGISTIC_SPECIALIST;
		}

		// compare their scores
		for (Person p : people) {
			SkillManager skillMgr = p.getSkillManager();
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
			logger.config("[" + winner.getLocationTag().getLocale() + "] " 
					+ winner + " got elected as the "
					+ role.getName() + ".");
		}
	}

	/**
	 * Finds a list of people with a particular role
	 * 
	 * @param role
	 * @return List<Person>
	 */
	public List<Person> findPeopleWithRole(RoleType role) {
		List<Person> people = new ArrayList<>();
		for (Person p : settlement.getAllAssociatedPeople()) {
			if (p.getRole().getType() == role)
				people.add(p);
		}
		
		return people;
	}
	
	/**
	 * Reloads instances after loading from a saved sim
	 * 
	 * @param clock
	 * @param um
	 */
	public static void initializeInstances(MarsClock clock, UnitManager um) {
//		marsClock = clock;
		unitManager = um;
	}

	public void destroy() {
		roleRegistry.clear();
		roleRegistry = null;
		settlement = null;
	}

}
