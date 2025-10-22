/*
 * Mars Simulation Project
 * MainDetailPanel.java
 * @date 2025-10-15
 * @author Scott Davis
 */

package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
import com.mars_sim.core.mission.objectives.ConstructionObjective;
import com.mars_sim.core.mission.objectives.EmergencySupplyObjective;
import com.mars_sim.core.mission.objectives.ExplorationObjective;
import com.mars_sim.core.mission.objectives.FieldStudyObjectives;
import com.mars_sim.core.mission.objectives.MiningObjective;
import com.mars_sim.core.mission.objectives.RescueVehicleObjective;
import com.mars_sim.core.mission.objectives.TradeObjective;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.mission.ConstructionMission;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionEventType;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.core.person.ai.mission.MissionLog;
import com.mars_sim.core.person.ai.mission.MissionStatus;
import com.mars_sim.core.person.ai.mission.VehicleMission;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Crewable;
import com.mars_sim.core.vehicle.GroundVehicle;
import com.mars_sim.core.vehicle.Rover;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.tool.mission.objectives.CollectResourcePanel;
import com.mars_sim.ui.swing.tool.mission.objectives.ConstructionPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.EmergencySupplyPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.ExplorationPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.FieldStudyPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.MiningPanel;
import com.mars_sim.ui.swing.tool.mission.objectives.RescuePanel;
import com.mars_sim.ui.swing.tool.mission.objectives.TradePanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.EntityLauncher;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * The main tab panel for showing mission  details.
 */
@SuppressWarnings("serial")
public class MainDetailPanel extends JPanel implements MissionListener, UnitListener {

	private static final int MAX_LENGTH = 48;
	private static final int OBJ_HEIGHT = 230;
	private static final int MEMBER_HEIGHT = 140;
	private static final int LOG_HEIGHT = 230;
	
	// Private members
	private JLabel vehicleStatusLabel;
	private JLabel speedLabel;
	private JLabel distanceNextNavLabel;
	private JLabel traveledLabel;
	private JLabel typeTextField;
	private JLabel designationTextField;
	private JLabel phaseTextField;
	private JLabel statusTextField;

	private JScrollPane memberScrollPane;
	private JPanel memberOuterPane;

	private JTabbedPane objectivesPane;
	
	private EntityLabel settlementTextField;
	private EntityLabel leadTextField;
	private EntityLabel vehicleLabel;
	
	private MemberTableModel memberTableModel;
	private LogTableModel logTableModel;

	private Mission missionCache;
	private Vehicle currentVehicle;
	private MissionWindow missionWindow;
	private MainDesktopPane desktop;


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
		JPanel mainBox = new JPanel();
		mainBox.setLayout(new BoxLayout(mainBox, BoxLayout.Y_AXIS));
		
		scrollPane.setViewportView(mainBox);

		mainBox.add(initProfilePane());
		
		Box.createVerticalGlue();
		
		mainBox.add(initLogPane());

		Box.createVerticalGlue();
		
		mainBox.add(initTravelPane());

		Box.createVerticalGlue();
		
		memberOuterPane = new JPanel(new BorderLayout());
			
		memberScrollPane = initMemberPane();
		memberOuterPane.add(memberScrollPane, BorderLayout.NORTH);
				
		mainBox.add(memberOuterPane, BorderLayout.NORTH);

		Box.createVerticalGlue();
		
		objectivesPane = initObjectivePane();
		mainBox.add(objectivesPane, BorderLayout.NORTH);
		
