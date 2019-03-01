/**
 * Mars Simulation Project
 * CommanderWindow.java
 * @version 3.1.0 2019-02-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.commander;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Commander;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

/**
 * Window for the Commander Dashboard.
 */
public class CommanderWindow
extends ToolWindow {

	/** Tool name. */
	public static final String NAME = "Commander Dashboard";

	// Private members
	private JTabbedPane tabPane;
	
	private JLabel leadershipPointsLabel;

	private Commander commander = SimulationConfig.instance().getPersonConfiguration().getCommander();

	/**
	 * Constructor.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public CommanderWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, desktop);
//		mainScene = desktop.getMainScene();

		// Create content panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		setContentPane(mainPane);

		// Create the mission list panel.
//		JPanel listPane = new JPanel(new BorderLayout());
//		listPane.setPreferredSize(new Dimension(200, 200));
//		mainPane.add(listPane, BorderLayout.WEST);

		JPanel bottomPane = new JPanel(new GridLayout(1, 4));
		bottomPane.setPreferredSize(new Dimension(200, 50));
		mainPane.add(bottomPane, BorderLayout.SOUTH);
		
//		JPanel leadershipPane = new JPanel(new BorderLayout());
//		leadershipPane.setPreferredSize(new Dimension(200, 50));
//		bottomPane.add(leadershipPane);
		
		JLabel leadershipLabel = new JLabel("  Leadership Points : ", JLabel.RIGHT);
		bottomPane.add(leadershipLabel);
		
		leadershipPointsLabel = new JLabel("", JLabel.LEFT);
		bottomPane.add(leadershipPointsLabel);
		bottomPane.add(new JLabel());
		bottomPane.add(new JLabel());
		
		leadershipPointsLabel.setText(commander.getLeadershipPoint() + "");
		
		// Create the info tab panel.
		tabPane = new JTabbedPane();
		mainPane.add(tabPane, BorderLayout.CENTER);

		setSize(new Dimension(640, 640));
		setMaximizable(true);
		setResizable(false);

		setVisible(true);
		//pack();

		Dimension desktopSize = desktop.getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

	}

	public MainDesktopPane getDesktop() {
		return desktop;
	}
	
	public boolean isNavPointsMapTabOpen() {
		if (tabPane.getSelectedIndex() == 1)
			return true;
		else
			return false;
	}
	
	public void update() {
		leadershipPointsLabel.setText(commander.getLeadershipPoint() + "");
	}
	
	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {

	}
}