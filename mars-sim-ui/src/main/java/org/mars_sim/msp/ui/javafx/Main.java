/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mars_sim.msp.ui.javafx;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author avinerbi
 */
public class Main extends Application
{
    
    FrostedPanel panel;

    
    @Override
    public void start(Stage primaryStage)
    {
        Group root = new Group();
        Dash dash = new Dash();

        Scene scene = new Scene(root, 800, 600);

        dash.setup(scene);
        
        panel = new FrostedPanel(dash);
        
        dash.prefWidthProperty().bind(scene.widthProperty());
        dash.prefHeightProperty().bind(scene.heightProperty());
        
        root.getChildren().add(dash);
        root.getChildren().add(panel);
        
        primaryStage.setTitle("Frosted panel");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }

}
