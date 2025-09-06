package com.mars_sim.ui.swing.astroarts;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class OrbitViewerMain {

	/**
	 * Player thread
	 */
	private OrbitPlayer		orbitPlayer;
	private transient Thread			playerThread = null;

	/**
	 * Initialization.
	 */
	private OrbitViewerMain() { 
	    
		JFrame frame = new JFrame("Orbit Viewer");
		frame.setSize(1024, 1024);

		frame.setLayout(new BorderLayout());
		
//		frame.getContentPane().add(createGUI(), BorderLayout.CENTER);
		
		frame.setBackground(Color.BLACK);
		  
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

		// Player Thread
//		orbitPlayer = new OrbitPlayer(this);
		playerThread = null;
		
		frame.pack();
        frame.setVisible(true);
	}

    public static void main(String[] args) {
    	new OrbitViewerMain();
    }
}
