//************************** Natural Attribute Manager **************************
// Last Modified: 5/25/00

// The NaturalAttributeManager class manages a person's natural attributes.
// There is only natural attribute manager for each person.

import java.util.*;

public class NaturalAttributeManager {

	// Data members
	
	private Hashtable attributeList;  // List of the person's natural attributes keyed by unique name.
	private String[] attributeKeys;   // List of the person's natural attributes by name.

	// Constructor

	public NaturalAttributeManager() {
	
		attributeList = new Hashtable();
		
		String[] attributeKeysTemp = {"Strength", "Endurance", "Agility", "Academic Aptitude", "Experience Aptitude", "Attractiveness", "Presence",
		                              "Leadership", "Conversation"};
		attributeKeys = attributeKeysTemp;
		
		// Create natural attributes using random values,
		// Note: this may change later.
		
		for (int x=0; x < attributeKeys.length; x++) {
			int attributeValue = 0;
			for (int y=0; y < 10; y++) attributeValue += RandomUtil.getRandomInteger(10);
			attributeList.put(attributeKeys[x], new Integer(attributeValue));
		}
		
		// Adjust certain attributes reflective of Martian settlers.
		
		addSettlerBonus("Strength", 20);
		addSettlerBonus("Endurance", 20);
		addSettlerBonus("Agility", 10);
		addSettlerBonus("Academic Aptitude", 40);
		addSettlerBonus("Experience Aptitude", 30);
	}
	
	// Adds a random bonus for Martian settlers in a given attribute.
	
	private void addSettlerBonus(String attributeName, int bonus) {
		
		int newValue = getAttribute(attributeName) + RandomUtil.getRandomInteger(bonus);
		if (newValue > 100) newValue = 100;
		if (newValue < 0) newValue = 0;
		attributeList.put(attributeName, new Integer(newValue));
	}
	
	// Returns the number of natural attributes.
	
	public int getAttributeNum() { return attributeKeys.length; }
	
	// Returns an array of the natural attribute names as strings.
	
	public String[] getKeys() { 
		String[] result = new String[attributeKeys.length];
		
		for (int x=0; x < result.length; x++) result[x] = attributeKeys[x];
		
		return result;
	}
	
	// Returns the integer value of a named natural attribute if it exists.
	// Returns 0 otherwise.
	
	public int getAttribute(String attributeName) { 
		int result = 0;
		if (attributeList.containsKey(attributeName)) result = ((Integer)attributeList.get(attributeName)).intValue();
		
		return result;
	}
}	

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
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