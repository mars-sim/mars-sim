package com.jme3x.jfx.util;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import com.jme3.app.Application;
import com.jme3.texture.Image;
import com.jme3.util.BufferUtils;

public class ImageExchanger {

    private final Semaphore imageExchange = new Semaphore(1);
    private ByteBuffer jmeData;
    private ByteBuffer fxData;
    private final Image jmeImage;
    private boolean fxDataReady;
    private final Application app;

    public ImageExchanger(int width, int height, Image.Format format, Application app) {
        this.app = app;
        this.jmeData = BufferUtils.createByteBuffer(width * height * format.getBitsPerPixel()/8);
        this.fxData = BufferUtils.createByteBuffer(width * height * format.getBitsPerPixel()/8);

        this.jmeImage = new Image(format, width, height, this.jmeData);
        // HACK pre-3.1 to support gamma correction with jme pre-implementation
        // of ColorSpace
        try {
            Class<?> classColorSpace = Class.forName("com.jme3.texture.image.ColorSpace");
            Method m = Image.class.getMethod("setColorSpace", classColorSpace);
            m.invoke(this.jmeImage, classColorSpace.getField("sRGB").get(null));
        } catch (Throwable exc) {
            // ignore jme 3.1 not available
        }
        // HACK pre-3.1 End
    }

    public Image getImage() {
        return jmeImage;
    }

    public ByteBuffer getFxData() {
        return fxData;
    }
    
    public ByteBuffer getJmeData() {
        return jmeData;
    }

    public void startUpdate() throws InterruptedException {
        imageExchange.acquire();
    }
    
    public void flushUpdate() {

        try {

            fxDataReady = true;
            this.app.enqueue(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    final boolean updateImage = imageExchange.tryAcquire();
                    // we update only if we can do that in nonblocking mode
                    // if would need to block, it means that another callable
                    // with
                    // newer data will be
                    // enqueued soon, so we can just ignore this repaint
                    if (updateImage) {
                        try {
                            if (fxDataReady) {
                                fxDataReady = false;
                                final ByteBuffer tmp = jmeData;
                                jmeData = fxData;
                                fxData = tmp;
                            }
                        } finally {
                            imageExchange.release();
                        }
                        jmeImage.setData(jmeData);
                    } else {
                        //System.out.println("Skipping update due to contention");
                    }
                    return null;
                }
            });

        } finally {
            imageExchange.release();
        }
    }

    public void dispose() {
        BufferUtils.destroyDirectBuffer(fxData);
        BufferUtils.destroyDirectBuffer(jmeData);
        jmeImage.dispose();
    }

}
