package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

public class InfoPanel extends JPanel {

	// Data members.
	JTextField descriptionField;
	
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
		
		
	}
}