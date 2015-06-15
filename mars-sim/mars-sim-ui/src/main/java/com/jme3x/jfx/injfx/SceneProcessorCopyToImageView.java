package com.jme3x.jfx.injfx;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.value.ChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import com.jme3.app.Application;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.FxPlatformExecutor;

//https://github.com/caprica/vlcj-javafx/blob/master/src/test/java/uk/co/caprica/vlcj/javafx/test/JavaFXDirectRenderingTest.java
//http://stackoverflow.com/questions/15951284/javafx-image-resizing
//http://hub.jmonkeyengine.org/forum/topic/offscreen-rendering-problem/
//TODO manage suspend/resume (eg when image/stage is hidden)
public class SceneProcessorCopyToImageView implements SceneProcessor {

	private RenderManager rm;
	private ViewPort latestViewPorts;
	private int askWidth  = 1;
	private int askHeight = 1;
	private boolean askFixAspect = true;
	private TransfertImage timage;
	private AtomicBoolean reshapeNeeded  = new AtomicBoolean(true);

	private ImageView imgView;
	private ChangeListener<? super Number> wlistener = (w,o,n)->{
		componentResized(n.intValue(), (int)this.imgView.getFitHeight(), this.imgView.preserveRatioProperty().get());
	};
	private ChangeListener<? super Number> hlistener = (w,o,n)->{
		componentResized((int)this.imgView.getFitWidth(), n.intValue(), this.imgView.preserveRatioProperty().get());
	};
	private ChangeListener<? super Boolean> rlistener = (w,o,n)->{
		componentResized((int)this.imgView.getFitWidth(), (int)this.imgView.getFitHeight(), n.booleanValue());
	};

	public void componentResized(int w, int h, boolean fixAspect) {
		int newWidth2 = Math.max(w, 1);
		int newHeight2 = Math.max(h, 1);
		if (askWidth != newWidth2 || askWidth != newHeight2 || askFixAspect != fixAspect){
			askWidth = newWidth2;
			askHeight = newHeight2;
			askFixAspect = fixAspect;
			reshapeNeeded.set(true);
		}
	}

	public void bind(ImageView view, Application jmeApp){
		unbind();

		if (jmeApp != null) {
			List<ViewPort> vps = jmeApp.getRenderManager().getPostViews();
			latestViewPorts = vps.get(vps.size() - 1);
			latestViewPorts.addProcessor(this);
		}

		FxPlatformExecutor.runOnFxApplication(() -> {
			imgView = view;
			if (imgView != null) {
				imgView.fitWidthProperty().addListener(wlistener);
				imgView.fitHeightProperty().addListener(hlistener);
				imgView.preserveRatioProperty().addListener(rlistener);
				componentResized((int)imgView.getFitWidth(), (int)imgView.getFitHeight(), imgView.isPreserveRatio());
				imgView.setScaleY(-1.0);
			}
		});
	}

	public void unbind(){

		if (latestViewPorts != null){
			latestViewPorts.removeProcessor(this); // call this.cleanup()
			latestViewPorts = null;
		}

		FxPlatformExecutor.runOnFxApplication(() -> {
			if (imgView != null) {
				imgView.fitWidthProperty().removeListener(wlistener);
				imgView.fitHeightProperty().removeListener(hlistener);
			}
		});
	}

	@Override
	public void initialize(RenderManager rm, ViewPort vp) {
		if (this.rm == null){
			// First time called in OGL thread
			this.rm = rm;
		}
	}

	private TransfertImage reshapeInThread(int width0, int height0, boolean fixAspect) {
		TransfertImage ti = new TransfertImage(width0, height0);

		rm.getRenderer().setMainFrameBufferOverride(ti.fb);
		rm.notifyReshape(ti.width, ti.height);

//		for (ViewPort vp : viewPorts){
//			vp.getCamera().resize(ti.width, ti.height, fixAspect);
//
//			// NOTE: Hack alert. This is done ONLY for custom framebuffers.
//			// Main framebuffer should use RenderManager.notifyReshape().
//			for (SceneProcessor sp : vp.getProcessors()){
//				sp.reshape(vp, ti.width, ti.height);
//			}
//		}
		return ti;
	}

	@Override
	public boolean isInitialized() {
		return timage != null;
	}

	@Override
	public void preFrame(float tpf) {
	}

	@Override
	public void postQueue(RenderQueue rq) {
	}

	@Override
	public void postFrame(FrameBuffer out) {
		if (imgView != null && timage != null) {
	//		if (out != timage.fb){
	//			throw new IllegalStateException("Why did you change the output framebuffer? " + out + " != " + timage.fb);
	//		}
			timage.copyFrameBufferToImage(rm, imgView);
		}
		// for the next frame
		if (reshapeNeeded.getAndSet(false)){
			timage = reshapeInThread(askWidth, askHeight, askFixAspect);
			//TODO dispose previous timage ASAP (when no longer used in JavafFX thread)
		}
	}

	@Override
	public void cleanup() {
		if (timage != null) {
			timage.dispose();
			timage = null;
		}
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
	}

	static class TransfertImage {
		public final int width;
		public final int height;
		public final FrameBuffer fb;
		public final ByteBuffer byteBuf;
		public final WritableImage img;
		private ImageView lastIv = null;

		TransfertImage(int width, int height) {
			this.width = width;
			this.height = height;

			fb = new FrameBuffer(width, height, 1);
			fb.setDepthBuffer(Format.Depth);
			fb.setColorBuffer(Format.RGB8);

			byteBuf = BufferUtils.createByteBuffer(width * height * 4);

			img = new WritableImage(width, height);
		}

		/** SHOULD run in JME'Display thread */
		void copyFrameBufferToImage(RenderManager rm, ImageView iv) {
			synchronized (byteBuf) {
				// Convert screenshot.
				byteBuf.clear();
				rm.getRenderer().readFrameBuffer(fb, byteBuf);
			}
			FxPlatformExecutor.runOnFxApplication(() -> {
				synchronized (byteBuf) {
					if (lastIv != iv) {
						lastIv = iv;
						lastIv.setImage(img);
					}
					img.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), byteBuf, width * 4);
				}
			});
		}

		void dispose() {
			fb.dispose();
		}
	}
}