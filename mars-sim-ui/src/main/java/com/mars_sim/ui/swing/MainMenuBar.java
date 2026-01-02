/*
 * Mars Simulation Project
 * MainMenuBar.java
 * @date 2025-12-31
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.stream.Collectors;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.StyleManager.StyleEntry;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.TimeTool;
import com.mars_sim.ui.swing.utils.SaveDialog;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This is a menu bar holding the common menu items for the Mars Simulation Project UI.
 */
public class MainMenuBar extends JMenuBar implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String EXIT = "exit";
	private static final String SAVE = "save";
	private static final String SAVE_AS = "saveAs";
	private static final String OPEN_GUIDE = "guide";
	private static final String TUTORIAL = "tutorial";
	private static final String ABOUT = "about";
	private static final String VERSION = "version";
	private static final String CHANGELOG = "changelog";
	private static final String LAF = "laf";
	private static final String LOOK_AND_FEEL_ICON = "action/theme";

	private UIContext context;

	/**
	 * Constructor.
	 *
	 * @param context The UI hosting the menu bar.
	 */
	public MainMenuBar(UIContext context) {

		// Initialize data members
		this.context = context;

		// Create file menu
		JMenu fileMenu = new JMenu(Msg.getString("mainMenu.file")); //$NON-NLS-1$
		fileMenu.setMnemonic(KeyEvent.VK_F); // Alt + F
		add(fileMenu);

		fileMenu.add(createMenuItemAction("mainMenu.save", "action/save", SAVE, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(createMenuItemAction("mainMenu.saveAs", "action/saveAs", SAVE_AS, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(new JSeparator());
		fileMenu.add(createMenuItemAction("mainMenu.exit", "action/exit", EXIT, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK, false)));

		// Create tools menu
		var toolsMenu = createToolsMenu();
		add(toolsMenu);
		
		add(createSettingsMenu());

		// Add a dynamic Windows menu
		JMenu displayMenu = createDisplayMenu();
		if (displayMenu != null) {
			add(displayMenu);
		}	

		// Create help menu
		add(createHelpMenu());
	}

	/**
	 * Create a JMenu that can control the windows displayed. This is window system specifc.
	 * @return Return null mean no menu is displayed.
	 */
	protected JMenu createDisplayMenu() {
		// By default no display menu
		return null;
	}

	/**
	 * Creates the tools menu.
	 */
	private JMenu createToolsMenu() {
		var newMenu = new JMenu(Msg.getString("mainMenu.tools")); //$NON-NLS-1$
		newMenu.setMnemonic(KeyEvent.VK_T);

		// Create tool menu items
		newMenu.add(createToolMenuItem(NavigatorWindow.NAME, NavigatorWindow.TITLE, NavigatorWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false)));
		newMenu.add(createToolMenuItem(SearchWindow.NAME, SearchWindow.TITLE, SearchWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false)));
		newMenu.add(createToolMenuItem(TimeTool.NAME, TimeTool.TITLE, TimeTool.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false)));								
		newMenu.add(createToolMenuItem(MonitorWindow.NAME, MonitorWindow.TITLE, MonitorWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false)));	
		newMenu.add(createToolMenuItem(MissionWindow.NAME, MissionWindow.TITLE, MissionWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false)));	
		newMenu.add(createToolMenuItem(SettlementWindow.NAME, SettlementWindow.TITLE, SettlementWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false)));	
		newMenu.add(createToolMenuItem(ResupplyWindow.NAME, ResupplyWindow.TITLE, ResupplyWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false)));	
		newMenu.add(createToolMenuItem(CommanderWindow.NAME, CommanderWindow.TITLE, CommanderWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false)));	
		return newMenu;
	}

	/**
	 * Creates the help menu.
	 * 
	 * @return
	 */
	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu(Msg.getString("mainMenu.help")); //$NON-NLS-1$
		helpMenu.setMnemonic(KeyEvent.VK_H); // Alt + H

		// Create About Mars Simulation Project menu item
		helpMenu.add(createMenuItem("mainMenu.about", "action/about", ABOUT, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false)));
	
		helpMenu.add(new JSeparator());
			
		helpMenu.add(createMenuItem("mainMenu.changelog", "action/changelog", CHANGELOG, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, false)));

		helpMenu.add(createMenuItem("mainMenu.version", "action/version", VERSION, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, false)));
				
		helpMenu.add(createMenuItem("mainMenu.tutorial", "action/tutorial", TUTORIAL, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, false)));
		helpMenu.add(createMenuItem("mainMenu.guide", GuideWindow.HELP_ICON, OPEN_GUIDE, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK, false)));
		return helpMenu;
	}


	private JMenu createSettingsMenu() {

		// Create settings menu
		JMenu settingsMenu = new JMenu(Msg.getString("mainMenu.settings")); //$NON-NLS-1$
		settingsMenu.setMnemonic(KeyEvent.VK_S); // Alt + S

		addWindowPreferences(settingsMenu);

		var currentLAF = StyleManager.getLAF();
		var lafGroups = StyleManager.STYLE_ENTRIES.stream()
				.collect(Collectors.groupingBy(StyleEntry::category));

		// Create submenu for LAF
		JMenu lafMenu = new JMenu("Look and Feel");
		lafMenu.setIcon(ImageLoader.getIconByName(LOOK_AND_FEEL_ICON));
		ButtonGroup group = new ButtonGroup();
		settingsMenu.add(lafMenu);

		for(var g : lafGroups.entrySet()) {
			JMenu subMenu = new JMenu(g.getKey());
			lafMenu.add(subMenu);
			for(var entry : g.getValue()) {
				JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(entry.name());
				lafItem.setSelected(entry.name().equals(currentLAF));
				lafItem.setActionCommand(LAF);
				lafItem.addActionListener(this);
				subMenu.add(lafItem);
				group.add(lafItem);
			}
		}
		
		return settingsMenu;
	}

	/**
	 * Add any window specific preferences to the settings menu. This should be overriden.
	 * @param settingsMenu The parent menu to add to.
	 */
	protected void addWindowPreferences(JMenu settingsMenu) {
		// Add no extra preferences by default
	}

	/**
	 * Creates a menu item with action.
	 * 
	 * @param nameKey
	 * @param iconName
	 * @param command
	 * @param tooltipKey
	 * @param keyStroke
	 * @return
	 */
	private JMenuItem createMenuItemAction(String nameKey, String iconName, String command, String tooltipKey, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(Msg.getString(nameKey));
		actionMenuItem(item, iconName, command, tooltipKey, keyStroke);
		return item;
	}
	
	/**
	 * Creates a menu item with action.
	 * 
	 * @param nameKey
	 * @param icon
	 * @param command
	 * @param tooltipKey
	 * @param keyStroke
	 * @return
	 */
	private JMenuItem createMenuItem(String nameKey, String icon, String command, String tooltipKey, KeyStroke keyStroke) {
		JMenuItem item = new JMenuItem(Msg.getString(nameKey));
		actionMenuItem(item, icon, command, tooltipKey, keyStroke);
		return item;
	}


	private void actionMenuItem(JMenuItem item, String iconName, String command, String tooltipKey,
								KeyStroke keyStroke) {
		configureMenuItem(item, iconName, tooltipKey, keyStroke);
		item.addActionListener(this);
		item.setActionCommand(command);
	}

	/**
	 * Creates a menu items for the Tool window.
	 * 
	 * @param name
	 * @param title
	 * @param iconName
	 * @param keyStroke
	 * @return
	 */
	private JMenuItem createToolMenuItem(String name, String title, String iconName, KeyStroke keyStroke) {
		var item = new JMenuItem(title);
		configureMenuItem(item, iconName, null, keyStroke);
		item.setActionCommand(name);
		item.addActionListener(e -> context.openToolWindow(e.getActionCommand()));

		return item;
	}

	/**
	 * Configures a menu item.
	 * 
	 * @param item
	 * @param iconName
	 * @param tooltipKey
	 * @param keyStroke
	 */
	protected static void configureMenuItem(JMenuItem item, String iconName,  String tooltipKey,
								KeyStroke keyStroke) {
		if (iconName != null) {
			Icon icon = ImageLoader.getIconByName(iconName);
			item.setIcon(icon);
		}
		if (tooltipKey != null) {
			item.setToolTipText(Msg.getString(tooltipKey)); //$NON-NLS-1$
		}

		item.setAccelerator(keyStroke);
	}
	
	/**
	 * Gets the Main Window.
	 *
	 * @return mainWindow
	 */
	protected UIContext getContext() {
		return context;
	}

	/** ActionListener method overriding. */
	@Override
	public final void actionPerformed(ActionEvent event) {
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		var top = context.getTopFrame();
		String command = event.getActionCommand();
		String newGuideURL = null;

		switch(command) {
			case EXIT:
				context.requestEndSimulation();
				break;
			case SAVE:
				SaveDialog.create(top, context.getSimulation(),true);
				break;
			case SAVE_AS:
				SaveDialog.create(top, context.getSimulation(),false);
				break;
			case ABOUT:
				newGuideURL = Msg.getString("doc.about"); //$NON-NLS-1$
				break;
			case CHANGELOG:
				newGuideURL = Msg.getString("doc.versionHistory"); //$NON-NLS-1$
				break;
			case VERSION:
				checkVersion();		
				break;
			case OPEN_GUIDE:
				newGuideURL = Msg.getString("doc.guide"); //$NON-NLS-1$
				break;
			case TUTORIAL:
				newGuideURL = Msg.getString("doc.tutorial"); //$NON-NLS-1$
				break;
			case LAF: 
				String newStyle = selectedItem.getText();
				if (StyleManager.setLAF(newStyle)) {
					SwingUtilities.updateComponentTreeUI(top);
				}
				break;
			default:
				// Shouldn't be here
				break;
		}

		if (newGuideURL != null) {
			SwingHelper.openBrowser(newGuideURL);
		}
	}

    /**
     * Opens browser to check latest version.
     */
    private void checkVersion() {
		var message = 
			"Version: " + SimulationRuntime.VERSION.getVersionTag() + "\n"
			+ "Base: " + SimulationRuntime.VERSION.getBuild() + "\n"
			+ "Built On: " + SimulationRuntime.VERSION.getBuildTime();
    	JOptionPane.showMessageDialog(context.getTopFrame(), message,
						SimulationRuntime.SHORT_TITLE, JOptionPane.INFORMATION_MESSAGE);
    }
}
