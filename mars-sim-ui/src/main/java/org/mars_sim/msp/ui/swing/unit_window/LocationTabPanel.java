/*
 * Mars Simulation Project
 * LocationTabPanel.java
 * @date 2021-12-20
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.environment.TerrainElevation;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import eu.hansolo.steelseries.gauges.DisplayCircular;
import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.BackgroundColor;
import eu.hansolo.steelseries.tools.FrameDesign;
import eu.hansolo.steelseries.tools.LcdColor;

/**
 * The LocationTabPanel is a tab panel for location information.
 */
@SuppressWarnings("serial")
public class LocationTabPanel extends TabPanel implements ActionListener{

	/** default logger. */
	private static final Logger logger = Logger.getLogger(LocationTabPanel.class.getName());

	private static final String MAP_ICON = NavigatorWindow.ICON;

	private static final String N = "N";
	private static final String S = "S";
	private static final String E = "E";
	private static final String W = "W";

	private double elevationCache;

	private String locationStringCache;

	private Unit containerCache;
	private Unit topContainerCache;

	private JComboBox combox;

	private Coordinates locationCache;

	private JButton locatorButton;
	private SettlementMapPanel mapPanel;

	private DisplaySingle lcdLong;
	private DisplaySingle lcdLat;
	private DisplaySingle lcdText; 
	private DisplayCircular gauge;

	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(null, ImageLoader.getIconByName(MAP_ICON), Msg.getString("LocationTabPanel.title"), unit, desktop);

		locationStringCache = unit.getLocationTag().getExtendedLocation();
		containerCache = unit.getContainerUnit();
		topContainerCache = unit.getTopContainerUnit();
	}

	@Override
	protected void buildUI(JPanel content) {

		Unit unit = getUnit();
		Unit container = unit.getContainerUnit();
		if (containerCache != container) {
			containerCache = container;
		}

		Unit topContainer = unit.getTopContainerUnit();
		if (topContainerCache != topContainer) {
			topContainerCache = topContainer;
		}

		mapPanel = getDesktop().getSettlementWindow().getMapPanel();

		combox = mapPanel.getSettlementTransparentPanel().getSettlementListBox();

		// Create location panel
		JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
		locationPanel.setBorder(new MarsPanelBorder());
		locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		content.add(locationPanel);

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

		JPanel northPanel = new JPanel(new FlowLayout());
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
		lcdLat.setLcdDecimals(4);
		lcdLat.setSize(new Dimension(150, 45));
		lcdLat.setMaximumSize(new Dimension(150, 45));
		lcdLat.setPreferredSize(new Dimension(150, 45));
		lcdLat.setVisible(true);

		northPanel.add(lcdLat);

		elevationCache = Math.round(TerrainElevation.getMOLAElevation(unit.getCoordinates()) * 1000.0) / 1000.0;

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
		locatorButton = new JButton(ImageLoader.getIconByName(NavigatorWindow.ICON));

		locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
		locatorButton.addActionListener(this);
		locatorButton.setOpaque(false);
		locatorButton.setToolTipText("Locate the unit on Mars Navigator");
		locatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));// new Cursor(Cursor.HAND_CURSOR));

		JPanel locatorPane = new JPanel(new FlowLayout());
		locatorPane.add(locatorButton);

		northPanel.add(locatorPane);

		JPanel lcdPanel = new JPanel();
		lcdLong = new DisplaySingle();
		// lcdLong.setCustomLcdForeground(getForeground());
		lcdLong.setLcdUnitString(dir_E_W);
		lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));
		lcdLong.setLcdInfoString("Longitude");
		lcdLong.setLcdColor(LcdColor.BEIGE_LCD);
