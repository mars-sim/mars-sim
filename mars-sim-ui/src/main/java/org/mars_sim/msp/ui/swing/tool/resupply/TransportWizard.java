/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.08 2016-03-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.*;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionValues;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.FXUtilities;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
//import javafx.scene.input.KeyEvent;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * The TransportWizard class is a class for hosting building transport event manually.
 *
 */
public class TransportWizard {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(TransportWizard.class.getName());

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 9D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 9D;

    private static int wait_time_in_secs = 30; // in seconds

    private final static String TITLE = "Transport Wizard";

    private double xLast, yLast;

	// 2016-03-08 Added key bindings related declarations
    private static final int ANIMATION_DELAY = 5;
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
	private BuildingConfig buildingConfig;


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
		this.buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
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
	// 2015-01-02 Added keyword synchronized to avoid JOption crash
    public synchronized void deliverBuildings(BuildingManager mgr) {
    	//logger.info("deliverBuildings() is on " + Thread.currentThread().getName());
    	// normally on JavaFX Application Thread
    	if (settlementWindow == null)
    		settlementWindow = desktop.getSettlementWindow();
    	if (mapPanel == null)
    		mapPanel = settlementWindow.getMapPanel();
		
   		// TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission
    	// how to make each building ask for a position ?

		// Select the relevant settlement
		desktop.openToolWindow(SettlementWindow.NAME);
		//System.out.println("Just open Settlement Map Tool");
	    settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(mgr.getSettlement());

        if (mainScene != null) {

        	try {
				FXUtilities.runAndWait(() -> {
					// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
					// 2015-12-16 Track the current pause state
					boolean previous0 = Simulation.instance().getMasterClock().isPaused();

					// Pause simulation.
					if (mainScene != null) {
						if (!previous0) {
							mainScene.pauseSimulation();
							//System.out.println("previous0 is false. Paused sim");
						}
						desktop.getTimeWindow().enablePauseButton(false);
					}

				    //List<BuildingTemplate> templates = mgr.getResupply().orderNewBuildings();
				    //BuildingTemplate aTemplate = templates.get(0);
					//String missionName = aTemplate.getMissionName();

				   	//askDefaultPosition(mgr, missionName, previous0);
					determineEachBuildingPosition(mgr);

					if (mainScene != null) {
						unpause(previous0);
					}


				});
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        else {
        	// non-javaFX mode
        	determineEachBuildingPosition(mgr);

        }

        // 2015-11-12 Deliver the rest of the supplies and add people.
	    // 2016-09-24 Needed to get back to the original thread that started the resupply event
	    Simulation.instance().getMasterClock().getClockListenerExecutor()
	    .execute(new DeliverTask(mgr));
    }


	//2016-09-24 Added DeliverTask
	class DeliverTask implements Runnable {

		private BuildingManager mgr;
		
		DeliverTask(BuildingManager mgr) {
			this.mgr = mgr;
		}

		public void run() {
		   	logger.info("DeliverTask's run() is on " + Thread.currentThread().getName() + " Thread");
			// it's now on pool-3-thread-1 Thread
			mgr.getResupply().deliverOthers();
		}
    }
    
    /**
     * Asks user if all arrival buildings use the default template positions

	// 2015-12-07 Added askDefaultPosition()
    @SuppressWarnings("restriction")
	public synchronized void askDefaultPosition(BuildingManager mgr, String missionName, boolean previousPause) {

		String header = "Building Delivery from a Resupply Transport";

        if (missionName != null)
			header = "Building Delivery for \"" + missionName + "\"";

    	String message = "Use default positions for all arriving buildings in "
    			+ mgr.getSettlement() + "?";


    	Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(TITLE);
    	alert.initOwner(mainScene.getStage());
		alert.initModality(Modality.NONE); // Modality.NONE is by default if initModality() is NOT specified.
    	//Note: with Modality.NONE, users can zoom in/out, move around the settlement map and move a vehicle elsewhere
		alert.initModality(Modality.APPLICATION_MODAL); // not working. the use of this will block the first alert dialog
		//alert.initModality(Modality.WINDOW_MODAL); // the use of this will not block the second aler dialog from appearing
		alert.setHeaderText("Building Delivery for a Resuply Mission");
		alert.setContentText(message);
		//DialogPane dialogPane = alert.getDialogPane();

		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");
		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

		alert.showAndWait().ifPresent(response -> {
		     if (response == buttonTypeYes) {
		    	 unpause(previousPause);
		    	 SwingUtilities.invokeLater(() -> {
			 			mgr.getResupply().deliverBuildings();
					});
		    	 logger.info("All buildings are put in place at default positions at " + mgr.getSettlement());
		     }
		     else if (response == buttonTypeNo) {
		    	 unpause(previousPause);
		    	 determineEachBuildingPosition(mgr);
		     }

		});

		// 2015-10-15 Made "Enter" key to work like the space bar for firing the button on focus
		EventHandler<KeyEvent> fireOnEnter = event -> {
		    if (KeyCode.ENTER.equals(event.getCode())
		            && event.getTarget() instanceof Button) {
		        ((Button) event.getTarget()).fire();
		    }
		};

		alert.getButtonTypes().stream()
		        .map(alert.getDialogPane()::lookupButton)
		        .forEach(button ->
		                button.addEventHandler(
		                        KeyEvent.KEY_PRESSED,
		                        fireOnEnter
		                )
		        );
    }
*/

    /**
     * Checks for the previous state before unpausing the sim.
     * @param previous state
     */
    public void unpause(boolean previous0) {
    	boolean now0 = Simulation.instance().getMasterClock().isPaused();
		if (!previous0) {
			if (now0) {
				mainScene.unpauseSimulation();
	    		//System.out.println("previous0 is false. now0 is true. Unpaused sim");
			}
		} else {
			if (!now0) {
				mainScene.unpauseSimulation();
	    		//System.out.println("previous0 is true. now0 is false. Unpaused sim");
			}
		}
		desktop.getTimeWindow().enablePauseButton(true);
    }

	/**
	 * Determines the placement of each building manually, instead of using the template positions
	 */
    // 2015-12-07 Added determineEachBuildingPosition()
	public synchronized void determineEachBuildingPosition(BuildingManager mgr) {
		//logger.info("determineEachBuildingPosition() is in " + Thread.currentThread().getName() + " Thread");
		// normally on JavaFX Application Thread
        List<BuildingTemplate> orderedBuildings = mgr.getResupply().orderNewBuildings();
        //System.out.println("orderedBuildings.size() : " + orderedBuildings.size());
        //if (orderedBuildings.size() > 0) {

        	// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
    		//if (mainScene != null)
    		//	mainScene.pauseSimulation();

	        // 2015-12-19 Added the use of ComparatorOfBuildingID()
	        Collections.sort(orderedBuildings, new ComparatorOfBuildingID());
	        Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();
	        while (buildingI.hasNext()) {
	           BuildingTemplate template = buildingI.next();
	           //System.out.println("TransportWizard : BuildingTemplate for " + template.getNickName());
	/*    	   // check if it's a building connector and if it's connecting the two buildings at their template position
	        	   boolean isConnector = buildingConfig.hasBuildingConnection(template.getBuildingType());
	           if (isConnector) confirmBuildingLocation(correctedTemplate, false);
	           else confirmBuildingLocation(correctedTemplate, true);
	*/
	           // 2015-12-06 Added this recursive method checkTemplatePosition()
	           // to handle the creation of a new building template in case of an obstacle.
	           checkTemplatePosition(mgr, template, true);
	           // TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission
		    } // end of while (buildingI.hasNext())

	        Building building = mgr.getACopyOfBuildings().get(0);
	        mgr.getSettlement().fireUnitUpdate(UnitEventType.END_CONSTRUCTION_WIZARD_EVENT, building);

			//if (mainScene != null)
			//	mainScene.unpauseSimulation();
        //} else {}

        //mgr.getResupply().deliverOthers();
        //2016-01-12 Needed to get back to the original thread in Resupply.java that started the instance
        //Simulation.instance().getMasterClock().getClockListenerExecutor().submit(new DeliverOthersTask(mgr));

	}


	//2016-01-12 Added DeliverOthersTask
	class DeliverOthersTask implements Runnable {
		BuildingManager mgr;
		DeliverOthersTask(BuildingManager mgr) {
			this.mgr = mgr;
		}

		public void run() {
			logger.info("DeliverOthersTask's run() is in " + Thread.currentThread().getName() + " Thread");
	       	// 2015-11-12 Deliver the rest of the supplies and add people.
	        mgr.getResupply().deliverOthers();
		}
    }


    /**
     * Checks if the prescribed template position for a building has obstacles and if it does, gets a new template position
     * @param template the position of the proposed building
     * @param checkVehicle if it has checked/moved the vehicle already
     */
    // 2015-12-06 Added checkTemplatePosition()
    public void checkTemplatePosition(BuildingManager mgr, BuildingTemplate template, boolean defaultPosition) {

        // Replace width and length defaults to deal with variable width and length buildings.
        double width = SimulationConfig.instance().getBuildingConfiguration().getWidth(template.getBuildingType());
        if (template.getWidth() > 0D) {
            width = template.getWidth();
        }
        if (width <= 0D) {
            width = DEFAULT_VARIABLE_BUILDING_WIDTH;
            //System.out.println("TransportWizard : set width to " + DEFAULT_VARIABLE_BUILDING_WIDTH);
        }

        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(template.getBuildingType());
        if (template.getLength() > 0D) {
            length = template.getLength();
        }
        if (length <= 0D) {
            length = DEFAULT_VARIABLE_BUILDING_LENGTH;
            //System.out.println("TransportWizard : set length to " + DEFAULT_VARIABLE_BUILDING_LENGTH);
        }

        int buildingID = mgr.getUniqueBuildingIDNumber();
        // 2015-12-13 Added buildingTypeID
        int buildingTypeID = mgr.getNextBuildingTypeID(template.getBuildingType());

        int scenarioID = mgr.getSettlement().getID();
        String scenario = getCharForNumber(scenarioID + 1);
        //buildingNickName = template.getBuildingType() + " " + scenario + buildingID;
        buildingNickName = template.getBuildingType() + " " + buildingTypeID;

    	// obtain the same template with a new nickname for the building
     	BuildingTemplate newT = new BuildingTemplate(template.getMissionName(),
     			buildingID, scenario, template.getBuildingType(), buildingNickName,
     			width, length, template.getXLoc(), template.getYLoc(), template.getFacing());
		//System.out.println("inside checkTemplatePosition(), calling checkTemplateAddBuilding() now ");
     	// 2015-12-08 Added checkTemplateAddBuilding()
        pauseAndCheck(mgr, newT);
/*		// True if the template position is clear of obstacles (existing buildings/vehicles/construction sites)
        if (mgr.getResupply().checkBuildingTemplatePosition(correctedTemplate)) {
     	   //System.out.println("TransportWizard : resupply.checkBuildingTemplatePosition(template) is true");
     	   confirmBuildingLocation(mgr, correctedTemplate, defaultPosition);
        } else {
     	    // check if a vehicle is the obstacle and move it
            boolean somethingStillBlocking = checkObstacleMoveVehicle(correctedTemplate);
            if (somethingStillBlocking) {
              	System.out.println("TransportWizard : somethingStillBlocking is true");
            	BuildingTemplate repositionedTemplate = mgr.getResupply().positionNewResupplyBuilding(template.getBuildingType());
             	checkTemplatePosition(mgr, repositionedTemplate, false);
            } else {
             	System.out.println("TransportWizard : somethingStillBlocking is false");
            	confirmBuildingLocation(mgr, correctedTemplate, defaultPosition);
            }
        } // end of else {
*/
    }


    /**
     * Checks for collision with existing buildings/vehicles/construction sites
     * and creates the building based on the template to the settlement
     * @param correctedTemplate
     */
    // 2015-12-07 Added checkTemplateAddBuilding()
    public synchronized void pauseAndCheck(BuildingManager mgr, BuildingTemplate correctedTemplate) {
    	//System.out.println("inside checkTemplateAddBuilding()");

    	boolean previous0 = Simulation.instance().getMasterClock().isPaused();

		// Pause simulation.
		if (mainScene != null) {
			if (!previous0) {
				mainScene.pauseSimulation();
				//System.out.println("previous0 is false. Paused sim");
			}
			desktop.getTimeWindow().enablePauseButton(false);
		}

    	// Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
    	boolean checking = checkBuildingTemplatePosition(mgr, correctedTemplate);
    	//System.out.println("checking is " + checking);
		if (checking) {
			createDialog(mgr, correctedTemplate, true, false);
			//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, true)");

		} else {
			BuildingTemplate newT = clearCollision(correctedTemplate, mgr);
			//System.out.println("inside checkTemplateAddBuilding(), just got newT");
			createDialog(mgr, newT, false, true);
			//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, false)");
		}

		if (mainScene != null) {
			unpause(previous0);
		}

    }


    /**
     * Checks if a building template's position is clear of collisions with any existing structures.
     * @param template the building template.
     * @return true if building template position is clear.
     */
    public boolean checkBuildingTemplatePosition(BuildingManager mgr, BuildingTemplate template) {
  		//System.out.println("inside checkBuildingTemplatePosition()");

        boolean result = true;

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

        result = mgr.checkIfNewBuildingLocationOpen(template.getXLoc(),
                template.getYLoc(), width, length, template.getFacing());
  		//System.out.println("inside checkBuildingTemplatePosition(), done calling mgr.checkIfNewBuildingLocationOpen()");
        return result;
    }


    /**
     * Identifies the type of collision and gets new template if the collision is immovable
     * @param correctedTemplate
     * @return BuildingTemplate
     */
    // 2015-12-07 Added clearCollision()
    public BuildingTemplate clearCollision(BuildingTemplate correctedTemplate, BuildingManager mgr) {
		//System.out.println("inside clearCollision()");

    	boolean noVehicle = true;
    	// check if a vehicle is the obstacle and move it
    	noVehicle = checkCollisionMoveVehicle(correctedTemplate, mgr);
		//System.out.println("noVehicle is now " + noVehicle);

	  	boolean noImmovable = true;
		noImmovable = checkCollisionImmovable(correctedTemplate, mgr);
		//System.out.println("noImmovable is now " + noImmovable);

		if (!noImmovable) {
			BuildingTemplate newT = mgr.getResupply().positionNewResupplyBuilding(correctedTemplate.getBuildingType());
    		//System.out.println("inside clearCollision(), just got newT");
			// 2015-12-16 Added setMissionName()
    		newT.setMissionName(correctedTemplate.getMissionName());
			// Call again recursively to check for any collision
			correctedTemplate = clearCollision(newT, mgr);
		}

		return correctedTemplate;
    }

    /**
     * Checks for collision and relocate any vehicles if found
     * @param xLoc
     * @param yLoc
     * @param coordinates
     * @return true if the location is clear of collision
     */
    // 2015-12-07 Added checkCollisionMoveVehicle()
    public boolean checkCollisionMoveVehicle(BuildingTemplate t, BuildingManager mgr) {

    	double xLoc = t.getXLoc();
    	double yLoc = t.getYLoc();
    	double w = t.getWidth();
		double l = t.getLength();
		double f = t.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

        boolean collison = LocalAreaUtil.checkVehicleBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);
        return !collison;

    }


    // 2015-12-07 Added checkCollisionImmovable()
    public boolean checkCollisionImmovable(BuildingTemplate t, BuildingManager mgr) {

    	double xLoc = t.getXLoc();
    	double yLoc = t.getYLoc();
    	double w = t.getWidth();
		double l = t.getLength();
		double f = t.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		boolean collison = LocalAreaUtil.checkImmovableBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates());
        //boolean noCollison = LocalAreaUtil.checkImmovableCollision(t.getXLoc(), t.getYLoc(), settlement.getCoordinates());

        return !collison;
    }


