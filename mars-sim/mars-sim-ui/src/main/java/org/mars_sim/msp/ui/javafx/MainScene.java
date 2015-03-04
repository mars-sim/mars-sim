/**
 * Mars Simulation Project
 * MainScene.java
 * @version 3.08 2015-02-25
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import static javafx.geometry.Orientation.VERTICAL;

import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.HiddenSidesPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.tools.Utils;

import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.UIConfig;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;


public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;
	
	// 2014-12-27 Added delay timer
	//private Timer delayLaunchTimer;
	//private Timer autosaveTimer;
	//private javax.swing.Timer earthTimer = null;
	private static int AUTOSAVE_EVERY_X_MINUTE = 10;
	private static final int TIME_DELAY = 940;
	
    private Text timeText;    
    private Text memUsedText;

    private int memMax;
    private int memTotal;
    private int memUsed, memUsedCache;
    private int memFree;
    

    private StringProperty timeStamp;
 
    private boolean cleanUI = true;
    //private boolean isLoadingFX;
    
    private MainDesktopPane desktop = null;
    private Stage stage;
    
    private MainWindowFXMenu menuBar;
    
    private SplitPane splitPane;
    private NotificationPane notificationPane;
    //private CheckBox cbUseDarkTheme;
    //private CheckBox cbHideCloseBtn;
    //private TextField textField;
    
    public MainScene(Stage stage) {
         	this.stage = stage;
    }
    
    public Scene createMainScene() {

        Scene scene = init();   
      
		startAutosaveTimer();        
		
        desktop.openInitialWindows();
        
        // Detect if a user hits ESC
        scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
              @Override
              public void handle(KeyEvent t) {
                if (t.getCode()==KeyCode.ESCAPE) {
                	//System.out.println("click on escape");
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

		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		boolean useDefault = UIConfig.INSTANCE.useUIDefault();

		setLookAndFeel(false);	

		// Detect if a user hits the top-right close button
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent event) {
		    	// Exit not just the stage but the simulation entirely
		    	exitSimulation();
		    }
		});
		
        //ImageView bg1 = new ImageView();
        //bg1.setImage(new Image("/images/splash.png"));  // in lieu of the interactive Mars map      
        //root.getChildren().add(bg1);     
	   
		// Create group to hold swingNode1 which holds the swing desktop
		StackPane swingPane = new StackPane();
		SwingNode swingNode1 = new SwingNode();
		createSwingNode1(swingNode1);
		swingPane.getChildren().add(swingNode1);
		//swingBox.setAutoSizeChildren(false);
		//swingPane.setPrefWidth(1024);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());
		//swingPane.setMaxWidth(Region.USE_PREF_SIZE);
		
		// Create ControlFX's StatusBar   
		StatusBar statusBar = createStatusBar(); 
		HBox notifyBar = new HBox();
		        
		VBox bottomBox = new VBox();
		//bottomBox.getChildren().addAll(node, statusBar);
		bottomBox.getChildren().addAll(statusBar);
		
		// Create menuBar
        menuBar = new MainWindowFXMenu(this, desktop);
        menuBar.getStylesheets().addAll("/fxui/css/mainskin.css");	
        
	    // Create BorderPane
		BorderPane borderPane = new BorderPane();  
	    borderPane.setTop(menuBar);	    
	    //borderPane.setTop(toolbar);    
	    borderPane.setBottom(bottomBox);
	    //borderPane.setStyle("-fx-background-color: palegorange");

	    VBox vBox = new VBox();
	    Label label = new Label("MarsNet");
	    label.setAlignment(Pos.TOP_CENTER);
	    //vBox.getStylesheets().addAll("/fxui/css/mainskin.css");
	    
	    //ListView<String> listView = new ListView<>(FXCollections.observableArrayList("List"));
	    //borderPane.setLeft(listView);

	    HiddenSidesPane pane = new HiddenSidesPane();
	    Text text = new Text("ABCDEFG");
	    pane.setContent(text);
	    Text bottomText = new Text("Note: under construction !!");
	    pane.setBottom(bottomText);	    
	    vBox.getChildren().addAll(label, pane);
	    
	    splitPane = new SplitPane();
	    //splitPane.setPrefWidth(100);
	    splitPane.setOrientation(Orientation.HORIZONTAL);
	    //splitPane.setMinWidth(Region.USE_PREF_SIZE);
	    splitPane.setDividerPositions(1.0f);
        //splitPane.setPrefSize(1000, 1280);
	    splitPane.getItems().addAll(swingPane, vBox);
	    
		Node node = createNotificationPane();
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notificationPane.setText("Breaking news for mars-simmers !!");
        notificationPane.hide();
	    
	    //borderPane.setCenter(splitPane);  
	    borderPane.setCenter(node);
	    
	    StackPane rootStackPane = new StackPane(borderPane);
	    //StackPane.setAlignment(borderPane, Pos.TOP_LEFT);	    
		
	    Scene scene = new Scene(rootStackPane, primaryScreenBounds.getWidth(), 640, Color.BROWN);
	    scene.getStylesheets().addAll("/fxui/css/mainskin.css");
	    
	    borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());    
	    
	    // Set up earth time text update
	    Timeline timeline = new Timeline(new KeyFrame(
	            Duration.millis(TIME_DELAY),
	            ae -> updateTimeText()));
	    timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
	    timeline.play();
   
        return (scene);
	}
	
	public StatusBar createStatusBar() {
		StatusBar statusBar = new StatusBar();

	    //statusBar.setAlignment(Pos.BASELINE_RIGHT);
	    //statusBar.setStyle("-fx-background-color: gainsboro;");
        //statusBar.setAlignment(Pos.CENTER);
        //statusBar.setStyle("-fx-border-stylel:solid; -fx-border-width:2pt; -fx-border-color:grey; -fx-font: 14 arial; -fx-text-fill: white; -fx-base: #cce6ff;");  
	    //statusBar.setMinHeight(memMaxText.getBoundsInLocal().getHeight() + 10);
	    //statusBar.setMijnWidth (memMaxText.getBoundsInLocal().getWidth()  + 10);
	    
        memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
	    Text memMaxText = new Text(" Total Designated : " + memMax +  " MB ");
	    //memMaxText.setTextAlignment(TextAlignment.RIGHT);
	    //statusBar.getChildren().add(memMaxText);
	    Button button1 = new Button(" [Memory] ");
        button1.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
	    statusBar.getRightItems().add(button1);
	    statusBar.getRightItems().add(memMaxText);
	    statusBar.getRightItems().add(new Separator(VERTICAL));
	    
	    memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
	    memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	    memUsed = memTotal - memFree;	       
	    memUsedText = new Text(" Currently Used : " + memUsed +  " MB ");
	    //memUsedText.setTextAlignment(TextAlignment.RIGHT);
	    //memUsedText.textProperty().bind(valueProperty);
	    //statusBar.getChildren().add(memUsedText);
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

        //String t = earthclock.getTimeStamp();
        //timeStamp = new SimpleStringProperty(earthclock.getTimeStamp());
	    timeText =  new Text(" Earth Side : " + timeStamp + "  ");
	    //timeText.setTextAlignment(TextAlignment.RIGHT);
	    //timeText.textProperty().bind(timeStamp);
	    Button button2 = new Button(" [Time] ");
        button2.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
	    statusBar.getRightItems().add(button2);
	    //statusBar.getChildren().add(timeText);	    
	    statusBar.getRightItems().add(timeText); 
	    statusBar.getRightItems().add(new Separator(VERTICAL));
	    
	    //TextFlow textFlow = new TextFlow(memMaxText, memUsedText, timeText);
	    //statusBar.getChildren().add(textFlow);
		
		return statusBar;
	}
	
	public NotificationPane getNotificationPane() {
		return notificationPane;
	}
	
	public Node createNotificationPane() {

        notificationPane = new NotificationPane(splitPane);
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

	    //public String getJavaDocURL() {
	    //    return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationPane.html";
	    //}

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
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
			desktop.clearDesktop();
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
                desktop.resetDesktop();
                //logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			desktop.disposeAnnouncementWindow();
			
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
				desktop,
				Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION
			) == JOptionPane.YES_OPTION
		) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.creatingNewSim")); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the single steps.
			Simulation.stopSimulation();

			try {
			    desktop.clearDesktop();
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

			JFrame frame = new JFrame();
			
			SimulationConfigEditor editor = new SimulationConfigEditor(
				frame.getOwner(), 
				SimulationConfig.instance()
			);
			editor.setVisible(true);

			Simulation.createNewSimulation();

			// Start the simulation.
			Simulation.instance().start();
			
			try {
                desktop.resetDesktop();
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			//startEarthTimer();

			desktop.disposeAnnouncementWindow();
			
			// Open user guide tool.
            desktop.openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
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
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation(fileLocn);			
		}
		else {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}
		
		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		Simulation.instance().getMasterClock().setPaused(false);
		desktop.disposeAnnouncementWindow();
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

		//if (changed) {
		//	SwingUtilities.updateComponentTreeUI(frame);
		//	if (desktop != null) {
		//		desktop.updateToolWindowLF();
		//	}
		//	desktop.updateAnnouncementWindowLF();
		//	desktop.updateTransportWizardLF();
		//}
	}

	public MainWindowFXMenu getMainWindowFXMenu() {
		return menuBar;
	}
	
	
	public Stage getStage() {
		return stage;
	}
	
    
	private void createSwingNode1(final SwingNode swingNode) {
		desktop = new MainDesktopPane(this);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(desktop);           
        });
    }

	public void closeMarsNet() {
		splitPane.setDividerPositions(1.0f);
	}

	public void openMarsNet() {
		splitPane.setDividerPositions(0.8f);
	}
}
