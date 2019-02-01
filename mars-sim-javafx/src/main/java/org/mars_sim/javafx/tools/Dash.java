package org.mars_sim.javafx.tools;
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.mars_sim.msp.ui.javafx.tools;
//
//import javafx.scene.Scene;
//import javafx.scene.layout.Pane;
//import javafx.scene.media.Media;
//import javafx.scene.media.MediaPlayer;
//import javafx.scene.media.MediaView;
//
///**
// *
// * @author avinerbi
// */
//public class Dash extends Pane
//{
//    private Media media;
//    private MediaPlayer player;
//    private MediaView mediaView;
//    
//    private FrostedPanel panel;
//    private Scene scene;
//    
//    public void setup(Scene scene)
//    {
//        this.scene = scene;
//        media = new Media(getClass().getResource("video_1.mp4").toExternalForm());
//        player = new MediaPlayer(media);
//        mediaView = new MediaView(player);
//        player.setAutoPlay(true);
//        player.setVolume(0);
//        getChildren().add(mediaView);
//        mediaView.fitWidthProperty().bind(scene.widthProperty());
//        mediaView.fitHeightProperty().bind(scene.heightProperty());
//        
//    }
//}
