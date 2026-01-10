/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2026-01-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * This tab shows the general details of the Settlement structure.
 */
class TabPanelGeneral extends EntityTabPanel<Settlement> {

    public TabPanelGeneral(Settlement settlement, UIContext context) {
		super(
			GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),
			GENERAL_TOOLTIP,
			context, settlement);
    }

    @Override
    protected void buildUI(JPanel contentPanel) {
        var settlement = getEntity();

        var infoPanel = new AttributePanel();
        contentPanel.add(infoPanel, BorderLayout.NORTH);

        infoPanel.addTextField(Msg.getString("Entity.name"), settlement.getName(), null);
        infoPanel.addLabelledItem(Msg.getString("Authority.singular"), 
                    new EntityLabel(settlement.getReportingAuthority(), getContext()));
        infoPanel.addTextField(Msg.getString("Settlement.template"), settlement.getTemplate(), null);
        infoPanel.addTextField(Msg.getString("Settlement.population"), String.valueOf(settlement.getNumCitizens()), null);
    }
}
