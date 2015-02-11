/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 3.07 2015-01-14

 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ColorTintFilter;
import org.mars_sim.msp.ui.swing.tool.DropShadowBorder;
import org.mars_sim.msp.ui.swing.tool.GraphicsUtilities;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** Main window. */
	protected MainDesktopPane desktop;
	/** Unit for this window. */
	protected Unit unit;
	protected JPanel namePanel;
	/** The tab panels. */
	private Collection<TabPanel> tabPanels;
	/** The center panel. */
	private JTabbedPane centerPanel;

    private static final int BLUR_SIZE = 7;
    private BufferedImage image;
    private float alpha = 0.0f;

    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit for this window.
     * @param displayDescription true if unit description is to be displayed.
     */
    public UnitWindow(MainDesktopPane desktop, Unit unit, boolean displayDescription) {
        
        // Use JInternalFrame constructor
        super(unit.getName(), true, true, false, true);

        // Initialize data members
        this.desktop = desktop;
        this.unit = unit;
        tabPanels = new ArrayList<TabPanel>();

        
        // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        // mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);
        //getContentPane().setBackground(THEME_COLOR);

        
        // Create name panel
        namePanel = new JPanel(new BorderLayout(0, 0));
        //namePanel.setBackground(THEME_COLOR);
        //namePanel.setBorder(new MarsPanelBorder());
        mainPane.add(namePanel, BorderLayout.NORTH);
        //mainPane.setBackground(THEME_COLOR);

        // Create name label
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        JLabel nameLabel = new JLabel(" " + unit.getName() + " ", displayInfo.getButtonIcon(), SwingConstants.CENTER);
        nameLabel.setOpaque(true);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        nameLabel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        namePanel.setBorder(new MarsPanelBorder());
        //namePanel.add(nameLabel, BorderLayout.EAST);
        //namePanel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        namePanel.add(nameLabel, BorderLayout.WEST);
        
        // Create description label if necessary.
        if (displayDescription) {
            JLabel descriptionLabel = new JLabel(unit.getDescription(), JLabel.CENTER);
            namePanel.add(descriptionLabel, BorderLayout.SOUTH);
        }
        
        // Create center panel
        centerPanel = new JTabbedPane();
        mainPane.add(namePanel, BorderLayout.NORTH);
        //centerPanel.setBackground(THEME_COLOR);
        mainPane.add(centerPanel, BorderLayout.CENTER);
        // add focusListener to play sounds and alert users of critical conditions.
       
        //TODO: disabled in SVN while in development
        //this.addInternalFrameListener(new UniversalUnitWindowListener(UnitInspector.getGlobalInstance()));
	
        //setStyle();
        
  		this.setBorder(new DropShadowBorder(Color.BLACK, 0, 11, .2f, 16,
  	  		    false, true, true, true));
    }
    
    /*
	public void setStyle() {
        Container contentPane = desktop.getRootPane();
          
          image = GraphicsUtilities.createCompatibleTranslucentImage(contentPane.getWidth() +
                  2 * (int) BLUR_SIZE,
                  contentPane.getHeight() +
                  2 * (int) BLUR_SIZE);
  		Graphics2D g2 = image.createGraphics();
  		g2.translate(BLUR_SIZE, BLUR_SIZE);
  		contentPane.paint(g2);
  		g2.translate(-BLUR_SIZE, -BLUR_SIZE);
  		g2.dispose();
  		
  		// 1.5 second vs 0.3 second
  		//long start = System.currentTimeMillis();
  		image = changeImageWidth(image, image.getWidth() / 2);
  		ConvolveOp gaussianFilter = getGaussianBlurFilter(BLUR_SIZE, true);
  		image = gaussianFilter.filter(image, null);
  		gaussianFilter = getGaussianBlurFilter(BLUR_SIZE, false);
  		image = gaussianFilter.filter(image, null);
  		ColorTintFilter colorMixFilter = new ColorTintFilter(Color.ORANGE, 0.1f);
  		image = colorMixFilter.filter(image, null);
  		image = changeImageWidth(image, image.getWidth() * 2);
  		//System.out.println("time = " +
  		//((System.currentTimeMillis() - start) / 1000.0f));
    }
    
    */
    /**
     * Adds a tab panel to the center panel.
     *
     * @param panel the tab panel to add.
     */
    protected final void addTabPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            centerPanel.addTab(panel.getTabTitle(), panel.getTabIcon(), 
                panel, panel.getTabToolTip());
        }
    }

    protected final void addTopPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            namePanel.add(panel,BorderLayout.CENTER);
        }
    }
     
    /**
     * Gets the unit for this window.
     *
     * @return unit 
     */
    public Unit getUnit() {
        return unit;
    }
    
    /**
     * Updates this window.
     */
    public void update() {
        // Update each of the tab panels.
        for (TabPanel tabPanel : tabPanels) {
        	tabPanel.update();
        }
    }
    
    

  /*
    public static BufferedImage changeImageWidth(BufferedImage image, int width) {
	        float ratio = (float) image.getWidth() / (float) image.getHeight();
	        int height = (int) (width / ratio);
	        
	        BufferedImage temp = new BufferedImage(width, height,
	                image.getType());
	        Graphics2D g2 = temp.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
	        g2.dispose();

	        return temp;
	}
	    
	public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
	        if (radius < 1) {
	            throw new IllegalArgumentException("Radius must be >= 1");
	        }
	        
	        int size = radius * 2 + 1;
	        float[] data = new float[size];
	        
	        float sigma = radius / 3.0f;
	        float twoSigmaSquare = 2.0f * sigma * sigma;
	        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
	        float total = 0.0f;
	        
	        for (int i = -radius; i <= radius; i++) {
	            float distance = i * i;
	            int index = i + radius;
	            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
	            total += data[index];
	        }
	        
	        for (int i = 0; i < data.length; i++) {
	            data[i] /= total;
	        }        
	        
	        Kernel kernel = null;
	        if (horizontal) {
	            kernel = new Kernel(size, 1, data);
	        } else {
	            kernel = new Kernel(1, size, data);
	        }
	        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}
	
	
	 @Override
	 public boolean isOpaque() {
	     return false;
	 }
	 
	*/
    
	 @Override
	 protected void paintComponent(Graphics g) {
	     setupGraphics((Graphics2D) g);
	
	     Point location = getLocation();
	     location.x = (int) (-location.x - BLUR_SIZE);
	     location.y = (int) (-location.y - BLUR_SIZE);
	
	     Insets insets = getInsets();
	     Shape oldClip = g.getClip();
	     g.setClip(insets.left, insets.top,
	               getWidth() - insets.left - insets.right,
	               getHeight() - insets.top - insets.bottom);
	     g.drawImage(image, location.x, location.y, null);
	     g.setClip(oldClip);
	 }
	
	 /*
	 public float getAlpha() {
	     return alpha;
	 }
	
	 public void setAlpha(float alpha) {
	     this.alpha = alpha;
	     repaint();
	 }
	
	*/
	 private static void setupGraphics(Graphics2D g2) {
	     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                         RenderingHints.VALUE_ANTIALIAS_ON);
	    
	     Toolkit tk = Toolkit.getDefaultToolkit();
	     Map desktopHints = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
	     if (desktopHints != null) {
	         g2.addRenderingHints(desktopHints);
	     }
	 }
	    
	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {}
	     
}