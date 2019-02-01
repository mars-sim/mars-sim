package org.mars_sim.javafx.tools;


/*
 * #%L
 * JFXC
 * %%
 * Copyright (C) 2016 Jonato IT Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a TableView with generic pair rows.
 * The cellFactory uses the toString method of key and value.
 * @param <K> Key column
 * @param <V> Value column
 */
public class KeyValueTableView<K,V> extends TableView<Pair<K,V>> {

    //FIELDS
    public static final float MAX_WIDTH = 5000F;
    public static final float PREF_WIDTH = 80F;
    public static final float MIN_WIDTH = 0F;

    private ObservableList<Pair<K, V>> allItems;
    private ObservableList<K> skippedKeys;
    private Double keyMaxWidth;
    private Double valueMaxWidth;

    private Double keyPrefWidth;
    private Double valuePrefWidth;

    private Double keyMinWidth;
    private Double valueMinWidth;

    //COLS
    private TableColumn<Pair<K,V>, String> keyColumn;
    private TableColumn<Pair<K,V>, String> valueColumn;

    //CTRS

    /**
     * Create a key value table view.
     * @param items table items.
     * @param skipKeys invisible keys.
     * @param keyMaxWidth key max column width.
     * @param valueMaxWidth value max column width.
     */
    public KeyValueTableView(ObservableList<Pair<K,V>> items, ObservableList<K> skipKeys, Double keyMaxWidth, Double valueMaxWidth) {
        super(items);
        initTable();
        initCells();
        this.allItems = items;
        this.skippedKeys = skipKeys;
        setKeyMaxWidth(keyMaxWidth == null ? Double.MIN_VALUE : keyMaxWidth);
        setValueMaxWidth(valueMaxWidth == null ? Double.MIN_VALUE : valueMaxWidth);
        filterItems();
    }
    /**
     * Create a key value table view.
     * @param items table items.
     * @param skipKeys invisible keys.
     */
    public KeyValueTableView(List<Pair<K,V>> items, List<K> skipKeys)
    {
        this(FXCollections.observableArrayList(items), FXCollections.observableArrayList(skipKeys), null, null);
    }
    /**
     * Create a key value table view.
     * @param items table items.
     * @param skipKeys invisible keys.
     */
    public KeyValueTableView(ObservableList<Pair<K,V>> items, ObservableList<K> skipKeys)
    {
        this(items, skipKeys, null, null);
    }

    /**
     * Create a key value table view.
     * @param items table items.
     */
    public KeyValueTableView(ObservableList<Pair<K,V>> items){
        this(items, FXCollections.observableArrayList(new ArrayList<K>()), null, null);
    }

    /**
     * Create a key value table view.
     * @param items table items.
     */
    public KeyValueTableView(List<Pair<K,V>> items){
        this(FXCollections.observableArrayList(items), FXCollections.observableArrayList(new ArrayList<K>()), null, null);
    }

    public KeyValueTableView(){
        this(FXCollections.observableArrayList(new ArrayList<Pair<K,V>>()));
    }

    /// GET & SET
    public ObservableList<K> getSkippedKeys() {return skippedKeys;}
    public ObservableList<Pair<K, V>> getAllItems() {return allItems;}
    public Double getKeyMaxWidth() {return keyMaxWidth;}
    public Double getValueMaxWidth() {return valueMaxWidth;}
    public Double getValuePrefWidth() { return valuePrefWidth; }
    public Double getKeyPrefWidth() { return keyPrefWidth; }
    public Double getValueMinWidth() { return valueMinWidth; }
    public Double getKeyMinWidth() { return keyMinWidth; }


    /**
     * set a list of invisible keys.
     * @param skippedKeys filter skipped keys
     */
    public void setSkippedKeys(ObservableList<K> skippedKeys) {
        this.skippedKeys = skippedKeys;
        filterItems();
    }

