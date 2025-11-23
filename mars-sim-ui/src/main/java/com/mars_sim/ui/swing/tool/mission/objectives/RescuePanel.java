/*
 * Mars Simulation Project
 * RescuePanel.java
 * @date 2025=08-22
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.malfunction.Malfunction;
import com.mars_sim.core.mission.objectives.RescueVehicleObjective;
import com.mars_sim.core.person.ai.mission.MissionEvent;
import com.mars_sim.core.person.ai.mission.MissionListener;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.utils.AttributePanel;



/**
 * A panel for displaying rescue/salvage vehicle mission information.
 */
@SuppressWarnings("serial")
public class RescuePanel extends JPanel
    implements MissionListener {

    // Data members
    private RescueVehicleObjective rescueMission;
    private JLabel vehicleStatusValueLabel;
    private JLabel malfunctionListLabel;

    public RescuePanel(RescueVehicleObjective objectives, UIContext context) {
        // Use MissionCustomInfoPanel constructor.
        super();

        this.rescueMission = objectives;

        // Set layout.
        setLayout(new BorderLayout());
        setName(objectives.getName());

        // Create content panel.
        var attrPanel = new AttributePanel();
        add(attrPanel, BorderLayout.NORTH);

        // Create rescue vehicle button.
        var rescueVehicleButton = new EntityLabel(objectives.getRecoverVehicle(), context);
        attrPanel.addLabelledItem("Vehicle to Rescue", rescueVehicleButton);
		vehicleStatusValueLabel = attrPanel.addRow("Vehicle Status", "");
        malfunctionListLabel = attrPanel.addRow("Vehicle Malfunctions", "");

        updateVehicle();
    }

    @Override
    public void missionUpdate(MissionEvent event) {
        updateVehicle();
    }
    
    private void updateVehicle() {
        var target = rescueMission.getRecoverVehicle();

        // Update rescue vehicle status.
        vehicleStatusValueLabel.setText(target.printStatusTypes());


        // Update malfunctions label.
        List<Malfunction> malfunctions = target.getMalfunctionManager().getMalfunctions();
        String txt = malfunctions.stream()
                .map(m -> m.getName())
                .collect(Collectors.joining(", "));


        malfunctionListLabel.setText(txt);
    }
}
