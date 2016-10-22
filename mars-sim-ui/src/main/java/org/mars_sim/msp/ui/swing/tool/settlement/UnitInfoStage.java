/**
 * Mars Simulation Project
 * UnitInfoStage.java
 * @version 3.1.0 2016-10-21
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.CustomScroll;


public class UnitInfoStage {

	private MainDesktopPane desktop;
	
    public UnitInfoStage(MainDesktopPane desktop) {		
    	this.desktop = desktop;	
    }

    public BorderPane init(String unitName, String unitType, String unitDescription) {

    	//this.setSize(350, 400); // undecorated 301, 348 ; decorated : 303, 373

        BorderPane mainPane = new BorderPane();
/*    
        // Creating the text Input
        TextField tf1 = new TextField();
        tf1.setEditable(false);
        tf1.setPrefWidth(400);
        tf1.setText(unitName);
*/ 
        String cssFile = null;
        
        if (desktop.getMainScene().getTheme() == 6)
        	cssFile = "/fxui/css/snowBlue.css";
        else
        	cssFile = "/fxui/css/nimrodskin.css";
		
        VBox box0 = new VBox();
        box0.setAlignment(Pos.CENTER);
        
        Label name = new Label(unitName);
        name.setTextAlignment(TextAlignment.CENTER);
        name.setContentDisplay(ContentDisplay.TOP);
        name.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        name.getStyleClass().add("label-large");	
        

	    box0.getChildren().addAll(name);
	    
        mainPane.setTop(box0);

        String type = "TYPE :";
        String description = "DESCRIPTION :";
 
        VBox box1 = new VBox();
        TextArea ta = new TextArea();
        ta.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        ta.getStyleClass().add("label-small");	
        
        ta.setEditable(false);
        ta.setWrapText(true);
        box1.getChildren().add(ta);

        ta.setText(System.lineSeparator() + type + System.lineSeparator() + unitType + System.lineSeparator() + System.lineSeparator());
        ta.appendText(description + System.lineSeparator() + unitDescription + System.lineSeparator() + System.lineSeparator());
        
        mainPane.setCenter(ta);
        
        return mainPane;
        
    }

}