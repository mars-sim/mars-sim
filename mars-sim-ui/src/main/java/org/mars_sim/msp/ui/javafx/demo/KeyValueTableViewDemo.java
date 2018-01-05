package org.mars_sim.msp.ui.javafx.demo;


/**
public class KeyValueTableViewDemo extends Application {

	private KeyValueTableView<String, ArrayList<String>> keyValueTableView;

	@Override
	public void start(Stage primaryStage) throws Exception {
	    keyValueTableView = new KeyValueTableView<>();

	    ArrayList<String> vornamen = new ArrayList<>();
	    vornamen.add("Hans");
	    vornamen.add("Tobias");
	    vornamen.add("Peter");

	    ArrayList<String> nachnamen = new ArrayList<>();
	    nachnamen.add("Lahm");
	    nachnamen.add("Ribery");
	    nachnamen.add("Robben");

	    //CREATE KEY / VALUE PAIRS
	    ArrayList<Pair<String, ArrayList<String>>> data = new ArrayList<>();
	    data.add(new Pair<>("vornamen", vornamen));
	    data.add(new Pair<>("nachnamen", nachnamen));
	    data.add(new Pair<>("Ausblenden", new ArrayList<>()));
	    data.add(new Pair<>("Ausblenden 2", new ArrayList<>()));

	    keyValueTableView.setAllItems(FXCollections.observableArrayList(data));

	    //DONT SHOW THE skippedKeys ArrayList in output
	    ArrayList<String> skippedKeys = new ArrayList<>();
	    skippedKeys.add("Ausblenden");
	    skippedKeys.add("Ausblenden 2");

	    keyValueTableView.setSkippedKeys(FXCollections.observableArrayList(skippedKeys));

	    Scene scene = new Scene(keyValueTableView, 800, 600);
	    primaryStage.setScene(scene);
	    primaryStage.show();
	}

     * @param args the command line arguments

    public static void main(String[] args) {
        launch(args);
    }
}
*/