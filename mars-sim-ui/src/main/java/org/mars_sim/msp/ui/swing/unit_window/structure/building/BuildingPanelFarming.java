/**
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @version 3.1.0 2017-03-31
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.building.function.farming.Crop;
import org.mars_sim.msp.core.structure.building.function.farming.CropConfig;
import org.mars_sim.msp.core.structure.building.function.farming.CropType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;
import org.mars_sim.msp.core.structure.building.function.farming.PhaseType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.alee.laf.text.WebTextField;


/**
 * The FarmingBuildingPanel class is a building function panel representing
 * the crop farming status of a settlement building.
 */
public class BuildingPanelFarming
extends BuildingFunctionPanel
implements MouseListener {
	
	// Data members
	private WebTextField radTF, farmersTF, cropsTF, waterUsageTF, o2TF, co2TF;
	
	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The cache for the amount of solar irradiance. */
	private double radCache;

	private int deletingCropIndex;
	/** The cache value for the average water usage per sol per square meters. */
	private double waterUsageCache;
	/** The cache value for the average O2 generated per sol per square meters. */
	private double o2Cache;
	/** The cache value for the average CO2 consumed per sol per square meters. */
	private double co2Cache;
	
	private DefaultComboBoxModel<CropType> comboBoxModel;
	private JComboBoxMW<CropType> comboBox;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;
	private WebScrollPane listScrollPanel;

	/** The farming building. */
	private Farming farm;
	private CropType cropType;
	private CropType deletingCropType;
	private Coordinates location;
	
	private ArrayList<String> tooltipArray;
	private List<CropType> cropCache;
	private JList<CropType> list;
	
	private static List<CropType> cropTypeList;
	
	private static SurfaceFeatures surface;
	
	/**
	 * Constructor.
	 * @param farm {@link Farming} the farming building this panel is for.
	 * @param desktop {@link MainDesktopPane} The main desktop.
	 */
	// 2014-11-20 Added tooltip for crops
	public BuildingPanelFarming(final Farming farm, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(farm.getBuilding(), desktop);
		
		// Initialize data members
		this.farm = farm;
		location = farm.getBuilding().getCoordinates();
		surface = Simulation.instance().getMars().getSurfaceFeatures();
		CropConfig config = SimulationConfig.instance().getCropConfiguration();
		cropTypeList = new ArrayList<>(config.getCropList());
		
		// Set panel layout
		setLayout(new BorderLayout()); //new GridLayout(6, 1, 0, 0));//

		// Prepare farming label
		WebLabel farmingLabel = new WebLabel(Msg.getString("BuildingPanelFarming.title"), WebLabel.CENTER);
		WebPanel farmingPanel = new WebPanel(new FlowLayout());
	    farmingPanel.add(farmingLabel);
		farmingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		add(farmingLabel, BorderLayout.NORTH);
		//farmingLabel.setForeground(new Color(102, 51, 0)); // dark brown

		// Create label panel
		WebPanel springPanel = new WebPanel(new SpringLayout());//GridLayout(5, 1, 0, 0));
		add(springPanel, BorderLayout.CENTER);
		
		// Prepare solar irradiance label
		//WebPanel radPanel = new WebPanel(new FlowLayout());
		WebLabel radLabel = new WebLabel(Msg.getString("BuildingPanelFarming.solarIrradiance.title", radCache), WebLabel.RIGHT);
	    //radPanel.add(radLabel);
		//balloonToolTip.createBalloonTip(radLabel, "<html>Estimated amount of available <br> sunlight on top of the <br> greenhouse roof outside</html>");
		springPanel.add(radLabel);

		radCache = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		radTF = new WebTextField(radCache + "");
		radTF.setEditable(false);
		radTF.setColumns(7);
		radTF.setPreferredSize(new Dimension(120, 25));
		wrapper1.add(radTF);
		springPanel.add(wrapper1);
		
		
		// Prepare farmers label
		//WebPanel farmersPanel = new WebPanel(new FlowLayout());
		WebLabel farmersLabel = new WebLabel(Msg.getString("BuildingPanelFarming.numberOfFarmers.title"), WebLabel.RIGHT);
	    //farmersPanel.add(farmersLabel);
		//balloonToolTip.createBalloonTip(farmersLabel, "<html># of active gardeners <br> tending the greenhouse</html>");
		springPanel.add(farmersLabel);

		farmersCache = farm.getFarmerNum();
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		farmersTF = new WebTextField(farmersCache + "");
		farmersTF.setEditable(false);
		farmersTF.setColumns(3);
		farmersTF.setPreferredSize(new Dimension(120, 25));
		wrapper2.add(farmersTF);
		springPanel.add(wrapper2);
		
		// Prepare crops label
		//WebPanel cropsPanel = new WebPanel(new FlowLayout());
		WebLabel cropsLabel = new WebLabel(Msg.getString("BuildingPanelFarming.numberOfCrops.title"), WebLabel.RIGHT);
	    //cropsPanel.add(cropsLabel);
		//balloonToolTip.createBalloonTip(cropsLabel, "<html># of growing crops<br> in this greenhouse</html>");
		springPanel.add(cropsLabel);

		cropsCache = farm.getCrops().size();
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		cropsTF = new WebTextField(cropsCache + "");
		cropsTF.setEditable(false);
		cropsTF.setColumns(3);
		cropsTF.setPreferredSize(new Dimension(120, 25));
		wrapper3.add(cropsTF);
		springPanel.add(wrapper3);
		
		//WebPanel waterUsagePanel = new WebPanel(new FlowLayout());
		WebLabel waterUsageLabel = new WebLabel(Msg.getString("BuildingPanelFarming.waterUsage.title"), WebLabel.RIGHT);
		//waterUsagePanel.add(waterUsageLabel);
		waterUsageLabel.setToolTipText(Msg.getString("BuildingPanelFarming.waterUsage.tooltip"));
		springPanel.add(waterUsageLabel);
		
		waterUsageCache = farm.computeWaterUsage();
		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		waterUsageTF = new WebTextField(Msg.getString("BuildingPanelFarming.waterUsage", waterUsageCache + ""));
		waterUsageTF.setEditable(false);
		waterUsageTF.setColumns(10);
		waterUsageTF.setPreferredSize(new Dimension(120, 25));
		wrapper4.add(waterUsageTF);
		springPanel.add(wrapper4);
		
		WebLabel o2Label = new WebLabel(Msg.getString("BuildingPanelFarming.o2.title"), WebLabel.RIGHT);
		o2Label.setToolTipText(Msg.getString("BuildingPanelFarming.o2.tooltip"));
		springPanel.add(o2Label);
		
		o2Cache = farm.computeTotalO2Generated();
		WebPanel wrapper5 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		o2TF = new WebTextField(Msg.getString("BuildingPanelFarming.o2", o2Cache + ""));
		o2TF.setEditable(false);
		o2TF.setColumns(10);
		o2TF.setPreferredSize(new Dimension(120, 25));
		wrapper5.add(o2TF);
		springPanel.add(wrapper5);

		WebLabel co2Label = new WebLabel(Msg.getString("BuildingPanelFarming.co2.title"), WebLabel.RIGHT);
		co2Label.setToolTipText(Msg.getString("BuildingPanelFarming.co2.tooltip"));
		springPanel.add(co2Label);
		
		co2Cache = farm.computeTotalCO2Consumed();
		WebPanel wrapper6 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		co2TF = new WebTextField(Msg.getString("BuildingPanelFarming.co2", co2Cache + ""));
		co2TF.setEditable(false);
		co2TF.setColumns(10);
		co2TF.setPreferredSize(new Dimension(120, 25));
		wrapper6.add(co2TF);
		springPanel.add(wrapper6);
	
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(springPanel,
		                                6, 2, //rows, cols
		                                65, 20,        //initX, initY
		                                3, 1);       //xPad, yPad
		
/*
		// 2015-09-19 Added opsPanel and opsButton
		WebPanel opsPanel = new WebPanel(new FlowLayout());
		labelPanel.add(opsPanel);
        opsButton = new WebButton("Ops Panel");
        //slotButton.setOpaque(false);
        //slotButton.setBackground(new Color(51,25,0,128));
        //slotButton.setForeground(Color.ORANGE);
        //slotButton.setEnabled(processComboBox.getItemCount() > 0);
        //opsButton.setToolTipText("Click to enter the greenhouse ops panel");
		balloonToolTip.createBalloonTip(opsButton, "<html>Enter the greenhouse ops panel.</html>"); //$NON-NLS-1$

        opsButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent event) {
        		//try {
        			openGreenhouseOps();
        		//}
        		//catch (Exception e) {
        		//	logger.log(Level.SEVERE, "new slot button", e);
        		//}
        	}
        });
        opsPanel.add(opsButton);
*/
		WebPanel southPanel = new WebPanel(new BorderLayout());
		add(southPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for crop table
		WebScrollPane tableScrollPanel = new WebScrollPane();
		if (farm.getBuilding().getBuildingType().equalsIgnoreCase("Large Greenhouse"))
			tableScrollPanel.setPreferredSize(new Dimension(200, 280)); // 280 is the best fit for 15 crops
		else
			// 2014-10-10 mkung: increased the height from 100 to 130 to make the first 5 rows of crop FULLY visible
			tableScrollPanel.setPreferredSize(new Dimension(200, 110)); // 110 is the best fit for 5 crops

		southPanel.add(tableScrollPanel, BorderLayout.NORTH);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		WebTable cropTable = new WebTable(cropTableModel){
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col) {
			                Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
			                //even index, selected or not selected
			                if (Index_row % 2 == 0 && !isCellSelected(Index_row, Index_col)) {
			                    comp.setBackground(new Color(242, 242, 242));
			                }
			                else {
			                    comp.setBackground(Color.white);
			                }
			                return comp;
			            }

			// 2014-11-20 Implement Table Cell ToolTip for crops
            public String getToolTipText(MouseEvent e) {
                String name = null;
                java.awt.Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                //int colIndex = columnAtPoint(p);
    			StringBuilder result = new StringBuilder("");

                try {
                	//if (colIndex == 1)
                		result.append(buildCropToolTip(rowIndex, null)).append("</html>");
                	} catch (RuntimeException e1) {//catch null pointer exception if mouse is over an empty line
                }
    			return result.toString();

            }
        }; // end of WebTable

		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(30);

		TableStyle.setTableStyle(cropTable);
		tableScrollPanel.setViewportView(cropTable);

		WebPanel queuePanel = new WebPanel(new BorderLayout());
	    //add(queuePanel, BorderLayout.SOUTH);
	    southPanel.add(queuePanel, BorderLayout.CENTER);
	    
	    WebPanel selectPanel = new WebPanel(new FlowLayout());
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add

		//2014-12-09 Added addButton for adding a crop to queue
		WebPanel buttonPanel = new WebPanel(new BorderLayout());
		WebButton addButton = new WebButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
	    //balloonToolTip.createBalloonTip(addButton, "<html>Select a crop from <br> the left to add</html>");
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cropType = (CropType) comboBox.getSelectedItem();
				farm.addCropListInQueue(cropType);
		        listUpdate();
				repaint();
			}
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		WebButton delButton = new WebButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
	    //balloonToolTip.createBalloonTip(delButton, "<html>Highlight a crop in <br> the queue below to delete </html>");
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));

		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
		           	selectCrop();
	            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
	            	listUpdate();
	            	repaint();
				}
			}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);

       	// Set up crop combo box model.
		Collections.sort(cropTypeList);
		cropCache = new ArrayList<CropType>(cropTypeList);
		comboBoxModel = new DefaultComboBoxModel<CropType>();

		//tooltipArray = new String[cropCache.size()];
		tooltipArray = new ArrayList<>();

		Iterator<CropType> i = cropCache.iterator();
		int j = 0;
		while (i.hasNext()) {
			CropType c = i.next();
	    	comboBoxModel.addElement(c);
			//tooltipArray[j] = buildCropToolTip(j, c).toString();
	    	tooltipArray.add(buildCropToolTip(j, c).toString());
	    	j++;
		}

		// Create comboBox.
		comboBox = new JComboBoxMW<CropType>(comboBoxModel);

		// 2015-10-20 Added ComboboxToolTipRenderer to use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
	    toolTipRenderer.setTooltips(tooltipArray);

		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	cropType = (CropType) comboBox.getSelectedItem();
            }
        });
		comboBox.setMaximumRowCount(10);
	    //balloonToolTip.createBalloonTip(comboBox, "<html>Select a crop from here</html>");
	    selectPanel.add(comboBox);

		WebPanel queueListPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		WebPanel queueButtonLabelPanel = new WebPanel(new BorderLayout());
	    WebLabel queueListLabel = new WebLabel("     Crop Queue     ");
	    //<html><center>Crop<br>Queue:</center></html>");
		//queueListLabel.setUI(new org.mars_sim.msp.ui.swing.tool.VerticalLabelUI(false));
		queueListLabel.setUI(new com.jidesoft.plaf.xerto.VerticalLabelUI(false));
	    queueListLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER); // 2nd add
	    
		// Create scroll panel for population list.
		listScrollPanel = new WebScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));
		listScrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );

		// Create list model
		listModel = new ListModel(); //settlement);
		// Create list
		list = new JList<CropType>(listModel);
	    //balloonToolTip.createBalloonTip(list, "<html>Crops in the queue</html>");
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting() && event != null){
					selectCrop();
		        }
		    }
		});
		queueListPanel.add(listScrollPanel);
	}

	/*
	 * Builds an tooltip for displaying the growth parameters of a crop
	 */
	public StringBuilder buildCropToolTip(int row, CropType ct) {

		StringBuilder result = new StringBuilder("");
		String cropName, cat;
		double time;
		double mass0, mass1;
		double water, PAR;

        if (ct == null) {
    		List<Crop> crops = farm.getCrops();
            Crop crop = crops.get(row);
        	cropName = Conversion.capitalize(crop.getCropType().getName());
            cat = crop.getCropType().getCropCategoryType().getName();
        	mass0 = crop.getCropType().getEdibleBiomass();
        	water = 100 * crop.getCropType().getEdibleWaterContent();
        	mass1 = crop.getCropType().getInedibleBiomass();
        	time = crop.getCropType().getGrowingTime() /1000;
        	PAR = crop.getCropType().getDailyPAR();
        }
        else {
        	cropName = Conversion.capitalize(ct.getName());
            cat = ct.getCropCategoryType().getName();
        	mass0 = ct.getEdibleBiomass();
        	water = 100 * ct.getEdibleWaterContent();
        	mass1 = ct.getInedibleBiomass();
        	time = ct.getGrowingTime() /1000;
        	PAR = ct.getDailyPAR();

        }

        result.append("<html>").append("&emsp;&nbsp;Crop Name:&emsp;").append(cropName);
    	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;").append(cat);
    	result.append("<br>&nbsp;Growing Days:&emsp;").append(time);
    	result.append("<br>&emsp;Edible Mass:&emsp;").append(mass0).append(" g/m2/day");
    	result.append("<br>&nbsp;Inedible Mass:&emsp;").append(mass1).append(" g/m2/day");
    	result.append("<br>&nbsp;Water Content:&emsp;").append(water).append(" %");
    	result.append("<br>&nbsp;&nbsp;PAR required:&emsp;").append(PAR).append(" mol/m2/day");

    	return result;
	}
	/*
	 * Creates a TWL display window for greenhouse operations
	 */
	// 2015-09-19 openGrowingArea()()
    public void openGreenhouseOps() {
/*
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("Greenhouse Operations Panel");
            Display.setVSyncEnabled(true);

            Mouse.setClipMouseCoordinatesToWindow(false);

            InventoryDemo ops = new InventoryDemo();

            LWJGLRenderer renderer = new LWJGLRenderer();
            GUI gui = new GUI(ops, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    InventoryDemo.class.getResource("/twl/inventory/inventory.xml"), renderer);
            gui.applyTheme(theme);

            gui.validateLayout();
            ops.positionFrame();

            while(!Display.isCloseRequested() && !ops.quit) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                gui.update();
                Display.update();
            }

            gui.destroy();
            theme.destroy();
        } catch (Exception ex) {
            TestUtils.showErrMsg(ex);
        }
        Display.destroy();
*/
    }


	/**
	 * Selects Crop
	 * @param table
	 */
	public void selectCrop() {

		CropType cropType = (CropType) list.getSelectedValue();
		if (cropType != null) {
			deletingCropType = cropType;
			deletingCropIndex = list.getSelectedIndex();
		} else

        listUpdate();
	}

	public void listUpdate() {

		listModel.update();
 		list.validate();
 		list.revalidate();
 		list.repaint();
 		listScrollPanel.validate();
 		listScrollPanel.revalidate();
 		listScrollPanel.repaint();
		//comboBox.setRenderer(new PromptComboBoxRenderer("A list of crops"));
		//comboBox.setSelectedIndex(-1);
    	//list.clearSelection(); // cause setting deletingCropIndex to -1
    	//list.setSelectedIndex(0);
	}

	/**
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {

		// TODO: If double-click, open tooltip for the selected crop?
		if (event.getClickCount() >= 2) {
			selectCrop();
			if (deletingCropType != null) {
            	//farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
            	//listModel.update();
			}
		}
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}


	/**
	 * Update this panel
	 */
	public void update() {

		// Update farmers label if necessary.
		if (farmersCache != farm.getFarmerNum()) {
			farmersCache = farm.getFarmerNum();
			farmersTF.setText(farmersCache + "");
		    //balloonToolTip.createBalloonTip(farmersLabel, "<html># of active gardeners <br> tending the greenhouse</html>");
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsTF.setText(cropsCache + "");
		    //balloonToolTip.createBalloonTip(cropsLabel, "<html># of growing crops<br> in this greenhouse</html>");
		}

		// Update solar irradiance label if necessary.
		//Coordinates location = farm.getBuilding().getCoordinates();
		double rad = Math.round(surface.getSolarIrradiance(location)*10.0)/10.0;
		if (radCache != rad) {
			radCache = rad;
			radTF.setText(Msg.getString("BuildingPanelFarming.solarIrradiance", radCache));
		    //balloonToolTip.createBalloonTip(radLabel, "<html>Estimated amount of available <br> sunlight on top of the <br> greenhouse roof outside</html>");
		}

		// Update the average water usage
		double new_water = farm.computeWaterUsage();
		if (waterUsageCache != new_water) {
			waterUsageCache = new_water;
			waterUsageTF.setText(Msg.getString("BuildingPanelFarming.waterUsage", waterUsageCache));
		}

		// Update the average O2 generated
		double new_o2 = farm.computeTotalO2Generated();
		if (o2Cache != new_o2) {
			o2Cache = new_o2;
			o2TF.setText(Msg.getString("BuildingPanelFarming.o2", o2Cache));
		}

		// Update the average CO2 consumed
		double new_co2 = farm.computeTotalCO2Consumed();
		if (co2Cache != new_co2) {
			co2Cache = new_co2;
			co2TF.setText(Msg.getString("BuildingPanelFarming.co2", co2Cache));
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
	private class ListModel extends AbstractListModel<CropType> {

	    /** default serial id. */
	    private static final long serialVersionUID = 1L;

	    //private Settlement settlement;
	    private List<CropType> list;

	    private ListModel() {
	    	//System.out.println("ListModel constructor");

        	List<CropType> c = farm.getCropListInQueue();
	        if (c != null)
	        	list = new ArrayList<CropType>(c);
	        else list = null;
	        //Collections.sort(list);
	    }

        @Override
        public CropType getElementAt(int index) {
        	//System.out.println("ListModel : index is " + index);
        	//System.out.println("ListModel : list.size() is " + list.size());

        	CropType result = null;

            if ((index >= 0) && (index < list.size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
         	//System.out.println("ListModel : index is " + index);
        	//System.out.println("ListModel : list.size() is " + list.size());
        	if (list == null)
        		return 0;
        	else return list.size();
        }

        /**
         * Update the list model.
         */
        public void update() {

        	List<CropType> c = farm.getCropListInQueue();
        		// if the list contains duplicate items, it somehow pass this test
        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
	                List<CropType> oldList = list;
	                List<CropType> tempList = new ArrayList<CropType>(c);
	                //Collections.sort(tempList);

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

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		private Farming farm;
		private java.util.List<Crop> crops;
		private ImageIcon redDot;
		private ImageIcon yellowDot;
		private ImageIcon greenDot;

		private CropTableModel(Farming farm) {
			this.farm = farm;
			crops = farm.getCrops();
			redDot = ImageLoader.getIcon("RedDot");
			yellowDot = ImageLoader.getIcon("YellowDot");
			greenDot = ImageLoader.getIcon("GreenDot");
		}

		public int getRowCount() {
			return crops.size();
		}

		// 2014-10-10 mkung: change from 4 to 5 in order to include the crop's category as columnIndex 4
		public int getColumnCount() {
			return 5;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = ImageIcon.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Health";
			else if (columnIndex == 1) return "Name";
			else if (columnIndex == 2) return "Phase";
			else if (columnIndex == 3) return "Growth";
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (columnIndex == 4) return "Category";
			else return null;
		}

		public Object getValueAt(int row, int column) {

			Crop crop = crops.get(row);
			//String phase = crop.getPhase();
			PhaseType currentPhase = crop.getPhaseType();
			// 2014-10-10 Added the crop's category
			String category = crop.getCropType().getCropCategoryType().getName();

			if (column == 0) {
				double condition = crop.getHealthCondition();
				if (condition > ((double) 2 / (double) 3)) return greenDot;
				else if (condition > ((double) 1 / (double) 3)) return yellowDot;
				else return redDot;
			}
			else if (column == 1) return Conversion.capitalize(crop.getCropType().getName());
			else if (column == 2) return currentPhase.getName();
			else if (column == 3) {
				double growth = 0;
				//if (phaseType == PhaseType.GERMINATION || phaseType == PhaseType.SPROUTING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else if (phaseType == PhaseType.GROWING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else
				if (currentPhase == PhaseType.HARVESTING) {
					double growingCompleted = crop.getGrowingTimeCompleted() / crop.getCropType().getGrowingTime();
					growth = Math.round(growingCompleted * 1000D)/10D;
				}
				else if (currentPhase == PhaseType.FINISHED) {
					growth = 100;
				}
				else {
					double growingCompleted = crop.getGrowingTimeCompleted() / crop.getCropType().getGrowingTime();
					growth = Math.round(growingCompleted * 1000D)/10D;
				}

				return String.valueOf(growth) + "%";
			}
			// 2014-10-10 mkung: added column 4 showing the crop's category
			else if (column == 4) return Conversion.capitalize(category);
			else return null;
		}

		public void update() {
			if (!crops.equals(farm.getCrops())) crops = farm.getCrops();
			fireTableDataChanged();
		}
	}


	@SuppressWarnings("serial")
	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    private ArrayList<String> tooltips;

	    @Override
	    public Component getListCellRendererComponent(JList list, Object value,
	                        int index, boolean isSelected, boolean cellHasFocus) {

	    	JComponent comp = (JComponent) super.getListCellRendererComponent(list,
	                value, index, isSelected, cellHasFocus);

	        if (-1 < index && null != value && null != tooltipArray) {
	        	list.setToolTipText((String) tooltipArray.get(index));
	        	//System.out.println("value.toString is "+ value.toString());
	        	//System.out.println("list.toString is "+ list.toString());
	        	//balloonToolTip.createListItemBalloonTip(list, (String)(tooltipArray.get(index)), index);
	        }
	        return comp;
	    }

	    public void setTooltips(ArrayList<String> tooltipArray) {
	        this.tooltips = tooltipArray;

	    }
	}

	// 2014-12-09 Added PromptComboBoxRenderer()
	class PromptComboBoxRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;

		private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	    // Width doesn't matter as the combo box will size
	    //private Dimension preferredSize = new Dimension(0, 20);

		/*
		 *  Set the text to display when no item has been selected.
		 */
		public PromptComboBoxRenderer(String prompt) {
			this.prompt = prompt;
		}

		/*
		 *  Custom rendering to display the prompt text when no item is selected
		 */
		// 2014-12-09 Added color rendering
		public Component getListCellRendererComponent(
			JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				Component c = defaultRenderer.getListCellRendererComponent(
		                list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText(Conversion.capitalize(prompt));
				return this;
			}
			//if (index == -1) {
				//value = prompt.toString();
			//	setText(prompt);
			//	return this;
			//}
			//else {
				//setText(Conversion.capitalize(value.toString()));
				//c = super.getListCellRendererComponent(
	            //        list, Conversion.capitalize(value.toString()), index, isSelected, cellHasFocus);
				//CropType ct = (CropType) value;
				//setText(Conversion.capitalize(ct.getName()));
				//String s = buildCropToolTip(index).toString();
			    //balloonToolTip.createBalloonTip(list, s);
			//}

			if (c instanceof WebLabel) {

	            if (isSelected) {
	                //c.setBackground(Color.orange);
	            } else {
	                //c.setBackground(Color.white);
	                //c.setBackground(new Color(51,25,0,128));
	            }

	        } else {
	        	//c.setBackground(Color.white);
	            //c.setBackground(new Color(51,25,0,128));
	            c = super.getListCellRendererComponent(
	                    list, value, index, isSelected, cellHasFocus);
	        }
	        return c;
		}
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		// take care to avoid null exceptions
		if (cropCache != null) {
			cropCache.clear();
			cropCache = null;
		}

		farm = null;
		tooltipArray = null;
		//balloonToolTip = null;
		comboBoxModel= null;
		comboBox= null;
		list= null;
		//opsButton= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
		cropType= null;
		deletingCropType= null;
	}
}
