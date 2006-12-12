/**
 * Mars Simulation Project
 * NavpointPanel.java
 * @version 2.80 2006-10-31
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.simulation.person.ai.mission.NavPoint;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.ui.standard.ImageLoader;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.NavpointMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.VehicleTrailMapLayer;


public class NavpointPanel extends JPanel implements ListSelectionListener,
		MissionListener {

	private Mission currentMission;
	private MapPanel mapPane;
	private VehicleTrailMapLayer trailLayer;
	private NavpointMapLayer navpointLayer;
	private NavpointTableModel navpointTableModel;
	private JTable navpointTable;
	
	NavpointPanel() {
		
		setLayout(new BorderLayout());
		
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		JPanel mapDisplayPane = new JPanel(new BorderLayout(0, 0));
		mainPane.add(mapDisplayPane);
		
		mapPane = new MapPanel();
		mapPane.addMapLayer(new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(new UnitLabelMapLayer());
		trailLayer = new VehicleTrailMapLayer();
		mapPane.addMapLayer(trailLayer);
		navpointLayer = new NavpointMapLayer(this);
		mapPane.addMapLayer(navpointLayer);
		mapDisplayPane.add(mapPane, BorderLayout.CENTER);
		
		JButton northButton = new JButton(ImageLoader.getIcon("NavpointNorth"));
		northButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Coordinates centerCoords = mapPane.getCenterLocation();
				if (centerCoords != null) {
					double phi = centerCoords.getPhi();
					phi = phi - CannedMarsMap.HALF_MAP_ANGLE;
					if (phi < 0D) phi = 0D;
					mapPane.showMap(new Coordinates(phi, centerCoords.getTheta()));
				}
			}
		});
		mapDisplayPane.add(northButton, BorderLayout.NORTH);
		
		JButton westButton = new JButton(ImageLoader.getIcon("NavpointWest"));
		westButton.setMargin(new Insets(1, 1, 1, 1));
		westButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Coordinates centerCoords = mapPane.getCenterLocation();
				if (centerCoords != null) {
					double theta = centerCoords.getTheta();
					theta = theta - CannedMarsMap.HALF_MAP_ANGLE;
					if (theta < 0D) theta += (Math.PI * 2D);
					mapPane.showMap(new Coordinates(centerCoords.getPhi(), theta));
				}
			}
		});
		mapDisplayPane.add(westButton, BorderLayout.WEST);
		
		JButton eastButton = new JButton(ImageLoader.getIcon("NavpointEast"));
		eastButton.setMargin(new Insets(1, 1, 1, 1));
		eastButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Coordinates centerCoords = mapPane.getCenterLocation();
				if (centerCoords != null) {
					double theta = centerCoords.getTheta();
					theta = theta + CannedMarsMap.HALF_MAP_ANGLE;
					if (theta < (Math.PI * 2D)) theta -= (Math.PI * 2D);
					mapPane.showMap(new Coordinates(centerCoords.getPhi(), theta));
				}
			}
		});
		mapDisplayPane.add(eastButton, BorderLayout.EAST);
		
		JButton southButton = new JButton(ImageLoader.getIcon("NavpointSouth"));
		southButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Coordinates centerCoords = mapPane.getCenterLocation();
				if (centerCoords != null) {
					double phi = centerCoords.getPhi();
					phi = phi + CannedMarsMap.HALF_MAP_ANGLE;
					if (phi > Math.PI) phi = Math.PI;
					mapPane.showMap(new Coordinates(phi, centerCoords.getTheta()));
				}
			}
		});
		mapDisplayPane.add(southButton, BorderLayout.SOUTH);
		
		JPanel navpointTablePane = new JPanel(new BorderLayout(0, 0));
		navpointTablePane.setBorder(new MarsPanelBorder());
		navpointTablePane.setPreferredSize(new Dimension(100, 100));
		mainPane.add(navpointTablePane);
		
		JScrollPane navpointScrollPane = new JScrollPane();
        navpointTablePane.add(navpointScrollPane, BorderLayout.CENTER);
        
        navpointTableModel = new NavpointTableModel();
        navpointTable = new JTable(navpointTableModel);
        navpointTable.setRowSelectionAllowed(true);
        navpointTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navpointTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
            	public void valueChanged(ListSelectionEvent e) {
            		if (e.getValueIsAdjusting()) {
            			if ((currentMission != null) && (currentMission instanceof TravelMission)) {
            				TravelMission travelMission = (TravelMission) currentMission;
            				int index = navpointTable.getSelectedRow();
            				if (index > -1) {
            					NavPoint navpoint = travelMission.getNavpoint(index); 
            					navpointLayer.setSelectedNavpoint(navpoint);
            					mapPane.showMap(navpoint.getLocation());
            				}
            				else navpointLayer.setSelectedNavpoint(null);
            			}
            		}
            	}
            });
        navpointScrollPane.setViewportView(navpointTable);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if ((currentMission != null) && (currentMission != mission)) 
			currentMission.removeMissionListener(this);
		if (mission != null) {
			if (mission != currentMission) {
				mission.addMissionListener(this);
				currentMission = mission;
				if (mission.getPeopleNumber() > 0) {
					if (mission instanceof VehicleMission) 
						trailLayer.setSingleVehicle(((VehicleMission) mission).getVehicle());
					if (mission instanceof TravelMission) {
						navpointLayer.setSingleMission((TravelMission) mission);
						navpointLayer.setSelectedNavpoint(null);
						navpointTableModel.updateNavpoints();
					}
					mapPane.showMap(currentMission.getPeople().get(0).getCoordinates());
				}
			}
		}
		else {
			currentMission = null;
			trailLayer.setSingleVehicle(null);
			navpointLayer.setSingleMission(null);
			navpointLayer.setSelectedNavpoint(null);
			navpointTableModel.updateNavpoints();
			mapPane.showMap(null);
		}
	}

	public void missionUpdate(MissionEvent event) {
		String type = event.getType();
		if (TravelMission.NAVPOINTS_EVENT.equals(type)) {
			navpointTableModel.updateNavpoints();
		}
	}
	
	/**
	 * Prepares navpoint panel for deletion.
	 */
	public void destroy() {
		mapPane.destroy();
	}
	
	private class NavpointTableModel extends AbstractTableModel {
		
		private List navpoints;
		
		private NavpointTableModel() {
			navpoints = new ArrayList();
		}
		
		public int getRowCount() {
            return navpoints.size();
        }
		
		public int getColumnCount() {
            return 3;
        }
		
		public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Name";
            else if (columnIndex == 1) return "Location";
            else if (columnIndex == 2) return "Description";
            else return "";
        }
		
		public Object getValueAt(int row, int column) {
            if (row < navpoints.size()) {
            	NavPoint navpoint = (NavPoint) navpoints.get(row);
            	if (column == 0) return "Navpoint " + (row + 1);
            	else if (column == 1) return navpoint.getLocation().getFormattedString();
            	else if (column == 2) return navpoint.getDescription();
            	else return "unknown";
            }   
            else return "unknown";
        }
		
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