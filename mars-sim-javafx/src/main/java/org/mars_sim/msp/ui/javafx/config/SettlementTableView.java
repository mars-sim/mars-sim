/**
 * Mars Simulation Project
 * SettlementTableView.java
 * @version 3.1.0 2016-07-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.validation.ValidationSupport;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
 
public class SettlementTableView {
 
    public static final int Column0MapKey = 0;
    public static final int Column1MapKey = 1;
    public static final int Column2MapKey = 2;
    public static final int Column3MapKey = 3;
    public static final int Column4MapKey = 4;
    public static final int Column5MapKey = 5;
    public static final int Column6MapKey = 6; 
    public static final int NUM_COLUMNS = 7;
    public static final int NUM_SPONSORS = ReportingAuthorityType.SPONSORS.length;
    
    private int rowCount = 2; //Will be re-calculated after if incorrect.

	private SimulationConfig simulationConfig;

	private SettlementConfig settlementConfig;
	
	private List<SettlementInfo> settlements = new ArrayList<>();
	
	private ObservableList<Map> allData;
 
	private TableView<Map> table_view;
	
    //private SpreadsheetView spreadSheetView;
	
	private final ReportingAuthorityType[] SPONSORS = ReportingAuthorityType.SPONSORS_LONG;
	
	private String[] headers = new String[]{"Settlement","Template","Settlers",
	                                      "Bots","Sponsor","Latitude","Longitude"};
	
	private List<String> settlementNames;
	
	private List<SettlementTemplate> templates;
	
//    private List<String> cityList = Arrays.asList("Shanghai", "Paris", "New York City", "Bangkok",
//            "Singapore", "Johannesburg", "Berlin", "Wellington", "London", "Montreal");
//    private final List<String> countryList = Arrays.asList("China", "France", "New Zealand",
//            "United States", "Germany", "Canada");
//    private final List<String> companiesList = Arrays.asList("", "ControlsFX", "Aperture Science",
//            "Rapture", "Ammu-Nation", "Nuka-Cola", "Pay'N'Spray", "Umbrella Corporation");

	private int[] col_widths = new int[]{200,220,80,80,194,90,90};

	private List<TableColumn<Map, String>> cols = new ArrayList<>();//TableColumn<>();

	private ValidationSupport validationSupport = new ValidationSupport();
	  
	public SettlementTableView() {
		
		simulationConfig = SimulationConfig.instance();
		settlementConfig = simulationConfig.getSettlementConfiguration();
		//settlementNames = settlementConfig.getSettlementNameList();
		templates = settlementConfig.getSettlementTemplates();
		
	}
	
//    public SpreadsheetView createGUI() {
//   	    	 	
//		for (int x = 0; x < NUM_COLUMNS; x++) {
//			TableColumn<Map, String> col = new TableColumn<>(headers[x]);
//			col.setCellValueFactory(new MapValueFactory(x));
//			col.setMinWidth(col_widths[x]);
//			cols.add(col);
//		}
//		
//		return buildSheet();
//	}

	public TableView createGUI() {

		for (int x = 0; x < NUM_COLUMNS; x++) {
			TableColumn<Map, String> col = new TableColumn<>(headers[x]);
			col.setCellValueFactory(new MapValueFactory(x));
			col.setMinWidth(col_widths[x]);
			cols.add(col);
		}

        table_view = new TableView<>(generateDataInMap());
        table_view.setEditable(true);
        table_view.getSelectionModel().setCellSelectionEnabled(true);
        table_view.getColumns().setAll(cols.get(0), cols.get(1), cols.get(2), 
        		cols.get(3), cols.get(4), cols.get(5), cols.get(6));

        Callback<TableColumn<Map, String>, TableCell<Map, String>>
            cellFactoryForMap = new Callback<TableColumn<Map, String>, TableCell<Map, String>>() {
                    @Override
                    public TableCell call(TableColumn p) {
                        return new TextFieldTableCell(new StringConverter() {
                            @Override
                            public String toString(Object o) {
                            	//updateSettlementInfo();
                            	//System.out.println("o.toString() is "+ o.toString());                    	
                        		//validationSupport.registerValidator((TextField) o, Validator.createEmptyValidator("Text is required"));
                            	return o.toString();
                            }
                            @Override
                            public Object fromString(String s) {
                            	//updateSettlementInfo();
                            	//System.out.println("string() is "+ s);
                                return s;
                            }                                    
                        });
                    }
        };
        
        for (int x = 0; x < NUM_COLUMNS; x++) {
			cols.get(x).setCellFactory(cellFactoryForMap);
		}
        
        return table_view;
    }
 
//    public SpreadsheetView buildSheet() {
//
//        //spreadSheetView = new SpreadsheetView(generateDataInMap());       
//        GridBase grid = new GridBase(rowCount, NUM_COLUMNS);
//        grid.setRowHeightCallback(new GridBase.MapBasedRowHeightFactory(generateRowHeight()));
//        buildGrid(grid);
//
//        spreadSheetView = new SpreadsheetView(grid);
//        //spreadSheetView.setShowRowHeader(rowHeader.isSelected());
//        //spreadSheetView.setShowColumnHeader(columnHeader.isSelected());
//        spreadSheetView.setEditable(true);//editable.isSelected());
//        //spreadSheetView.getSelectionModel().setSelectionMode(selectionMode.isSelected() ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
//
//        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        //spreadSheetView.getSelectionModel().setCellSelectionEnabled(true);
//        //spreadSheetView.getColumns().setAll(cols.get(0), cols.get(1), cols.get(2), 
//        //		cols.get(3), cols.get(4), cols.get(5), cols.get(6));
//   
//        return spreadSheetView;
//    }
 
//    private Map<Integer, Double> generateRowHeight() {
//        Map<Integer, Double> rowHeight = new HashMap<>();
//        rowHeight.put(1, 100.0);
//        return rowHeight;
//    }

    
//    /**
//     * Build the grid.
//     * @param grid
//    
//    private void buildGrid(GridBase grid) {
//
//        List<ObservableList<SpreadsheetCell>> cols = new ArrayList<>(grid.getColumnCount());
//        int colIndex = 0;
//        cols.add(createRow(grid, colIndex++));
//        
//        for (int i = colIndex; i < NUM_COLUMNS; ++i) {
//            final ObservableList<SpreadsheetCell> randomCol = FXCollections.observableArrayList();
//            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(0, i, 1, 1, "Random " + (i + 1));
//            cell.getStyleClass().add("first-cell");
//            randomCol.add(cell);
//
//            for (int column = 1; column < grid.getColumnCount(); column++) {
//                randomCol.add(generateCell(i, column, 1, 1));
//            }
//            cols.add(randomCol);
//        }
//
//        grid.setRows(cols);
// 
//
//      
//        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());
//        int rowIndex = 0;
//        rows.add(getSettlement(grid, rowIndex++));
//        for (int i = rowIndex; i < rowIndex + 2; ++i) {
//            final ObservableList<SpreadsheetCell> randomRow = FXCollections.observableArrayList();
//            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(i, 0, 1, 1, "Random " + (i + 1));
//            cell.getStyleClass().add("first-cell");
//            randomRow.add(cell);
//
//            for (int column = 1; column < grid.getColumnCount(); column++) {
//                randomRow.add(generateCell(i, column, 1, 1));
//            }
//            rows.add(randomRow);
//        }
//
//        grid.setRows(rows);
//        
//    }
// 
//    
//
//    /**
//     * Return a List of SpreadsheetCell with settlements
//     * @param grid
//     * @param row
//     * @return
//
//    private ObservableList<SpreadsheetCell> createRow(GridBase grid, int row) {
//
//        final ObservableList<SpreadsheetCell> settlementCells = FXCollections.observableArrayList();
//
//        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "");
//
//        ((SpreadsheetCellBase) cell).setTooltip("This cell displays a custom toolTip.");
//        cell.setEditable(false);
//        settlementCells.add(cell);
//
//        for (int column = 1; column < grid.getColumnCount(); ++column) {
//
//            cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1,
//                    settlementNames.get(column));
//            cell.setEditable(false);
//            //cell.getStyleClass().add("company");
//            settlementCells.add(cell);
//        }
//
//        return settlementCells;
//    }
//
//
//    /**
//     * Randomly generate a {@link SpreadsheetCell}.
//     
//    private SpreadsheetCell generateCell(int row, int column, int rowSpan, int colSpan) {
//
//        SpreadsheetCell cell = SpreadsheetCellType.LIST(countryList).createCell(row, column, rowSpan, colSpan,
//                    countryList.get((int) (Math.random() * 6)));
//            
//            //cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan,
//            //        cityList.get((int) (Math.random() * 10)));
//  
//        // Styling for preview
//        if (row % 5 == 0) {
//            cell.getStyleClass().add("five_rows");
//        }
//        return cell;
//    }
//

    private ObservableList<Map> generateDataInMap() {
    	
        allData = FXCollections.observableArrayList();
		//SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();		
		settlements.clear();

		SettlementInfo info = new SettlementInfo();

		int size = settlementConfig.getNumberOfInitialSettlements();
		for (int x = 0; x < size; x++) {

			info.name = settlementConfig.getInitialSettlementName(x);
			info.template = settlementConfig.getInitialSettlementTemplate(x);
			info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
			info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
			info.sponsor = settlementConfig.getInitialSettlementSponsor(x);
			info.latitude = settlementConfig.getInitialSettlementLatitude(x);
			info.longitude = settlementConfig.getInitialSettlementLongitude(x);

			settlements.add(info);			
			
			List<String> texts = new ArrayList<>();
			texts.add(info.name);
			texts.add(info.template);
			texts.add(info.population);
			texts.add(info.numOfRobots);
			texts.add(info.sponsor);
			texts.add(info.latitude);
			texts.add(info.longitude);
						
            Map<Integer, AutoFillTextBox<?>> dataRow = new HashMap<>();

			List<AutoFillTextBox<?>> boxes = new ArrayList<>();
			for (int j = 0; j < NUM_COLUMNS; j++) {
				AutoFillTextBox<?> b = null;		
				if (j==4)
					b = new AutoFillTextBox<>(createAutoCompleteData());
				else
					b = new AutoFillTextBox<>();
				b.setFilterMode(false);
				b.getTextbox().setText(texts.get(j));
				boxes.add(b);				
				dataRow.put(j, b);
			}
			
			//Iterator<AutoFillTextBox> i = boxes.iterator();
			//while (i.hasNext()) {
			//	AutoFillTextBox b = i.next();
			//}
        
            allData.add(dataRow);
		}

        return allData;
    }
    
	public int getRowCount() {
		return getTableView().getItems().size();
	}
	
	public ObservableList<Map> getAllData() {
		return allData;
	}
	
	public void loadDefaultSettlements() {
		reloadDefaultSettlements();
		table_view.setItems(allData);
	}
	
	/**
	 * Adds a new settlement to the table.
	 * @param settlement the settlement configuration.
	 */
	public void addSettlement(SettlementInfo settlement) {
		settlements.add(settlement);
		addNewSettlement(settlement);
		//table_view.refresh();
		table_view.setItems(allData);
	}
	
	public TableView getTableView() {
		return table_view;
	}
	

    private void reloadDefaultSettlements() {
    	
        allData = FXCollections.observableArrayList();
		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();		
		settlements.clear();
				
		SettlementInfo info = new SettlementInfo();

		int size = settlementConfig.getNumberOfInitialSettlements();
		for (int x = 0; x < size; x++) {	
			createDataRow(info, x);
		}

    }
    
    public void addNewSettlement(SettlementInfo info) {
		
        Map<Integer, String> dataRow = new HashMap<>();

        dataRow.put(Column0MapKey, info.name);
        dataRow.put(Column1MapKey, info.template);
        dataRow.put(Column2MapKey, info.population);
        dataRow.put(Column3MapKey, info.numOfRobots);
        dataRow.put(Column4MapKey, info.sponsor);
        dataRow.put(Column5MapKey, info.latitude);
        dataRow.put(Column6MapKey, info.longitude);
        
        allData.add(dataRow);
    }
    
	/**
	 * Remove a set of settlements from the table.
	 * @param rowIndexes an array of row indexes of the settlements to remove.
	 */
	public void removeSettlements(int i) {
	    //remove selected item from the table list
        allData.remove(table_view.getItems().get(i));  
		table_view.refresh();
		table_view.setItems(allData);
        
	}
	
	public List<SettlementInfo> getSettlementInfo() {
		return settlements;
	}
	
	public void updateSettlementInfo() {	
		settlements.clear();
		SettlementInfo info = new SettlementInfo();
		for (int x = 0; x < getRowCount(); x++) {	
			createDataRow(info, x);
		}
	}
	
	public void createDataRow(SettlementInfo info, int x) {

		info.name = allData.get(x).get(0).toString();
		info.template = allData.get(x).get(1).toString();
		info.population =  allData.get(x).get(2).toString();
		info.numOfRobots = allData.get(x).get(3).toString();
		info.sponsor = allData.get(x).get(4).toString();
		info.latitude = allData.get(x).get(5).toString();
		info.longitude = allData.get(x).get(6).toString();
	
		settlements.add(info);			
	
		addNewSettlement(info);
	}
	
    /**
     * Compiles the names of all sponsoring agencies into the autocomplete data list
     * @return ObservableList<String> 
     */
    public ObservableList<String> createAutoCompleteData() {
        List<String> sponsorList = new ArrayList<>();
        for (int i= 0; i<NUM_SPONSORS; i++) {
        	String s = SPONSORS[i].toString();//.getName();
        	//System.out.println(s);
        	sponsorList.add(s);
        }
		//List<String> sponsorList = Arrays.asList(sponsors.getName());	
    	return FXCollections.observableArrayList(sponsorList);
    } 
    
   	public void destroy() {
   		
   		simulationConfig = null;
   		//settlements.clear(); 
   		settlements = null;
   		//allData.clear(); 
   		allData = null;
   		table_view = null;
   	}
}