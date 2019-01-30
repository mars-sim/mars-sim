///**
// * Mars Simulation Project
// * DesktopPane.java
// * @version 3.1.0 2016-10-22
// * @author Manny Kung
// */
//package org.mars_sim.msp.ui.swing;
//
//import java.awt.Color;
//import java.awt.Component;
//import java.awt.Dimension;
//import java.util.logging.Logger;
//
//import javax.swing.ImageIcon;
//import javax.swing.JDesktopPane;
//import javax.swing.JInternalFrame;
//import javax.swing.JLabel;
//
//import org.mars_sim.msp.core.UnitManagerEvent;
//import org.mars_sim.msp.core.structure.Settlement;
//import org.mars_sim.msp.core.structure.building.Building;
//import org.mars_sim.msp.ui.javafx.MainScene;
//import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
//
///**
// * Each DesktopPane class is a swing desktop for housing a tool window
// */
//public class DesktopPane extends JDesktopPane
//
//{
//
//	/** default serial id. */
//	private static final long serialVersionUID = 1L;
//	/** default logger. */
//	private static Logger logger = Logger.getLogger(DesktopPane.class.getName());
//
//	/** True if this MainDesktopPane hasn't been displayed yet. */
//	private boolean firstDisplay;
//	private boolean isTransportingBuilding = false, isConstructingSite = false;
//
//	private ImageIcon backgroundImageIcon;
//	/** Label that contains the tiled background. */
//	private JLabel backgroundLabel;
//
//	private Building building;
//
//	private MainScene mainScene;
//
//	/**
//	 * Constructor 1 for setting up javaFX desktop
//	 * 
//	 * @param mainScene
//	 *            the main scene
//	 */
//	public DesktopPane(MainScene mainScene) {
//
//		this.mainScene = mainScene;
//
//		initialize();
//	}
//
//	public void initialize() {
//		// Set background color to black
//		setBackground(Color.black);
//
//		// set desktop manager
//		setDesktopManager(new MainDesktopManager());
//
//		// Set component listener
//		// addComponentListener(this);
//
//		// Create background label and set it to the back layer
//		backgroundImageIcon = new ImageIcon();
//		backgroundLabel = new JLabel(backgroundImageIcon);
//		add(backgroundLabel, Integer.MIN_VALUE);
//		backgroundLabel.setLocation(0, 0);
//		moveToBack(backgroundLabel);
//
//		// Initialize firstDisplay to true
//		firstDisplay = true;
//
//		setPreferredSize(new Dimension(1280, 1024));
//
//		prepareListeners();
//	}
//
//	/**
//	 * Constructor 2 for setting up swing desktop
//	 */
//	public DesktopPane() {
//
//		initialize();
//	}
//
//	/**
//	 * Returns the MainScene instance
//	 * 
//	 * @return MainScene instance
//	 */
//	public MainScene getMainScene() {
//		return mainScene;
//	}
//
//	/**
//	 * Create background tile when MainDesktopPane is first displayed. Recenter
//	 * logoLabel on MainWindow and set backgroundLabel to the size of
//	 * MainDesktopPane.
//	 * 
//	 * @param e
//	 *            the component event
//	 * 
//	 * @Override public void componentResized(ComponentEvent e) {
//	 * 
//	 *           // If displayed for the first time, create background image tile.
//	 *           // The size of the background tile cannot be determined during
//	 *           construction // since it requires the MainDesktopPane be displayed
//	 *           first. if (firstDisplay) { ImageIcon baseImageIcon =
//	 *           ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
//	 *           Dimension screen_size =
//	 *           Toolkit.getDefaultToolkit().getScreenSize(); Image backgroundImage
//	 *           = createImage((int) screen_size.getWidth(), (int)
//	 *           screen_size.getHeight()); Graphics backgroundGraphics =
//	 *           backgroundImage.getGraphics();
//	 * 
//	 *           for (int x = 0; x < backgroundImage.getWidth(this); x +=
//	 *           baseImageIcon.getIconWidth()) { for (int y = 0; y <
//	 *           backgroundImage.getHeight(this); y +=
//	 *           baseImageIcon.getIconHeight()) { backgroundGraphics.drawImage(
//	 *           baseImageIcon.getImage(), x, y, this); } }
//	 * 
//	 *           backgroundImageIcon.setImage(backgroundImage);
//	 * 
//	 *           backgroundLabel.setSize(getSize());
//	 * 
//	 *           firstDisplay = false; }
//	 * 
//	 *           // Set the backgroundLabel size to the size of the desktop
//	 *           backgroundLabel.setSize(getSize()); }
//	 */
//
//	/**
//	 * sets up this class with two listeners
//	 */
//	// 2014-12-19 Added prepareListeners()
//	public void prepareListeners() {
//
//	}
//
//	public void updateToolWindow() {
//		logger.info("DesktopPane : updateToolWindow()");
//		JInternalFrame[] frames = this.getAllFrames();
//		for (JInternalFrame f : frames) {
//			// f.updateUI();
//			// SwingUtilities.updateComponentTreeUI(f);
//			((ToolWindow) f).update();
//		}
//	}
//
//	@Override
//	public Component add(Component comp) {
//		super.add(comp);
//		centerJIF(comp);
//		return comp;
//	}
//
//	public void centerJIF(Component comp) {
//		Dimension desktopSize = getSize();
//		Dimension jInternalFrameSize = comp.getSize();
//		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
//		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
//		comp.setLocation(width, height);
//		comp.setVisible(true);
//	}
//
//	public void unitManagerUpdate(UnitManagerEvent event) {
//
//		Object unit = event.getUnit();
//		if (unit instanceof Settlement) {
//
//			updateToolWindow();
//		}
//	}
//
//	public void destroy() {
//		backgroundImageIcon = null;
//		backgroundLabel = null;
//		building = null;
//		mainScene = null;
//	}
//
//}