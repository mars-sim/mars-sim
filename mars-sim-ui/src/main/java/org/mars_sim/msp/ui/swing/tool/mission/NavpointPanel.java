/**
 * Mars Simulation Project
 * NavpointPanel.java
 * @version 3.1.0 2017-11-09
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.mapdata.MapDataUtil;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.mars.Landmark;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.Map;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.VehicleTrailMapLayer;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * Tab panel for displaying a mission's navpoints.
 */
@SuppressWarnings("serial")
public class NavpointPanel
extends WebPanel
implements ListSelectionListener, MissionListener {

	private static int WIDTH = MapDataUtil.IMAGE_WIDTH;
	private static int HEIGHT = MapDataUtil.IMAGE_HEIGHT;
	
	// Private members.
	private Mission currentMission;
	private MapPanel mapPanel;
	private VehicleTrailMapLayer trailLayer;
	private NavpointMapLayer navpointLayer;
    private MineralMapLayer mineralLayer;
	private NavpointTableModel navpointTableModel;
	private JTable navpointTable;
	private MainDesktopPane desktop;
	
	private static Simulation sim = Simulation.instance();
	private static TerrainElevation terrainElevation;
	private static Mars mars;
	
	private static UnitManager unitManager = sim.getUnitManager();
	private static List<Landmark> landmarks = sim.getMars().getSurfaceFeatures().getLandmarks();
	

	/**
	 * Constructor.
	 */
	protected NavpointPanel(MainDesktopPane desktop) {
		this.desktop = desktop;
		
		if (mars == null)
			mars = sim.getMars();
		if (terrainElevation == null)
			terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();
		
		// Set the layout.
		setLayout(new BorderLayout());
		
		// Create the main panel.
		//Box mainPane = Box.createVerticalBox();
		WebPanel mainPane = new WebPanel(new BorderLayout(0, 0));
		mainPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPane.setSize(new Dimension(WIDTH, HEIGHT));
		mainPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		// Create the map display panel.
		WebPanel mapDisplayPane = new WebPanel(new BorderLayout(0, 0));
		mapDisplayPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		mapDisplayPane.setSize(new Dimension(WIDTH, HEIGHT));
		mapDisplayPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		WebPanel left = new WebPanel();
        left.setPreferredSize(new Dimension(48, HEIGHT));
        WebPanel right = new WebPanel();
        right.setPreferredSize(new Dimension(48, HEIGHT));
		mainPane.add(mapDisplayPane, BorderLayout.CENTER);
		mainPane.add(left, BorderLayout.WEST);
		mainPane.add(right, BorderLayout.EAST);
		//mainPane.add(Box.createVerticalStrut(10));
	
		// Create the map panel.
		mapPanel = new MapPanel(desktop, 500L);
		// Set up mouse control
		mapPanel.setNavpointPanel(this);
		mapPanel.addMouseListener(new mapListener());
		mapPanel.addMouseMotionListener(new mouseMotionListener());
		
		mapPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		// Note remove the ShadingMapLayer to improve clarity of map display 
//		mapPanel.addMapLayer(new ShadingMapLayer(mapPanel), 0);
		// Note: mineralLayer is 1; // mapPanel.addMapLayer(mineralLayer, 1);
		mapPanel.addMapLayer(new UnitIconMapLayer(mapPanel), 2);
		mapPanel.addMapLayer(new UnitLabelMapLayer(), 3);
		
		trailLayer = new VehicleTrailMapLayer();
		mapPanel.addMapLayer(trailLayer, 4);
		navpointLayer = new NavpointMapLayer(this);
		mapPanel.addMapLayer(navpointLayer, 5);
        mineralLayer = new MineralMapLayer(this);
        
        // Forcing map panel to be 300x300 size.
        mapPanel.setSize(new Dimension(WIDTH, HEIGHT));
        mapPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        mapDisplayPane.add(mapPanel, BorderLayout.CENTER);
        
		// Create the north button.
        WebButton northButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.navpoint.north"))); //$NON-NLS-1$
		northButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Recenter the map to the north by a 1/8 map.
				Coordinates centerCoords = mapPanel.getCenterLocation();
				if (centerCoords != null) {
					double phi = centerCoords.getPhi();
					phi = phi - CannedMarsMap.HALF_MAP_ANGLE/4D;
					if (phi < 0D) phi = 0D;
					mapPanel.showMap(new Coordinates(phi, centerCoords.getTheta()));
				}
			}
		});
		mapDisplayPane.add(northButton, BorderLayout.NORTH);
		
		// Create the west button.
		WebButton westButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.navpoint.west"))); //$NON-NLS-1$
		westButton.setMargin(new Insets(1, 1, 1, 1));
		westButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Recenter the map to the west by 1/8 map.
				Coordinates centerCoords = mapPanel.getCenterLocation();
				if (centerCoords != null) {
					double theta = centerCoords.getTheta();
					theta = theta - CannedMarsMap.HALF_MAP_ANGLE/4D;
					if (theta < 0D) theta += (Math.PI * 2D);
					mapPanel.showMap(new Coordinates(centerCoords.getPhi(), theta));
				}
			}
		});
		mapDisplayPane.add(westButton, BorderLayout.WEST);
		
		// Create the east button.
		WebButton eastButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.navpoint.east"))); //$NON-NLS-1$
		eastButton.setMargin(new Insets(1, 1, 1, 1));
		eastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Recenter the map to the east by 1/8 map.
				Coordinates centerCoords = mapPanel.getCenterLocation();
				if (centerCoords != null) {
					double theta = centerCoords.getTheta();
					theta = theta + CannedMarsMap.HALF_MAP_ANGLE/4D;
					if (theta < (Math.PI * 2D)) theta -= (Math.PI * 2D);
					mapPanel.showMap(new Coordinates(centerCoords.getPhi(), theta));
				}
			}
		});
		mapDisplayPane.add(eastButton, BorderLayout.EAST);
		
		// Create the south button.
		WebButton southButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.navpoint.south"))); //$NON-NLS-1$
		southButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Recenter the map to the south by 1/8 map.
				Coordinates centerCoords = mapPanel.getCenterLocation();
				if (centerCoords != null) {
					double phi = centerCoords.getPhi();
					phi = phi + CannedMarsMap.HALF_MAP_ANGLE/4D;
					if (phi > Math.PI) phi = Math.PI;
					mapPanel.showMap(new Coordinates(phi, centerCoords.getTheta()));
				}
			}
		});
		mapDisplayPane.add(southButton, BorderLayout.SOUTH);
		
		// Create the navpoint table panel.
		WebPanel navpointTablePane = new WebPanel(new BorderLayout(0, 0));
		navpointTablePane.setBorder(new MarsPanelBorder());
		navpointTablePane.setPreferredSize(new Dimension(-1, 130));
		//mainPane.add(navpointTablePane);
		add(navpointTablePane, BorderLayout.SOUTH);
		
		// Create the navpoint scroll panel.
		WebScrollPane navpointScrollPane = new WebScrollPane();
        navpointTablePane.add(navpointScrollPane, BorderLayout.CENTER);
        
        // Create the navpoint table model.
        navpointTableModel = new NavpointTableModel();
        
        // Create the navpoint table.
        navpointTable = new ZebraJTable(navpointTableModel);
		TableStyle.setTableStyle(navpointTable);
        navpointTable.setRowSelectionAllowed(true);
        navpointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navpointTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
            	public void valueChanged(ListSelectionEvent e) {
            		if (e.getValueIsAdjusting()) {
            			// Recenter map on selected navpoint.
            			if ((currentMission != null) && (currentMission instanceof TravelMission)) {
            				TravelMission travelMission = (TravelMission) currentMission;
            				int index = navpointTable.getSelectedRow();
            				if (index > -1) {
            					NavPoint navpoint = travelMission.getNavpoint(index); 
            					navpointLayer.setSelectedNavpoint(navpoint);
            					mapPanel.showMap(navpoint.getLocation());
            				}
            				else navpointLayer.setSelectedNavpoint(null);
            			}
            		}
            	}
            });
        navpointScrollPane.setViewportView(navpointTable);
	}
	
	/**
	 * Update coordinates in map, buttons, and globe Redraw map and globe if
	 * necessary
	 * 
	 * @param newCoords the new center location
	 */
	public void updateCoords(Coordinates newCoords) {
		mapPanel.showMap(newCoords);
	}

	private class mapListener extends MouseAdapter {
		public void mouseEntered(MouseEvent event) {
			// checkHover(event);
		}
		public void mouseExited(MouseEvent event) {
		}

		public void mouseClicked(MouseEvent event) {
			checkClick(event);
		}
	}

	private class mouseMotionListener extends MouseMotionAdapter {
		public void mouseMoved(MouseEvent event) {
			checkHover(event);
		}
		public void mouseDragged(MouseEvent event) {
		}
	}
	
	public void checkClick(MouseEvent event) {

		if (mapPanel.getCenterLocation() != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);

			Coordinates clickedPosition = mapPanel.getCenterLocation().convertRectToSpherical(x, y, rho);
	
			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Open window if unit is clicked on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = unitCoords.getDistance(clickedPosition);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
//						openUnitWindow(unit);
					} else
						mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		}
	}

	public void checkHover(MouseEvent event) {

		Coordinates mapCenter = mapPanel.getCenterLocation();
		if (mapCenter != null) {
			double rho = CannedMarsMap.PIXEL_RHO;

			double x = (double) (event.getX() - (Map.DISPLAY_WIDTH / 2D) - 1);
			double y = (double) (event.getY() - (Map.DISPLAY_HEIGHT / 2D) - 1);
			// System.out.println("x is " + x + " y is " + y);
			Coordinates mousePos = mapPanel.getCenterLocation().convertRectToSpherical(x, y, rho);
			boolean onTarget = false;

			Iterator<Unit> i = unitManager.getDisplayUnits().iterator();

			// Change mouse cursor if hovering over an unit on the map
			while (i.hasNext()) {
				Unit unit = i.next();
				UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
				if (displayInfo != null && displayInfo.isMapDisplayed(unit)) {
					Coordinates unitCoords = unit.getCoordinates();
					double clickRange = Coordinates.computeDistance(unitCoords, mousePos);
					double unitClickRange = displayInfo.getMapClickRange();
					if (clickRange < unitClickRange) {
						// System.out.println("you're on a settlement or vehicle");
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
					// System.out.println("you're on a landmark");
					// TODO: may open a panel showing any special items at that landmark
					mapPanel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					// System.out.println("right on landmark");
				}
			}

			if (!onTarget) {
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	/**
	 * Implemented from ListSelectionListener.
	 * Note: this is called when a mission is selected on MissionWindow's mission list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		Mission mission = (Mission) ((JList<?>) e.getSource()).getSelectedValue();
		
		// Remove this as previous mission listener.
		if ((currentMission != null) && (currentMission != mission)) 
			currentMission.removeMissionListener(this);
		
		if (mission != null) {
			if (mission != currentMission) {
				// Add this as listener for new mission.
				mission.addMissionListener(this);
				
				// Update map and info for new mission.
				currentMission = mission;
				if (mission.getMembersNumber() > 0) {
					if (mission instanceof VehicleMission) {
						trailLayer.setSingleVehicle(((VehicleMission) mission).getVehicle());
                    }
                    
					navpointLayer.setSingleMission(mission);
					navpointLayer.setSelectedNavpoint(null);
					navpointTableModel.updateNavpoints();
                    
                    if ((mission instanceof Exploration) || (mission instanceof Mining)) {
                        if (!mapPanel.hasMapLayer(mineralLayer)) mapPanel.addMapLayer(mineralLayer, 1);
                    }
                    else {
                        if (mapPanel.hasMapLayer(mineralLayer)) mapPanel.removeMapLayer(mineralLayer);
                    }
                    
                    mapPanel.showMap(currentMission.getCurrentMissionLocation());
				}
			}
		}
		else {
			// Clear map and info.
			currentMission = null;
			trailLayer.setSingleVehicle(null);
			navpointLayer.setSingleMission(null);
			navpointLayer.setSelectedNavpoint(null);
			navpointTableModel.updateNavpoints();
            if (mapPanel.hasMapLayer(mineralLayer)) mapPanel.removeMapLayer(mineralLayer);
			mapPanel.showMap(null);
		}
	}

	/**
	 * Catch mission update event.
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		MissionEventType type = event.getType();
		if (MissionEventType.NAVPOINTS_EVENT == type) {
			// Update mission navpoints.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					navpointTableModel.updateNavpoints();
//					int t = MainScene.getTheme();		
//					if (theme != t) {
//						theme = t;
//						TableStyle.setTableStyle(navpointTable);
//					}

				}
			});
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
			navpoints = new ArrayList<NavPoint>();
		}
		
		/**
		 * Returns the number of rows in the model.
		 * @return number of rows.
		 */
		public int getRowCount() {
            return navpoints.size();
        }
		
		/**
		 * Returns the number of columns in the model.
		 * @return number of columns.
		 */
		public int getColumnCount() {
            return 3;
        }
		
		/**
		 * Returns the name of the column at columnIndex.
		 * @param columnIndex the index of the column.
		 * @return the name of the column.
		 */
		public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return Msg.getString("NavpointPanel.column.name"); //$NON-NLS-1$
            else if (columnIndex == 1) return Msg.getString("NavpointPanel.column.location"); //$NON-NLS-1$
            else if (columnIndex == 2) return Msg.getString("NavpointPanel.column.description"); //$NON-NLS-1$
            else return ""; //$NON-NLS-1$
        }
		
		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param row the row index.
		 * @param column the column index.
		 * @return the value object.
		 */
		public Object getValueAt(int row, int column) {
            if (row < navpoints.size()) {
            	NavPoint navpoint = navpoints.get(row);
            	if (column == 0) return Msg.getString("NavpointPanel.column.navpoint") + " " + (row + 1); //$NON-NLS-1$
            	else if (column == 1) return navpoint.getLocation().getFormattedString();
            	else if (column == 2) return Conversion.capitalize(navpoint.getDescription());
            	else return Msg.getString("unknown"); //$NON-NLS-1$
            }   
            else return Msg.getString("unknown"); //$NON-NLS-1$
        }
		
		/**
		 * Updates the navpoints for the table.
		 */
		public void updateNavpoints() {
		    
			if ((currentMission != null) && (currentMission instanceof TravelMission)) {
				navpoints.clear();
				TravelMission travelMission = (TravelMission) currentMission;
				for (int x=0; x < travelMission.getNumberOfNavpoints(); x++) 
					navpoints.add(travelMission.getNavpoint(x));
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