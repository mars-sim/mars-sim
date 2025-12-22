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
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.PersonTableModel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The TabPanelCrew is a tab panel for a vehicle's crew information.
 */
@SuppressWarnings("serial")
public class TabPanelCrew extends EntityTabPanel<Vehicle>
		implements ActionListener, TemporalComponent {

	private static final String CREW_ICON = "people"; //$NON-NLS-1$

	private OccupantTableModel memberTableModel;

	private JLabel crewNumTF;

	private int crewNumCache = -1;

	/**
	 * Constructor.
	 * 
	 * @param vehicle the vehicle.
	 * @param context the UI context.
	 */
	public TabPanelCrew(Vehicle vehicle, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCrew.title"),
			ImageLoader.getIconByName(CREW_ICON),
			Msg.getString("TabPanelCrew.tooltip"),
			context, vehicle
		);
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
		Crewable vehicle = (Crewable) getEntity();

		// Create crew cap header label
		int crewCapacityCache = vehicle.getCrewCapacity();
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
		memberTableModel = new OccupantTableModel(vehicle);
		var mission = getEntity().getMission();
		if ((mission instanceof VehicleMission vm) && vehicle.getName().equals(vm.getVehicle().getName()))
			memberTableModel.setMission(vm);

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
		EntityLauncher.attach(memberTable, getContext());

		clockUpdate(null);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {
		Mission newMission = getEntity().getMission();
		var mission = memberTableModel.getMission();
		if (newMission instanceof VehicleMission vm && ((mission == null) || !mission.equals(newMission))) {
			memberTableModel.setMission(vm);
		}

		// Update crew num
		Crewable crewable = (Crewable) getEntity();
		if (crewNumCache != crewable.getCrewNum() ) {
			crewNumCache = crewable.getCrewNum() ;
			crewNumTF.setText(Integer.toString(crewNumCache));
		}

		// Update crew table
		memberTableModel.updateOccupantList();
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the crew monitor button was pressed, create tab in monitor tool.
		Crewable crewable = (Crewable) getEntity();
		showModel(new PersonTableModel(crewable));
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
	 * Table model for occupants.
	 */
	private static class OccupantTableModel extends AbstractTableModel implements EntityListener, EntityModel {

		private static final String NAME = Msg.getString("MainDetailPanel.column.name");
		private static final String TASK = Msg.getString("MainDetailPanel.column.task");
		private static final String MEMBER = Msg.getString("MainDetailPanel.column.member");
		private static final String BOARDED = Msg.getString("MainDetailPanel.column.boarded");
		private static final String AIRLOCK =  Msg.getString("MainDetailPanel.column.airlock");
		
		// Private members.
		private VehicleMission mission;
		private List<Worker> occupantList;
		private Crewable crewable;

		/**
		 * Constructor.
		 */
		private OccupantTableModel(Crewable c) {
			mission = null;
			occupantList = new ArrayList<>();
			crewable = c;
		}
		
		
		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(VehicleMission newMission) {
			this.mission = newMission;

			updateOccupantList();
		}

		/**
		 * What mission is monitored ?
		 * @return
		 */
		VehicleMission getMission() {
			return mission;
		}	

		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		public int getRowCount() {
			return occupantList.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		public int getColumnCount() {
			return 5;
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
				case 0 -> NAME;
				case 1 -> TASK;
				case 2 -> MEMBER;
				case 3 -> BOARDED;
				case 4 -> AIRLOCK;
				default -> null;
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
			if (row < occupantList.size()) {
				Worker member = occupantList.get(row);
				return switch (column) {
					case 0 -> member.getName();
					case 1 -> member.getTaskDescription();
					case 2 -> (isMissionMember(member) ? "Y" : "N");
      				case 3 -> boarded(member) ? "Y" : "N";
      				case 4 -> isInAirlock(member) ? "Y" : "N";
     				default -> null;
  				};
    
			}
			return null;
		}

		/**
		 * Is this occupant a mission member ?
		 *
		 * @param member
		 * @return
		 */
		boolean isMissionMember(Worker member) {
			return (mission != null && mission.getMembers().contains(member));
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean boarded(Worker member) {
			if (member instanceof Person p) {
				return (crewable.isCrewmember(p));
			}
			return false;
		}

		/**
		 * Is this member currently in vehicle's airlock ?
		 *
		 * @param member
		 * @return
		 */
		boolean isInAirlock(Worker member) {
			return (member instanceof Person p		
				&& (Vehicle)crewable instanceof Rover r && r.isInAirlock(p));
		}
		
		/**
		 * Updates mission members.
		 */
		void updateOccupantList() { 
			List<Worker> newList = new ArrayList<>(crewable.getCrew());
			if (mission != null) {
				for (Worker w: mission.getMembers()) {
					if (!newList.contains(w)) {
						newList.add(w);
					}
				}
			}

			if (!occupantList.equals(newList)) {
				final var fixedList = newList;
				// Existing members, not in the new list then remove listener
				occupantList.stream()
						.filter(m -> !fixedList.contains(m))
						.forEach(mm -> mm.removeEntityListener(this));

				// New members, not in the existing list then add listener
				newList.stream()
						.filter(m -> !occupantList.contains(m))
						.forEach(mm -> mm.addEntityListener(this));

				// Replace the old member list with new one.
				occupantList = newList;

				// Update this row
				fireTableDataChanged();
			}
		}

		/**
		 * Clears all members from the table.
		 */
		private void clearMembers() {
			occupantList.forEach(m -> m.removeEntityListener(this));
			occupantList.clear();
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return occupantList.get(row);
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		@Override
		public void entityUpdate(EntityEvent event) {
			String type = event.getType();
			Worker member = (Worker) event.getSource();
			int rowIndex = occupantList.indexOf(member);

			if (rowIndex >= 0 && rowIndex < getRowCount()) {
				if (EntityEventType.NAME_EVENT.equals(type)) {
		            fireTableCellUpdated(rowIndex, 0);
				} 
				else if (EntityEventType.TASK_DESCRIPTION_EVENT.equals(type) || TaskManager.TASK_EVENT.equals(type)
						|| EntityEventType.TASK_ENDED_EVENT.equals(type) || EntityEventType.TASK_SUBTASK_EVENT.equals(type)
						|| EntityEventType.TASK_NAME_EVENT.equals(type)) {
					fireTableCellUpdated(rowIndex, 1);
				}
			}
		}
	}
}
