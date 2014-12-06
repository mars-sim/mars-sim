/**
 * Mars Simulation Project
 * SkillManager.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.person.Person;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * The SkillManager class manages skills for a given person.
 * Each person has one skill manager.
 */
public class SkillManager
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	// Data members
	/** The person owning the SkillManager. */
	private Person person;
	/** A list of the person's skills keyed by name. */
	private Hashtable<SkillType, Skill> skills;

	/** Constructor. */
	public SkillManager(Person person) {
		this.person = person;
		skills = new Hashtable<SkillType, Skill>();

		// Add starting skills randomly for person.
		for (SkillType startingSkill : SkillType.values()) {
			int skillLevel = getInitialSkillLevel(0, 50);
			// int skillLevel = 1;
			Skill newSkill = new Skill(startingSkill);
			newSkill.setLevel(skillLevel);
			addNewSkill(newSkill);
		}
	}

	/**
	 * Returns an initial skill level.
	 * @param level lowest possible skill level
	 * @param chance the chance that the skill will be greater
	 * @return the initial skill level
	 */
	private int getInitialSkillLevel(int level, int chance) {
		if (RandomUtil.lessThanRandPercent(chance))
			return getInitialSkillLevel(level + 1, chance / 2);
		else return level;
	}

	/**
	 * Returns the number of skills.
	 * @return the number of skills
	 */
	public int getSkillNum() {
		return skills.size();
	}

	/**
	 * Returns an array of the skills.
	 * @return an array of the skills
	 */
	public SkillType[] getKeys() {
		return skills.keySet().toArray(new SkillType[] {});
	}

	/**
	 * Returns true if the SkillManager has the named skill, false otherwise.
	 * @param skill {@link SkillType} the skill's name
	 * @return true if the manager has the named skill
	 */
	public boolean hasSkill(SkillType skill) {
		return skills.containsKey(skill);
	}

	/**
	 * Returns the integer skill level from a named skill if it exists in the SkillManager.
	 * Returns 0 otherwise.
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
	 * Returns the effective integer skill level from a named skill
	 * based on additional modifiers such as fatigue.
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		int skill = getSkillLevel(skillType);

		// Modify for fatigue
		// - 1 skill level for every 1000 points of fatigue.
		double performance = person.getPerformanceRating();
		int result = (int)Math.round(performance * skill);
		return result;
	}

	/**
	 * Adds a new skill to the SkillManager and indexes it under its name.
	 * @param newSkill the skill to be added
	 */
	public void addNewSkill(Skill newSkill) {
		SkillType skillType = newSkill.getSkill();
		if (hasSkill(skillType)) skills.get(skillType).setLevel(newSkill.getLevel());
		else skills.put(skillType, newSkill);
	}

	/**
	 * Adds given experience points to a named skill if it exists in the SkillManager.
	 * If it doesn't exist, create a skill of that name in the SkillManager and add the experience points to it.
	 * @param skillType {@link SkillType} the skill's type
	 * @param experiencePoints the experience points to be added
	 */
	public void addExperience(SkillType skillType, double experiencePoints) {

		// int initialSkill = getSkillLevel(skillName);

		if (hasSkill(skillType)) skills.get(skillType).addExperience(experiencePoints);
		else {
			addNewSkill(new Skill(skillType));
			addExperience(skillType, experiencePoints);
		}

		// int finalSkill = getSkillLevel(skillName);
		// if (finalSkill > initialSkill) logger.info(person.getName() + " improved " + skillName + " skill to " + finalSkill);
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		person = null;
		skills.clear();
		skills = null;
	}
}