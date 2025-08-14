/*
 * Mars Simulation Project
 * ChainOfCommand.java
 * @date 2025-08-13
 * @author Manny Kung
 */

package com.mars_sim.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.person.Commander;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PersonConfig;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.ai.role.RoleUtil;

/**
 * The ChainOfCommand class creates and assigns a person a role type based on
 * one's job type and the size of the settlement
 */
public class ChainOfCommand implements Serializable {
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ChainOfCommand.class.getName());

	public static final int POPULATION_WITH_COMMANDER = 4;
	public static final int POPULATION_WITH_SUB_COMMANDER = 12;
	public static final int POPULATION_WITH_CHIEFS = 24;
	public static final int POPULATION_WITH_ADMINISTRATOR = 120;
	public static final int POPULATION_WITH_MAYOR = 480;
	public static final int POPULATION_WITH_PRESIDENT = 1024;
	
	
	/** Stores the number for each role. */
	private Map<RoleType, Integer> roleRegistry;
	/** Store the availability of each role. */
	private Map<RoleType, Integer> roleAvailability;
	/** The settlement of interest. */
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

		roleRegistry = new HashMap<>();
		roleAvailability = new HashMap<>();

		// Initialize roleAvailability array once only
		if (roleAvailability.isEmpty())
			initializeRoleMaps();
	}

	/**
	 * Initializes the role maps.
	 */
	public void initializeRoleMaps() {
		int pop = settlement.getInitialPopulation();

		List<RoleType> roles = null;

		if (pop > POPULATION_WITH_COMMANDER) {
			roles = new ArrayList<>(RoleUtil.getSpecialists());
		}
		else {
			roles = new ArrayList<>(RoleUtil.getCrewRoles());
		}
		
		// Shuffle the role types randomize
		Collections.shuffle(roles);

		for (RoleType t : roles) {
			if (!roleAvailability.containsKey(t)) {
				roleAvailability.put(t, 0);
			}

			if (!roleRegistry.containsKey(t))
				roleRegistry.put(t, 0);
		}

		for (int i=0; i < pop; i++) {
			RoleType rt = roles.get(i % roles.size());
			int num = roleAvailability.get(rt);
			roleAvailability.put(rt, num+1);
		}
	}

	/**
	 * Is the given role type available ?
	 *
	 * @param type
	 * @return
	 */
	public boolean isRoleAvailable(RoleType type) {
        return roleAvailability.get(type) > roleRegistry.get(type);
	}

	/**
	 * Gets the role availability map.
	 *
	 * @return
	 */
	public Map<RoleType, Integer> getRoleAvailability() {
		return roleAvailability;
	}

	/**
	 * Increments the number of the target role type in the map.
	 *
	 * @param key {@link RoleType}
	 */
	public void registerRole(RoleType key) {
		int value = getNumFilled(key);
		roleRegistry.put(key, value + 1);
	}

	/**
	 * Decrements the number of the target role type from the map.
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
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				} else if (key == RoleType.MAYOR) {
					electLeader(key);
				}
			}
			
			else if (popSize >= POPULATION_WITH_ADMINISTRATOR) {
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				} else if (key == RoleType.ADMINISTRATOR) {
					electLeader(key);
				}
			}

			else if (popSize >= POPULATION_WITH_CHIEFS) {
				if (key.isChief()) {
					electChief(key);
				} else if (key == RoleType.COMMANDER || key == RoleType.SUB_COMMANDER) {
					int pop = settlement.getNumCitizens();
					electCommanders(pop);
				}
			}

			else if (popSize >= POPULATION_WITH_SUB_COMMANDER) {
				if (key.isChief()) {
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

	/**
	 * Computes the attribute composite score.
	 * 
	 * @param mgr
	 * @param p
	 * @return
	 */
	private double computeCompositeScore(NaturalAttributeManager mgr, Person p) {
		return 2 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.ORGANIZATION)
				+ 3 * mgr.getAttribute(NaturalAttributeType.STRESS_RESILIENCE)
				
				+ 3 * mgr.getAttribute(NaturalAttributeType.COURAGE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.DISCIPLINE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY)
				
				+ 2 * mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
				+ mgr.getAttribute(NaturalAttributeType.CONVERSATION);
	}
	
	/**
	 * Elects the commander. Possibly with a sub-commander.
	 *
	 * @param pop
	 */
	private void electCommanders(int pop) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person bestCandidate = null;
		int bestLeadership = 0;
		int bestCombined = 0;

		Person secondCandidate = null;
		int secondLeadership = 0;
		int seconCombined = 0;
		// compare their leadership scores
		for (Person candidate : people) {
			NaturalAttributeManager mgr = candidate.getNaturalAttributeManager();
			int leadership = mgr.getAttribute(NaturalAttributeType.LEADERSHIP);
			int combined = (int)(Math.round(computeCompositeScore(mgr, candidate)) / 20);

			if (leadership > bestLeadership) {
				if (pop >= POPULATION_WITH_SUB_COMMANDER) {
					secondLeadership = bestLeadership;
					secondCandidate = bestCandidate;
					seconCombined = bestCombined;
				}
				bestCandidate = candidate;
				bestLeadership = leadership;
				bestCombined = combined;
			}
			// if this person p has the same leadership score as the previous cc
			else if (leadership == bestLeadership) {
				// if this person p has a higher combined score than the
				// previous cc
				if (combined > bestCombined) {
					// this person becomes the cc
					if (pop >= POPULATION_WITH_SUB_COMMANDER) {
						secondCandidate = bestCandidate;
						secondLeadership = bestLeadership;
						seconCombined = bestCombined;
					}
					bestCandidate = candidate;
					bestLeadership = leadership;
					bestCombined = combined;
				}

			} else if (pop >= POPULATION_WITH_SUB_COMMANDER) {

				if (leadership > secondLeadership) {
					// this person p becomes the sub-commander
					secondCandidate = candidate;
					secondLeadership = leadership;
					seconCombined = combined;
				} else if (leadership == secondLeadership) {
					// compare person p's combined score with the cv's combined
					// score
					if (combined > seconCombined) {
						secondCandidate = candidate;
						secondLeadership = leadership;
						seconCombined = combined;
					}
				}
			}
		}
		// Note: look at other attributes and/or skills when comparing
		// individuals

		// Check if this settlement is the designated settlement for
		// housing the player commander
		if (bestCandidate != null) {
			if (settlement.hasDesignatedCommander()) {
				PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
				Commander commander = personConfig.getCommander();
				updateCommander(bestCandidate, commander);
				logger.config("[" + bestCandidate.getLocationTag().getLocale() + "] " + bestCandidate
						+ " had been assigned as the commander of " + settlement + ".");

				// Determine the initial leadership points
				int leadershipAptitude = bestCandidate.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
				commander.setInitialLeadershipPoint(leadershipAptitude);
			}

			else {
				bestCandidate.setRole(RoleType.COMMANDER);
			}
		}

		if (secondCandidate != null && pop >= POPULATION_WITH_SUB_COMMANDER) {
			secondCandidate.setRole(RoleType.SUB_COMMANDER);
		}
	}

	/**
	 * Establishes the leader in a settlement.
	 *
	 * @param role
	 */
	private void electLeader(RoleType role) {
		Collection<Person> people = settlement.getAllAssociatedPeople();
		Person bestCandidate = null;
		int bestLeaderScore = 0;
		int bestComposite = 0;
		// Compare their leadership scores
		for (Person candidate : people) {
			NaturalAttributeManager mgr = candidate.getNaturalAttributeManager();
			int leadership = (int)(Math.round(.85 * mgr.getAttribute(NaturalAttributeType.LEADERSHIP)));
			int tradeSkill = (int)(Math.round(.15 * candidate.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING)));
			int composite = (int)(Math.round(computeCompositeScore(mgr, candidate)) / 20);

			if (leadership + tradeSkill + composite > bestLeaderScore) {
				bestLeaderScore = leadership + tradeSkill;
				bestCandidate = candidate;
				bestComposite = composite;
			}
			else if (composite > bestComposite) {
				bestLeaderScore = leadership + tradeSkill;
				bestCandidate = candidate;
				bestComposite = composite;				
			}
			else if (leadership > bestLeaderScore) {
				bestLeaderScore = leadership;
				bestCandidate = candidate;
				bestComposite = composite;				
			}
		}

		if (bestCandidate != null) {
			bestCandidate.setRole(role);
		}
	}
	
	/**
	 * Applies the Commander profile to the Person with the COMMANDER role.
	 * 
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
	 * Updates the commander's profile.
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
		
		logger.config(commander.getFullName() 
				+ " accepted the role of being a Commander by the order of the Mission Control.");
		
		cc.setRole(RoleType.COMMANDER);
		//cc.setCountry(commander.getCountryStr());
		cc.setSponsor(SimulationConfig.instance().getReportingAuthorityFactory().getItem(commander.getSponsorStr()));
	}

	/**
	 * Establishes or reset the system of governance of a settlement.
	 *
	 */
	public void establishSettlementGovernance() {
		// Elect commander, subcommander, mayor
		establishTopLeadership();

		// Assign a basic role to each person
		assignBasicRoles();

		// Elect chiefs
		establishChiefs();
	}

	/**
	 * Assigns a basic role to each person.
	 *
	 * @param settlement
	 */
	private void assignBasicRoles() {
		// Assign roles to each person
		List<Person> oldlist = new ArrayList<>(settlement.getAllAssociatedPeople());
		List<Person> plist = new ArrayList<>();

		int pop = settlement.getInitialPopulation();
		
		// If a person does not have a (specialist) role, add him/her to the list
		for (Person p: oldlist) {
			if (p.getRole().getType() == null)
				plist.add(p);
		}

		List<RoleType> roleList = null;
				
		if (pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
			roleList = new ArrayList<>(RoleUtil.getCrewRoles());
		}
		else {
			roleList = new ArrayList<>(RoleUtil.getSpecialists());
		}
		
		while (!plist.isEmpty()) {
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
	 * Establishes the top leadership of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	private void establishTopLeadership() {

		int popSize = settlement.getNumCitizens();

		if (popSize >= POPULATION_WITH_MAYOR) {
			// Elect a mayor
			electLeader(RoleType.MAYOR);
		}
		else if (popSize >= POPULATION_WITH_ADMINISTRATOR) {
			// Elect a mayor
			electLeader(RoleType.ADMINISTRATOR);
		}
		// for pop < POPULATION_WITH_MAYOR
		else if (popSize >= POPULATION_WITH_COMMANDER) {
			// Elect commander and sub-commander
			electCommanders(popSize);
		}
	}

	/**
	 * Establishes the chiefs of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	private void establishChiefs() {

		int popSize = settlement.getNumCitizens();

		if (popSize >= POPULATION_WITH_CHIEFS) {
			// Elect chiefs
			int i = 0;
			int maxChiefs = popSize - POPULATION_WITH_CHIEFS + 1;
			for(RoleType rt : RoleType.values()) {
				if (rt.isChief() && (i < maxChiefs)) {
					electChief(rt);
				}
			}
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

		if (!role.isChief()) {
			return;
		}

		RoleType specialty = RoleUtil.getChiefSpeciality(role);
		Person winner = null;
		int cSkills = 0;
		int cCombined = 0;

		List<SkillType> requiredSkills = null;
		switch(role) {
		case CHIEF_OF_AGRICULTURE:
			requiredSkills = List.of(
									SkillType.BOTANY,
									SkillType.BIOLOGY,
									SkillType.CHEMISTRY,
									SkillType.COOKING,
									SkillType.TRADING
									);
			break;

		case CHIEF_OF_COMPUTING:
			requiredSkills = List.of(
									SkillType.ASTRONOMY,
									SkillType.COMPUTING,
									SkillType.CHEMISTRY,
									SkillType.MATHEMATICS,
									SkillType.PHYSICS
									);
			break;

		case CHIEF_OF_ENGINEERING:
			requiredSkills = List.of(
									SkillType.MATERIALS_SCIENCE,
									SkillType.COMPUTING,
									SkillType.PHYSICS,
									SkillType.MECHANICS,
									SkillType.CONSTRUCTION
									);
			break;

		case CHIEF_OF_LOGISTIC_OPERATION:
			requiredSkills = List.of(
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,					
									SkillType.METEOROLOGY,
									SkillType.MECHANICS,
									SkillType.PILOTING,
									SkillType.TRADING									
									);
			break;

		case CHIEF_OF_MISSION_PLANNING:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.COMPUTING,
									SkillType.EVA_OPERATIONS,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,									
									SkillType.PILOTING,									
									SkillType.PSYCHOLOGY,
									SkillType.TRADING									
									);
			break;

		case CHIEF_OF_SAFETY_HEALTH_SECURITY:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.BIOLOGY,
									SkillType.CONSTRUCTION,
									SkillType.EVA_OPERATIONS,
									SkillType.MEDICINE,
									SkillType.PSYCHOLOGY,
									SkillType.TRADING
									);
			break;

		case CHIEF_OF_SCIENCE:
			requiredSkills = List.of(
									SkillType.AREOLOGY,
									SkillType.ASTRONOMY,
									SkillType.BIOLOGY,
									SkillType.BOTANY,									
									SkillType.CHEMISTRY,
									SkillType.COMPUTING,
									SkillType.MATERIALS_SCIENCE,
									SkillType.MATHEMATICS,
									SkillType.MEDICINE,
									SkillType.PHYSICS,
									SkillType.PSYCHOLOGY
									);
			break;

		case CHIEF_OF_SUPPLY_RESOURCE:
			requiredSkills = List.of(
									SkillType.COOKING,
									SkillType.MATHEMATICS,
									SkillType.MANAGEMENT,
									SkillType.MATERIALS_SCIENCE,
									SkillType.TRADING
									);
			break;

		default:
			throw new IllegalStateException("Can not process Chief " + role);
		}

		// compare their scores
		for (Person p : people) {
			SkillManager skillMgr = p.getSkillManager();
			NaturalAttributeManager mgr = p.getNaturalAttributeManager();
			if (p.getRole().getType() == specialty) {

				// Calculate the skill with a decreasing importance
				int modifier = 5 + requiredSkills.size() - 1;
				int pSkills = 0;
				for (SkillType skillType : requiredSkills) {
					pSkills += (modifier * skillMgr.getEffectiveSkillLevel(skillType));
					modifier--;
				}

				int pCombined = mgr.getAttribute(NaturalAttributeType.LEADERSHIP)
						+ mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
						+ skillMgr.getEffectiveSkillLevel(SkillType.MANAGEMENT);
				// if this person p has a higher experience score than the
				// previous cc
				// or experience score match and the combined is higher
				if ((pSkills > cSkills)
						|| ((pSkills == cSkills) && (pCombined > cCombined))) {
					cSkills = pSkills;
					winner = p;
					cCombined = pCombined;
				}
			}
		}

		if (winner != null) {
			winner.setRole(role);
		}
	}

	/**
	 * Finds a list of people with a particular role.
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
