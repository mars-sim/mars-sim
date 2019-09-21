/**
 * Mars Simulation Project
 * NotificationMenu.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MainWindowMenu;


public class NotificationMenu implements ActionListener, MenuListener {

	private MainWindowMenu mainWindowMenu; // Needed and do NOT delete
	
	
	// 2014-12-04 Added notification related items
	private JCheckBoxMenuItem medicalMenuItem;
	private JCheckBoxMenuItem malfunctionMenuItem;
	// 2014-12-17 Added confirmMenuItem
	private JRadioButtonMenuItem confirmMenuItem;
	private JRadioButtonMenuItem threeMenuItem;
	private JRadioButtonMenuItem twoMenuItem;
	private JRadioButtonMenuItem oneMenuItem;
	private JRadioButtonMenuItem showAllMenuItem ;
	private JRadioButtonMenuItem showLastThreeMenuItem ;
	private JRadioButtonMenuItem showLastOneMenuItem ;
	// 2014-12-17 Added isConfirmButtonEnabled
	private boolean isConfirmEachEnabled  = false;
	private boolean showMedical = true;
	private boolean showMalfunction = true;
	private boolean isSetQueueToEmpty = false;
	private int maxNumMsg = 99;
	private int displayTime = 2;

	private Timer timer;
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
		
		//notificationMenu.add(new JSeparator());
		
		JMenu typeItem = new JMenu("Message Type");
		typeItem.setMnemonic(KeyEvent.VK_M);
		notificationMenu.add(typeItem);

		medicalMenuItem = new JCheckBoxMenuItem("Medical");
		medicalMenuItem.setSelected(true);
		medicalMenuItem.addActionListener(this);
		typeItem.add(medicalMenuItem);

		malfunctionMenuItem = new JCheckBoxMenuItem("Malfunction");
		malfunctionMenuItem.setSelected(true);
		malfunctionMenuItem.addActionListener(this);
		typeItem.add(malfunctionMenuItem);

		typeItem.addSeparator();
		
		JLabel resetLabel = new JLabel("<html><font size=-2>"
				+ "<font color=red>&nbsp;&nbsp;Note: any changes<br>&nbsp;&nbsp;may RESET Queue</font></font></html>");
		typeItem.add(resetLabel);
		

			
		JMenu displayTimeItem = new JMenu("Display Time");
		typeItem.setMnemonic(KeyEvent.VK_T);
		notificationMenu.add(displayTimeItem);

		
		ButtonGroup group = new ButtonGroup();
		confirmMenuItem = new JRadioButtonMenuItem("Confirm Each");
		//three.setSelected(true);
		group.add(confirmMenuItem);
		confirmMenuItem.addActionListener(this);
		displayTimeItem.add(confirmMenuItem);
		
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
		typeItem.setMnemonic(KeyEvent.VK_S);
		notificationMenu.add(queueItem);
		
		ButtonGroup group2 = new ButtonGroup();
		showAllMenuItem = new JRadioButtonMenuItem("Unlimited");
		//all.setSelected(true);
		group2.add(showAllMenuItem);
		showAllMenuItem.addActionListener(this);
		queueItem.add(showAllMenuItem);

		showLastThreeMenuItem = new JRadioButtonMenuItem("Three");
		showLastThreeMenuItem.setSelected(true);
		group2.add(showLastThreeMenuItem);
		showLastThreeMenuItem.addActionListener(this);
		queueItem.add(showLastThreeMenuItem);

		showLastOneMenuItem = new JRadioButtonMenuItem("One");
		//lastOne.setSelected(true);
		group2.add(showLastOneMenuItem);
		showLastOneMenuItem.addActionListener(this);
		queueItem.add(showLastOneMenuItem);
		
		notificationMenu.add(new JSeparator());	
		
		// 2014-12-10 Added Empty Queue option
		JButton b = new JButton(
				"<html><font size=-4>Purge</font></html>");
		JPanel p = new JPanel();
		JLabel l = new JLabel("<html><font size=-2><font color=red> All Msgs in Queue Now</font></font></html>");
		l.setOpaque(false);
		p.setOpaque(false);
		p.add(b);
		p.add(l);
			b.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
		    	isSetQueueToEmpty = true;
		    }
		});
		
		notificationMenu.add(p);
		
		notificationMenu.add(new JSeparator());
		JMenuItem noteItem = new JMenuItem("<html><font size=-2>"
		+ "<font color=blue>Note: May take a few seconds<br>for changes to Take Effect</font></font></html>");
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
	 * Gets the value of isQueueEmpty.
	 * @return isQueueEmpty
	 */
	public boolean getIsSetQueueEmpty() {
		boolean result = false;
		if (isSetQueueToEmpty) {
			result = true;
			//emptyMenuItem.setSelected(false);
			// reset it back to false once it calls this method
			isSetQueueToEmpty = false;
		}
		return result; 
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
	
	/**
	 * Gets the value of isConfirmButtonEnabled.
	 * @return isConfirmButtonEnabled
	 */
	// 2014-12-17 Added getIsConfirmButtonEnabled()
	public boolean getIsConfirmEachEnabled() {
		return isConfirmEachEnabled;
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
			//System.out.println("selectedItem ==  medicalMenuItem");
			//System.out.println("showMedical is " + showMedical);
		}
			
		if (selectedItem ==  malfunctionMenuItem) {
			if (malfunctionMenuItem.isSelected()) 
				showMalfunction = true;
			else 
				showMalfunction = false;		
			//System.out.println("selectedItem ==  malfunctionMenuItem");
			//System.out.println("showMalfunction is " + showMalfunction);			
		}
		
		// 2014-12-17 confirmMenuItem
		if (selectedItem ==  confirmMenuItem){
			if (confirmMenuItem.isSelected()) 
				isConfirmEachEnabled = true;
			else 
				isConfirmEachEnabled = false;	
		}
		
		if (selectedItem ==  threeMenuItem) displayTime = 3;
		if (selectedItem ==  twoMenuItem) displayTime = 2;
		if (selectedItem ==  oneMenuItem) displayTime = 1;
		if (selectedItem ==  showAllMenuItem) maxNumMsg = 99;
		if (selectedItem ==  showLastThreeMenuItem) maxNumMsg = 3;
		if (selectedItem ==  showLastOneMenuItem) maxNumMsg = 1;

	} // end of public final void actionPerformed(ActionEvent event) 
		
	public class CancelTask extends TimerTask {
		public void run() {
			System.out.println("Time's up!");
			timer.cancel(); // Terminate the thread
		}
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
