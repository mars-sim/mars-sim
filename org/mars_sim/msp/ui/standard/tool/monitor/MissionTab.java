/**
 * Mars Simulation Project
 * MissionTab.java
 * @version 2.80 2007-01-07
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.standard.tool.monitor;

import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.mission.MissionWindow;

/**
 * This class represents a mission table displayed within the Monitor Window. 
 */
public class MissionTab extends TableTab {

	/**
	 * Constructor
	 */
	MissionTab() {
		// Use TableTab constructor
		super(new MissionTableModel(), true, true);
	}
	
    /**
     * Display selected mission in mission tool.
     * @param desktop the main desktop.
     */
    public void displayMission(MainDesktopPane desktop) {
    	List selection = getSelection();
    	if (selection.size() > 0) {
    		Object selected = selection.get(0);
    		if (selected instanceof Mission) {
    			((MissionWindow) desktop.getToolWindow(MissionWindow.NAME)).selectMission((Mission) selected);
    			desktop.openToolWindow(MissionWindow.NAME);
    		}
    	}
    }
    
    /**
     * Center the map on the first selected row.
     * @param desktop Main window of application.
     */
    public void centerMap(MainDesktopPane desktop) {
        List rows = getSelection();
        Iterator it = rows.iterator();
        if (it.hasNext()) {
            Mission mission = (Mission) it.next();
            if (mission.getPeopleNumber() > 0) 
            	desktop.centerMapGlobe(mission.getPeople().get(0).getCoordinates());
        }
    }
}