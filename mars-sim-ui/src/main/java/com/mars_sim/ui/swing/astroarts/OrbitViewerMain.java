package com.mars_sim.ui.swing.astroarts;

import com.mars_sim.core.astroarts.ATime;

public class OrbitViewerMain implements Runnable {

	private OrbitViewer orbitViewer = null;
	
	/**
	 * Initialization.
	 */
	private OrbitViewerMain()  { 
	    
//		JFrame frame = new JFrame("Orbit Viewer");
//		frame.setSize(1024, 1024);
//
//		frame.setLayout(new BorderLayout());
//		
////		frame.getContentPane().add(createGUI(), BorderLayout.CENTER);
//		
//		frame.setBackground(Color.BLACK);
//		  
//		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

		// Player Thread
		
		orbitViewer = new OrbitViewer() ;
		
//		frame.pack();
//        frame.setVisible(true);
	}

    public static void main(String[] args) {
    	new OrbitViewerMain();
    }
    
	/**
	 * Play forever
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
			ATime atime = orbitViewer.getAtime();
			atime.changeDate(orbitViewer.timeStep, orbitViewer.playDirection);
			orbitViewer.setNewDate(atime);
		}
	}
}
