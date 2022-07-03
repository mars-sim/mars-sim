/*
 * Mars Simulation Project
 * InitialSettlementModel.java
 * @date 2022-06-15
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.configuration.Scenario;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.structure.InitialSettlement;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.tool.RandomUtil;

/**
 * Represents a table model of the initial settlements. This has some intelligence
 * in knowing that populations are derived from a Template by default and similar
 * for Settlement name derived from Reporting Authority.
 * When either of the driving columns are changed the dependent columns are
 * recalculated.
 */
@SuppressWarnings("serial")
class InitialSettlementModel extends AbstractTableModel {

	// Inner class representing a settlement configuration.
	private final class SettlementInfo {
		String name;
		String sponsor;
		String template;
		String population;
		String numOfRobots;
		String latitude;
		String longitude;
		String crew;
	}

	public static final int SETTLEMENT_COL = 0;
	public static final int SPONSOR_COL = 1;
	public static final int PHASE_COL = 2;
	public static final int SETTLER_COL = 3;
	public static final int CREW_COL = 4;
	public static final int BOT_COL = 5;
	public static final int LAT_COL = 6;
	public static final int LON_COL = 7;
	
	public static final int NUM_DECIMAL_PLACES = 4;
	public static final double DECIMAL_PLACES = 10000.0;
	
	private String[] columns;
	private List<SettlementInfo> settlementInfoList;
	private SettlementConfig settlementConfig;
	private ReportingAuthorityFactory raFactory;
	private String errorMessage;
	
	/**
	 * @param settlementConfig 
	 * @param raFactory 
	 */
	InitialSettlementModel(SettlementConfig settlementConfig, ReportingAuthorityFactory raFactory) {
		super();

		this.settlementConfig = settlementConfig;
		this.raFactory = raFactory;
		
		// Add table columns.
		columns = new String[] { 
				Msg.getString("SimulationConfigEditor.column.name"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.sponsor"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.template"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.population"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.crew"),
				Msg.getString("SimulationConfigEditor.column.numOfRobots"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.latitude"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.longitude") //$NON-NLS-1$
		};
		
		this.settlementInfoList = new ArrayList<>();
	}

	
	/**
	 * Load the default settlements in the table.
	 */
	public void loadDefaultSettlements(Scenario selected) {
		settlementInfoList.clear();
		List<String> usedNames = new ArrayList<>();

		for (InitialSettlement spec : selected.getSettlements()) {
			SettlementInfo info = toSettlementInfo(spec);

			// Save this name to the list
			usedNames.add(info.name);
				
			settlementInfoList.add(info);
		}
			
		fireTableDataChanged();
	}

	private SettlementInfo toSettlementInfo(InitialSettlement spec) {
		SettlementInfo info = new SettlementInfo();
		info.name = spec.getName();
		info.sponsor = spec.getSponsor();
		info.crew = spec.getCrew();
		info.template = spec.getSettlementTemplate();
		info.population = Integer.toString(spec.getPopulationNumber());
		info.numOfRobots = Integer.toString(spec.getNumOfRobots());
		Coordinates location = spec.getLocation();
		if (location != null) {
			info.latitude = location.getFormattedLatitudeString();
			info.longitude = location.getFormattedLongitudeString();
		}	
		return info;
	}


	/**
	 * Get the rows as InitialSettlements.
	 * @return
	 */
	public List<InitialSettlement> getSettlements() {

		List<InitialSettlement> is = new ArrayList<>();

		// Add configuration settlements from table data.
		for (SettlementInfo info : settlementInfoList) {
			int populationNum = Integer.parseInt(info.population);
			int numOfRobots = Integer.parseInt(info.numOfRobots);
			
			// take care to internationalize the coordinates
			String latitude = info.latitude.replace("N", Msg.getString("direction.northShort")); //$NON-NLS-1$ //$NON-NLS-2$
			latitude = latitude.replace("S", Msg.getString("direction.southShort")); //$NON-NLS-1$ //$NON-NLS-2$
			String longitude = info.longitude.replace("E", Msg.getString("direction.eastShort")); //$NON-NLS-1$ //$NON-NLS-2$
			longitude = longitude.replace("W", Msg.getString("direction.westShort")); //$NON-NLS-1$ //$NON-NLS-2$

			Coordinates location = new Coordinates(latitude, longitude);

			is.add(new InitialSettlement(info.name, info.sponsor, info.template,
												populationNum, numOfRobots, location, info.crew));

		}
		
		return is;
	}
	
