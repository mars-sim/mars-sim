package com.jme3x.jfx;

import java.nio.ByteBuffer;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.TransferMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import com.sun.javafx.embed.HostDragStartListener;

/**
 * A very hacky implementation of a DND system, similar to SwingDND but for jme context. <br>
 * Allows for inner application drag and drop support. <br>
 * Cross GuiManager support is untested.
 *
 * @author empire
 *
 */
public class JmeFxDNDHandler implements HostDragStartListener {
	private static final Logger logger = LoggerFactory.getLogger(JmeFxDNDHandler.class);

	private JmeFxContainer				jmeFxContainer;
	private EmbeddedSceneDTInterface	dropTarget;
	// mouse event stuff
	private EmbeddedSceneDSInterface	dragSource;
	private TransferMode				overtarget;
	private ImageView					dragImage;

	public JmeFxDNDHandler(final JmeFxContainer jmeFxContainer) {
		this.jmeFxContainer = jmeFxContainer;
	}

	@Override
	public void dragStarted(final EmbeddedSceneDSInterface dragSource, final TransferMode dragAction) {
		try {
			final Object dimg = dragSource.getData("application/x-java-drag-image");
			final Object offset = dragSource.getData("application/x-java-drag-image-offset");
			if (dimg != null) {
				this.createDragImageProxy(dimg, offset);
			}

			this.jmeFxContainer.getInputListener().setMouseDNDListener(this);
			assert dragAction == TransferMode.COPY : "Only Copy is supported currently";
			logger.debug("Drag started of {} in mode {}", dragSource, dragAction);
			final Clipboard clip = Clipboard.getSystemClipboard();
			logger.debug("clip : {}", clip);

			assert this.dragSource == null;
			assert this.dropTarget == null;

			this.dragSource = dragSource;
			this.dropTarget = JmeFxDNDHandler.this.jmeFxContainer.scenePeer.createDropTarget();
			// pseudo enter, we only support inner events, so it stays always entered
			this.dropTarget.handleDragEnter(0, 0, 0, 0, TransferMode.COPY, dragSource);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * this is kinda ridiculous, but well at least it seems to work
	 *
	 * @param jmeJfxDragImage
	 * @param offset
	 */
	private void createDragImageProxy(final Object jmeJfxDragImage, final Object offset) {
		if (jmeJfxDragImage instanceof ByteBuffer) {
			try {
				final ByteBuffer casted = (ByteBuffer) jmeJfxDragImage;
				casted.position(0);
				final int w = casted.getInt();
				final int h = casted.getInt();

				final byte[] imgdata = new byte[casted.remaining()];
				casted.get(imgdata);

				final WritableImage img = new WritableImage(w, h);
				final PixelWriter writer = img.getPixelWriter();
				writer.setPixels(0, 0, w, h, PixelFormat.getByteBgraInstance(), imgdata, 0, w * 4);

				this.dragImage = new ImageView(img);
				this.dragImage.setStyle("dragimage:true;");
				this.dragImage.setMouseTransparent(true);
				this.dragImage.setVisible(true);

				if (offset instanceof ByteBuffer) {
					((ByteBuffer) offset).position(0);
					final int x = ((ByteBuffer) offset).getInt();
					final int y = ((ByteBuffer) offset).getInt();
					logger.debug("Img offset {},{}", x, y);
				}

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void mouseUpdate(final int x, final int y, final boolean mousePressed) {
		try {
			if (this.dragSource == null || this.dropTarget == null) {
				return;
			}
			if (mousePressed) {
				if (this.dragImage != null) {
					this.dragImage.relocate(x, y);
					// only add once it has a valid position
					if (!this.jmeFxContainer.getRootChildren().contains(this.dragImage)) {
						this.jmeFxContainer.getRootChildren().add(this.dragImage);
					}
				}
				this.overtarget = this.dropTarget.handleDragOver(x, y, x, y, TransferMode.COPY);
			} else {
				if (this.dragImage != null) {
					this.jmeFxContainer.getRootChildren().remove(this.dragImage);
					this.dragImage = null;
				}
				logger.debug("Drag released!");
				if (this.overtarget != null) {
					// // causes exceptions when done without a target
					this.overtarget = JmeFxDNDHandler.this.dropTarget.handleDragOver(x, y, x, y, TransferMode.COPY);
					final TransferMode acceptedMode = JmeFxDNDHandler.this.dropTarget.handleDragDrop(x, y, x, y, TransferMode.COPY);
					// // Necessary to reset final the internal states, and allow final another drag drop
					this.dragSource.dragDropEnd(acceptedMode);
				} else {
					logger.debug("invalid drag target");
					// // seems to be necessary if no dragdrop attempt is being made
					JmeFxDNDHandler.this.dropTarget.handleDragLeave();
					this.dragSource.dragDropEnd(null);
				}
				this.jmeFxContainer.getInputListener().setMouseDNDListener(null);
				this.dragSource = null;
				this.dropTarget = null;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
