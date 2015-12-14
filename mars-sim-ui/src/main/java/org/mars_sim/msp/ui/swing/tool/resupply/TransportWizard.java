/**
 * Mars Simulation Project
 * TransportWizard.java
 * @version 3.08 2015-12-07
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.JOptionPane;

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
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Button;
import javafx.stage.Modality;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
    private final static String TITLE = "Transport Wizard";

	private String buildingNickName;

	private BuildingManager mgr;
	private MainDesktopPane desktop;
	//private Settlement settlement;
	private SettlementWindow settlementWindow;
	private SettlementMapPanel mapPanel;
	//private Resupply resupply;
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
		settlementWindow = desktop.getSettlementWindow();
		mapPanel = settlementWindow.getMapPanel();
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
    public void deliverBuildings(BuildingManager mgr) {
		//System.out.println("Just called TransportWizard's deliverBuildings()");

   		// TODO: Account for the case when the building is not from the default MD Phase 1 Resupply Mission
    	// how to make each building ask for a position ?

        if (mainScene != null) {
        	// Note: make sure pauseSimulation() doesn't interfere with resupply.deliverOthers();
           	mainScene.pauseSimulation();
           	askDefaultPosition(mgr);
    	   	//mainScene.unpauseSimulation();
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
     */
	// 2015-12-07 Added askDefaultPosition()
    @SuppressWarnings("restriction")
	public synchronized void askDefaultPosition(BuildingManager mgr) {
    	String message = "Use default positions for all arriving buildings in " + mgr.getSettlement() + "?";
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
		 		if (mainScene != null) mainScene.unpauseSimulation();
		    	 mgr.getResupply().deliverBuildings();
		    	 logger.info("All buildings are put in place at default positions at " + mgr.getSettlement());
		     }
		     else if (response == buttonTypeNo) {
		 		if (mainScene != null) mainScene.unpauseSimulation();
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
            System.out.println("TransportWizard : set width to " + DEFAULT_VARIABLE_BUILDING_WIDTH);
        }

        double length = SimulationConfig.instance().getBuildingConfiguration().getLength(template.getBuildingType());
        if (template.getLength() > 0D) {
            length = template.getLength();
        }
        if (length <= 0D) {
            length = DEFAULT_VARIABLE_BUILDING_LENGTH;
            System.out.println("TransportWizard : set length to " + DEFAULT_VARIABLE_BUILDING_LENGTH);
        }

        int buildingID = mgr.getUniqueBuildingIDNumber();
        // 2015-12-13 Added buildingTypeID
        int buildingTypeID = mgr.getNextBuildingTypeID(template.getBuildingType());
        
        int scenarioID = mgr.getSettlement().getID();
        String scenario = getCharForNumber(scenarioID + 1);
        //buildingNickName = template.getBuildingType() + " " + scenario + buildingID;
        buildingNickName = template.getBuildingType() + " " + buildingID;

    	// obtain the same template with a new nickname for the building
     	BuildingTemplate correctedTemplate = new BuildingTemplate(buildingID, scenario, template.getBuildingType(), buildingNickName, width,
                 length, template.getXLoc(), template.getYLoc(), template.getFacing());

     	// 2015-12-08 Added checkTemplateAddBuilding()
        checkTemplateAddBuilding(mgr, correctedTemplate);

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
        // Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
        if (mgr.getResupply().checkBuildingTemplatePosition(correctedTemplate)) {
        	confirmBuildingLocation(mgr, correctedTemplate, true);
        } else {
        	correctedTemplate = mgr.getResupply().clearCollision(correctedTemplate);
        	confirmBuildingLocation(mgr, correctedTemplate, false);
        }
    }

    /**
     * Check if the obstacle is a vehicle, if it is a vehicle, move it elsewhere
     * @param template the position of the proposed building
     * @return true if something else is still blocking
     */
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

		Building newBuilding ;
	    //final int TIME_OUT = 20;
	    //int count = TIME_OUT;
	    //pauseTimer = new Timer();
		// Hold off 10 seconds
		//int seconds = 10;

		newBuilding = mgr.getSettlement().getBuildingManager().addOneBuilding(template, mgr.getResupply(), true);

        // Determine location and facing for the new building.
  		// set up the Settlement Map Tool to display the suggested location of the building
		double xLoc = newBuilding.getXLocation();
		double yLoc = newBuilding.getYLocation();
		double scale = mapPanel.getScale();
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		mapPanel.setShowBuildingLabels(true);

		String message = null;
		if (isAtPreDefinedLocation)
			message = "Would you like to place " + buildingNickName + " at this default position?";
		else
			message = "Would you like to place " + buildingNickName + " at this new position?";

        if (mainScene != null) {
	    	//mainScene.pauseSimulation();
        	Alert alert = new Alert(AlertType.CONFIRMATION);
   			alert.initOwner(mainScene.getStage());
        	alert.initModality(Modality.NONE); // users can zoom in/out, move around the settlement map and move a vehicle elsewhere
   			double x = mainScene.getStage().getWidth();
   			double y = mainScene.getStage().getHeight();
   			double xx = alert.getDialogPane().getWidth();
   			double yy = alert.getDialogPane().getHeight();
   			alert.setX((x - xx)/2);
   			alert.setY((y - yy)*3/4);
   			alert.setTitle(TITLE);
			alert.setHeaderText("Confirm a building's position");
			alert.setContentText(message);
			//DialogPane dialogPane = alert.getDialogPane();

			ButtonType buttonTypeYes = new ButtonType("Yes");
			ButtonType buttonTypeNo = new ButtonType("No");
			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

			alert.showAndWait().ifPresent(response -> {
			     if (response == buttonTypeYes) {
			    	 logger.info(newBuilding.toString() + " is put in place");
			    	 //mainScene.unpauseSimulation();
			     }
			     else if (response == buttonTypeNo) {
			    	 mgr.removeBuilding(newBuilding);
			    	 BuildingTemplate repositionedTemplate = mgr.getResupply().positionNewResupplyBuilding(template.getBuildingType());
			    	 checkTemplatePosition(mgr, repositionedTemplate, false);
			    	 //mainScene.unpauseSimulation();
			     }

			});

			// 2015-10-15 Made "Enter" key to work like the space bar for firing the button on focus
			//buttonTypeYes.defaultButtonProperty().bind(startButton.focusedProperty());
			EventHandler<KeyEvent> fireOnEnter = event -> {
			    if (KeyCode.ENTER.equals(event.getCode())
			            && event.getTarget() instanceof Button) {
			        ((Button) event.getTarget()).fire();
			    }
			};
			//DialogPane dialogPane = alert.getDialogPane();
			alert.getButtonTypes().stream()
			        .map(alert.getDialogPane()::lookupButton)
			        .forEach(button ->
			                button.addEventHandler(
			                        KeyEvent.KEY_PRESSED,
			                        fireOnEnter
			                )
			        );

/*
        		      .filter(response -> response == buttonTypeYes)
        		      .ifPresent(response ->  {
        		    	  logger.info("Building in Placed : " + newBuilding.toString());
        		      })
        		      .filter(response -> response == buttonTypeNo)
        		      .ifPresent(response -> {
        		    	  settlement.getBuildingManager().removeBuilding(newBuilding);
          				confirmBuildingLocation(template, false);
      		      });
*/
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

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		mgr = null;
		desktop = null;
		settlementWindow = null;
		mapPanel = null;
	}

}