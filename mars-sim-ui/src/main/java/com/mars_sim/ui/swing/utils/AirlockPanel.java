/**
 * Mars Simulation Project
 * AirlockPanel.java
 * @date 2026-05-10
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Airlock;
import com.mars_sim.core.structure.Airlock.AirlockMode;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JDoubleLabel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.utils.model.GenericPersonModel;

/**
 * The AirlockPanel class presents the airlock information and activities.
 * It supports the ability to be updated on a ClockPulse.
 */
public class AirlockPanel extends JPanel implements TemporalComponent{

    private Airlock airlock;

	private static final String UNLOCKED = "Unlocked";
	private static final String LOCKED = "Locked";
	
	private boolean activationCache;
	private boolean transitionCache;
	
	private String operatorCache = "";
	private String airlockStateCache = "";
	private String innerDoorStateCache = "";
	private String outerDoorStateCache = "";
	
	private static final String WIKI_URL = "https://github.com/mars-sim/mars-sim/wiki/Airlock";
	
	private AirlockMode airlockModeCache;

	private JIntegerLabel innerDoorLabel;
	private JIntegerLabel outerDoorLabel;
	private JIntegerLabel occupiedLabel;
	private JIntegerLabel emptyLabel;
	private JLabel operatorLabel;
	private JLabel airlockStateLabel;
	private JLabel activationLabel;
	private JLabel transitionLabel;
	private JDoubleLabel cycleTimeLabel;
	private JLabel innerDoorStateLabel;
	private JLabel outerDoorStateLabel;
	private JLabel airlockModeLabel;

	private EVAModel outsideModel;
	private EVAModel occupantModel;
	private EVAModel reservationModel;
	private EVAModel insideModel;

    public AirlockPanel(Airlock airlock, UIContext context) {
        super(new BorderLayout());

        this.airlock = airlock;
 
		add(createWikiPanel(), BorderLayout.NORTH);
		
        add(createDetailsPanel(), BorderLayout.CENTER);

        add(createZonePanel(context), BorderLayout.SOUTH);
    }

    private JPanel createDetailsPanel() {
       
        // Create top panel
        JPanel topPanel = new JPanel(new BorderLayout());

        AttributePanel westGrid = new AttributePanel(6, 1);
		topPanel.add(westGrid, BorderLayout.CENTER);
        
		AttributePanel eastGrid = new AttributePanel(6, 1);
		topPanel.add(eastGrid, BorderLayout.EAST);	
		
		if (airlock.isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = eastGrid.addTextField( Msg.getString("AirlockPanel.innerDoor.state"),
										   innerDoorStateCache, null);

		// Create innerDoorLabel
		innerDoorLabel = new JIntegerLabel(airlock.getNumAwaitingInnerDoor());
		westGrid.addLabelledItem(Msg.getString("AirlockPanel.innerDoor.number"), innerDoorLabel, null);

		if (airlock.isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = eastGrid.addTextField(Msg.getString("AirlockPanel.outerDoor.state"),
										   outerDoorStateCache, null);
	
		// Create outerDoorLabel
		outerDoorLabel = new JIntegerLabel(airlock.getNumAwaitingOuterDoor());
		westGrid.addLabelledItem(Msg.getString("AirlockPanel.outerDoor.number"), outerDoorLabel, null);
		// Create airlockModeLabel
		airlockModeCache = airlock.getAirlockMode();
		
		airlockModeLabel = eastGrid.addTextField(Msg.getString("AirlockPanel.airlock.mode"),
				airlockModeCache.getName(), null);

		// Create occupiedLabel
		occupiedLabel = new JIntegerLabel(airlock.getNumInChamber());
		westGrid.addLabelledItem(Msg.getString("AirlockPanel.occupied"), occupiedLabel, null);
		// Create airlockStateLabel
		airlockStateLabel = eastGrid.addTextField( Msg.getString("AirlockPanel.airlock.state"),
								airlock.getState().toString(), null);

		// Create emptyLabel
		emptyLabel = new JIntegerLabel(airlock.getNumEmptied());
		westGrid.addLabelledItem(Msg.getString("AirlockPanel.empty"), emptyLabel, null);
		// Create transitionLabel
		transitionLabel = eastGrid.addTextField( Msg.getString("AirlockPanel.airlock.transition"),
				getBoolean(airlock.isTransitioning()), null);

		// Create cycleTimeLabel
		cycleTimeLabel = new JDoubleLabel(StyleManager.DECIMAL_PLACES1, airlock.getRemainingCycleTime());
		westGrid.addLabelledItem(Msg.getString("AirlockPanel.airlock.cycleTime"), cycleTimeLabel);

		// Create OperatorLabel
		operatorLabel = eastGrid.addTextField( Msg.getString("AirlockPanel.operator"),
									 airlock.getOperatorName(), null);

		// Create activationLabel
		activationLabel = westGrid.addTextField( Msg.getString("AirlockPanel.airlock.activation"),
				getBoolean(airlock.isActivated()), null);

        return topPanel;
    }

	/**
	 * Models the persons in the airlock.
	 */
	private abstract class EVAModel extends GenericPersonModel {

		EVAModel() {
			super(NAME, TASK);
		}

		public void update() {
			setEntities(getData());
		}

		protected abstract Collection<Person> getData();
	}

    private JPanel createZonePanel(UIContext context) {
		// Create gridPanel
		JPanel gridPanel = new JPanel(new GridLayout(2, 2));

		var listSize = new Dimension(200, 115);

		outsideModel = new EVAModel() {
			protected Collection<Person> getData() {
				return getPersonsFromIds(context, airlock.getAwaitingOuterDoor());
			}
		};
		gridPanel.add(SwingHelper.createScrolledTable(outsideModel, context, Msg.getString("AirlockPanel.outside"), listSize));

		// Create occupant list panel
		occupantModel = new EVAModel() {
			protected Collection<Person> getData() {
				return getPersonsFromIds(context, airlock.getAllInsideOccupants());
			}
		};
		gridPanel.add(SwingHelper.createScrolledTable(occupantModel, context, Msg.getString("AirlockPanel.within"), listSize));

		// Create insideListPanel 
		insideModel = new EVAModel() {
			@Override
			protected Collection<Person> getData() {
				return getPersonsFromIds(context, airlock.getAwaitingInnerDoor());
			}
		};
		gridPanel.add(SwingHelper.createScrolledTable(insideModel, context, Msg.getString("AirlockPanel.inside"), listSize));
		
		// Create reservation panel
		reservationModel = new EVAModel() {
			@Override
			protected Collection<Person> getData() {
				return getPersonsFromIds(context, airlock.getReserved());
			}		
		};	
		gridPanel.add(SwingHelper.createScrolledTable(reservationModel, context, Msg.getString("AirlockPanel.reserved"), listSize));
		return gridPanel;
    }

    private JPanel createWikiPanel() {
		JPanel wikiPanel = new JPanel(new FlowLayout());
		JButton wikiButton = new JButton(GuideWindow.wikiIcon);
		wikiPanel.add(wikiButton);
		wikiButton.setToolTipText("Open Airlock Wiki in GitHub");
		wikiButton.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));
		return wikiPanel;
    }

