/**
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2023-11-11
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.structure.building.function.BuildingAirlock;
import com.mars_sim.core.structure.building.function.EVA;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The BuildingPanelEVA class presents the EVA activities
 * of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelEVA extends BuildingFunctionPanel {
	
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
	private BuildingAirlock buildingAirlock;

	/**
	 * Constructor.
	 * 
	 * @param eva the eva function of a building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelEVA(EVA eva, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelEVA.title"), 
			ImageLoader.getIconByName(SUIT_ICON), 
			eva.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.eva = eva;
		this.buildingAirlock = (BuildingAirlock)eva.getAirlock();
	}
	
	/**
	 * Build the UI
	 * 
	 * @param content
	 */
	@Override
	protected void buildUI(JPanel content) {
		MainDesktopPane desktop = getDesktop();
		
		// Create label panel
		JPanel topPanel = new JPanel(new BorderLayout());
		content.add(topPanel, BorderLayout.NORTH);

		AttributePanel labelGrid = new AttributePanel(6, 2);
		topPanel.add(labelGrid, BorderLayout.NORTH);
		
		// Create innerDoorLabel
		innerDoorLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.innerDoor.number"),
									  Integer.toString(eva.getNumAwaitingInnerDoor()), null);

		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.innerDoor.state"),
										   innerDoorStateCache, null);

		// Create outerDoorLabel
		outerDoorLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.outerDoor.number"),
									  Integer.toString(eva.getNumAwaitingOuterDoor()), null);

		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.outerDoor.state"),
										   outerDoorStateCache, null);
		
		// Create occupiedLabel
		occupiedLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.occupied"),
									 Integer.toString(eva.getNumInChamber()), null);

		// Create airlockModeLabel
		airlockModeCache = buildingAirlock.getAirlockMode();
		airlockModeLabel = labelGrid.addTextField(Msg.getString("BuildingPanelEVA.airlock.mode"),
				airlockModeCache.getName(), null);

		// Create emptyLabel
		emptyLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.empty"),
								  Integer.toString(eva.getNumEmptied()), null);

		// Create airlockStateLabel
		airlockStateLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.state"),
								buildingAirlock.getState().toString(), null);

		// Create cycleTimeLabel
		cycleTimeLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.cycleTime"),
					StyleManager.DECIMAL_PLACES1.format(buildingAirlock.getRemainingCycleTime()), null);
		
		// Create transitionLabel
		transitionLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.transition"),
				Conversion.capitalize0(Boolean.toString(buildingAirlock.isTransitioning())), null);

		// Create activationLabel
		activationLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.airlock.activation"),
				Conversion.capitalize0(Boolean.toString(buildingAirlock.isActivated())), null);

		// Create OperatorLabel
		operatorLabel = labelGrid.addTextField( Msg.getString("BuildingPanelEVA.operator"),
									 eva.getOperatorName(), null);
		
		JPanel wikiPanel = new JPanel(new FlowLayout());
		Icon wikiIcon = ImageLoader.getIconByName(GuideWindow.WIKI_ICON);
		JButton wikiButton = new JButton(wikiIcon);
		wikiPanel.add(wikiButton);
//		wikiButton.setAlignmentX(.5f);
//		wikiButton.setAlignmentY(.5f);
		wikiButton.setToolTipText("Open Airlock Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));
		topPanel.add(wikiPanel, BorderLayout.SOUTH);
		
		// Create listPanel
		JPanel listPanel = new JPanel(new GridLayout(2, 2));
		Border margin = new EmptyBorder(2, 2, 2, 2);
		listPanel.setBorder(margin);
//		int width = UnitWindow.WIDTH - 40;
//		int halfWidth = width/2;
//		listPanel.setSize(new Dimension(width, -1));
		content.add(listPanel, BorderLayout.CENTER);
		
		// Create outside list panel
		JPanel outsidePanel = new JPanel(new BorderLayout());
//		outsidePanel.setSize(new Dimension(halfWidth, -1));
		outsidePanel.setBorder(BorderFactory.createTitledBorder(Msg.getString("BuildingPanelEVA.titledB.outside4")));
		listPanel.add(outsidePanel);

		// Create outsideListPanel 
		outsideListPanel = new UnitListPanel<>(desktop, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAwaitingOuterDoor());
			}
		};
		outsidePanel.add(outsideListPanel);
		
		// Create occupant panel
		JPanel occupantPanel = new JPanel(new BorderLayout());
//		occupantPanel.setSize(new Dimension(halfWidth, -1));
		occupantPanel.setBorder(BorderFactory.createTitledBorder(Msg.getString("BuildingPanelEVA.titledB.occupants")));
		listPanel.add(occupantPanel);

		// Create occupant list panel
		occupantListPanel = new UnitListPanel<>(desktop, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAllInsideOccupants());
			}
		};
		occupantPanel.add(occupantListPanel);

		// Create outside wait panel
		JPanel insidePanel = new JPanel(new BorderLayout());
//		insidePanel.setSize(new Dimension(halfWidth, -1));
		insidePanel.setBorder(BorderFactory.createTitledBorder(Msg.getString("BuildingPanelEVA.titledB.outside0")));
		listPanel.add(insidePanel);

		// Create insideListPanel 
		insideListPanel = new UnitListPanel<>(desktop, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getAwaitingInnerDoor());
			}
		};
		insidePanel.add(insideListPanel);
		
		// Create reservation panel
		JPanel reservationPanel = new JPanel(new BorderLayout());
//		reservationPanel.setSize(new Dimension(halfWidth, -1));
		addBorder(reservationPanel, Msg.getString("BuildingPanelEVA.titledB.Reserved"));
		listPanel.add(reservationPanel);
		
		reservationListPanel = new UnitListPanel<>(desktop, new Dimension(100, 100)) {
			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(buildingAirlock.getReserved());
			}		
		};	
		reservationPanel.add(reservationListPanel);
	}

	@Override
	public void update() {

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

	@Override
	public void destroy() {
		super.destroy();

		occupantListPanel = null;
		outsideListPanel = null;
		insideListPanel = null;
		reservationListPanel = null;
		
		eva = null;
		buildingAirlock = null;
		
		airlockModeCache = null;

		innerDoorLabel = null;
		outerDoorLabel = null;
		occupiedLabel = null;
		emptyLabel = null;
		operatorLabel = null;
		airlockStateLabel = null;
		activationLabel = null;
		transitionLabel = null;
		cycleTimeLabel = null;
		innerDoorStateLabel = null;
		outerDoorStateLabel = null;
		airlockModeLabel = null;
	}
}