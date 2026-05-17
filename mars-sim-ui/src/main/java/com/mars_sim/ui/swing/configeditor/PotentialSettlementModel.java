/*
 * Mars Simulation Project
 * PotentialSettlementModel.java
 * @date 2026-05-17
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.configeditor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.AuthorityFactory;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.CoordinatesException;
import com.mars_sim.core.map.location.CoordinatesFormat;
import com.mars_sim.core.structure.SettlementTemplateConfig;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.components.ColumnSpec;

/**
 * Sharable table model for both Initial and Potential settlements.
 * Supports editting of cell values.
 */
abstract class PotentialSettlementModel extends AbstractTableModel {
	public static final int SETTLEMENT_COL = 0;
	public static final int SPONSOR_COL = 1;
	public static final int TEMPLATE_COL = 2;
	public static final int SETTLER_COL = 3;
	public static final int CREW_COL = 4;
    public static final int ARRIVAL_COL = 4;
	public static final int LOCN_COL = 5;

    // Lookup values for get/setValue methods; allows missing columns
    private static final int SETTLEMENT_VALUE = 100;
	private static final int SPONSOR_VALUE = 101;
	private static final int TEMPLATE_VALUE = 102;
	private static final int SETTLER_VALUE = 103;
	private static final int CREW_VALUE = 104;
	private static final int LOCN_VALUE = 105;
    private static final int ARRIVAL_VALUE = 106;
    
	// Inner class representing a settlement configuration.
	protected final class PotentialSettlementInfo {
		String name;
		String sponsor;
		String template;
		int population;
        int arrivalIn;
	    Coordinates location;
		String crew;
	}

    private ColumnSpec[] columns;
    private String errorMessage;
    protected List<PotentialSettlementInfo> settlementInfoList = new ArrayList<>();
    private AuthorityFactory raFactory;
    protected SettlementTemplateConfig settlementTemplateConfig;


