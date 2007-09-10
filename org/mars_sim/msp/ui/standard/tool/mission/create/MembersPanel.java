/**
 * Mars Simulation Project
 * MembersPanel.java
 * @version 2.80 2007-03-22
 * @author Scott Davis
 */

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

/**
 * A wizard panel to select mission members.
 */
class MembersPanel extends WizardPanel {

	// The wizard panel name.
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
	
	/**
	 * Constructor
	 * @param wizard the create mission wizard.
	 */
	MembersPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor
		super(wizard);
		
		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the select members label.
		JLabel selectMembersLabel = new JLabel("Select members for the mission.", JLabel.CENTER);
		selectMembersLabel.setFont(selectMembersLabel.getFont().deriveFont(Font.BOLD));
		selectMembersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(selectMembersLabel);
		
		// Create the available people label.
		JLabel availablePeopleLabel = new JLabel("Available People", JLabel.CENTER);
		availablePeopleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(availablePeopleLabel);
		
		// Create the people panel.
		JPanel peoplePane = new JPanel(new BorderLayout(0, 0));
		peoplePane.setPreferredSize(new Dimension(300, 150));
		peoplePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(peoplePane);
		
        // Create scroll panel for available people list.
        JScrollPane peopleScrollPane = new JScrollPane();
        peoplePane.add(peopleScrollPane, BorderLayout.CENTER);
        
        // Create the people table model.
        peopleTableModel = new PeopleTableModel();
        
