/*
 * Mars Simulation Project
 * LocationTabPanel.java
 * @date 2021-12-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.equipment.Equipment;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.structure.building.Building;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;

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

	private static final String MAP_ICON = NavigatorWindow.PIN_ICON;

	private static final String N = "N";
	private static final String S = "S";
	private static final String E = "E";
	private static final String W = "W";

	private String locationStringCache;

	private Unit vicinityUnit;
	private Unit containerCache;
//	private Settlement settlementCache;
	private Building buildingCache;
	private LocationStateType locationStateTypeCache;
	
	private JLabel vicinityLabel;
	private JLabel containerLabel;
//	private JLabel settlementLabel;
	private JLabel buildingLabel;
	private JLabel locationStateLabel;
	private JLabel activitySpot;
	
	private Coordinates locationCache;

	private JButton locatorButton;

	private DisplaySingle lcdLong;
	private DisplaySingle lcdLat;
	private DisplaySingle bannerText; 
	private DisplayCircular gauge;

	private Dimension latLonDim = new Dimension(120, 30);
	private Dimension gaugeDim = new Dimension(70, 70);
	private Dimension bannerDim = new Dimension(150, 30);
	
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
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create location panel
		JPanel locationPanel = new JPanel(new BorderLayout(5, 5));
		locationPanel.setBorder(new MarsPanelBorder());
		locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		content.add(locationPanel, BorderLayout.NORTH);

		// Initialize location cache
		locationCache = getUnit().getCoordinates();

		if (locationCache == null) {
			locationCache = getUnit().getContainerUnit().getCoordinates();
		}
		

		JPanel northPanel = new JPanel(new FlowLayout());
		locationPanel.add(northPanel, BorderLayout.SOUTH);

		lcdLat = new DisplaySingle();
		lcdLat.setLcdUnitString("");
		lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
		lcdLat.setLcdInfoString("Latitude");
		lcdLat.setLcdColor(LcdColor.BEIGE_LCD);
		lcdLat.setGlowColor(Color.orange);
		lcdLat.setDigitalFont(true);
		lcdLat.setLcdDecimals(4);
		lcdLat.setSize(latLonDim);
		lcdLat.setMaximumSize(latLonDim);
		lcdLat.setPreferredSize(latLonDim);
		lcdLat.setVisible(true);

		northPanel.add(lcdLat);

		// Create center map button
		locatorButton = new JButton(ImageLoader.getIconByName(NavigatorWindow.ICON));

		locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
		locatorButton.addActionListener(this);
		locatorButton.setOpaque(false);
		locatorButton.setToolTipText("Locate the unit on Mars Navigator");
		locatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JPanel locatorPane = new JPanel(new FlowLayout());
		locatorPane.add(locatorButton);

		northPanel.add(locatorPane);

		JPanel lcdPanel = new JPanel();
		lcdLong = new DisplaySingle();
		lcdLong.setLcdUnitString("");
		lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));
		lcdLong.setLcdInfoString("Longitude");
		lcdLong.setLcdColor(LcdColor.BEIGE_LCD);
		lcdLong.setGlowColor(Color.yellow);
		lcdLong.setDigitalFont(true);
		lcdLong.setLcdDecimals(4);
		lcdLong.setSize(latLonDim);
		lcdLong.setMaximumSize(latLonDim);
		lcdLong.setPreferredSize(latLonDim);
		lcdLong.setVisible(true);
		lcdPanel.add(lcdLong);
		northPanel.add(lcdPanel);

		JPanel gaugePanel = new JPanel();
		gauge = new DisplayCircular();
		gauge.setSize(gaugeDim);
		gauge.setMaximumSize(gaugeDim);
		gauge.setPreferredSize(gaugeDim);
		setGauge(gauge, 0D);
		gaugePanel.add(gauge);
		
		locationPanel.add(gaugePanel, BorderLayout.CENTER);

		bannerText = new DisplaySingle();
		bannerText.setLcdInfoString("Last Known Position");
		bannerText.setGlowColor(Color.ORANGE);
		bannerText.setDigitalFont(true);
		bannerText.setSize(bannerDim);
		bannerText.setMaximumSize(bannerDim);
		bannerText.setPreferredSize(bannerDim);
		bannerText.setVisible(true);
		bannerText.setLcdNumericValues(false);
		bannerText.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));

		bannerText.setLcdText(locationStringCache);

		// Pause the location lcd text the sim is pause
        bannerText.setLcdTextScrolling(true);

		locationPanel.add(bannerText, BorderLayout.NORTH);

		/////

		// Prepare info panel.
		AttributePanel containerPanel = new AttributePanel(5);
		content.add(containerPanel, BorderLayout.CENTER);
		containerLabel = containerPanel.addRow("Container Unit", "");
//		settlementLabel = containerPanel.addRow("Settlement Container", "");
		locationStateLabel = containerPanel.addRow("Location State", "");
		vicinityLabel = containerPanel.addRow("Vicinity", "");
		buildingLabel = containerPanel.addRow("Building", "");
		activitySpot = containerPanel.addRow("Reserved Spot", "");
			
		updateLocationBanner(getUnit());

		bannerText.setLcdColor(LcdColor.DARKBLUE_LCD);
		gauge.setFrameDesign(FrameDesign.STEEL);

		update();
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
		gauge.setUnitString("km");
		gauge.setTitle("Elevation");
		gauge.setMinValue(min);
		gauge.setMaxValue(max);
		gauge.setBackgroundColor(BackgroundColor.NOISY_PLASTIC);
		gauge.setLcdValueAnimated(elevationCache);
		gauge.setValueAnimated(elevationCache);
		gauge.setLcdDecimals(4);
		gauge.setSize(new Dimension(220, 220));
		gauge.setMaximumSize(new Dimension(220, 220));
		gauge.setPreferredSize(new Dimension(220, 220));

		gauge.setVisible(true);

	}

	/**
	 * Updates the person.
	 * 
	 * @param p
	 */
	private void personUpdate(Person p) {
		MainDesktopPane desktop = getDesktop();
		Settlement settlement = p.getSettlement();
		boolean useSettlementTool = (settlement != null);
		vicinityUnit = null;
		
		if (p.isInVehicle()) {
			Vehicle vv = p.getVehicle();
			useSettlementTool = (vv.getSettlement() != null);
		}

		else if (p.isOutside()) {
			settlement = p.getLocationTag().findSettlementVicinity();
			if (settlement != null) {
				vicinityUnit = settlement;
				desktop.showDetails(settlement);
				useSettlementTool = (settlement != null);
			}
			else {
				Vehicle vehicle = p.getLocationTag().findVehicleVicinity();
				if (vehicle != null) {
					vicinityUnit = vehicle;
					desktop.showDetails(vehicle);
				}
			}
		}

		if (useSettlementTool) {
			SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
			sw.chooseSettlement(settlement);
			sw.displayPerson(p);
		}
//		else {
//			NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
//			nw.updateCoordsMaps(p.getCoordinates());
//		}
	}

	/**
	 * Updates the robot.
	 * 
	 * @param r
	 */
	private void robotUpdate(Robot r) {
		MainDesktopPane desktop = getDesktop();
		Settlement settlement = r.getSettlement();
		boolean useSettlementTool = (settlement != null);
		vicinityUnit = null;
		
		if (!useSettlementTool && r.isInVehicle()) {
			Vehicle vv = r.getVehicle();
			useSettlementTool = (vv.getSettlement() != null);
		}
		
		else if (r.isOutside()) {
			settlement = r.getLocationTag().findSettlementVicinity();
			if (settlement != null) {
				vicinityUnit = settlement;
				desktop.showDetails(settlement);
				useSettlementTool = (settlement != null);
			}
			else {
				Vehicle vehicle = r.getLocationTag().findVehicleVicinity();
				if (vehicle != null) {
					vicinityUnit = vehicle;
					desktop.showDetails(vehicle);
				}
			}
		}
		
		if (useSettlementTool) {
			SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
			sw.chooseSettlement(settlement);
			sw.displayRobot(r);
		}
//		else {
//			NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
//			nw.updateCoordsMaps(r.getCoordinates());
//		}
	}

	/**
	 * Updates the vehicle.
	 * 
	 * @param v
	 */
	private void vehicleUpdate(Vehicle v) {
		MainDesktopPane desktop = getDesktop();
		Settlement settlement = v.getSettlement();
		boolean useSettlementTool = (settlement != null);
		vicinityUnit = null;
		
		if (!useSettlementTool) {
			settlement = v.getLocationTag().findSettlementVicinity();
			if (settlement != null) {
				vicinityUnit = settlement;
				desktop.showDetails(settlement);
				useSettlementTool = (settlement != null);
			}
			
			// Out there on a mission
			NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
			nw.updateCoordsMaps(v.getCoordinates());
		}
		
//		else {
//			SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
//			sw.chooseSettlement(settlement);
//			sw.displayVehicle(v);
//		}
	}

	/**
	 * Updates the equipment.
	 * 
	 * @param e
	 */
	private void equipmentUpdate(Equipment e) {
		MainDesktopPane desktop = getDesktop();
		Settlement settlement = e.getSettlement();
		vicinityUnit = null;
		
		if (settlement != null) {
			if (settlement != null) {
				desktop.showDetails(settlement);
				SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
				sw.chooseSettlement(settlement);
			}
			
			Vehicle vehicle = e.getVehicle();
			if (vehicle != null) {
				settlement = vehicle.getLocationTag().findSettlementVicinity();
				if (settlement != null) {
					vicinityUnit = settlement;
					desktop.showDetails(settlement);
				}
				else {
					// still parked inside a garage or within the premise of a settlement
					SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
					sw.chooseSettlement(settlement);
					sw.displayVehicle(vehicle);
				}
			}
			
			Person person = e.getRegisteredOwner();
			if (person != null) {
				settlement = vehicle.getLocationTag().findSettlementVicinity();
				if (settlement != null) {
					vicinityUnit = settlement;
					desktop.showDetails(settlement);
				}
				else {
					// On a person
					SettlementWindow sw = (SettlementWindow) desktop.openToolWindow(SettlementWindow.NAME);
					sw.chooseSettlement(settlement);
					sw.displayPerson(person);
				}
			}
		}
		
		else if (e.isOutside()) {
			settlement = e.getLocationTag().findSettlementVicinity();
			if (settlement != null) {
				vicinityUnit = settlement;
				desktop.showDetails(settlement);
			}
			else {
				Vehicle vehicle = e.getLocationTag().findVehicleVicinity();
				if (vehicle != null) {
					vicinityUnit = vehicle;
					desktop.showDetails(vehicle);
				}
			}
		}

		// Out there on a mission
//		NavigatorWindow nw = (NavigatorWindow) desktop.openToolWindow(NavigatorWindow.NAME);
//		nw.updateCoordsMaps(e.getCoordinates());
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
			
			updateUnit(getUnit());
			// Updates the labels
			update();
		}
	}

	public void updateUnit(Unit unit) {
		
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
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		Unit unit = getUnit();
		
		// Update the unit's location
//		updateUnit(unit);
	
		// If unit's location has changed, update location display.
		Coordinates location = unit.getCoordinates();
		
		if (location == null) {
			location = unit.getContainerUnit().getCoordinates();
		}
		
		if (!locationCache.equals(location)) {
			locationCache = location;

			String dirNS = Msg.getString("direction.degreeSign") 
							+ ((locationCache.getLatitudeDouble() >= 0) ? N : S);
			String dirEW = Msg.getString("direction.degreeSign")
							+ ((locationCache.getLongitudeDouble() >= 0) ? E : W);

			lcdLat.setLcdUnitString(dirNS);
			lcdLong.setLcdUnitString(dirEW);
			lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
			lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

			double newElevation = Math.round(TerrainElevation.getAverageElevation(location)
					* 1000.0) / 1000.0;

			setGauge(gauge, newElevation);

		}

		/////////////////
		
		updateLabels(unit);
		
		updateActivitySpot(unit);
		
		updateLocationBanner(unit);
	}

	/**
	 * Updates the labels.
	 * 
	 * @param unit
	 */
	private void updateLabels(Unit unit) {
		// Update labels as necessary
		
		Unit container = unit.getContainerUnit();
		if ((containerCache == null) || !containerCache.equals(container)) {
			containerCache = container;
			String n = container != null ? container.getName() : "";
			containerLabel.setText(n);
		}
		
		// If this unit is inside a building
		Building building = unit.getBuildingLocation();
		if (buildingCache != building) {
			buildingCache = building;
			String n = building != null ? building.getName() : "";
			buildingLabel.setText(n);
		}
		else
			buildingLabel.setText("");

		LocationStateType locationStateType = unit.getLocationStateType();
		if (locationStateTypeCache != locationStateType) {
			locationStateTypeCache = locationStateType;
			String n = locationStateType != null ? locationStateType.getName() : "";
			locationStateLabel.setText(Conversion.capitalize0(n));
		}
		
		// If this unit is near a settlement
		if (locationStateType == LocationStateType.SETTLEMENT_VICINITY) {
			vicinityUnit = unit.getLocationTag().findSettlementVicinity();
			if (vicinityUnit != null) {
				vicinityLabel.setText(vicinityUnit.getName());
			}
			else
				vicinityLabel.setText("");
		}
		
		// If this unit (including a settlement) is on Mars surface
		else if (locationStateType == LocationStateType.MARS_SURFACE) {
			// Check if this is a vehicle
			if (unit instanceof Vehicle) {
				vicinityUnit = unit.getLocationTag().findVehicleVicinity();
				if (vicinityUnit != null) {
					vicinityLabel.setText(vicinityUnit.getName());
				}
				else
					vicinityLabel.setText("");
			}
		}
		else {
			vicinityLabel.setText("");
		}
	}
	
	/**
	 * Updates the activity spot.
	 * 
	 * @param unit
	 */
	private void updateActivitySpot(Unit unit) {
		String n5 = "";
		
		if (unit instanceof Worker w) {
			var allocated = w.getActivitySpot();
			if (allocated != null) {
				n5 = allocated.getSpotDescription();
			}
		}
		
		activitySpot.setText(n5);
	}
	
	/**
	 * Tracks the location of a person, bot, vehicle, or equipment.
	 * 
	 * @param unit
	 */
	private void updateLocationBanner(Unit unit) {

		String loc = unit.getLocationTag().getExtendedLocation();

		if (!locationStringCache.equalsIgnoreCase(loc)) {
			locationStringCache = loc;
			bannerText.setLcdText(loc);
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		containerCache = null;
		locationCache = null;
		locatorButton = null;
		lcdLong = null;
		lcdLat = null;
		bannerText = null;
		gauge = null;

	}
}
