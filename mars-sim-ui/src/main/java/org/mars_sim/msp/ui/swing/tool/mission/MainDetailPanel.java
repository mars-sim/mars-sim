/**
 * Mars Simulation Project
 * MainDetailPanel.java
 * @version 3.1.0 2017-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.AreologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BiologyStudyFieldMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.CollectIce;
import org.mars_sim.msp.core.person.ai.mission.CollectRegolith;
import org.mars_sim.msp.core.person.ai.mission.EmergencySupplyMission;
import org.mars_sim.msp.core.person.ai.mission.Exploration;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.StatusType;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;

/**
 * The tab panel for showing mission details.
 */
public class MainDetailPanel
extends JPanel
implements ListSelectionListener, MissionListener, UnitListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Custom mission panel IDs.
	private final static String EMPTY = Msg.getString("MainDetailPanel.empty"); //$NON-NLS-1$

	// Private members
	private Mission currentMission;
	private Vehicle currentVehicle;
	private JLabel descriptionLabel;
	private JLabel typeLabel;
	private JLabel phaseLabel;
	private JLabel memberNumLabel;
	private MemberTableModel memberTableModel;
	private JTable memberTable;
	private JButton centerMapButton;
	private JButton vehicleButton;
	private JLabel vehicleStatusLabel;
	private JLabel speedLabel;
	private JLabel distanceNextNavLabel;
	private JLabel traveledLabel;
	private MainDesktopPane desktop;
	private DecimalFormat formatter = new DecimalFormat(Msg.getString("MainDetailPanel.decimalFormat")); //$NON-NLS-1$
	private CardLayout customPanelLayout;
	private JPanel missionCustomPane;
	private Map<String, MissionCustomInfoPanel> customInfoPanels;
	private MissionWindow missionWindow;

    private static AmountResource iceAR = ResourceUtil.iceAR;//.findAmountResource("ice");
    private static AmountResource regolithAR = ResourceUtil.regolithAR;//AmountResource.findAmountResource("regolith");


	/**
	 * Constructor.
	 * @param desktop the main desktop panel.
	 */
	public MainDetailPanel(MainDesktopPane desktop, MissionWindow missionWindow) {
		// User JPanel constructor.
		super();
		// Initialize data members.
		this.desktop = desktop;
        //2016-09-24 Added missionWindow param
		this.missionWindow = missionWindow;

		// Set the layout.
		setLayout(new BorderLayout());

		// Create the main panel.
		Box mainPane = Box.createVerticalBox();
		//JPanel mainPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);

		// Create the description panel.
		Box infoPane = new CustomBox();
		infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel topPane = new JPanel(new SpringLayout());
		infoPane.setSize(new Dimension(200, 150));
		infoPane.setBorder(new MarsPanelBorder());
		infoPane.add(topPane);
		mainPane.add(infoPane);

		//JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		//descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		//springPane0.add(descriptionPane);

		// Create the description label.
		JLabel descriptionLabel0 = new JLabel(Msg.getString("MainDetailPanel.description", JLabel.LEFT)); //$NON-NLS-1$
		descriptionLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPane.add(descriptionLabel0);

		descriptionLabel = new JLabel("None", JLabel.LEFT);
		//descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		descriptionLabel.setToolTipText(Msg.getString("MainDetailPanel.description")); //$NON-NLS-1$

		String s = "";
		// 2015-12-15 Implemented the missing descriptionLabel
		if (missionWindow.getCreateMissionWizard() != null) {
			s = Conversion.capitalize(missionWindow.getCreateMissionWizard().getTypePanel().getDescription());
			//System.out.println("Mission Description : " + s);
			descriptionLabel.setText(s);
		}
		else
			descriptionLabel.setText("None");

		JPanel wrapper0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper0.add(descriptionLabel);
		topPane.add(wrapper0);

		// Create the type label.
		JLabel typeLabel0 = new JLabel(Msg.getString("MainDetailPanel.type", JLabel.LEFT)); //$NON-NLS-1$
		typeLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPane.add(typeLabel0);
		typeLabel0.setToolTipText(Msg.getString("MainDetailPanel.type"));//$NON-NLS-1$

		typeLabel = new JLabel("None", JLabel.LEFT);
		//typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper1.add(typeLabel);
		topPane.add(wrapper1);

		// Create the phase label.
		JLabel phaseLabel0 = new JLabel(Msg.getString("MainDetailPanel.phase", JLabel.LEFT)); //$NON-NLS-1$
		phaseLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		topPane.add(phaseLabel0);

		phaseLabel = new JLabel("None", JLabel.LEFT);
		//phaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper2.add(phaseLabel);
		topPane.add(wrapper2);

		// 2017-05-03 Prepare SpringLayout
		SpringUtilities.makeCompactGrid(topPane,
		                                3, 2, //rows, cols
		                                10, 2,        //initX, initY
		                                25, 1);       //xPad, yPad



		// Create the travel panel.
		Box travelBox = new CustomBox();
		travelBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		// 2017-05-03 Prepare SpringLayout
		JPanel travelPane = new JPanel(new SpringLayout());
		travelPane.setSize(new Dimension(200, 300));
		//travelPane.setBorder(new MarsPanelBorder());
		travelBox.add(travelPane);
		mainPane.add(travelBox);

		// Create the vehicle panel.
		JPanel vehiclePane = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		vehiclePane.setAlignmentX(Component.BOTTOM_ALIGNMENT);
		travelPane.add(vehiclePane);

		// Create center map button
		centerMapButton = new JButton(ImageLoader.getIcon(Msg.getString("img.centerMap"))); //$NON-NLS-1$
		centerMapButton.setMargin(new Insets(2, 2, 2, 2));
		centerMapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentMission != null) {
					getDesktop().centerMapGlobe(currentMission.getCurrentMissionLocation());
				}
			}
		});
		centerMapButton.setToolTipText(Msg.getString("MainDetailPanel.gotoMarsMap")); //$NON-NLS-1$
		centerMapButton.setEnabled(false);
		vehiclePane.add(centerMapButton);

		// Create the vehicle label.
		JLabel vehicleLabel = new JLabel("   " + Msg.getString("MainDetailPanel.vehicle"), JLabel.RIGHT); //$NON-NLS-1$
		vehiclePane.add(vehicleLabel);

		// Create the vehicle panel.
		vehicleButton = new JButton("\t\t\t\t\t"); //$NON-NLS-1$
		vehicleButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		vehicleButton.setVisible(false);
		vehicleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentMission instanceof VehicleMission) {
					// Open window for vehicle.
					VehicleMission vehicleMission = (VehicleMission) currentMission;
					Vehicle vehicle = vehicleMission.getVehicle();
					if (vehicle != null) {
					    getDesktop().openUnitWindow(vehicle, false);
					}
				}
				else if (currentMission instanceof BuildingConstructionMission) {
				    BuildingConstructionMission constructionMission = (BuildingConstructionMission) currentMission;
	                List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
				    if (constVehicles != null)
				    	if (!constVehicles.isEmpty() || constVehicles.size() > 0) {
		                    Vehicle vehicle = constVehicles.get(0);
		                    getDesktop().openUnitWindow(vehicle, false);
		                }
				}
				else if (currentMission instanceof BuildingSalvageMission) {
				    BuildingSalvageMission salvageMission = (BuildingSalvageMission) currentMission;
                    List<GroundVehicle> constVehicles = salvageMission.getConstructionVehicles();
    			    if (constVehicles != null)
    			    	if (!constVehicles.isEmpty() || constVehicles.size() > 0) {
	                        Vehicle vehicle = constVehicles.get(0);
	                        getDesktop().openUnitWindow(vehicle, false);
	                    }
                }
			}
		});
		JPanel wrapper00 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper00.add(vehicleButton);
		travelPane.add(wrapper00);

		// Create the vehicle status label.
		JLabel vehicleStatusLabel0 = new JLabel(Msg.getString("MainDetailPanel.vehicleStatus", JLabel.LEFT));//$NON-NLS-1$ //$NON-NLS-2$
		//vehicleStatusLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehicleStatusLabel0);

		vehicleStatusLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
		//vehicleStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper01 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper01.add(vehicleStatusLabel);
		travelPane.add(wrapper01);


		// Create the speed label.
		JLabel speedLabel0 = new JLabel(Msg.getString("MainDetailPanel.vehicleSpeed", JLabel.LEFT)); //$NON-NLS-1$ //$NON-NLS-2$
		//speedLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(speedLabel0);

		speedLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$ //$NON-NLS-2$
		//speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper02 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper02.add(speedLabel);
		travelPane.add(wrapper02);

		// Create the distance next navpoint label.
		JLabel distanceNextNavLabel0 = new JLabel(Msg.getString("MainDetailPanel.distanceNextNavPoint", JLabel.LEFT)); //$NON-NLS-1$
		//distanceNextNavLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(distanceNextNavLabel0);

		distanceNextNavLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		//distanceNextNavLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper03 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper03.add(distanceNextNavLabel);
		travelPane.add(wrapper03);

		// Create the traveled distance label.
		JLabel traveledLabel0 = new JLabel(Msg.getString("MainDetailPanel.distanceTraveled", JLabel.LEFT)); //$NON-NLS-1$
		//traveledLabel0.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(traveledLabel0);

		traveledLabel = new JLabel("", JLabel.LEFT); //$NON-NLS-1$
		//traveledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel wrapper04 = new JPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		wrapper04.add(traveledLabel);
		travelPane.add(wrapper04);

		// 2017-05-03 Prepare SpringLayout
		SpringUtilities.makeCompactGrid(travelPane,
		                                5, 2, //rows, cols
		                                10, 2,        //initX, initY
		                                5, 1);       //xPad, yPad

		// Create the member panel.
		Box memberPane = new CustomBox();
		memberPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(memberPane);

		// Create the member number label.
		memberNumLabel = new JLabel(Msg.getString("MainDetailPanel.missionMembersMinMax","","","", JLabel.LEFT)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		memberNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberNumLabel);

		// Create member bottom panel.
		JPanel memberBottomPane = new JPanel(new BorderLayout(0, 0));
		memberBottomPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberBottomPane);

		// Prepare member list panel
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
		memberListPane.setPreferredSize(new Dimension(100, 150));
		memberBottomPane.add(memberListPane, BorderLayout.CENTER);

		// Create scroll panel for member list.
		JScrollPane memberScrollPane = new JScrollPane();
		//memberScrollPane.setPreferredSize(new Dimension(300, 250));
		memberListPane.add(memberScrollPane, BorderLayout.CENTER);

		// Create member table model.
		memberTableModel = new MemberTableModel();

		// Create member table.
		//memberTable = new JTable(memberTableModel);
		memberTable = new ZebraJTable(memberTableModel);
		//memberTable.setPreferredSize(new Dimension(300, 250));
		memberTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		memberTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		memberTable.setRowSelectionAllowed(true);
		memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		memberTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							// Open window for selected person.
							int index = memberTable.getSelectedRow();

							MissionMember member = memberTableModel.getMemberAtIndex(index);
							Person person = null;
							Robot robot = null;
							if (member instanceof Person) {
								person = (Person) memberTableModel.getMemberAtIndex(index);
								if (person != null)
									getDesktop().openUnitWindow(person, false);

							}
							else if (member instanceof Robot) {
								robot = (Robot) memberTableModel.getMemberAtIndex(index);
								if (robot != null)
									getDesktop().openUnitWindow(robot, false);
							}

						}
					}
				});
		memberScrollPane.setViewportView(memberTable);
		
		// Create the mission custom panel.
		customPanelLayout = new CardLayout();
		missionCustomPane = new JPanel(customPanelLayout);
		missionCustomPane.setBorder(new MarsPanelBorder());
		missionCustomPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(missionCustomPane);

		// Create custom empty panel.
		JPanel emptyCustomPane1 = new JPanel();
		missionCustomPane.add(emptyCustomPane1, EMPTY);

		customInfoPanels = new HashMap<String, MissionCustomInfoPanel>();

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

		// Create custom biology field mission panel.
		MissionCustomInfoPanel biologyFieldPanel = new BiologyStudyFieldMissionCustomInfoPanel(desktop);
		String biologyMissionName = BiologyStudyFieldMission.class.getName();
		customInfoPanels.put(biologyMissionName, biologyFieldPanel);
		missionCustomPane.add(biologyFieldPanel, biologyMissionName);

		// Create custom areology field mission panel.
		MissionCustomInfoPanel areologyFieldPanel = new AreologyStudyFieldMissionCustomInfoPanel(desktop);
		String areologyMissionName = AreologyStudyFieldMission.class.getName();
		customInfoPanels.put(areologyMissionName, areologyFieldPanel);
		missionCustomPane.add(areologyFieldPanel, areologyMissionName);

		// Create custom collect regolith mission panel.
		MissionCustomInfoPanel collectRegolithPanel = new CollectResourcesMissionCustomInfoPanel(regolithAR);
		String collectRegolithMissionName = CollectRegolith.class.getName();
		customInfoPanels.put(collectRegolithMissionName, collectRegolithPanel);
		missionCustomPane.add(collectRegolithPanel, collectRegolithMissionName);

		// Create custom collect ice mission panel.
		MissionCustomInfoPanel collectIcePanel = new CollectResourcesMissionCustomInfoPanel(iceAR);
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
		String emergencySupplyMissionName = EmergencySupplyMission.class.getName();
		customInfoPanels.put(emergencySupplyMissionName, emergencySupplyPanel);
		missionCustomPane.add(emergencySupplyPanel, emergencySupplyMissionName);
	}

	/**
	 * Implemented from ListSelectionListener.
	 * Note: this is called when a mission is selected on MissionWindow's mission list.
	 */
	public void valueChanged(ListSelectionEvent e) {

		TableStyle.setTableStyle(memberTable);

		// Remove mission and unit listeners.
		if (currentMission != null) currentMission.removeMissionListener(this);
		if (currentVehicle != null) currentVehicle.removeUnitListener(this);

		// Get the selected mission.
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();

		if (mission != null) {
			// Update mission info in UI.

			if (mission.getDescription() != null) {
				descriptionLabel.setText(Conversion.capitalize(mission.getDescription()));
			}
			//2016-09-25 Used getTypePanel().getDescription()
			else if (missionWindow.getCreateMissionWizard().getTypePanel().getDescription() != null) {
				String s = missionWindow.getCreateMissionWizard().getTypePanel().getDescription();
				//System.out.println("Mission Description : " + s);
				descriptionLabel.setText(s);
			}


			typeLabel.setText(mission.getName()); //$NON-NLS-1$
			String phaseText = mission.getPhaseDescription();

			phaseLabel.setToolTipText(phaseText);
			if (phaseText.length() > 48) phaseText = phaseText.substring(0, 48) + "...";
			phaseLabel.setText(phaseText); //$NON-NLS-1$


			int memberNum = mission.getMembersNumber();
			int minMembers = mission.getMinMembers();
			String maxMembers = ""; //$NON-NLS-1$

			if (mission instanceof VehicleMission) {
				maxMembers = "" + mission.getMissionCapacity(); //$NON-NLS-1$
			}
			else {
				maxMembers = Msg.getString("MainDetailPanel.unlimited"); //$NON-NLS-1$
			}

			memberNumLabel.setText(Msg.getString("MainDetailPanel.missionMembersMinMax",memberNum,minMembers,maxMembers)); //$NON-NLS-1$
			memberTableModel.setMission(mission);
			centerMapButton.setEnabled(true);

			// Update mission vehicle info in UI.
			boolean isVehicle = false;
			if (mission instanceof VehicleMission) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				Vehicle vehicle = vehicleMission.getVehicle();
				if (vehicle != null) {
					isVehicle = true;
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
					StatusType s = vehicle.getStatus();
					//if (s == null)
					//	s = "Not Applicable";
					vehicleStatusLabel.setText(s.getName());
					speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed",formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
					try {
						int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
						distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint",distanceNextNav)); //$NON-NLS-1$
					}
					catch (Exception e2) {}
					int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
					int totalDistance = (int) vehicleMission.getTotalDistance();
					traveledLabel.setText(Msg.getString(
						"MainDetailPanel.kmTraveled", //$NON-NLS-1$
						travelledDistance,
						totalDistance
					));
					vehicle.addUnitListener(this);
					currentVehicle = vehicle;
				}
			}
			else if (mission instanceof BuildingConstructionMission) {
			    // Display first of mission's list of construction vehicles.
			    BuildingConstructionMission constructionMission = (BuildingConstructionMission) mission;
			    List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
			    if (constVehicles != null)
			    	if (!constVehicles.isEmpty() || constVehicles.size() > 0) {
				        Vehicle vehicle = constVehicles.get(0);
				        isVehicle = true;
				        vehicleButton.setText(vehicle.getName());
	                    vehicleButton.setVisible(true);
	                    vehicleStatusLabel.setText(vehicle.getStatus().getName()); //$NON-NLS-1$
	                    speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed",formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
	                    distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint","0")); //$NON-NLS-1$ //$NON-NLS-2$
	                    traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled","0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
	                    vehicle.addUnitListener(this);
	                    currentVehicle = vehicle;
				    }
			}
			else if (mission instanceof BuildingSalvageMission) {
	            // Display first of mission's list of construction vehicles.
			    BuildingSalvageMission salvageMission = (BuildingSalvageMission) mission;
                List<GroundVehicle> constVehicles = salvageMission.getConstructionVehicles();
			    if (constVehicles != null)
			    	if (!constVehicles.isEmpty() || constVehicles.size() > 0) {
	                    Vehicle vehicle = constVehicles.get(0);
	                    isVehicle = true;
	                    vehicleButton.setText(vehicle.getName());
	                    vehicleButton.setVisible(true);
	                    vehicleStatusLabel.setText(vehicle.getStatus().getName()); //$NON-NLS-1$
	                    speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed",formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
	                    distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint","0")); //$NON-NLS-1$ //$NON-NLS-2$
	                    traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled","0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
	                    vehicle.addUnitListener(this);
	                    currentVehicle = vehicle;
	                }
			}

			if (!isVehicle) {
				// Clear vehicle info.
				vehicleButton.setVisible(false);
				vehicleStatusLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
				speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed","0")); //$NON-NLS-1$ //$NON-NLS-2$
				distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint","0")); //$NON-NLS-1$ //$NON-NLS-2$
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled","0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
				currentVehicle = null;
			}

			// Add mission listener.
			mission.addMissionListener(this);
			currentMission = mission;
		}
		else {
			// Clear mission info in UI.
			descriptionLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
			typeLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
			phaseLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
			memberNumLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
			memberTableModel.setMission(null);
			centerMapButton.setEnabled(false);
			vehicleButton.setVisible(false);
			vehicleStatusLabel.setText(""); //$NON-NLS-1$ //$NON-NLS-2$
			speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed","0")); //$NON-NLS-1$ //$NON-NLS-2$
			distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint","0")); //$NON-NLS-1$ //$NON-NLS-2$
			traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled","0", "0")); //$NON-NLS-1$ //$NON-NLS-2$
			currentMission = null;
			currentVehicle = null;
			customPanelLayout.show(missionCustomPane, EMPTY);
		}

		// Update custom mission panel.
		updateCustomPanel(mission);
	}

	/**
	 * Update the custom mission panel with a mission.
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

		if (!hasMissionPanel) customPanelLayout.show(missionCustomPane, EMPTY);
	}

	/**
	 * Mission event update.
	 */
	public void missionUpdate(MissionEvent e) {
		SwingUtilities.invokeLater(new MissionEventUpdater(e, this));
	}

	/**
	 * Update the custom mission panels with a mission event.
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
	 * Catch unit update event.
	 * @param event the unit event.
	 */
	public void unitUpdate(UnitEvent event) {
	    if (event.getSource() instanceof Vehicle) {
	        SwingUtilities.invokeLater(new VehicleInfoUpdater(event));
	    }
	}

	/**
	 * Gets the main desktop.
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
			if (type == MissionEventType.NAME_EVENT)
				typeLabel.setText(mission.getName()); //$NON-NLS-1$
			else if (type == MissionEventType.DESCRIPTION_EVENT) {
				// 2015-12-15 Implemented the missing descriptionLabel
				if (missionWindow.getCreateMissionWizard() != null) {
					String s = missionWindow.getCreateMissionWizard().getTypePanel().getDescription();
					if (s == null) {
						s = "None";
					}
					else {
						s = Conversion.capitalize(s);
					}
					descriptionLabel.setText(s);
				}
				else {
					String s = mission.getDescription();
					if (s == null)
						s = "None";
					descriptionLabel.setText(s);
				}
			}
			else if (type == MissionEventType.PHASE_DESCRIPTION_EVENT) {
				String phaseText = mission.getPhaseDescription();
				if (phaseText.length() > 45) phaseText = phaseText.substring(0, 45) + "...";
				phaseLabel.setText(phaseText); //$NON-NLS-1$
			}
			else if (type == MissionEventType.ADD_MEMBER_EVENT || type == MissionEventType.REMOVE_MEMBER_EVENT ||
					type == MissionEventType.MIN_MEMBERS_EVENT || type == MissionEventType.CAPACITY_EVENT) {
				int memberNum = mission.getMembersNumber();
				int minMembers = mission.getMinMembers();
				String maxMembers = ""; //$NON-NLS-1$
				if (mission instanceof VehicleMission) {
					maxMembers = "" + mission.getMissionCapacity(); //$NON-NLS-1$
				}
				else {
					maxMembers = Msg.getString("MainDetailPanel.unlimited"); //$NON-NLS-1$
				}
				memberNumLabel.setText(Msg.getString(
				    "MainDetailPanel.missionMembersMinMax", //$NON-NLS-1$
					memberNum,minMembers,maxMembers
				));
				memberTableModel.updateMembers();
			}
			else if (type == MissionEventType.VEHICLE_EVENT) {
				Vehicle vehicle = ((VehicleMission) mission).getVehicle();
				if (vehicle != null) {
					vehicleButton.setText(vehicle.getName());
					vehicleButton.setVisible(true);
					StatusType s = vehicle.getStatus();
					//if (s == null)
					//	s = "Not Applicable";
					vehicleStatusLabel.setText(s.getName());
					speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed",formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
					vehicle.addUnitListener(panel);
					currentVehicle = vehicle;
				}
				else {
					vehicleButton.setVisible(false);
					vehicleStatusLabel.setText("Not Applicable");
					speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed","0")); //$NON-NLS-1$ //$NON-NLS-2$
					if (currentVehicle != null) currentVehicle.removeUnitListener(panel);
					currentVehicle = null;
				}
			}
			else if (type == MissionEventType.DISTANCE_EVENT) {
				VehicleMission vehicleMission = (VehicleMission) mission;
				try {
					int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
					distanceNextNavLabel.setText(Msg.getString("MainDetailPanel.kmNextNavPoint", distanceNextNav)); //$NON-NLS-1$
				}
				catch (Exception e2) {}
				int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
				int totalDistance = (int) vehicleMission.getTotalDistance();
				traveledLabel.setText(Msg.getString("MainDetailPanel.kmTraveled", travelledDistance,  //$NON-NLS-1$
					totalDistance));
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
				StatusType s = vehicle.getStatus();
				//if (s == null)
				//	s = "Not Applicable";
				vehicleStatusLabel.setText(s.getName());
			}
			else if (type == UnitEventType.SPEED_EVENT)
				speedLabel.setText(Msg.getString("MainDetailPanel.kmhSpeed",formatter.format(vehicle.getSpeed()))); //$NON-NLS-1$
		}
	}

	/**
	 * A custom box container inner class.
	 */
	private static class CustomBox extends Box {

		/**
		 * Constructor
		 */
		private CustomBox() {
			super(BoxLayout.Y_AXIS);
			setBorder(new MarsPanelBorder());
		}

		/**
		 * Gets the maximum size for the component.
		 * @return dimension.
		 */
		public Dimension getMaximumSize() {
			Dimension result = getPreferredSize();
			result.width = Short.MAX_VALUE;
			return result;
		}
	}

	/**
	 * Table model for mission members.
	 */
	private class MemberTableModel
	extends AbstractTableModel
	implements UnitListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Private members.
		private Mission mission;
		private List<MissionMember> members;

		/**
		 * Constructor.
		 */
		private MemberTableModel() {
			mission = null;
			members = new ArrayList<MissionMember>();
		}

		/**
		 * Gets the row count.
		 * @return row count.
		 */
		public int getRowCount() {
			return members.size();
		}

		/**
		 * Gets the column count.
		 * @return column count.
		 */
		public int getColumnCount() {
			return 2;
		}

		/**
		 * Gets the column name at a given index.
		 * @param columnIndex the column's index.
		 * @return the column name.
		 */
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("MainDetailPanel.column.name"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("MainDetailPanel.column.task"); //$NON-NLS-1$
			else return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Gets the value at a given row and column.
		 * @param row the table row.
		 * @param column the table column.
		 * @return the value.
		 */
		public Object getValueAt(int row, int column) {
			if (row < members.size()) {
				Object array[] = members.toArray();
				MissionMember member = (MissionMember) array[row];
				if (column == 0) return member.getName();
				else return member.getTaskDescription();
			}
			else return Msg.getString("unknown"); //$NON-NLS-1$
		}

		/**
		 * Sets the mission for this table model.
		 * @param newMission the new mission.
		 */
		void setMission(Mission newMission) {
			this.mission = newMission;
			updateMembers();
		}

		/**
		 * Catch unit update event.
		 * @param event the unit event.
		 */
		public void unitUpdate(UnitEvent event) {
			UnitEventType type = event.getType();
			MissionMember member = (MissionMember) event.getSource();
			int index = getIndex(members, member);
			if (type == UnitEventType.NAME_EVENT) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
			}
			else if ((type == UnitEventType.TASK_DESCRIPTION_EVENT) ||
					(type == UnitEventType.TASK_EVENT) ||
					(type == UnitEventType.TASK_ENDED_EVENT) ||
					(type == UnitEventType.TASK_SUBTASK_EVENT) ||
					(type == UnitEventType.TASK_NAME_EVENT)) {
				SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
			}
		}

		private int getIndex(Collection col, Object obj) {
			int result = -1;
			Object array[] = col.toArray();
			int size = array.length;

			for(int i = 0; i <size;i++){
				if(array[i].equals(obj)){
					result = i;
					break;
				}
			}

			return result;
		}

		/**
		 * Update mission members.
		 */
		void updateMembers() {
			if (mission != null) {
				clearMembers();
				members = new ArrayList<MissionMember>(mission.getMembers());
				Collections.sort(members, new Comparator<MissionMember>() {

                    @Override
                    public int compare(MissionMember o1, MissionMember o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
				});
				Iterator<MissionMember> i = members.iterator();
				while (i.hasNext()) {
				    MissionMember member = i.next();
				    member.addUnitListener(this);
				}
				SwingUtilities.invokeLater(new MemberTableUpdater());
			}
			else {
				if (members.size() > 0) {
					clearMembers();
					SwingUtilities.invokeLater(new MemberTableUpdater());
				}
			}
		}

		/**
		 * Clear all members from the table.
		 */
		private void clearMembers() {
			if (members != null) {
				Iterator<MissionMember> i = members.iterator();
				while (i.hasNext()) {
				    MissionMember member = i.next();
				    member.removeUnitListener(this);
				}
				members.clear();
			}
		}

		/**
		 * Gets the mission member at a given index.
		 * @param index the index.
		 * @return the mission member.
		 */
		MissionMember getMemberAtIndex(int index) {
			if ((index >= 0) && (index < members.size())) {
				return (MissionMember) members.toArray()[index];
			}
			else {
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
				}
				else {
				    fireTableCellUpdated(row, column);
				}
			}
		}
	}

	// 2016-09-24 Added getMissionWindow()
	public MissionWindow getMissionWindow() {
		return missionWindow;
	}
}