        // Create the people table.
        peopleTable = new JTable(peopleTableModel);
        peopleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(peopleTableModel));
        peopleTable.setRowSelectionAllowed(true);
        peopleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        peopleTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			// Get the selected rows.
        			int[] selectedRows = peopleTable.getSelectedRows();
    				if (selectedRows.length > 0) {
    					if (e.getValueIsAdjusting()) {
        					membersTable.clearSelection();
        					
        					// Check if any of the rows failed.
        					boolean failedRow = false;
        					for (int x = 0; x < selectedRows.length; x++) 
        						if (peopleTableModel.isFailureRow(selectedRows[x])) failedRow = true;
        				
        					if (failedRow) {
        						// Display failed row message and disable add button.
        						errorMessageLabel.setText("One or more selected people cannot be used on the mission (see red cells).");
        						addButton.setEnabled(false);
        					}
        					else {
        						// Check if number of rows exceed rover remaining capacity.
        						if (selectedRows.length > getRemainingRoverCapacity()) {
        							// Display over capacity message and disable add button.
        							errorMessageLabel.setText("Not enough rover capacity to hold selected people.");
            						addButton.setEnabled(false);
        						}
        						else {
        							// Enable add button.
        							errorMessageLabel.setText(" ");
        							addButton.setEnabled(true);
        						}
        					}
        				}
    				}
        			else {
        				// Disable add button when no rows are selected.
        				addButton.setEnabled(false);
        				errorMessageLabel.setText(" ");
        			}
        		}
        	});
        peopleScrollPane.setViewportView(peopleTable);
		
        // Create the message label.
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(errorMessageLabel);
		
		// Add vertical strut to make some UI space.
		add(Box.createVerticalStrut(10));
		
		// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
		buttonPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(buttonPane);
		
		// Create the add button.
		addButton = new JButton("Add Members");
		addButton.setEnabled(false);
		addButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Add the selected rows in the people table to the members table.
					int[] selectedRows = peopleTable.getSelectedRows();
					PersonCollection people = new PersonCollection();
					for (int x = 0; x < selectedRows.length; x++) 
						people.add(peopleTableModel.getUnit(selectedRows[x]));
					peopleTableModel.removePeople(people);
					membersTableModel.addPeople(people);
					updateRoverCapacityLabel();
				}
			});
		buttonPane.add(addButton);
		
		// Create the remove button.
		removeButton = new JButton("Remove Members");
		removeButton.setEnabled(false);
		removeButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// Remove the selected rows in the members table to the people table.
						int[] selectedRows = membersTable.getSelectedRows();
						PersonCollection people = new PersonCollection();
						for (int x = 0; x < selectedRows.length; x++) 
							people.add(membersTableModel.getUnit(selectedRows[x]));
						peopleTableModel.addPeople(people);
						membersTableModel.removePeople(people);
						updateRoverCapacityLabel();
					}
				});
		buttonPane.add(removeButton);
		
		// Add a vertical strut to make UI space.
		add(Box.createVerticalStrut(10));
		
		// Create the rover capacity label.
		roverCapacityLabel = new JLabel("Remaining rover capacity: ");
		roverCapacityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(roverCapacityLabel);
		
		// Add a vertical strut to make UI space.
		add(Box.createVerticalStrut(10));
		
		// Create the members label.
		JLabel membersLabel = new JLabel("Mission Members");
		membersLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(membersLabel);
		
		// Create the members panel.
		JPanel membersPane = new JPanel(new BorderLayout(0, 0));
		membersPane.setPreferredSize(new Dimension(300, 150));
		membersPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(membersPane);
		
        // Create scroll panel for members list.
        JScrollPane membersScrollPane = new JScrollPane();
        membersPane.add(membersScrollPane, BorderLayout.CENTER);
        
        // Create the members table model.
        membersTableModel = new MembersTableModel();
        
        // Create the members table.
        membersTable = new JTable(membersTableModel);
        membersTable.setRowSelectionAllowed(true);
        membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        membersTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
    				int[] selectedRows = membersTable.getSelectedRows();
    				if (selectedRows.length > 0) {
    					if (e.getValueIsAdjusting()) {
    						// Enable the remove button.
        					peopleTable.clearSelection();
        					removeButton.setEnabled(true);
        				}
    				}
        			else removeButton.setEnabled(false);
        		}
        	});
        membersScrollPane.setViewportView(membersTable);
	}
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}

	/**
	 * Commits changes from this wizard panel.
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		PersonCollection people = new PersonCollection();
		for (int x = 0; x < membersTableModel.getRowCount(); x++) 
			people.add(membersTableModel.getUnit(x));
		getWizard().getMissionData().setMembers(people);
		return true;
	}

	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		peopleTable.clearSelection();
		membersTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		peopleTableModel.updateTable();
		membersTableModel.updateTable();
		updateRoverCapacityLabel();
	}
	
	/**
	 * Updates the rover capacity label.
	 */
	void updateRoverCapacityLabel() {
		roverCapacityLabel.setText("Remaining rover capacity: " + getRemainingRoverCapacity());
	}
	
	/**
	 * Gets the remaining rover capacity.
	 * @return rover capacity.
	 */
	int getRemainingRoverCapacity() {
		int roverCapacity = ((Crewable) getWizard().getMissionData().getRover()).getCrewCapacity();
		int memberNum = membersTableModel.getRowCount();
		return roverCapacity - memberNum;
	}
	
	/**
	 * Table model for people.
	 */
    private class PeopleTableModel extends UnitTableModel {
    	
    	/**
    	 * Constructor
    	 */
    	private PeopleTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		// Add table columns.
    		columns.add("Name");
    		columns.add("Job");
    		columns.add("Current Mission");
    		columns.add("Performance");
    		columns.add("Health");
    	}
    	
    	/**
    	 * Returns the value for the cell at columnIndex and rowIndex.
    	 * @param row the row whose value is to be queried.
    	 * @param column the column whose value is to be queried.
    	 * @return the value Object at the specified cell.
    	 */
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
    	
    	/**
    	 * Updates the table data.
    	 */
    	void updateTable() {
    		units.clear();
    		Settlement startingSettlement = getWizard().getMissionData().getStartingSettlement();
    		PersonCollection people = startingSettlement.getInhabitants().sortByName();
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) units.add(i.next());
    		fireTableDataChanged();
    	}
    	
    	/**
    	 * Checks if a table cell is a failure cell.
    	 * @param row the table row.
    	 * @param column the table column.
    	 * @return true if cell is a failure cell.
    	 */
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
    	
    	/**
    	 * Adds people to the table.
    	 * @param people the collection of people to add.
    	 */
    	void addPeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (!units.contains(person)) units.add(person);
    		}
    		units = units.sortByName();
    		fireTableDataChanged();
    	}
    	
    	/**
    	 * Removes people from the table.
    	 * @param people the collection of people to remove.
    	 */
    	void removePeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (units.contains(person)) units.remove(person);
    		}
    		fireTableDataChanged();
    	}
    }
    
    /**
     * A table model for mission members.
     */
    private class MembersTableModel extends UnitTableModel {
    	
    	/**
    	 * Constructor.
    	 */
    	private MembersTableModel() {
    		// Use UnitTableModel constructor.
    		super();
    		
    		// Add columns.
    		columns.add("Name");
    		columns.add("Job");
    		columns.add("Current Mission");
    		columns.add("Performance");
    		columns.add("Health");
    	}
    	
    	/**
    	 * Returns the value for the cell at columnIndex and rowIndex.
    	 * @param row the row whose value is to be queried
    	 * @param column the column whose value is to be queried
    	 * @return the value Object at the specified cell
    	 */
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
    	
    	/**
    	 * Updates the table data.
    	 */
    	void updateTable() {
    		units.clear();
    		fireTableDataChanged();
    	}
    	
    	/**
    	 * Checks if a table cell is a failure cell.
    	 * @param row the table row.
    	 * @param column the table column.
    	 * @return true if cell is a failure cell.
    	 */
    	boolean isFailureCell(int row, int column) {
    		return false;
    	}
    	
    	/**
    	 * Adds people to the table.
    	 * @param people the collection of people to add.
    	 */
    	void addPeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (!units.contains(person)) units.add(person);
    		}
    		units = units.sortByName();
    		fireTableDataChanged();
    		
    		getWizard().setButtons(units.size() > 0);
    	}
    	
    	/**
    	 * Removes people from the list.
    	 * @param people the collection of people to remove.
    	 */
    	void removePeople(PersonCollection people) {
    		PersonIterator i = people.iterator();
    		while (i.hasNext()) {
    			Person person = i.next();
    			if (units.contains(person)) units.remove(person);
    		}
    		fireTableDataChanged();
    		
    		getWizard().setButtons(units.size() > 0);
    	}
    }
}