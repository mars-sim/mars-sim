package com.mars_sim.ui.swing.astroarts;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.time.MasterClock;


public class OrbitViewerMain {

	private OrbitViewer orbitViewer = null;
	
	/**
	 * Initialization.
	 */
	private OrbitViewerMain()  { 
	    
		JFrame frame = new JFrame("Orbit Viewer");
		frame.setSize(1024, 1024);

		var config = SimulationConfig.loadConfig();
		var masterClock = new MasterClock(config, 10);

		orbitViewer = new OrbitViewer(masterClock);
		frame.setLayout(new BorderLayout());

		frame.getContentPane().add(orbitViewer);

        frame.setVisible(true);
		frame.repaint();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	}

    public static void main(String[] args) {
    	new OrbitViewerMain();
    }
}
