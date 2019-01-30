/**
 * Mars Simulation Project
 * ConstructionWizard.java
 * @version 3.1.0 2017-09-21
 * @author Manny Kung
 */
package org.mars_sim.javafx;

import javax.swing.JOptionPane;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.stage.Modality;

import java.awt.Cursor;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConstructionWizard class is a class for hosting construction event manually.
 *
 */
@SuppressWarnings("restriction")
public class ConstructionWizard {

	/** default logger. */
	private static Logger logger = Logger.getLogger(ConstructionWizard.class.getName());

	/** Time (millisols) required to prepare construction site for stage. */
	//private static final double SITE_PREPARE_TIME = BuildingConstructionMission.SITE_PREPARE_TIME;

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = BuildingConstructionMission.DEFAULT_VARIABLE_BUILDING_WIDTH;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = BuildingConstructionMission.DEFAULT_VARIABLE_BUILDING_LENGTH;

	public static final double DEFAULT_HAB_BUILDING_DISTANCE = 5D;

	public static final double DEFAULT_SMALL_GREENHOUSE_DISTANCE = 5D;

	public static final double DEFAULT_LARGE_GREENHOUSE_DISTANCE = 5D;

	public static final double DEFAULT_RECT_DISTANCE = 5D;
	
	// Default distance between buildings for construction.
	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = BuildingConstructionMission.DEFAULT_INHABITABLE_BUILDING_DISTANCE;
	//private static final double DEFAULT_NONINHABITABLE_BUILDING_DISTANCE = BuildingConstructionMission.DEFAULT_NONINHABITABLE_BUILDING_DISTANCE;

    /** Minimum length of a building connector (meters). */
    private static final double MINIMUM_CONNECTOR_LENGTH = BuildingConstructionMission.MINIMUM_CONNECTOR_LENGTH;

    private final static String TITLE = "Construction Wizard";

    private boolean upKeyPressed = false;
    private boolean downKeyPressed = false;
    private boolean leftKeyPressed = false;
    private boolean rightKeyPressed = false;
    private boolean turnKeyPressed = false;
    
    private static int wait_time_in_secs = 90; // in seconds

    private double xLast, yLast;

	//private Settlement settlement;
	private static SettlementWindow settlementWindow;
	private static SettlementMapPanel mapPanel;
	private static MainScene mainScene;
	
	//private static MarsClock sitePreparationStartTime;
	//private static BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	private static MissionManager missionManager = Simulation.instance().getMissionManager(); 
  
	private static MainDesktopPane desktop;

	/**
	 * Constructor 1.
	 * For non-javaFX UI
	 * @param desktop the main desktop pane.
	 */
	public ConstructionWizard(final MainDesktopPane desktop) {
		ConstructionWizard.desktop = desktop;
		ConstructionWizard.mainScene = desktop.getMainScene();
		ConstructionWizard.settlementWindow = desktop.getSettlementWindow();
		ConstructionWizard.mapPanel = settlementWindow.getMapPanel();
	}

	/**
	 * Constructor 2.
	 * For JavaFX UI
	 * @param mainScene the main scene
	 */
	public ConstructionWizard(final MainScene mainScene, MainDesktopPane desktop) {
		ConstructionWizard.desktop = desktop;
		ConstructionWizard.mainScene = desktop.getMainScene();
		//this.settlementWindow = desktop.getSettlementWindow();
		//this.mapPanel = settlementWindow.getMapPanel();
	}

	public synchronized void selectSite(BuildingConstructionMission mission) {
		//logger.info("ConstructionWizard's selectSite() is in " + Thread.currentThread().getName() + " Thread");

		if (settlementWindow == null)
    		settlementWindow = desktop.getSettlementWindow();
    	if (mapPanel == null)
    		mapPanel = settlementWindow.getMapPanel();

		ConstructionSite site = mission.getConstructionSite();
	    Settlement settlement = site.getSettlement();
	    ConstructionManager mgr = settlement.getConstructionManager();

		if (mainScene != null) {
			mainScene.setSettlement(mgr.getSettlement());
		}
		else {
			// Select the relevant settlement
			desktop.openToolWindow(SettlementWindow.NAME);
			settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox()
				.setSelectedItem(mgr.getSettlement());
		}


		boolean isSitePicked = mission.getConstructionSite().getSitePicked();
	    boolean manual = mission.getConstructionSite().getManual();

	    int site_case = 1;
	    if (manual) {
	    	site_case = 2;
	    	if (!isSitePicked)
	    		site_case = 3;
	    }

	    //previous = 0;

	    if (mainScene != null)
	    	mainScene.pauseSimulation(false);
			//previous = mainScene.slowDownTimeRatio();//.startPause();

	    switch (site_case) {
	    	// A settler initiated the construction mission.
	    	// The site building had been pre-selected and approved by the settlement
	    	// Construction initiated by a starting member. Building picked by settlement. Site to be automatically picked.
	    
	    
	    	// Site Picked. Automatic (NOT Manual).
    		//case 0: constructionSite = executeCase0(mission, constructionManager, stageInfo, constructionSite, constructionSkill);
	    	//break;
	    
	    
	    	// Site NOT Picked. Automatic (NOT Manual).
	    	case 1: site = executeCase1(mission);
		    	break;
		    	
		    // Site Picked. Manual.
		    case 2: site = executeCase2(mission);
		    	break;
		    	
		    // Use Mission Tool to create a construction mission.
		    // Site NOT Picked. Manual.
		    case 3: site = executeCase3(mission);
		    	break;
	    }

	    if (mainScene != null)
	    	Simulation.instance().getMasterClock().setPaused(false, true);
//	    	mainScene.unpauseSimulation();
	    	//mainScene.speedUpTimeRatio(previous);//.endPause(previous);

	    settlement.fireUnitUpdate(UnitEventType.END_CONSTRUCTION_WIZARD_EVENT, site);
	}
 
	public ConstructionSite executeCase1(BuildingConstructionMission mission) {
		ConstructionSite site = mission.getConstructionSite();
		ConstructionStageInfo info = site.getStageInfo();
	    Settlement s = site.getSettlement();
	    ConstructionManager mgr = s.getConstructionManager();
	    int skill = site.getSkill();
	    
	    ConstructionValues values = mgr.getConstructionValues();
        values.clearCache();

        site = positionNewSite(site);//, skill);

        // Determine construction site location and facing.
	    //stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
        info = site.getStageInfo();

	    if (info != null) {

	        // Set construction site size.
	        if (info.getWidth() > 0D) {
	            site.setWidth(info.getWidth());
	        }
	        else {
	            // Set initial width value that may be modified later.
	            site.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);
	        }

	        if (info.getLength() > 0D) {
	            site.setLength(info.getLength());
	        }
	        else {
	            // Set initial length value that may be modified later.
	            site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
	        }

	        //modifiedSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
	        //confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);
	        confirmSiteLocation(site, mgr, true, info, skill);

	        logger.info("ConstructionWizard's executeCase1() : stageInfo is " + info.getName() );

	        logger.log(Level.INFO, "New construction site added at " + site.getSettlement().getName());
	    }
	    else {

	        confirmSiteLocation(site, mgr, true, info, skill);

	        info = site.getStageInfo();

	        if (info != null)
	        	logger.info("ConstructionWizard's executeCase1() : stageInfo is no longer null");

	        else {
		        //endMission("New construction stage could not be determined.");
	        	logger.info("ConstructionWizard's executeCase1() : new construction stageInfo could not be determined.");
	        	// TODO: determine what needs to be done right here
	        }
	    }


