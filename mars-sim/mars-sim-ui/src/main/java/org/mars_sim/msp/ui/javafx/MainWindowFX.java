/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-26
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.JFrame;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;

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
        Scene scene = createScene();
        fxPanel.setScene(scene);
        fxPanel.repaint();
    }

    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, Color.CORAL);
        Text  text  =  new  Text();
        
        text.setX(60);
        text.setY(150);
        text.setFont(new Font(25));
        text.setText("Building UI in JavaFX step by step!");

        root.getChildren().add(text);
        MenuBar menuBar = new MenuBar();
        
        // --- Menu File
        Menu menuFile = new Menu("File");
        MenuItem newItem = new MenuItem("New...");
        MenuItem openItem = new MenuItem("Open...");
        MenuItem openAutoSaveItem = new MenuItem("Open autosave");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As...");
        MenuItem exitItem = new MenuItem("Exit");
        SeparatorMenuItem separatorMenuItem1 = new SeparatorMenuItem();

        menuFile.getItems().addAll(newItem, separatorMenuItem1, openItem, openAutoSaveItem, saveItem, saveAsItem, exitItem);
        
        // --- Menu Tools
        Menu menuTools = new Menu("Tools");
        MenuItem marsNavigatorItem = new MenuItem("Mars Navigator");
        MenuItem searchToolItem = new MenuItem("Search Tool");
        MenuItem timeToolItem = new MenuItem("Time Tool");
        MenuItem monitorToolItem = new MenuItem("Monitor Tool");
        MenuItem missionToolItem = new MenuItem("Mission Tool");
        MenuItem settlementMapTool = new MenuItem("Settlement Map Tool");
        MenuItem scienceTool = new MenuItem("Science Tool");
        MenuItem resupplyTool = new MenuItem("Settlement Map Tool");

        menuTools.getItems().addAll(marsNavigatorItem, searchToolItem,timeToolItem, monitorToolItem, missionToolItem,settlementMapTool, scienceTool, resupplyTool );
        
        // --- Menu Settings
        Menu menuSettings = new Menu("Settings");
        MenuItem showUnitBarItem = new MenuItem("Show Unit Bar");
        MenuItem showToolBarItem = new MenuItem("Show Tool Bar");
        SeparatorMenuItem separatorMenuItem2 = new SeparatorMenuItem();
        MenuItem volumeUpItem = new MenuItem("Volume Up");
        MenuItem volumeDownItem = new MenuItem("Volume Down");

        menuTools.getItems().addAll(showUnitBarItem,showToolBarItem, separatorMenuItem2, volumeUpItem, volumeDownItem);
        
        // --- Menu Notification
        Menu menuNotification = new Menu("Notification");
        
        // --- Menu Help
        Menu menuHelp = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        MenuItem tutorialItem = new MenuItem("Tutorial");
        SeparatorMenuItem separatorMenuItem3 = new SeparatorMenuItem();
        MenuItem userGuideItem = new MenuItem("User Guide");

        menuTools.getItems().addAll(aboutItem, tutorialItem,separatorMenuItem3, userGuideItem);

 
        menuBar.getMenus().addAll(menuFile, menuTools, menuSettings, menuNotification, menuHelp);
        root.getChildren().addAll(menuBar); 

        return (scene);
    }
}