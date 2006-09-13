package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class MainDetailPanel extends JPanel implements ListSelectionListener, MissionListener {

	private Mission currentMission;
	private JLabel descriptionLabel;
	private JLabel typeLabel;
	private JLabel phaseLabel;
	private JLabel crewCapacityLabel;
	
	public MainDetailPanel() {
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 300));
		
		Box mainPane = new Box(BoxLayout.Y_AXIS);
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		descriptionLabel = new JLabel("Description:");
		mainPane.add(descriptionLabel);
		
		typeLabel = new JLabel("Type:");
		mainPane.add(typeLabel);
		
		phaseLabel = new JLabel("Phase:");
		mainPane.add(phaseLabel);
		
		crewCapacityLabel = new JLabel("Crew Capacity:");
		mainPane.add(crewCapacityLabel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			if (currentMission != null) currentMission.removeListener(this);
	
			descriptionLabel.setText("Description: " + mission.getDescription());
			typeLabel.setText("Type: " + mission.getName());
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
			crewCapacityLabel.setText("Crew Capacity: " + mission.getMissionCapacity());
			
			mission.addListener(this);
			currentMission = mission;
		}
		else {
			descriptionLabel.setText("Description:");
			typeLabel.setText("Type:");
			phaseLabel.setText("Phase:");
		}
	}
	
	public void missionUpdate(MissionEvent e) {
		Mission mission = (Mission) e.getSource();
		if (e.getType().equals(Mission.NAME_EVENT)) 
			typeLabel.setText("Type: " + mission.getName());
		else if (e.getType().equals(Mission.DESCRIPTION_EVENT)) 
			descriptionLabel.setText("Description: " + mission.getDescription());
		else if (e.getType().equals(Mission.PHASE_DESCRIPTION_EVENT))
			phaseLabel.setText("Phase: " + mission.getPhaseDescription());
		else if (e.getType().equals(Mission.CAPACITY_EVENT))
			crewCapacityLabel.setText("Crew Capacity: " + mission.getMissionCapacity());
	}
}