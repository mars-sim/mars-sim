/*
 * Mars Simulation Project
 * TabPanelCrew.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.core.Entity;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.unit_window.TabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;
import com.mars_sim.ui.swing.utils.EntityLauncher;

/**
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
@SuppressWarnings("serial")
public class TabPanelCrew extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCrew.class.getName());

	private static final String CREW_ICON = "people"; //$NON-NLS-1$

	private MemberTableModel memberTableModel;

	private JLabel crewNumTF;

	private int crewNumCache = -1;

	/** The mission instance. */
	private VehicleMission mission;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param desktop the main desktop.
	 */
	public TabPanelCrew(Vehicle vehicle, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCrew.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(CREW_ICON),
			Msg.getString("TabPanelCrew.tooltip"), //$NON-NLS-1$
			vehicle, desktop
		);

		if (vehicle.getMission() instanceof VehicleMission vm) {
			mission = vm;
		}
	}

	@Override
	protected void buildUI(JPanel content) {
        JTable memberTable;
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        
		// Create crew count panel
		AttributePanel crewCountPanel = new AttributePanel(2);
		northPanel.add(crewCountPanel, BorderLayout.CENTER);

		// Create crew num header label
		crewNumTF = crewCountPanel.addTextField(Msg.getString("TabPanelCrew.crewNum"),
								"",
								 Msg.getString("TabPanelCrew.crew.tooltip"));

		// Create crew cap header label
		int crewCapacityCache = ((Crewable) getUnit()).getCrewCapacity();
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
		Crewable vehicle = (Crewable) getUnit();
		memberTableModel = new MemberTableModel(vehicle);
		if ((mission != null) && vehicle.getName().equals(mission.getVehicle().getName()))
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
		EntityLauncher.attach(memberTable, getDesktop());

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
		if ((mission == null) || !mission.equals(newMission)) {
			if ((newMission instanceof VehicleMission vm) && vehicle.equals(vm.getVehicle())) {
				mission = vm;
			}
			else {
				mission = null;
			}

			memberTableModel.setMission(mission);
		}

		// Update crew num
		if (crewNumCache != crewable.getCrewNum() ) {
			crewNumCache = crewable.getCrewNum() ;
			crewNumTF.setText(Integer.toString(crewNumCache));
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
		
		if (memberTableModel != null) {
			memberTableModel.clearMembers();
			memberTableModel = null;
		}
	}

	/**
	 * Table model for mission members.
	 */
	private class MemberTableModel extends AbstractTableModel implements UnitListener, EntityModel {

		// Private members.
		private VehicleMission mission;
		private List<Worker> members;
		private Crewable vehicle;

		/**
		 * Constructor.
		 */
		private MemberTableModel(Crewable v) {
			mission = null;
			members = new ArrayList<>();
			vehicle = v;

			updateMembers();
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
		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("MainDetailPanel.column.name");
				case 1 -> Msg.getString("MainDetailPanel.column.task");
				default -> "Boarded";
};
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		@Override
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Worker member = members.get(row);
				return switch (column) {
					case 0 -> member.getName();
					case 1 -> member.getTaskDescription();
					default -> (boarded(member) ? "Y" : "N");
  				};
    
			}
			return null;
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean boarded(Worker member) {
			if (member.getUnitType() == UnitType.PERSON) {
				return (vehicle.isCrewmember((Person)member));
			}
			return false;
		}

		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(VehicleMission newMission) {
			this.mission = newMission;
			updateMembers();
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		@Override
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = members.indexOf(member);
			if (index < 0) {
				return;
			}
			if (type == UnitEventType.NAME_EVENT) {
				fireTableCellUpdated(index, 0);
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				fireTableCellUpdated(index, 1);
			}
		}

		/**
		 * Updates mission members.
		 */
		void updateMembers() { 
			List<Worker> newList = null;
			if (mission != null) {
				newList = new ArrayList<>(mission.getMembers());
			} else {
				newList = new ArrayList<>(vehicle.getCrew());
			}

			if (!members.equals(newList)) {
				final var fixedList = newList;
				// Existing members, not in the new list then remove listener
				members.stream()
						.filter(m -> !fixedList.contains(m))
						.forEach(mm -> mm.removeUnitListener(this));

				// New members, not in the existing list then add listener
				newList.stream()
						.filter(m -> !members.contains(m))
						.forEach(mm -> mm.addUnitListener(this));

				// Replace the old member list with new one.
				members = newList;

				// Update this row
				fireTableDataChanged();
			}
		}

		/**
		 * Clears all members from the table.
		 */
		private void clearMembers() {
			members.forEach(m -> m.removeUnitListener(this));
			members.clear();
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return members.get(row);
		}
	}
}
