package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.person.ai.task.TaskManager;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class MainDetailPanel extends JPanel implements ListSelectionListener, MissionListener {

	private Mission currentMission;
	private JLabel descriptionLabel;
	private JLabel typeLabel;
	private JLabel phaseLabel;
	private JLabel minNumberLabel;
	private JLabel currentMemberNumLabel;
	private JLabel crewCapacityLabel;
	private MemberTableModel memberTableModel;
	private JButton vehicleButton;
	private MainDesktopPane desktop;
	
	public MainDetailPanel(MainDesktopPane desktop) {
		
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
		
		currentMemberNumLabel = new JLabel("Current Members:");
		currentMemberNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(currentMemberNumLabel);
		
		minNumberLabel = new JLabel("Minimum Members:");
		minNumberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(minNumberLabel);
		
		crewCapacityLabel = new JLabel("Maximum Members:");
		crewCapacityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(crewCapacityLabel);
		
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
        JTable memberTable = new JTable(memberTableModel);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        memberTable.setCellSelectionEnabled(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
		
		
	}
	
	public void valueChanged(ListSelectionEvent e) {
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			if (currentMission != null) currentMission.removeListener(this);
	
			descriptionLabel.setText("Description: " + mission.getDescription());
			typeLabel.setText("Type: " + mission.getName());
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
			currentMemberNumLabel.setText("Current Members: " + mission.getPeopleNumber());
			minNumberLabel.setText("Minimum Members: " + mission.getMinPeople());
			crewCapacityLabel.setText("Maximum Members: " + mission.getMissionCapacity());
			memberTableModel.setMission(mission);
			
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				Vehicle vehicle = vehicleMission.getVehicle();
				if (vehicle != null) {
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
				}
				else vehicleButton.setVisible(false);
			}
			else vehicleButton.setVisible(false);
			
			mission.addListener(this);
			currentMission = mission;
		}
		else {
			descriptionLabel.setText("Description:");
			typeLabel.setText("Type:");
			phaseLabel.setText("Phase:");
			currentMemberNumLabel.setText("Current Members:");
			minNumberLabel.setText("Minimum Members:");
			crewCapacityLabel.setText("Maximum Members:");
			memberTableModel.setMission(null);
			vehicleButton.setVisible(false);
		}
	}
	
	public void missionUpdate(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		if (e.getType().equals(Mission.NAME_EVENT)) 
			typeLabel.setText("Type: " + mission.getName());
		else if (e.getType().equals(Mission.DESCRIPTION_EVENT)) 
			descriptionLabel.setText("Description: " + mission.getDescription());
		else if (e.getType().equals(Mission.PHASE_DESCRIPTION_EVENT))
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
		else if (e.getType().equals(Mission.PEOPLE_EVENT)) {
			currentMemberNumLabel.setText("Current Members: " + mission.getPeopleNumber());
			memberTableModel.updateMembers();
		}
		else if (e.getType().equals(Mission.MIN_PEOPLE_EVENT))
			minNumberLabel.setText("Minimum Members: " + mission.getMinPeople());
		else if (e.getType().equals(Mission.CAPACITY_EVENT))
			crewCapacityLabel.setText("Maximum Members: " + mission.getMissionCapacity());
		else if (e.getType().equals(VehicleMission.VEHICLE_EVENT)) {
			Vehicle vehicle = ((VehicleMission) mission).getVehicle();
			if (vehicle != null) {
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
			}
			else vehicleButton.setVisible(false);
		}
	}
	
    /**
     * Gets the main desktop.
     * @return desktop.
     */
    public MainDesktopPane getDesktop() {
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
    			while (i.hasNext()) i.next().addListener(this);
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
    			while (i.hasNext()) i.next().removeListener(this);
    			members.clear();
    		}
    	}
    }
}