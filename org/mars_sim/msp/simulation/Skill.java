//************************** Skill **************************
// Last Modified: 3/20/00

package org.mars_sim.msp.simulation;

// The Skill class represents a single skill that a person has.
// The skill must have a name unique among the person's collection of skills.

public class Skill {
	
	// Data memebers

	private int level;                      // The skill level (0 to infinity)
	private double experiencePoints;        // The experience points towards the next skill level.
	private double neededExperiencePoints;  // The experience points needed to reach the next skill level.
	private String name;                    // The unique (for each person) name of the skill.

	// Constructor

	public Skill(String name) {
		this.name = new String(name);
		level = 0;
		experiencePoints = 0D;
		neededExperiencePoints = 25D;
	}
	
	// Returns the name of the skill.
	
	public String getName() { return new String(name); }
	
	// Returns the level of the skill.
	
	public int getLevel() { return level; }

	// Sets the level of the skill.
	
	public void setLevel(int newLevel) {
		level = newLevel;
		experiencePoints = 0;
		neededExperiencePoints = 25D * Math.pow(2D, newLevel);
	}
	
	// Adds to the experience points of the skill
	
	public void addExperience(double newPoints) {
		experiencePoints += newPoints;
		if (experiencePoints >= neededExperiencePoints) {
			experiencePoints -= neededExperiencePoints;
			neededExperiencePoints *= 2D;
			level++;
		}
	}
}	

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
