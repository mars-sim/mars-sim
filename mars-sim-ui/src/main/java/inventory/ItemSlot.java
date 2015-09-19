/*
 * Copyright (c) 2008-2011, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package inventory;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ParameterMap;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.Image;

/**
 *
 * @author Matthias Mann
 */
public class ItemSlot extends Widget {
    
    public static final StateKey STATE_DRAG_ACTIVE = StateKey.get("dragActive");
    public static final StateKey STATE_DROP_OK = StateKey.get("dropOk");
    public static final StateKey STATE_DROP_BLOCKED = StateKey.get("dropBlocked");
    
    public interface DragListener {
        public void dragStarted(ItemSlot slot, Event evt);
        public void dragging(ItemSlot slot, Event evt);
        public void dragStopped(ItemSlot slot, Event evt);
    }
    
    private String item;
    private Image icon;
    private DragListener listener;
    private boolean dragActive;
    private ParameterMap icons;

    public ItemSlot() {
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        findIcon();
    }

    public boolean canDrop() {
        return item == null;
    }
    
    public Image getIcon() {
        return icon;
    }
    
    public DragListener getListener() {
        return listener;
    }

    public void setListener(DragListener listener) {
        this.listener = listener;
    }
    
    public void setDropState(boolean drop, boolean ok) {
        AnimationState as = getAnimationState();
        as.setAnimationState(STATE_DROP_OK, drop && ok);
        as.setAnimationState(STATE_DROP_BLOCKED, drop && !ok);
    }
    
    @Override
    protected boolean handleEvent(Event evt) {
        if(evt.isMouseEventNoWheel()) {
            if(dragActive) {
                if(evt.isMouseDragEnd()) {
                    if(listener != null) {
                        listener.dragStopped(this, evt);
                    }
                    dragActive = false;
                    getAnimationState().setAnimationState(STATE_DRAG_ACTIVE, false);
                } else if(listener != null) {
                    listener.dragging(this, evt);
                }
            } else if(evt.isMouseDragEvent()) {
                dragActive = true;
                getAnimationState().setAnimationState(STATE_DRAG_ACTIVE, true);
                if(listener != null) {
                    listener.dragStarted(this, evt);
                }
            }
            return true;
        }
        
        
        return super.handleEvent(evt);
    }

    @Override
    protected void paintWidget(GUI gui) {
        if(!dragActive && icon != null) {
            icon.draw(getAnimationState(), getInnerX(), getInnerY(), getInnerWidth(), getInnerHeight());
        }
    }

    @Override
    protected void paintDragOverlay(GUI gui, int mouseX, int mouseY, int modifier) {
        if(icon != null) {
            final int innerWidth = getInnerWidth();
            final int innerHeight = getInnerHeight();
            icon.draw(getAnimationState(),
                    mouseX - innerWidth/2,
                    mouseY - innerHeight/2,
                    innerWidth, innerHeight);
        }
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        icons = themeInfo.getParameterMap("icons");
        findIcon();
    }
    
    private void findIcon() {
        if(item == null || icons == null) {
            icon = null;
        } else {
            icon = icons.getImage(item);
        }
    }
}
