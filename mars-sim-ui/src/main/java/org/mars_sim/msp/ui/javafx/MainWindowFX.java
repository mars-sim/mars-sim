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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.swing.JFrame;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;

import org.mars_sim.msp.ui.javafx.MainWindowFXMenu;

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

		// TODO: we need to load some custom CSS here to replace Modena UI.
		
		
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
        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
        Text  text  =  new  Text();
        
        text.setX(60);
        text.setY(150);
        text.setFont(new Font(28));
        text.setText("Rebuilding our UI in JavaFX!");

        root.getChildren().add(text);
        MainWindowFXMenu menuBar = new MainWindowFXMenu(null);
        

 
        root.getChildren().addAll(menuBar); 

        return (scene);
    }
}