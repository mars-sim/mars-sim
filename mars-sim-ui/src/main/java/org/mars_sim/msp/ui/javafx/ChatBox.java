package org.mars_sim.msp.ui.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
	
@SuppressWarnings("restriction")
public class ChatBox extends BorderPane {
    protected final TextArea textArea = new TextArea();
    protected final TextField textField = new TextField();

    protected final List<String> history = new ArrayList<>();
    protected int historyPointer = 0;

    private Consumer<String> onMessageReceivedHandler;

    public ChatBox() {
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        setCenter(textArea);
        setBottom(textField);
         
        textField.addEventHandler(KeyEvent.KEY_RELEASED, keyEvent -> {
        	keyHandler(keyEvent);
        });

    }

    public void keyHandler(final KeyEvent keyEvent){
        switch (keyEvent.getCode()) {
        case ENTER:
            String text = textField.getText();
            textArea.appendText("You : " + text + System.lineSeparator());
            if (text != null && !text.trim().isEmpty()) {	
            	history.add(text);
            	historyPointer++;      
	            if (onMessageReceivedHandler != null) {
	                onMessageReceivedHandler.accept(text);
	            }
	            textField.clear();
            }
            break;
        case UP:
            if (historyPointer == 0) {
                break;
            }
            historyPointer--;
            ChatUtil.runSafe(() -> {
                textField.setText(history.get(historyPointer));
                textField.selectAll();
            });
            break;
        case DOWN:
            if (historyPointer == history.size() - 1) {
                break;
            }
            historyPointer++;
            ChatUtil.runSafe(() -> {
                textField.setText(history.get(historyPointer));
                textField.selectAll();
            });
            break;
        default:
            break;
    }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        textField.requestFocus();
    }

    public void setOnMessageReceivedHandler(final Consumer<String> onMessageReceivedHandler) {
        this.onMessageReceivedHandler = onMessageReceivedHandler;
    }

    public void clear() {
        ChatUtil.runSafe(() -> textArea.clear());
    }

    public void print(final String text) {
        Objects.requireNonNull(text, "text");
        ChatUtil.runSafe(() -> textArea.appendText(text));
    }

    public void println(final String text) {
        Objects.requireNonNull(text, "text");
        ChatUtil.runSafe(() -> textArea.appendText(text + System.lineSeparator()));
    }

    public void println() {
        ChatUtil.runSafe(() -> textArea.appendText(System.lineSeparator()));
    }
    
    public TextArea getTextArea() {
    	return textArea;
    }

    public TextField getTextField() {
    	return textField;
    }
}