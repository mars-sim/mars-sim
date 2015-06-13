/**
 * Mars Simulation Project
 * StormTrackingWindow.java
 * @version 3.08 2015-05-22
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mars_sim.msp.ui.jme3.JmeCanvas;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;

import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The window for tracking storms.
 */
public class StormTrackingWindow
extends JInternalFrame
implements InternalFrameListener, ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private JPanel infoPane;
	private TabPanelWeather tab;

	public StormTrackingWindow(MainDesktopPane desktop, TabPanelWeather tab) {
		// Use JInternalFrame constructor
        super("Storm Tracking Window", false, true, false, true);

        this.tab = tab;
		// Create info panel.
		infoPane = new JPanel(new CardLayout());
		infoPane.setBorder(new MarsPanelBorder());

		add(infoPane, BorderLayout.CENTER);

		setSize(new Dimension(400, 400));

		setContentPane(createJMEWindow());

		desktop.add(this);

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);
	    setVisible(true);

	}


	public JPanel createJMEWindow() {

		JmeCanvas jmeCanvas = new JmeCanvas();
		JPanel panel = new JPanel(new BorderLayout(0, 0));
		panel.add(jmeCanvas.setupJME());

		return panel;
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

	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
		tab.setViewer(null);
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


}
