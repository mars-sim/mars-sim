/*
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @date 2022-07-09
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.fav.Preference;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

//import com.vdurmont.emoji.EmojiParser;

/**
 * The TabPanelFavorite is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelFavorite
extends TabPanel {

	private static final String HEART_ICON = Msg.getString("icon.heart"); //$NON-NLS-1$
	
	/** The Preference Table. */	
	private JTable table;
	/** The Preference Table Model. */	
	private PreferenceTableModel tableModel;
	/** The Person instance. */
	private Person person = null;

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelFavorite(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getNewIcon(HEART_ICON),	
			Msg.getString("TabPanelFavorite.title"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;
	}

	@Override
	protected void buildUI(JPanel content) {
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		content.add(topPanel, BorderLayout.NORTH);
		
		// Prepare SpringLayout for info panel.
		WebPanel infoPanel = new WebPanel(new SpringLayout());
		topPanel.add(infoPanel);

		// Prepare main dish name label
		WebLabel mainDishNameLabel = new WebLabel(Msg.getString("TabPanelFavorite.mainDish"), WebLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(mainDishNameLabel);

		// Prepare main dish label
		String mainDish = person.getFavorite().getFavoriteMainDish();
		WebPanel wrapper1 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField mainDishTF = new WebTextField(Conversion.capitalize(mainDish));
		mainDishTF.setEditable(false);
		mainDishTF.setColumns(17);
		//mainDishTF.requestFocus();
		mainDishTF.setCaretPosition(0);
		wrapper1.add(mainDishTF);
		infoPanel.add(wrapper1);

		// Prepare side dish name label
		WebLabel sideDishNameLabel = new WebLabel(Msg.getString("TabPanelFavorite.sideDish"), WebLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(sideDishNameLabel);

		// Prepare side dish label
		String sideDish = person.getFavorite().getFavoriteSideDish();
		WebPanel wrapper2 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField sideDishTF = new WebTextField(Conversion.capitalize(sideDish));
		sideDishTF.setEditable(false);
		sideDishTF.setColumns(17);
		//sideDishTF.requestFocus();
		sideDishTF.setCaretPosition(0);
		wrapper2.add(sideDishTF);
		infoPanel.add(wrapper2);

		// Prepare dessert name label
		WebLabel dessertNameLabel = new WebLabel(Msg.getString("TabPanelFavorite.dessert"), WebLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(dessertNameLabel);

		// Prepare dessert label
		String dessert = person.getFavorite().getFavoriteDessert();
		WebPanel wrapper3 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField dessertTF = new WebTextField(Conversion.capitalize(dessert));
		dessertTF.setEditable(false);
		dessertTF.setColumns(17);
		//dessertTF.requestFocus();
		dessertTF.setCaretPosition(0);
		wrapper3.add(dessertTF);
		infoPanel.add(wrapper3);

		// Prepare activity name label
		WebLabel activityNameLabel = new WebLabel(Msg.getString("TabPanelFavorite.activity"), WebLabel.RIGHT); //$NON-NLS-1$
		infoPanel.add(activityNameLabel);

		// Prepare activity label
		FavoriteType activity = person.getFavorite().getFavoriteActivity();
		WebPanel wrapper4 = new WebPanel(new FlowLayout(0, 0, FlowLayout.LEADING));
		WebTextField activityTF = new WebTextField(activity.getName());
		activityTF.setEditable(false);
		activityTF.setColumns(17);
		//activityTF.requestFocus();
		activityTF.setCaretPosition(0);
		wrapper4.add(activityTF);
		infoPanel.add(wrapper4);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(infoPanel,
		                                4, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                10, 2);       //xPad, yPad

		// Create label panel.
		WebPanel labelPanel = new WebPanel(new BorderLayout(0, 0));
		topPanel.add(labelPanel);
		
		// Create preference title label
		WebLabel preferenceLabel = new WebLabel(Msg.getString("TabPanelFavorite.preferenceTable.title"), WebLabel.CENTER); //$NON-NLS-1$
		preferenceLabel.setFont(SUBTITLE_FONT);
		labelPanel.add(preferenceLabel, BorderLayout.NORTH);
		
		// Create scroll panel
		WebScrollPane scrollPane = new WebScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		topPanel.add(scrollPane, BorderLayout.CENTER);
		
		// Create skill table
		tableModel = new PreferenceTableModel(person);
		table = new ZebraJTable(tableModel);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);

		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.setRowSelectionAllowed(true);
		table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Added sorting
		table.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(table);

		scrollPane.setViewportView(table);
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class PreferenceTableModel
	extends AbstractTableModel {

		private Preference manager;
		private List<String> scoreStringList;
		private Map<String, Integer> scoreStringMap;

		private PreferenceTableModel(Unit unit) {

			Person person = null;
//	        Robot robot = null;

	        if (unit instanceof Person) {
	         	person = (Person) unit;
				manager = person.getPreference();
	        }
//	        else if (unit instanceof Robot) {
//	        }

	        scoreStringList = manager.getTaskStringList();
	        scoreStringMap = manager.getScoreStringMap();

		}

		public int getRowCount() {
			return scoreStringMap.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = Double.class; //String.class, Integer.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelFavorite.column.metaTask"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelFavorite.column.like"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			Object name = scoreStringList.get(row);
			if (column == 0)
				return name;
			else if (column == 1) {
				return scoreStringMap.get(name);

			}
			else
				return null;
		}
	}

	/**
	 * This renderer uses a delegation software design pattern to delegate
	 * this rendering of the table cell header to the real default render
	 **/
	class TableHeaderRenderer implements TableCellRenderer {
		private TableCellRenderer defaultRenderer;

		public TableHeaderRenderer(TableCellRenderer theRenderer) {
			defaultRenderer = theRenderer;
		}


		/**
		 * Renderer the specified Table Header cell
		 **/
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column) {

			Component theResult = defaultRenderer.getTableCellRendererComponent(
					table, value, isSelected, hasFocus,
					row, column);

			if (theResult instanceof WebLabel) {
				WebLabel cell = (WebLabel) theResult;
				cell.setText((String)value);
			}

			return theResult;
		}
	}
}
