/**
 * Mars Simulation Project
 * Skill.java
 * @version 2.71 2000-09-26
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/** The Skill class represents a single skill that a person has.
 *  The skill must have a name unique among the person's collection of skills.
 */
public class Skill {

    private int level; // The skill level (0 to infinity)
    private double experiencePoints; // The experience points towards the next skill level.
    private double neededExperiencePoints; // The experience points needed to reach the next skill level.
    private String name; // The unique (for each person) name of the skill.

    Skill(String name) {
        this.name = new String(name);
        level = 0;
        experiencePoints = 0D;
        neededExperiencePoints = 25D;
    }

    /** Returns the name of the skill. */
    public String getName() {
        return new String(name);
    }

    /** Returns the level of the skill. */
    public int getLevel() {
        return level;
    }

    /** Sets the level of the skill. */
    void setLevel(int newLevel) {
        level = newLevel;
        experiencePoints = 0;
        neededExperiencePoints = 25D * Math.pow(2D, newLevel);
    }

    /** Adds to the experience points of the skill. */
    void addExperience(double newPoints) {
        experiencePoints += newPoints;
        if (experiencePoints >= neededExperiencePoints) {
            experiencePoints -= neededExperiencePoints;
            neededExperiencePoints *= 2D;
            level++;
        }
    }
}

