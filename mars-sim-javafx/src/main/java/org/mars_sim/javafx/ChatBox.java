/**
 * Mars Simulation Project
 * ChatBox.java
 * @version 3.1.0 2016-10-06
 * @author Manny Kung
 */

package org.mars_sim.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.terminal.ChatMenu;
import org.mars_sim.msp.core.terminal.ChatUtils;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.javafx.autofill.AutoFillTextBox;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ChatBox extends BorderPane {

	public final static String STARFIELD = "/images/starfield.png";
	public final static String AUTOFILL = "/fxui/css/autofill/autofill.css";

	protected int historyPointer = 0;

	private int themeCache = -1;

	protected boolean hasPerson = false, reset = false;

	public final static int[] box_height = new int[] { 0, 0, 0, 0 };
	
	protected String image = MainScene.class.getResource(STARFIELD).toExternalForm();

	protected Label titleLabel;

	protected final TextArea textArea;
	
	protected final JFXButton broadcastButton;

	protected final AutoFillTextBox<String> autoFillTextBox;

	protected final List<String> history = new ArrayList<>();

	private Consumer<String> onMessageReceivedHandler;
	
	protected MainScene mainScene;
	
	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;

	/**
	 * Constructor for ChatBox
	 * 
	 * @param {{@link MainScene}
	 */
	public ChatBox(MainScene mainScene) {
		this.mainScene = mainScene;

		masterClock = sim.getMasterClock();

		this.setStyle("-fx-background-color: grey;"// #7ebcea;" //#426ab7;"//
				+ "-fx-background-color: linear-gradient(to bottom, -fx-base, derive(-fx-base,30%));"
				+ "-fx-background-radius: 3px;" + "-fx-text-fill: black;" + "-fx-border-color: white;"
				+ "-fx-border-radius: 3px;" + "-fx-border-width: 1px;" + "-fx-border-style: solid; ");

		box_height[0] = 256; // mainScene.getFlyout().getContainerHeight();
		box_height[1] = 512;// box_height[0] * 1.5D;
		box_height[2] = 768;// box_height[0] * 3D;
		box_height[3] = 1024;// box_height[0] * 4D;

		// Added autoCompleteData
		ObservableList<String> autoCompleteData = FXCollections.observableArrayList(CollectionUtils.createAutoCompleteKeywords());

		textArea = new TextArea();
		textArea.textProperty().addListener(new ChangeListener<Object>() {
		    @Override
		    public void changed(ObservableValue<?> observable, Object oldValue,
		            Object newValue) {
		    	textArea.setScrollTop(Double.MAX_VALUE); 
		    	// Note : this will scroll to the bottom
		        // while Double.MIN_VALUE to scroll to the top
		    }
		});
		// textArea.setPadding(new Insets(2, 0, 2, 0));
		// textArea.setPrefWidth(560);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		mainScene.setQuickToolTip(textArea, "Conversations on MarsNet");
		// ta.appendText("System : WARNING! A small dust storm 20 km away NNW may be
		// heading toward the Alpha Base" + System.lineSeparator());

		// Replaced textField with autoFillTextBox
		autoFillTextBox = new AutoFillTextBox<String>(autoCompleteData);
		autoFillTextBox.setPadding(new Insets(2, 0, 0, 0));
		autoFillTextBox.getStylesheets().addAll(AUTOFILL);
		autoFillTextBox.setStyle(
				// "-fx-background-color: white;"
				// + "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.8), 10, 0, 0, 0);"
				// + "-fx-text-fill: white;"
				"-fx-background-radius: 5px;");

		autoFillTextBox.setFilterMode(false);
		autoFillTextBox.getTextbox().addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
			keyHandler(keyEvent);
		});

		autoFillTextBox.getTextbox().setMaxWidth(565);
		autoFillTextBox.getTextbox().setMinWidth(565);
		autoFillTextBox.getTextbox().setPrefWidth(565);
		// autoFillTextBox.setStyle("-fx-font: 11pt 'Corbel';");
		// autoFillTextBox.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll
		// history"));
		mainScene.setQuickToolTip(autoFillTextBox, "Use UP/DOWN arrows to scroll history");
		autoFillTextBox.getTextbox().setPromptText("Type your msg here");// to broadcast to a channel");

		broadcastButton = new JFXButton(" Broadcast ".toUpperCase());
		broadcastButton.getStyleClass().clear();
		broadcastButton.getStyleClass().add("button-broadcast");
		// broadcastButton.setTooltip(new Tooltip ("Click or press Enter to send msg"));
		mainScene.setQuickToolTip(broadcastButton, "Click or press Enter to send msg");
		broadcastButton.setOnAction(e -> {
			String text = autoFillTextBox.getTextbox().getText();
			if (text != "" && text != null && !text.trim().isEmpty()) {
				hitEnter();
			} else {
				autoFillTextBox.getTextbox().clear();
			}
		});

		HBox hbox = new HBox();
		hbox.setSpacing(1);
		hbox.setPadding(new Insets(0, 0, 0, 0));
		hbox.getChildren().addAll(broadcastButton, autoFillTextBox);// .getTextbox());

		titleLabel = new Label("  " + Msg.getString("ChatBox.title")); //$NON-NLS-1$

		setCenter(textArea);
		setBottom(hbox);
	}

	/**
	 * Checks if it is in GUI mode or console mode
	 */
	public void checkConnection() {
		if (ChatUtils.getConnectionMode() == 0) {
			textArea.appendText(System.lineSeparator() + "Cannot establish more than one line of connections. Please type 'exit' to leave the chat terminal first." + System.lineSeparator());			
		}
		
		else if (ChatUtils.getConnectionMode() == -1) {
			// Set the GUI mode
			ChatUtils.setConnectionMode(1);
			
			textArea.appendText(System.lineSeparator() + "<< Connection to MarsNet established >>" + System.lineSeparator());
	
			int rand = RandomUtil.getRandomInt(2);
	
			if (rand == 0)
				textArea.appendText("System : how can I help you ? /h for help");
			else if (rand == 1)
				textArea.appendText("System : how may I assist you ? /h for help");
			else if (rand == 2)
				textArea.appendText("System : Is there anything I can help ? /h for help");
	
			textArea.appendText(System.lineSeparator());
		}
	}

	/*
	 * Display the initial system greeting and update the css style
	 */
	public void update() {

		int theme = MainScene.getTheme();
		if (themeCache != theme) {
			themeCache = theme;
			if (theme == 7) {
				broadcastButton.getStylesheets().clear();
				// broadcastButton.setStyle("-fx-text-fill: #E5AB00;");
				broadcastButton.getStyleClass().clear();
				broadcastButton.getStyleClass().add("button-broadcast");
				broadcastButton.getStylesheets()
						.add(getClass().getResource(MainScene.ORANGE_CSS_THEME).toExternalForm());

				titleLabel.getStylesheets().clear();
				titleLabel.setStyle("-fx-text-fill: #E5AB00;" + " -fx-font: bold 12pt 'Corbel';"
				// + " -fx-effect: dropshadow( one-pass-box , orange , 8 , 0.0 , 2 , 0 );"
				);
				textArea.getStylesheets().clear();
				textArea.getStylesheets().add(getClass().getResource(MainScene.ORANGE_CSS_THEME).toExternalForm());
			} else if (theme == 0 || theme == 6) {
				broadcastButton.getStylesheets().clear();
				// broadcastButton.setStyle("-fx-text-fill: #3291D2;");
				broadcastButton.getStyleClass().clear();
				broadcastButton.getStyleClass().add("button-broadcast");
				broadcastButton.getStylesheets().add(getClass().getResource(MainScene.BLUE_CSS_THEME).toExternalForm());
				titleLabel.getStylesheets().clear();
				titleLabel.setStyle("-fx-text-fill: #3291D2;" + " -fx-font: bold 12pt 'Corbel';"
				// + " -fx-effect: dropshadow( one-pass-box , blue , 8 , 0.0 , 2 , 0 );"
				);
				textArea.getStylesheets().clear();
				textArea.getStylesheets().add(getClass().getResource(MainScene.BLUE_CSS_THEME).toExternalForm());
			}

			textArea.setStyle("-fx-text-fill: black;");
			// textArea.setStyle("-fx-background-color: black;");
			textArea.positionCaret(textArea.getText().length());
		}
	}

	
	/**
	 * Close the chat box
	 * 
	 * @param disconnected
	 */
	public void closeChatBox(boolean disconnected) {

		if (disconnected) {
//    		textArea.appendText(YOU_PROMPT + "Farewell!" + System.lineSeparator() + "<< Disconnected from MarsNet >>" + System.lineSeparator() + System.lineSeparator());
			ChatUtils.personCache = null;
			ChatUtils.robotCache = null;
			ChatUtils.settlementCache = null;
			ChatUtils.vehicleCache = null;
			
			ChatUtils.setConnectionMode(-1);
		}

		mainScene.getFlyout().hide();

	}

	/**
	 * Processes a question and return an answer regarding an unit
	 * 
	 * @param text
	 */
	public void ask(String text) { 
//		System.out.println("askQuestion() in ChatBox");
		String questionText = "";
		String responseText = "";
		
		// if reseting the box height
		questionText = resetBoxHeight(text)[0];
		responseText = resetBoxHeight(text)[1];
			
        //ChatUtils.setConnectionMode(false);
		String[] ss =  ChatUtils.askQuestion(text);
		
		// Obtain responses
		questionText = ss[0];
		responseText = ss[1];

		// print question
		textArea.appendText(questionText);
		textArea.appendText(System.lineSeparator());

		// print response
		textArea.appendText(responseText);
		textArea.appendText(System.lineSeparator());
		
		//textArea.positionCaret(textArea.length());
	}
	
	/*
	 * Parses the text and interprets the contents in the chat box
	 * 
	 * @param input text
	 */
	public void parse(String text) {
//		System.out.println("parseText() in ChatBox");
		String responseText = null;

		// System.out.println("A: text is " + text + ". Running parseText()");
		text = text.trim();
		int len = text.length();
		
		if (ChatMenu.isPause(text)){
			if (masterClock.isPaused()) {
				masterClock.setPaused(false, false);
				responseText = "The simulation is now unpaused.";
			}
			else {
				masterClock.setPaused(true, false);
				responseText = "The simulation is now paused.";
			}
		}
		
		else if (ChatUtils.isQuitting(text)) {
			String[] txt = ChatUtils.farewell(ChatUtils.SYSTEM_PROMPT, false);
			// questionText = txt[0];
			responseText = txt[1];
			// Close the chat box
			closeChatBox(true);
		}
		
		else if (ChatUtils.checkExpertMode(text)) {
			ChatUtils.toggleExpertMode();
			responseText = "Set Expert Mode to " + ChatUtils.isExpertMode();// + System.lineSeparator();
		}
		
		// Add changing the height of the chat box
		else if (text.equalsIgnoreCase("/y1")) {
			responseText = ChatUtils.SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 256 pixels.";
			mainScene.setChatBoxPaneHeight(box_height[0]);
			closeChatBox(false);
			// mainScene.ToggleMarsNetButton(true);
			mainScene.getFlyout().show(mainScene.getMarsNetBtn(), PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);

		}

		else if (text.equalsIgnoreCase("/y2")) {
			responseText = ChatUtils.SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 512 pixels.";
			mainScene.setChatBoxPaneHeight(box_height[1]);
			closeChatBox(false);
			mainScene.getFlyout().show(mainScene.getMarsNetBtn(), PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
			// mainScene.ToggleMarsNetButton(true);
			// mainScene.getFlyout().flyout();
		}

		else if (text.equalsIgnoreCase("/y3")) {
			responseText = ChatUtils.SYSTEM_PROMPT + "Close the chat box to reset the chat box height to 768 pixels.";
			mainScene.setChatBoxPaneHeight(box_height[2]);
			closeChatBox(false);
			mainScene.getFlyout().show(mainScene.getMarsNetBtn(), PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);
			
		}

		else if (text.equalsIgnoreCase("/y4")) {
			mainScene.setChatBoxPaneHeight(box_height[3]);
			responseText = ChatUtils.SYSTEM_PROMPT + "Will close the chat box to reset the chat box height to 1024 pixels.";
			closeChatBox(false);
			mainScene.getFlyout().show(mainScene.getMarsNetBtn(), PopupVPosition.TOP, PopupHPosition.RIGHT, -15, 35);

		}
	
		else {
	        //ChatUtils.setConnectionMode(false);
			// Call ChatUtils' parseText
			responseText = ChatUtils.askSystem(text);
		}
		
		if (len > 0)
			textArea.appendText(responseText + System.lineSeparator());
	}

	/**
	 * Processes the textfield input
	 */
	public void hitEnter() {
//		System.out.println("hitEnter()");
		String text = autoFillTextBox.getTextbox().getText();
	
		if (text != "" && text != null && !text.trim().isEmpty()) {
			textArea.appendText(ChatUtils.YOU_PROMPT + text + System.lineSeparator());

			// if no settlement, robot, person, or vehicle has been selected yet
			if (ChatUtils.personCache == null && ChatUtils.robotCache == null 
					&& ChatUtils.settlementCache == null && ChatUtils.vehicleCache == null) {	
				// Call parse() to obtain a new value of unit
				parse(text);
			} 
			
			else {
				// Call ask() to further engage the conversion
				ask(text);
				// Note : if all _Cache are null, then leave
				// ask() and go back to parse()
			}

			// Checks if the text already exists
			if (history.contains(text)) {
				history.remove(text);
				history.add(text);
			} 
			
			else {
				history.add(text);
				historyPointer++;
			}

			if (onMessageReceivedHandler != null) {
				onMessageReceivedHandler.accept(text);
			}
		}
	
		autoFillTextBox.getTextbox().clear();
		
	}

	/**
	 * Processes the textfield in the chat box and saves a history of input
	 * 
	 * @param keyEvent
	 */
	public void keyHandler(final KeyEvent keyEvent) {
		switch (keyEvent.getCode()) {
		case ENTER:
			String text = autoFillTextBox.getTextbox().getText();
			if (text != "" && text != null && !text.trim().isEmpty()) {
				hitEnter();
			} else {
				autoFillTextBox.getTextbox().clear();
			}
			break;

		case UP:
			if (historyPointer == 0) {
				break;
			}

			historyPointer--;
			// System.out.println("historyPointer is " + historyPointer);
			ChatSafe.runSafe(() -> {
				/// textField.setText(history.get(historyPointer));
				// textField.selectAll();
				autoFillTextBox.getTextbox().setText(history.get(historyPointer));
				autoFillTextBox.getTextbox().selectAll();
			});

			break;

		case DOWN:
			if (historyPointer >= history.size() - 1) {
				break;
			}

			historyPointer++;
			// System.out.println("historyPointer is " + historyPointer);
			ChatSafe.runSafe(() -> {
				// textField.setText(history.get(historyPointer));
				// textField.selectAll();
				autoFillTextBox.getTextbox().setText(history.get(historyPointer));
				autoFillTextBox.getTextbox().selectAll();
			});

			break;

		default:
			break;
		}

	}

	@Override
	public void requestFocus() {
		super.requestFocus();
		// textField.requestFocus();
		autoFillTextBox.getTextbox().requestFocus();
	}

	public void setOnMessageReceivedHandler(final Consumer<String> onMessageReceivedHandler) {
		this.onMessageReceivedHandler = onMessageReceivedHandler;
	}

	public void clear() {
		ChatSafe.runSafe(() -> textArea.clear());
	}

	public void print(final String text) {
		Objects.requireNonNull(text, "text");
		ChatSafe.runSafe(() -> textArea.appendText(text));
	}

	public void println(final String text) {
		Objects.requireNonNull(text, "text");
		ChatSafe.runSafe(() -> textArea.appendText(text + System.lineSeparator()));
	}

	public void println() {
		ChatSafe.runSafe(() -> textArea.appendText(System.lineSeparator()));
	}

	public TextArea getTextArea() {
		return textArea;
	}

	public AutoFillTextBox<String> getAutoFillTextBox() {
		return autoFillTextBox;
	}

	public Label getTitleLabel() {
		return titleLabel;
	}

	public JFXButton getBroadcastButton() {
		return broadcastButton;
	}

	public String[] resetBoxHeight(String text) {
	
		StringBuilder responseText = new StringBuilder();
		String questionText = "";
		
		// Add changing the height of the chat box
		if (text.equalsIgnoreCase("/y1")) {
			questionText = ChatUtils.REQUEST_HEIGHT_CHANGE + box_height[0] + " pixels.";
			responseText.append(ChatUtils.SYSTEM_PROMPT);
			responseText.append("Close the chat box to reset the chat box height to 256 pixels.");
			mainScene.setChatBoxPaneHeight(box_height[0]);
			closeChatBox(false);
		}

		else if (text.equalsIgnoreCase("/y2")) {
			questionText = ChatUtils.REQUEST_HEIGHT_CHANGE + box_height[1] + " pixels.";
			responseText.append(ChatUtils.SYSTEM_PROMPT);
			responseText.append("Close the chat box to reset the chat box height to 512 pixels.");
			mainScene.setChatBoxPaneHeight(box_height[1]);
			closeChatBox(false);
		}

		else if (text.equalsIgnoreCase("/y3")) {
			questionText = ChatUtils.REQUEST_HEIGHT_CHANGE + box_height[2] + " pixels.";
			responseText.append(ChatUtils.SYSTEM_PROMPT);
			responseText.append("Close the chat box to reset the chat box height to 768 pixels.");
			mainScene.setChatBoxPaneHeight(box_height[2]);
			closeChatBox(false);
		}

		else if (text.equalsIgnoreCase("/y4")) {
			questionText = ChatUtils.REQUEST_HEIGHT_CHANGE + box_height[3] + " pixels.";
			mainScene.setChatBoxPaneHeight(box_height[3]);
			responseText.append(ChatUtils.SYSTEM_PROMPT);
			responseText.append("Will close the chat box to reset the chat box height to 1024 pixels.");
			closeChatBox(false);

		}
		
		return new String[] { questionText, responseText.toString() };
		
	}
	
	
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		mainScene = null;
		if (history != null)
			history.clear();
		onMessageReceivedHandler = null;
	}

}