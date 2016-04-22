/**
 * Mars Simulation Project
 * UnitWindow.java
 * @version 3.08 2015-06-26
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.RowNumberTable;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.swing.JideTabbedPane;

/**
 * The UnitWindow is the base window for displaying units.
 */
public abstract class UnitWindow extends JInternalFrame {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
    //private static final int BLUR_SIZE = 7;
	// Data members
	protected JPanel namePanel;
	/** The tab panels. */
	private Collection<TabPanel> tabPanels;
	/** The center panel. */
	//private JTabbedPane centerPanel;
	// 2015-06-20 Replaced with JideTabbedPane
	private JideTabbedPane centerPanel;

	//private BufferedImage image;

	private static final String TOWN = Msg.getString("icon.town");
	private static final String JOB = Msg.getString("icon.job");
	private static final String ROLE = Msg.getString("icon.role");
	private static final String SHIFT = Msg.getString("icon.shift");
	
	
	private JLabel townLabel;
    private JLabel jobLabel;
    private JLabel roleLabel;
    private JLabel shiftLabel; 
	
	/** Main window. */
	protected MainDesktopPane desktop;
	/** Unit for this window. */
	protected Unit unit;

    /**
     * Constructor
     *
     * @param desktop the main desktop panel.
     * @param unit the unit for this window.
     * @param hasDescription true if unit description is to be displayed.
     */
    public UnitWindow(MainDesktopPane desktop, Unit unit, boolean hasDescription) {
        // Use JInternalFrame constructor
        super(unit.getName(), false, true, false, true);

        // Initialize data members
        this.desktop = desktop;
        this.unit = unit;
        
	    this.setMaximumSize(new Dimension(460, 580));
	    this.setPreferredSize(new Dimension(460, 580));
	    
        // Causes titlePane to fill with light pale orange (or else it is rendered transparent by paintComponent)
        BasicInternalFrameTitlePane titlePane = (BasicInternalFrameTitlePane) ((BasicInternalFrameUI) this.getUI()).getNorthPane();
        titlePane.setOpaque(true);
        titlePane.setBackground(new Color(250, 213, 174)); // light pale orange


        tabPanels = new ArrayList<TabPanel>();

        // Create main panel
        JPanel mainPane = new JPanel(new BorderLayout());
        // mainPane.setBorder(MainDesktopPane.newEmptyBorder());
        setContentPane(mainPane);
        //getContentPane().setBackground(THEME_COLOR);

        // Create name panel
        //namePanel = new JPanel(new BorderLayout(0, 0));
        namePanel = new JPanel(new FlowLayout());//FlowLayout.LEFT));
        
        //namePanel.setBackground(THEME_COLOR);
        //namePanel.setBorder(new MarsPanelBorder());
        mainPane.add(namePanel, BorderLayout.NORTH);
        //mainPane.setBackground(THEME_COLOR);

        // Create name label
        UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
        JLabel nameLabel = new JLabel(" " + Conversion.capitalize(unit.getName()) + " ", displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
        nameLabel.setOpaque(true);
        nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
        nameLabel.setHorizontalTextPosition(JLabel.CENTER);
        //nameLabel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        //nameLabel.setBorder(new MarsPanelBorder());
        namePanel.setBorder(new MarsPanelBorder());
        //namePanel.add(nameLabel, BorderLayout.EAST);
        //namePanel.setBorder(new EmptyBorder(5, 5, 5, 5) );
        //namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(nameLabel);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel empty = new JLabel("    ");
        namePanel.add(empty);
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create description label if necessary.
        if (hasDescription) {
            if (unit instanceof Person) {
            	     	
            	JLabel townIconLabel = new JLabel();
            	townIconLabel.setToolTipText("Home Town or Associated Settlement");
            	setImage(TOWN, townIconLabel);
            	 
            	JLabel jobIconLabel = new JLabel();
            	jobIconLabel.setToolTipText("Job");
            	setImage(JOB, jobIconLabel);
            	
            	JLabel roleIconLabel = new JLabel();
            	roleIconLabel.setToolTipText("Role");
            	setImage(ROLE, roleIconLabel);
            	
            	JLabel shiftIconLabel = new JLabel();
            	shiftIconLabel.setToolTipText("Work Shift");
            	setImage(SHIFT, shiftIconLabel);
            	
            	//createImageIcon("/icons/city_32.png", null));
            	//SwingUtilities.invokeLater(new Runnable() { 
            	//	   public void run() {
            		      //JLabel myLabel = new JLabel("Old Text");
            	//		   iconLabel.setIcon(new ImageIcon("image.png"));
            	//	   }
            	//});          	
            	
            	JPanel townPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            	JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            	JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));      
            	JPanel shiftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));      
    
            	Person p = (Person) unit;
               	
            	String text = Conversion.capitalize(unit.getDescription());
             	townLabel = new JLabel(text);// , JLabel.CENTER);
                
                String jobString = p.getMind().getJob().getName(p.getGender());
                jobLabel = new JLabel(jobString);// , JLabel.CENTER);
                
                String roleString = p.getRole().getType().getName();
                roleLabel = new JLabel(roleString);// , JLabel.CENTER);
                
