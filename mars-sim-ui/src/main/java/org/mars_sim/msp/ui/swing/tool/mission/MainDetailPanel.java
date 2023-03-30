/*
 * Mars Simulation Project
 * MainDetailPanel.java
 * @date 2022-07-31
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.AbstractVehicleMission;
import org.mars_sim.msp.core.person.ai.mission.AreologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BiologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.Delivery;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupply;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.MeteorologyFieldStudy;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionLog;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

/**
 * The tab panel for showing mission details.
 */
@SuppressWarnings("serial")
public class MainDetailPanel extends JPanel implements MissionListener, UnitListener {

	// Custom mission panel IDs.
	private static final String EMPTY = Msg.getString("MainDetailPanel.empty"); //$NON-NLS-1$

	private static final int MAX_LENGTH = 48;
	private static final int HEIGHT_1 = 150;
	
	// Private members
	private JLabel vehicleStatusLabel;
	private JLabel speedLabel;
	private JLabel distanceNextNavLabel;
	private JLabel traveledLabel;
	
	private JLabel typeTextField;
	private JLabel designationTextField;
	private JLabel settlementTextField;
	private JLabel leadTextField;
	private JLabel phaseTextField;
	private JLabel statusTextField;
	
	private JLabel memberLabel = new JLabel("", SwingConstants.LEFT);
	
	private MemberTableModel memberTableModel;
	private JTable memberTable;

	private JButton centerMapButton;
	private JButton vehicleButton;

	private CardLayout customPanelLayout;

	private JPanel missionCustomPane;
	private JPanel memberPane;
	private JPanel memberOuterPane;
	
	private Mission missionCache;
	private Vehicle currentVehicle;
	private MissionWindow missionWindow;
	private MainDesktopPane desktop;

	private Map<String, MissionCustomInfoPanel> customInfoPanels;

	private LogTableModel logTableModel;


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
        setMaximumSize(new Dimension(MissionWindow.WIDTH - MissionWindow.LEFT_PANEL_WIDTH, MissionWindow.HEIGHT));
        
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		// Create the main panel.
		JPanel mainBox = new JPanel(new BorderLayout(1, 1));
		mainBox.setBorder(new MarsPanelBorder());
		scrollPane.setViewportView(mainBox);

		// Create the top box.
		JPanel topBox = new JPanel(new BorderLayout(1, 1));
		topBox.setBorder(new MarsPanelBorder());
		mainBox.add(topBox, BorderLayout.NORTH);

		// Create the center box.
		JPanel centerBox = new JPanel(new BorderLayout(1, 1));
		centerBox.setBorder(new MarsPanelBorder());
		mainBox.add(centerBox, BorderLayout.CENTER);

		// Create the member panel.
		JPanel bottomBox = new JPanel(new BorderLayout(1, 1));
		mainBox.add(bottomBox, BorderLayout.SOUTH);

		topBox.add(initMissionPane(), BorderLayout.CENTER);
		topBox.add(initLogPane(), BorderLayout.SOUTH);

		centerBox.add(initVehiclePane(), BorderLayout.NORTH);
		centerBox.add(initTravelPane(), BorderLayout.CENTER);

		memberOuterPane = new JPanel(new BorderLayout(1, 1));
		Border blackline = StyleManager.createLabelBorder("Team Members");
		memberOuterPane.setBorder(blackline);
			
		memberPane = initMemberPane();
		memberOuterPane.add(memberPane, BorderLayout.CENTER);
				
