/**
 * Mars Simulation Project
 * MainDetailPanel.java
 * @version 3.06 2014-01-29
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

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
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.person.ai.mission.Trade;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

/**
 * The tab panel for showing mission details.
 */
public class MainDetailPanel extends JPanel implements ListSelectionListener, 
		MissionListener, UnitListener {

	// Custom mission panel IDs.
	private final static String EMPTY = "empty";
	
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
	private DecimalFormat formatter = new DecimalFormat("0.0");
	private CardLayout customPanelLayout;
	private JPanel missionCustomPane;
	private Map<String, MissionCustomInfoPanel> customInfoPanels;
	
	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	MainDetailPanel(MainDesktopPane desktop) {
		// User JPanel constructor.
		super();
		
		// Initialize data members.
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout());
		
		// Create the main panel.
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		// Create the description panel.
		Box descriptionPane = new CustomBox();
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(descriptionPane);
		
		// Create the description label.
		descriptionLabel = new JLabel("Description:", SwingConstants.LEFT);
		descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(descriptionLabel);
		
		// Create the type label.
		typeLabel = new JLabel("Type:");
		typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(typeLabel);
		
		// Create the phase label.
		phaseLabel = new JLabel("Phase:");
		phaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPane.add(phaseLabel);
		
		// Create the member panel.
		Box memberPane = new CustomBox();
		memberPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(memberPane);
		
		// Create the member number label.
		memberNumLabel = new JLabel("Mission Members:   (Min:  - Max: )");
		memberNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberNumLabel);
		
		// Create member bottom panel.
		JPanel memberBottomPane = new JPanel(new BorderLayout(0, 0));
		memberBottomPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		memberPane.add(memberBottomPane);
		
		// Prepare member list panel
		JPanel memberListPane = new JPanel(new BorderLayout(0, 0));
		memberListPane.setPreferredSize(new Dimension(100, 100));
        memberBottomPane.add(memberListPane, BorderLayout.CENTER);
        
        // Create scroll panel for member list.
        JScrollPane memberScrollPane = new JScrollPane();
        memberListPane.add(memberScrollPane, BorderLayout.CENTER);
        
        // Create member table model.
        memberTableModel = new MemberTableModel();
        
        // Create member table.
        memberTable = new JTable(memberTableModel);
        memberTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        memberTable.setRowSelectionAllowed(true);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getSelectionModel().addListSelectionListener(
        	new ListSelectionListener() {
        		public void valueChanged(ListSelectionEvent e) {
        			if (e.getValueIsAdjusting()) {
        				// Open window for selected person.
        				int index = memberTable.getSelectedRow();
        				Person selectedPerson = memberTableModel.getMemberAtIndex(index);
        				if (selectedPerson != null) getDesktop().openUnitWindow(selectedPerson, false);
        			}
        		}
        	});
        memberScrollPane.setViewportView(memberTable);
		
        // Create the travel panel.
		Box travelPane = new CustomBox();
		travelPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPane.add(travelPane);
		
		// Create the vehicle panel.
		JPanel vehiclePane = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		vehiclePane.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehiclePane);
		
        // Create center map button
        centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
        centerMapButton.setMargin(new Insets(1, 1, 1, 1));
        centerMapButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (currentMission != null) {
        			getDesktop().centerMapGlobe(currentMission.getCurrentMissionLocation());
        		}
			}
        });
        centerMapButton.setToolTipText("Locate in Mars navigator tool");
        centerMapButton.setEnabled(false);
        vehiclePane.add(centerMapButton);
		
		// Create the vehicle label.
		JLabel vehicleLabel = new JLabel(" Vehicle: ");
		vehiclePane.add(vehicleLabel);
		
		// Create the vehicle panel.
		vehicleButton = new JButton("   ");
		vehiclePane.add(vehicleButton);
		vehicleButton.setVisible(false);
		vehicleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (currentMission instanceof VehicleMission) {
					// Open window for vehicle.
					VehicleMission vehicleMission = (VehicleMission) currentMission;
					Vehicle vehicle = vehicleMission.getVehicle();
					if (vehicle != null) getDesktop().openUnitWindow(vehicle, false);
				}
			}
		});
		
		// Create the vehicle status label.
		vehicleStatusLabel = new JLabel("Vehicle Status:");
		vehicleStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(vehicleStatusLabel);
		
		// Create the speed label.
		speedLabel = new JLabel("Vehicle Speed:");
		speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(speedLabel);
		
		// Create the distance next navpoint label.
		distanceNextNavLabel = new JLabel("Distance to Next Navpoint:");
		distanceNextNavLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(distanceNextNavLabel);
		
		// Create the traveled distance label.
		traveledLabel = new JLabel("Traveled Distance:");
		traveledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		travelPane.add(traveledLabel);
		
		// Create the mission custom panel.
		customPanelLayout = new CardLayout();
		missionCustomPane = new JPanel(customPanelLayout);
		missionCustomPane.setPreferredSize(new Dimension(-1, 250));
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
        MissionCustomInfoPanel collectRegolithPanel = new CollectResourcesMissionCustomInfoPanel(
                AmountResource.findAmountResource("regolith"));
        String collectRegolithMissionName = CollectRegolith.class.getName();
        customInfoPanels.put(collectRegolithMissionName, collectRegolithPanel);
        missionCustomPane.add(collectRegolithPanel, collectRegolithMissionName);
        
        // Create custom collect ice mission panel.
        MissionCustomInfoPanel collectIcePanel = new CollectResourcesMissionCustomInfoPanel(
                AmountResource.findAmountResource("ice"));
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
		// Remove mission and unit listeners.
		if (currentMission != null) currentMission.removeMissionListener(this);
		if (currentVehicle != null) currentVehicle.removeUnitListener(this);
		
		// Get the selected mission.
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		
		if (mission != null) {
			// Update mission info in UI.
			descriptionLabel.setText("Description: " + mission.getDescription());
			typeLabel.setText("Type: " + mission.getName());
            String phaseText = mission.getPhaseDescription();
            if (phaseText.length() > 40) phaseText = phaseText.substring(0, 40) + "...";
			phaseLabel.setText("Phase: " + phaseText);
			int memberNum = mission.getPeopleNumber();
			int minMembers = mission.getMinPeople();
			String maxMembers = "";
            if (mission instanceof VehicleMission) {
                maxMembers = "" + mission.getMissionCapacity();
            }
            else {
                maxMembers = "unlimited";
            }
			memberNumLabel.setText("Mission Members: " + memberNum + " (Min: " + minMembers + 
					" - Max: " + maxMembers + ")");
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
					vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
					speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
					try {
						int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
						distanceNextNavLabel.setText("Distance to Next Navpoint: " + distanceNextNav + " km");
					}
					catch (Exception e2) {}
					int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
					int totalDistance = (int) vehicleMission.getTotalDistance();
					traveledLabel.setText("Traveled Distance: " + travelledDistance + 
							" km of " + totalDistance + " km");
					vehicle.addUnitListener(this);
					currentVehicle = vehicle;
				}
			}
			if (!isVehicle) {
				// Clear vehicle info.
				vehicleButton.setVisible(false);
				vehicleStatusLabel.setText("Vehicle Status:");
				speedLabel.setText("Vehicle Speed:");
				distanceNextNavLabel.setText("Distance to Next Navpoint:");
				traveledLabel.setText("Traveled Distance:");
				currentVehicle = null;
			}
			
			// Add mission listener.
			mission.addMissionListener(this);
			currentMission = mission;
		}
		else {
			// Clear mission info in UI.
			descriptionLabel.setText("Description:");
			typeLabel.setText("Type:");
			phaseLabel.setText("Phase:");
			memberNumLabel.setText("Mission Members:   (Min:  - Max: )");
			memberTableModel.setMission(null);
			centerMapButton.setEnabled(false);
			vehicleButton.setVisible(false);
			vehicleStatusLabel.setText("Vehicle Status:");
			speedLabel.setText("Vehicle Speed:");
			distanceNextNavLabel.setText("Distance to Next Navpoint:");
			traveledLabel.setText("Traveled Distance:");
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
		SwingUtilities.invokeLater(new VehicleInfoUpdater(event));
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
    		String type = event.getType();
    		
    		// Update UI based on mission event type.
    		if (type.equals(Mission.NAME_EVENT)) 
    			typeLabel.setText("Type: " + mission.getName());
    		else if (type.equals(Mission.DESCRIPTION_EVENT)) 
    			descriptionLabel.setText("Description: " + mission.getDescription());
    		else if (type.equals(Mission.PHASE_DESCRIPTION_EVENT)) {
                String phaseText = mission.getPhaseDescription();
                if (phaseText.length() > 40) phaseText = phaseText.substring(0, 40) + "...";
                phaseLabel.setText("Phase: " + phaseText);
            }
    		else if (type.equals(Mission.ADD_MEMBER_EVENT) || type.equals(Mission.REMOVE_MEMBER_EVENT) || 
    				type.equals(Mission.MIN_PEOPLE_EVENT) || type.equals(Mission.CAPACITY_EVENT)) {
    			int memberNum = mission.getPeopleNumber();
    			int minMembers = mission.getMinPeople();
    			String maxMembers = "";
    			if (mission instanceof VehicleMission) {
    			    maxMembers = "" + mission.getMissionCapacity();
    			}
    			else {
    			    maxMembers = "unlimited";
    			}
    			memberNumLabel.setText("Mission Members: " + memberNum + " (Min: " + minMembers + 
    					" - Max: " + maxMembers + ")");
    			memberTableModel.updateMembers();
    		}
    		else if (type.equals(VehicleMission.VEHICLE_EVENT)) {
    			Vehicle vehicle = ((VehicleMission) mission).getVehicle();
    			if (vehicle != null) {
    				vehicleButton.setText(vehicle.getName());
    				vehicleButton.setVisible(true);
    				vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
    				speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
    				vehicle.addUnitListener(panel);
    				currentVehicle = vehicle;
    			}
    			else {
    				vehicleButton.setVisible(false);
    				vehicleStatusLabel.setText("Vehicle Status:");
    				speedLabel.setText("Vehicle Speed:");
    				if (currentVehicle != null) currentVehicle.removeUnitListener(panel);
    				currentVehicle = null;
    			}
    		}
    		else if (type.equals(TravelMission.DISTANCE_EVENT)) {
    			VehicleMission vehicleMission = (VehicleMission) mission;
    			try {
    				int distanceNextNav = (int) vehicleMission.getCurrentLegRemainingDistance();
    				distanceNextNavLabel.setText("Distance to Next Navpoint: " + distanceNextNav + " km");
    			}
    			catch (Exception e2) {}
    			int travelledDistance = (int) vehicleMission.getTotalDistanceTravelled();
    			int totalDistance = (int) vehicleMission.getTotalDistance();
    			traveledLabel.setText("Traveled Distance: " + travelledDistance + 
    					" km of " + totalDistance + " km");
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
    		if (type == UnitEventType.STATUS_EVENT) 
    			vehicleStatusLabel.setText("Vehicle Status: " + vehicle.getStatus());
    		else if (type == UnitEventType.SPEED_EVENT) 
    			speedLabel.setText("Vehicle Speed: " + formatter.format(vehicle.getSpeed()) + " km/h");
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
    private class MemberTableModel extends AbstractTableModel implements UnitListener {
    	
    	// Private members.
    	Mission mission;
    	Collection<Person> members;
    	
    	/**
    	 * Constructor
    	 */
    	private MemberTableModel() {
    		mission = null;
    		members = new ConcurrentLinkedQueue<Person>();
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
            if (columnIndex == 0) return "Name";
            else if (columnIndex == 1) return "Task";
            else return "unknown";
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
            	Person person = (Person) array[row];
            	if (column == 0) return person.getName();
            	else return person.getMind().getTaskManager().getTaskDescription();
            }   
            else return "unknown";
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
    		Person person = (Person) event.getSource();
    		int index = getIndex(members,person);
    		if (type == UnitEventType.NAME_EVENT) 
    			SwingUtilities.invokeLater(new MemberTableUpdater(index, 0));
    		else if (type == UnitEventType.TASK_DESC_EVENT || type == UnitEventType.TASK_EVENT) 
    			SwingUtilities.invokeLater(new MemberTableUpdater(index, 1));
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
    			members = new ConcurrentLinkedQueue<Person>(mission.getPeople());
    			Iterator<Person> i = members.iterator();
    			while (i.hasNext()) i.next().addUnitListener(this);
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
    			Iterator<Person> i = members.iterator();
    			while (i.hasNext()) i.next().removeUnitListener(this);
    			members.clear();
    		}
    	}
    	
    	/**
    	 * Gets the mission member at a given index.
    	 * @param index the index.
    	 * @return the mission member.
    	 */
    	Person getMemberAtIndex(int index) {
    		if ((index >= 0) && (index < members.size())) {
    		    return (Person) members.toArray()[index];
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
    			if (entireData) fireTableDataChanged();
    			else fireTableCellUpdated(row, column);
    		}
    	}
    }
}