	    // 2015-12-28 Needed to get back to the original thread that started the BuildingConstructionMission instance
	    Simulation.instance().getMasterClock().getClockListenerExecutor().execute(new SiteTask(
				//modifiedSite, stageInfo, constructionSkill, values, mission));
	    		site, info, skill, values, mission));

	    return site;
	}

	//2015-12-28 Added SiteTask
	class SiteTask implements Runnable {

		private ConstructionSite m_site;
		private ConstructionStageInfo info;
		private int skill;
		private ConstructionValues values;
		private BuildingConstructionMission mission;

		SiteTask(ConstructionSite modifiedSite,
				ConstructionStageInfo stageInfo,
				int constructionSkill,
				ConstructionValues values,
				BuildingConstructionMission mission) {
			this.m_site = modifiedSite;
			this.info = stageInfo;
			this.skill = constructionSkill;
			this.values = values;
			this.mission = mission;

		}

		public void run() {
		   	//logger.info("ConstructionWizard's SiteTask's run() is on " + Thread.currentThread().getName() + " Thread");
			// it's now on pool-3-thread-1 Thread

		   	mission.initCase1Step2(m_site, info, skill, values);
		    mission.initCase1Step3();
		    mission.selectSitePhase();
		}
    }

	public ConstructionSite  executeCase2(BuildingConstructionMission mission) {
		ConstructionSite site = mission.getConstructionSite();
		ConstructionStageInfo info = site.getStageInfo();
	    Settlement s = site.getSettlement();
	    ConstructionManager mgr = s.getConstructionManager();
	    int skill = site.getSkill();
	    
		confirmSiteLocation(site, mgr, true, info, skill);
	    info = site.getStageInfo();

	    if (info != null) {
	    	logger.info("ConstructionWizard's executeCase2() : stageInfo is " + info.getName() );
	    }
	    else {
	    	logger.info("ConstructionWizard's executeCase2() : new construction stageInfo could not be determined.");
	    }
/*	    
	    logger.info("Participating members are : " + mission.getMembers());
	    
	    // Reserve construction vehicles.
	    mission.reserveConstructionVehicles();
	    
        mission.initialize(m_site, info); 
        
        missionManager.addMission(mission);
    	// Retrieve construction LUV attachment parts.
        mission.retrieveConstructionLUVParts();
        
        mission.startPhase();
*/
	    logger.info("NOTE : Make sure someone has a reasonably good construction skill to head this project. ");
	    logger.info("Participating members are : " + mission.getMembers());
        // Add this mission to mission manager
        missionManager.addMission(mission);
	    // Set members' mission 
        mission.setMembers();
        // Set the site and stage        
        mission.initialize(site, info);
       
        // Reserve construction (specifically LUV) vehicles.
	    mission.reserveConstructionVehicles();
        // Retrieve vehicles
        //mission.retrieveVehicles(); // NOTE: should reserve LUV only and not any rovers
	    // Retrieve construction LUV attachment parts.
        mission.retrieveConstructionLUVParts();
	    // Add and set phase
        mission.startPhase();

	    return site;
	}


	public ConstructionSite executeCase3(BuildingConstructionMission mission) {
		ConstructionSite site = mission.getConstructionSite();
		ConstructionStageInfo info = site.getStageInfo();
	    Settlement s = site.getSettlement();
	    ConstructionManager mgr = s.getConstructionManager();
	    int skill = site.getSkill();
	    
		site = positionNewSite(site);//, skill);
	    info = site.getStageInfo();
		confirmSiteLocation(site, mgr, true, info, skill);

	    if (info != null) {
	    	logger.info("ConstructionWizard's executeCase3() : stageInfo is " + info.getName() );
	    }
	    else {
	    	logger.info("ConstructionWizard's executeCase3() : new construction stageInfo could not be determined.");
	        // TODO: this will cause NullPOinterException in init_2()
	    }
        
	    logger.info("NOTE : Make sure someone has a reasonably good construction skill to head this project. ");
	    logger.info("Participating members are : " + mission.getMembers());
        // Add this mission to mission manager
        missionManager.addMission(mission);
	    // Set members' mission 
        mission.setMembers();
        // Set the site and stage        
        mission.initialize(site, info);
       
        // Reserve construction (specifically LUV) vehicles.
	    mission.reserveConstructionVehicles();
        // Retrieve vehicles
        //mission.retrieveVehicles(); // NOTE: should reserve LUV only and not any rovers
	    // Retrieve construction LUV attachment parts.
        mission.retrieveConstructionLUVParts();
	    // Add and set phase
        mission.startPhase();

	    return site;
	}

	@SuppressWarnings("restriction")
	public synchronized void confirmSiteLocation(ConstructionSite site, ConstructionManager constructionManager,
			boolean isAtPreDefinedLocation, ConstructionStageInfo stageInfo, int constructionSkill) {
		//System.out.println("ConstructionWizard : entering confirmSiteLocation()");

        // Determine location and facing for the new building.
		double xLoc = site.getXLocation();
		double yLoc = site.getYLocation();
		double scale = mapPanel.getScale();

		Settlement currentS = settlementWindow.getMapPanel().getSettlement();
		if (currentS != constructionManager.getSettlement()) {

			if (mainScene != null) {
				mainScene.setSettlement(constructionManager.getSettlement());
			}
			else {
    			//desktop.openToolWindow(SettlementWindow.NAME);
				settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox()
					.setSelectedItem(constructionManager.getSettlement());
			}
		}
  		// set up the Settlement Map Tool to display the suggested location of the building
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		//mapPanel.setShowConstructionLabels(true);
		//mapPanel.getSettlementTransparentPanel().getConstructionLabelMenuItem().setSelected(true);

		String header = null;
		String title = null;
		String message = "(1) Will default to \"Yes\" in 90 secs unless timer is cancelled."
    			+ " (2) To manually place a site, click on \"Use Mouse\" button.";
		StringProperty msg = new SimpleStringProperty(message);
		String name = null;

		if (stageInfo != null) {
			name = stageInfo.getName();
			title = "A New " + Conversion.capitalize(stageInfo.getName()).replace(" X ", " x ") + " at " + constructionManager.getSettlement();
	        // stageInfo.getType() is less descriptive than stageInfo.getName()
		}
		else {
			name = site.getName();
			title = "A New " + Conversion.capitalize(site.getName()).replace(" X ", " x ") + " at " + constructionManager.getSettlement();
		}

		if (isAtPreDefinedLocation) {
			header = "Would you like to place the " +  name + " at its default position? ";
		}
		else {
			header = "Would you like to place the " + name + " at this position? ";
		}


        if (mainScene != null) {
        	alertDialog(title, header, msg, constructionManager, site, true, stageInfo, constructionSkill);

		} else {

	        desktop.openAnnouncementWindow("Pause for Construction Wizard");
	        AnnouncementWindow aw = desktop.getAnnouncementWindow();
	        Point location = MouseInfo.getPointerInfo().getLocation();
	        double Xloc = location.getX() - aw.getWidth() * 2;
			double Yloc = location.getY() - aw.getHeight() * 2;
			aw.setLocation((int)Xloc, (int)Yloc);

			int reply = JOptionPane.showConfirmDialog(aw, header, TITLE, JOptionPane.YES_NO_OPTION);
			//repaint();

			if (reply == JOptionPane.YES_OPTION) {
	            logger.info("The construction site is set");
			}
			else {
				//constructionManager.removeConstructionSite(site);
				//site = new ConstructionSite();
				site = positionNewSite(site);//, constructionSkill);
				confirmSiteLocation(site, constructionManager, false, stageInfo, constructionSkill);
			}

			desktop.disposeAnnouncementWindow();
		}

	}


	@SuppressWarnings("restriction")
	public void alertDialog(String title, String header, StringProperty msg,
		ConstructionManager constructionManager, ConstructionSite site, boolean hasTimer,
		ConstructionStageInfo stageInfo, int constructionSkill){
		//System.out.println("ConstructionWizard : Calling alertDialog()");
		Alert alert = new Alert(AlertType.CONFIRMATION);
		//alert.setOnCloseRequest((event) -> event.consume());
		//alert.initStyle(StageStyle.UNDECORATED);
		alert.initOwner(mainScene.getStage());
		alert.initModality(Modality.NONE); // users can zoom in/out, move around the settlement map and move a vehicle elsewhere
		//alert.initModality(Modality.APPLICATION_MODAL);
		double x = mainScene.getStage().getWidth();
		double y = mainScene.getStage().getHeight();
		double xx = alert.getDialogPane().getWidth();
		double yy = alert.getDialogPane().getHeight();

		alert.setX((x - xx)/2D);
		alert.setY((y - yy)*3D/4D);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(msg.get());
		// 2015-12-19 Used JavaFX binding
		alert.getDialogPane().contentTextProperty().bind(msg);
		//alert.getDialogPane().headerTextProperty().bind(arg0);

		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");
		ButtonType buttonTypeMouseKB = new ButtonType("Use Mouse");
		ButtonType buttonTypeCancelTimer = null;

		Timer timer = null;
		if (hasTimer) {
			buttonTypeCancelTimer = new ButtonType("Cancel Timer");
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeMouseKB, buttonTypeCancelTimer);

			IntegerProperty i = new SimpleIntegerProperty(wait_time_in_secs);
			// 2015-12-19 Added ReactFX's Timer and FxTimer
			timer = FxTimer.runPeriodically(java.time.Duration.ofMillis(1000), () -> {
	        	int num = i.get() - 1;
	        	if (num >= 0) {
	        		i.set(num);
	        	}
	        	//System.out.println(num);
	        	if (num == 0) {
	        		Button button = (Button) alert.getDialogPane().lookupButton(buttonTypeYes);
	        	    button.fire();
	        	}
	        	msg.set("Notes: (1) Will default to \"Yes\" in " + num + " secs unless timer is cancelled."
	        			+ " (2) To manually place a site, use Mouse Control.");
			});
		}
		else {
			msg.set("Note: To manually place a site, use Mouse Control.");
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeMouseKB);
		}

		Optional<ButtonType> result = null;
		result = alert.showAndWait();

		if (result.isPresent() && result.get() == buttonTypeYes) {
			logger.info(site.getName() + " is put in place in " + constructionManager.getSettlement());

		} else if (result.isPresent() && result.get() == buttonTypeNo) {
			//constructionManager.removeConstructionSite(site);
	    	//System.out.println("just removing building");
			site = positionNewSite(site);//, constructionSkill);
			confirmSiteLocation(site, constructionManager, false, stageInfo, constructionSkill);

		} else if (result.isPresent() && result.get() == buttonTypeMouseKB) {
			placementDialog(title,header, site);

		} else if (hasTimer && result.isPresent() && result.get() == buttonTypeCancelTimer) {
			timer.stop();
			alertDialog(title, header, msg, constructionManager, site, false, stageInfo, constructionSkill);
		}

	}

	/**
	 * Pops up an alert dialog for confirming the position placement of a new construction site via keyboard/mouse
	 * @param title
	 * @param header
	 * @param site
	 */
	// 2015-12-25 Added mouseDialog()
	@SuppressWarnings("restriction")
	public void placementDialog(String title, String header, ConstructionSite site) {
    	// Platform.runLater(() -> {
		// FXUtilities.runAndWait(() -> {
	/*
		String msg = "Keyboard Control :\t(1) Press w/a/s/d, arrows, or num pad keys to move around" + System.lineSeparator()
				+ "\t\t\t\t(2) Press 'r' or 'f' to rotate 45 degrees clockwise" + System.lineSeparator()
				+ "   Mouse Control :\t(1) Press & Hold the left button on the site" + System.lineSeparator()
				+ "\t\t\t\t(2) Move the cursor to the destination within a short distance" + System.lineSeparator()
				+ "\t\t\t\t(3) Release button to drop it off" + System.lineSeparator()
				+ "\t\t\t\t(4) Hit \"Confirm Position\" button to proceed";
	*/		
			String msg = 
			  "\t\t(1) Press & Hold the left button on the site" + System.lineSeparator()
			+ "\t\t(2) Move the cursor to the destination within a short distance" + System.lineSeparator()
			+ "\t\t(3) Release button to drop it off" + System.lineSeparator()
			+ "\t\t(4) Hit \"Confirm Position\" button to proceed";
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setOnCloseRequest(e -> e.consume());
			//alert.initStyle(StageStyle.UNDECORATED);
			alert.initOwner(mainScene.getStage());
			alert.initModality(Modality.NONE); // users can zoom in/out, move around the settlement map and move a vehicle elsewhere
			//alert.initModality(Modality.APPLICATION_MODAL);
			double x = mainScene.getStage().getWidth();
			double y = mainScene.getStage().getHeight();
			double xx = alert.getDialogPane().getWidth();
			double yy = alert.getDialogPane().getHeight();

			alert.setX((x - xx)/2);
			alert.setY((y - yy)*3/4);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.setContentText(msg);

			ButtonType buttonTypeConfirm = new ButtonType("Confirm Site Position");
			alert.getButtonTypes().setAll(buttonTypeConfirm);

			//double xLoc = site.getXLocation();
			//double yLoc = site.getYLocation();
			//double scale = mapPanel.getScale();
			//System.out.println("xLoc : " + xLoc + "   yLoc : " + yLoc + "   scale : " + scale);
			//moveMouse(new Point((int)(xLoc*scale), (int)(yLoc*scale)));

			mainScene.getStage().requestFocus();

			//final KeyAdapter kb = new KeyAdapter(site);
			MouseDetection md = new MouseDetection(site);
			MouseMotionDetection mmd = new MouseMotionDetection(site);
			
			//SwingUtilities.invokeLater(() -> {

				mapPanel.setFocusable(true);
				mapPanel.requestFocusInWindow();
				mapPanel.addMouseMotionListener(mmd);
				mapPanel.addMouseListener(md);
/*				
				mapPanel.addKeyListener(new KeyAdapter() {
					
						public void keyPressed(java.awt.event.KeyEvent e) {
						    int c = e.getKeyCode();
						    System.out.println("Calling processKeyPress()");
						    processKeyPress(site, c);
						}

						public void keyReleased(java.awt.event.KeyEvent e) {
						    int c = e.getKeyCode();
						    System.out.println("Calling processKeyRelease()");
						    processKeyRelease(c);
						}
					}	
				);
*/	
				
/*				
				mapPanel.addMouseListener(new MouseListener() {
				    @Override
				    public void mouseClicked(MouseEvent evt) {
					//	Point location = MouseInfo.getPointerInfo().getLocation();
				    }

					@Override
					public void mouseEntered(MouseEvent arg0) {}

					@Override
					public void mouseExited(MouseEvent arg0) {}

					@Override
					public void mousePressed(MouseEvent evt) {
						if (evt.getButton() == MouseEvent.BUTTON1) {
							//mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
							xLast = evt.getX();
							yLast = evt.getY();
						}
					}

					@Override
					public void mouseReleased(MouseEvent evt) {
						if (evt.getButton() == MouseEvent.BUTTON1) {
							//mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
							moveConstructionSiteAt(site, evt.getX(), evt.getY());
						}
						mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

				});
*/				
			//});

			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeConfirm) {
				//mapPanel.emoveKeyListener();
				mapPanel.removeMouseListener(md);
				mapPanel.removeMouseMotionListener(mmd);
			}

	}

	public class MouseDetection implements MouseListener{
		private ConstructionSite site;

		MouseDetection(ConstructionSite site) {
			this.site = site;
		}
		
		@Override
	    public void mouseClicked(MouseEvent e) {
			// empty
	    }

		@Override
		public void mouseEntered(MouseEvent e) {
			// empty
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// empty
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {				
				site.setMousePicked(false);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() != MouseEvent.BUTTON1) {
				site.setMousePicked(false);
			}
		}
		
	}
	
	public class MouseMotionDetection implements MouseMotionListener{
		private ConstructionSite site;

		MouseMotionDetection(ConstructionSite site) {
			this.site = site;
		}
		
		@Override
		public synchronized void mouseDragged(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				moveSite(site, e.getX(), e.getY());
			}
			else
				site.setMousePicked(false);
			xLast = e.getX();
			yLast = e.getY();
		}

		@Override
		public synchronized void mouseMoved(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				highlightSite(site, e.getX(), e.getY());
			}
			else 
				site.setMousePicked(false);
			xLast = e.getX();
			yLast = e.getY();
		}		
	}

