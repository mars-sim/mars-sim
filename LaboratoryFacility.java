//************************** Laboratory Facility **************************
// Last Modified: 5/8/00

// The LaboratoryFacility class represents the research laboratories in a settlement.

// A settlement may or may not have laboratories.

import java.util.*;

public class LaboratoryFacility extends Facility {

	// Data members
	
	private int laboratorySize;         // Size of collective laboratories (units defined later)
	private int technologyLevel;        // How advanced the laboratories are (units defined later)
	private Vector techSpecialities;  // What fields of science the laboratories specialize in.

	// Constructor for random creation.

	public LaboratoryFacility(FacilityManager manager) {
	
		// Use Facility's constructor.
		
		super(manager, "Research Laboratories", "Labs");
		
		// Initialize random laboratorySize from 1 to 5.
		
		laboratorySize = 1 + RandomUtil.getRandomInteger(4);
		
		// Initialize random technologyLevel from 1 to 5.
		
		technologyLevel = 1 + RandomUtil.getRandomInteger(4);
		
		// Initialize techSpecialities from 1 to 5 technologies.
		
		techSpecialities = new Vector();
		String[] techs = { "Medical Research", "Areology", "Botany", "Physics", "Material Science" };
		
		while (techSpecialities.size() == 0) {
			for (int x=0; x < techs.length; x ++) {
				if (RandomUtil.lessThanRandPercent(20)) techSpecialities.addElement(techs[x]);
			}
		}
	}
	
	// Constructor for set values (used later when facilities can be built or upgraded.)
	
	public LaboratoryFacility(FacilityManager manager, int size, int techLevel, String[] techFocus) {
	
		// Use Facility's constructor.
		
		super(manager, "Research Laboratories", "Labs");
		
		// Initialize data members.
		
		laboratorySize = size;
		technologyLevel = techLevel;
		
		techSpecialities = new Vector();
		for (int x=0; x < techFocus.length; x++) techSpecialities.addElement(techFocus[x]);
	}
	
	// Returns the size of the settlement's laboratories (units defined later)
	
	public int getLaboratorySize() { return laboratorySize; }
	
	// Returns the technology level of the settlement's laboratories (units defined later)
	
	public int getTechnologyLevel() { return technologyLevel; }
	
	// Returns the lab's science specialities as an array of Strings
	
	public String[] getTechSpecialities() { 
	
		String[] result = new String[techSpecialities.size()];
		for (int x=0; x < techSpecialities.size(); x++) result[x] = (String) techSpecialities.elementAt(x);
	
		return result;
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