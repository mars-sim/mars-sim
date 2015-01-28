/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.swing.JFrame;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.MainWindowFXMenu;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MenuScene;
import org.mars_sim.msp.ui.javafx.SettlementScene;

/**
 * The MainWindowFX class is the primary JavaFX frame for the project.
 * It replaces the MainWindow class from version 4.0 on.
 */
public class MainWindowFX {

	public final static String WINDOW_TITLE = Msg.getString(
		"MainWindow.title", //$NON-NLS-1$
		Simulation.VERSION + " build " + Simulation.BUILD
	);

	// Data members
	private static JFrame frame;
	
	/**
	 * Constructor.
	 */
	public MainWindowFX(boolean cleanUI) {
		// initAndShowGUI() will be on EDT since MainWindowFX is put on EDT in MarsProject.java

		
		initAndShowGUI();
	}
	

    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        frame = new JFrame(WINDOW_TITLE);
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(640, 480);
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
    	
        Scene menuScene = new MenuScene().createMenuScene();
        Scene mainScene = new MainScene().createMainScene();
        Scene settlementScene = new SettlementScene().createSettlementScene();
        
        // We start with the menu
        fxPanel.setScene(mainScene);
        fxPanel.repaint();
    }

    private static Scene createMenuScene() {
        Group  root  =  new  Group();
        Scene  menuScene  =  new  Scene(root);
        menuScene.getStylesheets().addAll("/fxui/css/menuskin.css");
        // testing out scene switching
        ImageView bg1 = new ImageView();
        bg1.setImage(new Image("/images/mars.png"));        
        root.getChildren().add(bg1);

        root.getChildren().add(new Button("Press me to go to Main Scene"));
        return (menuScene);
    }
    
    
    private static Scene createMainScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root);
		scene.getStylesheets().addAll("/fxui/css/mainskin.css");		

        MainWindowFXMenu menuBar = new MainWindowFXMenu(null);
        ImageView bg1 = new ImageView();
        bg1.setImage(new Image("/images/background.png"));        
        root.getChildren().add(bg1);

        root.getChildren().add(menuBar); 
//        root.getChildren().add(new Button("Press me to go to Settlement Scene"));

        return (scene);
    }
    
    private static Scene createSettlementScene() {
        Group  root  =  new  Group();
        Scene  settlementScene  =  new  Scene(root);
        settlementScene.getStylesheets().addAll("/fxui/css/settlementskin.css");		
        root.getChildren().add(new Button("Press me to go to Menu Scene"));

        return (settlementScene);
    }
    
}