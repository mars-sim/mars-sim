/*
 * Mars Simulation Project
 * ToolWindow.java
 * @date 2022-07-23
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.toolwindow;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

/**
 * The ToolWindow class is an abstract UI window for a tool. Particular tool
 * windows should be derived from this.
 */
@SuppressWarnings("serial")
public abstract class ToolWindow extends JInternalFrame {

	// Data members
	private static final String SPACE = "          ";
	
	/** True if window is open. */
	protected boolean opened;

	/** The name of the tool the window is for. */
	protected String toolName;

	/** The main desktop. */
	protected MainDesktopPane desktop;

	/**
	 * Constructor.
	 *
	 * @param name    the name of the tool
	 * @param desktop the main desktop.
	 */
	protected ToolWindow(String name, MainDesktopPane desktop) {

		// use JInternalFrame constructor
		super(SPACE + name, true, // resizable
				true, // closable
				false, // maximizable
				false // iconifiable
		);

		// Initialize data members
		this.toolName = name;
		this.desktop = desktop;

		opened = false;

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// Set internal frame listener
		addInternalFrameListener(new ToolFrameListener());

		// Set the icon
		setIconImage();
	}

	/**
	 * Sets the icon image for the main window.
	 */
	public void setIconImage() {
		super.setFrameIcon(MainWindow.getLanderIcon());
	}

	/**
	 * Gets the tool name.
	 *
	 * @return tool name
	 */
	public String getToolName() {
		return toolName;
	}

	/**
	 * Get the parent main desktop
	 * @return
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}
	
	/**
	 * Sets the tool name.
	 *
	 * @param tool name
	 */
	public void setTitleName(String value) {
		setName(value);
	}

	/**
	 * Checks if the tool window has previously been opened.
	 *
	 * @return true if tool window has previously been opened.
	 */
	public boolean wasOpened() {
		return opened;
	}

	/**
	 * Sets if the window has previously been opened.
	 *
	 * @param opened true if previously opened.
	 */
	public void setWasOpened(boolean opened) {
		this.opened = opened;
	}
	
	/**
	 * Update window. This is overridden by subclasses
	 * @param pulse Clock step advancement
	 */
	public void update(ClockPulse pulse) {
		// Nothing to do in the base class
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		desktop = null;
	}
}
