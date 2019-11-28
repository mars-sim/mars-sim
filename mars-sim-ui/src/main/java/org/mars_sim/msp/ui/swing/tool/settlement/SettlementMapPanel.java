/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.MoreMath;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;

/**
 * A panel for displaying the settlement map.
 */
@SuppressWarnings("serial")
public class SettlementMapPanel extends WebPanel implements ClockListener {

// 	Default logger.
//	private static Logger logger = Logger.getLogger(SettlementMapPanel.class.getName());

	// Static members.
	private static final double WIDTH = 6D;
	public static final double DEFAULT_SCALE = 10D;
	public static final double MAX_SCALE = 55D;
	public static final double MIN_SCALE = 5D / 11D;
	private static final Color MAP_BACKGROUND = new Color(181, 95, 0);

	// Data members
	private double xPos;
	private double yPos;
	private double rotation;
	private double scale;

	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;

	private int size;

	private boolean showBuildingLabels;
	private boolean showConstructionLabels;
	private boolean showPersonLabels;
	private boolean showVehicleLabels;
	private boolean showRobotLabels;
	private boolean showDaylightLayer;

//	private MainScene mainScene;
	private MainDesktopPane desktop;
	
	private Building building;
	private SettlementWindow settlementWindow;
	private Settlement settlement;
	private PopUpUnitMenu menu;
	private SettlementTransparentPanel settlementTransparentPanel;

	private List<SettlementMapLayer> mapLayers;
	private Map<Settlement, Person> selectedPerson;
	private Map<Settlement, Robot> selectedRobot;
	private Map<Settlement, Building> selectedBuilding;

	private static Simulation sim;
	private static UnitManager unitManager;
	private static MasterClock masterClock;
	
//	private FXGraphics2D fxg2;

	/**
	 * Constructor 1 A panel for displaying a settlement map.
	 */
	public SettlementMapPanel(MainDesktopPane desktop, final SettlementWindow settlementWindow) {
		super();
		this.settlementWindow = settlementWindow;
		this.desktop = desktop;
//		this.mainScene = desktop.getMainScene();

//		if (mainScene != null) {
////			fxg2 = MainScene.getFXGraphics2D();
//			//	    this.g2 = new FXGraphics2D(gc);
//			fxg2 = new FXGraphics2D(MainScene.getCanvas().getGraphicsContext2D());
//		}

		if (sim == null)
			sim = Simulation.instance();
		
		if (unitManager == null)
			unitManager = sim.getUnitManager();
		
		if (!unitManager.getSettlements().isEmpty())
			settlement = (Settlement) unitManager.getSettlements().toArray()[0];

		setLayout(new BorderLayout());

		setDoubleBuffered(true);

		// Initialize data members.
		xPos = 0D;
		yPos = 0D;
		rotation = 0D;
		scale = DEFAULT_SCALE;
		showBuildingLabels = false;
		showConstructionLabels = false;
		showPersonLabels = false;
		showVehicleLabels = false;
		showRobotLabels = false;
		showDaylightLayer = false; // turn off by default
		selectedBuilding = new HashMap<Settlement, Building>();
		selectedPerson = new HashMap<Settlement, Person>();
		selectedRobot = new HashMap<Settlement, Robot>();
	}
	
	public void createUI() {
		// logger.info("PERIOD_IN_MILLISOLS : " + PERIOD_IN_MILLISOLS);
//		SwingUtilities.invokeLater(() -> {
			initLayers(desktop);
//		});

		// Set foreground and background colors.
		setOpaque(false);
		setBackground(MAP_BACKGROUND);
		setForeground(Color.ORANGE);

		if (masterClock == null)
			masterClock = sim.getMasterClock();
		
		masterClock.addClockListener(this);
	
		// Add detectMouseMovement() after refactoring
//		SwingUtilities.invokeLater(() -> {
			detectMouseMovement();
			setFocusable(true);
			requestFocusInWindow();
//		});

		// SwingUtilities.updateComponentTreeUI(this);

		setVisible(true);

		// paintDoubleBuffer();
		repaint();
	}

