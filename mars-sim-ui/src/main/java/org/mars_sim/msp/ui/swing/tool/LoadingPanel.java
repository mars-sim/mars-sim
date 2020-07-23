/**
 * Mars Simulation Project
 * LoadingPanel.java
 * @version 3.1.1 2020-07-22
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;

public class LoadingPanel {

	static final WaitLayerUIPanel layerUI = new WaitLayerUIPanel();
	JFrame frame = new JFrame("JLayer With Animated Gif");

	public LoadingPanel() {
		JPanel panel = new JPanel() {

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(400, 300);
			}
		};
		JLayer<JPanel> jlayer = new JLayer<>(panel, layerUI);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(jlayer);
		frame.pack();
		frame.setVisible(true);
		layerUI.start();
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new LoadingPanel();

			}
		});
	}
}
