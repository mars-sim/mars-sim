/**
 * Mars Simulation Project
 * Skill.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.person.ai;

import java.io.Serializable;

/** The Skill class represents a single skill that a person has.
 *  The skill must have a name unique among the person's collection of skills.
 */
public class Skill implements Serializable {

	// Skills
	public static final String DRIVING = "Driving";
	public static final String BOTANY = "Botany";
	public static final String MECHANICS = "Mechanics";
	public static final String EVA_OPERATIONS = "EVA Operations";
	public static final String AREOLOGY = "Areology";
	public static final String MEDICAL = "Medicine";
	public static final String COOKING = "Cooking";
	public static final String TRADING = "Trading";
	public static final String MATERIALS_SCIENCE = "Materials Science";

    // Data members
    private int level; // The skill level (0 to infinity)
    private double experiencePoints; // The experience points towards the next skill level.
    private double neededExperiencePoints; // The experience points needed to reach the next skill level.
    private String name; // The unique (for each person) name of the skill.

    /** 
     * Constructs a Skill object 
     * @param name the skill's name
     */
    public Skill(String name) {
        this.name = new String(name);
        level = 0;
        experiencePoints = 0D;
        neededExperiencePoints = 25D;
    }
    
    /**
     * Constructor with level.
     * @param name the skill's name.
     * @param level the skill's initial level.
     */
    public Skill(String name, int level) {
    	this(name);
    	setLevel(level);
    }

    /** Returns the name of the skill. 
     *  @return the skill's name
     */
    public String getName() {
        return new String(name);
    }

    /** Returns the level of the skill. 
     *  @return the skill's level
     */
    public int getLevel() {
        return level;
    }

    /** Sets the level of the skill. 
     *  @param newLevel the new level of the skill
     */
    void setLevel(int newLevel) {
        level = newLevel;
        experiencePoints = 0;
        neededExperiencePoints = 25D * Math.pow(2D, newLevel);
    }

    /** Adds to the experience points of the skill. 
     *  @param newPoints the experience points to be added
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