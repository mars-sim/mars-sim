/**
 * Mars Simulation Project
 * UnitInfoStage.java
 * @version 3.1.0 2016-10-21
 * @author Manny Kung
 */

package org.mars_sim.javafx.settlement;

import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class UnitDescriptionStage {

	//private MainDesktopPane desktop;
	private TextArea ta;
	private VBox box1, box0;
	private BorderPane mainPane;
	private Label name;

    public UnitDescriptionStage() {//MainDesktopPane desktop) {
    	//this.desktop = desktop;
    }

    public BorderPane init(String unitName, String unitType, String unitDescription) {

    	//this.setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373

        mainPane = new BorderPane();

        name = new Label(unitName);
        name.setPadding(new Insets(5,5,5,5));
        name.setTextAlignment(TextAlignment.CENTER);
        name.setContentDisplay(ContentDisplay.TOP);

        box0 = new VBox();
        box0.setAlignment(Pos.CENTER);
        box0.setPadding(new Insets(5,5,5,5));
	    box0.getChildren().addAll(name);

        mainPane.setTop(box0);

        String type = "TYPE :";
        String description = "DESCRIPTION :";

        box1 = new VBox();
        ta = new TextArea();

        ta.setEditable(false);
        ta.setWrapText(true);
        box1.getChildren().add(ta);

        ta.setText(System.lineSeparator() + type + System.lineSeparator() + unitType + System.lineSeparator() + System.lineSeparator());
        ta.appendText(description + System.lineSeparator() + unitDescription + System.lineSeparator() + System.lineSeparator());
        ta.setScrollTop(300);

        applyTheme();

        mainPane.setCenter(ta);

        return mainPane;

    }

    public void applyTheme() {
        String cssFile = null;
        int theme = 0;// MainScene.getTheme(); 
        if (theme == 0 || theme == 6)
        	cssFile = MainDesktopPane.BLUE_CSS;
        else if (theme == 7)
        	cssFile = MainDesktopPane.ORANGE_CSS;
        
        name.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        name.getStyleClass().add("label-large");

        ta.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        //ta.getStyleClass().add("text-area");
        ta.setId("unit_description");

        mainPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        mainPane.getStyleClass().add("borderpane");

        box1.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        box1.getStyleClass().add("borderpane");

        box0.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        box0.getStyleClass().add("borderpane");

    }

}