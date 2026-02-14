/**
 * Mars Simulation Project
 * ProspectingSitePanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.missionwizard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.map.CircleLayer;
import com.mars_sim.ui.swing.tool.map.CreatePathLayer;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.MapUtils;
import com.mars_sim.ui.swing.tool.map.NavpointEditLayer;
import com.mars_sim.ui.swing.tool.map.UnitMapLayer;
import com.mars_sim.ui.swing.tool.mission.create.MissionDataBean;
import com.mars_sim.ui.swing.utils.wizard.WizardStep;

/**
 * A wizard panel for the ice or regolith prospecting site.
 */
@SuppressWarnings("serial")
class ProspectingSitePanel extends WizardStep<MissionDataBean> {
	
	// Wizard panel name.
	public static final String NAME = "Prospecting Site";
	
	// Range modifier.
	private static final double RANGE_MODIFIER = .95D;
	
	// Data members.
	private MapPanel mapPane;
	private CircleLayer rangeLayer;
	private CreatePathLayer navLayer;
	private Coordinates centerPoint;
	private boolean navSelected;
	private IntPoint navOffset;
	private JLabel locationLabel;
	private int pixelRange;
	private Rover rover;
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 * @param state the mission data bean.
	 * @param context the UI context.
	 */
	ProspectingSitePanel(MissionCreate wizard, MissionDataBean state, UIContext context) {
		super(NAME, wizard);
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
		// Create a vertical strut to add some UI space.
		add(Box.createVerticalStrut(10));

		centerPoint = state.getStartingSettlement().getCoordinates();

		// Create the map panel.
		mapPane = new MapPanel(context);
		mapPane.setPreferredSize(new Dimension(400, 512));
		mapPane.loadMap(MapDataFactory.DEFAULT_MAP_TYPE, 0);

		mapPane.addMapLayer(new UnitMapLayer(mapPane), 0);
		rangeLayer = new CircleLayer(Color.GREEN);
		mapPane.addMapLayer(rangeLayer, 1);
		navLayer = new CreatePathLayer(mapPane, false, centerPoint);
		mapPane.addMapLayer(navLayer, 2);
		
		mapPane.addMouseListener(new NavpointMouseListener());
		mapPane.addMouseMotionListener(new NavpointMouseMotionListener());
		mapPane.setMaximumSize(mapPane.getPreferredSize());
		mapPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(mapPane);
		
		// Create the location label.
		locationLabel = new JLabel("Location: ", SwingConstants.CENTER);
		locationLabel.setFont(locationLabel.getFont().deriveFont(Font.BOLD));
		locationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(locationLabel);
		
		// Create a vertical strut to add some UI space.
		add(Box.createVerticalStrut(10));
		
		// Create the instruction label.
				
		// Create the title label.
		String resource = "";
		MissionType type = state.getMissionType();
		if (MissionType.COLLECT_ICE == type) resource = "Ice";
		else if (MissionType.COLLECT_REGOLITH == type) resource = "Regolith";
		JLabel instructionLabel = new JLabel("Drag navpoint flag to desired " + resource + 
				" collection site.");
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD));
		instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instructionLabel);
		
		// Create vertical glue.
		add(Box.createVerticalGlue());

		// Locate map to starting settlement.
		mapPane.showMap(centerPoint);

		// Set rover range ellipse and initial navpoint position.
		rover = state.getRover();
		var rangeKM = getRoverRange(rover);
		rangeLayer.setCircleDetails(centerPoint, rangeKM);

		Coordinates firstPoint = centerPoint.getNewLocation(new Direction(0), rangeKM/4);
		navLayer.addNavpointPosition(firstPoint);
	}

	/**
	 * Commit changes to the mission data bean.
	 */
	@Override
	public void updateState(MissionDataBean state) {
		IntPoint navpointPixel = navLayer.getNavpointPosition(0);
		Coordinates navpoint = mapPane.getCoordsOfPoint(centerPoint, navpointPixel);
		state.setProspectingSite(navpoint);
	}

	/**
	 * Clears information on the wizard panel.
	 */
	@Override
	public void clearState(MissionDataBean state) {
		//navLayer.clearNavpointPositions();

		super.clearState(state);
	}
	
	/**
	 * Converts radius (km) into pixel range on map.
	 * 
	 * @param radius the radius (km).
	 * @return pixel radius.
	 */
	private int convertRadiusToMapPixels(double radius) {
		return MapUtils.getPixelDistance(radius, mapPane.getMap());
	}
	
	/**
	 * Gets the mission rover range.
	 * 
	 * @return range (km)
	 */
	private static double getRoverRange(Rover rover) {
		double range = rover.getEstimatedRange() * RANGE_MODIFIER;
		return range / 2D;
	}
	
	/**
	 * Inner class for listening to mouse events on the navpoint display.
	 */
	private class NavpointMouseListener extends MouseAdapter {
	
		/**
		 * Invoked when a mouse button has been pressed on a component.
		 * @param event the mouse event.
		 */
		@Override
		public void mousePressed(MouseEvent event) {
			if (navLayer.overNavIcon(event.getX(), event.getY()) == 0) {
				// Select navpoint flag.
				navSelected = true;
				navLayer.selectNavpoint(0);
				navOffset = determineOffset(event.getX(), event.getY());
				mapPane.repaint();
			}
		}
		
		/**
		 * Gets the pixel offset from the currently selected navpoint.
		 * @param x the x coordinate selected.
		 * @param y the y coordinate selected.
		 * @return the pixel offset.
		 */
		private IntPoint determineOffset(int x, int y) {
			int xOffset = navLayer.getNavpointPosition(0).getiX() - x;
			int yOffset = navLayer.getNavpointPosition(0).getiY() - y;
			return new IntPoint(xOffset, yOffset);
		}
	
		/**
		 * Invoked when a mouse button has been released on a component.
		 * @param event the mouse event.
		 */
		@Override
		public void mouseReleased(MouseEvent event) {
			navSelected = false;
			navLayer.clearSelectedNavpoint();
			mapPane.repaint();
		}
	}
	
	/**
	 * Inner class for listening to mouse movement on the navpoint display.
	 */
	private class NavpointMouseMotionListener extends MouseMotionAdapter {
		
		/**
		 * Invoked when a mouse button is pressed on a component and then dragged.
		 * @param event the mouse event.
		 */
		@Override
		public void mouseDragged(MouseEvent event) {
			if (navSelected) {
				// Drag navpoint flag.
				int displayX = event.getPoint().x + navOffset.getiX();
				int displayY = event.getPoint().y + navOffset.getiY();
				IntPoint displayPos = new IntPoint(displayX, displayY);
				if (withinBounds(displayPos)) {
					navLayer.setNavpointPosition(0, displayPos);
					Coordinates navpoint = mapPane.getCoordsOfPoint(centerPoint, displayPos);
					locationLabel.setText("Location: " + navpoint.getFormattedString());
				
					mapPane.repaint();
				}
			}
		}
		
		/**
		 * Checks if mouse location is within range boundaries and edge of map display.
		 * 
		 * @param position the mouse location.
		 * @return true if within boundaries.
		 */
		private boolean withinBounds(IntPoint position) {
			
			if (!navLayer.withinDisplayEdges(position)) 
				return false;
			
			pixelRange = convertRadiusToMapPixels(getRoverRange(rover));
            int radius = mapPane.getCenterPoint().getDistance(position);
			return (radius <= pixelRange);
		}
	}
}
