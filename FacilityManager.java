//************************** Facility Manager **************************
// Last Modified: 8/19/00

// The FacilityManager class manages a settlement's facilities.
// There is only one facility manager for each settlement.

import java.util.*;

public class FacilityManager {

	// Data members
	
	private Settlement settlement;  // The settlement the facility manager belongs to.
	private Vector facilityList;    // Unordered List of the settlement's facilities.

	// Constructor

	public FacilityManager(Settlement settlement) {
	
		// Initialize settlement
		
		this.settlement = settlement;
	
		// Initialize facilities for the settlement.
	
		facilityList = new Vector();
		
		// Add manditory facilities to manager.

		facilityList.addElement(new LivingQuartersFacility(this));
		facilityList.addElement(new GreenhouseFacility(this));
		facilityList.addElement(new StoreroomFacility(this));
		facilityList.addElement(new MaintenanceGarageFacility(this));
		facilityList.addElement(new LaboratoryFacility(this));
	}
	
	// Returns the settlement the owns this facility manager.
	
	public Settlement getSettlement() { return settlement; }
	
	// Returns the number of facilities in the manager.
	
	public int getFacilityNum() { return facilityList.size(); }
	
	// Returns a facility given an index number.
	// If there is no facility at that index number, return null.
	
	public Facility getFacility(int index) { 
		
		if ((index >= 0) && (index < facilityList.size())) return (Facility) facilityList.elementAt(index); 
		else return null;	
	}
	
	// Returns a facility given its name.
	// If there is no facility of the given name, return null.
	
	public Facility getFacility(String name) {
		
		for (int x=0; x < facilityList.size(); x++) {
			Facility tempFacility = getFacility(x);
			if (tempFacility.getName().equals(name)) return tempFacility;
		}
		return null;
	}
	
	// Returns an array of facility panels.
	// One panel for each facility in the settlement.
	
	public FacilityPanel[] getFacilityPanels(MainDesktopPane desktop) {
		
		FacilityPanel[] result = new FacilityPanel[facilityList.size()];
		
		for (int x=0; x < facilityList.size(); x++) { result[x] = ((Facility) facilityList.elementAt(x)).getUIPanel(desktop); }
		
		return result;
	}
	
	// Sends facilities time pulse.
	
	public void timePasses(int seconds) {
		for (int x=0; x < facilityList.size(); x++) ((Facility) facilityList.elementAt(x)).timePasses(seconds);
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