	// Add initLayers()
	public void initLayers(MainDesktopPane desktop) {
		// Create map layers.
		mapLayers = new ArrayList<SettlementMapLayer>();
		mapLayers.add(new BackgroundTileMapLayer(this));
		mapLayers.add(new DayNightMapLayer(this));
		mapLayers.add(new StructureMapLayer(this));
		mapLayers.add(new VehicleMapLayer(this));
		mapLayers.add(new PersonMapLayer(this));
		mapLayers.add(new RobotMapLayer(this));
		mapLayers.add(new LabelMapLayer(this));

		size = mapLayers.size();

		// SwingUtilities.invokeLater(() -> {
//		if (desktop.getMainScene() == null)
			settlementTransparentPanel = new SettlementTransparentPanel(desktop, this);
			settlementTransparentPanel.createAndShowGUI();
//		// });

		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Constructor 2 A panel for initializing the display of a building svg image
	 * by BuildingPanel
	 */
	public SettlementMapPanel(Settlement settlement, Building building) {
		super();

		// Initialize data members.
		xPos = 0D;
		yPos = 0D;
		rotation = 0D;
		scale = DEFAULT_SCALE;
		this.settlement = settlement;
		this.building = building;

		mapLayers = new ArrayList<SettlementMapLayer>(1);
		mapLayers.add(new StructureMapLayer(this));

		size = mapLayers.size();

		// Set preferred size.
		setPreferredSize(new Dimension(100, 100));

		// Set foreground and background colors.
		setOpaque(true);

	}


	public void detectMouseMovement() {

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
//				if (evt.getButton() == MouseEvent.BUTTON1) {
					// Move map center based on mouse drag difference.
					double xDiff = evt.getX() - xLast;
					double yDiff = evt.getY() - yLast;
					moveCenter(xDiff, yDiff);
					xLast = evt.getX();
					yLast = evt.getY();
//				}
			}
			
			@Override
			public void mouseMoved(MouseEvent evt) {
				int x = evt.getX();
				int y = evt.getY();
				
				// Display building (x, y) coordinate of the mouse pointer within a building on the status bar
				showBuildingCoord(x, y); 
				
				// Display the coordinate (with a reference to the settlement map) on the status bar
				// Note: the top left most corner is (0,0)
				settlementWindow.setMapXYCoord(x, y);
			}

			
		});
		
		addMouseListener(new MouseAdapter() {
				
			@Override
			public void mousePressed(MouseEvent evt) {
				
//				if (evt.getButton() == MouseEvent.BUTTON3 
//						|| evt.getButton() == MouseEvent.BUTTON1) {
				
					// Set initial mouse drag position.
					xLast = evt.getX();
					yLast = evt.getY();		
//				}				
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				
//				if (evt.getButton() == MouseEvent.BUTTON3
//						|| evt.getButton() == MouseEvent.BUTTON1) {
//					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					
					if (evt.isPopupTrigger()) {
						setCursor(new Cursor(Cursor.HAND_CURSOR));
						doPop(evt);
					}
					else
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					
					// Reset them to zero to prevent over-dragging of the settlement map
					xLast = 0;
					yLast = 0;
//				}
			}

//			@Override
//			public void mouseClicked(MouseEvent evt) {
//				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//			}
			
		});
	}

	/**
	 * Checks if the player selected an unit
	 * 
	 * @param evt
	 */
	private void doPop(final MouseEvent evt) {
		// System.out.println("doPop()");
		int x = evt.getX();
		int y = evt.getY();
		
		final ConstructionSite site = selectConstructionSiteAt(x, y);
		final Building building = selectBuildingAt(x, y);
		final Vehicle vehicle = selectVehicleAt(x, y);
		final Person person = selectPersonAt(x, y);
		final Robot robot = selectRobotAt(x, y);

//		if (site != null || building != null || vehicle != null || person != null || robot != null) {

			// Deconflict cases by the virtue of the if-else order below
			// when one or more are detected
			if (person != null) {
				menu = new PopUpUnitMenu(settlementWindow, person);
				menu.show(evt.getComponent(), x, y);
			}
			else if (robot != null) {
				menu = new PopUpUnitMenu(settlementWindow, robot);
				menu.show(evt.getComponent(), x, y);
			}
			else if (vehicle != null) {
				menu = new PopUpUnitMenu(settlementWindow, vehicle);
				menu.show(evt.getComponent(), x, y);
			}
			else if (building != null) {
				menu = new PopUpUnitMenu(settlementWindow, building);
				menu.show(evt.getComponent(), x, y);
			}
			else if (site != null) {
				menu = new PopUpUnitMenu(settlementWindow, site);
				menu.show(evt.getComponent(), x, y);
			}
//		}
		repaint();
	}
		
	/**
	 * Displays the specific x y coordinates within a building
	 * (based upon where the mouse is pointing at)
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 */
	public void showBuildingCoord(int xPixel, int yPixel) {
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);

		Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
		while (j.hasNext()) {
			Building building = j.next();

			if (!building.getInTransport()) {

				double width = building.getWidth();
				double length = building.getLength();
				int facing = (int) building.getFacing();
				double x = building.getXLocation();
				double y = building.getYLocation();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 270) {
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double c_x = clickPosition.getX();
				double c_y = clickPosition.getY();

				double distanceX = Math.round((c_x - x) * 100.0) / 100.0; // Math.abs(x - c_x);
				double distanceY = Math.round((c_y - y) * 100.0) / 100.0; // Math.abs(y - c_y);

				if (Math.abs(distanceX) <= xx && Math.abs(distanceY) <= yy) {

					settlementWindow.setBuildingXYCoord(distanceX, distanceY);

					break;
				}
			}
		}
	}

	/**
	 * Gets the settlement currently displayed.
	 * 
	 * @return settlement or null if none.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Gets the SettlementWindow class.
	 * 
	 * @return settlementWindow or null if none.
	 */
	public SettlementWindow getSettlementWindow() {
		return settlementWindow;
	}

	/**
	 * Sets the settlement to display.
	 * 
	 * @param settlement the settlement.
	 */
	public synchronized void setSettlement(Settlement newSettlement) {
		if (newSettlement != settlement) {

			this.settlement = newSettlement;
//			if (settlementWindow != null && settlementWindow.getMarqueeTicker() != null)
//				settlementWindow.getMarqueeTicker().updateSettlement(newSettlement);
			// paintDoubleBuffer();
			repaint();
		}
	}

	/**
	 * Gets the map scale.
	 * 
	 * @return scale (pixels per meter).
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the map scale.
	 * 
	 * @param scale (pixels per meter).
	 */
	public void setScale(double scale) {
		this.scale = scale;

		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Gets the map rotation.
	 * 
	 * @return rotation (radians).
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * Sets the map rotation.
	 * 
	 * @param rotation (radians).
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;

		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Resets the position, scale and rotation of the map. Separate function that
	 * only uses one repaint.
	 */
	public void reCenter() {
		xPos = 0D;
		yPos = 0D;
		setRotation(0D);
		scale = DEFAULT_SCALE;

		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Moves the center of the map by a given number of pixels.
	 * 
	 * @param xDiff the X axis pixels.
	 * @param yDiff the Y axis pixels.
	 */
	public void moveCenter(double xDiff, double yDiff) {
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
		xDiff /= scale;
		yDiff /= scale;

		// Correct due to rotation of map.
		double c = MoreMath.cos(rotation);
		double s = MoreMath.sin(rotation);
		
		double realXDiff = c * xDiff + s * yDiff;
		double realYDiff = c * yDiff - s * xDiff;

		xPos += realXDiff;
		yPos += realYDiff;

		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Selects a person if any person is at the given x and y pixel position.
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedPerson;
	 */
	public Person selectPersonAt(int xPixel, int yPixel) {
		double range = WIDTH / scale;
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		Person selectedPerson = null;

		Iterator<Person> i = CollectionUtils.getPeopleToDisplay(settlement).iterator();
		while (i.hasNext()) {
			Person person = i.next();
			double distanceX = person.getXLocation() - settlementPosition.getX();
			double distanceY = person.getYLocation() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				selectedPerson = person;
			}
		}

		if (selectedPerson != null) {
			selectPerson(selectedPerson);

		}
		return selectedPerson;
	}

	/**
	 * Selects the robot if any robot is at the given x and y pixel position.
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedRobot;
	 */
	public Robot selectRobotAt(int xPixel, int yPixel) {
		double range = WIDTH / scale;
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		Robot selectedRobot = null;

		Iterator<Robot> i = RobotMapLayer.getRobotsToDisplay(settlement).iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			double distanceX = robot.getXLocation() - settlementPosition.getX();
			double distanceY = robot.getYLocation() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				selectedRobot = robot;
			}
		}

		if (selectedRobot != null) {
			selectRobot(selectedRobot);

		}
		return selectedRobot;
	}

	/**
	 * Selects a building
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedBuilding
	 */
	public Building selectBuildingAt(int xPixel, int yPixel) {
		// System.out.println("selectBuildingAt()");
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);
		Building selectedBuilding = null;

		Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
		while (j.hasNext()) {
			Building building = j.next();

			if (!building.getInTransport()) {

				double width = building.getWidth();
				double length = building.getLength();
				int facing = (int) building.getFacing();
				double x = building.getXLocation();
				double y = building.getYLocation();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 270) {
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double c_x = clickPosition.getX();
				double c_y = clickPosition.getY();

				double distanceX = Math.round((c_x - x) * 100.0) / 100.0; // Math.abs(x - c_x);
				double distanceY = Math.round((c_y - y) * 100.0) / 100.0; // Math.abs(y - c_y);

				if (Math.abs(distanceX) <= xx && Math.abs(distanceY) <= yy) {
					selectedBuilding = building;

					if (selectedBuilding != null) {
						selectBuilding(selectedBuilding);
					}
					
					break;
				}
			}
		}
		return selectedBuilding;
	}

	/**
	 * Selects a construction site
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selected construction site
	 */
	public ConstructionSite selectConstructionSiteAt(int xPixel, int yPixel) {
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);
		ConstructionSite site = null;

		Iterator<ConstructionSite> j = settlement.getConstructionManager().getConstructionSites().iterator();
		while (j.hasNext()) {
			ConstructionSite s = j.next();

			if (!LabelMapLayer.getConstructionLabel(s).equals(Msg.getString("LabelMapLayer.noConstruction"))) {
				double width = s.getWidth();
				double length = s.getLength();
				int facing = (int) s.getFacing();
				double x = s.getXLocation();
				double y = s.getYLocation();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 90) {
					yy = width / 2D;
					xx = length / 2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width / 2D;
					yy = length / 2D;
				} else if (facing == 270) {
					yy = width / 2D;
					xx = length / 2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45) {
					yy = width / 2D;
					xx = length / 2D;
				} else if (facing == 135) {
					yy = width / 2D;
					xx = length / 2D;
				}

				double distanceX = Math.abs(x - clickPosition.getX());
				double distanceY = Math.abs(y - clickPosition.getY());

				if (distanceX <= xx && distanceY <= yy) {
					site = s;
					break;
				}
			}
		}

		return site;
	}

