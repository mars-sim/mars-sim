/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Simulation.SaveType;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.WaitLayerUIPanel;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

import com.alee.api.resource.ClassResource;
import com.alee.extended.button.WebSwitch;
import com.alee.extended.date.WebDateField;
import com.alee.extended.label.WebStyledLabel;
import com.alee.extended.memorybar.WebMemoryBar;
import com.alee.extended.overlay.FillOverlay;
import com.alee.extended.overlay.WebOverlay;
import com.alee.extended.svg.SvgIconSource;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebFrame;
import com.alee.managers.UIManagers;
import com.alee.managers.icon.IconManager;
import com.alee.managers.icon.LazyIcon;
import com.alee.managers.icon.set.IconSet;
import com.alee.managers.icon.set.RuntimeIconSet;
import com.alee.managers.language.LanguageManager;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.swing.NoOpKeyListener;
import com.alee.utils.swing.NoOpMouseListener;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow 
extends JComponent implements ClockListener {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	/** Icon image filename for frame */
	public static final String LANDER_PNG = "landerhab16.png";//"/images/LanderHab.png";
	public static final String LANDER_SVG = "/svg/icons/lander_hab.svg";
	
	public static final String INFO_RED_SVG = "/svg/icons/info_red.svg";
	public static final String PAUSE_ORANGE_SVG = "/svg/icons/pause_orange.svg";
	public static final String MARS_CALENDAR_SVG = "/svg/icons/calendar_mars.svg";
		
	public static final String INFO_SVG = "/svg/icons/info.svg";
	public static final String EDIT_SVG = "/svg/icons/edit.svg";
	public static final String LEFT_SVG = "/svg/icons/left_rotate.svg";
	public static final String RIGHT_SVG = "/svg/icons/right_rotate.svg";
	public static final String CENTER_SVG = "/svg/icons/center.svg";
	public static final String STACK_SVG = "/svg/icons/stack.svg";
	
	public static final String SAND_SVG = Msg.getString("img.svg.sand");//$NON-NLS-1$
	public static final String HAZY_SVG = Msg.getString("img.svg.hazy");//$NON-NLS-1$
	
	public static final String SANDSTORM_SVG = Msg.getString("img.svg.sandstorm"); //$NON-NLS-1$
	public static final String DUST_DEVIL_SVG = Msg.getString("img.svg.dust_devil");//$NON-NLS-1$

	public static final String COLD_WIND_SVG = Msg.getString("img.svg.cold_wind");//$NON-NLS-1$
	public static final String FROST_WIND_SVG = Msg.getString("img.svg.frost_wind");//$NON-NLS-1$

	public static final String SUN_SVG = Msg.getString("img.svg.sun"); //$NON-NLS-1$
	public static final String DESERT_SUN_SVG = Msg.getString("img.svg.desert_sun");//$NON-NLS-1$
	public static final String CLOUDY_SVG = Msg.getString("img.svg.cloudy");//$NON-NLS-1$
	public static final String SNOWFLAKE_SVG = Msg.getString("img.svg.snowflake");//$NON-NLS-1$
	public static final String ICE_SVG = Msg.getString("img.svg.ice");//$NON-NLS-1$

	public static final String MARS_SVG = Msg.getString("img.svg.mars");//$NON-NLS-1$
	public static final String TELESCOPE_SVG = Msg.getString("img.svg.telescope");//$NON-NLS-1$
	
	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	
	private static final String SOL = "   Sol ";
	private static final String WHITESPACES = "   ";
	private static final String UMT = " (UMT)";
//	private static final String SLEEP_TIME = "   Sleep Time : ";
//	private static final String MS = " ms   ";
	
	/** The size of the weather icons */
	public static final int WEATHER_ICON_SIZE = 64;
	/** The timer for update the status bar labels. */
	private static final int TIME_DELAY = 2_000;
	/** Keeps track of whether icons have been added to the IconManager . */
	private static boolean iconsConfigured = false;
	/** The main window frame. */	
	private static WebFrame frame;
	/** The lander hab icon. */
	private static Icon landerIcon;
	/** The Mars icon. */
	private static Icon marsIcon;
	/** The Telescope icon. */
	private static Icon telescopeIcon;
	
	/** The four types of theme types. */	
	public enum ThemeType {
		SYSTEM, NIMBUS, NIMROD, WEBLAF, METAL
	}
	/** The default ThemeType enum. */	
	public ThemeType defaultThemeType = ThemeType.NIMBUS;

	// Data members
	private boolean useDefault = true;

	private int solCache = 0;

	private String lookAndFeelTheme;

	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private MainWindowMenu mainWindowMenu;

	private final AtomicBoolean sleeping = new AtomicBoolean(false);

	private Timer delayTimer;
	private Timer delayTimer1;
	
	private OrbitViewer orbitViewer;
	
	private javax.swing.Timer earthTimer;

	private JStatusBar statusBar;

	/** WebSwitch for the control of play or pause the simulation*/
	private WebSwitch pauseSwitch;
	
	private WebButton increaseSpeed;

	private WebButton decreaseSpeed;

	private WebButton starMap;
	
	private JCheckBox overlayCheckBox;
	
	private WebOverlay overlay;
		
	private WebStyledLabel blockingOverlay; 
    
	private WebStyledLabel solLabel;

	private WebTextField marsTimeTF;
	
	private WebDateField earthDateField;
	
	private WebMemoryBar memoryBar;
	
	private WebPanel bottomPane;
	private WebPanel mainPane;

//	private Font FONT_SANS_SERIF = new Font(Font.SANS_SERIF, Font.BOLD, 12);
	private Font FONT_SANS_SERIF_1 = new Font(Font.SANS_SERIF, Font.BOLD, 13);
	
	/** Arial font. */ 
	private Font ARIAL_FONT = new Font("Arial", Font.PLAIN, 14);
	
	private JLayer<JPanel> jlayer;
	private WaitLayerUIPanel layerUI = new WaitLayerUIPanel();

	private static SplashWindow splashWindow;
	
	private static Simulation sim = Simulation.instance();
	// Warning: can't create the following instances at the start of the sim or else MainWindow won't load
	private static MasterClock masterClock;// = sim.getMasterClock(); 
	private static EarthClock earthClock;// = masterClock.getEarthClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();

	/**
	 * Constructor 1.
	 * 
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI) {
//		logger.config("MainWindow is on " + Thread.currentThread().getName() + " Thread");
//		SwingUtilities.invokeLater(() -> layerUI.start());
		
		// Start the wait layer
		layerUI.start();
		
		logger.config("width : " + InteractiveTerm.getWidth() + "  height : " + InteractiveTerm.getHeight());
		// this.cleanUI = cleanUI;
		// Set up the look and feel library to be used
		initializeTheme();
		
		// Set up the frame
		frame = new WebFrame();//StyleId.rootpane);
		frame.setPreferredSize(new Dimension(InteractiveTerm.getWidth(), InteractiveTerm.getHeight()));
		frame.setSize(new Dimension(InteractiveTerm.getWidth(), InteractiveTerm.getHeight()));
		
		frame.setResizable(false);
		
//		frame.setIconImages(WebLookAndFeel.getImages());
		
		// Disable the close button on top right
//		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set the UI configuration
		useDefault = UIConfig.INSTANCE.useUIDefault();

		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this);

		// Set up timers for use on the status bar
		setupDelayTimer();
		
		if (!iconsConfigured)
			MainWindow.initIconManager();
		
		// Initialize UI elements for the frame
		SwingUtilities.invokeLater(() -> {
        	init();    

    		// Set frame size
    		final Dimension frame_size;
    		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
    		if (useDefault) {
    			// Make frame size 80% of screen size.
    			if (screen_size.width > 800) {
    				frame_size = new Dimension(
    					(int) Math.round(screen_size.getWidth() * .9D),
    					(int) Math.round(screen_size.getHeight() * .9D)
    				);
    			} else {
    				frame_size = new Dimension(screen_size);
    			}
    		} else {
    			frame_size = UIConfig.INSTANCE.getMainWindowDimension();
    		}
    		
    		frame.setSize(frame_size);

    		// Set frame location.
    		if (useDefault) {
    			// Center frame on screen
    			frame.setLocation(
    				((screen_size.width - frame_size.width) / 2),
    				((screen_size.height - frame_size.height) / 2)
    			);
    		} else {
    			frame.setLocation(UIConfig.INSTANCE.getMainWindowLocation());
//		    	frame.setLocationRelativeTo(null);
    		}
    		
    		// Show frame
    		frame.pack();
    		frame.setVisible(true);
    		
    		layerUI.stop();
	    });  
		
		// Dispose the Splash Window
		disposeSplash();

		// Open all initial windows.
		desktop.openInitialWindows();
		
	}

	public void stopLayerUI() {
		layerUI.stop();
	}
	
	public static void initIconManager() {
		iconsConfigured = true;
		// Set up an icon set for use throughout mars-sim
		IconSet iconSet = new RuntimeIconSet("mars-sim-set");
	
		int size = 24;
		
		iconSet.addIcon(new SvgIconSource (
		        "info_red",
		        new ClassResource(MainWindow.class, INFO_RED_SVG),
		        new Dimension(12, 12)));

		iconSet.addIcon(new SvgIconSource (
		        "pause_orange",
		        new ClassResource(MainWindow.class, PAUSE_ORANGE_SVG),
		        new Dimension(600, 600)));

		iconSet.addIcon(new SvgIconSource (
		        "calendar_mars",
		        new ClassResource(MainWindow.class, MARS_CALENDAR_SVG),
		        new Dimension(16, 16)));
		
		
		
		iconSet.addIcon(new SvgIconSource (
		        "lander",
		        new ClassResource(MainWindow.class, LANDER_SVG),
		        new Dimension(16, 16)));
		
		iconSet.addIcon(new SvgIconSource (
		        "info",
		        new ClassResource(MainWindow.class, INFO_SVG),
		        new Dimension(size, size)));
		
//		String s1 = CrewEditor.class.getResource(MainWindow.EDIT_SVG).getPath();
		
		iconSet.addIcon(new SvgIconSource (
		        "edit",
		        new ClassResource(MainWindow.class, EDIT_SVG),
		        new Dimension(size, size)));
		
//		String s2 = CrewEditor.class.getResource(LANDER_SVG).getPath();		
//		File f2a = new File(LANDER_SVG);
		
		iconSet.addIcon(new SvgIconSource (
		        "left",
		        new ClassResource(MainWindow.class, LEFT_SVG),
		        new Dimension(size, size)));
		
		iconSet.addIcon(new SvgIconSource (
		        "right",
		        new ClassResource(MainWindow.class, RIGHT_SVG),
		        new Dimension(size, size)));
		
		iconSet.addIcon(new SvgIconSource (
		        "center",
		        new ClassResource(MainWindow.class, CENTER_SVG),
		        new Dimension(size, size)));
		
		iconSet.addIcon(new SvgIconSource (
		        "stack",
		        new ClassResource(MainWindow.class, STACK_SVG),
		        new Dimension(size, size)));

		/////////////////////////////////////////////////////////
		
		iconSet.addIcon(new SvgIconSource (
		        "sandstorm",
		        new ClassResource(MainWindow.class, SANDSTORM_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "dustDevil",
		        new ClassResource(MainWindow.class, DUST_DEVIL_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		////////////////////
		
		iconSet.addIcon(new SvgIconSource (
		        "frost_wind",
		        new ClassResource(MainWindow.class, FROST_WIND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "cold_wind",
		        new ClassResource(MainWindow.class, COLD_WIND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
				
		////////////////////
		
		iconSet.addIcon(new SvgIconSource (
		        "sun",
		        new ClassResource(MainWindow.class, SUN_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		iconSet.addIcon(new SvgIconSource (
		        "desert_sun",
		        new ClassResource(MainWindow.class, DESERT_SUN_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "cloudy",
		        new ClassResource(MainWindow.class, CLOUDY_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "snowflake",
		        new ClassResource(MainWindow.class, SNOWFLAKE_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "ice",
		        new ClassResource(MainWindow.class, ICE_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		////////////////////
		
		iconSet.addIcon(new SvgIconSource (
		        "sand",
		        new ClassResource(MainWindow.class, SAND_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));
		
		iconSet.addIcon(new SvgIconSource (
		        "hazy",
		        new ClassResource(MainWindow.class, HAZY_SVG),
		        new Dimension(WEATHER_ICON_SIZE, WEATHER_ICON_SIZE)));

		
		////////////////////
				
		iconSet.addIcon(new SvgIconSource (
		      "mars",
		      new ClassResource(MainWindow.class, MARS_SVG),
		      new Dimension(18, 18)));

		iconSet.addIcon(new SvgIconSource (
			      "telescope",
			      new ClassResource(MainWindow.class, TELESCOPE_SVG),
			      new Dimension(18, 18)));
		
		// Add the icon set to the icon manager
		IconManager.addIconSet(iconSet);
			
		landerIcon = new LazyIcon("lander").getIcon(); 
	}
	
 
//	/**
//	 * Converts InputStream to File
//	 * 
//	 * @param inputStream
//	 * @param file
//	 * @throws IOException
//	 */
//    private static File copyInputStreamToFile(InputStream inputStream, File file)
//		throws IOException {
//
//        try (FileOutputStream outputStream = new FileOutputStream(file)) {
//
//            int read;
//            byte[] bytes = new byte[1024];
//
//            while ((read = inputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, read);
//            }
//
//			// commons-io
//            //IOUtils.copy(inputStream, outputStream);
//        }
//        
//        return file;
//    }

//	/**
//	 * Returns an image from an icon
//	 * 
//	 * @param icon
//	 * @return
//	 */
//	public static Image iconToImage(Icon icon) {
//		if (icon instanceof ImageIcon) {
//			return ((ImageIcon)icon).getImage();
//		} 
//		else {
//			int w = icon.getIconWidth();
//			int h = icon.getIconHeight();
//			GraphicsEnvironment ge = 
//					GraphicsEnvironment.getLocalGraphicsEnvironment();
//			GraphicsDevice gd = ge.getDefaultScreenDevice();
//			GraphicsConfiguration gc = gd.getDefaultConfiguration();
//			BufferedImage image = gc.createCompatibleImage(w, h);
//			Graphics2D g = image.createGraphics();
//			icon.paintIcon(null, g, 0, 0);
//			g.dispose();
//			return image;
//		}
//	}
	
	/**
	 * Initializes UI elements for the frame
	 */
	@SuppressWarnings("serial")
	public void init() {
			
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

    	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    	
		desktop.changeTitle(false);
		
		frame.setIconImage(getIconImage());

		// Set up the main pane
		mainPane = new WebPanel(new BorderLayout());
		frame.add(mainPane);

		// Set up the jlayer pane
		WebPanel jlayerPane = new WebPanel(new BorderLayout());
		jlayerPane.add(desktop);
//		jlayer.add(desktop);
			
		// Set up the glassy wait layer for pausing
		jlayer = new JLayer<>(jlayerPane, layerUI);
//		mainPane.add(jlayer);

		// Add overlay
//		jlayerPane.add(overlay, BorderLayout.CENTER);	
	
		// Set up the overlay pane
		WebPanel overlayPane = new WebPanel(new BorderLayout());

		// Create a pause overlay
		createOverlay(overlayPane);
				
		// Add desktop
//		mainPane.add(desktop, BorderLayout.CENTER);
		
		// Add desktop to the overlay pane
//		overlayPane.add(desktop, BorderLayout.CENTER);
		overlayPane.add(jlayer, BorderLayout.CENTER);
		
		// Add overlay
		mainPane.add(overlay, BorderLayout.CENTER);
		
		// TODO: it doesn't work.
		// Set up the ESC key for pausing 
//		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "pause");
//		getActionMap().put("pause", new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            	if (masterClock == null)
//        			masterClock = sim.getMasterClock();
//        		System.out.println(masterClock);
//        		masterClock.setPaused(!masterClock.isPaused(), true);
//            }
//        });
        
		// TODO: it doesn't work.
//		frame.addKeyListener(new KeyAdapter() {
//            public void keyPressed(KeyEvent ke) {  // handler
//            	if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
//            		if (masterClock == null)
//            			masterClock = sim.getMasterClock();
////            		if (masterClock.isPaused()) {
////    					masterClock.setPaused(false, false);
////    				}
////    				else {
////    					masterClock.setPaused(true, false);
////    				}
//            		System.out.println(masterClock);
//            		masterClock.setPaused(!masterClock.isPaused(), true);
//            	}
//           } 
//        });
		
		// Initialize data members
		if (earthClock == null) {
			if (masterClock == null)
				masterClock = sim.getMasterClock();
			earthClock = masterClock.getEarthClock();
			marsClock = masterClock.getMarsClock();
		}
		
		// Add this class to the master clock's listener
		masterClock.addClockListener(this);
		
		// Create Earth date text field
		createEarthDate();	
		
		// Create sol label
		createSolLabel();
		
		// Create Mars date text field
		createMarsDate();
	
		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		
//		WebPanel topPane = new WebPanel(new GridLayout(2, 1));
//		topPane.add(mainWindowMenu);
//		topPane.add(toolToolbar);
		
		// Add toolToolbar to mainPane
		overlayPane.add(toolToolbar, BorderLayout.NORTH);
	
		// Add bottomPane for holding unitToolbar and statusBar
		bottomPane = new WebPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
			@Override
			protected JButton createActionComponent(Action a) {
				JButton jb = super.createActionComponent(a);
//				jb.setOpaque(false);
				return jb;
			}
		};

		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		// Prepare menu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Close the unit bar when starting up
		unitToolbar.setVisible(false);
		
		// Create the status bar
		statusBar = new JStatusBar(1, 1, 28);

		// Create speed buttons
		createSpeedButtons();
		// Add the decrease speed button
		statusBar.addLeftComponent(decreaseSpeed, false);

		// Create pause switch
		createPauseSwitch();
		statusBar.addLeftComponent(pauseSwitch, false);
		
		// Add the increase speed button
		statusBar.addLeftComponent(increaseSpeed, false);

		// Create overlay button
		createOverlayCheckBox();
		statusBar.addLeftComponent(overlayCheckBox, false);
		
		createStarMapButton();
		statusBar.addRightComponent(starMap, true);
		
		// Create memory bar
		createMemoryBar();
		statusBar.addRightComponent(memoryBar, true);
		
		statusBar.addRightCorner();
		
		bottomPane.add(statusBar, BorderLayout.SOUTH);
//		logger.config("Done with init()");
	}

	/**
	 * Sets up the pause overlay
	 * 
	 * @param overlayPane
	 */
	public void createOverlay(WebPanel overlayPane) {
		// Add overlayPane to overlay
		overlay = new WebOverlay(StyleId.overlay, overlayPane);

		Icon pauseIcon = new LazyIcon("pause_orange").getIcon();
				
        blockingOverlay = new WebStyledLabel(
        		//StyleId.overlay,//.of("blocking-layer"),
        		pauseIcon,
//        		"P A U S E",
                SwingConstants.CENTER
        );
        NoOpMouseListener.install(blockingOverlay);
        NoOpKeyListener.install(blockingOverlay);
	}
	
	public void createOverlayCheckBox() {
		overlayCheckBox = new JCheckBox("{Pause Overlay On/Off:b}", false);
		overlayCheckBox.putClientProperty(StyleId.STYLE_PROPERTY, StyleId.checkboxLink);
		TooltipManager.setTooltip(overlayCheckBox, "Turn on/off pause overlay in desktop", TooltipWay.up);
		
		overlayCheckBox.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED) {
		        	// Checkbox has been selected
//		        	if (masterClock.isPaused())
		        	overlay.addOverlay(new FillOverlay(blockingOverlay));
		        } else {
		        	// Checkbox has been unselected
	                if (blockingOverlay.isShowing()) {
	                    overlay.removeOverlay(blockingOverlay);
	                }
		        };
		    }
		});
		// Disable the overlay check box at start of the sim
		overlayCheckBox.setEnabled(false);
		
