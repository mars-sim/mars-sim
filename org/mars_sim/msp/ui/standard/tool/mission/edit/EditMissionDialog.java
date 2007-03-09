package org.mars_sim.msp.ui.standard.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mars_sim.msp.simulation.person.ai.mission.Mission;

public class EditMissionDialog extends JDialog {

	private Mission mission;
	private InfoPanel infoPane;
	private NavpointPanel navpointPane;
	
	public EditMissionDialog(Frame owner, Mission mission) {
		// Use JDialog constructor
		super(owner, "Edit Mission", true);
	
		this.mission = mission;
		
		setLayout(new BorderLayout(0, 0));
		
        JTabbedPane tabPane = new JTabbedPane();
        add(tabPane, BorderLayout.CENTER);
        
        infoPane = new InfoPanel(mission);
        tabPane.add("Info", infoPane);
        
        navpointPane = new NavpointPanel(mission);
        tabPane.add("Navpoints", navpointPane);
        
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        add(buttonPane, BorderLayout.SOUTH);
        
        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				modifyMission();
        				setVisible(false);
        			}
				});
        buttonPane.add(modifyButton);
        
        JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				setVisible(false);
        			}
				});
        buttonPane.add(cancelButton);
		
		// Finish and display dialog.
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}
	
	private void modifyMission() {
		// Set the mission description.
		mission.setDescription(infoPane.descriptionField.getText());
	}
}