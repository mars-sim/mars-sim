/*
 * Mars Simulation Project
 * MainDesktopPane.java
 * @date 2025-08-17
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.mars_sim.core.Entity;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.desktop.ContentWindow;
import com.mars_sim.ui.swing.entitywindow.EntityContentFactory;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.sound.SoundConstants;
import com.mars_sim.ui.swing.tool.ToolRegistry;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.EntityMonitorModel;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.TimeTool;

/**
 * The MainDesktopPane class is the desktop part of the project's UI. It
 * contains all tool and unit windows, and is itself contained, along with the
 * tool bars, by the main window.
 */
@SuppressWarnings("serial")
public class MainDesktopPane extends JDesktopPane
		implements ClockListener, UIContext {

	// Properties for UIConfig settings
	private static final String DESKTOP_PROPS = "desktop";
	private static final String PRELOAD_TOOLS = "preload_tools";

	/** Default logger. */
	private static Logger logger = Logger.getLogger(MainDesktopPane.class.getName());

	/** The sound player. */
	private static AudioPlayer soundPlayer;

	/** The game mode of this simulation session. */
	public GameMode mode;

	private Collection<ContentWindow> entityWindows;
	private Collection<ContentWindow> toolWindows;

	/** ImageIcon that contains the tiled background. */
	private ImageIcon backgroundImageIcon;
	/** Label that contains the tiled background. */
	private JLabel backgroundLabel;
	/** The image icon of the tiled background. */
	private Image baseImageIcon = ImageLoader.getImage("background");

	private MainWindow mainWindow;
	
	// Preload the Tool windows
	private boolean preloadTools = true;

	// Simulation reference used by the UI windows
	private Simulation sim;

	/**
	 * Constructor 1.
	 *
	 * @param mainWindow the main outer window
	 */
	public MainDesktopPane(MainWindow mainWindow, Simulation sim) {
		super();

		this.mainWindow = mainWindow;
		this.sim = sim;

		// Initialize sound player
		if (!AudioPlayer.isAudioDisabled()) {
			soundPlayer = new AudioPlayer(this);
			soundPlayer.playMusic(SoundConstants.SND_SPLASH);
		}

		// Prepare tool windows. Needs to be thread safe as windows are used by clock pulse
		toolWindows = new CopyOnWriteArrayList<>();
		entityWindows = new CopyOnWriteArrayList<>();

		init();
	}

	private void init() {
		// Set background color to black
		setBackground(Color.black);
		// set desktop manager
		setDesktopManager(new MainDesktopManager());
		// Set component listener
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				MainDesktopPane.this.componentResized();
			}
		});

		// Create background label and set it to the back layer
		backgroundImageIcon = new ImageIcon();
		// Set up background
		backgroundLabel = new JLabel(backgroundImageIcon);
		// Add background
		add(backgroundLabel, Integer.MIN_VALUE);
		// Set location of background
		backgroundLabel.setLocation(0, 0);
		// Push the background to the back
		moveToBack(backgroundLabel);

		Properties props = mainWindow.getConfig().getPropSet(DESKTOP_PROPS);
		preloadTools = UIConfig.extractBoolean(props, PRELOAD_TOOLS, false);
		if (preloadTools) {
			// Prep tool windows
			prepareToolWindows();
		}

		// Set the main window's size
		Dimension selectedSize = mainWindow.getSelectedSize();
		if (selectedSize != null) {
			setSize(selectedSize);
			setPreferredSize(selectedSize);
			logger.config("Main Window initially set to " + selectedSize);
		}
	}

	/**
	 * Starts a background sound track.
	 */
	public void playBackgroundMusic() {
		// Play a random music track
		soundPlayer.playRandomMusicTrack();
	}
	
	/**
	 * Creates background tile when MainDesktopPane is first displayed. Center
	 * logoLabel on MainWindow and set backgroundLabel to the size of
	 * MainDesktopPane.
	 *
	 * @param e the component event
	 */
	private void componentResized() {

		Dimension screenSize = getSize();

		if (screenSize == null || screenSize.getWidth() == 0 || screenSize.getHeight() == 0) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			logger.config("Toolkit default screen size is " + screenSize.getWidth() + " x " + screenSize.getHeight());
		}

		Image backgroundImage = createImage((int) screenSize.getWidth(), (int) screenSize.getHeight());
		if (backgroundImage != null) {
			// Not loaded when the window is first building
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			int sourceWidth = baseImageIcon.getWidth(this);
			int sourceHeight = baseImageIcon.getHeight(this);
			int targetWidth = backgroundImage.getWidth(this);
			int targetHeight = backgroundImage.getHeight(this);
			for (int x = 0; x < targetWidth; x += sourceWidth) {
				for (int y = 0; y < targetHeight; y += sourceHeight) {
					backgroundGraphics.drawImage(baseImageIcon, x, y, this);
				}
			}

			backgroundImageIcon.setImage(backgroundImage);
			// Set the backgroundLabel size to the size of the desktop
			backgroundLabel.setSize(getSize());
			
			backgroundGraphics.dispose();
		}
	}

	@Override
	public Component add(Component comp) {
		super.add(comp);
		centerJIF(comp);
		return comp;
	}

	/**
	 * Move a window to the center of the desktop
	 * @param comp
	 */
	void centerJIF(Component comp) {
		Dimension desktopSize = getSize();
		Dimension jInternalFrameSize = comp.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		comp.setLocation(width, height);
		comp.setVisible(true);
	}

	/**
	 * Returns the MainWindow instance.
	 *
	 * @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	@Override
	public JFrame getTopFrame() {
		return mainWindow.getFrame();
	}

	/**
	 * Creates tool windows.
	 */
	private void prepareToolWindows() {
		getToolWindow(CommanderWindow.NAME, true);
		getToolWindow(NavigatorWindow.NAME, true);
		getToolWindow(SearchWindow.NAME, true);
		getToolWindow(TimeTool.NAME, true);
		getToolWindow(SettlementWindow.NAME, true);
		getToolWindow(MonitorWindow.NAME, true);
		getToolWindow(MissionWindow.NAME, true);
		getToolWindow(ResupplyWindow.NAME, true);
	}

	/**
	 * Get the list of open Tool windows
	 */
	Collection<ContentWindow> getToolWindows() {
		return toolWindows;
	}

	/**
	 * Get the list of open Entity windows
	 */
	Collection<ContentWindow> getEntityWindows() {
		return entityWindows;
	}

	/**
	 * Returns a tool window for a given tool name.
	 *
	 * @param toolName      the name of the tool window
	 * @param createWinddow Create a window if it doesn't exist
	 * @return the tool window
	 */
	private ContentWindow getToolWindow(String toolName, boolean createWindow) {
		for (var w : toolWindows) {
			if (toolName.equals(w.getContent().getName())) {
				bringToFront(w);
				return w;
			}
		}

		if (!createWindow) {
			return null;
		}

		var toolProps = mainWindow.getConfig().getInternalWindowProps(toolName);
		ContentPanel content = ToolRegistry.getTool(toolName, this, toolProps);
		if (content == null) {
			logger.warning("No tool called " + toolName);
			return null;
		}
		ContentWindow w = new ContentWindow(this, content);

		// Close it from the start
		try {
			w.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.warning("Problem loading tool window " + e.getMessage());
		}
		toolWindows.add(w);
		return w;
	}

	/**
	 * Displays a new Unit model in the monitor window.
	 *
	 * @param model the new model to display
	 */
	public void addModel(EntityMonitorModel<?> model) {
		var cw = openToolWindow(MonitorWindow.NAME);
		((MonitorWindow)cw).displayModel(model);
	}

	/**
	 * Returns true if tool window is open.
	 *
	 * @param toolName the name of the tool window
	 * @return true true if tool window is open
	 */
	public boolean isToolWindowOpen(String toolName) {
		var w = getToolWindow(toolName, false);
		if (w != null)
			return !w.isClosed();
		return false;
	}
	
	/**
	 * Opens a tool window if necessary.
	 *
	 * @param toolName the name of the tool window
	 */
	@Override
	public ContentPanel openToolWindow(String toolName) {
		ContentWindow window = getToolWindow(toolName, true);

		if (window == null) {
			return null;
		}

		if (window.isClosed()) {
			UIConfig config = mainWindow.getConfig();
			Point location = null;
			WindowSpec previousDetails = config.getInternalWindowDetails(toolName);
			if (previousDetails != null) {
				location = previousDetails.position();
				if (window.isResizable()) {
					window.setSize(previousDetails.size());
				}
			} else if (toolName.equals(TimeTool.NAME))
				location = computeLocation(window, 0, 2);
			else if (toolName.equals(MonitorWindow.NAME))
				location = computeLocation(window, 1, 0);
			else
				location = getCenterLocation(window);

			// Check is visible
			Dimension currentSize = getSize();
			location = new Point(Math.clamp(location.x, 1,(int)currentSize.getWidth() - 20),
								 Math.clamp(location.y, 1, (int)currentSize.getHeight() - 20));
			window.setLocation(location);
		}

		// in case of classic swing mode for MainWindow
		add(window, 0);

		try {
			window.setClosed(false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
		}

		window.show();

		// bring to front if it overlaps with other windows
		try {
			window.setSelected(true);
		} catch (PropertyVetoException e) {
			// ignore if setSelected is vetoed
		}

		validate();
		repaint();

		return window.getContent();
	}

	/**
	 * Closes a tool window if it is open.
	 *
	 * @param toolName the name of the tool window
	 */
	public void closeToolWindow(String toolName) {
		var window = getToolWindow(toolName, false);
		if ((window != null) && !window.isClosed()) {
			try {
				window.setClosed(true);
			} catch (PropertyVetoException e) {
				// ignore
			}
		}
	}

	/**
	 * Creates and opens a window for an Entity if it isn't already in existence and
	 * open. This selects the most appropriate tool window.
	 *
	 * @param entity Entity to display
	 */
	@Override
    public void showDetails(Entity entity) {
		if (entity instanceof Mission m) {
			var cw = openToolWindow(MissionWindow.NAME);
			((MissionWindow)cw).openMission(m);
		}
		else if (entity instanceof Transportable t) {
			var cw = openToolWindow(ResupplyWindow.NAME);
			((ResupplyWindow)cw).openTransportable(t);
		}
		else {
			openEntityPanel(entity, null);
		}
    }

	private class EntityPanelListener extends InternalFrameAdapter {
		/**
		 * Removes unit button from toolbar when unit window is closed.
		 *
		 * @param e internal frame event.
		 */
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			disposeEntityPanel((ContentWindow) e.getSource());
		}
	}
	
	/**
	 * Opens a Entity Window for a specific Entity with a optional set of user properties.
	 * 
	 * @param entity Entity to display
	 * @param initProps Initial properties
     * @return
	 */
	private void openEntityPanel(Entity entity, WindowSpec initProps) {
		// Is it already open?
		ContentWindow existing = entityWindows.stream()
						.filter(w -> w.getContent() instanceof EntityContentPanel<?> panel && panel.getEntity().equals(entity))
						.findFirst().orElse(null);
		if (existing != null) {
			bringToFront(existing);
			return;
		}
				
		// Build a new window
		var panelProps = (initProps != null) ? initProps.props() : new Properties();
		var panel = EntityContentFactory.getEntityPanel(entity, this, panelProps);
		if (panel != null) {
			var cw = new ContentWindow(this, panel);
			// Set internal frame listener
			cw.addInternalFrameListener(new EntityPanelListener());

			add(cw, 0);

			positionWindow(cw, initProps);

			// Add unit window to unit windows
			entityWindows.add(cw);

			// Create new unit button in tool bar if necessary
			mainWindow.createUnitButton(entity);

			cw.setVisible(true);

			// Correct window becomes selected
			bringToFront(cw);

			// Play sound
			// String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
			// if (soundFilePath != null && !soundFilePath.isEmpty() && soundPlayer != null) {
			// 	soundPlayer.playSound(soundFilePath);
			// }		
		}
	}

	/**
	 * Entity panel has been closed
	 * @param source Parent comntent window
	 */
	private void disposeEntityPanel(ContentWindow source) {
		if (source.getContent() instanceof EntityContentPanel panel) {
			Entity unit = panel.getEntity();
			if (unit != null) {
				mainWindow.disposeUnitButton(unit);
			}
		}
		entityWindows.remove(source);
		source.destroy();
	}

	/**  
     * Positions the given window on the desktop pane.  
     * <p>  
     * If {@code initProps} is provided and contains a valid position that is visible  
     * within the desktop pane, the window is placed at that position. Otherwise,  
     * the window is placed at a random location within the desktop pane.  
     *  
     * @param window    the window component to position  
     * @param initProps the initial window properties, which may specify a preferred position  
     */ 
	private void positionWindow(Component window, WindowSpec initProps) {
		Point newPosition = null;
		if (initProps != null) {
			newPosition = initProps.position();
			
			// Make sure store position is visible
			Dimension desktopSize = getSize();
			if ((newPosition.getX() >= desktopSize.getWidth())
					|| (newPosition.getY() >= desktopSize.getHeight())) {
				newPosition = null;
			}
		}
		if (newPosition == null) {
			newPosition = getRandomLocation((JInternalFrame)window);
		}
		// Put window in random position on desktop.
		window.setLocation(newPosition);
	}

	/**
	 * Bring an internal window to the front of the desktop
	 * @param tempWindow Window to bring to front
	 */
	private static void bringToFront(JInternalFrame tempWindow) {
		try {
			tempWindow.setSelected(true);
			tempWindow.moveToFront();
		} catch (java.beans.PropertyVetoException e) {
			// Window issue but can be ignored
		}
	}

	/**
	 * Update the desktop and all of its windows.
	 */
	private void updateWindows(ClockPulse pulse) {

		// Update all entity windows.
		for (var w : entityWindows) {
			if (w.isVisible())
				w.clockUpdate(pulse);
		}

		// Update all tool windows.
		for (var w : toolWindows) {
			if (w.isVisible())
				w.clockUpdate(pulse);
		}
	}

	private Point getCenterLocation(JInternalFrame tempWindow) {

		Dimension desktopSize = getSize();
		Dimension windowSize = tempWindow.getSize();

		int rX = (int) Math.round((desktopSize.width - windowSize.width) / 2D);
		int rY = (int) Math.round((desktopSize.height - windowSize.height - 100) / 2D);

		// Added rX checking
		if (rX < 0) {
			rX = 0;
		}

		// Make sure y position isn't < 0.
		if (rY < 0) {
			rY = 0;
		}

		return new Point(rX, rY);
	}

	/**
	 * Gets a random location on the desktop for a given {@link JInternalFrame}.
	 *
	 * @param tempWindow an internal window
	 * @return random point on the desktop
	 */
	private Point getRandomLocation(JInternalFrame tempWindow) {

		Dimension desktopSize = getSize();
		Dimension windowSize = tempWindow.getSize();

		// Populate windows in grid=like starting position
		int w = desktopSize.width - windowSize.width;
		int rX = RandomUtil.getRandomInt(w / 20) * 20;

		int rY = RandomUtil.getRandomInt(5) * 20;

		return new Point(rX, rY);
	}

	/**
	 * Gets a particular location on the desktop for a given {@link JInternalFrame}.
	 *
	 * @param f
	 * @param position
	 * @return a specific point on the desktop
	 */
	private Point computeLocation(JInternalFrame f, int positionX, int positionY) {
		Dimension desktopSize = getSize();
		Dimension windowSize = f.getSize();

		// Populate windows in grid=like starting position
		int w = desktopSize.width - windowSize.width;
		int h = desktopSize.height - windowSize.height;
		int rX = 0;
		int rY = 0;

		if (positionX == 0)
			rX = 0;
		else if (positionX == 1)
			rX = w / 2;
		else if (positionX == 2)
			rX = w;

		if (positionY == 0)
			rY = 0;
		else if (positionY == 1)
			rY = h / 2;
		else if (positionY == 2)
			rY = h;

		return new Point(rX, rY);
	}

	/**
	 * Gets the sound player used by the desktop.
	 *
	 * @return sound player.
	 */
	public AudioPlayer getSoundPlayer() {
		return soundPlayer;
	}

	/**
	 * Opens all initial windows based on UI configuration.
	 */
	public void openInitialWindows() {
		List<WindowSpec> startingWindows = mainWindow.getConfig().getConfiguredWindows();

		if (!startingWindows.isEmpty()) {
			for(WindowSpec w : startingWindows) {
				switch(w.type()) {
					case UIConfig.TOOL:
						openToolWindow(w.name());
					break;

					case UIConfig.UNIT:
						var u = EntityContentFactory.getEntity(sim, w.props());
						if (u != null) {
							openEntityPanel(u, w);
						}
					break;
					default:
						logger.warning("Unknown window type " + w.type() + " for window " + w.name());
				}
 			}
		}
		else {
			if (mode == GameMode.COMMAND) {
				// Open the time window for the Commander Mode
				openToolWindow(TimeTool.NAME);
				openToolWindow(CommanderWindow.NAME);
			}

			else {
				openToolWindow(MonitorWindow.NAME);
			}
		}
	}

	/**
	 * Gets a reference to the Simulation being displayed.
	 * 
	 * @return
	 */
	@Override
	public Simulation getSimulation() {
		return sim;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		updateWindows(pulse);
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Nothing to do
	}

	/**
	 * Gets UI properties of the Desktop.
	 */
	public Map<String, Properties> getUIProps() {
		Map<String, Properties> result = new HashMap<>();

		if (soundPlayer != null) {
			result.put(AudioPlayer.PROPS_NAME, soundPlayer.getUIProps());
		}

		// Add desktop properties
		Properties desktopProps = new Properties();
		desktopProps.setProperty(PRELOAD_TOOLS, Boolean.toString(preloadTools));
		result.put(DESKTOP_PROPS, desktopProps);
		return result;
    }

	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		
		mode = null;
		if (toolWindows != null) {
			toolWindows.forEach(w -> w.destroy());
			toolWindows = null;
		}
	}

	/**
	 * Prompts user to exit simulation.
	 */
	@Override
	public void requestEndSimulation() {
		if (!sim.isSavePending()) {
			int reply = JOptionPane.showConfirmDialog(getTopFrame(),
					"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION);
			if (reply == JOptionPane.YES_OPTION) {

				getTopFrame().setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

				mainWindow.exitSimulation();
			}
		}
	}
}
