package org.mars_sim.msp.demo;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.ShortStringConverter;

public class Main extends Application {
  //ObservableList<String> cursors = FXCollections.observableArrayList("1","2","3"); 
  
    @Override
    public void start(Stage stage) {
 
    	  Scene scene = new Scene(new Group(), 450, 250);
    	    ObservableList<String> list = FXCollections.observableArrayList("1","2","3","4");
    	    ComboBox<String> emailComboBox = new ComboBox<String>();
    	    emailComboBox.setItems(list);
    	    emailComboBox.setValue("A");
    	    
    	    StringConverter sc = new ShortStringConverter();
    	    emailComboBox.setConverter(sc);
    	    
    	    
    	    GridPane grid = new GridPane();
    	    grid.setVgap(4);
    	    grid.setHgap(10);
    	    grid.setPadding(new Insets(5, 5, 5, 5));
    	    grid.add(new Label("To: "), 0, 0);
    	    grid.add(emailComboBox, 1, 0);
    	    
    	    
    	    Group root = (Group) scene.getRoot();
    	    root.getChildren().add(grid);
    	    stage.setScene(scene);
    	    stage.show();
    	
    	/*  	
      ChoiceBox<String> choiceBox = new ChoiceBox<String>(cursors);
          
      StringConverter sc = new NumberStringConverter();
      
      choiceBox.setConverter(sc);
      
      VBox box = new VBox();
      box.getChildren().add(choiceBox);
      final Scene scene = new Scene(box,300, 250);
      scene.setFill(null);
      stage.setScene(scene);
      stage.show();
      */
    	
    	
    }

    public static void main(String[] args) {
        launch(args);
    }
}