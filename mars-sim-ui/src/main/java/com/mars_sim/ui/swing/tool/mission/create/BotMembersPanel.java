/*
 * Mars Simulation Project
 * BotMembersPanel.java
 * @date 2025-08-07
 * @author Manny Kung
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
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionType;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MarsPanelBorder;

/**
 * A wizard panel for selecting bots.
 */
@SuppressWarnings("serial")
class BotMembersPanel
extends WizardPanel
implements ActionListener {

	/** The wizard panel name. */
	private static final String NAME = "Bots";

	// Data members.
	private BotsTableModel botsTableModel;
	private JTable botsTable;
	private BotMembersTableModel botMembersTableModel;

	private JTable botMembersTable;
	private JLabel errorMessageLabel;
	private JButton addButton;
	private JButton removeButton;
	private JButton skipButton;
	
	/**
	 * Constructor.
	 * 
	 * @param wizard the create mission wizard.
	 */
	public BotMembersPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor
		super(wizard);

		// Set the layout.
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// Set the border.
		setBorder(new MarsPanelBorder());

		// Create the select members label.
		JLabel selectMembersLabel = createTitleLabel("Select Bots for the Mission");
		add(selectMembersLabel);

		// Add an empty row
		add(new JLabel("     "));
		
		// Create the available bots label.
		JLabel availableBotsLabel = new JLabel("Available Bots", SwingConstants.CENTER);
		availableBotsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(availableBotsLabel);

		// Create the bots panel.
		JPanel botsPane = new JPanel(new BorderLayout(0, 0));
		botsPane.setPreferredSize(new Dimension(300, 150));
		botsPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(botsPane);

		// Create scroll panel for available bots list.
		JScrollPane botsScrollPane = new JScrollPane();
		botsPane.add(botsScrollPane, BorderLayout.CENTER);

		// Create the bots table model.
		botsTableModel = new BotsTableModel();

		// Create the bots table.
		botsTable = new JTable(botsTableModel);
		botsTable.setDefaultRenderer(Object.class, new UnitTableCellRenderer(botsTableModel));
		botsTable.setRowSelectionAllowed(true);
		botsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		botsTable.getSelectionModel().addListSelectionListener(
				e -> {
					// Get the selected rows.
					int[] selectedRows = botsTable.getSelectedRows();
					if (selectedRows.length > 0) {
						if (e.getValueIsAdjusting()) {
							botMembersTable.clearSelection();

							// Check if any of the rows failed.
							boolean failedRow = false;
							for (int selectedRow : selectedRows)
								if (botsTableModel.isFailureRow(selectedRow)) failedRow = true;

							if (failedRow) {
								// Display failed row message and disable add button.
								errorMessageLabel.setText("One or more selected bots cannot be used on the mission " +
										"(see red cells).");
								addButton.setEnabled(false);
							}
							else {
								// Check if number of rows exceed drone remaining capacity.
								if (selectedRows.length > 1) {
									// Display over capacity message and disable add button.
									errorMessageLabel.setText("Not allow to have more than 1 bot to navigate the drone.");
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
		// call it a click to add button when user double clicks the table
		botsTable.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2 && !e.isConsumed()) {
							
							addButtonClicked();
							
							int[] selectedRows = botsTable.getSelectedRows();
							if (selectedRows.length > 0) {
								for (int selectedRow : selectedRows)  {
									Robot robot = (Robot) botMembersTableModel.getUnit(selectedRow);
									getDesktop().showDetails(robot);
								}
							}
						}
					}
				}
				);
		botsScrollPane.setViewportView(botsTable);

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
		addButton = new JButton("Add Bots");
		addButton.setEnabled(false);
		addButton.addActionListener(this);
		buttonPane.add(addButton);

		// Create the remove button.
		removeButton = new JButton("Remove Bots");
		removeButton.setEnabled(false);
		removeButton.addActionListener(e -> {
			// Remove the selected rows in the members table to the bots table.
			int[] selectedRows = botMembersTable.getSelectedRows();
			Collection<Robot> bots = new ConcurrentLinkedQueue<>();
			for (int selectedRow : selectedRows)
				bots.add((Robot) botMembersTableModel.getUnit(selectedRow));
			botsTableModel.addRobots(bots);
			botMembersTableModel.removeRobots(bots);
		});
		buttonPane.add(removeButton);


		skipButton = new JButton("Skip");
		skipButton.setEnabled(true);
		skipButton.addActionListener(e -> wizard.buttonClickedNext());
		buttonPane.add(skipButton);
		
		// Add a vertical strut to make UI space.
		add(Box.createVerticalStrut(10));

		// Add a vertical strut to make UI space.
		add(Box.createVerticalStrut(10));

		// Create the members label.
		JLabel membersLabel = new JLabel("Mission Bots");
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
		botMembersTableModel = new BotMembersTableModel();

		// Create the members table.
		botMembersTable = new JTable(botMembersTableModel);
		// Added sorting
		botMembersTable.setAutoCreateRowSorter(true);
		botMembersTable.setRowSelectionAllowed(true);
		botMembersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		botMembersTable.getSelectionModel().addListSelectionListener(
				e -> {
					int[] selectedRows = botMembersTable.getSelectedRows();
					if (selectedRows.length > 0) {
						if (e.getValueIsAdjusting()) {
							// Enable the remove button.
							botsTable.clearSelection();
							removeButton.setEnabled(true);
						}
					}
					else removeButton.setEnabled(false);
				});
		membersScrollPane.setViewportView(botMembersTable);
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
	 * 
	 * @param isTesting true if it's only testing conditions
	 * @return true if changes can be committed.
	 */
	@Override
	boolean commitChanges(boolean isTesting) {
		Collection<Worker> members = new ConcurrentLinkedQueue<>();
		int size = botMembersTableModel.getRowCount();
		for (int x = 0; x < size; x++) {
			members.add((Worker) botMembersTableModel.getUnit(x));
		}
		// Note: Compare the effect of addMixedMembers and setMixedMembers
		getWizard().getMissionData().setBotMembers(members);

		// Note: if player goes back and forth from panels to panels,
		// how do we handle those members that have been added previously ? 
		// How to remove those previous members ?
		return true;
	}

	/**
	 * Clears information on the wizard panel.
	 */
	void clearInfo() {
		botsTable.clearSelection();
		botMembersTable.clearSelection();
		errorMessageLabel.setText(" ");
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		botsTableModel.updateTable();
		botMembersTableModel.updateTable();
	}

	/**
	 * Table model for bots.
	 */
	private class BotsTableModel
	extends UnitTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** Constructor. */
		private BotsTableModel() {
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
		@Override
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < units.size()) {
				Robot robot = (Robot) getUnit(row);

				try {
					if (column == 0) 
						result = robot.getName();
					else if (column == 1) 
						result = robot.getRobotType().getName();
					else if (column == 2) {
						Mission mission = robot.getBotMind().getMission();
						if (mission != null) result = mission.getName();
						else result = "None";
					}
					else if (column == 3) 
						result = (int) (robot.getPerformanceRating() * 100D) + "%";
					else if (column == 4){			
						if (!robot.isOperable())
							result = "Inoperable";
						else 
							result = "Operable";
					}
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
			MissionDataBean missionData = getWizard().getMissionData();
			Settlement settlement = missionData.getStartingSettlement();
			if (MissionType.CONSTRUCTION == missionData.getMissionType())
				settlement = missionData.getConstructionSettlement();
			// Pick only deliverybot for delivery mission
			var robots = settlement.getAllAssociatedRobots().stream()
							.filter(r -> r.getRobotType() == RobotType.DELIVERYBOT)
							.sorted()
							.toList();
			
			units.addAll(robots);

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
//				Robot robot = (Robot) getUnit(row);

				if (column == 2) {
//					if (robot.getBotMind().getMission() != null) 
//						return true;
				}
			}

			return result;
		}

		/**
		 * Adds robots to the table.
		 * @param robots the collection of robots to add.
		 */
		void addRobots(Collection<Robot> robots) {
			Iterator<Robot> i = robots.iterator();
			while (i.hasNext()) {
				Robot robot = i.next();
				if (!units.contains(robot)) units.add(robot);
			}
			units = CollectionUtils.sortByName(units);
			fireTableDataChanged();
		}

		/**
		 * Removes robots from the table.
		 * @param robots the collection of robots to remove.
		 */
		void removeRobots(Collection<Robot> robots) {
			Iterator<Robot> i = robots.iterator();
			while (i.hasNext()) {
				Robot robot = i.next();
				if (units.contains(robot)) units.remove(robot);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * A table model for mission members.
	 */
	private class BotMembersTableModel
	extends UnitTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/** * Constructor. */
		private BotMembersTableModel() {
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
		@Override
		public Object getValueAt(int row, int column) {
			Object result = null;

			if (row < units.size()) {
				Robot robot = (Robot) getUnit(row);

				switch(column) { 
					case 0 -> result = robot.getName();
					case 1 -> result = robot.getRobotType().getName();
					case 2 -> {
						Mission mission = robot.getBotMind().getMission();
						if (mission != null) result = mission.getName();
						else 
							result = "None";
					}
					case 3 -> result = (int) (robot.getPerformanceRating() * 100D) + "%";
					case 4 -> {			
						if (!robot.isOperable())
							result = "Inoperable";
						else 
							result = "Operable";
					}
					default -> result = null;
				}
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
		 * Adds robots to the table.
		 * @param robots the collection of robots to add.
		 */
		void addRobots(Collection<Robot> robots) {
			Iterator<Robot> i = robots.iterator();
			while (i.hasNext()) {
				Robot robot = i.next();
				if (!units.contains(robot)) units.add(robot);
			}
			units = CollectionUtils.sortByName(units);
			fireTableDataChanged();

			getWizard().setButtons(!units.isEmpty());
		}

		/**
		 * Removes robots from the list.
		 * @param robots the collection of robots to remove.
		 */
		void removeRobots(Collection<Robot> robots) {
			Iterator<Robot> i = robots.iterator();
			while (i.hasNext()) {
				Robot robot = i.next();
				if (units.contains(robot)) units.remove(robot);
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
		// Add the selected rows in the robots table to the members table.
		int[] selectedRows = botsTable.getSelectedRows();
		Collection<Robot> robots = new ConcurrentLinkedQueue<>();
		for (int selectedRow : selectedRows) robots.add((Robot) botsTableModel.getUnit(selectedRow));
		botsTableModel.removeRobots(robots);
		botMembersTableModel.addRobots(robots);
	}

}
