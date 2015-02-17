/**
 * Mars Simulation Project
 * MainWindowFX.java
 * @version 3.08 2015-01-28
 * @author Lars Næsbye Christensen
 */

package org.mars_sim.msp.ui.javafx;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
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
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;


public class MainScene {

	private static Logger logger = Logger.getLogger(MainScene.class.getName());

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;
	
	// 2014-12-27 Added delay timer
	private Timer delayLaunchTimer;
	private Timer autosaveTimer;
	private javax.swing.Timer earthTimer = null;
	private static int AUTOSAVE_MINUTES = 15;
	private static final int TIMEDELAY = 900;

    //protected ShowDateTime showDateTime;
    private JStatusBar statusBar;
    private JLabel leftLabel;
    private JLabel memMaxLabel;
    private JLabel memUsedLabel;
    //private JLabel dateLabel;
    private JLabel timeLabel;

    private int memMax;
    private int memTotal;
    private int memUsed, memUsedCache;
    private int memFree;
    
    private String statusText;
    private String earthTimeString;
 
    private boolean cleanUI = true;
    //private boolean isLoadingFX;
    
    private MainDesktopPane desktop = null;
    private Stage stage;
    
    private MainWindowFXMenu menuBar;
    
    public MainScene(Stage stage) {
         	this.stage = stage;
    }
    
    public Scene createMainScene() {

        Scene scene = init(stage);
        
		showStatusBar();		

		startAutosaveTimer();
        
        desktop.openInitialWindows();
        
        return (scene);
    }
	
    
	private void createSwingNode1(final SwingNode swingNode) {
		desktop = new MainDesktopPane(this);
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(desktop);           
        });
    }
	
	private void createSwingNode2(final SwingNode swingNode) {	    
        statusBar = new JStatusBar();
        SwingUtilities.invokeLater(() -> {
            swingNode.setContent(statusBar);           
        });
    }
	
	public Scene init(Stage stage) {
	
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent event) {
		    	exitSimulation();
		    }
		});
		
		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		boolean useDefault = UIConfig.INSTANCE.useUIDefault();

		setLookAndFeel(false);	

		BorderPane borderPane = new BorderPane();   
		BorderPane bottomPane = new BorderPane();
		//HBox statusBox = new HBox();
    
        Scene scene = new Scene(borderPane, 1024, 800, Color.WHITE);
        scene.getStylesheets().addAll("/fxui/css/mainskin.css");		
  
        //ImageView bg1 = new ImageView();
        //bg1.setImage(new Image("/images/splash.png"));  // in lieu of the interactive Mars map      
        //root.getChildren().add(bg1);
        
		final SwingNode swingNode1 = new SwingNode();
		createSwingNode1(swingNode1);
		final SwingNode swingNode2 = new SwingNode();
		createSwingNode2(swingNode2);

        menuBar = new MainWindowFXMenu(this, desktop);

	    borderPane.setTop(menuBar);	    
	    //borderPane.setTop(toolbar);  
	    borderPane.setBottom(bottomPane); 
		borderPane.setCenter(swingNode1);
	    //bottomPane.setBottom(statusBox);   
	    bottomPane.setBottom(swingNode2);     
	    
        //statusBox.getChildren().addAll(swingNode2);
        //statusBox.setAlignment(Pos.CENTER);
        //statusBox.setStyle("-fx-border-stylel:solid; -fx-border-width:1pt; -fx-border-color:grey;");  
        
        //statusText = "Mars-Sim 3.08 is running";
        leftLabel = new JLabel(statusText);
		statusBar.setLeftComponent(leftLabel);
    
        memMaxLabel = new JLabel();
        memMaxLabel.setHorizontalAlignment(JLabel.CENTER);
        memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
        memMaxLabel.setText("Total Designated Memory : " + memMax +  " MB");
        statusBar.addRightComponent(memMaxLabel, false);

        memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
        
        memUsedLabel = new JLabel();
        memUsedLabel.setHorizontalAlignment(JLabel.CENTER);
        memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
        memUsed = memTotal - memFree;
        memUsedLabel.setText("Current Used Memory : " + memUsed +  " MB");
        statusBar.addRightComponent(memUsedLabel, false);       
  
        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        statusBar.addRightComponent(timeLabel, false);

        //statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);
               
        return (scene);
	}
	
	public JInternalFrame createInternalFrame( String title, int width, int height ) {
	      final JInternalFrame frame = new JInternalFrame( title, true, true, true, true );
	      frame.setVisible( true );
	      frame.setSize( width, height );
	      return frame;
	   }

	// 2015-02-05 Added showEarthTime()
	public void showStatusBar() {

		if (earthTimer == null) {	
			delayLaunchTimer = new Timer();
			int millisec = 500;
			delayLaunchTimer.schedule(new StatusBar(), millisec );	
		}
	}
	
	
	// 2015-01-07 Added startAutosaveTimer()	
	public void startAutosaveTimer() {
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

    }
	
	
	// 2015-01-13 Added startEarthTimer()
	public void startEarthTimer() {
	
		earthTimer = new javax.swing.Timer(TIMEDELAY, 
			new ActionListener() {		
			String t = null;
			    @SuppressWarnings("deprecation")
				@Override
			    public void actionPerformed(ActionEvent evt) {
				    try {
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
				        }
				    }
				    catch (Exception ee) {
				        ee.printStackTrace(System.err);
				    }
					timeLabel.setText("Earth Time : " + t);
					memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;			        
					memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	                memUsed = memTotal - memFree;
	                int mem = ( memUsedCache + memUsed ) /2;
	                if (mem > memUsedCache * 1.1 || mem < memUsedCache * 0.9)
	                	memUsedLabel.setText("Current Used Memory : " + mem +  " MB");
	                memUsedCache = mem;
			    }
			});
	
		earthTimer.start();
	}
	
	// 2015-01-19 Added StatusBar
	class StatusBar extends TimerTask { // (final String t) {
		public void run() {		
			startEarthTimer();
			//delayLaunchTimer.cancel();
		}
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
        if (earthTimer != null) 
            earthTimer.stop();
        earthTimer = null;
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
		
		if (earthTimer == null) {
			delayLaunchTimer = new Timer();
			int seconds = 1;
			delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		}
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
		if (earthTimer == null) {
			//System.out.println(" newSimulation() : earthTimer == null");
			delayLaunchTimer = new Timer();
			int seconds = 1;
			delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		}
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
			    if (earthTimer != null) {
                    earthTimer.stop();
			    }
                earthTimer = null;
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
			
			startEarthTimer();

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
		
		earthTimer = null;
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
	
}
