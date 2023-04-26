/*
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @date 2022-08-22
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.environment.SurfaceFeatures;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropSpec;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.building.function.farming.PhaseType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.PercentageCellRenderer;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming extends BuildingFunctionPanel {

	private static final String PLANT_ICON = "plant";
	private static final String G_M2_DAY = " g/m2/day";
	private static final String HTML = "<html>";
	private static final String END_HTML = "</html>";
	private static final String CROP_NAME = "&emsp;&nbsp;Crop Name:&emsp;";
	private static final String CATEGORY = "<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;";
	private static final String GROWING_DAYS = "<br>&nbsp;Growing Days:&emsp;";
	private static final String EDIBLE_MASS = "<br>&emsp;Edible Mass:&emsp;";
	private static final String INEDIBLE_MASS = "<br>&nbsp;Inedible Mass:&emsp;";
	private static final String WATER_CONTENT = "<br>&nbsp;Water Content:&emsp;";
	private static final String PERCENT = " %";
	private static final String PAR_REQUIRED = "<br>&nbsp;&nbsp;PAR required:&emsp;";
	private static final String MOL_M2_DAY = " mol/m2/day";

	private static final DecimalFormat DECIMAL_KG_SOL = new DecimalFormat("#,##0.0 kg/Sol");

	
	// Data members
	private JLabel radTF;
	private JLabel farmersTF;
	private JLabel cropsTF;
	private JLabel waterUsageTF;
	private JLabel greyWaterUsageTF;
	private JLabel o2TF;
	private JLabel co2TF;
	private JLabel workTimeTF;

	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The cache for the amount of solar irradiance. */
	private double radCache;
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average grey water usage per sol per square meters. */
	private double greyWaterUsageCache;
	/** The cache value for the average O2 generated per sol per square meters. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol per square meters. */
	private double co2Cache;
	/** The cache value for the work time done in this greenhouse. */
	private double workTimeCache;

	private JComboBox<CropSpec> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;
	private JScrollPane listScrollPanel;

	/** The farming building. */
	private Farming farm;
	private Coordinates location;

	private JList<String> list;

	private CropConfig cropConfig;
	private SurfaceFeatures surfaceFeatures;


	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelFarming.title"), 
			ImageLoader.getIconByName(PLANT_ICON), 
			farm.getBuilding(), 
			desktop
		);

		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();
		cropConfig = SimulationConfig.instance().getCropConfiguration();
	
		surfaceFeatures = getSimulation().getSurfaceFeatures();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		AttributePanel springPanel = new AttributePanel(4, 2);
		center.add(springPanel, BorderLayout.CENTER);

		// Prepare solar irradiance label
		radCache = surfaceFeatures.getSolarIrradiance(location);
		radTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.solarIrradiance.title"),
							 radCache + " W/m", "Estimated sunlight on top of the greenhouse roof");

		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		farmersTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numFarmers.title"),
				                 Integer.toString(farmersCache), "# of active gardeners tending the greenhouse");

		// Prepare crops label
		cropsCache = farm.getCrops().size();
		cropsTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.numCrops.title"),
							   Integer.toString(cropsCache), null);

		waterUsageCache = farm.computeUsage(ResourceUtil.waterID);
		waterUsageTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.waterUsage.title"),
									DECIMAL_KG_SOL.format(waterUsageCache),
									Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));

		greyWaterUsageCache = farm.computeUsage(ResourceUtil.greyWaterID);
		greyWaterUsageTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.greyWaterUsage.title"),
									DECIMAL_KG_SOL.format(greyWaterUsageCache),
									Msg.getString("BuildingPanelFarming.greyWaterUsage.tooltip"));
		
		o2Cache = farm.computeUsage(ResourceUtil.oxygenID);
		o2TF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.o2.title"),
									DECIMAL_KG_SOL.format(o2Cache),
									Msg.getString("BuildingPanelFarming.o2.tooltip"));

		co2Cache = farm.computeUsage(ResourceUtil.co2ID);
		co2TF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.co2.title"),
									DECIMAL_KG_SOL.format(co2Cache),
								 	Msg.getString("BuildingPanelFarming.co2.tooltip"));

		// Update the cumulative work time
		workTimeCache = farm.getCumulativeWorkTime()/1000.0;
		workTimeTF = springPanel.addTextField(Msg.getString("BuildingPanelFarming.workTime.title"),
									StyleManager.DECIMAL_SOLS.format(workTimeCache),
									Msg.getString("BuildingPanelFarming.workTime.tooltip"));

		JPanel southPanel = new JPanel(new BorderLayout());
		center.add(southPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for crop table
		JScrollPane tableScrollPanel = new JScrollPane();
		// Set the height and width of the table
		tableScrollPanel.setPreferredSize(new Dimension(200, 290)); // 290 is the best fit for 10 crops

		southPanel.add(tableScrollPanel, BorderLayout.NORTH);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		JTable cropTable = new JTable(cropTableModel) {
			// Implement Table Cell ToolTip for crops
			@Override
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
				RowSorter<? extends TableModel> sorter = getRowSorter();
				if (sorter != null) {
					rowIndex = sorter.convertRowIndexToModel(rowIndex);
				}	

				CropTableModel model = (CropTableModel) getModel();
				if ((rowIndex < 0) || (rowIndex >= model.getRowCount())) {
					return "";
				}

				Crop crop = model.getCrop(rowIndex);
                int colIndex = columnAtPoint(p);
				if (colIndex == 1) {
					return generateCropSpecTip(crop.getCropSpec());
				}
				double sols = Math.round(crop.getGrowingTimeCompleted()/1_000.0 *10.0)/10.0;
				return "# of Sols since planted: " + sols;
            }
        }; // end of WebTable
		cropTable.setAutoCreateRowSorter(true);
		TableColumnModel cropColumns = cropTable.getColumnModel();
		cropColumns.getColumn(CropTableModel.HEALTH).setPreferredWidth(5);
		cropColumns.getColumn(CropTableModel.NAME).setPreferredWidth(40);
		cropColumns.getColumn(CropTableModel.PHASE).setPreferredWidth(40);
		cropColumns.getColumn(CropTableModel.GROWTH).setPreferredWidth(20);
		cropColumns.getColumn(CropTableModel.GROWTH).setCellRenderer(new PercentageCellRenderer(true));
		cropColumns.getColumn(CropTableModel.CAT).setPreferredWidth(30);
		cropColumns.getColumn(CropTableModel.WORK).setPreferredWidth(30);
		cropColumns.getColumn(CropTableModel.WORK).setCellRenderer(new NumberCellRenderer());

		// Note: Use of setAutoCreateRowSorter causes array error 
		// whenever old crop is removed and new crop is added: cropTable.setAutoCreateRowSorter(true);
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		
		tableScrollPanel.setViewportView(cropTable);

		JPanel queuePanel = new JPanel(new BorderLayout());
	    southPanel.add(queuePanel, BorderLayout.CENTER);

	    JPanel selectPanel = new JPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.addActionListener(s -> {
				CropSpec cs = (CropSpec) comboBox.getSelectedItem();
				farm.addCropListInQueue(cs.getName());
				listModel.update();
				repaint();
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(s -> {
			if (!list.isSelectionEmpty()) {
				String deletingCropType = list.getSelectedValue();
				int deletingCropIndex = list.getSelectedIndex();
            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
				listModel.update();
            	repaint();
			}
		});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up crop combo box model.
		DefaultComboBoxModel<CropSpec> comboBoxModel = new DefaultComboBoxModel<>();
		for(CropSpec sp : cropConfig.getCropTypes()) {
	    	comboBoxModel.addElement(sp);
		}

		// Create comboBox.
		comboBox = new JComboBox<CropSpec>(comboBoxModel);

		// Use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
		comboBox.setMaximumRowCount(10);
	    selectPanel.add(comboBox);

		JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel queueButtonLabelPanel = new JPanel(new BorderLayout());
	    JLabel queueListLabel = new JLabel("     Crop Queue     ");
		queueListLabel.setUI(new VerticalLabelUI(false));
	    StyleManager.applySubHeading(queueListLabel);
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER);

		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));

		// Create list model
		listModel = new ListModel();
		// Create list
		list = new JList<>(listModel);
		listScrollPanel.setViewportView(list);
		queueListPanel.add(listScrollPanel);
	}

	/**
	 * Mouse clicked event occurs.
	 * 
	 * @param event the mouse event
	 */
	
	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {

		// Update farmers label if necessary.
		if (farmersCache != farm.getFarmerNum()) {
			farmersCache = farm.getFarmerNum();
			farmersTF.setText(farmersCache + "");
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsTF.setText(cropsCache + "");
		}

		// Update solar irradiance label
		double rad = Math.round(surfaceFeatures.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radTF.setText(radCache + " W/m");
		}

		// Update the average water usage
		double newWater = farm.computeUsage(ResourceUtil.waterID);
		if (waterUsageCache != newWater) {
			waterUsageCache = newWater;
			waterUsageTF.setText(DECIMAL_KG_SOL.format(newWater));
		}

		// Update the average O2 generated
		double newO2 = farm.computeUsage(ResourceUtil.oxygenID);
		if (o2Cache != newO2) {
			o2Cache = newO2;
			o2TF.setText(DECIMAL_KG_SOL.format(newO2));
		}

		// Update the average CO2 consumed
		double newCo2 = farm.computeUsage(ResourceUtil.co2ID);
		if (co2Cache != newCo2) {
			co2Cache = newCo2;
			co2TF.setText(DECIMAL_KG_SOL.format(newCo2));
		}

		// Update the average grey water usage
		double newGreyWater = farm.computeUsage(ResourceUtil.greyWaterID);
		if (greyWaterUsageCache != newGreyWater) {
			greyWaterUsageCache = newGreyWater;
			greyWaterUsageTF.setText(DECIMAL_KG_SOL.format(newGreyWater));
		}
		
		// Update the cumulative work time
		double workTime = farm.getCumulativeWorkTime()/1000.0;
		if (workTimeCache != workTime) {
			workTimeCache = workTime;
			workTimeTF.setText(StyleManager.DECIMAL_SOLS.format(workTime));
		}
		
		// Update crop table.
		cropTableModel.update();

		// Update list
		listModel.update();
	}

	
	/**
	 * Generate a tool tip describing a Crop Spec
	 * @param ct
	 * @return
	 */
	private static String generateCropSpecTip(CropSpec ct) {
		StringBuilder result = new StringBuilder();
		result.append(HTML)
			.append(CROP_NAME).append(ct.getName())
			.append(CATEGORY).append(ct.getCropCategory().getName())
			.append(GROWING_DAYS).append(ct.getGrowingTime() /1000)
			.append(EDIBLE_MASS).append(ct.getEdibleBiomass()).append(G_M2_DAY)
			.append(INEDIBLE_MASS).append(ct.getInedibleBiomass()).append(G_M2_DAY)
			.append(WATER_CONTENT).append(100 * ct.getEdibleWaterContent()).append(PERCENT)
			.append(PAR_REQUIRED).append(ct.getDailyPAR()).append(MOL_M2_DAY)
			.append(END_HTML);
		
			return result.toString();
	}

	/**
	 * List model for the crops in queue.
	 */
	private class ListModel extends AbstractListModel<String> {

	    private List<String> list;

	    private ListModel() {

        	List<String> c = farm.getCropListInQueue();
	        if (c != null)
	        	list = new ArrayList<>(c);
	        else
	        	list = null;
	    }

        @Override
        public String getElementAt(int index) {
        	String result = null;

            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
        	if (list == null)
        		return 0;
        	return list.size();
        }

        /**
         * Update the list model.
         */
        public void update() {

        	List<String> c = farm.getCropListInQueue();
        		// if the list contains duplicate items, it somehow pass this test
        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
	                List<String> oldList = list;
	                List<String> tempList = new ArrayList<>(c);
	 
	                list = tempList;
	                fireContentsChanged(this, 0, getSize());

	                oldList.clear();
	           }

        }
	}

	/**
	 * Internal class used as model for the crop table.
	 */
	private static class CropTableModel extends AbstractTableModel {

		private static final int HEALTH = 0;
		private static final int NAME = 1;
		private static final int PHASE = 2;
		private static final int GROWTH = 3;
		private static final int CAT = 4;
		private static final int WORK = 5;

		private Farming farm;
		private List<Crop> crops;
		private Icon redDot;
		private Icon redHalfDot;
		private Icon yellowDot;
		private Icon yellowHalfDot;
		private Icon greenDot;
		private Icon greenHalfDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = farm.getCrops();
			redDot = ImageLoader.getIconByName("dot/red");
			redHalfDot = ImageLoader.getIconByName("dot/red_half");
			yellowDot = ImageLoader.getIconByName("dot/yellow");
			yellowHalfDot = ImageLoader.getIconByName("dot/yellow_half");
			greenDot = ImageLoader.getIconByName("dot/green");
			greenHalfDot = ImageLoader.getIconByName("dot/green_half");
		}

		public Crop getCrop(int rowIndex) {
			return crops.get(rowIndex);
		}

		public int getRowCount() {
			return crops.size();
		}

		// Change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return WORK+1;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch(columnIndex) {
				case HEALTH -> Icon.class;
				case NAME, PHASE, CAT -> String.class;
				case GROWTH, WORK -> Double.class;
				default -> null;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case HEALTH -> "Health";
				case NAME -> "Name";
				case PHASE -> "Phase";
				case GROWTH -> "Growth";
				case CAT -> "Category";
				case WORK -> "Work";
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			PhaseType currentPhase = crop.getPhaseType();
			String category = crop.getCropSpec().getCropCategory().getName();

			switch(column) {
				case HEALTH:
					double condition = crop.getHealthCondition();
					if (condition > .95) return greenDot;
					else if (condition > .75) return greenHalfDot;
					else if (condition > .55 ) return yellowDot;
					else if (condition > .35 ) return yellowHalfDot;
					else if (condition > .2 ) return redDot;
					else return redHalfDot;
				case NAME:
					return crop.getCropName();
				case PHASE:
					return currentPhase.getName();
				case GROWTH: 
					return crop.getPercentGrowth();
				case CAT:
					return category;
				case WORK:
					return crop.getCurrentWorkRequired();
				default:
					return null;
				}
		}

		public void update() {
			if (!crops.equals(farm.getCrops()))
				crops = farm.getCrops();
			fireTableDataChanged();
		}
	}

	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (value instanceof CropSpec cs) {

	        	list.setToolTipText(generateCropSpecTip(cs));
	        }
	        return comp;
	    }
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		farm = null;
		comboBox= null;
		list= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
	}
}
