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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.authority.GovernanceFactory;
import com.mars_sim.core.authority.GovernanceRules;
import com.mars_sim.core.person.Commander;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
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

	/** A record to store the maximum and current number of a role. */
	public class RoleCount implements Serializable {
		private int maximum;
		private int current;

		public RoleCount(int maximum) {
			this.maximum = maximum;
			this.current = 0;
		}

		public int getMaximum() {
			return maximum;
		}
		
		public int getCurrent() {
			return current;
		}

		private void changeCurrent(int delta) {
			current = delta;
			if (current < 0) {
				current = 0;
			}
		}
	}

	/** Stores the number for each role. */
	private Map<RoleType, RoleCount> roleRegistry = new EnumMap<>(RoleType.class);

	/** The settlement of interest. */
	private Settlement settlement;
	private GovernanceRules	 governanceRules;


	/**
	 * This class creates a chain of command structure for a settlement. A
	 * settlement can have either 3 divisions or 7 divisions organizational
	 * structure
	 *
	 * @param settlement {@link Settlement}
	 */
	public ChainOfCommand(Settlement settlement) {
		this.settlement = settlement;
		
		this.governanceRules = GovernanceFactory.getByPopulation(settlement.getInitialPopulation());

		// Initialize roleAvailability array once only
		initializeRoleMaps();
	}

	/**
	 * Initializes the role maps.
	 */
	private void initializeRoleMaps() {

		List<RoleType> roles = new ArrayList<>(governanceRules.getAssignableRoles());
		
		// Shuffle the role types randomize
		Collections.shuffle(roles);

		int base = settlement.getInitialPopulation() / roles.size();
		int remainder = settlement.getInitialPopulation() % roles.size();
		for(RoleType rt : roles) {
			int count = base;
			if (remainder > 0) {
				count++;
				remainder--;
			}
			roleRegistry.put(rt, new RoleCount(count));
		}
	}

	/**
	 * Is the given role type available ?
	 *
	 * @param type
	 * @return
	 */
	public boolean isRoleAvailable(RoleType type) {
		var roleCount = roleRegistry.get(type);
		if (roleCount == null) {
			return false;
		}
        return roleCount.getMaximum() > roleCount.getCurrent();
	}
	
	/**
	 * Increments the number of the target role type in the map.
	 *
	 * @param key {@link RoleType}
	 */
	public void registerRole(RoleType key) {
		var roleCount = roleRegistry.get(key);
		if (roleCount == null) {
			return;
		}
		roleCount.changeCurrent(1);
	}

	/**
	 * Decrements the number of the target role type from the map.
	 *
	 * @param key {@link RoleType}
	 */
	public void releaseRole(RoleType key) {
		var roleCount = roleRegistry.get(key);
		if (roleCount == null) {
			return;
		}
		roleCount.changeCurrent(-1);
	}

	/**
	 * Elects a new person for leadership in a settlement if a mayor, administrator,
	 * deputy administrator, commander, sub-commander, or chiefs vacates his/her position.
	 *
	 * @param key {@link RoleType}
	 */
	public void reelectLeadership(RoleType key) {
		if (getNumFilled(key) == 0) {
			if (key.isChief()) {
				electChief(key);
			} else if (key.isCouncil()) {
				establishTopLeadership();
			}
		}
	}

	/**
	 * Finds out the number of people already fill this roleType.
	 *
	 * @param key
	 */
	public int getNumFilled(RoleType key) {
		var roles = roleRegistry.get(key);
		if (roles == null) {
			return 0;
		}
		return roles.getCurrent();
	}

	/**
	 * Computes the attribute composite score.
	 * 
	 * @param mgr
	 * @param p
	 * @return
	 */
	private double computeCompositeScore(NaturalAttributeManager mgr) {
		return 2 * mgr.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.ORGANIZATION)
				+ 3 * mgr.getAttribute(NaturalAttributeType.STRESS_RESILIENCE)
				
				+ 3 * mgr.getAttribute(NaturalAttributeType.COURAGE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.DISCIPLINE)
				+ 3 * mgr.getAttribute(NaturalAttributeType.EMOTIONAL_STABILITY)
				
				+ 2 * mgr.getAttribute(NaturalAttributeType.ATTRACTIVENESS)
				+ 1D + mgr.getAttribute(NaturalAttributeType.CONVERSATION);
	}

	private record LeadershipScore(int score, Person person) {}

	/**
	 * Establishes the top leadership of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	void establishTopLeadership() {
		
		Collection<Person> people = settlement.getAllAssociatedPeople();
		List<LeadershipScore> candidates = new ArrayList<>();

		// Generate leadership score for every person
		for (Person candidate : people) {
			if (candidate.isDeclaredDead() || RoleType.GUEST == candidate.getRole().getType()) {
				continue;
			}
	
			NaturalAttributeManager mgr = candidate.getNaturalAttributeManager();
			int leadership = (int)(Math.round(.9 * mgr.getAttribute(NaturalAttributeType.LEADERSHIP)));
			int composite = (int)(Math.round(computeCompositeScore(mgr)) / 20);
			int total = leadership + composite;

			candidates.add(new LeadershipScore(total, candidate));
		}
		
		// Sort the candidates by reverse score os best if first
		Collections.sort(candidates, (a, b) -> Integer.compare(b.score, a.score));

		// Match up the council to the candidates
		var councilRoles = governanceRules.getCouncilRoles();
		int bestId = 0;
		for(var c : councilRoles) {
			if (bestId >= candidates.size()) {
				break;
			}
			// Allocate the next bext person
			var bestCandidate = candidates.get(bestId);
			bestCandidate.person.setRole(c);

			// If the best and a commander
			if ((bestId == 0) && settlement.hasDesignatedCommander()) {
				Commander commander = SimulationConfig.instance().getPersonConfig().getCommander();
				updateCommander(bestCandidate.person, commander);
				logger.info(bestCandidate.person.getName()
						+ " had been assigned as the commander of " + settlement.getName() + ".");

				// Determine the initial leadership points
				int leadershipAptitude = bestCandidate.person.getNaturalAttributeManager().getAttribute(NaturalAttributeType.LEADERSHIP);
				commander.setInitialLeadershipPoint(leadershipAptitude);
			}

			bestId++;
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

		// If a person does not have a (specialist) role, add him/her to the list
		for (Person p: oldlist) {
			if (p.getRole().getType() == null)
				plist.add(p);
		}

		List<RoleType> roleList = new ArrayList<>(governanceRules.getAssignableRoles());		
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
	 * Establishes the chiefs of a settlement.
	 *
	 * @param settlement the settlement.
	 */
	private void establishChiefs() {

		int maxChiefs = GovernanceRules.getMaxChiefs(settlement.getNumCitizens());
		if (maxChiefs > 0) {
			// Elect chiefs
			for(RoleType rt : RoleUtil.getChiefs()) {
				if (rt.isChief() && (maxChiefs > 0)) {
					electChief(rt);
					maxChiefs--;
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

		RoleType specialty = RoleType.getChiefSpeciality(role);
		Person winner = null;
		int cSkills = 0;
		int cCombined = 0;

		List<SkillType> requiredSkills = RoleType.getRequiredSkills(role);

		// compare their scores
		for (Person p : people) {
			if (p.isDeclaredDead() || RoleType.GUEST == p.getRole().getType()) {
				continue;
			}
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
		return settlement.getAllAssociatedPeople().stream()
				.filter(p -> p.getRole().getType() == role)
				.toList();
	}

	/**
	 * Get the Rules of governance that controls this council
	 * @return
	 */
	public GovernanceRules getGovernance() {
		return governanceRules;
	}

	public void destroy() {
		roleRegistry.clear();
		roleRegistry = null;
		settlement = null;
	}

}
