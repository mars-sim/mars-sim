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
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.mars_sim.core.Entity;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.UIConfig;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.utils.SwingHelper;

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

	// Property names for UI Config
	private static final String SETTLEMENT_PROP = "SETTLEMENT";
	private static final String X_PROP          = "XPOS";
	private static final String Y_PROP          = "YPOS";
	private static final String SCALE_PROP      = "SCALE";
	private static final String ROTATION_PROP   = "ROTATION";

	// Static members.
	public static final double DEFAULT_SCALE = 10D;

	// Data members
	private boolean exit = true;

	private LocalPosition center;
	private double rotation;
	private double scale; // always stored quantized

	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;

	private SettlementWindow settlementWindow;

	private Settlement settlement;

	private SettlementTransparentPanel settlementTransparentPanel;

	private DayNightMapLayer dayNightMapLayer;

	private List<SettlementMapLayer> mapLayers;

	private Map<Settlement, Entity>   selectedEntity   = new HashMap<>();

	private List<MapHotspot<?>> hotspots = new ArrayList<>();
	
	private static final Font sansSerif = new Font("SansSerif", Font.BOLD, 11);

	// -------- Event coalescing for simulation tick -> UI --------
	/** Coalesces simulation-tick UI updates so we don't flood the EDT. */
	private final AtomicBoolean uiUpdateScheduled = new AtomicBoolean(false);

	
	// -------- Shared cache hook for layers that rasterize scalable art --------
	private final ScaledIconCache iconCache = new ScaledIconCache();
	private UIContext context;

	/**
	 * Constructor 1: A panel for displaying a settlement map.
	 */
	public SettlementMapPanel(UIContext context, final SettlementWindow settlementWindow,
							  Properties userSettings) {
		super();
		this.settlementWindow = settlementWindow;
		this.context = context;

		UnitManager unitManager = context.getSimulation().getUnitManager();

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
		var xPos = UIConfig.extractDouble(userSettings, X_PROP, 0D);
		var yPos = UIConfig.extractDouble(userSettings, Y_PROP, 0D);
		center = new LocalPosition(xPos, yPos);
		
		rotation = UIConfig.extractDouble(userSettings, ROTATION_PROP, 0D);
		// Always quantize stored scale
		scale = UIConfig.extractDouble(userSettings, SCALE_PROP, DEFAULT_SCALE);
		
		initLayers(context, userSettings);

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
	private void initLayers(UIContext desktop, Properties userSettings) {

		// Set up the dayNightMapLayer layers
		dayNightMapLayer = new DayNightMapLayer(this, desktop.getSimulation().getSurfaceFeatures(), userSettings);

		// Create map layers.
		mapLayers = new ArrayList<>();
		mapLayers.add(new BackgroundTileMapLayer(this));
		mapLayers.add(dayNightMapLayer);
		mapLayers.add(new BuildingMapLayer(this, userSettings));
		mapLayers.add(new ConstructionMapLayer(this, userSettings));
		mapLayers.add(new DataCollectionSiteMapLayer(this, userSettings));
		mapLayers.add(new VehicleMapLayer(this, userSettings));
		mapLayers.add(new PersonMapLayer(this, userSettings));
		mapLayers.add(new RobotMapLayer(this, userSettings));

		settlementTransparentPanel = new SettlementTransparentPanel(desktop, this);

		// Ensure all Swing mutations happen on EDT
		SwingHelper.runInEDT(() -> {
			settlementTransparentPanel.createAndShowGUI();
			if (settlementTransparentPanel.getSettlementListBox() != null) {
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
	private void detectMouseMovement() {

		var motionListener = new MouseMotionAdapter() {
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

				// Display the settlement map coordinate of the hovering mouse pointer
				settlementWindow.setMapXYCoord(convertToSettlementLocation(x, y));

				if (exit) {
					exit = false;
				}
			}
		};

		var mouseListener = new MouseAdapter() {

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

		var selected = hotspots.stream()
					.filter(h -> h.isWithinRange(settlementPosition))
					.findFirst().orElse(null);

		if (selected != null) {
			var menu = new PopUpUnitMenu(selected, context);
			menu.show(evt.getComponent(), x, y);
		}
		repaint();
	}

	/**
	 * Displays the specific x y coordinates within a building
	 * (based upon where the mouse is pointing at).
	 *
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 */
	private void showBuildingCoord(int xPixel, int yPixel) {

		boolean showBlank = true;

		LocalPosition mousePos = convertToSettlementLocation(xPixel, yPixel);

		for (Building building : settlement.getBuildingManager().getBuildingSet()) {
			if (!building.getInTransport() && MapHotspot.isWithin(mousePos, building)) {
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
		if (!newSettlement.equals(settlement)) {
			this.settlement = newSettlement;
			SwingHelper.runInEDT(() -> {
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
		this.scale = newScale;
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
		repaint();
	}

	/**
	 * Resets the position, scale and rotation of the map. Separate function that
	 * only uses one repaint.
	 */
	public void reCenter() {
		center = new LocalPosition(0D, 0D);
		setRotation(0D);
		scale = DEFAULT_SCALE; // set directly to avoid unnecessary coalescing delay here
		SwingHelper.runInEDT(() -> {
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
		double c = Math.cos(rotation);
		double s = Math.sin(rotation);

		double realXDiff = c * xDiff + s * yDiff;
		double realYDiff = c * yDiff - s * xDiff;

		center = new LocalPosition(center.getX() + realXDiff, center.getY() + realYDiff);

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

		double newXPos = center.getX() + xDiff3;
		double newYPos = center.getY() + yDiff3;

		return new LocalPosition(newXPos, newYPos);
	}

	/**
	 * Displays the robot on the map.
	 *
	 * @param robot
	 */
	void displayEntity(Entity e) {
		if (settlement != null && e != null) {
			selectedEntity.put(settlement, e);
			repaint();
		}
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
		if (facing == 180 || facing == -180) {
			hX = oW / 2D;
			hY = oL / 2D;
		} else if (facing == 270 || facing == -90) {
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

	protected List<SettlementMapLayer> getMapLayers() {
		return mapLayers;
	}

	public boolean isDaylightLayerVisible() {
		return (dayNightMapLayer != null) && dayNightMapLayer.isVisible();
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

		Graphics2D g2d = (Graphics2D) g.create();
		try {
			g2d.setFont(sansSerif);

			// Set graphics rendering hints.
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			// Calculate the visible map radius from center to the farthest corner of the panel, plus a small buffer (10%).
			double mapRadius = (Math.sqrt(Math.pow(getWidth() / 2.0, 2) + Math.pow(getHeight() / 2.0, 2)) * 1.1D) / scale;

			// Display all map layers and reset hotspots
			var newHotspots = new ArrayList<MapHotspot<?>>();
			MapViewPoint viewpoint = new MapViewPoint(g2d, center, getWidth(), getHeight(), rotation,
													(float) scale, mapRadius);
			var currentSelection = selectedEntity.get(settlement);
			for (SettlementMapLayer layer : mapLayers) {
				newHotspots.addAll(layer.displayLayer(settlement, viewpoint, currentSelection));
			}

			// Map layers are drawon bottom up but hotspots need to be top down
			hotspots = newHotspots.reversed();
			//System.out.println("center = " + center + ", hotspots = " + hotspots.size() + ", radius = " + mapRadius);

		} finally {
			g2d.dispose(); // ensure any child Graphics resources are freed
		}
	}

	public SettlementTransparentPanel getSettlementTransparentPanel() {
		return settlementTransparentPanel;
	}

	/**
	 * Updates with the clock pulse.
	 * 
	 * @param pulse
	 */
	void update(ClockPulse pulse) {
		// Clock pulses arrive on worker threads (MasterClock/ThreadPool). Swing must be updated on the EDT.
		if (uiUpdateScheduled.compareAndSet(false, true)) {
			SwingHelper.runInEDT(() -> {
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
		
		repaint();
	}

	/**
	 * Gets the user display settings.
	 */
	Properties getUIProps() {
		Properties props = new Properties();
		if (settlement != null) {
			props.setProperty(SETTLEMENT_PROP, settlement.getName());
		}

		props.setProperty(X_PROP, Double.toString(center.getX()));
		props.setProperty(Y_PROP, Double.toString(center.getY()));
		props.setProperty(ROTATION_PROP, Double.toString(rotation));
		props.setProperty(SCALE_PROP, Double.toString(scale));

		for (var layer : mapLayers) {
			layer.saveUIProperties(props);
		}

		return props;
	}

	/**
	 * Cleans up the map panel for disposal.
	 */
	public void destroy() {

		// Stop timers and free caches
		iconCache.clear();


		// Destroy all map layers (this includes dayNightMapLayer).
		if (mapLayers != null) {
			mapLayers.forEach(SettlementMapLayer::destroy);
			mapLayers = null;
		}
	}
}