    /**
     * Check if the obstacle is a vehicle, if it is a vehicle, move it elsewhere
     * @param template the position of the proposed building
     * @return true if something else is still blocking

    // 2015-12-08 Replaced with using LocalAreaUtil.checkVehicleBoundedOjectIntersected()
    // and checkImmovableBoundedOjectIntersected() for better accuracy and less buggy collision checking
    public boolean checkObstacleMoveVehicle(BuildingTemplate template){

		boolean isSomethingElseBlocking = false;
		boolean quit = false;

		// Note: why using the do while loop ? could there be more than one unit at a location ? a person, a construction site and a vehicle?
		do {
			Unit unit = mapPanel.selectVehicleAsObstacle(template.getXLoc(), template.getYLoc());
			if (unit == null) {
				// no obstacle is found
				isSomethingElseBlocking = false;
				quit = true;
				//System.out.println("TranportWizard : unit is null");
			} else if (unit != null) {
				//System.out.println("TranportWizard : unit is NOT null");
				if (unit instanceof Vehicle) {
					//Vehicle vehicle = mapPanel.selectVehicleAt(0, 0);
					Vehicle vehicle = (Vehicle) unit;
					//System.out.println("TranportWizard : calling vehicle.determinedSettlementParkedLocationAndFacing() ");
					// move the vehicle elsewhere
					vehicle.determinedSettlementParkedLocationAndFacing();
					isSomethingElseBlocking = false;
				} else {
					// something is blocking it but it's NOT a vehicle
					isSomethingElseBlocking = true;
					quit = true;
				}
			}

		} while (!quit);

		return isSomethingElseBlocking;
    }
*/

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
     * @param template the position of the proposed building
     * @param buildingManager
     * @param isAtPreDefinedLocation
     */
	public synchronized void createDialog(BuildingManager mgr, BuildingTemplate template,
			boolean isAtPreDefinedLocation, boolean isNewTemplate) {
		//System.out.println("inside confirmBuildingLocation");
		//Building newBuilding = mgr.addOneBuilding(template, mgr.getResupply(), true);
		Building newBuilding = null;

		if (isAtPreDefinedLocation || isNewTemplate) {
			newBuilding = mgr.prepareToAddBuilding(template, mgr.getResupply(), true);
		}

        // Determine location and facing for the new building.
		double xLoc = newBuilding.getXLocation();
		double yLoc = newBuilding.getYLocation();
		double scale = mapPanel.getScale();

		Settlement currentS = settlementWindow.getMapPanel().getSettlement();
		if (currentS != mgr.getSettlement()) {
			settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(mgr.getSettlement());
		}
  		// set up the Settlement Map Tool to display the suggested location of the building
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		//mapPanel.setShowBuildingLabels(true);
		//mapPanel.getSettlementTransparentPanel().getBuildingLabelMenuItem().setSelected(true);


		String header = null;
		String title = null;
		String message = "(1) Will default to \"Yes\" in 30 secs unless timer is cancelled."
    			+ " (2) To manually place a site, click on \"Use Mouse/Keyboard Control\" button ";
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

		} else {

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
				createDialog(mgr, template, false, true);
			}

