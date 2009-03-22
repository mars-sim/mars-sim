/**
 * Mars Simulation Project
 * MiningSitePanel.java
 * @version 2.86 2009-03-21
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.mission.create;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.NumberCellRenderer;
import org.mars_sim.msp.ui.standard.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.EllipseLayer;
import org.mars_sim.msp.ui.standard.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.Map;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.MapUtils;
import org.mars_sim.msp.ui.standard.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

/**
 * A wizard panel for the mining site.
 */
public class MiningSitePanel extends WizardPanel {
	
	// Wizard panel name.
	private final static String NAME = "Mining Site";
	
	// Range modifier.
	private final static double RANGE_MODIFIER = .95D;
	
	// Click range.
	private final static double CLICK_RANGE = 50D;
	
	// Data members.
	private MapPanel mapPane;
	private UnitIconMapLayer unitIconLayer;
	private UnitLabelMapLayer unitLabelLayer;
	private EllipseLayer ellipseLayer;
	private ExploredSiteMapLayer exploredSiteLayer;
	private JLabel longitudeLabel;
	private JLabel latitudeLabel;
	private JLabel errorMessageLabel;
	private ExploredLocation selectedSite;
	private DefaultTableModel concentrationTableModel;
	
	/**
	 * Constructor
	 * @param wizard the create mission wizard.
	 */
	MiningSitePanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		// Set the layout.
		setLayout(new BorderLayout(20, 20));
		
		// Set the border.
		setBorder(new MarsPanelBorder());
		
