/*
 * Mars Simulation Project
 * SettlementUnitWindow.java
 * @date 2022-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.SponsorTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

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

	private static final String ONE_SPACE = " ";
	private static final String TWO_SPACES = "  ";
	private static final String SIX_SPACES = "      ";
	
	private JLabel popLabel;
	private JLabel vehLabel;
	private JLabel sponsorLabel;
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

		Settlement settlement = (Settlement) unit;
		this.settlement = settlement;

		// Create status panel
		statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		initTopPanel(settlement);
		
		initTabPanel(settlement);

		statusUpdate();
	}
	
	
	public void initTopPanel(Settlement settlement) {
		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = SIX_SPACES + unit.getShortenedName() + SIX_SPACES;

		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		JLabel nameLabel = new JLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		JPanel namePane = new JPanel(new BorderLayout(50, 0));
		namePane.add(nameLabel, BorderLayout.CENTER);
		nameLabel.setToolTipText("Name of Settlement");
		setImage(BASE, nameLabel);
		
		Font font = null;

		if (MainWindow.OS.contains("linux")) {
			font = new Font("DIALOG", Font.BOLD, 8);
		} else {
			font = new Font("DIALOG", Font.BOLD, 10);
		}
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);

		statusPanel.add(namePane);

		JLabel popIconLabel = new JLabel();
		popIconLabel.setToolTipText("# of population : (indoor / total)");
		setImage(POP, popIconLabel);
		
		JLabel sponsorIconLabel = new JLabel();
		sponsorIconLabel.setToolTipText("Name of sponsor");
		setImage(SPONSOR, sponsorIconLabel);

		JLabel vehIconLabel = new JLabel();
		vehIconLabel.setToolTipText("# of vehicles : (in settlement / total)");
		setImage(VEHICLE, vehIconLabel);
		
		JLabel templateIconLabel = new JLabel();
		templateIconLabel.setToolTipText("Settlement template being used");
		setImage(TEMPLATE, templateIconLabel);

		JPanel popPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel vehPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel sponsorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		popLabel = new JLabel();
		popLabel.setFont(font);

		vehLabel = new JLabel();
		vehLabel.setFont(font);

		sponsorLabel = new JLabel();
		sponsorLabel.setFont(font);

		templateLabel = new JLabel();
		templateLabel.setFont(font);

		popPanel.add(popIconLabel);
		popPanel.add(popLabel);
		popPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		vehPanel.add(vehIconLabel);
		vehPanel.add(vehLabel);
		vehPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		sponsorPanel.add(sponsorIconLabel);
		sponsorPanel.add(sponsorLabel);
		sponsorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		templatePanel.add(templateIconLabel);
		templatePanel.add(templateLabel);
		templatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel rowPanel = new JPanel(new GridLayout(2, 2, 0, 0));
		rowPanel.add(popPanel);
		rowPanel.add(vehPanel);
		rowPanel.add(sponsorPanel);
		rowPanel.add(templatePanel);

		statusPanel.add(rowPanel);
		rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);	
		
		sponsorLabel.setText(TWO_SPACES + settlement.getSponsor().getName());
		templateLabel.setText(TWO_SPACES + settlement.getTemplate());
	}
	
		
	public void initTabPanel(Settlement settlement) {		
		
		addTabPanel(new TabPanelAirComposition(settlement, desktop));

		addTabPanel(new TabPanelBots(settlement, desktop));

		addTabPanel(new TabPanelCitizen(settlement, desktop));
		
		addTabPanel(new TabPanelCooking(settlement, desktop));

		addTabPanel(new TabPanelConstruction(settlement, desktop));

		addTabPanel(new TabPanelCredit(settlement, desktop));

		addTabPanel(new TabPanelFoodProduction(settlement, desktop));

		addTabPanel(new TabPanelGoods(settlement, desktop));

		addTabPanel(new TabPanelIndoor(settlement, desktop));
		
		addTabPanel(new InventoryTabPanel(settlement, desktop));

		addTabPanel(new LocationTabPanel(settlement, desktop));
		
		addTabPanel(new TabPanelMaintenance(settlement, desktop));

		addTabPanel(new TabPanelMalfunction(settlement, desktop));
		
		addTabPanel(new TabPanelManufacture(settlement, desktop));

		addTabPanel(new TabPanelMissions(settlement, desktop));
		
		addTabPanel(new NotesTabPanel(settlement, desktop));

		addTabPanel(new TabPanelOrganization(settlement, desktop));

		addTabPanel(new TabPanelPowerGrid(settlement, desktop));

		addTabPanel(new TabPanelResourceProcesses(settlement, desktop));

		addTabPanel(new TabPanelScience(settlement, desktop));

		addTabPanel(new SponsorTabPanel(settlement.getSponsor(), desktop));
		
		addTabPanel(new TabPanelThermalSystem(settlement, desktop));

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
	 * Updates the status of the settlement
	 */
	public void statusUpdate() {
		popLabel.setText(TWO_SPACES + settlement.getIndoorPeopleCount() 
				+ ONE_SPACE + "/" + ONE_SPACE + settlement.getNumCitizens());
		vehLabel.setText(TWO_SPACES + settlement.getNumParkedVehicles() 
				+ ONE_SPACE + "/" + ONE_SPACE + settlement.getOwnedVehicleNum());
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	}

	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {		
		popLabel = null;
		vehLabel = null;
		sponsorLabel = null;
		templateLabel = null;
		
		statusPanel = null;
			
		settlement = null;
	}
}
