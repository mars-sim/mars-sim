/**
 * Mars Simulation Project
 * TableViewCombo.java
 * @version 3.1.0 2016-08-09
 * @author Manny Kung
 */

package org.mars_sim.msp.javafx.configEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.javafx.configEditor.SettlementBase;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;

//import io.swagger.models.properties.IntegerProperty;


import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TableViewCombo {

    public static final int NUM_COLUMNS = 7;

	private String[] headers = new String[]{"Settlement","Template","Settlers",
            "Bots","Sponsor","Latitude","Longitude"};

	private static ReportingAuthorityType[] SPONSORS = UnitManager.SPONSORS;

    public static int NUM_SPONSORS = SPONSORS.length;
    
	private TableView<SettlementBase> table_view;
	
	private List<SettlementBase> settlements = new ArrayList<>();
	private List<String> settlementNames;
	//private Map<String, List<String>> settlementNamesMap = new HashMap<>();
	
	private List<SettlementTemplate> templates;
	
	private ObservableList<SettlementBase> allData;
	
	private SettlementConfig settlementConfig;
	
	private SimulationConfig simulationConfig;
	
	public TableViewCombo() {
		
		simulationConfig = SimulationConfig.instance();
		settlementConfig = simulationConfig.getSettlementConfiguration();
		settlementNames = settlementConfig.getSettlementNameList();
		templates = settlementConfig.getSettlementTemplates();
		
	}
 
	public TableView createGUI() {

        table_view = new TableView<>();
        table_view.setEditable(true);

        TableColumn<SettlementBase, String> nameCol = new TableColumn<>(headers[0]);
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setMinWidth(150);

        TableColumn<SettlementBase, String> templateCol = new TableColumn<>(headers[1]);
        templateCol.setCellValueFactory(cellData -> cellData.getValue().templateProperty());
        templateCol.setCellFactory(ComboBoxTableCell.forTableColumn(
        		templates.get(0).getTemplateName(), 
        		templates.get(1).getTemplateName(), 
        		templates.get(2).getTemplateName(), 
        		templates.get(3).getTemplateName(), 
        		templates.get(4).getTemplateName(), 
        		templates.get(5).getTemplateName()));
        templateCol.setMinWidth(250);
     
        TableColumn<SettlementBase, String> settlerCol = new TableColumn<>(headers[2]);
        settlerCol.setCellValueFactory(cellData -> cellData.getValue().settlerProperty());
        settlerCol.setCellFactory(TextFieldTableCell.forTableColumn());
        settlerCol.setMinWidth(50);

        //private ValidationSupport validationSupport = new ValidationSupport();
		//validationSupport.registerValidator(TextField, Validator.createEmptyValidator("Text is required"));
        
        TableColumn<SettlementBase, String> botCol = new TableColumn<>(headers[3]);
        botCol.setCellValueFactory(cellData -> cellData.getValue().botProperty());
        botCol.setCellFactory(TextFieldTableCell.forTableColumn());
        botCol.setMinWidth(50);
        
        TableColumn<SettlementBase, String> sponsorCol = new TableColumn<>(headers[4]);
        sponsorCol.setCellValueFactory(cellData -> cellData.getValue().sponsorProperty());
        sponsorCol.setCellFactory(ComboBoxTableCell.forTableColumn(
        		SPONSORS[0].toString(),
        		SPONSORS[1].toString(), 
        		SPONSORS[2].toString(), 
        		SPONSORS[3].toString(), 
        		SPONSORS[4].toString(),
        		SPONSORS[5].toString(),
        		SPONSORS[6].toString(),
        		SPONSORS[7].toString()));
        sponsorCol.setMinWidth(250);
       
        TableColumn<SettlementBase, String> latCol = new TableColumn<>(headers[5]);
        latCol.setCellValueFactory(cellData -> cellData.getValue().latitudeProperty());
        latCol.setCellFactory(TextFieldTableCell.forTableColumn());
        latCol.setMinWidth(70);

        TableColumn<SettlementBase, String> longCol = new TableColumn<>(headers[6]);
        longCol.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty());
        longCol.setCellFactory(TextFieldTableCell.forTableColumn());
        longCol.setMinWidth(70);
 
        table_view.getColumns().addAll(nameCol,templateCol,settlerCol,botCol,sponsorCol,latCol, longCol);
        table_view.getItems().addAll(generateDataInMap());
        
/*
        table_view.getItems().addAll(
            //new Base("Alpha Base", "Alpha Base (MD Phase 4)", 9, 24, "Mars Society"),
            //new Base("Schiaparelli Point", "Mars Direct Base (Phase 1)", 3, 4, "Mars Society"),
            //new Base("Port Zubrin", "Mars Direct Base (Phase 2)", 4, 8, "Mars Society")
            new SettlementBase("Alpha Base", "Alpha Base (MD Phase 4)", "9", "24", "Mars Society", "0.0 N", "0.0 W"),
            new SettlementBase("Schiaparelli Point", "Mars Direct Base (Phase 1)", "3", "4", "Mars Society", "5.0 N", "5.0 W"),
            new SettlementBase("Port Zubrin", "Mars Direct Base (Phase 2)", "4", "8", "Mars Society", "9.0 N", "9.0 W"));
*/
        
/*
        settlerCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>(){
            @Override
            public String toString(Integer object) {
                return object.toString();
            }
            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        }));
*/       
         return table_view;
    }

	public TableView getTableView() {
		return table_view;
	}
	
	/**
	 * Adds a new settlement to the table.
	 * @param settlement the settlement configuration.
	 */
	public void addSettlement(SettlementBase base) {
		settlements.add(base);
		addNewSettlement(base);
		//table_view.refresh();
		table_view.setItems(allData);
	}
	
	
	public void addNewSettlement(SettlementBase base) {     
		allData.add(base);
	}
	

    private ObservableList<SettlementBase> generateDataInMap() {
    	
        allData = FXCollections.observableArrayList();
		settlements.clear();

		int size = settlementConfig.getNumberOfInitialSettlements();

		for (int x = 0; x < size; x++) {

			SettlementBase base = new SettlementBase();

			base.setName(settlementConfig.getInitialSettlementName(x));
			base.setTemplate(settlementConfig.getInitialSettlementTemplate(x));
			base.setSettler(Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x)));
			base.setBot(Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x)));
			base.setSponsor(settlementConfig.getInitialSettlementSponsor(x));
			base.setLatitude(settlementConfig.getInitialSettlementLatitude(x));
			base.setLongitude(settlementConfig.getInitialSettlementLongitude(x));

			settlements.add(base);			