		// Create the title label.
		JLabel titleLabel = new JLabel("Select explored site (yellow flag) to mine.", JLabel.CENTER);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		add(titleLabel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(centerPanel, BorderLayout.CENTER);
		
		// Create the map panel.
		mapPane = new MapPanel();
        mapPane.addMapLayer(new MineralMapLayer(mapPane));
		mapPane.addMapLayer(unitIconLayer = new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(unitLabelLayer = new UnitLabelMapLayer());
		mapPane.addMapLayer(ellipseLayer = new EllipseLayer(Color.GREEN));
		mapPane.addMapLayer(exploredSiteLayer = new ExploredSiteMapLayer(mapPane));
		exploredSiteLayer.setDisplayMined(false);
		exploredSiteLayer.setDisplayReserved(false);
		mapPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				mouseSelection(event.getX(), event.getY());
			}
		});
		mapPane.setMaximumSize(mapPane.getPreferredSize());
		centerPanel.add(mapPane);
		
		// Create selected site panel.
		JPanel selectedSitePane = new JPanel();
		selectedSitePane.setBorder(new MarsPanelBorder());
		selectedSitePane.setPreferredSize(new Dimension(250, -1));
		selectedSitePane.setLayout(new BoxLayout(selectedSitePane, BoxLayout.Y_AXIS));
		add(selectedSitePane, BorderLayout.EAST);
		
		// Create selected site label.
		JLabel selectedSiteLabel = new JLabel("Selected Mining Site", JLabel.CENTER);
		selectedSiteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		selectedSitePane.add(selectedSiteLabel);
		
		// Create a vertical strut to add some UI space.
		selectedSitePane.add(Box.createVerticalStrut(10));
		
		// Create longitude label.
		longitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
		longitudeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		selectedSitePane.add(longitudeLabel);
		
		// Create latitude label.
		latitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
		latitudeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		selectedSitePane.add(latitudeLabel);
		
		// Create a vertical strut to add some UI space.
		selectedSitePane.add(Box.createVerticalStrut(10));
		
		// Create mineral concentration label.
		JLabel mineralConcentrationLabel = new JLabel("Estimated Mineral Concentrations:", JLabel.LEFT);
		mineralConcentrationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		selectedSitePane.add(mineralConcentrationLabel);
		
		// Create a vertical strut to add some UI space.
		selectedSitePane.add(Box.createVerticalStrut(10));
		
		// Create mineral concentration table.
		concentrationTableModel = new DefaultTableModel();
		concentrationTableModel.addColumn("Mineral");
		concentrationTableModel.addColumn("Concentration %");
		String[] mineralTypes = Simulation.instance().getMars().getSurfaceFeatures().
				getMineralMap().getMineralTypeNames();
		for (int x = 0; x < mineralTypes.length; x++)
			concentrationTableModel.addRow(new Object[]{mineralTypes[x], new Double(0D)});
		JTable mineralConcentrationTable = new JTable(concentrationTableModel);
		mineralConcentrationTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(2));
		selectedSitePane.add(mineralConcentrationTable.getTableHeader());
		selectedSitePane.add(mineralConcentrationTable);
		
		// Create verticle glue.
		selectedSitePane.add(Box.createVerticalGlue());
		
        // Create the error message label.
		errorMessageLabel = new JLabel(" ", JLabel.CENTER);
		errorMessageLabel.setForeground(Color.RED);
		errorMessageLabel.setFont(errorMessageLabel.getFont().deriveFont(Font.BOLD));
		add(errorMessageLabel, BorderLayout.SOUTH);
	}
	
	/**
	 * Gets the wizard panel name.
	 * @return panel name.
	 */
	String getPanelName() {
		return NAME;
	}

	/**
	 * Commits changes from this wizard panel.
	 * @retun true if changes can be committed.
	 */
	boolean commitChanges() {
		if (selectedSite != null) {
			getWizard().getMissionData().setMiningSite(selectedSite);
			return true;
		}
		else return false;
	}

	/**
	 * Clear information on the wizard panel.
	 */
	void clearInfo() {
		getWizard().setButtons(false);
		longitudeLabel.setText("Longitude: ");
		latitudeLabel.setText("Latitude: ");
		
		// Update mineral concentrations table.
		for (int x = 0; x < concentrationTableModel.getRowCount(); x++)
			concentrationTableModel.setValueAt(new Double(0D), x, 1);
	}

	/**
	 * Updates the wizard panel information.
	 */
	void updatePanel() {
		try {
			Collection<Unit> unitsToDisplay = new ArrayList<Unit>(1);
			unitsToDisplay.add(getWizard().getMissionData().getStartingSettlement());
			unitIconLayer.setUnitsToDisplay(unitsToDisplay);
			unitLabelLayer.setUnitsToDisplay(unitsToDisplay);
			ellipseLayer.setEllipseDetails(new IntPoint(150, 150), new IntPoint(150, 150), 
					(convertRadiusToMapPixels(getRoverRange()) * 2));
			ellipseLayer.setDisplayEllipse(true);
			selectMiningSite(null);
			mapPane.showMap(getCenterCoords());
		}
		catch (Exception e) {}
	}
	
	/**
	 * Selects an explored site to mine.
	 * @param site the site to mine.
	 */
	private void selectMiningSite(ExploredLocation site) {
		selectedSite = site;
		exploredSiteLayer.setSelectedSite(site);
		
		if (site != null) {
			longitudeLabel.setText("Longitude: " + site.getLocation().getFormattedLongitudeString());
			latitudeLabel.setText("Latitude: " + site.getLocation().getFormattedLatitudeString());
		
			// Update mineral concentrations table.
			java.util.Map<String, Double> estimatedConcentrations = site.getEstimatedMineralConcentrations();
			for (int x = 0; x < concentrationTableModel.getRowCount(); x++) {
				String mineralType = (String) concentrationTableModel.getValueAt(x, 0);
				Double concentration = estimatedConcentrations.get(mineralType);
				concentrationTableModel.setValueAt(concentration, x, 1);
			}
		
			try {
				if (getCenterCoords().getDistance(site.getLocation()) <= getRoverRange()) {
					errorMessageLabel.setText(" ");
					getWizard().setButtons(true);
				}
				else {
					errorMessageLabel.setText("Selected mining site is out of rover range.");
					getWizard().setButtons(false);
				}
			}
			catch (Exception e) {}
		}
		else {
			longitudeLabel.setText("Longitude: ");
			latitudeLabel.setText("Latitude: ");
			for (int x = 0; x < concentrationTableModel.getRowCount(); x++)
				concentrationTableModel.setValueAt(new Double(0D), x, 1);
			errorMessageLabel.setText(" ");
			getWizard().setButtons(false);
		}
	}
	
	/**
	 * Gets the mission rover range.
	 * @return range (km)
	 * @throws Exception if error getting mission rover.
	 */
	private double getRoverRange() throws Exception {
		return (getWizard().getMissionData().getRover().getRange() * RANGE_MODIFIER) / 2D;
	}
	
	/**
	 * Gets the center coordinates.
	 * @return center coordinates.
	 */
	private Coordinates getCenterCoords() {
		return getWizard().getMissionData().getStartingSettlement().getCoordinates();
	}
	
	/**
	 * Converts radius (km) into pixel range on map.
	 * @param radius the radius (km).
	 * @return pixel radius.
	 */
	private int convertRadiusToMapPixels(double radius) {
		return MapUtils.getPixelDistance(radius, SurfMarsMap.TYPE);
	}
	
	/**
	 * Mouse click.
	 * @param xLoc the mouse X coordinate.
	 * @param yLoc the moues Y coordinate.
	 */
	private void mouseSelection(int xLoc, int yLoc) {
		
		Coordinates center = getCenterCoords();
		if (center != null) {
			int xValue = xLoc - (Map.DISPLAY_HEIGHT / 2) - 1 + (exploredSiteLayer.getIconWidth() / 2);
			int yValue = yLoc - (Map.DISPLAY_HEIGHT / 2) - 1 + (exploredSiteLayer.getIconHeight() / 2);
			Coordinates clickedPosition = center.convertRectToSpherical((double) xValue,
					(double) yValue, CannedMarsMap.PIXEL_RHO);

			ExploredLocation closestSite = null;
			double closestRange = Double.MAX_VALUE;
			
			SurfaceFeatures surfaceFeatures = Simulation.instance().getMars().getSurfaceFeatures();
			Iterator<ExploredLocation> i = surfaceFeatures.getExploredLocations().iterator();
			while (i.hasNext()) {
				ExploredLocation site = i.next();
				if (!site.isReserved() && !site.isMined() && site.isExplored()) {
					double clickRange = site.getLocation().getDistance(clickedPosition);
					if ((clickRange <= CLICK_RANGE) && (clickRange < closestRange)) {
						closestSite = site;
						closestRange = clickRange;
					}
				}
			}
			
			if (closestSite != null) selectMiningSite(closestSite);
		}
	}
}