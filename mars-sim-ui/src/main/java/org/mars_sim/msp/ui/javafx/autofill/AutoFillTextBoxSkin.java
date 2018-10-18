/**
 * Mars Simulation Project
 * AutoFillTextBoxSkin.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.autofill;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
//import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
//import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * This class helps to change the skin of the AutoFillTextBox Control
 * <p>
 * @author Narayan G. Maharjan
 * @see <a href="http://www.blog.ngopal.com.np"> Blog </a>
 */
public class AutoFillTextBoxSkin<T> extends SkinBase<AutoFillTextBox<T>>
        implements ChangeListener<String>,
        EventHandler {

    //Final Static variables for Window Insets
    private final static int TITLE_HEIGHT = 28;

    //private final static int WINDOW_BORDER = 8;

    //This is listview for showing the matched words
    private ListView listview;

    //This is Textbox where user types
    private TextField textbox;
    //This is the main Control of AutoFillTextBox
    private AutoFillTextBox<?> autofillTextbox;

    //This is the ObservableData where the matching words are saved
    private ObservableList data;

    //This is the Popup where listview is embedded.
    private Popup popup;

    public Window getWindow() {
        return autofillTextbox.getScene().getWindow();
    }

    private String temporaryTxt = "";

    /**
     * ****************************
     * CONSTRUCTOR
     * <p>
     * @param text AutoTextBox ****************************
     */
	@SuppressWarnings("unchecked")
	public AutoFillTextBoxSkin(AutoFillTextBox<T> text) {
        super((AutoFillTextBox<T>) text);

        //variable Assignment
        autofillTextbox = text;

        //listview for autofill textbox
        listview = text.getListview();
        if (text.getFilterMode()) {
            // listview.getItems().clear();
            //listview.getItems().addAll(text.getData());
            listview.setItems(text.getData());
        }
        
        listview.itemsProperty().addListener(new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {
            	
                // Check if (listview != null && t1 != null)
            	if (listview != null && t1 != null) {
	                if (listview.getItems().size() > 0 && listview.getItems() != null) {
	                    showPopup();
	                } // Hiding textfield popup when no matches found
	                else {
	                    hidePopup();
	                }
            	}
                //else {
                //	hidePopup();
                //}
            }

        });
        //listview.getItems().addListener(this);
        listview.setOnMouseReleased(this);
        listview.setOnKeyReleased(this);
        //This cell factory helps to know which cell has been selected so that
        //when ever any cell is selected the textbox rawText must be changed
        listview.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> p) {
                //A simple ListCell containing only Label

                final ListCell cell = new ListCell() {
                    @Override
                    public void updateItem(Object item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {                   	
                        	//if (text.getFilterMode()) {     
                                setText(item.toString());
                        	//}                           
                        }
                    }

                };
                //A listener to know which cell was selected so that the textbox
                //we can set the rawTextProperty of textbox

                cell.focusedProperty().addListener(new InvalidationListener() {

                    // @Override
                    public void invalidated(Observable ove) {
                        ObservableValue<Boolean> ov = (ObservableValue<Boolean>) ove;
                        if (cell.getItem() != null && cell.isFocused()) {
                            //here we are using 'temporaryTxt' as temporary saving text
                            //If temporaryTxt length is 0 then assign with current rawText()
                            String prev = null;

                            //first check ...(either texmporaryTxt is empty char or not)
                            if (temporaryTxt.length() <= 0) {
                                //second check...
                                if (listview.getItems().size() != data.size()) {              	
                                	//if (text.getFilterMode()) {     
	                                    temporaryTxt = textbox.getText();
                                	//}
                                }
                            }

                            prev = temporaryTxt;
                            textbox.textProperty().removeListener(AutoFillTextBoxSkin.this);
                            //textbox.rawTextProperty().removeListener(AutoFillTextBoxSkin.this);
                            //textbox.setText(prev);
                            if (text.getFilterMode()) { 
                            	textbox.textProperty().setValue(cell.getItem().toString());
                            }
                            //textbox.rawTextProperty().setValue(cell.getItem().toString());
                            //textbox.rawTextProperty().addListener(AutoFillTextBoxSkin.this);
                            textbox.textProperty().addListener(AutoFillTextBoxSkin.this);
                            textbox.selectRange(prev.length(), cell.getItem().toString().length());

                        }
                    }

                });

                return cell;
            }

        });

        //main textbox
        textbox = text.getTextbox();
        //textbox.set
        //textbox.setSelectOnFocus(false);
        textbox.setOnKeyPressed(this);
        textbox.textProperty().addListener(this);

        textbox.focusedProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> ov, Object t, Object t1) {
                textbox.end();
            }

        });

        //popup
        popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(listview);

        //list data and sorted ordered
        data = text.getData();
        FXCollections.sort(data);
   
        //Adding textbox in this control Children
        getChildren().addAll(textbox);//, autofillTextbox.getClearButton());

    }

