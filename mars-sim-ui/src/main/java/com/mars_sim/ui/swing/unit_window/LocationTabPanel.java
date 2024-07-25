/*
 * Mars Simulation Project
 * LocationTabPanel.java
 * @date 2024-07-17
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
import com.mars_sim.ui.swing.StyleManager;
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

	private boolean isSettlement;
	private boolean isVehicle;
	private boolean isPerson;
	private boolean isRobot;
	private boolean isEquipment;

	private String themeCache = "";
	private String locationStringCache;

	private Unit vicinityUnit;
	private Unit containerCache;
	private Building buildingCache;
	private Settlement settlementCache;
	
	private LocationStateType locationStateTypeCache;
	
	private JLabel vicinityLabel;
	private JLabel containerLabel;
	private JLabel settlementLabel;
	private JLabel buildingLabel;
	private JLabel locationStateLabel;
	private JLabel activitySpot;
	private JLabel iceLabel;
	private JLabel regolithLabel;
	private JLabel areothermalLabel;
	
	
	private JButton locatorButton;

	private Coordinates locationCache;

	private DisplaySingle lcdLong;
	private DisplaySingle lcdLat;
	private DisplaySingle bannerText; 
	private DisplayCircular gauge;

	private Dimension latLonDim = new Dimension(150, 40);
	private Dimension gaugeDim = new Dimension(200, 200);
	private Dimension bannerDim = new Dimension(140, 30);
	
	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param desktop the main desktop.
	 */
	public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
				Msg.getString("LocationTabPanel.title"), //$NON-NLS-1$
				ImageLoader.getIconByName(MAP_ICON), 
				Msg.getString("LocationTabPanel.title"), //$NON-NLS-1$
				unit, desktop);

		isSettlement = unit instanceof Settlement;
		isVehicle = unit instanceof Vehicle;
		isPerson = unit instanceof Person;
		isRobot = unit instanceof Robot;
		isEquipment = unit instanceof Equipment;

		locationStringCache = unit.getLocationTag().getExtendedLocation();
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create top panel
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBorder(new MarsPanelBorder());
//		locationPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
		content.add(topPanel, BorderLayout.NORTH);

		// Initialize location cache
		Coordinates location = getUnit().getCoordinates();
		// If this unit depends on the container unit to provide coordinates
		if (location == null) {
			location = getUnit().getContainerUnit().getCoordinates();
		}
		
		JPanel northPanel = new JPanel(new FlowLayout());
		topPanel.add(northPanel, BorderLayout.SOUTH);

		lcdLat = new DisplaySingle();
		lcdLat.setLcdInfoFont(new Font("Verdana", 0, 32));
		lcdLat.setLcdInfoString("Lat");
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
		lcdLong.setLcdInfoFont(new Font("Verdana", 0, 32));
		lcdLong.setLcdInfoString("Lon");
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

		// Update the LCDs
		updateLCDs(location);
		
		JPanel gaugePanel = new JPanel();
		setupGauge(gaugePanel);
		topPanel.add(gaugePanel, BorderLayout.CENTER);

		// Update the elevation in the gauge
		updateGauge(location);
		
		bannerText = new DisplaySingle();
		bannerText.setLcdInfoString("Last Known Position");
		bannerText.setGlowColor(Color.ORANGE);
		bannerText.setLcdColor(LcdColor.BEIGE_LCD);	
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
		topPanel.add(bannerText, BorderLayout.NORTH);

		// Create data panel
		JPanel dataPanel = new JPanel(new BorderLayout(2, 2));
		content.add(dataPanel, BorderLayout.CENTER);
        addBorder(dataPanel, "Data");
        
		if (isPerson || isRobot) {
			
			AttributePanel containerPanel = new AttributePanel(6);
			dataPanel.add(containerPanel, BorderLayout.NORTH);	
			
			activitySpot = containerPanel.addRow("Reserved Spot", "");
			
			settlementLabel = containerPanel.addRow("Settlement", "");
			containerLabel = containerPanel.addRow("Container Unit", "");
			locationStateLabel = containerPanel.addRow("Location State", "");
			buildingLabel = containerPanel.addRow("Building", "");
			vicinityLabel = containerPanel.addRow("Vicinity", "");
		}
		
		else if (isVehicle) {
			
			AttributePanel containerPanel = new AttributePanel(5);
			dataPanel.add(containerPanel, BorderLayout.NORTH);	
	
			settlementLabel = containerPanel.addRow("Settlement", "");
			containerLabel = containerPanel.addRow("Container Unit", "");
			locationStateLabel = containerPanel.addRow("Location State", "");
			buildingLabel = containerPanel.addRow("Building", "");
			vicinityLabel = containerPanel.addRow("Vicinity", "");
		}
		
		else if (isEquipment) {
			
			AttributePanel containerPanel = new AttributePanel(3);
			dataPanel.add(containerPanel, BorderLayout.NORTH);	
				
			settlementLabel = containerPanel.addRow("Settlement", "");
			containerLabel = containerPanel.addRow("Container Unit", "");
			locationStateLabel = containerPanel.addRow("Location State", "");
		}
		
		else if (isSettlement) {
			AttributePanel containerPanel = new AttributePanel(3);
			dataPanel.add(containerPanel, BorderLayout.NORTH);

			iceLabel = containerPanel.addRow("Ice Score", "");
			regolithLabel = containerPanel.addRow("Regolith Score", "");
			areothermalLabel = containerPanel.addRow("Areothermal Score", "");
		}

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		Unit unit = getUnit();
		
		if (!isSettlement) {
			updateLocationElevation(unit);
			
			if (!isEquipment && !isVehicle) {
				updateActivitySpot(unit);
			}
			
			updateBanner(unit);
		}
		
		updateLabels(unit);
		
		String theme = StyleManager.getLAF();
		if (!themeCache.equals(theme)) {	
			themeCache = theme;
			updateBannerThemeColor(theme);
		}
	}
	
	/**
	 * Updates the banner theme color.
	 * 
	 * @param gauge
	 */
	private void updateBannerThemeColor(String theme) {
		if (theme.equalsIgnoreCase(StyleManager.LIGHT_BLUE)) {
			bannerText.setGlowColor(Color.WHITE);
			bannerText.setLcdColor(LcdColor.BLUEGRAY_LCD);			
		}
		else if (theme.equalsIgnoreCase(StyleManager.LIGHT_GREEN)) {
			bannerText.setGlowColor(Color.lightGray);
			bannerText.setLcdColor(LcdColor.DARKGREEN_LCD);			
		}
		else if (theme.equalsIgnoreCase(StyleManager.LIGHT_ORANGE)
				|| theme.equalsIgnoreCase(StyleManager.SOLARIZED_LIGHT)) {
			bannerText.setGlowColor(Color.ORANGE);
			bannerText.setLcdColor(LcdColor.AMBER_LCD);			
		}
		else if (theme.equalsIgnoreCase(StyleManager.LIGHT_RED)) {
			bannerText.setGlowColor(Color.LIGHT_GRAY);
			bannerText.setLcdColor(LcdColor.REDDARKRED_LCD);			
		}
		else if (theme.equalsIgnoreCase(StyleManager.DARK)
				|| theme.equalsIgnoreCase(StyleManager.HIBERBEE_DARK)
				|| theme.equalsIgnoreCase(StyleManager.SOLARIZED_DARK)) {
			bannerText.setGlowColor(Color.WHITE);
			bannerText.setLcdColor(LcdColor.DARKBLUE_LCD);			
		}
		else if (theme.equalsIgnoreCase(StyleManager.SYSTEM)) {
			bannerText.setGlowColor(Color.ORANGE);
			bannerText.setLcdColor(LcdColor.BEIGE_LCD);			
		}
	}
	
	/**
	 * Sets up the circular gauge.
	 * 
	 * @param gauge
	 */
	private void setupGauge(JPanel gaugePanel) {
		gauge = new DisplayCircular();
		gauge.setDisplayMulti(false);
		gauge.setDigitalFont(true);
		gauge.setUnitString("km");
		gauge.setTitle("Elevation");
		
		if (isSettlement) {
			gauge.setFrameDesign(FrameDesign.GOLD);
			gauge.setBackgroundColor(BackgroundColor.BEIGE);
		} 
		else if (isVehicle) {
			gauge.setFrameDesign(FrameDesign.CHROME);
			gauge.setBackgroundColor(BackgroundColor.DARK_GRAY);
		}
		else if (isPerson) {
			gauge.setFrameDesign(FrameDesign.ANTHRACITE);
			gauge.setBackgroundColor(BackgroundColor.LINEN);
		}
		else if (isRobot) {
			gauge.setFrameDesign(FrameDesign.BRASS);
			gauge.setBackgroundColor(BackgroundColor.CARBON);
		}
		
		gauge.setLcdDecimals(4);
		gauge.setSize(gaugeDim);
		gauge.setPreferredSize(gaugeDim);
		gauge.setVisible(true);
	
		gaugePanel.add(gauge);
	}

	
	/**
	 * Updates location and elevation data.
	 */
	private void updateLocationElevation(Unit unit) {

		// If unit's location has changed, update location display.
		
		Coordinates location = unit.getCoordinates();
		// If this unit depends on the container unit to provide coordinates
		if (location == null) {
			location = unit.getContainerUnit().getCoordinates();
		}
			
		if (locationCache == null 
				|| (locationCache != null && !locationCache.equals(location))) {
			locationCache = location;
			
			// Update the LCDs
			updateLCDs(location);
			
			// Update the elevation in the gauge
			updateGauge(location);
		}
	}

	/**
	 * Updates the gauge.
	 * 
	 * @param location
	 */
	private void updateLCDs(Coordinates location) {

		String dirNS = Msg.getString("direction.degreeSign") 
						+ ((location.getLatitudeDouble() >= 0) ? N : S);
		String dirEW = Msg.getString("direction.degreeSign")
						+ ((location.getLongitudeDouble() >= 0) ? E : W);

		lcdLat.setLcdUnitString(dirNS);
		lcdLong.setLcdUnitString(dirEW);
		lcdLat.setLcdValueAnimated(Math.abs(location.getLatitudeDouble()));
		lcdLong.setLcdValueAnimated(Math.abs(location.getLongitudeDouble()));
	}
	
	/**
	 * Updates the gauge.
	 * 
	 * @param location
	 */
	private void updateGauge(Coordinates location) {
		
		double elevation = Math.round(TerrainElevation.getAverageElevation(location)
				* 1000.0) / 1000.0;

		// Note: The peak of Olympus Mons is 21,229 meters (69,649 feet) above the Mars
		// areoid (a reference datum similar to Earth's sea level). The lowest point is
		// within the Hellas Impact Crater (marked by a flag with the letter "L").
		// The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet)
		// below the Mars areoid.

		int max = -1;
		int min = 2;

		if (elevation < -8) {
			max = -8;
			min = -9;
		} else if (elevation < -5) {
			max = -5;
			min = -9;
		} else if (elevation < -3) {
			max = -3;
			min = -5;
		} else if (elevation < 0) {
			max = 1;
			min = -1;
		} else if (elevation < 1) {
			max = 2;
			min = 0;
		} else if (elevation < 3) {
			max = 5;
			min = 0;
		} else if (elevation < 10) {
			max = 10;
			min = 5;
		} else if (elevation < 20) {
			max = 20;
			min = 10;
		} else if (elevation < 30) {
			max = 30;
			min = 20;
		}

		gauge.setMinValue(min);
		gauge.setMaxValue(max);
		gauge.setLcdValueAnimated(elevation);
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
	 * Updates the labels.
	 * 
	 * @param unit
	 */
	private void updateLabels(Unit unit) {
		// Update labels as necessary
		
		if (!isSettlement) {
			Unit container = unit.getContainerUnit();
			if ((containerCache == null) || !containerCache.equals(container)) {
				containerCache = container;
				String n = container != null ? container.getName() : "";
				containerLabel.setText(n);
			}
			
			// If this unit is inside a settlement
			Settlement settlement = unit.getSettlement();
			if (settlementCache != settlement) {
				settlementCache = settlement;
				String n = settlement != null ? settlement.getName() : "";
				settlementLabel.setText(n);
			}
			
			// If this unit is inside a building
			Building building = unit.getBuildingLocation();
			if (buildingCache != building) {
				buildingCache = building;
				String n = building != null ? building.getName() : "";
				buildingLabel.setText(n);
			}

			LocationStateType locationStateType = unit.getLocationStateType();
			if (locationStateTypeCache != locationStateType) {
				locationStateTypeCache = locationStateType;
				String n = locationStateType != null ? locationStateType.getName() : "";
				locationStateLabel.setText(Conversion.capitalize0(n));
			}
			
			if (locationStateType == LocationStateType.SETTLEMENT_VICINITY) {
				// If this unit is near a settlement
				vicinityUnit = unit.getLocationTag().findSettlementVicinity();
				if (vicinityUnit != null) {
					vicinityLabel.setText(vicinityUnit.getName());
				}
				else
					vicinityLabel.setText("");
			}
			else if (locationStateType == LocationStateType.VEHICLE_VICINITY) {
				// If this unit is near a vehicle
				vicinityUnit = unit.getLocationTag().findVehicleVicinity();
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
		
		else {
			iceLabel.setText(Math.round(((Settlement)unit).getIceCollectionRate() * 100.0)/100.0 + "");
			regolithLabel.setText(Math.round(((Settlement)unit).getRegolithCollectionRate() * 100.0)/100.0 + "");
			areothermalLabel.setText(Math.round(((Settlement)unit).getAreothermalPotential() * 100.0)/100.0 + " %");
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
	private void updateBanner(Unit unit) {

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
