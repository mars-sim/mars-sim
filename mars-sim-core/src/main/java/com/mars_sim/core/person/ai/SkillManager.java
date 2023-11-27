/*
 * Mars Simulation Project
 * SkillManager.java
 * @date 2022-09-01
 * @author Scott Davis
 */

package com.mars_sim.core.person.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.tools.util.RandomUtil;


/**
 * The SkillManager class manages skills for a given Worker.
 */
public class SkillManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private Worker owner;
	
	/** A list of skills keyed by skill type enum. */
	private Map<SkillType, Skill> skills;

	/** Constructor. */
	public SkillManager(Worker owner) {
	
		this.owner = owner;

		skills = new EnumMap<>(SkillType.class);
	}

	/**
	 * Adds a new skill at the prescribed level. THi also adds a random experience.
	 * 
	 * @param startingSkill
	 * @param skillLevel
	 */
	public void addNewSkill(SkillType startingSkill, int skillLevel) {
		Skill newSkill = new Skill(startingSkill, skillLevel);
		addNewSkill(newSkill);

		// Add some initial experience points
		int exp = RandomUtil.getRandomInt(0, (int)(Skill.BASE * Math.pow(2, skillLevel)) - 1);
		this.addExperience(startingSkill, exp, 0);
	}

	/**
	 * Returns a random skill type.
	 * 
	 * @return a skill type
	 */
	public SkillType getARandomSkillType() {
		List<Skill> randSkills = getSkills();
		int rand = RandomUtil.getRandomInt(randSkills.size() - 1);
		return randSkills.get(rand).getType();
	}	
	
	/**
	 * Returns the integer skill level from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skillType {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillLevel(SkillType skillType) {
		int result = 0;
		if (skills.containsKey(skillType)) {
			result = skills.get(skillType).getLevel();
		}
		return result;
	}

	/**
	 * Returns the skill instance if it exists in the
	 * SkillManager. Returns null otherwise.
	 * 
	 * @param skillType {@link SkillType}
	 * @return {@link Skill}
	 */
	public Skill getSkill(SkillType skillType) {
		if (skills.containsKey(skillType)) {
			return skills.get(skillType);
		}
		return null;
	}
	
	/**
	 * Gets the cumulative experience points of the skill.
	 * 
	 * @param skillType {@link SkillType}
	 * @return the cumulative experience points
	 */
	public double getCumulativeExperience(SkillType skillType) {
		Skill skill = getSkill(skillType);
		if (skill != null) {
			// Calculate exp points at the current level
			double pts = skill.getExperience();
			int level = skill.getLevel();
			// Calculate the exp points at previous levels
			for (int i=0; i<level; i++) {
				pts += Skill.BASE * Math.pow(2D, level);
			}
			return pts;
		}
		else
			return 0;

	}
	
	/**
	 * Returns the integer skill experiences at the current level.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillExp(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getExperience();
		}
		return result;
	}
	
	/**
	 * Returns the effective integer skill level from a named skill based on
	 * additional modifiers such as performance.
	 * 
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		int skill = getSkillLevel(skillType);
		double performance = owner.getPerformanceRating();
		return (int) Math.round(performance * skill);
	}

	/**
	 * Adds a new skill to the SkillManager and indexes it under its name.
	 * 
	 * @param newSkill the skill to be added
	 */
	private void addNewSkill(Skill newSkill) {
		skills.put(newSkill.getType(), newSkill);
	}

	/**
	 * Adds given experience points to a named skill if it exists in the
	 * SkillManager. If it doesn't exist, create a skill of that name in the
	 * SkillManager and add the experience points to it.
	 * 
	 * @param skillType        {@link SkillType} the skill's type
	 * @param experiencePoints the experience points to be added
	 */
	public void addExperience(SkillType skillType, double experiencePoints, double time) {
		Skill skill = skills.computeIfAbsent(skillType, Skill::new);
		
		skill.addExperience(experiencePoints);
		skill.addTime(time);
	}

	/**
	 * Gets all the skills known by the owner.
	 * 
	 * @return
	 */
	public List<Skill> getSkills() {
		return new ArrayList<>(skills.values());
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		skills.clear();
		skills = null;
	}
}
