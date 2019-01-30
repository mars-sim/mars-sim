/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.1.0 2017-04-13
 * @author Manny Kung
 */
package org.mars_sim.javafx;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;

import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The TransportWizard class is a class for hosting building transport event manually.
 *
 */
public class TransportWizard {

	/** default logger. */
	private static Logger logger = Logger.getLogger(TransportWizard.class.getName());

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 9D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 9D;
	// Add key bindings related declarations
    private static final int ANIMATION_DELAY = 5;
    
    private static int wait_time_in_secs = 90; // in seconds

    private final static String TITLE = "Transport Wizard";

    private double xLast, yLast;

    private boolean upKeyPressed = false;
    private boolean downKeyPressed = false;
    private boolean leftKeyPressed = false;
    private boolean rightKeyPressed = false;
    private boolean turnKeyPressed = false;
    
    private EnumMap<KeyboardDirection, Boolean> enumMap = new EnumMap<>(KeyboardDirection.class);
    private Map<Integer, KeyboardDirection> keyboardMap = new HashMap<>();
    private javax.swing.Timer animationTimer;

    //public int xLoc;
    //public int yLoc;
    //public int facing;

	enum KeyboardDirection {
	   UP(0, 1, 0), DOWN(0, -1, 0), LEFT(1, 0, 0), RIGHT(-1, 0, 0), TURN(0, 0, 45);
	   private int incrX;
	   private int incrY;
	   private int facingChange;

	   private KeyboardDirection(int incrX, int incrY, int facingChange) {
	      this.incrX = incrX;
	      this.incrY = incrY;
	      this.facingChange = facingChange;
	   }
	   public int getIncrX() {
	      return incrX;
	   }
	   public int getIncrY() {
	      return incrY;
	   }
	   public int getFacing() {
		   return facingChange;
	   }
	}

	private String buildingNickName;

	private MainDesktopPane desktop;
	private SettlementWindow settlementWindow;
	private SettlementMapPanel mapPanel;
	private MainScene mainScene;
	private MainWindow mainWindow;
	private static BuildingConfig buildingConfig;

	private Resupply resupply;

