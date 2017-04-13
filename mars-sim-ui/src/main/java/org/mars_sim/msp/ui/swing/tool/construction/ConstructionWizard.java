/**
 * Mars Simulation Project
 * ConstructionWizard.java
 * @version 3.1.0 2017-04-13
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.construction;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.FXUtilities;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.awt.AWTException;
import java.awt.Cursor;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConstructionWizard class is a class for hosting construction event manually.
 *
 */
public class ConstructionWizard {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(ConstructionWizard.class.getName());

	/** Time (millisols) required to prepare construction site for stage. */
	private static final double SITE_PREPARE_TIME = BuildingConstructionMission.SITE_PREPARE_TIME;

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = BuildingConstructionMission.DEFAULT_VARIABLE_BUILDING_WIDTH;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = BuildingConstructionMission.DEFAULT_VARIABLE_BUILDING_LENGTH;

	// Default distance between buildings for construction.
	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = BuildingConstructionMission.DEFAULT_INHABITABLE_BUILDING_DISTANCE;
	private static final double DEFAULT_NONINHABITABLE_BUILDING_DISTANCE = BuildingConstructionMission.DEFAULT_NONINHABITABLE_BUILDING_DISTANCE;

    /** Minimum length of a building connector (meters). */
    private static final double MINIMUM_CONNECTOR_LENGTH = BuildingConstructionMission.MINIMUM_CONNECTOR_LENGTH;

    private final static String TITLE = "Construction Wizard";

    private static int wait_time_in_secs = 30; // in seconds

    private double xLast, yLast;

	//private ConstructionStage constructionStage;

	private MainDesktopPane desktop;
	//private Settlement settlement;
	private SettlementWindow settlementWindow;
	private SettlementMapPanel mapPanel;
	private MainScene mainScene;
	private MarsClock sitePreparationStartTime;
	private BuildingConfig buildingConfig;


