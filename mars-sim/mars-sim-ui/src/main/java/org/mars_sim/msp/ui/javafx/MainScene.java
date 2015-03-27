/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2015-03-21
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;

public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	private static int AUTOSAVE_EVERY_X_MINUTE = 10;
	private static final int TIME_DELAY = 940;
	
	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;
	
    private Text timeText;    
    private Text memUsedText;
    //private SplitPane splitPane;

    private StringProperty timeStamp;
    
    private Tab swingTab;
    private Tab settlementTab; 
    private TabPane tp;
    
    private int memMax;
    private int memTotal;
    private int memUsed, memUsedCache;
    private int memFree;  

    private boolean cleanUI = true;
	boolean useDefault;
    
    private MainDesktopPane desktop;
    private MainWindow mainWindow;
    private Stage stage;   
    private MainWindowFXMenu menuBar;
    private NotificationPane notificationPane;
    
    public MainScene(Stage stage) {
         	this.stage = stage;
    }
    
    public Scene createMainScene() {

        Scene scene = init();   
        
		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		useDefault = UIConfig.INSTANCE.useUIDefault();
		//setLookAndFeel(false);	
	
		startAutosaveTimer();        		
        //desktop.openInitialWindows(); // doesn't work here
		
        // Detect if a user hits ESC
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
              @Override
              public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ESCAPE) {
                 	// Toggle the full screen mode to OFF in the pull-down menu under setting
                	menuBar.exitFullScreen();
                	// close the MarsNet side panel
                	closeMarsNet();
                }
              }
          });
        
        return scene;
    }
	
	@SuppressWarnings("unchecked")
	public Scene init() {
		//TODO: refresh the pull down menu and statusBar when clicking the maximize/iconify/restore button on top-right.		

		// Detect if a user hits the top-right close button
		//stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		//    @Override
		//    public void handle(WindowEvent event) {
		    	// Exit not just the stage but the simulation entirely
		//    	exitSimulation();
		//    }});
		
		// Exit not just the stage but the simulation entirely
		stage.setOnCloseRequest(e -> exitSimulation());
		
        //ImageView bg1 = new ImageView();
        //bg1.setImage(new Image("/images/splash.png"));  // in lieu of the interactive Mars map      
        //root.getChildren().add(bg1);     
	   
		// Create group to hold swingNode1 which holds the swing desktop
		StackPane swingPane = new StackPane();
		SwingNode swingNode = new SwingNode();
		createSwingNode(swingNode);
		swingPane.getChildren().add(swingNode);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());
		
		// Create ControlFX's StatusBar   
		StatusBar statusBar = createStatusBar(); 	        
		VBox bottomBox = new VBox();
		bottomBox.getChildren().addAll(statusBar);
		
		// Create menuBar
        menuBar = new MainWindowFXMenu(this, getDesktop());
        menuBar.getStylesheets().addAll("/fxui/css/mainskin.css");	
        
	    // Create BorderPane
		BorderPane borderPane = new BorderPane();  
	    borderPane.setTop(menuBar);	    
	    //borderPane.setTop(toolbar);    
	    borderPane.setBottom(bottomBox);
	    //borderPane.setStyle("-fx-background-color: palegorange");


	    //TabPane tpFX = new TabPane();
	    settlementTab = new Tab();
	    settlementTab.setClosable(false);
	    //tpFX.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
	    settlementTab.setText("MarsNet");
	    //settlementTab.setContent(new Rectangle(200,200, Color.LIGHTSTEELBLUE));
	    settlementTab.setContent(createPane("black"));
	    //tpFX.getTabs().add(settlementTab);
	    
/*
	    // create a button to toggle floating.
	    final RadioButton floatControl = new RadioButton("Toggle floating");
	    floatControl.selectedProperty().addListener(new ChangeListener<Boolean>() {
	      @Override public void changed(ObservableValue<? extends Boolean> prop, Boolean wasSelected, Boolean isSelected) {
	        if (isSelected) {
	          tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
	        } else {
	          tabPane.getStyleClass().remove(TabPane.STYLE_CLASS_FLOATING);
	        }
	      }
	    });
*/
	    // layout the stage.
	    //VBox layout = new VBox(10);
	    //layout.getChildren().addAll(tabPane);
	    //VBox.setVgrow(tabPane, Priority.ALWAYS);
	    //layout.setStyle("-fx-padding: 10;");	    
	    
	    tp = new TabPane();
	    tp.setSide(Side.RIGHT);
	    swingTab = new Tab();
	    swingTab.setClosable(false);
	    swingTab.setText("Swing");
	    swingTab.setContent(swingPane);
	    
	    tp.getSelectionModel().select(swingTab);
	    tp.getTabs().addAll(swingTab,settlementTab);