	/**
	 * Constructor 1.
	 * For non-javaFX UI
	 * @param desktop the main desktop pane.
	 */
	public TransportWizard(final MainWindow mainWindow, final MainDesktopPane desktop) {
		this.desktop = desktop;
		this.mainWindow = mainWindow;
		this.settlementWindow = desktop.getSettlementWindow();
		this.mapPanel = settlementWindow.getMapPanel();
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	/**
	 * Constructor 2.
	 * For JavaFX UI
	 * @param mainScene the main scene
	 */
	public TransportWizard(final MainScene mainScene, MainDesktopPane desktop) {
		this.desktop = desktop;
		this.mainScene = mainScene;
    	//if (desktop.getSettlementWindow() != null)
    	//	settlementWindow = desktop.getSettlementWindow();
		//if (settlementWindow == null) System.out.println("settlementWindow is null");
    	//if (settlementWindow.getMapPanel() != null)
    	//	mapPanel = settlementWindow.getMapPanel();
		//if (mapPanel == null) System.out.println("mapPanel is null");
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}


	/**
     * Delivers buildings to the destination settlement.
     */
	// Add keyword synchronized to avoid JOption crash
    public synchronized void deliverBuildings(BuildingManager mgr) {
    	logger.info("deliverBuildings() is on " + Thread.currentThread().getName()); // normally on JavaFX Application Thread
//	    resupply = mgr.getResupply();

    	if (settlementWindow == null)
    		settlementWindow = desktop.getSettlementWindow();
    	if (mapPanel == null)
    		mapPanel = settlementWindow.getMapPanel();

   		// TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission
    	// how to make each building ask for a position ?

		if (mainScene != null) {
			mainScene.setSettlement(mgr.getSettlement());
		}
		else {
			// Select the relevant settlement
			desktop.openToolWindow(SettlementWindow.NAME);
			settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(mgr.getSettlement());
		}

        if (mainScene != null) {
			//boolean previous = mainScene.startPause();
        	mainScene.pauseSimulation(false);
			determineEachBuildingPosition(mgr);
			//mainScene.endPause(previous);
	    	Simulation.instance().getMasterClock().setPaused(false, true);
			
		}
        else {
        	// non-javaFX mode
        	determineEachBuildingPosition(mgr);
        }

        // Deliver the rest of the supplies and add people.
	    // Needed to get back to the original thread that started the resupply event
	    Simulation.instance().getMasterClock().getClockListenerExecutor()
	    		.execute(new DeliverTask(mgr));
    }


	class DeliverTask implements Runnable {

		private BuildingManager mgr;

		DeliverTask(BuildingManager mgr) {
			this.mgr = mgr;
		}

		public void run() {
		   	logger.info("DeliverTask's run() is on " + Thread.currentThread().getName() + " Thread");
			// it's now on pool-3-thread-1 Thread
		   	resupply.deliverOthers();
           	mgr.getSettlement().fireUnitUpdate(UnitEventType.END_TRANSPORT_WIZARD_EVENT);
		}
    }

//    /**
//     * Asks user if all arrival buildings use the default template positions
//
//    @SuppressWarnings("restriction")
//	public synchronized void askDefaultPosition(BuildingManager mgr, String missionName, boolean previousPause) {
//
//		String header = "Building Delivery from a Resupply Transport";
//
//        if (missionName != null)
//			header = "Building Delivery for \"" + missionName + "\"";
//
//    	String message = "Use default positions for all arriving buildings in "
//    			+ mgr.getSettlement() + "?";
//
//
//    	Alert alert = new Alert(AlertType.CONFIRMATION);
//		alert.setTitle(TITLE);
//    	alert.initOwner(mainScene.getStage());
//		alert.initModality(Modality.NONE); // Modality.NONE is by default if initModality() is NOT specified.
//    	//Note: with Modality.NONE, users can zoom in/out, move around the settlement map and move a vehicle elsewhere
//		alert.initModality(Modality.APPLICATION_MODAL); // not working. the use of this will block the first alert dialog
//		//alert.initModality(Modality.WINDOW_MODAL); // the use of this will not block the second aler dialog from appearing
//		alert.setHeaderText("Building Delivery for a Resuply Mission");
//		alert.setContentText(message);
//		//DialogPane dialogPane = alert.getDialogPane();
//
//		ButtonType buttonTypeYes = new ButtonType("Yes");
//		ButtonType buttonTypeNo = new ButtonType("No");
//		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
//
//		alert.showAndWait().ifPresent(response -> {
//		     if (response == buttonTypeYes) {
//		    	 unpause(previousPause);
//		    	 SwingUtilities.invokeLater(() -> {
//			 			resupply.deliverBuildings();
//					});
//		    	 logger.info("All buildings are put in place at default positions at " + mgr.getSettlement());
//		     }
//		     else if (response == buttonTypeNo) {
//		    	 unpause(previousPause);
//		    	 determineEachBuildingPosition(mgr);
//		     }
//
//		});
//
//		// Make "Enter" key to work like the space bar for firing the button on focus
//		EventHandler<KeyEvent> fireOnEnter = event -> {
//		    if (KeyCode.ENTER.equals(event.getCode())
//		            && event.getTarget() instanceof Button) {
//		        ((Button) event.getTarget()).fire();
//		    }
//		};
//
//		alert.getButtonTypes().stream()
//		        .map(alert.getDialogPane()::lookupButton)
//		        .forEach(button ->
//		                button.addEventHandler(
//		                        KeyEvent.KEY_PRESSED,
//		                        fireOnEnter
//		                )
//		        );
//    }


	/**
	 * Determines the placement of each building manually, instead of using the template positions
	 */
	public synchronized void determineEachBuildingPosition(BuildingManager mgr) {
		//logger.info("determineEachBuildingPosition() is in " + Thread.currentThread().getName() + " Thread");
		// normally on JavaFX Application Thread
        List<BuildingTemplate> orderedBuildings = resupply.orderNewBuildings();
        //System.out.println("orderedBuildings.size() : " + orderedBuildings.size());
        //if (orderedBuildings.size() > 0) {

	        // Add the use of ComparatorOfBuildingID()
	        Collections.sort(orderedBuildings, new ComparatorOfBuildingID());
	        Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();
	        while (buildingI.hasNext()) {
	           BuildingTemplate template = buildingI.next();
	           //System.out.println("TransportWizard : BuildingTemplate for " + template.getNickName());
	    	   // check if it's a building connector and if it's connecting the two buildings at their template position
//	        	   boolean isConnector = buildingConfig.hasBuildingConnection(template.getBuildingType());
//	           if (isConnector) confirmBuildingLocation(correctedTemplate, false);
//	           else confirmBuildingLocation(correctedTemplate, true);
	
	           // Add this recursive method checkTemplatePosition()
	           // to handle the creation of a new building template in case of an obstacle.
	           checkTemplatePosition(mgr, template, true);
	           // TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission
		    } // end of while (buildingI.hasNext())

	        Building building = mgr.getACopyOfBuildings().get(0);
	        mgr.getSettlement().fireUnitUpdate(UnitEventType.END_CONSTRUCTION_WIZARD_EVENT, building);

        //resupply.deliverOthers();
        // Need to get back to the original thread in Resupply.java that started the instance
        //Simulation.instance().getMasterClock().getClockListenerExecutor().submit(new DeliverOthersTask(mgr));

	}


	class DeliverOthersTask implements Runnable {
		BuildingManager mgr;
		DeliverOthersTask(BuildingManager mgr) {
			this.mgr = mgr;
		}

		public void run() {
			logger.info("DeliverOthersTask's run() is in " + Thread.currentThread().getName() + " Thread");
	       	// Deliver the rest of the supplies and add people.
	        resupply.deliverOthers();
		}
    }


    /**
     * Checks if the prescribed template position for a building has obstacles and if it does, gets a new template position
     * @param template the position of the proposed building
     * @param checkVehicle if it has checked/moved the vehicle already
     */
    public synchronized void checkTemplatePosition(BuildingManager mgr, BuildingTemplate template, boolean defaultPosition) {

        // Replace width and length defaults to deal with variable width and length buildings.
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(template.getBuildingType());
        if (template.getWidth() > 0D) {
            width = template.getWidth();
        }
        if (width <= 0D) {
            width = DEFAULT_VARIABLE_BUILDING_WIDTH;
         }

        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(template.getBuildingType());
        if (template.getLength() > 0D) {
            length = template.getLength();
        }
        if (length <= 0D) {
            length = DEFAULT_VARIABLE_BUILDING_LENGTH;
        }

        int buildingID = mgr.getNextTemplateID();

        int buildingTypeID = mgr.getNextBuildingTypeID(template.getBuildingType());

        int scenarioID = mgr.getSettlement().getID();
        String scenario = getCharForNumber(scenarioID + 1);
        //buildingNickName = template.getBuildingType() + " " + scenario + buildingID;
        buildingNickName = template.getBuildingType() + " " + buildingTypeID;

    	// obtain the same template with a new nickname for the building
     	BuildingTemplate newT = new BuildingTemplate(template.getMissionName(),
     			buildingID, scenario, template.getBuildingType(), buildingNickName,
     			width, length, template.getXLoc(), template.getYLoc(), template.getFacing());

		newT = clearCollision(newT, mgr, Resupply.MAX_COUNTDOWN);

		if (newT != null)
			pauseAndCheck(mgr, newT, true);

    }


    /**
     * Checks for collision with existing buildings/vehicles/construction sites
     * and creates the building based on the template to the settlement
     * @param correctedTemplate
     */
    public synchronized void pauseAndCheck(BuildingManager mgr, BuildingTemplate correctedTemplate, boolean preconfigured) {
    	//System.out.println("inside checkTemplateAddBuilding()");
    	// TODO: make use of the preconfigured boolean field to distinguish between the planned template loaded from xml vs. a newly created template
	    mapPanel.repaint();
    	//boolean previous = true;

		// Pause simulation.
		if (mainScene != null)
			mainScene.pauseSimulation(false);
			//previous = mainScene.startPause();

    	// Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
    	boolean ok = isTemplatePositionClear(mgr, correctedTemplate);
    	//System.out.println("checking is " + checking);
		if (ok) {
			createDialog(mgr, correctedTemplate, true, false); // true, false);
			//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, true)");

		} else {
			// The building's original template position has been occupied. Get another location for the building
			BuildingTemplate newT = clearCollision(correctedTemplate, mgr, Resupply.MAX_COUNTDOWN);
			//System.out.println("inside checkTemplateAddBuilding(), just got newT");
			if (newT != null)
				createDialog(mgr, newT, true, false); //false, true);
			//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, false)");
		}

		if (mainScene != null)
	    	Simulation.instance().getMasterClock().setPaused(false, true);
//			mainScene.unpauseSimulation();
			//mainScene.endPause(previous);

    }


    /**
     * Checks if a building original template's position is clear of collisions with any existing structures.
     * @param template the building template.
     * @return true if building template position is clear.
     */
    public boolean isTemplatePositionClear(BuildingManager mgr, BuildingTemplate bt) {

        boolean result = true;

     	double xLoc = bt.getXLoc();
    	double yLoc = bt.getYLoc();
    	double w = bt.getWidth();
		double l = bt.getLength();
		double f = bt.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

        result = !LocalAreaUtil.isVehicleBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);
        
//        // Replace width and length defaults to deal with variable width and length buildings.
//        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(template.getBuildingType());
//        if (template.getWidth() > 0D) {
//            width = template.getWidth();
//        }
//        if (width <= 0D) {
//            width = DEFAULT_VARIABLE_BUILDING_WIDTH;
//        }
//
//        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(template.getBuildingType());
//        if (template.getLength() > 0D) {
//            length = template.getLength();
//        }
//        if (length <= 0D) {
//            length = DEFAULT_VARIABLE_BUILDING_LENGTH;
//        }
//
//        result = mgr.isBuildingLocationOpen(template.getXLoc(),
//                template.getYLoc(), width, length, template.getFacing());

        return result;
    }


