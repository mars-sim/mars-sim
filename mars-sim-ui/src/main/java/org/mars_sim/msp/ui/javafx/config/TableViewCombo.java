/**
 * Mars Simulation Project
 * TableViewCombo.java
 * @version 3.1.0 2016-08-09
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.javafx.config.SettlementBase;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;

//import io.swagger.models.properties.IntegerProperty;


import javafx.application.Application;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
	
	//private Formatter myLatitudeFormatter;
	
	private SettlementConfig settlementConfig;
	
	private SimulationConfig simulationConfig;
	
	private ScenarioConfigEditorFX editor;
	
	public TableViewCombo(ScenarioConfigEditorFX editor) {
		this.editor = editor;
		
		simulationConfig = SimulationConfig.instance();
		settlementConfig = simulationConfig.getSettlementConfiguration();
		settlementNames = settlementConfig.getSettlementNameList();
		templates = settlementConfig.getSettlementTemplates();
			
	}
 
	@SuppressWarnings("restriction")
	public TableView createGUI() {
		
        table_view = new TableView<>();
        table_view.setEditable(true);

        init();
        
        return table_view;
	}
	
	@SuppressWarnings("restriction")
	public void init() {
        TableColumn<SettlementBase, String> nameCol = new TableColumn<>(headers[0]);
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        //nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol = setCellFactory(nameCol);
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
        //settlerCol.setCellFactory(TextFieldTableCell.forTableColumn());
        settlerCol = setCellFactory(settlerCol);
        settlerCol.setMinWidth(50);

        //private ValidationSupport validationSupport = new ValidationSupport();
		//validationSupport.registerValidator(TextField, Validator.createEmptyValidator("Text is required"));
        
        TableColumn<SettlementBase, String> botCol = new TableColumn<>(headers[3]);
        botCol.setCellValueFactory(cellData -> cellData.getValue().botProperty());
        //botCol.setCellFactory(TextFieldTableCell.forTableColumn());
        botCol = setCellFactory(botCol);
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
        //latCol.setCellFactory(TextFieldTableCell.forTableColumn()); 
        latCol = setCellFactory(latCol);
        latCol.setMinWidth(70);


        TableColumn<SettlementBase, String> longCol = new TableColumn<>(headers[6]);
        longCol.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty());
        //longCol.setCellFactory(TextFieldTableCell.forTableColumn());  
        longCol = setCellFactory(longCol);
        longCol.setMinWidth(70); 
        
        table_view.getColumns().addAll(nameCol,templateCol,settlerCol,botCol,sponsorCol,latCol, longCol);
        table_view.getItems().addAll(generateDataInMap());
    }

	@SuppressWarnings("restriction")
	public TableColumn<SettlementBase, String> setCellFactory(TableColumn<SettlementBase, String> col) {
		
        col.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>(){
            @Override
            public String toString(String item) {            	
            	editor.checkForErrors();
                return item.toString();
            }
            
            @Override
            public String fromString(String string) {
                return string;
            }
            
        }));
        
        return col;
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
		createObservableList(base);
		//table_view.refresh();
		table_view.setItems(allData);
	}
	
	
	public void createObservableList(SettlementBase base) {     
		allData.add(base);
	}
	
	public void startNewObsList() {
	    allData = FXCollections.observableArrayList();
	    
	    allData.addListener(new ListChangeListener() {
	    	@Override
	    	public void onChanged(ListChangeListener.Change change) {
	    		//System.out.println("onChange event: ");
	    		while (change.next()) {
	    			if (change.wasAdded() || change.wasPermutated() || change.wasReplaced()) {
	    				int i = change.getTo() - 1;
	    				//System.out.println("i : " + i);
	    				//String s = change.toString();
	    				//System.out.println("s : " + s);
	    				ObservableList<SettlementBase> list = change.getList();
	    				String name = list.get(i).getName().toString();
	    				String template = list.get(i).getTemplate().toString();
	    				String settler = list.get(i).getSettler().toString();
	    				String bot = list.get(i).getBot().toString();
	    				String sponsor = list.get(i).getSponsor().toString();
	    				String latitude = list.get(i).getLatitude().toString();
	    				String longitude = list.get(i).getLongitude().toString();
	    				
	    				//System.out.println("name : " + name);
	    				
	    				if (editor.getStartButton() != null) {
		    				if (name.contains("?")) {
		    					System.out.println("invalid settlement name !");
		    					
		    					editor.getStartButton().setDisable(true);
		    				}
		    				else
		    					editor.getStartButton().setDisable(false);	
	    				}
	    			}
	    		}
	    	}
	    });
	}
	
    @SuppressWarnings("unchecked")
	private ObservableList<SettlementBase> generateDataInMap() {

    	startNewObsList();

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
	 * Remove a settlement from the table.
	 * @param rowIndex the row index of the settlement to be removed.
	 */
	public void removeSettlement(int i) {
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
		settlements.clear(); 
		// start a new allData
		startNewObsList();
        // create a new instance of SettlementBase
		SettlementBase base = new SettlementBase();

		//SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();		
		int size = settlementConfig.getNumberOfInitialSettlements();
		//System.out.println("size : " + size);
		for (int x = 0; x < size; x++) {	
			createARow(base, x);
		}

    }
    

    /*
     * Create a settlement in a row
     */
	public void createARow(SettlementBase base, int r) {
		//System.out.println("allData.get(r) : " + allData.get(r));
		//System.out.println("allData.get(r).getName() : " + allData.get(r).getName());
		base.setName(allData.get(r).getName().toString());
		base.setTemplate(allData.get(r).getTemplate().toString());
		base.setSettler(allData.get(r).getSettler().toString());
		base.setBot(allData.get(r).getBot().toString());
		base.setSponsor(allData.get(r).getSponsor().toString());
		base.setLatitude(allData.get(r).getLatitude().toString());
		base.setLongitude(allData.get(r).getLongitude().toString());
	
		settlements.add(base);			
	
		createObservableList(base);
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