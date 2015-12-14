/**
 * Mars Simulation Project
 * ConstructionWizard.java
 * @version 3.08 2015-04-06
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.JOptionPane;

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
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.AnnouncementWindow;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
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
import java.util.Iterator;
import java.util.List;
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

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 10D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 10D;
    private final static String TITLE = "Transport Wizard";

	private String buildingNickName;

	private BuildingManager mgr;
	private MainDesktopPane desktop;
	private Settlement settlement;
	private SettlementWindow settlementWindow;
	private SettlementMapPanel mapPanel;
	private Resupply resupply;
	private MainScene mainScene;
	private BuildingConfig buildingConfig;


	/**
	 * Constructor 1.
	 * For non-javaFX UI
	 * @param desktop the main desktop pane.
	 */
	public ConstructionWizard(final MainDesktopPane desktop) {
		this.desktop = desktop;
		mainScene = desktop.getMainScene();
		settlementWindow = desktop.getSettlementWindow();
		mapPanel = settlementWindow.getMapPanel();
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	/**
	 * Constructor 2.
	 * For JavaFX UI
	 * @param mainScene the main scene
	 */
	public ConstructionWizard(final MainScene mainScene, MainDesktopPane desktop) {
		this.desktop = desktop;
		this.mainScene = mainScene;
		//if (desktop == null) 	System.out.println("desktop is null");
		settlementWindow = desktop.getSettlementWindow();
		//if (settlementWindow == null) System.out.println("settlementWindow is null");
		mapPanel = settlementWindow.getMapPanel();
		//if (mapPanel == null) System.out.println("mapPanel is null");
		buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	public void createGUI(Building newBuilding) {
		settlement = newBuilding.getBuildingManager().getSettlement();
	}

	public void initialize(BuildingManager mgr) { //, SettlementWindow settlementWindow) {
		this.mgr = mgr;
		this.settlement = mgr.getSettlement();
		this.resupply = mgr.getResupply();
	}


    /**
     * Creates a new building from the construction site.
     * @param manager the settlement's building manager.
     * @return newly constructed building.
     * @throws Exception if error constructing building.
     
    public Building createBuilding(BuildingManager manager) {
        if (buildingStage == null) throw new IllegalStateException("Building stage doesn't exist");

        // 2014-10-27 Added uniqueName
        int id = manager.getUniqueBuildingIDNumber();
        String buildingType = buildingStage.getInfo().getName();
        String uniqueName = manager.getBuildingNickName(buildingType);
        
        Building newBuilding = new Building(id, buildingType, uniqueName, width, length,
                xLocation, yLocation, facing, manager);
        manager.addBuilding(newBuilding, true);

        // Record completed building name.
        ConstructionManager constructionManager = manager.getSettlement().getConstructionManager();
        MarsClock timeStamp = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
        constructionManager.addConstructedBuildingLogEntry(buildingStage.getInfo().getName(), timeStamp);

        // Clear construction value cache.
        constructionManager.getConstructionValues().clearCache();

        // Fire construction event.
        fireConstructionUpdate(CREATE_BUILDING_EVENT, newBuilding);

        return newBuilding;
    }
    */
	
	/**
     * Delivers supplies to the destination settlement.
     */
	// 2015-01-02 Added keyword synchronized to avoid JOption crash
    public synchronized void deliverBuildings() {
        List<BuildingTemplate> orderedBuildings = resupply.orderNewBuildings();
        // 2014-12-23 Added sorting orderedBuildings according to its building id
        //Collections.sort(orderedBuildings);
        Iterator<BuildingTemplate> buildingI = orderedBuildings.iterator();
        int size = orderedBuildings.size();
        int i = 0;

        while (buildingI.hasNext()) {
           BuildingTemplate template = buildingI.next();

           // Correct length and width in building template.
 
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

           int buildingID = settlement.getBuildingManager().getUniqueBuildingIDNumber();
           // 2015-12-13 Added buildingTypeID          
           int buildingTypeID = settlement.getBuildingManager().getNextBuildingTypeID(template.getBuildingType());
 
           // 2015-01-16 Added getScenario()
           int scenarioID = settlement.getID();
           String scenario = getCharForNumber(scenarioID + 1);
           //buildingNickName = template.getBuildingType() + " " + scenario + buildingID;
           buildingNickName = template.getBuildingType() + " " + buildingTypeID;

           
           BuildingTemplate correctedTemplate = new BuildingTemplate(buildingID, scenario, template.getBuildingType(), buildingNickName, width,
                   length, template.getXLoc(), template.getYLoc(), template.getFacing());

            // Check if building template position/facing collides with any existing buildings/vehicles/construction sites.
           //boolean hasObstacle = resupply.checkBuildingTemplatePosition(template);

           if (resupply.checkBuildingTemplatePosition(template)) {
        	   //System.out.println("TransportWizard : resupply.checkBuildingTemplatePosition(template) is true");
        	   confirmBuildingLocation(correctedTemplate, true);
        	   
        	   // check if it's a building connector and if it's connecting the two buildings at their template position 
/*        	   boolean isConnector = buildingConfig.hasBuildingConnection(template.getBuildingType());
        	   
               if (isConnector) {
            	   // let 
            	   confirmBuildingLocation(correctedTemplate, false);
            	   
               }
               else {
            	   
            	   confirmBuildingLocation(correctedTemplate, true);
               }
*/     
  

           } // end of if (checkBuildingTemplatePosition(template)) {

           else { // when the building is not from the default MD Phase 1 Resupply Mission (NO pre-made template is available)
            	   // or when the building's designated location has already been occupied
            	//System.out.println("TransportWizard : resupply.checkBuildingTemplatePosition(template) is false");
                moveVehicle(correctedTemplate);
                
                if (resupply.checkBuildingTemplatePosition(template))
                	
                	confirmBuildingLocation(correctedTemplate, true);
                else 
                	confirmBuildingLocation(correctedTemplate, false);
            	//confirmBuildingLocation(template, true);
            	//confirmBuildingLocation(correctedTemplate, true);
           } // end of else {

           //confirmBuildingLocation(correctedTemplate, true);
   		   // Move the blocking vehicle elsewhere (if any)

           i++;
           if (i == size) {
        	   // TODO: do we need to place each placed building into the fireUnitUpdate() ?
        	   Building randomBuilding = mgr.getBuildings().get(0);
        	   settlement.fireUnitUpdate(UnitEventType.FINISH_BUILDING_PLACEMENT_EVENT, randomBuilding);
	        }
           
           // move vehicle one more time just in case
           //moveVehicle(correctedTemplate);
	    } // end of while (buildingI.hasNext())
        
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


    /**
     * Asks user to confirm the location of the new building.
     * @param template
     * @param buildingManager
     * @param isAtPreDefinedLocation
     */
	public synchronized void confirmBuildingLocation(BuildingTemplate template, boolean isAtPreDefinedLocation) {

		// 2015-10-17 Save the current pause state
		boolean isOnPauseMode = Simulation.instance().getMasterClock().isPaused();
	
		BuildingTemplate positionedTemplate ; // should NOT be null
		Building newBuilding ;
	    //final int TIME_OUT = 20;
	    //int count = TIME_OUT;
	    //pauseTimer = new Timer();
		// Hold off 10 seconds
		//int seconds = 10;

         // Determine location and facing for the new building.
		if (isAtPreDefinedLocation) {

			//System.out.println("TranportWizard : isAtDefinedLocation is true ");
			//positionedTemplate = template;
			newBuilding = settlement.getBuildingManager().addOneBuilding(template, resupply, false);
		}

		else {
			//System.out.println("TranportWizard : isAtDefinedLocation is false ");
			// Reposition the building elsewhere
			positionedTemplate = resupply.positionNewResupplyBuilding(template.getBuildingType());
			//buildingManager.setBuildingArrived(true);
			// Define new position for this building
			newBuilding = settlement.getBuildingManager().addOneBuilding(positionedTemplate, resupply, false);
		}

		//createGUI(newBuilding);
  		// set settlement based on where this building is located
  		// important for MainDesktopPane to look up this settlement variable when placing/transporting building
  		settlement = newBuilding.getBuildingManager().getSettlement();

  		// set up the Settlement Map Tool to display the suggested location of the building
		double xLoc = newBuilding.getXLocation();
		double yLoc = newBuilding.getYLocation();
		double scale = mapPanel.getScale();
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		mapPanel.setShowBuildingLabels(true);

        String message = "Would you like to place " + buildingNickName + " at this location on the map?";

        if (mainScene != null) {

	    	mainScene.pauseSimulation();
	    	
        	Alert alert = new Alert(AlertType.CONFIRMATION);
   			//alert.initModality(Modality.APPLICATION_MODAL);
   			//alert.initModality(Modality.WINDOW_MODAL);
        	alert.initModality(Modality.NONE);
        	
   			alert.initOwner(mainScene.getStage());
   			double x = mainScene.getStage().getWidth();
   			double y = mainScene.getStage().getHeight();
   			double xx = alert.getDialogPane().getWidth();
   			double yy = alert.getDialogPane().getHeight();
   			alert.setX((x - xx)/2);
   			alert.setY((y - yy)*3/4);
   			alert.setTitle(TITLE);
			alert.setHeaderText("Confirm New Building's Location");
			alert.setContentText(message);
			//DialogPane dialogPane = alert.getDialogPane();

			ButtonType buttonTypeYes = new ButtonType("Yes");
			
			ButtonType buttonTypeNo = new ButtonType("No");

			alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
/*
        			Optional<ButtonType> result = alert.showAndWait(); // not show()
        			if (result.get() == buttonTypeYes){
        	            logger.info("Building in Placed : " + newBuilding.toString());
        			} else if (result.get() == buttonTypeNo) {
        				settlement.getBuildingManager().removeBuilding(newBuilding);
        				confirmBuildingLocation(template, false);
        			}
*/
			alert.showAndWait().ifPresent(response -> {
			     if (response == buttonTypeYes) {
			    	 logger.info(newBuilding.toString() + " is in-place");
			    	 mainScene.unpauseSimulation();
			     }
			     else if (response == buttonTypeNo) {
			    	 settlement.getBuildingManager().removeBuilding(newBuilding);
			    	 confirmBuildingLocation(template, false);
			    	 mainScene.unpauseSimulation();
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
  
		}

        else {

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
				settlement.getBuildingManager().removeBuilding(newBuilding);
				confirmBuildingLocation(template, false);
			}

        }
        
		// 2015-10-17 Check if it was previously on pause mode
		if (isOnPauseMode) {
			mainScene.pauseSimulation();
		}
		//else 
		//	mainScene.unpauseSimulation(); // Do NOT do this or it will take away any previous announcement window

	}



	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		mgr = null;
		desktop = null;
		settlement = null;
		settlementWindow = null;
		mapPanel = null;
		resupply = null;
	}

}