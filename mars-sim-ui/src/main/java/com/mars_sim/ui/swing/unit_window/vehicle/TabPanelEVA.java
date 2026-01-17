/*
 * Mars Simulation Project
 * TabPanelEVA.java
 * @date 2024-06-24
 * @author Manny Kung
 */

package com.mars_sim.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.VehicleAirlock;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The TabPanelEVA class represents the EVA airlock function of a vehicle.
 */
@SuppressWarnings("serial")
class TabPanelEVA extends EntityTabPanel<Rover> implements TemporalComponent{

	private static final String SUIT_ICON = "eva"; 
	
	private static final String UNLOCKED = "Unlocked";
	private static final String LOCKED = "Locked";
	
	private static final String WIKI_URL = "https://github.com/mars-sim/mars-sim/wiki/Airlock";
	
	
	private boolean activationCache;
	private boolean transitionCache;
	
	private int innerDoorCache;
	private int outerDoorCache;
	private int occupiedCache;
	private int emptyCache;
	
	private double cycleTimeCache;
	
	private String operatorCache = "";
	private String airlockStateCache = "";
	private String innerDoorStateCache = "";
	private String outerDoorStateCache = "";

	private AirlockMode airlockModeCache;
	
	private JLabel innerDoorLabel;
	private JLabel outerDoorLabel;
	private JLabel occupiedLabel;
	private JLabel emptyLabel;
	private JLabel operatorLabel;
	private JLabel airlockStateLabel;
	private JLabel activationLabel;
	private JLabel transitionLabel;
	private JLabel cycleTimeLabel;
	private JLabel innerDoorStateLabel;
	private JLabel outerDoorStateLabel;
	private JLabel airlockModeLabel;
	
	private	UnitListPanel<Person> occupantListPanel;
	private UnitListPanel<Person> outsideListPanel;
	private UnitListPanel<Person> insideListPanel;
	
	private VehicleAirlock vehicleAirlock;

    /**
     * Constructor.
     * 
     * @param vehicle the vehicle.
     * @param context The UI context.
     */
    public TabPanelEVA(Rover vehicle, UIContext context) {
        // Use the TabPanel constructor
        super(
            Msg.getString("TabPanelEVA.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(SUIT_ICON),        	
        	Msg.getString("TabPanelEVA.tooltip"), //$NON-NLS-1$
        	context, vehicle
        );

        vehicleAirlock = (VehicleAirlock)vehicle.getAirlock();
    }
    
	/**
	 * Builds the UI.
	 * 
	 * @param content
	 */
    @Override
    protected void buildUI(JPanel content) {

        // Create top panel
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        content.add(panel, BorderLayout.NORTH);
        
        // Create top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        panel.add(topPanel, BorderLayout.CENTER);
        
    	if (vehicleAirlock == null) {
    		return;
    	}

		JPanel wikiPanel = new JPanel(new FlowLayout());
		JButton wikiButton = new JButton(GuideWindow.wikiIcon);
		wikiPanel.add(wikiButton);
		wikiButton.setToolTipText("Open Airlock Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));
		panel.add(wikiPanel, BorderLayout.NORTH);

		AttributePanel westGrid = new AttributePanel(6, 1);
		topPanel.add(westGrid);
        
		AttributePanel eastGrid = new AttributePanel(6, 1);
		topPanel.add(eastGrid);		
		
		if (vehicleAirlock.isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.innerDoor.state"),
										   innerDoorStateCache);

		// Create innerDoorLabel
		innerDoorLabel = westGrid.addRow(Msg.getString("TabPanelEVA.innerDoor.number"),
						Integer.toString(vehicleAirlock.getNumAwaitingInnerDoor()));

		if (vehicleAirlock.isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.outerDoor.state"),
										   outerDoorStateCache);

		// Create outerDoorLabel
		outerDoorLabel = westGrid.addRow(Msg.getString("TabPanelEVA.outerDoor.number"),
									Integer.toString(vehicleAirlock.getNumAwaitingOuterDoor()));
		
		// Create airlockModeLabel
		airlockModeLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.airlock.mode"),
											vehicleAirlock.getAirlockMode().getName());

		// Create occupiedLabel
		occupiedLabel = westGrid.addRow(Msg.getString("TabPanelEVA.occupied"),
											Integer.toString(vehicleAirlock.getNumInChamber()));

		// Create airlockStateLabel
		airlockStateLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.airlock.state"),
											vehicleAirlock.getState().toString());

		// Create emptyLabel
		emptyLabel = westGrid.addRow(Msg.getString("TabPanelEVA.empty"),
											Integer.toString(vehicleAirlock.getNumEmptied()));

		// Create transitionLabel
		transitionLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.airlock.transition"),
				Conversion.capitalize0(Boolean.toString(vehicleAirlock.isTransitioning())));

		// Create cycleTimeLabel
		cycleTimeLabel = westGrid.addRow(Msg.getString("TabPanelEVA.airlock.cycleTime"),
											StyleManager.DECIMAL_PLACES1.format(vehicleAirlock.getRemainingCycleTime()));
		
