/**
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2024-06-24
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.ClassicAirlock;
import com.mars_sim.core.building.function.EVA;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
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
 * The BuildingPanelEVA class presents the EVA activities
 * of a building.
 */
@SuppressWarnings("serial")
class BuildingPanelEVA extends EntityTabPanel<Building> implements TemporalComponent {
	
	private static final String SUIT_ICON = "eva";

	private static final String UNLOCKED = "Unlocked";
	private static final String LOCKED = "Locked";
	
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
	
	private final String WIKI_URL = "https://github.com/mars-sim/mars-sim/wiki/Airlock";
	
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

	private UnitListPanel<Person> occupantListPanel;
	private UnitListPanel<Person> outsideListPanel;
	private UnitListPanel<Person> insideListPanel;
	private UnitListPanel<Person> reservationListPanel;

	private EVA eva;
	private ClassicAirlock buildingAirlock;

	/**
	 * Constructor.
	 * 
	 * @param eva the eva function of a building this panel is for.
	 * @param context the UI context
	 */
	public BuildingPanelEVA(EVA eva, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelEVA.title"), 
			ImageLoader.getIconByName(SUIT_ICON), null,
			context, eva.getBuilding()
		);

		// Initialize data members
		this.eva = eva;
		this.buildingAirlock = (ClassicAirlock)eva.getAirlock();
	}
	
	/**
	 * Builds the UI.
	 * 
	 * @param content
	 */
	@Override
	protected void buildUI(JPanel content) {
		
		// Create top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        content.add(topPanel, BorderLayout.CENTER);
 
		JPanel wikiPanel = new JPanel(new FlowLayout());
		JButton wikiButton = new JButton(GuideWindow.wikiIcon);
		wikiPanel.add(wikiButton);
		wikiButton.setToolTipText("Open Airlock Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));
		content.add(wikiPanel, BorderLayout.NORTH);
		
		AttributePanel westGrid = new AttributePanel(6, 1);
		topPanel.add(westGrid, BorderLayout.CENTER);
        
		AttributePanel eastGrid = new AttributePanel(6, 1);
		topPanel.add(eastGrid, BorderLayout.EAST);	
		
		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = eastGrid.addRow( Msg.getString("BuildingPanelEVA.innerDoor.state"),
										   innerDoorStateCache);

		// Create innerDoorLabel
		innerDoorLabel = westGrid.addRow(Msg.getString("BuildingPanelEVA.innerDoor.number"),
									  Integer.toString(eva.getNumAwaitingInnerDoor()));


		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = eastGrid.addRow(Msg.getString("BuildingPanelEVA.outerDoor.state"),
										   outerDoorStateCache);
	
		// Create outerDoorLabel
		outerDoorLabel = westGrid.addRow(Msg.getString("BuildingPanelEVA.outerDoor.number"),
									  Integer.toString(eva.getNumAwaitingOuterDoor()));

		// Create airlockModeLabel
		airlockModeCache = buildingAirlock.getAirlockMode();
		
		airlockModeLabel = eastGrid.addRow(Msg.getString("BuildingPanelEVA.airlock.mode"),
				airlockModeCache.getName());

		// Create occupiedLabel
		occupiedLabel = westGrid.addRow(Msg.getString("BuildingPanelEVA.occupied"),
									 Integer.toString(eva.getNumInChamber()));

		// Create airlockStateLabel
		airlockStateLabel = eastGrid.addRow( Msg.getString("BuildingPanelEVA.airlock.state"),
								buildingAirlock.getState().toString());

		// Create emptyLabel
		emptyLabel = westGrid.addRow( Msg.getString("BuildingPanelEVA.empty"),
								  Integer.toString(eva.getNumEmptied()));
		
		// Create transitionLabel
		transitionLabel = eastGrid.addRow( Msg.getString("BuildingPanelEVA.airlock.transition"),
				Conversion.capitalize0(Boolean.toString(buildingAirlock.isTransitioning())));

		// Create cycleTimeLabel
		cycleTimeLabel = westGrid.addRow( Msg.getString("BuildingPanelEVA.airlock.cycleTime"),
					StyleManager.DECIMAL_PLACES1.format(buildingAirlock.getRemainingCycleTime()));

		// Create OperatorLabel
		operatorLabel = eastGrid.addRow( Msg.getString("BuildingPanelEVA.operator"),
									 eva.getOperatorName());

		// Create activationLabel
		activationLabel = westGrid.addRow( Msg.getString("BuildingPanelEVA.airlock.activation"),
				Conversion.capitalize0(Boolean.toString(buildingAirlock.isActivated())));
		
		// Create gridPanel
		JPanel gridPanel = new JPanel(new GridLayout(2, 2));
		Border margin = new EmptyBorder(2, 2, 2, 2);
		gridPanel.setBorder(margin);
		content.add(gridPanel, BorderLayout.SOUTH);
		
		// Create outside list panel
		JPanel outsidePanel = new JPanel(new BorderLayout());
		outsidePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("BuildingPanelEVA.titledB.zone4")));
		outsidePanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone4.tooltip"));
		gridPanel.add(outsidePanel);

		// Create outsideListPanel 
		outsideListPanel = new UnitListPanel<>(getContext(), new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAwaitingOuterDoor());
			}
		};
		outsidePanel.add(outsideListPanel);
		
		// Create occupant panel
		JPanel occupantPanel = new JPanel(new BorderLayout());
		occupantPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("BuildingPanelEVA.titledB.zone13")));
		occupantPanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone13.tooltip"));
		gridPanel.add(occupantPanel);

		// Create occupant list panel
		occupantListPanel = new UnitListPanel<>(getContext(), new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAllInsideOccupants());
			}
		};
		occupantPanel.add(occupantListPanel);

		// Create outside wait panel
		JPanel insidePanel = new JPanel(new BorderLayout());
		insidePanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("BuildingPanelEVA.titledB.zone0")));
		insidePanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.zone0.tooltip"));
		gridPanel.add(insidePanel);

		// Create insideListPanel 
		insideListPanel = new UnitListPanel<>(getContext(), new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAwaitingInnerDoor());
			}
		};
		insidePanel.add(insideListPanel);
		
		// Create reservation panel
		JPanel reservationPanel = new JPanel(new BorderLayout());
		reservationPanel.setBorder(SwingHelper.createLabelBorder(Msg.getString("BuildingPanelEVA.titledB.reserved")));
		reservationPanel.setToolTipText(Msg.getString("BuildingPanelEVA.titledB.reserved.tooltip"));
		gridPanel.add(reservationPanel);
		
		reservationListPanel = new UnitListPanel<>(getContext(), new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getReserved());
			}		
		};	
		reservationPanel.add(reservationListPanel);
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {

		// Update innerDoorLabel
		int inner = eva.getNumAwaitingInnerDoor();
		if (innerDoorCache != inner) {
			innerDoorCache = inner;
			innerDoorLabel.setText(Integer.toString(inner));
		}

		// Update outerDoorLabel
		int outer = eva.getNumAwaitingOuterDoor();
		if (outerDoorCache != outer) {
			outerDoorCache = outer;
			outerDoorLabel.setText(Integer.toString(outer));
		}

		// Update occupiedLabel
		int numChamber = eva.getNumInChamber();
		if (occupiedCache != numChamber) {
			occupiedCache = numChamber;
			occupiedLabel.setText(Integer.toString(numChamber));
		}

		// Update emptyLabel
		int emptyNumChamber = eva.getNumEmptied();
		if (emptyCache != emptyNumChamber) {
			emptyCache = emptyNumChamber;
			emptyLabel.setText(Integer.toString(emptyNumChamber));
		}

		// Update operatorLabel
		String name = eva.getOperatorName();
		if (!operatorCache.equalsIgnoreCase(name)) {
			operatorCache = name;
			operatorLabel.setText(name);
		}
		
		// Update cycleTimeLabel
		double time = buildingAirlock.getRemainingCycleTime();
		if (cycleTimeCache != time) {
			cycleTimeCache = time;
			cycleTimeLabel.setText(StyleManager.DECIMAL_PLACES1.format(cycleTimeCache));
		}

		String innerDoorState = "";
		if (buildingAirlock.isInnerDoorLocked())
			innerDoorState = LOCKED;
		else {
			innerDoorState = UNLOCKED;
		}

		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(innerDoorState);
		}

		String outerDoorState = "";
		if (buildingAirlock.isOuterDoorLocked())
			outerDoorState = LOCKED;
		else {
			outerDoorState = UNLOCKED;
		}

		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(outerDoorState);
		}

		// Update airlockStateLabel
		String state = buildingAirlock.getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(state);
		}
		
		// Update activationLabel
		boolean activated = buildingAirlock.isActivated();
		if (activationCache != activated) {
			activationCache = activated;
			activationLabel.setText(Conversion.capitalize0(Boolean.toString(activated)));
		}

		// Update activationLabel
		boolean transition = buildingAirlock.isTransitioning();
		if (transitionCache != transition) {
			transitionCache = transition;
			transitionLabel.setText(Conversion.capitalize0(Boolean.toString(transition)));
		}
		
		// Update airlockModeLabel
		AirlockMode airlockMode = buildingAirlock.getAirlockMode();
		if (airlockModeCache != airlockMode) {
			airlockModeCache = airlockMode;
			airlockModeLabel.setText(airlockMode.getName());
		}
		
		// Update list
		occupantListPanel.update();
		outsideListPanel.update();
		insideListPanel.update();
		reservationListPanel.update();
	}
}