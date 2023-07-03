/**
 * Mars Simulation Project
 * EditMissionDialog.java
 * @date 2022-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.edit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.ModalInternalFrame;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;


/**
 * The edit mission dialog for the mission tool.
 */
public class EditMissionDialog extends ModalInternalFrame {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
	// Private members
	private Mission mission;
	private InfoPanel infoPane;
	protected MainDesktopPane desktop;
	private MissionWindow missionWindow;
	
	/**
	 * Constructor
	 * @param owner the owner frame.
	 * @param mission the mission to edit.
	 */
	public EditMissionDialog(MainDesktopPane desktop, Mission mission, MissionWindow missionWindow) {
		// Use ModalInternalFrame constructor
        super("Edit Mission");
        this.missionWindow = missionWindow;
        
		// Initialize data members.
		this.mission = mission;
		this.desktop = desktop;
		
		// Set the layout.
		setLayout(new BorderLayout(0, 0));
		
		// Set the border.
		((JComponent) getContentPane()).setBorder(new MarsPanelBorder());
        
		// Create the info panel.
        infoPane = new InfoPanel(mission, desktop, this);
        add(infoPane, BorderLayout.CENTER);
        
        // Create the button panel.
        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        add(buttonPane, BorderLayout.SOUTH);
        
        // Create the modify button.
        JButton modifyButton = new JButton("Execute");
        modifyButton.addActionListener(
        		new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Commit the mission modification and close dialog.
        				modifyMission();
        				dispose();
        			}
				});
        buttonPane.add(modifyButton);
        
        // Create the cancel button.
        JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(
				new ActionListener() {
        			public void actionPerformed(ActionEvent e) {
        				// Close the dialog.
        				dispose();
        			}
				});
        buttonPane.add(cancelButton);
		
		// Finish and display dialog.
		//pack();
		//setLocationRelativeTo(owner);
		setResizable(false);

        desktop.add(this);
	    
	    
        setSize(new Dimension(400, 400));
		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    
	    //setModal(true);
	    setVisible(true);
	}
	
	/**
	 * Commits the modification of the mission.
	 */
	private void modifyMission() {
		// Set the mission description.
		mission.setName(infoPane.descriptionField.getText());
		
		// Change the mission's action.
		setAction((String) infoPane.actionDropDown.getSelectedItem());
		
		// Set mission members.
		setWorkers();
	}
	
	/**
	 * Sets the mission action.
	 * 
	 * @param action the action string.
	 */
	private void setAction(String action) {
		if (action.equals(InfoPanel.ACTION_CONTINUE)) endEVAPhase();
		else if (action.equals(InfoPanel.ACTION_HOME)) returnHome();
	}
	
	/**
	 * Ends the mission EVA phase at the current site.
	 */
	private void endEVAPhase() {
		if (mission != null) {
			mission.abortPhase();
		}
	}
	
	/**
	 * Have the mission return home and end collection phase if necessary.
	 */
	private void returnHome() {
		if (mission != null) {
			mission.abortMission("Return home");
			// Q: but what's the principle reason for returning home ?
		}
	}
	
	/**
	 * Sets the mission members.
	 */
	private void setWorkers() {
		// Add new members.
		for (int x = 0; x < infoPane.memberListModel.size(); x++) {
		    Worker member = (Worker) infoPane.memberListModel.elementAt(x);
			if (!mission.getMembers().contains(member)) {
			    member.setMission(mission);
			}
		}
		
		// Remove old members.
		Iterator<Worker> i = mission.getMembers().iterator();
		while (i.hasNext()) {
			Worker member = i.next();
			if (!infoPane.memberListModel.contains(member)) {
			    member.setMission(null);
			}
		}
	}
	
	public InfoPanel getInfoPanel() {
		return infoPane;
	}
	
	public MissionWindow getMissionWindow() {
		return missionWindow;
	}
}
