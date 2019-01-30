/* 
 * Copyright 2015 David Ray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mars_sim.javafx;

import com.jfoenix.controls.JFXButton;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.Pair;

/**
 * <p>
 * Slides a user defined component into and out of the view
 * starting from the bottom of a user defined anchor node.
 * </p><p>
 * This {@code Flyout} consists of two parts. 
 * <ol>
 *     <li>The "anchor" node which is the visible node; the bottom 
 *     from which the flown out node will fly out. 
 *     <li>The container node containing the user defined node to 
 *     "reveal" by flying out.
 * </ol>
 * </p>
 * 
 * @author cogmission
 */
public class Flyout extends Region {
    public enum Side { TOP, BOTTOM, LEFT, RIGHT, TOP_RIGHT, BOTTOM_LEFT }
    public enum Status { RUNNING, COMPLETE };
    
    public final static int DELAY = 10;
    
    //private Side flyoutSide = Side.BOTTOM;
    private Side flyoutSide = Side.BOTTOM_LEFT;//BOTTOM;
    
    private Timeline tl = new Timeline();
    private DoubleProperty loc = new SimpleDoubleProperty();
    private ReadOnlyObjectWrapper<Flyout.Status> flyOutStatus = new ReadOnlyObjectWrapper<>();
    private Interpolator interpolator = Interpolator.SPLINE(0.8, 0.1, 0.1, 0.8);
    
    private Node anchor;
    private Node flyoutContents;
    
    private StackPane clipContainer;
    private Pane userNodeContainer;
    private Stage popup;
    
    private boolean shownOnce;
    private boolean flyoutShowing;
    
    /** The default style of the background */
    private String userNodeContainerStyle = "-fx-background-color: rgba(0, 0, 0, 0.5);"  +
                	"-fx-background-radius: 1px;";
    
    private MainScene mainScene;
    
    /**
     * Constructs a new {@code Flyout} using the specified "anchor"
     * as the location from which the specified "contents" will 
     * fly out.
     * 
     * @param anchor        Node used to define the start point of the flyout animation
     * @param contents      Node containing the "control" to fly out
     */
    @SuppressWarnings("restriction")
	public Flyout(Node anchor, Node contents, MainScene mainScene) {
    	this.mainScene = mainScene;
    	this.anchor = anchor;
        this.flyoutContents = contents;
        userNodeContainer = new Pane();
        
        getChildren().addListener((Change<? extends Node> c) -> {
            if(getChildren().size() > 1) {
                throw new IllegalStateException("May only add one child to a Flyout");
            }
        });
        
        layoutBoundsProperty().addListener((v, o, n) -> {
            if(getChildren().size() < 1) return;
            
            if(getChildren().size() > 1) {
                throw new IllegalStateException("May only add one child to a Flyout");
            }
        });
        
        getChildren().add(anchor);
        
        popup = new Stage();
        popup.setResizable(true);
        
    }
    
    /**
     * Sets the side this {@link Flyout} will fly out towards
     * (default = {@link Side#BOTTOM})
     * 
     * @param side  the flyout side
     */
    public void setFlyoutSide(Side side) {
        this.flyoutSide = side;
    }
    
    /**
     * Returns a property that can be listened to or queried to 
     * determine if the current animated operation is complete
     * or not.
     * 
     * @return  a property useful for completed animation monitoring
     */
    public ReadOnlyObjectProperty<Flyout.Status> getFlyoutStatusProperty() {
        return flyOutStatus.getReadOnlyProperty();
    }
    
