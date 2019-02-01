package org.mars_sim.javafx.settlement;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class GreenhouseOperation extends Application {
    public void start(Stage stage) {
        Circle circ = new Circle(40, 40, 30);
        Group root = new Group(circ);
        Scene scene = new Scene(root, 400, 300);

    	//MyBean myBean = new MyBean(); // get an instance of the bean to be edited
    	//Node fxForm = new FXForm(myBean);  // create the FXForm node for your bean
    	//root.getChildren().add(fxForm);  // add it to your scene graph

        stage.setTitle("My JavaFX Applcation");
        stage.setScene(scene);
        stage.show();
    }
}