//    /**
//     * **************************************************************
//     * This is recursive function which gives the sum of X and Y position of all
//     * it's Parent and give the exact position where Popup needs to be visibled.
//     * <p>
//     * @param p javafx.scene.Parent
//     * @param w double
//     * @param h double
//     * @return
//     * <a href="http://download.oracle.com/javafx/2.0/api/javafx/geometry/Dimension2D.html">Dimension2D</a>
//     * ***************************************************************
//     */
//    /* public Dimension2D getDimension(Parent p, double w, double h){
//     * if(getScene().getRoot() == p)
//     * return new Dimension2D(w,h);
//     * else{
//     * return getDimension(p.getParent(),w+p.getLayoutX()+p.getTranslateX(),h+p.getLayoutY()+p.getTranslateY());
//     * }
//     * } */
//    //@Override
//   /* public void invalidated(ObservableValue ov) {
//     * if(ov.getValue().toString().length()<=0){
//     * temporaryTxt = "";
//     * if(autofillTextbox.getFilterMode()){ *
//     * //listview.getItems().clear();
//     * //listview.getItems().addAll(data);
//     * listview.setItems(data);
//     * showPopup();
//     *
//     * }
//     * else{
//     * hidePopup();
//     * }
//     * }
//     * } */
	
    /**
     * ********************************************************
     * Selects the current Selected Item from the list and the content of that
     * selected Item is set to textbox.
     * ********************************************************
     */
    public void selectList() {
        Object i = listview.getSelectionModel().getSelectedItem();
        if (i != null) {
            textbox.setText(listview.getSelectionModel().getSelectedItem().toString());
            listview.getItems().clear();
            textbox.requestFocus();
            textbox.requestLayout();
            textbox.end();
            temporaryTxt = "";
            hidePopup();
            //textbox.setPromptText("");
        }
    }

    /**
     * ****************************************************
     * This is the main event handler which handles all the event of the
     * listview and textbox
     * <p>
     * @param evt ****************************************************
     */
    @Override
    public void handle(Event evt) {

        /**
         * ******************************
         * EVENT HANDLING FOR 'TextBox' ******************************
         */
        if (evt.getEventType() == KeyEvent.KEY_PRESSED) {
            /* --------------------------------
             * - KeyEvent Handling for Textbox -
             * -------------------------------- */
            KeyEvent t = (KeyEvent) evt;
            if (t.getSource() == textbox) {         	
       	 
            	if (t.getCode() == KeyCode.BACK_SPACE && textbox.isFocused()) {
            		//textbox.setText("");
            		textbox.clear();
            	}
                //WHEN USER PRESS DOWN ARROW KEY FOCUS TRANSFER TO LISTVIEW
            	else if (t.getCode() == KeyCode.DOWN) {
                    if (popup.isShowing()) {
                        listview.requestFocus();
                        listview.getSelectionModel().select(0);
                    }
                }
            	
            	// Check for whitespaces
            	else if (t.getCode() == KeyCode.ENTER) {
              	                 	
                    String text = textbox.getText();
    	            if (text != "" && text != null && !text.trim().isEmpty()) {
    	            }
    	            else {
    	            	textbox.clear();
    	            }                   
                }

            }
        } /**
         * ******************************
         * EVENT HANDLING FOR 'LISTVIEW' ******************************
         */
        else if (evt.getEventType() == KeyEvent.KEY_RELEASED) {
            /* ---------------------------------
             * - KeyEvent Handling for ListView -
             * ---------------------------------- */
            KeyEvent t = (KeyEvent) evt;
            if (t.getSource() == listview) {
                if (t.getCode() == KeyCode.ENTER) {
                	  
                	// Check for whitespaces
                    String text = textbox.getText();
    	            if (text != "" && text != null && !text.trim().isEmpty()) {
    	            	selectList();
    	            }
    	            else {
    	            	 textbox.clear();
    	            }
                    
                    
                } else if (t.getCode() == KeyCode.UP) {
                    if (listview.getSelectionModel().getSelectedIndex() == 0) {
                        textbox.requestFocus();
                    }
                }

            }
        } else if (evt.getEventType() == MouseEvent.MOUSE_RELEASED) {
            /* -----------------------------------
             * - MouseEvent Handling for Listview -
             * ------------------------------------ */
            if (evt.getSource() == listview) {
                selectList();
            }
        }
    }

