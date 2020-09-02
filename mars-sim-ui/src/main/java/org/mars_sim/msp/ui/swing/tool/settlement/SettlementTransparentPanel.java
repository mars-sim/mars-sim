/**
 * Mars Simulation Project
 * SettlementTransparentPanel.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
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
import javax.swing.SwingConstants;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
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
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.extended.WebComponent;
import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.managers.icon.IconManager;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.style.StyleId;


@SuppressWarnings("serial")
public class SettlementTransparentPanel extends WebComponent {

	/** Rotation change (radians per rotation button press). */
	private static final double ROTATION_CHANGE = Math.PI / 20D;
	/** Zoom change. */
	public static final double ZOOM_CHANGE = 0.25;

	private GameMode mode;
	
	private JLabel emptyLabel;

	private JSlider zoomSlider;
	private JPanel controlCenterPane, namePane, eastPane, labelPane, buttonPane, controlPane, settlementPanel;//, infoP, renameP ;
	private WebButton renameBtn, infoButton;
	//private JLabel zoomLabel;
	private JPopupMenu labelsMenu;
	/** Lists all settlements. */
	private WebComboBox settlementListBox;
	/** Combo box model. */
	private SettlementComboBoxModel settlementCBModel;

	private JCustomCheckBoxMenuItem buildingLabelMenuItem, personLabelMenuItem, constructionLabelMenuItem, vehicleLabelMenuItem, robotLabelMenuItem ;

	private SettlementMapPanel mapPanel;
	private MainDesktopPane desktop;

	private static UnitManager unitManager = Simulation.instance().getUnitManager();
		
    public SettlementTransparentPanel(MainDesktopPane desktop, SettlementMapPanel mapPanel) {
        this.mapPanel = mapPanel;
        this.desktop = desktop;

		if (GameManager.mode == GameMode.COMMAND) {
			mode = GameMode.COMMAND;
		}
		else
			mode = GameMode.SANDBOX;
		
		setDoubleBuffered(true);
    }

    public void paintComponent (Graphics g) { 
		((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.0f)); // draw transparent background
		super.paintComponent(g);
		((Graphics2D) g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f)); // turn on opacity
		g.setColor(Color.RED);
		g.fillRect(20, 20, 500, 300);
	} 
    
    public void createAndShowGUI() {

	    emptyLabel = new JLabel("  ") {
	    	@Override
	    	public Dimension getMinimumSize() {
	    		return new Dimension(50, 100);
	    	};
	    	@Override
	    	public Dimension getPreferredSize() {
	    		return new Dimension(50, 100);
	    	};
	    };

        buildInfoP();
        buildrenameBtn();
        buildLabelPane();
        buildButtonPane();
        buildSettlementNameComboBox();
        buildZoomSlider();

		namePane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2,2));
		namePane.setBackground(new Color(0,0,0,128));
        namePane.setOpaque(false);

		settlementPanel = new JPanel();//new BorderLayout());
		settlementPanel.setBackground(new Color(0,0,0,128));
		settlementPanel.setOpaque(false);
		settlementPanel.add(settlementListBox);//, BorderLayout.CENTER);
	    
//      	namePane.add(infoButton);
       	namePane.add(settlementPanel);
//       	namePane.add(renameBtn);

       	
	    mapPanel.add(namePane, BorderLayout.NORTH);

	    controlPane = new JPanel(new BorderLayout());//GridLayout(2,1,10,2));
	    controlPane.setBackground(new Color(0,0,0,128));//,0));
		controlPane.setOpaque(false);
       	
	    controlCenterPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
//	    controlCenterPane = new JPanel(new BoxLayout(controlPane, BoxLayout.Y_AXIS));
	    controlCenterPane.setBackground(new Color(0,0,0,128));//,0));
	    controlCenterPane.setOpaque(false);