    /**
     * Reverses the flyout animation; at the end of which, the 
     * user defined contents will no longer be visible.
     */
    public void dismiss() {
        if(tl.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        
        doFlyOut(true);
    }
    
    /**
     * Instructs this {@link Flyout} to begin its "reveal" 
     * animation.
     */
    @SuppressWarnings("restriction")
	public void flyout() {
        if(tl.getStatus() != Animation.Status.STOPPED) {
            return;
        }
        
        if(!shownOnce) {
            clipContainer = new StackPane();
            //clipContainer.setPadding(new Insets(0, 0, 0, 0));
            
            userNodeContainer.setStyle(userNodeContainerStyle);
            userNodeContainer.setManaged(false);
            userNodeContainer.setVisible(true);
            userNodeContainer.getChildren().add(flyoutContents);
            
            clipContainer.getChildren().add(userNodeContainer);
            clipContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5)");
            clipContainer.layoutBoundsProperty().addListener((v, o, n) -> {
                userNodeContainer.resize(n.getWidth(), n.getHeight());
            });
            
            Scene popupScene = new Scene(clipContainer, Color.TRANSPARENT);
            popup.initStyle(StageStyle.TRANSPARENT);
            popup.initOwner(anchor.getScene().getWindow());
            popup.setScene(popupScene);
/* 
            popup.initStyle(StageStyle.UTILITY);
            popup.setTitle("MarsNet");
            popup.setHeight(clipContainer.getHeight());
            popup.setWidth(clipContainer.getWidth());
*/
            //popup.setPadding(new Insets(0, 0, 0, 0));
            
            defineFlyout();

            popup.setOnShown(e -> {
                configureChildrenBounding();
            });
        }
        
        //setButtonStyle();
        
        popup.show();
        doFlyOut(false);
/*        
        // 2016-06-12 When popup loses the focus, it will close
        popup.focusedProperty().addListener(new ChangeListener<Boolean>()
	   	{
	   	  @Override
	   	  public void changed(javafx.beans.value.ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
	   	  {
	   		//mainScene.ToggleOffMarsNetButton();
	   		((ToggleButton)anchor).setSelected(false);
	   		dismiss();
	   	  }
	   	});
*/	   	
    }
    
    
    public void setButtonStyle() {
	    JFXButton b = mainScene.getChatBox().getBroadcastButton();//((ChatBox)flyoutContents).getBroadcastButton();
		b.getStyleClass().clear();
		b.getStyleClass().add("button-broadcast");
		String cssFile = "/fxui/css/nimrodskin.css";
		b.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
    }
	
    public Stage getStage() {
    	return popup;
    }
    
    /**
     * Sets the style of the flyout's background container
     * @param s
     */
    public void setFlyoutStyle(String s) {
        this.userNodeContainerStyle = s;
    }
    
    /**
     * Returns the Pane containing the user specified component Node tree.
     * 
     * @return  the Pane containing the user's Node tree.
     */
    public Pane getFlyoutContainer() {
        return userNodeContainer;
    }
    
    /**
     * Sets the {@link Modality} scope of this {@code Flyout}'s popup.
     * @param modality
     */
    public void initModality(Modality modality) {
        popup.initModality(modality);
    }
    
    /**
     * Returns the flag indicating whether this {@code Flyout} is in
     * the shown state or invisible state
     * 
     * @return  true if showing false if not
     */
    public boolean flyoutShowing() {
        return flyoutShowing;
    }
    
    /**
     * The "do once" configuration
     */
    private void defineFlyout() {
        tl.setCycleCount(1);
        loc.addListener((obs, oldY, newY) -> {
            if(flyoutSide == Side.TOP || flyoutSide == Side.TOP_RIGHT 
            		|| flyoutSide == Side.BOTTOM || flyoutSide == Side.BOTTOM_LEFT) {
                userNodeContainer.setLayoutY(newY.doubleValue());
            }else{
                userNodeContainer.setLayoutX(newY.doubleValue());
            }

        });
        tl.statusProperty().addListener((v, o, n) -> {
            if(n == Animation.Status.STOPPED) {
                if(!flyoutShowing) {
                    popup.hide();
                } 
                flyOutStatus.setValue(Flyout.Status.COMPLETE);
            }else{
                flyOutStatus.setValue(Flyout.Status.RUNNING);
            }
        });
    }
    
