/*
 * Mars Simulation Project
 * TabPanelObjectives.java
 * @date 2026-01-24
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.mission;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.objectives.CollectResourceObjective;
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.mission.objectives.EmergencySupplyObjective;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.mission.objectives.FieldStudyObjectives;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.mission.objectives.RescueVehicleObjective;
import com.mars_sim.core.mission.objectives.TradeObjective;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.mission.ObjectivesPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.CollectResourcePanel;
import com.mars_sim.ui.swing.tool.mission.objectives.ConstructionPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.EmergencySupplyPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.ExplorationPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.FieldStudyPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.MiningPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.RescuePanel;
import com.mars_sim.ui.swing.tool.mission.objectives.TradePanel;

/** 
 * The TabPanelObjectives is the tab panel for mission objectives.
 */
class TabPanelObjectives extends EntityTabPanel<Mission> 
        implements EntityListener{

    private JTabbedPane tabPanel;

    public TabPanelObjectives(Mission mission, UIContext context) {
		super(
			"Objectives",
			ImageLoader.getIconByName("objective"),
			null,
			context, mission
		);
    }

    @Override
    protected void buildUI(JPanel centerContentPanel) {
        var context = getContext();
        var mission = getEntity();

        tabPanel = new JTabbedPane();
		for(MissionObjective o : mission.getObjectives()) {
			JPanel newPanel = switch(o) {
                case CollectResourceObjective cro -> new CollectResourcePanel(cro);
                case FieldStudyObjectives fso -> new FieldStudyPanel(fso, context);
                case ExplorationObjective eo -> new ExplorationPanel(eo);
                case MiningObjective mo -> new MiningPanel(mo, context);
                case TradeObjective to -> new TradePanel(to, context);
                case ConstructionObjective co -> new ConstructionPanel(co, context);
                case RescueVehicleObjective ro -> new RescuePanel(ro, context);
                case EmergencySupplyObjective so -> new EmergencySupplyPanel(so, context);
                default -> null;
            };

            if (newPanel != null) {
                tabPanel.addTab(newPanel.getName(), newPanel);
            }
		}
        centerContentPanel.add(tabPanel, BorderLayout.CENTER);
    }

    /**
     * The Objectives panels may have registered listeners; unregister them.
     */
    @Override
    public void destroy() {
        // Release objectives
        if (tabPanel != null) {
            for(var c : tabPanel.getComponents()) {
                if (c instanceof ObjectivesPanel odp) {
                    odp.unregister();
                }
            }
        }

        super.destroy();
    }

    @Override
    public void entityUpdate(EntityEvent event) {
        // Forward to objectives panels
        for(var c : tabPanel.getComponents()) {
            if (c instanceof EntityListener el) {
                el.entityUpdate(event);
            }
        }
    }
}
