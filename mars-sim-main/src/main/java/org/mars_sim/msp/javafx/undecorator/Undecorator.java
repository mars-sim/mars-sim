/*
 * BSD
 * Copyright (c) 2013, Arnaud Nouard
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the In-SideFX nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mars_sim.msp.javafx.undecorator;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.javafx.controller.UndecoratorController;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Side;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * This class, with the UndecoratorController, is the central class for the decoration of Transparent Stages. The Stage
 * Undecorator TODO: Themes, manage Quit (main stage)
 *
 * Bugs (Mac only?): Accelerators + Fullscreen crashes JVM KeyCombination does not respect keyboard's locale. Multi
 * screen: On second screen JFX returns wrong value for MinY (300)
 */
public class Undecorator extends StackPane {

    public int SHADOW_WIDTH = 15;
    public int SAVED_SHADOW_WIDTH = 15;
    static public int RESIZE_PADDING = 7;
    static public int FEEDBACK_STROKE = 4;
    static public double ROUNDED_DELTA = 5;
    public static final Logger LOGGER = Logger.getLogger("Undecorator");
    public static ResourceBundle LOC;
    StageStyle stageStyle;
    @FXML
    private Button menu;
    @FXML
    private Button close;
    @FXML
    private Button maximize;
    @FXML
    private Button minimize;
    @FXML
    private Button resize;
    @FXML
    private Button fullscreen;
    @FXML
    private Label title;
    @FXML
    private Pane decorationRoot;
    @FXML
    private ContextMenu contextMenu;

    MenuItem maximizeMenuItem;
    CheckMenuItem fullScreenMenuItem;
    Region clientArea;
    Pane stageDecoration = null;
    Rectangle shadowRectangle;
    Pane glassPane;
    Rectangle dockFeedback;
    FadeTransition dockFadeTransition;
    Stage dockFeedbackPopup;
    ParallelTransition parallelTransition;
    Effect dsFocused;
    Effect dsNotFocused;
    UndecoratorController undecoratorController;
    Stage stage;
    Rectangle backgroundRect;
    public SimpleBooleanProperty maximizeProperty;
    SimpleBooleanProperty minimizeProperty;
    SimpleBooleanProperty closeProperty;
    SimpleBooleanProperty fullscreenProperty;
    String shadowBackgroundStyleClass = "decoration-shadow";
    String decorationBackgroundStyle = "decoration-background";
    TranslateTransition fullscreenButtonTransition;
    final Rectangle internal = new Rectangle();
    final Rectangle external = new Rectangle();

    public SimpleBooleanProperty maximizeProperty() {
        return maximizeProperty;
    }

    public SimpleBooleanProperty minimizeProperty() {
        return minimizeProperty;
    }

    public SimpleBooleanProperty closeProperty() {
        return closeProperty;
    }

    public SimpleBooleanProperty fullscreenProperty() {
        return fullscreenProperty;
    }

    public Undecorator(Stage stage, Region root) {
        this(stage, root, "/undecorator/fxml/stagedecoration.fxml", StageStyle.UNDECORATED);
    }

    public Undecorator(Stage stag, Region clientArea, String stageDecorationFxml, StageStyle st) {
        create(stag, clientArea, getClass().getResource(stageDecorationFxml), st);
    }

    public Undecorator(Stage stag, Region clientArea, URL stageDecorationFxmlAsURL, StageStyle st) {
        create(stag, clientArea, stageDecorationFxmlAsURL, st);
    }

