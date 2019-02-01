/**
 * Mars Simulation Project
 * DragDropList.java
 * @version 3.1.0 2017-06-01
 * @author Manny Kung
 */

package org.mars_sim.javafx.tools;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;



public class DragDropList extends Application {
    private String buffer = "";

    public static void main(String args[]) {
        launch(args);
    }
    
    @Override
    public void start (Stage primaryStage) {
        HBox box = new HBox();

        // ListView
        ListView<String> list = new ListView<String>( FXCollections.observableArrayList( "Strawberry", "Red Beet", "Lettuce", "Potato" ) );

        list.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                ListCell<String> listCell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem( item, empty );
                        setText( item );
                    }
                };

                listCell.setOnDragDetected( ( MouseEvent event ) -> {
                    System.out.println( "listcell setOnDragDetected" );
                    Dragboard db = listCell.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString( listCell.getItem() );
                    db.setContent( content );
                    event.consume();
                } );

                return listCell;
            }
        } );

        //TreeView
        TreeItem<String> rootItem = new TreeItem<>("Crop Queue");
        rootItem.setExpanded(true);
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<>("Crop " + i);
            rootItem.getChildren().add(item);
        }
        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setEditable(true);

        treeView.setCellFactory( new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call( TreeView<String> stringTreeView ) {
                TreeCell<String> treeCell = new TreeCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem( item, empty );
                        if (item != null) {
                            setText(item);
                        }
                    }
                };

                treeCell.setOnDragEntered( (DragEvent event) -> {
                    treeCell.setStyle("-fx-background-color: aqua;");
                } );

                treeCell.setOnDragExited( (DragEvent event) -> {
                    treeCell.setStyle("");
                } );

                treeCell.setOnDragOver( (DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    if ( db.hasString() )
                    {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE );
                    }
                    event.consume();
                } );

                treeCell.setOnDragDropped( (DragEvent event) -> {
                    System.out.println( "treeCell.setOnDragDropped" );
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        System.out.println( "Dropped: " + db.getString() );
                        treeCell.getTreeItem().getChildren().add( new TreeItem<>( db.getString() ) );
                        treeCell.getTreeItem().setExpanded( true );
                        success = true;
                    }
                    event.setDropCompleted( success );
                    event.consume();
                } );

                return treeCell;
            }
        } );

        box.getChildren().addAll(list, treeView);
        primaryStage.setScene(new Scene(box) );
        primaryStage.show();
    }
}