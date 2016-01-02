package org.mars_sim.msp.ui.swing.tool;

import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 *
 * @author Narayan G. Maharjan
 * @see  Blog 
 */
public class AutoFillTextBoxDemo extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AutoFillTextBox without FilterMode");

        //SAMPLE DATA
        ObservableList data = FXCollections.observableArrayList();
        String[] s = new String[]{"apple","ball","cat","doll","elephant",
            "fight","georgeous","height","ice","jug",
             "aplogize","bank","call","done","ego",
             "finger","giant","hollow","internet","jumbo",
             "kilo","lion","for","length","primary","stage",
             "scene","zoo","jumble","auto","text",
            "root","box","items","hip-hop","himalaya","nepal",
            "kathmandu","kirtipur","everest","buddha","epic","hotel"};

            for(int j=0; j<s.length; j++){
                data.add(s[j]);
            }

        //Layout
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        //CustomControl
        final AutoFillTextBox box = new AutoFillTextBox(data);
        //Label
        Label l = new Label("AutoFillTextBox: ");
        l.translateYProperty().set(5);
        l.translateXProperty().set(5); 

        hbox.getChildren().addAll(l,box);
        Scene scene = new Scene(hbox,300,200);

        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("/css/autofill.css").toExternalForm());
        scene.getStylesheets().addAll("/css/autofill.css");

        primaryStage.show();

    }
}