                String shiftString = p.getTaskSchedule().getShiftType().getName();
                shiftLabel = new JLabel(shiftString);// , JLabel.CENTER);
            	
                //setAlignment(int align) 
                
                townPanel.add(townIconLabel);
                townPanel.add(townLabel);
                townPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                jobPanel.add(jobIconLabel);
                jobPanel.add(jobLabel);
                jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                rolePanel.add(roleIconLabel);
                rolePanel.add(roleLabel);
                rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                shiftPanel.add(shiftIconLabel);
                shiftPanel.add(shiftLabel);
                shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
            	JPanel rowPanel = new JPanel(new GridLayout(2,2,0,0));
            	rowPanel.setBorder(new MarsPanelBorder());
            	
            	rowPanel.add(townPanel);//, FlowLayout.LEFT);
            	rowPanel.add(rolePanel);//, FlowLayout.LEFT);
            	rowPanel.add(shiftPanel);//, FlowLayout.LEFT);
            	rowPanel.add(jobPanel);//, FlowLayout.LEFT);
            	
                namePanel.add(rowPanel);
                rowPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                
                
            	statueUpdate();
            }
        }

        // Create center panel
        centerPanel = new JideTabbedPane();
        LookAndFeelFactory.installJideExtension(LookAndFeelFactory.OFFICE2003_STYLE);
        centerPanel.setBoldActiveTab(true);
        centerPanel.setScrollSelectedTabOnWheel(true);
        centerPanel.setTabShape(JideTabbedPane.SHAPE_WINDOWS_SELECTED);
        centerPanel.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003); //COLOR_THEME_VSNET);

        // Setting foreground color for tab text.
        centerPanel.setForeground(Color.DARK_GRAY);

        //centerPanel.setTabColorProvider(JideTabbedPane.ONENOTE_COLOR_PROVIDER);
        //centerPanel.setBackground(UIDefaultsLookup.getColor("control"));
        centerPanel.setTabPlacement(JideTabbedPane.LEFT);

        //centerPanel.setBackground(THEME_COLOR);
        mainPane.add(centerPanel, BorderLayout.CENTER);
        // add focusListener to play sounds and alert users of critical conditions.

        //TODO: disabled in SVN while in development
        //this.addInternalFrameListener(new UniversalUnitWindowListener(UnitInspector.getGlobalInstance()));

        //setStyle();
  		//setBorder(new DropShadowBorder(Color.BLACK, 0, 11, .2f, 16,false, true, true, true));
    }

	/**
	 * Sets weather image.
	 */
	public void setImage(String imageLocation, JLabel label) {
        URL resource = ImageLoader.class.getResource(imageLocation);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(resource);
        ImageIcon imageIcon = new ImageIcon(img);
    	label.setIcon(imageIcon);
	}
	
    private ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
        	if (description != null)
        		return new ImageIcon(imgURL, description);
        	else 
        		return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public void statueUpdate() {   
        	
    	Person p = (Person) unit;
   	
    	String text = Conversion.capitalize(unit.getDescription());
    	//System.out.println("Description is : " + text);
    	townLabel.setText(text);// , JLabel.CENTER);
        
        String jobString = p.getMind().getJob().getName(p.getGender());
        jobLabel.setText(jobString);// , JLabel.CENTER);
        
        String roleString = p.getRole().getType().getName();
        roleLabel.setText(roleString);// , JLabel.CENTER);
        
        String shiftString = p.getTaskSchedule().getShiftType().getName();
        shiftLabel.setText(shiftString);// , JLabel.CENTER);
   

    }
    
    /**
     * Adds a tab panel to the center panel.
     *
     * @param panel the tab panel to add.
     */
    protected final void addTabPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            //centerPanel.addTab(panel.getTabTitle(), panel.getTabIcon(),
            //    panel, panel.getTabToolTip());
        }
      }

    protected final void addTopPanel(TabPanel panel) {
        if (!tabPanels.contains(panel)) {
            tabPanels.add(panel);
            namePanel.add(panel,BorderLayout.CENTER);
        }
    }

    // 2015-06-20 Added tab sorting
    protected void sortTabPanels() {
        tabPanels.stream().sorted(
        		(t1, t2) -> t2.getTabTitle().compareTo(t1.getTabTitle()));
        tabPanels.forEach(panel -> {
	            centerPanel.addTab(panel.getTabTitle(), panel.getTabIcon(),
	                panel, panel.getTabToolTip());
        });

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
        
        if (unit instanceof Person) {
        	statueUpdate();
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

	 /*
	 public float getAlpha() {
	     return alpha;
	 }

	 public void setAlpha(float alpha) {
	     this.alpha = alpha;
	     repaint();
	 }


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


	 private static void setupGraphics(Graphics2D g2) {
	     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                         RenderingHints.VALUE_ANTIALIAS_ON);

	     Toolkit tk = Toolkit.getDefaultToolkit();
	     Map desktopHints = (Map) (tk.getDesktopProperty("awt.font.desktophints"));
	     if (desktopHints != null) {
	         g2.addRenderingHints(desktopHints);
	     }
	 }
*/

	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {}

}