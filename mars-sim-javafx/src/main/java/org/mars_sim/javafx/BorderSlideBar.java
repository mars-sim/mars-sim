/**
 * Mars Simulation Project
 * BorderSlideBar.java
 * @version 3.1.0 2016-10-06
 * @author Manny Kung
 */
// Adapted from http://physalix.com/javafx2-borderpane-which-slides-in-and-out-on-command/
	
package org.mars_sim.javafx;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Animates a node on and off screen to the top, right, bottom or left side.
 * 
 */
public class BorderSlideBar extends VBox {
    private final String CSS = this.getClass().getResource("/slide/BorderSlideBar.css").toExternalForm();
    // "/" + this.getClass().getSimpleName() + ".css";
    
    private final static int DELAY = 100; // in milliseconds
    
    private double expandedSize;
    private Pos flapbarLocation;

    /**
     * Creates a sidebar panel in a BorderPane, containing an horizontal alignment
     * of the given nodes.
     *
     * <pre>
     * <code>
     *  Example:
     *
     *  BorderSlideBar topFlapBar = new BorderSlideBar(
     *                  100, button, Pos.TOP_LEFT, new contentController());
     *  mainBorderPane.setTop(topFlapBar);
     * </code>
     * </pre>
     *
     * @param expandedSize The size of the panel.
     * @param controlButton The button responsible to open/close slide bar.
     * @param location The location of the panel (TOP_LEFT, BOTTOM_LEFT, BASELINE_RIGHT, BASELINE_LEFT).
     * @param nodes Nodes inside the panel.
     */
    public BorderSlideBar(double expandedSize,
            final Button controlButton, Pos location, Node... nodes) {

        getStyleClass().add("sidebar");
        getStylesheets().add(CSS);        
        setExpandedSize(expandedSize);
        setVisible(false);

        // Set location 
        if (location == null) {
            flapbarLocation = Pos.TOP_CENTER; // Set default location 
        }
        flapbarLocation = location;
        
        initPosition();        

        // Add nodes in the vbox
        getChildren().addAll(nodes);

        controlButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	slide();
            }
        });
    }

    /*
     * Create an animation to hide or show the panel .
     */
    public synchronized void slide() {
        // Create an animation to hide the panel.
        final Animation hidePanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * (1.0 - frac);
                translateByPos(size);
            }
        };

        hidePanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setVisible(false);
            	MainScene.menuBarVisible = false;
            }
        });

        // Create an animation to show the panel.
        final Animation showPanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * frac;
                translateByPos(size);
            }
        };

        showPanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            	MainScene.menuBarVisible = true;
            }
        });

        if (showPanel.statusProperty().get() == Animation.Status.STOPPED
                && hidePanel.statusProperty().get() == Animation.Status.STOPPED) {

            if (isVisible()) {
                hidePanel.play();

            } else {
                setVisible(true);
                showPanel.play();
            }
        }
    }
    
    
    /*
     * Create an animation to hide or show the panel .
     */
    public synchronized void slideOpen() {
        // Create an animation to hide the panel.
        final Animation hidePanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * (1.0 - frac);
                translateByPos(size);
            }
        };

        hidePanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setVisible(false);
            }
        });

        // Create an animation to show the panel.
        final Animation showPanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * frac;
                translateByPos(size);
            }
        };

        showPanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            }
        });

        if (showPanel.statusProperty().get() == Animation.Status.STOPPED
                && hidePanel.statusProperty().get() == Animation.Status.STOPPED) {

            if (isVisible()) {
                hidePanel.play();

            } else {
                setVisible(true);
                showPanel.play();
            }
        }
    }
    
    /*
     * Create an animation to hide or show the panel .
     */
    public synchronized void slideClose() {
        // Create an animation to hide the panel.
        final Animation hidePanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * (1.0 - frac);
                translateByPos(size);
            }
        };

        hidePanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                setVisible(false);
            }
        });

        // Create an animation to show the panel.
        final Animation showPanel = new Transition() {
            {
                setCycleDuration(Duration.millis(DELAY));
            }

            @Override
            protected void interpolate(double frac) {
                final double size = getExpandedSize() * frac;
                translateByPos(size);
            }
        };

        showPanel.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            }
        });

        if (showPanel.statusProperty().get() == Animation.Status.STOPPED
                && hidePanel.statusProperty().get() == Animation.Status.STOPPED) {

            if (isVisible()) {
                hidePanel.play();

            } else {
                setVisible(true);
                showPanel.play();
            }
        }
    }
    /**
     * Initialize position orientation.
     */
    private void initPosition() {
        switch (flapbarLocation) {
            case TOP_LEFT:
                setPrefHeight(0);
                setMinHeight(0);
                break;
            case BOTTOM_LEFT:
                setPrefHeight(0);
                setMinHeight(0);
                break;
            case BASELINE_RIGHT:
                setPrefWidth(0);
                setMinWidth(0);
                break;
            case BASELINE_LEFT:
                setPrefWidth(0);
                setMinWidth(0);
                break;
        }
    }

    /**
     * Translate the VBox according to location Pos.
     *
     * @param size
     */
    private void translateByPos(double size) {
        switch (flapbarLocation) {
            case TOP_LEFT:
                setPrefHeight(size);
                setTranslateY(-getExpandedSize() + size);                
                break;
            case BOTTOM_LEFT:
                setPrefHeight(size);
                setTranslateY(-getExpandedSize() + size);   
                break;
            case BASELINE_RIGHT:
                setPrefWidth(size);
                break;
            case BASELINE_LEFT:
                setPrefWidth(size);
                break;
        }
    }

    /**
     * @return the expandedSize
     */
    public double getExpandedSize() {
        return expandedSize;
    }

    /**
     * @param expandedSize the expandedSize to set
     */
    public void setExpandedSize(double expandedSize) {
        this.expandedSize = expandedSize;
    }
    
}