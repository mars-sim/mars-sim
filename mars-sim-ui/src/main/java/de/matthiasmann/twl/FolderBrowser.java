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
package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.model.FileSystemModel;
import de.matthiasmann.twl.model.FileSystemTreeModel.FolderFilter;
import de.matthiasmann.twl.model.JavaFileSystemModel;
import de.matthiasmann.twl.model.SimpleListModel;
import de.matthiasmann.twl.utils.NaturalSortComparator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A folder browser with plugable file system.
 * 
 * @author Matthias Mann
 */
public class FolderBrowser extends Widget {

    final FileSystemModel fsm;
    final ListBox<Object> listbox;
    final FolderModel model;
    private final BoxLayout curFolderGroup;
    private Runnable[] selectionChangedCallbacks;

    Comparator<String> folderComparator;
    private Object currentFolder;
    private Runnable[] callbacks;
    
    public FolderBrowser() {
        this(JavaFileSystemModel.getInstance());
    }
    
    public FolderBrowser(FileSystemModel fsm) {
        if(fsm == null) {
            throw new NullPointerException("fsm");
        }
        
        this.fsm = fsm;
        this.model = new FolderModel();
        this.listbox = new ListBox<Object>(model);
        this.curFolderGroup = new BoxLayout();
        
        curFolderGroup.setTheme("currentpathbox");
        curFolderGroup.setScroll(true);
        curFolderGroup.setClip(true);
        curFolderGroup.setAlignment(Alignment.BOTTOM);
        
        listbox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
            private Object lastSelection;
            public void callback(ListBox.CallbackReason reason) {
                if(listbox.getSelected() != ListBox.NO_SELECTION) {
                    if(reason.actionRequested()) {
                        setCurrentFolder(model.getFolder(listbox.getSelected()));
                    }
                }
                Object selection = getSelectedFolder();
                if(selection != lastSelection) {
                    lastSelection = selection;
                    fireSelectionChangedCallback();
                }
            }
        });
        
        add(listbox);
        add(curFolderGroup);
        
        setCurrentFolder(null);
    }

    public void addCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, cb, Runnable.class);
    }

    public void removeCallback(Runnable cb) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, cb);
    }

    protected void doCallback() {
        CallbackSupport.fireCallbacks(callbacks);
    }

    public Comparator<String> getFolderComparator() {
        return folderComparator;
    }

    public void setFolderComparator(Comparator<String> folderComparator) {
        this.folderComparator = folderComparator;
    }

    public FileSystemModel getFileSystemModel() {
        return fsm;
    }

    /**
     * Get the current displayed folder
     * @return the displayed folder or null if root is displayed
     */
    public Object getCurrentFolder() {
        return currentFolder;
    }
    
    public boolean setCurrentFolder(Object folder) {
        if(model.listFolders(folder)) {
            // if we show root and it has only a single entry go directly into it
            if(folder == null && model.getNumEntries() == 1) {
                if(setCurrentFolder(model.getFolder(0))) {
                    return true;
                }
            }
            
            currentFolder = folder;
            listbox.setSelected(ListBox.NO_SELECTION);

            rebuildCurrentFolderGroup();

            doCallback();
            return true;
        }
        return false;
    }

    public boolean goToParentFolder() {
        if(currentFolder != null) {
            Object current = currentFolder;
            if(setCurrentFolder(fsm.getParent(current))) {
                selectFolder(current);
                return true;
            }
        }
        return false;
    }

    /**
     * Get the current selected folder in the list box
     * @return a folder or null if nothing is selected
     */
    public Object getSelectedFolder() {
        if(listbox.getSelected() != ListBox.NO_SELECTION) {
            return model.getFolder(listbox.getSelected());
        }
        return null;
    }

    public boolean selectFolder(Object current) {
        int idx = model.findFolder(current);
        listbox.setSelected(idx);
        return idx != ListBox.NO_SELECTION;
    }

    public void addSelectionChangedCallback(Runnable cb) {
        callbacks = CallbackSupport.addCallbackToList(selectionChangedCallbacks, cb, Runnable.class);
    }

    public void removeSelectionChangedCallback(Runnable cb) {
        selectionChangedCallbacks = CallbackSupport.removeCallbackFromList(selectionChangedCallbacks, cb);
    }

    protected void fireSelectionChangedCallback() {
        CallbackSupport.fireCallbacks(selectionChangedCallbacks);
    }
    
    @Override
    public boolean handleEvent(Event evt) {
        if(evt.isKeyPressedEvent()) {
            switch (evt.getKeyCode()) {
            case Event.KEY_BACK:
                goToParentFolder();
                return true;
            }
        }
        return super.handleEvent(evt);
    }
    
    private void rebuildCurrentFolderGroup() {
        curFolderGroup.removeAllChildren();
        recursiveAddFolder(currentFolder, null);
    }

    private void recursiveAddFolder(final Object folder, final Object subFolder) {
        if(folder != null) {
            recursiveAddFolder(fsm.getParent(folder), folder);
        }
        if(curFolderGroup.getNumChildren() > 0) {
            Label l = new Label(fsm.getSeparator());
            l.setTheme("pathseparator");
            curFolderGroup.add(l);
        }
        String name = getFolderName(folder);
        if(name.endsWith(fsm.getSeparator())) {
            name = name.substring(0, name.length() - 1);
        }
        Button btn = new Button(name);
        btn.addCallback(new Runnable() {
            public void run() {
                if(setCurrentFolder(folder)) {
                    selectFolder(subFolder);
                }
                listbox.requestKeyboardFocus();
            }
        });
        btn.setTheme("pathbutton");
        curFolderGroup.add(btn);
    }

    @Override
    public void adjustSize() {
    }

    @Override
    protected void layout() {
        curFolderGroup.setPosition(getInnerX(), getInnerY());
        curFolderGroup.setSize(getInnerWidth(), curFolderGroup.getHeight());
        listbox.setPosition(getInnerX(), curFolderGroup.getBottom());
        listbox.setSize(getInnerWidth(), Math.max(0, getInnerBottom() - listbox.getY()));
    }
    
    String getFolderName(Object folder) {
        if(folder != null) {
            return fsm.getName(folder);
        } else {
            return "ROOT";
        }
    }
    
    class FolderModel extends SimpleListModel<Object> {
        private Object[] folders = new Object[0];

        public boolean listFolders(Object parent) {
            Object[] newFolders;
            if(parent == null) {
                newFolders = fsm.listRoots();
            } else {
                newFolders = fsm.listFolder(parent, FolderFilter.instance);
            }
            if(newFolders == null) {
                Logger.getLogger(FolderModel.class.getName()).log(Level.WARNING, "can''t list folder: {0}", parent);
                return false;
            }
            Arrays.sort(newFolders, new FileSelector.NameSorter(fsm, (folderComparator != null)
                    ? folderComparator
                    : NaturalSortComparator.stringComparator));
            folders = newFolders;
            fireAllChanged();
            return true;
        }
        
        public int getNumEntries() {
            return folders.length;
        }

        public Object getFolder(int index) {
            return folders[index];
        }
        
        public Object getEntry(int index) {
            Object folder = getFolder(index);
            return getFolderName(folder);
        }
        
        public int findFolder(Object folder) {
            int idx = fsm.find(folders, folder);
            return (idx < 0) ? ListBox.NO_SELECTION : idx;
        }
    }
}
