/*
 * Mars Simulation Project
 * TabPanelAssigned.java
 * @date 2026-01-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;

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
}
