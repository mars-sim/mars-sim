/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.steelseries.gauges.DisplayCircular;
import org.mars_sim.msp.ui.steelseries.gauges.DisplaySingle;
import org.mars_sim.msp.ui.steelseries.tools.BackgroundColor;
import org.mars_sim.msp.ui.steelseries.tools.FrameDesign;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;

/**
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel extends TabPanel implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	private static final String LOCATOR_ORANGE = "locator48_orange";
	private static final String LOCATOR_BLUE = "locator48_blue";

	private static final String N = "N";
	private static final String S = "S";
	private static final String E = "E";
	private static final String W = "W";
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private int themeCache;

	private double elevationCache;
	
	private String locationStringCache;
	
	private Unit containerCache;
	private Unit topContainerCache;

//	private JComboBoxMW<?> combox;
	private WebComboBox combox;

	private Coordinates locationCache;

	private WebButton locatorButton;
	private SettlementMapPanel mapPanel;

	private DisplaySingle lcdLong, lcdLat, lcdText; // lcdElev,
	private DisplayCircular gauge;// RadialQuarterN gauge;

	private static Mars mars;
	private static TerrainElevation terrainElevation;
	private static Simulation sim = Simulation.instance();
	
	/**
	 * Constructor.
	 * 
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("LocationTabPanel.title"), null, Msg.getString("LocationTabPanel.tooltip"), unit, desktop);

		this.unit = unit;
		
		locationStringCache = unit.getLocationTag().getExtendedLocation();
		containerCache = unit.getContainerUnit();
		topContainerCache = unit.getTopContainerUnit();
	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		Unit container = unit.getContainerUnit();
		if (containerCache != container) {
			containerCache = container;
		}

		Unit topContainer = unit.getTopContainerUnit();
		if (topContainerCache != topContainer) {
			topContainerCache = topContainer;
		}

		if (terrainElevation == null)
			terrainElevation = Simulation.instance().getMars().getSurfaceFeatures().getTerrainElevation();

		mapPanel = desktop.getSettlementWindow().getMapPanel();

		combox = mapPanel.getSettlementTransparentPanel().getSettlementListBox();

		// Initialize location header.
		WebPanel titlePane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		WebLabel titleLabel = new WebLabel(Msg.getString("LocationTabPanel.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		// titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);

		// Create location panel
		WebPanel locationPanel = new WebPanel(new BorderLayout(5, 5));
		locationPanel.setBorder(new MarsPanelBorder());
		locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		topContentPanel.add(locationPanel);

		// Initialize location cache
		locationCache = new Coordinates(unit.getCoordinates());

		String dir_N_S = null;
		String dir_E_W = null;
		if (locationCache.getLatitudeDouble() >= 0)
			dir_N_S = Msg.getString("direction.degreeSign") + "N";
		else
			dir_N_S = Msg.getString("direction.degreeSign") + "S";

		if (locationCache.getLongitudeDouble() >= 0)
			dir_E_W = Msg.getString("direction.degreeSign") + "E";
		else
			dir_E_W = Msg.getString("direction.degreeSign") + "W";

		WebPanel northPanel = new WebPanel(new FlowLayout());
		locationPanel.add(northPanel, BorderLayout.NORTH);

		lcdLat = new DisplaySingle();
		lcdLat.setLcdUnitString(dir_N_S);
		lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
		lcdLat.setLcdInfoString("Latitude");
		// lcd1.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
		lcdLat.setLcdColor(LcdColor.BEIGE_LCD);
		// lcdLat.setBackground(BackgroundColor.NOISY_PLASTIC);
		lcdLat.setGlowColor(Color.orange);
		// lcd1.setBorder(new EmptyBorder(5, 5, 5, 5));
		lcdLat.setDigitalFont(true);
		lcdLat.setLcdDecimals(2);
		lcdLat.setSize(new Dimension(150, 45));
		lcdLat.setMaximumSize(new Dimension(150, 45));
		lcdLat.setPreferredSize(new Dimension(150, 45));
		lcdLat.setVisible(true);

		northPanel.add(lcdLat);

		if (mars == null)
			mars = Simulation.instance().getMars();
		if (terrainElevation == null)
			terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();

		elevationCache = Math.round(terrainElevation.getMOLAElevation(unit.getCoordinates()) * 1000.0) / 1000.0;

		logger.info(unit.getName() 
				+ "'s coordinates: " + unit.getCoordinates()
				+ "   Elevation: " + elevationCache + " km.");

//        lcdElev = new DisplaySingle();
//        lcdElev.setLcdValueFont(new Font("Serif", Font.ITALIC, 12));
//        lcdElev.setLcdUnitString("km");
//        lcdElev.setLcdValueAnimated(elevationCache);
//        lcdElev.setLcdDecimals(3);
//        lcdElev.setLcdInfoString("Elevation");
//        //lcd0.setLcdColor(LcdColor.DARKBLUE_LCD);
//        lcdElev.setLcdColor(LcdColor.YELLOW_LCD);//REDDARKRED_LCD);
//        lcdElev.setDigitalFont(true);
//        //lcd0.setBorder(new EmptyBorder(5, 5, 5, 5));
//        lcdElev.setSize(new Dimension(150, 60));
//        lcdElev.setMaximumSize(new Dimension(150, 60));
//        lcdElev.setPreferredSize(new Dimension(150, 60));
//        lcdElev.setVisible(true);
//        locationPanel.add(lcdElev, BorderLayout.NORTH);

		// Create center map button
		locatorButton = new WebButton(ImageLoader.getIcon(LOCATOR_ORANGE));

		locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
		locatorButton.addActionListener(this);
		locatorButton.setOpaque(false);
		locatorButton.setToolTipText("Locate the unit on Mars Navigator");
		locatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));// new Cursor(Cursor.HAND_CURSOR));

		WebPanel locatorPane = new WebPanel(new FlowLayout());
		locatorPane.add(locatorButton);

		northPanel.add(locatorPane);

		lcdLong = new DisplaySingle();
		// lcdLong.setCustomLcdForeground(getForeground());
		lcdLong.setLcdUnitString(dir_E_W);
		lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));
		lcdLong.setLcdInfoString("Longitude");
		lcdLong.setLcdColor(LcdColor.BEIGE_LCD);
//		lcdLong.setBackgroundColor(BackgroundColor.LINEN);
		lcdLong.setGlowColor(Color.yellow);
		lcdLong.setDigitalFont(true);
		lcdLong.setLcdDecimals(2);
		lcdLong.setSize(new Dimension(150, 45));
		lcdLong.setMaximumSize(new Dimension(150, 45));
		lcdLong.setPreferredSize(new Dimension(150, 45));
		lcdLong.setVisible(true);
		northPanel.add(lcdLong);

		gauge = new DisplayCircular();

		setGauge(gauge, elevationCache);

		locationPanel.add(gauge, BorderLayout.CENTER);

		lcdText = new DisplaySingle();
		lcdText.setLcdInfoString("Last Known Position");
		// lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
		lcdText.setGlowColor(Color.ORANGE);
		// lcdText.setBackground(Background.SATIN_GRAY);
		lcdText.setDigitalFont(true);
		lcdText.setSize(new Dimension(150, 30));
		lcdText.setMaximumSize(new Dimension(150, 30));
		lcdText.setPreferredSize(new Dimension(150, 30));
		lcdText.setVisible(true);
		lcdText.setLcdNumericValues(false);
		lcdText.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));
		
		lcdText.setLcdText(locationStringCache);
				
		// Pause the location lcd text the sim is pause
        lcdText.setLcdTextScrolling(!sim.getMasterClock().isPaused());
		
		locationPanel.add(lcdText, BorderLayout.SOUTH);

		updateLocation();

		checkTheme(true);

	}

	public void checkTheme(boolean firstRun) {
//		if (mainScene != null) {
//			int theme = MainScene.getTheme();
//
//			if (themeCache != theme || firstRun) {
//				themeCache = theme;

//				if (theme == 7) {
//					lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
//					gauge.setFrameDesign(FrameDesign.GOLD);
//					locatorButton.setIcon(ImageLoader.getIcon(LOCATOR_ORANGE));
//				} else if (theme == 6 || theme == 0) {
					lcdText.setLcdColor(LcdColor.DARKBLUE_LCD);
					gauge.setFrameDesign(FrameDesign.STEEL);
					locatorButton.setIcon(ImageLoader.getIcon(LOCATOR_BLUE));
//				}
//			}
//		}
	}

	public void setGauge(DisplayCircular gauge, double elevationCache) {

		// Note: The peak of Olympus Mons is 21,229 meters (69,649 feet) above the Mars
		// areoid (a reference datum similar to Earth's sea level). The lowest point is
		// within the Hellas Impact Crater (marked by a flag with the letter "L").
		// The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet)
		// below the Mars areoid.

		int max = -1;
		int min = 2;

		if (elevationCache < -8) {
			max = -8;
			min = -9;
		} else if (elevationCache < -5) {
			max = -5;
			min = -9;
		} else if (elevationCache < -3) {
			max = -3;
			min = -5;
		} else if (elevationCache < 0) {
			max = 1;
			min = -1;
		} else if (elevationCache < 1) {
			max = 2;
			min = 0;
		} else if (elevationCache < 3) {
			max = 5;
			min = 0;
		} else if (elevationCache < 10) {
			max = 10;
			min = 5;
		} else if (elevationCache < 20) {
			max = 20;
			min = 10;
		} else if (elevationCache < 30) {
			max = 30;
			min = 20;
		}

		gauge.setDisplayMulti(false);
		gauge.setDigitalFont(true);
		// gauge.setFrameDesign(FrameDesign.GOLD);
		// gauge.setOrientation(Orientation.EAST);//.NORTH);//.VERTICAL);
		// gauge.setPointerType(PointerType.TYPE5);
		// gauge.setTextureColor(Color.yellow);//, Texture_Color BRUSHED_METAL and
		// PUNCHED_SHEET);
		gauge.setUnitString("km");
		gauge.setTitle("Elevation");
		// gauge.setMinValue(min);
		// gauge.setMaxValue(max);
		// gauge.setTicklabelsVisible(true);
		// gauge.setMaxNoOfMajorTicks(10);
		// gauge.setMaxNoOfMinorTicks(10);
		gauge.setBackgroundColor(BackgroundColor.NOISY_PLASTIC);// .BRUSHED_METAL);
		// alt.setGlowColor(Color.yellow);
		// gauge.setLcdColor(LcdColor.BEIGE_LCD);//.BLACK_LCD);
		// gauge.setLcdInfoString("Elevation");
		// gauge.setLcdUnitString("km");
		gauge.setLcdValueAnimated(elevationCache);
		gauge.setValueAnimated(elevationCache);
		// gauge.setValue(elevationCache);
		gauge.setLcdDecimals(4);

		// alt.setMajorTickmarkType(TICKMARK_TYPE);
		// gauge.setSize(new Dimension(250, 250));
		// gauge.setMaximumSize(new Dimension(250, 250));
		// gauge.setPreferredSize(new Dimension(250, 250));

		gauge.setSize(new Dimension(250, 250));
		gauge.setMaximumSize(new Dimension(250, 250));
		gauge.setPreferredSize(new Dimension(250, 250));

		gauge.setVisible(true);

	}

	public void personUpdate(Person p) {

		if (p.isInSettlement()) {
			desktop.openToolWindow(SettlementWindow.NAME);

			combox.setSelectedItem(p.getSettlement());

			Building b = p.getBuildingLocation();
			double xLoc = b.getXLocation();
			double yLoc = b.getYLocation();
			double scale = mapPanel.getScale();
			mapPanel.reCenter();
			mapPanel.moveCenter(xLoc * scale, yLoc * scale);
//			mapPanel.setShowBuildingLabels(true);

			if (mapPanel.getSelectedPerson() != null && mapPanel.getSelectedPerson() != p)
				mapPanel.selectPerson(p);
		}

		else if (p.isInVehicle()) {

			Vehicle vv = p.getVehicle();

			if (vv.getSettlement() == null) {

				// out there on a mission
				desktop.openToolWindow(NavigatorWindow.NAME);
				desktop.centerMapGlobe(p.getCoordinates());
			} else {
				// still parked inside a garage or within the premise of a settlement
				desktop.openToolWindow(SettlementWindow.NAME);

				combox.setSelectedItem(vv.getSettlement());

				double xLoc = vv.getXLocation();
				double yLoc = vv.getYLocation();
				double scale = mapPanel.getScale();
//				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
//				mapPanel.setShowVehicleLabels(true);

				if (mapPanel.getSelectedPerson() != null && mapPanel.getSelectedPerson() != p)
					mapPanel.selectPerson(p);

			}
		}

		else if (p.isOutside()) {// .getLocationSituation() == LocationSituation.OUTSIDE) {
			Vehicle vv = p.getVehicle();

			if (vv == null) {
				
				Settlement s = p.findSettlementVicinity();
				
				if (s != null) {
					desktop.openToolWindow(SettlementWindow.NAME);
	
					// System.out.println("Just open Settlement Map Tool");
	
					// TODO: Case 1 : person is on a mission on the surface of Mars and just happens
					// to step outside the vehicle temporarily
	
					// TODO: Case 2 : person just happens to step outside the settlement at its
					// vicinity temporarily
	
					combox.setSelectedItem(s);

					double xLoc = p.getXLocation();
					double yLoc = p.getYLocation();
					double scale = mapPanel.getScale();
					mapPanel.reCenter();
					mapPanel.moveCenter(xLoc * scale, yLoc * scale);
	//				mapPanel.setShowBuildingLabels(true);
	
					if (mapPanel.getSelectedPerson() != null && mapPanel.getSelectedPerson() != p)
						mapPanel.selectPerson(p);
				}
			} 
			
			else {
				if (vv.getSettlement() == null) {

					// out there on a mission
					desktop.openToolWindow(NavigatorWindow.NAME);
					// he's stepped outside a vehicle
					desktop.centerMapGlobe(p.getCoordinates());
				}
			}
		}
	}
	
	public void robotUpdate(Robot r) {
		
		if (r.isInSettlement()) {
			desktop.openToolWindow(SettlementWindow.NAME);

			combox.setSelectedItem(r.getSettlement());

			Building b = r.getBuildingLocation();
			double xLoc = b.getXLocation();
			double yLoc = b.getYLocation();
			double scale = mapPanel.getScale();
			mapPanel.reCenter();
			mapPanel.moveCenter(xLoc * scale, yLoc * scale);
			mapPanel.setShowBuildingLabels(true);

			if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
				mapPanel.selectRobot(r);
		}

		else if (r.isInVehicle()) {

			Vehicle vv = r.getVehicle();
			if (vv.getSettlement() == null) {
				// out there on a mission
				desktop.centerMapGlobe(r.getCoordinates());
			} else {
				// still parked inside a garage or within the premise of a settlement
				desktop.openToolWindow(SettlementWindow.NAME);

				combox.setSelectedItem(vv.getSettlement());

				double xLoc = vv.getXLocation();
				double yLoc = vv.getYLocation();
				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
				mapPanel.setShowVehicleLabels(true);

				if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
					mapPanel.selectRobot(r);
			}
		}

		else if (r.isOutside()) {
//			Vehicle vv = r.getVehicle();
			
			Settlement s = r.findSettlementVicinity();
			
			if (s != null) {
				desktop.openToolWindow(SettlementWindow.NAME);

				// System.out.println("Just open Settlement Map Tool");

				// TODO: Case 1 : person is on a mission on the surface of Mars and just happens
				// to step outside the vehicle temporarily

				// TODO: Case 2 : person just happens to step outside the settlement at its
				// vicinity temporarily

				combox.setSelectedItem(s);

				double xLoc = r.getXLocation();
				double yLoc = r.getYLocation();
				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
//				mapPanel.setShowBuildingLabels(true);

				if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
					mapPanel.selectRobot(r);
			} else
				// he's stepped outside a vehicle
				desktop.centerMapGlobe(r.getCoordinates());
		}
	}
	
	public void vehicleUpdate(Vehicle v) {
		if (v.getSettlement() != null) {
			desktop.openToolWindow(SettlementWindow.NAME);
			combox.setSelectedItem(v.getSettlement());

			double xLoc = v.getXLocation();
			double yLoc = v.getYLocation();
			double scale = mapPanel.getScale();
			mapPanel.reCenter();
			mapPanel.moveCenter(xLoc * scale, yLoc * scale);
			mapPanel.setShowVehicleLabels(true);

			// mapPanel.selectVehicleAt((int)xLoc, (int)yLoc);
		} else {
			// out there on a mission
			desktop.openToolWindow(NavigatorWindow.NAME);
//			if (mainScene != null)
//				Platform.runLater(() -> mainScene.openMinimap());
			desktop.centerMapGlobe(unit.getCoordinates());
		}
	}
	
	public void equipmentUpdate(Equipment e) {
		if (e.isInSettlement()) {// .getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			desktop.openToolWindow(SettlementWindow.NAME);

			combox.setSelectedItem(e.getSettlement());

//			Building b = e.getBuildingLocation();
//			double xLoc = b.getXLocation();
//			double yLoc = b.getYLocation();
//			double scale = mapPanel.getScale();
//			mapPanel.reCenter();
//			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
//			mapPanel.setShowBuildingLabels(true);
//
//			mapPanel.selectRobot(r);
		}

		else if (e.isInVehicle()) {

			Vehicle vv = e.getVehicle();
			if (vv.getSettlement() == null) {
				// out there on a mission
				desktop.centerMapGlobe(e.getCoordinates());
			} else {
				// still parked inside a garage or within the premise of a settlement
				desktop.openToolWindow(SettlementWindow.NAME);
//				if (mainScene != null)
//					mainScene.setSettlement(vv.getSettlement());
//				else
					combox.setSelectedItem(vv.getSettlement());

				double xLoc = vv.getXLocation();
				double yLoc = vv.getYLocation();
				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
				mapPanel.setShowVehicleLabels(true);

				// mapPanel.selectVehicleAt((int)xLoc, (int)yLoc);

			}
		}

		else if (e.isOutside()) {

		}

		else
			desktop.centerMapGlobe(e.getCoordinates());
	}
	
	/**
	 * Action event occurs.
	 *
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		JComponent source = (JComponent) event.getSource();
		// If the center map button was pressed, update navigator tool.
		if (source == locatorButton) {		
			// Add codes to open the settlement map tool and center the map to
			// show the exact/building location inside a settlement if possible
			// SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();
			
			// TODO: should it open the unit window also ?
			// desktop.openUnitWindow(unit.getContainerUnit(), false);

			update();

			if (unit instanceof Person) {
				personUpdate((Person) unit);
			}

			else if (unit instanceof Robot) {
				robotUpdate((Robot) unit);
			}

			else if (unit instanceof Vehicle) {
				vehicleUpdate((Vehicle) unit);
			}

			else if (unit instanceof Equipment) {
				equipmentUpdate((Equipment) unit);
			}
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		// If unit's location has changed, update location display.
		// TODO: if a person goes outside the settlement for servicing an equipment
		// does the coordinate (down to how many decimal) change?
		Coordinates location = unit.getCoordinates();
		if (!locationCache.equals(location)) {
			locationCache.setCoords(location);

			String dir_N_S = null;
			String dir_E_W = null;

			if (locationCache.getLatitudeDouble() >= 0)
				dir_N_S = Msg.getString("direction.degreeSign") + N;
			else
				dir_N_S = Msg.getString("direction.degreeSign") + S;

			if (locationCache.getLongitudeDouble() >= 0)
				dir_E_W = Msg.getString("direction.degreeSign") + E;
			else
				dir_E_W = Msg.getString("direction.degreeSign") + W;

			lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
			lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

			if (mars == null)
				mars = Simulation.instance().getMars();
			
			if (terrainElevation == null)
				terrainElevation =  mars.getSurfaceFeatures().getTerrainElevation();

			double elevationCache = Math.round(terrainElevation.getMOLAElevation(unit.getCoordinates()) 
					* 1000.0) / 1000.0;

			setGauge(gauge, elevationCache);

		}
		
		// Pause the location lcd text the sim is pause
        lcdText.setLcdTextScrolling(!sim.getMasterClock().isPaused());

		// Update location button or location text label as necessary.
		Unit container = unit.getContainerUnit();
		if (containerCache != container) {
			containerCache = container;
		}

		Unit topContainer = unit.getTopContainerUnit();
		if (topContainerCache != topContainer) {
			topContainerCache = topContainer;
		}

		updateLocation();
		
		checkTheme(false);

	}

	/**
	 * Tracks the location of a person, bot, vehicle, or equipment
	 */
	public void updateLocation() {

		String loc = unit.getLocationTag().getExtendedLocation();

		if (!locationStringCache.equalsIgnoreCase(loc)) {
			locationStringCache = loc;
			lcdText.setLcdText(loc);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		containerCache = null;
		topContainerCache = null;
		terrainElevation = null;
		locationCache = null;
//		mainScene = null;
		locatorButton = null;
		lcdLong = null;
		lcdLat = null;
		lcdText = null;
		gauge = null;

	}

}
