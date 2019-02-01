package org.mars_sim.javafx.tools;
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.mars_sim.msp.ui.javafx.tools;
//
//import javafx.animation.AnimationTimer;
//import javafx.scene.SnapshotParameters;
//import javafx.scene.control.Label;
//import javafx.scene.effect.BoxBlur;
//import javafx.scene.image.ImageView;
//import javafx.scene.image.WritableImage;
//import javafx.scene.layout.Pane;
//import javafx.scene.paint.Color;
//
///**
// *
// * @author avinerbi
// */
//public class FrostedPanel extends Pane
//{
//
//    private ImageView view;
//    private AnimationTimer timer;
//    private Dash dash;
//    private Pane translucent;
//
//    public FrostedPanel(Dash dash)
//    {
//        this.dash = dash;
//        view = new ImageView();
//
//        view.setLayoutX(0);
//        view.setLayoutY(0);
//
//        view.fitWidthProperty().bind(prefWidthProperty());
//        view.fitHeightProperty().bind(prefHeightProperty());
//
//        view.setEffect(new BoxBlur(15, 15, 20));
//        view.setPreserveRatio(false);
//
//        getChildren().add(view);
//        setStyle("-fx-background-color:rgba(0,0,0,0);");
//
//        setPrefWidth(300);
//        setPrefHeight(300);
//
//        setLayoutX(70);
//        setLayoutY(10);
//
//        translucent = new Pane();
//        translucent.setLayoutX(0);
//        translucent.setLayoutY(0);
//
//        translucent.prefWidthProperty().bind(prefWidthProperty());
//        translucent.prefHeightProperty().bind(prefHeightProperty());
//        translucent.setStyle("-fx-background-color:rgba(0,0,0,0.5);");
//
//        Label label;
//        label = new Label("Frosted panel");
//        label.setLayoutX(10);
//        label.setLayoutY(10);
//        label.setTextFill(Color.WHITE);
//
//        getChildren().add(translucent);
//        getChildren().add(label);
//        startTimer();
//
//        setOnMouseDragged(e ->
//        {
//            double x, y;
//            x = (e.getSceneX() < 0) ? 0 : e.getSceneX();
//            y = (e.getSceneY() < 0) ? 0 : e.getSceneY();
//            
//            
//            setLayoutX(x);
//            setLayoutY(y);
//
//        });
//    }
//
//    private void startTimer()
//    {
//        timer = new AnimationTimer()
//        {
//
//            @Override
//            public void handle(long now)
//            {
//                WritableImage image = dash.snapshot(new SnapshotParameters(), null);
//                WritableImage imageCrop = new WritableImage(image.getPixelReader(), (int) getLayoutX(), (int) getLayoutY(), (int) getPrefWidth(), (int) getPrefHeight());
//                view.setImage(imageCrop);
//            }
//        };
//
//        timer.start();
//    }
//}
