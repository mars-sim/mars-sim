/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MenuScene;
import org.mars_sim.msp.ui.javafx.SettlementScene;

/**
 * The MainWindowFX class is the primary JavaFX frame for the project.
 * It replaces the MainWindow class from version 4.0 on.
 */
public class MainWindowFX_bak extends Thread {

	public final static String WINDOW_TITLE = Msg.getString(
		"MainWindow.title", //$NON-NLS-1$
		Simulation.VERSION + " build " + Simulation.BUILD
	);

	// Data members
	private static JFrame frame;
	static Scene menuScene;
	static Scene mainScene;
	static Scene settlementScene;
    public static JFXPanel fxPanel = new JFXPanel();

    private boolean cleanUI = true;
	/**
	 * Constructor.
	 */
	public MainWindowFX_bak(boolean cleanUI) {
		 this.cleanUI =  cleanUI;
	}
	
	public void run() {
        // starting the simulation
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
        		initAndShowGUI();
            }
        });

	}

    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        frame = new JFrame(WINDOW_TITLE+ " (JavaFX)");
        //final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(1000, 300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });
    }

    private static void initFX(JFXPanel fxPanel) {
        // This method is invoked on the JavaFX thread
    	
         menuScene = new MenuScene().createMenuScene();
         mainScene = new MainScene().createMainScene();
         settlementScene = new SettlementScene().createSettlementScene();
        
        // We start with the menu
         fxPanel.setScene(mainScene);
         fxPanel.repaint();
         }    
 
    public static JFXPanel getPanel()  { 
    	return fxPanel;
    }
    public static void changeScene(int toscene) {
    switch(toscene) {
    
    	case 1: {fxPanel.setScene(menuScene);
}
    	case 2: {fxPanel.setScene(mainScene);
}
    	case 3: {fxPanel.setScene(settlementScene);
}
        fxPanel.repaint();
    }
    }   
    
}