//        overlayButton = new WebButton("Overlay");
//        overlayButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(@NotNull final ActionEvent e) {
//                if (blockingOverlay.isShowing()) {
//                    overlay.removeOverlay(blockingOverlay);
////                    overlayButton.setLanguage("Overlay Off");
////                    overlayButton.setText("Overlay Off");
//                }
//                else {
//                    overlay.addOverlay(new FillOverlay(blockingOverlay));
////                    overlayButton.setText("Overlay On");
////                    overlayButton.setLanguage("Overlay On");
//                }
//            }
//        } );
	}
	
	public void createPauseSwitch() {
		pauseSwitch = new WebSwitch(true);
		pauseSwitch.setSwitchComponents(
				ImageLoader.getIcon(Msg.getString("img.speed.play")), 
				ImageLoader.getIcon(Msg.getString("img.speed.pause")));
		TooltipManager.setTooltip(pauseSwitch, "Pause or Resume the Simulation", TooltipWay.up);
		
		pauseSwitch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pauseSwitch.isSelected())
					masterClock.setPaused(false, false);
				else
					masterClock.setPaused(true, false);
			};
		});
	}
	
	/**
	 * Open orbit viewer
	 */
	public void openOrbitViewer() {
		if (orbitViewer == null) {
			orbitViewer = new OrbitViewer(desktop);
//			orbitViewer.setVisible(true);
			return;
		}
			
		if (!orbitViewer.isVisible())
			orbitViewer.setVisible(true);
		else
			orbitViewer.setVisible(false);
		
	}
	