	@Override
	public int getRowCount() {
		return settlementInfoList.size();
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for each
	 * cell. If we didn't implement this method, then the last column would contain
	 * text ("true"/"false"), rather than a check box.
	 */
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex > -1) && (columnIndex < columns.length)) {
			return columns[columnIndex];
		} else {
			return Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	@Override
	public Object getValueAt(int row, int column) {
		Object result = Msg.getString("unknown"); //$NON-NLS-1$

		if ((row > -1) && (row < getRowCount())) {
			SettlementInfo info = settlementInfoList.get(row);
			if ((column > -1) && (column < getColumnCount())) {
				switch (column) {
				case SETTLEMENT_COL:
					result = info.name;
					break;
				case SPONSOR_COL:
					result = info.sponsor;
					break;
				case PHASE_COL:
					result = info.template;
					break;
				case SETTLER_COL:
					result = info.population;
					break;
				case CREW_COL:
					result = info.crew;
					break;
				case BOT_COL:
					result = info.numOfRobots;
					break;
				case LAT_COL:
					result = info.latitude;
					break;
				case LON_COL:
					result = info.longitude;
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
			SettlementInfo info = settlementInfoList.get(rowIndex);
			if ((columnIndex > -1) && (columnIndex < getColumnCount())) {
				switch (columnIndex) {
				
				case SETTLEMENT_COL:
					info.name = (String) aValue;
					break;
					
				case SPONSOR_COL:
					String newSponsor = (String) aValue;
					if (!info.sponsor.equals(newSponsor)) {
						info.sponsor = newSponsor;
						String newName = tailorSettlementNameBySponsor(info.sponsor, rowIndex);
						if (newName != null) {
							info.name = newName;
						}
					}
					break;	
					
				case PHASE_COL:
					info.template = (String) aValue;
					info.population = Integer.toString(ConfigModelHelper.determineNewSettlementPopulation(info.template, settlementConfig));
					info.numOfRobots = Integer.toString(ConfigModelHelper.determineNewSettlementNumOfRobots(info.template, settlementConfig));
					break;
					
				case SETTLER_COL:
					info.population = (String) aValue;
					break;
					
				case CREW_COL:
					info.crew = (String) aValue;
					break;
					
				case BOT_COL:
					info.numOfRobots = (String) aValue;
					break;

				case LAT_COL:
					String latStr = ((String) aValue).trim();
					double doubleLat = 0;
					String dir1 = latStr.substring(latStr.length() - 1, latStr.length());
					if (dir1.equalsIgnoreCase("N") || dir1.equalsIgnoreCase("S")) {
						if (latStr.length() > NUM_DECIMAL_PLACES) {
							doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
							doubleLat = Math.round(doubleLat * DECIMAL_PLACES) / DECIMAL_PLACES;
							info.latitude = doubleLat + " " + dir1.toUpperCase();
						}
						else {
							info.latitude = (String) aValue;
						}
					}
					else {
						info.latitude = (String) aValue;
					}
					String latError = Coordinates.checkLat(info.latitude);
					if (latError != null)
						setError(latError);
					break;

				case LON_COL:
					String longStr = ((String) aValue).trim();
					double doubleLong = 0;
					String dir2 = longStr.substring(longStr.length() - 1, longStr.length());
					if (dir2.equalsIgnoreCase("E") || dir2.equalsIgnoreCase("W")) {
						if (longStr.length() > NUM_DECIMAL_PLACES) {
							doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
							doubleLong = Math.round(doubleLong * DECIMAL_PLACES) / DECIMAL_PLACES;
							info.longitude = doubleLong + " " + dir2.toUpperCase();
						}
						else {
							info.longitude = (String) aValue;
						}
					}
					else {
						info.longitude = (String) aValue;
					}
					String lonError = Coordinates.checkLon(info.longitude);
					if (lonError != null)
						setError(lonError);
					break;
				}
			}

			if (columnIndex != SPONSOR_COL || columnIndex != PHASE_COL)
				checkForAllErrors();

			fireTableDataChanged();
		}
	}


	/**
	 * Returns a random settlement name tailored by the sponsor
	 * 
	 * @param sponsor
	 * @return
	 */
	private String tailorSettlementNameBySponsor(String sponsor, int index) {
		ReportingAuthority ra = raFactory.getItem(sponsor);

		List<String> usedNames = new ArrayList<>();
		
		// Add configuration settlements from table data.
		for (int x = 0; x < getRowCount(); x++) {
			String name = (String) getValueAt(x, SETTLEMENT_COL);
			usedNames.add(name);

		}

		// Gets a list of settlement names that are tailored to this country
		List<String> candidateNames = new ArrayList<String>(ra.getSettlementNames());
		candidateNames.removeAll(usedNames);

		if (candidateNames.isEmpty())
			return "Settlement #" + index;
		else
			return candidateNames.get(RandomUtil.getRandomInt(candidateNames.size()-1));
	}
	
	/**
	 * Remove a set of settlements from the table.
	 * 
	 * @param rowIndexes
	 *            an array of row indexes of the settlements to remove.
	 */
	public void removeSettlements(int[] rowIndexes) {
		List<SettlementInfo> removedSettlements = new ArrayList<>(rowIndexes.length);

		for (int x = 0; x < rowIndexes.length; x++) {
			if ((rowIndexes[x] > -1) && (rowIndexes[x] < getRowCount())) {
				removedSettlements.add(settlementInfoList.get(rowIndexes[x]));
			}
		}

		Iterator<SettlementInfo> i = removedSettlements.iterator();
		while (i.hasNext()) {
			SettlementInfo s = i.next();
			settlementInfoList.remove(s);
		}

		fireTableDataChanged();
	}

	/**
	 * Check for all errors in the table.
	 */
	private void checkForAllErrors() {
		errorMessage = null;
		
		Set<Coordinates> usedCoordinates = new HashSet<>();
		Set<String> usedCrews = new HashSet<>();
		for(SettlementInfo settlement : settlementInfoList) {

			// Check that settlement name is valid.
			if ((settlement.name == null) || (settlement.name.isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.nameMissing")); //$NON-NLS-1$
			}

			// Check if population is valid.
			if ((settlement.population == null) || (settlement.population.isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.populationMissing")); //$NON-NLS-1$
			} else {
				try {
					int popInt = Integer.parseInt(settlement.population);
					if (popInt < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.populationTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.populationInvalid")); //$NON-NLS-1$
				}
			}

			// Check if number of robots is valid.
			if ((settlement.numOfRobots == null) || (settlement.numOfRobots.isEmpty())) {
				setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsMissing")); //$NON-NLS-1$
			} else {
				try {
					int num = Integer.parseInt(settlement.numOfRobots);
					if (num < 0) {
						setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsTooFew")); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsInvalid")); //$NON-NLS-1$
				}
			}
			
			String latError = Coordinates.checkLat(settlement.latitude);
			if (latError != null)
				setError(latError);
			String lonError = Coordinates.checkLon(settlement.longitude);
			if (lonError != null)
				setError(lonError);
			
			// Only check duplicate if no errors
			if ((latError == null) && (lonError == null)) {
				Coordinates c = new Coordinates(settlement.latitude, settlement.longitude);
				if (!usedCoordinates.add(c))
					setError(Msg.getString("Coordinates.error.latitudeLongitudeRepeating")); //$NON-NLS-1$
			}
			
			if (settlement.crew != null) {
				if (usedCrews.contains(settlement.crew)) {
					setError(Msg.getString("SimulationConfigEditor.error.duplicateCrew"));
				}
				else {
					usedCrews.add(settlement.crew);
				}
			}
		}
	}
	
	private void setError(String error) {
		if (errorMessage == null) {
			errorMessage = error;
		}
		else {
			errorMessage += ", " + error;
		}	
	}

	/**
	 * Add a partial Settlement with the minimum information. The rest is defaulted fron Sponsor & Template.
	 * @param sponsor
	 * @param template
	 * @param location
	 */
	public void addPartialSettlement(String sponsor, String template, Coordinates location) {
		InitialSettlement newRow = new InitialSettlement(tailorSettlementNameBySponsor(sponsor, 
											settlementInfoList.size()),
					sponsor, template,
					ConfigModelHelper.determineNewSettlementPopulation(template, settlementConfig),
					ConfigModelHelper.determineNewSettlementNumOfRobots(template, settlementConfig),
					location, null);
		settlementInfoList.add(toSettlementInfo(newRow));
		fireTableDataChanged();
	}


	public String getErrorMessage() {
		return errorMessage;
	}
}
