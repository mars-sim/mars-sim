package com.jme3x.jfx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3x.jfx.media.MovieMaterial;
import com.jme3x.jfx.media.TextureMovie;
import com.sun.javafx.application.PlatformImpl;

public class TestMovieMaterial extends SimpleApplication {

    private MovieMaterial    movieMaterial;
    private MediaPlayer     mp;

    public static void main(final String[] args) {

        PlatformImpl.startup(() -> {
        });

        final TestMovieMaterial app = new TestMovieMaterial();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //getInputManager().setCursorVisible(true);
        //getFlyByCamera().setEnabled(false);
        final Media media = new Media("http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv");
        //final Media media = new Media("file:///archive/movies/kamera/kotywalka1.mp4");
        //final Media media = new Media("http://techslides.com/demos/sample-videos/small.mp4");
        //final Media media = new Media(new File("/home/dwayne/tmp/small.mp4").toURI().toASCIIString());
        //final Media media = new Media(new File("/home/dwayne/tmp/output.mp4").toURI().toASCIIString());

        media.errorProperty().addListener(new ChangeListener<MediaException>() {

            @Override
            public void changed(final ObservableValue<? extends MediaException> observable, final MediaException oldValue, final MediaException newValue) {
                newValue.printStackTrace();
            }
        });
        this.mp = new MediaPlayer(media);
        this.mp.play();

        this.movieMaterial = new MovieMaterial(this, this.mp, true);
        this.movieMaterial.setLetterboxColor(ColorRGBA.Black);

        final Geometry screen1 = new Geometry("Screen1", new Quad(20, 20));

        
        screen1.setMaterial(movieMaterial.getMaterial());
        this.rootNode.attachChild(screen1);

        this.cam.setLocation(new Vector3f(10, 10, 15));

    }
    
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        this.movieMaterial.update(tpf);
    }

    @Override
    public void destroy() {
        super.destroy();
        this.mp.stop();
        PlatformImpl.exit();
    }

}
