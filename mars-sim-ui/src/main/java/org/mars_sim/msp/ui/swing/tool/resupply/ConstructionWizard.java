/**
 * Mars Simulation Project
 * ConstructionWizard.java
 * @version 3.08 2015-12-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool.resupply;

import javax.swing.JOptionPane;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.interplanetary.transport.resupply.Resupply;
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

import java.awt.MouseInfo;
import java.awt.Point;
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
	private static final double SITE_PREPARE_TIME = 500D;

    // Default width and length for variable size buildings if not otherwise determined.
    private static final double DEFAULT_VARIABLE_BUILDING_WIDTH = 10D;
    private static final double DEFAULT_VARIABLE_BUILDING_LENGTH = 10D;

	// Default distance between buildings for construction.
	private static final double DEFAULT_INHABITABLE_BUILDING_DISTANCE = 5D;
	private static final double DEFAULT_NONINHABITABLE_BUILDING_DISTANCE = 2D;

    /** Minimum length of a building connector (meters). */
    private static final double MINIMUM_CONNECTOR_LENGTH = 1D;

    private final static String TITLE = "Transport Wizard";

    private static int wait_time_in_secs = 30; // in seconds

	//private ConstructionStage constructionStage;

	private MainDesktopPane desktop;
	private Settlement settlement;
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
		this.settlementWindow = desktop.getSettlementWindow();
		this.mapPanel = settlementWindow.getMapPanel();
		this.buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
	}

	public synchronized void selectSite(BuildingConstructionMission mission) {
		logger.info("ConstructionWizard's selectSite() is in " + Thread.currentThread().getName() + " Thread");
	    ConstructionSite constructionSite = mission.getConstructionSite();
	    settlement = constructionSite.getSettlement();
	    ConstructionManager constructionManager = settlement.getConstructionManager();

		// Select the relevant settlement
		desktop.openToolWindow(SettlementWindow.NAME);
		//System.out.println("Just open Settlement Map Tool");
	    settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox()
	    	.setSelectedItem(constructionManager.getSettlement());

	    ConstructionStageInfo stageInfo = constructionSite.getStageInfo();
	    ConstructionSite modifiedSite = null;
		boolean empty = mission.getConstructionSite().getEmpty();
	    boolean manual = mission.getConstructionSite().getManual();
	    //onstructionStageInfo stageInfo = null;

		if (manual) { // Case 1 : mars-simmer initiates this contruction task

			if (!empty) {
				//ConstructionSite
				modifiedSite = constructionSite;
				confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, 0);
			}
			else {
			    int constructionSkill = constructionSite.getSkill();

	        	    //System.out.println("selectSite() : Case 1 : stageInfo is " + stageInfo.toString());
	                //System.out.println(//"constructionSite is " + constructionSite.getDescription()
	        	    //	 "x is " + constructionSite.getXLocation()
	        	    //	+ "  y is " + constructionSite.getYLocation()
	                //	+ "  w is " + constructionSite.getWidth()
	                //	+ "  l is " + constructionSite.getLength()
	                //	+ "  f is " + constructionSite.getFacing());

			    boolean previous = Simulation.instance().getMasterClock().isPaused();
				if (!previous) {
					mainScene.pauseSimulation();
			    	//System.out.println("previous is false. Paused sim");
				}
				desktop.getTimeWindow().enablePauseButton(false);


					//FXUtilities.runAndWait(() -> {
					//Platform.runLater(() -> {
						//ConstructionSite
						modifiedSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
						confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);
					//});


				boolean now = Simulation.instance().getMasterClock().isPaused();
				if (!previous) {
					if (now) {
						mainScene.unpauseSimulation();
		   	    		//System.out.println("previous is false. now is true. Unpaused sim");
					}
				} else {
					if (!now) {
						mainScene.unpauseSimulation();
		   	    		//System.out.println("previous is true. now is false. Unpaused sim");
					}
				}
				desktop.getTimeWindow().enablePauseButton(true);
			}

            mission.init_2(modifiedSite, modifiedSite.getStageInfo());
            mission.setPhases_2();

           //System.out.println("# of sites : " + settlement.getConstructionManager().getConstructionSites().size());

		}
		else {
			// Case 2 : msp initiates this construction task.

		    //getCreateMissionWizard().getMissionData();
		    int constructionSkill = constructionSite.getSkill();
		    //ConstructionStageInfo stageInfo = null;// constructionSite.getStageInfo();
		    ConstructionValues values = constructionManager.getConstructionValues();
	        values.clearCache();

	        // Determine construction site location and facing.
		    stageInfo = determineNewStageInfo(constructionSite, constructionSkill);

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

		        //boolean again = false;

		        //while (again) {
		        	modifiedSite = positionNewConstructionSite(constructionSite, stageInfo, constructionSkill);
		        	confirmSiteLocation(modifiedSite, constructionManager, true, stageInfo, constructionSkill);
		        //}

		        logger.log(Level.INFO, "New construction site added at " + settlement.getName());
		    }
		    else {
		        //endMission("New construction stage could not be determined.");
		        System.out.println("New construction stage could not be determined.");
		    }

		    mission.init_1b(modifiedSite, stageInfo, constructionSkill, values);
		    mission.setPhases_1();

		}


	}

	public synchronized void confirmSiteLocation(ConstructionSite site, ConstructionManager constructionManager,
			boolean isAtPreDefinedLocation, ConstructionStageInfo stageInfo, int constructionSkill) {
		//System.out.println("entering confirmSiteLocation");

        // Determine location and facing for the new building.
		double xLoc = site.getXLocation();
		double yLoc = site.getYLocation();
		double scale = mapPanel.getScale();

		Settlement currentS = settlementWindow.getMapPanel().getSettlement();
		if (currentS != constructionManager.getSettlement()) {
			settlementWindow.getMapPanel().getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(constructionManager.getSettlement());
		}
  		// set up the Settlement Map Tool to display the suggested location of the building
		mapPanel.reCenter();
		mapPanel.moveCenter(xLoc*scale, yLoc*scale);
		mapPanel.setShowConstructionLabels(true);

		String header = null;
		String title = null;
		String message = null;

		if (isAtPreDefinedLocation) {
			header = "Would you like to place " +  site.getStageInfo().getName() + " at its default position? ";
		}
		else {
			header = "Would you like to place " + site.getStageInfo().getName() + " at this position? ";
		}

		message = "Note: unless timer is cancelled, default to \"Yes\" in 30 secs";

		//String missionName = template.getMissionName();

       // if (missionName != null)
		//if (missionName.equals("null"))
			title = site.getStageInfo().getType() + " at " + constructionManager.getSettlement();
        //else
        //	title = "A Resupply Transport" + " at " + mgr.getSettlement();

		StringProperty msg = new SimpleStringProperty(message);

        if (mainScene != null) {
        	alertDialog(title, header, msg, constructionManager, site, true, stageInfo, constructionSkill);

		} else {

	        desktop.openAnnouncementWindow("Pause for Cnstruction Wizard");
	        AnnouncementWindow aw = desktop.getAnnouncementWindow();
	        Point location = MouseInfo.getPointerInfo().getLocation();
	        double Xloc = location.getX() - aw.getWidth() * 2;
			double Yloc = location.getX() - aw.getHeight() * 2;
			aw.setLocation((int)Xloc, (int)Yloc);

			int reply = JOptionPane.showConfirmDialog(aw, message, TITLE, JOptionPane.YES_NO_OPTION);
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
		}

	}



	@SuppressWarnings("restriction")
	public void alertDialog(String title, String header, StringProperty msg,
			ConstructionManager constructionManager, ConstructionSite site, boolean hasTimer,
			ConstructionStageInfo stageInfo, int constructionSkill){

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
				logger.info(site.toString() + " is put in place in " + constructionManager.getSettlement());

			} else if (result.isPresent() && result.get() == buttonTypeNo) {
				//constructionManager.removeConstructionSite(site);
		    	//System.out.println("just removing building");
				site = positionNewConstructionSite(site, stageInfo, constructionSkill);
				confirmSiteLocation(site, constructionManager,false, stageInfo, constructionSkill);
			}

			else if (result.isPresent() && result.get() == buttonTypeCancelTimer) {
				timer.stop();
				alertDialog(title, header, msg, constructionManager, site, false, stageInfo, constructionSkill);
			}

	}


	   /**
     * Determines a new construction stage info for a site.
     * @param site the construction site.
     * @param skill the architect's construction skill.
     * @return construction stage info.
     * @throws Exception if error determining construction stage info.
     */
    private ConstructionStageInfo determineNewStageInfo(ConstructionSite site, int skill) {
		logger.info("ConstructionWizard's determineNewStageInfo() is in " + Thread.currentThread().getName() + " Thread");

        ConstructionStageInfo result = null;

        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
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
		logger.info("ConstructionWizard's positionNewConstructionSite() is in " + Thread.currentThread().getName() + " Thread");

        boolean goodPosition = false;

        // Determine preferred building type from foundation stage info.
        String buildingType = determinePreferredConstructedBuildingType(foundationStageInfo, constructionSkill);
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
                List<Building> inhabitableBuildings = settlement.getBuildingManager().getBuildings(BuildingFunction.LIFE_SUPPORT);
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
                List<Building> sameBuildings = settlement.getBuildingManager().getBuildingsOfSameType(buildingType);
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
            BuildingManager buildingManager = settlement.getBuildingManager();
            if (buildingManager.getBuildingNum() > 0) {
                for (int x = 10; !goodPosition; x+= 10) {
                    List<Building> allBuildings = buildingManager.getBuildings();
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
    private String determinePreferredConstructedBuildingType(ConstructionStageInfo foundationStageInfo,
            int constructionSkill) {
		logger.info("ConstructionWizard's determinePreferredConstructedBuildingType() is in " + Thread.currentThread().getName() + " Thread");

        String result = null;

        ConstructionValues values = settlement.getConstructionManager().getConstructionValues();
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

        BuildingManager manager = settlement.getBuildingManager();
        List<Building> inhabitableBuildings = manager.getBuildings(BuildingFunction.LIFE_SUPPORT);
        Collections.shuffle(inhabitableBuildings);

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        int baseLevel = buildingConfig.getBaseLevel(buildingType);

        // Try to find a connection between an inhabitable building without access to airlock and
        // another inhabitable building with access to an airlock.
        if (settlement.getAirlockNum() > 0) {

            double leastDistance = Double.MAX_VALUE;

            Iterator<Building> i = inhabitableBuildings.iterator();
            while (i.hasNext()) {
                Building startingBuilding = i.next();
                if (!settlement.hasWalkableAvailableAirlock(startingBuilding)) {

                    // Find a different inhabitable building that has walkable access to an airlock.
                    Iterator<Building> k = inhabitableBuildings.iterator();
                    while (k.hasNext()) {
                        Building building = k.next();
                        if (!building.equals(startingBuilding)) {

                            // Check if connector base level matches either building.
                            boolean matchingBaseLevel = (baseLevel == startingBuilding.getBaseLevel()) ||
                                    (baseLevel == building.getBaseLevel());

                            if (settlement.hasWalkableAvailableAirlock(building) && matchingBaseLevel) {
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
                    boolean hasWalkingPath = settlement.getBuildingConnectorManager().hasValidPath(
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
                    boolean directlyConnected = (settlement.getBuildingConnectorManager().getBuildingConnections(
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
            if (settlement.getBuildingManager().checkIfNewBuildingLocationOpen(rectCenterX,
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
                    boolean clearPath = LocalAreaUtil.checkLinePathCollision(line, settlement.getCoordinates(), false);
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


    /**
     * Asks user to confirm the location of the new building.
     * @param template
     * @param buildingManager
     * @param isAtPreDefinedLocation
     */
	public synchronized void confirmSiteLocation(BuildingTemplate template, boolean isAtPreDefinedLocation) {

	}




	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Prepares tool window for deletion.
	 */
	public void destroy() {
		//constructionStage = null;
		mainScene = null;
		desktop = null;
		settlement = null;
		settlementWindow = null;
		mapPanel = null;

	}

}