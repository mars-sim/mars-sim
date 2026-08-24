/*
 * Mars Simulation Project
 * TabPanelMember.java
 * @date 2026-01-24
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.construction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.LightUtilityVehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * Tab panel for mission members.
 */
class TabPanelMember extends EntityTableTabPanel<Mission> 
        implements EntityListener {

    private MemberTableModel memberTableModel;

    private LightUtilityVehicle luv = null;
    
    private JLabel vehicleStatusLabel;
    private JLabel vehicleOperatorLabel;
    
    
    public TabPanelMember(Mission entity, UIContext context) {
		super(
			"Member", 
			ImageLoader.getIconByName("inventory"), null,
			entity, context
		);
		
        setTableTitle(Msg.getString("mission.members"));

    }

    /**
	 * Initializes the vehicle pane if one is assigned to the mission.
	 * 
	 * @return May return null if no vehicle is assigned.
	 */
	@Override
    protected JPanel createInfoPanel() {
		
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel();
        attributePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("vehicle.singular")));
        
        attributePanel.addLabelledItem(Msg.getString("entity.name"), new EntityLabel(luv, getContext()));
		vehicleStatusLabel = attributePanel.addTextField(Msg.getString("vehicle.status"), "", null);
		vehicleOperatorLabel = attributePanel.addTextField(Msg.getString("vehicle.operator"), "", null);
		
		updateVehicleInfo();
		
        return attributePanel;
	}

	/**
	 * Updates the vehicle info.
	 */
	private void updateVehicleInfo() {
		if (getEntity() instanceof ConstructionMission cm) {
			luv = cm.getConstructionVehicles().stream().findFirst().orElse(null);
			
	        luv.addEntityListener(this);
	        
			vehicleStatusLabel.setText(luv.printStatusTypes());
			var op = luv.getOperator();
			String name = "";
			if (op != null);
				name = op.getName();
			vehicleOperatorLabel.setText(name);
		}
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
		columnModel.getColumn(1).setPreferredWidth(80);
		columnModel.getColumn(2).setPreferredWidth(12);
		columnModel.getColumn(3).setPreferredWidth(12);
		columnModel.getColumn(4).setPreferredWidth(20);
	}


    @Override
    public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT,
					Mission.MIN_MEMBERS_EVENT, Mission.CAPACITY_EVENT -> {
					memberTableModel.updateOccupantList();
					updateVehicleInfo();
			}

			case EntityEventType.WORK_TIME_EVENT -> {
				Mission context = (Mission)getContext();
				if (context instanceof ConstructionMission cm) {
					updateVehicleInfo();
				}
			}
			
			default -> {
				// Do nothing
			}
		}
    }
    
	/**
	 * Remove the entity listeners
	 */
    @Override
    public void destroy() {
		if (luv != null) {
			luv.removeEntityListener(this);
		}
        super.destroy();
    }
}