		bottomBox.add(memberOuterPane, BorderLayout.NORTH);
		bottomBox.add(initCustomMissionPane(), BorderLayout.SOUTH);
	}

	private JPanel initMissionPane() {

		// Create the vehicle pane.
		JPanel missionLayout = new JPanel(new BorderLayout());
		Border blackline = StyleManager.createLabelBorder("Profile");
		missionLayout.setBorder(blackline);
	
		// Prepare count spring layout panel.
		AttributePanel missionPanel = new AttributePanel(6);
		missionLayout.add(missionPanel, BorderLayout.NORTH);
		
		typeTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.type"), "", null); // $NON-NLS-1$
		designationTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.designation"), "",null); // $NON-NLS-1$
		settlementTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.settlement"), "", null); // $NON-NLS-1$
		leadTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.startingMember"), "", null); // $NON-NLS-1$
		phaseTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.phase"), "", null); // $NON-NLS-1$
		statusTextField = missionPanel.addTextField(Msg.getString("MainDetailPanel.missionStatus"), "", null); // $NON-NLS-1$

		
		return missionLayout;
	}
	
	private JPanel initVehiclePane() {

		// Create the vehicle grid panel.
		JPanel vehicleLayout = new JPanel(new GridLayout(1, 2));
		
		// Create the vehicle pane.
		JPanel vehiclePane = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		vehiclePane.setAlignmentX(Component.BOTTOM_ALIGNMENT);
		vehicleLayout.add(vehiclePane);

		// Create center map button
		centerMapButton = new JButton(ImageLoader.getIconByName("mars")); //$NON-NLS-1$
		centerMapButton.setMargin(new Insets(2, 2, 2, 2));
		centerMapButton.addActionListener(e -> {
			if (missionCache != null)
				getDesktop().centerMapGlobe(missionCache.getCurrentMissionLocation());
		});
		centerMapButton.setToolTipText(Msg.getString("MainDetailPanel.gotoMarsMap")); //$NON-NLS-1$
		centerMapButton.setEnabled(false);
		vehiclePane.add(centerMapButton);

		// Create the vehicle label.
		JLabel vehicleLabel = new JLabel(" " + Msg.getString("MainDetailPanel.vehicle"), SwingConstants.LEFT); //$NON-NLS-1$
		vehiclePane.add(vehicleLabel);

		// Create the vehicle panel.
		vehicleButton = new JButton("");
		vehicleButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		vehicleButton.setVisible(false);
		vehicleButton.addActionListener(e -> {
			if (MissionType.isVehicleMission(missionCache.getMissionType())) {
				// Open window for vehicle.
				VehicleMission vehicleMission = (VehicleMission) missionCache;
				Vehicle vehicle = vehicleMission.getVehicle();
				if (vehicle != null) {
					getDesktop().openUnitWindow(vehicle, false);
				}
			} else if (missionCache.getMissionType() == MissionType.BUILDING_CONSTRUCTION) {
				BuildingConstructionMission constructionMission = (BuildingConstructionMission) missionCache;
				if (!constructionMission.getConstructionVehicles().isEmpty()) {
					Vehicle vehicle = constructionMission.getConstructionVehicles().get(0);
					getDesktop().openUnitWindow(vehicle, false);
				}
			} else if (missionCache.getMissionType() == MissionType.BUILDING_SALVAGE) {
				BuildingSalvageMission salvageMission = (BuildingSalvageMission) missionCache;
				if (!salvageMission.getConstructionVehicles().isEmpty()) {
					Vehicle vehicle = salvageMission.getConstructionVehicles().get(0);
					getDesktop().openUnitWindow(vehicle, false);
				}
			}
		});

		JPanel wrapper00 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper00.add(vehicleButton);
		vehicleLayout.add(wrapper00);

		return vehicleLayout;
	}

	private JPanel initTravelPane() {
		
		JPanel mainLayout = new JPanel(new BorderLayout());
		Border blackline = StyleManager.createLabelBorder("Travel");
		mainLayout.setBorder(blackline);
	
		// Prepare travel grid layout.
		AttributePanel travelGridPane = new AttributePanel(4);
		mainLayout.add(travelGridPane, BorderLayout.NORTH);

		vehicleStatusLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.vehicleStatus"), "", null);
		speedLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.vehicleSpeed"), "", null);
		distanceNextNavLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.distanceNextNavPoint"), "", null);
		traveledLabel = travelGridPane.addTextField(Msg.getString("MainDetailPanel.distanceTraveled"), "", null);

		return mainLayout;
	}

	private JPanel initLogPane() {

		Border blackline = StyleManager.createLabelBorder("Phase Log");
		
		// Create the member panel.
		JPanel logPane = new JPanel(new BorderLayout());
		logPane.setBorder(blackline);
		logPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		logPane.setPreferredSize(new Dimension(100, HEIGHT_1));

		// Create scroll panel for member list.
		JScrollPane logScrollPane = new JScrollPane();
		logPane.add(logScrollPane, BorderLayout.CENTER);

		// Create member table model.
		logTableModel = new LogTableModel();

		// Create member table.
		JTable logTable = new JTable(logTableModel);
		logTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(150);

		logScrollPane.setViewportView(logTable);

		return logPane;
	}

	private JPanel initMemberPane() {
		
		if (memberPane == null) {	
			// Create the member panel.
			memberPane = new JPanel(new BorderLayout(1, 1));
			memberPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
	
			// Create member bottom panel.
			JPanel memberBottomPane = new JPanel(new BorderLayout(5, 5));
			memberBottomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
			memberPane.add(memberBottomPane);
	
			// Prepare member list panel
			JPanel memberListPane = new JPanel(new BorderLayout(5, 5));
			memberListPane.setPreferredSize(new Dimension(100, HEIGHT_1));
			memberBottomPane.add(memberListPane, BorderLayout.CENTER);
	
			// Create scroll panel for member list.
			JScrollPane memberScrollPane = new JScrollPane();
			memberListPane.add(memberScrollPane, BorderLayout.CENTER);
	
			// Create member table model.
			memberTableModel = new MemberTableModel();
	
			// Create member table.
			memberTable = new JTable(memberTableModel);
			memberTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			memberTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			memberTable.setRowSelectionAllowed(true);
			memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			memberTable.addMouseListener(new UnitTableLauncher(desktop));
			memberScrollPane.setViewportView(memberTable);
		}
		
		return memberPane;
	}

	private JPanel initCustomMissionPane() {

		// Create the mission custom panel.
		customPanelLayout = new CardLayout(10, 10);
		missionCustomPane = new JPanel(customPanelLayout);
		missionCustomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

		Border blackline = StyleManager.createLabelBorder("Mission Specific");
		missionCustomPane.setBorder(blackline);
		
		// Create custom empty panel.
		JPanel emptyCustomPanel = new JPanel();
		missionCustomPane.add(emptyCustomPanel, EMPTY);
		customInfoPanels = new HashMap<>();

		// Create custom areology field mission panel.
		MissionCustomInfoPanel areologyFieldPanel = new AreologyStudyFieldMissionCustomInfoPanel(desktop);
		String areologyMissionName = AreologyFieldStudy.class.getName();
		customInfoPanels.put(areologyMissionName, areologyFieldPanel);
		missionCustomPane.add(areologyFieldPanel, areologyMissionName);

		// Create custom biology field mission panel.
		MissionCustomInfoPanel biologyFieldPanel = new BiologyStudyFieldMissionCustomInfoPanel(desktop);
		String biologyMissionName = BiologyFieldStudy.class.getName();
		customInfoPanels.put(biologyMissionName, biologyFieldPanel);
		missionCustomPane.add(biologyFieldPanel, biologyMissionName);

		// Create custom meteorology field mission panel.
		MissionCustomInfoPanel meteorologyFieldPanel = new MeteorologyStudyFieldMissionCustomInfoPanel(desktop);
		String meteorologyMissionName = MeteorologyFieldStudy.class.getName();
		customInfoPanels.put(meteorologyMissionName, meteorologyFieldPanel);
		missionCustomPane.add(meteorologyFieldPanel, meteorologyMissionName);

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
		String constructionMissionName = BuildingConstructionMission.class.getName();
		customInfoPanels.put(constructionMissionName, constructionPanel);
		missionCustomPane.add(constructionPanel, constructionMissionName);

		// Create custom salvage mission panel.
		MissionCustomInfoPanel salvagePanel = new SalvageMissionCustomInfoPanel(desktop);
		String salvageMissionName = BuildingSalvageMission.class.getName();
		customInfoPanels.put(salvageMissionName, salvagePanel);
		missionCustomPane.add(salvagePanel, salvageMissionName);

		// Create custom exploration mission panel.
		MissionCustomInfoPanel explorationPanel = new ExplorationCustomInfoPanel();
		String explorationMissionName = Exploration.class.getName();
		customInfoPanels.put(explorationMissionName, explorationPanel);
		missionCustomPane.add(explorationPanel, explorationMissionName);

		// Create custom collect regolith mission panel.
		MissionCustomInfoPanel collectRegolithPanel = new CollectResourcesMissionCustomInfoPanel(ResourceUtil.REGOLITH_TYPES);
		String collectRegolithMissionName = CollectRegolith.class.getName();
		customInfoPanels.put(collectRegolithMissionName, collectRegolithPanel);
		missionCustomPane.add(collectRegolithPanel, collectRegolithMissionName);

		// Create custom collect ice mission panel.
		MissionCustomInfoPanel collectIcePanel = new CollectResourcesMissionCustomInfoPanel(new int[] {ResourceUtil.iceID});
		String collectIceMissionName = CollectIce.class.getName();
		customInfoPanels.put(collectIceMissionName, collectIcePanel);
		missionCustomPane.add(collectIcePanel, collectIceMissionName);

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
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
	    Objects.requireNonNull(text);
	    Objects.requireNonNull(changeListener);
	    DocumentListener dl = new DocumentListener() {
	        private int lastChange = 0, lastNotifiedChange = 0;

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
		if (newMission == null) {	
			clearInfo();
			return;
		}
		
		// Remove this as previous mission listener.
		if (missionCache != null)
			missionCache.removeMissionListener(this);
					
		if (missionCache == null || missionCache != newMission) {
			missionCache = newMission;
			
			// Add this as listener for new mission.
			newMission.addMissionListener(this);
			
			setCurrentMission(newMission);
			// Update info on Main tab
			updateMainTab(newMission);
			// Update custom mission panel.
			updateCustomPanel(newMission);
		}
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

		if (currentVehicle == null && mission.getMembers().isEmpty() && mission.isDone()) {
			// Check if the mission is done and the members have been disbanded
			memberOuterPane.removeAll();
			memberOuterPane.add(memberLabel);
			memberLabel.setText(" Disbanded: " + printMembers(mission));
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
		
		leadTextField.setText(mission.getStartingPerson().getName());

		String phaseText = mission.getPhaseDescription();
		phaseTextField.setToolTipText(phaseText);
		if (phaseText.length() > MAX_LENGTH)
			phaseText = phaseText.substring(0, MAX_LENGTH) + "...";
		phaseTextField.setText(phaseText);

		var missionStatusText = new StringBuilder();
		missionStatusText.append(mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
		statusTextField.setText(missionStatusText.toString());
		
		settlementTextField.setText(mission.getAssociatedSettlement().getName());

		logTableModel.setMission(mission);
		
		centerMapButton.setEnabled(true);
		
		// Update mission vehicle info in UI.
		if (MissionType.isVehicleMission(mission.getMissionType())) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			Vehicle vehicle = vehicleMission.getVehicle();
			if (vehicle != null) {
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(StyleManager.DECIMAL_KMH.format(vehicle.getSpeed())); //$NON-NLS-1$
				try {
					int currentLegRemainingDist = (int) vehicleMission.getDistanceCurrentLegRemaining();
					distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(currentLegRemainingDist)); //$NON-NLS-1$
				} catch (Exception e2) {
				}

				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getDistanceProposed()*10.0)/10.0;

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
	
				String name = ((AbstractVehicleMission)vehicleMission).getVehicleName();
				
				if (name != null)
					vehicleButton.setText(name);
				
				vehicleStatusLabel.setText(" ");
				speedLabel.setText(StyleManager.DECIMAL_KMH.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
		
				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getDistanceProposed()*10.0)/10.0;

				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						estTotalDistance
						));
				
				
				if (currentVehicle != null) {
					currentVehicle.removeUnitListener(this);
				}
				currentVehicle = null;
			}
		} else if (mission.getMissionType() == MissionType.BUILDING_CONSTRUCTION) {
			// Display first of mission's list of construction vehicles.
			BuildingConstructionMission constructionMission = (BuildingConstructionMission) mission;
			List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
			if (!constVehicles.isEmpty()) {
				Vehicle vehicle = constVehicles.get(0);
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(StyleManager.DECIMAL_KMH.format(vehicle.getSpeed())); //$NON-NLS-1$
				distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", "0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
				vehicle.addUnitListener(this);
				currentVehicle = vehicle;
			}
		} else if (mission.getMissionType() == MissionType.BUILDING_SALVAGE) {
			// Display first of mission's list of construction vehicles.
			BuildingSalvageMission salvageMission = (BuildingSalvageMission) mission;
			List<GroundVehicle> constVehicles = salvageMission.getConstructionVehicles();
			if (!constVehicles.isEmpty()) {
				Vehicle vehicle = constVehicles.get(0);
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(StyleManager.DECIMAL_KMH.format(vehicle.getSpeed())); //$NON-NLS-1$
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
		leadTextField.setText(" ");
		designationTextField.setText(" ");
		typeTextField.setText(" ");
		phaseTextField.setText(" ");
		phaseTextField.setToolTipText(" ");
		
		statusTextField.setText(" ");
		settlementTextField.setText(" ");

		memberTableModel.setMission(null);
		logTableModel.setMission(null);
		
		centerMapButton.setEnabled(false);
		
		vehicleStatusLabel.setText(" ");
		speedLabel.setText(StyleManager.DECIMAL_KMH.format(0)); //$NON-NLS-1$ //$NON-NLS-2$
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
		boolean hasMissionPanel = false;
		if (mission != null) {		
			String missionClassName = mission.getClass().getName();
			if (customInfoPanels.containsKey(missionClassName)) {
				hasMissionPanel = true;
				MissionCustomInfoPanel panel = customInfoPanels.get(missionClassName);
				customPanelLayout.show(missionCustomPane, missionClassName);
				panel.updateMission(mission);
			}
		}

		if (!hasMissionPanel)
			customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Mission event update.
	 */
	public void missionUpdate(MissionEvent e) {
		if (e.getSource().equals(missionCache)) {
			SwingUtilities.invokeLater(new MissionEventUpdater(e, this));
		}
	}

	/**
	 * Updates the custom mission panels with a mission event.
	 *
	 * @param e the mission event.
	 */
	private void updateCustomPanelMissionEvent(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		if (mission != null) {
			String missionClassName = mission.getClass().getName();
			if (customInfoPanels.containsKey(missionClassName)) {
				customInfoPanels.get(missionClassName).updateMissionEvent(e);
			}
		}
	}

	/**
	 * Catches unit update event.
	 *
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
		if ((((Unit)event.getSource()).getUnitType() == UnitType.VEHICLE)
			&& event.getSource().equals(currentVehicle)) {
				SwingUtilities.invokeLater(new VehicleInfoUpdater(event));
		}
	}

	public MissionWindow getMissionWindow() {
		return missionWindow;
	}

	public void destroy() {
		designationTextField = null;
		typeTextField = null;
		leadTextField = null;
		phaseTextField = null;
		statusTextField = null;
		settlementTextField = null;
		vehicleStatusLabel = null;
		speedLabel = null;
		distanceNextNavLabel = null;
		traveledLabel = null;
		centerMapButton = null;
		vehicleButton = null;
		customPanelLayout = null;
		missionCustomPane = null;
		missionCache = null;
		currentVehicle = null;
		missionWindow = null;
		desktop = null;
		memberTable = null;
		memberTableModel = null;
	}
	
	/**
	 * Gets the main desktop.
	 *
	 * @return desktop.
	 */
	private MainDesktopPane getDesktop() {
		return desktop;
	}

	private class MissionEventUpdater implements Runnable {

		private MissionEvent event;
		private MainDetailPanel panel;

		private MissionEventUpdater(MissionEvent event, MainDetailPanel panel) {
			this.event = event;
			this.panel = panel;
		}

		public void run() {
			Mission mission = (Mission) event.getSource();
			MissionEventType type = event.getType();

			// Update UI based on mission event type.
			if (type == MissionEventType.TYPE_EVENT || type == MissionEventType.TYPE_ID_EVENT)
				typeTextField.setText(mission.getName());
			else if (type == MissionEventType.DESCRIPTION_EVENT) {
				// Implement the missing descriptionLabel
			}
			else if (type == MissionEventType.DESIGNATION_EVENT) {
				// Implement the missing descriptionLabel
				if (missionWindow.getCreateMissionWizard() != null) {
					String s = mission.getFullMissionDesignation();
					if (s == null || s.equals("")) {
						s = "[TBA]";
					}

					designationTextField.setText(Conversion.capitalize(s));
				}
			} else if (type == MissionEventType.PHASE_DESCRIPTION_EVENT) {
				String phaseText = mission.getPhaseDescription();
				if (phaseText.length() > MAX_LENGTH)
					phaseText = phaseText.substring(0, MAX_LENGTH) + "...";
				phaseTextField.setText(phaseText);
			} else if (type == MissionEventType.END_MISSION_EVENT) {
				var missionStatusText = new StringBuilder();
				missionStatusText.append( mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
				statusTextField.setText(missionStatusText.toString());
			} else if (type == MissionEventType.ADD_MEMBER_EVENT || type == MissionEventType.REMOVE_MEMBER_EVENT
					|| type == MissionEventType.MIN_MEMBERS_EVENT || type == MissionEventType.CAPACITY_EVENT) {
				memberTableModel.updateMembers();
			} else if (type == MissionEventType.VEHICLE_EVENT) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if (vehicle != null) {
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
					vehicleStatusLabel.setText(vehicle.printStatusTypes());
					speedLabel.setText(StyleManager.DECIMAL_KMH.format(vehicle.getSpeed())); //$NON-NLS-1$
					vehicle.addUnitListener(panel);
					currentVehicle = vehicle;
				} else {
					vehicleButton.setVisible(false);
					vehicleStatusLabel.setText("Not Applicable");
					speedLabel.setText(StyleManager.DECIMAL_KMH.format(0)); //$NON-NLS-1$
					if (currentVehicle != null)
						currentVehicle.removeUnitListener(panel);
					currentVehicle = null;
				}
			} else if (type == MissionEventType.DISTANCE_EVENT) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				try {
					int distanceNextNav = (int) vehicleMission.getDistanceCurrentLegRemaining();
					distanceNextNavLabel.setText(StyleManager.DECIMAL_KM.format(distanceNextNav)); //$NON-NLS-1$
				} catch (Exception e2) {
				}
				double travelledDistance = Math.round(vehicleMission.getTotalDistanceTravelled()*10.0)/10.0;
				double estTotalDistance = Math.round(vehicleMission.getDistanceProposed()*10.0)/10.0;
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						estTotalDistance
						));
			}

			// Update custom mission panel.
			updateCustomPanelMissionEvent(event);
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
				speedLabel.setText(StyleManager.DECIMAL_KMH.format(vehicle.getSpeed())); //$NON-NLS-1$
		}
	}

	
	/**
	 * Adapter for the mission log
	 */
	private class LogTableModel extends AbstractTableModel {
		private Mission mission;

		/**
		 * Constructor.
		 */
		private LogTableModel() {
			mission = null;
		}

		/**
		 * Gets the row count.
		 *
		 * @return row count.
		 */
		public int getRowCount() {
			return (mission != null ? mission.getLog().getEntries().size() : 0);
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
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return "Date"; //$NON-NLS-1$
			else if (columnIndex == 1)
				return "Phase";
			else
				return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			List<MissionLog.MissionLogEntry> entries = mission.getLog().getEntries();
			if (row < entries.size()) {
				if (column == 0)
					return entries.get(row).getTime();
				else
					return entries.get(row).getEntry();
			} else
				return Msg.getString("unknown"); //$NON-NLS-1$
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
	private class MemberTableModel extends AbstractTableModel implements UnitListener, UnitModel {

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
		public int getRowCount() {
			return members.size();
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
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("MainDetailPanel.column.name"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("MainDetailPanel.column.task"); //$NON-NLS-1$
			else
				return Msg.getString("MainDetailPanel.column.onboard"); //$NON-NLS-1$
		}

		/**
		 * Gets the value at a given row and column.
		 *
		 * @param row    the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Worker member = members.get(row);
				if (column == 0)
					return member.getName();
				else if (column == 1)
					return member.getTaskDescription();
				else {
					if (isOnboard(member))
						return "Y";
					else
						return "N";
				}
			} else
				return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Has this member boarded the vehicle ?
		 *
		 * @param member
		 * @return
		 */
		boolean isOnboard(Worker member) {
			if (mission instanceof VehicleMission) {		
				if (member.getUnitType() == UnitType.PERSON) {
					Vehicle v = ((VehicleMission)mission).getVehicle();
					if (v.getVehicleType() == VehicleType.DELIVERY_DRONE) {
						return false;
					}
					else if (v instanceof Rover) {
						Rover r = (Rover) v;
						if (r != null && r.isCrewmember((Person)member)) {
							return true;
						}
					}
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
				if (members.size() > 0) {
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
		public Unit getAssociatedUnit(int row) {
			return (Unit) members.get(row);

		}
	}
}
