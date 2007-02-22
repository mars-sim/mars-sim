package org.mars_sim.msp.ui.standard.tool.mission.create_wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.IntPoint;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.UnitCollection;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.SettlementCollection;
import org.mars_sim.msp.simulation.structure.SettlementIterator;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.tool.map.CenteredCircleLayer;
import org.mars_sim.msp.ui.standard.tool.map.MapPanel;
import org.mars_sim.msp.ui.standard.tool.map.NavpointEditLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitIconMapLayer;
import org.mars_sim.msp.ui.standard.tool.map.UnitLabelMapLayer;

class ExplorationSitesPanel extends WizardPanel {

	private final static String NAME = "Exploration Sites";

	private MapPanel mapPane;
	private CenteredCircleLayer circleLayer;
	private NavpointEditLayer navLayer;
	private boolean navSelected;
	private IntPoint navOffset;
	private JPanel siteListPane;
	
	ExplorationSitesPanel(CreateMissionWizard wizard) {
		// Use WizardPanel constructor.
		super(wizard);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new MarsPanelBorder());
		
		JLabel titleLabel = new JLabel("Choose the exploration sites.");
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(titleLabel);
		
		mapPane = new MapPanel();
		mapPane.addMapLayer(new UnitIconMapLayer(mapPane));
		mapPane.addMapLayer(new UnitLabelMapLayer());
		mapPane.addMapLayer(circleLayer = new CenteredCircleLayer(Color.GREEN));
		mapPane.addMapLayer(navLayer = new NavpointEditLayer(mapPane));
		// mapPane.addMouseListener(new NavpointMouseListener());
		// mapPane.addMouseMotionListener(new NavpointMouseMotionListener());
		mapPane.setMaximumSize(mapPane.getPreferredSize());
		mapPane.setMinimumSize(mapPane.getPreferredSize());
		mapPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(mapPane);
		
		add(Box.createVerticalStrut(10));
		
		JPanel sitePane = new JPanel(new BorderLayout(0, 0));
		sitePane.setMaximumSize(new Dimension(400, 100));
		sitePane.setPreferredSize(sitePane.getMaximumSize());
		sitePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(sitePane);
		
        // Create scroll panel for site list.
        JScrollPane siteScrollPane = new JScrollPane();
        sitePane.add(siteScrollPane, BorderLayout.CENTER);
        
        JPanel siteListMainPane = new JPanel(new BorderLayout(0, 0));
        siteScrollPane.setViewportView(siteListMainPane);
        
        siteListPane = new JPanel();
        siteListPane.setLayout(new BoxLayout(siteListPane, BoxLayout.Y_AXIS));
        siteListMainPane.add(siteListPane, BorderLayout.NORTH);
        
        add(Box.createVerticalStrut(10));
		
		JLabel instructionLabel = new JLabel("Drag navpoint flags to the desired exploration sites.");
		instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.BOLD));
		instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(instructionLabel);
		
		add(Box.createVerticalGlue());
	}
	
	String getPanelName() {
		return NAME;
	}

	void commitChanges() {
		// TODO Auto-generated method stub

	}

	void clearInfo() {
		siteListPane.removeAll();
	}

	void updatePanel() {
		siteListPane.add(new SitePanel(0, new Coordinates(0D, 0D)));
		siteListPane.add(new SitePanel(1, new Coordinates(Math.PI / 2D, 0D)));
		siteListPane.add(new SitePanel(2, new Coordinates(Math.PI / 2D, Math.PI)));
		siteListPane.add(new SitePanel(3, new Coordinates(Math.PI / 2D, Math.PI / 3D)));
		siteListPane.add(new SitePanel(4, new Coordinates(Math.PI / 3D, Math.PI)));
	}
	
	private class SitePanel extends JPanel {
		
		private Coordinates site;
		private int siteNum;
		private JLabel siteNumLabel;
		private JLabel siteLocationLabel;
		
		SitePanel(int siteNum, Coordinates site) {
			// Use JPanel constructor.
			super();
		
			this.siteNum = siteNum;
			this.site = site;
			
			setLayout(new GridLayout(1, 3));
			setBorder(new EtchedBorder());
			
			siteNumLabel = new JLabel(" Site " + (siteNum + 1));
			add(siteNumLabel);
			
			siteLocationLabel = new JLabel(site.getFormattedString());
			add(siteLocationLabel);
			
			JButton removeButton = new JButton("Remove");
			add(removeButton);
		}
		
		void setSiteNum(int siteNum) {
			this.siteNum = siteNum;
			siteNumLabel.setText(" Site " + (siteNum + 1));
		}
		
		void setLocation(Coordinates site) {
			this.site = site;
			siteLocationLabel.setText(site.getFormattedString());
		}
		
		Coordinates getSite() {
			return site;
		}
	}
}