/*	    
	    splitPane = new SplitPane();
	    //splitPane.setPrefWidth(100);
	    splitPane.setOrientation(Orientation.HORIZONTAL);
	    //splitPane.setMinWidth(Region.USE_PREF_SIZE);
	    splitPane.setDividerPositions(1.0f);
	    splitPane.getItems().addAll(swingPane, layout);
*/	    
		Node node = createNotificationPane();
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setText("Breaking news for mars-simmers !!");
        notificationPane.hide();
	    
	    //borderPane.setCenter(splitPane);  
	    borderPane.setCenter(node);
	    StackPane rootStackPane = new StackPane(borderPane);	    
		
	    Scene scene = new Scene(rootStackPane, primaryScreenBounds.getWidth(), 640, Color.BROWN);
	    scene.getStylesheets().add("/fxui/css/mainskin.css");
	    
	    borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());    
	    
	    // Set up earth time text update
	    Timeline timeline = new Timeline(new KeyFrame(
	            Duration.millis(TIME_DELAY),
	            ae -> updateTimeText()));
	    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
	    timeline.play();
        return scene;
	}
	
	  // create a pane of a given color to hold tab content.
	  private Pane createPane(String color) {
	    Pane pane = new Pane();
	    //pane.setPrefSize(400, 200);
	    pane.setStyle("-fx-background-color: " + color);
	    VBox v = new VBox();
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20)); 
	    
	    Collection<Settlement> settlements = Simulation.instance().getUnitManager().getSettlements();	    
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		Iterator<Settlement> i = settlementList.iterator();
		while(i.hasNext()) {	
			Settlement settlement = i.next();
			String sname = settlement.getName();
			Label label = new Label(" ");
			v.getChildren().addAll(label, createButton(settlement));
		}
		
		pane.getChildren().add(v);
		
	    return pane;
	  }
	  
	public Button createButton(Settlement settlement) {
		Button b = new Button(settlement.getName());
		b.setPadding(new Insets(20));
		b.setMaxWidth(Double.MAX_VALUE);

		b.setId("settlement-node");
		b.getStylesheets().add("/fxui/css/settlementnode.css");
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
        	PopOver popOver = null;
            @Override
            public void handle(MouseEvent evt) {
            	if (popOver == null) {
            		// TODO: the new popover will go to the front, pushing the old popover to the background
                    popOver = createPopOver(b, settlement);
            	}
            	
            	else if (popOver.isShowing() && !popOver.isDetached()) {
                    popOver.hide(Duration.seconds(.5));  // (Duration.ZERO)
                    //popOver = null;
                }

            	else if (popOver.isShowing() && popOver.isDetached()) {
                    popOver.hide(Duration.ZERO);  // (Duration.ZERO)
                    //popOver.show(b);
                    //popOver.setDetached(true);
                }         	
            	
            	else if (!popOver.isShowing()) {
                    popOver = createPopOver(b, settlement);
                    //popOver.setDetached(false);
            		//popOver.show(b);
                }
            	
            	else if (evt.getClickCount() == 2 ) {
            		if (!popOver.isShowing()) {
            			popOver = createPopOver(b, settlement);
            			popOver.setDetached(false);
            		}
            		//popOver.show(b);
                }   
            	
            }
        });
        /*
		// Handle Button event.
		b.setOnAction((event) -> {

			if (popover == null)
				popover = createPopOver(b);
			else 
				popover.hide();
			//System.out.println("create popover");
		});
				
		
		b.setOnMouseDragged(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent t) {
	            if (t.isPrimaryButtonDown()) {
	                System.out.println("rockets armed");
	            }
	            if (t.isSecondaryButtonDown()) {
	                System.out.println("autoaim engaged");
	            }            
	        }
	    });
		*/		
		
		//b.setStyle("-fx-background-color: orange");
	       
		return b;
	}	  
	
	public PopOver createPopOver(Button b, Unit unit) {
		Settlement settlement = null;
		Person person = null;
		if (unit instanceof Settlement)
			settlement = (Settlement) unit;
		else if (unit instanceof Person)
			person = (Person) unit;
		
		//isPopped = true;
		String title = b.getText();
		PopOver popover = new PopOver();	
		popover.setDetachedTitle(title);
		popover.setDetachable(true);
        popover.show(b);
        
		/*
        popover.setOnShown(evt -> {
        // The user clicked somewhere into the transparent background. If
        // this is the case the hide the window (when attached).
	    	popover.getScene().addEventHandler(MOUSE_CLICKED, mouseEvent -> {
	            if (mouseEvent.getTarget().equals(popover.getScene().getRoot())) {
	                if (!popover.isDetached()) {
	                	popover.hide();
	                }
	            }
	        });
	
	    });
	      */ 
        HBox root = new HBox();
        
        if (settlement != null) {
			//String sname = settlement.getName();
			Label label = new Label("                   ");
			
	        VBox yesaccordion = new VBox();       
	        Accordion acc = new Accordion();
	        acc.getPanes().addAll(this.createPanes(settlement));
	        yesaccordion.getChildren().add(acc);
	        root.getChildren().addAll(label, yesaccordion);	 
        }
        else if (person != null) {
			String sname = person.getName();
			Label label = new Label("  ");
        	
        }
        
        // Fade In
        Node skinNode = popover.getSkin().getNode();
        skinNode.setOpacity(0);

        Duration fadeInDuration = Duration.seconds(1);
        
        FadeTransition fadeIn = new FadeTransition(fadeInDuration, skinNode);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        popover.setContentNode(root);
        
        return popover;
	}
	
	private Collection<TitledPane> createPanes(Settlement settlement){
        Collection<TitledPane> result = new ArrayList<TitledPane>();
        TitledPane tp = new TitledPane();
        tp.setText("Population");
        
        String family = "Helvetica";
        double size = 12;
     
        TextFlow textFlow = new TextFlow();
        textFlow.setLayoutX(80);
        textFlow.setLayoutY(80);
        Text text1 = new Text("Settlers: " + settlement.getCurrentPopulationNum() + " of " + settlement.getPopulationCapacity() + "\n");
        text1.setFont(Font.font(family, FontWeight.LIGHT, size));
        //Text text2 = new Text("\nNames: " + settlement.getInhabitants());
        //text2.setFont(Font.font(family, FontPosture.ITALIC, size));
        Text text3 = new Text("Bots: " + settlement.getCurrentNumOfRobots() + " of " + settlement.getRobotCapacity() + "\n");
        text3.setFont(Font.font(family, FontWeight.LIGHT, size));
        //Text text4 = new Text("\nNames: " + settlement.getRobots());
        //text4.setFont(Font.font(family, FontPosture.ITALIC, size));
        textFlow.getChildren().addAll(text1, text3);
        
        //TextArea ta1 = new TextArea();
        //ta1.setText("Settlers: " + settlement.getCurrentPopulationNum() + " of " + settlement.getPopulationCapacity() );
        //ta1.appendText("Names: " + settlement.getInhabitants());       
        //ta1.appendText("Bots: " + settlement.getCurrentNumOfRobots() + " of " + settlement.getRobotCapacity() );
        //ta1.appendText("Names: " + settlement.getRobots());
 
        ScrollPane s1 = new ScrollPane();
        s1.setPrefSize(200, 200);
        s1.setContent(createPeople(settlement));
     // Add listener to set ScrollPane FitToWidth/FitToHeight when viewport bounds changes
        //s1.viewportBoundsProperty().addListener(new ChangeListener() {
        //  public void changed(ObservableValue<? extends Bounds> arg0, Bounds arg1, Bounds arg2) {
         //     Node content = s1.getContent();
        //      s1.setFitToWidth(content.prefWidth(-1)<arg2.getWidth());
        //      s1.setFitToHeight(content.prefHeight(-1)<arg2.getHeight());
        //    }}});
        
        VBox vb = new VBox();
        vb.getChildren().addAll(textFlow, s1);
        tp.setContent(vb);    
        
        result.add(tp);
        
        tp = new TitledPane();
        tp.setText("Food Preparation");
        tp.setContent(new Button("Kitchen 1"));
        result.add(tp);
        
        tp = new TitledPane();
        tp.setText("Greenhouse");
        createGreenhouses(tp, settlement);       
        result.add(tp);
        
        return result;
    }
	
	public VBox createPeople(Settlement settlement) {
	    //BorderPane border = new BorderPane();
	    //border.setPadding(new Insets(20, 0, 20, 20));

	    VBox v = new VBox();
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20)); 
	    

	    Collection<Person> persons = settlement.getInhabitants();
		List<Person> personList = new ArrayList<Person>(persons);
		Iterator<Person> i = personList.iterator();
		while(i.hasNext()) {	
			Person person = i.next();
			String sname = person.getName();
			v.getChildren().add(createPersonButton(person));
		}
		
		return v;
	}
	
	public Button createPersonButton(Person person) {
		Button b = new Button(person.getName());
		b.setPadding(new Insets(20));
		b.setMaxWidth(Double.MAX_VALUE);
		b.setId("settlement-node");
		b.getStylesheets().add("/fxui/css/personnode.css");
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
        	PopOver popOver = null;
            @Override
            public void handle(MouseEvent evt) {
            	if (popOver == null) {
            		// TODO: the new popover will go to the front, pushing the old popover to the background
                    popOver = createPopOver(b, person);
            	}
            	
            	else if (popOver.isShowing() && !popOver.isDetached()) {
                    popOver.hide(Duration.seconds(.5));  // (Duration.ZERO)
                    //popOver = null;
                }

            	else if (popOver.isShowing() && popOver.isDetached()) {
                    popOver.hide(Duration.ZERO);  // (Duration.ZERO)
                    //popOver.show(b);
                    //popOver.setDetached(true);
                }         	
            	
            	else if (!popOver.isShowing()) {
                    popOver = createPopOver(b, person);
                    //popOver.setDetached(false);
            		//popOver.show(b);
                }
            	
            	else if (evt.getClickCount() == 2 ) {
            		if (!popOver.isShowing()) {
            			popOver = createPopOver(b, person);
            			popOver.setDetached(false);
            		}
            		//popOver.show(b);
                }   
            	
            }
        });
 
		return b;
	}
	
    public void createGreenhouses(TitledPane tp, Settlement settlement) {
    	VBox v = new VBox();
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20)); 
   	
    	List<Building> buildings = settlement.getBuildingManager().getBuildings();   	
    	
		Iterator<Building> iter1 = buildings.iterator();
		while (iter1.hasNext()) {
			Building building = iter1.next();				
	    	if (building.hasFunction(BuildingFunction.FARMING)) {
	//        	try {
	        		Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
	            	Button b = createGreenhouseDialog(farm);
	            	v.getChildren().add(b);
	//        	}
	//        	catch (BuildingException e) {}
	        }
		}

    
	    tp.setContent(v);//"1 2 3 4 5..."));
	    tp.setExpanded(true);
	    
    }
    

	public Button createGreenhouseDialog(Farming farm) {
		String name = farm.getBuilding().getNickName();
		Button b = new Button(name);
		b.setMaxWidth(Double.MAX_VALUE);
		
        List<String> choices = new ArrayList<>();
        choices.add("Lettuce");
        choices.add("Green Peas");
        choices.add("Carrot");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("List of Crops", choices);
        dialog.setTitle(name);
        dialog.setHeaderText("Plant a Crop");
        dialog.setContentText("Choose Your Crop:");
        dialog.initOwner(stage); // post the same icon from stage
        dialog.initStyle(StageStyle.UTILITY);
        //dialog.initModality(Modality.NONE);
        
		b.setPadding(new Insets(20));
		b.setId("settlement-node");
		b.getStylesheets().add("/fxui/css/settlementnode.css");
	    b.setOnAction(e->{
	        // The Java 8 way to get the response value (with lambda expression).
	    	Optional<String> selected = dialog.showAndWait();
	        selected.ifPresent(crop -> System.out.println("Crop added to the queue: " + crop));
	    });
		
	   //ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	   //ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
	   //dialog.getButtonTypes().setAll(buttonTypeCancel, buttonTypeOk);
	    
	    return b;
	}
	
	/*
	public void createDialog(Alert alert) {
	    
	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.getStylesheets().add(
	    //getClass().getResource("dialog.css").toExternalForm());
	    "/fxui/css/dialog.css");
	    
	    final DialogPane dlgPane = dlg.getDialogPane(); 
	    dlgPane.getButtonTypes().add(ButtonType.OK); 
	    dlgPane.getButtonTypes().add(ButtonType.CANCEL); 

	    final Button btOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK); 
	    btOk.addEventFilter(ActionEvent.ACTION, (event) -> { 
	      if (!validateAndStore()) { 
	        event.consume(); 
	      } 
	    }); 

	    dlg.showAndWait();
	}
	*/
	
	public StatusBar createStatusBar() {
		StatusBar statusBar = new StatusBar();
		statusBar.setText(""); // needed for deleting the default text "OK"
	    //statusBar.setAlignment(Pos.BASELINE_RIGHT);
	    //statusBar.setStyle("-fx-background-color: gainsboro;");
        //statusBar.setAlignment(Pos.CENTER);
        //statusBar.setStyle("-fx-border-stylel:solid; -fx-border-width:2pt; -fx-border-color:grey; -fx-font: 14 arial; -fx-text-fill: white; -fx-base: #cce6ff;");  
	    //statusBar.setMinHeight(memMaxText.getBoundsInLocal().getHeight() + 10);
	    //statusBar.setMijnWidth (memMaxText.getBoundsInLocal().getWidth()  + 10);
	    

	    Button button1 = new Button(" [Memory] ");
        button1.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
        statusBar.getRightItems().add(new Separator(VERTICAL));
	    statusBar.getRightItems().add(button1);
	    
        memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
	    Text memMaxText = new Text(" Total Designated : " + memMax +  " MB ");
	    statusBar.getRightItems().add(memMaxText);
    
	    memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
	    memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	    memUsed = memTotal - memFree;	       
	    memUsedText = new Text(" Currently Used : " + memUsed +  " MB ");
	    statusBar.getRightItems().add(memUsedText);
	    statusBar.getRightItems().add(new Separator(VERTICAL));
	    
        MasterClock master = Simulation.instance().getMasterClock();
        if (master == null) {
            throw new IllegalStateException("master clock is null");
        }
        EarthClock earthclock = master.getEarthClock();
        if (earthclock == null) {
            throw new IllegalStateException("earthclock is null"); 
        }

	    timeText =  new Text(" Earth Side : " + timeStamp + "  ");
	    Button button2 = new Button(" [Time] ");
        button2.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
	    statusBar.getRightItems().add(button2);	    
	    statusBar.getRightItems().add(timeText); 
	    statusBar.getRightItems().add(new Separator(VERTICAL));
	    
		return statusBar;
	}
	
	public NotificationPane getNotificationPane() {
		return notificationPane;
	}
	
	public Node createNotificationPane() {

        //notificationPane = new NotificationPane(splitPane);
        notificationPane = new NotificationPane(tp);   
        String imagePath = getClass().getResource("/notification/notification-pane-warning.png").toExternalForm();
        ImageView image = new ImageView(imagePath);
        notificationPane.setGraphic(image);
        notificationPane.getActions().addAll(new Action("Sync", ae -> {
                // do sync, then hide...
                notificationPane.hide();
        }));
        
 
        return notificationPane;
    }
    
		public String getSampleName() {
	        return "Notification Pane";
	    }


	    public String getControlStylesheetURL() {
	        return "/org/controlsfx/control/notificationpane.css";
	    }
