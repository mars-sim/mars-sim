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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Unit;
import com.mars_sim.core.building.Building;
import com.mars_sim.core.environment.TerrainElevation;
import com.mars_sim.core.location.LocationStateType;
import com.mars_sim.core.location.LocationTag;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.unit.AbstractMobileUnit;
import com.mars_sim.core.unit.MobileUnit;
import com.mars_sim.core.vehicle.Vehicle;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.tool.MapSelector;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;

import eu.hansolo.steelseries.gauges.DisplayCircular;
import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.LcdColor;

/**
 * The LocationTabPanel is a tab panel for location information.
 */
@SuppressWarnings("serial")
public class LocationTabPanel extends EntityTabPanel<Unit> {

	private static final String MAP_ICON = NavigatorWindow.PIN_ICON;

	private static final String N = "N";
	private static final String S = "S";
	private static final String E = "E";
	private static final String W = "W";

	private static final Dimension LAT_LON_DIM = new Dimension(150, 40);
	private static final Dimension GAUGE_DIM = new Dimension(180, 180);
	private static final Dimension BANNER_DIM = new Dimension(140, 30);

	private String locationStringCache = "";

	private JLabel vicinityLabel;
	private JLabel containerLabel;
	private JLabel posnLabel;
	private JLabel buildingLabel;
	private JLabel locationStateLabel;
	private JLabel activitySpot;
	private JLabel iceLabel;
	private JLabel regolithLabel;
	private JLabel areothermalLabel;
	
	private DisplaySingle lcdLong;
	private DisplaySingle lcdLat;
	private DisplaySingle bannerText; 
	private DisplayCircular gauge;

	
	private Entity containerCache;
	private Building buildingCache;
	private Coordinates locationCache = new Coordinates(0D, 0D);

	private LocationStateType locationStateTypeCache;
	
