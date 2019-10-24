/**
 * Mars Simulation Project
 * RescueMissionCustomInfoPanel.java
 * @version 3.1.0 2017-10-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.SpringLayout;

import org.mars_sim.msp.core.malfunction.Malfunction;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.RescueSalvageVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * A panel for displaying rescue/salvage vehicle mission information.
 */
public class RescueMissionCustomInfoPanel extends MissionCustomInfoPanel {

    // Data members
    private MainDesktopPane desktop;
    private RescueSalvageVehicle rescueMission;
    private WebButton rescueVehicleButton;
    private WebLabel vehicleStatusValueLabel;
    private WebLabel malfunctionListLabel;
    private WebPanel contentPanel;

    RescueMissionCustomInfoPanel(MainDesktopPane desktop) {
        // Use MissionCustomInfoPanel constructor.
        super();

        // Initialize data members.
        this.desktop = desktop;

        // Set layout.
        setLayout(new BorderLayout());

        // Create content panel.
        contentPanel = new WebPanel(new SpringLayout());//new GridLayout(3, 1));
        add(contentPanel, BorderLayout.NORTH);

        // Create rescue vehicle panel.
        WebPanel rescueVehiclePanel = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(rescueVehiclePanel);

        // Create rescue vehicle title label.
        WebLabel rescueVehicleTitleLabel = new WebLabel("Vehicle to Rescue : ", WebLabel.LEFT);
        rescueVehiclePanel.add(rescueVehicleTitleLabel);

        // Create rescue vehicle button.
        rescueVehicleButton = new WebButton("");
        WebPanel wrapper0 = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper0.add(rescueVehicleButton);
		contentPanel.add(wrapper0);
        //contentPanel.add(rescueVehicleButton);
        rescueVehicleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Open window for vehicle to be rescued.
                openRescueVehicleWindow();
            }
        });

        // Create status panel.
        WebPanel statusPanel = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(statusPanel);

        // Create vehicle status title label.
        WebLabel vehicleStatusTitleLabel = new WebLabel("Vehicle Status : ", WebLabel.RIGHT);
        statusPanel.add(vehicleStatusTitleLabel);

        // Create vehicle status value label.
        vehicleStatusValueLabel = new WebLabel("", WebLabel.LEFT);
        WebPanel wrapper1 = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		wrapper1.add(vehicleStatusValueLabel);
		contentPanel.add(wrapper1);

        // Create malfunction panel.
		WebPanel malfunctionPanel = new WebPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPanel.add(malfunctionPanel);

        // Create malfunction title panel.
        WebLabel malfunctionTitleLabel = new WebLabel("Vehicle Malfunctions : ", WebLabel.RIGHT);
        malfunctionPanel.add(malfunctionTitleLabel);

        // Create malfunction list label.
        malfunctionListLabel = new WebLabel("", WebLabel.LEFT);
        WebPanel wrapper2 = new WebPanel(new FlowLayout(FlowLayout.LEFT));
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
            	if (!serious.equals(first))
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