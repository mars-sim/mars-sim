/**
 * Mars Simulation Project
 * ScreensSwitcher.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import java.util.HashMap;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;


public class ScreensSwitcher extends StackPane {
    //Holds the screens to be displayed

    private HashMap<String, Node> screens = new HashMap<>();
    private MainMenu mainMenu;

    public ScreensSwitcher(MainMenu mainmenu) {
        super();
        this.mainMenu = mainmenu;
    }

    //Add the screen to the collection
    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }

    //Returns the Node with the appropriate name
    public Node getScreen(String name) {
        return screens.get(name);
    }

    //Loads the fxml file, add the screen to the screens collection and
    //finally injects the screenPane to the controller.
    public boolean loadScreen(String name, String resource) {
        try {
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent screen = (Parent) myLoader.load();
            screen.getStylesheets().add("/fxui/css/mainmenu.css");
            ControlledScreen controller = ((ControlledScreen) myLoader.getController());
            controller.setScreenParent(this);
            mainMenu.setController(controller);
            addScreen(name, screen);
            return true;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            return false;
        }
    }

    //This method tries to displayed the screen with a predefined name.
    //First it makes sure the screen has been already loaded.  Then if there is more than
    //one screen the new screen is been added second, and then the current screen is removed.
    // If there isn't any screen being displayed, the new screen is just added to the root.
    @SuppressWarnings("restriction")
	public boolean setScreen(final String name) {
        if (screens.get(name) != null) {   //screen loaded
            final DoubleProperty opacity = opacityProperty();

            if (!getChildren().isEmpty()) {    //if there is more than one screen
                Timeline fade = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                        new KeyFrame(new Duration(1000), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent t) {
                        getChildren().remove(0);                    //remove the displayed screen
                        getChildren().add(0, screens.get(name));     //add the screen
                        Timeline fadeIn = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                                new KeyFrame(new Duration(800), new KeyValue(opacity, 1.0)));
                        fadeIn.play();
                    }
                }, new KeyValue(opacity, 0.0)));
                fade.play();

            } else {
                setOpacity(0.0);
                getChildren().add(screens.get(name));       //no one else been displayed, then just show
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                        new KeyFrame(new Duration(2500), new KeyValue(opacity, 1.0)));
                fadeIn.play();
            }
            return true;
        } else {
            System.out.println("screen hasn't been loaded!!! \n");
            return false;
        }

    }

    public MainMenu getMainMenu() {
        return mainMenu;
    }


    //This method will remove the screen with the given name from the collection of screens
    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Screen didn't exist");
            return false;
        } else {
            return true;
        }
    }

	public boolean exitDialog(Stage stage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setTitle("Confirmation for Exit");//("Confirmation Dialog");
		alert.setHeaderText("Leaving mars-sim ?");
		//alert.initModality(Modality.APPLICATION_MODAL);
		alert.setContentText("Note: Yes to exit mars-sim");
		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");
	   	alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
	   	Optional<ButtonType> result = alert.showAndWait();
	   	if (result.get() == buttonTypeYes){
	   		if (mainMenu.getMultiplayerMode() != null)
	   			if (mainMenu.getMultiplayerMode().getChoiceDialog() != null)
	   				mainMenu.getMultiplayerMode().getChoiceDialog().close();
	   		alert.close();
			Platform.exit();
    		System.exit(0);
	   		return true;
	   	} else {
	   		alert.close();
	   	    return false;
	   	}
   	}

/*
    public void exitDialog() {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Exiting MSP");//("Confirmation Dialog");
		alert.setHeaderText("Do you really want to exit MPS?");
		//alert.initModality(Modality.APPLICATION_MODAL);
		alert.setContentText("Warning: exiting the main menu will terminate any running simultion without saving it.");
    	//alert.setTitle("Mars Simulation Project");
    	//alert.setHeaderText("Confirmation Dialog");
		alert.initOwner(mainMenu.getStage());
    	//alert.setContentText("Are you sure about exiting MSP?");
    	ButtonType buttonTypeYes = new ButtonType("Yes");
    	ButtonType buttonTypeNo = new ButtonType("No");
    	alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
    	Optional<ButtonType> result = alert.showAndWait();
    	if (result.get() == buttonTypeYes){
    		if (mainMenu.getMultiplayerMode() != null)
    			if (mainMenu.getMultiplayerMode().getChoiceDialog() != null)
    				mainMenu.getMultiplayerMode().getChoiceDialog().close();
    		System.exit(0);
    	} else {
    	}
    }
*/
}