    public void create(Stage stag, Region clientArea, URL stageDecorationFxmlAsURL, StageStyle st) {
        this.stage = stag;
        this.clientArea = clientArea;

        setStageStyle(st);
        loadConfig();

        // Properties
        maximizeProperty = new SimpleBooleanProperty(false);
        maximizeProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                getController().maximizeOrRestore();
            }
        });
        minimizeProperty = new SimpleBooleanProperty(false);
        minimizeProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                getController().minimize();
            }
        });

        closeProperty = new SimpleBooleanProperty(false);
        closeProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                getController().close();
            }
        });
        fullscreenProperty = new SimpleBooleanProperty(false);
        fullscreenProperty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                getController().setFullScreen(!stage.isFullScreen());
            }
        });

        // The controller
        undecoratorController = new UndecoratorController(this);

        undecoratorController.setAsStageDraggable(stage, clientArea);

        // Focus drop shadows: radius, spread, offsets
        dsFocused = new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, SHADOW_WIDTH, 0.1, 0, 0);
        dsNotFocused = new DropShadow(BlurType.THREE_PASS_BOX, Color.DARKGREY, SHADOW_WIDTH, 0, 0, 0);

        shadowRectangle = new Rectangle();
        shadowRectangle.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldBounds, Bounds newBounds) {
                if (SHADOW_WIDTH != 0) {
                    shadowRectangle.setVisible(true);
                    setShadowClip(newBounds);
                } else {
                    shadowRectangle.setVisible(false);
                }
            }
        });

        // UI part of the decoration
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(stageDecorationFxmlAsURL);
            fxmlLoader.setController(this);
            stageDecoration = (Pane) fxmlLoader.load();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Decorations not found", ex);
        }

        initDecoration();

        /*
         * Resize rectangle
         */
        undecoratorController.setStageResizableWith(stage, decorationRoot, RESIZE_PADDING, SHADOW_WIDTH);

        // If not resizable (quick fix)
        if (fullscreen
                != null) {
            fullscreen.setVisible(stage.isResizable());
        }

        resize.setVisible(stage.isResizable());
        if (maximize
                != null) {
            maximize.setVisible(stage.isResizable());
        }
        if (minimize
                != null && !stage.isResizable()) {
            AnchorPane.setRightAnchor(minimize, 34d);
        }

        // Glass Pane
        glassPane = new Pane();

        glassPane.setMouseTransparent(true);
        buildDockFeedbackStage();

        title.getStyleClass().add("undecorator-label-titlebar");
        shadowRectangle.getStyleClass().add(shadowBackgroundStyleClass);
//        resizeRect.getStyleClass().add(resizeStyleClass);
        // Do not intercept mouse events on stage's shadow
        shadowRectangle.setMouseTransparent(true);

        // Is it possible to apply an effect without affecting decendent?
        super.setStyle("-fx-background-color:transparent;");
        // Or this:
//        super.setStyle("-fx-background-color:transparent;-fx-border-color:white;-fx-border-radius:30;-fx-border-width:1;-fx-border-insets:"+SHADOW_WIDTH+";");
//        super.setEffect(dsFocused);
//          super.getChildren().addAll(clientArea,stageDecoration, glassPane);

        backgroundRect = new Rectangle();
        backgroundRect.getStyleClass().add(decorationBackgroundStyle);
        backgroundRect.setMouseTransparent(true);

        // Add all layers
        super.getChildren().addAll(shadowRectangle, backgroundRect, clientArea, stageDecoration, glassPane);