    /**
     * set pref width for value column.
     * @param valuePrefWidth pref width
     */
    public void setValuePrefWidth(Double valuePrefWidth) {
        this.valuePrefWidth = valuePrefWidth;
        if(valuePrefWidth != Double.MIN_VALUE) {
            valueColumn.setPrefWidth(valuePrefWidth);
        }else{
            valueColumn.setPrefWidth(PREF_WIDTH);
        }
        setResizePolicy();
    }

    /**
     * set pref width for key column.
     * @param keyPrefWidth pref width
     */
    public void setKeyPrefWidth(Double keyPrefWidth) {
        this.keyPrefWidth = keyPrefWidth;
        if(keyPrefWidth != Double.MIN_VALUE) {
            keyColumn.setPrefWidth(keyPrefWidth);
        }else{
            keyColumn.setPrefWidth(PREF_WIDTH);
        }
        setResizePolicy();
    }

    /**
     * set min width of value column.
     * @param valueMinWidth min width
     */
    public void setValueMinWidth(Double valueMinWidth) {
        this.valueMinWidth = valuePrefWidth;
        if(valueMinWidth != Double.MIN_VALUE) {
            valueColumn.setMinWidth(valueMinWidth);
        }else{
            valueColumn.setMinWidth(MIN_WIDTH);
        }
        setResizePolicy();
    }

    /**
     * set min width of key column.
     * @param keyMinWidth min width
     */
    public void setKeyMinWidth(Double keyMinWidth) {
        this.keyMinWidth = keyPrefWidth;
        if(keyMinWidth != Double.MIN_VALUE) {
            keyColumn.setMinWidth(keyMinWidth);
        }else{
            keyColumn.setMinWidth(MIN_WIDTH);
        }
        setResizePolicy();
    }

    /**
     * set max width of key column.
     * @param keyMaxWidth max width
     */
    public void setKeyMaxWidth(Double keyMaxWidth) {
        this.keyMaxWidth = keyMaxWidth;
        if(keyMaxWidth != Double.MIN_VALUE) {
            keyColumn.setMaxWidth(keyMaxWidth);
        }else{
            keyColumn.setMaxWidth(MAX_WIDTH);
        }
        setResizePolicy();
    }

    /**
     * Set max width of value column.
     * @param valueMaxWidth max width
     */
    public void setValueMaxWidth(Double valueMaxWidth) {
        this.valueMaxWidth = valueMaxWidth;
        if(valueMaxWidth != Double.MIN_VALUE) {
            valueColumn.setMaxWidth(keyMaxWidth);
        }else{
            valueColumn.setMaxWidth(MAX_WIDTH);
        }
        setResizePolicy();
    }

    public void setAllItems(ObservableList<Pair<K, V>> allItems) {
        this.allItems = allItems;
        filterItems();
    }


    /**
     * Could bound to ObservableList to rerender cells.
     * Renders the items of the list and skips the skippedKeys.
     */
    public void renderItems(){
        filterItems();
    }
    //**PRIVATE**//
    private void filterItems() {
        if(skippedKeys.size() == 0){
            setItems(allItems);
        }else{
            setItems(allItems.filtered(filter -> !skippedKeys.contains(filter.getKey())));
        }
    }

    private void setResizePolicy() {
        if(valueMaxWidth == null || keyMaxWidth == null
                || valueMinWidth == null || keyMinWidth == null
                ){
            this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }else {
            this.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        }
    }

    private void initTable() {
        this.widthProperty().addListener((ov, t, t1) -> {
            // Get the table header
            Pane header = (Pane)lookup("TableHeaderRow");
            if(header!=null && header.isVisible()) {
                header.setMaxHeight(3);
                header.setMinHeight(3);
                header.setPrefHeight(3);
                //header.setVisible(false);
                //header.setManaged(false);
            }
        });
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void initCells() {
        keyColumn = new TableColumn<>();
        keyColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getKey().toString()));

        valueColumn = new TableColumn<>();
        valueColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getValue().toString()));

        this.getColumns().addAll(keyColumn, valueColumn);
    }
}