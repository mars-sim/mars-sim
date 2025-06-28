/*
 * Mars Simulation Project
 * MainDetailPanel.java
 * @date 2024-07-12
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitEvent;
import com.mars_sim.core.UnitEventType;
import com.mars_sim.core.UnitListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.mission.MissionObjective;
import com.mars_sim.core.mission.objectives.CollectResourceObjective;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.mission.objectives.FieldStudyObjectives;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Delivery;
import com.mars_sim.core.person.ai.mission.EmergencySupply;
import com.mars_sim.core.person.ai.mission.Mining;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionEventType;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.person.ai.mission.SalvageMission;
import com.mars_sim.core.person.ai.mission.Trade;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.core.vehicle.VehicleType;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.mission.objectives.CollectResourcePanel;
import com.mars_sim.ui.swing.tool.mission.objectives.ExplorationPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.FieldStudyPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The tab panel for showing mission details.
 */
@SuppressWarnings("serial")
public class MainDetailPanel extends JPanel implements MissionListener, UnitListener {

	// Custom mission panel IDs.
	private static final String EMPTY = Msg.getString("MainDetailPanel.empty"); //$NON-NLS-1$

	private static final int MAX_LENGTH = 48;
	private static final int WIDTH = 250;
	private static final int MEMBER_HEIGHT = 125;
	private static final int LOG_HEIGHT = 125;
	
	// Private members
	private JLabel vehicleStatusLabel;
	private JLabel speedLabel;
	private JLabel distanceNextNavLabel;
	private JLabel traveledLabel;
	
	private JLabel typeTextField;
	private JLabel designationTextField;
	private EntityLabel settlementTextField;
	private EntityLabel leadTextField;
	private JLabel phaseTextField;
	private JLabel statusTextField;
	
	private MemberTableModel memberTableModel;

	private CardLayout customPanelLayout;

	private JPanel missionCustomPane;
	private JScrollPane memberPane;
	private JPanel memberOuterPane;
	
	private Mission missionCache;
	private Vehicle currentVehicle;
	private MissionWindow missionWindow;
	private MainDesktopPane desktop;

	private Map<String, MissionCustomInfoPanel> customInfoPanels;

	private LogTableModel logTableModel;

	private JTabbedPane objectivesPane;

	private EntityLabel vehicleLabel;


	/**
	 * Constructor.
	 *
	 * @param desktop the main desktop panel.
	 */
	public MainDetailPanel(MainDesktopPane desktop, MissionWindow missionWindow) {
		// User JPanel constructor.
		super();
		// Initialize data members.
		this.desktop = desktop;
		this.missionWindow = missionWindow;
		
		// Set the layout.
		setLayout(new BorderLayout());
        setMinimumSize(new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, MissionWindow.HEIGHT));
        
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		// Create the main panel.
		JPanel mainBox = new JPanel(new BorderLayout(1, 1));
		scrollPane.setViewportView(mainBox);

		// Create the top box.
		JPanel topBox = new JPanel(new BorderLayout(1, 1));
		mainBox.add(topBox, BorderLayout.NORTH);

		// Create the center box.
		JPanel centerBox = new JPanel(new BorderLayout(1, 1));
		mainBox.add(centerBox, BorderLayout.CENTER);

		// Create the member panel.
		JPanel bottomBox = new JPanel(new BorderLayout(1, 1));
		mainBox.add(bottomBox, BorderLayout.SOUTH);

		topBox.add(initMissionPane(), BorderLayout.CENTER);
		topBox.add(initLogPane(), BorderLayout.SOUTH);

		centerBox.add(initTravelPane(), BorderLayout.CENTER);

		memberOuterPane = new JPanel(new BorderLayout(1, 1));
			
		memberPane = initMemberPane();
		memberOuterPane.add(memberPane, BorderLayout.CENTER);
				
		bottomBox.add(memberOuterPane, BorderLayout.NORTH);

		objectivesPane = initObjectivePane();
		bottomBox.add(objectivesPane, BorderLayout.SOUTH);
		
