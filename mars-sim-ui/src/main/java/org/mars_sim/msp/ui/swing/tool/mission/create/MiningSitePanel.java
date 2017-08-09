/**
 * Mars Simulation Project
 * MiningSitePanel.java
 * @version 3.1.0 2017-08-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.mission.create;

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
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.IntPoint;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.mars.ExploredLocation;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.map.CannedMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.EllipseLayer;
import org.mars_sim.msp.ui.swing.tool.map.ExploredSiteMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.Map;
import org.mars_sim.msp.ui.swing.tool.map.MapPanel;
import org.mars_sim.msp.ui.swing.tool.map.MapUtils;
import org.mars_sim.msp.ui.swing.tool.map.MineralMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.SurfMarsMap;
import org.mars_sim.msp.ui.swing.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.swing.tool.map.UnitLabelMapLayer;

/**
 * A wizard panel for the mining site.
 */
public class MiningSitePanel extends WizardPanel {

    /** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Wizard panel name. */
	private final static String NAME = "Mining Site";
	
	/** Range modifier. */
	private final static double RANGE_MODIFIER = .95D;
	private final static double MAX_RANGE = 2500D;
	
	/** Click range. */
	private final static double CLICK_RANGE = 50D;
	
	// Data members.
	private MapPanel mapPane;
	private UnitIconMapLayer unitIconLayer;
	private UnitLabelMapLayer unitLabelLayer;
	private EllipseLayer ellipseLayer;
	private ExploredSiteMapLayer exploredSiteLayer;
    private MineralMapLayer mineralLayer;
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
		
        JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
        add(leftPanel, BorderLayout.CENTER);
        
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		leftPanel.add(centerPanel, BorderLayout.CENTER);
		
		// Create the map panel.
		mapPane = new MapPanel(200L);
        mineralLayer = new MineralMapLayer(mapPane);
        mapPane.addMapLayer(mineralLayer, 0);
		mapPane.addMapLayer(unitIconLayer = new UnitIconMapLayer(mapPane), 1);
		mapPane.addMapLayer(unitLabelLayer = new UnitLabelMapLayer(), 2);
		mapPane.addMapLayer(ellipseLayer = new EllipseLayer(Color.GREEN), 3);
		mapPane.addMapLayer(exploredSiteLayer = new ExploredSiteMapLayer(mapPane), 4);
		exploredSiteLayer.setDisplayMined(false);
		exploredSiteLayer.setDisplayReserved(false);
		mapPane.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				mouseSelection(event.getX(), event.getY());
			}
		});
		mapPane.setMaximumSize(mapPane.getPreferredSize());
		centerPanel.add(mapPane);
		
        // Create legend panel.
        JPanel legendPane = new JPanel(new BorderLayout(0, 0));
        leftPanel.add(legendPane, BorderLayout.SOUTH);
        
        // Create mineral legend label.
        JLabel mineralLegendLabel = new JLabel("Mineral Legend", JLabel.CENTER);
        mineralLegendLabel.setFont(mineralLegendLabel.getFont().deriveFont(Font.BOLD));
        legendPane.add(mineralLegendLabel, BorderLayout.NORTH);
        
        // Create mineral legend scroll panel.
        JScrollPane mineralLegendScrollPane = new JScrollPane();
        legendPane.add(mineralLegendScrollPane, BorderLayout.CENTER);
        
        // Create mineral legend table model.
        MineralTableModel mineralTableModel = new MineralTableModel();
        
        // Create mineral legend table.
        JTable mineralLegendTable = new JTable(mineralTableModel);
        mineralLegendTable.setPreferredScrollableViewportSize(new Dimension(300, 50));
        mineralLegendTable.setCellSelectionEnabled(false);
        mineralLegendTable.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
        mineralLegendScrollPane.setViewportView(mineralLegendTable);
        
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
        for (String mineralType : mineralTypes) concentrationTableModel.addRow(new Object[]{mineralType, 0D});
		JTable mineralConcentrationTable = new JTable(concentrationTableModel);
		mineralConcentrationTable.getColumnModel().getColumn(1).setCellRenderer(new NumberCellRenderer(2));
		selectedSitePane.add(mineralConcentrationTable.getTableHeader());
		selectedSitePane.add(mineralConcentrationTable);
		
		// Create vertical glue.
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
	 * @return true if changes can be committed.
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
	private double getRoverRange() {
		//return (getWizard().getMissionData().getRover().getRange() * RANGE_MODIFIER) / 2D;
		
		double range = getWizard().getMissionData().getRover().getRange() * RANGE_MODIFIER ;
		if (range > MAX_RANGE)
			range = MAX_RANGE;
		return range / 2D;
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
			int xValue = xLoc - (Map.MAP_VIS_WIDTH / 2) - 1 + (exploredSiteLayer.getIconWidth() / 2);
			int yValue = yLoc - (Map.MAP_VIS_HEIGHT / 2) - 1 + (exploredSiteLayer.getIconHeight() / 2);
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
    
    /** 
     * Internal class used as model for the mineral table.
     */
    private class MineralTableModel extends AbstractTableModel {

        /** default serial id. */
		private static final long serialVersionUID = 1L;

        private java.util.Map<String, Color> mineralColors = null;
        private List<String> mineralNames = null;
        
        private MineralTableModel() {
            mineralColors = mineralLayer.getMineralColors();
            mineralNames = new ArrayList<String>(mineralColors.keySet());
        }
        
        public int getRowCount() {
            return mineralNames.size();
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> dataType = super.getColumnClass(columnIndex);
            if (columnIndex == 0) dataType = String.class;
            if (columnIndex == 1) dataType = Color.class;
            return dataType;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "Mineral";
            else if (columnIndex == 1) return "Color";
            else return null;
        }
        
        public Object getValueAt(int row, int column) {
            if (row < getRowCount()) {
                String mineralName = mineralNames.get(row);
                if (column == 0) {
                    return mineralName;
                }
                else if (column == 1) {
                    return mineralColors.get(mineralName);
                }
                else return null;
            }
            else return null;
        }
    }
    
    /**
     * Internal class used to render color cells in the mineral table.
     */
    private static class ColorTableCellRenderer implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
        
            if ((value != null) && (value instanceof Color)) {
                Color color = (Color) value;
                JPanel colorPanel = new JPanel();
                colorPanel.setOpaque(true);
                colorPanel.setBackground(color);
                return colorPanel;
            }
            else return null;
            
        }
    }
}