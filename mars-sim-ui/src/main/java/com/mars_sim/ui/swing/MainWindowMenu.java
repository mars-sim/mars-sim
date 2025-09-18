/*
 * Mars Simulation Project
 * MainWindowMenu.java
 * @date 2025-09-18
 * @author Scott Davis
 */

package com.mars_sim.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.mars_sim.console.MarsTerminal;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.science.ScienceWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.TimeWindow;

/**
 * The MainWindowMenu class is the menu for the main window.
 */
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String EXIT = "exit";
	private static final String SAVE = "save";
	private static final String SAVE_AS = "saveAs";
	private static final String OPEN_GUIDE = "guide";
	private static final String TUTORIAL = "tutorial";
	private static final String ABOUT = "about";
	private static final String NEW = "new";
	private static final String LAF = "laf";
	private static final String UNIT_TOOLBAR = "unitbar";
	private static final String TOOL_TOOLBAR = "toolbar";
	private static final String EXTERNAL_BROWSER = "browser";
	private static final String EFFECT_UP = "effectUp";
	private static final String EFFECT_DOWN = "effectdown";
	private static final String EFFECT_MUTE = "effectmute";
	private static final String MUSIC_UP = "musicup";
	private static final String MUSIC_DOWN = "musicdown";
	private static final String MUSIC_MUTE = "musicmute";
	
	private static final String LOOK_AND_FEEL_ICON = "action/theme";
	private static final String BROWSER_ICON = "action/browser";
	
	private static final String VOL_UP_ICON = "action/vol_up";
	private static final String VOL_DOWN_ICON = "action/vol_down";
	
	// Data members
	/** The main window frame. */
	private MainWindow mainWindow;
	/** The audio player instance. */
	private AudioPlayer soundPlayer;
	/** Unit Bar menu item. */
	private JCheckBoxMenuItem showUnitBarItem;
	/** Tool Bar menu item. */
	private JCheckBoxMenuItem showToolBarItem;
	/** Tool Bar menu item. */
	private JCheckBoxMenuItem useExternalBrowser;


	/** Music mute menu item. */
	private JCheckBoxMenuItem musicMuteItem;
	/** Sound Effect mute menu item. */
	private JCheckBoxMenuItem effectMuteItem;
	/** Music volume slider menu item. */
	private JSlider musicVolumeSlider;
	/** Sound effect volume slider menu item. */
	private JSlider effectVolumeSlider;

	private JMenu toolsMenu;

	/**
	 * Constructor.
	 *
	 * @param mainWindow
	 *            the main window pane
	 */
	public MainWindowMenu(MainWindow mainWindow, MainDesktopPane desktop) {

		// Use JMenuBar constructor
		super();

		// Initialize data members
		this.mainWindow = mainWindow;

		// Create file menu
		JMenu fileMenu = new JMenu(Msg.getString("mainMenu.file")); //$NON-NLS-1$
		fileMenu.setMnemonic(KeyEvent.VK_F); // Alt + F
		add(fileMenu);

		fileMenu.add(createMenuItemAction("mainMenu.save", "action/save", SAVE, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(createMenuItemAction("mainMenu.saveAs", "action/saveAs", SAVE_AS, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK, false)));
		fileMenu.add(new JSeparator());
		fileMenu.add(createMenuItemAction("mainMenu.exit", "action/exit", EXIT, null,
									KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false)));

		// Create tools menu
		toolsMenu = createToolsMenu();
		add(toolsMenu);
		
		add(createSettingsMenu(desktop));

		// Add a dynamic Windows menu
		final JMenu displayMenu = new JMenu("Windows");
        displayMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent me) {
				buildWindowsMenu(me);
            }

            @Override
            public void menuDeselected(MenuEvent me) {
				// Nothing to do
            }

            @Override
            public void menuCanceled(MenuEvent me) {
				// Nothing to do
            }
        });
		add(displayMenu);

		// Create help menu
		add(createHelpMenu());
	}

	/**
	 * Dynamically build the windows menu showing Tools & Unit
	 * @param me
	 */
	protected void buildWindowsMenu(MenuEvent me) {
		JMenu menuSource = (JMenu) me.getSource();

		menuSource.removeAll();//remove previous opened window jmenuitems

		for (var tw : mainWindow.getDesktop().getToolWindows()) {
			if (!tw.isClosed()) {
				menuSource.add(createWindowControlMenu(tw.getTitle(), tw));
			}
		}
		for (var tw : mainWindow.getDesktop().getUnitWindows()) {
			menuSource.add(createWindowControlMenu(tw.getTitle(), tw));
		}

		menuSource.revalidate();
		menuSource.repaint();
		menuSource.doClick();
	}

	/**
	 * Create an internal window control menu
	 * @return
	 */
	private JMenu createWindowControlMenu(String title, JInternalFrame internal) {
		JMenu top = new JMenu(title);
		JMenuItem center = new JMenuItem("Center");
		center.addActionListener(e -> {
			mainWindow.getDesktop().centerJIF(internal);
			try {
				internal.setSelected(true);
			} catch (PropertyVetoException e1) {
				// ignore
			}
		});
		top.add(center);

		JMenuItem show = new JMenuItem("Front");
		show.addActionListener(e -> {
			try {
				internal.setSelected(true);
			} catch (PropertyVetoException e1) {
				// ignore
			}
		});
		top.add(show);

		JMenuItem close = new JMenuItem("Close");
		close.addActionListener(e -> {
			try {
				internal.setClosed(true);
			} catch (PropertyVetoException e1) {
				// Ignore
			}
		});
		top.add(close);
		return top;
	}

	/**
	 * Create the tools menu
	 */
	private JMenu createToolsMenu() {
		var newMenu = new JMenu(Msg.getString("mainMenu.tools")); //$NON-NLS-1$
		newMenu.addMenuListener(this);
		newMenu.setMnemonic(KeyEvent.VK_T);

		// Create tool menu items
		newMenu.add(createToolMenuItem(NavigatorWindow.NAME, NavigatorWindow.TITLE, NavigatorWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false)));
		newMenu.add(createToolMenuItem(SearchWindow.NAME, SearchWindow.TITLE, SearchWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false)));
		newMenu.add(createToolMenuItem(TimeWindow.NAME, TimeWindow.TITLE, TimeWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false)));								
		newMenu.add(createToolMenuItem(MonitorWindow.NAME, MonitorWindow.TITLE, MonitorWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false)));	
		newMenu.add(createToolMenuItem(MissionWindow.NAME, MissionWindow.TITLE, MissionWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false)));	
		newMenu.add(createToolMenuItem(SettlementWindow.NAME, SettlementWindow.TITLE, SettlementWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false)));	
		newMenu.add(createToolMenuItem(ScienceWindow.NAME, ScienceWindow.TITLE, ScienceWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false)));	
		newMenu.add(createToolMenuItem(ResupplyWindow.NAME, ResupplyWindow.TITLE, ResupplyWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false)));	
		newMenu.add(createToolMenuItem(CommanderWindow.NAME, CommanderWindow.TITLE, CommanderWindow.ICON,
										 KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false)));	
		return newMenu;
	}

	private JMenu createHelpMenu() {
		JMenu helpMenu = new JMenu(Msg.getString("mainMenu.help")); //$NON-NLS-1$
		helpMenu.setMnemonic(KeyEvent.VK_H); // Alt + H
		helpMenu.addMenuListener(this);

		// Create About Mars Simulation Project menu item
		helpMenu.add(createMenuItem("mainMenu.about", "action/about", ABOUT, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, false)));
	
		helpMenu.add(new JSeparator());
			
		helpMenu.add(createMenuItem("mainMenu.new", "action/new", NEW, null,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, false)));

		helpMenu.add(createMenuItem("mainMenu.tutorial", "action/tutorial", TUTORIAL, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK, false)));
		helpMenu.add(createMenuItem("mainMenu.guide", GuideWindow.HELP_ICON, OPEN_GUIDE, null,
										KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK, false)));
		return helpMenu;
	}

    /**
     * Opens the about dialog box.
     */
    public void openAboutDialog() {
    	JOptionPane.showMessageDialog(mainWindow, MarsTerminal.ABOUT_MSG, MarsTerminal.MARS_SIM, JOptionPane.INFORMATION_MESSAGE);
    }
    
	private JMenu createSettingsMenu(MainDesktopPane desktop) {

		// Create settings menu
		JMenu settingsMenu = new JMenu(Msg.getString("mainMenu.settings")); //$NON-NLS-1$
		settingsMenu.setMnemonic(KeyEvent.VK_S); // Alt + S
		settingsMenu.addMenuListener(this);

		// Create Show Unit Bar menu item
		showUnitBarItem = createCheckMenuItemAction(Msg.getString("mainMenu.unitbar"), null,
											  UNIT_TOOLBAR, "mainMenu.tooltip.unitbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK, false));
		settingsMenu.add(showUnitBarItem);
		showToolBarItem = createCheckMenuItemAction(Msg.getString("mainMenu.toolbar"), null,
											  TOOL_TOOLBAR, "mainMenu.tooltip.toolbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK, false));
		settingsMenu.add(showToolBarItem);	
		useExternalBrowser = createCheckMenuItemAction("Use Desktop Browser", BROWSER_ICON,
					EXTERNAL_BROWSER, "mainMenu.tooltip.toolbar"	, null);
		settingsMenu.add(useExternalBrowser);	
		settingsMenu.add(new JSeparator());

		// Create submenu for LAF
		JMenu lafMenu = new JMenu("Look and Feel");
		Icon lafIcon = ImageLoader.getIconByName(LOOK_AND_FEEL_ICON);
		lafMenu.setIcon(lafIcon);
		JMenu lightMenu = new JMenu("Light Theme");
		JMenu darkMenu = new JMenu("Dark Theme");
		ButtonGroup group = new ButtonGroup();
		settingsMenu.add(lafMenu);
		lafMenu.add(lightMenu);
		lafMenu.add(darkMenu);

		for (String i : StyleManager.getAvailableLightLAF()) {
			JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(i);
			if (i.equals(StyleManager.getLAF()))
				lafItem.setSelected(true);
			lafItem.setActionCommand(LAF);
			lafItem.addActionListener(this);
			lightMenu.add(lafItem);
			group.add(lafItem);
		}
		for (String i : StyleManager.getAvailableDarkLAF()) {
			JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(i);
			if (i.equals(StyleManager.getLAF()))
				lafItem.setSelected(true);
			lafItem.setActionCommand(LAF);
			lafItem.addActionListener(this);
			darkMenu.add(lafItem);
			group.add(lafItem);
		}
		for (String i : StyleManager.getAvailableSystemLAF()) {
			JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(i);
			if (i.equals(StyleManager.getLAF()))
				lafItem.setSelected(true);
			lafItem.setActionCommand(LAF);
			lafItem.addActionListener(this);
			lafMenu.add(lafItem);
			group.add(lafItem);
		}

		// Create Background Music Volume slider menu item
		soundPlayer = desktop.getSoundPlayer();
		
		// Note: if "-nosound" argument is given when starting mars-sim
		// then the following sound control won't be shown under settings
		if (soundPlayer != null) {
			double musicVolume = soundPlayer.getMusicVolume();
			int intMusicVolume = (int) Math.round(musicVolume * 10.0);

			// Create Background Music Volume Slider
			musicVolumeSlider = new JSliderMW(SwingConstants.HORIZONTAL, 0, 10, intMusicVolume); // $NON-NLS-1$
			musicVolumeSlider.setPreferredSize(new Dimension(20, 100));
			musicVolumeSlider.setSize(new Dimension(20, 100));
			musicVolumeSlider.setMajorTickSpacing(2);
			musicVolumeSlider.setPaintTicks(true);
			musicVolumeSlider.setPaintLabels(true);
			musicVolumeSlider.setPaintTrack(true);
			musicVolumeSlider.setSnapToTicks(true);
			musicVolumeSlider.setToolTipText(Msg.getString("mainMenu.tooltip.musicVolumeSlider")); //$NON-NLS-1$
			musicVolumeSlider.addChangeListener(e -> {
				float newVolume = musicVolumeSlider.getValue() / 10F;
				soundPlayer.setMusicVolume(newVolume);
			});
			
			// Create submenu for music volume
			// Note: Unable to show musicVolumeSlider 
//			JMenuItem musicMenu = new JMenuItem("Music Volume");
//			musicMenu.add(musicVolumeSlider);
//			settingsMenu.add(musicMenu);

			settingsMenu.add(createMenuItemAction("mainMenu.musicVolumeUp", VOL_UP_ICON,
							MUSIC_UP, "mainMenu.musicVolumeUp",
							KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_DOWN_MASK, false)));
			settingsMenu.add(createMenuItemAction("mainMenu.musicVolumeDown", VOL_DOWN_ICON,
							MUSIC_DOWN, "mainMenu.musicVolumeDown",
							KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_DOWN_MASK, false)));
			musicMuteItem = createCheckMenuItemAction(Msg.getString("mainMenu.muteMusic"), null,
												MUSIC_MUTE, "mainMenu.muteMusic",
												KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK, false));
			settingsMenu.add(musicMuteItem);

			// Create Sound Effect Volume Slider
			double soundEffectVolume = soundPlayer.getMusicVolume();
			int intSoundEffectVolume = (int) Math.round(soundEffectVolume * 10.0);
			
			effectVolumeSlider = new JSliderMW(SwingConstants.HORIZONTAL, 0, 10, intSoundEffectVolume); // $NON-NLS-1$
			effectVolumeSlider.setPreferredSize(new Dimension(20, 100));
			effectVolumeSlider.setSize(new Dimension(20, 100));
			effectVolumeSlider.setMajorTickSpacing(2);
			effectVolumeSlider.setPaintTicks(true);
			effectVolumeSlider.setPaintLabels(true);
			effectVolumeSlider.setPaintTrack(true);
			effectVolumeSlider.setSnapToTicks(true);

			effectVolumeSlider.setToolTipText(Msg.getString("mainMenu.tooltip.effectVolumeSlider")); //$NON-NLS-1$
			effectVolumeSlider.addChangeListener(e -> {
				float newVolume = effectVolumeSlider.getValue() / 10F;
				soundPlayer.setSoundVolume(newVolume);
			});
			
			// Create submenu for sound effect volume
			// Note: Unable to show effectVolumeSlider 
//			JMenuItem soundEffectMenu = new JMenuItem("Sound Effect Volume");
//			soundEffectMenu.add(effectVolumeSlider);
//			settingsMenu.add(soundEffectMenu);

			settingsMenu.add(createMenuItemAction("mainMenu.effectVolumeUp", VOL_UP_ICON,
											EFFECT_UP, "mainMenu.effectVolumeUp",
											KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.CTRL_DOWN_MASK, false)));
			settingsMenu.add(createMenuItemAction("mainMenu.effectVolumeDown", VOL_DOWN_ICON,
											EFFECT_DOWN, "mainMenu.effectVolumeDown",
											KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, KeyEvent.CTRL_DOWN_MASK, false)));
			effectMuteItem = createCheckMenuItemAction(Msg.getString("mainMenu.muteEffect"), null,
												EFFECT_MUTE, "mainMenu.muteEffect",
												KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK, false));
			settingsMenu.add(effectMuteItem);
		}

		return settingsMenu;
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
		JMenuItem item = createMenuItem(nameKey);
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
		JMenuItem item = createMenuItem(nameKey);
		actionMenuItem(item, icon, command, tooltipKey, keyStroke);
		return item;
	}

	/**
	 * Creates a menu item.
	 * 
	 * @param nameKey
	 * @return
	 */
	private JMenuItem createMenuItem(String nameKey) {
		return new JMenuItem(Msg.getString(nameKey)); //$NON-NLS-1$
	}
	
	/**
	 * Creates a check box menu item with action.
	 * 
	 * @param name
	 * @param iconName
	 * @param command
	 * @param tooltipKey
	 * @param keyStroke
	 * @return
	 */
	private JCheckBoxMenuItem createCheckMenuItemAction(String name, String iconName, String command, String tooltipKey,
										KeyStroke keyStroke) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
		actionMenuItem(item, iconName, command, tooltipKey, keyStroke);
		return item;
	}

	private void actionMenuItem(JMenuItem item, String iconName, String command, String tooltipKey,
								KeyStroke keyStroke) {
		configureMenuItem(item, iconName, tooltipKey, keyStroke);
		item.addActionListener(this);
		item.setActionCommand(command);
	}

	/**
	 * Create a menu items for the Tool window
	 * @param name
	 * @param title
	 * @param iconName
	 * @param keyStroke
	 * @return
	 */
	private JCheckBoxMenuItem createToolMenuItem(String name, String title, String iconName, KeyStroke keyStroke) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(title);
		configureMenuItem(item, iconName, null, keyStroke);
		item.setActionCommand(name);
		item.addActionListener(e -> {
			JMenuItem selectedItem = (JMenuItem) e.getSource();
			if (selectedItem.isSelected())
				mainWindow.getDesktop().openToolWindow(e.getActionCommand());
			else
				mainWindow.getDesktop().closeToolWindow(e.getActionCommand());
		});

		return item;
	}

	private void configureMenuItem(JMenuItem item, String iconName,  String tooltipKey,
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
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/** ActionListener method overriding. */
	@Override
	public final void actionPerformed(ActionEvent event) {
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		MainDesktopPane desktop = mainWindow.getDesktop();
		String command = event.getActionCommand();
		String newGuideURL = null;

		switch(command) {
			case EXIT:
				mainWindow.exitSimulation();
				break;
			case SAVE:
				mainWindow.saveSimulation(true);
				break;
			case SAVE_AS:
				mainWindow.saveSimulation(false);
				break;
			case UNIT_TOOLBAR:
				desktop.getMainWindow().getUnitToolBar().setVisible(selectedItem.isSelected());
				break;
			case TOOL_TOOLBAR:
				desktop.getMainWindow().getToolToolBar().setVisible(selectedItem.isSelected());
				break;
			case EXTERNAL_BROWSER:
				desktop.getMainWindow().setExternalBrowser(selectedItem.isSelected());
				break;
			case MUSIC_UP: {
				int newVolume = musicVolumeSlider.getValue() + 1;
				if (newVolume <= 10) {
					soundPlayer.musicVolumeUp();
					musicVolumeSlider.setValue(newVolume);
				}
				else if (newVolume <= 0)
					musicMuteItem.setSelected(true);
				} break;
			case MUSIC_DOWN: {
				int newVolume = musicVolumeSlider.getValue() - 1;
				if (newVolume >= 0) {
					soundPlayer.musicVolumeDown();
					musicVolumeSlider.setValue(newVolume);
				}
				else
					musicMuteItem.setSelected(true);
				} break;
			case EFFECT_UP: {
				int newVolume = effectVolumeSlider.getValue() + 1;
				if (newVolume <= 10) {
					soundPlayer.soundVolumeUp();
					effectVolumeSlider.setValue(newVolume);
				}
			} break;
			case EFFECT_DOWN:{
				int newVolume = effectVolumeSlider.getValue() - 1;
				if (newVolume >= 0) {
					soundPlayer.soundVolumeDown();
					effectVolumeSlider.setValue(newVolume);
				}
			} break;
			case MUSIC_MUTE:
				if (selectedItem.isSelected()) {
					// mute the music
					soundPlayer.setUserMuteMusic(true);
					musicVolumeSlider.setEnabled(false);
					musicMuteItem.revalidate();
					musicMuteItem.repaint();
				}
				else {
					// unmute the music
					soundPlayer.setUserMuteMusic(false);
					if (!Simulation.instance().getMasterClock().isPaused()) {
//						soundPlayer.unmuteMusic();
						soundPlayer.resumeMusic();
					}
					musicVolumeSlider.setEnabled(true);
					musicMuteItem.revalidate();
					musicMuteItem.repaint();
				}
				break;
			case EFFECT_MUTE:
				if (selectedItem.isSelected()) {
					// mute the sound effect
					soundPlayer.setUserMuteSoundEffect(true);
					soundPlayer.muteSoundEffect();
					effectVolumeSlider.setEnabled(false);
				} else {
					// player unmute the sound effect
					soundPlayer.setUserMuteSoundEffect(false);
					if (!Simulation.instance().getMasterClock().isPaused()) {
						soundPlayer.unmuteSoundEffect();
					}
					effectVolumeSlider.setEnabled(true);
				}
				break;
			case ABOUT:
				newGuideURL = Msg.getString("doc.about"); //$NON-NLS-1$
				openAboutDialog();		
				break;
			case NEW:
				newGuideURL = Msg.getString("doc.whatsnew"); //$NON-NLS-1$
				break;
			case OPEN_GUIDE:
				newGuideURL = Msg.getString("doc.guide"); //$NON-NLS-1$
				break;
			case TUTORIAL:
				newGuideURL = Msg.getString("doc.tutorial"); //$NON-NLS-1$
				break;
			case LAF: 
				String text = selectedItem.getText();
				mainWindow.updateLAF(text);
				break;
			default:
				// Shouldn't be here
				break;
		}

		if (newGuideURL != null) {
			mainWindow.showHelp(newGuideURL);
		}
	}

	/** 
	 * MenuListener method overriding. 
	 */
	@Override
	public final void menuSelected(MenuEvent event) {
		MainDesktopPane desktop = mainWindow.getDesktop();
		for(Component c : toolsMenu.getComponents()) {
			if (c instanceof JCheckBoxMenuItem) {
				JCheckBoxMenuItem jc = (JCheckBoxMenuItem) c;
				jc.setSelected(desktop.isToolWindowOpen(jc.getActionCommand()));
			}
		}
	
		showUnitBarItem.setSelected(mainWindow.getUnitToolBar().isVisible());
		showToolBarItem.setSelected(mainWindow.getToolToolBar().isVisible());
		useExternalBrowser.setSelected(mainWindow.useExternalBrowser());

		if (soundPlayer != null) {
			musicVolumeSlider.setValue((int) Math.round(soundPlayer.getMusicVolume() * 10));
			musicVolumeSlider.setEnabled(!soundPlayer.userMuteMusic());

			effectVolumeSlider.setValue((int) Math.round(soundPlayer.getEffectVolume() * 10));
			effectVolumeSlider.setEnabled(!soundPlayer.userMuteSoundEffect());

			musicMuteItem.setSelected(soundPlayer.userMuteMusic());
			effectMuteItem.setSelected(soundPlayer.userMuteSoundEffect());
		}
	}

	@Override
	public void menuCanceled(MenuEvent event) {
	}

	@Override
	public void menuDeselected(MenuEvent event) {
	}
}
