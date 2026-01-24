/*
 * Mars Simulation Project
 * TabPanelAssigned.java
 * @date 2026-01-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.TaskManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.TableModelUpdater;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * Tab panel for assigned mission members and Vehicles.
 */
class TabPanelAssigned extends EntityTableTabPanel<Mission> 
        implements EntityListener {

    private MemberTableModel memberTableModel;
    private Vehicle v = null;
    private JLabel vehicleStatusLabel;
    private JDoubleLabel speedLabel;
    private JDoubleLabel distanceNextNavLabel;
    private JLabel traveledLabel;

    public TabPanelAssigned(Mission entity, UIContext context) {
		super(
			"Assigned", 
			ImageLoader.getIconByName("inventory"), null,
			entity, context
		);
		
        setTableTitle(Msg.getString("mission.members"));

        if (entity instanceof VehicleMission vm) {
            v = vm.getVehicle();
        }
		else if (entity instanceof ConstructionMission cm) {
			v = cm.getConstructionVehicles().stream().findFirst().orElse(null);
		}
    }

    /**
	 * Initializes the vehicle pane if one is assigned to the mission.
	 * 
	 * @return May return null if no vehicle is assigned.
	 */
	@Override
    protected JPanel createInfoPanel() {
        if (v == null) {
            return null;
        }
		
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
        attributePanel.setBorder(StyleManager.createLabelBorder(Msg.getString("vehicle.singular")));

		attributePanel.addLabelledItem(Msg.getString("entity.name"), new EntityLabel(v, getContext()));
		vehicleStatusLabel = attributePanel.addTextField(Msg.getString("vehicle.status"), "", null);
		speedLabel = new JDoubleLabel(StyleManager.DECIMAL_KPH, 0, 0.01D);
        attributePanel.addLabelledItem(Msg.getString("vehicle.speed"), speedLabel);
		distanceNextNavLabel = new JDoubleLabel(StyleManager.DECIMAL_KM, 0, 0.01D);
        attributePanel.addLabelledItem(Msg.getString("MainDetailPanel.distanceNextNavPoint"), distanceNextNavLabel);
		
        traveledLabel = attributePanel.addTextField(Msg.getString("MainDetailPanel.distanceTraveled"), "", null);

        v.addEntityListener(this);
		updateVehicleInfo();
        return attributePanel;
	}

    /**
     * Creates the table model for the assigned members.
     */
    @Override
    protected TableModel createModel() {
        memberTableModel = new MemberTableModel(getEntity());

        return memberTableModel;
    }

	/**
	 * Configure the columns of the member table.
	 * 
	 * @param columnModel Columns to be configured
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(60);
		columnModel.getColumn(1).setPreferredWidth(90);
		columnModel.getColumn(2).setPreferredWidth(20);
		if (v != null) {
			columnModel.getColumn(3).setPreferredWidth(20);
			columnModel.getColumn(4).setPreferredWidth(20);			
		}
	}

	/**
	 * Remove the entity listeners
	 */
    @Override
    public void destroy() {
        memberTableModel.destroy();
		if (v != null) {
			v.removeEntityListener(this);
		}
        super.destroy();
    }

	private void updateVehicleInfo() {
		vehicleStatusLabel.setText(v.printStatusTypes());
		speedLabel.setValue(v.getSpeed());

		if (getEntity() instanceof VehicleMission vm) {
			distanceNextNavLabel.setValue(vm.getDistanceCurrentLegRemaining());

			double travelledDistance = Math.round(vm.getTotalDistanceTravelled()*10.0)/10.0;
			double estTotalDistance = Math.round(vm.getTotalDistanceProposed()*10.0)/10.0;
			traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
					travelledDistance,
					estTotalDistance
					));
		}
	}

    @Override
    public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT,
					Mission.MIN_MEMBERS_EVENT, Mission.CAPACITY_EVENT -> {
					memberTableModel.updateOccupantList();
					updateVehicleInfo();
			}

			case EntityEventType.STATUS_EVENT, EntityEventType.SPEED_EVENT ->
				updateVehicleInfo();
			default -> {
				// Do nothing
			}
		}
    }
    
	/**
	 * Table model for mission members.
	 */
	private static class MemberTableModel extends AbstractTableModel implements EntityListener, EntityModel {

		private static final String NAME = Msg.getString("entity.name");
		private static final String TASK = Msg.getString("task.singular");
		private static final String MEMBER = Msg.getString("mission.member");
		private static final String BOARDED = Msg.getString("MainDetailPanel.column.boarded");
		private static final String AIRLOCK =  Msg.getString("MainDetailPanel.column.airlock");
		
		// Private members.
		private Mission mission;
		private List<Worker> occupantList = Collections.emptyList();
		private Vehicle v = null;
		
		/**
		 * Constructor.
		 */
		private MemberTableModel(Mission mission) {
            this.mission = mission;
			if ((mission instanceof VehicleMission vm)
                    && (vm.getVehicle() instanceof Crewable)) {
				v = vm.getVehicle();
				updateOccupantList();
			}
		}
		
		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		@Override
		public int getRowCount() {
			return occupantList.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		@Override
		public int getColumnCount() {
			return (v != null) ? 5 : 3;
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
      				case 2 -> isMissionMember(member) ? "Y" : "N";
      				case 3 -> isBoarded(member) ? "Y" : "N";
      				case 4 -> isInAirlock(member) ? "Y" : "N";
     				default -> null;
				};
			}
			return null;
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member Worker member.
		 * @return Is the worker boarded ?
		 */
		private boolean isBoarded(Worker member) {
			if (member instanceof Person p) {
				return ((Crewable)v).isCrewmember(p);
			}
			return false;
		}
		
		/**
		 * Is this occupant a mission member ?
		 *
		 * @param member Worker member.
		 * @return Is the worker a mission member ?
		 */
		private boolean isMissionMember(Worker member) {
			return (mission != null && mission.getMembers().contains(member));
		}
		
		/**
		 * Is this member currently in vehicle's airlock ?
		 *
		 * @param member	 Worker member.
		 * @return Is the worker in the airlock ?
		 */
		private boolean isInAirlock(Worker member) {
			return (member instanceof Person p && v instanceof Rover r && r.isInAirlock(p));
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void entityUpdate(EntityEvent event) {
			String type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = occupantList.indexOf(member);
			if (EntityEventType.NAME_EVENT.equals(type)) {
				SwingUtilities.invokeLater(new TableModelUpdater(this, index, 0));
			} else if (EntityEventType.TASK_DESCRIPTION_EVENT.equals(type) || TaskManager.TASK_EVENT.equals(type)
					|| EntityEventType.TASK_ENDED_EVENT.equals(type) || EntityEventType.TASK_SUBTASK_EVENT.equals(type)
					|| EntityEventType.TASK_NAME_EVENT.equals(type)) {
				SwingUtilities.invokeLater(new TableModelUpdater(this, index, 1));
			}
		}

        void destroy() {
            // Remove listeners
            occupantList.forEach(m -> m.removeEntityListener(this));
        }

		/**
		 * Updates the occupant list.
		 */
		void updateOccupantList() {
            Crewable crewable = (Crewable)v;
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
				SwingUtilities.invokeLater(new TableModelUpdater(this));
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return occupantList.get(row);
		}
	}
}