    /**
     * Executes the "reveal" animation
     * 
     * @param   isReverse the direction of the animation, true if animation
     *          will hide this {@link Flyout}'s popup, false if it will 
     *          show the popup.
     */
    private void doFlyOut(boolean isReverse) {
        flyOutStatus.setValue(Flyout.Status.RUNNING);
        
        // set initial position
        switch(flyoutSide) {
            case TOP: {
                userNodeContainer.setLayoutY(isReverse ? 0 : userNodeContainer.getHeight());
                break;
            }
            case BOTTOM: {
                userNodeContainer.setLayoutY(isReverse ? 0 : -userNodeContainer.getHeight());
                break;
            }
            case LEFT: {
                userNodeContainer.setLayoutX(isReverse ? 0 : userNodeContainer.getWidth());
                break;
            }
            case RIGHT: {
                userNodeContainer.setLayoutX(isReverse ? 0 : -userNodeContainer.getWidth());
                break;
            }
            case TOP_RIGHT: {
                userNodeContainer.setLayoutY(isReverse ? 0 : userNodeContainer.getHeight());
                break;
            }
            case BOTTOM_LEFT: {
            	
            }
        }
        
        // still invisible because user contain is outside clip area
        popup.show();
        
        // set the current and destination y locations
        double currentVal = flyoutSide == Side.TOP || flyoutSide == Side.TOP_RIGHT || flyoutSide == Side.BOTTOM ? 
            userNodeContainer.getLayoutY() : userNodeContainer.getLayoutX();
        double destVal = 0;
        switch(flyoutSide) {
            case TOP: {
                destVal = isReverse ? 
                    currentVal + userNodeContainer.getHeight() : 
                        currentVal - userNodeContainer.getHeight();
                break;
            }
            case BOTTOM: {
                destVal = isReverse ? 
                    currentVal - userNodeContainer.getHeight() : 
                        currentVal + userNodeContainer.getHeight();
                break;
            }
            case LEFT: {
                destVal = isReverse ? 
                    currentVal + userNodeContainer.getWidth() : 
                        currentVal - userNodeContainer.getWidth();
                break;
            }
            case RIGHT: {
                destVal = isReverse ? 
                    currentVal - userNodeContainer.getWidth() : 
                        currentVal + userNodeContainer.getWidth();
                    //userNodeContainer.setLayoutX(isReverse ? 0 : -userNodeContainer.getWidth());
                break;
            }
            case TOP_RIGHT: {
                destVal = isReverse ? 
                        currentVal + userNodeContainer.getHeight() : 
                            currentVal - userNodeContainer.getHeight();
                break;
            }
        }
        loc.set(currentVal);
        
        KeyValue keyValue = new KeyValue(loc, destVal, interpolator);
        // create a keyFrame with duration 500ms
        KeyFrame keyFrame = new KeyFrame(Duration.millis(DELAY), keyValue);
        // erase last keyframes: forward & reverse have different frames
        tl.getKeyFrames().clear();
        // add the keyframe to the timeline
        tl.getKeyFrames().add(keyFrame);
        
        tl.play();
        
        flyoutShowing = !isReverse;
    }
    
    /**
     * Configures the bounds and location parameters which are dependent
     * on the nodes being currently visible.
     */
    private void configureChildrenBounding() {
        Bounds contentBounds = flyoutContents.getBoundsInParent();
        Bounds anchorBounds = anchor.getBoundsInParent();
        
        if(!shownOnce) {
            userNodeContainer.resize(contentBounds.getWidth(), contentBounds.getHeight());
            
            clipContainer.setLayoutX(0);
            clipContainer.setLayoutY(0);
        }
        
        clipContainer.resize(flyoutContents.getLayoutBounds().getWidth(), flyoutContents.getLayoutBounds().getHeight());
        clipContainer.setVisible(true);
        clipContainer.requestLayout();
        
        Pair<Double, Double> xy = initPopupLocation(anchorBounds);
        popup.setX(xy.getKey());
        popup.setY(xy.getValue());
        popup.setWidth(contentBounds.getWidth());
        popup.setHeight(contentBounds.getHeight());
        
        shownOnce = true;
    }
    
    private Pair<Double, Double> initPopupLocation(Bounds anchorBounds) {
        Point2D fp = anchor.localToScreen(0.0, 0.0);
        
        switch(flyoutSide) {
            case BOTTOM : return new Pair<>(fp.getX(), fp.getY() + anchorBounds.getHeight());
            case TOP : return new Pair<>(fp.getX(), fp.getY() - userNodeContainer.getHeight());
            case LEFT : return new Pair<>(fp.getX() - userNodeContainer.getWidth(), fp.getY());
            case RIGHT : return new Pair<>(fp.getX() + anchorBounds.getWidth(), fp.getY());
            case TOP_RIGHT : return new Pair<>(fp.getX() + anchorBounds.getWidth(), fp.getY() - userNodeContainer.getHeight());
            default : return null;
        }
    }
    
    public void setContainerHeight(double value) {
    	userNodeContainer.setPrefHeight(value);
    	popup.setHeight(value);
    }  
    
    public double getContainerHeight() {
        return userNodeContainer.getHeight();	
    }
}
