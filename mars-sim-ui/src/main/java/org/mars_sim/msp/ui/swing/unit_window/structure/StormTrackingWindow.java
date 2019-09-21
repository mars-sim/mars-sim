/**
 * Mars Simulation Project
 * StormTrackingWindow.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

//import javafx.embed.swing.SwingNode;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;

/**
 * The window for tracking storms.
 */
@SuppressWarnings("serial")
public class StormTrackingWindow
extends JInternalFrame
implements InternalFrameListener, ActionListener {

	// Data members
	private JPanel infoPane;
//	private TabPanelWeather tabPanelWeather;

	public StormTrackingWindow(MainDesktopPane desktop, TabPanelWeather tabPanelWeather) {
		// Use JInternalFrame constructor
        super("Storm Tracking", false, true, false, false);

//        this.tabPanelWeather = tabPanelWeather;
		// Create info panel.
		infoPane = new JPanel(new CardLayout());
		infoPane.setBorder(new MarsPanelBorder());

		add(infoPane, BorderLayout.CENTER);

		setSize(new Dimension(400, 400));

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		addInternalFrameListener(this);

		desktop.add(this);

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    setVisible(true);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		//if (source == prevButton) buttonClickedPrev();
		//else if (source == nextButton) buttonClickedNext();
		//else if (source == finalButton) buttonClickedFinal();
	}


	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
//		tabPanelWeather.setViewer(null);
		//System.out.println("internalFrameClosing()");
	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
//		tabPanelWeather.setViewer(null);
		//System.out.println("internalFrameClosed()");
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		infoPane = null;
//		tabPanelWeather = null;
	}
}