//	public boolean isOrbitViewerOn() {
//		if (orbitViewer == null)
//			return false;
//		else
//			return true;
//	}
//
	public void setOrbitViewer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}
	
	public Icon getMarsIcon() {
		return marsIcon;
	}
	
	public Icon getTelescopeIcon() {
		return telescopeIcon;
	}
	
	
	public void createStarMapButton() {
		starMap = new WebButton();
		marsIcon = new LazyIcon("telescope").getIcon(); 
		starMap.setIcon(marsIcon);//ImageLoader.getIcon(Msg.getString("img.starMap"))); //$NON-NLS-1$
		TooltipManager.setTooltip(starMap, "Open the Orbit Viewer", TooltipWay.up);
		
		starMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openOrbitViewer();
			};
		});
	}
	
	public void createSpeedButtons() {
		increaseSpeed = new WebButton();
		increaseSpeed.setIcon(ImageLoader.getIcon(Msg.getString("img.speed.increase"))); //$NON-NLS-1$
		TooltipManager.setTooltip(increaseSpeed, "Increase the sim speed (aka time ratio)", TooltipWay.up);
		
		increaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.increaseSpeed();
					updateTimeLabel();
				}
			};
		});
		
		decreaseSpeed = new WebButton();
		decreaseSpeed.setIcon(ImageLoader.getIcon(Msg.getString("img.speed.decrease"))); //$NON-NLS-1$
		TooltipManager.setTooltip(decreaseSpeed, "Decrease the sim speed (aka time ratio)", TooltipWay.up);
		
		decreaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.decreaseSpeed();
					updateTimeLabel();	
				}
			};
		});
		
	}
	
	public void updateTimeLabel() {
		((TimeWindow) desktop.getToolWindow(TimeWindow.NAME)).updateSlowLabels();
	}
	
	public void createSolLabel() {
		solLabel = new WebStyledLabel(StyleId.styledlabelShadow);
		solLabel.setFont(FONT_SANS_SERIF_1);
		solLabel.setForeground(Color.DARK_GRAY);
		solLabel.setText(SOL + "1" + WHITESPACES);
		solLabel.setHorizontalAlignment(JLabel.CENTER);
		solLabel.setVerticalAlignment(JLabel.CENTER);
		TooltipManager.setTooltip(solLabel, "# of sols since the beginning of the sim", TooltipWay.up);
	}
	
	public WebStyledLabel getSolLabel() {
		return solLabel;
	}
		
	public void createMemoryBar() {
		memoryBar = new WebMemoryBar();
		memoryBar.setPreferredWidth(180);
		memoryBar.setRefreshRate(3000);
		memoryBar.setFont(ARIAL_FONT);
		memoryBar.setForeground(Color.DARK_GRAY);
	}
	
	public WebMemoryBar getMemoryBar() {
		return memoryBar;
	}
	
	public void createEarthDate() {
		earthDateField = new WebDateField(StyleId.datefield);//new Date(earthClock.getInstant().toEpochMilli()));
		TooltipManager.setTooltip(earthDateField, "Earth Timestamp in Greenwich Mean Time (GMT)", TooltipWay.up);
		earthDateField.setPreferredWidth(240);
		earthDateField.setAllowUserInput(false);
//		Customizer<WebCalendar> c = dateField.getCalendarCustomizer();
//		c.customize();
//		earthDateField.setCalendarCustomizer(c);
		earthDateField.setFont(ARIAL_FONT);
		earthDateField.setForeground(new Color(0, 69, 165));
		earthDateField.setAlignmentX(.5f);
		earthDateField.setAlignmentY(.5f);
		DateFormat d = new SimpleDateFormat("yyyy-MMM-dd  HH:mm a '['z']'", LanguageManager.getLocale());
		d.setTimeZone(TimeZone.getTimeZone("GMT"));
		earthDateField.setDateFormat(d); 
		
		if (earthClock.getInstant() != null) {
//			LocalDateTime ldt = LocalDateTime.ofInstant(earthClock.getInstant(), ZoneId.of("UTC"));
//			ZonedDateTime zdt = ldt.atZone(ZoneId.of("UTC"));
//			Date date = Date.from(zdt.toInstant());
//			earthDateField.setDate(date);
			earthDateField.setDate(new Date(earthClock.getInstant().toEpochMilli()));
		}
	}
	
	public WebDateField getEarthDate() {
		return earthDateField;
	}
	
	public void createMarsDate() {	
		marsTimeTF = new WebTextField(StyleId.formattedtextfieldNoFocus, 16);
		marsTimeTF.setEditable(false);
		marsTimeTF.setFont(ARIAL_FONT);
//		marsTimeTF.setPreferredWidth(240);
		marsTimeTF.setForeground(new Color(150,96,0));//135,100,39));
		marsTimeTF.setAlignmentX(.5f);
		marsTimeTF.setAlignmentY(.5f);
		marsTimeTF.setHorizontalAlignment(JLabel.LEFT);
		TooltipManager.setTooltip(marsTimeTF, "Mars Timestamp in Universal Mars Time (UMT)", TooltipWay.up);
	}
	
	public WebTextField getMarsTime() {
		return marsTimeTF;
	}
	 
	/**
	 * Set up the timer for status bar
	 */
	public void setupDelayTimer() {
		if (delayTimer == null) {
			delayTimer = new Timer();
			delayTimer.schedule(new DelayTimer(), 300);
		}
	}

	/**
	 * Defines the delay timer class
	 */
	class DelayTimer extends TimerTask {
		public void run() {
			runStatusTimer();
		}
	}

	/**
	 * Set up the timer for caching settlement windows
	 */
	public void setupSettlementWindowTimer() {
		delayTimer1 = new Timer();
		delayTimer1.schedule(new CacheLoadingSettlementTimerTask(), 2000);
	}

	/**
	 * Defines the delay timer class
	 */
	class CacheLoadingSettlementTimerTask extends TimerTask {
		public void run() {
			// Cache each settlement unit window
			SwingUtilities.invokeLater(() -> desktop.cacheSettlementUnitWindow());
		}
	}

	public JPanel getBottomPane() {
		return bottomPane;
	}

	/**
	 * Start the earth timer
	 */
	public void runStatusTimer() {
		earthTimer = new javax.swing.Timer(TIME_DELAY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (earthClock == null || marsClock == null) {
					if (masterClock == null)
						masterClock = sim.getMasterClock();

					earthClock = masterClock.getEarthClock();
					marsClock = masterClock.getMarsClock();
				}

				// Increment both the earth and mars clocks
				incrementClocks();
				
				if (solLabel != null) {
					// Track mission sol
					int sol = marsClock.getMissionSol();
					if (solCache != sol) {
						solCache = sol;
						solLabel.setText(SOL + sol + WHITESPACES);
					}
				}
				// Check if the music track should be played
				desktop.getSoundPlayer().playRandomMusicTrack();
			}
		});

		earthTimer.start();
	}

	/**
	 * Get the window's frame.
	 * 
	 * @return the frame.
	 */
	public WebFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the main desktop panel.
	 * 
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Gets the Main Window Menu.
	 * 
	 * @return mainWindowMenu
	 */
	public MainWindowMenu getMainWindowMenu() {
		return mainWindowMenu;
	}


	/**
	 * Saves the current simulation.
	 * 
	 * @param defaultFile is the default.sim file be used
	 * @param isAutosave
	 */
	public void saveSimulation(boolean defaultFile, final boolean isAutosave) {
//		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
//			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
		saveSimulationProcess(defaultFile, isAutosave);
//				}
//			};
//			saveSimThread.start();
//		} 
//		
//		else {
//			saveSimThread.interrupt();
//			stopSleeping();
//		}
	}

	/**
	 * Performs the process of saving a simulation.
	 * Note: if defaultFile is false, displays a FileChooser to select the
	 * location and new filename to save the simulation.
	 * 
	 * @param defaultFile is the default.sim file be used
	 * @param isAutosave
	 */
	private void saveSimulationProcess(boolean defaultFile, boolean isAutosave) {
		if (isAutosave) {
//			SwingUtilities.invokeLater(() -> layerUI.start());
			masterClock.setSaveSim(SaveType.AUTOSAVE, null);
		}

		else {

			if (!defaultFile) {
				JFileChooser chooser = new JFileChooser(SimulationFiles.getSaveDir());
				chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
				if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
					final File fileLocn = chooser.getSelectedFile();
//					SwingUtilities.invokeLater(() -> layerUI.start());
					masterClock.setSaveSim(SaveType.SAVE_AS, fileLocn);
				} else {
					return;
				}
			}

			else {
//				SwingUtilities.invokeLater(() -> layerUI.start());
				masterClock.setSaveSim(SaveType.SAVE_DEFAULT, null);
			}
		}

		sleeping.set(true);
		while (sleeping.get() && masterClock.isSavingSimulation()) {
			try {
				// Thread.sleep(interval);
				TimeUnit.MILLISECONDS.sleep(200L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.log(Level.SEVERE, Msg.getString("MainWindow.log.sleepInterrupt") + ". " + e); //$NON-NLS-1$
				e.printStackTrace(System.err);
			}
			// do something here
		}
		
		// Save the current main window ui config
		UIConfig.INSTANCE.saveFile(this);

//		SwingUtilities.invokeLater(() -> layerUI.stop());

	}

	public void stopSleeping() {
		sleeping.set(false);
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.pausingSim") + "  "); //$NON-NLS-1$
		masterClock.setPaused(true, false);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		masterClock.setPaused(false, false);
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Create a new unit button in toolbar.
	 * 
	 * @param unit the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 * 
	 * @param unit the unit to dispose.
	 */
	public void disposeUnitButton(Unit unit) {
		unitToolbar.disposeUnitButton(unit);
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		if (!masterClock.isPaused() && !masterClock.isSavingSimulation()) {
			int reply = JOptionPane.showConfirmDialog(frame, 
					"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION);
	        if (reply == JOptionPane.YES_OPTION) {
	        	
	        	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	        	
	        	endSimulation();		
	    		// Save the UI configuration.
	    		UIConfig.INSTANCE.saveFile(this);
	    		masterClock.exitProgram();
	    		frame.dispose();		
	//			frame.setVisible(false);	
	    		destroy();
	    		System.exit(0);
	        } 
	        
	        else { //if (reply == JOptionPane.CANCEL_OPTION) {
	        	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	        }
		}
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSimulation() {
		sim.endSimulation();
//		sim.getSimExecutor().shutdown();// .shutdownNow();
	}

	/**
	 * Sets the theme skin after calling stage.show() at the start of the sim
	 */
	public void initializeTheme() {
//		SwingUtilities.invokeLater(() -> setLookAndFeel(defaultThemeType)); //initializeWeblaf());//
		setLookAndFeel(defaultThemeType);
	}

	/**
	 * Initialize weblaf them
	 */
	public void initializeWeblaf() {

		try {
			// use the weblaf skin
//			UIManager.setLookAndFeel(new WebLookAndFeel());
//			WebLookAndFeel.setForceSingleEventsThread ( true );
			WebLookAndFeel.install();
			UIManagers.initialize();
			
			// Installing our extension for default skin
//	        StyleManager.addExtensions ( new XmlSkinExtension ( MainWindow.class, "SimpleExtension.xml" ) );

            // They contain all custom styles demo application uses
//          StyleManager.addExtensions ( new AdaptiveExtension (), new LightSkinExtension (), new DarkSkinExtension () );

		} catch (Exception e) {
			logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
		}

	}

	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param choice
	 */
	public void setLookAndFeel(ThemeType choice1) {
		boolean changed = false;

		if (choice1 == ThemeType.METAL) {

			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");// UIManager.getCrossPlatformLookAndFeelClassName());//.getSystemLookAndFeelClassName());

//				logger.config(UIManager.getLookAndFeel().getName() + " is used in MainWindow.");

				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeTheme();

		}

		else if (choice1 == ThemeType.SYSTEM) {

			try {
				
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

//		else if (choice1 == ThemeType.NIMROD) {
//
//			initializeWeblaf();
//			
//			try {
//				NimRODTheme nt = new NimRODTheme(
//						getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme")); //
//				NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not
//				// working if switching to a brand new .theme file
//				NimRODLookAndFeel nf = new NimRODLookAndFeel();
//				nf.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme
//										// // file
//				UIManager.setLookAndFeel(nf);
//				changed = true; //
//	
//			} catch (Exception e) {
//				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$ } }
//			}
//			
//			try {
//				UIManager.setLookAndFeel(new NimRODLookAndFeel());
//			} catch (UnsupportedLookAndFeelException e) {
//				e.printStackTrace();
//			}
//		}

		else if (choice1 == ThemeType.NIMBUS) {

			try {
				boolean foundNimbus = false;			    
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) {
						// Set Nimbus look & feel if found in JVM.

						// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						// themeSkin = "nimbus";
						changed = true;
						 break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());

					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

		if (changed) {

//			logger.config(UIManager.getLookAndFeel().getName() + " is used in MainWindow.");

			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
//				SwingUtilities.updateComponentTreeUI(desktop);
				// desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}

//			frame.validate();
//			frame.repaint();
//			SwingUtilities.updateComponentTreeUI(frame);
//			frame.pack();

		}
	}

	/**
	 * Gets the unit toolbar.
	 * 
	 * @return unit toolbar.
	 */
	public UnitToolBar getUnitToolBar() {
		return unitToolbar;
	}

	/**
	 * Gets the tool toolbar.
	 * 
	 * @return tool toolbar.
	 */
	public ToolToolBar getToolToolBar() {
		return toolToolbar;
	}

	public String getLookAndFeelTheme() {
		return lookAndFeelTheme;
	}

//	public void setupMainWindow(boolean cleanUI) {
//		new Timer().schedule(new WindowDelayTimer(cleanUI), 1000);
//	}
//	
//	/**
//	 * Defines the delay timer class
//	 */
//	class WindowDelayTimer extends TimerTask {
//		public void run() {
//			// Create main window
//			SwingUtilities.invokeLater(() -> new MainWindow(cleanUI));
//		}
//	}
	
	/**
	 * Gets the main pane instance
	 * 
	 * @return
	 */
	public WebPanel getMainPane() {
		return mainPane;
	}
	
	/**
	 * Gets the lander hab icon instance
	 * 
	 * @return
	 */
	public static Icon getLanderIcon() {
		return landerIcon;
	}
	
	public static Image getIconImage() {
		return ((ImageIcon)landerIcon).getImage();
	}
	
	/**
	 * Increment the label of both the earth and mars clocks
	 */
	public void incrementClocks() {
		if (earthDateField != null && earthClock != null && earthClock.getInstant() != null) {
			earthDateField.setDate(new Date(earthClock.getInstant().toEpochMilli()));
		}
		
		if (marsTimeTF != null && marsClock != null) {
			marsTimeTF.setText(WHITESPACES + marsClock.getTrucatedDateTimeStamp() + UMT);
		}
		
	}
	
//	public WebButton getOverlayButton() {
//		return overlayButton;
//	}
	
//	public void clickOverlay() {
//		overlayButton.doClick();
//	}

	public void checkOverlay() {
		overlayCheckBox.setSelected(true);
	}
	
	public void uncheckOverlay() {
		overlayCheckBox.setSelected(false);
	}
	
	/**
	 * Starts the splash window frame
	 */
	public static void startSplash() {
        // Create a splash window
		if (splashWindow == null) {
			splashWindow = new SplashWindow();
		}
		
		splashWindow.setIconImage();
        splashWindow.display();
        splashWindow.getJFrame().setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
	}
	
	/**
	 * Disposes the splash window frame
	 */
	public void disposeSplash() {
		splashWindow.remove();
		splashWindow = null;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (pulse.getElapsed() > 0) {
			if (isVisible() || isShowing())
				// Increments the Earth and Mars clock labels.
				incrementClocks();
		}
	}

	@Override
	public void uiPulse(double time) {
//		if (time > 0)
//			; // nothing
	}

	/**
	 * Change the pause status. Called by Masterclock's firePauseChange() since
	 * TimeWindow is on clocklistener.
	 * 
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Update pause/resume webswitch buttons, based on masterclock's pause state.	
		if (isPaused) { // if it needs to pause
			// if the web switch is at the play position
			if (pauseSwitch.isSelected()) {
				// then switch it to the pause position and animate the change
				pauseSwitch.setSelected(false, true);
			}
			// Disable the overlay check box
			overlayCheckBox.setEnabled(true);
		} 
		
		else { // if it needs to resume playing
			// if the web switch is at the pause position
			if (!pauseSwitch.isSelected()) {
				// then switch it to the play position and animate the change
				pauseSwitch.setSelected(true, true);
			}
			// Disable the overlay check box
			overlayCheckBox.setEnabled(false);
		}
	}
	
	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		frame = null;
		unitToolbar = null;
		toolToolbar = null;
		desktop.destroy();
		desktop = null;
		mainWindowMenu = null;
//		mgr = null;
//		newSimThread = null;
//		loadSimThread = null;
//		saveSimThread = null;
		delayTimer = null;
//		autosaveTimer = null;
		earthTimer = null;
		statusBar = null;
		solLabel = null;
//		memMaxLabel = null;
//		memUsedLabel = null;
//		earthTimeLabel = null;
		bottomPane = null;
		mainPane = null;
		sim = null;
		masterClock = null;
		earthClock = null;
	}
}

//class WaitLayerUIPanel extends LayerUI<JPanel> implements ActionListener {
//
//	private boolean mIsRunning;
//	private boolean mIsFadingOut;
//	private javax.swing.Timer mTimer;
//	private int mAngle;
//	private int mFadeCount;
//	private int mFadeLimit = 15;
//
//	@Override
//	public void paint(Graphics g, JComponent c) {
//		int w = c.getWidth();
//		int h = c.getHeight();
//		super.paint(g, c); // Paint the view.
//		if (!mIsRunning) {
//			return;
//		}
//		Graphics2D g2 = (Graphics2D) g.create();
//		float fade = (float) mFadeCount / (float) mFadeLimit;
//		Composite urComposite = g2.getComposite(); // Gray it out.
//		if (.5f * fade < 0.0f) {
//			fade = 0;
//		}
//		else if (.5f * fade > 1.0f) {
//			fade = 1;
//		}
//		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f * fade));
//		g2.fillRect(0, 0, w, h);
//		g2.setComposite(urComposite);
//		int s = Math.min(w, h) / 5;// Paint the wait indicator.
//		int cx = w / 2;
//		int cy = h / 2;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setStroke(new BasicStroke(s / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//		g2.setPaint(Color.white);
//		g2.rotate(Math.PI * mAngle / 180, cx, cy);
//		for (int i = 0; i < 12; i++) {
//			float scale = (11.0f - (float) i) / 11.0f;
//			g2.drawLine(cx + s, cy, cx + s * 2, cy);
//			g2.rotate(-Math.PI / 6, cx, cy);
//			if (scale * fade < 0.0f) {
//				fade = 0;
//			}
//			else if (scale * fade > 1.0f) {
//				fade = 1;
//			}
//			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, scale * fade));
//		}
//		g2.dispose();
//	}
//
//	@Override
//	public void actionPerformed(ActionEvent e) {
//		if (mIsRunning) {
//			firePropertyChange("tick", 0, 1);
//			mAngle += 3;
//			if (mAngle >= 360) {
//				mAngle = 0;
//			}
//			if (mIsFadingOut) {
//				if (--mFadeCount == 0) {
//					mIsRunning = false;
//					mTimer.stop();
//				}
//			} else if (mFadeCount < mFadeLimit) {
//				mFadeCount++;
//			}
//		}
//	}
//	
//	public void start() {
//		if (mIsRunning) {
//			return;
//		}
//		mIsRunning = true;// Run a thread for animation.
//		mIsFadingOut = false;
//		mFadeCount = 0;
//		int fps = 24;
//		int tick = 1000 / fps;
//		mTimer = new javax.swing.Timer(tick, this);
//		mTimer.start();
//	}
//
//	public void stop() {
//		mIsFadingOut = true;
////		mIsRunning = false;
//	}
//
//	@Override
//	public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
//		if ("tick".equals(pce.getPropertyName())) {
//			l.repaint();
//		}
//	}
//}
