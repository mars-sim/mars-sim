/*
 * Mars Simulation Project
 * MapSelector.java
 * @date 2024-11-09
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool_window;

import com.mars_sim.core.Unit;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.unit.FixedUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;

/**
 * This is a helper class that selects teh best Map display to show for a Unit. It selects between 
 * the Mars Navigator and the Settlement tool. The types and state of the Unit influences the choice.
 */
public final class MapSelector {
    private MapSelector() {
        // Stop creation as static helper
    }

    public static void displayOnMap(MainDesktopPane desktop, Unit u) {
        Coordinates marsPosn = null;
        if (u instanceof MobileUnit mu) {
            marsPosn = openMobileUnit(desktop, mu);
        }
        else if (u instanceof FixedUnit fu) {
            SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
            sw.displayPosition(fu.getAssociatedSettlement(), fu.getPosition());
        }
        else if (u instanceof Settlement s) {
            marsPosn = s.getCoordinates();
        }

        if (marsPosn != null) {
        	// person is on a mission on the surface of Mars 
			desktop.openToolWindow(NavigatorWindow.NAME);
			desktop.centerMapGlobe(marsPosn);
        }
    }

    /**
	 * Opens the mobile unit in Mars Navigator or Settlement Map.
	 * 
	 * @param u
	 */
	private static Coordinates openMobileUnit(MainDesktopPane desktop, MobileUnit u) {
		
		if (u.isInSettlement()) {
            showSettlementMap(desktop, u);
		}
		else if (u.isInVehicle()) {
			Vehicle vv = u.getVehicle();

			if (vv.getSettlement() == null) {
				return vv.getCoordinates();
			} 	
			else {
				// still parked inside a garage or within the premise of a settlement
                showSettlementMap(desktop, u);
			}
		}
		else if (u.isOutside()) {
			if (u instanceof Vehicle) {
				return u.getCoordinates();
			}

			Vehicle vv = u.getVehicle();
			if (vv == null) {
				// if it's not in a vehicle
                showSettlementMap(desktop, u);
			}	
			else {
				// if it's in a vehicle			
				if (vv.getSettlement() != null) {
					// if the vehicle is in a settlement
					showSettlementMap(desktop, u);
				}	
				else {
					return u.getCoordinates();
				}
			}
		}
        return null;
	}	

	/**
	 * Opens the Settlement map and show a Unit
	 * 
	 * @param u
	 */
	private static void showSettlementMap(MainDesktopPane desktop, MobileUnit u) {
		// person just happens to step outside the settlement at its
		// vicinity temporarily
		SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
		if (u instanceof Person p) {
			sw.displayPerson(p);
		} 
		else if (u instanceof Robot r) {
			sw.displayRobot(r);
		}
        else if (u instanceof FixedUnit fu) {
            sw.displayPosition(fu.getAssociatedSettlement(), fu.getPosition());
        }
	}
	
}
