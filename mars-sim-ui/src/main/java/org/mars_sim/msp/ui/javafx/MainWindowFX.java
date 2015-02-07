/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MenuScene;
import org.mars_sim.msp.ui.javafx.SettlementScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
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
	private Stage primaryStage;
	
	private static Stage stage;
	
	static Scene menuScene;
	static Scene mainScene;
	static Scene settlementScene;
	
    public static String screen1ID = "mainMenu";
    public static String screen1File = "MainMenu.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "Credits.fxml";
    
    
    private boolean cleanUI = true;
    
	/** The main desktop. */
	//private MainDesktopPane desktop;

	
	/**
	 * Constructor.
	 */
	public MainWindowFX(Stage primaryStage, boolean cleanUI) {
		 this.cleanUI =  cleanUI;
		 this.primaryStage = primaryStage;
		 initAndShowGUI();
	}

    private void initAndShowGUI() {        
        
        ScreensSwitcher switcher = new ScreensSwitcher(this);
        switcher.loadScreen(MainWindowFX.screen1ID, MainWindowFX.screen1File);
        switcher.loadScreen(MainWindowFX.screen2ID, MainWindowFX.screen2File);
        switcher.loadScreen(MainWindowFX.screen3ID, MainWindowFX.screen3File);
        
        switcher.setScreen(MainWindowFX.screen1ID);
        
        Group root = new Group();
        root.getChildren().addAll(switcher);
        Scene scene = new Scene(root);
        
	    primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
               
        // NOTE: mainScene, menuScene and settlementScene are now listed in MainMenu.fxml
        menuScene = new MenuScene().createMenuScene();
        mainScene = new MainScene().createMainScene();
        settlementScene = new SettlementScene().createSettlementScene();
	    //stage.setScene(menuScene);
	    //stage.show();
        
    
	    // TODO: recreate everything that's still in MainWindow right here in MainWindowFX
	    MainWindow mw = new MainWindow(true);	    
	    MainDesktopPane desktop = new MainDesktopPane(mw);		
		// Open all initial windows.
		desktop.openInitialWindows();
    }    
 
    public void runOne() {
        
    	stage = new Stage();
 	    stage.setTitle(WINDOW_TITLE);
 	    stage.setScene(mainScene);
        stage.show();
    }
    
    public void runTwo() {
        
    	stage = new Stage();
 	    stage.setTitle(WINDOW_TITLE);
 	    stage.setScene(menuScene);
        stage.show();
    }
    
    public void runThree() {
        
    	stage = new Stage();
 	    stage.setTitle(WINDOW_TITLE);
 	    stage.setScene(settlementScene);
        stage.show();
    }
    
    public static void changeScene(int toscene) {
    	
    	switch(toscene) {
    
		    	case 1: {stage.setScene(menuScene);
		}
		    	case 2: {stage.setScene(mainScene);
		}
		    	case 3: {stage.setScene(settlementScene);
		}

    }
    }   
    
    
}