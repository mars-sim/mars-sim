/**
 * Mars Simulation Project
 * MissionWindow.java
 * @version 2.80 2006-08-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission;

import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;

/**
 * Window for the mission tool.
 */
public class MissionWindow extends ToolWindow {

	private JList missionList;
	private NavpointPanel navpointPane;
	
	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	public MissionWindow(MainDesktopPane desktop) {
		
		// Use ToolWindow constructor
		super("Mission Tool", desktop);
		
		// Set window resizable to false.
        setResizable(false);
		
		// Create content pane
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(mainPane);
        
        JPanel missionListPane = new JPanel(new BorderLayout());
        missionListPane.setPreferredSize(new Dimension(200, 200));
        mainPane.add(missionListPane, BorderLayout.WEST);
        
        missionList = new JList(new MissionListModel());
        missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        missionListPane.add(new JScrollPane(missionList), BorderLayout.CENTER);
        
        JTabbedPane infoPane = new JTabbedPane();
        mainPane.add(infoPane, BorderLayout.EAST);
        
        MainDetailPanel mainDetailPane = new MainDetailPanel(desktop);
        missionList.addListSelectionListener(mainDetailPane);
        infoPane.add("Info", mainDetailPane);
        
        navpointPane = new NavpointPanel();
        missionList.addListSelectionListener(navpointPane);
        infoPane.add("Navpoints", navpointPane);
        
        // Pack window
        pack();
	}
	
	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		missionList.clearSelection();
		navpointPane.destroy();
	}
}