		// Create OperatorLabel
		operatorLabel = eastGrid.addRow(Msg.getString("TabPanelEVA.operator"),
				vehicleAirlock.getOperatorName());

		// Create activationLabel
		activationLabel = westGrid.addRow(Msg.getString("TabPanelEVA.airlock.activation"),
						Boolean.toString(vehicleAirlock.isActivated()));
		
		// Create boxBottomPanel
		JPanel boxBottomPanel = new JPanel();
		boxBottomPanel.setLayout(new BoxLayout(boxBottomPanel, BoxLayout.X_AXIS));
		panel.add(boxBottomPanel, BorderLayout.SOUTH);
	
		// Create outside list panel - zone 4
		JPanel outsidePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		outsidePanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone4.tooltip"));
		outsidePanel.setBorder(
				SwingHelper.createLabelBorder(
						Msg.getString("BuildingPanelEVA.titledB.zone4")));
		boxBottomPanel.add(outsidePanel);

		// Create outsideListPanel 
		var context = getContext();
		outsideListPanel = new UnitListPanel<>(context, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(vehicleAirlock.getAwaitingOuterDoor());
			}
		};
		outsidePanel.add(outsideListPanel);
		
		// Create occupant panel - zone 1 to 3
		JPanel occupantPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		occupantPanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone13.tooltip"));
		occupantPanel.setBorder(
				SwingHelper.createLabelBorder(
						Msg.getString("BuildingPanelEVA.titledB.zone13")));
		boxBottomPanel.add(occupantPanel);
		
        // Create occupant list
        occupantListPanel = new UnitListPanel<>(context, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(vehicleAirlock.getAllInsideOccupants());
			}
        };
        occupantPanel.add(occupantListPanel);
        
		// Create inside wait panel - zone 0
		JPanel insidePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		insidePanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone0.tooltip"));
		insidePanel.setBorder(
				SwingHelper.createLabelBorder(
						Msg.getString("BuildingPanelEVA.titledB.zone0")));
		boxBottomPanel.add(insidePanel);

		// Create insideListPanel 
		insideListPanel = new UnitListPanel<>(context, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(vehicleAirlock.getAwaitingInnerDoor());
			}
		};
		insidePanel.add(insideListPanel);
    }

	/**
	 * Update airlock counts
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update innerDoorLabel
		int inner = vehicleAirlock.getNumAwaitingInnerDoor();
		if (innerDoorCache != inner) {
			innerDoorCache = inner;
			innerDoorLabel.setText(Integer.toString(inner));
		}

		// Update outerDoorLabel
		int outer = vehicleAirlock.getNumAwaitingOuterDoor();
		if (outerDoorCache != outer) {
			outerDoorCache = outer;
			outerDoorLabel.setText(Integer.toString(outer));
		}

		// Update occupiedLabel
		int numChamber = vehicleAirlock.getNumInChamber();
		if (occupiedCache != numChamber) {
			occupiedCache = numChamber;
			occupiedLabel.setText(Integer.toString(numChamber));
		}

		// Update emptyLabel
		int emptyNumChamber = vehicleAirlock.getNumEmptied();
		if (emptyCache != emptyNumChamber) {
			emptyCache = emptyNumChamber;
			emptyLabel.setText(Integer.toString(emptyNumChamber));
		}

		// Update operatorLabel
		String name = vehicleAirlock.getOperatorName();
		if (!operatorCache.equalsIgnoreCase(name)) {
			operatorCache = name;
			operatorLabel.setText(name);
		}

		// Update airlockStateLabel
		String state = vehicleAirlock.getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(state);
		}
		
		// Update activationLabel
		boolean activated = vehicleAirlock.isActivated();
		if (activationCache != activated) {
			activationCache = activated;
			activationLabel.setText(Conversion.capitalize0(Boolean.toString(activated)));
		}

		// Update activationLabel
		boolean transition = vehicleAirlock.isTransitioning();
		if (transitionCache != transition) {
			transitionCache = transition;
			transitionLabel.setText(Conversion.capitalize0(Boolean.toString(transition)));
		}
		
		// Update airlockModeLabel
		AirlockMode airlockMode = vehicleAirlock.getAirlockMode();
		if (airlockModeCache != airlockMode) {
			airlockModeCache = airlockMode;
			airlockModeLabel.setText(airlockMode.getName());
		}
		
		// Update cycleTimeLabel
		double time = vehicleAirlock.getRemainingCycleTime();
		if (cycleTimeCache != time) {
			cycleTimeCache = time;
			cycleTimeLabel.setText(StyleManager.DECIMAL_PLACES1.format(cycleTimeCache));
		}

		String innerDoorState = (vehicleAirlock.isInnerDoorLocked() ?
											LOCKED : UNLOCKED);

		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(innerDoorState);
		}

		String outerDoorState = (vehicleAirlock.isOuterDoorLocked() ?
											LOCKED : UNLOCKED);

		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(outerDoorState);
		}
		
        // Update occupant list
        occupantListPanel.update();
        outsideListPanel.update();
    	insideListPanel.update();
    }
}