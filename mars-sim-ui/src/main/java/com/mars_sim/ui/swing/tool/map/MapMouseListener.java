/*
 * Mars Simulation Project
 * MapMouseListener.java
 * @date 2024-09-29
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.environment.Landmark;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * This is a listener to mouse event on a MapPanel. It changes the cursor when hovered over a 
 * surface unit or a landmark.
 * If clicked the appropriate detailed window is shown.
 */
public class MapMouseListener extends MouseAdapter {

    private UnitManager unitManager;
    private MapPanel mapPanel;
    private MainDesktopPane desktop;
    	
	private List<Landmark> landmarks;


    public MapMouseListener(MainDesktopPane desktop, MapPanel mapPanel) {
        var sim = desktop.getSimulation();
        this.unitManager = sim.getUnitManager();
        this.mapPanel = mapPanel;
        this.desktop = desktop;
        this.landmarks = sim.getConfig().getLandmarkConfiguration().getLandmarkList();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
            checkClick(mapPanel.getMouseCoordinates(event.getX(), event.getY()));
        }
    }

    /**
	 * Checks the click location.
	 * 
	 * @param event
	 */
	private void checkClick(Coordinates clickedPosition) {

		Unit foundMatch = findUnitUnderMouse(clickedPosition);
		if (foundMatch != null) {
			mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			desktop.showDetails(foundMatch);
		}
		else
			mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

    @Override
    public void mouseMoved(MouseEvent event) {
        checkHover(mapPanel.getMouseCoordinates(event.getX(), event.getY()));
    }

    /**
	 * Checks if the mouse is hovering over a map.
	 * 
	 * @param event
	 */
	protected void checkHover(Coordinates pos) {
        // Check if over a object
		Unit unit = findUnitUnderMouse(pos);
		if (unit != null) {
			mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			return;
		}

		// FUTURE: how to avoid overlapping labels ?		
		// Change mouse cursor if hovering over a landmark on the map
		for(Landmark landmark : landmarks) {
			double clickRange = landmark.getLandmarkCoord().getDistance(pos);
			double unitClickRange = 20;
			if (clickRange < unitClickRange) {
				mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				return;
			}
		}

		mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	private Unit findUnitUnderMouse(Coordinates clickedPosition) {
		// Check Settlements first
		for(var s : unitManager.getSettlements()) {
			if (isUnitAtPosition(s, clickedPosition)) {
				return s;
			}
		}

		// Check vehicles on surface
		for(var v : unitManager.getVehicles()) {
			if (v.isOutsideOnMarsMission() && isUnitAtPosition(v, clickedPosition)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Sets the cursor and open detail window of a unit.
	 * 
	 * @param unit
	 * @param clickedPosition
	 */
	private boolean isUnitAtPosition(Unit unit, Coordinates clickedPosition) {
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
			Coordinates unitCoords = unit.getCoordinates();
			double clickRange = unitCoords.getDistance(clickedPosition);
			double unitClickRange = displayInfo.getMapClickRange();
			if (clickRange < unitClickRange) {
				return true;
			}
		}
		return false;
	}
}
