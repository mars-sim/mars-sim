package com.jme3x.jfx;

import java.awt.Point;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import javafx.application.Platform;
import javafx.scene.Scene;

import org.lwjgl.opengl.Display;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.cursor.ICursorDisplayProvider;
import com.jme3x.jfx.util.JFXUtils;
import com.sun.javafx.embed.EmbeddedStageInterface;

public class JmeFxScreenContainer extends JmeFxContainer {

    

    /** Indent the window position to account for window decoration by Ronn */
    protected int windowOffsetX;
    protected int windowOffsetY;
    
    private final Picture picture;
    
    public JmeFxScreenContainer(AssetManager assetManager, Application app, boolean fullScreenSupport, ICursorDisplayProvider cursorDisplayProvider) {
        super();
        
        final Point decorationSize = JFXUtils.getWindowDecorationSize();

        this.windowOffsetX = (int) decorationSize.getX();
        this.windowOffsetY = (int) decorationSize.getY();
        this.cursorDisplayProvider = cursorDisplayProvider;
        this.app = app;
        this.fullScreenSuppport = fullScreenSupport;

        app.getStateManager().attach(new AbstractAppState() {

            @Override
            public void cleanup() {
                Platform.exit();
                super.cleanup();
            }
        });

        this.hostContainer = new JmeFXHostInterfaceImpl(this);
        this.picture = new Picture("JavaFXContainer", true) {

            @Override
            public void updateLogicalState(final float tpf) {

                final EmbeddedStageInterface currentStage = getStagePeer();
                try {

                    if(currentStage == null) {
                        return;
                    }
                    
                    if (stage != null && Display.isFullscreen() ) {
                        sceneContainerMap.put(stage, JmeFxScreenContainer.this);
                    } else {
                        sceneContainerMap.remove(stage);
                    }

                    final int currentWidth = Display.getWidth();
                    final int currentHeight = Display.getHeight();

                    if(currentWidth != getpWidth() || currentHeight != getpHeight()) {
                        handleResize();
                    }

                    final int x = Display.getX() + (Display.isFullscreen() ? 0 : getWindowOffsetX());
                    final int y = Display.getY() + (Display.isFullscreen() ? 0 : getWindowOffsetY());

                    if(getOldX() != x || getOldY() != y) {

                        setOldX(x);
                        setOldY(y);

                        Platform.runLater(() -> currentStage.setLocation(x, y));
                    }

                } finally {
                    super.updateLogicalState(tpf);
                }
            }
        };

        this.picture.move(0, 0, -1);
        this.picture.setPosition(0, 0);

        this.handleResize();

        this.tex = new Texture2D(this.jmeImage);
        this.picture.setTexture(assetManager, this.tex, true);
        
    }
    
    
    public Picture getJmeNode() {
        return this.picture;
    }

    @Override
    public int getWindowX() {
        return this.oldX;
    }

    @Override
    public int getWindowY() {
        return this.oldY;
    }
    
    private int getOldX() {
        return oldX;
    }

    private int getOldY() {
        return oldY;
    }

    private void setOldX(int oldX) {
        this.oldX = oldX;
    }

    private void setOldY(int oldY) {
        this.oldY = oldY;
    }

    private int getpHeight() {
        return pHeight;
    }

    private int getpWidth() {
        return pWidth;
    }

    /**
     * Indent the window position to account for window decoration.
     */
    public void setWindowOffsetX(int windowOffsetX) {
        this.windowOffsetX = windowOffsetX;
    }

    /**
     * Indent the window position to account for window decoration.
     */
    public void setWindowOffsetY(int windowOffsetY) {
        this.windowOffsetY = windowOffsetY;
    }

    /**
     * Indent the window position to account for window decoration.
     */
    public int getWindowOffsetX() {
        return windowOffsetX;
    }

    /**
     * Indent the window position to account for window decoration.
     */
    public int getWindowOffsetY() {
        return windowOffsetY;
    }
    

    private void handleResize() {

        try {
            this.imageExchange.acquire();
            dispose();

            this.pWidth = Display.getWidth();
            this.pHeight = Display.getHeight();
            if (this.pWidth < 64) {
                this.pWidth = 64;
            }
            if (this.pHeight < 64) {
                this.pHeight = 64;
            }
            this.picture.setWidth(this.pWidth);
            this.picture.setHeight(this.pHeight);
            this.jmeData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);
            this.fxData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);
            //TODO 3.1 : use new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData, com.jme3.texture.image.ColorSpace.sRGB);
            this.jmeImage = new Image(this.nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData);
            //HACK pre-3.1 to support gamma correction with jme pre-implementation of ColorSpace
            try {
                Class<?> classColorSpace = Class.forName("com.jme3.texture.image.ColorSpace");
                Method m = Image.class.getMethod("setColorSpace", classColorSpace);
                m.invoke(this.jmeImage, classColorSpace.getField("sRGB").get(null));
            } catch(Throwable exc) {
                // ignore jme 3.1 not available
            }
            //HACK pre-3.1 End
            if (this.tex != null) {
                this.tex.setImage(this.jmeImage);
            }

            if (this.stagePeer != null) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        JmeFxScreenContainer.this.stagePeer.setSize(JmeFxScreenContainer.this.pWidth, JmeFxScreenContainer.this.pHeight);
                        JmeFxScreenContainer.this.scenePeer.setSize(JmeFxScreenContainer.this.pWidth, JmeFxScreenContainer.this.pHeight);
                        JmeFxScreenContainer.this.hostContainer.repaint();
                    }
                });
            }

        } catch (final Exception exc) {
            exc.printStackTrace();
        } finally {
            this.imageExchange.release();
        }
    }

    
    @Override
    protected void setSceneImpl(Scene newScene) {
        super.setSceneImpl(newScene);
        this.app.enqueue(new Callable<Void>() {

            @Override
            public Void call() {
                JmeFxScreenContainer.this.picture.setCullHint(newScene == null ? CullHint.Always : CullHint.Never);
                return null;
            }
        });
    }
    
    @Override
    public int getXPosition() {
        if (!Display.isFullscreen()) {
            return Display.getX();
        }
        return 0;
    }
    
    @Override
    public int getYPosition() {
        if (!Display.isFullscreen()) {
            return Display.getY();
        }
        return 0;
    }

    
}
