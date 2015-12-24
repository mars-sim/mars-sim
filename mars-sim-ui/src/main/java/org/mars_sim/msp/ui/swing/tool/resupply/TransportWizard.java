/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.08 2015-12-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
import org.mars_sim.msp.core.structure.BuildingTemplate;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingManager;
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

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
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
import javafx.util.Duration;

import java.awt.MouseInfo;
import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
		settlementWindow = desktop.getSettlementWindow();
		//if (settlementWindow == null) System.out.println("settlementWindow is null");
		mapPanel = settlementWindow.getMapPanel();
		//if (mapPanel == null) System.out.println("mapPanel is null");
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}


	/**
     * Delivers buildings to the destination settlement.
     */
	// 2015-01-02 Added keyword synchronized to avoid JOption crash
    public synchronized void deliverBuildings(BuildingManager mgr) {
		//System.out.println("Just called TransportWizard's deliverBuildings()");

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

				    List<BuildingTemplate> templates = mgr.getResupply().orderNewBuildings();
				    BuildingTemplate aTemplate = templates.get(0);
					String missionName = aTemplate.getMissionName();

				   	//askDefaultPosition(mgr, missionName, previous0);
				   	//mainScene.unpauseSimulation();
					determineEachBuildingPosition(mgr);

				    unpause(previous0);

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
        mgr.getResupply().deliverOthers();
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
	public void determineEachBuildingPosition(BuildingManager mgr) {

		// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
		if (mainScene != null)
			mainScene.pauseSimulation();

        List<BuildingTemplate> orderedBuildings = mgr.getResupply().orderNewBuildings();
        // 2014-12-23 Added sorting orderedBuildings according to its building id
        //Collections.sort(orderedBuildings);
        //Collections.shuffle(orderedBuildings);

        // 2015-12-19 Added the use of ComparatorOfBuildingID()
        Collections.sort(orderedBuildings, new ComparatorOfBuildingID());

        Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();
        //int size = orderedBuildings.size();
        //int i = 0;

        while (buildingI.hasNext()) {

           BuildingTemplate template = buildingI.next();
           //System.out.println("TransportWizard : BuildingTemplate for " + template.getNickName());

           // TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission


/*    	   // check if it's a building connector and if it's connecting the two buildings at their template position
        	   boolean isConnector = buildingConfig.hasBuildingConnection(template.getBuildingType());
           if (isConnector)
        	   confirmBuildingLocation(correctedTemplate, false);
           else
        	   confirmBuildingLocation(correctedTemplate, true);
*/

           // 2015-12-06 Added this recursive method checkTemplatePosition()
           // to handle the creation of a new building template in case of an obstacle.
           checkTemplatePosition(mgr, template, true);

	    } // end of while (buildingI.hasNext())

        Building building = mgr.getBuildings().get(0);
        mgr.getSettlement().fireUnitUpdate(UnitEventType.FINISH_BUILDING_PLACEMENT_EVENT, building);

		// 2015-10-17 Check if it was previously on pause mode
		//if (isOnPauseMode) {
		//	mainScene.pauseSimulation();
		//}
		//else
		//	mainScene.unpauseSimulation(); // Note: this can take away any previous announcement window

		if (mainScene != null)
			mainScene.unpauseSimulation();

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
        checkTemplateAddBuilding(mgr, newT);

/*
        // True if the template position is clear of obstacles (existing buildings/vehicles/construction sites)
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
    public void checkTemplateAddBuilding(BuildingManager mgr, BuildingTemplate correctedTemplate) {
    	//System.out.println("inside checkTemplateAddBuilding()");
        // Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
    	boolean checking = checkBuildingTemplatePosition(mgr, correctedTemplate);

    	//System.out.println("checking is " + checking);

        if (checking) {
        	confirmBuildingLocation(mgr, correctedTemplate, true);
    		//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, true)");

        } else {
        	BuildingTemplate newT = clearCollision(correctedTemplate, mgr);
    		//System.out.println("inside checkTemplateAddBuilding(), just got newT");
        	confirmBuildingLocation(mgr, newT, false);
    		//System.out.println("inside checkTemplateAddBuilding(), done calling confirmBuildingLocation(mgr, correctedTemplate, false)");

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

		boolean collison = LocalAreaUtil.checkImmovableBoundedOjectIntersected(boundedObject, mgr.getSettlement().getCoordinates(), true);
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
	public synchronized void confirmBuildingLocation(BuildingManager mgr, BuildingTemplate template, boolean isAtPreDefinedLocation) {
		//System.out.println("inside confirmBuildingLocation");

		Building newBuilding = mgr.addOneBuilding(template, mgr.getResupply(), true);

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
		mapPanel.setShowBuildingLabels(true);

		String header = null;
		String title = null;
		String message = null;

		if (isAtPreDefinedLocation) {
			header = "Would you like to place " + buildingNickName + " at its default position? ";
		}
		else {
			header = "Would you like to place " + buildingNickName + " at this position? ";
		}

		message = "Note: unless timer is cancelled, default to \"Yes\" in 30 secs";

		String missionName = template.getMissionName();

        if (missionName != null)
		//if (missionName.equals("null"))
			title = template.getMissionName() + " at " + mgr.getSettlement();
        else
        	title = "A Resupply Transport" + " at " + mgr.getSettlement();

		StringProperty msg = new SimpleStringProperty(message);
		//Timer timer = null;

        if (mainScene != null) {
    		//System.out.println("inside confirmBuildingLocation, calling alertDialog");
        	alertDialog(title, header, msg, template, mgr, newBuilding, true);//, timer);

		} else {

	        desktop.openAnnouncementWindow("Pause for Building Transport and Confirm Location");
	        AnnouncementWindow aw = desktop.getAnnouncementWindow();
	        Point location = MouseInfo.getPointerInfo().getLocation();
	        double Xloc = location.getX() - aw.getWidth() * 2;
			double Yloc = location.getX() - aw.getHeight() * 2;
			aw.setLocation((int)Xloc, (int)Yloc);

			int reply = JOptionPane.showConfirmDialog(aw, message, TITLE, JOptionPane.YES_NO_OPTION);
			//repaint();

			if (reply == JOptionPane.YES_OPTION) {
	            logger.info("Building in Place : " + newBuilding.toString());
			}
			else {
				mgr.removeBuilding(newBuilding);
				confirmBuildingLocation(mgr, template, false);
			}

        }
	}


	@SuppressWarnings("restriction")
	public void alertDialog(String title, String header, StringProperty msg, BuildingTemplate template,
		BuildingManager mgr, Building newBuilding, boolean hasTimer){

    	// Platform.runLater(() -> {
		// FXUtilities.runAndWait(() -> {

			Alert alert = new Alert(AlertType.CONFIRMATION);
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
			ButtonType buttonTypeCancelTimer = null;
			if (hasTimer) {
				buttonTypeCancelTimer = new ButtonType("Cancel Timer");
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancelTimer);
			}
			else
				alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

			Optional<ButtonType> result = null;

			IntegerProperty i = new SimpleIntegerProperty(wait_time_in_secs);
			// 2015-12-19 Added ReactFX's Timer and FxTimer
			Timer timer = FxTimer.runPeriodically(java.time.Duration.ofMillis(1000), () -> {
	        	int num = i.get() - 1;
	            i.set(num);
	        	//System.out.println(num);
	        	if (num == 0) {
	        		Button button = (Button) alert.getDialogPane().lookupButton(buttonTypeYes);
	        	    button.fire();
	        	}
	        	msg.set("Note: unless timer is cancelled, "
	        			+ "default to \"Yes\" in " + num + " secs");
			});

			result = alert.showAndWait();

			if (result.isPresent() && result.get() == buttonTypeYes) {
				logger.info(newBuilding.toString() + " from " + template.getMissionName()
			 	+ " is put in place in " + mgr.getSettlement());

			} else if (result.isPresent() && result.get() == buttonTypeNo) {
		    	mgr.removeBuilding(newBuilding);
		    	//System.out.println("just removing building");
		    	BuildingTemplate repositionedTemplate = mgr.getResupply().positionNewResupplyBuilding(template.getBuildingType());
		    	//System.out.println("obtain new repositionedTemplate");
		    	// 2015-12-16 Added setMissionName()
				repositionedTemplate.setMissionName(template.getMissionName());
				//System.out.println("just called setMissionName()");
				checkTemplatePosition(mgr, repositionedTemplate, false);
				//System.out.println("done calling checkTemplatePosition()");

			}

			else if (result.isPresent() && result.get() == buttonTypeCancelTimer) {
				timer.stop();
				alertDialog(title, header, msg, template, mgr, newBuilding, false);

			}

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