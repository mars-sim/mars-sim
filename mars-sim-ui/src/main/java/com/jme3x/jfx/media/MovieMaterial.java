package com.jme3x.jfx.media;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Function;

import com.jme3.app.Application;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Image;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.util.FormatUtils;
import com.jme3x.jfx.util.ImageExchanger;
import com.sun.media.jfxmedia.control.VideoDataBuffer;
import com.sun.media.jfxmedia.control.VideoFormat;
import com.sun.media.jfxmedia.events.NewFrameEvent;
import com.sun.media.jfxmedia.events.VideoRendererListener;

/**
 *
 * Example usage
 * 
 * <pre>
 * PlatformImpl.startup(() -&gt; {
 * });
 * media = new Media(&quot;http://download.oracle.com/otndocs/products/javafx/oow2010-2.flv&quot;);
 * mp = new javafx.scene.media.MediaPlayer(media);
 * mp.play();
 * movieMaterial = new MovieMaterial(application, mp, true);
 * movieMaterial.setLetterboxColor(ColorRGBA.Black);
 * quad.setMaterial(movieMaterial.getMaterial());
 * 
 * </pre>
 * 
 * And then somewhere in update method call movieMaterial.update(tpf);
 * 
 */
public class MovieMaterial {

    public static final int NO_SWIZZLE = 0;
    public static final int SWIZZLE_RB = 1;

    private static Image emptyImage = new Image(Format.ABGR8, 1, 1, BufferUtils.createByteBuffer(4));

    private final javafx.scene.media.MediaPlayer jPlayer;
    private final com.sun.media.jfxmedia.MediaPlayer cPlayer;
    private final boolean letterbox;
    private final ColorRGBA letterboxColor = ColorRGBA.Red.clone();
    private final Vector2f aspectValues = new Vector2f(1, 1);
    private final Vector2f validRange = new Vector2f(1, 1);
    private final VideoRendererListener vrListener;
    private float aspectRatio = 1.0f;
    boolean running = true;

    private Texture2D textureLuma;
    private Texture2D textureCr;
    private Texture2D textureCb;
    private Application app;

    VideoDataBuffer latestFrame;
    VideoDataBuffer jmeFrame;

    public MovieMaterial(final Application app, javafx.scene.media.MediaPlayer mediaPlayer) {
        this(app, mediaPlayer, true);
    }

    public MovieMaterial(final Application app, javafx.scene.media.MediaPlayer mediaPlayer, ColorRGBA letterboxColor) {
        this(app, mediaPlayer, true);
        setLetterboxColor(letterboxColor);
    }

