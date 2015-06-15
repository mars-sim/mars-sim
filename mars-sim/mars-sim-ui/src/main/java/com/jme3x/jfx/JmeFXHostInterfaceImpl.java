/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3x.jfx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.embed.AbstractEvents;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;

/**
 * Fakes a top level window
 *
 */
public class JmeFXHostInterfaceImpl implements HostInterface {
	private static final Logger		logger	= LoggerFactory.getLogger(JmeFXHostInterfaceImpl.class);

	private final JmeFxContainer	jmeFxContainer;

	public JmeFXHostInterfaceImpl(final JmeFxContainer jmeFxContainer) {
		this.jmeFxContainer = jmeFxContainer;
	}

	@Override
	public void setEmbeddedStage(final EmbeddedStageInterface embeddedStage) {
		this.jmeFxContainer.stagePeer = embeddedStage;
		if (this.jmeFxContainer.stagePeer == null) {
			return;
		}
		if (this.jmeFxContainer.pWidth > 0 && this.jmeFxContainer.pHeight > 0) {
			this.jmeFxContainer.stagePeer.setSize(this.jmeFxContainer.pWidth, this.jmeFxContainer.pHeight);
		}

		this.jmeFxContainer.stagePeer.setFocused(true, AbstractEvents.FOCUSEVENT_ACTIVATED);
	}

	@Override
	public void setEmbeddedScene(final EmbeddedSceneInterface embeddedScene) {

		this.jmeFxContainer.scenePeer = embeddedScene;
		if (this.jmeFxContainer.scenePeer == null) {
			return;
		}
		if (this.jmeFxContainer.pWidth > 0 && this.jmeFxContainer.pHeight > 0) {
			this.jmeFxContainer.scenePeer.setSize(this.jmeFxContainer.pWidth, this.jmeFxContainer.pHeight);
		}

		if (this.jmeFxContainer.scenePeer != null) {
			this.jmeFxContainer.scenePeer.setDragStartListener(new JmeFxDNDHandler(this.jmeFxContainer));
		}

	}

	@Override
	public boolean requestFocus() {
		return true;
	}

	@Override
	public boolean traverseFocusOut(final boolean forward) {
		JmeFXHostInterfaceImpl.logger.debug("Called traverseFocusOut({})", forward);
		return true;
	}

	@Override
	public void setPreferredSize(final int width, final int height) {
	}

	int	repaintCounter	= 0;

	@Override
	public void repaint() {
		this.jmeFxContainer.paintComponent();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		this.jmeFxContainer.setFxEnabled(enabled);
	}

	@Override
	public void setCursor(final CursorFrame cursorFrame) {
		if (this.jmeFxContainer.cursorDisplayProvider != null) {
			this.jmeFxContainer.cursorDisplayProvider.showCursor(cursorFrame);
		}
	}

	@Override
	public boolean grabFocus() {
		return true;
	}

	@Override
	public void ungrabFocus() {
	}

}
