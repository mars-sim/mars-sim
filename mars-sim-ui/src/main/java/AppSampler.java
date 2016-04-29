
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/**
 *
 * @author cdea
 */
public class AppSampler extends Application{


     /**
     * @param args the command line arguments
     */
    private Point2D anchorPt;
    private Point2D previousLocation;
    
    public static void main(String[] args) {
        
        Application.launch(args);
       
    }
    
    @Override
    public void start(final Stage primaryStage) throws IOException {
        System.setProperty("sampler.mode", "true");
        URL url = AppSampler.class.getResource("AppSampler.fxml");
        AnchorPane mainViewPane = FXMLLoader.load(url);

        primaryStage.centerOnScreen();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        double sceneWidth = mainViewPane.getPrefWidth() + 30;
        double sceneHeight = mainViewPane.getPrefHeight()+ 30;

        Group root = new Group();
        Scene scene = new Scene(root, sceneWidth, sceneHeight, Color.rgb(0, 0, 0, 0));

       // application area
       @SuppressWarnings("restriction")
	Rectangle applicationArea = RectangleBuilder.create()
                .width(sceneWidth - 10)
                .height(sceneHeight - 10)
                .arcWidth(20)
                .arcHeight(20)
                .fill(Color.rgb(0, 0, 0, .80))
                .x(0)
                .y(0)
                .strokeWidth(2)
                .stroke(Color.rgb(255, 255, 255, .70))
                .build();
       root.getChildren().add(applicationArea);
       mainViewPane.setLayoutX(10);
       mainViewPane.setLayoutY(10);
       
       root.getChildren().add(mainViewPane);
       
       // starting initial anchor point
       scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                anchorPt = new Point2D(event.getScreenX(), event.getScreenY());
            }
        });
        
        // dragging the entire stage
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                if (anchorPt != null && previousLocation != null) {
                    primaryStage.setX(previousLocation.getX() + event.getScreenX() - anchorPt.getX());
                    primaryStage.setY(previousLocation.getY() + event.getScreenY() - anchorPt.getY());                    
                }
            }
        });
        
        // set the current location
        scene.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event){
                previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY());
            }
        });
        
        // close button
        final Group closeApp = new Group();
        Node closeRect = RectangleBuilder.create()
                .width(25)
                .height(25)
                .arcWidth(15)
                .arcHeight(15)
                .fill(Color.rgb(0, 0, 0, .80))
                .stroke(Color.WHITE)
                .build();
        
        Text closeXmark = new Text(8.5, 16.5, "X");
        closeXmark.setStroke( Color.WHITE);
        closeXmark.setFill(Color.WHITE);
        closeXmark.setStrokeWidth(2);
        closeApp.translateXProperty().bind(scene.widthProperty().subtract(40));
        closeApp.setTranslateY(5);
        closeApp.getChildren().addAll(closeRect, closeXmark);
        closeApp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Platform.exit();
            }
        });
        
        root.getChildren().add(closeApp);

       
       
       
       primaryStage.setScene(scene);
       primaryStage.show();
       previousLocation = new Point2D(primaryStage.getX(), primaryStage.getY()); 
    }
    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