/*	
	public class KeyAdapter implements KeyListener{
		private ConstructionSite site;

		KeyAdapter(ConstructionSite site) {
			this.site = site;
		}

		//@Override
		public void keyPressed(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
		    System.out.println("Calling processKeyPress()");
		    processKeyPress(site, c);
		}


		//@Override
		public void keyReleased(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
		    System.out.println("Calling processKeyRelease()");
		    processKeyRelease(c);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
*/	
/*
	public boolean checkLocation(ConstructionSite site, int distance) {
		boolean goodPosition = true;
		List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
        Collections.shuffle(inhabitableBuildings);
        Iterator<Building> i = inhabitableBuildings.iterator();
        while (i.hasNext()) {
            goodPosition = positionNextToBuilding(site, i.next(), distance, false);
            if (!goodPosition) {
                return false;
            }
        }
        return goodPosition;
	}
*/
	
	public boolean isCollided(BoundedObject b0) {
/*		
		double x0 = s.getXLocation();
    	double y0 = s.getYLocation();
    	double w0 = s.getWidth();
		double l0 = s.getLength();
		double f0 = s.getFacing();
		
		b0 = new BoundedObject(x0, y0, w0, l0, f0);
*/		
		List<Building> buildings = settlementWindow.getMapPanel().getSettlement().getBuildingManager().getBuildings();
        for (Building b : buildings) {
	
			double x1 = b.getXLocation();
	    	double y1 = b.getYLocation();
	    	double w1 = b.getWidth();
			double l1 = b.getLength();
			double f1 = b.getFacing();
			BoundedObject b1 = new BoundedObject(x1, y1, w1, l1, f1);
	
	    	if (LocalAreaUtil.isTwoBoundedOjectsIntersected(b0, b1))
	    		return true;
	    	
        }
        
        return false;
	}
	
	public void highlightSite(ConstructionSite s, double xPixel, double yPixel) {
		Point.Double pixel = mapPanel.convertToSettlementLocation((int)xPixel, (int)yPixel);

		double xDiff = xPixel - xLast;
		double yDiff = yPixel - yLast;

		if (xDiff < mapPanel.getWidth() && yDiff < mapPanel.getHeight()) {

			double width = s.getWidth();
			double length = s.getLength();
			int facing = (int) s.getFacing();
			double x = s.getXLocation();
			double y = s.getYLocation();
			double xx = 0;
			double yy = 0;

			if (facing == 0) {
				xx = width/2D;
				yy = length/2D;
			}
			else if (facing == 90){
				yy = width/2D;
				xx = length/2D;
			}
			// Loading Dock Garage
			if (facing == 180) {
				xx = width/2D;
				yy = length/2D;
			}
			else if (facing == 270){
				yy = width/2D;
				xx = length/2D;
			}

			// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
			// Fortunately, they both have the same width and length
			else if (facing == 45){
				yy = width/2D;
				xx = length/2D;
			}
			else if (facing == 135){
				yy = width/2D;
				xx = length/2D;
			}

			double distanceX = Math.abs(x - pixel.getX());
			double distanceY = Math.abs(y - pixel.getY());
	
			//System.out.println("distanceX : " + distanceX + "  distanceY : " + distanceY);

			if (distanceX <= xx && distanceY <= yy) {
				System.out.println("xx and yy is within the box");
				s.setMousePicked(true);
			}		
			else
				s.setMousePicked(false);
		}
		else
			s.setMousePicked(false);
	}
	
	/**
	 * Moves the site to a new position via the mouse's left drag
	 * @param s
	 * @param xPixel
	 * @param yPixel
	 */
	public void moveSite(ConstructionSite s, double xPixel, double yPixel) {
		Point.Double pixel = mapPanel.convertToSettlementLocation((int)xPixel, (int)yPixel);

		//double dx = xPixel - xLast;
		//double dy = yPixel - yLast;

		double width = s.getWidth();
		double length = s.getLength();
		int facing = (int) s.getFacing();
		double x = s.getXLocation();
		double y = s.getYLocation();

			double xx = 0;
			double yy = 0;

			if (facing == 0) {
				xx = width/2D;
				yy = length/2D;
			}
			else if (facing == 90){
				yy = width/2D;
				xx = length/2D;
			}
			// Loading Dock Garage
			if (facing == 180) {
				xx = width/2D;
				yy = length/2D;
			}
			else if (facing == 270){
				yy = width/2D;
				xx = length/2D;
			}

			// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
			// Fortunately, they both have the same width and length
			else if (facing == 45){
				yy = width/2D;
				xx = length/2D;
			}
			else if (facing == 135){
				yy = width/2D;
				xx = length/2D;
			}

			double distanceX = Math.abs(x - pixel.getX());
			double distanceY = Math.abs(y - pixel.getY());
/*
			System.out.println("xPixel : " + xPixel
					+ "  yPixel: " + yPixel
					+ "  xLast: " + xLast
					+ "  yLast: " + yLast
					+ "  dx: " + dx
					+ "  dy: " + dy
					+ "  xx: " + xx
					+ "  yy: " + yy	
					+ "  x: " + Math.round(x*10.0)/10.0
					+ "  y: " + Math.round(y*10.0)/10.0
					+ "  distanceX: " + Math.round(distanceX*10.0)/10.0
					+ "  distanceY: " + Math.round(distanceY*10.0)/10.0
					+ "  pixel.getX(): " + Math.round(pixel.getX()*10.0)/10.0
					+ "  pixel.getY(): " + Math.round(pixel.getY()*10.0)/10.0
					);
*/
			if (distanceX <= 2 * xx && distanceY <= 2 * yy) {
				s.setMousePicked(true);
				
				//System.out.println("xx and yy is within range");
				mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

				Point.Double last = mapPanel.convertToSettlementLocation((int)xLast, (int)yLast);
				double new_x = Math.round(x + pixel.getX() - last.getX());	
				double new_y = Math.round(y + pixel.getY() - last.getY());
				
		    	double w0 = s.getWidth();
				double l0 = s.getLength();
				double f0 = s.getFacing();
				
				BoundedObject b0 = new BoundedObject(new_x, new_y, w0, l0, f0);
				
				if (!isCollided(b0)) {
					s.setYLocation(new_y);
					s.setXLocation(new_x);
				}
				else
					s.setMousePicked(false);
			}
			
			else {
				mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		//}
	}

	/**
	 * Sets the new x and y location and facing of the site
	 * @param s
	 * @param c
	 */
	public void processKeyPress(ConstructionSite s, int c) {
		int facing = (int) s.getFacing();
		double x = s.getXLocation();
		double y = s.getYLocation();

	    if (c == KeyEvent.VK_UP // 38
	    	|| c == KeyEvent.VK_KP_UP
	    	|| c == KeyEvent.VK_W
	    	|| c == KeyEvent.VK_NUMPAD8) {
	    	upKeyPressed = true;
	    } else if(c == KeyEvent.VK_DOWN // 40
	    	|| c == KeyEvent.VK_KP_DOWN
	    	|| c == KeyEvent.VK_S
	    	|| c == KeyEvent.VK_NUMPAD2) {
	    	downKeyPressed = true;
	    } else if(c == KeyEvent.VK_LEFT // 37
	    	|| c == KeyEvent.VK_KP_LEFT
	    	|| c == KeyEvent.VK_A
	    	|| c == KeyEvent.VK_NUMPAD4) {
	    	leftKeyPressed = true;
	    } else if(c == KeyEvent.VK_RIGHT // 39
	    	|| c == KeyEvent.VK_KP_RIGHT
	    	|| c == KeyEvent.VK_D
	    	|| c == KeyEvent.VK_NUMPAD6) {
	    	rightKeyPressed = true;
	    } else if(c == KeyEvent.VK_R
	    	|| c == KeyEvent.VK_F) {
	    	turnKeyPressed = true;
	    }
	    	
    	double w0 = s.getWidth();
		double l0 = s.getLength();
		double f0 = s.getFacing();
		
		BoundedObject b0 = null;
		
	    if (upKeyPressed) {
	    	b0 = new BoundedObject(s.getXLocation(), s.getYLocation() + 3, w0, l0, f0);
	    	if (!isCollided(b0))
	    		s.setYLocation(y + 1);	
	    }
	    
	    if (downKeyPressed) {
	    	b0 = new BoundedObject(s.getXLocation(), s.getYLocation() - 3, w0, l0, f0);
	    	if (!isCollided(b0)) 
	    		s.setYLocation(y - 1);
	    }
	    
	    if (leftKeyPressed) {
	    	b0 = new BoundedObject(s.getXLocation() + 3, s.getYLocation(), w0, l0, f0);
	    	if (!isCollided(b0)) 
	    		s.setXLocation(x + 1);
	    }
	    
	    if (rightKeyPressed) {
	    	b0 = new BoundedObject(s.getXLocation() - 3, s.getYLocation(), w0, l0, f0);
	    	if (!isCollided(b0)) 
	    		s.setXLocation(x - 1);
	    }
	    
    	if (turnKeyPressed) {
	    	facing = facing + 45;
	    	if (facing >= 360)
	    		facing = facing - 360;
	    	b0 = new BoundedObject(s.getXLocation(), s.getYLocation(), w0, l0, facing);
    		if (!isCollided(b0)) {    	
		    	s.setFacing(facing);
    		}
	    }
	    
	}

	/**
	 * Sets the new x and y location and facing of the site
	 * @param c
	 */
	public void processKeyRelease(int c) {

	    if (c == KeyEvent.VK_UP // 38
		    	|| c == KeyEvent.VK_KP_UP
		    	|| c == KeyEvent.VK_W
		    	|| c == KeyEvent.VK_NUMPAD8) {
		    	upKeyPressed = false;
		    } else if(c == KeyEvent.VK_DOWN // 40
		    	|| c == KeyEvent.VK_KP_DOWN
		    	|| c == KeyEvent.VK_S
		    	|| c == KeyEvent.VK_NUMPAD2) {
		    	downKeyPressed = false;
		    } else if(c == KeyEvent.VK_LEFT // 37
		    	|| c == KeyEvent.VK_KP_LEFT
		    	|| c == KeyEvent.VK_A
		    	|| c == KeyEvent.VK_NUMPAD4) {
		    	leftKeyPressed = false;
		    } else if(c == KeyEvent.VK_RIGHT // 39
		    	|| c == KeyEvent.VK_KP_RIGHT
		    	|| c == KeyEvent.VK_D
		    	|| c == KeyEvent.VK_NUMPAD6) {
		    	rightKeyPressed = false;
		    } else if(c == KeyEvent.VK_R
		    	|| c == KeyEvent.VK_F) {
		    	turnKeyPressed = false;
		    }

	}
	
/*
	// for future use
	public void moveMouse(Point p) {
	    GraphicsEnvironment ge =
	        GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gs = ge.getScreenDevices();

	    // Search the devices for the one that draws the specified point.
	    for (GraphicsDevice device: gs) {
	        GraphicsConfiguration[] configurations =
	            device.getConfigurations();
	        for (GraphicsConfiguration config: configurations) {
	            Rectangle bounds = config.getBounds();
	            if(bounds.contains(p)) {
	                // Set point to screen coordinates.
	                Point b = bounds.getLocation();
	                Point s = new Point(p.x - b.x, p.y - b.y);

	                try {
	                	java.awt.Robot r = new java.awt.Robot(device);
	                    r.mouseMove(s.x, s.y);
	                } catch (AWTException e) {
	                    e.printStackTrace();
	                }

	                return;
	            }
	        }
	    }
	    // Couldn't move to the point, it may be off screen.
	    return;
	}
*/

	   /**
     * Determines a new construction stage info for a site.
     * @param site the construction site.
     * @param skill the architect's construction skill.
     * @return construction stage info.
     * @throws Exception if error determining construction stage info.
 
    private ConstructionStageInfo determineNewStageInfo(ConstructionSite site, int skill) {
		//logger.info("ConstructionWizard's determineNewStageInfo() is in " + Thread.currentThread().getName() + " Thread");

        ConstructionStageInfo result = null;

        ConstructionValues values = site.getSettlement().getConstructionManager().getConstructionValues();
        Map<ConstructionStageInfo, Double> stageProfits =
            values.getNewConstructionStageProfits(site, skill);
        if (!stageProfits.isEmpty()) {
            result = RandomUtil.getWeightedRandomObject(stageProfits);
        }

        return result;
    }
*/
/*
    private double computeDist(String buildingType) {
    	if (buildingType.equalsIgnoreCase("inflatable greenhouse")
    			|| buildingType.equalsIgnoreCase("inground greenhouse"))
    		return 3;
    	
    	return 3;
    }
 */   
    public boolean determineSite(String buildingType, double dist, ConstructionSite site) {
    	boolean goodPosition = false;
        // Try to put building next to the same building type.
        List<Building> sameBuildings = site.getSettlement().getBuildingManager().getBuildingsOfSameType(buildingType);
        Collections.shuffle(sameBuildings);
        for (Building b : sameBuildings) {
        	logger.info("Positioning next to " + b.getNickName());
            goodPosition = positionNextToBuilding(site, b, dist, false);
            if (goodPosition) {
                break;
            }
        }
        return goodPosition;
    }
    
    /**
     * Determines and sets the position of a new construction site.
     * @param site the new construction site.
     * @param stageInfo the site's foundation stage info.
     * @param skill the mission starter's construction skill.
     * @return modified construction site
     */
    private ConstructionSite positionNewSite(ConstructionSite site) { //ConstructionStageInfo stageInfo,
            //int skill) {
        boolean goodPosition = false;
        Settlement s = site.getSettlement();

        // Try to put building next to another inhabitable building.
        List<Building> inhabitableBuildings = s.getBuildingManager().getBuildings();//FunctionType.LIFE_SUPPORT);
        Collections.shuffle(inhabitableBuildings);
        for (Building b : inhabitableBuildings) {
        	// Match the floor area (e.g look more organize to put all 7m x 9m next to one another)
            if (b.getFloorArea() == site.getWidth()*site.getLength()) {
                goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
                if (goodPosition) {
                    break;
                }
            }
        }
/*        
        // Use settlement's objective to determine the desired building type
        String buildingType = s.getObjectiveBuildingType();

        if (buildingType != null) {
            site.setWidth(buildingConfig.getWidth(buildingType));
            site.setLength(buildingConfig.getLength(buildingType));
            boolean isBuildingConnector = buildingConfig.hasBuildingConnection(buildingType);
            boolean hasLifeSupport = buildingConfig.hasLifeSupport(buildingType);

            if (isBuildingConnector) {
            	//logger.info("isBuildingConnector : " + isBuildingConnector);
                // Try to find best location to connect two buildings.
                goodPosition = positionNewBuildingConnectorSite(site, buildingType);
            }
            else if (hasLifeSupport) {
            	logger.info("hasLifeSupport : " + hasLifeSupport);
            	if (buildingType.equalsIgnoreCase("inflatable greenhouse")) {
            		goodPosition = determineSite(buildingType, DEFAULT_SMALL_GREENHOUSE_DISTANCE, site);
            	}
            	
            	else if (buildingType.equalsIgnoreCase("inground greenhouse")) {
            		goodPosition = determineSite(buildingType, DEFAULT_SMALL_GREENHOUSE_DISTANCE, site);
            	}
            	
            	else if (buildingType.equalsIgnoreCase("large greenhouse")) {
            		goodPosition = determineSite(buildingType, DEFAULT_LARGE_GREENHOUSE_DISTANCE, site);
            	}
            	
            	else {
                	logger.info("trying to match floor area.");
	                // Try to put building next to another inhabitable building.
	                List<Building> inhabitableBuildings = s.getBuildingManager().getBuildings(FunctionType.LIFE_SUPPORT);
	                Collections.shuffle(inhabitableBuildings);
	                for (Building b : inhabitableBuildings) {
	                	// Match the floor area (e.g look more organize to put all 7m x 9m next to one another)
		                if (b.getFloorArea() == site.getWidth()*site.getLength()) {
		                	logger.info("Positioning next to " + b.getNickName());
		                    goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
		                    if (goodPosition) {
		                        break;
		                    }
		                }
	                }
                }
            }
            
            else {
            	logger.info("no Life Support");
                // Try to put building next to the same building type.
            	goodPosition = determineSite(buildingType, DEFAULT_NONINHABITABLE_BUILDING_DISTANCE, site);
            }
        }
        
        else {
        	logger.info("buildingType : " + buildingType);
            // Try to put building next to another inhabitable building.
            List<Building> inhabitableBuildings = s.getBuildingManager().getBuildings();//FunctionType.LIFE_SUPPORT);
            Collections.shuffle(inhabitableBuildings);
            for (Building b : inhabitableBuildings) {
            	// Match the floor area (e.g look more organize to put all 7m x 9m next to one another)
                if (b.getFloorArea() == site.getWidth()*site.getLength()) {
                    goodPosition = positionNextToBuilding(site, b, DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
                    if (goodPosition) {
                        break;
                    }
                }
            }
        }
*/        
        if (!goodPosition) {
        	logger.info("goodPosition : " + goodPosition);
            // Try to put building next to another building.
            // If not successful, try again 10m from each building and continue out at 10m increments
            // until a location is found.
            BuildingManager buildingManager = site.getSettlement().getBuildingManager();
            if (buildingManager.getNumBuildings() > 0) {
                for (int x = 10; !goodPosition; x+= 10) {
                    List<Building> allBuildings = buildingManager.getACopyOfBuildings();
                    Collections.shuffle(allBuildings);
                    Iterator<Building> i = allBuildings.iterator();
                    while (i.hasNext()) {
                        goodPosition = positionNextToBuilding(site, i.next(), (double) x, false);
                        if (goodPosition) {
                            break;
                        }
                    }
                }
            }
            else {
            	
                // If no buildings at settlement, position new construction site at 0,0 with random facing.
                site.setXLocation(0); 
                site.setYLocation(12D); // arbitrarily move to below the Lander Hab 1 on the map
                site.setFacing(RandomUtil.getRandomDouble(360D));
            }
        }


        return site;
    }


    /**
     * Determines the preferred construction building type for a given foundation.
     * @param info the foundation stage info.
     * @param skill the mission starter's construction skill.
     * @return preferred building type or null if none found.
     
    private String determinePrefType(ConstructionSite site, ConstructionStageInfo info, int skill) {
		//logger.info("ConstructionWizard's determinePreferredConstructedBuildingType() is in " + Thread.currentThread().getName() + " Thread");

        String result = null;

        ConstructionValues values = site.getConstructionManager().getConstructionValues();
        List<String> constructableBuildings = ConstructionUtil.getConstructableBuildingNames(info);
        Iterator<String> i = constructableBuildings.iterator();
        double maxBuildingValue = Double.NEGATIVE_INFINITY;
        while (i.hasNext()) {
            String buildingType = i.next();
            double buildingValue = values.getConstructionStageValue(info, skill);
            if (buildingValue > maxBuildingValue) {
                maxBuildingValue = buildingValue;
                result = buildingType;
            }
        }

        return result;
    }
*/

    /**
     * Determine the position and length (for variable length sites) for a new building
     * connector construction site.
     * @param site the construction site.
     * @param buildingType the new building type.
     * @return true if position/length of construction site could be found, false if not.
     */
    private boolean positionNewBuildingConnectorSite(ConstructionSite site, String buildingType) {

        boolean result = false;

        BuildingManager manager = site.getSettlement().getBuildingManager();
        List<Building> inhabitableBuildings = manager.getBuildings(FunctionType.LIFE_SUPPORT);
        Collections.shuffle(inhabitableBuildings);

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        int baseLevel = buildingConfig.getBaseLevel(buildingType);

        // Try to find a connection between an inhabitable building without access to airlock and
        // another inhabitable building with access to an airlock.
        if (site.getSettlement().getAirlockNum() > 0) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> i = inhabitableBuildings.iterator();
            while (i.hasNext()) {
                Building startingBuilding = i.next();
                if (!site.getSettlement().hasWalkableAvailableAirlock(startingBuilding)) {

                    // Find a different inhabitable building that has walkable access to an airlock.
                    Iterator<Building> k = inhabitableBuildings.iterator();
                    while (k.hasNext()) {
                        Building building = k.next();
                        if (!building.equals(startingBuilding)) {

                            // Check if connector base level matches either building.
                            boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                                    (baseLevel == building.getBaseLevel());

                            if (site.getSettlement().hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
                                double distance = Point2D.distance(startingBuilding.getXLocation(),
                                        startingBuilding.getYLocation(), building.getXLocation(),
                                        building.getYLocation());
                                if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                                    // Check that new building can be placed between the two buildings.
                                    if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                            building)) {
                                        leastDistance = distance;
                                        result = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Try to find valid connection location between two inhabitable buildings with no joining walking path.
        if (!result) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> j = inhabitableBuildings.iterator();
            while (j.hasNext()) {
                Building startingBuilding = j.next();

                // Find a different inhabitable building.
                Iterator<Building> k = inhabitableBuildings.iterator();
                while (k.hasNext()) {
                    Building building = k.next();
                    boolean hasWalkingPath = site.getSettlement().getBuildingConnectorManager().hasValidPath(
                            startingBuilding, building);

                    // Check if connector base level matches either building.
                    boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                            (baseLevel == building.getBaseLevel());

                    if (!building.equals(startingBuilding) && !hasWalkingPath && matchingBaseLevel) {

                        double distance = Point2D.distance(startingBuilding.getXLocation(),
                                startingBuilding.getYLocation(), building.getXLocation(),
                                building.getYLocation());
                        if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                            // Check that new building can be placed between the two buildings.
                            if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                    building)) {
                                leastDistance = distance;
                                result = true;
                            }
                        }
                    }
                }
            }
        }

        // Try to find valid connection location between two inhabitable buildings that are not directly connected.
        if (!result) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> j = inhabitableBuildings.iterator();
            while (j.hasNext()) {
                Building startingBuilding = j.next();

                // Find a different inhabitable building.
                Iterator<Building> k = inhabitableBuildings.iterator();
                while (k.hasNext()) {
                    Building building = k.next();
                    boolean directlyConnected = (site.getSettlement().getBuildingConnectorManager().getBuildingConnections(
                            startingBuilding, building).size() > 0);

                    // Check if connector base level matches either building.
                    boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                            (baseLevel == building.getBaseLevel());

                    if (!building.equals(startingBuilding) && !directlyConnected && matchingBaseLevel) {
                        double distance = Point2D.distance(startingBuilding.getXLocation(),
                                startingBuilding.getYLocation(), building.getXLocation(),
                                building.getYLocation());
                        if ((distance < leastDistance) && (distance >= MINIMUM_CONNECTOR_LENGTH)) {

                            // Check that new building can be placed between the two buildings.
                            if (positionConnectorBetweenTwoBuildings(buildingType, site, startingBuilding,
                                    building)) {
                                leastDistance = distance;
                                result = true;
                            }
                        }
                    }
                }
            }
        }

        // Try to find connection to existing inhabitable building.
        if (!result) {

            // If variable length, set construction site length to default.
            if (buildingConfig.getLength(buildingType) == -1D) {
                site.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
            }

            Iterator<Building> l = inhabitableBuildings.iterator();
            while (l.hasNext()) {
                Building building = l.next();
                // Make connector building face away from building.
                result = positionNextToBuilding(site, building, 0D, true);

                if (result) {
                    break;
                }
            }
        }

        return result;
    }


    /**
     * Positions a new construction site near an existing building.
     * @param site the new construction site.
     * @param building the existing building.
     * @param separationDistance the separation distance (meters) from the building.
     * @param faceAway true if new building should face away from other building.
     * @return true if construction site could be positioned, false if not.
     */
    private boolean positionNextToBuilding(ConstructionSite site, Building building,
            double separationDistance, boolean faceAway) {

        boolean goodPosition = false;

        final int front = 0;
        final int back = 1;
        final int right = 2;
        final int left = 3;

        List<Integer> directions = new ArrayList<Integer>(4);
        directions.add(front);
        directions.add(back);
        directions.add(right);
        directions.add(left);
        Collections.shuffle(directions);

        double direction = 0D;
        double structureDistance = 0D;
        double rectRotation = building.getFacing();

        for (int x = 0; x < directions.size(); x++) {
            switch (directions.get(x)) {
                case front: direction = building.getFacing();
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            break;
                case back: direction = building.getFacing() + 180D;
                            structureDistance = (building.getLength() / 2D) + (site.getLength() / 2D);
                            if (faceAway) {
                                rectRotation = building.getFacing() + 180D;
                            }
                            break;
                case right:  direction = building.getFacing() + 90D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
                            if (faceAway) {
                                structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
                                rectRotation = building.getFacing() + 90D;
                            }
                            break;
                case left:  direction = building.getFacing() + 270D;
                            structureDistance = (building.getWidth() / 2D) + (site.getWidth() / 2D);
                            if (faceAway) {
                                structureDistance = (building.getWidth() / 2D) + (site.getLength() / 2D);
                                rectRotation = building.getFacing() + 270D;
                            }
            }

            if (rectRotation > 360D) {
                rectRotation -= 360D;
            }

            double distance = structureDistance + separationDistance;
            double radianDirection = Math.PI * direction / 180D;
            double rectCenterX = building.getXLocation() - (distance * Math.sin(radianDirection));
            double rectCenterY = building.getYLocation() + (distance * Math.cos(radianDirection));

            BuildingManager mgr = site.getSettlement().getBuildingManager();
            // Check to see if proposed new site position intersects with any existing buildings
            // or construction sites.
            if (mgr.isBuildingLocationOpen(rectCenterX,
                    rectCenterY, site.getWidth(), site.getLength(), rectRotation, site)) {
                // Set the new site here.
                site.setXLocation(rectCenterX);
                site.setYLocation(rectCenterY);
                site.setFacing(rectRotation);
                goodPosition = true;
                break;
            }
        }

        return goodPosition;
    }


    /**
     * Determine the position and length (for variable length) for a connector building between two existing
     * buildings.
     * @param buildingType the new connector building type.
     * @param site the construction site.
     * @param firstBuilding the first of the two existing buildings.
     * @param secondBuilding the second of the two existing buildings.
     * @return true if position/length of construction site could be found, false if not.
     */
    private boolean positionConnectorBetweenTwoBuildings(String buildingType, ConstructionSite site,
            Building firstBuilding, Building secondBuilding) {

        boolean result = false;

        // Determine valid placement lines for connector building.
        List<Line2D> validLines = new ArrayList<Line2D>();

        // Check each building side for the two buildings for a valid line unblocked by obstacles.
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(buildingType);
        List<Point2D> firstBuildingPositions = getFourPositionsSurroundingBuilding(firstBuilding, .1D);
        List<Point2D> secondBuildingPositions = getFourPositionsSurroundingBuilding(secondBuilding, .1D);
        for (int x = 0; x < firstBuildingPositions.size(); x++) {
            for (int y = 0; y < secondBuildingPositions.size(); y++) {

                Point2D firstBuildingPos = firstBuildingPositions.get(x);
                Point2D secondBuildingPos = secondBuildingPositions.get(y);

                double distance = Point2D.distance(firstBuildingPos.getX(), firstBuildingPos.getY(),
                        secondBuildingPos.getX(), secondBuildingPos.getY());

                if (distance >= MINIMUM_CONNECTOR_LENGTH) {
                    // Check line rect between positions for obstacle collision.
                    Line2D line = new Line2D.Double(firstBuildingPos.getX(), firstBuildingPos.getY(),
                            secondBuildingPos.getX(), secondBuildingPos.getY());
                    boolean clearPath = LocalAreaUtil.checkLinePathCollision(line, site.getSettlement().getCoordinates(), false);
                    if (clearPath) {
                        validLines.add(new Line2D.Double(firstBuildingPos, secondBuildingPos));
                    }
                }
            }
        }

        if (validLines.size() > 0) {

            // Find shortest valid line.
            double shortestLineLength = Double.MAX_VALUE;
            Line2D shortestLine = null;
            Iterator<Line2D> i = validLines.iterator();
            while (i.hasNext()) {
                Line2D line = i.next();
                double length = Point2D.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
                if (length < shortestLineLength) {
                    shortestLine = line;
                    shortestLineLength = length;
                }
            }

            // Create building template with position, facing, width and length for the connector building.
            double shortestLineFacingDegrees = LocalAreaUtil.getDirection(shortestLine.getP1(), shortestLine.getP2());
            Point2D p1 = adjustConnectorEndPoint(shortestLine.getP1(), shortestLineFacingDegrees, firstBuilding, width);
            Point2D p2 = adjustConnectorEndPoint(shortestLine.getP2(), shortestLineFacingDegrees, secondBuilding, width);
            double centerX = (p1.getX() + p2.getX()) / 2D;
            double centerY = (p1.getY() + p2.getY()) / 2D;
            double newLength = p1.distance(p2);
            double facingDegrees = LocalAreaUtil.getDirection(p1, p2);

            site.setXLocation(centerX);
            site.setYLocation(centerY);
            site.setFacing(facingDegrees);
            site.setLength(newLength);
            result = true;
        }

        return result;
    }

    /**
     * Adjust the connector end point based on relative angle of the connection.
     * @param point the initial connector location.
     * @param lineFacing the facing of the connector line (degrees).
     * @param building the existing building being connected to.
     * @param connectorWidth the width of the new connector.
     * @return point adjusted location for connector end point.
     */
    private Point2D adjustConnectorEndPoint(Point2D point, double lineFacing, Building building, double connectorWidth) {

        double lineFacingRad = Math.toRadians(lineFacing);
        double angleFromBuildingCenterDegrees = LocalAreaUtil.getDirection(new Point2D.Double(building.getXLocation(),
                building.getYLocation()), point);
        double angleFromBuildingCenterRad = Math.toRadians(angleFromBuildingCenterDegrees);
        double offsetAngle = angleFromBuildingCenterRad - lineFacingRad;
        double offsetDistance = Math.abs(Math.sin(offsetAngle)) * (connectorWidth / 2D);

        double newXLoc = (-1D * Math.sin(angleFromBuildingCenterRad) * offsetDistance) + point.getX();
        double newYLoc = (Math.cos(angleFromBuildingCenterRad) * offsetDistance) + point.getY();

        return new Point2D.Double(newXLoc, newYLoc);
    }


    /**
     * Gets four positions surrounding a building with a given distance from its edge.
     * @param building the building.
     * @param distanceFromSide distance (distance) for positions from the edge of the building.
     * @return list of four positions.
     */
    private List<Point2D> getFourPositionsSurroundingBuilding(Building building, double distanceFromSide) {

        List<Point2D> result = new ArrayList<Point2D>(4);

        final int front = 0;
        final int back = 1;
        final int right = 2;
        final int left = 3;

        for (int x = 0; x < 4; x++) {
            double xPos = 0D;
            double yPos = 0D;

            switch(x) {
                case front: xPos = 0D;
                             yPos = (building.getLength() / 2D) + distanceFromSide;
                             break;
                case back:  xPos = 0D;
                             yPos = 0D - (building.getLength() / 2D) - distanceFromSide;
                             break;
                case right: xPos = 0D - (building.getWidth() / 2D) - distanceFromSide;
                             yPos = 0D;
                             break;
                case left:  xPos = (building.getWidth() / 2D) + distanceFromSide;
                             yPos = 0D;
                             break;
            }

            Point2D position = LocalAreaUtil.getLocalRelativeLocation(xPos, yPos, building);
            result.add(position);
        }

        return result;
    }

    public void moveVehicle(BuildingTemplate template){
       	// Find the pre-defined location of the building
    	//Building newBuilding = new Building(template, settlement.getBuildingManager());
		//double xLoc = newBuilding.getXLocation();
		//double yLoc = newBuilding.getYLocation();
		//double scale = mapPanel.getScale();
		boolean isVehicleBlocking = true;
		// Check if the obstacle is a vehicle, if it is, move the vehicle.

		do {
			//Unit unit = mapPanel.selectVehicleAt((int)(xLoc*scale), (int)(yLoc*scale));
			Unit unit = mapPanel.selectVehicleAsObstacle(template.getXLoc(), template.getYLoc());
			if (unit == null) {
				isVehicleBlocking = false;
				//System.out.println("TranportWizard : unit is null");
			}
			else if (unit != null) {
				//System.out.println("TranportWizard : unit is NOT null");
				if (unit instanceof Vehicle) {
					//Vehicle vehicle = mapPanel.selectVehicleAt(0, 0);
					Vehicle vehicle = (Vehicle) unit;
					//System.out.println("TranportWizard : calling vehicle.determinedSettlementParkedLocationAndFacing() ");
					vehicle.determinedSettlementParkedLocationAndFacing();
				}
				else
					isVehicleBlocking = false;
			}
		} while (isVehicleBlocking);

    }
	/**
	 * Maps a number to an alphabet
	 * @param a number
	 * @return a String
	 */
	private String getCharForNumber(int i) {
		// NOTE: i must be > 1, if i = 0, return null
	    return i > 0 && i < 27 ? String.valueOf((char)(i + 'A' - 1)) : null;
	}

	//public Settlement getSettlement() {
	//	return settlement;
	//}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		//constructionStage = null;
		mainScene = null;
		desktop = null;
		//settlement = null;
		settlementWindow = null;
		mapPanel = null;

	}

}