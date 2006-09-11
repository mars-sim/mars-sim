package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.FlowLayout;
import java.awt.Dimension;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;

public class MainDetailPanel extends JPanel implements ListSelectionListener {

	private JLabel selectedMissionLabel;
	
	public MainDetailPanel() {
		
		setLayout(new FlowLayout(FlowLayout.CENTER));
		setPreferredSize(new Dimension(200, 200));
		
		selectedMissionLabel = new JLabel("Selected mission:");
		add(selectedMissionLabel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			selectedMissionLabel.setText("Selected mission: " + mission.getDescription());
		}
	}
}