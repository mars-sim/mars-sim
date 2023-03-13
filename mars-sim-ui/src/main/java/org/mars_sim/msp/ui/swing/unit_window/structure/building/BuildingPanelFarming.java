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
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

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
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.VerticalLabelUI;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;


/**
 * The BuildingPanelFarming class is a building function panel representing
 * the crop farm of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelFarming extends BuildingFunctionPanel
implements MouseListener {

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
	/** The index cache for the crop to be deleted. */
	private int deletingCropIndex;
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

	private DefaultComboBoxModel<String> comboBoxModel;
	private JComboBoxMW<String> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;
	private JScrollPane listScrollPanel;

	/** The farming building. */
	private Farming farm;
	private String cropName;
	private String deletingCropType;
	private Coordinates location;

	private ArrayList<String> tooltipArray;
	private List<String> cropCache;
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
		AttributePanel springPanel = new AttributePanel(8);
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
                int colIndex = columnAtPoint(p);
    			StringBuilder result = new StringBuilder("");

                try {
                		result.append(buildCropToolTip(rowIndex, colIndex, null));
                	} catch (RuntimeException e1) {
                		//catch null pointer exception if mouse is over an empty line
                }
    			return result.toString();

            }
        }; // end of WebTable

		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		TableColumnModel cropColumns = cropTable.getColumnModel();
		cropColumns.getColumn(0).setPreferredWidth(5);
		cropColumns.getColumn(1).setPreferredWidth(40);
		cropColumns.getColumn(2).setPreferredWidth(40);
		cropColumns.getColumn(3).setPreferredWidth(20);
		cropColumns.getColumn(4).setPreferredWidth(30);
		cropColumns.getColumn(5).setPreferredWidth(30);
		// Note: Use of setAutoCreateRowSorter causes array error 
		// whenever old crop is removed and new crop is added: cropTable.setAutoCreateRowSorter(true);
				
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		cropTable.setRowSelectionAllowed(true);
		
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
				cropName = (String) comboBox.getSelectedItem();
				farm.addCropListInQueue(cropName);
		        listUpdate();
				repaint();
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(s -> {
			if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
	           	selectCrop();
            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
            	listUpdate();
            	repaint();
			}
		});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up crop combo box model.
		List<String> nameList = cropConfig.getCropTypeNames();
		cropCache = new ArrayList<>(nameList);
		comboBoxModel = new DefaultComboBoxModel<>();

		tooltipArray = new ArrayList<>();

		Iterator<String> i = cropCache.iterator();
		int j = 0;
		while (i.hasNext()) {
			String n = i.next();
	    	comboBoxModel.addElement(n);
	    	tooltipArray.add(buildCropToolTip(j, -1, n).toString());
	    	j++;
		}

		// Create comboBox.
		comboBox = new JComboBoxMW<>(comboBoxModel);

		// Use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
	    toolTipRenderer.setTooltips(tooltipArray);

		comboBox.addActionListener(s -> cropName = (String) comboBox.getSelectedItem());
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
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()){
				selectCrop();
			}
		});
		queueListPanel.add(listScrollPanel);
	}

	/*
	 * Builds an tooltip for displaying the growth parameters of a crop
	 */
	private StringBuilder buildCropToolTip(int row, int col, String n) {

		StringBuilder result = new StringBuilder("");
		String cropName = "";
		String cat = "";
		double time = 0;
		double mass0 = 0;
		double mass1 = 0;
		double water = 0;
		double PAR = 0;
		double sols = 0;
		double health = 0;

        if (n == null || n.equals("")) {
    		List<Crop> crops = farm.getCrops();
            Crop crop = crops.get(row);
            CropSpec ct = crop.getCropSpec();
        	cropName = crop.getCropName();
            cat = ct.getCropCategory().getName();
        	mass0 = ct.getEdibleBiomass();
        	water = 100 * ct.getEdibleWaterContent();
        	mass1 = ct.getInedibleBiomass();
        	time = ct.getGrowingTime() /1000;
        	PAR = ct.getDailyPAR();
        	health =  Math.round(crop.getHealthCondition()*10.0 * 100.0)/10.0;
        	sols = Math.round(crop.getGrowingTimeCompleted()/1_000.0 *10.0)/10.0;


        	if (col == 0) {
	        	result.append("Health: ").append(health).append(" %");
        	}

        	else if (col == 1) {
	            result.append(HTML)
	            .append(CROP_NAME).append(cropName);
	        	result.append(CATEGORY).append(cat);
	           	result.append(GROWING_DAYS).append(time);
	        	result.append(EDIBLE_MASS).append(mass0).append(G_M2_DAY);
	        	result.append(INEDIBLE_MASS).append(mass1).append(G_M2_DAY);
	        	result.append(WATER_CONTENT).append(water).append(PERCENT);
	        	result.append(PAR_REQUIRED).append(PAR)
	        	.append(MOL_M2_DAY).append(END_HTML);
        	}
        	
        	else {
	        	result.append("# of Sols since planted: ").append(sols);
        	}

        }

        if (col == -1) {
        	cropName = n;
        	CropSpec cType = cropConfig.getCropTypeByName(n);
            cat = cType.getCropCategory().getName();
        	mass0 = cType.getEdibleBiomass();
        	water = 100 * cType.getEdibleWaterContent();
        	mass1 = cType.getInedibleBiomass();
        	time = cType.getGrowingTime() /1000;
        	PAR = cType.getDailyPAR();

            result.append("<html>").append("&emsp;&nbsp;Crop Name:&emsp;").append(cropName);
        	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;").append(cat);
           	result.append("<br>&nbsp;Growing Days:&emsp;").append(time);
        	result.append("<br>&emsp;Edible Mass:&emsp;").append(mass0).append(G_M2_DAY);
        	result.append("<br>&nbsp;Inedible Mass:&emsp;").append(mass1).append(G_M2_DAY);
        	result.append("<br>&nbsp;Water Content:&emsp;").append(water).append(" %");
        	result.append("<br>&nbsp;&nbsp;PAR required:&emsp;").append(PAR).append(" mol/m2/day").append("</p></html>");
        }

    	return result;
	}

	/**
	 * Selects Crop.
	 * 
	 * @param table
	 */
	public void selectCrop() {

		String n = list.getSelectedValue();
		if (n != null) {
			deletingCropType = n;
			deletingCropIndex = list.getSelectedIndex();
		}
		else
			listUpdate();
	}

	@SuppressWarnings("unchecked")
	public void listUpdate() {

		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
		comboBox.setRenderer(new PromptComboBoxRenderer("A list of crops"));
		comboBox.setSelectedIndex(-1);
	}

	/**
	 * Mouse clicked event occurs.
	 * 
	 * @param event the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent event) {

		// Note: If double-click, open tooltip for the selected crop?
		if (event.getClickCount() >= 2) {
			selectCrop();
			if (deletingCropType != null) {
            	// do something
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// do nothing
	}
	@Override
	public void mouseReleased(MouseEvent event) {
		// do nothing
	}
	@Override
	public void mouseEntered(MouseEvent event) {
		// do nothing
	}
	@Override
	public void mouseExited(MouseEvent event) {
		// do nothing
	}
	
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
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
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

		public int getRowCount() {
			return crops.size();
		}

		// Change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return 6;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Icon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			else if (columnIndex == 4) dataType = String.class;
			else if (columnIndex == 5) dataType = Double.class;
			
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			if (columnIndex == 1) return "Name";
			if (columnIndex == 2) return "Phase";
			if (columnIndex == 3) return "Growth";
			if (columnIndex == 4) return "Category";
			if (columnIndex == 5) return "Work";
			return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			PhaseType currentPhase = crop.getPhaseType();
			String category = crop.getCropSpec().getCropCategory().getName();

			if (column == 0) {
				double condition = crop.getHealthCondition();
				if (condition > .95) return greenDot;
				else if (condition > .75) return greenHalfDot;
				else if (condition > .55 ) return yellowDot;
				else if (condition > .35 ) return yellowHalfDot;
				else if (condition > .2 ) return redDot;
				else return redHalfDot;
			}
			else if (column == 1) return crop.getCropName();
			else if (column == 2) return currentPhase.getName();
			else if (column == 3) {
				double growth = crop.getPercentGrowth();
				if (growth > 100)
					growth = 100;
				return String.valueOf(growth) + "%";
			}
			else if (column == 4) return category;
			else if (column == 5) return crop.getCurrentWorkRequired();
	
			return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}

	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    private List<String> tooltips;

	    @Override
	    public Component getListCellRendererComponent(JList<?> list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (-1 < index && null != value && null != tooltipArray) {
	        	list.setToolTipText(tooltipArray.get(index));
	        }
	        return comp;
	    }

	    public void setTooltips(List<String> tooltipArray) {
	        this.tooltips = tooltipArray;

	    }
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private String prompt;

		/*
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/*
		 *  Custom rendering to display the prompt text when no item is selected
		 */
		// Add color rendering
		public Component getListCellRendererComponent(
				JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(prompt);
				return this;
			}

	        return c;
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	@Override
	public void destroy() {
		super.destroy();
		
		// take care to avoid null exceptions
		if (cropCache != null) {
			cropCache.clear();
			cropCache = null;
		}

		farm = null;
		tooltipArray = null;
		comboBoxModel= null;
		comboBox= null;
		list= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
		cropName= null;
		deletingCropType= null;
	}
}
