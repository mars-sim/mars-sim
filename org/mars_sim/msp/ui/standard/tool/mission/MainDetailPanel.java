/**
 * Mars Simulation Project
 * MainDetailPanel.java
 * @version 2.80 2007-03-19
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.UnitEvent;
import org.mars_sim.msp.simulation.UnitListener;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.person.ai.task.TaskManager;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * The tab panel for showing mission details.
 */
public class MainDetailPanel extends JPanel implements ListSelectionListener, 
		MissionListener, UnitListener {

	// Private members
	private Mission currentMission;
	private Vehicle currentVehicle;
	private JLabel descriptionLabel;
	private JLabel typeLabel;
	private JLabel phaseLabel;
	private JLabel memberNumLabel;
	private MemberTableModel memberTableModel;
	private JTable memberTable;
	private JButton vehicleButton;
	private JLabel vehicleStatusLabel;
	private JLabel speedLabel;
	private JLabel distanceNextNavLabel;
	private JLabel travelledLabel;
	private MainDesktopPane desktop;
	private DecimalFormat formatter = new DecimalFormat("0.0");
	
	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	MainDetailPanel(MainDesktopPane desktop) {
		// User JPanel constructor.
		super();
		
		// Initialize data members.
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout());
		
		// Create the main panel.
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		// Create the description panel.
		Box descriptionPane = new CustomBox();
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(descriptionPane);
		
		// Create the description label.
		descriptionLabel = new JLabel("Description:", SwingConstants.LEFT);
		descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(descriptionLabel);
		
		// Create the type label.
		typeLabel = new JLabel("Type:");
		typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(typeLabel);
		
		// Create the phase label.
		phaseLabel = new JLabel("Phase:");
		phaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(phaseLabel);
		
		// Create the member panel.
		Box memberPane = new CustomBox();
		memberPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(memberPane);
		
		// Create the member number label.
		memberNumLabel = new JLabel("Mission Members:   (Min:  - Max: )");
		memberNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberNumLabel);
		
		// Create member bottom panel.
		JPanel memberBottomPane = new JPanel(new BorderLayout(0, 0));
		memberBottomPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberBottomPane);
		
		// Prepare member list panel
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
		memberListPane.setPreferredSize(new Dimension(100, 100));
        memberBottomPane.add(memberListPane, BorderLayout.CENTER);
        
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberListPane.add(memberScrollPane, BorderLayout.CENTER);
        
        // Create member table model.
        memberTableModel = new MemberTableModel();
        
        // Create member table.
        memberTable = new JTable(memberTableModel);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        memberTable.setRowSelectionAllowed(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				// Open window for selected person.
        				int index = memberTable.getSelectedRow();
        				Person selectedPerson = memberTableModel.getMemberAtIndex(index);
        				if (selectedPerson != null) getDesktop().openUnitWindow(selectedPerson, false);
        			}
        		}
        	});
        memberScrollPane.setViewportView(memberTable);
		
        // Create the travel panel.
		Box travelPane = new CustomBox();
		travelPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(travelPane);
		
		// Create the vehicle panel.
		JPanel vehiclePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		vehiclePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehiclePane);
		
		// Create the vehicle label.
		JLabel vehicleLabel = new JLabel("Vehicle: ");
		vehiclePane.add(vehicleLabel);
		
		// Create the vehicle panel.
		vehicleButton = new JButton("   ");
		vehiclePane.add(vehicleButton);
		vehicleButton.setVisible(false);
		vehicleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentMission instanceof VehicleMission) {
					// Open window for vehicle.
					VehicleMission vehicleMission = (VehicleMission) currentMission;
					Vehicle vehicle = vehicleMission.getVehicle();
					if (vehicle != null) getDesktop().openUnitWindow(vehicle, false);
				}
			}
		});
		
		// Create the vehicle status label.
		vehicleStatusLabel = new JLabel("Vehicle Status:");
		vehicleStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehicleStatusLabel);
		
		// Create the speed label.
		speedLabel = new JLabel("Vehicle Speed:");
		speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(speedLabel);
		
		// Create the distance next navpoint label.
		distanceNextNavLabel = new JLabel("Distance to Next Navpoint:");
		distanceNextNavLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(distanceNextNavLabel);
		
		// Create the travelled distance label.
		travelledLabel = new JLabel("Travelled Distance:");
		travelledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(travelledLabel);
	}
	
	/**
	 * Implemented from ListSelectionListener.
	 * Note: this is called when a mission is selected on MissionWindow's mission list.
	 */
	public void valueChanged(ListSelectionEvent e) {
		// Remove mission and unit listeners.
		if (currentMission != null) currentMission.removeMissionListener(this);
		if (currentVehicle != null) currentVehicle.removeUnitListener(this);
		
		// Get the selected mission.
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		
		if (mission != null) {
			// Update mission info in UI.
			descriptionLabel.setText("Description: " + mission.getDescription());
			typeLabel.setText("Type: " + mission.getName());
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
			int memberNum = mission.getPeopleNumber();
			int minMembers = mission.getMinPeople();
			int maxMembers = mission.getMissionCapacity();
			memberNumLabel.setText("Mission Members: " + memberNum + " (Min: " + minMembers + 
					" - Max: " + maxMembers + ")");
			memberTableModel.setMission(mission);
		
			// Update mission vehicle info in UI.
			boolean isVehicle = false;
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				Vehicle vehicle = vehicleMission.getVehicle();
				if (vehicle != null) {
					isVehicle = true;
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
					vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
					speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
					try {
						int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
						distanceNextNavLabel.setText("Distance to Next Navpoint: " + distanceNextNav + " km");
					}
					catch (Exception e2) {}
					int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
					int totalDistance = (int) vehicleMission.getTotalDistance();
					travelledLabel.setText("Travelled Distance: " + travelledDistance + 
							" km of " + totalDistance + " km");
					vehicle.addUnitListener(this);
					currentVehicle = vehicle;
				}
			}
			if (!isVehicle) {
				// Clear vehicle info.
				vehicleButton.setVisible(false);
				vehicleStatusLabel.setText("Vehicle Status:");
				speedLabel.setText("Vehicle Speed:");
				distanceNextNavLabel.setText("Distance to Next Navpoint:");
				travelledLabel.setText("Travelled Distance:");
				currentVehicle = null;
			}
			
			// Add mission listener.
			mission.addMissionListener(this);
			currentMission = mission;
		}
		else {
			// Clear mission info in UI.
			descriptionLabel.setText("Description:");
			typeLabel.setText("Type:");
			phaseLabel.setText("Phase:");
			memberNumLabel.setText("Mission Members:   (Min:  - Max: )");
			memberTableModel.setMission(null);
			vehicleButton.setVisible(false);
			vehicleStatusLabel.setText("Vehicle Status:");
			speedLabel.setText("Vehicle Speed:");
			distanceNextNavLabel.setText("Distance to Next Navpoint:");
			travelledLabel.setText("Travelled Distance:");
			currentMission = null;
			currentVehicle = null;
		}
	}
	
	/**
	 * Mission event update.
	 */
	public void missionUpdate(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		String type = e.getType();
		
		// Update UI based on mission event type.
		if (type.equals(Mission.NAME_EVENT)) 
			typeLabel.setText("Type: " + mission.getName());
		else if (type.equals(Mission.DESCRIPTION_EVENT)) 
			descriptionLabel.setText("Description: " + mission.getDescription());
		else if (type.equals(Mission.PHASE_DESCRIPTION_EVENT))
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
		else if (type.equals(Mission.ADD_MEMBER_EVENT) || type.equals(Mission.REMOVE_MEMBER_EVENT) || 
				type.equals(Mission.MIN_PEOPLE_EVENT) || type.equals(Mission.CAPACITY_EVENT)) {
			int memberNum = mission.getPeopleNumber();
			int minMembers = mission.getMinPeople();
			int maxMembers = mission.getMissionCapacity();
			memberNumLabel.setText("Mission Members: " + memberNum + " (Min: " + minMembers + 
					" - Max: " + maxMembers + ")");
			memberTableModel.updateMembers();
		}
		else if (type.equals(VehicleMission.VEHICLE_EVENT)) {
			Vehicle vehicle = ((VehicleMission) mission).getVehicle();
			if (vehicle != null) {
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
				vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
				speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
				vehicle.addUnitListener(this);
				currentVehicle = vehicle;
			}
			else {
				vehicleButton.setVisible(false);
				vehicleStatusLabel.setText("Vehicle Status:");
				speedLabel.setText("Vehicle Speed:");
				if (currentVehicle != null) currentVehicle.removeUnitListener(this);
				currentVehicle = null;
			}
		}
		else if (type.equals(TravelMission.DISTANCE_EVENT)) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			try {
				int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
				distanceNextNavLabel.setText("Distance to Next Navpoint: " + distanceNextNav + " km");
			}
			catch (Exception e2) {}
			int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
			int totalDistance = (int) vehicleMission.getTotalDistance();
			travelledLabel.setText("Travelled Distance: " + travelledDistance + 
					" km of " + totalDistance + " km");
		}
	}
	
	/**
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		// Update vehicle info in UI based on event type.
		String type = event.getType();
		Vehicle vehicle = (Vehicle) event.getSource();
		if (type.equals(Vehicle.STATUS_EVENT)) 
			vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
		else if (type.equals(Vehicle.SPEED_EVENT)) 
			speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
	}
	
    /**
     * Gets the main desktop.
     * @return desktop.
     */
    private MainDesktopPane getDesktop() {
    	return desktop;
    }
    
    /**
     * A custom box container inner class.
     */
    private class CustomBox extends Box {
    	
    	/**
    	 * Constructor
    	 */
    	private CustomBox() {
    		super(BoxLayout.Y_AXIS);
    		setBorder(new MarsPanelBorder());
    	}
    	
    	/**
    	 * Gets the maximum size for the component.
    	 * @return dimension.
    	 */
    	public Dimension getMaximumSize() {
    		Dimension result = getPreferredSize();
    		result.width = Short.MAX_VALUE;
    		return result;
    	}
    }
    
    /**
     * Table model for mission members.
     */
    private class MemberTableModel extends AbstractTableModel implements UnitListener {
    	
    	// Private members.
    	Mission mission;
    	PersonCollection members;
    	
    	/**
    	 * Constructor
    	 */
    	private MemberTableModel() {
    		mission = null;
    		members = new PersonCollection();
    	}
    	
    	/**
    	 * Gets the row count.
    	 * @return row count.
    	 */
    	public int getRowCount() {
            return members.size();
        }
    	
    	/**
    	 * Gets the column count.
    	 * @return column count.
    	 */
    	public int getColumnCount() {
            return 2;
        }
    	
    	/**
    	 * Gets the column name at a given index.
    	 * @param columnIndex the column's index.
    	 * @return the column name.
    	 */
    	public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Name";
            else if (columnIndex == 1) return "Task";
            else return "unknown";
        }
    	
    	/**
    	 * Gets the value at a given row and column.
    	 * @param row the table row.
    	 * @param column the table column.
    	 * @return the value.
    	 */
    	public Object getValueAt(int row, int column) {
            if (row < members.size()) {
            	Person person = (Person) members.get(row);
            	if (column == 0) return person.getName();
            	else return person.getMind().getTaskManager().getTaskDescription();
            }   
            else return "unknown";
        }
    	
    	/**
    	 * Sets the mission for this table model.
    	 * @param newMission the new mission.
    	 */
    	void setMission(Mission newMission) {
    		this.mission = newMission;
    		updateMembers();
    	}
    	
    	/**
    	 * Catch unit update event.
    	 * @param event the unit event.
    	 */
    	public void unitUpdate(UnitEvent event) {
    		String type = event.getType();
    		Person person = (Person) event.getSource();
    		int index = members.indexOf(person);
    		if (type.equals(Unit.NAME_EVENT)) fireTableCellUpdated(index, 0);
    		else if (type.equals(Task.TASK_DESC_EVENT) || type.equals(TaskManager.TASK_EVENT)) 
    			fireTableCellUpdated(index, 1);
    	}
    	
    	/**
    	 * Update mission members.
    	 */
    	void updateMembers() {
    		if (mission != null) {
    			clearMembers();
    			members = mission.getPeople();
    			PersonIterator i = members.iterator();
    			while (i.hasNext()) i.next().addUnitListener(this);
    			fireTableDataChanged();
    		}
    		else {
    			if (members.size() > 0) {
    				clearMembers();
    				fireTableDataChanged();
    			}
    		}
    	}
    	
    	/**
    	 * Clear all members from the table.
    	 */
    	private void clearMembers() {
    		if (members != null) {
    			PersonIterator i = members.iterator();
    			while (i.hasNext()) i.next().removeUnitListener(this);
    			members.clear();
    		}
    	}
    	
    	/**
    	 * Gets the mission member at a given index.
    	 * @param index the index.
    	 * @return the mission member.
    	 */
    	Person getMemberAtIndex(int index) {
    		if (index < members.size()) return (Person) members.get(index);
    		else return null;
    	}
    }
}