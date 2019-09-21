/**
 * Mars Simulation Project
 * SettlementTableModel.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.network.SettlementRegistry;

import javafx.application.Platform;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/**
 * Inner class for the settlement table model.
 */
public class SettlementTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(SettlementTableModel.class.getName());
	// Data members
	private String[] columns;
	private int numS = 0; // # of existing settlements recognized by the editor at the moment
	private List<SettlementInfo> settlements;
	private List<SettlementInfo> cacheSList = new CopyOnWriteArrayList<>();
	private SimulationConfig config;
	private SettlementInfo cacheS = new SettlementInfo();
	private ScenarioConfigEditorFX configEditor;

	/**
	 * Hidden Constructor.
	 */
	public SettlementTableModel(ScenarioConfigEditorFX configEditor) {
		super();

		this.configEditor = configEditor;
		this.config = SimulationConfig.instance();

		// Add table columns.
		columns = new String[] {
			Msg.getString("SimulationConfigEditor.column.player"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.name"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.template"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.population"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.numOfRobots"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.latitude"), //$NON-NLS-1$
			Msg.getString("SimulationConfigEditor.column.longitude"), //$NON-NLS-1$
			//Msg.getString("SimulationConfigEditor.column.hasMSD"), //$NON-NLS-1$
			//Msg.getString("SimulationConfigEditor.column.edit") //$NON-NLS-1$
		};

		// Load default settlements.
		settlements = new CopyOnWriteArrayList<>();

		if (!configEditor.getHasSettlement()) {
			Platform.runLater(() -> {
				//configEditor.getUndoButton().setText(Msg.getString("SimulationConfigEditor.button.default"));
				configEditor.getUndoButton().setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.undo"))); //$NON-NLS-1$
			});
			loadDefaultSettlements();
		}
		else {
			Platform.runLater(() -> {
				//configEditor.getUndoButton().setText(Msg.getString("SimulationConfigEditor.button.refresh"));
				configEditor.getUndoButton().setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.refresh"))); //$NON-NLS-1$
			});
			loadExistingSettlements();
		}

	}

	public List<SettlementInfo> getSettlements() {
		return settlements;
	}

	public void setConfigEditor(ScenarioConfigEditorFX configEditor) {
		this.configEditor = configEditor;
	}

	//public void setSettlementList(List<SettlementRegistry> settlementList) {
	//	this.settlementList = settlementList;
	//}

	public void editMSD(int modelRow) {
		System.out.println("Editing MSD on row "+ modelRow);
	}


	/**
	 * Load the default settlements from settlements.xml into the table.
	 */
	public void loadDefaultSettlements() {
		//if (getRowCount() > 0 )
			//saveCache();
		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		settlements.clear();
		for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
			SettlementInfo info = new SettlementInfo();
			info.playerName = configEditor.getPlayerName();
			info.name = settlementConfig.getInitialSettlementName(x);
			info.template = settlementConfig.getInitialSettlementTemplate(x);
			info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
			info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
			info.latitude = settlementConfig.getInitialSettlementLatitude(x);
			info.longitude = settlementConfig.getInitialSettlementLongitude(x);
			//info.hasMaxMSD = new Boolean(true);
			//if (info.hasMaxMSD) // if the checkbox for hasMaxMSD is checked
			//info.maxMSD = Integer.toString(settlementConfig.getInitialSettlementMaxMSD(x));
			//else
			//	info.maxMSD = Integer.toString(0);
			settlements.add(info);
		}
		fireTableDataChanged();
		//loadCache();
	}

	/**
	 * Load the existing settlements from SettlementRegistry into the table.
	 */
	public void loadExistingSettlements() {
		//if (getRowCount() > 0)
			//saveCache();
		settlements.clear();
			configEditor.getSettlementList().forEach( s -> {
				SettlementInfo info = new SettlementInfo();
				info.playerName = s.getPlayerName();
				info.name = s.getName();
				info.template = s.getTemplate();
				info.population = s.getPopulation() + "";
				info.numOfRobots = s.getNumOfRobots() + "";
				info.latitude = s.getLatitudeStr();
				info.longitude = s.getLongitudeStr();
				info.hasMaxMSD = true;//new Boolean(true); //s.getMaxMSDStr();
				info.maxMSD = "1"; // TODO:

				settlements.add(info);
				logger.info(info.name + "  " + info.template + "  " + info.population
						+ "  " + info.numOfRobots + "  " + info.latitude + "  " + info.longitude
						+ "  " + info.maxMSD);
			});
		fireTableDataChanged();
		//loadCache();
	}

	private void saveCache() {
		int size = getRowCount();
		cacheSList.clear();
		int s = numS; // x needs to be constant running running for loop and should not be set to the global variable numS
		// Add configuration settlements from table data.
		for (int x = s ; x < size; x++) {
			cacheS.playerName = (String) getValueAt(x, SettlementTable.COLUMN_PLAYER_NAME);
			cacheS.name = (String) getValueAt(x, SettlementTable.COLUMN_SETTLEMENT_NAME);
			cacheS.template = (String) getValueAt(x, SettlementTable.COLUMN_TEMPLATE);
			cacheS.population = (String) getValueAt(x, SettlementTable.COLUMN_POPULATION);
			cacheS.numOfRobots = (String) getValueAt(x, SettlementTable.COLUMN_BOTS);
			cacheS.latitude = (String) getValueAt(x, SettlementTable.COLUMN_LATITUDE);
			cacheS.longitude = (String) getValueAt(x, SettlementTable.COLUMN_LONGITUDE);
			//cacheS.msd = (String)getValueAt(x, COLUMN_MSD);
		}
		cacheSList.add(cacheS);
	}

	private void loadCache() {
		int rowCount = getRowCount();
		int size = cacheSList.size();
		// Add configuration settlements from table data.
		for (int x = rowCount ; x < rowCount + size; x++) {
			cacheS = cacheSList.get(x - rowCount);
			setValueAt((String)cacheS.playerName, x, SettlementTable.COLUMN_PLAYER_NAME);
			setValueAt((String)cacheS.name, x, SettlementTable.COLUMN_SETTLEMENT_NAME);
			setValueAt((String)cacheS.template, x, SettlementTable.COLUMN_TEMPLATE);
			setValueAt((String)cacheS.population, x, SettlementTable.COLUMN_POPULATION);
			setValueAt((String)cacheS.numOfRobots, x, SettlementTable.COLUMN_BOTS);
			setValueAt((String)cacheS.latitude, x, SettlementTable.COLUMN_LATITUDE);
			setValueAt((String)cacheS.longitude, x, SettlementTable.COLUMN_LONGITUDE);
			//setValueAt((String)cacheS.msd, x, COLUMN_MSD);

		}
	}


	@Override
	public int getRowCount() {
		return settlements.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex > -1) && (columnIndex < columns.length)) {
			return columns[columnIndex];
		}
		else {
			return Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
		}
	}
	/*
	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}
*/

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

	@Override
    public boolean isCellEditable(int row, int column) {
		boolean result = true;
    	if (column == 0)
    		// the first column is the player name, which is uneditable
    		result = false;

    	else if (configEditor.getHasSettlement()) {
		   if (row < configEditor.getSettlementList().size())
			   // if this is an existing settlement registered by another client machine,
			   // gray out the row and disable row cell editing
			   result = false;
		}

    	return result;
    }

	@Override
	public Object getValueAt(int row, int column) {
		Object result = Msg.getString("unknown"); //$NON-NLS-1$
		if ((row > -1) && (row < getRowCount())) {
			SettlementInfo info = settlements.get(row);
			if ((column > -1) && (column < getColumnCount())) {
				switch (column) {
				case 0:
					result = info.playerName;
					if (configEditor.getMultiplayerClient() != null) {
						List<SettlementRegistry> list = configEditor.getMultiplayerClient().getSettlementRegistryList();
						if (configEditor.getHasSettlement() && row < list.size()) {
							result = configEditor.getSettlementList().get(row).getPlayerName();
						}
						//else
						//	result = info.playerName;
					}
					//else
					//	result = info.playerName;
					break;
				case 1:
					result = info.name;
					break;
				case 2:
					result = info.template;
					break;
				case 3:
					result = info.population;
					break;
				case 4:
					result = info.numOfRobots;
					break;
				case 5:
					result = info.latitude;
					break;
				case 6:
					result = info.longitude;
					break;
				case 7:
					result = info.hasMaxMSD; //new Boolean(true);
					break;
				case 8:
					result = info.editMSD;
					break;

				}
			} else {
				result = Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
			}
		} else {
			result = Msg.getString("SimulationConfigEditor.log.invalidRow"); //$NON-NLS-1$
		}

		return result;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if ((rowIndex > -1) && (rowIndex < getRowCount())) {
			SettlementInfo info = settlements.get(rowIndex);
			// At this particular row, select a column
			if ((columnIndex > -1) && (columnIndex < getColumnCount())) {
		    	if (configEditor.getMultiplayerClient() != null) {
		    		if (configEditor.getSettlementList().size() == 0) {
		    			// if this cell belongs to an existing settlement owned by other players,
		    			// do NOT edit the cell
		    			displayCell(info, aValue, columnIndex);
		    		}
		    		else { // settlementList.size() > 0
		    			// if this cell is an attribute of the settlement which the player is creating
		    			editDisplayCell(info, aValue, columnIndex);
					}
		    	}
				else { // if this is a single-player sim
					editDisplayCell(info, aValue, columnIndex);
				}

			}

			if (columnIndex != SettlementTable.COLUMN_HAS_MSD)
				checkForErrors();

			fireTableDataChanged();
		}
	}

	private void editDisplayCell(SettlementInfo info, Object aValue, int columnIndex) {
		switch (columnIndex) {

		case 0:
			info.playerName = (String) aValue;
			break;

		case 1:
			info.name = (String) aValue;
			break;
		case 2:
			info.template = (String) aValue;
			info.population = configEditor.determineNewSettlementPopulation(info.template);
			info.numOfRobots = configEditor.determineNewSettlementNumOfRobots(info.template);
			break;
		case 3:
			info.population = (String) aValue;
			break;
		case 4:
			info.numOfRobots = (String) aValue;
			break;

		case 5:
			String latStr = ((String) aValue).trim();
			double doubleLat = 0;
			String latA = latStr.substring(0, latStr.length() - 1).trim();
			//System.out.println("latA is "+ latA);
			if ( !(isInteger(latA) || isDecimal(latA)) ) {
				//System.out.println("latA is not an integer nor a double: " + latA);
				info.latitude = (String) aValue;
			}

			else  {
				String d = latStr.substring(latStr.length() - 1, latStr.length()).toUpperCase();
				//System.out.println("d is "+ d);
				if (d.equals("N") | d.equals("S")) {
					//System.out.println("latStr has the direction of " + d);
					if (latA.length() > 2) {
						// round it to only one decimal place
						doubleLat = Double.parseDouble(latA);
						doubleLat = Math.round(doubleLat*10.0)/10.0;
						//System.out.println("doubleLat is " + doubleLat);
						info.latitude =  doubleLat + " " + d;
					}
					else{
						//System.out.println("No need to round. latStr is not a double and latstr has < 3 char");
						info.latitude = (String) aValue;
					}
				}
				else {
					//System.out.println("Invalid ! Trimming off the last character. New latStr is " + latStr);
					latStr = latStr.substring(0, latStr.length() - 1);
					info.latitude = latStr;
				}
			}

			break;

		case 6:
			String loStr = ((String) aValue).trim();
			double doubleLo = 0;
			String loA = loStr.substring(0, loStr.length() - 1).trim();
			//System.out.println("loA is "+ loA);
			if ( !(isInteger(loA) || isDecimal(loA)) ) {
				//System.out.println("loA is not an integer nor a double: " + loA);
				info.longitude = (String) aValue;
			}

			else  {
				String d = loStr.substring(loStr.length() - 1, loStr.length()).toUpperCase();
				//System.out.println("d is "+ d);
				if (d.equals("E") | d.equals("W")) {
					//System.out.println("loStr has the direction of " + d);
					if (loA.length() > 2) {
						// round it to only one decimal place
						doubleLo = Double.parseDouble(loA);
						doubleLo = Math.round(doubleLo*10.0)/10.0;
						//System.out.println("doubleLo is " + doubleLo);
						info.longitude =  doubleLo + " " + d;
					}
					else{
						//System.out.println("No need to round. loStr is not a double and latstr has < 3 char");
						info.longitude = (String) aValue;
					}
				}
				else {
					//System.out.println("Invalid ! Trimming off the last character. New loStr is " + loStr);
					loStr = loStr.substring(0, loStr.length() - 1);
					info.longitude = loStr;
				}
			}

			break;
			
		case 7:
			info.hasMaxMSD = (Boolean) aValue; // new Boolean(true); //
			break;

		case 8:

			break;


		}  // switch (columnIndex) {

	}

	private void displayCell(SettlementInfo info, Object aValue, int columnIndex) {

		switch (columnIndex) {
			case 0:
				info.playerName = (String) aValue;
				break;
			case 1:
				info.name = (String) aValue;
				break;
			case 2:
				info.template = (String) aValue;
				if (configEditor.getMultiplayerClient() == null) {
					info.population = configEditor.determineNewSettlementPopulation(info.template);
					info.numOfRobots = configEditor.determineNewSettlementNumOfRobots(info.template);
				}
				break;
			case 3:
				info.population = (String) aValue;
				break;
			case 4:
				info.numOfRobots = (String) aValue;
				break;
			case 5:
				info.latitude = (String) aValue;
				break;
			case 6:
				info.longitude = (String) aValue;
				break;
			case 7:
				info.hasMaxMSD = (Boolean) aValue; //new Boolean(true);//
				break;
			case 8:
				info.editMSD = (String) aValue;
				break;
		}

	}

	/**
	 * Checks if the string is an integer or not
	 * Note: this is the fastest way. See Jonas' solution at
	 * http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java
	 * @param Str the tested string
	 *
	 */
	public boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c > '9') {
				return false;
			}
		}
		return true;
	}


	/**
	 * Checks if the string is a decimal number or not
	 * @param Str the tested string
	 * see http://stackoverflow.com/questions/3133770/how-to-find-out-if-the-value-contained-in-a-string-is-double-or-not/29686308#29686308
	 */
	public boolean isDecimal(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 1 ) {
			return false;
		}
		int i = 0;
		
		if (str.charAt(0) == '-') { 
			if (length < 3) {
				return false;
			}
			i = 1;
		}
		else
			return false;
		
		int numOfDot = 0;
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c == '.')
				numOfDot++;
			else if (c == '/')
				return false;
			else if (c < '.' || c > '9') {
				return false;
			}
		}
		if (numOfDot != 1 )
			return false;

		return true;
	}


	/**
	 * Remove a set of settlements from the table.
	 * @param rowIndexes an array of row indexes of the settlements to remove.
	 */
	public void removeSettlements(int[] rowIndexes) {
		List<SettlementInfo> removedSettlements = new CopyOnWriteArrayList<>();

		for (int x = 0; x < rowIndexes.length; x++) {
			if ((rowIndexes[x] > -1) && (rowIndexes[x] < getRowCount())) {
				removedSettlements.add(settlements.get(rowIndexes[x]));
			}
		}

		Iterator<SettlementInfo> i = removedSettlements.iterator();
		while (i.hasNext()) {
			settlements.remove(i.next());
		}

		fireTableDataChanged();
	}

	/**
	 * Adds a new settlement to the table.
	 * @param settlement the settlement configuration.
	 */
	public void addSettlement(SettlementInfo settlement) {
		settlements.add(settlement);
		fireTableDataChanged();
	}

	/**
	 * Checks and updates the current number of settlement registered in the host server.
	 * @param settlement the settlement configuration.
	 */
	public void checkNumExistingSettlement() {
		if (configEditor.getMultiplayerClient() != null) {
			int newNumS = configEditor.getMultiplayerClient().getNumSettlement();

			if (newNumS != numS)
				// when a brand new settlement was just newly registered in the host server
				if (newNumS > 0) {
					configEditor.setHasSettlement(true);
					//setExistingSettlements();
					Platform.runLater(() -> {
						// show the label text
						configEditor.getErrorLabel().setText("The Settlement list has just been refreshed");
		    			configEditor.getErrorLabel().setTextFill(Color.GREEN);
					});
				}
			    // when an existing settlement was deleted or no longer registered with the host server
				else if (newNumS == 0) {
					configEditor.setHasSettlement(false);
					//setDefaultSettlements();
					Platform.runLater(() -> {
						// show the label text
						configEditor.getErrorLabel().setText("Cannot detect any existing settlements");
		    			configEditor.getErrorLabel().setTextFill(Color.GREEN);
					});
				}

			fireTableDataChanged();
		}
	}


	/**
	 * Sets an edit-check error.
	 * @param errorString the error description.
	 */
	private void setError(String errorString) {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		Platform.runLater(() -> {
			if (!configEditor.getHasError()) {
				configEditor.setHasError(true);
    			configEditor.getErrorLabel().setText(errorString);
    			//errorLabel.setStyle("-fx-font-color:red;");
    			configEditor.getErrorLabel().setTextFill(Color.RED);
    			configEditor.getStartButton().setDisable(true);
    		}
		});
	}

	/**
	 * Clears all edit-check errors.
	 */
	private void clearError() {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		Platform.runLater(() -> {
			configEditor.setHasError(false);
        		configEditor.getErrorLabel().setText(""); //$NON-NLS-1$
        		configEditor.getErrorLabel().setTextFill(Color.BLACK);
        		configEditor.getStartButton().setDisable(false);
        });
	}

	/**
	 * Check for errors in table settlement values.
	 */
	private void checkForErrors() {
		//System.out.println("checkForErrors"); // runs only when a user click on a cell
		checkNumExistingSettlement();
		clearError();

		// TODO: check to ensure the latitude/longitude has NOT been chosen already in the table by another settlement registered by the host server

		// TODO: incorporate checking for user locale and its decimal separation symbol (. or ,)

		try {
			boolean repeated = false;
			int size = getRowCount();
			for (int x = 0; x < size; x++) {

				String latStr = ((String) (getValueAt(x, SettlementTable.COLUMN_LATITUDE))).trim().toUpperCase();
				String longStr = ((String) (getValueAt(x, SettlementTable.COLUMN_LONGITUDE))).trim().toUpperCase();

				// check if the second from the last character is a digit or a letter, if a letter, setError
				if (Character.isLetter(latStr.charAt(latStr.length() - 2))){
					setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
					return;
				}

				// check if the last character is a digit or a letter, if digit, setError
				if (Character.isDigit(latStr.charAt(latStr.length() - 1))){
					setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
					return;
				}

				if (latStr == null || latStr.length() < 2) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
					return;
				}

				if (longStr == null || longStr.length() < 2 ) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
					return;
				}

				//System.out.println("settlement.latitude is "+ settlement.latitude);
				if (x + 1 < size ) {
					String latNextStr = ((String) (getValueAt(x + 1, SettlementTable.COLUMN_LATITUDE))).trim().toUpperCase();
					String longNextStr = ((String) (getValueAt(x + 1, SettlementTable.COLUMN_LONGITUDE))).trim().toUpperCase();

					//System.out.println("latStr is "+ latStr);
					//System.out.println("latNextStr is "+ latNextStr);
					if ( latNextStr == null || latNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
						return;
					}
					else if (latStr.equals(latNextStr)) {
						repeated = true;
						break;
					}

					else {
						double doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
						double doubleLatNext = Double.parseDouble(latNextStr.substring(0, latNextStr.length() - 1));
						//System.out.println("doubleLat is "+ doubleLat);
						//System.out.println("doubleLatNext is "+ doubleLatNext);
						if (doubleLatNext == 0 && doubleLat == 0) {
							repeated = true;
							break;
						}
					}

					//System.out.println("now checking for longitude");

					if ( longNextStr == null ||  longNextStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
						return;
					}
					else if (longStr.equals(longNextStr)) {
						repeated = true;
						break;
					}

					else {
						double doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
						double doubleLongNext = Double.parseDouble(longNextStr.substring(0, longNextStr.length() - 1));
						//System.out.println("doubleLong is "+ doubleLong);
						//System.out.println("doubleLongNext is "+ doubleLongNext);
						if (doubleLongNext == 0 && doubleLong == 0) {
							repeated = true;
							break;
						}
					}
				}
			}

			if (repeated) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeRepeating")); //$NON-NLS-1$
				return;
			}

		} catch(NumberFormatException e) {
			setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
			e.printStackTrace();
		}

		Iterator<SettlementInfo> i = settlements.iterator();
		while (i.hasNext()) {
			SettlementInfo settlement = i.next();

			// Check that settlement name is valid.
			if ((settlement.name.trim() == null) || (settlement.name.trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.nameMissing")); //$NON-NLS-1$
			}

			// Check if population is valid.
			if ((settlement.population.trim() == null) || (settlement.population.trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.populationMissing")); //$NON-NLS-1$
			} else {
				try {
					int popInt = Integer.parseInt(settlement.population.trim());
					if (popInt < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.populationTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.populationInvalid")); //$NON-NLS-1$
					e.printStackTrace();
				}
			}

			// Check if number of robots is valid.
			if ((settlement.numOfRobots.trim() == null) || (settlement.numOfRobots.trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsMissing")); //$NON-NLS-1$
			} else {
				try {
					int num = Integer.parseInt(settlement.numOfRobots.trim());
					if (num < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsInvalid")); //$NON-NLS-1$
					e.printStackTrace();
				}
			}

			// Check that settlement latitude is valid.
			if ((settlement.latitude.trim() == null) || (settlement.latitude.trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
			} else {
				String cleanLatitude = settlement.latitude.trim().toUpperCase();
				if (!cleanLatitude.endsWith(Msg.getString("direction.northShort")) &&
				        !cleanLatitude.endsWith(Msg.getString("direction.southShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
					setError(
						Msg.getString(
							"SimulationConfigEditor.error.latitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.northShort"), //$NON-NLS-1$
							Msg.getString("direction.southShort") //$NON-NLS-1$
						)
					);
				}
				else {
					String numLatitude = cleanLatitude.substring(0, cleanLatitude.length() - 1);
					try {
						double doubleLatitude = Double.parseDouble(numLatitude.trim());
						if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
							setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
						}
					}
					catch(NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}

			// Check that settlement longitude is valid.
			if ((settlement.longitude.trim() == null) || (settlement.longitude.trim().isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
			} else {
				String cleanLongitude = settlement.longitude.trim().toUpperCase();
				if (!cleanLongitude.endsWith(Msg.getString("direction.westShort")) &&
				        !cleanLongitude.endsWith(Msg.getString("direction.eastShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
					setError(
						Msg.getString(
							"SimulationConfigEditor.error.longitudeEndWith", //$NON-NLS-1$
							Msg.getString("direction.eastShort"), //$NON-NLS-1$
							Msg.getString("direction.westShort") //$NON-NLS-1$
						)
					);
				} else {
					String numLongitude = cleanLongitude.substring(0, cleanLongitude.length() - 1);
					try {
						double doubleLongitude = Double.parseDouble(numLongitude.trim());
						if ((doubleLongitude < 0) || (doubleLongitude > 180)) {
							setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
						}
					} catch(NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
						e.printStackTrace();
					}
				}
			}
		}
	}
}
