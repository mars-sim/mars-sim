/**
 * Mars Simulation Project
 * QNotification.java
 * @version 3.1.0 2017-11-24
 * @author Manny Kung
 */


/*
 * Copyright (c) 2013 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mars_sim.msp.ui.javafx.quotation;

import org.mars_sim.javafx.MainScene;
import org.mars_sim.javafx.tools.StartUpLocation;

import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class QNotification {
    public static Image QUOTE_ICON;

    public final String       TITLE;
    public final String       MESSAGE;
    public Image        IMAGE;

    // ******************** Constructors **************************************
    //public Notification() {}

    public QNotification(final String TITLE, final String MESSAGE) {
        this(TITLE, MESSAGE, null);
    }
    public QNotification(final String MESSAGE, final Image IMAGE) {
        this("", MESSAGE, IMAGE);
    }
    public QNotification(final String TITLE, final String MESSAGE, final Image IMAGE) {
        this.TITLE   = TITLE;
        this.MESSAGE = MESSAGE;
        this.IMAGE   = IMAGE;
    }


    // ******************** Inner Classes *************************************
    public enum Notifier {
        INSTANCE;

    	private static final double left_indent		= 10;
        private static final double ICON_WIDTH    = 48;//32;//24;
        private static final double ICON_HEIGHT   = 48;//32;//24;
        private static       double width         = 500;
        private static       double height        = 75;
        private static       double offsetX       = 23;
        private static       double offsetY       = 66;
        private static       double spacingY      = 10;
        private static       Pos    popupLocation = Pos.TOP_RIGHT;
        private static       Stage  stageRef      = null;
        private Duration              popupLifetime;
    	private static final int POPUP_IN_MILLISECONDS = 20_000;
        private Stage                 stage;
        //private Scene                 scene;
        private ObservableList<Popup> popups;
        private static AnchorPane anchorPane;
   		double xPos = 0;
		double yPos = 0;
	    int count = 0;

	    private TextArea		ta;


        // ******************** Constructor ***************************************
        private Notifier() {
            init();
            //initGraphics();
        }


        // ******************** Initialization ************************************
        private void init() {
            popupLifetime = Duration.millis(POPUP_IN_MILLISECONDS);
            popups = FXCollections.observableArrayList();

            QUOTE_ICON  = new Image(this.getClass().getResourceAsStream("/icons/notification/blue_quote_64.png"));//quote_24.png"));

        }


        // ******************** Methods *******************************************
        /**
         * @param STAGE_REF  The Notification will be positioned relative to the given Stage.<br>
         * 					If null then the Notification will be positioned relative to the primary Screen.
         * @param POPUP_LOCATION  The default is TOP_RIGHT of primary Screen.
         */
        public void setPopupLocation(final Stage STAGE_REF, final Pos POPUP_LOCATION) {
            if (null != STAGE_REF) {
                //INSTANCE.stage.initOwner(STAGE_REF);
                Notifier.stageRef = STAGE_REF;

                // only need to add listener once
        		//stageRef.xProperty().addListener((obs, oldVal, newVal) -> System.out.println("X: " + newVal));
        		//stageRef.yProperty().addListener((obs, oldVal, newVal) -> System.out.println("Y: " + newVal));

            }
            Notifier.popupLocation = POPUP_LOCATION;
        }

        /**
         * Sets the Notification's owner stage so that when the owner
         * stage is closed Notifications will be shut down as well.<br>
         * This is only needed if <code>setPopupLocation</code> is called
         * <u>without</u> a stage reference.
         * @param OWNER
         */
        public static void setNotificationOwner(final Stage OWNER) {
        	INSTANCE.stage = OWNER;
        	//if (INSTANCE.stage.getOwner() == null) {
        	//	INSTANCE.stage.initOwner(OWNER);
        		//INSTANCE.stage.initModality(Modality.WINDOW_MODAL);
        	//}
        }

        public static void setPane(final AnchorPane anchorPane) {
        	INSTANCE.anchorPane = anchorPane;
        }

        /**
         * @param OFFSET_X  The horizontal shift required.
         * <br> The default is 0 px.
         */
        public static void setOffsetX(final double OFFSET_X) {
            Notifier.offsetX = OFFSET_X;
        }

        /**
         * @param OFFSET_Y  The vertical shift required.
         * <br> The default is 25 px.
         */
        public static void setOffsetY(final double OFFSET_Y) {
            Notifier.offsetY = OFFSET_Y;
        }

        /**
         * @param WIDTH  The default is 300 px.
         */
        public static void setWidth(final double WIDTH) {
/*           //Notifier.width = WIDTH * res / 1920 / 1.05 ;
*/
        	//if (MainScene.OS.equals("mac os x"))
        	//	Notifier.width = WIDTH/1.35; // 547 - > 438 is optimal
        	//else
        	//	 Notifier.width = WIDTH;
        	//if (Notifier.width < WIDTH/1.3)
        	//	Notifier.width = WIDTH/1.3;
        	Notifier.width = WIDTH;
            //System.out.println("adjusted width is " + Math.round(width));// / 1.25));
        }

        /**
         * @param HEIGHT  The default is 80 px.
         */
        public static void setHeight(final double HEIGHT) {
            Notifier.height = HEIGHT;// / 1.10;
        }

        /**
         * @param SPACING_Y  The spacing between multiple Notifications.
         * <br> The default is 5 px.
         */
        public static void setSpacingY(final double SPACING_Y) {
            Notifier.spacingY = SPACING_Y;
        }

        //public void stop() {
        //    popups.clear();
        //    stage.close();
        //}

        /**
         * Returns the Duration that the notification will stay on screen before it
         * will fade out.
         * @return the Duration the popup notification will stay on screen
         */
        public Duration getPopupLifetime() {
            return popupLifetime;
        }

        /**
         * Defines the Duration that the popup notification will stay on screen before it
         * will fade out. The parameter is limited to values between 2 and 20 seconds.
         * @param POPUP_LIFETIME
         */
        public void setPopupLifetime(Duration POPUP_LIFETIME) {
        	if (POPUP_LIFETIME == Duration.ZERO)
        		popupLifetime = Duration.ZERO;
        	else
        		popupLifetime = Duration.millis(clamp(2000, 20_000_000, POPUP_LIFETIME.toMillis()));
            //System.out.println("wait time : " + popupLifetime.toSeconds() + " secs");
        }

        /**
         * Show the given Notification on the screen
         * @param NOTIFICATION
         */
        public void notify(final QNotification NOTIFICATION) {
            //System.out.println("starting notify()");
            preOrder();
            showPopup(NOTIFICATION);
            //showPopup(createUI(NOTIFICATION));
        }


        /**
         * Show a Notification with the given parameters on the screen
         * @param TITLE
         * @param MESSAGE
         * @param IMAGE
         */
        public void notify(final String TITLE, final String MESSAGE, final Image IMAGE) {
            //System.out.println("goint to call notify(new Notification(TITLE, MESSAGE, IMAGE) in notify(1,2,3)");
            notify(new QNotification(TITLE, MESSAGE, IMAGE));
        }


        /**
         * Makes sure that the given VALUE is within the range of MIN to MAX
         * @param MIN
         * @param MAX
         * @param VALUE
         * @return
         */
        private double clamp(final double MIN, final double MAX, final double VALUE) {
            if (VALUE < MIN) return MIN;
            if (VALUE > MAX) return MAX;
            return VALUE;
        }

        /**
         * Reorder the popup Notifications on screen so that the latest Notification will stay on top
         */
        private void preOrder() {
            if (popups.isEmpty()) return;
            for (int i = 0 ; i < popups.size() ; i++) {
                switch (popupLocation) {
                    case TOP_LEFT: case TOP_CENTER: case TOP_RIGHT: popups.get(i).setY(popups.get(i).getY() + height + spacingY); break;
                    //case BOTTOM_LEFT: case BOTTOM_CENTER: case BOTTOM_RIGHT: popups.get(i).setY(popups.get(i).getY() + spacingY); break;
                    //case CENTER_LEFT: case CENTER: case CENTER_RIGHT: popups.get(i).setY(..); break;
                    default: popups.get(i).setY(popups.get(i).getY() - height - spacingY);
                }
            }
        }

	    public TextArea getTextArea() {
	    	return ta;
	    }

        /**
         * Creates and shows a popup with the data from the given Notification object
         * @param NOTIFICATION
         */
        @SuppressWarnings("restriction")
		private void showPopup(QNotification NOTIFICATION) {
            //System.out.println("starting showPopup(stage)");

            Label title = new Label(NOTIFICATION.TITLE);
            title.getStyleClass().add("title");

            ImageView icon = new ImageView(NOTIFICATION.IMAGE);
            icon.setLayoutY(5);
            icon.setFitWidth(ICON_WIDTH);
            icon.setFitHeight(ICON_HEIGHT);

            Label message = new Label("", icon);
            message.getStyleClass().add("message");

            String cssFile = null;
            int theme = MainScene.getTheme();
            
            if (theme == 0 || theme == 6)
            	cssFile = MainScene.BLUE_CSS_THEME;
            else
            	cssFile = MainScene.ORANGE_CSS_THEME;
            
            ta = new TextArea();
            ta.setId("quotation");
            ta.appendText(NOTIFICATION.MESSAGE);
            ta.setMinSize(width, height);
            ta.setEditable(false);
            ta.setWrapText(true);
    		ta.getStylesheets().add(this.getClass().getResource(cssFile).toExternalForm());
            ta.positionCaret(0);

    		/*
            ta.skinProperty().addListener(new ChangeListener<Skin<?>>() {
                @Override
                public void changed(
                  ObservableValue<? extends Skin<?>> ov, Skin<?> t, Skin<?> t1) {
                    if (t1 != null && t1.getNode() instanceof Region) {
                        Region r = (Region) t1.getNode();
                        r.setBackground(Background.EMPTY);

                        r.getChildrenUnmodifiable().stream().
                                filter(n -> n instanceof Region).
                                map(n -> (Region) n).
                                forEach(n -> n.setBackground(Background.EMPTY));

                        r.getChildrenUnmodifiable().stream().
                                filter(n -> n instanceof Control).
                                map(n -> (Control) n).
                                forEach(c -> c.skinProperty().addListener(this)); // *
                    }
                }
            });
*/
            HBox hBox = new HBox();
            hBox.setSpacing(5);
            hBox.setPadding(new Insets(5, 5, 5, 5));
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.getChildren().addAll(message, title);

            VBox popupLayout = new VBox();
            popupLayout.setSpacing(5);
            //popupLayout.setStyle("-fx-background-color: transparent;");
            popupLayout.setPadding(new Insets(5, 5, 5, left_indent));
            popupLayout.getChildren().addAll(hBox, ta);

            StackPane popupContent = new StackPane();
            popupContent.getStylesheets().add(this.getClass().getResource("/fxui/css/notification/notifier.css").toExternalForm());
            popupContent.setPrefSize(width, ta.getPrefHeight() + ICON_HEIGHT + 10);//.getPrefHeight());
            popupContent.getStyleClass().add("notification");
            popupContent.getChildren().addAll(popupLayout);

            Popup POPUP = new Popup();
            POPUP.setX( getX() );
            POPUP.setY( getY() );
            POPUP.getContent().add(popupContent);

            popups.add(POPUP);

            // Add a timeline for popup fade out
            KeyValue fadeOutBegin = new KeyValue(POPUP.opacityProperty(), 1.0);
            KeyValue fadeOutEnd   = new KeyValue(POPUP.opacityProperty(), 0.0);

            KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
            KeyFrame kfEnd   = new KeyFrame(Duration.millis(500), fadeOutEnd);

            Timeline timeline = null;
            // 2016-06-17 Enabled indefinite popup e.g. Pause popup
            if (popupLifetime != Duration.ZERO) {
	            timeline = new Timeline(kfBegin, kfEnd);
	            timeline.setDelay(popupLifetime);
	            timeline.setOnFinished(actionEvent -> Platform.runLater(() -> {
	                POPUP.hide();
	                popups.remove(POPUP);
	            }));
            }

            // Move popup to the right during fade out
            //POPUP.opacityProperty().addListener((observableValue, oldOpacity, opacity) -> popup.setX(popup.getX() + (1.0 - opacity.doubleValue()) * popup.getWidth()) );


            if (stage.isShowing()) {
            	stage.toFront();
            } else {
                stage.show();
            }

            POPUP.show(stage);


            if (popupLifetime != Duration.ZERO)
            	timeline.play();

           	//System.out.println();
        }

        private double getX() {
        	double w1 = Screen.getPrimary().getBounds().getWidth();
        	double w2 = 0;
        	if (Screen.getScreens().size() == 2) {
           		w2 = Screen.getScreens().get(1).getBounds().getWidth();
        	}

        	// check if mainScene is on primary or secondary and set w0
        	double m = getMonitor(w2);
        	double w0 = 0;

        	if (m == 1)
        		w0 = w1;
        	else
        		w0 = w2;

    	    //System.out.println("width is " + w0);

            if (null == stageRef) return calcX( 0.0, w0 );

            return calcX(stageRef.getX(), stageRef.getWidth());
        }

        private double getY() {
        	double h1 = Screen.getPrimary().getBounds().getWidth();
        	double h2 = 0;
        	if (Screen.getScreens().size() == 2) {
           		h2 = Screen.getScreens().get(1).getBounds().getWidth();
        	}

        	// check if mainScene is on primary or secondary and set h0
        	double m = getMonitor(h2);
        	double h0 = 0;

        	if (m == 1)
        		h0 = h1;
        	else
        		h0 = h2;

    	    //System.out.println("height is " + h0);

            if (null == stageRef) return calcY( 0.0, h0 );

            return calcY(stageRef.getY(), stageRef.getHeight());
        }

        private double calcX(final double LEFT, final double TOTAL_WIDTH) {
            switch (popupLocation) {
                case TOP_LEFT  : case CENTER_LEFT : case BOTTOM_LEFT  : return LEFT + offsetX;
                case TOP_CENTER: case CENTER      : case BOTTOM_CENTER: return LEFT + (TOTAL_WIDTH - width) * 0.5 - offsetX;
                case TOP_RIGHT : case CENTER_RIGHT: case BOTTOM_RIGHT : return LEFT + TOTAL_WIDTH - width - offsetX;
                default: return 0.0;
            }
        }
        private double calcY(final double TOP, final double TOTAL_HEIGHT ) {
            switch (popupLocation) {
                case TOP_LEFT   : case TOP_CENTER   : case TOP_RIGHT   : return TOP + offsetY;
                case CENTER_LEFT: case CENTER       : case CENTER_RIGHT: return TOP + (TOTAL_HEIGHT- height)/2 - offsetY;
                case BOTTOM_LEFT: case BOTTOM_CENTER: case BOTTOM_RIGHT: return TOP + TOTAL_HEIGHT - height - offsetY;
                default: return 0.0;
            }
        }

        //2016-06-27 Added getMonitor()
    	private int getMonitor(double position) {
    		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
    		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
    		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
    		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762

      		//System.out.println("count is "+ count);

    		if (count < 3) {

          		count++;
 	 	       // only need to add listener once
 		 		stageRef.xProperty().addListener((obs, oldVal, newVal) -> {
 		 			//System.out.println("X: " + newVal);
 		 			xPos = (double) newVal;
 		 		});

 		 		stageRef.yProperty().addListener((obs, oldVal, newVal) -> {
 		 			//System.out.println("y: " + newVal);
 		 			yPos = (double) newVal;
 		 		});

	    		StartUpLocation startUpLoc = new StartUpLocation(anchorPane.getPrefWidth(), anchorPane.getPrefHeight());
	            double x = startUpLoc.getXPos();
	            double y = startUpLoc.getYPos();
	            // Set Only if X and Y are not zero and were computed correctly
	         	//ObservableList<Screen> screens = Screen.getScreensForRectangle(xPos, yPos, 1, 1);
	         	//ObservableList<Screen> screens = Screen.getScreens();
	        	//System.out.println("# of monitors : " + screens.size());


	            if ( (Math.abs(x) < 2 * Double.MIN_VALUE) &&(Math.abs(y) < 2 * Double.MIN_VALUE)) {
	           	   // xPos = 0 and yPos = 0 in startUpLocation, there may be 1 or more screens
	            	//stage.centerOnScreen();
	            	//System.out.println("Your system has a 1-monitor setup. Window starting position is at monitor 1");
	                return 1;
	            }

	            else {
	          	   // in order for xPos != 0 and yPos != 0 in startUpLocation, there has to be more than 1 screen

	            	if (position > 0) {
	            		// if position can be > 0, then it has 2 screens
	                    //System.out.println("Your system has a 2-monitor setup. Window starting position is at (" + x + ", " + y + ")");
	                    return 2;
	            	}
	            	else {
		                //System.out.println("Your system has a 2-monitor setup. Window starting position is unknown");
		                return 1;
	            	}
	            }

    		}

    		else {
    			// Caution: this works only if the window moves around
          		if (position > 0) {
            		// if position can be > 0, then it's
                    //System.out.println("Your system has a 2-monitor setup. Window is positioned at (" + xPos + ", " + yPos + ")");
                    return 2;
            	}
            	else {
            		if (xPos > 1 || yPos > 1)
            			;//System.out.println("Your system has a 1-monitor setup. Window is positioned at (" + xPos + ", " + yPos + ")");
            		else
               			;//System.out.println("Your system has a 1-monitor setup. Window is positioned at monitor");
            		return 1;
            	}
    		}
    	}
    }

	public void destroy() {
		QUOTE_ICON = null;
		IMAGE = null;

	}
}
