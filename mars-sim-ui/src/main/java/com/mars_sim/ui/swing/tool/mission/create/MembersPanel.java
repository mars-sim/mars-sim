/*
 * Mars Simulation Project
 * MembersPanel.java
 * @date 2021-08-27
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.PhysicalConditionFormat;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MarsPanelBorder;
/**
 * A wizard panel for selecting settlers.
 */
@SuppressWarnings("serial")
class MembersPanel
extends WizardPanel
implements ActionListener {

	/** The wizard panel name. */
	private static final String NAME = "Settlers";

	// Data members.
	private PeopleTableModel peopleTableModel;
	private MembersTableModel membersTableModel;

	private JTable peopleTable;
	private JTable membersTable;
	
	private JLabel vehicleCapacityLabel;
	private JLabel errorMessageLabel;
	
	private JButton addButton;
	private JButton removeButton;
	private JButton skipButton;
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard
	 */
	public MembersPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor
		super(wizard);

		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create the select members label.
		JLabel selectMembersLabel = createTitleLabel("Select members for the mission.");
		add(selectMembersLabel);

		// Add an empty row
		add(new JLabel("     "));
		
		// Create the available people label.
		JLabel availablePeopleLabel = new JLabel("Available People", SwingConstants.CENTER);
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
		// Added sorting
		peopleTable.setAutoCreateRowSorter(true);
		peopleTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(peopleTableModel));
		peopleTable.setRowSelectionAllowed(true);
		peopleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		peopleTable.getSelectionModel().addListSelectionListener(
				e -> {
					// Get the selected rows.
					int[] selectedRows = peopleTable.getSelectedRows();
					if (selectedRows.length > 0) {
						if (e.getValueIsAdjusting()) {
							membersTable.clearSelection();

							// Check if any of the rows failed.
							boolean failedRow = false;
							for (int selectedRow : selectedRows)
								if (peopleTableModel.isFailureRow(selectedRow)) failedRow = true;

							if (failedRow) {
								// Display failed row message and disable add button.
								errorMessageLabel.setText("One or more selected people cannot be used on the mission " +
										"(see red cells).");
								addButton.setEnabled(false);
							}
							else {
								// Check if number of rows exceed rover remaining capacity.
								if (selectedRows.length > getRemainingVehicleCapacity()) {
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
				);

		peopleTable.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && !e.isConsumed()) {

							addButtonClicked();
							 
							int[] selectedRows = peopleTable.getSelectedRows();
							if (selectedRows.length > 0) {
								for (int selectedRow : selectedRows)  {
									Person person = (Person) peopleTableModel.getUnit(selectedRow);
									getDesktop().showDetails(person);
								}
							}
						}
					}
				}
				);
		peopleScrollPane.setViewportView(peopleTable);

		// Create the message label.
		errorMessageLabel = createErrorLabel();
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
		addButton.addActionListener(this);
		buttonPane.add(addButton);

		// Create the remove button.
		removeButton = new JButton("Remove Members");
		removeButton.setEnabled(false);
		removeButton.addActionListener(e -> {
			// Remove the selected rows in the members table to the people table.
			int[] selectedRows = membersTable.getSelectedRows();
			Collection<Person> people = new ConcurrentLinkedQueue<>();
			for (int selectedRow : selectedRows)
				people.add((Person) membersTableModel.getUnit(selectedRow));
			peopleTableModel.addPeople(people);
			membersTableModel.removePeople(people);
			updateVehicleCapacityLabel();
		});
		buttonPane.add(removeButton);

		skipButton = new JButton("Skip");
		skipButton.setEnabled(true);
		skipButton.addActionListener(e -> wizard.buttonClickedNext());
		buttonPane.add(skipButton);
		
		// Add a vertical strut to make UI space.
		add(Box.createVerticalStrut(10));

		// Create the rover capacity label.
		vehicleCapacityLabel = new JLabel("Remaining rover capacity: ");
		vehicleCapacityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(vehicleCapacityLabel);

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
		membersTable.setAutoCreateRowSorter(true);
		membersTable.setRowSelectionAllowed(true);
		membersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		membersTable.getSelectionModel().addListSelectionListener(
				e -> {
					int[] selectedRows = membersTable.getSelectedRows();
					if (selectedRows.length > 0) {
						if (e.getValueIsAdjusting()) {
							// Enable the remove button.
							peopleTable.clearSelection();
							removeButton.setEnabled(true);
						}
					}
					else removeButton.setEnabled(false);
				});
		membersScrollPane.setViewportView(membersTable);
	}

	/**
	 * Gets the wizard panel name.
	 * 
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}

	/**
	 * Commits changes from this wizard panel.
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		Collection<Worker> members = new ConcurrentLinkedQueue<>();
		int size = membersTableModel.getRowCount();

		for (int x = 0; x < size; x++) {
			members.add((Worker) membersTableModel.getUnit(x));
		}
		// Use setMixedMembers here, not addMixedMembers
		getWizard().getMissionData().setMixedMembers(members);
	
		return true;
	}

	/**
	 * Clears information on the wizard panel.
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
		updateVehicleCapacityLabel();
	}

	/**
	 * Updates the vehicle capacity label.
	 */
	void updateVehicleCapacityLabel() {
		MissionType type = getWizard().getMissionData().getMissionType();
		if (MissionType.CONSTRUCTION == type) {
			vehicleCapacityLabel.setText(" ");
		}
		else {
			
			if (MissionType.DELIVERY == type) {
				vehicleCapacityLabel.setText("Remaining Drone Capacity: " + getRemainingVehicleCapacity());
			}
			else {
				vehicleCapacityLabel.setText("Remaining Rover Capacity: " + getRemainingVehicleCapacity());
			}
		}
	}

	/**
	 * Gets the remaining vehicle capacity.
	 * 
	 * @return vehicle capacity.
	 */
	int getRemainingVehicleCapacity() {
		MissionType type = getWizard().getMissionData().getMissionType();
		if (MissionType.CONSTRUCTION == type) return Integer.MAX_VALUE;
		else {
			if (MissionType.DELIVERY == type) {
				return Delivery.MAX_MEMBERS;
			}
			else {
				int roverCapacity = getWizard().getMissionData().getRover().getCrewCapacity();
				int memberNum = membersTableModel.getRowCount();
				return roverCapacity - memberNum;
			}
		}
	}

	/**
	 * Table model for people.
	 */
	private class PeopleTableModel
	extends UnitTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** Constructor. */
		private PeopleTableModel() {
			// Use UnitTableModel constructor.
			super();

			// Add table columns.
			columns.add("Name");
			columns.add("Job");
			columns.add("Work Shift");
			columns.add("Current Mission");
			columns.add("Performance");
			columns.add("Health");
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row the row whose value is to be queried.
		 * @param column the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < units.size()) {
				Person person = (Person) getUnit(row);

				if (column == 0) 
					result = person.getName();
				else if (column == 1) 
					result = person.getMind().getJob().getName();
				else if (column == 2) {
					result = person.getShiftSlot().getStatusDescription();
				}
				else if (column == 3) {
					Mission mission = person.getMind().getMission();
					if (mission != null) result = mission.getName();
					else result = "None";
				}
				else if (column == 4) 
					result = (int) (person.getPerformanceRating() * 100D) + "%";
				else if (column == 5)
					result = PhysicalConditionFormat.getHealthSituation(person.getPhysicalCondition());
			}

			return result;
		}

		/**
		 * Updates the table data.
		 */
		void updateTable() {
			units.clear();
			MissionDataBean missionData = getWizard().getMissionData();
			Settlement settlement = missionData.getStartingSettlement();
			if (MissionType.CONSTRUCTION == missionData.getMissionType())
				settlement = missionData.getConstructionSettlement();
			Collection<Person> people = CollectionUtils.sortByName(settlement.getIndoorPeople());
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) units.add(i.next());
			fireTableDataChanged();
		}

		/**
		 * Checks if a table cell is a failure cell.
		 * 
		 * @param row the table row.
		 * @param column the table column.
		 * @return true if cell is a failure cell.
		 */
		boolean isFailureCell(int row, int column) {
			boolean result = false;

			if (row < units.size()) {
				Person person = (Person) getUnit(row);

				if (column == 3 && person.getMind().getMission() != null)
					return true;
			}

			return result;
		}

		/**
		 * Adds people to the table.
		 * 
		 * @param people the collection of people to add.
		 */
		void addPeople(Collection<Person> people) {
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (!units.contains(person)) units.add(person);
			}
			units = CollectionUtils.sortByName(units);
			fireTableDataChanged();
		}

		/**
		 * Removes people from the table.
		 * 
		 * @param people the collection of people to remove.
		 */
		void removePeople(Collection<Person> people) {
			Iterator<Person> i = people.iterator();
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
	private class MembersTableModel
	extends UnitTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** * Constructor. */
		private MembersTableModel() {
			// Use UnitTableModel constructor.
			super();

			// Add columns.
			columns.add("Name");
			columns.add("Job");
			columns.add("Work Shift");
			columns.add("Current Mission");
			columns.add("Performance");
			columns.add("Health");
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * 
		 * @param row the row whose value is to be queried
		 * @param column the column whose value is to be queried
		 * @return the value Object at the specified cell
		 */
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < units.size()) {
				Person person = (Person) getUnit(row);

				if (column == 0) 
					result = person.getName();
				else if (column == 1) 
					result = person.getMind().getJob().getName();
				else if (column == 2) {
					result = person.getShiftSlot().getStatusDescription();
				}
				else if (column == 3) {
					Mission mission = person.getMind().getMission();
					if (mission != null) result = mission.getName();
					else result = "None";
				}						
				else if (column == 4) 
					result = (int) (person.getPerformanceRating() * 100D) + "%";
				else if (column == 5)
					result = PhysicalConditionFormat.getHealthSituation(person.getPhysicalCondition());
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
		 * 
		 * @param row the table row.
		 * @param column the table column.
		 * @return true if cell is a failure cell.
		 */
		boolean isFailureCell(int row, int column) {
			return false;
		}

		/**
		 * Adds people to the table.
		 * 
		 * @param people the collection of people to add.
		 */
		void addPeople(Collection<Person> people) {
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (!units.contains(person)) units.add(person);
			}
			units = CollectionUtils.sortByName(units);
			fireTableDataChanged();

			getWizard().setButtons(!units.isEmpty());
		}

		/**
		 * Removes people from the list.
		 * 
		 * @param people the collection of people to remove.
		 */
		void removePeople(Collection<Person> people) {
			Iterator<Person> i = people.iterator();
			while (i.hasNext()) {
				Person person = i.next();
				if (units.contains(person)) units.remove(person);
			}
			fireTableDataChanged();

			getWizard().setButtons(!units.isEmpty());
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == addButton) {
			addButtonClicked();
		}
	}

	private final void addButtonClicked() {
		// Add the selected rows in the people table to the members table.
		int[] selectedRows = peopleTable.getSelectedRows();
		Collection<Person> people = new ConcurrentLinkedQueue<>();
		for (int selectedRow : selectedRows) 
			people.add((Person)peopleTableModel.getUnit(selectedRow));
		peopleTableModel.removePeople(people);
		membersTableModel.addPeople(people);
		updateVehicleCapacityLabel();
	}
	
}
