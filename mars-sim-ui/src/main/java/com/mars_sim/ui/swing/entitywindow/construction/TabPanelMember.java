/*
 * Mars Simulation Project
 * TabPanelMember.java
 * @date 2026-01-24
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.construction;

import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.entitywindow.mission.MemberTableModel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * Tab panel for mission members.
 */
class TabPanelMember extends EntityTableTabPanel<Mission> 
        implements EntityListener {

    private MemberTableModel memberTableModel;

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
		columnModel.getColumn(1).setPreferredWidth(80);
		columnModel.getColumn(2).setPreferredWidth(15);
		columnModel.getColumn(3).setPreferredWidth(15);
		columnModel.getColumn(4).setPreferredWidth(15);
	}

	/**
	 * Remove the entity listeners
	 */
    @Override
    public void destroy() {
        super.destroy();
    }


    @Override
    public void entityUpdate(EntityEvent event) {
		switch(event.getType()) {
			case Mission.ADD_MEMBER_EVENT, Mission.REMOVE_MEMBER_EVENT,
					Mission.MIN_MEMBERS_EVENT, Mission.CAPACITY_EVENT -> {
					memberTableModel.updateOccupantList();
			}

			case EntityEventType.WORK_TIME_EVENT -> {
				//;
			}
			
			default -> {
				// Do nothing
			}
		}
    }
}