	/**
	 * Constructor 1.
	 * For non-javaFX UI
	 * @param desktop the main desktop pane.
	 */
	public ConstructionWizard(final MainDesktopPane desktop) {
		this.desktop = desktop;
		this.mainScene = mainScene;
		this.settlementWindow = desktop.getSettlementWindow();
		this.mapPanel = settlementWindow.getMapPanel();
		this.buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	/**
	 * Constructor 2.
	 * For JavaFX UI
	 * @param mainScene the main scene
	 */
	public ConstructionWizard(final MainScene mainScene, MainDesktopPane desktop) {
		this.desktop = desktop;
		this.mainScene = mainScene;
		//this.settlementWindow = desktop.getSettlementWindow();
		//this.mapPanel = settlementWindow.getMapPanel();
		this.buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	public synchronized void selectSite(BuildingConstructionMission mission) {
		//logger.info("ConstructionWizard's selectSite() is in " + Thread.currentThread().getName() + " Thread");

		if (settlementWindow == null)
    		settlementWindow = desktop.getSettlementWindow();
    	if (mapPanel == null)
    		mapPanel = settlementWindow.getMapPanel();

		ConstructionSite constructionSite = mission.getConstructionSite();
	    Settlement settlement = constructionSite.getSettlement();
	    ConstructionManager constructionManager = settlement.getConstructionManager();


		if (mainScene != null) {
			mainScene.setSettlement(constructionManager.getSettlement());
		}
		else {
			// Select the relevant settlement
			desktop.openToolWindow(SettlementWindow.NAME);
			settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(constructionManager.getSettlement());
		}

	    ConstructionStageInfo stageInfo = constructionSite.getStageInfo();
	    int constructionSkill = constructionSite.getSkill();
		boolean isSitePicked = mission.getConstructionSite().getSitePicked();
	    boolean manual = mission.getConstructionSite().getManual();

	    int site_case = 1;
	    if (manual) {
	    	site_case = 2;
	    	if (!isSitePicked)
	    		site_case = 3;
	    }

	    boolean previous = true;

	    if (mainScene != null)
			previous = mainScene.startPause();

	    switch (site_case) {
	    	// A settler initiated the construction mission.
	    	// The site building had been pre-selected and approved by the settlement
	    	// Construction initiated by a starting member. Building picked by settlement. Site to be automatically picked.
	    	// Site NOT picked. NOT manual.
	    	case 1: constructionSite = executeCase1(mission, constructionManager, stageInfo, constructionSite, constructionSkill);
		    	break;
		    // Site picked. Manual.
		    case 2: constructionSite = executeCase2(mission, constructionManager, stageInfo, constructionSite, constructionSkill);
		    	break;
		    // Use Mission Tool to create a construction mission.
		    // Site NOT picked. Manual.
		    case 3: constructionSite = executeCase3(mission, constructionManager, stageInfo, constructionSite, constructionSkill);
		    	break;
	    }

	    if (mainScene != null)
	    	mainScene.endPause(previous);

	    settlement.fireUnitUpdate(UnitEventType.END_CONSTRUCTION_WIZARD_EVENT, constructionSite);
	}

	public ConstructionSite executeCase1(BuildingConstructionMission mission, ConstructionManager constructionManager, ConstructionStageInfo stageInfo,
			ConstructionSite constructionSite, int constructionSkill) {
	    ConstructionValues values = constructionManager.getConstructionValues();
        values.clearCache();

        constructionSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);

        // Determine construction site location and facing.
	    //stageInfo = determineNewStageInfo(constructionSite, constructionSkill);
        stageInfo = constructionSite.getStageInfo();

	    if (stageInfo != null) {

	        // Set construction site size.
	        if (stageInfo.getWidth() > 0D) {
	            constructionSite.setWidth(stageInfo.getWidth());
	        }
	        else {
	            // Set initial width value that may be modified later.
	            constructionSite.setWidth(DEFAULT_VARIABLE_BUILDING_WIDTH);
	        }

	        if (stageInfo.getLength() > 0D) {
	            constructionSite.setLength(stageInfo.getLength());
	        }
	        else {
	            // Set initial length value that may be modified later.
	            constructionSite.setLength(DEFAULT_VARIABLE_BUILDING_LENGTH);
	        }

	        //modifiedSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
	        //confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);
	        confirmSiteLocation(constructionSite, constructionManager, true, stageInfo, constructionSkill);

		    System.out.println("ConstructionWizard's executeCase1() : stageInfo is " + stageInfo.getName() );

	        logger.log(Level.INFO, "New construction site added at " + constructionSite.getSettlement().getName());
	    }
	    else {

	        confirmSiteLocation(constructionSite, constructionManager, true, stageInfo, constructionSkill);

	        stageInfo = constructionSite.getStageInfo();

	        if (stageInfo != null)
	        	System.out.println("ConstructionWizard's executeCase1() : stageInfo is no longer null");

	        else {
		        //endMission("New construction stage could not be determined.");
		        System.out.println("ConstructionWizard's executeCase1() : new construction stageInfo could not be determined.");
	        	// TODO: determine what needs to be done right here
	        }
	    }


	    // 2015-12-28 Needed to get back to the original thread that started the BuildingConstructionMission instance
	    Simulation.instance().getMasterClock().getClockListenerExecutor().execute(new SiteTask(
				//modifiedSite, stageInfo, constructionSkill, values, mission));
	    		constructionSite, stageInfo, constructionSkill, values, mission));

	    return constructionSite;
	}

	//2015-12-28 Added SiteTask
	class SiteTask implements Runnable {

		ConstructionSite modifiedSite;
		ConstructionStageInfo stageInfo;
		int constructionSkill;
		ConstructionValues values;
		BuildingConstructionMission mission;

		SiteTask(ConstructionSite modifiedSite,
				ConstructionStageInfo stageInfo,
				int constructionSkill,
				ConstructionValues values,
				BuildingConstructionMission mission) {
			this.modifiedSite = modifiedSite;
			this.stageInfo = stageInfo;
			this.constructionSkill = constructionSkill;
			this.values = values;
			this.mission = mission;

		}

		public void run() {
		   	//logger.info("ConstructionWizard's SiteTask's run() is on " + Thread.currentThread().getName() + " Thread");
			// it's now on pool-3-thread-1 Thread

		   	mission.init_case_1_step_2(modifiedSite, stageInfo, constructionSkill, values);
		    mission.init_case_1_step_3();
		    mission.selectSitePhase();
		}
    }

	public ConstructionSite  executeCase2(BuildingConstructionMission mission, ConstructionManager constructionManager, ConstructionStageInfo stageInfo,
			ConstructionSite constructionSite, int constructionSkill) {

	    //System.out.println("ConstructionWizard : Case 2. stageInfo is " + stageInfo.getName() );

		ConstructionSite modifiedSite = constructionSite;
		confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);

	    stageInfo = modifiedSite.getStageInfo();

	    if (stageInfo != null) {
	    	System.out.println("ConstructionWizard's executeCase2() : stageInfo is " + stageInfo.getName() );
	    }
	    else {
	        System.out.println("ConstructionWizard's executeCase2() : new construction stageInfo could not be determined.");
	    }

        mission.init_2(modifiedSite, stageInfo);
	    // Reserve construction vehicles.
	    mission.reserveConstructionVehicles();
    	// Retrieve construction LUV attachment parts.
        mission.retrieveConstructionLUVParts();
        mission.useTwoPhases();


	    return constructionSite;
	}


