/**
 * Mars Simulation Project
 * SettlementTransparentPanel.java
 * @version 3.08 2015-03-28
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.Painter;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import eu.hansolo.steelseries.gauges.DisplaySingle;
import eu.hansolo.steelseries.tools.LcdColor;

public class SettlementTransparentPanel extends JComponent {

	private static final long serialVersionUID = 1L;
	/** Rotation change (radians per rotation button press). */
	private static final double ROTATION_CHANGE = Math.PI / 20D;
	/** Zoom change. */
	private static final double ZOOM_CHANGE = 1D;


	private JSlider zoomSlider;
	private JPanel rightPane, borderPane, nameBtnPane, zoomPane, labelPane, buttonPane, controlPane, settlementPanel, infoP, renameP ;
	private JButton renameBtn, infoButton;
	private JLabel zoomLabel;
	private JPopupMenu labelsMenu;
	/** Lists all settlements. */
	private JComboBoxMW<?> settlementListBox;
	/** Combo box model. */
	private SettlementComboBoxModel settlementCBModel;

	private SettlementMapPanel mapPanel;
	private MainDesktopPane desktop;
	//private Settlement settlement;

    public SettlementTransparentPanel(MainDesktopPane desktop, SettlementMapPanel mapPanel) {

        this.mapPanel = mapPanel;
        this.desktop = desktop;

		setDoubleBuffered(true);

        createAndShowGUI();
    }


    public void createAndShowGUI() {

        buildSettlementNameComboBox();
        buildInfoP();
        buildrenameBtn();

        buildZoomLabel();
        buildZoomSlider();
        buildButtonPane();
        buildLabelPane();

		nameBtnPane = new JPanel(new FlowLayout());
		nameBtnPane.setBackground(new Color(0,0,0,0));
        nameBtnPane.setOpaque(false);

      	nameBtnPane.add(infoP);
       	nameBtnPane.add(renameP);
       	nameBtnPane.add(new JLabel(""));

		settlementPanel = new JPanel();//new BorderLayout());
		settlementPanel.setBackground(new Color(0,0,0,0));
		settlementPanel.add(settlementListBox);//, BorderLayout.CENTER);

		Box box = new Box(BoxLayout.Y_AXIS);
	    box.add(Box.createVerticalGlue());
	    box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	    //box.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
	    box.add(Box.createVerticalGlue());
		box.setOpaque(false);
		box.setBackground(new Color(0,0,0,0));
	    box.add(settlementPanel);
	    box.add(nameBtnPane);
/*
	    JPanel lcdPanel = new JPanel();
	    DisplaySingle lcd1 = new DisplaySingle();
        lcd1.setLcdUnitString("S");//dir_N_S);
        lcd1.setLcdValueAnimated(5);//locationCache.getLatitudeDouble());
        lcd1.setLcdInfoString("Latitude");
        lcd1.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        //lcd1.init(150, 100);
        lcd1.setMaximumSize(new Dimension(200, 100));
        lcd1.setPreferredSize(new Dimension(150,50));
        lcd1.setVisible(true);
        lcdPanel.add(lcd1);
        lcdPanel.setOpaque(false);
        lcdPanel.setBackground(new Color(0,0,0,125));
		mapPanel.add(lcdPanel, BorderLayout.WEST);
        //box.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        //box.add(Box.createHorizontalGlue());
	    //box.add(lcd1);
 */      
        // Make panel drag-able
//    	ComponentMover cmName = new ComponentMover();
//    	cmName.registerComponent(box);

	    borderPane = new JPanel(new BorderLayout());//new GridLayout(2,1,2,2));
	    borderPane.setBackground(new Color(0,0,0,0));
	    controlPane = new JPanel(new GridLayout(2,1,2,2));
	    controlPane.setBackground(new Color(0,0,0,0));
	    zoomPane = new JPanel(new GridLayout(3,1,2,2));
		zoomPane.setBackground(new Color(0,0,0,15));
		zoomPane.setBackground(new Color(0,0,0,0));
	    rightPane = new JPanel(new BorderLayout());
//		rightPane.setBackground(new Color(0,0,0,15));
	    rightPane.setBackground(new Color(0,0,0,0));

	    controlPane.add(buttonPane);
	    controlPane.add(zoomLabel);
	    borderPane.add(controlPane, BorderLayout.SOUTH);

        zoomPane.add(borderPane);
        zoomPane.add(zoomSlider);
        zoomPane.add(labelPane);

        // Make panel drag-able
//  		ComponentMover cmZoom = new ComponentMover(zoomPane);
		//cmZoom.registerComponent(rightPane);
//		cmZoom.registerComponent(zoomPane);

	    mapPanel.add(box, BorderLayout.NORTH);
        //mapPanel.add(rightPane, BorderLayout.EAST);
        //mapPanel.add(zoomPane, BorderLayout.WEST);
        mapPanel.add(zoomPane, BorderLayout.EAST);
        mapPanel.setVisible(true);
    }


	@SuppressWarnings("unchecked")
	public void buildSettlementNameComboBox() {

		settlementCBModel = new SettlementComboBoxModel();
		settlementListBox = new JComboBoxMW(settlementCBModel);
		//settlementListBox.setBorder(null);
		settlementListBox.setOpaque(false);//setBackground(new Color(139,69,19));
		//settlementListBox.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
		//((JLabel)settlementListBox.getRenderer()).setBackground(Color.darkGray);;//SwingConstants.CENTER);
		settlementListBox.setBackground(new Color(51,25,0,40)); // dull gold color
		settlementListBox.setFont(new Font("Dialog", Font.BOLD, 18));
		settlementListBox.setForeground(Color.GREEN);
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementListBox.setRenderer(new PromptComboBoxRenderer());
		settlementListBox.addItemListener(new ItemListener() {
			@Override
			// unitUpdate will update combobox when a new building is added
			public void itemStateChanged(ItemEvent event) {
				Settlement s;
				// 2014-12-19 Added if else clause for selecting the settlement that the new building is arriving
				if (desktop.getIsTransportingBuilding()) {
					s = desktop.getSettlement();
					settlementListBox.setSelectedItem(s);
					//settlementListBox.setForeground(Color.green);
				}
				else {
					s = (Settlement) event.getItem();
				}
				//System.out.println(" settlement is " + settlement.getName());
				mapPanel.setSettlement(s);
			}
		});


		if (settlementListBox.getModel().getSize() > 0) {
			settlementListBox.setSelectedIndex(0);
			Settlement s;
			// 2014-12-19 Added if else clause for selecting the settlement that the new building is arriving
			if (desktop.getIsTransportingBuilding()) {
				s = desktop.getSettlement();
				settlementListBox.setSelectedItem(s);
				//settlementListBox.setForeground(Color.green);
			}
			else {
				s = (Settlement) settlementListBox.getSelectedItem();
			}
			//System.out.println(" settlement is " + settlement.getName());
			mapPanel.setSettlement(s);
		}

	}

	class PromptComboBoxRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;
		//public boolean isOptimizedDrawingEnabled();
		//private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		public PromptComboBoxRenderer(){
			//defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		    //settlementListBox.setRenderer(defaultRenderer);
		    //setOpaque(false);
		    setHorizontalAlignment(CENTER);
		    setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
			}

			@Override
		    public Component getListCellRendererComponent(JList list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
		        JComponent result = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				if (value == null) {
					setText( prompt );
					//this.setForeground(Color.green);
			        //this.setBackground(new Color(184,134,11));
					return this;
				}

				if (isSelected) {
					result.setForeground(Color.GREEN);
			        result.setBackground(new Color(184,134,11,50));

		          // unselected, and not the DnD drop location
		          } else {
		        	  result.setForeground(Color.ORANGE);
		        	  result.setBackground(new Color(255,229,204,50));
				      //result.setBackground(new Color(184,134,11)); //brown
		          }

		        //result.setOpaque(false);

		        return result;
		    }
	}


    public void buildZoomLabel() {

		zoomLabel = new JLabel(Msg.getString("SettlementTransparentPanel.label.zoom")); //$NON-NLS-1$
		//zoomLabel.setPreferredSize(new Dimension(60, 20));
		zoomLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		zoomLabel.setForeground(Color.GREEN);
		//zoomLabel.setContentAreaFilled(false);
		zoomLabel.setOpaque(false);
		zoomLabel.setVerticalAlignment(JLabel.BOTTOM);
		zoomLabel.setHorizontalAlignment(JLabel.CENTER);
		//zoomLabel.setBorder(new LineBorder(Color.green, 1, true));
		//zoomLabel.setBorderPainted(true);
		zoomLabel.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$

    }

    public void buildZoomSlider() {

        UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.green);
                g.fillOval(1, 1, w-1, h-1);
                g.setColor(Color.WHITE);
                g.drawOval(1, 1, w-1, h-1);
            }
        });
        sliderDefaults.put("Slider:SliderTrack.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                //g.setColor(new Color(139,69,19)); // brown
                g.setColor(Color.green);
                g.fillRoundRect(0, 6, w, 6, 6, 6); // g.fillRoundRect(0, 6, w-1, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(JSlider.VERTICAL, -10, 10, 0);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        zoomSlider.putClientProperty("Nimbus.Overrides",sliderDefaults);
        zoomSlider.putClientProperty("Nimbus.Overrides.InheritDefaults",false);

    	//zoomSlider = new JSliderMW(JSlider.VERTICAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(5);
		//zoomSlider.setMinorTickSpacing(1);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.GREEN);
		zoomSlider.setBackground(new Color(0,0,0,15));
		//zoomSlider.setContentAreaFilled(false);
		zoomSlider.setOpaque(false);
		zoomSlider.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				// Change scale of map based on slider position.
				int sliderValue = zoomSlider.getValue();
				double defaultScale = SettlementMapPanel.DEFAULT_SCALE;
				double newScale = defaultScale;
				if (sliderValue > 0) {
					newScale += defaultScale * (double) sliderValue * ZOOM_CHANGE;
				}
				else if (sliderValue < 0) {
					newScale = defaultScale / (1D + ((double) sliderValue * -1D * ZOOM_CHANGE));
				}
				mapPanel.setScale(newScale);
			}
		});

		//zoomPane.add(zoomSlider);

		// Add mouse wheel listener for zooming.
		mapPanel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent evt) {
				int numClicks = evt.getWheelRotation();
				if (numClicks > 0) {
					// Move zoom slider down.
					if (zoomSlider.getValue() > zoomSlider.getMinimum())
						zoomSlider.setValue(zoomSlider.getValue() - 1);
				}
				else if (numClicks < 0) {
					// Move zoom slider up.
					if (zoomSlider.getValue() < zoomSlider.getMaximum())
						zoomSlider.setValue(zoomSlider.getValue() + 1);
				}
			}
		});

    }

    public void buildInfoP() {

		infoP = new JPanel(new FlowLayout());
		infoP.setBackground(new Color(0,0,0,0));
		infoP.setAlignmentX(FlowLayout.CENTER);

		infoButton = new JButton(Msg.getString("SettlementTransparentPanel.button.info")); //$NON-NLS-1$
		infoButton.setPreferredSize(new Dimension(50, 20)); //35, 20));
		infoButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		infoButton.setForeground(Color.GREEN);
		infoButton.setContentAreaFilled(false);
		//infoButton.setOpaque(false); // text disappeared if setOpaque(false)
		infoButton.setBorder(new LineBorder(Color.green, 1, true));
		//infoButton.setBorderPainted(true);
		infoButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.info")); //$NON-NLS-1$
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Settlement settlement = mapPanel.getSettlement();
				if (settlement != null) {
					// 2014-10-26 obtained settlement object
					//setCurrentSettlement(settlement);
					desktop.openUnitWindow(settlement, false);
				}
			}
		});
		infoP.add(infoButton);
    }

    public void buildrenameBtn() {

		renameP  = new JPanel(new FlowLayout());
		renameP.setBackground(new Color(0,0,0,0));
		renameP.setAlignmentX(FlowLayout.CENTER);

		renameBtn = new JButton(Msg.getString("SettlementTransparentPanel.button.rename")); //$NON-NLS-1$
		renameBtn.setPreferredSize(new Dimension(80, 20)); //
		renameBtn.setFont(new Font("Dialog", Font.PLAIN, 12));
		renameBtn.setForeground(Color.GREEN);
		renameBtn.setContentAreaFilled(false);
		//renameBtn.setOpaque(false); // text disappeared if setOpaque(false)
		renameBtn.setBorder(new LineBorder(Color.green, 1, true));
		infoButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.rename")); //$NON-NLS-1$
		renameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				renameSettlement();
			}
		});
		renameP.add(renameBtn);
    }


    public void buildButtonPane() {

        buttonPane = new JPanel();
        buttonPane.setBackground(new Color(0,0,0,0));
		JButton rotateClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.clockwise"))); //$NON-NLS-1$
		rotateClockwiseButton.setPreferredSize(new Dimension(20, 20));
		rotateClockwiseButton.setOpaque(false);
		rotateClockwiseButton.setForeground(Color.GREEN);
		rotateClockwiseButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		rotateClockwiseButton.setContentAreaFilled(false);
		rotateClockwiseButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		rotateClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			}
		});

		buttonPane.add(rotateClockwiseButton);

		JButton recenterButton = new JButton(ImageLoader.getIcon(Msg.getString("img.centerMap")));//Msg.getString("SettlementTransparentPanel.button.recenter")); //$NON-NLS-1$
		recenterButton.setPreferredSize(new Dimension(20, 20));
		recenterButton.setOpaque(false);
		recenterButton.setForeground(Color.GREEN);
		recenterButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		recenterButton.setContentAreaFilled(false);
		recenterButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		recenterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.reCenter();
				zoomSlider.setValue(0);
			}
		});

		buttonPane.add(recenterButton);

		// Create rotate counter-clockwise button.
		JButton rotateCounterClockwiseButton = new JButton(ImageLoader.getIcon(Msg.getString("img.counterClockwise"))); //$NON-NLS-1$
		rotateCounterClockwiseButton.setPreferredSize(new Dimension(20, 20));
		rotateCounterClockwiseButton.setOpaque(false);
		rotateCounterClockwiseButton.setForeground(Color.GREEN);
		rotateCounterClockwiseButton.setContentAreaFilled(false);
		rotateCounterClockwiseButton.setBorder(new LineBorder(Color.GREEN, 1, true));
		rotateCounterClockwiseButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		rotateCounterClockwiseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			}
		});

		buttonPane.add(rotateCounterClockwiseButton);

    }

    public void buildLabelPane() {

        labelPane = new JPanel();
        labelPane.setBackground(new Color(0,0,0,0));

		JButton labelsButton = new JButton(Msg.getString("SettlementTransparentPanel.button.labels")); //$NON-NLS-1$
		labelsButton.setOpaque(false);
		//labelsButton.setFont(new Font("Dialog", Font.BOLD, 16));
		//labelsButton.setBackground(new Color(139,69,19)); // (139,69,19) is brown
		//labelsButton.setBackground(new Color(139,69,19,40));
		//labelsButton.setBackground(new Color(51,25,0,5)); // dull gold color
		labelsButton.setBackground(new Color(0,0,0,0));
		labelsButton.setPreferredSize(new Dimension(80, 20));
		labelsButton.setForeground(Color.green);
		//labelsButton.setVerticalAlignment(JLabel.TOP);
		labelsButton.setHorizontalAlignment(JLabel.CENTER);
		//labelsButton.setContentAreaFilled(false); more artifact when enabled
		labelsButton.setBorder(new LineBorder(Color.green, 1, true));
		labelsButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.labels")); //$NON-NLS-1$
		labelsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JButton button = (JButton) evt.getSource();
				if (labelsMenu == null) {
					labelsMenu = createLabelsMenu();
				}
				labelsMenu.show(button, 0, button.getHeight());
				//repaint();
			}
		});

		labelPane.add(labelsButton);

	}
    /*
    class MyCellRenderer extends JLabel implements ListCellRenderer<Object>  {
		private static final long serialVersionUID = 1L;

		public MyCellRenderer() {
	          setOpaque(true);
	      }
	      public Component getListCellRendererComponent(JList<?> list,
	                                                    Object value,
	                                                    int index,
	                                                    boolean isSelected,
	                                                    boolean cellHasFocus) {

	          setText(value.toString());
	  		  setBackground(new Color(0,0,0,0));

	          Color background = Color.orange;
	          Color foreground = Color.green;

	          // check if this cell represents the current DnD drop location
	          JList.DropLocation dropLocation = list.getDropLocation();

	          if (dropLocation != null
	                  && !dropLocation.isInsert()
	                  && dropLocation.getIndex() == index) {

	          // check if this cell is selected
	          } else if (isSelected) {
	              background = Color.orange;
	              foreground = Color.green;

	          // unselected
	          } else {
	          };

	          setBackground(background);
	          setForeground(foreground);

	          return this;
	      }
    }
*/
	/**
	 * Create the labels popup menu.
	 * @return popup menu.
	 */
	public JPopupMenu createLabelsMenu() {
		JPopupMenu result = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$

		result.setOpaque(false);
		result.setBorder(BorderFactory.createLineBorder(new Color(139,69,19)));// dark brown
		result.setBackground(new Color(222,184,135,0)); // pale silky brown
        UIResource res = new BorderUIResource.LineBorderUIResource(new Color(139,69,19));
        UIManager.put("PopupMenu.border", res);
        result.setLightWeightPopupEnabled(false);

		// Create Day Night Layer menu item.
		JCustomCheckBoxMenuItem dayNightLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.dayNightLayer"), mapPanel.isShowDayNightLayer()); //$NON-NLS-1$
		dayNightLabelMenuItem.setForeground(new Color(139,69,19));
		dayNightLabelMenuItem.setContentAreaFilled(false);
		dayNightLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowDayNightLayer(!mapPanel.isShowDayNightLayer());
			}
		});
		result.add(dayNightLabelMenuItem);

		// Create building label menu item.
		JCustomCheckBoxMenuItem buildingLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.buildings"), mapPanel.isShowBuildingLabels()); //$NON-NLS-1$
		// 2014-12-24 Added setting setForeground setContentAreaFilled setOpaque
		buildingLabelMenuItem.setForeground(new Color(139,69,19));
		//buildingLabelMenuItem.setBackground(new Color(222,184,135,0));
		buildingLabelMenuItem.setContentAreaFilled(false);
		//buildingLabelMenuItem.setOpaque(false);
		buildingLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowBuildingLabels(!mapPanel.isShowBuildingLabels());
			}
		});
		result.add(buildingLabelMenuItem);

		// Create construction/salvage label menu item.
		JCustomCheckBoxMenuItem constructionLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.constructionSites"), mapPanel.isShowConstructionLabels()); //$NON-NLS-1$
		constructionLabelMenuItem.setForeground(new Color(139,69,19));
		//constructionLabelMenuItem.setBackground(new Color(222,184,135,0));
		constructionLabelMenuItem.setContentAreaFilled(false);
		//constructionLabelMenuItem.setOpaque(false);
		constructionLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowConstructionLabels(!mapPanel.isShowConstructionLabels());
			}
		});
		result.add(constructionLabelMenuItem);

		// Create vehicle label menu item.
		JCustomCheckBoxMenuItem vehicleLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.vehicles"), mapPanel.isShowVehicleLabels()); //$NON-NLS-1$
		vehicleLabelMenuItem.setForeground(new Color(139,69,19));
		//vehicleLabelMenuItem.setBackground(new Color(222,184,135,0));
		vehicleLabelMenuItem.setContentAreaFilled(false);
		//vehicleLabelMenuItem.setOpaque(false);
		vehicleLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowVehicleLabels(!mapPanel.isShowVehicleLabels());
			}
		});
		result.add(vehicleLabelMenuItem);

		// Create person label menu item.
		JCustomCheckBoxMenuItem personLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.people"), mapPanel.isShowPersonLabels()); //$NON-NLS-1$
		personLabelMenuItem.setForeground(new Color(139,69,19));
		//personLabelMenuItem.setBackground(new Color(222,184,135,0));
		personLabelMenuItem.setContentAreaFilled(false);
		//personLabelMenuItem.setOpaque(false);
		personLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowPersonLabels(!mapPanel.isShowPersonLabels());
			}
		});
		result.add(personLabelMenuItem);

		// Create person label menu item.
		JCustomCheckBoxMenuItem robotLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.robots"), mapPanel.isShowRobotLabels()); //$NON-NLS-1$
		robotLabelMenuItem.setForeground(new Color(139,69,19));
		robotLabelMenuItem.setContentAreaFilled(false);
		robotLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowRobotLabels(!mapPanel.isShowRobotLabels());
			}
		});
		result.add(robotLabelMenuItem);

		result.pack();

		return result;
	}


	public class JCustomCheckBoxMenuItem extends JCheckBoxMenuItem {

		public JCustomCheckBoxMenuItem(String s, boolean b) {
			super(s, b);
		}
		private static final long serialVersionUID = 1L;
		/*public void paint(Graphics g) {
			//protected void paintComponent(Graphics g) {
				//super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                super.paint(g2d);
                g2d.dispose();
        } */
	}

	
	/**
	 * Change and validate the new name of the Settlement
	 * @return call Dialog popup
	 */
	// 2014-10-26 Modified renameSettlement()
	public void renameSettlement() {

		String oldName = mapPanel.getSettlement().getName();

		//logger.info("Old name was " + oldName);
		//boolean isFX = Platform.isFxApplicationThread();

		if (desktop.getMainScene() != null) {

			Platform.runLater(() -> {
				
				String newName = askNameFX(oldName).trim();
				if (!isBlank(newName)) { // newName != null && !newName.isEmpty() && newName with only whitespace(s)
					mapPanel.getSettlement().changeName(newName);
	            }
				else {
					Alert alert = new Alert(AlertType.ERROR, "Please use a valid name.");
					alert.initOwner(desktop.getMainScene().getStage());
					alert.showAndWait();
				}
/*
				// Note: do not use if (newName.trim().equals(null), will throw java.lang.NullPointerException
				if (newName == null || newName.trim() == "" || (newName.trim().length() == 0)) {
					//System.out.println("newName is " + newName);
					newName = askNameFX(oldName);

					if (newName == null || newName.trim() == "" || (newName.trim().length() == 0))
						return;
					else
						mapPanel.getSettlement().changeName(newName);
				}
				else {
					mapPanel.getSettlement().changeName(newName);
					//logger.info("New name is now " + newName);
				}
*/				
				
			});
				
			
			
			
			
			//desktop.closeToolWindow(SettlementWindow.NAME);
			//desktop.openToolWindow(SettlementWindow.NAME);
		}

		else {

			JDialog.setDefaultLookAndFeelDecorated(true);
			//String nameCache = settlement.getType();
			String settlementNewName = askNameDialog().trim();

			if ( settlementNewName.trim() == null || settlementNewName.trim().length() == 0)
				settlementNewName = askNameDialog();
			else {
				mapPanel.getSettlement().changeName(settlementNewName);
			}

			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.openToolWindow(SettlementWindow.NAME);

		}

	}

	 /**
	 * <p>Checks if a String is whitespace, empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 * @author commons.apache.org
	 */
	// 2015-10-19 Added isBlank()
	public static boolean isBlank(String str) {
	    int strLen;
	    if (str == null || (strLen = str.length()) == 0) {
	        return true;
	    }
	    for (int i = 0; i < strLen; i++) {
	        if ((Character.isWhitespace(str.charAt(i)) == false)) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * Ask for a new Settlement name
	 * @return pop up jDialog
	 */
	// 2014-10-26 Added askNameDialog()
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop,
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.input"), //$NON-NLS-1$
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.title"), //$NON-NLS-1$
			        JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Ask for a new building name using TextInputDialog in JavaFX/8
	 * @return new name
	 */
	public String askNameFX(String oldName) {
		String newName = null;
		TextInputDialog dialog = new TextInputDialog(oldName);
		dialog.initOwner(desktop.getMainScene().getStage());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle(Msg.getString("BuildingPanel.renameBuilding.dialogTitle"));
		dialog.setHeaderText(Msg.getString("BuildingPanel.renameBuilding.dialog.header"));
		dialog.setContentText(Msg.getString("BuildingPanel.renameBuilding.dialog.content"));

		Optional<String> result = dialog.showAndWait();
		//result.ifPresent(name -> {});

		if (result.isPresent()){
		    //logger.info("The settlement name has been changed to : " + result.get());
			newName = result.get();
		}
		
		return newName;
	}
	
	/**
	 * Inner class combo box model for settlements.
	 */
	public class SettlementComboBoxModel
	extends DefaultComboBoxModel<Object>
	implements
	UnitManagerListener,
	UnitListener {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor.
		 */
		public SettlementComboBoxModel() {
			// User DefaultComboBoxModel constructor.
			super();
			// Initialize settlement list.
			updateSettlements();
			// Add this as a unit manager listener.
			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.addUnitManagerListener(this);
			/*
			// 2014-12-19 Added addUnitListener
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().addUnitListener(this);
			}
			*/
		}

		/**
		 * Update the list of settlements.
		 */
		private void updateSettlements() {

			removeAllElements();
			UnitManager unitManager = Simulation.instance().getUnitManager();
			List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				addElement(i.next());
			}
		}

		@Override
		public void unitManagerUpdate(UnitManagerEvent event) {
			if (event.getUnit() instanceof Settlement) {
				updateSettlements();
			}
		}

		//2014-12-19 Added unitUpdate()
		public void unitUpdate(UnitEvent event) {
			// Note: Easily 100+ UnitEvent calls every second
			UnitEventType eventType = event.getType();
			if (eventType == UnitEventType.ADD_BUILDING_EVENT) {
				Object target = event.getTarget();
				Building building = (Building) target; // overwrite the dummy building object made by the constructor
				BuildingManager mgr = building.getBuildingManager();
				Settlement s = mgr.getSettlement();
				mapPanel.setSettlement(s);
				// Updated ComboBox
				settlementListBox.setSelectedItem(s);
				//this.pack();
			}
		}

		/**
		 * Prepare class for deletion.
		 */
		public void destroy() {

			removeAllElements();

			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(this);
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}

		}
	}

	/**
	 * Prepare class for deletion.
	 */
	public void destroy() {
		mapPanel = null;
		settlementCBModel.destroy();
		desktop = null;
		//settlementListBox.destroy();
		settlementListBox = null;
		settlementCBModel = null;
	}
}
