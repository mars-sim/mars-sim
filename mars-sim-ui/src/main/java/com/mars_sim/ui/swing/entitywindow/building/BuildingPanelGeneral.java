/*
 * Mars Simulation Project
 * BuildingPanelGeneral.java
 * @date 2024-07-10
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

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

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.svg.SVGMapUtil;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The BuildingPanelGeneral class is a building function panel showing
 * the general status of a settlement building.
 */
class BuildingPanelGeneral extends EntityTabPanel<Building> 
implements TemporalComponent {
		
	private Map<JLabel, Double> labels = new HashMap<>();

	/**
	 * Constructor.
	 * 
	 * @param building the building
	 * @param context the UI context
	 */
	public BuildingPanelGeneral(Building building, UIContext context) {
		super(GENERAL_TITLE,
			ImageLoader.getIconByName(GENERAL_ICON),		
			GENERAL_TOOLTIP,
			context, building
		);
	}

	/**
	 * Builds the UI elements.
	 */
	@Override
	protected void buildUI(JPanel center) {

		JPanel topPanel = new JPanel(new BorderLayout());
		center.add(topPanel, BorderLayout.NORTH);

		var building = getEntity();

		// Add SVG Image loading for the building
		JPanel svgPanel = SVGMapUtil.createBuildingPanel(building.getBuildingType().toLowerCase(), 220, 110);
		topPanel.add(svgPanel, BorderLayout.NORTH);

		var labelPanel = SwingHelper.createTextBlock(Msg.getString("entity.description"), building.getDescription());
		topPanel.add(labelPanel, BorderLayout.CENTER);
		
		// Prepare spring layout info panel.
		AttributePanel infoPanel = new AttributePanel();
		topPanel.add(infoPanel, BorderLayout.SOUTH);

		infoPanel.addBlankField();
		infoPanel.addTextField(Msg.getString("entity.name"), building.getName(), null);
		infoPanel.addTextField(Msg.getString("building.type"), building.getBuildingType(), null);
		infoPanel.addTextField(Msg.getString("building.category"), building.getCategory().getName(), null);
		infoPanel.addTextField(Msg.getString("building.construction"), Conversion.capitalize(building.getConstruction().name()), null);
		infoPanel.addTextField(Msg.getString("building.templateid"), building.getTemplateID(), null);
		// Prepare dimension label
		infoPanel.addTextField(Msg.getString("entity.internalPosn"), building.getPosition().getShortFormat(), 
				"The center x and y coordinates of this building, according to the Settlement Map");
		infoPanel.addTextField(Msg.getString("entity.dimension"), building.getLength() + " m x " + building.getWidth() 
			+ " m x 2.5 m", "Length x Width x Height");
		infoPanel.addTextField("Floor Area", StyleManager.DECIMAL_M2.format(building.getFloorArea()),
				"The floor area in square meters");
		infoPanel.addTextField("Overall Value", StyleManager.DECIMAL_PLACES2.format(building.getBuildingManager().getBuildingValue(building)),
				"The overall building value");
		
		// Prepare attribute panel for building values
		AttributePanel valuePanel = new AttributePanel();
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		listPanel.add(valuePanel, BorderLayout.CENTER);
		center.add(listPanel, BorderLayout.CENTER);
		
		listPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("settlement.buildingtypeValues")));

//		Map<String, Double> buildingTypeMap = building.getBuildingManager().getAllBuildingTypeValues();
		Map<FunctionType, Double> buildingTypeMap = building.getBuildingManager().getFunctionTypeValue(building);
		
		List<FunctionType> buildingList = new ArrayList<>(buildingTypeMap.keySet());
		
		// Sort by descending value and cap the number of displayed rows.
		buildingList.sort((a, b) -> Double.compare(
				buildingTypeMap.getOrDefault(b, 0D),
				buildingTypeMap.getOrDefault(a, 0D)));
		
		int rows =  buildingList.size();
		for (int i = 0; i < rows; i++) {
			FunctionType type = buildingList.get(i);
			double pct = buildingTypeMap.getOrDefault(type, 0D);
			JLabel label = valuePanel.addTextField(type.getName(), StyleManager.DECIMAL_PLACES2.format(pct), null);
			labels.put(label, pct);
		}
	}
	/**
	 * Updates this panel on clock pulse.
	 * Ideally could be converted to event driven update later.
	 */
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
	
		var building = getEntity();

		Map<FunctionType, Double> buildingTypeMap = building.getBuildingManager().getFunctionTypeValue(building);
		
		List<FunctionType> buildingList = new ArrayList<>(buildingTypeMap.keySet());
		
		// Question: how to re-sort the order of label once the attribute panel has been created ?
		
		int rows =  buildingList.size();

		for (int i = 0; i < rows; i++) {
			FunctionType type = buildingList.get(i);
			double pct = buildingTypeMap.getOrDefault(type, 0D);
			String name = type.getName();
			
			Optional<Map.Entry<JLabel, Double>> foundEntry = labels.entrySet().stream()
				    .filter(entry -> name.equals(entry.getKey().getName()))
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
