/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import java.nio.IntBuffer;
import java.util.concurrent.Semaphore;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Window;

import com.sun.javafx.perf.PerformanceTracker;

/**
 * Redirect popups to the Bridge
 * 
 */
public class PopupSnapper {
    
	private Window	window;
	private Scene	scene;
	WritableImage	img;
	double		    ignoreRepaintHeight;
	JmeFxContainer	jmeFXcontainerReference;
	private Semaphore  repaintLock = new Semaphore(1);

	public PopupSnapper(final JmeFxContainer containReference, final Window window, final Scene scene) {
		this.window = window;
		Parent root = scene.getRoot();
		scene.setRoot(new Group());
		
		Scene s = new Scene(root,scene.getWidth(),scene.getHeight(),scene.getFill());
		
		this.scene = s;
		this.jmeFXcontainerReference = containReference;
		
		
	}

	public void paint(final IntBuffer buf, final int pWidth, final int pHeight) {
	    		try {

			final WritableImage img = this.img;
			if (img == null) {
			    //System.out.println("Skipping popup merge due to no image");
				return;
			}
			boolean lock = repaintLock.tryAcquire();
			if ( lock ) {
			    try {
    				final PixelReader pr = img.getPixelReader();
    
    				final int w = (int) img.getWidth();
    				final int h = (int) img.getHeight();
    
    				final byte[] pixels = new byte[w * h * 4];
    				pr.getPixels(0, 0, w, h, PixelFormat.getByteBgraPreInstance(), pixels, 0, w * 4);
    
    				final int xoff = (int) this.window.getX() - this.jmeFXcontainerReference.getWindowX();
    				final int yoff = (int) this.window.getY() - this.jmeFXcontainerReference.getWindowY();
    
    				for (int x = 0; x < w; x++) {
    					for (int y = 0; y < h; y++) {
    						final int offset = x + xoff + (y + yoff) * pWidth;
    						final int old = buf.get(offset);
    						final int boff = 4 * (x + y * w);
    						final int toMerge = (pixels[boff]&0xff) | ((pixels[boff + 1]&0xff) << 8) | ((pixels[boff + 2]&0xff) << 16)
    								| ((pixels[boff+3]&0xff) << 24);
    						    						
    						final int merge = PixelUtils.mergeBgraPre(old, toMerge);
    						buf.put(offset, merge);
    					}
    				}
    				//System.out.println("Done popup merge");
			    } finally {
			        repaintLock.release();
			    }
			} else {
			    //System.out.println("Skipping popup merge due to contention");
			}
			
		} catch (final Exception exc) {
			exc.printStackTrace();
		}

	}

	public void repaint() {
		try {
			if (!Color.TRANSPARENT.equals(this.scene.getFill())) {
				this.scene.setFill(Color.TRANSPARENT);
			}
			if (this.img != null) {
				if (this.img.getWidth() != this.scene.getWidth() || this.img.getHeight() != this.scene.getHeight()) {
				    //System.out.println("Invalidating image due to size change");
					this.img = null;
				}
			}
			boolean lock = repaintLock.tryAcquire();
			if ( lock ) {
			    try {
			        //System.out.println("Popup render");
			        this.img = this.scene.snapshot(this.img);
			    } finally {
			        repaintLock.release();
			    }
			    this.jmeFXcontainerReference.paintComponent();
			} else {
			    //System.out.println("Schedulling extra repaint due to contention");
			    Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //System.out.println("Extra popup repaint due to contention");
                        repaint();
                    }
                });
			}
			
			
		} catch (final Exception exc) {
			exc.printStackTrace();
		}
	}

	public void start() {

	    Runnable run = new Runnable () {
            @Override
            public void run() {
                System.out.println(ignoreRepaintHeight + " v " + scene.getHeight());
                
                if (ignoreRepaintHeight == scene.getHeight()) {
                    ignoreRepaintHeight = -1;
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        ignoreRepaintHeight = scene.getHeight();
                        repaint();
                    }
                });
            }
        };
	    
	    PerformanceTracker.getSceneTracker(window.getScene()).setOnRenderedFrameTask(run);
	    PerformanceTracker.getSceneTracker(scene).setOnRenderedFrameTask(run);

	    scene.getRoot().boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
	        @Override
	        public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
	            
	            run.run();
	        }
        });
	    
		this.jmeFXcontainerReference.activeSnappers.add(this);
	}

	public void stop() {
		this.jmeFXcontainerReference.activeSnappers.remove(this);
		Parent root = scene.getRoot();
		scene.setRoot(new Group());
		window.getScene().setRoot(root);
	}
}
