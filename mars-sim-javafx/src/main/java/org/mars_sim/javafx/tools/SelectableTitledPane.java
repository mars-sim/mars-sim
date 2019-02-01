package org.mars_sim.javafx.tools;

//import com.sun.javafx.scene.control.skin.TitledPaneSkin;
//import javafx.scene.control.skin.TitledPaneSkin;

import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TitledPane;

@SuppressWarnings("restriction")
public class SelectableTitledPane extends TitledPane {
	 
	  private CheckBox checkBox;
	 
	  public SelectableTitledPane(String title, Node content) {
	    super(title, content);
	    checkBox = new CheckBox(title);
	    checkBox.selectedProperty().
	            bindBidirectional(this.expandedProperty());
	    setExpanded(false);
	    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	    setGraphic(checkBox);
	    //setSkin(new TitledPaneSkin(this));
	    lookup(".arrow").
	            setVisible(false);
	    lookup(".title").
	            setStyle("-fx-padding: 0 0 4 -10;"
	            		+ "-fx-font-color: white;"	            		
	            + "-fx-background-color: black;");
	    lookup(".content").
	            setStyle("-fx-background-color: black;"
	            		+ "-fx-font-color: white;"
	            		+ "-fx-font-smoothing-type: lcd;"
	            		+ "-fx-padding:  0.2em 0.2em 0.2em 1.316667em;");
	  }
	 
	  public BooleanProperty getSelectedProperty() {
	    return checkBox.selectedProperty();
	  }
	 
	  public boolean isSelected() {
	    return checkBox.isSelected();
	  }
	 
	  public void setSelected(boolean selected) {
	    checkBox.setSelected(selected);
	  }
	}