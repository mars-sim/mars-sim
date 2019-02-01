package org.mars_sim.javafx.tools;

//using FranzXaver's SvgLoader from https://github.com/afester/FranzXaver
/*
import afester.javafx.svg.SvgLoader;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.InputStream;

// see https://stackoverflow.com/questions/12436274/svg-image-in-javafx-2-2#12440367

// For alternate implementation, see https://gist.github.com/ComFreek/b0684ac324c815232556

@SuppressWarnings("restriction")
public class SVGJavaFXDemo extends Application {

	// ERROR afester.javafx.svg.GradientFactory - GradientTransform for RadialGradient not yet implemented!
	
    @SuppressWarnings("restriction")
	public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        // load the svg file
        InputStream svgFile = 
              getClass().getResourceAsStream("/svg/lander_hab.svg");
        SvgLoader loader = new SvgLoader();
        Group svgImage = loader.loadSvg(svgFile);

        // Scale the image and wrap it in a Group to make the button 
        // properly scale to the size of the image  
        svgImage.setScaleX(1.5);
        svgImage.setScaleY(1.5);
        //svgImage.setRotate(value);
        Group graphic = new Group(svgImage);

        // create a button and set the graphics node
        Button button = new Button();
        button.setGraphic(graphic);

        // add the button to the scene and show the scene
        HBox layout = new HBox(button);
        HBox.setMargin(button, new Insets(10));
        Scene scene = new Scene(layout);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
*/