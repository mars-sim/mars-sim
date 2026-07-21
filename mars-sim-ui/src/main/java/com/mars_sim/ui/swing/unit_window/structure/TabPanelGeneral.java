/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2026-01-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;

/**
 * This tab shows the general details of the Settlement structure.
 */
class TabPanelGeneral extends EntityTabPanel<Settlement> implements TemporalComponent {

	private int populationCitizensCache = -1;
	private int populationCapacityCache = -1;
	
	private JLabel populationCitizensLabel;
	private JLabel populationCapacityLabel;
	
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

        
        infoPanel.addTextField(Msg.getString("entity.name"), settlement.getName(), null);
        infoPanel.addLabelledItem(Msg.getString("authority.singular"), 
                    new EntityLabel(settlement.getReportingAuthority(), getContext()));
        infoPanel.addTextField(Msg.getString("settlement.template"), settlement.getTemplate(), null);
        
        // Create citizen label
     	populationCitizensLabel = infoPanel.addTextField(Msg.getString("settlement.population"),
     			String.valueOf(settlement.getNumCitizens()), null);

     	// Create population capacity label
     	populationCapacityLabel = infoPanel.addTextField(Msg.getString("settlement.capacity"),
     			String.valueOf(settlement.getBuildingManager().getPopulationCapacity()), null);

    }
    

	@Override
	public void clockUpdate(ClockPulse pulse) {
		var settlement = getEntity();

		int num0 = settlement.getNumCitizens();
		// Update citizen num
		if (populationCitizensCache != num0) {
			populationCitizensCache = num0;
			populationCitizensLabel.setText(Integer.toString(populationCitizensCache));
		}
		
		int cap = settlement.getPopulationCapacity();
		// Update capacity
		if (populationCapacityCache != cap) {
			populationCapacityCache = cap;
			populationCapacityLabel.setText(Integer.toString(populationCapacityCache));
		}
	}
		
}