    /**
     * Identifies the type of collision and gets new template if the collision is immovable
     * @param bt a building template
     * @param mgr BuildingManager
     * @param count number of counts
     * @return Updated building template
     */
    public BuildingTemplate clearCollision(BuildingTemplate bt, BuildingManager mgr, int count) {
    	
      	double xLoc = bt.getXLoc();
    	double yLoc = bt.getYLoc();
    	double w = bt.getWidth();
		double l = bt.getLength();
		double f = bt.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

//        boolean isCollided = 
        LocalAreaUtil.isVehicleBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);
        
        return bt;
//    	return resupply.clearCollision(bt, count);
    }

    /**
     * Checks for collision and relocate any vehicles if found
     * @param xLoc
     * @param yLoc
     * @param coordinates
     * @return true if the location is clear of collision
     */
    public boolean isCollisionFreeVehicle(Building b, BuildingManager mgr) {

    	double xLoc = b.getXLocation();
    	double yLoc = b.getYLocation();
    	double w = b.getWidth();
		double l = b.getLength();
		double f = b.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

        return !LocalAreaUtil.isVehicleBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);

    }



    /**
     * Check for collision for an immovable object
     * @param building
     * @param buildingManager
     * @param count The number of times remaining checking collision
     * @return true if no collision.
     */
    public boolean isCollisionFreeImmovable(Building b, BuildingManager mgr, int count) {
    	count--;
		logger.info(count + " : calling checkCollisionImmovable(b, mgr) for " + b.getNickName());

    	double xLoc = b.getXLocation();
    	double yLoc = b.getYLocation();
    	double w = b.getWidth();
		double l = b.getLength();
		double f = b.getFacing();

		//BoundedObject boundedObject = null;

		//if (b.getBuildingType().equalsIgnoreCase("hallway")
		//	|| b.getBuildingType().equalsIgnoreCase("tunnel"))
		//	boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);
		//else
		//	boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		return !LocalAreaUtil.isImmovableBoundedOjectIntersected(new BoundedObject(xLoc, yLoc, w, l, f), mgr.getSettlement().getCoordinates());

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


    /**
     * Asks user to confirm the location of the new building.
     * @param buildingManager
     * @param template the position of the proposed building
     * @param isAtPreDefinedLocation
     * @param isNewTemplate
     */
	public synchronized void createDialog(BuildingManager mgr, BuildingTemplate template,
			boolean isAtPreDefinedLocation, boolean isNewTemplate) {
		//System.out.println("inside confirmBuildingLocation");
		//Building newBuilding = mgr.addOneBuilding(template, mgr.getResupply(), true);
		Building newBuilding = mgr.prepareToAddBuilding(template, resupply, true);

		// Clear any vehicles that block the location
		clearCollision(template, mgr, 10);
				
		//if (isAtPreDefinedLocation || isNewTemplate) {
		//	newBuilding = mgr.prepareToAddBuilding(template, resupply, false);
		//}

        // Determine location and facing for the new building.
		double xLoc = template.getXLoc();//newBuilding.getXLocation();
		double yLoc = template.getYLoc();//newBuilding.getYLocation();
		double scale = mapPanel.getScale();

		Settlement currentS = settlementWindow.getMapPanel().getSettlement();
		if (currentS != mgr.getSettlement()) {
			if (mainScene != null) {
				mainScene.setSettlement(mgr.getSettlement());
			}
			else
				settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(mgr.getSettlement());
		}
  		// set up the Settlement Map Tool to display the suggested location of the building
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	    //mapPanel.repaint();
		//mapPanel.setShowBuildingLabels(true);
		//mapPanel.getSettlementTransparentPanel().getBuildingLabelMenuItem().setSelected(true);

		String header = null;
		String title = null;
		String message = "(1) Will default to \"Yes\" in 30 secs unless timer is cancelled."
				 + System.lineSeparator() + "(2) To MANUALLY select a location, press 'Keyboard/Mouse' Button.";
		StringProperty msg = new SimpleStringProperty(message);

		if (isAtPreDefinedLocation) {
			header = "Would you like to place " + buildingNickName + " at its default position? ";
		}
		else {
			header = "Would you like to place " + buildingNickName + " at this position? ";
		}


		String missionName = template.getMissionName();

        if (missionName != null)
		//if (missionName.equals("null"))
			title = template.getMissionName() + " at " + mgr.getSettlement();
        else
        	title = "A Resupply Transport" + " at " + mgr.getSettlement();

        if (mainScene != null) {
    		//System.out.println("inside confirmBuildingLocation, calling alertDialog");
        	alertDialog(title, header, msg, template, mgr, newBuilding, true);//, timer);
        	mapPanel.repaint();
		} else {
			// for Swing mode
	        desktop.openAnnouncementWindow("Pause for Transport Wizard");
	        AnnouncementWindow aw = desktop.getAnnouncementWindow();
	        Point location = MouseInfo.getPointerInfo().getLocation();
	        double Xloc = location.getX() - aw.getWidth() * 2;
			double Yloc = location.getY() - aw.getHeight() * 2;
			aw.setLocation((int)Xloc, (int)Yloc);

			int reply = JOptionPane.showConfirmDialog(aw, header, TITLE, JOptionPane.YES_NO_OPTION);
			//repaint();

			if (reply == JOptionPane.YES_OPTION) {
	            logger.info(newBuilding.toString() + " is put in Place.");
			}
			else {
				mgr.removeBuilding(newBuilding);
				// recursive calling of createDialog()
				createDialog(mgr, template, false, true);
			}

			desktop.disposeAnnouncementWindow();
        }
	}


	public synchronized void alertDialog(String title, String header, StringProperty msg, BuildingTemplate template,
		BuildingManager mgr, Building newBuilding, boolean hasTimer){

    	// Platform.runLater(() -> {
		//try {
		//	FXUtilities.runAndWait(() -> {

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
				alert.setContentText(msg.get());
				// Use JavaFX binding
				alert.getDialogPane().contentTextProperty().bind(msg);
				//alert.getDialogPane().headerTextProperty().bind(arg0);

				ButtonType buttonTypeYes = new ButtonType("Yes");
				ButtonType buttonTypeNo = new ButtonType("No");
				ButtonType buttonTypeMouseKB = new ButtonType("Keyboard/Mouse");
				ButtonType buttonTypeCancelTimer = null;

				Timer timer = null;

				if (hasTimer) {
					buttonTypeCancelTimer = new ButtonType("Cancel Timer");
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancelTimer, buttonTypeMouseKB);

					IntegerProperty i = new SimpleIntegerProperty(wait_time_in_secs);
					// Add ReactFX's Timer and FxTimer
					timer = FxTimer.runPeriodically(java.time.Duration.ofMillis(1000), () -> {
			        	int num = i.get() - 1;
			        	if (num >= 0) {
			        		i.set(num);
			        	}
			        	
			        	if (num == 0) {
			        		Button button = (Button) alert.getDialogPane().lookupButton(buttonTypeYes);
			        	    button.fire();
			        	}
			        	msg.set("(1) Will default to \"Yes\" in " + num + " seconds unless countdown timer is cancelled."
			        			+ System.lineSeparator() +  "(2) To MANUALLY select a location, press 'Keyboard/Mouse' Button.");
					});
				}
				else {
					msg.set("To MANUALLY select a location, press 'Keyboard/Mouse' Button.");
					alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeMouseKB);
				}

				// For hallway and tunnel, use the computer-generated position
				//if (newBuilding.getBuildingType().equalsIgnoreCase("hallway")
				//		|| newBuilding.getBuildingType().equalsIgnoreCase("tunnel")) {
				//	Button button = (Button) alert.getDialogPane().lookupButton(buttonTypeMouseKB);
				//	button.setVisible(false);
				//}

				Optional<ButtonType> result = null;

				result = alert.showAndWait();

				if (result.isPresent() && result.get() == buttonTypeYes) {
					if (template.getMissionName() != null) {
						logger.info(newBuilding.toString() + " from " + template.getMissionName()
					 			+ " is put in place in " + mgr.getSettlement());
						newBuilding.setInTransport(false);
					}

				} else if (result.isPresent() && result.get() == buttonTypeNo) {
			    	mgr.removeBuilding(newBuilding);
			    	mapPanel.repaint();

			    	BuildingTemplate repositionedTemplate = resupply.positionNewResupplyBuilding(template.getBuildingType());

			    	// Add setMissionName()
					repositionedTemplate.setMissionName(template.getMissionName());

					pauseAndCheck(mgr, repositionedTemplate, false);
					//checkTemplatePosition(mgr, repositionedTemplate, false);

				} else if (result.isPresent() && result.get() == buttonTypeMouseKB) {
					placementDialog(title, header, newBuilding, mgr);

				} else if (hasTimer && result.isPresent() && result.get() == buttonTypeCancelTimer) {
					timer.stop();
					alertDialog(title, header, msg, template, mgr, newBuilding, false);
				}
		//	});
		//} catch (InterruptedException e) {
		//	e.printStackTrace();
		//} catch (ExecutionException e) {
		//	e.printStackTrace();
		//}
	}


	/**
	 * Pops up an alert dialog for confirming the position placement of a new construction site via keyboard/mouse
	 * @param title
	 * @param header
	 * @param site
	 */
	@SuppressWarnings("restriction")
	public void placementDialog(String title, String header, Building newBuilding, BuildingManager mgr) {

		//SwingUtilities.invokeLater(() -> {
			mapPanel.setFocusable(true);
			mapPanel.requestFocusInWindow();
		//});

		// Platform.runLater(() -> {
		// FXUtilities.runAndWait(() -> {
			String msg = "KEYBOARD :\t(1) Highlight the Settlement Map Tool" + System.lineSeparator()
					+ "\t\t\t(2) Use w/a/s/d, num pad keys arrows to move" + System.lineSeparator()
					+ "\t\t\t(3) Press 'r' or 'f' to rotate 45 degrees clockwise" + System.lineSeparator()
					+ "   MOUSE :\t(1) Press & Hold the left button on the building" + System.lineSeparator()
					+ "\t\t\t(2) Move the cursor to the destination" + System.lineSeparator()
					+ "\t\t\t(3) Release button to drop it off" + System.lineSeparator()
					+ "\t\t\t(4) Hit \"Confirm Position\" button to proceed";
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


			KeyboardDetection keyboard = new KeyboardDetection(newBuilding, mgr);
			// Add keyboard mapping and key bindings
//			for (KeyboardDirection dir : KeyboardDirection.values()) {
//				enumMap.put(dir, Boolean.FALSE);
//			}
//
//			keyboardMap.put(java.awt.event.KeyEvent.VK_UP, KeyboardDirection.UP);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_DOWN, KeyboardDirection.DOWN);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_LEFT, KeyboardDirection.LEFT);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_RIGHT, KeyboardDirection.RIGHT);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_R, KeyboardDirection.TURN);
//
//			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_UP, KeyboardDirection.UP);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_DOWN, KeyboardDirection.DOWN);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_LEFT, KeyboardDirection.LEFT);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_RIGHT, KeyboardDirection.RIGHT);
//			keyboardMap.put(java.awt.event.KeyEvent.VK_F, KeyboardDirection.TURN);
//
//			setKeyBindings();
//			animationTimer = new javax.swing.Timer(ANIMATION_DELAY, new AnimationListener(newBuilding, mgr));
//			animationTimer.start();

			MouseMotionDetection mouseMotion = new MouseMotionDetection(newBuilding, mgr);

			//SwingUtilities.invokeLater(() -> {
				mapPanel.setFocusable(true);
				mapPanel.requestFocusInWindow();
				mapPanel.addKeyListener(keyboard);
				mapPanel.addMouseMotionListener(mouseMotion);
				logger.info("addKeyListener() and addMouseMotionListener()");  
//				mapPanel.addMouseListener(new MouseListener() {
//				    @Override
//				    public void mouseClicked(MouseEvent evt) {
//					//	Point location = MouseInfo.getPointerInfo().getLocation();
//						// empty
//				    }
//
//					@Override
//					public void mouseEntered(MouseEvent evt) {
//						// empty
//					}
//
//					@Override
//					public void mouseExited(MouseEvent evt) {
//						// empty
//					}
//
//					@Override
//					public synchronized void mousePressed(MouseEvent evt) {
//						// empty
//					}
//
//					@Override
//					public synchronized void mouseReleased(MouseEvent evt) {
//						// empty
//					}
//
//				});			
			//});

			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeConfirm) {
				newBuilding.setInTransport(false);
				mapPanel.removeKeyListener(keyboard);
				//removeKeyBindings();
				//animationTimer.stop();
				mapPanel.removeMouseMotionListener(mouseMotion);
			}

	}

	/**
	 * Check if the new building is outside minimum radius and within the maximum radius
	 * @param newBuilding the new building
	 * @param mgr the building manager
	 * @return true if it's within the prescribed zone
	 */
	public boolean isWithinZone(Building newBuilding, BuildingManager mgr) {

    	boolean withinRadius = true;
    	int maxDistance = 0;
    	int leastDistance = 0;
    	// TOD: also check if
    	boolean hasLifeSupport = buildingConfig.hasLifeSupport(newBuilding.getBuildingType());
    	if (hasLifeSupport) {

    	  	if (newBuilding.getBuildingType().equalsIgnoreCase("Astronomy Observatory")) {
    	  		maxDistance = Resupply.MAX_OBSERVATORY_BUILDING_DISTANCE;
    	  		leastDistance = Resupply.MIN_OBSERVATORY_BUILDING_DISTANCE;
    	  	}
    	  	else {
        		maxDistance = Resupply.MAX_INHABITABLE_BUILDING_DISTANCE;
        		leastDistance = Resupply.MIN_INHABITABLE_BUILDING_DISTANCE;
    	  	}

    	}

    	else {
    		maxDistance = Resupply.MAX_NONINHABITABLE_BUILDING_DISTANCE;
    		leastDistance = Resupply.MIN_NONINHABITABLE_BUILDING_DISTANCE;
    	}

    	List<Building> list = mgr.getBuildings(FunctionType.LIFE_SUPPORT);
        Collections.shuffle(list);

        Iterator<Building> i = list.iterator();
        while (i.hasNext()) {
            Building startingBuilding = i.next();

            double distance = Point2D.distance(startingBuilding.getXLocation(),
                startingBuilding.getYLocation(), newBuilding.getXLocation(),
                newBuilding.getYLocation());
            //logger.info("distance : " + distance);
            if (distance < leastDistance) {
            	withinRadius = false;
            	break;
            }
        }


	    return withinRadius;
	}


	/**
	 * Check if the building template is outside minimum radius and within the maximum radius
	 * @param bt the building template
	 * @param buildingManager buildingManager
	 * @return true if it's within the prescribed zone
	 */
	public boolean isWithinZone(BuildingTemplate bt, BuildingManager buildingManager) {

    	boolean withinRadius = true;
    	int maxDistance = 0;
    	int leastDistance = 0;
    	// TOD: also check if
    	boolean hasLifeSupport = buildingConfig.hasLifeSupport(bt.getBuildingType());
    	if (hasLifeSupport) {

    	  	if (bt.getBuildingType().equalsIgnoreCase("Astronomy Observatory")) {
    	  		maxDistance = Resupply.MAX_OBSERVATORY_BUILDING_DISTANCE;
    	  		leastDistance = Resupply.MIN_OBSERVATORY_BUILDING_DISTANCE;
    	  	}
    	  	else {
        		maxDistance = Resupply.MAX_INHABITABLE_BUILDING_DISTANCE;
        		leastDistance = Resupply.MIN_INHABITABLE_BUILDING_DISTANCE;
    	  	}

    	}

    	else {
    		maxDistance = Resupply.MAX_NONINHABITABLE_BUILDING_DISTANCE;
    		leastDistance = Resupply.MIN_NONINHABITABLE_BUILDING_DISTANCE;
    	}

    	List<Building> list = buildingManager.getBuildings(FunctionType.LIFE_SUPPORT);
        Collections.shuffle(list);

        Iterator<Building> i = list.iterator();
        while (i.hasNext()) {
            Building startingBuilding = i.next();

            double distance = Point2D.distance(startingBuilding.getXLocation(), startingBuilding.getYLocation(),
            		bt.getXLoc(), bt.getYLoc());
            //logger.info("distance : " + distance);
            if (distance < leastDistance) {
            	withinRadius = false;
            	break;
            }
        }

	    return withinRadius;
	}

	private void setKeyBindings() {
	      int condition = javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
	      final javax.swing.InputMap inputMap = mapPanel.getInputMap(condition);
	      final javax.swing.ActionMap actionMap = mapPanel.getActionMap();
	      boolean[] keyPressed = { true, false };

	      //boolean[] keys = new boolean[KeyEvent.KEY_TYPED];
		  //keys[evt.getKeyCode()] = true;

	      for (Integer keyCode : keyboardMap.keySet()) {
	         KeyboardDirection dir = keyboardMap.get(keyCode);
	         for (boolean onKeyPress : keyPressed) {
	            boolean onKeyRelease = !onKeyPress;
	            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, onKeyRelease);
	            Object key = keyStroke.toString();
	            inputMap.put(keyStroke, key);
	            actionMap.put(key, new KeyBindingsAction(dir, onKeyPress));
	         }
	      }
	   }

	private void removeKeyBindings() {
	      int condition = javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
	      final javax.swing.InputMap inputMap = mapPanel.getInputMap(condition);
	      final javax.swing.ActionMap actionMap = mapPanel.getActionMap();
	      boolean[] keyPressed = { true, false };

	      //boolean[] keys = new boolean[KeyEvent.KEY_TYPED];
		  //keys[evt.getKeyCode()] = true;

	      for (Integer keyCode : keyboardMap.keySet()) {
	         KeyboardDirection dir = keyboardMap.get(keyCode);
	         for (boolean onKeyPress : keyPressed) {
	            boolean onKeyRelease = !onKeyPress;
	            KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, onKeyRelease);
	            //Object key = keyStroke.toString();
	            inputMap.put(keyStroke, "none");
	            actionMap.put("none", new KeyBindingsAction(dir, onKeyPress));
	         }
	      }
	   }

	   private class KeyBindingsAction extends AbstractAction {
	      private KeyboardDirection dir;
	      boolean pressed;

	      public KeyBindingsAction(KeyboardDirection dir, boolean pressed) {
	         this.dir = dir;
	         this.pressed = pressed;
	      }

	      @Override
	      public void actionPerformed(ActionEvent evt) {
	         enumMap.put(dir, pressed);
	      }
	   }

	   private class AnimationListener implements ActionListener {
		   private Building b;
		   private BuildingManager mgr;

		   AnimationListener(Building b, BuildingManager mgr) {
			   this.b = b;
			   this.mgr = mgr;
		   }

	      @Override
	      public void actionPerformed(ActionEvent evt) {
	    	  boolean repaint = false;
	    	  int xLoc = (int) b.getXLocation();
	    	  int yLoc = (int) b.getYLocation();
	    	  int facing = (int) b.getFacing();

			  for (KeyboardDirection dir : KeyboardDirection.values()) {

				  if (enumMap.get(dir)) {
					  //System.out.println("dir.getIncrX() : " + dir.getIncrX());
					  isCollisionFreeVehicle(b, mgr);
					  boolean ok2 = isCollisionFreeImmovable(b, mgr, Resupply.MAX_COUNTDOWN);

					  if (ok2 && isWithinZone(b, mgr)) {

						  //System.out.println("ok2 : "+ ok2);
		    			  xLoc += dir.getIncrX();
		    			  yLoc += dir.getIncrY();

		    			  b.setXLocation(xLoc);
		    			  b.setYLocation(yLoc);

		    			  facing += dir.getFacing();
		    			  if (facing >= 360)
		    			    facing = facing - 360;
		    			  b.setFacing(facing);

		    			  repaint = true;

		    			  return;

					  } else {

	    				  xLoc = xLoc - dir.getIncrX() * 2;
		    			  yLoc = yLoc - dir.getIncrY() * 2;

		    			  b.setXLocation(xLoc);
		    			  b.setYLocation(yLoc);

		    			  facing -= dir.getFacing();
		    			  if (facing < 0)
		    				  facing = facing + 360;
		    			  b.setFacing(facing);

		    			  repaint = true;

		    			  return;
	    				  //break;
		    			  //continue;
	    			  }
    			  }
    			  else {
    				  //System.out.println("dir.getIncrX() : " + dir.getIncrX());
    				  //System.out.println("dir.getIncrY() : " + dir.getIncrY());

    			  }
			  }


			  if (repaint) {
				  mapPanel.repaint();
			  }

	      }
	}

	class MouseMotionDetection implements MouseMotionListener{
		private Building newBuilding;
		private BuildingManager mgr;

		MouseMotionDetection(Building newBuilding, BuildingManager mgr) {
			this.newBuilding = newBuilding;
			this.mgr = mgr;
		}

		@Override
		public synchronized void mouseDragged(MouseEvent e) {
			xLast = e.getX();
			yLast = e.getY();
		}

		@Override
		public synchronized void mouseMoved(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				
				mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			    // Check for collision here
			    isCollisionFreeVehicle(newBuilding, mgr);
			    //boolean ok2 = isCollisionFreeImmovable(newBuilding, mgr, Resupply.MAX_COUNTDOWN);

			    //if (ok2) {
			    //	boolean withinRadius = isWithinZone(newBuilding, mgr);

			    //	if (withinRadius)
			    		moveNewBuildingTo(newBuilding, e.getX(), e.getY());
			    //}
			}
			xLast = e.getX();
			yLast = e.getY();
		}
	
	}

	class KeyboardDetection implements KeyListener{
		private Building newBuilding;
		private BuildingManager mgr;

		KeyboardDetection(Building newBuilding, BuildingManager mgr) {
			this.newBuilding = newBuilding;
			this.mgr = mgr;
		}

		@Override
		public void keyPressed(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
	    
		    //System.out.println("c is " + c);
		    // Check for collision here
		    //isCollisionFreeVehicle(newBuilding, mgr);
		    //boolean ok2 = isCollisionFreeImmovable(newBuilding, mgr, Resupply.MAX_COUNTDOWN);

		    //if (ok2) {
		    //	boolean withinRadius = isWithinZone(newBuilding, mgr);

		    //	if (withinRadius)
		    		handleKeyPress(newBuilding, c);
		    //}

		    mapPanel.repaint();
			//e.consume();
		}

		@Override
		public void keyTyped(java.awt.event.KeyEvent e) {
			e.consume();
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
		    handleKeyRelease(newBuilding, c);
		    //e.consume();
/*		    
		    //System.out.println("c is " + c);
		    // Check for collision here
		    isCollisionFreeVehicle(newBuilding, mgr);
		    boolean ok2 = isCollisionFreeImmovable(newBuilding, mgr, Resupply.MAX_COUNTDOWN);

		    if (ok2) {
		    	boolean withinRadius = isWithinZone(newBuilding, mgr);

		    	if (withinRadius)
		    		handleKeyRelease(newBuilding, c);
		    }

		    mapPanel.repaint();
			e.consume();
*/			
		}
	}

	/**
	 * Moves the site to a new position via the mouse's right drag
	 * @param b
	 * @param xPixel
	 * @param yPixel
	 */
	public void moveNewBuildingTo(Building b, double xPixel, double yPixel) {
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

			double width = b.getWidth();
			double length = b.getLength();
			int facing = (int) b.getFacing();
			double x = b.getXLocation();
			double y = b.getYLocation();
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
				b.setXLocation(new_x);
				double new_y = Math.round(y + pixel.getY() - last.getY());
				b.setYLocation(new_y);

				xLast = xPixel;
				yLast = yPixel;
			}
		}
	}

	public void moveNewBuildingTo(Building b, double xPixel, double yPixel, double turnAngle) {
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

			double width = b.getWidth();
			double length = b.getLength();
			int facing = (int) b.getFacing();
			double x = b.getXLocation();
			double y = b.getYLocation();
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
				b.setXLocation(new_x);
				double new_y = Math.round(y + pixel.getY() - last.getY());
				b.setYLocation(new_y);

				xLast = xPixel;
				yLast = yPixel;
			}
		}
	}
	
	/**
	 * Sets the new x and y location and facing of the site
	 * @param b
	 * @param c
	 */
	public void handleKeyPress(Building b, int c) {

	    if (c == java.awt.event.KeyEvent.VK_UP // 38
	    	|| c == java.awt.event.KeyEvent.VK_KP_UP
	    	|| c == java.awt.event.KeyEvent.VK_W
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD8) {
	    	upKeyPressed = true;
	    	
	    } else if (c == java.awt.event.KeyEvent.VK_DOWN // 40
	    	|| c == java.awt.event.KeyEvent.VK_KP_DOWN
	    	|| c == java.awt.event.KeyEvent.VK_S
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD2) {
	    	downKeyPressed = true;

	    } else if (c == java.awt.event.KeyEvent.VK_LEFT // 37
	    	|| c == java.awt.event.KeyEvent.VK_KP_LEFT
	    	|| c == java.awt.event.KeyEvent.VK_A
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD4) {
	    	leftKeyPressed = true;

	    } else if (c == java.awt.event.KeyEvent.VK_RIGHT // 39
	    	|| c == java.awt.event.KeyEvent.VK_KP_RIGHT
	    	|| c == java.awt.event.KeyEvent.VK_D
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD6) {
	    	rightKeyPressed = true;

	    } else if (c == java.awt.event.KeyEvent.VK_R
	    	|| c == java.awt.event.KeyEvent.VK_F) {
	    	turnKeyPressed = true;
	    }

		int facing = (int) b.getFacing();
		double x = b.getXLocation();
		double y = b.getYLocation();

	    if (upKeyPressed) {
	    	b.setYLocation(y + 1);
	    	logger.info("setYLocation(y + 1)");
	    	//moveNewBuildingTo(b, 0, 1);
	    	upKeyPressed = false;

	    } 
	    else if (downKeyPressed) {
	    	b.setYLocation(y - 1);
	    	logger.info("setYLocation(y - 1)");
	    	//moveNewBuildingTo(b, 0, -1);
	    	downKeyPressed = false;
				
		} 
	    else if (leftKeyPressed) {
			b.setXLocation(x + 1);
			logger.info("setYLocation(x + 1)");
	    	//moveNewBuildingTo(b, 1, 0);
			leftKeyPressed = false;
			
		} 
	    else if (rightKeyPressed) {
			b.setXLocation(x - 1);
			logger.info("setYLocation(x - 1)");
	    	//moveNewBuildingTo(b, -1, 0);
			rightKeyPressed = false;
				
		}
	    else if (turnKeyPressed) {
	    	facing = facing + 45;
	    	if (facing >= 360)
	    		facing = facing - 360;
	    	b.setFacing(facing);
	    	logger.info("setFacing(facing)");
	    	//moveNewBuildingTo(b, 0, 0, 45);
	    	turnKeyPressed = false;
		}
	}


	/**
	 * Sets the new x and y location and facing of the site
	 * @param b
	 * @param c
	 */
	public void handleKeyRelease(Building b, int c) {

	    if (c == java.awt.event.KeyEvent.VK_UP // 38
	    	|| c == java.awt.event.KeyEvent.VK_KP_UP
	    	|| c == java.awt.event.KeyEvent.VK_W
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD8) {
	    	upKeyPressed = false;
	    	
	    } else if (c == java.awt.event.KeyEvent.VK_DOWN // 40
	    	|| c == java.awt.event.KeyEvent.VK_KP_DOWN
	    	|| c == java.awt.event.KeyEvent.VK_S
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD2) {
	    	downKeyPressed = false;

	    } else if (c == java.awt.event.KeyEvent.VK_LEFT // 37
	    	|| c == java.awt.event.KeyEvent.VK_KP_LEFT
	    	|| c == java.awt.event.KeyEvent.VK_A
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD4) {
	    	leftKeyPressed = false;

	    } else if (c == java.awt.event.KeyEvent.VK_RIGHT // 39
	    	|| c == java.awt.event.KeyEvent.VK_KP_RIGHT
	    	|| c == java.awt.event.KeyEvent.VK_D
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD6) {
	    	rightKeyPressed = false;

	    } else if (c == java.awt.event.KeyEvent.VK_R
	    	|| c == java.awt.event.KeyEvent.VK_F) {
	    	turnKeyPressed = false;
	    }

	}

	
	/**
	 * Compares and sorts a list of BuildingTemplates according to its building id
	 */
	class ComparatorOfBuildingID implements Comparator<BuildingTemplate>{

		@Override
		public int compare(BuildingTemplate t1, BuildingTemplate t2) {
			return t1.getID()-t2.getID();
		}
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		//mgr = null;
		desktop = null;
		settlementWindow = null;
		mapPanel = null;
	}

}