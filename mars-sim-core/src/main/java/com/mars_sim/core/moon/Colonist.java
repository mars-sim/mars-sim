package com.mars_sim.core.moon;

import com.mars_sim.core.Entity;
import com.mars_sim.core.person.PersonBuilder;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillOwner;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.core.tool.RandomUtil;

public abstract class Colonist implements Entity, SkillOwner, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(Colonist.class.getName())
	
	private int age;
		
	private double performance = 100;
	
	private String name;
	
	private Colony colony;

	/** The researcher's skill manager. */
	private SkillManager skillManager;
	
	protected Colonist(String name, Colony colony) {
		this.name = name;
		this.colony = colony;
		this.age = RandomUtil.getRandomInt(18, 70);

		// Construct the SkillManager instance
		skillManager = new SkillManager(this);
		// Determine skills
		determineSkills();
	}

	
	/**
	 * Determines random skills for this researcher.
	 */
	private void determineSkills() {

		// Add starting skills randomly for a person.
		for (SkillType startingSkill : SkillType.values()) {
			int skillLevel = -1;

			switch (startingSkill) {
				case PILOTING: 
					// Checks to see if a person has a pilot license/certification
					skillLevel = PersonBuilder.getInitialSkillLevel(0, 35);
					break;
			
				// Medicine skill is highly needed for diagnosing sickness and prescribing medication 
				case MEDICINE:
					skillLevel = PersonBuilder.getInitialSkillLevel(0, 35);
					break;

				// psychology skill is sought after for living in confined environment
				case PSYCHOLOGY: 
					skillLevel = PersonBuilder.getInitialSkillLevel(0, 35);
					break;
	
				// Mechanics skill is sought after for repairing malfunctions
				case MATERIALS_SCIENCE, MECHANICS:
					skillLevel = PersonBuilder.getInitialSkillLevel(0, 45);
					break;

				default: {
					int rand = RandomUtil.getRandomInt(0, 3);
					if (rand == 0) {
						skillLevel = PersonBuilder.getInitialSkillLevel(0, (int)(10 + age/10.0));
					}
					else if (rand == 1) {
						skillLevel = PersonBuilder.getInitialSkillLevel(1, (int)(5 + age/8.0));
					}
					else if (rand == 2) {
						skillLevel = PersonBuilder.getInitialSkillLevel(2, (int)(2.5 + age/6.0));
					}
				} break;
			}

			// If a initial skill level then add it and assign experience
			if (skillLevel >= 0) {
				int exp = RandomUtil.getRandomInt(0, 24);
				skillManager.addNewSkill(startingSkill, skillLevel);
				skillManager.addExperience(startingSkill, exp, 0D);
			}
		}
	}
	
	public double getTotalSkillExperience() {
		return skillManager.getTotalSkillExperiences();
	}
	
	
	@Override
	public SkillManager getSkillManager() {
		return skillManager;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Context of a colonist is always their parent Colony.
	 */
	@Override
	public String getContext() {
		return colony.getName();
	}

	public Colony getColony() {
		return colony;
	}

	@Override
	public double getPerformanceRating() {
		return performance;
	}

}
