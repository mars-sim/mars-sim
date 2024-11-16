/*
 * Mars Simulation Project
 * SettlementUnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.Unit;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.MalfunctionTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SponsorTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;

/**
 * The SettlementUnitWindow is the window for displaying a settlement.
 */
@SuppressWarnings("serial")
public class SettlementUnitWindow extends UnitWindow {
	
	private static final String BASE = "settlement";
	
	private static final String POP = "pop";
	private static final String VEHICLE = "vehicle";
	private static final String SPONSOR = "sponsor";
	private static final String TEMPLATE = "template";

	private static final String X_OF_Y = "%d / %d";
	
	private JLabel popLabel;
	private JLabel vehLabel;
	private JLabel countryLabel;
	private JLabel templateLabel;

	private JPanel statusPanel;
	
	private Settlement settlement;
	
	/**
	 * Constructor
	 *
	 * @param desktop the main desktop panel.
	 * @param unit    the unit to display.
	 */
	public SettlementUnitWindow(MainDesktopPane desktop, Unit unit) {
		// Use UnitWindow constructor
		super(desktop, unit, unit.getName(), false);

		this.settlement = (Settlement) unit;

		// Create status panel
		statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		initTopPanel(settlement);
		
		initTabPanel(settlement);

		statusUpdate();
	}
	
	
	public void initTopPanel(Settlement settlement) {
		statusPanel.setPreferredSize(new Dimension(-1, UnitWindow.STATUS_HEIGHT + 5));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);

		JLabel nameLabel = new JLabel(getShortenedName(unit.getName()), displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		JPanel namePane = new JPanel(new BorderLayout(0, 0));
		namePane.add(nameLabel, BorderLayout.CENTER);
		namePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		namePane.setAlignmentY(Component.CENTER_ALIGNMENT);
		nameLabel.setToolTipText("Name of Settlement");
		setImage(BASE, nameLabel);
		
		Font font = StyleManager.getSmallLabelFont();
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		statusPanel.add(namePane, BorderLayout.WEST);
		
		JLabel countryIconLabel = new JLabel();
		countryIconLabel.setToolTipText("Country of Origin");
		setImage(SPONSOR, countryIconLabel);
	
		JLabel popIconLabel = new JLabel();
		popIconLabel.setToolTipText("# of population : (indoor / total)");
		setImage(POP, popIconLabel);

		JLabel templateIconLabel = new JLabel();
		templateIconLabel.setToolTipText("Settlement template being used");
		setImage(TEMPLATE, templateIconLabel);
		
		JLabel vehIconLabel = new JLabel();
		vehIconLabel.setToolTipText("# of vehicles : (in settlement / total)");
		setImage(VEHICLE, vehIconLabel);

		JPanel countryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JPanel popPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JPanel vehPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		countryLabel = new JLabel();
		countryLabel.setFont(font);
		
		popLabel = new JLabel();
		popLabel.setFont(font);

		templateLabel = new JLabel();
		templateLabel.setFont(font);
		
		vehLabel = new JLabel();
		vehLabel.setFont(font);

		countryPanel.add(countryIconLabel);
		countryPanel.add(countryLabel);
		countryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		popPanel.add(popIconLabel);
		popPanel.add(popLabel);
		popPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		vehPanel.add(vehIconLabel);
		vehPanel.add(vehLabel);
		vehPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		templatePanel.add(templateIconLabel);
		templatePanel.add(templateLabel);
		templatePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel gridPanel = new JPanel(new GridLayout(2, 2, 5, 1));
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);	
		gridPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		gridPanel.add(countryPanel);
		gridPanel.add(popPanel);
		gridPanel.add(templatePanel);
		gridPanel.add(vehPanel);

		statusPanel.add(gridPanel, BorderLayout.CENTER);
		
		// Show country
		List<String> list = settlement.getReportingAuthority().getCountries();
		String countryName = "Multi-National";
		if (list.size() == 1)
			countryName = settlement.getReportingAuthority().getCountries().get(0);
		
		countryLabel.setText(countryName);
		templateLabel.setText(settlement.getTemplate());
		
	}
	
		
	public void initTabPanel(Settlement settlement) {		
		
		addTabPanel(new TabPanelAirComposition(settlement, desktop));

		addTabPanel(new TabPanelBots(settlement, desktop));

		addTabPanel(new TabPanelCitizen(settlement, desktop));
		
		addTabPanel(new TabPanelComputing(settlement, desktop));
		
		addTabPanel(new TabPanelCooking(settlement, desktop));

		addTabPanel(new TabPanelConstruction(settlement, desktop));

		addTabPanel(new TabPanelCredit(settlement, desktop));

		addTabPanel(new TabPanelFoodProduction(settlement, desktop));
		
		addTabPanel(new TabPanelGroupActivity(settlement, desktop));

		addTabPanel(new TabPanelGoods(settlement, desktop));

		addTabPanel(new TabPanelIndoor(settlement, desktop));
		
		addTabPanel(new InventoryTabPanel(settlement, desktop));

		addTabPanel(new LocationTabPanel(settlement, desktop));
		
		addTabPanel(new TabPanelMaintenance(settlement, desktop));

		addTabPanel(new MalfunctionTabPanel(settlement, desktop));

		addTabPanel(new TabPanelManufacture(settlement, desktop));

		addTabPanel(new TabPanelMissions(settlement, desktop));
		
		addTabPanel(new NotesTabPanel(settlement, desktop));

		addTabPanel(new TabPanelPreferences(settlement, desktop));

		addTabPanel(new TabPanelOrganization(settlement, desktop));

		addTabPanel(new TabPanelPowerGrid(settlement, desktop));
		
		addTabPanel(new TabPanelProcessHistory(settlement, desktop));

		addTabPanel(new TabPanelResourceProcesses(settlement, desktop));

		addTabPanel(new TabPanelScience(settlement, desktop));

		addTabPanel(new SponsorTabPanel(settlement.getReportingAuthority(), desktop));
		
		addTabPanel(new TabPanelThermal(settlement, desktop));

		addTabPanel(new TabPanelVehicles(settlement, desktop));

		addTabPanel(new TabPanelWeather(settlement, desktop));

		addTabPanel(new TabPanelWasteProcesses(settlement, desktop));

		sortTabPanels();
		
		// Add to tab panels. 
		addTabIconPanels();
	}

	
	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();
		
		statusUpdate();
	}

	/*
	 * Updates the status of the settlement.
	 */
	public void statusUpdate() {
		popLabel.setText(String.format(X_OF_Y, settlement.getIndoorPeopleCount(),
										settlement.getNumCitizens()));
		vehLabel.setText(String.format(X_OF_Y, settlement.getNumParkedVehicles(),
										settlement.getOwnedVehicleNum()));
	}
	
	/**
	 * Prepares unit window for deletion.
	 */
	@Override
	public void destroy() {		
		popLabel = null;
		vehLabel = null;
		countryLabel = null;
		templateLabel = null;
		statusPanel = null;		
		settlement = null;
	}
}
