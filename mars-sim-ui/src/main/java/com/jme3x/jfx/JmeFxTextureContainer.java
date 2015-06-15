package com.jme3x.jfx;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class JmeFxTextureContainer extends JmeFxContainer {

    public JmeFxTextureContainer(Application app,int width, int height) {

        this.pWidth = width;
        this.pHeight = height;
        this.app = app;
        this.fullScreenSuppport = true;
        app.getStateManager().attach(new AbstractAppState() {

            @Override
            public void cleanup() {
                Platform.exit();
                super.cleanup();
            }
        });

        this.hostContainer = new JmeFXHostInterfaceImpl(this);

        try {
            this.jmeData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);
            this.fxData = BufferUtils.createByteBuffer(this.pWidth * this.pHeight * 4);
            this.jmeImage = new Image(nativeFormat.get(), this.pWidth, this.pHeight, this.jmeData);
            this.tex = new Texture2D(this.jmeImage);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        
        inputListener = new JmeFXInputListener(this);
        installSceneAccessorHack();

    }

    public Texture2D getTexture() {
        return tex;
    }
    
    @Override
    public int getXPosition() {
        return 0;
    }
    
    @Override
    public int getYPosition() {
        return 0;
    }
    

}
