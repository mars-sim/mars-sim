package org.mars_sim.msp.ui.javafx.callouts;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/** The demo of JavaFX animated callouts.
 * <pre>
 *     Animation steps:
 *     1) Draw head (x, y)
 *     2) Interpolate begin leader line 1 LineTo (x, y)
 *     3) Interpolate end leader line 2 LineTo (x, y)
 *     4) Interpolate main title [hbox with text]
 *     5) Interpolate a rectangle downward [hbox rect underneath main title]
 *     6) Interpolate sub title beside step 5 rect [hbox with text]
 * </pre>
 *
 */
public class Main extends Application {

	private static final String IMG = "/1024x768.jpg";//"/callouts.jpg";
	
    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Callouts");
        Pane root = new Pane();
        int maxWidth = 1024;

        primaryStage.setScene(new Scene(root, maxWidth, 760));
        primaryStage.setMaxWidth(maxWidth);
        primaryStage.setMinWidth(maxWidth);

        Image image = new Image(this.getClass().getResource(IMG).toExternalForm(), true);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(primaryStage.widthProperty());

        Callout callout1 = CalloutBuilder.create()
                .headPoint(600, 550)
                .leaderLineToPoint(400, 300)
                .endLeaderLineLeft()
                .mainTitle("STONEY CREEK")
                .subTitle("Pasadena, MD")
                .pause(5000)
                .build();

        Callout callout2 = CalloutBuilder.create()
                .headPoint(200, 550)
                .leaderLineToPoint(400, 350.5)
                .endLeaderLineRight()
                .mainTitle("STONEY CREEK")
                .subTitle("Pasadena, MD")
                .pause(5000)
                .build();

        Callout callout3 = CalloutBuilder.create()
                .headPoint(498, 186)
                .leaderLineToPoint(375, 406)
                .endLeaderLineLeft()
                .mainTitle("BLUE SKY")
                .subTitle("October 2017")
                .pause(5000)
                .build();

        Callout callout4 = CalloutBuilder.create()
                .headPoint(667, 400)
                .leaderLineToPoint(778, 576)
                .endLeaderLineRight()
                .mainTitle("BOATS")
                .subTitle("On Lift")
                .pause(5000)
                .build();

        root.getChildren().add(imageView);
        root.getChildren().addAll(callout1, callout2, callout3, callout4);

        // Animate 1-4 callouts to point out things in the picture
        EventHandler<KeyEvent> calloutKeyHandler = keyEvent -> {
            switch (keyEvent.getCharacter()) {
                case "1":
                    callout1.play();
                    break;
                case "2":
                    callout2.play();
                    break;
                case "3":
                    callout3.play();
                    break;
                case "4":
                    callout4.play();
                    break;
                default:
                    callout1.play();
            }
        };

        root.addEventHandler(KeyEvent.KEY_TYPED, calloutKeyHandler);
        root.requestFocus();

        // Observe mouse clicked coordinates
        root.setOnMouseClicked(mouseevent -> {
            System.out.println(mouseevent);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