//        super.getChildren().addAll(shadowRectangle, backgroundRect);

        /*
         * Focused stage
         */
        stage.focusedProperty()
                .addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1
                    ) {
                        setShadowFocused(t1.booleanValue());
                    }
                }
                );
        /*
         * Fullscreen
         */
        if (fullscreen
                != null) {
            fullscreen.setOnMouseEntered(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if (stage.isFullScreen()) {
                        fullscreen.setOpacity(1);
                    }
                }
            });

            fullscreen.setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    if (stage.isFullScreen()) {
                        fullscreen.setOpacity(0.4);
                    }
                }
            });

            stage.fullScreenProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean fullscreenState) {
                    setShadow(!fullscreenState.booleanValue());
                    fullScreenMenuItem.setSelected(fullscreenState.booleanValue());
                    maximize.setVisible(!fullscreenState.booleanValue());
                    minimize.setVisible(!fullscreenState.booleanValue());
                    resize.setVisible(!fullscreenState.booleanValue());
                    if (fullscreenState.booleanValue()) {
                        // String and icon
                        fullscreen.getStyleClass().add("decoration-button-unfullscreen");
                        fullscreen.setTooltip(new Tooltip(LOC.getString("Restore")));

                        undecoratorController.saveFullScreenBounds();
                        if (fullscreenButtonTransition != null) {
                            fullscreenButtonTransition.stop();
                        }
                        // Animate the fullscreen button
                        fullscreenButtonTransition = new TranslateTransition();
                        fullscreenButtonTransition.setDuration(Duration.millis(3000));
                        fullscreenButtonTransition.setToX(66);
                        fullscreenButtonTransition.setNode(fullscreen);
                        fullscreenButtonTransition.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                fullscreenButtonTransition = null;
                            }
                        });
                        fullscreenButtonTransition.play();
                        fullscreen.setOpacity(0.2);
                    } else {
                        // String and icon
                        fullscreen.getStyleClass().remove("decoration-button-unfullscreen");
                        fullscreen.setTooltip(new Tooltip(LOC.getString("FullScreen")));

                        undecoratorController.restoreFullScreenSavedBounds(stage);
                        fullscreen.setOpacity(1);
                        if (fullscreenButtonTransition != null) {
                            fullscreenButtonTransition.stop();
                        }
                        // Animate the change
                        fullscreenButtonTransition = new TranslateTransition();
                        fullscreenButtonTransition.setDuration(Duration.millis(1000));
                        fullscreenButtonTransition.setToX(0);
                        fullscreenButtonTransition.setNode(fullscreen);
                        fullscreenButtonTransition.setOnFinished(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent t) {
                                fullscreenButtonTransition = null;
                            }
                        });

                        fullscreenButtonTransition.play();
                    }

                }
            });
        }

        computeAllSizes();
    }

    /**
     * Compute the needed clip for stage's shadow border
     *
     * @param newBounds
     * @param shadowVisible
     */
    void setShadowClip(Bounds newBounds) {
        external.relocate(
                newBounds.getMinX() - SHADOW_WIDTH,
                newBounds.getMinY() - SHADOW_WIDTH
        );
        internal.setX(SHADOW_WIDTH);
        internal.setY(SHADOW_WIDTH);
        internal.setWidth(newBounds.getWidth());
        internal.setHeight(newBounds.getHeight());
        internal.setArcWidth(shadowRectangle.getArcWidth());    // shadowRectangle CSS cannot be applied on this
        internal.setArcHeight(shadowRectangle.getArcHeight());

        external.setWidth(newBounds.getWidth() + SHADOW_WIDTH * 2);
        external.setHeight(newBounds.getHeight() + SHADOW_WIDTH * 2);
        Shape clip = Shape.subtract(external, internal);
        shadowRectangle.setClip(clip);

    }

    /**
     * Install default accelerators
     *
     * @param scene
     */
    public void installAccelerators(Scene scene) {
        // Accelerators
        if (stage.isResizable()) {
            scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN), new Runnable() {
                @Override
                public void run() {
                    switchFullscreen();
                }
            });
        }
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN), new Runnable() {
            @Override
            public void run() {
                switchMinimize();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN), new Runnable() {
            @Override
            public void run() {
                switchClose();
            }
        });
    }

    /**
     * Init the minimum/pref/max size in order to be reflected in the primary stage
     */
    private void computeAllSizes() {
        double minWidth = minWidth(getHeight());
        setMinWidth(minWidth);
        double minHeight = minHeight(getWidth());
        setMinHeight(minHeight);

        double prefHeight = prefHeight(getWidth());
        setPrefHeight(prefHeight);
        double prefWidth = prefWidth(getHeight());
        setPrefWidth(prefWidth);

        double maxWidth = maxWidth(getHeight());
        if (maxWidth > 0) {
            setMaxWidth(maxWidth);
        }
        double maxHeight = maxHeight(getWidth());
        if (maxHeight > 0) {
            setMaxHeight(maxHeight);
        }
    }
    /*
     * The sizing is based on client area's bounds.
     */

    @Override
    protected double computePrefWidth(double d) {
        return clientArea.getPrefWidth() + SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
    }

    @Override
    protected double computePrefHeight(double d) {
        return clientArea.getPrefHeight() + SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
    }

    @Override
    protected double computeMaxHeight(double d) {
        return clientArea.getMaxHeight() + SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
    }

    @Override
    protected double computeMinHeight(double d) {
        double d2 = super.computeMinHeight(d);
        d2 += SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
        return d2;
    }

    @Override
    protected double computeMaxWidth(double d) {
        return clientArea.getMaxWidth() + SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
    }

    @Override
    protected double computeMinWidth(double d) {
        double d2 = super.computeMinWidth(d);
        d2 += SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
        return d2;
    }

    public void setStageStyle(StageStyle st) {
        stageStyle = st;
    }

    public StageStyle getStageStyle() {
        return stageStyle;
    }

    /**
     * Activate fade in transition on showing event
     */
    public void setFadeInTransition() {
        super.setOpacity(0);
        stage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (t1.booleanValue()) {
                    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), Undecorator.this);
                    fadeTransition.setToValue(1);
                    fadeTransition.play();
                }
            }
        });
    }

    /**
     * Launch the fade out transition. Must be invoked when the application/window is supposed to be closed
     */
    public void setFadeOutTransition() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), Undecorator.this);
        fadeTransition.setToValue(0);
        fadeTransition.play();
        fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                stage.hide();
                if (dockFeedbackPopup != null && dockFeedbackPopup.isShowing()) {
                    dockFeedbackPopup.hide();
                }
            }
        });
    }

    public void removeDefaultBackgroundStyleClass() {
        shadowRectangle.getStyleClass().remove(shadowBackgroundStyleClass);
    }

    public Rectangle getShadowNode() {
        return shadowRectangle;
    }

    public Rectangle getBackgroundRectangle() {
        return backgroundRect;
    }

    /**
     * Background opacity
     *
     * @param opacity
     */
    public void setBackgroundOpacity(double opacity) {
        shadowRectangle.setOpacity(opacity);
    }

    /**
     * Manage buttons and menu items
     */
    public void initDecoration() {
        MenuItem minimizeMenuItem = null;
        // Menu
        contextMenu.setAutoHide(true);
        if (minimize != null) { // Utility Stage
            minimizeMenuItem = new MenuItem(LOC.getString("Minimize"));
            minimizeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN));

            minimizeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    switchMinimize();
                }
            });
            contextMenu.getItems().add(minimizeMenuItem);
        }
        if (maximize != null && stage.isResizable()) { // Utility Stage type
            maximizeMenuItem = new MenuItem(LOC.getString("Maximize"));
            maximizeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    switchMaximize();
                    contextMenu.hide(); // Stay stuck on screen
                }
            });
            contextMenu.getItems().addAll(maximizeMenuItem, new SeparatorMenuItem());
        }

        // Fullscreen
        if (stageStyle != StageStyle.UTILITY && stage.isResizable()) {
            fullScreenMenuItem = new CheckMenuItem(LOC.getString("FullScreen"));
            fullScreenMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    // fake
                    //maximizeProperty().set(!maximizeProperty().get());
                    switchFullscreen();
                }
            });
            fullScreenMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHORTCUT_DOWN));

            contextMenu.getItems().addAll(fullScreenMenuItem, new SeparatorMenuItem());
        }

        // Close
        MenuItem closeMenuItem = new MenuItem(LOC.getString("Close"));
        closeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                switchClose();
            }
        });
        closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN));

        contextMenu.getItems().add(closeMenuItem);

        menu.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                if (contextMenu.isShowing()) {
                    contextMenu.hide();
                } else {
                    contextMenu.show(menu, Side.BOTTOM, 0, 0);
                }
            }
        });

        // Close button
        close.setTooltip(new Tooltip(LOC.getString("Close")));
        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                switchClose();
            }
        });

        // Maximize button
        // If changed via contextual menu
        maximizeProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                if (!stage.isResizable()) {
                    return;
                }
                Tooltip tooltip = maximize.getTooltip();
                if (tooltip.getText().equals(LOC.getString("Maximize"))) {
                    tooltip.setText(LOC.getString("Restore"));
                    maximizeMenuItem.setText(LOC.getString("Restore"));
                    maximize.getStyleClass().add("decoration-button-restore");
                    resize.setVisible(false);
                } else {
                    tooltip.setText(LOC.getString("Maximize"));
                    maximizeMenuItem.setText(LOC.getString("Maximize"));
                    maximize.getStyleClass().remove("decoration-button-restore");
                    resize.setVisible(true);
                }
            }
        });

        if (maximize != null) { // Utility Stage
            maximize.setTooltip(new Tooltip(LOC.getString("Maximize")));
            maximize.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    switchMaximize();
                }
            });
        }
        if (fullscreen != null) { // Utility Stage
            fullscreen.setTooltip(new Tooltip(LOC.getString("FullScreen")));
            fullscreen.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    switchFullscreen();
                }
            });
        }

        // Minimize button
        if (minimize != null) { // Utility Stage
            minimize.setTooltip(new Tooltip(LOC.getString("Minimize")));
            minimize.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    switchMinimize();
                }
            });
        }
        // Transfer stage title to undecorator tiltle label

        title.setText(stage.getTitle());
    }

    public void switchFullscreen() {
        // Invoke runLater even if it's on EDT: Crash apps on Mac
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                undecoratorController.setFullScreen(!stage.isFullScreen());
            }
        });
    }

    public void switchMinimize() {
        minimizeProperty().set(!minimizeProperty().get());
    }

    public void switchMaximize() {
        maximizeProperty().set(!maximizeProperty().get());
    }

    public void switchClose() {
        closeProperty().set(!closeProperty().get());
    }

    /**
     * Bridge to the controller to enable the specified node to drag the stage
     *
     * @param stage
     * @param node
     */
    public void setAsStageDraggable(Stage stage, Node node) {
        undecoratorController.setAsStageDraggable(stage, node);
    }

    /**
     * Switch the visibility of the window's drop shadow
     */
    public void setShadow(boolean shadow) {
        // Already removed?
        if (!shadow && shadowRectangle.getEffect() == null) {
            return;
        }
        // From fullscreen to maximize case
        if (shadow && maximizeProperty.get()) {
            return;
        }
        if (!shadow) {
            shadowRectangle.setEffect(null);
            SAVED_SHADOW_WIDTH = SHADOW_WIDTH;
            SHADOW_WIDTH = 0;
        } else {
            shadowRectangle.setEffect(dsFocused);
            SHADOW_WIDTH = SAVED_SHADOW_WIDTH;
        }
    }

    /**
     * Set on/off the stage shadow effect
     *
     * @param b
     */
    protected void setShadowFocused(boolean b) {
        // Do not change anything while maximized (in case of dialog closing for instance)
        if (stage.isFullScreen()) {
            return;
        }
        if (maximizeProperty().get()) {
            return;
        }
        if (b) {
            shadowRectangle.setEffect(dsFocused);
        } else {
            shadowRectangle.setEffect(dsNotFocused);
        }
    }

    /**
     * Set the layout of different layers of the stage
     */
    @Override
    public void layoutChildren() {
        Bounds b = super.getLayoutBounds();
        double w = b.getWidth();
        double h = b.getHeight();
        ObservableList<Node> list = super.getChildren();
//        ROUNDED_DELTA=shadowRectangle.getArcWidth()/4;
        ROUNDED_DELTA = 0;
        for (Node node : list) {
            if (node == shadowRectangle) {
                shadowRectangle.setWidth(w - SHADOW_WIDTH * 2);
                shadowRectangle.setHeight(h - SHADOW_WIDTH * 2);
                shadowRectangle.setX(SHADOW_WIDTH);
                shadowRectangle.setY(SHADOW_WIDTH);
            } else if (node == backgroundRect) {
                backgroundRect.setWidth(w - SHADOW_WIDTH * 2);
                backgroundRect.setHeight(h - SHADOW_WIDTH * 2);
                backgroundRect.setX(SHADOW_WIDTH);
                backgroundRect.setY(SHADOW_WIDTH);
            } else if (node == stageDecoration) {
                stageDecoration.resize(w - SHADOW_WIDTH * 2 - ROUNDED_DELTA * 2, h - SHADOW_WIDTH * 2 - ROUNDED_DELTA * 2);
                stageDecoration.setLayoutX(SHADOW_WIDTH + ROUNDED_DELTA);
                stageDecoration.setLayoutY(SHADOW_WIDTH + ROUNDED_DELTA);
            } //            else if (node == resizeRect) {
            //                resizeRect.setWidth(w - SHADOW_WIDTH * 2);
            //                resizeRect.setHeight(h - SHADOW_WIDTH * 2);
            //                resizeRect.setLayoutX(SHADOW_WIDTH);
            //                resizeRect.setLayoutY(SHADOW_WIDTH);
            //            }
            else {
                node.resize(w - SHADOW_WIDTH * 2 - ROUNDED_DELTA * 2, h - SHADOW_WIDTH * 2 - ROUNDED_DELTA * 2);
                node.setLayoutX(SHADOW_WIDTH + ROUNDED_DELTA);
                node.setLayoutY(SHADOW_WIDTH + ROUNDED_DELTA);
//                node.resize(w - SHADOW_WIDTH * 2 - RESIZE_PADDING * 2, h - SHADOW_WIDTH * 2 - RESIZE_PADDING * 2);
//                node.setLayoutX(SHADOW_WIDTH + RESIZE_PADDING);
//                node.setLayoutY(SHADOW_WIDTH + RESIZE_PADDING);
            }
        }
    }

    public int getShadowBorderSize() {
        return SHADOW_WIDTH * 2 + RESIZE_PADDING * 2;
    }

    public UndecoratorController getController() {
        return undecoratorController;
    }

    public Stage getStage() {
        return stage;
    }

    protected Pane getGlassPane() {
        return glassPane;
    }

    public void addGlassPane(Node node) {
        glassPane.getChildren().add(node);
    }

    public void removeGlassPane(Node node) {
        glassPane.getChildren().remove(node);
    }

    /**
     * Returns the decoration (buttons...)
     *
     * @return
     */
    public Pane getStageDecorationNode() {
        return stageDecoration;
    }

    /**
     * Prepare Stage for dock feedback display
     */
    void buildDockFeedbackStage() {
        dockFeedbackPopup = new Stage(StageStyle.TRANSPARENT);
        dockFeedback = new Rectangle(0, 0, 100, 100);
        dockFeedback.setArcHeight(10);
        dockFeedback.setArcWidth(10);
        dockFeedback.setFill(Color.TRANSPARENT);
        dockFeedback.setStroke(Color.BLACK);
        dockFeedback.setStrokeWidth(2);
        dockFeedback.setCache(true);
        dockFeedback.setCacheHint(CacheHint.SPEED);
        dockFeedback.setEffect(new DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 10, 0.2, 3, 3));
        dockFeedback.setMouseTransparent(true);
        BorderPane borderpane = new BorderPane();
        borderpane.setStyle("-fx-background-color:transparent"); //J8
        borderpane.setCenter(dockFeedback);
        Scene scene = new Scene(borderpane);
        scene.setFill(Color.TRANSPARENT);
        dockFeedbackPopup.setScene(scene);
        dockFeedbackPopup.sizeToScene();
    }

    /**
     * Activate dock feedback on screen's bounds
     *
     * @param x
     * @param y
     */
    public void setDockFeedbackVisible(double x, double y, double width, double height) {
        dockFeedbackPopup.setX(x);
        dockFeedbackPopup.setY(y);

        dockFeedback.setX(SHADOW_WIDTH);
        dockFeedback.setY(SHADOW_WIDTH);
        dockFeedback.setHeight(height - SHADOW_WIDTH * 2);
        dockFeedback.setWidth(width - SHADOW_WIDTH * 2);

        dockFeedbackPopup.setWidth(width);
        dockFeedbackPopup.setHeight(height);

        dockFeedback.setOpacity(1);
        dockFeedbackPopup.show();

        dockFadeTransition = new FadeTransition();
        dockFadeTransition.setDuration(Duration.millis(200));
        dockFadeTransition.setNode(dockFeedback);
        dockFadeTransition.setFromValue(0);
        dockFadeTransition.setToValue(1);
        dockFadeTransition.setAutoReverse(true);
        dockFadeTransition.setCycleCount(3);

        dockFadeTransition.play();

    }

    public void setDockFeedbackInvisible() {
        if (dockFeedbackPopup.isShowing()) {
            dockFeedbackPopup.hide();
            if (dockFadeTransition != null) {
                dockFadeTransition.stop();
            }
        }
    }

    void loadConfig() {
        Properties prop = new Properties();

        try {
            //prop.load(Undecorator.class.getClassLoader().getResourceAsStream("/skin/undecorator.properties"));
            prop.load(getClass().getResourceAsStream("/undecorator/skin/undecorator.properties"));

            SHADOW_WIDTH = Integer.parseInt(prop.getProperty("window-shadow-width"));
            RESIZE_PADDING = Integer.parseInt(prop.getProperty("window-resize-padding"));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while loading confguration flie", ex);
        }
        //LOC = ResourceBundle.getBundle("insidefx/undecorator/resources/localization", Locale.getDefault());
        LOC = ResourceBundle.getBundle("localization", Locale.getDefault());
    }
}