	public ConstructionSite executeCase3(BuildingConstructionMission mission, ConstructionManager constructionManager, ConstructionStageInfo stageInfo,
			ConstructionSite constructionSite, int constructionSkill) {
	    //System.out.println("ConstructionWizard : Case 3. stageInfo is " + stageInfo.getName() );

		ConstructionSite modifiedSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
		confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);

	    stageInfo = modifiedSite.getStageInfo();

	    if (stageInfo != null) {
	    	System.out.println("ConstructionWizard's executeCase3() : stageInfo is " + stageInfo.getName() );
	    }
	    else {
	        System.out.println("ConstructionWizard's executeCase3() : new construction stageInfo could not be determined.");
	        // TODO: this will cause NullPOinterException in init_2()
	    }

        mission.init_2(modifiedSite, stageInfo);
	    // Reserve construction vehicles.
	    mission.reserveConstructionVehicles();
    	// Retrieve construction LUV attachment parts.
        mission.retrieveConstructionLUVParts();
        mission.useTwoPhases();

	    return constructionSite;
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
				settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(constructionManager.getSettlement());
			}
		}
  		// set up the Settlement Map Tool to display the suggested location of the building
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		//mapPanel.setShowConstructionLabels(true);
		//mapPanel.getSettlementTransparentPanel().getConstructionLabelMenuItem().setSelected(true);

		String header = null;
		String title = null;
		String message = "(1) Will default to \"Yes\" in 30 secs unless timer is cancelled."
    			+ " (2) To manually place a site, click on \"Use Mouse/Keyboard Control\" button ";
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
				site = positionNewConstructionSite(site, stageInfo, constructionSkill);
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
		ButtonType buttonTypeMouseKB = new ButtonType("Use Mouse/Keyboard Control");
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
	        			+ " (2) To manually place a site, use Mouse/Keyboard Control.");
			});
		}
		else {
			msg.set("Note: To manually place a site, use Mouse/Keyboard Control.");
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeMouseKB);
		}

		Optional<ButtonType> result = null;
		result = alert.showAndWait();

		if (result.isPresent() && result.get() == buttonTypeYes) {
			logger.info(site.getName() + " is put in place in " + constructionManager.getSettlement());

		} else if (result.isPresent() && result.get() == buttonTypeNo) {
			//constructionManager.removeConstructionSite(site);
	    	//System.out.println("just removing building");
			site = positionNewConstructionSite(site, stageInfo, constructionSkill);
			confirmSiteLocation(site, constructionManager,false, stageInfo, constructionSkill);

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
			String msg = "Keyboard Control :\t(1) Press w/a/s/d, arrows, or num pad keys to move around" + System.lineSeparator()
				+ "\t\t\t\t(2) Press 'r' or 'f' to rotate 45 degrees clockwise" + System.lineSeparator()
				+ "   Mouse Control :\t(1) Press & Hold the left button on the site" + System.lineSeparator()
				+ "\t\t\t\t(2) Move the cursor to the destination" + System.lineSeparator()
				+ "\t\t\t\t(3) Release button to drop it off" + System.lineSeparator()
				+ "\t\t\t\t(4) Hit \"Confirm Position\" button to proceed";

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

			alert.setX((x - xx)/2);
			alert.setY((y - yy)*3/4);
			alert.setTitle(title);
			alert.setHeaderText(header);
			alert.setContentText(msg);

			ButtonType buttonTypeConfirm = new ButtonType("Confirm Position");
			alert.getButtonTypes().setAll(buttonTypeConfirm);

			//double xLoc = site.getXLocation();
			//double yLoc = site.getYLocation();
			//double scale = mapPanel.getScale();
			//System.out.println("xLoc : " + xLoc + "   yLoc : " + yLoc + "   scale : " + scale);
			//moveMouse(new Point((int)(xLoc*scale), (int)(yLoc*scale)));

			mainScene.getStage().requestFocus();

			final KeyboardDetection kb = new KeyboardDetection(site);
			final MouseDetection md = new MouseDetection(site);

			SwingUtilities.invokeLater(() -> {

				mapPanel.setFocusable(true);
				mapPanel.requestFocusInWindow();

				mapPanel.addKeyListener(kb);

				mapPanel.addMouseMotionListener(md);

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
			});


			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeConfirm) {
				mapPanel.removeKeyListener(kb);
				mapPanel.removeMouseMotionListener(md);
			}

	}

	// 2015-12-25 Added MouseDetection
	public class MouseDetection implements MouseMotionListener{
		private ConstructionSite site;

		MouseDetection(ConstructionSite site) {
			this.site = site;
		}

		@Override
		public void mouseDragged(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				moveConstructionSiteAt(site, evt.getX(), evt.getY());
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

	}

	// 2015-12-25 Added KeyboardDetection
	public class KeyboardDetection implements KeyListener{
		private ConstructionSite site;

		KeyboardDetection(ConstructionSite site) {
			this.site = site;
		}

		@Override
		public void keyPressed(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
		    //System.out.println("c is " + c);
		    moveConstructionSite(site, c);
		    mapPanel.repaint();
			e.consume();
		}

		@Override
		public void keyTyped(java.awt.event.KeyEvent e) {
			e.consume();
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent e) {
			e.consume();
		}
	}
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
	/**
	 * Moves the site to a new position via the mouse's right drag
	 * @param s
	 * @param xPixel
	 * @param yPixel
	 */
	// 2015-12-25 Added moveConstructionSiteAt()
	public void moveConstructionSiteAt(ConstructionSite s, double xPixel, double yPixel) {
		Point.Double pixel = mapPanel.convertToSettlementLocation((int)xPixel, (int)yPixel);

		double xDiff = xPixel - xLast;
		double yDiff = yPixel - yLast;

		if (xDiff < mapPanel.getWidth() && yDiff < mapPanel.getHeight()) {
			//System.out.println("xPixel : " + xPixel
			//		+ "  yPixel : " + yPixel
			//		+ "  xLast : " + xLast
			//		+ "  yLast : " + yLast
			//		+ "  xDiff : " + xDiff
			//		+ "  yDiff : " + yDiff);

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
				Point.Double last = mapPanel.convertToSettlementLocation((int)xLast, (int)yLast);
				double new_x = Math.round(x + pixel.getX() - last.getX());
				s.setXLocation(new_x);
				double new_y = Math.round(y + pixel.getY() - last.getY());
				s.setYLocation(new_y);

				xLast = xPixel;
				yLast = yPixel;
			}

		}
	}

	/**
	 * Sets the new x and y location and facing of the site
	 * @param s
	 * @param c
	 */
	// 2015-12-25 Added moveConstructionSite()
	public void moveConstructionSite(ConstructionSite s, int c) {
		int facing = (int) s.getFacing();
		double x = s.getXLocation();
		double y = s.getYLocation();

	    if (c == java.awt.event.KeyEvent.VK_UP // 38
	    	|| c == java.awt.event.KeyEvent.VK_KP_UP
	    	|| c == java.awt.event.KeyEvent.VK_W
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD8) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("up");
			s.setYLocation(y + 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_DOWN // 40
	    	|| c == java.awt.event.KeyEvent.VK_KP_DOWN
	    	|| c == java.awt.event.KeyEvent.VK_S
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD2) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("down");
			s.setYLocation(y - 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_LEFT // 37
	    	|| c == java.awt.event.KeyEvent.VK_KP_LEFT
	    	|| c == java.awt.event.KeyEvent.VK_A
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD4) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("left");
			s.setXLocation(x + 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_RIGHT // 39
	    	|| c == java.awt.event.KeyEvent.VK_KP_RIGHT
	    	|| c == java.awt.event.KeyEvent.VK_D
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD6) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("right");
			s.setXLocation(x - 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_R
	    	|| c == java.awt.event.KeyEvent.VK_F) {
	    	//System.out.println("f : " + facing);
	    	//System.out.println("turn 90");
	    	facing = facing + 45;
	    	if (facing >= 360)
	    		facing = facing - 360;
	    	s.setFacing(facing);
	    	//System.out.println("f : " + s.getFacing());
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
     */
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

    /**
     * Determines and sets the position of a new construction site.
     * @param site the new construction site.
     * @param foundationStageInfo the site's foundation stage info.
     * @param constructionSkill the mission starter's construction skill.
     * @return modified construction site
     */
    private ConstructionSite positionNewConstructionSite(ConstructionSite site, ConstructionStageInfo foundationStageInfo,
            int constructionSkill) {
		//logger.info("ConstructionWizard's positionNewConstructionSite() is in " + Thread.currentThread().getName() + " Thread");
        boolean goodPosition = false;
        String buildingType = determinePreferredConstructedBuildingType(site, foundationStageInfo, constructionSkill);

		//if (foundationStageInfo != null) {
		//}
		//else {
		//}

      	if (buildingType == null) {
			Settlement settlement = site.getSettlement();
			// 2016-05-08 Added the use of getObjectiveBuildingType() to determine the desired building type
	        buildingType = settlement.getObjectiveBuildingType();
	        System.out.println("ConstructionWizard's positionNewConstructionSite() : using the length and width from Settlement Objective's " + buildingType);
        }

        //if (buildingType == null || buildingType.isEmpty())
        // Determine preferred building type from foundation stage info.
        //	buildingType = determinePreferredConstructedBuildingType(site, foundationStageInfo, constructionSkill);
        //System.out.println("ConstructionWizard's positionNewConstructionSite() buildingType : " + buildingType);

        if (buildingType != null) {
            //buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
            site.setWidth(buildingConfig.getWidth(buildingType));
            site.setLength(buildingConfig.getLength(buildingType));
            boolean isBuildingConnector = buildingConfig.hasBuildingConnection(buildingType);
            boolean hasLifeSupport = buildingConfig.hasLifeSupport(buildingType);
            if (isBuildingConnector) {
                // Try to find best location to connect two buildings.
                goodPosition = positionNewBuildingConnectorSite(site, buildingType);
            }
            else if (hasLifeSupport) {
                // Try to put building next to another inhabitable building.
                List<Building> inhabitableBuildings = site.getSettlement().getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
                Collections.shuffle(inhabitableBuildings);
                Iterator<Building> i = inhabitableBuildings.iterator();
                while (i.hasNext()) {
                    goodPosition = positionNextToBuilding(site, i.next(), DEFAULT_INHABITABLE_BUILDING_DISTANCE, false);
                    if (goodPosition) {
                        break;
                    }
                }
            }
            else {
                // Try to put building next to the same building type.
                List<Building> sameBuildings = site.getSettlement().getBuildingManager().getBuildingsOfSameType(buildingType);
                Collections.shuffle(sameBuildings);
                Iterator<Building> j = sameBuildings.iterator();
                while (j.hasNext()) {
                    goodPosition = positionNextToBuilding(site, j.next(), DEFAULT_NONINHABITABLE_BUILDING_DISTANCE, false);
                    if (goodPosition) {
                        break;
                    }
                }
            }
        }

        if (!goodPosition) {
            // Try to put building next to another building.
            // If not successful, try again 10m from each building and continue out at 10m increments
            // until a location is found.
            BuildingManager buildingManager = site.getSettlement().getBuildingManager();
            if (buildingManager.getBuildingNum() > 0) {
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
                site.setXLocation(0D);
                site.setYLocation(0D);
                site.setFacing(RandomUtil.getRandomDouble(360D));
            }
        }


        return site;
    }


    /**
     * Determines the preferred construction building type for a given foundation.
     * @param foundationStageInfo the foundation stage info.
     * @param constructionSkill the mission starter's construction skill.
     * @return preferred building type or null if none found.
     */
    private String determinePreferredConstructedBuildingType(ConstructionSite site,
    		ConstructionStageInfo foundationStageInfo, int constructionSkill) {
		//logger.info("ConstructionWizard's determinePreferredConstructedBuildingType() is in " + Thread.currentThread().getName() + " Thread");

        String result = null;

        ConstructionValues values = site.getConstructionManager().getConstructionValues();
        List<String> constructableBuildings = ConstructionUtil.getConstructableBuildingNames(foundationStageInfo);
        Iterator<String> i = constructableBuildings.iterator();
        double maxBuildingValue = Double.NEGATIVE_INFINITY;
        while (i.hasNext()) {
            String buildingType = i.next();
            double buildingValue = values.getConstructionStageValue(foundationStageInfo, constructionSkill);
            if (buildingValue > maxBuildingValue) {
                maxBuildingValue = buildingValue;
                result = buildingType;
            }
        }

        return result;
    }


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
        List<Building> inhabitableBuildings = manager.getBuildings(BuildingFunction.LIFE_SUPPORT);
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

            // Check to see if proposed new site position intersects with any existing buildings
            // or construction sites.
            if (site.getSettlement().getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX,
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