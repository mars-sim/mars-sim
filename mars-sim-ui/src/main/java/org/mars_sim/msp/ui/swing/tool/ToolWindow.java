/**
 * Mars Simulation Project
 * ToolWindow.java
 * @version 3.07 2015-06-05

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool;

import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;

import org.mars_sim.msp.ui.javafx.MainSceneMenu;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javafx.scene.control.CheckMenuItem;

/**
 * The ToolWindow class is an abstract UI window for a tool.
 * Particular tool windows should be derived from this.
 */
public abstract class ToolWindow
extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The name of the tool the window is for. */
	protected String name;
	private CheckMenuItem item;
	private MainSceneMenu msm;
	/** The main desktop. */
	protected MainDesktopPane desktop;
	/** True if window is open. */
	protected boolean opened;

	/**
	 * Constructor.
	 * @param name the name of the tool
	 * @param desktop the main desktop.
	 */
	public ToolWindow(String name, MainDesktopPane desktop) {

		// use JInternalFrame constructor
		super(
			name,
			true, // resizable
			true, // closable
			false, // maximizable
			false // iconifiable
		);

		// Initialize data members
		this.name = name;
		this.desktop = desktop;
		opened = false;

		if (desktop.getMainScene() != null)
			msm = desktop.getMainScene().getMainSceneMenu();

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// Set internal frame listener
		addInternalFrameListener(new ToolFrameListener());
	}

	/**
	 * Gets the tool name.
	 * @return tool name
	 */
	public String getToolName() {
		return name;
	}

	/**
	 * Checks if the tool window has previously been opened.
	 * @return true if tool window has previously been opened.
	 */
	public boolean wasOpened() {
		return opened;
	}

	/**
	 * Sets if the window has previously been opened.
	 * @param opened true if previously opened.
	 */
	public void setWasOpened(boolean opened) {
		this.opened = opened;
	}

	/**
	 * Update window.
	 */
    // 2015-06-05 Added checking if the tool window is invisible/closed while its check menu item is still toggle on
	public void update() {
		if(!this.isVisible()) {
			if (desktop.getMainScene() != null) {
				if (msm == null)
					msm = desktop.getMainScene().getMainSceneMenu();
				item = msm.getCheckMenuItem(name);
				if (item != null)
					// if item is a guide window, it will be null
					if (item.isSelected())
						msm.setCheckMenuItem(name);
			}
		}
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {}
}