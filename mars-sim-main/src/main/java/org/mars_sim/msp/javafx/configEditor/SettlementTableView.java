package org.mars_sim.msp.javafx.configEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
 
public class SettlementTableView {
 
    public static final String Column0MapKey = "0";
    public static final String Column1MapKey = "1";
    public static final String Column2MapKey = "2";
    public static final String Column3MapKey = "3";
    public static final String Column4MapKey = "4";
    public static final String Column5MapKey = "5";
  
	private SimulationConfig simulationConfig = SimulationConfig.instance();
	
	private List<SettlementInfo> settlements = new ArrayList<>();
	
	private ObservableList<Map> allData;
 
	private TableView table_view;
	
    public TableView createGUI() {
   	
        TableColumn<Map, String> dataColumn0 = new TableColumn<>("Settlement");
        TableColumn<Map, String> dataColumn1 = new TableColumn<>("Template");
        TableColumn<Map, String> dataColumn2 = new TableColumn<>("Settlers");
        TableColumn<Map, String> dataColumn3 = new TableColumn<>("Bots");
        TableColumn<Map, String> dataColumn4 = new TableColumn<>("Latitude");
        TableColumn<Map, String> dataColumn5 = new TableColumn<>("Longitude");
        
        dataColumn0.setCellValueFactory(new MapValueFactory(Column0MapKey));
        dataColumn0.setMinWidth(220);
        dataColumn1.setCellValueFactory(new MapValueFactory(Column1MapKey));
        dataColumn1.setMinWidth(220);
        dataColumn2.setCellValueFactory(new MapValueFactory(Column2MapKey));
        dataColumn2.setMinWidth(80);
        dataColumn3.setCellValueFactory(new MapValueFactory(Column3MapKey));
        dataColumn3.setMinWidth(80);
        dataColumn4.setCellValueFactory(new MapValueFactory(Column4MapKey));
        dataColumn4.setMinWidth(90);
        dataColumn5.setCellValueFactory(new MapValueFactory(Column5MapKey));
        dataColumn5.setMinWidth(90);
       
        table_view = new TableView<>(generateDataInMap());
 
        table_view.setEditable(true);
        table_view.getSelectionModel().setCellSelectionEnabled(true);
        table_view.getColumns().setAll(dataColumn0, dataColumn1, dataColumn2, dataColumn3, dataColumn4, dataColumn5);
        Callback<TableColumn<Map, String>, TableCell<Map, String>>
            cellFactoryForMap = new Callback<TableColumn<Map, String>,
                TableCell<Map, String>>() {
                    @Override
                    public TableCell call(TableColumn p) {
                        return new TextFieldTableCell(new StringConverter() {
                            @Override
                            public String toString(Object t) {
                            	updateSettlementInfo();
                            	//System.out.println("t.toString() is "+ t.toString());                    	
                                return t.toString();
                            }
                            @Override
                            public Object fromString(String string) {
                            	updateSettlementInfo();
                            	//System.out.println("string() is "+ string);
                                return string;
                            }                                    
                        });
                    }
        };
        
        dataColumn0.setCellFactory(cellFactoryForMap);
        dataColumn1.setCellFactory(cellFactoryForMap);
        dataColumn2.setCellFactory(cellFactoryForMap);
        dataColumn3.setCellFactory(cellFactoryForMap);
        dataColumn4.setCellFactory(cellFactoryForMap);
        dataColumn5.setCellFactory(cellFactoryForMap);

        return table_view;
    }
 
    
    private ObservableList<Map> generateDataInMap() {
    	
        allData = FXCollections.observableArrayList();

		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
		
		settlements.clear();
		
		for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
			SettlementInfo info = new SettlementInfo();

			info.name = settlementConfig.getInitialSettlementName(x);
			info.template = settlementConfig.getInitialSettlementTemplate(x);
			info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
			info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
			info.latitude = settlementConfig.getInitialSettlementLatitude(x);
			info.longitude = settlementConfig.getInitialSettlementLongitude(x);

			settlements.add(info);			
			
            Map<String, String> dataRow = new HashMap<>();

            dataRow.put(Column0MapKey, info.name);
            dataRow.put(Column1MapKey, info.template);
            dataRow.put(Column2MapKey, info.population);
            dataRow.put(Column3MapKey, info.numOfRobots);
            dataRow.put(Column4MapKey, info.latitude);
            dataRow.put(Column5MapKey, info.longitude);
            
            allData.add(dataRow);
		}

        return allData;
    }
    
	public int getRowCount() {
		return settlements.size();
	}
	
	public ObservableList<Map> getAllData() {
		return allData;
	}
	
	public void loadDefaultSettlements() {
		reloadDefaultSettlements();
		//table_view.refresh();//.set.generateDataInMap();
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
		
		for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
			SettlementInfo info = new SettlementInfo();

			info.name = settlementConfig.getInitialSettlementName(x);
			info.template = settlementConfig.getInitialSettlementTemplate(x);
			info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
			info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
			info.latitude = settlementConfig.getInitialSettlementLatitude(x);
			info.longitude = settlementConfig.getInitialSettlementLongitude(x);

			settlements.add(info);			
			
            Map<String, String> dataRow = new HashMap<>();

            dataRow.put(Column0MapKey, info.name);
            dataRow.put(Column1MapKey, info.template);
            dataRow.put(Column2MapKey, info.population);
            dataRow.put(Column3MapKey, info.numOfRobots);
            dataRow.put(Column4MapKey, info.latitude);
            dataRow.put(Column5MapKey, info.longitude);
            
            allData.add(dataRow);
		}

    }
    
    public void addNewSettlement(SettlementInfo s) {
		
        Map<String, String> dataRow = new HashMap<>();

        dataRow.put(Column0MapKey, s.name);
        dataRow.put(Column1MapKey, s.template);
        dataRow.put(Column2MapKey, s.population);
        dataRow.put(Column3MapKey, s.numOfRobots);
        dataRow.put(Column4MapKey, s.latitude);
        dataRow.put(Column5MapKey, s.longitude);
        
        allData.add(dataRow);
    }
    
	/**
	 * Remove a set of settlements from the table.
	 * @param rowIndexes an array of row indexes of the settlements to remove.
	 */
	public void removeSettlements(int i) {//int[] rowIndexes) {
		//int l = rowIndexes.length;
		//for (int i=0; i<l; i++) {
			//allData.remove(i);
		//}	
	    //remove selected item from the table list
        allData.remove(table_view.getItems().get(i));        
	}
	
	public List<SettlementInfo> getSettlementInfo() {
		return settlements;
	}
	
	public void updateSettlementInfo() {
		
		settlements.clear();
	
		for (int x = 0; x < getRowCount(); x++) {	
			SettlementInfo info = new SettlementInfo();
			
			info.name = (String) allData.get(x).get("0");
			info.template = (String) allData.get(x).get("1");
			info.population = (String) allData.get(x).get("2");
			info.numOfRobots = (String) allData.get(x).get("3");
			info.latitude = (String) allData.get(x).get("4");
			info.longitude = (String) allData.get(x).get("5");
	
			settlements.add(info);			
			
	        Map<String, String> dataRow = new HashMap<>();
	
	        dataRow.put(Column0MapKey, info.name);
	        dataRow.put(Column1MapKey, info.template);
	        dataRow.put(Column2MapKey, info.population);
	        dataRow.put(Column3MapKey, info.numOfRobots);
	        dataRow.put(Column4MapKey, info.latitude);
	        dataRow.put(Column5MapKey, info.longitude);
	        
	        allData.add(dataRow);
		}
	}
}