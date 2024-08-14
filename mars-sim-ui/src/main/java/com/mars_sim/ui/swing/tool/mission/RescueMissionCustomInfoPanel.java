/*
 * Mars Simulation Project
 * RescueMissionCustomInfoPanel.java
 * @date 2022-08-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.RescueSalvageVehicle;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool.SpringUtilities;



/**
 * A panel for displaying rescue/salvage vehicle mission information.
 */
@SuppressWarnings("serial")
public class RescueMissionCustomInfoPanel extends MissionCustomInfoPanel {

    // Data members
    private MainDesktopPane desktop;
    private RescueSalvageVehicle rescueMission;
    private JButton rescueVehicleButton;
    private JLabel vehicleStatusValueLabel;
    private JLabel malfunctionListLabel;
    private JPanel contentPanel;

    RescueMissionCustomInfoPanel(MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();

        // Initialize data members.
        this.desktop = desktop;

        // Set layout.
        setLayout(new BorderLayout());

        // Create content panel.
        contentPanel = new JPanel(new SpringLayout());//new GridLayout(3, 1));
        add(contentPanel, BorderLayout.NORTH);

        // Create rescue vehicle panel.
        JPanel rescueVehiclePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(rescueVehiclePanel);

        // Create rescue vehicle title label.
        JLabel rescueVehicleTitleLabel = new JLabel("Vehicle to Rescue : ", SwingConstants.LEFT);
        rescueVehiclePanel.add(rescueVehicleTitleLabel);

        // Create rescue vehicle button.
        rescueVehicleButton = new JButton("");
        JPanel wrapper0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper0.add(rescueVehicleButton);
		contentPanel.add(wrapper0);
        //contentPanel.add(rescueVehicleButton);
        rescueVehicleButton.addActionListener(e -> openRescueVehicleWindow());

        // Create status panel.
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(statusPanel);

        // Create vehicle status title label.
        JLabel vehicleStatusTitleLabel = new JLabel("Vehicle Status : ", SwingConstants.RIGHT);
        statusPanel.add(vehicleStatusTitleLabel);

        // Create vehicle status value label.
        vehicleStatusValueLabel = new JLabel("", SwingConstants.LEFT);
        JPanel wrapper1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper1.add(vehicleStatusValueLabel);
		contentPanel.add(wrapper1);

        // Create malfunction panel.
		JPanel malfunctionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(malfunctionPanel);

        // Create malfunction title panel.
        JLabel malfunctionTitleLabel = new JLabel("Vehicle Malfunctions : ", SwingConstants.RIGHT);
        malfunctionPanel.add(malfunctionTitleLabel);

        // Create malfunction list label.
        malfunctionListLabel = new JLabel("", SwingConstants.LEFT);
        JPanel wrapper2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper2.add(malfunctionListLabel);
		contentPanel.add(wrapper2);

		// 2017-05-03 Prepare SpringLayout
		SpringUtilities.makeCompactGrid(contentPanel,
		                                3, 2, //rows, cols
		                                15, 10,        //initX, initY
		                                10, 2);       //xPad, yPad
    }

    /**
     * Opens the info window for the vehicle to be rescued.
     */
    private void openRescueVehicleWindow() {
        if (rescueMission != null) {
            // Open window for vehicle.
            Vehicle vehicle = rescueMission.getVehicleTarget();
            if (vehicle != null) desktop.showDetails(vehicle);
        }
    }

    @Override
    public void updateMission(Mission mission) {
        if (mission instanceof RescueSalvageVehicle) {
            rescueMission = (RescueSalvageVehicle) mission;
            Vehicle vehicle = rescueMission.getVehicleTarget();

            // Update rescue vehicle button.
            rescueVehicleButton.setText(vehicle.getName());

            // Update rescue vehicle status.
            vehicleStatusValueLabel.setText(vehicle.printStatusTypes());

            StringBuffer malfunctionBuff = new StringBuffer("");
            String serious = null;
			Malfunction failure = vehicle.getMalfunctionManager().getMostSeriousMalfunction();
			if (failure != null) {
				serious = failure.getName();
        		malfunctionBuff.append(serious);
			}

            // Update malfunctions label.
            List<Malfunction> malfunctions = vehicle.getMalfunctionManager().getMalfunctions();
            if (malfunctions.size() > 0) {
            	String first = malfunctions.get(0).getName();
            	if (serious != null && !serious.equals(first))
            		malfunctionBuff.append(first);
                for (int x = 1; x < malfunctions.size(); x++) {
                	String next = malfunctions.get(x).getName();
                	if (!next.equals(serious)) {
                        malfunctionBuff.append(", ");
                        malfunctionBuff.append(next);
                	}
                }
            }

            malfunctionListLabel.setText(malfunctionBuff.toString());
        }
    }

    @Override
    public void updateMissionEvent(MissionEvent e) {
        // Do nothing.
    }
}
