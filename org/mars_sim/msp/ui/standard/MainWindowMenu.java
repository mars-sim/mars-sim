/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 2.83 2008-02-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.mars_sim.msp.ui.standard.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.standard.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.standard.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.standard.tool.search.SearchWindow;
import org.mars_sim.msp.ui.standard.tool.sound.SoundWindow;
import org.mars_sim.msp.ui.standard.tool.time.TimeWindow;
import org.mars_sim.msp.ui.standard.tool.about.AboutWindow;
import org.mars_sim.msp.ui.standard.tool.guide.GuideWindow;

/** The MainWindowMenu class is the menu for the main window.
 */
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

    // Data members
    private MainWindow mainWindow;                // The main window frame
    private JMenuItem loadItem;                   // Load menu item
    private JMenuItem saveItem;                   // Save menu item
    private JMenuItem saveAsItem;                 // Save As menu item
    private JMenuItem exitItem;                   // Exit menu item
    private JMenuItem newItem;                    // New menu item
    private JCheckBoxMenuItem marsNavigatorItem;  // Mars navigator menu item
    private JCheckBoxMenuItem searchToolItem;     // Search tool menu item
    private JCheckBoxMenuItem timeToolItem;       // Time tool menu item
    private JCheckBoxMenuItem monitorToolItem;    // Monitor tool menu item
    private JCheckBoxMenuItem soundToolItem;      // Sound tool menu item
    private JCheckBoxMenuItem missionToolItem;    // Mission tool menu item
    private JCheckBoxMenuItem lookAndFeelItem;    // Look and feel menu item
    private JCheckBoxMenuItem aboutMspItem;       // About Mars Simulation Project menu item
    private JCheckBoxMenuItem guideItem;          // User Guide menu item

    /** 
     * Constructor
     * @param mainWindow the main window pane
     */
    public MainWindowMenu(MainWindow mainWindow) {

        // Use JMenuBar constructor
        super();

        // Initialize data members
        this.mainWindow = mainWindow;

        // Create file menu
        JMenu fileMenu = new JMenu("File");
        add(fileMenu);

        // Create new menu item
        newItem = new JMenuItem("New");
        newItem.addActionListener(this);
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, false));
        fileMenu.add(newItem);

        // Create load menu item
        loadItem = new JMenuItem("Load");
        loadItem.addActionListener(this);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK, false));
        fileMenu.add(loadItem);

        fileMenu.add(new JSeparator());

        // Create save menu item
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false));
        fileMenu.add(saveItem);

        // Create save as menu item
        saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(this);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, false));
        fileMenu.add(saveAsItem);

        fileMenu.add(new JSeparator());

        // Create exit menu item
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false));
        fileMenu.add(exitItem);

        // Create tools menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.addMenuListener(this);
        add(toolsMenu);

        // Create Mars navigator menu item
        marsNavigatorItem = new JCheckBoxMenuItem(NavigatorWindow.NAME);
        marsNavigatorItem.addActionListener(this);
        marsNavigatorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false));
        toolsMenu.add(marsNavigatorItem);

        // Create search tool menu item
        searchToolItem = new JCheckBoxMenuItem(SearchWindow.NAME);
        searchToolItem.addActionListener(this);
        searchToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false));
        toolsMenu.add(searchToolItem);

        // Create time tool menu item
        timeToolItem = new JCheckBoxMenuItem(TimeWindow.NAME);
        timeToolItem.addActionListener(this);
        timeToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false));
        toolsMenu.add(timeToolItem);

        // Create monitor tool menu item
        monitorToolItem = new JCheckBoxMenuItem(MonitorWindow.NAME);
        monitorToolItem.addActionListener(this);
        monitorToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false));
        toolsMenu.add(monitorToolItem);

        // Create sound tool menu item
        soundToolItem = new JCheckBoxMenuItem(SoundWindow.NAME);
        soundToolItem.addActionListener(this);
        soundToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
        toolsMenu.add(soundToolItem);
        
        // Create mission tool menu item
        missionToolItem = new JCheckBoxMenuItem(MissionWindow.NAME);
        missionToolItem.addActionListener(this);
        missionToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false));
        toolsMenu.add(missionToolItem);
        
		// Create option menu
		JMenu optionMenu = new JMenu("Option");
		add(optionMenu);
		
		// Create look and feel menu item
		boolean nativeLookAndFeel = UIConfig.INSTANCE.useNativeLookAndFeel();
		if (UIConfig.INSTANCE.useUIDefault()) nativeLookAndFeel = false;
		lookAndFeelItem = new JCheckBoxMenuItem("Native Look & Feel", nativeLookAndFeel);
		lookAndFeelItem.addActionListener(this);
		optionMenu.add(lookAndFeelItem);

        // Create help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addMenuListener(this);
        add(helpMenu);

        // Create about Mars Simulation Project menu item
        aboutMspItem = new JCheckBoxMenuItem("About The Mars Simulation Project");
        aboutMspItem.addActionListener(this);
        helpMenu.add(aboutMspItem);

        helpMenu.add(new JSeparator());

        // Create User Guide menu item
        guideItem = new JCheckBoxMenuItem("User Guide");
        guideItem.addActionListener(this);
        guideItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false));
        helpMenu.add(guideItem);

    }

    // ActionListener method overriding
    public void actionPerformed(ActionEvent event) {

        JMenuItem selectedItem = (JMenuItem) event.getSource();

        if (selectedItem == exitItem) mainWindow.exitSimulation();
        else if (selectedItem == newItem) mainWindow.newSimulation();
        else if (selectedItem == saveItem) mainWindow.saveSimulation(true);
        else if (selectedItem == saveAsItem) mainWindow.saveSimulation(false);
        else if (selectedItem == loadItem) mainWindow.loadSimulation();
        else if (selectedItem == lookAndFeelItem) 
        	mainWindow.setLookAndFeel(lookAndFeelItem.isSelected());

        if (selectedItem == marsNavigatorItem) {
            if (marsNavigatorItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(NavigatorWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(NavigatorWindow.NAME);
        }

        if (selectedItem == searchToolItem) {
            if (searchToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(SearchWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(SearchWindow.NAME);
        }

        if (selectedItem == timeToolItem) {
            if (timeToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(TimeWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(TimeWindow.NAME);
        }

        if (selectedItem == monitorToolItem) {
            if (monitorToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(MonitorWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(MonitorWindow.NAME);
        }
        
        if (selectedItem == soundToolItem) {
            if (soundToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(SoundWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(SoundWindow.NAME);
        }
        
        if (selectedItem == missionToolItem) {
            if (missionToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(MissionWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(MissionWindow.NAME);
        }

        if (selectedItem == aboutMspItem) {
            if (aboutMspItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(AboutWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(AboutWindow.NAME);
        }

        if (selectedItem == guideItem) {
            if (guideItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow(GuideWindow.NAME);
            else mainWindow.getDesktop().closeToolWindow(GuideWindow.NAME);
        }
    }

    // MenuListener method overriding
    public void menuSelected(MenuEvent event) {
        marsNavigatorItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen(NavigatorWindow.NAME));
        searchToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen(SearchWindow.NAME));
        timeToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen(TimeWindow.NAME));
        monitorToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen(MonitorWindow.NAME));
        soundToolItem.setSelected(
        	mainWindow.getDesktop().isToolWindowOpen(SoundWindow.NAME));
        missionToolItem.setSelected(
        	mainWindow.getDesktop().isToolWindowOpen(MissionWindow.NAME));
        aboutMspItem.setSelected(
        	mainWindow.getDesktop().isToolWindowOpen(AboutWindow.NAME));
    }

    public void menuCanceled(MenuEvent event) {}
    public void menuDeselected(MenuEvent event) {}
}