/*		
			List<String> texts = new ArrayList<>();
			texts.add(base.getName());
			texts.add(base.getTemplate());
			texts.add(base.getSettler());
			texts.add(base.getSettler());
			texts.add(base.getSponsor());
			texts.add(base.getLat());
			texts.add(base.getLong());
						
            Map<Integer, AutoFillTextBox> dataRow = new HashMap<>();

			List<AutoFillTextBox> boxes = new ArrayList<>();
			for (int j = 0; j < NUM_COLUMNS; j++) {
				AutoFillTextBox b = null;		
				//if (j==4)
				//	b = new AutoFillTextBox(createAutoCompleteData());
				//else
					b = new AutoFillTextBox();
				b.setFilterMode(false);
				b.getTextbox().setText(texts.get(j));
				boxes.add(b);				
				dataRow.put(j, b);
			}
			
			//Iterator<AutoFillTextBox> i = boxes.iterator();
			//while (i.hasNext()) {
			//	AutoFillTextBox b = i.next();
			//}
*/        
            allData.add(base);
		}

        return allData;
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

	/*
	 * Display the default settlement templates
	 */
	public void loadDefaultSettlements() {
		recreateDefaultSettlements();
		table_view.setItems(allData);
	}
	
	/*
	 * Recreate default settlement templates
	 */
    private void recreateDefaultSettlements() {
    	
        allData = FXCollections.observableArrayList();
		SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();		
		settlements.clear();
				
		SettlementBase base = new SettlementBase();

		int size = settlementConfig.getNumberOfInitialSettlements();
		for (int x = 0; x < size; x++) {	
			createRow(base, x);
		}

    }
    

    /*
     * Create each settlement row
     */
	public void createRow(SettlementBase base, int x) {
		base.setName(allData.get(x).getName().toString());
		base.setTemplate(allData.get(x).getTemplate().toString());
		base.setSettler(allData.get(x).getSettler().toString());
		base.setBot(allData.get(x).getBot().toString());
		base.setSponsor(allData.get(x).getSponsor().toString());
		base.setLatitude(allData.get(x).getLatitude().toString());
		base.setLongitude(allData.get(x).getLongitude().toString());
	
		settlements.add(base);			
	
		addNewSettlement(base);
	}
	
	public int getRowCount() {
		return getTableView().getItems().size();
	}
	
	public ObservableList<SettlementBase> getAllData() {
		return allData;
	}
	
	public List<SettlementBase> getSettlementBase() {
		return settlements;
	}
	

}