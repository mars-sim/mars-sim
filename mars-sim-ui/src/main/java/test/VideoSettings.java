/*
 * Copyright (c) 2008-2010, Matthias Mann
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
package test;

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.model.BooleanModel;
import de.matthiasmann.twl.model.ListModel;
import de.matthiasmann.twl.model.SimpleBooleanModel;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.ComboBox;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.prefs.Preferences;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Matthias Mann
 */
public class VideoSettings extends DialogLayout {

    public enum CallbackReason {
        ACCEPT,
        CANCEL
    };

    private final Preferences prefs;
    private final Label lResolution;
    private final ComboBox<ModeEntry> cResolution;
    private final ListModel<ModeEntry> mResolutionFullscreen;
    private final SimpleChangableListModel<ModeEntry> mResolutionWindowed;
    private final Label lFullscreen;
    private final ToggleButton cFullscreen;
    private final BooleanModel mFullscreen;
    private final Button bAccept;
    private final Button bCancel;

    private CallbackWithReason<?>[] callbacks;

    private static int[] WINDOWED_MODES = {
        640, 480,
        800, 600,
        1024, 768,
        1280, 1024,
        1600, 1200};
    
    public VideoSettings(Preferences prefs, DisplayMode desktopMode) {
        this.prefs = prefs;
        
        ArrayList<ModeEntry> modes = new ArrayList<ModeEntry>();
        try {
            for(DisplayMode dm : Display.getAvailableDisplayModes()) {
                if(dm.getBitsPerPixel() == desktopMode.getBitsPerPixel()) {
                    addModeToList(modes, dm);
                }
            }
        } catch (LWJGLException ex) {
        }

        Collections.sort(modes);
        mResolutionFullscreen = new SimpleChangableListModel<ModeEntry>(modes);
        mResolutionWindowed = new SimpleChangableListModel<ModeEntry>();

        for(int i=0 ; i<WINDOWED_MODES.length ; i+=2) {
            int w = WINDOWED_MODES[i+0];
            int h = WINDOWED_MODES[i+1];
            if(w <= desktopMode.getWidth() && h <= desktopMode.getHeight()) {
                mResolutionWindowed.addElement(new ModeEntry(new DisplayMode(w, h)));
            }
        }
        
        mFullscreen = new SimpleBooleanModel();
        mFullscreen.addCallback(new Runnable() {
            public void run() {
                setModeList();
            }
        });
        
        lResolution = new Label("Resolution");
        cResolution = new ComboBox<ModeEntry>();
        cResolution.setComputeWidthFromModel(true);
        cResolution.addCallback(new Runnable() {
            public void run() {
                selectionChanged();
            }
        });
        lFullscreen = new Label("Fullscreen");
        cFullscreen = new ToggleButton(mFullscreen);
        cFullscreen.setTheme("checkbox");

        setModeList();

        bAccept = new Button();
        bAccept.setTheme("btnAccept");
        bAccept.addCallback(new ButtonCallback(CallbackReason.ACCEPT));

        bCancel = new Button();
        bCancel.setTheme("btnCancel");
        bCancel.addCallback(new ButtonCallback(CallbackReason.CANCEL));

        selectionChanged();
                
        Group ghLabels = createParallelGroup().
                addWidget(lFullscreen).
                addWidget(lResolution);
        Group ghControls = createParallelGroup().
                addWidget(cFullscreen, Alignment.LEFT).
                addWidget(cResolution);

        setHorizontalGroup(createParallelGroup().
                addGroup(createSequentialGroup().addGroup(ghLabels).addGroup(ghControls).addGap()).
                addGroup(createSequentialGroup().addGap().addWidget(bAccept).addGap(DialogLayout.MEDIUM_GAP).addWidget(bCancel)));
        setVerticalGroup(createSequentialGroup().
                addGroup(createParallelGroup().addWidget(lFullscreen).addWidget(cFullscreen)).
                addGroup(createParallelGroup().addWidget(lResolution).addWidget(cResolution)).
                addGap(DialogLayout.MEDIUM_GAP, DialogLayout.MEDIUM_GAP, Short.MAX_VALUE).
                addGroup(createParallelGroup().addWidget(bAccept).addWidget(bCancel)));
    }

