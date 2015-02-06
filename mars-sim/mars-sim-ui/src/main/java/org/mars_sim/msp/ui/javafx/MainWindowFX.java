/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MenuScene;
import org.mars_sim.msp.ui.javafx.SettlementScene;
import org.mars_sim.msp.ui.swing.MainWindow;

/**
 * The MainWindowFX class is the primary JavaFX frame for the project.
 * It replaces the MainWindow class from version 4.0 on.
 */
public class MainWindowFX extends Thread {

	public final static String WINDOW_TITLE = Msg.getString(
		"MainWindow.title", //$NON-NLS-1$
		Simulation.VERSION + " build " + Simulation.BUILD
	);

	// Data members
	private static Stage primaryStage;
	
	static Scene menuScene;
	static Scene mainScene;
	static Scene settlementScene;
	
    private boolean cleanUI = true;
    
	/**
	 * Constructor.
	 */
	public MainWindowFX(Stage primaryStage, boolean cleanUI) {
		 this.cleanUI =  cleanUI;
		 MainWindowFX.primaryStage = primaryStage;
		 initAndShowGUI();
	}

    private static void initAndShowGUI() {
        
        menuScene = new MenuScene().createMenuScene();
        mainScene = new MainScene().createMainScene();
        settlementScene = new SettlementScene().createSettlementScene();

	    //StackPane root = new StackPane();
	    //root.getChildren().add(btn);
	    //Scene scene = new Scene(root, 1000, 300);
	
	    primaryStage.setTitle(WINDOW_TITLE);
	    primaryStage.setScene(mainScene);
	    primaryStage.show();
    
	    // TODO: recreate everything that's still in MainWindow right here in MainWindowFX
	    MainWindow mw = new MainWindow(true);
    }    
 
    
    public static void changeScene(int toscene) {
    	
    	switch(toscene) {
    
		    	case 1: {primaryStage.setScene(menuScene);
		}
		    	case 2: {primaryStage.setScene(mainScene);
		}
		    	case 3: {primaryStage.setScene(settlementScene);
		}

    }
    }   
    
}