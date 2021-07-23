/**
 * Mars Simulation Project
 * ChainOfCommand.java
 * @version 3.2.0 2021-06-20
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
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;

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

//	private boolean has7Divisions = false;
//	private boolean has3Divisions = false;
	/** Stores the number for each role. */
	private Map<RoleType, Integer> roleRegistry;
	/** Store the availability of each role. */
	private Map<RoleType, Integer> roleAvailability;
	
	private Settlement settlement;


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
		
//		has7Divisions = true;
		
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
		
		RoleType[] types = RoleUtil.SPECIALISTS;
		
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
//			if (i < RoleType.SEVEN) {
//				int num = roleAvailability.get(types[i]);
//				roleAvailability.put(types[i], num+1);
////				System.out.println("1. " + types[i] + " : " + (num+1));
//			}
//			else {
				int num = roleAvailability.get(types[i%7]);
				roleAvailability.put(types[i%7], num+1);
//				System.out.println("2. " + types[i%7] + " : " + (num+1));
//			}
		}
	}
	
	/**
	 * Is the given role type available
	 * 
	 * @param type
	 * @return
	 */
	public boolean isRoleAvailable(RoleType type) {
        return roleAvailability.get(type) > roleRegistry.get(type);
	}
	
//	public boolean areAllRolesFilled() {
//		int numSpecialists;
//		int pop = settlement.getNumCitizens();
//		if (pop > RoleType.SEVEN)
//			return false;
//	}
	
	/**
	 * Gets the role availability map
	 * 
	 * @return
	 */
	public Map<RoleType, Integer> getRoleAvailability() {
		return roleAvailability;
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
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				} else if (key == RoleType.MAYOR) {
					electMayor(key);
				}
			}

			else if (popSize >= POPULATION_WITH_CHIEFS) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE || key == RoleType.CHIEF_OF_ENGINEERING
						|| key == RoleType.CHIEF_OF_LOGISTICS_N_OPERATIONS || key == RoleType.CHIEF_OF_MISSION_PLANNING
						|| key == RoleType.CHIEF_OF_SAFETY_N_HEALTH || key == RoleType.CHIEF_OF_SCIENCE
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}

			}
			
			else if (popSize >= POPULATION_WITH_SUB_COMMANDER) {
				if (key == RoleType.CHIEF_OF_AGRICULTURE || key == RoleType.CHIEF_OF_SAFETY_N_HEALTH
						|| key == RoleType.CHIEF_OF_SUPPLY_N_RESOURCES) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}
			}

			else {
				if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
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

//	/**
//	 * Sets this settlement to have 3 divisions
//	 */
//	public void set3Divisions(boolean value) {
//		has3Divisions = value;
//	}
//
//	/**
//	 * Sets this settlement to have 7 divisions
//	 */
//	public void set7Divisions(boolean value) {
//		has7Divisions = value;
//	}

//	/**
//	 * Checks if all the roleTypes in a settlement have been filled
//	 * 
//	 * @param minimum
//	 */
//	public boolean metMinimiumFilled(int minimum) {
//		boolean result = false;
//		if (has3Divisions) {
//			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= minimum)
//				result = true;
//		} else if (has7Divisions) {
//			if (getNumFilled(RoleType.SAFETY_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.ENGINEERING_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.RESOURCE_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.MISSION_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.AGRICULTURE_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.SCIENCE_SPECIALIST) >= minimum
//					&& getNumFilled(RoleType.LOGISTIC_SPECIALIST) >= minimum)
//				result = true;
//		}
//		return result;
//	}

//	/**
//	 * Checks if all the roleTypes in a settlement have been filled
//	 * 
//	 * @param minimum
//	 */
//	public RoleType getMissingSpecialistRole(int minimum) {
//		if (has3Divisions) {
//			if (getNumFilled(RoleType.SAFETY_SPECIALIST) < minimum)
//				return RoleType.SAFETY_SPECIALIST;
//			else if (getNumFilled(RoleType.ENGINEERING_SPECIALIST) < minimum)
//				return RoleType.ENGINEERING_SPECIALIST;
//			else if (getNumFilled(RoleType.RESOURCE_SPECIALIST) < minimum)
//				return RoleType.RESOURCE_SPECIALIST;
//		}
//
//		else if (has7Divisions) {
//			if (getNumFilled(RoleType.SAFETY_SPECIALIST) < minimum)
//				return RoleType.SAFETY_SPECIALIST;
//			else if (getNumFilled(RoleType.ENGINEERING_SPECIALIST) < minimum)
//				return RoleType.ENGINEERING_SPECIALIST;
//			else if (getNumFilled(RoleType.RESOURCE_SPECIALIST) < minimum)
//				return RoleType.RESOURCE_SPECIALIST;
//			else if (getNumFilled(RoleType.MISSION_SPECIALIST) < minimum)
//				return RoleType.MISSION_SPECIALIST;
//			else if (getNumFilled(RoleType.AGRICULTURE_SPECIALIST) < minimum)
//				return RoleType.AGRICULTURE_SPECIALIST;
//			else if (getNumFilled(RoleType.SCIENCE_SPECIALIST) < minimum)
//				return RoleType.SCIENCE_SPECIALIST;
//			else if (getNumFilled(RoleType.LOGISTIC_SPECIALIST) < minimum)
//				return RoleType.LOGISTIC_SPECIALIST;
//		}
//
//		return null;
//	}

	/**
	 * Elect the commanders
	 * 
	 * @param pop
	 */
	private void electCommanders(int pop) {
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
			PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
			Commander commander = personConfig.getCommander();
			updateCommander(cc, commander);
			cc.setAssociatedSettlement(settlement.getIdentifier());
			logger.config("[" + cc.getLocationTag().getLocale() + "] " + cc
					+ " had been assigned as the commander of " + settlement + ".");

			// Determine the initial leadership points
			int leadershipAptitude = cc.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
			commander.setInitialLeadershipPoint(leadershipAptitude);
		}

		else {
			cc.setRole(RoleType.COMMANDER);
			
//			RoleUtil.setNewRole(cc, RoleType.COMMANDER);	
		}

		if (pop >= POPULATION_WITH_SUB_COMMANDER) {
			
			cv.setRole(RoleType.SUB_COMMANDER);
			
//			RoleUtil.setNewRole(cv, RoleType.SUB_COMMANDER);
		}
	}
	
	/**
	 * Apply the Commander profile to the Person with the COMMANDER role.
	 * @param profile
	 * @return
	 */
	public Person applyCommander(Commander profile) {
		List<Person> commanders = findPeopleWithRole(RoleType.COMMANDER);
		if (commanders.isEmpty()) {
			return null;
		}
		Person commander = commanders.get(0);
		updateCommander(commander, profile);
		
		logger.info("[" + settlement.getName() + "] "
						+ commander.getName() + " selected as the commander.");	
		Simulation.instance().getUnitManager().setCommanderId(commander.getIdentifier());
		
		return commander;
	}
	
	/**
	 * Update the commander's profile
	 * 
	 * @param cc the person instance
	 */
	private void updateCommander(Person cc, Commander commander) {

		// Replace the commander 
		cc.setName(commander.getFullName());
		cc.setGender((commander.getGender().equalsIgnoreCase("M")
							? GenderType.MALE : GenderType.FEMALE));
		cc.changeAge(commander.getAge());
		cc.setJob(JobType.valueOf(commander.getJob().toUpperCase()), JobUtil.MISSION_CONTROL);
		logger.config(commander.getFullName() + " accepted the role of being a Commander by the order of the Mission Control.");
		cc.setRole(RoleType.COMMANDER);
		cc.setCountry(commander.getCountryStr());
		cc.setSponsor(commander.getSponsorStr());		
	}

	/**
	 * Establish or reset the system of governance of a settlement.
	 * 
	 */
	public void establishSettlementGovernance() {
		// Elect commander, subcommander, mayor
		establishTopLeadership();
		
		// Assign a role to each person
		assignRoles();
		
		// Elect chiefs
		establishChiefs();
	}
	
	/**
	 * Assign a role to each person
	 * 
	 * @param settlement
	 */
	private void assignRoles() {
		// Assign roles to each person
		List<Person> oldlist = new ArrayList<>(settlement.getAllAssociatedPeople());
		List<Person> plist = new ArrayList<>();
		
		// If a person does not have a (specialist) role, add him/her to the list
		for (Person p: oldlist) {
			if (p.getRole().getType() == null)
				plist.add(p);
		}
			
		while (plist.size() > 0) {
			List<RoleType> roleList = new ArrayList<>(
							Arrays.asList(RoleUtil.SPECIALISTS));
			// Randomly reorient the order of roleList so that the 
			// roles to go in different order each time 
			Collections.shuffle(roleList);
			
			for (RoleType r : roleList) {
				Person p = RoleUtil.findBestFit(r, plist);
				p.setRole(r);
				plist.remove(p);
				if (plist.isEmpty()) 
					return;
			}
		}
	}
	
	/**
	 * Establish the top leadership of a settlement.
	 * 
	 * @param settlement the settlement.
	 */
	private void establishTopLeadership() {

		int popSize = settlement.getNumCitizens();

		if (popSize >= POPULATION_WITH_MAYOR) {
			// Elect a mayor
			electMayor(RoleType.MAYOR);
			// Elect commander and sub-commander
			electCommanders(popSize);
		}
		// for pop < POPULATION_WITH_MAYOR
		else if (popSize >= POPULATION_WITH_COMMANDER) {
			// Elect commander and sub-commander
			electCommanders(popSize);
		}
	}

	/**
	 * Establish the chiefs of a settlement.
	 * 
	 * @param settlement the settlement.
	 */
	private void establishChiefs() {

		int popSize = settlement.getNumCitizens();
		RoleType [] chiefs = RoleUtil.CHIEFS;
		int numChiefs = chiefs.length;
				
		if (popSize >= POPULATION_WITH_MAYOR) {
			// Elect chiefs
			for (int i = 0; i < popSize - POPULATION_WITH_CHIEFS + 1; i++) {
				if (i < numChiefs && getNumFilled(chiefs[i]) == 0)
					electChief(chiefs[i]);
			}
		}

		else if (popSize >= POPULATION_WITH_COMMANDER) {
			if (popSize >= POPULATION_WITH_CHIEFS) {
				// Elect chiefs
				for (int i = 0; i < popSize - POPULATION_WITH_CHIEFS + 1; i++) {
					if (i < numChiefs && getNumFilled(chiefs[i]) == 0)
						electChief(chiefs[i]);
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
	private void electMayor(RoleType role) {
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
			
//			RoleUtil.setNewRole(mayorCandidate, RoleType.MAYOR);		
		}
	}

	/**
	 * Establish the chiefs in a settlement
	 * 
	 * @param settlement
	 * @param role
	 */
	private void electChief(RoleType role) {
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
			skill_3 = SkillType.MANAGEMENT;
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
			skill_3 = SkillType.MECHANICS;
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
		
//			RoleUtil.setNewRole(winner, role);
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
	
	public void destroy() {
		roleRegistry.clear();
		roleRegistry = null;
		settlement = null;
	}


}
