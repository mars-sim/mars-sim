/**
 * Mars Simulation Project
 * TableViewCombo.java
 * @version 3.1.0 2016-08-09
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.javafx.config.SettlementBase;


//import io.swagger.models.properties.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class TableViewCombo {

	/** default logger. */
	private static Logger logger = Logger.getLogger(TableViewCombo.class.getName());
	
    private static String sourceName = logger.getName();
    
    public static final int NUM_COLUMNS = 7;
    
	private String[] headers = new String[]{"Settlement", "Template", "Settlers",
            								"Bots", "Sponsor", "Latitude", "Longitude"};

	private static ReportingAuthorityType[] SPONSORS = ReportingAuthorityType.SPONSORS_LONG;

    public static int NUM_SPONSORS = SPONSORS.length;

	private TableView<SettlementBase> table_view;

	private TableColumn<SettlementBase, String> latCol, longCol, settlerCol, botCol, templateCol;

	private List<SettlementBase> settlements = new ArrayList<>();

	private List<SettlementTemplate> templates;

	private ObservableList<SettlementBase> allData = FXCollections.observableArrayList();

	private SettlementConfig settlementConfig;

	private SimulationConfig simulationConfig;

	private ScenarioConfigEditorFX editor;

	private ListChangeListener<Object> listener;
	
	private int[] populations = new int[] {4, 8, 12, 24, 4, 4};

	private int[] bots = new int[] {2, 4, 6, 12, 2, 2 };

	public TableViewCombo(ScenarioConfigEditorFX editor) {
		this.editor = editor;

		simulationConfig = SimulationConfig.instance();
		settlementConfig = simulationConfig.getSettlementConfiguration();
		//settlementConfigNames = settlementConfig.getSettlementNameList();
		templates = settlementConfig.getSettlementTemplates();

	}

	public TableView<SettlementBase> createGUI() {

        table_view = new TableView<>();
        table_view.setEditable(true);
		// attach a list change listener to allData
		//addListChangeLister();

        init();

        return table_view;
	}

	public void init() {
        TableColumn<SettlementBase, String> nameCol = new TableColumn<>(headers[0]);
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        //nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol = setCellFactory(nameCol);
        nameCol.setMinWidth(150);

        templateCol = new TableColumn<>(headers[1]);
        templateCol.setCellValueFactory(cellData -> cellData.getValue().templateProperty());
        templateCol.setCellFactory(ComboBoxTableCell.forTableColumn(
        		templates.get(0).getTemplateName(),
        		templates.get(1).getTemplateName(),
        		templates.get(2).getTemplateName(),
        		templates.get(3).getTemplateName(),
        		templates.get(4).getTemplateName(),
        		templates.get(5).getTemplateName()));
        templateCol.setMinWidth(250);

        settlerCol = new TableColumn<>(headers[2]);
        settlerCol.setCellValueFactory(cellData -> cellData.getValue().settlerProperty());
        //settlerCol.setCellFactory(TextFieldTableCell.forTableColumn());
        settlerCol = setCellFactory(settlerCol);
        settlerCol.setMinWidth(50);

        //private ValidationSupport validationSupport = new ValidationSupport();
		//validationSupport.registerValidator(TextField, Validator.createEmptyValidator("Text is required"));

        botCol = new TableColumn<>(headers[3]);
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
        		SPONSORS[7].toString()
//        		SPONSORS[8].toString()    		
        		));
        sponsorCol.setMinWidth(300);


        latCol = new TableColumn<>(headers[5]);
        latCol.setCellValueFactory(cellData -> cellData.getValue().latitudeProperty());
        //latCol.setCellFactory(TextFieldTableCell.forTableColumn());
        latCol = setCellFactory(latCol);
        latCol.setMinWidth(70);


        longCol = new TableColumn<>(headers[6]);
        longCol.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty());
        //longCol.setCellFactory(TextFieldTableCell.forTableColumn());
        longCol = setCellFactory(longCol);
        longCol.setMinWidth(70);

        table_view.getColumns().addAll(nameCol, templateCol ,settlerCol ,botCol ,sponsorCol ,latCol, longCol);
        table_view.getItems().addAll(generateDataInMap());

		// attach a list change listener to allData
		addListChangeListener();
    }

	public TableColumn<SettlementBase, String> setCellFactory(TableColumn<SettlementBase, String> col) {

        col.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>(){

            @Override
            public String fromString(String s) {
            	//System.out.println("Calling fromString()");
                return removeUnQualifiedChar(col, s, false);
            }

        	@Override
            public String toString(String s) {
            	//System.out.println("Calling toString()");
                return removeUnQualifiedChar(col, s, true);
            }

        }));

        return col;
	}

	public String removeUnQualifiedChar(TableColumn<SettlementBase, String> col, String s, boolean checkError) {
    	
		if (col == latCol || col == longCol) {
			
	    	s = // remove multiple dot to one single dot
	    			s.replaceAll("[\\.]+", ".")
					// remove multiple comma to one single comma
					.replaceAll("[\\,]+", ",")
					// remove excessive directional sign and convert lowercase to uppercase
					.replaceAll("[n]+", "N").replaceAll("[s]+", "S").replaceAll("[e]+", "E").replaceAll("[w]+", "W")
					// fill in a zero in case of a decimal separator followed by a whitespace
					.replaceAll("\\.\\s", ".0 ").replaceAll("\\,\\s", ",0 ")
					//replaceAll(". W", " W").replaceAll(". N", " N").replaceAll(". S", " S")
					// remove all whitespace 
					.replaceAll("[ ]+", "")
					// insert a whitespace right before the directional notation
					.replaceAll("E", " E").replaceAll("W", " W").replaceAll("N", " N").replaceAll("S", " S")
					//.replaceAll("[\\`\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\_\\+\\-\\=\\{\\}\\|\\;\\'\\:\\,\\/\\<\\>\\?]+", "");
					//.replaceAll("[`~!@#$%^&*()_+-={}|;':,/<>?]+", "");
		    		// remove all underscores
					.replaceAll("[\\_]+", "")
		    		// remove everything except for dot, letters, digits, underscores and whitespace.
					.replaceAll("[^\\w\\s\\.\\,]+", "");
					// remove dot if found elsewhere
					//.replaceAll("\\.\\s", "").replaceAll("\\s\\.", "").replaceAll("\\,\\s", "").replaceAll("\\s\\,", "");
			
    		//see http://stackoverflow.com/questions/13494912/java-regex-for-replacing-all-special-characters-except-underscore
    	}
    	else if (col == botCol || col == settlerCol) {
        	//System.out.println("Calling fromString()");
    		// remove multiple dot to one single dot
    		// remove multiple whitespace to a single whitespace
        	// remove a list of unqualified symbols
    		//s = s.replaceAll("[ ]+", "").replaceAll("[!@#$%^&)_+-={}|;':,/<>?.]+", "");
       		// remove all letters
        	s = s.replaceAll("[a-zA-Z]+", "")
    	    		// remove all whitespaces
        			.replaceAll("[ ]+", "")
    	    		// remove all punctuations
        			.replaceAll("[^\\P{Punct}]+", "");//"\\p{P}\\p{S}", "");

    	}

    	if (checkError) {
    		editor.checkForErrors();
        	checkCapacity(col);
    	}

		return s;
	}

	public void checkCapacity(TableColumn<SettlementBase, String> col) {
    	if (col == botCol) {

        	int size = allData.size();
    		for (int i = 0; i < size; i++) {
				String name = allData.get(i).getName().toString();
				String template = allData.get(i).getTemplate().toString();
				String bot = allData.get(i).getBot().toString();
				//System.out.println("template : " + template);// + " <> " + templates.get(i).getTemplateName());
				int botNum = Integer.parseInt(bot);
				//System.out.println("botNum : " + botNum);// + " <> " + bots[i]);

	    		for (int j = 0; j < 6; j++) {

					if (template.equalsIgnoreCase(templates.get(j).getTemplateName())) {
						if (botNum > bots[j])
							LogConsolidated.log(logger, Level.WARNING, 1000, sourceName, 
									"In " + name + ", you specify " + botNum + " bots–– more than the standard capacity of "
											+ bots[j] + " for " + template, null);
						break;
					}
	    		}
    		}
    	}

    	else if (col == settlerCol) {

        	int size = allData.size();
    		for (int i = 0; i < size; i++) {
				String name = allData.get(i).getName().toString();
				String template = allData.get(i).getTemplate().toString();
				String settler = allData.get(i).getSettler().toString();
				//System.out.println("template : " + template);// + " <> " + templates.get(i).getTemplateName());
				int popNum = Integer.parseInt(settler);
				//System.out.println("popNum : " + popNum);// + " <> " + populations[i]);

	    		for (int j = 0; j < 6; j++) {

					if (template.equalsIgnoreCase(templates.get(j).getTemplateName())) {
						if (popNum > populations[j])
							LogConsolidated.log(logger, Level.WARNING, 1000, sourceName, 
									"In " + name + ", you specify " + popNum 
									+ " settlers–– more than the standard capacity of " + populations[j] + " for " + template, null);
						break;
					}
	    		}
    		}
    	}
	}

	public TableView<SettlementBase> getTableView() {
		return table_view;
	}

	/**
	 * Adds a new settlement to the table.
	 * @param settlement the settlement configuration.
	 */
	public void addSettlement(SettlementBase base) {
		settlements.add(base);
		//createObservableList(base);
		allData.add(base);
		// attach a list change listener to allData
		addListChangeListener();
		//table_view.refresh();
		table_view.setItems(allData);
	}


	//public void createObservableList(SettlementBase base) {
	//	allData.add(base);
	//}

	/**
	 * Add a list change listener and watch for changes.
	 */
	public void removeListChangeListener() {

	    allData.removeListener(listener);
	    
	}
	
	/**
	 * Add a list change listener and watch for changes.
	 */
	public void addListChangeListener() {

	    listener = new ListChangeListener<Object>() {
	    	@Override
	    	public void onChanged(ListChangeListener.Change<?> change) {
	    		//System.out.println("onChange event: ");
	    		while (change.next()) {
	    			if (change.wasAdded() || change.wasPermutated() || change.wasReplaced() || change.wasUpdated()) {
	    				ObservableList<SettlementBase> list = (ObservableList<SettlementBase>) change.getList();
	    				//int i = change.getTo() - 1;
//	    				//System.out.println("i : " + i);
//	    				//String s = change.toString();
//	    				//System.out.println("s : " + s);
//	    				String name = list.get(i).getName().toString();
//
	    				for (int i=0 ; i< list.size(); i++) {
		    				SettlementBase base = list.get(i);
	
		    				String name = list.get(i).getName().toString();
		    				String template = list.get(i).getTemplate().toString();
		    				String settler = list.get(i).getSettler().toString();
		    				String bot = list.get(i).getBot().toString();
		    				String sponsor = list.get(i).getSponsor().toString();
		    				String latitude = list.get(i).getLatitude().toString();
		    				String longitude = list.get(i).getLongitude().toString();
		    				
		    				base.setName(name);
		    				base.setTemplate(template);
		    				base.setSettler(settler);
		    				base.setBot(bot);
		    				base.setSponsor(sponsor);
		    				base.setLatitude(latitude);
		    				base.setLongitude(longitude);
		    				
		    				System.out.println("new sponsor : " + sponsor);
	    				}
//}
//    					//System.out.println("template : " + template);
//	    				int popNum = Integer.parseInt(settler);
//	    				int botNum = Integer.parseInt(bot);
//	    				//System.out.println("popNum : " + popNum);
//	    				//System.out.println("botNum : " + botNum);
//
//	    				if (template.equalsIgnoreCase(templates.get(i).getTemplateName()) && popNum > populations[i])
//	    					System.out.println("Warning : the # of settlers is more than the base can hold!");
//
//	    				if (template.equalsIgnoreCase(templates.get(i).getTemplateName()) && botNum > bots[i])
//	    					System.out.println("Warning : the # of bot is more than what the base is designed to support!");
//
//	    				//System.out.println("name : " + name);
//
//	    				if (editor.getStartButton() != null) {
//		    				if (name.contains("?")) {
//		    					System.out.println("invalid settlement name !");
//
//		    					editor.getStartButton().setDisable(true);
//		    				}
//		    				else
//		    					editor.getStartButton().setDisable(false);
//	    				}
	    			}
	    		}
	    	}
	    };
	    
	    allData.addListener(listener);
	}

	private ObservableList<SettlementBase> generateDataInMap() {

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

            allData.add(base);

//			List<String> texts = new ArrayList<>();
//			texts.add(base.getName());
//			texts.add(base.getTemplate());
//			texts.add(base.getSettler());
//			texts.add(base.getSettler());
//			texts.add(base.getSponsor());
//			texts.add(base.getLat());
//			texts.add(base.getLong());
//
//            Map<Integer, AutoFillTextBox> dataRow = new HashMap<>();
//
//			List<AutoFillTextBox> boxes = new ArrayList<>();
//			for (int j = 0; j < NUM_COLUMNS; j++) {
//				AutoFillTextBox b = null;
//				//if (j==4)
//				//	b = new AutoFillTextBox(createAutoCompleteData());
//				//else
//					b = new AutoFillTextBox();
//				b.setFilterMode(false);
//				b.getTextbox().setText(texts.get(j));
//				boxes.add(b);
//				dataRow.put(j, b);
//			}
//
//			//Iterator<AutoFillTextBox> i = boxes.iterator();
//			//while (i.hasNext()) {
//			//	AutoFillTextBox b = i.next();
//			//}
		}

        return allData;
    }

	/**
	 * Remove a settlement from the table.
	 * @param rowIndex the row index of the settlement to be removed.
	 */
	public void removeSettlement(int i) {
	    // Remove selected item from the table list
        allData.remove(table_view.getItems().get(i));
		// Remove the list change listener to allData
        removeListChangeListener();
//
		// Attach the list change listener to allData
		addListChangeListener();
		
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
		// Remove the list change listener to allData
        removeListChangeListener();
        // create a new instance of SettlementBase
		SettlementBase base = new SettlementBase();
		//SettlementConfig settlementConfig = simulationConfig.getSettlementConfiguration();
		int size = settlementConfig.getNumberOfInitialSettlements();
		//System.out.println("size : " + size);
		for (int x = 0; x < size; x++) {
			createARow(base, x);
		}
		// Attach a list change listener to allData
		addListChangeListener();
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

		System.out.println("TableViewComobo : createARow : " + allData.get(r).getSponsor().toString());

		settlements.add(base);
		//createObservableList(base);
		allData.add(base);

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
	
//	public void setSponsor(String destination, String sponsor) {
//		for (int i = 0; i < allData.size(); i++) {
//			if (allData.get(i).getName().equals(destination)) {
//				allData.get(i).setSponsor(sponsor);
//			}
//		}
//	}
	
	public void setSameSponsor(String destination, String sponsor) {
		for (int i = 0; i < allData.size(); i++) {
			if (allData.get(i).getName().equals(destination)) {
				allData.get(i).setSponsor(sponsor);
			}
		}
	}
		
//	public void setSameDestination(String destination) {
//		for (int i = 0; i < allData.size(); i++) {
//			if (allData.get(i).getName().equals(destination)) {
//				allData.get(i).setSponsor(sponsor);
//			}
//		}
//	}
	
	public void destroy() {
		table_view = null;
		latCol = null;
		longCol = null;
		settlerCol = null;
		botCol = null;
		templateCol = null;
		settlements = null;
		//settlementConfigNames = null;
		templates = null;
		allData = null;
		settlementConfig = null;
		simulationConfig = null;
		editor = null;
	}

}