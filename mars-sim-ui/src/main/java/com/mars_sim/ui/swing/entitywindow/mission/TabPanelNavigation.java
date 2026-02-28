/*
 * Mars Simulation Project
 * TabPanelNavigation.java
 * @date 2026-01-31
 * @author Barry Evans
 */

package com.mars_sim.ui.swing.entitywindow.mission;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.map.MapDataFactory;
import com.mars_sim.core.person.ai.mission.Exploration;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.NavPoint;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.map.MapMouseListener;
import com.mars_sim.ui.swing.tool.map.MapPanel;
import com.mars_sim.ui.swing.tool.map.MineralMapLayer;
import com.mars_sim.ui.swing.tool.map.MissionMapLayer;
import com.mars_sim.ui.swing.tool.map.UnitMapLayer;
import com.mars_sim.ui.swing.tool.map.VehicleTrailMapLayer;
import com.mars_sim.ui.swing.utils.SwingHelper;


/**
 * Tab panel for displaying a mission's navpoints.
 */
@SuppressWarnings("serial")
class TabPanelNavigation extends EntityTabPanel<VehicleMission>
	implements EntityListener {

	private MapPanel mapPanel;
	private Vehicle vehicle;
	
	private NavpointTableModel navpointTableModel;

	private MissionMapLayer navpointLayer;

	/**
	 * Constructor.
	 */
	protected TabPanelNavigation(VehicleMission entity, UIContext context) {
		super(
			"Navigation", 
			ImageLoader.getIconByName("mars"), null,
			context, entity
		);

		vehicle = entity.getVehicle();
	}
		
	@Override
	protected void buildUI(JPanel centerContentPanel) {

		// Set the layout.
		centerContentPanel.setLayout(new BorderLayout());

		var mission = getEntity();
		
		// Create the main panel.
		var mapPane = initMapPane(mission);
		centerContentPanel.add(mapPane, BorderLayout.CENTER);
		
		var logPane = initLogPane(mission);
		centerContentPanel.add(logPane, BorderLayout.SOUTH);

		// Register as entity listener.
		vehicle.addEntityListener(this);

		// Force a redraw
		mapPanel.loadMap(MapDataFactory.DEFAULT_MAP_TYPE, 1);
		mapPanel.showMap(mission.getAssociatedSettlement().getCoordinates());
		mapPanel.updateDisplay();
	}

	private JComponent initMapPane(VehicleMission mission) {
		// Create the map panel.
		mapPanel = new MapPanel(getContext());
		mapPanel.setBackground(new Color(0, 0, 0, 128));
		mapPanel.setOpaque(false);

		// Set up mouse control
		mapPanel.setMouseDragger();

		var mouseListener = new MapMouseListener(mapPanel);
		mapPanel.addMouseListener(mouseListener);
		mapPanel.addMouseMotionListener(mouseListener);
		
		// Mineral layer only if mining or exploration mission
		if (mission instanceof Mining || mission instanceof Exploration) {
		    var mineralLayer = new MineralMapLayer(mapPanel);
		    mapPanel.addMapLayer(mineralLayer);
		}					

		// Always add unit layer
		mapPanel.addMapLayer(new UnitMapLayer(mapPanel));

		// Vehicle trail layer for the target vehicle
		var trailLayer = new VehicleTrailMapLayer(mapPanel);
		trailLayer.setSingleVehicle(vehicle);
		mapPanel.addMapLayer(trailLayer);

		// Lastly add navpoint layer
		navpointLayer = new MissionMapLayer(mapPanel, mission);
		navpointLayer.setSelectedNavpoint(null);
		mapPanel.addMapLayer(navpointLayer);

		var mapPane = new JPanel(new BorderLayout());
		mapPane.setBorder(SwingHelper.createLabelBorder("Route"));
        
		var dims = new Dimension(10, 200);
		mapPane.setPreferredSize(dims);
		mapPane.setMinimumSize(dims);
		mapPane.add(mapPanel, BorderLayout.CENTER);
       	return mapPane;	
	}


	private JComponent initLogPane(VehicleMission mission) {
		// Create the navpoint scroll panel.
		JScrollPane navpointScrollPane = new JScrollPane();
        
        // Create the navpoint table model.
        navpointTableModel = new NavpointTableModel(mission);
        
        // Create the navpoint table.
        var navpointTable = new JTable(navpointTableModel);
        navpointTable.setRowSelectionAllowed(true);
        navpointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navpointTable.getSelectionModel().addListSelectionListener(e -> {
			// Recenter map on selected navpoint.
			if (e.getValueIsAdjusting()) {
				int index = navpointTable.getSelectedRow();
				if (index > -1) {
					NavPoint navpoint = getEntity().getNavpoints().get(index); 
					navpointLayer.setSelectedNavpoint(navpoint);
					mapPanel.showMap(navpoint.getLocation());
				}
				else navpointLayer.setSelectedNavpoint(null);
			}
        });
        navpointScrollPane.setViewportView(navpointTable);

		var dims = new Dimension(10, 100);
		navpointScrollPane.setPreferredSize(dims);
		navpointScrollPane.setMinimumSize(dims);

		return navpointScrollPane;
	}
	
	/**
	 * Catches mission update event.
	 * 
	 * @param event the entity event.
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		String type = event.getType();
		if (VehicleMission.NAVPOINTS_EVENT.equals(type)) {
			// Update mission navpoints.
			SwingUtilities.invokeLater(() -> navpointTableModel.updateNavpoints());
		}
		else if (EntityEventType.SPEED_EVENT.equals(type)) {
			// Update map display.
			SwingUtilities.invokeLater(() -> mapPanel.updateDisplay());
		}
	}
	
	/**
	 * Prepares navpoint panel for deletion.
	 */
	@Override
	public void destroy() {
		vehicle.removeEntityListener(this);
		if (mapPanel != null) {
			mapPanel.destroy();
		}
		super.destroy();
	}
	
	/**
	 * Navpoint table model as an inner class.
	 */
	private static class NavpointTableModel extends AbstractTableModel {
		
		// Private members.
		private List<NavPoint> navpoints;
		private VehicleMission missionCache;
		
		/**
		 * Constructor.
		 */
		private NavpointTableModel(VehicleMission mission) {
			navpoints = new ArrayList<>(mission.getNavpoints());
			this.missionCache = mission;
		}
		
		/**
		 * Returns the number of rows in the model.
		 * 
		 * @return number of rows.
		 */
		@Override
		public int getRowCount() {
            return navpoints.size();
        }
		
		/**
		 * Returns the number of columns in the model.
		 * 
		 * @return number of columns.
		 */
		@Override
		public int getColumnCount() {
            return 5;
        }
		
		/**
		 * Returns the name of the column at columnIndex.
		 * 
		 * @param columnIndex the index of the column.
		 * @return the name of the column.
		 */
		@Override
		public String getColumnName(int columnIndex) {
            return switch (columnIndex) {
              case 0 -> Msg.getString("NavpointPanel.column.name");
              case 1 -> Msg.getString("NavpointPanel.column.location");
              case 2 -> Msg.getString("NavpointPanel.column.description");
              case 3 -> Msg.getString("NavpointPanel.column.distance.leg");
              case 4 -> Msg.getString("NavpointPanel.column.distance.actual");
              default -> "";
            };
        }
		
		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row the row index.
		 * @param column the column index.
		 * @return the value object.
		 */
		@Override
		public Object getValueAt(int row, int column) {
            if (row < navpoints.size()) {
            	NavPoint navpoint = navpoints.get(row);
            	return switch (column) {
					case 0 -> Msg.getString("NavpointPanel.column.navpoint") + " " + (row + 1);
					case 1 -> navpoint.getLocation().getFormattedString();
					case 2 -> navpoint.getDescription();
					case 3 -> Math.round(navpoint.getPointToPointDistance()*10.0)/10.0;
					case 4 -> Math.round(navpoint.getActualTravelled()*10.0)/10.0;
					default -> Msg.getString("unknown");
					}; //$NON-NLS-1$
            }   
            else return Msg.getString("unknown"); //$NON-NLS-1$
        }
		
		/**
		 * Updates the navpoints for the table.
		 */
		public void updateNavpoints() {  
			navpoints.clear();
			navpoints.addAll(missionCache.getNavpoints());
			fireTableDataChanged();
		}
	}

}
