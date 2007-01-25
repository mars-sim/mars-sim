/**
 * Mars Simulation Project
 * CreateMissionWizard.java
 * @version 2.80 2007-01-25
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/**
 * A dialog wizard for creating new missions.
 */
public class CreateMissionWizard extends JDialog {

	// Data members
	private JButton prevButton;
	private JButton nextButton;
	private JButton finalButton;
	
	/**
	 * Constructor
	 * @param owner The owner frame.
	 */
	CreateMissionWizard(Frame owner) {
		// Use JDialog constructor
		super(owner, "Create Mission Wizard", true);
		
		// Create info panel.
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new MarsPanelBorder());
		add(infoPanel, BorderLayout.CENTER);
		
		JLabel nameLabel = new JLabel("Create Mission Wizard");
		infoPanel.add(nameLabel);
		
		// Create bottom button panel.
		JPanel bottomButtonPane = new JPanel();
		add(bottomButtonPane, BorderLayout.SOUTH);
		
		// Create prevous button.
		prevButton = new JButton("Previous");
		prevButton.setEnabled(false);
		bottomButtonPane.add(prevButton);
		
		// Create next button.
		nextButton = new JButton("Next");
		bottomButtonPane.add(nextButton);
		
		// Create final button.
		finalButton = new JButton("Final");
		finalButton.setEnabled(false);
		bottomButtonPane.add(finalButton);
		
		// Create cancel button.
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				setVisible(false);
        			}
				});
		bottomButtonPane.add(cancelButton);
		
		// Finish and display wizard.
		pack();
		setLocationRelativeTo(owner);
		setResizable(false);
		setVisible(true);
	}
}