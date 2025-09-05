/*
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @date 2025-08-01
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.function.FunctionType;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.MoreMath;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A panel for displaying the settlement map.
 *
 * <p><b>What's new in this version</b> (memory‑safety & UX fixes):
 * <ul>
 *   <li><b>Zoom coalescing</b>: calls to {@link #setScale(double)} are throttled via a Swing Timer
 *       to prevent excessive re-rendering while a slider is dragged.</li>
 *   <li><b>Listener lifecycle</b>: mouse listeners are installed once and removed on dispose to
 *       avoid leaks when the panel is recreated.</li>
 *   <li><b>Graphics hygiene</b>: uses a child {@code Graphics2D} and disposes it after painting.</li>
 *   <li><b>Icon cache hook</b>: an LRU {@code ScaledIconCache} is exposed for layers that render
 *       scalable art to reuse rasterizations at the current scale.</li>
 *   <li><b>Tile cache cleanup</b>: background tile images are released during panel destroy.</li>
 * </ul>
 * </p>
 */
@SuppressWarnings("serial")
public class SettlementMapPanel extends JPanel {

	/**
	 * Run the given task on the Swing EDT (immediately if already on EDT).
	 */
	private static void onEdt(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) r.run();
		else SwingUtilities.invokeLater(r);
	}

	/**
	 * Display options that can be selected
	 */
	public enum DisplayOption {
		BUILDING_LABELS,
		CONSTRUCTION_LABELS,
		PERSON_LABELS,
		ROBOT_LABELS,
		VEHICLE_LABELS,
		DAYLIGHT_LAYER
	}

	// Property names for UI Config
	private static final String SPOT_LBL_PROP   = "SPOT_LABELS_";
	private static final String SETTLEMENT_PROP = "SETTLEMENT";
	private static final String X_PROP          = "XPOS";
	private static final String Y_PROP          = "YPOS";
	private static final String SCALE_PROP      = "SCALE";
	private static final String ROTATION_PROP   = "ROTATION";

	// Static members.
	public static final double DEFAULT_SCALE = 10D;
	private static final double SELECTION_RANGE = 0.25; // Settlement coordinate frame, 25 cm

	// Zoom bounds/quantization (helps caching + stability)
	private static final double MIN_SCALE = 1D;
	private static final double MAX_SCALE = 200D;
	private static final double SCALE_QUANTUM = 0.5D; // Quantize to 0.5 px/m steps

	// Data members
	private boolean exit = true;

	private double xPos;
	private double yPos;
	private double rotation;
	private double scale; // always stored quantized

	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;

	private MainDesktopPane desktop;

	private SettlementWindow settlementWindow;

	private Settlement settlement;

	private PopUpUnitMenu menu;

	private SettlementTransparentPanel settlementTransparentPanel;

	private DayNightMapLayer dayNightMapLayer;

	private Set<FunctionType> showSpotLabels = new HashSet<>();

	private List<SettlementMapLayer> mapLayers;

	private Map<Settlement, Person>   selectedPerson;
	private Map<Settlement, Robot>    selectedRobot;
	private Map<Settlement, Building> selectedBuilding;
	private Map<Settlement, Vehicle>  selectedVehicle;

	private static final Font sansSerif = new Font("SansSerif", Font.BOLD, 11);

	private Set<DisplayOption> displayOptions = EnumSet.noneOf(DisplayOption.class);

	// -------- Event coalescing for scale updates --------
	private Timer zoomCoalesceTimer;
	private volatile Double pendingScale = null; // when non-null, an update is queued

	// -------- Event coalescing for simulation tick -> UI --------
	/** Coalesces simulation-tick UI updates so we don't flood the EDT. */
	private final AtomicBoolean uiUpdateScheduled = new AtomicBoolean(false);

	// -------- Listener lifecycle management --------
	private boolean listenersInstalled = false;
	private MouseMotionAdapter motionListener;
	private MouseAdapter mouseListener;

	// -------- Shared cache hook for layers that rasterize scalable art --------
	private final ScaledIconCache iconCache = new ScaledIconCache();

	/**
	 * Constructor 1: A panel for displaying a settlement map.
	 */
	public SettlementMapPanel(MainDesktopPane desktop, final SettlementWindow settlementWindow,
							  Properties userSettings) {
		super();
		this.settlementWindow = settlementWindow;
		this.desktop = desktop;

		UnitManager unitManager = desktop.getSimulation().getUnitManager();

		List<Settlement> settlements = new ArrayList<>(unitManager.getSettlements());

		if (!settlements.isEmpty()) {
			Collections.sort(settlements);

			// Search for matching settlement
			String userChoice = ((userSettings != null) && userSettings.containsKey(SETTLEMENT_PROP) ?
					userSettings.getProperty(SETTLEMENT_PROP) : null);
			if (userChoice != null) {
				for (Settlement s : settlements) {
					if (s.getName().equals(userChoice)) {
						settlement = s;
					}
				}
			}

			if (settlement == null) {
				settlement = settlements.get(0);
			}
		}

		setLayout(new BorderLayout());

		setDoubleBuffered(true);

		// Initialize data members.
		xPos = UIConfig.extractDouble(userSettings, X_PROP, 0D);
		yPos = UIConfig.extractDouble(userSettings, Y_PROP, 0D);
		rotation = UIConfig.extractDouble(userSettings, ROTATION_PROP, 0D);
		// Always quantize stored scale
		scale = quantizeScale(UIConfig.extractDouble(userSettings, SCALE_PROP, DEFAULT_SCALE));
		for (DisplayOption op : DisplayOption.values()) {
			if (UIConfig.extractBoolean(userSettings, op.name(), false)) {
				displayOptions.add(op);
			}
		}

		for (FunctionType ft : FunctionType.values()) {
			if (UIConfig.extractBoolean(userSettings, SPOT_LBL_PROP + ft.name(), false)) {
				showSpotLabels.add(ft);
			}
		}
		selectedVehicle = new HashMap<>();
		selectedBuilding = new HashMap<>();
		selectedPerson = new HashMap<>();
		selectedRobot = new HashMap<>();

		// Throttle zoom changes to avoid flood of repaints/rasterizations
		zoomCoalesceTimer = new Timer(50, e -> applyPendingScale());
		zoomCoalesceTimer.setRepeats(false);
	}

	void createUI() {

		initLayers(desktop);

		// Set foreground and background colors.
		setOpaque(false);
		setBackground(new Color(0, 0, 0, 128));

		setForeground(Color.ORANGE);

		detectMouseMovement(); // installs listeners once
		setFocusable(true);
		requestFocusInWindow();

		setVisible(true);

		repaint();
	}

	/**
	 * Initializes map layers.
	 *
	 * @param desktop
	 */
	public void initLayers(MainDesktopPane desktop) {

		// Set up the dayNightMapLayer layers
		dayNightMapLayer = new DayNightMapLayer(this);

		// Check the DayNightLayer at the start of the sim
		displayOptions.remove(DisplayOption.DAYLIGHT_LAYER);

		// Create map layers.
		mapLayers = new ArrayList<>();
		mapLayers.add(new BackgroundTileMapLayer(this));
		mapLayers.add(dayNightMapLayer);
		mapLayers.add(new BuildingMapLayer(this));
		mapLayers.add(new ConstructionMapLayer(this));
		mapLayers.add(new VehicleMapLayer(this));
		mapLayers.add(new PersonMapLayer(this));
		mapLayers.add(new RobotMapLayer(this));

		settlementTransparentPanel = new SettlementTransparentPanel(desktop, this);

		// Ensure all Swing mutations happen on EDT
		onEdt(() -> {
			settlementTransparentPanel.createAndShowGUI();
			if (settlement != null && settlementTransparentPanel.getSettlementListBox() != null) {
				settlementTransparentPanel.getSettlementListBox().setSelectedItem(settlement);
			}
			// Loads the value of scale possibly modified from UIConfig's Properties
			settlementTransparentPanel.setZoomValue((int) Math.round(scale));

			repaint();
		});
	}

	/**
	 * Installs mouse listeners once; safe to call multiple times.
	 */
	public void detectMouseMovement() {
		if (listenersInstalled) return;

		motionListener = new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				// Move map center based on mouse drag difference.
				int xDiff = evt.getX() - xLast;
				int yDiff = evt.getY() - yLast;
				moveCenter(1.0 * xDiff, 1.0 * yDiff);
				xLast = evt.getX();
				yLast = evt.getY();
			}

			@Override
			public void mouseMoved(MouseEvent evt) {
				int x = evt.getX();
				int y = evt.getY();

				if (getSettlement() != null) {
					settlementWindow.setPop(getSettlement().getNumCitizens());
				}
				// Call to determine if it should display or remove the building coordinate within a building
				showBuildingCoord(x, y);
				// Display the pixel coordinate of the window panel
				// Note: the top left-most corner of window panel is (0,0)
				settlementWindow.setPixelXYCoord(x, y);
				// Display the settlement map coordinate of the hovering mouse pointer
				settlementWindow.setMapXYCoord(convertToSettlementLocation(x, y));

				if (exit) {
					exit = false;
				}
			}
		};

		mouseListener = new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent evt) {
				exit = false;
			}

			@Override
			public void mouseExited(MouseEvent evt) {
				if (!exit) {
					exit = true;
				}
			}

			@Override
			public void mousePressed(MouseEvent evt) {
				// Set initial mouse drag position.
				xLast = evt.getX();
				yLast = evt.getY();

				evt.consume();
			}

			@Override
			public void mouseReleased(MouseEvent evt) {
				// Note that SwingUtilities.isRightMouseButton() is needed for macOS to detect right mouse button (Ctrl + left button)
				if (evt.isPopupTrigger() || SwingUtilities.isRightMouseButton(evt)) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					doPop(evt);
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}

				// Reset them to zero to prevent over-dragging of the settlement map
				xLast = 0;
				yLast = 0;

				evt.consume();
			}
		};

		addMouseMotionListener(motionListener);
		addMouseListener(mouseListener);

		listenersInstalled = true;
	}

	private void removeInteractionListeners() {
		if (!listenersInstalled) return;
		if (motionListener != null) {
			removeMouseMotionListener(motionListener);
			motionListener = null;
		}
		if (mouseListener != null) {
			removeMouseListener(mouseListener);
			mouseListener = null;
		}
		listenersInstalled = false;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		// Ensure listeners are attached if panel is re-added to a container
		detectMouseMovement();
	}

	@Override
	public void removeNotify() {
		// Ensure listeners are detached to allow GC of this panel
		removeInteractionListeners();
		super.removeNotify();
	}

	/**
	 * Checks if the player selected an unit.
	 *
	 * @param evt
	 */
	private void doPop(final MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();

		LocalPosition settlementPosition = convertToSettlementLocation(x, y);

		// Deconflict cases by the virtue of the if-else order below
		// when one or more are detected
		Unit selectedUnit = selectPersonAt(settlementPosition);
		if (selectedUnit == null) {
			selectedUnit = selectRobotAt(settlementPosition);
			if (selectedUnit == null) {
				selectedUnit = selectVehicleAt(settlementPosition);
				if (selectedUnit == null) {
					selectedUnit = selectBuildingAt(settlementPosition);
					if (selectedUnit == null) {
						selectedUnit = selectConstructionSiteAt(settlementPosition);
					}
				}
			}
		}
		if (selectedUnit != null) {
			setPopUp(evt, x, y, selectedUnit);
		}
		repaint();
	}

	private void setPopUp(final MouseEvent evt, int x, int y, Unit unit) {
		menu = new PopUpUnitMenu(settlementWindow, unit);
		menu.show(evt.getComponent(), x, y);
	}

	/**
	 * Displays the specific x y coordinates within a building
	 * (based upon where the mouse is pointing at).
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 */
	public void showBuildingCoord(int xPixel, int yPixel) {

		boolean showBlank = true;

		LocalPosition mousePos = convertToSettlementLocation(xPixel, yPixel);

		for (Building building : settlement.getBuildingManager().getBuildingSet()) {
			if (!building.getInTransport() && isWithin(mousePos, building)) {
				settlementWindow.setBuildingXYCoord(building.getPosition(), false);

				LocalPosition pointerPos = convertToBuildingLoc(mousePos, building);
				settlementWindow.setBuildingPointerXYCoord(pointerPos, false);

				showBlank = false;
				break;
			}
		}

		if (showBlank) {
			// Remove the building coordinate
			settlementWindow.setBuildingXYCoord(LocalPosition.DEFAULT_POSITION, true);
			settlementWindow.setBuildingPointerXYCoord(LocalPosition.DEFAULT_POSITION, true);
		}
	}

	/**
	 * Gets the settlement currently displayed.
	 *
	 * @return settlement or null if none.
	 */
	public synchronized Settlement getSettlement() {
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
	 * @param newSettlement the settlement.
	 */
	public synchronized void setSettlement(Settlement newSettlement) {
		if (newSettlement != settlement) {
			this.settlement = newSettlement;
			onEdt(() -> {
				if (getSettlementTransparentPanel() != null
						&& getSettlementTransparentPanel().getSettlementListBox() != null) {
					getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(settlement);
				}
				repaint();
			});
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
	 * <p><b>NOTE:</b> This call is coalesced; fast, repeated calls (e.g., while dragging a slider)
	 * will trigger at most ~20 updates/sec.</p>
	 *
	 * @param newScale (pixels per meter).
	 */
	public void setScale(double newScale) {
		// Queue the new scale; apply after a short delay (coalescing)
		pendingScale = newScale;
		if (zoomCoalesceTimer != null) {
			onEdt(() -> zoomCoalesceTimer.restart()); // ensure Swing Timer is touched on EDT
		} else {
			onEdt(this::applyPendingScale);
		}
	}

	/**
	 * Applies the pending scale (quantized and clamped) and repaints once.
	 * Must be invoked on the EDT.
	 */
	private void applyPendingScale() {
		assert SwingUtilities.isEventDispatchThread() : "applyPendingScale must run on EDT";

		final double target = (pendingScale != null ? pendingScale : scale);
		pendingScale = null;

		final double q = quantizeScale(target);
		if (Math.abs(q - this.scale) < 1e-9) {
			// Nothing to do
			return;
		}

		this.scale = q;

		// Avoid re-entrant slider event storms: do not call setZoomValue here.

		revalidate();
		repaint();
	}

	/**
	 * Quantize and clamp the scale to keep cache keys stable and avoid extremes.
	 */
	private static double quantizeScale(double s) {
		double clamped = Math.max(MIN_SCALE, Math.min(MAX_SCALE, s));
		double steps = Math.round(clamped / SCALE_QUANTUM);
		return steps * SCALE_QUANTUM;
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
		scale = DEFAULT_SCALE; // set directly to avoid unnecessary coalescing delay here
		onEdt(() -> {
			if (settlementTransparentPanel != null) {
				settlementTransparentPanel.setZoomValue((int) Math.round(scale));
			}
			repaint();
		});
	}

	/**
	 * Moves the center of the map by a given number of pixels.
	 *
	 * @param xd the X axis pixels.
	 * @param yd the Y axis pixels.
	 */
	public void moveCenter(double xd, double yd) {
		setCursor(new Cursor(Cursor.MOVE_CURSOR));
		double xDiff = xd / scale;
		double yDiff = yd / scale;

		// Correct due to rotation of map.
		double c = MoreMath.cos(rotation);
		double s = MoreMath.sin(rotation);

		double realXDiff = c * xDiff + s * yDiff;
		double realYDiff = c * yDiff - s * xDiff;

		xPos += realXDiff;
		yPos += realYDiff;

		repaint();
	}

	/**
	 * Converts a pixel X,Y position to a X,Y (meter) position local to the
	 * settlement in view.
	 *
	 * @param xPixel the pixel X position.
	 * @param yPixel the pixel Y position.
	 * @return the X,Y settlement position.
	 */
	private LocalPosition convertToSettlementLocation(int xPixel, int yPixel) {

		double xDiff1 = (getWidth() / 2.0) - xPixel;
		double yDiff1 = (getHeight() / 2.0) - yPixel;

		double xDiff2 = xDiff1 / scale;
		double yDiff2 = yDiff1 / scale;

		// Correct due to rotation of map.
		double xDiff3 = (Math.cos(rotation) * xDiff2) + (Math.sin(rotation) * yDiff2);
		double yDiff3 = (Math.cos(rotation) * yDiff2) - (Math.sin(rotation) * xDiff2);

		double newXPos = xPos + xDiff3;
		double newYPos = yPos + yDiff3;

		return new LocalPosition(newXPos, newYPos);
	}

	/**
	 * Converts a local position from settlement positioning system to screen pixel x and y.
	 *
	 * @param pos
	 * @return
	 */
	Point convertToPixelPos(LocalPosition pos) {
		double x = pos.getX();
		double y = pos.getY();
		double xDiff3 = x - xPos;
		double yDiff3 = y - yPos;
		double xDiff2 = (Math.cos(rotation) * xDiff3) + (Math.sin(rotation) * yDiff3);
		double yDiff2 = (Math.cos(rotation) * yDiff3) - (Math.sin(rotation) * xDiff3);
		double xDiff1 = xDiff2 * scale;
		double yDiff1 = yDiff2 * scale;
		int xPixel = (int) Math.round(getWidth() / 2.0 - xDiff1);
		int yPixel = (int) Math.round(getHeight() / 2.0 - yDiff1);
		return new Point(xPixel, yPixel);
	}

	/**
	 * Selects a person if any person is at the given x and y pixel position.
	 *
	 * @param settlementPosition Position to search for
	 * @return selectedPerson;
	 */
	private Person selectPersonAt(LocalPosition settlementPosition) {

		// Note 1: Not using settlement.getIndoorPeople() for now since it doesn't
		// 		   include those who have stepped outside
		// Note 2: This should include non-associated people from other settlements
		//         in the vicinity of this settlement
		// Note 3: Could create a list of people not being out there on a mission as well as
		//         those visiting this settlement to shorten the execution time to find people
		for (Person person : CollectionUtils.getPeopleInSettlementVicinity(settlement, false)) {
			if (person.getPosition().getDistanceTo(settlementPosition) <= SELECTION_RANGE) {
				selectPerson(person);
				return person;

			}
		}
		return null;
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
		}
	}

	/**
	 * Displays the person on the map
	 *
	 * @param person
	 */
	public void displayPerson(Person person) {
		if (settlement != null && person != null)
			selectedPerson.put(settlement, person);
	}

	/**
	 * Gets the selected person for the current settlement.
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
	 * Selects the robot if any robot is at the given x and y pixel position.
	 *
	 * @param settlementPosition Position to search for
	 * @return selectedRobot;
	 */
	private Robot selectRobotAt(LocalPosition settlementPosition) {

		for (Robot robot : CollectionUtils.getAssociatedRobotsInSettlementVicinity(settlement)) {
			if (robot.getPosition().getDistanceTo(settlementPosition) <= SELECTION_RANGE) {
				selectRobot(robot);
				return robot;
			}
		}
		return null;
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
		}
	}

	/**
	 * Displays the robot on the map.
	 *
	 * @param robot
	 */
	public void displayRobot(Robot robot) {
		if (settlement != null && robot != null)
			selectedRobot.put(settlement, robot);
	}

	/**
	 * Gets the selected Robot for the current settlement.
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
	 * Is a position within the bounds of an Object ?
	 * This should be in a common class.
	 *
	 * @param pos the mouse pointer position under settlement positioning system
	 * @param obj
	 * @return
	 */
	private static boolean isWithin(LocalPosition pos, LocalBoundedObject obj) {
		double oW = obj.getWidth();
		double oL = obj.getLength();
		int facing = (int) obj.getFacing();
		// The center position of the object
		double oX = obj.getPosition().getX();
		double oY = obj.getPosition().getY();
		// Half the width and length
		double hX = 0;
		double hY = 0;

		if (facing == 0) {
			hX = oW / 2D;
			hY = oL / 2D;
		} else if (facing == 90) {
			hY = oW / 2D;
			hX = oL / 2D;
		}
		// Loading Dock Garage
		if (facing == 180) {
			hX = oW / 2D;
			hY = oL / 2D;
		} else if (facing == 270) {
			hY = oW / 2D;
			hX = oL / 2D;
		}

		// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
		// Fortunately, they both have the same width and length
		else if (facing == 45) {
			hY = oW / 2D;
			hX = oL / 2D;
		} else if (facing == 135) {
			hY = oW / 2D;
			hX = oL / 2D;
		}

		// Mouse pointer position under the settlement positioning system
		double mX = pos.getX();
		double mY = pos.getY();

		double rangeX = Math.round((mX - oX) * 100.0) / 100.0;
		double rangeY = Math.round((mY - oY) * 100.0) / 100.0;

		return Math.abs(rangeX) <= Math.abs(hX) && Math.abs(rangeY) <= Math.abs(hY);
	}

	/**
	 * Is a position within the bounds of an Object ?
	 * This should be in a common class.
	 *
	 * @param pos the mouse pointer position under settlement coordinate system
	 * @param obj
	 * @return
	 */
	private static LocalPosition convertToBuildingLoc(LocalPosition pos, LocalBoundedObject obj) {
		double oW = obj.getWidth();
		double oL = obj.getLength();
		int facing = (int) obj.getFacing();
		// The center position of the object
		double oX = obj.getPosition().getX();
		double oY = obj.getPosition().getY();
		// Half the width and length
		double hX = 0;
		double hY = 0;

		if (facing == 0) {
			hX = oW / 2D;
			hY = oL / 2D;
		} else if (facing == 90) {
			hY = oW / 2D;
			hX = oL / 2D;
		}
		// Loading Dock Garage
		if (facing == 180) {
			hX = oW / 2D;
			hY = oL / 2D;
		} else if (facing == 270) {
			hY = oW / 2D;
			hX = oL / 2D;
		}

		// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
		// Fortunately, they both have the same width and length
		else if (facing == 45) {
			hY = oW / 2D;
			hX = oL / 2D;
		} else if (facing == 135) {
			hY = oW / 2D;
			hX = oL / 2D;
		}

		// Mouse pointer position under the settlement positioning system
		double mX = pos.getX();
		double mY = pos.getY();

		double rangeX = Math.round((mX - oX) * 100.0) / 100.0;
		double rangeY = Math.round((mY - oY) * 100.0) / 100.0;

		boolean isWithin = Math.abs(rangeX) <= Math.abs(hX) && Math.abs(rangeY) <= Math.abs(hY);

		if (isWithin)
			return new LocalPosition(rangeX, rangeY);
		else
			return null;
	}

	/**
	 * Selects a building.
	 *
	 * @param settlementPosition Position to search
	 * @return selectedBuilding
	 */
	private Building selectBuildingAt(LocalPosition settlementPosition) {
		for (Building building : settlement.getBuildingManager().getBuildingSet()) {
			if (!building.getInTransport() && isWithin(settlementPosition, building)) {
				selectBuilding(building);
				return building;
			}
		}
		return null;
	}

	/**
	 * Selects a construction site.
	 *
	 * @param settlementPosition Position to search
	 * @return selected construction site
	 */
	private ConstructionSite selectConstructionSiteAt(LocalPosition settlementPosition) {
		for (ConstructionSite s : settlement.getConstructionManager().getConstructionSites()) {
			if (isWithin(settlementPosition, s)) {
				return s;
			}
		}

		return null;
	}

	/**
	 * Selects a vehicle.
	 *
	 * @param settlementPosition Poition to search
	 * @return selectedVehicle
	 */
	private Vehicle selectVehicleAt(LocalPosition settlementPosition) {
		for (Vehicle vehicle : settlement.getParkedGaragedVehicles()) {
			double width = vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange = width / 2.0;
			else
				newRange = length / 2.0;

			if (vehicle.getPosition().getDistanceTo(settlementPosition) <= newRange) {
				selectVehicle(vehicle);
				return vehicle;
			}
		}
		return null;
	}

	/**
	 * Selects a vehicle on the map.
	 *
	 * @param vehicle the selected vehicle.
	 */
	public void selectVehicle(Vehicle vehicle) {
		if ((settlement != null) && (vehicle != null)) {
			Vehicle currentlySelected = selectedVehicle.get(settlement);
			if (vehicle.equals(currentlySelected)) {
				selectedVehicle.put(settlement, null);
			} else {
				selectedVehicle.put(settlement, vehicle);
			}
		}
	}

	/**
	 * Gets the selected vehicle for the current settlement.
	 *
	 * @return the selected vehicle.
	 */
	public Vehicle getSelectedVehicle() {
		Vehicle result = null;
		if (settlement != null) {
			result = selectedVehicle.get(settlement);
		}
		return result;
	}

	/**
	 * Selects a building on the map.
	 *
	 * @param building the selected building.
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
	 * Gets the selected building for the current settlement.
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
	 * Is a display option enabled?
	 *
	 * @param op
	 * @return
	 */
	public boolean isOptionDisplayed(DisplayOption op) {
		return displayOptions.contains(op);
	}

	/**
	 * Toggle a display option, i.e if not set enable and vice versa.
	 *
	 * @param op Display option to toggle
	 */
	void toggleDisplayOption(DisplayOption op) {
		if (!displayOptions.remove(op)) {
			displayOptions.add(op);
		}
		repaint();
	}

	/**
	 * Reverses the settings of the Spot label.
	 *
	 * @param possible The range of possible values
	 */
	void reverseSpotLabels(Collection<FunctionType> possible) {
		if (!showSpotLabels.isEmpty()) {
			showSpotLabels.clear();
		} else {
			showSpotLabels.addAll(possible);
		}
	}

	/**
	 * Clears the settings of the Spot label.
	 */
	void clearSpotLabels() {
		showSpotLabels.clear();
	}

	/**
	 * Checks if building spots should be displayed.
	 *
	 * @param ft
	 * @return true if building activity spots should be displayed.
	 */
	boolean isShowSpotLabels(FunctionType ft) {
		return showSpotLabels.contains(ft);
	}

	/**
	 * Gets all active Function Activity Spots enabled.
	 */
	Set<FunctionType> getShowSpotLabels() {
		return showSpotLabels;
	}

	/**
	 * Sets if spot labels should be displayed.
	 *
	 * @param ft
	 * @param showLabels true if spot labels should be displayed.
	 */
	void setShowSpotLabels(FunctionType ft, boolean showLabels) {
		if (showLabels) {
			this.showSpotLabels.add(ft);
		} else {
			this.showSpotLabels.remove(ft);
		}
		repaint();
	}

	public DayNightMapLayer getDayNightMapLayer() {
		return dayNightMapLayer;
	}

	/**
	 * Exposes the shared scaled-icon cache for layers that rasterize scalable art (e.g., SVG).
	 * Layers may key by asset identifier + {@link #getScale()}.
	 */
	public ScaledIconCache getIconCache() {
		return iconCache;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (desktop != null && settlementWindow != null && settlementWindow.isShowing()
				&& desktop.isToolWindowOpen(SettlementWindow.NAME)) {
			Graphics2D g2d = (Graphics2D) g.create();
			try {
				g2d.setFont(sansSerif);

				// Set graphics rendering hints.
				// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				// g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				// g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

				float scaleMod = 1f;
				if (scale > 1) scaleMod = (float) Math.sqrt(scale);

				// Display all map layers.
				MapViewPoint viewpoint = new MapViewPoint(g2d, xPos, yPos, getWidth(), getHeight(), rotation, (float) scale, scaleMod);
				for (SettlementMapLayer layer : mapLayers) {
					layer.displayLayer(settlement, viewpoint);
				}
			} finally {
				g2d.dispose(); // ensure any child Graphics resources are freed
			}
		}
	}

	public SettlementTransparentPanel getSettlementTransparentPanel() {
		return settlementTransparentPanel;
	}

	void update(ClockPulse pulse) {
		// Clock pulses arrive on worker threads (MasterClock/ThreadPool). Swing must be updated on the EDT.
		if (uiUpdateScheduled.compareAndSet(false, true)) {
			SwingUtilities.invokeLater(() -> {
				try {
					if (settlementTransparentPanel != null) {
						settlementTransparentPanel.update(pulse);
					}
					// If layers request repaints, they’ll do so on EDT; avoid repaint flood.
				} finally {
					uiUpdateScheduled.set(false);
				}
			});
		}
		// repaint(); // avoid repaint flood; layers request repaints when needed
	}

	/**
	 * Gets the user display settings.
	 */
	Properties getUIProps() {
		Properties props = new Properties();
		if (settlement != null) {
			props.setProperty(SETTLEMENT_PROP, settlement.getName());
		}

		for (DisplayOption op : DisplayOption.values()) {
			props.setProperty(op.name(), Boolean.toString(displayOptions.contains(op)));
		}

		props.setProperty(X_PROP, Double.toString(xPos));
		props.setProperty(Y_PROP, Double.toString(yPos));
		props.setProperty(ROTATION_PROP, Double.toString(rotation));
		props.setProperty(SCALE_PROP, Double.toString(scale));

		for (FunctionType ft : showSpotLabels) {
			props.setProperty(SPOT_LBL_PROP + ft.name(), "true");
		}
		return props;
	}

	/**
	 * Cleans up the map panel for disposal.
	 */
	public void destroy() {

		// Stop timers and free caches
		if (zoomCoalesceTimer != null) {
			zoomCoalesceTimer.stop();
			zoomCoalesceTimer = null;
		}
		iconCache.clear();

		// Remove listeners to prevent leaks
		removeInteractionListeners();

		menu = null;
		settlement = null;
		settlementWindow = null;

		// Destroy all map layers (this includes dayNightMapLayer).
		if (mapLayers != null) {
			mapLayers.forEach(SettlementMapLayer::destroy);
			mapLayers.clear();
			mapLayers = null;
		}

		if (selectedPerson != null) selectedPerson.clear();
		if (selectedRobot != null) selectedRobot.clear();
		if (selectedBuilding != null) selectedBuilding.clear();
		if (selectedVehicle != null) selectedVehicle.clear();
		selectedRobot = null;
		selectedPerson = null;
		selectedBuilding = null;
		selectedVehicle = null;

		// dayNightMapLayer was already destroyed via mapLayers loop; just null it out.
		dayNightMapLayer = null;

		if (showSpotLabels != null) {
			showSpotLabels.clear();
			showSpotLabels = null;
		}

		if (displayOptions != null) {
			displayOptions.clear();
			displayOptions = null;
		}

		if (settlementTransparentPanel != null) {
			settlementTransparentPanel.destroy();
			settlementTransparentPanel = null;
		}
	}

	// =====================================================================
	//                    Scaled Icon LRU Cache (Soft-ref)
	// =====================================================================

	/**
	 * Small LRU cache for rasterized icons/images keyed by (resourceId, scale).
	 * Uses {@link SoftReference} values so the GC can reclaim under pressure.
	 * <p>
	 * This is a hook for map layers (e.g., buildings, vehicles) to reuse
	 * rasterizations at the active {@link SettlementMapPanel#getScale()} rather than
	 * recreating the same {@link BufferedImage} repeatedly while the user drags
	 * the zoom slider.
	 * </p>
	 */
	public static final class ScaledIconCache {
		private static final int MAX_ENTRIES = 256;

		private static final class Key {
			final String resourceId;
			final double scale;

			Key(String resourceId, double scale) {
				this.resourceId = resourceId;
				this.scale = scale;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (!(o instanceof Key)) return false;
				Key k = (Key) o;
				return Double.doubleToLongBits(scale) == Double.doubleToLongBits(k.scale)
						&& resourceId.equals(k.resourceId);
			}

			@Override
			public int hashCode() {
				long bits = Double.doubleToLongBits(scale);
				return 31 * resourceId.hashCode() + (int) (bits ^ (bits >>> 32));
			}
		}

		private final LinkedHashMap<Key, SoftReference<BufferedImage>> cache =
				new LinkedHashMap<>(64, 0.75f, true) {
					@Override
					protected boolean removeEldestEntry(Map.Entry<Key, SoftReference<BufferedImage>> e) {
						return size() > MAX_ENTRIES;
					}
				};

		public synchronized BufferedImage get(String id, double scale) {
			SoftReference<BufferedImage> ref = cache.get(new Key(id, scale));
			return ref != null ? ref.get() : null;
		}

		public synchronized void put(String id, double scale, BufferedImage img) {
			cache.put(new Key(id, scale), new SoftReference<>(img));
		}

		public synchronized void clear() {
			cache.clear();
		}
	}
}