/*
	    private void updateBar() {
	        boolean useDarkTheme = cbUseDarkTheme.isSelected();
	        if (useDarkTheme) {
	            notificationPane.setText("Hello World! Using the dark theme");
	            notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
	        } else {
	            notificationPane.setText("Hello World! Using the light theme");
	            notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
	        }
	    }
*/	    
	public void updateTimeText() {

		String t = null;
		//try {
	        // Check if new simulation is being created or loaded from file.
	        if (!Simulation.isUpdating()) {
	             
	            MasterClock master = Simulation.instance().getMasterClock();
	            if (master == null) {
	                throw new IllegalStateException("master clock is null");
	            }
	            EarthClock earthclock = master.getEarthClock();
	            if (earthclock == null) {
	                throw new IllegalStateException("earthclock is null"); 
	            }
	            t = earthclock.getTimeStamp();
	            //timeStamp = new SimpleStringProperty(earthclock.getTimeStamp());
	        }
	    //}
	   // catch (Exception ee) {
	    //    ee.printStackTrace(System.err);
	    //}
		timeText.setText(" Earth Side : " + t + "  ");
		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;			        
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	    memUsed = memTotal - memFree;
	    //int mem = ( memUsedCache + memUsed ) /2;
	    if (memUsed > memUsedCache * 1.1 || memUsed < memUsedCache * 0.9) {
	    	memUsedText.setText(" Currently Used : " + memUsed +  " MB ");
	    }
    	memUsedCache = memUsed;

	}


	// 2015-01-07 Added startAutosaveTimer()	
	public void startAutosaveTimer() {
		/*
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                autosaveTimer.cancel();
    			saveSimulation(true,true);
    			startAutosaveTimer();
            }
        };
        autosaveTimer = new Timer();
        autosaveTimer.schedule(timerTask, 1000* 60 * AUTOSAVE_MINUTES);
*/
        
	    Timeline timeline = new Timeline(new KeyFrame(
	            Duration.millis(1000*60*AUTOSAVE_EVERY_X_MINUTE),
	            ae -> saveSimulation(true,true)));
	    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
	    timeline.play();
	    
    }
	
	
	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		//return mainWindow.getDesktop();
		return desktop;
	}

	/**
	 * Load a previously saved simulation.
	 */
	// 2015-01-25 Added autosave
	public void loadSimulation(boolean autosave) {	
		final boolean ans = autosave;
        //if (earthTimer != null) 
        //    earthTimer.stop();
        //earthTimer = null;
		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					loadSimulationProcess(ans);
				}
			};
			loadSimThread.start();
		} else {
			loadSimThread.interrupt();
		}
		
		//if (earthTimer == null) {
		//	delayLaunchTimer = new Timer();
		//	int seconds = 1;
		//	delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		//}
		
	}


	/**
	 * Performs the process of loading a simulation.
	 */
	private void loadSimulationProcess(boolean autosave) {
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (autosave) {			
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		}
		else {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}
		JFileChooser chooser= new JFileChooser(dir);
		chooser.setDialogTitle(title); //$NON-NLS-1$
		if (chooser.showOpenDialog(new Frame()) == JFileChooser.APPROVE_OPTION) {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
			getDesktop().clearDesktop();
			MasterClock clock = Simulation.instance().getMasterClock();
			clock.loadSimulation(chooser.getSelectedFile());
			while (clock.isLoadingSimulation()) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
				}
			}
			
			try {
				getDesktop().resetDesktop();
                //logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			getDesktop().disposeAnnouncementWindow();
			
		}
	}
	
	/**
	 * Performs the process of loading a simulation.
	 // (NOT finished) USE JAVAFX's FileChooser instead of swing's JFileChooser
	   
	private void loadSimulationProcess(boolean autosave) {
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (autosave) {			
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		}
		else {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}
		//JFileChooser chooser= new JFileChooser(dir);
		FileChooser chooser = new FileChooser();
		//chooser.setInitialFileName(dir);
		//Set to user directory or go to default if cannot access
		//String userDirectoryString = System.getProperty("user.home");
		File userDirectory = new File(dir);
		chooser.setInitialDirectory(userDirectory);	
		chooser.setTitle(title); //$NON-NLS-1$
		chooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Text Files", "*.txt"),
		         new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
		         new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
		         new ExtensionFilter("All Files", "*.*"));
		 
		File selectedFile = chooser.showOpenDialog(stage);
		//if (selectedFile != null) stage.display(selectedFile);
		 

		if (chooser.showOpenDialog(stage) == FileChooser.APPROVE_OPTION) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
			desktop.clearDesktop();
			MasterClock clock = Simulation.instance().getMasterClock();
			clock.loadSimulation(selectedFile);
			while (clock.isLoadingSimulation()) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
				}
			}
			
			try {
                desktop.resetDesktop();
                //logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			desktop.disposeAnnouncementWindow();
			
			// Open navigator tool after loading.
//			desktop.openToolWindow(NavigatorWindow.NAME);
		}
	}
*/
	/**
	 * Create a new simulation.
	 */
	public void newSimulation() {
		if ((newSimThread == null) || !newSimThread.isAlive()) {
			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					newSimulationProcess();
				}
			};
			newSimThread.start();
		} else {
			newSimThread.interrupt();
		}
	
		// 2015-01-19 Added using delayLaunchTimer to launch earthTime 
		//if (earthTimer == null) {
		//	//System.out.println(" newSimulation() : earthTimer == null");
		//	delayLaunchTimer = new Timer();
		//	int seconds = 1;
		//	delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		//}
	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	private void newSimulationProcess() {
		if (
			JOptionPane.showConfirmDialog(
					getDesktop(),
				Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION
			) == JOptionPane.YES_OPTION
		) {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.creatingNewSim")); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the single steps.
			Simulation.stopSimulation();

			try {
				getDesktop().clearDesktop();
			    //if (earthTimer != null) {
                //    earthTimer.stop();
			    //}
                //earthTimer = null;
			}
			catch (Exception e) {
			    // New simulation process should continue even if there's an exception in the UI.
			    logger.severe(e.getMessage());
			    e.printStackTrace(System.err);
			}
			
			SimulationConfig.loadConfig();
			
			//SimulationConfigEditor editor = new SimulationConfigEditor(mainMenu, 
			//	SimulationConfig.instance());
			//editor.setVisible(true);

			//Simulation.createNewSimulation();

			// Start the simulation.
			//Simulation.instance().start();
			
			try {
				getDesktop().resetDesktop();
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			//startEarthTimer();
			getDesktop().disposeAnnouncementWindow();
			
			// Open user guide tool.
			getDesktop().openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide = (GuideWindow) getDesktop().getToolWindow(GuideWindow.NAME);
            ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * @param useDefault Should the user be allowed to override location?
	 */
	public void saveSimulation(final boolean useDefault, final boolean isAutosave) {
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {		
					saveSimulationProcess(useDefault, isAutosave);
				}
			};
			saveSimThread.start();
		} else {
			saveSimThread.interrupt();
		}
	}

	/**
	 * Performs the process of saving a simulation.
	 */
    // 2015-01-08 Added autosave
	private void saveSimulationProcess(boolean useDefault, boolean isAutosave) {
		File fileLocn = null;

		if (!useDefault) {
			JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(new Frame()) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			} else {
				return;
			}
		}

		MasterClock clock = Simulation.instance().getMasterClock();
		
		if (isAutosave) {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation(fileLocn);			
		}
		else {
			getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}
		
		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}
		getDesktop().disposeAnnouncementWindow();
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		getDesktop().openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		Simulation.instance().getMasterClock().setPaused(false);
		getDesktop().disposeAnnouncementWindow();
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		//logger.info("Exiting simulation");

		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
		Simulation sim = Simulation.instance();
		try {
			sim.getMasterClock().saveSimulation(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}

		sim.getMasterClock().exitProgram();
		
		//earthTimer = null;
	}

	/**
	 * Sets the look and feel of the UI
	 * @param nativeLookAndFeel true if native look and feel should be used.
	 */
	public void setLookAndFeel(boolean nativeLookAndFeel) {
		boolean changed = false;
		if (nativeLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else {
			try {
				// Set Nimbus look & feel if found in JVM.
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						changed = true;
						break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {
		//	SwingUtilities.updateComponentTreeUI(frame);
			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateAnnouncementWindowLF();
				desktop.updateTransportWizardLF();
			}
		}
	}

	public MainWindowFXMenu getMainWindowFXMenu() {
		return menuBar;
	}
	
	
	public Stage getStage() {
		return stage;
	}
	
	public MainWindow getMainWindow() {
		return mainWindow;	
	}
    
	private void createSwingNode(final SwingNode swingNode) {
		desktop = new MainDesktopPane(this);
		//mainWindow = new MainWindow(true, true);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(desktop);
    		setLookAndFeel(false);
            //swingNode.setContent(mainWindow);
        });
    }

	public void closeMarsNet() {
		//splitPane.setDividerPositions(1.0f);
	    tp.getSelectionModel().select(swingTab);
	}

	public void openMarsNet() {
		//splitPane.setDividerPositions(0.8f);
	    tp.getSelectionModel().select(settlementTab);
	}
}
