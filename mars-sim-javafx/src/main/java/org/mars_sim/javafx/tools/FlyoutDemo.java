package org.mars_sim.javafx.tools;

import org.mars_sim.javafx.Flyout;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Demonstrates the {@link Flyout} widget.
 * 
 * You chose the fly out direction (see {@link Flyout.Side}) !!
 * <p>
 * Note: Test by substituting other {@link Flyout.Side}s below in the
 * {@link #start(Stage)} method.
 * 
 * @author cogmission
 */
public class FlyoutDemo extends Application {
    private ToggleButton stuffChooserTrigger;
    
    private Flyout flyout;
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane pane = new BorderPane();
        
        ToolBar  toolBar = new ToolBar();
        
        Label fileLabel = new Label("File");
        
        flyout = createFlyout();
        
        // Could be TOP, LEFT, RIGHT too!
        flyout.setFlyoutSide(Flyout.Side.BOTTOM);
        
        toolBar.getItems().addAll(
            fileLabel,
            new Separator(),
            flyout
        );
        
        pane.setTop(toolBar);
        
        Scene scene = new Scene(pane, 600, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Creates and returns a {@link Flyout}
     * @return  a new {@link Flyout}
     */
    public Flyout createFlyout() {
        stuffChooserTrigger = new ToggleButton("Create / Select Item");
        stuffChooserTrigger.setOnAction(e -> {
            if(stuffChooserTrigger.isSelected()) {
                flyout.flyout();
            }else{
                flyout.dismiss();
            }
        });
        
        Flyout retVal = new Flyout(stuffChooserTrigger, getStuffControl(), null);
        
        return retVal;
    }
    
    /**
     * Creates and returns demo widget
     * @return  the control for demo-ing widget
     */
    public GridPane getStuffControl() {
        GridPane gp = new GridPane();
        gp.setPadding(new Insets(5, 5, 5, 5));
        gp.setHgap(5);
        
        ComboBox<String> stuffCombo = new ComboBox<>();
        stuffCombo.setEditable(true);
        stuffCombo.setPromptText("Add stuff...");
        stuffCombo.getItems().addAll(
            "Stuff",
            "contained",
            "within",
            "the",
            "combo"
        );
        
        Label l = new Label("Select or enter example text:");
        l.setFont(Font.font(l.getFont().getFamily(), 10));
        l.setTextFill(Color.WHITE);
        Button add = new Button("Add");
        add.setOnAction(e -> stuffCombo.getItems().add(stuffCombo.getSelectionModel().getSelectedItem()));
        Button del = new Button("Clear");
        del.setOnAction(e -> stuffCombo.getSelectionModel().clearSelection());
        gp.add(l, 0, 0, 2, 1);
        gp.add(stuffCombo, 0, 1, 2, 1);
        gp.add(add, 2, 1);
        gp.add(del, 3, 1);
        
        return gp;
    }
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        launch(args);
    }
}