			desktop.disposeAnnouncementWindow();
        }
	}


	@SuppressWarnings("restriction")
	public synchronized void alertDialog(String title, String header, StringProperty msg, BuildingTemplate template,
		BuildingManager mgr, Building newBuilding, boolean hasTimer){

    	// Platform.runLater(() -> {
		// FXUtilities.runAndWait(() -> {

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
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancelTimer, buttonTypeMouseKB);

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
		        			+ " (2) To manually place a building, use Mouse/Keyboard Control.");
				});
			}
			else {
				msg.set("Note: To manually place a building, use Mouse/Keyboard Control.");
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeMouseKB);
			}

			if (newBuilding.getBuildingType().equals("Hallway")
					|| newBuilding.getBuildingType().equals("Tunnel")) {
				Button button = (Button) alert.getDialogPane().lookupButton(buttonTypeMouseKB);
				button.setVisible(false);
			}

			Optional<ButtonType> result = null;

			result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeYes) {
				logger.info(newBuilding.toString() + " from " + template.getMissionName()
			 	+ " is put in place in " + mgr.getSettlement());
				newBuilding.setInTransport(false);

			} else if (result.isPresent() && result.get() == buttonTypeNo) {
		    	mgr.removeBuilding(newBuilding);
		    	System.out.println("just removing building");
		    	BuildingTemplate repositionedTemplate = mgr.getResupply().positionNewResupplyBuilding(template.getBuildingType());
		    	//System.out.println("obtain new repositionedTemplate");
		    	// 2015-12-16 Added setMissionName()
				repositionedTemplate.setMissionName(template.getMissionName());
				//System.out.println("just called setMissionName()");\
				pauseAndCheck(mgr, repositionedTemplate);
				//checkTemplatePosition(mgr, repositionedTemplate, false);
				//System.out.println("done calling checkTemplatePosition()");


			} else if (result.isPresent() && result.get() == buttonTypeMouseKB) {
				placementDialog(title, header, newBuilding, mgr);

			} else if (hasTimer && result.isPresent() && result.get() == buttonTypeCancelTimer) {
				timer.stop();
				alertDialog(title, header, msg, template, mgr, newBuilding, false);
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
	public void placementDialog(String title, String header, Building newBuilding, BuildingManager mgr) {
    	
		//SwingUtilities.invokeLater(() -> {
			mapPanel.setFocusable(true);
			mapPanel.requestFocusInWindow();
		//});

		// Platform.runLater(() -> {
		// FXUtilities.runAndWait(() -> {
			String msg = "Keyboard Control :\t(1) Highlight the Settlement Map Tool" + System.lineSeparator()
					+ "\t\t\t\t(2) Use w/a/s/d, num pad keys arrows to move" + System.lineSeparator()
					+ "\t\t\t\t(2) Press 'r' or 'f' to rotate 45 degrees clockwise" + System.lineSeparator()
					+ "   Mouse Control :\t(1) Press & Hold the left button on the building" + System.lineSeparator()
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

			
			//final KeyboardDetection kb = new KeyboardDetection(newBuilding, mgr);
			
			// 2016-03-08 Added keyboard mapping and key bindings
			for (KeyboardDirection dir : KeyboardDirection.values()) {
				enumMap.put(dir, Boolean.FALSE);
			}
			
			keyboardMap.put(java.awt.event.KeyEvent.VK_UP, KeyboardDirection.UP);
			keyboardMap.put(java.awt.event.KeyEvent.VK_DOWN, KeyboardDirection.DOWN);
			keyboardMap.put(java.awt.event.KeyEvent.VK_LEFT, KeyboardDirection.LEFT);
			keyboardMap.put(java.awt.event.KeyEvent.VK_RIGHT, KeyboardDirection.RIGHT);
			keyboardMap.put(java.awt.event.KeyEvent.VK_R, KeyboardDirection.TURN);

			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_UP, KeyboardDirection.UP);
			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_DOWN, KeyboardDirection.DOWN);
			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_LEFT, KeyboardDirection.LEFT);
			keyboardMap.put(java.awt.event.KeyEvent.VK_KP_RIGHT, KeyboardDirection.RIGHT);
			keyboardMap.put(java.awt.event.KeyEvent.VK_F, KeyboardDirection.TURN);

			setKeyBindings();			
			animationTimer = new javax.swing.Timer(ANIMATION_DELAY, new AnimationListener(newBuilding, mgr));
			animationTimer.start();

			
			final MouseDetection md = new MouseDetection(newBuilding, mgr);

			SwingUtilities.invokeLater(() -> {

				mapPanel.setFocusable(true);
				mapPanel.requestFocusInWindow();

				//mapPanel.addKeyListener(kb);

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
							
						    // Check for collision here
						    boolean ok1 = checkCollisionMoveVehicle(newBuilding, mgr);
						    boolean ok2 = checkCollisionImmovable(newBuilding, mgr);
						    if (ok1 && ok2) 							
						    	moveNewBuildingTo(newBuilding, evt.getX(), evt.getY());
						}
						mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

				});
			});

			Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeConfirm) {
				newBuilding.setInTransport(false);
				//mapPanel.removeKeyListener(kb);
				removeKeyBindings();
				animationTimer.stop();
				mapPanel.removeMouseMotionListener(md);
			}

	}

	// 2016-03-08 Added setKeyBindings()
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

	// 2016-03-08 Added removeKeyBindings()
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
	
	// 2016-03-08 KeyBindingsAction class
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
    					  //boolean ok1 = checkCollisionMoveVehicle(b, mgr);
    					  boolean ok2 = checkCollisionImmovable(b, mgr);
    					  
    					  if (ok2) {
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
		   
	// 2015-12-25 Added MouseDetection
	class MouseDetection implements MouseMotionListener{
		private Building newBuilding;
		private BuildingManager mgr;

		MouseDetection(Building newBuilding, BuildingManager mgr) {
			this.newBuilding = newBuilding;
			this.mgr = mgr;
		}

		@Override
		public void mouseDragged(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			    // Check for collision here
			    boolean ok1 = checkCollisionMoveVehicle(newBuilding, mgr);
			    boolean ok2 = checkCollisionImmovable(newBuilding, mgr);
			    if (ok1 && ok2) 			
			    	moveNewBuildingTo(newBuilding,  evt.getX(), evt.getY());
			}
		}

		@Override
		public void mouseMoved(MouseEvent evt) {
			if (evt.getButton() == MouseEvent.BUTTON1) {
				mapPanel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			    // Check for collision here
			    boolean ok1 = checkCollisionMoveVehicle(newBuilding, mgr);
			    boolean ok2 = checkCollisionImmovable(newBuilding, mgr);
			    if (ok1 && ok2) 			
			    	moveNewBuildingTo(newBuilding,  evt.getX(), evt.getY());
			}
		}
	}

	// 2015-12-25 Added KeyboardDetection
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
		    boolean ok1 = checkCollisionMoveVehicle(newBuilding, mgr);
		    boolean ok2 = checkCollisionImmovable(newBuilding, mgr);
		    if (ok1 && ok2) 
		    	handleKeyboardInput(newBuilding, c);
		    mapPanel.repaint();
			e.consume();
		}

		@Override
		public void keyTyped(java.awt.event.KeyEvent e) {
			// TODO Auto-generated method stub
			e.consume();
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent e) {
		    int c = e.getKeyCode();
		    //System.out.println("c is " + c);
		    // Check for collision here
		    boolean ok1 = checkCollisionMoveVehicle(newBuilding, mgr);
		    boolean ok2 = checkCollisionImmovable(newBuilding, mgr);
		    if (ok1 && ok2) 
		    	handleKeyboardInput(newBuilding, c);
		    mapPanel.repaint();
			e.consume();
		}
	}

	/**
	 * Moves the site to a new position via the mouse's right drag
	 * @param b
	 * @param xPixel
	 * @param yPixel
	 */
	// 2015-12-26 Added moveNewBuildingAt()
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

	/**
	 * Sets the new x and y location and facing of the site
	 * @param b
	 * @param c
	 */
	// 2016-03-08 Renamed to handleKeyboardInput()
	public void handleKeyboardInput(Building b, int c) {
		int facing = (int) b.getFacing();
		double x = b.getXLocation();
		double y = b.getYLocation();

	    if (c == java.awt.event.KeyEvent.VK_UP // 38
	    	|| c == java.awt.event.KeyEvent.VK_KP_UP
	    	|| c == java.awt.event.KeyEvent.VK_W
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD8) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("up");
			b.setYLocation(y + 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_DOWN // 40
	    	|| c == java.awt.event.KeyEvent.VK_KP_DOWN	    	
	    	|| c == java.awt.event.KeyEvent.VK_S
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD2) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("down");
			b.setYLocation(y - 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_LEFT // 37
	    	|| c == java.awt.event.KeyEvent.VK_KP_LEFT
	    	|| c == java.awt.event.KeyEvent.VK_A
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD4) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("left");
			b.setXLocation(x + 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_RIGHT // 39
	    	|| c == java.awt.event.KeyEvent.VK_KP_RIGHT
	    	|| c == java.awt.event.KeyEvent.VK_W
	    	|| c == java.awt.event.KeyEvent.VK_NUMPAD8) {
	    	//System.out.println("x : " + x + "  y : " + y);
	    	//System.out.println("right");
			b.setXLocation(x - 1);
	    	//System.out.println("x : " + s.getXLocation() + "  y : " + s.getYLocation());
	    } else if(c == java.awt.event.KeyEvent.VK_R
	    	|| c == java.awt.event.KeyEvent.VK_F) {
	    	//System.out.println("f : " + facing);
	    	//System.out.println("turn 90");
	    	facing = facing + 45;
	    	if (facing >= 360)
	    		facing = facing - 360;
	    	b.setFacing(facing);
	    	//System.out.println("f : " + s.getFacing());
	    }

	}

    /**
     * Checks for collision and relocate any vehicles if found
     * @param xLoc
     * @param yLoc
     * @param coordinates
     * @return true if the location is clear of collision
     */
    // 2015-12-07 Added checkCollisionMoveVehicle()
    public boolean checkCollisionMoveVehicle(Building b, BuildingManager mgr) {

    	double xLoc = b.getXLocation();
    	double yLoc = b.getYLocation();
    	double w = b.getWidth();
		double l = b.getLength();
		double f = b.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		// true if it doesn't collide
        boolean col = LocalAreaUtil.checkVehicleBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);
        return !col;
    }


    // 2015-12-07 Added checkCollisionImmovable()
    public boolean checkCollisionImmovable(Building b, BuildingManager mgr) {

    	double xLoc = b.getXLocation();
    	double yLoc = b.getYLocation();
    	double w = b.getWidth();
		double l = b.getLength();
		double f = b.getFacing();

		BoundedObject boundedObject = new BoundedObject(xLoc, yLoc, w, l, f);

		// true if it doesn't collide
		boolean col = LocalAreaUtil.checkImmovableBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates());
        //boolean noCollison = LocalAreaUtil.checkImmovableCollision(t.getXLoc(), t.getYLoc(), settlement.getCoordinates());
        return !col;
    }
	
	
	/**
	 * Compares and sorts a list of BuildingTemplates according to its building id
	 */
    // 2015-12-19 Added ComparatorOfBuildingID()
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