//    /* protected void layoutChildren() {
//     * double width = this.getWidth();
//     * double height = this.getHeight();
//     * textbox.resize(width, height);
//     * positionInArea(textbox, 0.0, 0.0, width, height, 0.0d, HPos.CENTER, VPos.CENTER);
//     *
//     * } */
    
    /**
     * A Popup containing Listview is trigged from this function This function
     * automatically resize it's height and width according to the width of
     * textbox and item's cell height
     */
    public void showPopup() {
        listview.setPrefWidth(textbox.getWidth());

        if (listview.getItems().size() > 6) {
            listview.setPrefHeight((6 * 24));
        } else {
            listview.setPrefHeight((listview.getItems().size() * 24));
        }

//        listview.impl_updatePG();
//        listview.impl_transformsChanged();
        //CALCULATING THE X AND Y POSITION FOR POPUP
        //Dimension2D dimen = getDimension(getParent(),getLayoutX()+getTranslateX(),getLayoutY()+getHeight() +getTranslateY());
        //SHOWING THE POPUP JUST BELOW TEXTBOX
        popup.show(
                getWindow(),
                getWindow().getX() + textbox.localToScene(0, 0).getX() + textbox.getScene().getX(),
                getWindow().getY() + textbox.localToScene(0, 0).getY() + textbox.getScene().getY() + TITLE_HEIGHT);
        //getWindow().getX()+dimen.getWidth()+WINDOW_BORDER,
        //getWindow().getY()+dimen.getHeight()+TITLE_HEIGHT);

        listview.getSelectionModel().clearSelection();
        listview.getFocusModel().focus(-1);
    }

    /**
     * This function hides the popup containing listview
     */
    public void hidePopup() {

        popup.hide();

    }

    /**
     * *********************************************
     * When ever the the rawTextProperty is changed then this listener is
     * activated
     * <p>
     * @param ov
     * @param t
     * @param t1 **********************************************
     */
    @Override
    public void changed(ObservableValue<? extends String> ov, String t, String t1) {

        if (ov.getValue().toString().length() > 0) {
            String txtdata = (textbox.getText()).trim();

            //Limit of data cell to be shown in ListView
            int limit = 0;
            if (txtdata.length() > 0) {
                ObservableList list = FXCollections.observableArrayList();
                String compare = txtdata.toLowerCase();
                for (Object dat : data) {
                    String str = dat.toString().toLowerCase();

                    if (str.startsWith(compare)) {
                        list.add(dat);
                        limit++;
                    }
                    if (limit == autofillTextbox.getListLimit()) {
                        break;
                    }
                }
                
                // Check listview.getItems() != null
                //if (!listview.getItems().isEmpty()) {// != null) {
	                if (listview.getItems().containsAll(list)
	                        && listview.getItems().size() == list.size() 
	                        && listview.getItems() != null) {
	                    showPopup();
	                } else {
	                    listview.setItems(list);
	                }
                //}
                //else {
                //	hidePopup();
                //}

            } else {
                //listview.getItems().clear();
                if (autofillTextbox.getFilterMode()) {
                    listview.setItems(data);
                } else {
                	//TODO: 2016-06-16 what is the use of setting listview.setItems(null)
                    //listview.setItems(null);
                }
                //  listview.getItems().addAll(data);

            }

        }

        if (ov.getValue().toString().length() <= 0) {
            temporaryTxt = "";
            if (autofillTextbox.getFilterMode()) {

                //listview.getItems().clear();
                //listview.getItems().addAll(data);
                listview.setItems(data);
                showPopup();

            } else {
                hidePopup();
            }
        }
    }

}
