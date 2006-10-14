/**
 * Mars Simulation Project
 * MainDetailPanel.java
 * @version 2.80 2006-10-09
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

public class MainDetailPanel extends JPanel implements ListSelectionListener, 
		MissionListener, UnitListener {

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
	private JLabel travelledLabel;
	private MainDesktopPane desktop;
	private DecimalFormat formatter = new DecimalFormat("0.0");
	
	MainDetailPanel(MainDesktopPane desktop) {
		
		this.desktop = desktop;
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 300));
		
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		Box descriptionPane = new CustomBox();
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(descriptionPane);
		
		descriptionLabel = new JLabel("Description:", SwingConstants.LEFT);
		descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(descriptionLabel);
		
		typeLabel = new JLabel("Type:");
		typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(typeLabel);
		
		phaseLabel = new JLabel("Phase:");
		phaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(phaseLabel);
		
		Box memberPane = new CustomBox();
		memberPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(memberPane);
		
		memberNumLabel = new JLabel("Mission Members:   (Min:  - Max: )");
		memberNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberNumLabel);
		
		JPanel memberBottomPane = new JPanel(new BorderLayout(0, 0));
		memberBottomPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberBottomPane);
		
		// Prepare member list panel
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
        memberBottomPane.add(memberListPane, BorderLayout.CENTER);
        
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberListPane.add(memberScrollPane, BorderLayout.CENTER);
        
        memberTableModel = new MemberTableModel();
        memberTable = new JTable(memberTableModel);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        memberTable.setRowSelectionAllowed(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				int index = memberTable.getSelectedRow();
        				Person selectedPerson = memberTableModel.getMemberAtIndex(index);
        				if (selectedPerson != null) getDesktop().openUnitWindow(selectedPerson);
        			}
        		}
        	});
        memberScrollPane.setViewportView(memberTable);
		
		Box travelPane = new CustomBox();
		travelPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(travelPane);
		
		JPanel vehiclePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		vehiclePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehiclePane);
		
		JLabel vehicleLabel = new JLabel("Vehicle: ");
		vehiclePane.add(vehicleLabel);
		
		vehicleButton = new JButton("   ");
		vehiclePane.add(vehicleButton);
		vehicleButton.setVisible(false);
		vehicleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentMission instanceof VehicleMission) {
					VehicleMission vehicleMission = (VehicleMission) currentMission;
					Vehicle vehicle = vehicleMission.getVehicle();
					if (vehicle != null) getDesktop().openUnitWindow(vehicle);
				}
			}
		});
		
		vehicleStatusLabel = new JLabel("Vehicle Status:");
		vehicleStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehicleStatusLabel);
		
		speedLabel = new JLabel("Vehicle Speed:");
		speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(speedLabel);
		
		travelledLabel = new JLabel("Travelled Distance:");
		travelledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(travelledLabel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (currentMission != null) currentMission.removeListener(this);
		if (currentVehicle != null) currentVehicle.removeUnitListener(this);
		
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			descriptionLabel.setText("Description: " + mission.getDescription());
			typeLabel.setText("Type: " + mission.getName());
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
			int memberNum = mission.getPeopleNumber();
			int minMembers = mission.getMinPeople();
			int maxMembers = mission.getMissionCapacity();
			memberNumLabel.setText("Mission Members: " + memberNum + " (Min: " + minMembers + 
					" - Max: " + maxMembers + ")");
			memberTableModel.setMission(mission);
			
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
					int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
					int totalDistance = (int) vehicleMission.getTotalDistance();
					travelledLabel.setText("Travelled Distance: " + travelledDistance + 
							" km of " + totalDistance + " km");
					vehicle.addUnitListener(this);
					currentVehicle = vehicle;
				}
			}
			if (!isVehicle) {
				vehicleButton.setVisible(false);
				vehicleStatusLabel.setText("Vehicle Status:");
				speedLabel.setText("Vehicle Speed:");
				travelledLabel.setText("Travelled Distance:");
				currentVehicle = null;
			}
			
			mission.addListener(this);
			currentMission = mission;
		}
		else {
			descriptionLabel.setText("Description:");
			typeLabel.setText("Type:");
			phaseLabel.setText("Phase:");
			memberNumLabel.setText("Mission Members:   (Min:  - Max: )");
			memberTableModel.setMission(null);
			vehicleButton.setVisible(false);
			vehicleStatusLabel.setText("Vehicle Status:");
			speedLabel.setText("Vehicle Speed:");
			travelledLabel.setText("Travelled Distance:");
			currentMission = null;
			currentVehicle = null;
		}
	}
	
	public void missionUpdate(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		String type = e.getType();
		if (type.equals(Mission.NAME_EVENT)) 
			typeLabel.setText("Type: " + mission.getName());
		else if (type.equals(Mission.DESCRIPTION_EVENT)) 
			descriptionLabel.setText("Description: " + mission.getDescription());
		else if (type.equals(Mission.PHASE_DESCRIPTION_EVENT))
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
		else if (type.equals(Mission.PEOPLE_EVENT) || type.equals(Mission.MIN_PEOPLE_EVENT) || 
				type.equals(Mission.CAPACITY_EVENT)) {
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
    
    private class CustomBox extends Box {
    	
    	private CustomBox() {
    		super(BoxLayout.Y_AXIS);
    		setBorder(new MarsPanelBorder());
    	}
    	
    	public Dimension getMaximumSize() {
    		Dimension result = getPreferredSize();
    		result.width = Short.MAX_VALUE;
    		return result;
    	}
    }
    
    private class MemberTableModel extends AbstractTableModel implements UnitListener {
    	
    	Mission mission;
    	PersonCollection members;
    	
    	private MemberTableModel() {
    		mission = null;
    		members = new PersonCollection();
    	}
    	
    	public int getRowCount() {
            return members.size();
        }
    	
    	public int getColumnCount() {
            return 2;
        }
    	
    	public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Name";
            else if (columnIndex == 1) return "Task";
            else return "unknown";
        }
    	
    	public Object getValueAt(int row, int column) {
            if (row < members.size()) {
            	Person person = (Person) members.get(row);
            	if (column == 0) return person.getName();
            	else return person.getMind().getTaskManager().getTaskDescription();
            }   
            else return "unknown";
        }
    	
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
    	
    	private void clearMembers() {
    		if (members != null) {
    			PersonIterator i = members.iterator();
    			while (i.hasNext()) i.next().removeUnitListener(this);
    			members.clear();
    		}
    	}
    	
    	Person getMemberAtIndex(int index) {
    		if (index < members.size()) return (Person) members.get(index);
    		else return null;
    	}
    }
}