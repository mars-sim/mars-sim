/**
 * Mars Simulation Project
 * TradeMissionCustomInfoPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.mission;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Map;

import org.mars_sim.msp.core.goods.CommerceMission;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.Trade;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

/**
 * A panel for displaying trade mission information.
 */
@SuppressWarnings("serial")
public class TradeMissionCustomInfoPanel extends MissionCustomInfoPanel {

	private final static int HEIGHT = 120;
	
	// Data members.
	private Trade mission;
	private GoodsTableModel sellingGoodsTableModel;
	private WebLabel desiredGoodsProfitLabel;
	private GoodsTableModel desiredGoodsTableModel;
	private WebLabel boughtGoodsProfitLabel;
	private GoodsTableModel boughtGoodsTableModel;

	/**
	 * Constructor.
	 */
	public TradeMissionCustomInfoPanel() {
		// Use JPanel constructor
		super();

		// Set the layout.
		setLayout(new GridLayout(3, 1));

		// Create the selling goods panel.
		WebPanel sellingGoodsPane = new WebPanel(new BorderLayout());
		add(sellingGoodsPane);

		// Create the selling goods label.
		WebLabel sellingGoodsLabel = new WebLabel("Goods to Sell:", WebLabel.LEFT);
		sellingGoodsPane.add(sellingGoodsLabel, BorderLayout.NORTH);

		// Create a scroll pane for the selling goods table.
		WebScrollPane sellingGoodsScrollPane = new WebScrollPane();
		sellingGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		sellingGoodsPane.add(sellingGoodsScrollPane, BorderLayout.CENTER);

		// Create the selling goods table and model.
		sellingGoodsTableModel = new GoodsTableModel() {
			@Override
			protected Map<Good, Integer> getLoad(CommerceMission commerce) {
				return commerce.getSellLoad();
			}
		};
		WebTable sellingGoodsTable = new WebTable(sellingGoodsTableModel);
		sellingGoodsScrollPane.setViewportView(sellingGoodsTable);

		// Create the desired goods panel.
		WebPanel desiredGoodsPane = new WebPanel(new BorderLayout());
		add(desiredGoodsPane);

		// Create the desired goods label panel.
		WebPanel desiredGoodsLabelPane = new WebPanel(new GridLayout(1, 2, 0, 0));
		desiredGoodsPane.add(desiredGoodsLabelPane, BorderLayout.NORTH);

		// Create the desired goods label.
		WebLabel desiredGoodsLabel = new WebLabel("Desired Goods to Buy:", WebLabel.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsLabel);

		// Create the desired goods profit label.
		desiredGoodsProfitLabel = new WebLabel("Profit:", WebLabel.LEFT);
		desiredGoodsLabelPane.add(desiredGoodsProfitLabel);

		// Create a scroll pane for the desired goods table.
		WebScrollPane desiredGoodsScrollPane = new WebScrollPane();
		desiredGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		desiredGoodsPane.add(desiredGoodsScrollPane, BorderLayout.CENTER);

		// Create the desired goods table and model.
		desiredGoodsTableModel = new GoodsTableModel() {
			@Override
			protected Map<Good, Integer> getLoad(CommerceMission commerce) {
				return commerce.getDesiredBuyLoad();
			}
		};
		WebTable desiredGoodsTable = new WebTable(desiredGoodsTableModel);
		desiredGoodsScrollPane.setViewportView(desiredGoodsTable);

		// Create the bought goods panel.
		WebPanel boughtGoodsPane = new WebPanel(new BorderLayout());
		add(boughtGoodsPane);

		// Create the bought goods label panel.
		WebPanel boughtGoodsLabelPane = new WebPanel(new GridLayout(1, 2, 0, 0));
		boughtGoodsPane.add(boughtGoodsLabelPane, BorderLayout.NORTH);

		// Create the bought goods label.
		WebLabel boughtGoodsLabel = new WebLabel("Goods Bought:", WebLabel.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsLabel);

		// Create the bought goods profit label.
		boughtGoodsProfitLabel = new WebLabel("Profit:", WebLabel.LEFT);
		boughtGoodsLabelPane.add(boughtGoodsProfitLabel);

		// Create a scroll pane for the bought goods table.
		WebScrollPane boughtGoodsScrollPane = new WebScrollPane();
		boughtGoodsScrollPane.setPreferredSize(new Dimension(-1, HEIGHT));
		boughtGoodsPane.add(boughtGoodsScrollPane, BorderLayout.CENTER);

		// Create the bought goods table and model.
		boughtGoodsTableModel = new GoodsTableModel() {
			@Override
			protected Map<Good, Integer> getLoad(CommerceMission commerce) {
				return commerce.getBuyLoad();
			}
		};
		WebTable boughtGoodsTable = new WebTable(boughtGoodsTableModel);
		boughtGoodsScrollPane.setViewportView(boughtGoodsTable);
	}

	@Override
	public void updateMissionEvent(MissionEvent e) {
		if (e.getType() == MissionEventType.BUY_LOAD_EVENT) {
			boughtGoodsTableModel.updateTable(mission);
			updateBoughtGoodsProfit();
		}
	}

	@Override
	public void updateMission(Mission newMission) {
		if (newMission instanceof Trade) {
			this.mission = (Trade) newMission;
			sellingGoodsTableModel.updateTable(mission);
			desiredGoodsTableModel.updateTable(mission);
			boughtGoodsTableModel.updateTable(mission);
			updateDesiredGoodsProfit();
			updateBoughtGoodsProfit();
		}
	}

	/**
	 * Updates the desired goods profit label.
	 */
	private void updateDesiredGoodsProfit() {
		int profit = (int) mission.getDesiredProfit();
		desiredGoodsProfitLabel.setText("Profit: " + profit + " VP");
	}

	/**
	 * Updates the bought goods profit label.
	 */
	private void updateBoughtGoodsProfit() {
		int profit = (int) mission.getProfit();
		boughtGoodsProfitLabel.setText("Profit: " + profit + " VP");
	}
}