//		lcdLong.setBackgroundColor(BackgroundColor.LINEN);
		lcdLong.setGlowColor(Color.yellow);
		lcdLong.setDigitalFont(true);
		lcdLong.setLcdDecimals(4);
		lcdLong.setSize(new Dimension(150, 45));
		lcdLong.setMaximumSize(new Dimension(150, 45));
		lcdLong.setPreferredSize(new Dimension(150, 45));
		lcdLong.setVisible(true);
		lcdPanel.add(lcdLong);
		northPanel.add(lcdPanel);

		JPanel gaugePanel = new JPanel();
		gauge = new DisplayCircular();
		gauge.setSize(new Dimension(120, 120));
		gauge.setMaximumSize(new Dimension(120, 120));
		gauge.setPreferredSize(new Dimension(120, 120));
		setGauge(gauge, elevationCache);
		gaugePanel.add(gauge);
		
		locationPanel.add(gaugePanel, BorderLayout.CENTER);

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
        lcdText.setLcdTextScrolling(true);

		locationPanel.add(lcdText, BorderLayout.SOUTH);

		updateLocationBanner(unit);

		checkTheme(true);
	}

	public void checkTheme(boolean firstRun) {
		lcdText.setLcdColor(LcdColor.DARKBLUE_LCD);
		gauge.setFrameDesign(FrameDesign.STEEL);
//		locatorButton.setIcon(ImageLoader.getIcon(FIND_ORANGE));
	}

	private void setGauge(DisplayCircular gauge, double elevationCache) {

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
		gauge.setMinValue(min);
		gauge.setMaxValue(max);
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

	private void personUpdate(Person p) {
		MainDesktopPane desktop = getDesktop();
		
		if (p.isInSettlement()) {
			desktop.openToolWindow(SettlementWindow.NAME);

			combox.setSelectedItem(p.getSettlement());

			Building b = p.getBuildingLocation();
			double xLoc = b.getPosition().getX();
			double yLoc = b.getPosition().getY();
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

				double xLoc = vv.getPosition().getX();
				double yLoc = vv.getPosition().getY();
				double scale = mapPanel.getScale();
//				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
//				mapPanel.setShowVehicleLabels(true);

				if (mapPanel.getSelectedPerson() != null && mapPanel.getSelectedPerson() != p)
					mapPanel.selectPerson(p);

			}
		}

		else if (p.isOutside()) {
			Vehicle vv = p.getVehicle();

			if (vv == null) {

				Settlement s = p.findSettlementVicinity();

				if (s != null) {
					desktop.openToolWindow(SettlementWindow.NAME);

					// NOTE: Case 1 : person is on a mission on the surface of Mars and just happens
					// to step outside the vehicle temporarily

					// NOTE: Case 2 : person just happens to step outside the settlement at its
					// vicinity temporarily

					combox.setSelectedItem(s);

					double scale = mapPanel.getScale();
					mapPanel.reCenter();
					mapPanel.moveCenter(p.getPosition().getX() * scale, p.getPosition().getY() * scale);
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

	private void robotUpdate(Robot r) {
		MainDesktopPane desktop = getDesktop();

		if (r.isInSettlement()) {
			desktop.openToolWindow(SettlementWindow.NAME);

			combox.setSelectedItem(r.getSettlement());

			Building b = r.getBuildingLocation();
			double xLoc = b.getPosition().getX();
			double yLoc = b.getPosition().getY();
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

				double xLoc = vv.getPosition().getX();
				double yLoc = vv.getPosition().getY();
				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
				mapPanel.setShowVehicleLabels(true);

				if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
					mapPanel.selectRobot(r);
			}
		}

		else if (r.isOutside()) {
			Settlement s = r.findSettlementVicinity();

			if (s != null) {
				desktop.openToolWindow(SettlementWindow.NAME);

				// NOTE: Case 1 : person is on a mission on the surface of Mars and just happens
				// to step outside the vehicle temporarily

				// NOTE: Case 2 : person just happens to step outside the settlement at its
				// vicinity temporarily

				combox.setSelectedItem(s);

				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(r.getPosition().getX() * scale, r.getPosition().getY() * scale);
//				mapPanel.setShowBuildingLabels(true);

				if (mapPanel.getSelectedRobot() != null && mapPanel.getSelectedRobot() != r)
					mapPanel.selectRobot(r);
			} else
				// he's stepped outside a vehicle
				desktop.centerMapGlobe(r.getCoordinates());
		}
	}

	private void vehicleUpdate(Vehicle v) {
		MainDesktopPane desktop = getDesktop();

		if (v.getSettlement() != null) {
			desktop.openToolWindow(SettlementWindow.NAME);
			combox.setSelectedItem(v.getSettlement());

			double xLoc = v.getPosition().getX();
			double yLoc = v.getPosition().getY();
			double scale = mapPanel.getScale();
			mapPanel.reCenter();
			mapPanel.moveCenter(xLoc * scale, yLoc * scale);
			mapPanel.setShowVehicleLabels(true);
		} else {
			// out there on a mission
			desktop.openToolWindow(NavigatorWindow.NAME);
			desktop.centerMapGlobe(getUnit().getCoordinates());
		}
	}

	private void equipmentUpdate(Equipment e) {
		MainDesktopPane desktop = getDesktop();

		if (e.isInSettlement()) {
			desktop.openToolWindow(SettlementWindow.NAME);
			combox.setSelectedItem(e.getSettlement());
		}

		else if (e.isInVehicle()) {

			Vehicle vv = e.getVehicle();
			if (vv.getSettlement() == null) {
				// out there on a mission
				desktop.centerMapGlobe(e.getCoordinates());
			} else {
				// still parked inside a garage or within the premise of a settlement
				desktop.openToolWindow(SettlementWindow.NAME);
				combox.setSelectedItem(vv.getSettlement());

				double xLoc = vv.getPosition().getX();
				double yLoc = vv.getPosition().getY();
				double scale = mapPanel.getScale();
				mapPanel.reCenter();
				mapPanel.moveCenter(xLoc * scale, yLoc * scale);
				mapPanel.setShowVehicleLabels(true);
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

			// NOTE: should it open the unit window also ?
			// desktop.openUnitWindow(unit.getContainerUnit(), false);

			update();

			Unit unit = getUnit();
			if (unit.getUnitType() == UnitType.PERSON) {
				personUpdate((Person) unit);
			}

			else if (unit.getUnitType() == UnitType.ROBOT) {
				robotUpdate((Robot) unit);
			}

			else if (unit.getUnitType() == UnitType.VEHICLE) {
				vehicleUpdate((Vehicle) unit);
			}

			else if (unit.getUnitType() == UnitType.CONTAINER
					|| unit.getUnitType() == UnitType.EVA_SUIT) {
				equipmentUpdate((Equipment) unit);
			}
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		Unit unit = getUnit();
		
		// If unit's location has changed, update location display.
		// TODO: if a person goes outside the settlement for servicing an equipment
		// does the coordinate (down to how many decimal) change?
		Coordinates location = unit.getCoordinates();
		if (!locationCache.equals(location)) {
			locationCache = location;

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

			lcdLat.setLcdUnitString(dir_N_S);
			lcdLong.setLcdUnitString(dir_E_W);
			lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
			lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

			double elevationCache = Math.round(TerrainElevation.getMOLAElevation(location)
					* 1000.0) / 1000.0;

			setGauge(gauge, elevationCache);

		}

		// Update location button or location text label as necessary.
		Unit container = unit.getContainerUnit();
		if (containerCache != container) {
			containerCache = container;
		}

		Unit topContainer = unit.getTopContainerUnit();
		if (topContainerCache != topContainer) {
			topContainerCache = topContainer;
		}

		updateLocationBanner(unit);
	}

	/**
	 * Tracks the location of a person, bot, vehicle, or equipment
	 */
	private void updateLocationBanner(Unit unit) {

		String loc = unit.getLocationTag().getExtendedLocation();

		if (!locationStringCache.equalsIgnoreCase(loc)) {
			locationStringCache = loc;
			lcdText.setLcdText(loc);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		containerCache = null;
		topContainerCache = null;
		locationCache = null;
		locatorButton = null;
		lcdLong = null;
		lcdLat = null;
		lcdText = null;
		gauge = null;

	}
}