	private List<Person> getPersonsFromIds(UIContext context, Collection<Integer> ids) {
		var um = context.getSimulation().getUnitManager();
		return ids.stream()
			.map(um::getPersonByID)
			.filter(p -> p != null)
			.toList();
	}

    /**
	 * Update airlock counts
	 */
	@Override
	public void clockUpdate(ClockPulse pulse) {

		innerDoorLabel.setValue(airlock.getNumAwaitingInnerDoor());
		outerDoorLabel.setValue(airlock.getNumAwaitingOuterDoor());
		occupiedLabel.setValue(airlock.getNumInChamber());
		emptyLabel.setValue(airlock.getNumEmptied());

		// Update operatorLabel
		String name = airlock.getOperatorName();
		if (!operatorCache.equalsIgnoreCase(name)) {
			operatorCache = name;
			operatorLabel.setText(name);
		}

		// Update airlockStateLabel
		String state = airlock.getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(state);
		}
		
		// Update activationLabel
		boolean activated = airlock.isActivated();
		if (activationCache != activated) {
			activationCache = activated;
			activationLabel.setText(getBoolean(activated));
		}

		// Update transitionLabel
		boolean transition = airlock.isTransitioning();
		if (transitionCache != transition) {
			transitionCache = transition;
			transitionLabel.setText(getBoolean(transition));
		}
		
		// Update airlockModeLabel
		AirlockMode airlockMode = airlock.getAirlockMode();
		if (airlockModeCache != airlockMode) {
			airlockModeCache = airlockMode;
			airlockModeLabel.setText(airlockMode.getName());
		}
		
		// Update cycleTimeLabel
		cycleTimeLabel.setValue(airlock.getRemainingCycleTime());

		String innerDoorState = (airlock.isInnerDoorLocked() ?
											LOCKED : UNLOCKED);

		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(innerDoorState);
		}

		String outerDoorState = (airlock.isOuterDoorLocked() ?
											LOCKED : UNLOCKED);

		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(outerDoorState);
		}
		
        // Update occupant list
        occupantModel.update();
        outsideModel.update();
    	insideModel.update();
        reservationModel.update();
    }

	/**
	 * Drop listeners to prevent memory leaks when panel is closed.
	 */
	public void unregister() {
		outsideModel.unregister();
		occupantModel.unregister();
		insideModel.unregister();
		reservationModel.unregister();
	}
	
    private static String getBoolean(boolean value) {
        return value ? "Yes" : "No";
    }
}
