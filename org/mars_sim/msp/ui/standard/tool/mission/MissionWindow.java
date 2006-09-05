/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 2.80 2006-08-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/**
 * Window for the mission tool.
 */
public class MissionWindow extends ToolWindow {

	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {
		
		// Use ToolWindow constructor
		super("Mission Tool", desktop);
		
		// Create content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);

        // Create mission panel
        JPanel missionPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        mainPane.add(missionPane, BorderLayout.NORTH);

        // Create mission label
        JLabel missionLabel = new JLabel("Mission Tool");
        missionPane.add(missionLabel);
        
        JPanel missionListPane = new JPanel(new BorderLayout());
        missionListPane.setPreferredSize(new Dimension(200, 200));
        mainPane.add(missionListPane, BorderLayout.CENTER);
        
        MissionListModel missionListModel = new MissionListModel();
        JList missionList = new JList(missionListModel);
        missionListPane.add(new JScrollPane(missionList), BorderLayout.CENTER);
        
        // Pack window
        pack();
	}
}