    protected PotentialSettlementModel(boolean showInitials, SettlementTemplateConfig settlementTemplateConfig, AuthorityFactory raFactory) {
        this.settlementTemplateConfig = settlementTemplateConfig;
        this.raFactory = raFactory;

        if (showInitials)
            columns = new ColumnSpec[] {
                new ColumnSpec(SETTLEMENT_VALUE, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(SPONSOR_VALUE, Msg.getString("authority.singular"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(TEMPLATE_VALUE, Msg.getString("settlement.template"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(SETTLER_VALUE, Msg.getString("settlement.population"), Integer.class, ColumnSpec.STYLE_INTEGER),
                new ColumnSpec(CREW_VALUE, Msg.getString("SimulationConfigEditor.column.crew"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(LOCN_VALUE, Msg.getString("entity.coordinates"), String.class, ColumnSpec.STYLE_DEFAULT)
        };
        else {
            columns = new ColumnSpec[] {
                new ColumnSpec(SETTLEMENT_VALUE, Msg.getString("entity.name"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(SPONSOR_VALUE, Msg.getString("authority.singular"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(TEMPLATE_VALUE, Msg.getString("settlement.template"), String.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(SETTLER_VALUE, Msg.getString("settlement.population"), Integer.class, ColumnSpec.STYLE_INTEGER),
                new ColumnSpec(ARRIVAL_VALUE, Msg.getString("SimulationConfigEditor.column.arrivalIn"), Integer.class, ColumnSpec.STYLE_DEFAULT),
                new ColumnSpec(LOCN_VALUE, Msg.getString("entity.coordinates"), String.class, ColumnSpec.STYLE_DEFAULT)
            };
        }
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
	@Override
	public Class<?> getColumnClass(int c) {
		return columns[c].type();
	}

	@Override
	public String getColumnName(int columnIndex) {
		if ((columnIndex > -1) && (columnIndex < columns.length)) {
			return columns[columnIndex].name();
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
		Object result = null;

		if ((row > -1) && (row < getRowCount())) {
			var info = settlementInfoList.get(row);
			if ((column > -1) && (column < getColumnCount())) {
                var spec = columns[column];
                result = switch (spec.id()) {
                    case SETTLEMENT_VALUE -> info.name;
                    case SPONSOR_VALUE -> info.sponsor;
                    case TEMPLATE_VALUE -> info.template;
                    case SETTLER_VALUE -> info.population;
                    case CREW_VALUE -> info.crew;
                    case ARRIVAL_VALUE -> info.arrivalIn;
                    case LOCN_VALUE -> info.location.getFormattedString();
                    default -> result;
                };
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
			var info = settlementInfoList.get(rowIndex);
			if ((columnIndex > -1) && (columnIndex < getColumnCount())) {
                errorMessage = null;
                var spec = columns[columnIndex];
				switch (spec.id()) {
				
				case SETTLEMENT_VALUE:
					info.name = (String) aValue;
					break;
					
				case SPONSOR_VALUE:
					String newSponsor = (String) aValue;
					if (!info.sponsor.equals(newSponsor)) {
						info.sponsor = newSponsor;
						info.name = tailorSettlementNameBySponsor(info.sponsor, rowIndex);
					}
					break;	
					
				case TEMPLATE_VALUE:
					info.template = (String) aValue;
					info.population = ConfigModelHelper.determineNewSettlementPopulation(info.template, settlementTemplateConfig);
					break;
					
				case SETTLER_VALUE:
					info.population = (Integer) aValue;
					break;
					
				case CREW_VALUE:
                    if (isCrewValid((String) aValue, info)) {
                        info.crew = (String) aValue;
                    }
					break;
				
				case ARRIVAL_VALUE:
					info.arrivalIn = (Integer) aValue;
					break;
				
				case LOCN_VALUE:
					String locnStr = ((String) aValue).trim();
                    try {
                        var c = CoordinatesFormat.fromString(locnStr);
                        if (isLocnValid(c, info)) {
                            info.location = c;
                        }
                    } catch (CoordinatesException e) {
                        setError(e.getMessage());
                    }
					break;

				default:
					break;
				}
			}

			fireTableDataChanged();
		}
	}

    private boolean isLocnValid(Coordinates newLocn, PotentialSettlementInfo info) {
        if (newLocn != null) {
            for (var settlement : settlementInfoList) {
                if ((settlement != info) && newLocn.equals(settlement.location)) {
                    setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeRepeating"));
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isCrewValid(String newCrew, PotentialSettlementInfo info) {
        if (newCrew != null) {
            for (var settlement : settlementInfoList) {
                if ((settlement != info) && newCrew.equals(settlement.crew)) {
                    setError(Msg.getString("SimulationConfigEditor.error.duplicateCrew"));
                    return false;
                }
            }
        }

        return true;
    }
	
    /**
	 * Returns a random settlement name tailored by the sponsor
	 * 
	 * @param sponsor
	 * @return
	 */
	protected String tailorSettlementNameBySponsor(String sponsor, int index) {
		Authority ra = raFactory.getItem(sponsor);

		List<String> usedNames = new ArrayList<>();
		
		// Add configuration settlements from table data.
		for (int x = 0; x < getRowCount(); x++) {
			String name = (String) getValueAt(x, SETTLEMENT_COL);
			usedNames.add(name);

		}

		// Gets a list of settlement names that are tailored to this country
		var name = ra.getSettlementNames().generateName(usedNames);

		if (name == null)
			name = "Settlement #" + index;
		return name;
	}
	
	private void setError(String error) {
		if (errorMessage == null) {
			errorMessage = error;
		}
		else {
			errorMessage += ", " + error;
		}	
	}

    public String getErrorMessage() {
        return errorMessage;
    }
    
	/**
	 * Remove a set of entries from the table.
	 * 
	 * @param rowIndexes an array of row indexes of the entries to remove.
	 */
	public void removeEntries(int[] rowIndexes) {
		List<PotentialSettlementInfo> removedSettlements = new ArrayList<>(rowIndexes.length);

		for (int x = 0; x < rowIndexes.length; x++) {
			if ((rowIndexes[x] > -1) && (rowIndexes[x] < getRowCount())) {
				removedSettlements.add(settlementInfoList.get(rowIndexes[x]));
			}
		}

		removedSettlements.forEach(s -> {
			settlementInfoList.remove(s);
		});

		fireTableDataChanged();
	}
}