		// Update the log table model
		logTableModel.update();
	}

	/**
	 * Initializes the mission profile pane.
	 * 
	 * @return
	 */
	private JPanel initProfilePane() {

		// Create the profile pane.
		JPanel profileLayout = new JPanel();
		Border blackline = StyleManager.createLabelBorder("Profile");
		profileLayout.setBorder(blackline);
	
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel(6);
		profileLayout.add(attributePanel, BorderLayout.NORTH);
		
		typeTextField = attributePanel.addTextField(Msg.getString("MainDetailPanel.column.name"), "", null);
		phaseTextField = attributePanel.addTextField(Msg.getString("MainDetailPanel.phase"), "", null);
		designationTextField = attributePanel.addTextField(Msg.getString("MainDetailPanel.designation"), "",null);
		settlementTextField = new EntityLabel(desktop);
		attributePanel.addLabelledItem(Msg.getString("MainDetailPanel.settlement"), settlementTextField);

		leadTextField = new EntityLabel(desktop);
		attributePanel.addLabelledItem(Msg.getString("MainDetailPanel.startingMember"), leadTextField);
		statusTextField = attributePanel.addTextField(Msg.getString("MainDetailPanel.missionStatus"), "", null);
		
		return profileLayout;
	}
	
	/**
	 * Initializes the travel pane.
	 * 
	 * @return
	 */
	private JPanel initTravelPane() {
		
		JPanel travelLayout = new JPanel();
		Border blackline = StyleManager.createLabelBorder("Travel");
		travelLayout.setBorder(blackline);
		
		// Prepare attribute panel.
		AttributePanel attributePanel = new AttributePanel(5);
		travelLayout.add(attributePanel, BorderLayout.NORTH);

		vehicleLabel = new EntityLabel(desktop);
		attributePanel.addLabelledItem("Vehicle", vehicleLabel);
		vehicleStatusLabel = attributePanel.addTextField(Msg.getString("MainDetailPanel.vehicleStatus"), "", null);
		speedLabel = attributePanel.addTextField(Msg.getString("MainDetailPanel.vehicleSpeed"), "", null);
		distanceNextNavLabel = attributePanel.addTextField(Msg.getString("MainDetailPanel.distanceNextNavPoint"), "", null);
		traveledLabel = attributePanel.addTextField(Msg.getString("MainDetailPanel.distanceTraveled"), "", null);

		return travelLayout;
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
		logTable.getColumnModel().getColumn(0).setPreferredWidth(70);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(90);
		logTable.getColumnModel().getColumn(2).setPreferredWidth(70);
		
		var scroller = StyleManager.createScrollBorder("Phase Log", logTable);
		var dim = new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, LOG_HEIGHT);
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
		
		if (memberScrollPane == null) {		
			// Create member table model.
			memberTableModel = new MemberTableModel();
	
			// Create member table.
			var memberTable = new JTable(memberTableModel);
			memberTable.getColumnModel().getColumn(0).setPreferredWidth(70);
			memberTable.getColumnModel().getColumn(1).setPreferredWidth(100);
			memberTable.getColumnModel().getColumn(2).setPreferredWidth(20);
			memberTable.getColumnModel().getColumn(3).setPreferredWidth(20);
			memberTable.setRowSelectionAllowed(true);
			memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			EntityLauncher.attach(memberTable, desktop);

			memberScrollPane = StyleManager.createScrollBorder("Team Mambers", memberTable);
			var dim = new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, MEMBER_HEIGHT);
			memberScrollPane.setPreferredSize(dim);
			memberScrollPane.setMinimumSize(dim);

		}
		
		return memberScrollPane;
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

		return objectivePane;
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

		clearObjectives();
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
	
	private void clearObjectives() {
		while(objectivesPane.getTabCount() > 0) {
			var pan = objectivesPane.getComponentAt(0);
			if (pan instanceof ObjectivesPanel op) {
				op.unregister();
			}
			objectivesPane.removeTabAt(0);
		}
	}
	
	/**
	 * Updates the custom mission panel with a mission.
	 *
	 * @param mission the mission.
	 */
	private void updateCustomPanel(Mission mission) {
		// Drop old panels except first one
		clearObjectives();

		if (mission != null) {
			// Add custom mission panel.
			for(MissionObjective o : mission.getObjectives()) {
				JPanel newPanel = switch(o) {
					case CollectResourceObjective cro -> new CollectResourcePanel(cro);
					case FieldStudyObjectives fso -> new FieldStudyPanel(fso, desktop);
					case ExplorationObjective eo -> new ExplorationPanel(eo);
					case MiningObjective mo -> new MiningPanel(mo, desktop);
					case TradeObjective to -> new TradePanel(to, desktop);
					case ConstructionObjective co -> new ConstructionPanel(co, desktop);
					case RescueVehicleObjective ro -> new RescuePanel(ro, desktop);
					case EmergencySupplyObjective so -> new EmergencySupplyPanel(so, desktop);
					default -> null;
				};

				if (newPanel != null) {
					var dim = new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, OBJ_HEIGHT);
					objectivesPane.setMinimumSize(dim);
					objectivesPane.setPreferredSize(dim);
	 				objectivesPane.addTab(newPanel.getName(), newPanel);
					objectivesPane.setSelectedComponent(newPanel);
				}
			}
		}
	}

	/**
	 * Updates with a mission event.
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
			case TYPE_EVENT, MISSION_STRING_EVENT ->
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
				memberTableModel.updateOccupantList();

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
			
			default -> {}
				
			}

			
			// Forward to any objective panels
			for (int i = 0; i < objectivesPane.getTabCount(); i++) {
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
			if (mission != null) {
				entries = mission.getLog().getEntries();
			}
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
			return 3;
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
				case 2 -> "Logged by";
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
				else if (column == 1)
					return entries.get(row).getEntry();
				else
					return entries.get(row).getEnterBy();
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

		private static final String NAME = Msg.getString("MainDetailPanel.column.name");
		private static final String TASK = Msg.getString("MainDetailPanel.column.task");
		private static final String MEMBER = Msg.getString("MainDetailPanel.column.member");
		private static final String BOARDED = Msg.getString("MainDetailPanel.column.boarded");
		private static final String AIRLOCK =  Msg.getString("MainDetailPanel.column.airlock");
		
		// Private members.
		private Mission mission;
		private List<Worker> occupantList;
		private Vehicle v;
		private Crewable crewable;
		
		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			occupantList = new ArrayList<>();
		}

		/**
		 * Sets the mission for this table model.
		 *
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			if ((mission instanceof VehicleMission vm)) {
				v = vm.getVehicle();
				crewable = (Crewable)v; 
				updateOccupantList();
			}
		}
		
		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		@Override
		public int getRowCount() {
			return occupantList.size();
		}

		/**
		 * Gets the column count.
		 *
		 * @return column count.
		 */
		@Override
		public int getColumnCount() {
			return 5;
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
				case 0 -> NAME;
				case 1 -> TASK;
				case 2 -> MEMBER;
				case 3 -> BOARDED;
				case 4 -> AIRLOCK;
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
			if (row < occupantList.size()) {
				Worker member = occupantList.get(row);
				return switch (column) {
					case 0 -> member.getName();
      				case 1 -> member.getTaskDescription();
      				case 2 -> isMissionMember(member) ? "Y" : "N";
      				case 3 -> boarded(member) ? "Y" : "N";
      				case 4 -> isInAirlock(member) ? "Y" : "N";
     				default -> null;
				};
			}
			return null;
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean boarded(Worker member) {
			if (member instanceof Person p) {
				return (crewable.isCrewmember(p));
			}
			return false;
		}
		
		/**
		 * Is this occupant a mission member ?
		 *
		 * @param member
		 * @return
		 */
		boolean isMissionMember(Worker member) {
			if (mission != null && mission.getMembers().contains(member))
				return true;
			return false;
		}
		
		/**
		 * Is this member currently in vehicle's airlock ?
		 *
		 * @param member
		 * @return
		 */
		boolean isInAirlock(Worker member) {
			if (member instanceof Person p		
				&& v instanceof Rover r && r.isInAirlock(p)) {
				return true;
			}
			return false;
		}

		/**
		 * Catches unit update event.
		 *
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			Worker member = (Worker) event.getSource();
			int index = occupantList.indexOf(member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		/**
		 * Updates the occupant list.
		 */
		void updateOccupantList() {
			List<Worker> newList = new ArrayList<>(crewable.getCrew());
			if (mission != null) {
				for (Worker w: mission.getMembers()) {
					if (!newList.contains(w)) {
						newList.add(w);
					}
				}
			}

			if (!occupantList.equals(newList)) {
				final var fixedList = newList;
				// Existing members, not in the new list then remove listener
				occupantList.stream()
						.filter(m -> !fixedList.contains(m))
						.forEach(mm -> mm.removeUnitListener(this));

				// New members, not in the existing list then add listener
				newList.stream()
						.filter(m -> !occupantList.contains(m))
						.forEach(mm -> mm.addUnitListener(this));

				// Replace the old member list with new one.
				occupantList = newList;

				// Update this row
				SwingUtilities.invokeLater(new MemberTableUpdater());
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
			       	if (row >= 0 && row < getRowCount()) {
		                fireTableCellUpdated(row, column);
		            }
				}
			}
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return occupantList.get(row);
		}
	}
}
