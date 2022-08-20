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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import org.mars_sim.msp.core.person.ai.mission.MissionLogEntry;
import org.mars_sim.msp.core.person.ai.mission.MissionStatus;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.style.StyleId;

/**
 * The tab panel for showing mission details.
 */
@SuppressWarnings("serial")
public class MainDetailPanel extends WebPanel implements MissionListener, UnitListener {

	// Custom mission panel IDs.
	private static final String EMPTY = Msg.getString("MainDetailPanel.empty"); //$NON-NLS-1$

	private static final int MAX_LENGTH = 48;
	private static final int HEIGHT_1 = 150;
	
	// Private members
	private WebLabel designationLabel;
	private WebLabel typeLabel;
	private WebLabel leaderLabel;
	private WebLabel phaseLabel;
	private WebLabel settlementLabel;
	private WebLabel missionStatusLabel;
	
	private WebLabel vehicleStatusLabel;
	private WebLabel speedLabel;
	private WebLabel distanceNextNavLabel;
	private WebLabel traveledLabel;

	private JLabel memberLabel = new JLabel("", SwingConstants.LEFT);
	
	private MemberTableModel memberTableModel;
	private JTable memberTable;

	private WebButton centerMapButton;
	private WebButton vehicleButton;

	private DecimalFormat formatter = new DecimalFormat(Msg.getString("MainDetailPanel.decimalFormat")); //$NON-NLS-1$
	private CardLayout customPanelLayout;

	private WebPanel missionCustomPane;
	private WebPanel memberPane;
	private WebPanel memberOuterPane;
	
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

