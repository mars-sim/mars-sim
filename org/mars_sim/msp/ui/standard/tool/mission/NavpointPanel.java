/**
 * Mars Simulation Project
 * NavpointPanel.java
 * @version 2.80 2006-10-09
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.mission.MissionEvent;
import org.mars_sim.msp.simulation.person.ai.mission.MissionListener;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class NavpointPanel extends JPanel implements ListSelectionListener,
		MissionListener {

	private Mission currentMission;
	
	NavpointPanel() {
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(300, 300));
		
		Box mainPane = Box.createVerticalBox();
		mainPane.setBorder(new MarsPanelBorder());
		add(mainPane, BorderLayout.CENTER);
		
		JPanel mapDisplayPanel = new JPanel(new BorderLayout(0, 0));
		mainPane.add(mapDisplayPanel);
		
		JLabel mapLabel = new JLabel("Map", JLabel.CENTER);
		mapDisplayPanel.add(mapLabel);
	}
	
	public void valueChanged(ListSelectionEvent e) {
		if (currentMission != null) currentMission.removeListener(this);
		Mission mission = (Mission) ((JList) e.getSource()).getSelectedValue();
		if (mission != null) {
			mission.addListener(this);
			currentMission = mission;
		}
		else currentMission = null;
	}

	public void missionUpdate(MissionEvent event) {
		// TODO Auto-generated method stub

	}
}