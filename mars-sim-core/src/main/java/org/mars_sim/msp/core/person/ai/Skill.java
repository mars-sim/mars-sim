/**
 * Mars Simulation Project
 * Skill.java
 * @version 3.1.0 2018-10-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;

/**
 * The Skill class represents a single skill that a person has. The skill must
 * have a name unique among the person's collection of skills.
 */
public class Skill implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Skills
//	/** @deprecated */
//	public static final String DRIVING = "Driving";
//	/** @deprecated */
//	public static final String BOTANY = "Botany";
//	/** @deprecated */
//	public static final String MECHANICS = "Mechanics";
//	/** @deprecated */
//	public static final String EVA_OPERATIONS = "EVA Operations";
//	/** @deprecated */
//	public static final String AREOLOGY = "Areology";
//	/** @deprecated */
//	public static final String MEDICAL = "Medicine";
//	/** @deprecated */
//	public static final String COOKING = "Cooking";
//	/** @deprecated */
//	public static final String TRADING = "Trading";
//	/** @deprecated */
//	public static final String MATERIALS_SCIENCE = "Materials Science";
//	/** @deprecated */
//	public static final String CONSTRUCTION = "Construction";
//	/** @deprecated */
//	public static final String BIOLOGY = "Biology";
//	/** @deprecated */
//	public static final String ASTRONOMY = "Astronomy";
//	/** @deprecated */
//	public static final String CHEMISTRY = "Chemistry";
//	/** @deprecated */
//	public static final String PHYSICS = "Physics";
//	/** @deprecated */
//	public static final String MATHEMATICS = "Mathematics";
//	/** @deprecated */
//	public static final String METEOROLOGY = "Meteorology";

	// Data members
	/** The skill level (0 to infinity). */
	private int level;
	/** The experience points towards the next skill level. */
	private double experiencePoints;
	/** The experience points needed to reach the next skill level. */
	private double neededExperiencePoints;
	/** The unique (for each person) skill. */
	private SkillType skill;

	/**
	 * Constructor.
	 * 
	 * @param skill {@link SkillType}
	 */
	public Skill(SkillType skill) {
		this.skill = skill;
		level = 0;
		experiencePoints = 0D;
		neededExperiencePoints = 25D;
	}

	/**
	 * Constructor with level.
	 * 
	 * @param skill {@link SkillType} the skill's name.
	 * @param level the skill's initial level.
	 */
	public Skill(SkillType skill, int level) {
		this(skill);
		setLevel(level);
	}

	/**
	 * Returns the name of the skill.
	 * 
	 * @return the skill's name
	 */
	public SkillType getSkill() {
		return skill;
	}

	/**
	 * Returns the level of the skill.
	 * 
	 * @return the skill's level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * Sets the level of the skill.
	 * 
	 * @param newLevel the new level of the skill
	 */
	void setLevel(int newLevel) {
		level = newLevel;
		experiencePoints = 0;
		neededExperiencePoints = 25D * Math.pow(2D, newLevel);
	}

	/**
	 * Adds to the experience points of the skill.
	 * 
	 * @param newPoints the experience points to be added
	 */
	void addExperience(double newPoints) {
		experiencePoints += newPoints;
		if (experiencePoints >= neededExperiencePoints) {
			experiencePoints -= neededExperiencePoints;
			neededExperiencePoints *= 2D;
			level++;
		}
	}
}