		WebScrollPane scrollPane = new WebScrollPane();
//		scrollPane.setBorder(new MarsPanelBorder());
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollPane, BorderLayout.CENTER);

		// Create the main panel.
		WebPanel mainBox = new WebPanel(new BorderLayout(1, 1));
		mainBox.setBorder(new MarsPanelBorder());
		scrollPane.setViewportView(mainBox);

		// Create the top box.
		WebPanel topBox = new WebPanel(new BorderLayout(1, 1));
		topBox.setBorder(new MarsPanelBorder());
		mainBox.add(topBox, BorderLayout.NORTH);

		// Create the center box.
		WebPanel centerBox = new WebPanel(new BorderLayout(1, 1));
		centerBox.setBorder(new MarsPanelBorder());
		mainBox.add(centerBox, BorderLayout.CENTER);

		// Create the member panel.
		WebPanel bottomBox = new WebPanel(new BorderLayout(1, 1));
		mainBox.add(bottomBox, BorderLayout.SOUTH);

		topBox.add(initMissionPane(), BorderLayout.CENTER);
		topBox.add(initLogPane(), BorderLayout.SOUTH);

		centerBox.add(initVehiclePane(), BorderLayout.NORTH);
		centerBox.add(initTravelPane(), BorderLayout.CENTER);

		memberOuterPane = new WebPanel(new BorderLayout(1, 1));
		Border blackline = BorderFactory.createTitledBorder("Team Members");
		memberOuterPane.setBorder(blackline);
			
		memberPane = initMemberPane();
		memberOuterPane.add(memberPane, BorderLayout.CENTER);
				
		bottomBox.add(memberOuterPane, BorderLayout.NORTH);
		bottomBox.add(initCustomMissionPane(), BorderLayout.SOUTH);
	}

	private WebPanel initMissionPane() {

		// Create the vehicle pane.
		WebPanel missionLayout = new WebPanel(new FlowLayout(10, 10, 10));
		Border blackline = BorderFactory.createTitledBorder("Profile");
		missionLayout.setBorder(blackline);
	
		// Create the mission pane spring layout.
		WebPanel missionGridPane = new WebPanel(new GridLayout(6, 2, 10, 2));
		missionLayout.add(missionGridPane);
		
		// Create the type label.
		WebLabel typeLabel0 = new WebLabel(Msg.getString("MainDetailPanel.type", SwingConstants.LEFT)); //$NON-NLS-1$
//		typeLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(typeLabel0);

		typeLabel = new WebLabel(" ", SwingConstants.LEFT);
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper2.add(typeLabel);
		missionGridPane.add(wrapper2);
		
		// Create the designation label.
		WebLabel designationLabel0 = new WebLabel(Msg.getString("MainDetailPanel.designation", SwingConstants.LEFT)); //$NON-NLS-1$
//		designationLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(designationLabel0);

		designationLabel = new WebLabel("", SwingConstants.LEFT);
		WebPanel wrapper1a = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper1a.add(designationLabel);
		missionGridPane.add(wrapper1a);

		// Create the phase label.
		WebLabel settlementLabel0 = new WebLabel(Msg.getString("MainDetailPanel.settlement", SwingConstants.LEFT)); //$NON-NLS-1$
//		settlementLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(settlementLabel0);

		String settlementName = "                ";
		if (missionWindow.getSettlement() != null)
			settlementName = missionWindow.getSettlement().getName();
		
		settlementLabel = new WebLabel(settlementName, SwingConstants.LEFT);
		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper4.add(settlementLabel);
		missionGridPane.add(wrapper4);

		// Create the type label.
		WebLabel leadLabel0 = new WebLabel(Msg.getString("MainDetailPanel.startingMember", SwingConstants.LEFT)); //$NON-NLS-1$
//		leadLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(leadLabel0);
		leaderLabel = new WebLabel(" ", SwingConstants.LEFT);
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper1.add(leaderLabel);
		missionGridPane.add(wrapper1);

		// Create the phase label.
		WebLabel phaseLabel0 = new WebLabel(Msg.getString("MainDetailPanel.phase", SwingConstants.LEFT)); //$NON-NLS-1$
//		phaseLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(phaseLabel0);

		phaseLabel = new WebLabel(" ", SwingConstants.LEFT);
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper3.add(phaseLabel);
		missionGridPane.add(wrapper3);

		// Create the mission status Label
		WebLabel missionStatusLabel0 = new WebLabel(Msg.getString("MainDetailPanel.missionStatus", SwingConstants.LEFT)); //$NON-NLS-1$
//		missionStatusLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		missionGridPane.add(missionStatusLabel0);

		missionStatusLabel = new WebLabel(" ", SwingConstants.LEFT);
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper5.add(missionStatusLabel);
		missionGridPane.add(wrapper5);
		
		return missionLayout;
	}

	private WebPanel initVehiclePane() {

		// Create the vehicle grid panel.
		WebPanel vehicleLayout = new WebPanel(new GridLayout(1, 2));
		
		// Create the vehicle pane.
		WebPanel vehiclePane = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		vehiclePane.setAlignmentX(Component.BOTTOM_ALIGNMENT);
		vehicleLayout.add(vehiclePane);

		// Create center map button
        final ImageIcon centerIcon = new LazyIcon("center").getIcon();
		centerMapButton = new WebButton(StyleId.buttonUndecorated, centerIcon);//ImageLoader.getIcon(Msg.getString("img.centerMap"))); //$NON-NLS-1$
		centerMapButton.setMargin(new Insets(2, 2, 2, 2));
		centerMapButton.addActionListener(e -> {
			if (missionCache != null)
				getDesktop().centerMapGlobe(missionCache.getCurrentMissionLocation());
		});
		centerMapButton.setToolTipText(Msg.getString("MainDetailPanel.gotoMarsMap")); //$NON-NLS-1$
		centerMapButton.setEnabled(false);
		vehiclePane.add(centerMapButton);

		// Create the vehicle label.
		WebLabel vehicleLabel = new WebLabel(" " + Msg.getString("MainDetailPanel.vehicle"), SwingConstants.LEFT); //$NON-NLS-1$
		vehiclePane.add(vehicleLabel);

		// Create the vehicle panel.
		vehicleButton = new WebButton("");
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

		WebPanel wrapper00 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper00.add(vehicleButton);
		vehicleLayout.add(wrapper00);

		return vehicleLayout;
	}

	private WebPanel initTravelPane() {
		
		WebPanel mainLayout = new WebPanel(new FlowLayout(10, 10, 10));
		Border blackline = BorderFactory.createTitledBorder("Travel");
		mainLayout.setBorder(blackline);
	
		// Prepare travel grid layout.
		WebPanel travelGridPane = new WebPanel(new GridLayout(4, 2, 10, 2));
		mainLayout.add(travelGridPane);

		// Create the vehicle status label.
		WebLabel vehicleStatusLabel0 = new WebLabel(Msg.getString("MainDetailPanel.vehicleStatus",SwingConstants.LEFT)); //$NON-NLS-2$
		travelGridPane.add(vehicleStatusLabel0);

		vehicleStatusLabel = new WebLabel(" ", SwingConstants.LEFT);
		WebPanel wrapper01 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper01.add(vehicleStatusLabel);
		travelGridPane.add(wrapper01);

		// Create the speed label.
		WebLabel speedLabel0 = new WebLabel(Msg.getString("MainDetailPanel.vehicleSpeed", SwingConstants.LEFT)); //$NON-NLS-1$
		travelGridPane.add(speedLabel0);

		speedLabel = new WebLabel(" ", SwingConstants.LEFT);
//		speedLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		WebPanel wrapper02 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper02.add(speedLabel);
		travelGridPane.add(wrapper02);

		// Create the distance next navpoint label.
		WebLabel distanceNextNavLabel0 = new WebLabel(
				Msg.getString("MainDetailPanel.distanceNextNavPoint", SwingConstants.LEFT)); //$NON-NLS-1$
//		distanceNextNavLabel0.setAlignmentX(Component.RIGHT_ALIGNMENT);
		travelGridPane.add(distanceNextNavLabel0);

		distanceNextNavLabel = new WebLabel(" ", SwingConstants.LEFT); //$NON-NLS-1$
		WebPanel wrapper03 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper03.add(distanceNextNavLabel);
		travelGridPane.add(wrapper03);

		// Create the traveled distance label.
		WebLabel traveledLabel0 = new WebLabel(Msg.getString("MainDetailPanel.distanceTraveled", SwingConstants.LEFT)); //$NON-NLS-1$
		travelGridPane.add(traveledLabel0);

		traveledLabel = new WebLabel(" ", SwingConstants.LEFT); //$NON-NLS-1$
		WebPanel wrapper04 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper04.add(traveledLabel);
		travelGridPane.add(wrapper04);

		return mainLayout;
	}

	private WebPanel initLogPane() {

		Border blackline = BorderFactory.createTitledBorder("Phase Log");
		
		// Create the member panel.
		WebPanel logPane = new WebPanel(new BorderLayout());
		logPane.setBorder(blackline);
		logPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
		logPane.setPreferredSize(new Dimension(100, HEIGHT_1));

		// Create scroll panel for member list.
		WebScrollPane logScrollPane = new WebScrollPane();
		logPane.add(logScrollPane, BorderLayout.CENTER);

		// Create member table model.
		logTableModel = new LogTableModel();

		// Create member table.
		JTable logTable = new ZebraJTable(logTableModel);
		logTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		logTable.getColumnModel().getColumn(1).setPreferredWidth(150);

		logScrollPane.setViewportView(logTable);

		return logPane;
	}

	private WebPanel initMemberPane() {
		
		if (memberPane == null) {	
			// Create the member panel.
			memberPane = new WebPanel(new BorderLayout(1, 1));
			memberPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
	
			// Create member bottom panel.
			WebPanel memberBottomPane = new WebPanel(new BorderLayout(5, 5));
			memberBottomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
			memberPane.add(memberBottomPane);
	
			// Prepare member list panel
			WebPanel memberListPane = new WebPanel(new BorderLayout(5, 5));
			memberListPane.setPreferredSize(new Dimension(100, HEIGHT_1));
			memberBottomPane.add(memberListPane, BorderLayout.CENTER);
	
			// Create scroll panel for member list.
			WebScrollPane memberScrollPane = new WebScrollPane();
			memberListPane.add(memberScrollPane, BorderLayout.CENTER);
	
			// Create member table model.
			memberTableModel = new MemberTableModel();
	
			// Create member table.
			memberTable = new ZebraJTable(memberTableModel);
			memberTable.getColumnModel().getColumn(0).setPreferredWidth(80);
			memberTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			memberTable.setRowSelectionAllowed(true);
			memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			memberTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) {
						// Open window for selected person.
						int index = memberTable.getSelectedRow();
						if (index > -1) {
							Worker member = memberTableModel.getMemberAtIndex(index);
							if (member.getUnitType() == UnitType.PERSON) {
								getDesktop().openUnitWindow((Person) member, false);
							} else {
								getDesktop().openUnitWindow((Robot) member, false);
							}
						}
					}
				}
			});
			memberScrollPane.setViewportView(memberTable);
		}
		
		return memberPane;
	}

	private WebPanel initCustomMissionPane() {

		// Create the mission custom panel.
		customPanelLayout = new CardLayout(10, 10);
		missionCustomPane = new WebPanel(customPanelLayout);
		missionCustomPane.setAlignmentX(Component.RIGHT_ALIGNMENT);

		Border blackline = BorderFactory.createTitledBorder("Mission Specific");
		missionCustomPane.setBorder(blackline);
		
		// Create custom empty panel.
		WebPanel emptyCustomPanel = new WebPanel();
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
		}

		else {
			
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
			designationLabel.setText(d);
			typeLabel.setText(mission.getName());
			
			leaderLabel.setText(mission.getStartingPerson().getName());
	
			String phaseText = mission.getPhaseDescription();
			phaseLabel.setToolTipText(phaseText);
			if (phaseText.length() > MAX_LENGTH)
				phaseText = phaseText.substring(0, MAX_LENGTH) + "...";
			phaseLabel.setText(phaseText);
	
			var missionStatusText = new StringBuilder();
			missionStatusText.append(mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
			missionStatusLabel.setText(missionStatusText.toString());
			
			settlementLabel.setText(mission.getAssociatedSettlement().getName());

			logTableModel.setMission(mission);
			
			centerMapButton.setEnabled(true);
		}
		
		// Update mission vehicle info in UI.
		if (MissionType.isVehicleMission(mission.getMissionType())) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			Vehicle vehicle = vehicleMission.getVehicle();
			if (vehicle != null) {
				vehicleButton.setText(vehicle.getName());
				vehicleButton.setVisible(true);
				vehicleStatusLabel.setText(vehicle.printStatusTypes());
				speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
				try {
					int currentLegRemainingDist = (int) vehicleMission.getDistanceCurrentLegRemaining();
					distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", currentLegRemainingDist)); //$NON-NLS-1$
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
	
				vehicleButton.setText(vehicleMission.getVehicleRecord());
				
				vehicleStatusLabel.setText(" ");
				speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", "0")); //$NON-NLS-1$ //$NON-NLS-2$
				distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", "0")); //$NON-NLS-1$ //$NON-NLS-2$
//				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", "0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
		
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
				speedLabel.setText(
						Msg.getString("MainDetailPanel.kmhSpeed", formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
				distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", "0")); //$NON-NLS-1$ //$NON-NLS-2$
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
				speedLabel.setText(
						Msg.getString("MainDetailPanel.kmhSpeed", formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
				distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", "0")); //$NON-NLS-1$ //$NON-NLS-2$
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
		leaderLabel.setText(" ");
		designationLabel.setText(" ");
		typeLabel.setText(" ");
		phaseLabel.setText(" ");
		phaseLabel.setToolTipText(" ");
		
		missionStatusLabel.setText(" ");
		settlementLabel.setText(" ");

		memberTableModel.setMission(null);
		logTableModel.setMission(null);
		
		centerMapButton.setEnabled(false);
//		vehicleButton.setVisible(false);
		
		vehicleStatusLabel.setText(" ");
		speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", "0")); //$NON-NLS-1$ //$NON-NLS-2$
		distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", "0")); //$NON-NLS-1$ //$NON-NLS-2$
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
				typeLabel.setText(mission.getName());
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

					designationLabel.setText(Conversion.capitalize(s));
				}
			} else if (type == MissionEventType.PHASE_DESCRIPTION_EVENT) {
				String phaseText = mission.getPhaseDescription();
				if (phaseText.length() > MAX_LENGTH)
					phaseText = phaseText.substring(0, MAX_LENGTH) + "...";
				phaseLabel.setText(phaseText);
			} else if (type == MissionEventType.END_MISSION_EVENT) {
				var missionStatusText = new StringBuilder();
				missionStatusText.append( mission.getMissionStatus().stream().map(MissionStatus::getName).collect(Collectors.joining(", ")));
				missionStatusLabel.setText(missionStatusText.toString());
			} else if (type == MissionEventType.ADD_MEMBER_EVENT || type == MissionEventType.REMOVE_MEMBER_EVENT
					|| type == MissionEventType.MIN_MEMBERS_EVENT || type == MissionEventType.CAPACITY_EVENT) {
				memberTableModel.updateMembers();
			} else if (type == MissionEventType.VEHICLE_EVENT) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if (vehicle != null) {
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
					vehicleStatusLabel.setText(vehicle.printStatusTypes());
					speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
					vehicle.addUnitListener(panel);
					currentVehicle = vehicle;
				} else {
					vehicleButton.setVisible(false);
					vehicleStatusLabel.setText("Not Applicable");
					speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", "0")); //$NON-NLS-1$ //$NON-NLS-2$
					if (currentVehicle != null)
						currentVehicle.removeUnitListener(panel);
					currentVehicle = null;
				}
			} else if (type == MissionEventType.DISTANCE_EVENT) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				try {
					int distanceNextNav = (int) vehicleMission.getDistanceCurrentLegRemaining();
					distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", distanceNextNav)); //$NON-NLS-1$
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
				speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed", formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
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
			return (mission != null ? mission.getLog().size() : 0);
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
			List<MissionLogEntry> entries = mission.getLog();
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
	private class MemberTableModel extends AbstractTableModel implements UnitListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

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
				return Msg.getString("MainDetailPanel.column.name"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("MainDetailPanel.column.task"); //$NON-NLS-1$
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
			if (row < members.size()) {
				Object array[] = members.toArray();
				Worker member = (Worker) array[row];
				if (column == 0)
					return member.getName();
				else
					return member.getTaskDescription();
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
			int index = getIndex(members, member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			} else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) || (type == UnitEventType.TASK_EVENT)
					|| (type == UnitEventType.TASK_ENDED_EVENT) || (type == UnitEventType.TASK_SUBTASK_EVENT)
					|| (type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		private int getIndex(Collection<?> col, Object obj) {
			int result = -1;
			Object array[] = col.toArray();
			int size = array.length;

			for (int i = 0; i < size; i++) {
				if (array[i].equals(obj)) {
					result = i;
					break;
				}
			}

			return result;
		}

		/**
		 * Updates mission members.
		 */
		void updateMembers() {
			
			if (mission != null) {
				
				clearMembers();
				members = new ArrayList<>(mission.getMembers());
				Collections.sort(members, new Comparator<>() {
					@Override
					public int compare(Worker o1, Worker o2) {
						return o1.getName().compareToIgnoreCase(o2.getName());
					}
				});
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
		 * Gets the mission member at a given index.
		 *
		 * @param index the index.
		 * @return the mission member.
		 */
		Worker getMemberAtIndex(int index) {
			if ((index >= 0) && (index < members.size())) {
				return (Worker) members.toArray()[index];
			} else {
				return null;
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
	}

	public MissionWindow getMissionWindow() {
		return missionWindow;
	}

	public void destroy() {
		designationLabel = null;
		typeLabel = null;
		leaderLabel = null;
		phaseLabel = null;
		missionStatusLabel = null;
		settlementLabel = null;
		vehicleStatusLabel = null;
		speedLabel = null;
		distanceNextNavLabel = null;
		traveledLabel = null;
		centerMapButton = null;
		vehicleButton = null;
		formatter = null;
		customPanelLayout = null;
		missionCustomPane = null;
		missionCache = null;
		currentVehicle = null;
		missionWindow = null;
		desktop = null;
		memberTable = null;
		memberTableModel = null;
	}
}
