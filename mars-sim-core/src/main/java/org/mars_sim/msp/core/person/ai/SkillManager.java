/**
 * Mars Simulation Project
 * SkillManager.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.tool.RandomUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * The SkillManager class manages skills for a given person. Each person has one
 * skill manager.
 */
public class SkillManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	/** The person owning the SkillManager. */
	private Person person;
	private Robot robot;
	/** A list of the person's skills keyed by name. */
	private Hashtable<SkillType, Skill> skills;

	private Map<String, Integer> skillsMap;
	private List<String> skillNames;
	
	/** Constructor. */
	public SkillManager(Unit unit) {
		Person person = null;
		Robot robot = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			this.person = person;
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			this.robot = robot;
		}

		skills = new Hashtable<SkillType, Skill>();

		if (person != null) {
			// Add starting skills randomly for a person.
			for (SkillType startingSkill : SkillType.values()) {
				int skillLevel = getInitialSkillLevel(0, 50);
				// int skillLevel = 1;
				Skill newSkill = new Skill(startingSkill);
				newSkill.setLevel(skillLevel);
				addNewSkill(newSkill);
			}
		}
		else {		
			// Add starting skills randomly for a bot.
			List<SkillType> skills = new ArrayList<>();
			
			if (robot.getRobotType() == RobotType.MAKERBOT) {
				skills.add(SkillType.MATERIALS_SCIENCE);
				skills.add(SkillType.PHYSICS);
			}
			else if (robot.getRobotType() == RobotType.GARDENBOT) {
				skills.add(SkillType.BOTANY);
				skills.add(SkillType.BIOLOGY);
			}
			else if (robot.getRobotType() == RobotType.REPAIRBOT) {
				skills.add(SkillType.MATERIALS_SCIENCE);
				skills.add(SkillType.MECHANICS);
			}
			else if (robot.getRobotType() == RobotType.CHEFBOT) {
				skills.add(SkillType.CHEMISTRY);
				skills.add(SkillType.COOKING);
			}
			else if (robot.getRobotType() == RobotType.MEDICBOT) {
				skills.add(SkillType.CHEMISTRY);
				skills.add(SkillType.MEDICINE);
			}
			else if (robot.getRobotType() == RobotType.DELIVERYBOT) {
				skills.add(SkillType.DRIVING);
				skills.add(SkillType.TRADING);
			}
			else if (robot.getRobotType() == RobotType.CONSTRUCTIONBOT) {
				skills.add(SkillType.AREOLOGY);
				skills.add(SkillType.CONSTRUCTION);
			}
			
			for (SkillType startingSkill : skills) {
				int skillLevel = 1;
				Skill newSkill = new Skill(startingSkill);
				newSkill.setLevel(skillLevel);
				addNewSkill(newSkill);
			}
		}
		
		// Create map and list
		SkillType[] keys = getKeys();
		skillsMap = new HashMap<String, Integer>();
		skillNames = new ArrayList<String>();
		for (SkillType skill : keys) {
			int level = getSkillLevel(skill);
			if (level > 0) {
				skillNames.add(skill.getName());
				skillsMap.put(skill.getName(), level);
			}
		}
		
	}

	/**
	 * Returns an initial skill level.
	 * 
	 * @param level  lowest possible skill level
	 * @param chance the chance that the skill will be greater
	 * @return the initial skill level
	 */
	private int getInitialSkillLevel(int level, int chance) {
		if (RandomUtil.lessThanRandPercent(chance))
			return getInitialSkillLevel(level + 1, chance / 2);
		else
			return level;
	}

	/**
	 * Returns the number of skills.
	 * 
	 * @return the number of skills
	 */
	public int getSkillNum() {
		return skills.size();
	}

	/**
	 * Returns an array of the skills.
	 * 
	 * @return an array of the skills
	 */
	public SkillType[] getKeys() {
		return skills.keySet().toArray(new SkillType[] {});
	}

	/**
	 * Returns true if the SkillManager has the named skill, false otherwise.
	 * 
	 * @param skill {@link SkillType} the skill's name
	 * @return true if the manager has the named skill
	 */
	public boolean hasSkill(SkillType skill) {
		return skills.containsKey(skill);
	}

	/**
	 * Returns the integer skill level from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillLevel(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = skills.get(skill).getLevel();
		}
		return result;
	}

	/**
	 * Returns the effective integer skill level from a named skill based on
	 * additional modifiers such as fatigue.
	 * 
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		int skill = getSkillLevel(skillType);
		double performance = 0;

		// Modify for fatigue
		// - 1 skill level for every 1000 points of fatigue.
		if (person != null)
			performance = person.getPerformanceRating();
		else if (robot != null)
			performance = robot.getPerformanceRating();

		int result = (int) Math.round(performance * skill);
		return result;
	}

	/**
	 * Adds a new skill to the SkillManager and indexes it under its name.
	 * 
	 * @param newSkill the skill to be added
	 */
	public void addNewSkill(Skill newSkill) {
		SkillType skillType = newSkill.getSkill();
		if (hasSkill(skillType))
			skills.get(skillType).setLevel(newSkill.getLevel());
		else
			skills.put(skillType, newSkill);
	}

	/**
	 * Adds given experience points to a named skill if it exists in the
	 * SkillManager. If it doesn't exist, create a skill of that name in the
	 * SkillManager and add the experience points to it.
	 * 
	 * @param skillType        {@link SkillType} the skill's type
	 * @param experiencePoints the experience points to be added
	 */
	public void addExperience(SkillType skillType, double experiencePoints) {

		// int initialSkill = getSkillLevel(skillName);

		if (hasSkill(skillType))
			skills.get(skillType).addExperience(experiencePoints);
		else {
			addNewSkill(new Skill(skillType));
			addExperience(skillType, experiencePoints);
		}

		// int finalSkill = getSkillLevel(skillName);
		// if (finalSkill > initialSkill) logger.info(person.getName() + " improved " +
		// skillName + " skill to " + finalSkill);
	}

	public Map<String, Integer> getSkillsMap() {
		return skillsMap;
	}
	
	public List<String> getSkillNames() {
		return skillNames;
	}
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		person = null;
		robot = null;
		skills.clear();
		skills = null;
	}
}