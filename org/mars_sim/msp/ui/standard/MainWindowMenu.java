/**
 * Mars Simulation Project
 * MainWindowMenu.java
 * @version 2.76 2004-08-06
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

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
    private JMenuItem aboutMspItem;               // About Mars Simulation Project menu item

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
        fileMenu.add(newItem);

        // Create load menu item
        loadItem = new JMenuItem("Load");
        loadItem.addActionListener(this);
        fileMenu.add(loadItem);

        // Create save menu item
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        fileMenu.add(saveItem);

        // Create save as menu item
        saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(this);
        fileMenu.add(saveAsItem);

        // Create exit menu item
        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        fileMenu.add(exitItem);

        // Create tools menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.addMenuListener(this);
        add(toolsMenu);

        // Create Mars navigator menu item
        marsNavigatorItem = new JCheckBoxMenuItem("Mars Navigator");
        marsNavigatorItem.addActionListener(this);
        toolsMenu.add(marsNavigatorItem);

        // Create search tool menu item
        searchToolItem = new JCheckBoxMenuItem("Search Tool");
        searchToolItem.addActionListener(this);
        toolsMenu.add(searchToolItem);

        // Create time tool menu item
        timeToolItem = new JCheckBoxMenuItem("Time Tool");
        timeToolItem.addActionListener(this);
        toolsMenu.add(timeToolItem);

        // Create monitor tool menu item
        monitorToolItem = new JCheckBoxMenuItem("Monitor Tool");
        monitorToolItem.addActionListener(this);
        toolsMenu.add(monitorToolItem);

        // Create help menu
        JMenu helpMenu = new JMenu("Help");
        add(helpMenu);

        // Create about Mars Simulation Project menu item
        aboutMspItem = new JMenuItem("About The Mars Simulation Project");
        aboutMspItem.addActionListener(this);
        helpMenu.add(aboutMspItem);
    }

    // ActionListener method overriding
    public void actionPerformed(ActionEvent event) {

        JMenuItem selectedItem = (JMenuItem) event.getSource();

        if (selectedItem == exitItem) mainWindow.exitSimulation();
        else if (selectedItem == newItem) mainWindow.newSimulation();
        else if (selectedItem == saveItem) mainWindow.saveSimulation(true);
        else if (selectedItem == saveAsItem) mainWindow.saveSimulation(false);
        else if (selectedItem == loadItem) mainWindow.loadSimulation();

        if (selectedItem == marsNavigatorItem) {
            if (marsNavigatorItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow("Mars Navigator");
            else mainWindow.getDesktop().closeToolWindow("Mars Navigator");
        }

        if (selectedItem == searchToolItem) {
            if (searchToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow("Search Tool");
            else mainWindow.getDesktop().closeToolWindow("Search Tool");
        }

        if (selectedItem == timeToolItem) {
            if (timeToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow("Time Tool");
            else mainWindow.getDesktop().closeToolWindow("Time Tool");
        }

        if (selectedItem == monitorToolItem) {
            if (monitorToolItem.isSelected()) 
                mainWindow.getDesktop().openToolWindow("Monitor Tool");
            else mainWindow.getDesktop().closeToolWindow("Monitor Tool");
        }

        if (selectedItem == aboutMspItem) new AboutDialog(mainWindow);
    }

    private synchronized void sleep(int millis) {
        try {
            this.wait(millis);
        } 
        catch(InterruptedException ie) {}
    }

    // MenuListener method overriding
    public void menuSelected(MenuEvent event) {
        marsNavigatorItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen("Mars Navigator"));
        searchToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen("Search Tool"));
        timeToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen("Time Tool"));
        monitorToolItem.setSelected(
            mainWindow.getDesktop().isToolWindowOpen("Monitor Tool"));
    }

    public void menuCanceled(MenuEvent event) {}
    public void menuDeselected(MenuEvent event) {}
}
