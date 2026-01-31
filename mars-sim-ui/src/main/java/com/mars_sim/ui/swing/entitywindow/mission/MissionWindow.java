/*
 * Mars Simulation Project
 * MissionWindow.java
 * @date 2025-12-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.util.Properties;

import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;

/**
 * The class MissionWindow is the window for a mission entity.
 * It contains a number of tabs.
 */
@SuppressWarnings("serial")
public class MissionWindow extends EntityContentPanel<Mission> {

    public MissionWindow(Mission entity, UIContext context, Properties props) {
        super(entity, context);

        setHeading(entity.getAssociatedSettlement(), "specs", Msg.getString("mission.type"),
                        entity.getMissionType().getName());

        addDefaultTabPanel(new TabPanelGeneral(entity, context));
        addTabPanel(new TabPanelObjectives(entity, context));
        addTabPanel(new TabPanelAssigned(entity, context));

        if (entity instanceof VehicleMission vm) {
            addTabPanel(new TabPanelNavigation(vm, context));
        }
        
        applyProps(props);
    }
}
