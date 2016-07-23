/**
 * Mars Simulation Project
 * BuildingPanelFarming.java
 * @version 3.07 2015-09-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
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
import org.mars_sim.msp.ui.swing.tool.BalloonToolTip;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;

//import de.matthiasmann.twl.GUI;
//import de.matthiasmann.twl.demo.test.TestUtils;
//import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
//import de.matthiasmann.twl.theme.ThemeManager;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.InventoryDemo;


/**
 * The FarmingBuildingPanel class is a building function panel representing
 * the crop farming status of a settlement building.
 */
public class BuildingPanelFarming
extends BuildingFunctionPanel
implements Serializable, MouseListener {
	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private static final int CENTER = 0;
	private static Logger logger = Logger.getLogger(BuildingPanelFarming.class.getName());
	// Data members

	/** The number of farmers label. */
	private JLabel farmersLabel;
	/** The number of crops label. */
	private JLabel cropsLabel;
	/** The label for the amount solar irradiance. */
	private JLabel radLabel;

	// Data cache
	/** The number of farmers cache. */
	private int farmersCache;
	/** The number of crops cache. */
	private int cropsCache;
	/** The cache for the amount of solar irradiance. */
	private int radCache;

	private int deletingCropIndex;

	//private String[] tooltipArray;
	private ArrayList tooltipArray;
	private BalloonToolTip balloonToolTip = new BalloonToolTip();
	//private String deletingCrop = "";

	// 2014-12-09 Added comboBox for crop queue
	private DefaultComboBoxModel<CropType> comboBoxModel;
	private JComboBoxMW<CropType> comboBox;
	private List<CropType> cropCache;
	private JList<CropType> list;
	private JButton opsButton;

	//private String cropInQueue;
	private ListModel listModel;
	/** Table model for crop info. */
	private CropTableModel cropTableModel;

	private JScrollPane listScrollPanel;
	
	/** The farming building. */
	private Farming farm;
	private CropType cropType;
	private CropType deletingCropType;

	
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

		// Set panel layout
		setLayout(new BorderLayout()); //new GridLayout(6, 1, 0, 0));//

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(4, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		//labelPanel.setOpaque(false);
		//labelPanel.setBackground(new Color(0,0,0,128));
		
		// Prepare farming label
		// 2014-11-21 Changed font type, size and color and label text
		// 2014-11-21 Added internationalization for the three labels
		JLabel farmingLabel = new JLabel(Msg.getString("BuildingPanelFarming.title"), JLabel.CENTER);
		JPanel farmingPanel = new JPanel(new FlowLayout());
	    farmingPanel.add(farmingLabel);
		farmingLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//farmingLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(farmingPanel);

		// Prepare solar irradiance label
		radCache = farm.getFarmerNum();
		JPanel radPanel = new JPanel(new FlowLayout());
		radLabel = new JLabel(Msg.getString("BuildingPanelFarming.solarIrradiance", radCache),  JLabel.CENTER);
	    radPanel.add(radLabel);
		balloonToolTip.createBalloonTip(radLabel, "<html>Estimated amount of available <br> sunlight on top of the <br> greenhouse roof outside</html>");
		labelPanel.add(radPanel);
		
		// Prepare farmers label
		farmersCache = farm.getFarmerNum();
		JPanel farmersPanel = new JPanel(new FlowLayout());
		farmersLabel = new JLabel(Msg.getString("BuildingPanelFarming.numberOfFarmers", farmersCache), JLabel.CENTER);
	    farmersPanel.add(farmersLabel);
		balloonToolTip.createBalloonTip(farmersLabel, "<html># of active gardeners <br> tending the greenhouse</html>");
		labelPanel.add(farmersPanel);

		// Prepare crops label
		cropsCache = farm.getCrops().size();
		JPanel cropsPanel = new JPanel(new FlowLayout());
		cropsLabel = new JLabel(Msg.getString("BuildingPanelFarming.numberOfCrops", cropsCache), JLabel.CENTER);
	    cropsPanel.add(cropsLabel);
		balloonToolTip.createBalloonTip(cropsLabel, "<html># of growing crops<br> in this greenhouse</html>");
		labelPanel.add(cropsPanel);


/*
		// 2015-09-19 Added opsPanel and opsButton
		JPanel opsPanel = new JPanel(new FlowLayout());
		labelPanel.add(opsPanel);
        opsButton = new JButton("Ops Panel");
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
		// Create scroll panel for crop table
		JScrollPane scrollPanel = new JScrollPane();
		if (farm.getBuilding().getName().equals("Large Greenhouse"))
			scrollPanel.setPreferredSize(new Dimension(200, 280)); // 280 is the best fit for 15 crops
		else
			// 2014-10-10 mkung: increased the height from 100 to 130 to make the first 5 rows of crop FULLY visible
			scrollPanel.setPreferredSize(new Dimension(200, 110)); // 110 is the best fit for 5 crops

		//scrollPanel.setOpaque(false);
		//scrollPanel.setBackground(new Color(0,0,0,128));
		add(scrollPanel, BorderLayout.CENTER);

		// Prepare crop table model
		cropTableModel = new CropTableModel(farm);

		// Prepare crop table
		JTable cropTable = new JTable(cropTableModel){
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
                int colIndex = columnAtPoint(p);
    			StringBuilder result = new StringBuilder("");

                try {
                	//if (colIndex == 1)
                		result.append(buildCropToolTip(rowIndex, null)).append("</html>");
                } catch (RuntimeException e1) {//catch null pointer exception if mouse is over an empty line
                }			
    			return result.toString();

            }
        }; // end of JTable
        
		cropTable.setDefaultRenderer(Double.class, new NumberCellRenderer());
		cropTable.setCellSelectionEnabled(false); // need it so that the tooltip can be displayed.
		cropTable.getColumnModel().getColumn(0).setPreferredWidth(5);
		cropTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(2).setPreferredWidth(40);
		cropTable.getColumnModel().getColumn(3).setPreferredWidth(20);
		cropTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		//cropTable.setOpaque(false);
		//cropTable.setBackground(new Color(0,0,0,128));
		TableStyle.setTableStyle(cropTable);

		//cropTable = TableStyle.setTableStyle(cropTable);
		scrollPanel.setViewportView(cropTable);

		JPanel queuePanel = new JPanel(new BorderLayout());
	    add(queuePanel, BorderLayout.SOUTH);
	    //queuePanel.setOpaque(false);
	    //queuePanel.setBackground(new Color(0,0,0,128));
	    
	    JPanel selectPanel = new JPanel(new FlowLayout());
	    //selectPanel.setOpaque(false);
	    //selectPanel.setBackground(new Color(0,0,0,128));
	    //JLabel selectLabel = new JLabel("Choose : ");
	    //selectLabel.setFont(new Font("Serif", Font.BOLD, 16));
	    //selectLabel.setForeground(new Color(102, 51, 0)); // dark brown
	    //selectPanel.add(selectLabel);
	    queuePanel.add(selectPanel, BorderLayout.NORTH); // 1st add


		//2014-12-09 Added addButton for adding a crop to queue
		JPanel buttonPanel = new JPanel(new BorderLayout());
		//buttonPanel.setOpaque(false);
		//buttonPanel.setBackground(new Color(0,0,0,128));
		JButton addButton = new JButton(Msg.getString("BuildingPanelFarming.addButton")); //$NON-NLS-1$
	    balloonToolTip.createBalloonTip(addButton, "<html>Select a crop from <br> the left to add</html>");
		addButton.setPreferredSize(new Dimension(60, 20));
		addButton.setFont(new Font("Serif", Font.PLAIN, 9));
		//addButton.setOpaque(false);
		//addButton.setBackground(new Color(0,0,0,128));
		//addButton.setForeground(Color.ORANGE);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cropType = (CropType) comboBox.getSelectedItem();
				farm.addCropListInQueue(cropType);
            	//System.out.println("BuildingPanelFarming.java: Just added " + cropType );
		        listUpdate();
				repaint();
			}
			});
		buttonPanel.add(addButton, BorderLayout.NORTH);
		selectPanel.add(buttonPanel);

		JButton delButton = new JButton(Msg.getString("BuildingPanelFarming.delButton")); //$NON-NLS-1$
	    balloonToolTip.createBalloonTip(delButton, "<html>Highlight a crop in <br> the queue below to delete </html>");
		delButton.setPreferredSize(new Dimension(60, 20));
		delButton.setFont(new Font("Serif", Font.PLAIN, 9));
		//delButton.setOpaque(false);
		//delButton.setBackground(new Color(0,0,0,128));
		//delButton.setForeground(Color.ORANGE);

		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!list.isSelectionEmpty() && (list.getSelectedValue() != null)) {
		           	selectCrop();
	            	farm.deleteACropFromQueue(deletingCropIndex, deletingCropType);
		           	//System.out.println("BuildingPanelFarming.java: Just deleted " + cropType );
			        // 2015-01-06 Added listUpdate()
	            	listUpdate();
	            	repaint();
				}
			}
			});
		buttonPanel.add(delButton, BorderLayout.CENTER);
	    
       	// 2014-12-09 Added crop combo box model.
        CropConfig config = SimulationConfig.instance().getCropConfiguration();
		List<CropType> cropTypeList = config.getCropList();
		//2014-12-12 Enabled Collections.sorts by implementing Comparable<CropType>
		Collections.sort(cropTypeList);
		cropCache = new ArrayList<CropType>(cropTypeList);
		comboBoxModel = new DefaultComboBoxModel<CropType>();
		
		//tooltipArray = new String[cropCache.size()];
		tooltipArray = new ArrayList();
		
		Iterator<CropType> i = cropCache.iterator();
		int j = 0;
		while (i.hasNext()) {
			CropType c = i.next();
	    	comboBoxModel.addElement(c);
			//tooltipArray[j] = buildCropToolTip(j, c).toString();
	    	tooltipArray.add(buildCropToolTip(j, c).toString());
	    	j++;
		}		
    	//System.out.println("tooltipArray is "+ tooltipArray);	
		//cropType = cropTypeList.get(0);

		// Create comboBox.
		comboBox = new JComboBoxMW<CropType>(comboBoxModel);

		// 2015-10-20 Added ComboboxToolTipRenderer to use tooltip to display the crop parameters for each crop in the combobox
	    ComboboxToolTipRenderer toolTipRenderer = new ComboboxToolTipRenderer();
	    comboBox.setRenderer(toolTipRenderer);
	    toolTipRenderer.setTooltips(tooltipArray);
		// 2014-12-01 Added PromptComboBoxRenderer() & setSelectedIndex(-1)
		//comboBox.setRenderer(new PromptComboBoxRenderer("A list of crops"));
		//comboBox.setSelectedIndex(-1);

		//comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXX");
		//comboBox.setOpaque(false);
		//comboBox.setBackground(new Color(51,25,0,128));
		//comboBox.setBackground(Color.LIGHT_GRAY);
		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	cropType = (CropType) comboBox.getSelectedItem();
            	//System.out.println("BuildingPanelFarming.java: Selected cropType is " + cropType );
            }
            });
		comboBox.setMaximumRowCount(10);
	    balloonToolTip.createBalloonTip(comboBox, "<html>Select a crop from here</html>");	    
	    selectPanel.add(comboBox);

		JPanel queueListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //new FlowLayout(FlowLayout.CENTER));

		//queueListPanel.setOpaque(false);
		//queueListPanel.setBackground(new Color(0,0,0,128));
		JPanel queueButtonLabelPanel = new JPanel(new BorderLayout()); //new FlowLayout(FlowLayout.CENTER));
		//queueButtonLabelPanel.setOpaque(false);
		//queueButtonLabelPanel.setBackground(new Color(0,0,0,128));
	    JLabel queueListLabel = new JLabel("     Crop Queue     ");//<html><center>Crop<br>Queue:</center></html>");
		//queueListLabel.setUI(new org.mars_sim.msp.ui.swing.tool.VerticalLabelUI(false));
		queueListLabel.setUI(new com.jidesoft.plaf.xerto.VerticalLabelUI(false));
	    queueListLabel.setFont( new Font( "Dialog", Font.PLAIN, 14) );
		queueListLabel.setBorder(new MarsPanelBorder());
	    queueButtonLabelPanel.add(queueListLabel, BorderLayout.NORTH);
		queueListPanel.add(queueButtonLabelPanel);
	    queuePanel.add(queueListPanel, BorderLayout.CENTER); // 2nd add
	    
		// Create scroll panel for population list.
		listScrollPanel = new JScrollPane();
		listScrollPanel.setPreferredSize(new Dimension(150, 150));
		listScrollPanel.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY) );
        //scrollPanel.setViewportBorder(null);
        //scrollPanel.setBorder(BorderFactory.createEmptyBorder());
		//listScrollPanel.getViewport().setOpaque(false);
		//listScrollPanel.getViewport().setBackground(new Color(0, 0, 0, 128));
		//listScrollPanel.setOpaque(false);
		//listScrollPanel.setBackground(new Color(0, 0, 0, 128));

		// Create list model
		listModel = new ListModel(); //settlement);
		// Create list
		list = new JList<CropType>(listModel);
	    balloonToolTip.createBalloonTip(list, "<html>Crops in the queue</html>");	
		listScrollPanel.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
		        if (!event.getValueIsAdjusting() && event != null){
					selectCrop();
		            //JList source = (JList)event.getSource();
		            //deletingCropIndex = source.getSelectedIndex();
		            //deletingCrop = source.getSelectedValue().toString();
					//if (listModel.getSize() > 0) listModel.removeElementAt(deletingCropIndex);
		        }
		    }
		});
		queueListPanel.add(listScrollPanel);
		//list.setOpaque(false);
		//list.setBackground(new Color(0, 0, 0, 128));

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
        
        result.append("<html>");
    	result.append("&emsp;&nbsp;Crop Name:&emsp;");
    	result.append(cropName);
    	result.append("<br>&emsp;&emsp;&nbsp;&nbsp;Category:&emsp;");
    	result.append(cat);
    	result.append("<br>&nbsp;Growing Days:&emsp;");
    	result.append(time);
    	result.append("<br>&emsp;Edible Mass:&emsp;");
    	result.append(mass0).append(" g/m2/day");
    	result.append("<br>&nbsp;Inedible Mass:&emsp;");
    	result.append(mass1).append(" g/m2/day");
    	result.append("<br>&nbsp;Water Content:&emsp;");
    	result.append(water).append(" %");
    	result.append("<br>&nbsp;&nbsp;PAR required:&emsp;");
    	result.append(PAR).append(" mol/m2/day");
    	
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

	@SuppressWarnings("unchecked")
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
			farmersLabel.setText("# Farmers: " + farmersCache);
		    balloonToolTip.createBalloonTip(farmersLabel, "<html># of active gardeners <br> tending the greenhouse</html>");
		}

		// Update crops label if necessary.
		if (cropsCache != farm.getCrops().size()) {
			cropsCache = farm.getCrops().size();
			cropsLabel.setText("# Crops: " + cropsCache);
		    balloonToolTip.createBalloonTip(cropsLabel, "<html># of growing crops<br> in this greenhouse</html>");
		}


		// Update solar irradiance label if necessary.
		Coordinates location = farm.getBuilding().getCoordinates();
		int rad = (int) Simulation.instance().getMars().getSurfaceFeatures().getSolarIrradiance(location);
		if (radCache != rad) {
			radCache = rad;
			radLabel.setText(Msg.getString("BuildingPanelFarming.solarIrradiance", radCache));
		    balloonToolTip.createBalloonTip(radLabel, "<html>Estimated amount of available <br> sunlight on top of the <br> greenhouse roof outside</html>");
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

        		//System.out.println("listModel.update() : Size is different. Proceed...");
        		// if the list contains duplicate items, it somehow pass this test

        		if (list.size() != c.size() || !list.containsAll(c) || !c.containsAll(list)) {
	                List<CropType> oldList = list;
	            	//System.out.println("listModel.update() : oldList.size() is " + oldList.size());
	                List<CropType> tempList = new ArrayList<CropType>(c);
	                //Collections.sort(tempList);

	             	//System.out.println("ListModel : index is " + index);
	            	//System.out.println("listModel.update() : tempList.size() is " + tempList.size());

	                list = tempList;
	                fireContentsChanged(this, 0, getSize());

	                oldList.clear();
	           }

        }
	}

    //public void setCropInQueue(String cropInQueue) {
    //	this.cropInQueue = cropInQueue;
    //}
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
			PhaseType phaseType = crop.getPhaseType();
			// 2014-10-10 Added the crop's category
			String category = crop.getCropType().getCropCategoryType().getName();

			if (column == 0) {
				double condition = crop.getCondition();
				if (condition > ((double) 2 / (double) 3)) return greenDot;
				else if (condition > ((double) 1 / (double) 3)) return yellowDot;
				else return redDot;
			}
			else if (column == 1) return Conversion.capitalize(crop.getCropType().getName());
			else if (column == 2) return phaseType.getName();
			else if (column == 3) {
				int growth = 0;
				double growingCompleted = crop.getGrowingTimeCompleted() / crop.getCropType().getGrowingTime();
				//if (phaseType == PhaseType.GERMINATION || phaseType == PhaseType.SPROUTING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else if (phaseType == PhaseType.GROWING) {
				//	growth = (int) (growingCompleted * 100D);
				//}
				//else 
				if (phaseType == PhaseType.HARVESTING)
					growth = (int) (growingCompleted * 100D);
				else if (phaseType == PhaseType.FINISHED)
					growth = 100;
				else
					growth = (int) (growingCompleted * 100D);				
				
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

	
	class ComboboxToolTipRenderer extends DefaultListCellRenderer {
	    ArrayList tooltips;
	    
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

	    public void setTooltips(ArrayList tooltipArray) {
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
			
			if (c instanceof JLabel) {
				
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
		balloonToolTip = null;
		comboBoxModel= null;
		comboBox= null;
		list= null;
		opsButton= null;
		listModel= null;
		cropTableModel= null;
		listScrollPanel= null;
		cropType= null;
		deletingCropType= null;
	}
}