		// Update the log table model
		logTableModel.update();
	}

	/**
	 * Initializes the mission pane.
	 * 
	 * @return
	 */
	private JPanel initMissionPane() {

		// Create the vehicle pane.
		JPanel missionLayout = new JPanel(new BorderLayout());
		Border blackline = StyleManager.createLabelBorder("Profile");
		missionLayout.setBorder(blackline);
	
		// Prepare count spring layout panel.
		AttributePanel missionPanel = new AttributePanel();
		missionLayout.add(missionPanel, BorderLayout.NORTH);
		
		typeTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.column.name"), "", null);
		phaseTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.phase"), "", null);
		designationTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.designation"), "",null);
		settlementTextField = new EntityLabel(desktop);
		missionPanel.addLabelledItem(Msg.getString("MainDetailPanel.settlement"), settlementTextField);

		leadTextField = new EntityLabel(desktop);
		missionPanel.addLabelledItem(Msg.getString("MainDetailPanel.startingMember"), leadTextField);
		statusTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.missionStatus"), "", null);
		
		return missionLayout;
	}
	
	/**
	 * Initializes the travel pane.
	 * 
	 * @return
	 */
	private JPanel initTravelPane() {
		
		JPanel mainLayout = new JPanel(new BorderLayout());
		mainLayout.setAlignmentX(CENTER_ALIGNMENT);
		mainLayout.setAlignmentY(CENTER_ALIGNMENT);
		Border blackline = StyleManager.createLabelBorder("Travel");
		mainLayout.setBorder(blackline);
		
		// Prepare travel grid layout.
		AttributePanel travelGridPane = new AttributePanel();
		mainLayout.add(travelGridPane, BorderLayout.CENTER);

		vehicleLabel = new EntityLabel(desktop);
		travelGridPane.addLabelledItem("Vehicle", vehicleLabel);
		vehicleStatusLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.vehicleStatus"), "", null);
		speedLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.vehicleSpeed"), "", null);
		distanceNextNavLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.distanceNextNavPoint"), "", null);
		traveledLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.distanceTraveled"), "", null);

		return mainLayout;
	}

	/**
	 * Initializes the phase log pane.
	 * 
	 * @return
	 */
	private Component initLogPane() {

		// Create member table model.
		logTableModel = new LogTableModel();

		// Create member table.
		JTable logTable = new JTable(logTableModel);
		logTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(150);

		var scroller = StyleManager.createScrollBorder("Phase Log", logTable);
		var dim = new Dimension(WIDTH, LOG_HEIGHT);
		scroller.setPreferredSize(dim);
		scroller.setMinimumSize(dim);
		return scroller;
	}

	/**
	 * Initializes the member pane.
	 * 
	 * @return
	 */
	private JScrollPane initMemberPane() {
		
		if (memberPane == null) {		
			// Create member table model.
			memberTableModel = new MemberTableModel();
	
			// Create member table.
			var memberTable = new JTable(memberTableModel);
			memberTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			memberTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			memberTable.getColumnModel().getColumn(2).setPreferredWidth(20);
			memberTable.getColumnModel().getColumn(3).setPreferredWidth(20);
			memberTable.setRowSelectionAllowed(true);
			memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			EntityLauncher.attach(memberTable, desktop);

			memberPane = StyleManager.createScrollBorder("Team Mambers", memberTable);
			var dim = new Dimension(WIDTH, MEMBER_HEIGHT);
			memberPane.setPreferredSize(dim);
			memberPane.setMinimumSize(dim);

		}
		
		return memberPane;
	}

	/**
	 * Initializes the objective pane.
	 *
	 * @return
	 */
 	private JTabbedPane initObjectivePane() {	
		// Create the objective panel.
		var objectivePane = new JTabbedPane();
		objectivePane.setBorder(StyleManager.createLabelBorder("Objectives"));

		// First tab is legacy; this will be removed
		objectivePane.addTab("Legacy", initCustomMissionPane());

		return objectivePane;
	}

	/**
	 * Initializes the custom mission pane.
	 * 
	 * @return
	 */
	private JPanel initCustomMissionPane() {

		// Create the mission custom panel.
		customPanelLayout = new CardLayout(10, 10);
		missionCustomPane = new JPanel(customPanelLayout);
		missionCustomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		
		// Create custom empty panel.
		JPanel emptyCustomPanel = new JPanel();
		missionCustomPane.add(emptyCustomPanel, EMPTY);
		customInfoPanels = new HashMap<>();

		// Create custom delivery mission panel.
		MissionCustomInfoPanel deliveryPanel = new DeliveryMissionCustomInfoPanel();
		String deliveryMissionName = Delivery.class.getName();
		customInfoPanels.put(deliveryMissionName, deliveryPanel);
		missionCustomPane.add(deliveryPanel, deliveryMissionName);

		// Create custom trade mission panel.
		MissionCustomInfoPanel tradePanel = new TradeMissionCustomInfoPanel();
		String tradeMissionName = Trade.class.getName();
		customInfoPanels.put(tradeMissionName, tradePanel);
		missionCustomPane.add(tradePanel, tradeMissionName);

		// Create custom mining mission panel.
		MissionCustomInfoPanel miningPanel = new MiningMissionCustomInfoPanel(desktop);
		String miningMissionName = Mining.class.getName();
		customInfoPanels.put(miningMissionName, miningPanel);
		missionCustomPane.add(miningPanel, miningMissionName);

		// Create custom construction mission panel.
		MissionCustomInfoPanel constructionPanel = new ConstructionMissionCustomInfoPanel(desktop);
		String constructionMissionName = ConstructionMission.class.getName();
		customInfoPanels.put(constructionMissionName, constructionPanel);
		missionCustomPane.add(constructionPanel, constructionMissionName);

		// Create custom salvage mission panel.
		MissionCustomInfoPanel salvagePanel = new SalvageMissionCustomInfoPanel(desktop);
		String salvageMissionName = SalvageMission.class.getName();
		customInfoPanels.put(salvageMissionName, salvagePanel);
		missionCustomPane.add(salvagePanel, salvageMissionName);

		// Create custom rescue/salvage vehicle mission panel.
		MissionCustomInfoPanel rescuePanel = new RescueMissionCustomInfoPanel(desktop);
		String rescueMissionName = RescueSalvageVehicle.class.getName();
		customInfoPanels.put(rescueMissionName, rescuePanel);
		missionCustomPane.add(rescuePanel, rescueMissionName);

		// Create custom emergency supply mission panel.
		MissionCustomInfoPanel emergencySupplyPanel = new EmergencySupplyMissionCustomInfoPanel();
		String emergencySupplyMissionName = EmergencySupply.class.getName();
		customInfoPanels.put(emergencySupplyMissionName, emergencySupplyPanel);
		missionCustomPane.add(emergencySupplyPanel, emergencySupplyMissionName);

		return missionCustomPane;
	}


	
	public void setCurrentMission(Mission mission) {
		if (missionCache != null) {
			if (!missionCache.equals(mission)) {
				missionCache = mission;
			}
		}
		else {
			missionCache = mission;
		}
	}
	
	public Mission getCurrentMission() {
		return missionCache;
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 *
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receive {@link ChangeEvent}
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
	    Objects.requireNonNull(text);
	    Objects.requireNonNull(changeListener);
	    DocumentListener dl = new DocumentListener() {
	        private int lastChange = 0;
			private int lastNotifiedChange = 0;

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void removeUpdate(DocumentEvent e) {
	            changedUpdate(e);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	            lastChange++;
	            SwingUtilities.invokeLater(() -> {
	                if (lastNotifiedChange != lastChange) {
	                    lastNotifiedChange = lastChange;
	                    changeListener.stateChanged(new ChangeEvent(text));
	                }
	            });
	        }
	    };
	    text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
	        Document d1 = (Document)e.getOldValue();
	        Document d2 = (Document)e.getNewValue();
	        if (d1 != null) d1.removeDocumentListener(dl);
	        if (d2 != null) d2.addDocumentListener(dl);
	        dl.changedUpdate(null);
	    });
	    Document d = text.getDocument();
	    if (d != null) d.addDocumentListener(dl);
	}

	/**
	 * Sets to the given mission.
	 *
	 * @param newMission
	 */
	public void setMission(Mission newMission) {
		// Remove this as previous mission listener.
		if (missionCache != null)
			missionCache.removeMissionListener(this);

		if (newMission == null) {	
			clearInfo();
			return;
		}
				
		missionCache = newMission;
		
		// Add this as listener for new mission.
		newMission.addMissionListener(this);
		
		setCurrentMission(newMission);
		// Update info on Main tab
		updateMainTab(newMission);
		// Update custom mission panel.
		updateCustomPanel(newMission);
	}


	/**
	 * Updates the mission content on the Main tab.
	 *
	 * @param mission
	 */
	public void updateMainTab(Mission mission) {

		if (mission == null || missionCache == null) {	
			clearInfo();
			return;
		}

		if (mission.isDone()) {
			// Check if the mission is done and the members have been disbanded
			memberOuterPane.removeAll();
				
			var memberLabel = new JLabel(printMembers(mission), SwingConstants.LEFT);
			memberLabel.setBorder(StyleManager.createLabelBorder("Disbanded Members"));
			memberOuterPane.add(memberLabel);
		}
		else {
			memberOuterPane.removeAll();
			memberOuterPane.add(initMemberPane());
			memberTableModel.setMission(mission);
		}
		
		String d = mission.getFullMissionDesignation();
		if (d == null || d.equals(""))
			d = "";
		designationTextField.setText(d);
		typeTextField.setText(mission.getName());
		
		leadTextField.setEntity(mission.getStartingPerson());

		String phaseText = mission.getPhaseDescription();
		phaseTextField.setToolTipText(phaseText);
		phaseTextField.setText(Conversion.trim(phaseText, MAX_LENGTH));

		var missionStatusText = new StringBuilder();
		missionStatusText.append(mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
		statusTextField.setText(missionStatusText.toString());
		
		settlementTextField.setEntity(mission.getAssociatedSettlement());

		logTableModel.setMission(mission);
		logTableModel.update();

		
		// Update mission vehicle info in UI.
		if (mission instanceof VehicleMission vehicleMission) {
			Vehicle vehicle = vehicleMission.getVehicle();
			vehicleLabel.setEntity(vehicle);

			if (vehicle != null && !mission.isDone()) {
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(StyleManager.DECIMAL_KPH.format(vehicle.getSpeed())); //$NON-NLS-1$
				int currentLegRemainingDist = (int) vehicleMission.getDistanceCurrentLegRemaining();
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(currentLegRemainingDist));

				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getTotalDistanceProposed()*10.0)/10.0;

				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						estTotalDistance
						));

				if (!vehicle.equals(currentVehicle)) {
					vehicle.addUnitListener(this);
					if (currentVehicle != null) {
						currentVehicle.removeUnitListener(this);
					}
					vehicle.addUnitListener(this);
					currentVehicle = vehicle;
				}
			}
			else {
				vehicleStatusLabel.setText(" ");
				speedLabel.setText(StyleManager.DECIMAL_KPH.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
		
				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getTotalDistanceProposed()*10.0)/10.0;

				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						estTotalDistance
						));
				
				if (currentVehicle != null) {
					currentVehicle.removeUnitListener(this);
				}
				currentVehicle = null;
			}
		} else if (mission instanceof ConstructionMission constructionMission) {
			// Display first of mission's list of construction vehicles.
			List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
			if (!constVehicles.isEmpty()) {
				Vehicle vehicle = constVehicles.get(0);
				vehicleLabel.setEntity(vehicle);
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(StyleManager.DECIMAL_KPH.format(vehicle.getSpeed())); //$NON-NLS-1$
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", "0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
				vehicle.addUnitListener(this);
				currentVehicle = vehicle;
			}
		}

		// Add mission listener.
		mission.addMissionListener(this);
		missionCache = mission;
	}


	/**
	 * Clears the mission content on the Main tab.
	 */
	public void clearInfo() {
		// NOTE: do NOT clear the mission info. Leave the info there for future viewing
		// Clear mission info in UI.
		leadTextField.setEntity(null);
		designationTextField.setText(" ");
		typeTextField.setText(" ");
		phaseTextField.setText(" ");
		phaseTextField.setToolTipText(" ");
		
		statusTextField.setText(" ");
		settlementTextField.setEntity(null);

		memberTableModel.setMission(null);
		
		logTableModel.update();
		logTableModel.setMission(null);
				
		vehicleStatusLabel.setText(" ");
		speedLabel.setText(StyleManager.DECIMAL_KPH.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
		distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
		traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", "0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (missionCache != null) {
			missionCache.removeMissionListener(this);
		}
		missionCache = null;
		
		if (currentVehicle != null)
			currentVehicle.removeUnitListener(this);
		currentVehicle = null;
		
		customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Prints the list of members.
	 * 
	 * @return
	 */
	private String printMembers(Mission mission) {
		Set<Worker> list = mission.getSignup();
		if (list.isEmpty()) {
			return "";
		}
		
		return list.stream().map(Worker::getName).collect(Collectors.joining(", "));
	}
	
	/**
	 * Updates the custom mission panel with a mission.
	 *
	 * @param mission the mission.
	 */
	private void updateCustomPanel(Mission mission) {
		boolean clearLegacy = false;
		// Drop old panels expecgt first one legacy
		while(objectivesPane.getTabCount() > 1) {
			var pan = objectivesPane.getComponentAt(1);
			if (pan instanceof ObjectivesPanel op) {
				op.unregister();
			}
			objectivesPane.removeTabAt(1);
		}

		if (mission != null) {

			// Add custom mission panel.
			for(MissionObjective o : mission.getObjectives()) {
				JPanel newPanel = switch(o) {
					case CollectResourceObjective cro -> new CollectResourcePanel(cro);
					case FieldStudyObjectives fso -> new FieldStudyPanel(fso, desktop);
					case ExplorationObjective eo -> new ExplorationPanel(eo);

					default -> null;
				};

				if (newPanel != null) {
					var dim = new Dimension(WIDTH, 300);
					objectivesPane.setMinimumSize(dim);
					objectivesPane.setPreferredSize(dim);
	 				objectivesPane.addTab(newPanel.getName(), newPanel);
					clearLegacy = true;
				}
			}

			if (!clearLegacy) {
				// Defautl back to legacy behaviour
				String missionClassName = mission.getClass().getName();
				if (customInfoPanels.containsKey(missionClassName)) {
					MissionCustomInfoPanel panel = customInfoPanels.get(missionClassName);
					customPanelLayout.show(missionCustomPane, missionClassName);
					panel.updateMission(mission);
				}
				else {
					clearLegacy = true;
				}
			}
		}

		if (clearLegacy)
			customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Mission event update.
	 */
	@Override
	public void missionUpdate(MissionEvent e) {
		if (e.getSource().equals(missionCache)) {
			SwingUtilities.invokeLater(new MissionEventUpdater(e, this));
		}
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	@Override
	public void unitUpdate(UnitEvent event) {
		if ((((Unit)event.getSource()).getUnitType() == UnitType.VEHICLE)
			&& event.getSource().equals(currentVehicle)) {
				SwingUtilities.invokeLater(new VehicleInfoUpdater(event));
		}
	}

	public void destroy() {
		memberTableModel = null;

		// Drop old panels
		for(int i = 0; i < objectivesPane.getTabCount(); i++) {
			var pan = objectivesPane.getComponentAt(i);
			if (pan instanceof ObjectivesPanel op) {
				op.unregister();
			}
		}
	}

	private class MissionEventUpdater implements Runnable {

		private MissionEvent event;
		private MainDetailPanel panel;

		private MissionEventUpdater(MissionEvent event, MainDetailPanel panel) {
			this.event = event;
			this.panel = panel;
		}

		@Override
		public void run() {
			Mission mission = (Mission) event.getSource();
			MissionEventType type = event.getType();

			// Update UI based on mission event type.
			switch(type) {
			case TYPE_EVENT, TYPE_ID_EVENT ->
				typeTextField.setText(mission.getName());
		
			case DESIGNATION_EVENT -> {
				// Implement the missing descriptionLabel
				if (missionWindow.getCreateMissionWizard() != null) {
					String s = mission.getFullMissionDesignation();
					if (s == null || s.equals("")) {
						s = "[TBA]";
					}

					designationTextField.setText(Conversion.capitalize(s));
				}
			}
			
			case PHASE_EVENT, PHASE_DESCRIPTION_EVENT -> {
				String phaseText = mission.getPhaseDescription();
				phaseTextField.setText(Conversion.trim(phaseText, MAX_LENGTH));
				
				// Update the log table model
				logTableModel.update();
			}

			case END_MISSION_EVENT -> {
				var missionStatusText = new StringBuilder();
				missionStatusText.append( mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
				statusTextField.setText(missionStatusText.toString());
			} 
			
			case ADD_MEMBER_EVENT, REMOVE_MEMBER_EVENT, MIN_MEMBERS_EVENT, CAPACITY_EVENT ->
				memberTableModel.updateMembers();

			case VEHICLE_EVENT -> {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				vehicleLabel.setEntity(vehicle);
				if (vehicle != null) {
					vehicleStatusLabel.setText(vehicle.printStatusTypes());
					speedLabel.setText(StyleManager.DECIMAL_KPH.format(vehicle.getSpeed())); //$NON-NLS-1$
					vehicle.addUnitListener(panel);
					currentVehicle = vehicle;
				} else {
					vehicleStatusLabel.setText("Not Applicable");
					speedLabel.setText(StyleManager.DECIMAL_KPH.format(0)); //$NON-NLS-1$
					if (currentVehicle != null)
						currentVehicle.removeUnitListener(panel);
					currentVehicle = null;
				}
			}
			
			case DISTANCE_EVENT -> {
				VehicleMission vehicleMission = (VehicleMission) mission;
				
				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getTotalDistanceProposed()*10.0)/10.0;
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						estTotalDistance
						));
				
				// Make sure to call getTotalDistanceTravelled first. 
				// It should be by default already been called in performPhase's TRAVELLING
				int distanceNextNav = (int) vehicleMission.getDistanceCurrentLegRemaining();
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(distanceNextNav));
				}
			}

			
			// Forward to any objective panels
			for(int i = 0; i < objectivesPane.getTabCount(); i++) {
				Component comp = objectivesPane.getComponentAt(i);
				if (comp instanceof MissionListener ul) {
					ul.missionUpdate(event);
				}
			}

			logTableModel.fireTableDataChanged();
		}
	}

	/**
	 * Inner class for updating vehicle info.
	 */
	private class VehicleInfoUpdater implements Runnable {

		private UnitEvent event;

		private VehicleInfoUpdater(UnitEvent event) {
			this.event = event;
		}

		public void run() {
			// Update vehicle info in UI based on event type.
			UnitEventType type = event.getType();
			Vehicle vehicle = (Vehicle) event.getSource();
			if (type == UnitEventType.STATUS_EVENT) {
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
			} else if (type == UnitEventType.SPEED_EVENT)
				speedLabel.setText(StyleManager.DECIMAL_KPH.format(vehicle.getSpeed())); //$NON-NLS-1$

			// Forward to any objective panels
			for(int i = 0; i < objectivesPane.getTabCount(); i++) {
				Component comp = objectivesPane.getComponentAt(i);
				if (comp instanceof UnitListener ul) {
					ul.unitUpdate(event);
				}
   			}
		}
	}

	
	/**
	 * Adapter for the mission log
	 */
	private class LogTableModel extends AbstractTableModel {
		
		private Mission mission;
		
		private List<MissionLog.MissionLogEntry> entries;
	
		/**
		 * Constructor.
		 */
		private LogTableModel() {
			mission = null;
			entries = new ArrayList<>();
		}

		public void update() {
			if (mission != null)
				entries = mission.getLog().getEntries();
		}
		
		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		public int getRowCount() {
			return (mission != null ? entries.size() : 0);
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Gets the column name at a given index.
		 *
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> "Date";
				case 1 -> "Entry";
				default -> null;
			};
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			if (mission == null || entries == null)
				return null;
				
			if (row < entries.size()) {
				if (column == 0)
					return entries.get(row).getTime().getTruncatedDateTimeStamp();
				else
					return entries.get(row).getEntry();
			}
			return null;
		}

		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			fireTableDataChanged();
		}
	}

	/**
	 * Table model for mission members.
	 */
	private class MemberTableModel extends AbstractTableModel implements UnitListener, EntityModel {

		// Private members.
		private Mission mission;
		private List<Worker> members;

		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			members = new ArrayList<>();
		}
		
		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		@Override
		public int getRowCount() {
			return members.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		@Override
		public int getColumnCount() {
			return 4;
		}

		/**
		 * Gets the column name at a given index.
		 *
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("MainDetailPanel.column.name");
				case 1 -> Msg.getString("MainDetailPanel.column.task");
				case 2 -> Msg.getString("MainDetailPanel.column.onboard");
				case 3 -> Msg.getString("MainDetailPanel.column.airlock");
				default -> null;
			};
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		@Override
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Worker member = members.get(row);
				return switch (column) {
					case 0 -> member.getName();
      				case 1 -> member.getTaskDescription();
      				case 2 -> isOnboard(member) ? "Y" : "N";
      				case 3 -> isInAirlock(member) ? "Y" : "N";
     				default -> null;
				};
			}
			return null;
		}

		/**
		 * Is this member currently onboard a rover ?
		 *
		 * @param member
		 * @return
		 */
		boolean isOnboard(Worker member) {
			if ((mission instanceof VehicleMission vm)
						&& (member instanceof Person p)) {
				Vehicle v = vm.getVehicle();
				if (VehicleType.isDrone(v.getVehicleType())) {
					return false;
				}
				else if (v instanceof Rover r && r.isCrewmember(p)) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Is this member currently in vehicle's airlock ?
		 *
		 * @param member
		 * @return
		 */
		boolean isInAirlock(Worker member) {
			if ((mission instanceof VehicleMission vm) 
				&& (member instanceof Person p)) {		
				Vehicle v = vm.getVehicle();
				if (VehicleType.isDrone(v.getVehicleType())) {
					return false;
				}
				else if (v instanceof Rover r && r.isInAirlock(p)) {
					return true;
				}	
			}
			return false;
		}
		
		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			updateMembers();
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = members.indexOf(member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		/**
		 * Updates mission members.
		 */
		void updateMembers() {
			
			if (mission != null) {
				
				clearMembers();
				members = new ArrayList<>(mission.getMembers());
				Iterator<Worker> i = members.iterator();
				while (i.hasNext()) {
					Worker member = i.next();
					member.addUnitListener(this);
				}
				SwingUtilities.invokeLater(new MemberTableUpdater());
			} else {
				if (!members.isEmpty()) {
					clearMembers();
					SwingUtilities.invokeLater(new MemberTableUpdater());
				}
			}
		}

		/**
		 * Clears all members from the table.
		 */
		private void clearMembers() {
			if (members != null) {
				Iterator<Worker> i = members.iterator();
				while (i.hasNext()) {
					Worker member = i.next();
					member.removeUnitListener(this);
				}
				members.clear();
			}
		}

		/**
		 * Inner class for updating member table.
		 */
		private class MemberTableUpdater implements Runnable {

			private int row;
			private int column;
			private boolean entireData;

			private MemberTableUpdater(int row, int column) {
				this.row = row;
				this.column = column;
				entireData = false;
			}

			private MemberTableUpdater() {
				entireData = true;
			}

			public void run() {
				if (entireData) {
					fireTableDataChanged();
				} else {
					fireTableCellUpdated(row, column);
				}
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return members.get(row);
		}
	}
}
