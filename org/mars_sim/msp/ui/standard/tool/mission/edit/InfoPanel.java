package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.simulation.person.ai.mission.CollectResourcesMission;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.TravelMission;
import org.mars_sim.msp.simulation.person.ai.mission.VehicleMission;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class InfoPanel extends JPanel {

	// Action text
	final static String ACTION_NONE = "None";
	final static String ACTION_CONTINUE = "End EVA and Continue to Next Site";
	final static String ACTION_HOME = "Return to Home Settlement and End Mission";
	final static String ACTION_NEAREST = "Go to Nearest Settlement and End Mission";
	
	// Data members.
	JTextField descriptionField;
	JComboBox actionDropDown;
	
	InfoPanel(Mission mission) {
		// Use JPanel constructor.
		super();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JPanel descriptionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		descriptionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(descriptionPane);
		
		JLabel descriptionLabel = new JLabel("Description: ");
		descriptionPane.add(descriptionLabel);
		
		descriptionField = new JTextField(mission.getDescription(), 20);
		descriptionPane.add(descriptionField);
		
		JPanel actionPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		actionPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(actionPane);
		
		JLabel actionLabel = new JLabel("Action: ");
		actionPane.add(actionLabel);
		
		actionDropDown = new JComboBox(getActions(mission));
		actionDropDown.setEnabled(actionDropDown.getItemCount() > 1);
		actionPane.add(actionDropDown);
	}
	
	private Vector getActions(Mission mission) {
		Vector actions = new Vector();
		actions.add(ACTION_NONE);
		
		String phase = mission.getPhase();
		
		if (phase.equals(CollectResourcesMission.COLLECT_RESOURCES)) {
			CollectResourcesMission collectResourcesMission = (CollectResourcesMission) mission;
			if (collectResourcesMission.getNumCollectionSites() > collectResourcesMission.getNumCollectionSitesVisited())
				actions.add(ACTION_CONTINUE);
		}
		
		if (mission instanceof TravelMission) {
			TravelMission travelMission = (TravelMission) mission;
			int nextNavpointIndex = travelMission.getNextNavpointIndex();
			if ((nextNavpointIndex > -1) && (nextNavpointIndex < (travelMission.getNumberOfNavpoints() - 1)))
				actions.add(ACTION_HOME);
		}
		
		if (mission instanceof VehicleMission) {
			VehicleMission vehicleMission = (VehicleMission) mission;
			try {
				Settlement closestSettlement = vehicleMission.findClosestSettlement();
				if (!closestSettlement.equals(vehicleMission.getAssociatedSettlement())) 
					actions.add(ACTION_NEAREST);
			}
			catch (Exception e) {}
		}
		
		return actions;
	}
}