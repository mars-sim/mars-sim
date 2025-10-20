/*
 * Mars Simulation Project
 * OrbitWindow.java
 * @date 2025-10-18
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.astroarts;

import javax.swing.WindowConstants;

import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * The Orbit Viewer Window.
 */
@SuppressWarnings("serial")
public class OrbitWindow extends ToolWindow {

	public static final String NAME = "astro";
	public static final String ICON = "astro";
    private OrbitViewer orbitViewer;
    
    /**
	 * Initialization.
	 */
	public OrbitWindow(MainDesktopPane desktop) {
		// Call ModalInternalFrame constructor
        super(NAME, "Orbit Viewer", desktop);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        orbitViewer = new OrbitViewer(desktop.getSimulation().getMasterClock());    

        setContentPane(orbitViewer);
		desktop.add(this);
			
		pack();
        setVisible(true);
    }

    
	/**
	 * Destroy.
	 */
	@Override
	public void destroy() {
		orbitViewer.destroy();
		super.destroy();
	}
}
