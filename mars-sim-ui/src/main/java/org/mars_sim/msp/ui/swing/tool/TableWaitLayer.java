package org.mars_sim.msp.ui.swing.tool;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class TableWaitLayer {
	private final String[] columnNames = { "String", "Integer", "Boolean" };
	private final Object[][] data = { { "aaa", 12, true }, { "bbb", 5, false }, { "ccc", 9, false }, };
	private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
		@Override
		public Class<?> getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}
	};
	private final JTable table = new JTable(model);
	private final JButton startButton = new JButton();
	private final WaitLayerUI layerUI = new WaitLayerUI();

	public JComponent makeUI() {
		startButton.setAction(new AbstractAction("start") {
			@Override
			public void actionPerformed(ActionEvent e) {
				layerUI.start();
				startButton.setEnabled(false);
				SwingWorker<String, Object[]> worker = new SwingWorker<String, Object[]>() {
					@Override
					public String doInBackground() {
						int current = 0, lengthOfTask = 120;
						while (current < lengthOfTask && !isCancelled()) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException ie) {
								return "Interrupted";
							}
							publish(new Object[] { "aaa", current++, false });
						}
						return "Done";
					}

					@Override
					protected void process(java.util.List<Object[]> chunks) {
						for (Object[] array : chunks) {
							model.addRow(array);
						}
						table.scrollRectToVisible(table.getCellRect(model.getRowCount() - 1, 0, true));
					}

					@Override
					public void done() {
						layerUI.stop();
						startButton.setEnabled(true);
						String text = null;
						if (isCancelled()) {
							text = "Cancelled";
						} else {
							try {
								text = get();
							} catch (Exception ex) {
								ex.printStackTrace();
								text = "Exception";
							}
						}
					}
				};
				worker.execute();
			}
		});
		JPanel p = new JPanel(new BorderLayout());
		p.add(new JButton("dummy"), BorderLayout.NORTH);
		p.add(new JLayer<JComponent>(new JScrollPane(table), layerUI));
		p.add(startButton, BorderLayout.SOUTH);
		return p;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public static void createAndShowGUI() {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.getContentPane().add(new TableWaitLayer().makeUI());
		f.setSize(320, 240);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}