/*
 * Mars Simulation Project
 * NavpointPanel.java
 * @date 2023-06-18
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.Landmark;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.map.Map;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;


/**
 * Tab panel for displaying a mission's navpoints.
 */
@SuppressWarnings("serial")
public class NavpointPanel
extends JPanel
implements MissionListener {
	
	private static final int WIDTH = MapPanel.MAP_BOX_WIDTH;
	private static final int HEIGHT = MapPanel.MAP_BOX_HEIGHT;
	private static final double TWO_PI = Math.PI * 2D;
	
	// Private members.
	private Mission missionCache;
	private MapPanel mapPanel;
	private transient VehicleTrailMapLayer trailLayer;
	private transient NavpointMapLayer navpointLayer;
    private transient MineralMapLayer mineralLayer;
	private transient NavpointTableModel navpointTableModel;
	private JTable navpointTable;

	private Coordinates coordCache = new Coordinates(0, 0);
	
	private UnitManager unitManager;
	private List<Landmark> landmarks;

	/**
	 * Constructor.
	 */
	protected NavpointPanel(MissionWindow missionWindow) {
		Simulation sim = missionWindow.getDesktop().getSimulation();

		unitManager = sim.getUnitManager();
		landmarks = SimulationConfig.instance().getLandmarkConfiguration().getLandmarkList();

		// Set the layout.
		setLayout(new BorderLayout());
		
		// Create the main panel.
		JPanel mainPane = new JPanel(new BorderLayout(0, 0));	
		mainPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		// Create the map display panel.
		JPanel mapDisplayPane = new JPanel(new BorderLayout(0, 0));
		JPanel mapPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		mapPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mapPane.setAlignmentY(Component.TOP_ALIGNMENT);
		mapDisplayPane.add(mapPane, BorderLayout.CENTER);
		
		
		// Create the navpoint table panel.
		JPanel navpointTablePane = new JPanel(new BorderLayout());
		navpointTablePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		navpointTablePane.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		navpointTablePane.setPreferredSize(new Dimension(-1, -1)); 
		navpointTablePane.setBorder(new MarsPanelBorder());

		
		///////////////////////////////////////////////////////////
		
		
		// Define splitPane to house mapDisplayPane and ..
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapDisplayPane, navpointTablePane);
	    splitPane.setOneTouchExpandable(true);
	    splitPane.setDividerLocation(HEIGHT + 10);
	    mainPane.add(splitPane, BorderLayout.CENTER);
		
	    
		///////////////////////////////////////////////////////////

	    
		// Create the map panel.
		mapPanel = new MapPanel(missionWindow.getDesktop(), 500L);
		// Set up mouse control
		mapPanel.setNavpointPanel(this);
		mapPanel.addMouseListener(new MapListener());
		mapPanel.addMouseMotionListener(new MouseMotionListener());
		
		mapPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mapPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		trailLayer = new VehicleTrailMapLayer();
		navpointLayer = new NavpointMapLayer(this);
        mineralLayer = new MineralMapLayer(this);
        
		// Note remove the ShadingMapLayer to improve clarity of map display mapPanel.addMapLayer(new ShadingMapLayer(mapPanel), 0); 
        mapPanel.addMapLayer(mineralLayer, 1);
		mapPanel.addMapLayer(new UnitIconMapLayer(mapPanel), 2);
		mapPanel.addMapLayer(new UnitLabelMapLayer(), 3);
		mapPanel.addMapLayer(trailLayer, 4);
		mapPanel.addMapLayer(navpointLayer, 5);
  
        mapPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));  
        
        mapPane.add(mapPanel);
        
		// Create the north button.
        JButton northButton = new JButton(ImageLoader.getIconByName("map/navpoint_north")); //$NON-NLS-1$
		northButton.addActionListener(e -> {
			// Recenter the map to the north by a 1/8 map.
			Coordinates centerCoords = mapPanel.getCenterLocation();
			if (centerCoords != null) {
				double phi = centerCoords.getPhi();
				phi = phi - Map.QUARTER_HALF_MAP_ANGLE;
				if (phi < 0D) phi = 0D;
				mapPanel.showMap(new Coordinates(phi, centerCoords.getTheta()));
			}
		});
		mapDisplayPane.add(northButton, BorderLayout.NORTH);
		
		// Create the west button.
		JButton westButton = new JButton(ImageLoader.getIconByName("map/navpoint_west"));
		westButton.setMargin(new Insets(1, 1, 1, 1));
		westButton.addActionListener(e -> {
			// Recenter the map to the west by 1/8 map.
			Coordinates centerCoords = mapPanel.getCenterLocation();
			if (centerCoords != null) {
				double theta = centerCoords.getTheta();
				theta = theta - Map.QUARTER_HALF_MAP_ANGLE;
				if (theta < 0D) theta += (TWO_PI);
				mapPanel.showMap(new Coordinates(centerCoords.getPhi(), theta));
			}
		});
		mapDisplayPane.add(westButton, BorderLayout.WEST);
		
		// Create the east button.
		JButton eastButton = new JButton(ImageLoader.getIconByName("map/navpoint_east"));
		eastButton.setMargin(new Insets(1, 1, 1, 1));
		eastButton.addActionListener(e -> {
			// Recenter the map to the east by 1/8 map.
			Coordinates centerCoords = mapPanel.getCenterLocation();
			if (centerCoords != null) {
				double theta = centerCoords.getTheta();
				theta = theta + Map.QUARTER_HALF_MAP_ANGLE;
				if (theta < (TWO_PI)) theta -= (TWO_PI);
				mapPanel.showMap(new Coordinates(centerCoords.getPhi(), theta));
			}
		});
		mapDisplayPane.add(eastButton, BorderLayout.EAST);
		
		// Create the south button.
		JButton southButton = new JButton(ImageLoader.getIconByName("map/navpoint_south"));
		southButton.addActionListener(e -> {
			// Recenter the map to the south by 1/8 map.
			Coordinates centerCoords = mapPanel.getCenterLocation();
			if (centerCoords != null) {
				double phi = centerCoords.getPhi();
				phi = phi + Map.QUARTER_HALF_MAP_ANGLE;
				if (phi > Math.PI) phi = Math.PI;
				mapPanel.showMap(new Coordinates(phi, centerCoords.getTheta()));
			}
		});
		mapDisplayPane.add(southButton, BorderLayout.SOUTH);
			

		///////////////////////////////////////////////////////////
		
		
		// Create the navpoint scroll panel.
		JScrollPane navpointScrollPane = new JScrollPane();
        navpointTablePane.add(navpointScrollPane);
        
        // Create the navpoint table model.
        navpointTableModel = new NavpointTableModel();
        
        // Create the navpoint table.
        navpointTable = new JTable(navpointTableModel);
        navpointTable.setPreferredSize(new Dimension(-1, -1)); 
        navpointTable.setRowSelectionAllowed(true);
        navpointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navpointTable.getSelectionModel().addListSelectionListener(e -> {
			// Recenter map on selected navpoint.
			if (e.getValueIsAdjusting() && (missionCache != null) 
					&& (missionCache instanceof VehicleMission)) {
				VehicleMission travelMission = (VehicleMission) missionCache;
				int index = navpointTable.getSelectedRow();
				if (index > -1) {
					NavPoint navpoint = travelMission.getNavpoints().get(index); 
					navpointLayer.setSelectedNavpoint(navpoint);
					mapPanel.showMap(navpoint.getLocation());
				}
				else navpointLayer.setSelectedNavpoint(null);
			}
        });
        navpointScrollPane.setViewportView(navpointTable);
	}
	
	/**
	 * Time has advanced.
	 * 
	 * @param pulse The clock change
	 */
	public void update(ClockPulse pulse) {
		mapPanel.update(pulse);
	}

	/**
	 * Updates coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary.
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		if (newCoords != null
			&& (coordCache == null || !coordCache.equals(newCoords))) {
				coordCache = newCoords;
				mapPanel.showMap(newCoords);
		}
		else {
			coordCache = null;
			mapPanel.showMap(null);
		}
	}

	private class MapListener extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent event) {
			// nothing
		}
		@Override
		public void mouseExited(MouseEvent event) {
			// nothing
		}
		@Override
		public void mouseClicked(MouseEvent event) {
			checkClick(event);
		}
	}

	private class MouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		@Override
		public void mouseDragged(MouseEvent event) {
			checkHover(event);
		}
	}
	
	/**
	 * Checks if the mouse clicks over an object.
	 * 
	 * @param event
	 */
	public void checkClick(MouseEvent event) {

		if (mapPanel.getCenterLocation() != null) {
			displayUnits(event);
		}
	}

	/**
	 * Displays the units on the map.
	 * 
	 * @param event
	 */
	public void displayUnits(MouseEvent event) {
		Coordinates clickedPosition = mapPanel.getMouseCoordinates(event.getX(), event.getY());

		Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

		// Open window if unit is clicked on the map
		while (i.hasNext()) {
			Unit unit = i.next();
			
			if (unit.getUnitType() == UnitType.VEHICLE
				&& !((Vehicle)unit).isOutsideOnMarsMission()) {
				// Display the cursor for this vehicle only when
				// it's outside on a mission
				return;
			}
			
			UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
			if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
				Coordinates unitCoords = unit.getCoordinates();
				double clickRange = unitCoords.getDistance(clickedPosition);
				double unitClickRange = displayInfo.getMapClickRange();
				if (clickRange < unitClickRange) {
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				} else
					mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	/**
	 * Checks if the mouse is hovering over an object.
	 * 
	 * @param event
	 */
	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapPanel.getCenterLocation();
		if (mapCenter != null) {
			Coordinates mousePos = mapPanel.getMouseCoordinates(event.getX(), event.getY());
			boolean onTarget = false;

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				
				if (unit.getUnitType() == UnitType.VEHICLE
					&& !((Vehicle)unit).isOutsideOnMarsMission()) {
					// Display the cursor for this vehicle only when
					// it's outside on a mission
					return;	
				}
				
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = Coordinates.computeDistance(unitCoords, mousePos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						// Click on this unit.
						mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						onTarget = true;
					}
				}
			}

			// Change mouse cursor if hovering over a landmark on the map
			Iterator<Landmark> j = landmarks.iterator();
			while (j.hasNext()) {
				Landmark landmark = (Landmark) j.next();

				Coordinates unitCoords = landmark.getLandmarkCoord();
				double clickRange = Coordinates.computeDistance(unitCoords, mousePos);
				double unitClickRange = 40D;

				if (clickRange < unitClickRange) {
					onTarget = true;
					// Click on a landmark
					// Note: may open a panel showing any special items at that landmark
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				}
			}

			if (!onTarget) {
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	/**
	 * Sets the mission object.
	 * 
	 * @param newMission
	 */
	public void setMission(Mission newMission) {
		boolean isDiff = missionCache != newMission;
				
		if (isDiff) {
			// Remove this as previous mission listener.
			if (missionCache != null) {
				missionCache.removeMissionListener(this);
			}
			// Update the cache
			missionCache = newMission;

			if (missionCache != null) {
				// Add this as listener for new mission.
				missionCache.addMissionListener(this);
				// Update the mission content on the Nav tab
				updateNavTab();
			}
			else {
				clearNavTab(null);
			}
		}
	}
	

	/**
	 * Updates the mission content on the Nav tab.
	 */
	private void updateNavTab() {
		// Updates coordinates in map
		updateCoords(missionCache.getAssociatedSettlement().getCoordinates());
		
		if (missionCache instanceof VehicleMission) {
			trailLayer.setSingleVehicle(((VehicleMission) missionCache).getVehicle());
		}
		
		navpointLayer.setSingleMission(missionCache);
		navpointLayer.setSelectedNavpoint(null);
		navpointTableModel.updateNavpoints();
		
		if ((missionCache instanceof Exploration) || (missionCache instanceof Mining)) {
			if (!mapPanel.hasMapLayer(mineralLayer)) mapPanel.addMapLayer(mineralLayer, 1);
		}
		else {
			if (mapPanel.hasMapLayer(mineralLayer)) mapPanel.removeMapLayer(mineralLayer);
		}
		
		mapPanel.showMap(missionCache.getCurrentMissionLocation());		
	}
	
	/**
	 * Clears the mission content on the Nav tab
	 */
	private void clearNavTab(Settlement settlement) {
		// Center the map to this settlement's coordinate
		if (settlement != null)
			updateCoords(settlement.getCoordinates());
		
		// Clear map and info.
		trailLayer.setSingleVehicle(null);
		navpointLayer.setSingleMission(null);
		navpointLayer.setSelectedNavpoint(null);
		navpointTableModel.updateNavpoints();
        if (mapPanel.hasMapLayer(mineralLayer)) 
        	mapPanel.removeMapLayer(mineralLayer);
		mapPanel.showMap(null);
	}
	
	/**
	 * Catches mission update event.
	 * 
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		MissionEventType type = event.getType();
		if (MissionEventType.NAVPOINTS_EVENT == type) {
			// Update mission navpoints.
			SwingUtilities.invokeLater(() -> navpointTableModel.updateNavpoints());
		}
	}
	
	/**
	 * Prepares navpoint panel for deletion.
	 */
	public void destroy() {
		mapPanel.destroy();
	}
	
	/**
	 * Navpoint table model.
	 * Inner class
	 */
	private class NavpointTableModel extends AbstractTableModel {
		
		// Private members.
		private List<NavPoint> navpoints;
		
		/**
		 * Constructor.
		 */
		private NavpointTableModel() {
			navpoints = new ArrayList<>();
		}
		
		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		public int getRowCount() {
            return navpoints.size();
        }
		
		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		public int getColumnCount() {
            return 4;
        }
		
		/**
		 * Returns the name of the column at columnIndex.
		 * 
		 * @param columnIndex the index of the column.
		 * @return the name of the column.
		 */
		@Override
		public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return Msg.getString("NavpointPanel.column.name"); //$NON-NLS-1$
            else if (columnIndex == 1) return Msg.getString("NavpointPanel.column.location"); //$NON-NLS-1$
            else if (columnIndex == 2) return Msg.getString("NavpointPanel.column.description"); //$NON-NLS-1$
            else if (columnIndex == 3) return Msg.getString("NavpointPanel.column.distance"); //$NON-NLS-1$
            else return "";
        }
		
		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row the row index.
		 * @param column the column index.
		 * @return the value object.
		 */
		public Object getValueAt(int row, int column) {
            if (row < navpoints.size()) {
            	NavPoint navpoint = navpoints.get(row);
            	if (column == 0) return Msg.getString("NavpointPanel.column.navpoint") + " " + (row + 1); //$NON-NLS-1$
            	else if (column == 1) return navpoint.getLocation().getFormattedString();
            	else if (column == 2) return navpoint.getDescription();
            	else if (column == 3) return Math.round(navpoint.getDistance()*10.0)/10.0;
            	else return Msg.getString("unknown"); //$NON-NLS-1$
            }   
            else return Msg.getString("unknown"); //$NON-NLS-1$
        }
		
		/**
		 * Updates the navpoints for the table.
		 */
		public void updateNavpoints() {
		    
			if (missionCache instanceof VehicleMission travelMission) {
				navpoints.clear();
				navpoints.addAll(travelMission.getNavpoints());
				fireTableDataChanged();
			}
			else {
				if (navpoints.size() > 0) {
					navpoints.clear();
					fireTableDataChanged();
				}
			}
		}
	}
}