//        controlCenterPane.setPreferredSize(new Dimension(40, 200));
//        controlCenterPane.setSize(new Dimension(40, 200));
//	    controlCenterPane.add(emptyLabel);
//	    controlCenterPane.add(emptyLabel);
	    controlCenterPane.add(zoomSlider);
	    controlCenterPane.setAlignmentY(CENTER_ALIGNMENT);

       	controlPane.add(buttonPane, BorderLayout.NORTH);
	    controlPane.add(labelPane, BorderLayout.SOUTH);
       	controlPane.add(controlCenterPane, BorderLayout.CENTER);
       	
	    eastPane = new JPanel(new BorderLayout());//GridLayout(3,1,10,2));
		eastPane.setBackground(new Color(0,0,0,15));
		eastPane.setBackground(new Color(0,0,0));//,0));
		eastPane.setOpaque(false);
        eastPane.add(emptyLabel, BorderLayout.EAST);
        eastPane.add(emptyLabel, BorderLayout.WEST);
        eastPane.add(emptyLabel, BorderLayout.NORTH);
        eastPane.add(emptyLabel, BorderLayout.SOUTH);
        eastPane.add(controlPane, BorderLayout.CENTER);

        mapPanel.add(eastPane, BorderLayout.EAST);
        // Make panel drag-able
//  	ComponentMover cmZoom = new ComponentMover(zoomPane);
		//cmZoom.registerComponent(rightPane);
//		cmZoom.registerComponent(zoomPane);
        mapPanel.setVisible(true);
    }

    /**
     * Gets the length of the most lengthy settlement name
     * 
     * @return
     */
    private int getNameLength() {
    	Collection<Settlement> list = unitManager.getSettlements();
    	int max = 12;
    	for (Settlement s: list) {
    		int size = s.getNickName().length();
    		if (max < size)
    			max = size;
    	}
    	return max;
    }

	@SuppressWarnings("unchecked")
	public void buildSettlementNameComboBox() {

		settlementCBModel = new SettlementComboBoxModel();
		settlementListBox = new WebComboBox(StyleId.comboboxHover, settlementCBModel);
		settlementListBox.setWidePopup(true);
//		settlementListBox.setAlignmentX(CENTER_ALIGNMENT);
//		((JLabel)settlementListBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		DefaultListCellRenderer listRenderer = new DefaultListCellRenderer();
	    listRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER); // center-aligned items
	    settlementListBox.setRenderer(listRenderer);
	      
		settlementListBox.setPreferredSize(getNameLength() * 12, 25);
		
		settlementListBox.setBackground(new Color(51,25,0,128)); // dull gold color
		settlementListBox.setOpaque(false);
		settlementListBox.setFont(new Font("Dialog", Font.BOLD, 16));
		settlementListBox.setForeground(Color.BLACK);
		settlementListBox.setToolTipText(Msg.getString("SettlementWindow.tooltip.selectSettlement")); //$NON-NLS-1$
		settlementListBox.setRenderer(new PromptComboBoxRenderer());
		settlementListBox.addItemListener(new ItemListener() {
			@Override
			// unitUpdate will update combobox when a new building is added
			public void itemStateChanged(ItemEvent event) {
				Settlement s = (Settlement) event.getItem();
				// Set the selected settlement in SettlementMapPanel
				mapPanel.setSettlement(s);
				// Set the population label in the status bar
				mapPanel.getSettlementWindow().setPop(s.getNumCitizens());
				// Set the box opaque
				settlementListBox.setOpaque(false);
			}
		});


		if (settlementListBox.getModel().getSize() > 0) {
			settlementListBox.setSelectedIndex(0);
			Settlement s = (Settlement) settlementListBox.getSelectedItem();
			// Set the selected settlement in SettlementMapPanel
			mapPanel.setSettlement(s);
			// Set the population label in the status bar
			mapPanel.getSettlementWindow().setPop(s.getNumCitizens());
			// Set the box opaque
			settlementListBox.setOpaque(false);
		}

	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;
		//public boolean isOptimizedDrawingEnabled();
		//private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		public PromptComboBoxRenderer(){
			//defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		    //settlementListBox.setRenderer(defaultRenderer);
		    //setOpaque(false);
