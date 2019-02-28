/**
 * Mars Simulation Project
 * CommanderWindow.java
 * @version 3.1.0 2019-02-28
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.commander;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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
		JPanel listPane = new JPanel(new BorderLayout());
		listPane.setPreferredSize(new Dimension(200, 200));
		mainPane.add(listPane, BorderLayout.WEST);

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
	
	/**
	 * Prepares tool window for deletion.
	 */
	@Override
	public void destroy() {

	}
}