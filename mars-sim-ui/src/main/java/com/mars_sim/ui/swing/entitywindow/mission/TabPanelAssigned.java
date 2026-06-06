/*
 * Mars Simulation Project
 * TabPanelAssigned.java
 * @date 2026-01-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

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
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BaseWorkerModel;

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
        attributePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("vehicle.singular")));

		attributePanel.addLabelledItem(Msg.getString("entity.name"), new EntityLabel(v, getContext()));
		vehicleStatusLabel = attributePanel.addTextField(Msg.getString("vehicle.status"), "", null);
		speedLabel = new JDoubleLabel(StyleManager.DECIMAL_KPH, 0, 0.01D);
        attributePanel.addLabelledItem(Msg.getString("vehicle.speed"), speedLabel);
		distanceNextNavLabel = new JDoubleLabel(StyleManager.DECIMAL2_KM, 0, 0.01D);
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
		columnModel.getColumn(3).setPreferredWidth(20);
	}

	/**
	 * Remove the entity listeners
	 */
    @Override
    public void destroy() {
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

			double travelledDistance = Math.round(vm.getTotalDistanceTravelled()*100.0)/100.0;
			double estTotalDistance = Math.round(vm.getTotalDistanceProposed()*100.0)/100.0;
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
	@SuppressWarnings("serial")
	private static class MemberTableModel extends BaseWorkerModel {
		private static final int BOARDED_VAL = 201;
		private static final int AIRLOCK_VAL = 202;

		protected static final EntityColumnSpec BOARDED = new EntityColumnSpec(new ColumnSpec(BOARDED_VAL, Msg.getString("MainDetailPanel.column.boarded"),
                                                        Boolean.class), Set.of(MobileUnit.CONTAINER_EVENT));	
		protected static final EntityColumnSpec AIRLOCK = new EntityColumnSpec(new ColumnSpec(AIRLOCK_VAL, Msg.getString("MainDetailPanel.column.airlock"),
														Boolean.class), Set.of(EntityEventType.TASK_SUBTASK_EVENT, EntityEventType.STATUS_EVENT, EntityEventType.SPEED_EVENT, TaskManager.TASK_EVENT));											

		
		// Private members.
		private Mission mission;
		private Crewable v = null;
		
		/**
		 * Constructor.
		 */
		private MemberTableModel(Mission mission) {
			super(NAME, TASK, BOARDED, AIRLOCK);
            this.mission = mission;
			if ((mission instanceof VehicleMission vm)
                    && (vm.getVehicle() instanceof Crewable c)) {
				v = c;
			}
			updateOccupantList();
		}


		@Override
		protected Object getEntityValue(Worker entity, int valueIndex) {
			return switch(valueIndex) {
				case BOARDED_VAL -> isBoarded(entity);
				case AIRLOCK_VAL -> isInAirlock(entity);
				default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
			};
		}
		
		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member Worker member.
		 * @return Is the worker boarded ?
		 */
		private boolean isBoarded(Worker member) {
			if (member instanceof Person p && v != null) {
				return v.isCrewmember(p);
			}
			return false;
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
		 * Updates the occupant list.
		 */
		void updateOccupantList() {
			setEntities(mission.getMembers());
		}
	}
}
