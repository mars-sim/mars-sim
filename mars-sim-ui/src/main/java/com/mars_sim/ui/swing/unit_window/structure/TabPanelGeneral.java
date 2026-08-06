/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2026-01-03
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.EntityLabel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This tab shows the general details of the Settlement structure.
 */
class TabPanelGeneral extends EntityTabPanel<Settlement> implements TemporalComponent {

	private int populationCitizensCache = -1;
	private int populationCapacityCache = -1;
	
	private JLabel populationCitizensLabel;
	private JLabel populationCapacityLabel;
	
	private Map<JLabel, Double> labels = new HashMap<>();

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
	
		// Prepare attribute panel for building values
		AttributePanel valuePanel = new AttributePanel();
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.add(valuePanel, BorderLayout.CENTER);
		contentPanel.add(listPanel, BorderLayout.CENTER);
		
		listPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("settlement.buildingtypeValues")));

		Map<String, Double> buildingTypeMap = settlement.getBuildingManager().getAllBuildingTypeValues();
				
		List<String> buildingList = new ArrayList<>(buildingTypeMap.keySet());
		
		// Sort by descending value and cap the number of displayed rows.
		buildingList.sort((a, b) -> Double.compare(
				buildingTypeMap.getOrDefault(b, 0D),
				buildingTypeMap.getOrDefault(a, 0D)));
		
		int rows =  buildingList.size();
		for (int i = 0; i < rows; i++) {
			String type = buildingList.get(i);
			double pct = buildingTypeMap.getOrDefault(type, 0D);
			JLabel label = valuePanel.addTextField(type, StyleManager.DECIMAL_PLACES2.format(pct), null);
			labels.put(label, pct);
		}
    }
    

	@Override
	public void clockUpdate(ClockPulse pulse) {
		refreshUI();
	}
	
	/**
	 * Refresh the UI elements of this tab. Commonly called when the tab is selected.
	 * Can be overridden by subclasses. but should be rarely needed.
	 */
	@Override
    public void refreshUI() {
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
		
		Map<String, Double> buildingTypeMap = settlement.getBuildingManager().getAllBuildingTypeValues();
		
		List<String> buildingList = new ArrayList<>(buildingTypeMap.keySet());
		
		// Sort by descending value and cap the number of displayed rows.
//		buildingList.sort((a, b) -> Double.compare(
//				buildingTypeMap.getOrDefault(b, 0D),
//				buildingTypeMap.getOrDefault(a, 0D)));
		
		// Question: how to re-sort the order of label once the attribute panel has been created ?
		
		int rows =  buildingList.size();
		
		for (int i = 0; i < rows; i++) {
			String type = buildingList.get(i);
			double pct = buildingTypeMap.getOrDefault(type, 0D);
			
			Optional<Map.Entry<JLabel, Double>> foundEntry = labels.entrySet().stream()
				    .filter(entry -> type.equals(entry.getKey().getName()))
				    .findFirst();
			
			foundEntry.ifPresent(entry -> {
				JLabel keyLabel = entry.getKey();
			    double value = entry.getValue();
				if (value != pct)
					keyLabel.setText(StyleManager.DECIMAL_PLACES2.format(pct));
			});
		}
    }
}
