/**
 * Mars Simulation Project
 * SkillManager.java
 * @version 3.1.0 2017-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.RobotType;
import org.mars_sim.msp.core.tool.RandomUtil;


/**
 * The SkillManager class manages skills for a given person. Each person has one
 * skill manager.
 */
public class SkillManager implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
//	private static Logger logger = Logger.getLogger(SkillManager.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	// Data members
	/** The person owning the SkillManager. */
	private Person person;
	private Robot robot;
//	private CoreMind coreMind;
	
	/** A list of the person's skills keyed by name. */
	private Hashtable<SkillType, Skill> skills;

//	private Map<String, Integer> skillsMap;
//	private List<String> skillNames;
	
	/** Constructor. */
	public SkillManager(Unit unit) {//, CoreMind coreMind) {
		Person person = null;
		Robot robot = null;
//		this.coreMind = coreMind;
		
		if (unit instanceof Person) {
			person = (Person) unit;
			this.person = person;
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			this.robot = robot;
		}

		skills = new Hashtable<SkillType, Skill>();

//		if (person != null && !person.isPreConfigured()) {
//		} else if (robot != null) {		
//		}
	}

	/**
	 * Sets some random bot skills
	 */
	public void setRandomBotSkills() {
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
			skills.add(SkillType.PILOTING);
			skills.add(SkillType.TRADING);
		}
		else if (robot.getRobotType() == RobotType.CONSTRUCTIONBOT) {
			skills.add(SkillType.AREOLOGY);
			skills.add(SkillType.CONSTRUCTION);
		}
		
		for (SkillType startingSkill : skills) {
			int skillLevel = 1;
			addNewSkillNExperience(startingSkill, skillLevel);
		}

	}
	
	/**
	 * Sets some random skills
	 */
	public void setRandomSkills() {
		// Add starting skills randomly for a person.
		for (SkillType startingSkill : SkillType.values()) {
			int skillLevel = 0;
			if (startingSkill == SkillType.MEDICINE) {
					skillLevel = getInitialSkillLevel(0, 35);
					int exp = RandomUtil.getRandomInt(0, 24);
					this.addExperience(startingSkill, exp, 0);
				}
			else if (startingSkill == SkillType.MATERIALS_SCIENCE
				 || startingSkill == SkillType.MECHANICS) {
				skillLevel = getInitialSkillLevel(0, 45);
				int exp = RandomUtil.getRandomInt(0, 24);
				this.addExperience(startingSkill, exp, 0);
			}
			else {
				int rand = RandomUtil.getRandomInt(0, 3);
				
				if (rand == 0) {
					skillLevel = getInitialSkillLevel(0, 30);
					addNewSkillNExperience(startingSkill, skillLevel);
				}
				else if (rand == 1) {
					skillLevel = getInitialSkillLevel(1, 15);
					addNewSkillNExperience(startingSkill, skillLevel);
				}
				else if (rand == 2) {
					skillLevel = getInitialSkillLevel(2, 7);
					addNewSkillNExperience(startingSkill, skillLevel);
				}
				else if (rand == 3) {
					skillLevel = getInitialSkillLevel(3, 3);
					addNewSkillNExperience(startingSkill, skillLevel);
				}
			}
		}
	}
	
	/**
	 * Adds a new skill at the prescribed level
	 * 
	 * @param startingSkill
	 * @param skillLevel
	 */
	public void addNewSkillNExperience(SkillType startingSkill, int skillLevel) {
		Skill newSkill = new Skill(startingSkill);
		newSkill.setLevel(skillLevel);
		addNewSkill(newSkill);
		// Add some initial experience points
		int exp = RandomUtil.getRandomInt(0, (int)( 25 * Math.pow(2, skillLevel)) - 1);
		this.addExperience(startingSkill, exp, 0);
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
	 * Returns an array of the skill strings.
	 * 
	 * @return an array of the skill strings
	 */
	public List<String> getKeyStrings() {
		return new ArrayList<>(skills.keySet()).stream()
				   .map(o -> o.getName())
				   .collect(Collectors.toList());
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
	 * Returns the integer skill experiences from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
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
	 * Returns the integer skill experiences from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillDeltaExp(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getDeltaExp();
		}
		return result;
	}
	
	/**
	 * Returns the integer labor time from a named skill if it exists in the
	 * SkillManager. Returns 0 otherwise.
	 * 
	 * @param skill {@link SkillType}
	 * @return {@link Integer} >= 0
	 */
	public int getSkillTime(SkillType skill) {
		int result = 0;
		if (skills.containsKey(skill)) {
			result = (int) skills.get(skill).getTime();
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
		else {
			skills.put(skillType, newSkill);
		}
		
//		// Set up the core mind
//		String skillEnumString = skillType.ordinal() + "";
//		String name = "";
//		if (person != null) 
//			name = person.getName();
//		else 
//			name = robot.getName();
//		LogConsolidated.log(Level.SEVERE, 5_000, sourceName,
//				name + " is acquiring the " + skillType.getName() + " skill (id " + skillEnumString + ")");
//		coreMind.create(skillEnumString);
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

		if (hasSkill(skillType)) {
			skills.get(skillType).addExperience(experiencePoints);
			skills.get(skillType).addTime(time);
		}
		else {
			addNewSkill(new Skill(skillType));
			addExperience(skillType, experiencePoints, time);
		}

//		 int finalSkill = getSkillLevel(skillType);
//		 if (finalSkill > initialSkill) 
//			 logger.info(person.getName() + " improved " +
//					 skillName + " skill to " + finalSkill);
	}

	public Map<String, Integer> getSkillLevelMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillLevelMap = new HashMap<String, Integer>();
		for (SkillType skill : keys) {
			int level = getSkillLevel(skill);
//			if (level > 0) {
				skillLevelMap.put(skill.getName(), level);
//			}
		}
		return skillLevelMap;
	}
	
	public Map<String, Integer> getSkillExpMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillExpMap = new HashMap<String, Integer>();
		for (SkillType skill : keys) {
			int exp = getSkillExp(skill);
			skillExpMap.put(skill.getName(), exp);
		}
		return skillExpMap;
	}
	
	public Map<String, Integer> getSkillDeltaExpMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillDeltaExpMap = new HashMap<String, Integer>();
		for (SkillType skill : keys) {
			int exp = getSkillDeltaExp(skill);
			skillDeltaExpMap.put(skill.getName(), exp);
		}
		return skillDeltaExpMap;
	}
	
	public Map<String, Integer> getSkillTimeMap() {
		SkillType[] keys = getKeys();
		Map<String, Integer> skillTimeMap = new HashMap<String, Integer>();
		for (SkillType skill : keys) {
			int exp = getSkillTime(skill);
			skillTimeMap.put(skill.getName(), exp);
		}
		return skillTimeMap;
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