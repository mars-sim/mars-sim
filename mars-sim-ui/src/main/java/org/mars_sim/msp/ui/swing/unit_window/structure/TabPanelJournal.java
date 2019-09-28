/**
 * Mars Simulation Project
 * TabPanelJournal.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.vehicle.TabPanelMission;

/**
 * Tab panel displaying a list of settlement missions.<br>
 * Renamed to plural form to be distinguishable from
 * {@link TabPanelMission}, which displays a vehicle's
 * single mission's details.
 */
@SuppressWarnings("serial")
public class TabPanelJournal
extends TabPanel {
	
	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	private Settlement settlement;


	/**
	 * Constructor.
	 * @param settlement {@link Settlement} the settlement this tab panel is for.
	 * @param desktop {@link MainDesktopPane} the main desktop panel.
	 */
	public TabPanelJournal(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelJournal.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelJournal.tooltip"), //$NON-NLS-1$
			settlement, desktop
		);

		this.settlement = settlement;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Create title label.
		JLabel label = new JLabel(Msg.getString("TabPanelJournal.label"), JLabel.CENTER); //$NON-NLS-1$
		label.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(label);

		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(centerPanel, BorderLayout.CENTER);
		
		JournalArea textArea = new JournalArea();
		centerPanel.add(textArea);
		
	}

	class JournalArea extends JTextArea  {
		
			private static final long serialVersionUID = 1L;
		public JournalArea()   {  }  
		    //Override JTextArea paint method  
		 //Enables it to paint JTextArea background image  
		public void paint(Graphics g)  {  
			  //Make JTextArea transparent  
			  setOpaque(false);  
			   
			  //add wrap  
			  setLineWrap(true);  
			   
			  //Make JTextArea word wrap  
			  setWrapStyleWord(true);  
			   
			  //Get image that we use as JTextArea background
			  //Choose your own image directory in parameters.
			  //ImageIcon ii=new ImageIcon("one.jpg");  
			  
			  //Image i=ii.getImage();  
			  
			   Image img;
			   String IMAGE_DIR = "/images/";
	           String fullImageName = "LanderHab.png";
	            String fileName = fullImageName.startsWith("/") ?
	                	fullImageName :
	                	IMAGE_DIR + fullImageName;
	            URL resource = ImageLoader.class.getResource(fileName);
				Toolkit kit = Toolkit.getDefaultToolkit();
				img = kit.createImage(resource);
			   
			  //Draw JTextArea background image  
			  g.drawImage(img,0,0,null,this);  
			   
			  //Call super.paint to see TextArea
			  super.paint(g);  
		  }
		}
		 
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
//		settlement = null;
	}
}