//		    setHorizontalAlignment(LEFT);
//		    setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
			}

			public Component getListCellRendererComponent(JList<?> list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
//		        JComponent result = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				if (value == null) {
					setText(prompt);
					//this.setForeground(Color.green);
			        //this.setBackground(new Color(184,134,11));
					return this;
				}

				if (isSelected) {
		        	  c.setForeground(Color.black);
		        	  c.setBackground(new Color(255,229,204,50)); // pale orange
		          } else {
						c.setForeground(Color.black);
				        c.setBackground(new Color(184,134,11,50)); // mud orange
		          }

		        //result.setOpaque(false);

		        return c;
		    }
	}


//    public void buildZoomLabel() {
//
//		zoomLabel = new JLabel(Msg.getString("SettlementTransparentPanel.label.zoom")); //$NON-NLS-1$
//		//zoomLabel.setPreferredSize(new Dimension(60, 20));
//		zoomLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
//		zoomLabel.setForeground(Color.GREEN);
//		//zoomLabel.setContentAreaFilled(false);
//		zoomLabel.setOpaque(false);
//		zoomLabel.setVerticalAlignment(JLabel.CENTER);
//		zoomLabel.setHorizontalAlignment(JLabel.CENTER);
//		//zoomLabel.setBorder(new LineBorder(Color.green, 1, true));
//		//zoomLabel.setBorderPainted(true);
//		zoomLabel.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.zoom")); //$NON-NLS-1$
//
//    }


    public void buildZoomSlider() {

        UIDefaults sliderDefaults = new UIDefaults();

        sliderDefaults.put("Slider.thumbWidth", 15);
        sliderDefaults.put("Slider.thumbHeight", 15);
        sliderDefaults.put("Slider:SliderThumb.backgroundPainter", new Painter<JComponent>() {
            public void paint(Graphics2D g, JComponent c, int w, int h) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setStroke(new BasicStroke(2f));
                g.setColor(Color.ORANGE.darker().darker());
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
                g.setColor(Color.ORANGE.darker().darker());
                g.fillRoundRect(0, 6, w, 6, 6, 6); // g.fillRoundRect(0, 6, w-1, 6, 6, 6);
                g.setColor(Color.WHITE);
                g.drawRoundRect(0, 6, w, 6, 6, 6);
            }
        });

        zoomSlider = new JSlider(JSlider.VERTICAL, -20, 30, 0);
        zoomSlider.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 100));
        zoomSlider.setPreferredSize(new Dimension(30, 300));
        zoomSlider.setSize(new Dimension(30, 300));
//        zoomSlider.putClientProperty("Nimbus.Overrides",sliderDefaults);
//        zoomSlider.putClientProperty("Nimbus.Overrides.InheritDefaults",false);

    	//zoomSlider = new JSliderMW(JSlider.VERTICAL, -10, 10, 0);
		zoomSlider.setMajorTickSpacing(10);
		zoomSlider.setMinorTickSpacing(5);
		zoomSlider.setPaintTicks(true);
		zoomSlider.setPaintLabels(true);
		zoomSlider.setForeground(Color.ORANGE.darker().darker());
//		zoomSlider.setBackground(new Color(0,0,0,15));
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
//				System.out.println("newScale : " + newScale);
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

//		infoP = new JPanel(new FlowLayout());
//		infoP.setBackground(new Color(0,0,0,128));
////		infoP.setBackground(new Color(51,25,0,128)); // dark brown
//		infoP.setOpaque(false);
//		infoP.setAlignmentX(FlowLayout.CENTER);
		
//		infoButton = new JButton(Msg.getString("SettlementTransparentPanel.button.info")); //$NON-NLS-1$
//		infoButton.setPreferredSize(new Dimension(70, 25)); //35, 20));
//		infoButton.setFont(new Font("Dialog", Font.ITALIC, 13));
//		infoButton.setForeground(Color.ORANGE.darker().darker());
//		infoButton.setContentAreaFilled(false);
//		infoButton.setOpaque(false); // text disappeared if setOpaque(false)
//		infoButton.setBorder(new LineBorder(Color.ORANGE.darker().darker(), 1, true));
//		infoButton.setBorderPainted(true);
//		infoButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.info")); //$NON-NLS-1$
//		infoButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				Settlement settlement = mapPanel.getSettlement();
//				if (settlement != null) {
//					desktop.openUnitWindow(settlement, false);
//				}
//			}
//		});
		
    	ImageIcon icon = IconManager.getIcon ("info");//new LazyIcon("info").getIcon();
    	infoButton = new WebButton(StyleId.buttonUndecorated, icon);
    	
