package org.mars_sim.msp.ui.javafx.demo.webfxml;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;


/**
 * Created by rene on 28/03/16.
 */
public class Demo02HelloWorld extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // the root with all the components that we will show
        StackPane group = new StackPane();

        // Simple rectangle with background color
        Rectangle rectangle = new Rectangle(400, 60, Color.LIGHTGREEN);


        // Text with color and font decided
        Text text = new Text("Hello world!!!");
        text.setFill(Color.BLACK);
        text.setFont(Font.font(null, FontWeight.BOLD, 32));

        // Add component to the root, all the component are located one inside other. In this case, on the stackPane
        // put the rectangle and then the text
        group.getChildren().addAll(rectangle, text);


        // add the group to the scene and define the screen size and the background color
        Scene scene = new Scene(group, 500, 250, Color.LIGHTBLUE);

        // primaryStage is the root of the entire app, we will add a title on it
        primaryStage.setTitle("Hello JavaFX");
        // set the scene to show
        primaryStage.setScene(scene);
        // show the stage with the above configuration
        primaryStage.show();
    }

    /**
     * You should use the main method if you want to start the application using custom arguments
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
