/**
 * Mars Simulation Project
 * RescueMissionCustomInfoPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * A panel for displaying rescue/salvage vehicle mission information.
 */
public class RescueMissionCustomInfoPanel extends MissionCustomInfoPanel {

    // Data members
    private MainDesktopPane desktop;
    private RescueSalvageVehicle rescueMission;
    private JButton rescueVehicleButton;
    private JLabel vehicleStatusValueLabel;
    private JLabel malfunctionListLabel;
    
    RescueMissionCustomInfoPanel(MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();
        
        // Initialize data members.
        this.desktop = desktop;
        
        // Set layout.
        setLayout(new BorderLayout());
        
        // Create content panel.
        JPanel contentPanel = new JPanel(new GridLayout(3, 1));
        add(contentPanel, BorderLayout.NORTH);
        
        // Create rescue vehicle panel.
        JPanel rescueVehiclePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.add(rescueVehiclePanel);
        
        // Create rescue vehicle title label.
        JLabel rescueVehicleTitleLabel = new JLabel("Vehicle to Rescue: ");
        rescueVehiclePanel.add(rescueVehicleTitleLabel);
        
        // Create rescue vehicle button.
        rescueVehicleButton = new JButton("");
        rescueVehiclePanel.add(rescueVehicleButton);
        rescueVehicleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Open window for vehicle to be rescued.
                openRescueVehicleWindow();
            }
        });
        
        // Create status panel.
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.add(statusPanel);
        
        // Create vehicle status title label.
        JLabel vehicleStatusTitleLabel = new JLabel("Vehicle Status: ");
        statusPanel.add(vehicleStatusTitleLabel);
        
        // Create vehicle status value label.
        vehicleStatusValueLabel = new JLabel("");
        statusPanel.add(vehicleStatusValueLabel);
        
        // Create malfunction panel.
        JPanel malfunctionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        contentPanel.add(malfunctionPanel);
        
        // Create malfunction title panel.
        JLabel malfunctionTitleLabel = new JLabel("Vehicle Malfunctions: ");
        malfunctionPanel.add(malfunctionTitleLabel);
        
        // Create malfunction list label.
        malfunctionListLabel = new JLabel("");
        malfunctionPanel.add(malfunctionListLabel);
    }
    
    /**
     * Opens the info window for the vehicle to be rescued.
     */
    private void openRescueVehicleWindow() {
        if (rescueMission != null) {
            // Open window for vehicle.
            Vehicle vehicle = rescueMission.getVehicleTarget();
            if (vehicle != null) desktop.openUnitWindow(vehicle, false);
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
            vehicleStatusValueLabel.setText(vehicle.getStatus());
            
            // Update malfunctions label.
            List<Malfunction> malfunctions = vehicle.getMalfunctionManager().getMalfunctions();
            StringBuffer malfunctionBuff = new StringBuffer("");
            if (malfunctions.size() > 0) {
                malfunctionBuff.append(malfunctions.get(0).getName());
                for (int x = 1; x < malfunctions.size(); x++) {
                    malfunctionBuff.append(", ");
                    malfunctionBuff.append(malfunctions.get(x).getName());
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