//		infoButton = new ToolButton(Msg.getString("SettlementTransparentPanel.button.info"),
//				Msg.getString("icon.info")); //$NON-NLS-1$ //$NON-NLS-2$
		infoButton.setPreferredSize(new Dimension(32, 32));
//		infoButton.setSize(32, 32);
//		infoButton.setMaximumSize(new Dimension(32, 32));
		
		infoButton.setOpaque(false);
		infoButton.setBackground(new Color(0,0,0,128));
		infoButton.setContentAreaFilled(false);
		infoButton.setBorderPainted(false);
		infoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settlement settlement = mapPanel.getSettlement();
				if (settlement != null) {
					desktop.openUnitWindow(settlement, false);
				}
			};
		});
		
//		infoP.add(infoButton);
    }

    public void buildrenameBtn() {

//		renameP  = new JPanel(new FlowLayout());
//		renameP.setBackground(new Color(0,0,0,128));
////		renameP.setBackground(new Color(51,25,0,128));
//		renameP.setOpaque(false);
//		renameP.setAlignmentX(FlowLayout.CENTER);

//		renameBtn = new JButton(Msg.getString("SettlementTransparentPanel.button.rename")); //$NON-NLS-1$
//		renameBtn.setPreferredSize(new Dimension(70, 25)); //
//		renameBtn.setFont(new Font("Dialog", Font.ITALIC, 13));
//		renameBtn.setForeground(Color.ORANGE.darker().darker());
//		renameBtn.setContentAreaFilled(false);
//		renameBtn.setOpaque(false); // text disappeared if setOpaque(false)
//		renameBtn.setBorder(new LineBorder(Color.ORANGE.darker().darker(), 1, true));
//		renameBtn.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.rename")); //$NON-NLS-1$
//		renameBtn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				openRenameDialog();
//			}
//		});
    	
    	ImageIcon icon = new LazyIcon("edit").getIcon();
    	renameBtn = new WebButton(StyleId.buttonUndecorated, icon);
    	
//		renameBtn = new ToolButton(Msg.getString("SettlementTransparentPanel.button.rename"),
//				Msg.getString("icon.rename")); //$NON-NLS-1$ //$NON-NLS-2$
    	renameBtn.setPreferredSize(new Dimension(32, 32));
//		renameBtn.setSize(30, 30);
//		renameBtn.setMaximumSize(new Dimension(30, 30));
    	
		renameBtn.setOpaque(false);
		renameBtn.setBackground(new Color(0,0,0,128));
		renameBtn.setContentAreaFilled(false);
		renameBtn.setBorderPainted(false);
		
		renameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openRenameDialog();
			};
		});
//		renameP.add(renameBtn);
    }

