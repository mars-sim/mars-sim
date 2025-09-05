/*
 * Mars Simulation Project
 * ToolWindow.java
 * @date 2022-07-23
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool_window;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MainWindow;

/**
 * The ToolWindow class is an abstract UI window for a tool. Particular tool
 * windows should be derived from this.
 */
@SuppressWarnings("serial")
public abstract class ToolWindow extends JInternalFrame {
	
	/** True if window is open. */
	protected boolean opened;

	/** The name of the tool the window is for. */
	protected String toolName;

	/** The main desktop. */
	protected MainDesktopPane desktop;

	/**
	 * Constructor.
	 *
	 * @param name    the internal name of the tool
	 * @param title  The window title
	 * @param desktop the main desktop.
	 */
	protected ToolWindow(String name, String title, MainDesktopPane desktop) {

		// use JInternalFrame constructor
		super(title, true, // resizable
				true, // closable
				false, // maximizable
				false // iconifiable
		);

		// Initialize data members
		this.toolName = name;
		this.desktop = desktop;

		opened = false;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

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
	 * Updates window. 
	 * Note: This is overridden by subclasses.
	 * 
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
