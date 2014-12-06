/**
 * Mars Simulation Project
 * NotificationMenu.java
 * @version 3.07 2014-12-05
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MainWindowMenu;


public class NotificationMenu implements ActionListener, MenuListener {

	private MainWindowMenu mainWindowMenu; // Needed and do NOT delete
	
	
	// 2014-12-04 Added notification related items
	private JCheckBoxMenuItem medicalMenuItem;
	private JCheckBoxMenuItem malfunctionMenuItem;	
	private JRadioButtonMenuItem threeMenuItem;
	private JRadioButtonMenuItem twoMenuItem;
	private JRadioButtonMenuItem oneMenuItem;
	private JRadioButtonMenuItem showAllMenuItem ;
	private JRadioButtonMenuItem showLastTwoMenuItem ;
	private JRadioButtonMenuItem showLastOneMenuItem ;
	
	private boolean showMedical = true;
	private boolean showMalfunction = true;
	private int maxNumMsg = 99;
	private int displayTime = 2;

	/** The main window frame. */
	private MainWindow mainWindow;
	
	public NotificationMenu(MainWindowMenu mainWindowMenu) {
		this.mainWindowMenu = mainWindowMenu;
		this.mainWindow = mainWindowMenu.getMainWindow();

		
		JMenu notificationMenu = new JMenu(Msg.getString("mainMenu.notification")); //$NON-NLS-1$
		notificationMenu.addActionListener(this);
		notificationMenu.setMnemonic(KeyEvent.VK_N);
		//notificationMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK, false));
		notificationMenu.setToolTipText(Msg.getString("mainMenu.tooltip.notification")); //$NON-NLS-1$
		mainWindowMenu.add(notificationMenu);
		
		JMenu typeItem = new JMenu("Message Type");
		notificationMenu.add(typeItem);

		medicalMenuItem = new JCheckBoxMenuItem("Medical");
		medicalMenuItem.setSelected(true);
		medicalMenuItem.addActionListener(this);
		typeItem.add(medicalMenuItem);

		malfunctionMenuItem = new JCheckBoxMenuItem("Malfunction");
		malfunctionMenuItem.setSelected(true);
		malfunctionMenuItem.addActionListener(this);
		typeItem.add(malfunctionMenuItem);
		

		//notificationMenu.addSeparator();
			
		JMenu displayTimeItem = new JMenu("Display Time");
		notificationMenu.add(displayTimeItem);

		
		ButtonGroup group = new ButtonGroup();
		threeMenuItem = new JRadioButtonMenuItem("3 Seconds");
		//three.setSelected(true);
		group.add(threeMenuItem);
		threeMenuItem.addActionListener(this);
		displayTimeItem.add(threeMenuItem);

		twoMenuItem = new JRadioButtonMenuItem("2 Seconds");
		twoMenuItem.setSelected(true);
		group.add(twoMenuItem);
		twoMenuItem.addActionListener(this);
		displayTimeItem.add(twoMenuItem);

		oneMenuItem = new JRadioButtonMenuItem("1 Second");
		//one.setSelected(true);
		group.add(oneMenuItem);
		oneMenuItem.addActionListener(this);
		displayTimeItem.add(oneMenuItem);
		
		//notificationMenu.addSeparator();
		//notificationMenu.add(new JSeparator());
		
		JMenu queueItem = new JMenu("Queue Size");
		notificationMenu.add(queueItem);

		
		ButtonGroup group2 = new ButtonGroup();
		showAllMenuItem = new JRadioButtonMenuItem("Unlimited");
		//all.setSelected(true);
		group2.add(showAllMenuItem);
		showAllMenuItem.addActionListener(this);
		queueItem.add(showAllMenuItem);

		showLastTwoMenuItem = new JRadioButtonMenuItem("Two");
		showLastTwoMenuItem.setSelected(true);
		group2.add(showLastTwoMenuItem);
		showLastTwoMenuItem.addActionListener(this);
		queueItem.add(showLastTwoMenuItem);

		showLastOneMenuItem = new JRadioButtonMenuItem("One");
		//lastOne.setSelected(true);
		group2.add(showLastOneMenuItem);
		showLastOneMenuItem.addActionListener(this);
		queueItem.add(showLastOneMenuItem);

		notificationMenu.add(new JSeparator());
		
		JMenuItem noteItem = new JMenuItem("<html><i><h5>Note: Expect delay for changes to take effect</h5></i></html>");
		notificationMenu.add(noteItem);

	}
	
	
	/**
	 * Gets the value of showMedical.
	 * @return showMedical
	 */
	public boolean getShowMedical() {
		return showMedical;
	}

	/**
	 * Gets the value of showMalfunction.
	 * @return showMalfunction
	 */
	public boolean getShowMalfunction() {
		return showMalfunction;
	}
	
	/**
	 * Gets the value of maxNumMsg.
	 * @return maxNumMsg
	 */
	public int getMaxNumMsg() {
		return maxNumMsg;
	}
	
	/**
	 * Gets the value of displayTime.
	 * @return displayTime
	 */
	public int getDisplayTime() {
		return displayTime;
	}

	
	
	
	/** ActionListener method overriding. */
	@Override
	public final void actionPerformed(ActionEvent event) {
		JMenuItem selectedItem = (JMenuItem) event.getSource();
		//MainDesktopPane desktop = mainWindow.getDesktop();

		if (selectedItem == medicalMenuItem) {
			if (medicalMenuItem.isSelected()) 
				showMedical = true;
			else 
				showMedical = false;
			System.out.println("selectedItem ==  medicalMenuItem");
			System.out.println("showMedical is " + showMedical);
		}
			
		if (selectedItem ==  malfunctionMenuItem) {
			if (malfunctionMenuItem.isSelected()) 
				showMalfunction = true;
			else 
				showMalfunction = false;		
			System.out.println("selectedItem ==  malfunctionMenuItem");
			System.out.println("showMalfunction is " + showMalfunction);			
		}
		
		
		if (selectedItem ==  threeMenuItem) displayTime = 3;
		if (selectedItem ==  twoMenuItem) displayTime = 2;
		if (selectedItem ==  oneMenuItem) displayTime = 1;
		if (selectedItem ==  showAllMenuItem) maxNumMsg = 99;
		if (selectedItem ==  showLastTwoMenuItem) maxNumMsg = 2;
		if (selectedItem ==  showLastOneMenuItem) maxNumMsg = 1;
		
	}
	
	/** MenuListener method overriding. */
	@Override
	public final void menuSelected(MenuEvent event) {
		//MainDesktopPane desktop = mainWindow.getDesktop();

		//notificationItem.setSelected(desktop.getMainWindow().getNotification());

		//volumeItem.setValue(Math.round(desktop.getSoundPlayer().getVolume() * 10F));
		//volumeItem.setEnabled(!desktop.getSoundPlayer().isMute());
		//muteItem.setSelected(desktop.getSoundPlayer().isMute());
	}
	

	public void menuCanceled(MenuEvent event) {}
	public void menuDeselected(MenuEvent event) {}

	
}