//    private BufferedImage getModifiedImage(String n) {
//    	BufferedImage originalImage = null;
//    	BufferedImage modifiedImage = null;
//        originalImage = (BufferedImage) (ImageLoader.getIcon(n, "png", "/icons/map/").getImage()); //$NON-NLS-1$
//		 modifiedImage = new BufferedImage(
//		    originalImage.getWidth(),
//		    originalImage.getHeight(),
//		    BufferedImage.TYPE_INT_ARGB);
//
//        Graphics2D g2 = modifiedImage.createGraphics();
//        AlphaComposite newComposite = 
//            AlphaComposite.getInstance(
//                AlphaComposite.SRC_OVER, 0.5f);
//        g2.setComposite(newComposite);      
//        g2.drawImage(originalImage, 0, 0, null);
//        g2.dispose();
//        
//        return modifiedImage;
//    }
    
    public void buildButtonPane() {

        buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPane.setPreferredSize(new Dimension(150, 36));
        buttonPane.setBackground(new Color(0,0,0,128));
        buttonPane.setOpaque(false);
		    
		// Create rotate clockwise button.
        final ImageIcon cwIcon = new LazyIcon("right").getIcon();
        WebButton cwButton = new WebButton(StyleId.buttonUndecorated, cwIcon);
        cwButton.setPreferredSize(new Dimension(32, 32));
        cwButton.setOpaque(false);
		cwButton.setBorderPainted(false);
		cwButton.setContentAreaFilled(false);
		cwButton.setBackground(new Color(0,0,0,128));

		cwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.clockwise")); //$NON-NLS-1$
		cwButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() + ROTATION_CHANGE);
			}
		});
		
		// Create center button.
        final ImageIcon centerIcon = new LazyIcon("center").getIcon();
		WebButton recenterButton = new WebButton(StyleId.buttonUndecorated, centerIcon);
		recenterButton.setPreferredSize(new Dimension(32, 32));
		recenterButton.setOpaque(false);
		recenterButton.setBorderPainted(false);
		recenterButton.setContentAreaFilled(false);
		recenterButton.setBackground(new Color(0,0,0,128));
		
		recenterButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.recenter")); //$NON-NLS-1$
		recenterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.reCenter();
				zoomSlider.setValue(0);
			}
		});

		// Create rotate counter-clockwise button.
        final ImageIcon ccwIcon = new LazyIcon("left").getIcon();
        WebButton ccwButton = new WebButton(StyleId.buttonUndecorated, ccwIcon);
        ccwButton.setPreferredSize(new Dimension(32, 32));
		ccwButton.setOpaque(false);
		ccwButton.setBorderPainted(false);
		ccwButton.setContentAreaFilled(false);
		ccwButton.setBackground(new Color(0,0,0,128));
		
		ccwButton.setToolTipText(Msg.getString("SettlementTransparentPanel.tooltip.counterClockwise")); //$NON-NLS-1$
		ccwButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mapPanel.setRotation(mapPanel.getRotation() - ROTATION_CHANGE);
			}
		});

		buttonPane.add(ccwButton);
		buttonPane.add(recenterButton);
		buttonPane.add(cwButton);
		
		// Need to clean up these icons
//				img.clockwise          = /icons/map/Clockwise.png
//				img.counterClockwise   = /icons/map/CounterClockwise.png
//				img.centerMap          = /icons/map/CenterMap.png
//				img.cw					= /icons/map/CW.png
//				img.ccw					= /icons/map/CCW.png
//				img.recenter			= /icons/map/recenter.png
//				img.cw_yellow			= /icons/map/CW_yellow.png
//				img.ccw_yellow			= /icons/map/CCW_yellow.png
//				img.recenter_yellow		= /icons/map/recenter_yellow.png
				
		buttonPane.add(emptyLabel);
    }

    public void buildLabelPane() {
        labelPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        labelPane.setPreferredSize(new Dimension(150, 36));
        labelPane.setBackground(new Color(0,0,0,128));
		labelPane.setOpaque(false);
		
	    final ImageIcon labelsIcon = new LazyIcon("stack").getIcon();
	    WebButton labelsButton = new WebButton(StyleId.buttonUndecorated, 
	    		Msg.getString("SettlementTransparentPanel.button.labels"), labelsIcon);  //$NON-NLS-1$
	        
//		WebButton labelsButton = new WebButton(Msg.getString("SettlementTransparentPanel.button.labels")); //$NON-NLS-1$
//		labelsButton.setBorder(new LineBorder(Color.ORANGE.darker().darker(), 2, true));

		//labelsButton.setBackground(new Color(139,69,19)); // (139,69,19) is brown
		//labelsButton.setBackground(new Color(139,69,19,40));
		//labelsButton.setBackground(new Color(51,25,0,5)); // dull gold color
//		labelsButton.setBackground(new Color(0,0,0));//,0));
		
		labelsButton.setFont(new Font("Dialog", Font.BOLD, 13));
		labelsButton.setForeground(Color.ORANGE.darker().darker());		
		labelsButton.setPreferredSize(new Dimension(32, 32));
		labelsButton.setVerticalAlignment(JLabel.CENTER);
		labelsButton.setHorizontalAlignment(JLabel.CENTER);
		
		labelsButton.setOpaque(false);
		labelsButton.setBackground(new Color(0,0,0,128));
		labelsButton.setContentAreaFilled(false); //more artifact when enabled
		labelsButton.setBorderPainted(false);

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

		labelPane.add(renameBtn);
		labelPane.add(infoButton);
		labelPane.add(labelsButton);

		labelPane.add(emptyLabel);
	}
    
