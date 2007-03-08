package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonCollection;
import org.mars_sim.msp.simulation.person.PersonIterator;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Crewable;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

class MembersPanel extends WizardPanel {

	private final static String NAME = "Members";
	
	// Data members.
	private PeopleTableModel peopleTableModel;
	private JTable peopleTable;
	private MembersTableModel membersTableModel;
	private JTable membersTable;
	private JLabel errorMessageLabel;
	private JButton addButton;
	private JButton removeButton;
	private JLabel roverCapacityLabel;
	
	MembersPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel selectMembersLabel = new JLabel("Select members for the mission.", JLabel.CENTER);
		selectMembersLabel.setFont(selectMembersLabel.getFont().deriveFont(Font.BOLD));
		selectMembersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectMembersLabel);
		
		JLabel availablePeopleLabel = new JLabel("Available People", JLabel.CENTER);
		availablePeopleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(availablePeopleLabel);
		
		JPanel peoplePane = new JPanel(new BorderLayout(0, 0));
		peoplePane.setPreferredSize(new Dimension(300, 150));
		peoplePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(peoplePane);
		
        // Create scroll panel for available people list.
        JScrollPane peopleScrollPane = new JScrollPane();
        peoplePane.add(peopleScrollPane, BorderLayout.CENTER);
        
        peopleTableModel = new PeopleTableModel();
        peopleTable = new JTable(peopleTableModel);
        peopleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(peopleTableModel));
        peopleTable.setRowSelectionAllowed(true);
        peopleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        peopleTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			int[] selectedRows = peopleTable.getSelectedRows();
    				if (selectedRows.length > 0) {
    					if (e.getValueIsAdjusting()) {
        					membersTable.clearSelection();
        					boolean failedRow = false;
        					for (int x = 0; x < selectedRows.length; x++) 
        						if (peopleTableModel.isFailureRow(selectedRows[x])) failedRow = true;
        				
        					if (failedRow) {
        						errorMessageLabel.setText("One or more selected people cannot be used on the mission (see red cells).");
        						addButton.setEnabled(false);
        					}
        					else {
        						if (selectedRows.length > getRemainingRoverCapacity()) {
        							errorMessageLabel.setText("Not enough rover capacity to hold selected people.");
            						addButton.setEnabled(false);
        						}
        						else {
        							errorMessageLabel.setText(" ");
        							addButton.setEnabled(true);
        						}
        					}
        				}
    				}
        			else {
        				addButton.setEnabled(false);
        				errorMessageLabel.setText(" ");
        			}
        		}
        	});
        peopleScrollPane.setViewportView(peopleTable);
		
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(errorMessageLabel);
		
		add(Box.createVerticalStrut(10));
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(buttonPanel);
		
		addButton = new JButton("Add Members");
		addButton.setEnabled(false);
		addButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] selectedRows = peopleTable.getSelectedRows();
					PersonCollection people = new PersonCollection();
					for (int x = 0; x < selectedRows.length; x++) 
						people.add(peopleTableModel.getUnit(selectedRows[x]));
					peopleTableModel.removePeople(people);
					membersTableModel.addPeople(people);
					updateRoverCapacityLabel();
				}
			});
		buttonPanel.add(addButton);
		
		removeButton = new JButton("Remove Members");
		removeButton.setEnabled(false);
		removeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int[] selectedRows = membersTable.getSelectedRows();
						PersonCollection people = new PersonCollection();
						for (int x = 0; x < selectedRows.length; x++) 
							people.add(membersTableModel.getUnit(selectedRows[x]));
						peopleTableModel.addPeople(people);
						membersTableModel.removePeople(people);
						updateRoverCapacityLabel();
					}
				});
		buttonPanel.add(removeButton);
		
		add(Box.createVerticalStrut(10));
		
		roverCapacityLabel = new JLabel("Remaining rover capacity: ");
		roverCapacityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(roverCapacityLabel);
		
		add(Box.createVerticalStrut(10));
		
		JLabel membersLabel = new JLabel("Mission Members");
		membersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(membersLabel);
		
		JPanel membersPane = new JPanel(new BorderLayout(0, 0));
		membersPane.setPreferredSize(new Dimension(300, 150));
		membersPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(membersPane);
		
        // Create scroll panel for members list.
        JScrollPane membersScrollPane = new JScrollPane();
        membersPane.add(membersScrollPane, BorderLayout.CENTER);
        
        membersTableModel = new MembersTableModel();
        membersTable = new JTable(membersTableModel);
        membersTable.setRowSelectionAllowed(true);
        membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        membersTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
    				int[] selectedRows = membersTable.getSelectedRows();
    				if (selectedRows.length > 0) {
    					if (e.getValueIsAdjusting()) {
        					peopleTable.clearSelection();
        					removeButton.setEnabled(true);
        				}
    				}
        			else removeButton.setEnabled(false);
        		}
        	});
        membersScrollPane.setViewportView(membersTable);
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		PersonCollection people = new PersonCollection();
		for (int x = 0; x < membersTableModel.getRowCount(); x++) 
			people.add(membersTableModel.getUnit(x));
		getWizard().getMissionData().setMembers(people);
	}

	void clearInfo() {
		peopleTable.clearSelection();
		membersTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	void updatePanel() {
		peopleTableModel.updateTable();
		membersTableModel.updateTable();
		updateRoverCapacityLabel();
	}
	
	void updateRoverCapacityLabel() {
		roverCapacityLabel.setText("Remaining rover capacity: " + getRemainingRoverCapacity());
	}
	
	int getRemainingRoverCapacity() {
		int roverCapacity = ((Crewable) getWizard().getMissionData().getRover()).getCrewCapacity();
		int memberNum = membersTableModel.getRowCount();
		return roverCapacity - memberNum;
	}
	
    private class PeopleTableModel extends UnitTableModel {
    	
    	private PeopleTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		columns.add("Name");
    		columns.add("Job");
    		columns.add("Current Mission");
    		columns.add("Performance");
    		columns.add("Health");
    	}
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Person person = (Person) getUnit(row);
            	
            	try {
            		if (column == 0) 
            			result = person.getName();
            		else if (column == 1) 
            			result = person.getMind().getJob().getName();
            		else if (column == 2) {
            			Mission mission = person.getMind().getMission();
            			if (mission != null) result = mission.getName();
            			else result = "none";
            		}
            		else if (column == 3) 
            			result = (int) (person.getPerformanceRating() * 100D) + "%";
            		else if (column == 4)
            			result = person.getPhysicalCondition().getHealthSituation();
            	}
            	catch (Exception e) {}
            }
            
            return result;
        }
    	
    	void updateTable() {
    		units.clear();
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    		PersonCollection people = startingSettlement.getInhabitants().sortByName();
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) units.add(i.next());
    		fireTableDataChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		boolean result = false;
    		
    		if (row < units.size()) {
    			Person person = (Person) getUnit(row);
    			
    			if (column == 2) {
    				if (person.getMind().getMission() != null) return true;
    			}
    		}
    		
    		return result;
    	}
    	
    	void addPeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (!units.contains(person)) units.add(person);
    		}
    		units = units.sortByName();
    		fireTableDataChanged();
    	}
    	
    	void removePeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (units.contains(person)) units.remove(person);
    		}
    		fireTableDataChanged();
    	}
    }
    
    private class MembersTableModel extends UnitTableModel {
    	
    	private MembersTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		columns.add("Name");
    		columns.add("Job");
    		columns.add("Current Mission");
    		columns.add("Performance");
    		columns.add("Health");
    	}
    	
    	public Object getValueAt(int row, int column) {
    		Object result = "unknown";
    		
            if (row < units.size()) {
            	Person person = (Person) getUnit(row);
            	
            	try {
            		if (column == 0) 
            			result = person.getName();
            		else if (column == 1) 
            			result = person.getMind().getJob().getName();
            		else if (column == 2) {
            			Mission mission = person.getMind().getMission();
            			if (mission != null) result = mission.getName();
            			else result = "none";
            		}
            		else if (column == 3) 
            			result = (int) (person.getPerformanceRating() * 100D) + "%";
            		else if (column == 4)
            			result = person.getPhysicalCondition().getHealthSituation();
            	}
            	catch (Exception e) {}
            }
            
            return result;
        }
    	
    	void updateTable() {
    		units.clear();
    		fireTableDataChanged();
    	}
    	
    	boolean isFailureCell(int row, int column) {
    		return false;
    	}
    	
    	void addPeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (!units.contains(person)) units.add(person);
    		}
    		units = units.sortByName();
    		fireTableDataChanged();
    		
    		if (units.size() > 0) getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, true);
    		else getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, false);
    	}
    	
    	void removePeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (units.contains(person)) units.remove(person);
    		}
    		fireTableDataChanged();
    		
    		if (units.size() > 0) getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, true);
    		else getWizard().setButtonEnabled(CreateMissionWizard.NEXT_BUTTON, false);
    	}
    }
}