/*
 * Mars Simulation Project
 * TabPanelCrew.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

/**
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
@SuppressWarnings("serial")
public class TabPanelCrew extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCrew.class.getName());

	private static final String CREW_ICON = "people"; //$NON-NLS-1$

	private MemberTableModel memberTableModel;
	private JTable memberTable;

	private JLabel crewNumTF;

	private int crewNumCache;
	private int crewCapacityCache;

	/** The mission instance. */
	private Mission mission;
	/** The Crewable instance. */
	private Crewable crewable;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelCrew(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(CREW_ICON),
			Msg.getString("TabPanelCrew.tooltip"), //$NON-NLS-1$
			vehicle, desktop
		);

		crewable = (Crewable) vehicle;
		mission = vehicle.getMission();
	}

	@Override
	protected void buildUI(JPanel content) {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel(2);
		northPanel.add(crewCountPanel, BorderLayout.CENTER);

		// Create crew num header label
		crewNumCache = crewable.getCrewNum();
		crewNumTF = crewCountPanel.addTextField(Msg.getString("TabPanelCrew.crewNum"),
								Integer.toString(crewNumCache),
								 Msg.getString("TabPanelCrew.crew.tooltip"));

		// Create crew cap header label
		crewCapacityCache = crewable.getCrewCapacity();
		crewCountPanel.addTextField(Msg.getString("TabPanelCrew.crewCapacity"),
								Integer.toString(crewCapacityCache),
					 			Msg.getString("TabPanelCrew.crewCapacity.tooltip"));


		// Create crew monitor button
		JButton monitorButton = new JButton(ImageLoader.getIconByName(MonitorWindow.ICON)); 
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText(Msg.getString("TabPanelCrew.tooltip.monitor")); //$NON-NLS-1$

		JPanel crewButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		crewButtonPanel.add(monitorButton);
		northPanel.add(crewButtonPanel, BorderLayout.SOUTH);
       	content.add(northPanel, BorderLayout.NORTH);

		// Create scroll panel for member list.
		JScrollPane memberScrollPane = new JScrollPane();
		memberScrollPane.setPreferredSize(new Dimension(300, 300));
		content.add(memberScrollPane, BorderLayout.CENTER);

		// Create member table model.
		memberTableModel = new MemberTableModel();
		if (mission != null)
			memberTableModel.setMission(mission);

		// Create member table.
		memberTable = new JTable(memberTableModel);
		memberTable.getColumnModel().getColumn(0).setPreferredWidth(110);
		memberTable.getColumnModel().getColumn(1).setPreferredWidth(140);
		memberTable.setRowSelectionAllowed(true);
		memberTable.setAutoCreateRowSorter(true);
		memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		memberTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		memberScrollPane.setViewportView(memberTable);

		// Call it a click to display details button when user double clicks the table
		memberTable.addMouseListener(new UnitTableLauncher(getDesktop()));

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Vehicle vehicle = (Vehicle) getUnit();
		Crewable crewable = (Crewable) vehicle;
		Mission newMission = vehicle.getMission();
		if (mission != newMission) {
			mission = newMission;
			memberTableModel.setMission(newMission);
		}

		// Update crew num
		if (crewNumCache != crewable.getCrewNum() ) {
			crewNumCache = crewable.getCrewNum() ;
			crewNumTF.setText(crewNumCache + "");
		}

		// Update crew table
		memberTableModel.updateMembers();
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the crew monitor button was pressed, create tab in monitor tool.
		Vehicle vehicle = (Vehicle) getUnit();
		Crewable crewable = (Crewable) vehicle;
		try {
			getDesktop().addModel(new PersonTableModel(crewable));
		} catch (Exception e) {
			logger.severe("PersonTableModel cannot be added.");
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		
		crewNumTF = null;
		memberTable = null;
		memberTableModel = null;
	}

	/**
	 * Table model for mission members.
	 */
	private class MemberTableModel extends AbstractTableModel implements UnitListener, UnitModel {

		// Private members.
		private Mission mission;
		private List<Worker> members;

		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			members = new ArrayList<>();
		}

		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		public int getRowCount() {
			return members.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		public int getColumnCount() {
			return 3;
		}

		/**
		 * Gets the column name at a given index.
		 *
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("MainDetailPanel.column.name"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("MainDetailPanel.column.task"); //$NON-NLS-1$
			else
				return "Boarded";
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Worker member = members.get(row);
				if (column == 0)
					return member.getName();
				else if (column == 1)
					return member.getTaskDescription();
				else {
					if (boarded(member))
						return "Y";
					else
						return "N";
				}
			} else
				return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean boarded(Worker member) {
			if (mission instanceof VehicleMission) {			
				if (member.getUnitType() == UnitType.PERSON) {
					Rover r = (Rover)(((VehicleMission)mission).getVehicle());
					if (r != null && r.isCrewmember((Person)member))
						return true;
				}
			}
			return false;
		}

		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			updateMembers();
		}

		/**
		 * Catch unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = members.indexOf(member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		/**
		 * Update mission members.
		 */
		void updateMembers() {
			if (mission != null) {
				List<Worker> newList = new ArrayList<>(mission.getMembers());

				if (!members.equals(newList)) {
					List<Integer> rows = new ArrayList<>();

					for (Worker mm: members) {
						if (!newList.contains(mm)) {
							mm.removeUnitListener(this);
						}
					}

					for (Worker mm: newList) {
						if (!members.contains(mm)) {
							mm.addUnitListener(this);
							int index = newList.indexOf(mm);
							rows.add(index);
						}
					}

					// Replace the old member list with new one.
					members = newList;

					for (int i : rows) {
						// Update this row
						SwingUtilities.invokeLater(new MemberTableUpdater(i));
					}
				}
			} else {
				if (members.size() > 0) {
					clearMembers();
					SwingUtilities.invokeLater(new MemberTableUpdater());
				}
			}
		}

		/**
		 * Clear all members from the table.
		 */
		private void clearMembers() {
			if (members != null) {
				Iterator<Worker> i = members.iterator();
				while (i.hasNext()) {
					Worker member = i.next();
					member.removeUnitListener(this);
				}
				members.clear();
			}
		}


		/**
		 * Inner class for updating member table.
		 */
		private class MemberTableUpdater implements Runnable {

			private int row;
			private int column;
			private boolean entireData;

			private MemberTableUpdater(int row, int column) {
				this.row = row;
				this.column = column;
				entireData = false;
			}

			private MemberTableUpdater(int row) {
				this.row = row;
				this.column = -1;
				entireData = false;
			}

			private MemberTableUpdater() {
				entireData = true;
			}

			public void run() {
				if (entireData) {
					fireTableDataChanged();
				} else if (column == -1) {
					fireTableRowsUpdated(row, row);
				} else {
					fireTableCellUpdated(row, column);
				}
			}
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return (Unit) members.get(row);
		}
	}
}