    public MovieMaterial(final Application app, javafx.scene.media.MediaPlayer mediaPlayer, boolean letterbox) {
        this.app = app;
        this.jPlayer = mediaPlayer;
        this.letterbox = letterbox;

        try {
            Method m1 = jPlayer.getClass().getDeclaredMethod("retrieveJfxPlayer");
            m1.setAccessible(true);

            while (true) {
                com.sun.media.jfxmedia.MediaPlayer player = (com.sun.media.jfxmedia.MediaPlayer) m1.invoke(jPlayer);
                if (player != null) {
                    cPlayer = player;
                    break;
                }
                Thread.sleep(50);
            }

        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        vrListener = createVrListener();
    }

    LinkedList<VideoDataBuffer> frameQueue = new LinkedList<VideoDataBuffer>();
    private Material material;

    private VideoRendererListener createVrListener() {
        return new VideoRendererListener() {

            @Override
            public void videoFrameUpdated(NewFrameEvent event) {
                try {
                    // VideoDataBuffer frame = jPlayer.impl_getLatestFrame();
                    VideoDataBuffer frame = event.getFrameData();
                    if (!running || frame == null) {
                        return;
                    }
                    if ( frame.getFormat() == null ) {
                        // early return for end-of-movie null frames
                        return;
                    }
                    if ( frame.getPlaneCount() != 3 ) {
                        System.out.println("MovieMaterial supports 3-plane YCrCb only at the moment, got " + frame);
                        return;
                    }
                    
                    frame.holdFrame();


                    float bufferWidth, bufferHeight;
                    
                    ByteBuffer mainBuffer = frame.getBufferForPlane(0);
                    bufferWidth = frame.getStrideForPlane(0);
                    bufferHeight = mainBuffer.capacity()/bufferWidth;
                    
                    float validWidth = frame.getWidth()/bufferWidth;
                    float validHeight = frame.getHeight()/bufferHeight;
                    
                    aspectRatio = frame.getWidth() / (float)frame.getHeight();

                    synchronized (MovieMaterial.this) {
                        if ( !running ) {
                            frame.releaseFrame();
                            return;
                        }
                        if ( letterbox ) {
                            aspectValues.set(Math.max(1, 1/aspectRatio),Math.max(1,aspectRatio));
                        } else {
                            aspectValues.set(1,1);    
                        }
                        validRange.set(validWidth,validHeight);
                        latestFrame = frame;
                    }

                } catch (Exception exc) {
                    exc.printStackTrace();
                    System.exit(0);
                }
            }

            @Override
            public void releaseVideoFrames() {
            }
        };
    }

    private void updateTexture(Texture2D tex, ByteBuffer buf, int stride) {
        if ( buf == null ) {
            tex.setImage(emptyImage);
            return;
        }
        if (tex.getImage().getData(0).capacity() != buf.capacity()) {
            Image img = new Image(Format.Luminance8, stride, buf.capacity() / stride, buf);
            tex.setImage(img);
        } else {
            tex.getImage().setData(buf);
        }
    }

    /**
     * Sets the color which should be used for letterbox fill. It is annoying
     * red by default to help with debugging.
     *
     * @param letterboxColor
     */
    public void setLetterboxColor(ColorRGBA letterboxColor) {
        this.letterboxColor.set(letterboxColor);
        if (letterbox && material != null) {
            material.setColor("LetterboxColor", letterboxColor);
        }
    }

    /**
     *
     * @return aspect ratio of played movie (width/height) - for widescreen
     *         movies it will be in range of 1.8-2.9
     */
    public float getAspectRatio() {
        return aspectRatio;
    }

    public Material getMaterial() {
        if (material == null) {
            init();
        }
        return material;
    }

    private void init() {

        textureLuma = new Texture2D(emptyImage);
        textureCr = new Texture2D(emptyImage);
        textureCb = new Texture2D(emptyImage);

        material = new Material(app.getAssetManager(), "com/jme3x/jfx/media/MovieMaterial.j3md");

        material.setTexture("TexLuma", textureLuma);
        material.setTexture("TexCr", textureCr);
        material.setTexture("TexCb", textureCb);
        material.setVector2("AspectValues", aspectValues.clone());
        material.setVector2("ValidRange", validRange.clone());

        if (letterbox) {
            material.setColor("LetterboxColor", letterboxColor);
        }

        cPlayer.getVideoRenderControl().addVideoRendererListener(vrListener);

    }

    public void update(float tpf) {
        synchronized (MovieMaterial.this) {
            if (latestFrame != null && latestFrame != jmeFrame) {
                if (!aspectValues.equals(material.getParam("AspectValues").getValue())) {
                    material.setVector2("AspectValues", aspectValues.clone());
                }
                if (!validRange.equals(material.getParam("ValidRange").getValue())) {
                    material.setVector2("ValidRange", validRange.clone());
                }

                updateTexture(textureLuma, latestFrame.getBufferForPlane(VideoDataBuffer.YCBCR_PLANE_LUMA),
                        latestFrame.getStrideForPlane(VideoDataBuffer.YCBCR_PLANE_LUMA));
                updateTexture(textureCr, latestFrame.getBufferForPlane(VideoDataBuffer.YCBCR_PLANE_CR),
                        latestFrame.getStrideForPlane(VideoDataBuffer.YCBCR_PLANE_CR));
                updateTexture(textureCb, latestFrame.getBufferForPlane(VideoDataBuffer.YCBCR_PLANE_CB),
                        latestFrame.getStrideForPlane(VideoDataBuffer.YCBCR_PLANE_CB));

                if (jmeFrame != null) {
                    jmeFrame.releaseFrame();
                }
                jmeFrame = latestFrame;
            }

        }

    }
    
    public void dispose() {
        synchronized (MovieMaterial.this) {
            running = false;
            textureLuma.setImage(emptyImage);
            textureCr.setImage(emptyImage);
            textureCb.setImage(emptyImage);
            if ( jmeFrame != null ) {
                jmeFrame.releaseFrame();
            }
        }
        
    }

}
