/*
 * Mars Simulation Project
 * TabPanelSupplies.java
 * @date 2026-03-02
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.entitywindow.transport;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.interplanetary.transport.resupply.Resupply;
import com.mars_sim.core.interplanetary.transport.settlement.ArrivingSettlement;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.transportable.SettlementSuppliesPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * Supplies tab panel for a Transportable entity.
 * Displays information about the supplies carried by the supply mission.
 */
class TabPanelSupplies extends EntityTabPanel<Transportable> {

	private static final String SUPPLIES_TITLE = Msg.getString("transportable.supplies");

	private SettlementSuppliesPanel suppliesPanel;

	public TabPanelSupplies(Transportable entity, UIContext context) {
		super(
			SUPPLIES_TITLE,
			ImageLoader.getIconByName("inventory"),
			null,
			context, entity
		);
	}

	@Override
	protected void buildUI(JPanel centerContentPanel) {
		suppliesPanel = new SettlementSuppliesPanel();

		var entity = getEntity();
        int persons = 0;
        int robots = 0;
		if (entity instanceof Resupply r) {            
            persons = r.getNewImmigrantNum();
            robots = r.getNewBotNum();

			suppliesPanel.show(r);
		}
        else if (entity instanceof ArrivingSettlement as) {
            persons = as.getPopulationNum();
            robots = as.getNumOfRobots();

            // A bit messy
            SettlementTemplateConfig sConfig = getContext().getSimulation().getConfig().getSettlementTemplateConfiguration();
			var template = sConfig.getItem(as.getTemplate());
			if (template != null) {
				suppliesPanel.show(template.getSupplies());
            }
        }

        // Add top attr panel
        var attrPanel = new AttributePanel();
        attrPanel.addTextField(Msg.getString("person.plural"), Integer.toString(persons), null);
        attrPanel.addTextField(Msg.getString("robot.plural"), Integer.toString(robots), null);

        centerContentPanel.add(attrPanel, BorderLayout.NORTH);
        centerContentPanel.add(suppliesPanel.getComponent(), BorderLayout.CENTER);
	}
}