//	public static List<ConstructionSite> returnConstructionSiteList(Settlement settlement) {
//
//		List<ConstructionSite> result = new ArrayList<ConstructionSite>();
//		if (settlement != null) {
//		    Iterator<ConstructionSite> i = settlement.getBuildingManager().getBuildings().iterator();
//			while (i.hasNext()) {
//				ConstructionSite site = i.next();
//						result.add(site);
//			}
//		}
//		return result;
//	}
//
//	public static List<Building> returnBuildingList(Settlement settlement) {
//
//		List<Building> result = new ArrayList<Building>();
//		if (settlement != null) {
//		    Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
//			while (i.hasNext()) {
//				Building building = i.next();
//						result.add(building);
//			}
//		}
//		return result;
//	}

	/**
	 * Selects a vehicle
	 * 
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedVehicle
	 */
	public Vehicle selectVehicleAt(int xPixel, int yPixel) {
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = returnVehicleList(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width = vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange = width / 2.0;
			else
				newRange = length / 2.0;

			double x = vehicle.getXLocation();
			double y = vehicle.getYLocation();

			double distanceX = x - settlementPosition.getX();
			double distanceY = y - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;

				//// paintDoubleBuffer();
				// repaint();
			}
		}
		return selectedVehicle;
	}

	/**
	 * Selects a vehicle, as used by TransportWizard
	 * 
	 * @param xLoc the position of the template building on the displayed map.
	 * @param yLoc the position of the template building on the displayed map.
	 * @return selectedVehicle
	 */
	public Vehicle selectVehicleAsObstacle(double xLoc, double yLoc) {

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = returnVehicleList(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width = vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double buildingWidth = 10;
			double buildingLength = 10;
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange = (width + buildingWidth) / 2.0;
			else
				newRange = (length + buildingLength) / 2.0;

			double x = vehicle.getXLocation();
			double y = vehicle.getYLocation();

			// distances between the center of the vehicle and the center of the building
			double distanceX = x - xLoc;
			double distanceY = y - yLoc;
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;

				//// paintDoubleBuffer();
				repaint();
			}
		}
		return selectedVehicle;
	}

	public static List<Vehicle> returnVehicleList(Settlement settlement) {

		List<Vehicle> result = new ArrayList<Vehicle>();
		if (settlement != null) {
			Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
				result.add(vehicle);
			}
		}
		return result;
	}

	/**
	 * Selects a person on the map.
	 * 
	 * @param person the selected person.
	 */
	public void selectPerson(Person person) {
		if ((settlement != null) && (person != null)) {
			Person currentlySelected = selectedPerson.get(settlement);
			if (person.equals(currentlySelected)) {
				selectedPerson.put(settlement, null);
			} else {
				selectedPerson.put(settlement, person);
			}

			// Repaint to refresh the label display.
			//// paintDoubleBuffer();
			// repaint();
		}
	}

	/**
	 * Get the selected person for the current settlement.
	 * 
	 * @return the selected person.
	 */
	public Person getSelectedPerson() {
		Person result = null;
		if (settlement != null) {
			result = selectedPerson.get(settlement);
		}
		return result;
	}

	/**
	 * Selects a building on the map.
	 * 
	 * @param person the selected building.
	 */
	public void selectBuilding(Building building) {
		if ((settlement != null) && (building != null)) {
			Building currentlySelected = selectedBuilding.get(settlement);
			if (building.equals(currentlySelected)) {
				selectedBuilding.put(settlement, null);
			} else {
				selectedBuilding.put(settlement, building);
			}
		}
	}
	
	/**
	 * Get the selected building for the current settlement.
	 * 
	 * @return the selected building.
	 */
	public Building getSelectedBuilding() {
		Building result = null;
		if (settlement != null) {
			result = selectedBuilding.get(settlement);
		}
		return result;
	}
	
	/**
	 * Selects a robot on the map.
	 * 
	 * @param robot the selected robot.
	 */
	public void selectRobot(Robot robot) {
		if ((settlement != null) && (robot != null)) {
			Robot currentlySelected = selectedRobot.get(settlement);
			if (robot.equals(currentlySelected)) {
				selectedRobot.put(settlement, null);
			} else {
				selectedRobot.put(settlement, robot);
			}

			// Repaint to refresh the label display.
			//// paintDoubleBuffer();
			// repaint();
		}
	}

	/**
	 * Get the selected Robot for the current settlement.
	 * 
	 * @return the selected Robot.
	 */
	public Robot getSelectedRobot() {
		Robot result = null;
		if (settlement != null) {
			result = selectedRobot.get(settlement);
		}
		return result;
	}

	/**
	 * Convert a pixel X,Y position to a X,Y (meter) position local to the
	 * settlement in view.
	 * 
	 * @param xPixel the pixel X position.
	 * @param yPixel the pixel Y position.
	 * @return the X,Y settlement position.
	 */
	public Point.Double convertToSettlementLocation(int xPixel, int yPixel) {

		Point.Double result = new Point.Double(0D, 0D);

		double xDiff1 = (getWidth() / 2) - xPixel;
		double yDiff1 = (getHeight() / 2) - yPixel;

		double xDiff2 = xDiff1 / scale;
		double yDiff2 = yDiff1 / scale;

		// Correct due to rotation of map.
		double xDiff3 = (Math.cos(rotation) * xDiff2) + (Math.sin(rotation) * yDiff2);
		double yDiff3 = (Math.cos(rotation) * yDiff2) - (Math.sin(rotation) * xDiff2);

		double newXPos = xPos + xDiff3;
		double newYPos = yPos + yDiff3;

		result.setLocation(newXPos, newYPos);

		return result;
	}

	/**
	 * Checks if building labels should be displayed.
	 * 
	 * @return true if building labels should be displayed.
	 */
	public boolean isShowBuildingLabels() {
		return showBuildingLabels;
	}

	/**
	 * Sets if building labels should be displayed.
	 * 
	 * @param showLabels true if building labels should be displayed.
	 */
	public void setShowBuildingLabels(boolean showLabels) {
		this.showBuildingLabels = showLabels;
		// if (showLabels)
		// settlementTransparentPanel.getBuildingLabelMenuItem().setState(true);
		// else settlementTransparentPanel.getBuildingLabelMenuItem().setState(false);
		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if construction site labels should be displayed.
	 * 
	 * @return true if construction site labels should be displayed.
	 */
	public boolean isShowConstructionLabels() {
		return showConstructionLabels;
	}

	/**
	 * Sets if construction site labels should be displayed.
	 * 
	 * @param showLabels true if construction site labels should be displayed.
	 */
	public void setShowConstructionLabels(boolean showLabels) {
		this.showConstructionLabels = showLabels;
		// if (showLabels)
		// settlementTransparentPanel.getConstructionLabelMenuItem().setState(true);
		// else
		// settlementTransparentPanel.getConstructionLabelMenuItem().setState(false);
		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if person labels should be displayed.
	 * 
	 * @return true if person labels should be displayed.
	 */
	public boolean isShowPersonLabels() {
		return showPersonLabels;
	}

	/**
	 * Sets if person labels should be displayed.
	 * 
	 * @param showLabels true if person labels should be displayed.
	 */
	public void setShowPersonLabels(boolean showLabels) {
		this.showPersonLabels = showLabels;
		// if (showLabels)
		// settlementTransparentPanel.getPersonLabelMenuItem().setState(true);
		// else settlementTransparentPanel.getPersonLabelMenuItem().setState(false);
		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if Robot labels should be displayed.
	 * 
	 * @return true if Robot labels should be displayed.
	 */
	public boolean isShowRobotLabels() {
		return showRobotLabels;
	}

	/**
	 * Sets if Robot labels should be displayed.
	 * 
	 * @param showLabels true if Robot labels should be displayed.
	 */
	public void setShowRobotLabels(boolean showLabels) {
		this.showRobotLabels = showLabels;
		// if (showLabels)
		// settlementTransparentPanel.getRobotLabelMenuItem().setState(true);
		// else settlementTransparentPanel.getRobotLabelMenuItem().setState(false);
		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if vehicle labels should be displayed.
	 * 
	 * @return true if vehicle labels should be displayed.
	 */
	public boolean isShowVehicleLabels() {
		return showVehicleLabels;
	}

	/**
	 * Sets if vehicle labels should be displayed.
	 * 
	 * @param showLabels true if vehicle labels should be displayed.
	 */
	public void setShowVehicleLabels(boolean showLabels) {
		this.showVehicleLabels = showLabels;
		// if (showLabels)
		// settlementTransparentPanel.getVehicleLabelMenuItem().setState(true);
		// else settlementTransparentPanel.getVehicleLabelMenuItem().setState(false);
		// paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if DaylightLayer should be displayed.
	 * 
	 * @return true if DaylightLayer should be displayed.
	 */
	public boolean isDaylightTrackingOn() {
		return showDaylightLayer;
	}

	/**
	 * Sets if DayNightLayershould be displayed.
	 * 
	 * @param showDayNightLayer true if DayNightLayer should be displayed.
	 */
	public void setShowDayNightLayer(boolean showDayNightLayer) {
		this.showDaylightLayer = showDayNightLayer;

		// paintDoubleBuffer();
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

//		if (dbImage != null) {
//			g.drawImage(dbImage,  0, 0, null);
//		}
//	}

//	/*
//	 * Uses double buffering to draws into its own graphics object dbg before calling paintComponent()
//	 */
///		
//	public void paintDoubleBuffer() {
//		if (dbImage == null) {
//			dbImage = createImage(width, height);
//			if (dbImage == null) {
//				//System.out.println("dbImage is null");
//				return;
//			}
//			else
//				dbg = dbImage.getGraphics();
//		}
//		Graphics2D g2d = (Graphics2D) dbg;

		if (building != null 
				|| (desktop != null && settlementWindow.isShowing() && desktop.isToolWindowOpen(SettlementWindow.NAME))) {
			Graphics2D g2d = (Graphics2D) g;
	
			// long startTime = System.nanoTime();
	
			// Set graphics rendering hints.
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	
			// Display all map layers.
			Iterator<SettlementMapLayer> i = mapLayers.iterator();
			while (i.hasNext()) {
				// Add building parameter
				i.next().displayLayer(g2d, settlement, building, xPos, yPos, getWidth(), getHeight(), rotation, scale);
			}

//		for (int i = 0; i < size; i++) {
//			mapLayers.get(i).displayLayer(g2d, settlement, building, xPos, yPos, getWidth(), getHeight(), rotation,
//					scale);
//		}

		// long endTime = System.nanoTime();
		// double timeDiff = (endTime - startTime) / 1000000D;
		// System.out.println("SMT paint time: " + (int) timeDiff + " ms");
		}
	}

	public SettlementTransparentPanel getSettlementTransparentPanel() {
		return settlementTransparentPanel;
	}

	@Override
	public void clockPulse(double time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uiPulse(double time) {
		if (isShowing() && desktop.isToolWindowOpen(SettlementWindow.NAME)) {
			repaint();
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// TODO Auto-generated method stub

	}

	/**
	 * Cleans up the map panel for disposal.
	 */
	public void destroy() {
		// Remove clock listener.
		if (masterClock != null)
			masterClock.removeClockListener(this);

		menu = null;
		settlement = null;
		selectedPerson = null;
		building = null;
		settlementWindow = null;

		// Destroy all map layers.
		Iterator<SettlementMapLayer> i = mapLayers.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

		mapLayers = null;
		selectedRobot = null;
//		mainScene = null;
		building = null;
		settlementTransparentPanel = null;

	}

}