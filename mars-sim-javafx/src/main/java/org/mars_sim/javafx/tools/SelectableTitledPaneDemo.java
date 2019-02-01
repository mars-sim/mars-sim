
package org.mars_sim.javafx.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
//import javafx.scene.control.ComboBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SelectableTitledPaneDemo extends Application {
	
	String[] strs = new String[]{"A", "B", "C", "D"};
	
	@Override 
	public void start(Stage stage) {
	        
		  List<String> list = new ArrayList<>(Arrays.asList(strs));
		  	  
//		  ComboBox<?> combo = ComboBoxBuilder.create().
//		            prefWidth(150).
//		            //items(list).
//		            items(FXCollections.observableArrayList(list)).//"aa", "bb", "bb")); 		  
//		            //promptText(resourceBundle.getString("search.prompt.owner")).
//		            promptText("Choice").
//		            build();
		 
		  ComboBox<String> combo = new ComboBox<String>();
		  combo.setItems(FXCollections.observableArrayList(list));
		  combo.setPrefWidth(150);
		  combo.setPromptText("Choice");

		  //combo.setItems((ObservableList<?>) FXCollections.observableArrayList(list));//"aa", "bb", "bb"));
		  
		  
		  SelectableTitledPane ownerParams = new SelectableTitledPane(
				  //resourceBundle.getString("search.checkbox.owner"),
				  "checkbox",
				  combo);

	        
	        StackPane pane = new StackPane();
	        pane.setBackground(null);
	        pane.setPadding(new Insets(10, 10, 10, 10));
	        pane.getChildren().addAll( ownerParams);

	        Scene scene = new Scene(pane);
	        stage.setScene(scene);
	        stage.show();
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
	    
	
}