    public void addCallback(CallbackWithReason<CallbackReason> cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, CallbackWithReason.class);
    }

    public void removeCallback(CallbackWithReason<CallbackReason> cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    public void readSettings() {
        mFullscreen.setValue(prefs.getBoolean("fullscreen", false));

        int resX = prefs.getInt("resX", 640);
        int resY = prefs.getInt("resY", 480);
        selectMode(resX, resY);
    }

    public boolean storeSettings() {
        DisplayMode mode = getSelectedMode();
        if(mode != null) {
            prefs.putBoolean("fullscreen", mFullscreen.getValue());
            prefs.putInt("resX", mode.getWidth());
            prefs.putInt("resY", mode.getHeight());
            return true;
        }
        return false;
    }
    
    public VideoMode getSelectedVideoMode() {
        DisplayMode mode = getSelectedMode();
        if(mode != null) {
            return new VideoMode(mode, mFullscreen.getValue());
        }
        return null;
    }

    private DisplayMode getSelectedMode() {
        int selectedMode = cResolution.getSelected();
        if(selectedMode >= 0) {
            return ((ModeEntry)cResolution.getModel().getEntry(selectedMode)).mode;
        }
        return null;
    }

    private void selectMode(int width, int height) {
        ListModel<ModeEntry> model = getListModel();
        int matchedMode = -1;
        for(int i=0 ; i<model.getNumEntries() ; i++) {
            DisplayMode mode = model.getEntry(i).mode;
            if(mode.getWidth() == width && mode.getHeight() == height) {
                matchedMode = i;
                break;
            }
        }

        cResolution.setSelected(matchedMode);
    }

    private ListModel<ModeEntry> getListModel() {
        if(mFullscreen.getValue()) {
            return mResolutionFullscreen;
        } else {
            return mResolutionWindowed;
        }
    }

    void setModeList() {
        DisplayMode current = getSelectedMode();
        cResolution.setModel(getListModel());
        if(current != null) {
            selectMode(current.getWidth(), current.getHeight());
        }
    }
    
    void selectionChanged() {
        bAccept.setEnabled(cResolution.getSelected() >= 0);
    }
    
    private void addModeToList(ArrayList<ModeEntry> modes, DisplayMode dm) {
        int entryToReplace = -1;
        for(int idx = 0 ; idx < modes.size() ; idx++) {
            DisplayMode mode = modes.get(idx).mode;
            if(mode.getWidth() == dm.getWidth() &&
                    mode.getHeight() == dm.getHeight()) {
                if(mode.getFrequency() > dm.getFrequency()) {
                    entryToReplace = idx;
                    break;
                } else {
                    // better one already in the list
                    return;
                }
            }
        }
        
        ModeEntry me = new ModeEntry(dm);
        if(entryToReplace >= 0) {
            modes.set(entryToReplace, me);
        } else {
            modes.add(me);
        }
    }

    protected void fireCallback(CallbackReason reason) {
        CallbackSupport.fireCallbacks(callbacks, reason);
    }

    static class ModeEntry implements Comparable<ModeEntry> {
        final DisplayMode mode;
        ModeEntry(DisplayMode mode) {
            this.mode = mode;
        }
        @Override
        public String toString() {
            return mode.getWidth() + "x" + mode.getHeight();
        }
        public int compareTo(ModeEntry o) {
            int diff = mode.getHeight() - o.mode.getHeight();
            if(diff == 0) {
                diff = mode.getWidth() - o.mode.getWidth();
            }
            return diff;
        }
    }

    /**
     * A utility class used as callback for the "accept" and "cancel" buttons.
     *
     * It will call {@code fireCallback} with the selected button
     * @see #fireCallback(test.VideoSettings.CallbackReason) 
     */
    class ButtonCallback implements Runnable {
        final CallbackReason reason;
        ButtonCallback(CallbackReason reason) {
            this.reason = reason;
        }
        public void run() {
            fireCallback(reason);
        }
    };
}
