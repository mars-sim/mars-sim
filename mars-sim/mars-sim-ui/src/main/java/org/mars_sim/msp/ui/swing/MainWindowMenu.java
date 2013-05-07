/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 3.04 2012-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.preferences.PreferencesWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/** The MainWindowMenu class is the menu for the main window.
 */
public class MainWindowMenu extends JMenuBar implements ActionListener, MenuListener {

    // Data members
    private MainWindow mainWindow;                // The main window frame
    private JMenuItem newItem;                    // New menu item
    private JMenuItem loadItem;                   // Load menu item
    private JMenuItem saveItem;                   // Save menu item
    private JMenuItem saveAsItem;                 // Save As menu item
    private JMenuItem exitItem;                   // Exit menu item
    private JCheckBoxMenuItem marsNavigatorItem;  // Mars navigator menu item
    private JCheckBoxMenuItem searchToolItem;     // Search tool menu item
    private JCheckBoxMenuItem timeToolItem;       // Time tool menu item
    private JCheckBoxMenuItem monitorToolItem;    // Monitor tool menu item
    private JCheckBoxMenuItem prefsToolItem;      // Prefs tool menu item
    private JCheckBoxMenuItem missionToolItem;    // Mission tool menu item
    private JCheckBoxMenuItem settlementToolItem; // Settlement tool menu item
    private JCheckBoxMenuItem scienceToolItem;    // Science tool menu item
    private JCheckBoxMenuItem resupplyToolItem;   // Resupply tool menu item
    private JMenuItem aboutMspItem;       // About Mars Simulation Project menu item
    private JMenuItem tutorialItem;       // Tutorial menu item
    private JMenuItem guideItem;          // User Guide menu item

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
        newItem.setToolTipText("Create new simulation");
        fileMenu.add(newItem);

