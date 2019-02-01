package org.mars_sim.javafx.tools;



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Borders");
        //primaryStage.initStyle(StageStyle.UTILITY); // .UNDECORATED);//
        
        Group root = new Group();
        
        Scene scene = new Scene(root, 600, 330, Color.WHITE); //TRANSPARENT);//
        
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.getClass().getResource("/fxui/css/demo/main.css").toExternalForm());

        
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        
        final String cssDefault = "-fx-border-color: blue;\n"
                + "-fx-border-insets: 5;\n"
                + "-fx-border-width: 3;\n"
                + "-fx-border-style: dashed;\n";
        final HBox pictureRegion = new HBox();
   
        pictureRegion.setStyle(cssDefault);
        gridpane.add(pictureRegion, 1, 1,10,10);
        
        root.getChildren().add(gridpane);        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
