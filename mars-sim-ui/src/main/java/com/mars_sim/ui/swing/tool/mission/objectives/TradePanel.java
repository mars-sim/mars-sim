/**
 * Mars Simulation Project
 * TradePanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.mission.objectives;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.mars_sim.core.mission.objectives.TradeObjective;
import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.tool.mission.GoodsTableModel;


/**
 * A panel for displaying trade objectives
 */
@SuppressWarnings("serial")
public class TradePanel extends JPanel implements EntityListener{
	
	// Data members.
	private TradeObjective objective;
	private GoodsTableModel sellingGoodsTableModel;
	private JLabel desiredGoodsProfitLabel;
	private GoodsTableModel desiredGoodsTableModel;
	private JLabel boughtGoodsProfitLabel;
	private GoodsTableModel boughtGoodsTableModel;

	/**
	 * Constructor.
	 * @param context the UI context.
	 */
	public TradePanel(TradeObjective objective, UIContext context) {
		// Use JPanel constructor
		super();

		setName(objective.getName());
		this.objective = objective;

		// Set the layout.
		setLayout(new GridLayout(3, 1));

		// Create the selling goods panel.
		JPanel sellingGoodsPane = new JPanel(new BorderLayout());
		add(sellingGoodsPane);

		// Create the selling goods label.
		JLabel sellingGoodsLabel = new JLabel("Goods to Sell:", SwingConstants.LEFT);
		sellingGoodsPane.add(sellingGoodsLabel, BorderLayout.NORTH);

		// Create a scroll pane for the selling goods table.
		JScrollPane sellingGoodsScrollPane = new JScrollPane();
		sellingGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		sellingGoodsPane.add(sellingGoodsScrollPane, BorderLayout.CENTER);

		// Create the selling goods table and model.
		sellingGoodsTableModel = new GoodsTableModel();
		JTable sellingGoodsTable = new JTable(sellingGoodsTableModel);
		sellingGoodsTable.setAutoCreateRowSorter(true);
		sellingGoodsScrollPane.setViewportView(sellingGoodsTable);

		// Create the desired goods panel.
		JPanel desiredGoodsPane = new JPanel(new BorderLayout());
		add(desiredGoodsPane);

		// Create the desired goods label panel.
		JPanel desiredGoodsLabelPane = new JPanel(new GridLayout(1, 2, 0, 0));
		desiredGoodsPane.add(desiredGoodsLabelPane, BorderLayout.NORTH);

		// Create the desired goods label.
		JLabel desiredGoodsLabel = new JLabel("Desired Goods to Buy:", SwingConstants.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsLabel);

		// Create the desired goods profit label.
		desiredGoodsProfitLabel = new JLabel("Profit:", SwingConstants.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsProfitLabel);

		// Create a scroll pane for the desired goods table.
		JScrollPane desiredGoodsScrollPane = new JScrollPane();
		desiredGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		desiredGoodsPane.add(desiredGoodsScrollPane, BorderLayout.CENTER);

		// Create the desired goods table and model.
		desiredGoodsTableModel = new GoodsTableModel();
		JTable desiredGoodsTable = new JTable(desiredGoodsTableModel);
		desiredGoodsTable.setAutoCreateRowSorter(true);
		desiredGoodsScrollPane.setViewportView(desiredGoodsTable);

		// Create the bought goods panel.
		JPanel boughtGoodsPane = new JPanel(new BorderLayout());
		add(boughtGoodsPane);

		// Create the bought goods label panel.
		JPanel boughtGoodsLabelPane = new JPanel(new GridLayout(1, 2, 0, 0));
		boughtGoodsPane.add(boughtGoodsLabelPane, BorderLayout.NORTH);

		// Create the bought goods label.
		JLabel boughtGoodsLabel = new JLabel("Goods Bought:", SwingConstants.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsLabel);

		// Create the bought goods profit label.
		boughtGoodsProfitLabel = new JLabel("Profit:", SwingConstants.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsProfitLabel);

		// Create a scroll pane for the bought goods table.
		JScrollPane boughtGoodsScrollPane = new JScrollPane();
		boughtGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		boughtGoodsPane.add(boughtGoodsScrollPane, BorderLayout.CENTER);

		// Create the bought goods table and model.
		boughtGoodsTableModel = new GoodsTableModel();
		JTable boughtGoodsTable = new JTable(boughtGoodsTableModel);
		boughtGoodsTable.setAutoCreateRowSorter(true);
		boughtGoodsScrollPane.setViewportView(boughtGoodsTable);

		// Update the tables
		desiredGoodsTableModel.updateTable(objective.getDesiredBuy());
		boughtGoodsTableModel.updateTable(objective.getBought());
		sellingGoodsTableModel.updateTable(objective.getSell());
		desiredGoodsProfitLabel.setText("Profit: " + objective.getDesiredProfit() + " VP");
		updateBoughtGoodsProfit();
	}


	@Override
	public void missionUpdate(EntityEvent e) {
		if (e.getType().equals(EntityEventType.MISSION_BUY_LOAD_EVENT)) {
			boughtGoodsTableModel.updateTable(objective.getBought());
			sellingGoodsTableModel.updateTable(objective.getSell());

			updateBoughtGoodsProfit();
		}
	}

	/**
	 * Updates the bought goods profit label.
	 */
	private void updateBoughtGoodsProfit() {
		int profit = (int) objective.getProfit();
		boughtGoodsProfitLabel.setText("Profit: " + profit + " VP");
	}
}
