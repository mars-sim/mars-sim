/*
 * Mars Simulation Project
 * NotificationManager.java
 * @date 2026-05-04
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.events.HistoricalEvent;
import com.mars_sim.core.events.HistoricalEventListener;
import com.mars_sim.core.logging.SimLogger;

/**
 * This class provides the ability to publish notifications onto the desktop of the user.
 * It listens for historical events and shows a notification when a new event is added to the simulation. It also provides a menu for the user to control when notifications are shown.
 * Notifications are shown using the system tray if it is supported by the operating system.
 * If the system tray is not supported, notifications will be disabled and a warning will be logged.
 */
public class NotificationManager implements HistoricalEventListener {

    private static final String NOTIFICATION_CONTROL = "notificationControl";
	private static final SimLogger logger = SimLogger.getLogger(NotificationManager.class.getName());

    enum NotificationControl {
        NEVER, ALWAYS, WHEN_MINIMISED
    }

    private NotificationControl notificationControl = NotificationControl.WHEN_MINIMISED; // Default value
    private TrayIcon trayIcon;
    private JFrame mainFrame;

    public NotificationManager(Simulation sim, JFrame mainframe, Properties uiProps) {
        this.mainFrame = mainframe;

        // Build systray
        if (SystemTray.isSupported()) {
            // Initialize system tray notifications here
            SystemTray tray = SystemTray.getSystemTray();

            trayIcon = new TrayIcon(StyleManager.getIconImage(), SimulationRuntime.SHORT_TITLE);
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
                createTrayIconMenu();
            } catch (AWTException e) {
                logger.severe("Failed to add tray icon", e);
                trayIcon = null; // Disable tray icon if it fails to initialize
            }
        }
        else {
            logger.warning("System tray not supported. Notifications will be disabled.");
        }

        // Restore notification control setting from properties, defaulting to existing value if not set or invalid
        String controlValue = uiProps.getProperty(NOTIFICATION_CONTROL, notificationControl.name());
        try {   
            this.notificationControl = NotificationControl.valueOf(controlValue);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid notification control value in properties: " + controlValue);
        }

        // Listen for changes in the notification control setting
        sim.getEventManager().addListener(this);
    }

    private List<JMenuItem> buildSettingsMenu() {
        List<JMenuItem>  menuItems = new ArrayList<>();
        ButtonGroup group = new ButtonGroup();
        // Create radio button menu items for each notification control option
        for (NotificationControl control : NotificationControl.values()) {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(getDisplayName(control));
            menuItem.setSelected(control == notificationControl);
            menuItem.addActionListener(new NotificationControlActionListener(control));
            group.add(menuItem);
            menuItems.add(menuItem);
        }

        return menuItems;
    }

    public JMenu getSettingsMenu() {
        JMenu settingsMenu = new JMenu("Notification Settings");
        settingsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent me) {
                JMenu menuSource = (JMenu) me.getSource();
		        menuSource.removeAll();//
				for (JMenuItem item : buildSettingsMenu()) {
                    menuSource.add(item);
                }
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
        return settingsMenu;
     }

    /**
     * Creates and configures the popup menu for the tray icon.
     */
    private void createTrayIconMenu() {

        // Add mouse listener to show popup menu on right-click
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            private void showPopupMenu(MouseEvent e) {
                var popupMenu = new JPopupMenu();
                for (JMenuItem menuItem : buildSettingsMenu()) {
                    popupMenu.add(menuItem);
                }
                popupMenu.setLocation(e.getX(), e.getY());
                popupMenu.setInvoker(popupMenu);
                popupMenu.setVisible(true);
            }
        });
    }

    /**
     * Returns a user-friendly display name for the notification control option.
     * @param control The notification control option
     * @return User-friendly display name
     */
    private static String getDisplayName(NotificationControl control) {
        return switch (control) {
            case NEVER -> "Never show";
            case ALWAYS -> "Always show";
            case WHEN_MINIMISED -> "Only Show when minimised";
            default -> control.name();  
        };
    }

    /**
     * Action listener for notification control menu items.
     */
    private class NotificationControlActionListener implements ActionListener {
        private final NotificationControl control;

        public NotificationControlActionListener(NotificationControl control) {
            this.control = control;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            notificationControl = control;
            logger.info("Notification control changed to: " + control);
        }
    }

    /**
     * Show a notification message. It will only be shown if the visibility criteria is meet. 
     * @param title The title of the notification.
     * @param message The message content of the notification.
     * @param isWarning Whether the notification is a warning.
     */    
    public void showNotification(String title, String message, boolean isWarning) {
        TrayIcon.MessageType messageType = isWarning ? TrayIcon.MessageType.WARNING : TrayIcon.MessageType.INFO;

        // Check if we can show a notification based on the control setting and main frame visibility
        if (trayIcon != null && (notificationControl == NotificationControl.ALWAYS
                    || (notificationControl == NotificationControl.WHEN_MINIMISED
                            && (mainFrame != null) && (mainFrame.getState() == Frame.ICONIFIED)))) {
            SwingUtilities.invokeLater(() -> trayIcon.displayMessage(title, message, messageType));
        }
    }

    /**
     * Get the notification-related UI properties to be saved in the configuration.
     * @return Settings to control notifications.
     */
    public Properties getUIProps() {
        Properties props = new Properties();
        props.setProperty(NOTIFICATION_CONTROL, notificationControl.name());
        return props;
    }

    /**
     * A historical event has occured so potentially show a notification about it.
     * @param event The historical event that has been added to the simulation.
     */
    @Override
    public void eventAdded(HistoricalEvent event) {
        String message = event.getType().getName() + ": " + event.getSource().getName();
        showNotification("Event Added", message, false);
    }
}