//    class MyCellRenderer extends JLabel implements ListCellRenderer<Object>  {
//		private static final long serialVersionUID = 1L;
//
//		public MyCellRenderer() {
//	          setOpaque(true);
//	      }
//	      public Component getListCellRendererComponent(JList<?> list,
//	                                                    Object value,
//	                                                    int index,
//	                                                    boolean isSelected,
//	                                                    boolean cellHasFocus) {
//
//	          setText(value.toString());
//	  		  setBackground(new Color(0,0,0,0));
//
//	          Color background = Color.orange;
//	          Color foreground = Color.green;
//
//	          // check if this cell represents the current DnD drop location
//	          JList.DropLocation dropLocation = list.getDropLocation();
//
//	          if (dropLocation != null
//	                  && !dropLocation.isInsert()
//	                  && dropLocation.getIndex() == index) {
//
//	          // check if this cell is selected
//	          } else if (isSelected) {
//	              background = Color.orange;
//	              foreground = Color.green;
//
//	          // unselected
//	          } else {
//	          };
//
//	          setBackground(background);
//	          setForeground(foreground);
//
//	          return this;
//	      }
//    }

	/**
	 * Create the labels popup menu.
	 * @return popup menu.
	 */
	public JPopupMenu createLabelsMenu() {
		JPopupMenu popMenu = new JPopupMenu(Msg.getString("SettlementWindow.menu.labelOptions")); //$NON-NLS-1$
		popMenu.setOpaque(false);
		popMenu.setBackground(new Color(0,0,0,128));
//		result.setContentAreaFilled(false);
		popMenu.setBorderPainted(false);
//		result.setOpaque(false);
//		result.setBorder(BorderFactory.createLineBorder(new Color(139,69,19)));// dark brown
//		result.setBackground(new Color(222,184,135,128)); // pale silky brown
//        UIResource res = new BorderUIResource.LineBorderUIResource(new Color(139,69,19));
//        UIManager.put("PopupMenu.border", res);
//        result.setLightWeightPopupEnabled(false);

		// Create Day Night Layer menu item.
		JCustomCheckBoxMenuItem dayNightLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.daylightTracking"), mapPanel.isDaylightTrackingOn()); //$NON-NLS-1$
		dayNightLabelMenuItem.setForeground(new Color(139,69,19));
		dayNightLabelMenuItem.setContentAreaFilled(false);
		dayNightLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowDayNightLayer(!mapPanel.isDaylightTrackingOn());
			}
		});
		popMenu.add(dayNightLabelMenuItem);

		// Create building label menu item.
		buildingLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.buildings"), mapPanel.isShowBuildingLabels()); //$NON-NLS-1$
		// Add setting setForeground setContentAreaFilled setOpaque
		buildingLabelMenuItem.setForeground(new Color(139,69,19));
		//buildingLabelMenuItem.setBackground(new Color(222,184,135,0));
		buildingLabelMenuItem.setContentAreaFilled(false);
		//buildingLabelMenuItem.setOpaque(false);
		buildingLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowBuildingLabels(!mapPanel.isShowBuildingLabels());
			}
		});
		popMenu.add(buildingLabelMenuItem);

		// Create construction/salvage label menu item.
		constructionLabelMenuItem = new JCustomCheckBoxMenuItem(
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
		popMenu.add(constructionLabelMenuItem);

		// Create vehicle label menu item.
		vehicleLabelMenuItem = new JCustomCheckBoxMenuItem(
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
		popMenu.add(vehicleLabelMenuItem);

		// Create person label menu item.
		personLabelMenuItem = new JCustomCheckBoxMenuItem(
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
		popMenu.add(personLabelMenuItem);

		// Create person label menu item.
		robotLabelMenuItem = new JCustomCheckBoxMenuItem(
				Msg.getString("SettlementWindow.menu.robots"), mapPanel.isShowRobotLabels()); //$NON-NLS-1$
		robotLabelMenuItem.setForeground(new Color(139,69,19));
		robotLabelMenuItem.setContentAreaFilled(false);
		robotLabelMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mapPanel.setShowRobotLabels(!mapPanel.isShowRobotLabels());
			}
		});
		popMenu.add(robotLabelMenuItem);

		popMenu.pack();

		return popMenu;
	}


	public class JCustomCheckBoxMenuItem extends JCheckBoxMenuItem {

		public JCustomCheckBoxMenuItem(String s, boolean b) {
			super(s, b);
		}

//		public void paint(Graphics g) {
//			//protected void paintComponent(Graphics g) {
//				//super.paintComponent(g);
//
//                Graphics2D g2d = (Graphics2D) g.create();
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//                super.paint(g2d);
//                g2d.dispose();
//      }
	}


	/**
	 * Open dialog box to take in the new settlement name
	 * 
	 */
	public void openRenameDialog() {

		String oldName = mapPanel.getSettlement().getName();

		JDialog.setDefaultLookAndFeelDecorated(true);
		String newName = askNameDialog();
		if (!oldName.equals(newName) 
				&& newName != null 
				&& newName.trim() != "" 
				&& newName.trim().length() != 0) {
			mapPanel.getSettlement().changeName(newName.trim());

			desktop.closeToolWindow(SettlementWindow.NAME);
			desktop.openToolWindow(SettlementWindow.NAME);

		} else {
			return;
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
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop,
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.input"), //$NON-NLS-1$
					Msg.getString("SettlementWindow.JDialog.changeSettlementName.title"), //$NON-NLS-1$
			        JOptionPane.QUESTION_MESSAGE);
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
//			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.addUnitManagerListener(this);

//			// Add addUnitListener
//			Collection<Settlement> settlements = unitManager.getSettlements();
//			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
//			Iterator<Settlement> i = settlementList.iterator();
//			while (i.hasNext()) {
//				i.next().addUnitListener(this);
//			}

		}

		/**
		 * Update the list of settlements.
		 */
		private void updateSettlements() {
			// Clear all elements
			removeAllElements();

			List<Settlement> settlements = new ArrayList<Settlement>();
			
			// Add the command dashboard button
			if (mode == GameMode.COMMAND) {
				settlements = unitManager.getCommanderSettlements();
			}
			
			else if (mode == GameMode.SANDBOX) {
				settlements.addAll(unitManager.getSettlements());
			}
			
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

//			UnitManager unitManager = Simulation.instance().getUnitManager();
			unitManager.removeUnitManagerListener(this);
			Collection<Settlement> settlements = unitManager.getSettlements();
			List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
			Iterator<Settlement> i = settlementList.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}

		}
	}

//	public JComboBoxMW<?> getSettlementListBox() {
//		return settlementListBox;
//	}

	public WebComboBox getSettlementListBox() {
		return settlementListBox;
	}
	
//	public JCustomCheckBoxMenuItem getBuildingLabelMenuItem() {
//		return buildingLabelMenuItem;
//	}
//
//	public JCustomCheckBoxMenuItem getPersonLabelMenuItem () {
//		return personLabelMenuItem ;
//	}
//
//	public JCustomCheckBoxMenuItem getConstructionLabelMenuItem () {
//		return constructionLabelMenuItem ;
//	}
//
//	public JCustomCheckBoxMenuItem getVehicleLabelMenuItem () {
//		return vehicleLabelMenuItem ;
//	}
//
//	public JCustomCheckBoxMenuItem getRobotLabelMenuItem () {
//		return robotLabelMenuItem ;
//	}

	/**
	 * Prepare class for deletion.
	 */
	public void destroy() {
		mapPanel = null;
		settlementCBModel.destroy();
		desktop = null;
		settlementListBox = null;
		settlementCBModel = null;
	}


	@Override
	public StyleId getDefaultStyleId() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getUIClassID() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void updateUI() {
		// TODO Auto-generated method stub
		
	}
}
