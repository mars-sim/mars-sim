/*
 * Mars Simulation Project
 * MainWindowMenu.java
 * @date 2025-10-24
 * @author Scott Davis
 */

package com.mars_sim.ui.swing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.mars_sim.core.tool.Msg;

/**
 * The MainWindowMenu class is the menu for the main window.
 */
public class MainWindowMenu extends MainMenuBar {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String BROWSER_ICON = "action/browser";


	/**
	 * Constructor.
	 *
	 * @param mainWindow
	 *            the main window pane
	 */
	public MainWindowMenu(MainDesktopPane mainWindow) {
		super(mainWindow);
	}

	@Override
	protected JMenu createDisplayMenu() {
		
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
		
		return displayMenu;
	}

	/**
	 * Dynamically builds the windows menu showing Tools & Unit.
	 * 
	 * @param me
	 */
	private void buildWindowsMenu(MenuEvent me) {
		JMenu menuSource = (JMenu) me.getSource();

		menuSource.removeAll();//remove previous opened window jmenuitems
		var desktop = (MainDesktopPane)getContext();

		for (var tw : desktop.getToolWindows()) {
			if (!tw.isClosed()) {
				menuSource.add(createWindowControlMenu(desktop, tw.getTitle(), tw));
			}
		}
		for (var tw : desktop.getUnitWindows()) {
			menuSource.add(createWindowControlMenu(desktop, tw.getTitle(), tw));
		}

		menuSource.revalidate();
		menuSource.repaint();
		menuSource.doClick();
	}

	/**
	 * Creates an internal window control menu.
	 * 
	 * @return
	 */
	private JMenu createWindowControlMenu(MainDesktopPane desktop, String title, JInternalFrame internal) {
		JMenu top = new JMenu(title);
		JMenuItem center = new JMenuItem("Center");
		center.addActionListener(e -> {
			desktop.centerJIF(internal);
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
	 * Creates a check box menu item with action.
	 * 
	 * @param name
	 * @param iconName
	 * @param tooltipKey
	 * @param keyStroke
	 * @return
	 */
	private JCheckBoxMenuItem createCheckMenuItemAction(String name, String iconName, String tooltipKey,
										KeyStroke keyStroke) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
		configureMenuItem(item, iconName, tooltipKey, keyStroke);
		return item;
	}

	private MainWindow getMainWindow() {
		return ((MainDesktopPane)getContext()).getMainWindow();
	}

	@Override
	protected void addWindowPreferences(JMenu settingsMenu) {
				// Create Show Unit Bar menu item
		var showUnitBarItem = createCheckMenuItemAction(Msg.getString("mainMenu.unitbar"), null,"mainMenu.tooltip.unitbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK, false));
		showUnitBarItem.addActionListener(e ->
					getMainWindow().getUnitToolBar().setVisible(((JMenuItem)e.getSource()).isSelected()));
		settingsMenu.add(showUnitBarItem);
		
		var showToolBarItem = createCheckMenuItemAction(Msg.getString("mainMenu.toolbar"), null,
											   "mainMenu.tooltip.toolbar"	,
										      KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK, false));
		showToolBarItem.addActionListener(e ->
					getMainWindow().getToolToolBar().setVisible(((JMenuItem)e.getSource()).isSelected()));
		settingsMenu.add(showToolBarItem);

		var useExternalBrowser = createCheckMenuItemAction("Use Desktop Browser", BROWSER_ICON,
					 "mainMenu.tooltip.toolbar"	, null);
		useExternalBrowser.addActionListener(e ->
					getMainWindow().setExternalBrowser(((JMenuItem)e.getSource()).isSelected()));
		settingsMenu.add(useExternalBrowser);	
		settingsMenu.add(new JSeparator());
	}
}
