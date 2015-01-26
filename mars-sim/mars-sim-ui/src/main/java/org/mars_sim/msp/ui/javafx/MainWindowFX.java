/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.07 2015-01-10
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * The MainWindowFX class is the primary JavaFX frame for the project.
 * It replaces the MainWindow class from version 4.0 on.
 */
public class MainWindowFX {

	public static final String WINDOW_TITLE = "4.0";


	/**
	 * Constructor.
	 
	public MainWindowFX() {
        Text text = new Text(10, 40, "Mars Simulation Project "+WINDOW_TITLE);
        text.setFont(new Font(40));
        Stage stage = new Stage();
        Scene scene = new Scene(new Group(text));

        stage.setTitle("JavaFX MSP testbed!"); 
        stage.setScene(scene); 
        stage.sizeToScene(); 
        stage.show(); 

	}
	*/

    private static void initAndShowGUI() {
        // This method is invoked on the EDT thread
        JFrame frame = new JFrame("Mars Simulation Project " + WINDOW_TITLE);
        final JFXPanel fxPanel = new JFXPanel();
        frame.add(fxPanel);
        frame.setSize(300, 200);
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
    }

    private static Scene createScene() {
        Group  root  =  new  Group();
        Scene  scene  =  new  Scene(root, Color.ALICEBLUE);
        Text  text  =  new  Text();
        
        text.setX(40);
        text.setY(100);
        text.setFont(new Font(25));
        text.setText("JavaFX MSP testbed!");

        root.getChildren().add(text);

        return (scene);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                initAndShowGUI();
            }
        });
    }
}