        // Create load menu item
        loadItem = new JMenuItem("Open...");
        loadItem.addActionListener(this);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK, false));
        loadItem.setToolTipText("Open a saved simulation");
        fileMenu.add(loadItem);

        fileMenu.add(new JSeparator());

        // Create save menu item
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK, false));
        saveItem.setToolTipText("Save the current simulation");
        fileMenu.add(saveItem);

        // Create save as menu item
        saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(this);
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK, false));
        saveAsItem.setToolTipText("Save the current simulation under a new name");
        fileMenu.add(saveAsItem);

        fileMenu.add(new JSeparator());

        // Create exit menu item
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK, false));
        exitItem.setToolTipText("Leave Mars Simulation Project");
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

        // Create prefs tool menu item
        prefsToolItem = new JCheckBoxMenuItem(PreferencesWindow.NAME);
        prefsToolItem.addActionListener(this);
        prefsToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
        toolsMenu.add(prefsToolItem);
        
        // Create mission tool menu item
        missionToolItem = new JCheckBoxMenuItem(MissionWindow.NAME);
        missionToolItem.addActionListener(this);
        missionToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false));
        toolsMenu.add(missionToolItem);
        
        // Create settlement map tool menu item
        settlementToolItem = new JCheckBoxMenuItem(SettlementWindow.NAME);
        settlementToolItem.addActionListener(this);
        settlementToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false));
        toolsMenu.add(settlementToolItem);

        // Create science tool menu item
        scienceToolItem = new JCheckBoxMenuItem(ScienceWindow.NAME);
        scienceToolItem.addActionListener(this);
        scienceToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false));
        toolsMenu.add(scienceToolItem);
          
        // Create resupply tool menu item
        resupplyToolItem = new JCheckBoxMenuItem(ResupplyWindow.NAME);
        resupplyToolItem.addActionListener(this);
        resupplyToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false));
        toolsMenu.add(resupplyToolItem);
        
        // Create help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.addMenuListener(this);
        add(helpMenu);

        // Create About Mars Simulation Project menu item
        aboutMspItem = new JMenuItem("About");
        aboutMspItem.addActionListener(this);
        aboutMspItem.setToolTipText("Show credits for MSP");
        helpMenu.add(aboutMspItem);

        helpMenu.add(new JSeparator());

        // Create Tutorial menu item
        tutorialItem = new JMenuItem("Tutorial");
        tutorialItem.addActionListener(this);
        tutorialItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK, false));
        tutorialItem.setToolTipText("Show the tutorial for beginners");
        helpMenu.add(tutorialItem);
        
        // Create User Guide menu item
        guideItem = new JMenuItem("User Guide");
        guideItem.addActionListener(this);
        guideItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK, false));
        guideItem.setToolTipText("Show the User Guide");
        helpMenu.add(guideItem);
    }

    // ActionListener method overriding
    public void actionPerformed(ActionEvent event) {
// This method always runs through an awful lot of if-then-else statements 
// when it seems we could save cycles by using a switch-case statement [lechimp 22/09/10]
        JMenuItem selectedItem = (JMenuItem) event.getSource();

        if (selectedItem == exitItem) mainWindow.exitSimulation();
        else if (selectedItem == newItem) mainWindow.newSimulation();
        else if (selectedItem == saveItem) mainWindow.saveSimulation(true);
        else if (selectedItem == saveAsItem) mainWindow.saveSimulation(false);
        else if (selectedItem == loadItem) mainWindow.loadSimulation();

        MainDesktopPane desktop = mainWindow.getDesktop();
        
        if (selectedItem == marsNavigatorItem) {
            if (marsNavigatorItem.isSelected()) desktop.openToolWindow(NavigatorWindow.NAME);
            else desktop.closeToolWindow(NavigatorWindow.NAME);
        }

        if (selectedItem == searchToolItem) {
            if (searchToolItem.isSelected()) desktop.openToolWindow(SearchWindow.NAME);
            else desktop.closeToolWindow(SearchWindow.NAME);
        }

        if (selectedItem == timeToolItem) {
            if (timeToolItem.isSelected()) desktop.openToolWindow(TimeWindow.NAME);
            else desktop.closeToolWindow(TimeWindow.NAME);
        }

        if (selectedItem == monitorToolItem) {
            if (monitorToolItem.isSelected()) desktop.openToolWindow(MonitorWindow.NAME);
            else desktop.closeToolWindow(MonitorWindow.NAME);
        }
        
        if (selectedItem == prefsToolItem) {
            if (prefsToolItem.isSelected()) desktop.openToolWindow(PreferencesWindow.NAME);
            else desktop.closeToolWindow(PreferencesWindow.NAME);
        }
        
        if (selectedItem == missionToolItem) {
            if (missionToolItem.isSelected()) desktop.openToolWindow(MissionWindow.NAME);
            else desktop.closeToolWindow(MissionWindow.NAME);
        }
        
        if (selectedItem == settlementToolItem) {
            if (settlementToolItem.isSelected()) desktop.openToolWindow(SettlementWindow.NAME);
            else desktop.closeToolWindow(SettlementWindow.NAME);
        }

        if (selectedItem == scienceToolItem) {
            if (scienceToolItem.isSelected()) desktop.openToolWindow(ScienceWindow.NAME);
            else desktop.closeToolWindow(ScienceWindow.NAME);
        }
        
        if (selectedItem == resupplyToolItem) {
            if (resupplyToolItem.isSelected()) desktop.openToolWindow(ResupplyWindow.NAME);
            else desktop.closeToolWindow(ResupplyWindow.NAME);
        }

        if (selectedItem == aboutMspItem) {
            desktop.openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide;
            ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
            ourGuide.setURL("/docs/help/about.html");
        }

        if (selectedItem == guideItem) {
            desktop.openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide;
            ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
            ourGuide.setURL("/docs/help/userguide.html");        }
        
        if (selectedItem == tutorialItem) {
            desktop.openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide;
            ourGuide = (GuideWindow)desktop.getToolWindow(GuideWindow.NAME);
            ourGuide.setURL("/docs/help/tutorial1.html");        }
    }

    // MenuListener method overriding
    public void menuSelected(MenuEvent event) {
        MainDesktopPane desktop = mainWindow.getDesktop();
        marsNavigatorItem.setSelected(desktop.isToolWindowOpen(NavigatorWindow.NAME));
        searchToolItem.setSelected(desktop.isToolWindowOpen(SearchWindow.NAME));
        timeToolItem.setSelected(desktop.isToolWindowOpen(TimeWindow.NAME));
        monitorToolItem.setSelected(desktop.isToolWindowOpen(MonitorWindow.NAME));
        prefsToolItem.setSelected(desktop.isToolWindowOpen(PreferencesWindow.NAME));
        missionToolItem.setSelected(desktop.isToolWindowOpen(MissionWindow.NAME));
        settlementToolItem.setSelected(desktop.isToolWindowOpen(SettlementWindow.NAME));
        scienceToolItem.setSelected(desktop.isToolWindowOpen(ScienceWindow.NAME));
        resupplyToolItem.setSelected(desktop.isToolWindowOpen(ResupplyWindow.NAME));
   }

    public void menuCanceled(MenuEvent event) {}
    public void menuDeselected(MenuEvent event) {}
}