	/**
	 * Constructor.
	 *
	 * @param unit    the unit to display.
	 * @param context the UI context.
	 */
	public LocationTabPanel(Unit unit, UIContext context) {
		// Use the TabPanel constructor
		super(
				Msg.getString("LocationTabPanel.title"), //$NON-NLS-1$
				ImageLoader.getIconByName(MAP_ICON), 
				null,
				context, unit);
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create top panel
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBorder(new MarsPanelBorder());
		content.add(topPanel, BorderLayout.NORTH);

		JPanel northPanel = new JPanel(new FlowLayout());
		topPanel.add(northPanel, BorderLayout.SOUTH);

		lcdLat = new DisplaySingle();
		lcdLat.setLcdInfoFont(new Font("Verdana", 0, 32));
		lcdLat.setLcdInfoString("Lat");
		lcdLat.setLcdColor(LcdColor.BEIGE_LCD);
		lcdLat.setGlowColor(Color.orange);
		lcdLat.setDigitalFont(true);
		lcdLat.setLcdDecimals(4);
		lcdLat.setSize(LAT_LON_DIM);
		lcdLat.setMaximumSize(LAT_LON_DIM);
		lcdLat.setPreferredSize(LAT_LON_DIM);
		lcdLat.setVisible(true);
		northPanel.add(lcdLat);

		// Create center map button
		var locatorButton = new JButton(ImageLoader.getIconByName(NavigatorWindow.ICON));
		locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
		locatorButton.addActionListener(e -> displayMap());
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
		lcdLong.setSize(LAT_LON_DIM);
		lcdLong.setMaximumSize(LAT_LON_DIM);
		lcdLong.setPreferredSize(LAT_LON_DIM);
		lcdLong.setVisible(true);
		lcdPanel.add(lcdLong);
		northPanel.add(lcdPanel);

		// Update the LCDs
		updateLCDs(locationCache);
		
		JPanel gaugePanel = new JPanel();
		setupGauge(gaugePanel);
		topPanel.add(gaugePanel, BorderLayout.CENTER);

		// Update the elevation in the gauge
		updateGauge(locationCache);
		
		bannerText = new DisplaySingle();
		bannerText.setLcdInfoString("Last Known Position");
		bannerText.setGlowColor(Color.ORANGE);
		bannerText.setLcdColor(LcdColor.BEIGE_LCD);	
		bannerText.setDigitalFont(true);
		bannerText.setSize(BANNER_DIM);
		bannerText.setMaximumSize(BANNER_DIM);
		bannerText.setPreferredSize(BANNER_DIM);
		bannerText.setVisible(true);
		bannerText.setLcdNumericValues(false);
		bannerText.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));
		bannerText.setLcdText(locationStringCache);
		// Pause the location lcd text the sim is pause
        bannerText.setLcdTextScrolling(true);
		topPanel.add(bannerText, BorderLayout.NORTH);

		// Create data panel
		var dataPanel = new AttributePanel();
		content.add(dataPanel, BorderLayout.CENTER);
        addBorder(dataPanel, "Location Data");
        var unit = getEntity();
		if (unit instanceof MobileUnit mu) {
			if (mu instanceof Worker) {
				activitySpot = dataPanel.addRow("Reserved Spot", "");
			}

			addUnitValues(dataPanel);
			addMobileUnitValues(dataPanel);
		}
		
		else if (unit instanceof Settlement) {
			iceLabel = dataPanel.addRow("Ice Score", "");
			regolithLabel = dataPanel.addRow("Regolith Score", "");
			areothermalLabel = dataPanel.addRow("Areothermal Score", "");
		}

		update();
	}

	private void addUnitValues(AttributePanel containerPanel) {
		containerLabel = containerPanel.addRow("Container Unit", "");
		locationStateLabel = containerPanel.addRow("Location State", "");
	}
	
	private void addMobileUnitValues(AttributePanel containerPanel) {
		buildingLabel = containerPanel.addRow("Building", "");
		posnLabel = containerPanel.addRow("Position", "");
		vicinityLabel = containerPanel.addRow("Vicinity", "");
	}
	
	private void updateMobileLabels(MobileUnit mu) {
		// If this unit is inside a building
		Building building = mu.getBuildingLocation();
		if (buildingCache != building) {
			buildingCache = building;
			String n = building != null ? building.getName() : "";
			buildingLabel.setText(n);
		}

		posnLabel.setText(mu.getPosition().getShortFormat());

		// Update labels as necessary
		Entity container = mu.getContainerUnit();
		if ((containerCache == null) || !containerCache.equals(container)) {
			containerCache = container;
			String n = container != null ? container.getName() : "";
			containerLabel.setText(n);
		}
		updateSurfacePOI(mu);
	}

	private void updateVicinityLabel(AbstractMobileUnit unit) {
		LocationStateType locationStateType = unit.getLocationStateType();
		if (locationStateTypeCache != locationStateType) {
			locationStateTypeCache = locationStateType;
			String n = locationStateType != null ? locationStateType.getName() : "";
			locationStateLabel.setText(n);
		}
			
		Unit vicinityUnit = null;
		if (locationStateType == LocationStateType.SETTLEMENT_VICINITY) {
			// If this unit is near a settlement
			vicinityUnit = unit.getLocationTag().findSettlementVicinity();
		}
		else if (locationStateType == LocationStateType.VEHICLE_VICINITY) {
			// If this unit is near a vehicle
			vicinityUnit = unit.getLocationTag().findVehicleVicinity();
		}
		
		// If this unit (including a settlement) is on Mars surface
		else if (locationStateType == LocationStateType.MARS_SURFACE && unit instanceof Vehicle) {
			vicinityUnit = unit.getLocationTag().findVehicleVicinity();
		}
		if (vicinityUnit != null) {
			vicinityLabel.setText(vicinityUnit.getName());
		}
		else {
			vicinityLabel.setText("");
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		
		Unit unit = getEntity();

		updateBanner(unit);
		
		if (unit instanceof AbstractMobileUnit mu) {
			if (unit instanceof Worker w) {
				updateActivitySpot(w);
			}
			if (vicinityLabel != null) {
				updateVicinityLabel(mu);
			}
			updateMobileLabels(mu);
		}
		else if (unit instanceof SurfacePOI sp) {
			updateSurfacePOI(sp);
		}

		if (unit instanceof Settlement s) {
			updateSettlementLabels(s);
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
		gauge.setLcdDecimals(4);
		gauge.setSize(GAUGE_DIM);
		gauge.setPreferredSize(GAUGE_DIM);
		gauge.setVisible(true);
	
		gaugePanel.add(gauge);
	}

	
	/**
	 * Updates location and elevation data.
	 */
	private void updateSurfacePOI(SurfacePOI unit) {

		// If unit's location has changed, update location display.
		
		Coordinates location = unit.getCoordinates();
		// If this unit depends on the container unit to provide coordinates
		if (location == null) {
			// Should never happen
			return;
		}
			
		if (!locationCache.equals(location)) {
	
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

		var latDeg = location.getLatitudeDouble();
		var lonDeg = location.getLongitudeDouble();
		String dirNS = Msg.getString("direction.degreeSign") 
						+ ((latDeg >= 0) ? N : S);
		String dirEW = Msg.getString("direction.degreeSign")
						+ ((lonDeg >= 0) ? E : W);

		lcdLat.setLcdUnitString(dirNS);
		lcdLong.setLcdUnitString(dirEW);
		lcdLat.setLcdValueAnimated(Math.abs(latDeg));
		lcdLong.setLcdValueAnimated(Math.abs(lonDeg));
	}
	
	/**
	 * Updates the gauge.
	 * 
	 * @param location
	 */
	private void updateGauge(Coordinates location) {
		
		double elevation = Math.round(TerrainElevation.getAverageElevation(location)
				* 1000.0) / 1000.0;

		// Note: The peak of Olympus Mons is 21,229 meters (69,649 feet) above
		// the Mars areoid (a reference datum similar to Earth's sea level). 
		
		// The lowest point is within the Hellas Impact Crater (marked by a flag 
		// with the letter "L").
		
		// The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet)
		// below the Mars areoid.

		double larger = elevation * 1.25;
		double smaller = elevation * 0.75;
		int max = 0;
		int min = 0;
		
		if (elevation > 0) {
			max = (int) larger; 
			min = (int) smaller;
		}
		else if (elevation < 0) {
			max = (int) smaller;
			min = (int) larger;
		}
		else {
			max = 2;
			min = -2;
		}

		gauge.getModel().setRange(min, max);
		gauge.setLcdValueAnimated(elevation);
	}
	
	private void displayMap() {
		if (locationCache != null) {
			MapSelector.displayOnMap(getContext(), getEntity());
		}
	}

	/**
	 * Updates the labels.
	 * 
	 * @param s
	 */
	private void updateSettlementLabels(Settlement s) {
		iceLabel.setText(Math.round(s.getIceCollectionRate() * 100.0)/100.0 + "");
		regolithLabel.setText(Math.round(s.getRegolithCollectionRate() * 100.0)/100.0 + "");
		areothermalLabel.setText(Math.round(s.getAreothermalPotential() * 100.0)/100.0 + " %");
	}
	
	/**
	 * Updates the activity spot.
	 * 
	 * @param unit
	 */
	private void updateActivitySpot(Worker w) {
		String n5 = "";
		var allocated = w.getActivitySpot();
		if (allocated != null) {
			n5 = allocated.getSpotDescription();
		}
		
		activitySpot.setText(n5);
	}
	
	/**
	 * Tracks the location of a person, bot, vehicle, or equipment.
	 * 
	 * @param unit
	 */
	private void updateBanner(Unit unit) {

		String loc = LocationTag.MARS_SURFACE;
		if (unit instanceof AbstractMobileUnit mu) {
			loc = mu.getLocationTag().getExtendedLocation();
		}

		if (!locationStringCache.equalsIgnoreCase(loc)) {
			locationStringCache = loc;
			bannerText.setLcdText(loc);
		}
	}
}
