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

import de.matthiasmann.twl.ListBox.CallbackReason;
import de.matthiasmann.twl.model.BitfieldBooleanModel;
import de.matthiasmann.twl.model.FileSystemAutoCompletionDataSource;
import de.matthiasmann.twl.model.FileSystemModel;
import de.matthiasmann.twl.model.FileSystemModel.FileFilter;
import de.matthiasmann.twl.model.FileSystemTreeModel;
import de.matthiasmann.twl.model.IntegerModel;
import de.matthiasmann.twl.model.MRUListModel;
import de.matthiasmann.twl.model.PersistentIntegerModel;
import de.matthiasmann.twl.model.PersistentMRUListModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import de.matthiasmann.twl.model.SimpleMRUListModel;
import de.matthiasmann.twl.model.SimpleListModel;
import de.matthiasmann.twl.model.ToggleButtonModel;
import de.matthiasmann.twl.model.TreeTableModel;
import de.matthiasmann.twl.model.TreeTableNode;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.NaturalSortComparator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.prefs.Preferences;

/**
 * A File selector widget using FileSystemModel
 *
 * @author Matthias Mann
 */
public class FileSelector extends DialogLayout {

    public interface Callback {
        public void filesSelected(Object[] files);
        public void canceled();
    }

    public interface Callback2 extends Callback {
        public void folderChanged(Object folder);
        public void selectionChanged(FileTable.Entry[] selection);
    }

    public static class NamedFileFilter {
        private final String name;
        private final FileSystemModel.FileFilter fileFilter;

        public NamedFileFilter(String name, FileFilter fileFilter) {
            this.name = name;
            this.fileFilter = fileFilter;
        }
        public String getDisplayName() {
            return name;
        }
        public FileSystemModel.FileFilter getFileFilter() {
            return fileFilter;
        }
    }

    public static final NamedFileFilter AllFilesFilter = new NamedFileFilter("All files", null);

    private final IntegerModel flags;
    private final MRUListModel<String> folderMRU;
    final MRUListModel<String> filesMRU;

    private final TreeComboBox currentFolder;
    private final Label labelCurrentFolder;
    private final FileTable fileTable;
    private final ScrollPane fileTableSP;
    private final Button btnUp;
    private final Button btnHome;
    private final Button btnFolderMRU;
    private final Button btnFilesMRU;
    private final Button btnOk;
    private final Button btnCancel;
    private final Button btnRefresh;
    private final Button btnShowFolders;
    private final Button btnShowHidden;
    private final ComboBox<String> fileFilterBox;
    private final FileFiltersModel fileFiltersModel;
    private final EditFieldAutoCompletionWindow autoCompletion;

    private boolean allowFolderSelection;
    private Callback[] callbacks;
    private NamedFileFilter activeFileFilter;

    FileSystemModel fsm;
    private FileSystemTreeModel model;

    private Widget userWidgetBottom;
    private Widget userWidgetRight;

    private Object fileToSelectOnSetCurrentNode;
    
    /**
     * Create a FileSelector without persistent state
     */
    public FileSelector() {
        this(null, null);
    }

    public FileSelector(Preferences prefs, String prefsKey) {
        if((prefs == null) != (prefsKey == null)) {
            throw new IllegalArgumentException("'prefs' and 'prefsKey' must both be valid or both null");
        }

        if(prefs != null) {
            flags     = new PersistentIntegerModel(prefs, prefsKey.concat("_Flags"), 0, 0xFFFF, 0);
            folderMRU = new PersistentMRUListModel<String>(10, String.class, prefs, prefsKey.concat("_foldersMRU"));
            filesMRU  = new PersistentMRUListModel<String>(20, String.class, prefs, prefsKey.concat("_filesMRU"));
        } else {
            flags     = new SimpleIntegerModel(0, 0xFFFF, 0);
            folderMRU = new SimpleMRUListModel<String>(10);
            filesMRU  = new SimpleMRUListModel<String>(20);
        }

        currentFolder = new TreeComboBox();
        currentFolder.setTheme("currentFolder");
        fileTable = new FileTable();
        fileTable.setTheme("fileTable");
        fileTable.addCallback(new FileTable.Callback() {
            public void selectionChanged() {
                FileSelector.this.selectionChanged();
            }
            public void sortingChanged() {
            }
        });

        btnUp = new Button();
        btnUp.setTheme("buttonUp");
        btnUp.addCallback(new Runnable() {
            public void run() {
                goOneLevelUp();
            }
        });

        btnHome = new Button();
        btnHome.setTheme("buttonHome");
        btnHome.addCallback(new Runnable() {
            public void run() {
                goHome();
            }
        });

        btnFolderMRU = new Button();
        btnFolderMRU.setTheme("buttonFoldersMRU");
        btnFolderMRU.addCallback(new Runnable() {
            public void run() {
                showFolderMRU();
            }
        });

        btnFilesMRU = new Button();
        btnFilesMRU.setTheme("buttonFilesMRU");
        btnFilesMRU.addCallback(new Runnable() {
            public void run() {
                showFilesMRU();
            }
        });

        btnOk = new Button();
        btnOk.setTheme("buttonOk");
        btnOk.addCallback(new Runnable() {
            public void run() {
                acceptSelection();
            }
        });

        btnCancel = new Button();
        btnCancel.setTheme("buttonCancel");
        btnCancel.addCallback(new Runnable() {
            public void run() {
                fireCanceled();
            }
        });

        currentFolder.setPathResolver(new TreeComboBox.PathResolver() {
            public TreeTableNode resolvePath(TreeTableModel model, String path) throws IllegalArgumentException {
                return FileSelector.this.resolvePath(path);
            }
        });
        currentFolder.addCallback(new TreeComboBox.Callback() {
            public void selectedNodeChanged(TreeTableNode node, TreeTableNode previousChildNode) {
                setCurrentNode(node, previousChildNode);
            }
        });

        autoCompletion = new EditFieldAutoCompletionWindow(currentFolder.getEditField());
        autoCompletion.setUseInvokeAsync(true);
        currentFolder.getEditField().setAutoCompletionWindow(autoCompletion);

        fileTable.setAllowMultiSelection(true);
        fileTable.addCallback(new TableBase.Callback() {
            public void mouseDoubleClicked(int row, int column) {
                acceptSelection();
            }
            public void mouseRightClick(int row, int column, Event evt) {
            }
            public void columnHeaderClicked(int column) {
            }
        });

        activeFileFilter = AllFilesFilter;
        fileFiltersModel = new FileFiltersModel();
        fileFilterBox = new ComboBox<String>(fileFiltersModel);
        fileFilterBox.setTheme("fileFiltersBox");
        fileFilterBox.setComputeWidthFromModel(true);
        fileFilterBox.setVisible(false);
        fileFilterBox.addCallback(new Runnable() {
            public void run() {
                fileFilterChanged();
            }
        });
        
        labelCurrentFolder = new Label("Folder");
        labelCurrentFolder.setLabelFor(currentFolder);

        fileTableSP = new ScrollPane(fileTable);

        Runnable showBtnCallback = new Runnable() {
            public void run() {
                refreshFileTable();
            }
        };

        btnRefresh = new Button();
        btnRefresh.setTheme("buttonRefresh");
        btnRefresh.addCallback(showBtnCallback);
        
        btnShowFolders = new Button(new ToggleButtonModel(new BitfieldBooleanModel(flags, 0), true));
        btnShowFolders.setTheme("buttonShowFolders");
        btnShowFolders.addCallback(showBtnCallback);

        btnShowHidden = new Button(new ToggleButtonModel(new BitfieldBooleanModel(flags, 1), false));
        btnShowHidden.setTheme("buttonShowHidden");
        btnShowHidden.addCallback(showBtnCallback);

        addActionMapping("goOneLevelUp", "goOneLevelUp");
        addActionMapping("acceptSelection", "acceptSelection");
    }
    
    protected void createLayout() {
        setHorizontalGroup(null);
        setVerticalGroup(null);
        removeAllChildren();

        add(fileTableSP);
        add(fileFilterBox);
        add(btnOk);
        add(btnCancel);
        add(btnRefresh);
        add(btnShowFolders);
        add(btnShowHidden);
        add(labelCurrentFolder);
        add(currentFolder);
        add(btnFolderMRU);
        add(btnUp);
        
        Group hCurrentFolder = createSequentialGroup()
                .addWidget(labelCurrentFolder)
                .addWidget(currentFolder)
                .addWidget(btnFolderMRU)
                .addWidget(btnUp)
                .addWidget(btnHome);
        Group vCurrentFolder = createParallelGroup()
                .addWidget(labelCurrentFolder)
                .addWidget(currentFolder)
                .addWidget(btnFolderMRU)
                .addWidget(btnUp)
                .addWidget(btnHome);

        Group hButtonGroup = createSequentialGroup()
                .addWidget(btnRefresh)
                .addGap(MEDIUM_GAP)
                .addWidget(btnShowFolders)
                .addWidget(btnShowHidden)
                .addWidget(fileFilterBox)
                .addGap("buttonBarLeft")
                .addWidget(btnFilesMRU)
                .addGap("buttonBarSpacer")
                .addWidget(btnOk)
                .addGap("buttonBarSpacer")
                .addWidget(btnCancel)
                .addGap("buttonBarRight");
        Group vButtonGroup = createParallelGroup()
                .addWidget(btnRefresh)
                .addWidget(btnShowFolders)
                .addWidget(btnShowHidden)
                .addWidget(fileFilterBox)
                .addWidget(btnFilesMRU)
                .addWidget(btnOk)
                .addWidget(btnCancel);

        Group horz = createParallelGroup()
                .addGroup(hCurrentFolder)
                .addWidget(fileTableSP);

        Group vert = createSequentialGroup()
                .addGroup(vCurrentFolder)
                .addWidget(fileTableSP);

        if(userWidgetBottom != null) {
            horz.addWidget(userWidgetBottom);
            vert.addWidget(userWidgetBottom);
        }

        if(userWidgetRight != null) {
            horz = createParallelGroup().addGroup(createSequentialGroup()
                    .addGroup(horz)
                    .addWidget(userWidgetRight));
            vert = createSequentialGroup().addGroup(createParallelGroup()
                    .addGroup(vert)
                    .addWidget(userWidgetRight));
        }

        setHorizontalGroup(horz.addGroup(hButtonGroup));
        setVerticalGroup(vert.addGroup(vButtonGroup));
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        createLayout();
    }

    public FileSystemModel getFileSystemModel() {
        return fsm;
    }

    public void setFileSystemModel(FileSystemModel fsm) {
        this.fsm = fsm;
        if(fsm == null) {
            model = null;
            currentFolder.setModel(null);
            fileTable.setCurrentFolder(null, null);
            autoCompletion.setDataSource(null);
        } else {
            model = new FileSystemTreeModel(fsm);
            model.setSorter(new NameSorter(fsm));
            currentFolder.setModel(model);
            currentFolder.setSeparator(fsm.getSeparator());
            autoCompletion.setDataSource(new FileSystemAutoCompletionDataSource(fsm,
                    FileSystemTreeModel.FolderFilter.instance));
            if(!gotoFolderFromMRU(0) && !goHome()) {
                setCurrentNode(model);
            }
        }
    }

    public boolean getAllowMultiSelection() {
        return fileTable.getAllowMultiSelection();
    }

    /**
     * Controls if multi selection is allowed.
     *
     * Default is true.
     *
     * @param allowMultiSelection true if multiple files can be selected.
     */
    public void setAllowMultiSelection(boolean allowMultiSelection) {
        fileTable.setAllowMultiSelection(allowMultiSelection);
    }

    public boolean getAllowFolderSelection() {
        return allowFolderSelection;
    }

    /**
     * Controls if folders can be selected. If false then the "Ok" button
     * is disabled when a folder is selected.
     *
     * Default is false.
     *
     * @param allowFolderSelection true if folders can be selected
     */
    public void setAllowFolderSelection(boolean allowFolderSelection) {
        this.allowFolderSelection = allowFolderSelection;
        selectionChanged();
    }
    
    public boolean getAllowHorizontalScrolling() {
        return fileTableSP.getFixed() != ScrollPane.Fixed.HORIZONTAL;
    }
    
    /**
     * Controls if the file table allows horizontal scrolling or not.
     * 
     * Default is true.
     * 
     * @param allowHorizontalScrolling true if horizontal scrolling is allowed
     */
    public void setAllowHorizontalScrolling(boolean allowHorizontalScrolling) {
        fileTableSP.setFixed(allowHorizontalScrolling
                ? ScrollPane.Fixed.NONE
                : ScrollPane.Fixed.HORIZONTAL);
    }

    public void addCallback(Callback callback) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, callback, Callback.class);
    }

    public void removeCallback(Callback callback) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, callback);
    }

    public Widget getUserWidgetBottom() {
        return userWidgetBottom;
    }

    public void setUserWidgetBottom(Widget userWidgetBottom) {
        this.userWidgetBottom = userWidgetBottom;
        createLayout();
    }

    public Widget getUserWidgetRight() {
        return userWidgetRight;
    }

    public void setUserWidgetRight(Widget userWidgetRight) {
        this.userWidgetRight = userWidgetRight;
        createLayout();
    }

    public FileTable getFileTable() {
        return fileTable;
    }

    public void setOkButtonEnabled(boolean enabled) {
        btnOk.setEnabled(enabled);
    }

    public Object getCurrentFolder() {
        Object node = currentFolder.getCurrentNode();
        if(node instanceof FileSystemTreeModel.FolderNode) {
            return ((FileSystemTreeModel.FolderNode)node).getFolder();
        } else {
            return null;
        }
    }

    public boolean setCurrentFolder(Object folder) {
        FileSystemTreeModel.FolderNode node = model.getNodeForFolder(folder);
        if(node != null) {
            setCurrentNode(node);
            return true;
        }
        return false;
    }

    public boolean selectFile(Object file) {
        if(fsm == null) {
            return false;
        }
        Object parent = fsm.getParent(file);
        if(setCurrentFolder(parent)) {
            return fileTable.setSelection(file);
        }
        return false;
    }
    
    public void clearSelection() {
        fileTable.clearSelection();
    }

    /**
     * Adds a named file filter to the FileSelector.
     *
     * The first added file filter is selected as default.
     *
     * @param filter the file filter.
     * @throws NullPointerException if filter is null
     * @see #AllFilesFilter
     */
    public void addFileFilter(NamedFileFilter filter) {
        if(filter == null) {
            throw new NullPointerException("filter");
        }
        fileFiltersModel.addFileFilter(filter);
        fileFilterBox.setVisible(fileFiltersModel.getNumEntries() > 0);
        if(fileFilterBox.getSelected() < 0) {
            fileFilterBox.setSelected(0);
        }
    }

    public void removeFileFilter(NamedFileFilter filter) {
        if(filter == null) {
            throw new NullPointerException("filter");
        }
        fileFiltersModel.removeFileFilter(filter);
        if(fileFiltersModel.getNumEntries() == 0) {
            fileFilterBox.setVisible(false);
            setFileFilter(AllFilesFilter);
        }
    }

    public void removeAllFileFilters() {
        fileFiltersModel.removeAll();
        fileFilterBox.setVisible(false);
        setFileFilter(AllFilesFilter);
    }

    public void setFileFilter(NamedFileFilter filter) {
        if(filter == null) {
            throw new NullPointerException("filter");
        }
        int idx = fileFiltersModel.findFilter(filter);
        if(idx < 0) {
            throw new IllegalArgumentException("filter not registered");
        }
        fileFilterBox.setSelected(idx);
    }

    public NamedFileFilter getFileFilter() {
        return activeFileFilter;
    }

    public boolean getShowFolders() {
        return btnShowFolders.getModel().isSelected();
    }

    public void setShowFolders(boolean showFolders) {
        btnShowFolders.getModel().setSelected(showFolders);
    }

    public boolean getShowHidden() {
        return btnShowHidden.getModel().isSelected();
    }

    public void setShowHidden(boolean showHidden) {
        btnShowHidden.getModel().setSelected(showHidden);
    }

    public void goOneLevelUp() {
        TreeTableNode node = currentFolder.getCurrentNode();
        TreeTableNode parent = node.getParent();
        if(parent != null) {
            setCurrentNode(parent, node);
        }
    }

    public boolean goHome() {
        if(fsm != null) {
            Object folder = fsm.getSpecialFolder(FileSystemModel.SPECIAL_FOLDER_HOME);
            if(folder != null) {
                return setCurrentFolder(folder);
            }
        }
        return false;
    }

    public void acceptSelection() {
        FileTable.Entry[] selection = fileTable.getSelection();
        if(selection.length == 1) {
            FileTable.Entry entry = selection[0];
            if(entry != null && entry.isFolder) {
                setCurrentFolder(entry.obj);
                return;
            }
        }
        fireAcceptCallback(selection);
    }

    void fileFilterChanged() {
        int idx = fileFilterBox.getSelected();
            if(idx >= 0) {
            NamedFileFilter filter = fileFiltersModel.getFileFilter(idx);
            activeFileFilter = filter;
            fileTable.setFileFilter(filter.getFileFilter());
        }
    }

    void fireAcceptCallback(FileTable.Entry[] selection) {
        if(callbacks != null) {
            Object[] objects = new Object[selection.length];
            for(int i=0 ; i<selection.length ; i++) {
                FileTable.Entry e = selection[i];
                if(e.isFolder && !allowFolderSelection) {
                    return;
                }
                objects[i] = e.obj;
            }
            addToMRU(selection);
            for(Callback cb : callbacks) {
                cb.filesSelected(objects);
            }
        }
    }

    void fireCanceled() {
        if(callbacks != null) {
            for(Callback cb : callbacks) {
                cb.canceled();
            }
        }
    }

    void selectionChanged() {
        boolean foldersSelected = false;
        boolean filesSelected = false;
        FileTable.Entry[] selection = fileTable.getSelection();
        for(FileTable.Entry entry : selection) {
            if(entry.isFolder) {
                foldersSelected = true;
            } else {
                filesSelected = true;
            }
        }
        if(allowFolderSelection) {
            btnOk.setEnabled(filesSelected ||  foldersSelected);
        } else {
            btnOk.setEnabled(filesSelected && !foldersSelected);
        }
        if(callbacks != null) {
            for(Callback cb : callbacks) {
                if(cb instanceof Callback2) {
                    ((Callback2)cb).selectionChanged(selection);
                }
            }
        }
    }

    protected void setCurrentNode(TreeTableNode node, TreeTableNode childToSelect) {
        if(childToSelect instanceof FileSystemTreeModel.FolderNode) {
            fileToSelectOnSetCurrentNode = ((FileSystemTreeModel.FolderNode)childToSelect).getFolder();
        }
        setCurrentNode(node);
    }
    
    protected void setCurrentNode(TreeTableNode node) {
        currentFolder.setCurrentNode(node);
        refreshFileTable();
        if(callbacks != null) {
            Object curFolder = getCurrentFolder();
            for(Callback cb : callbacks) {
                if(cb instanceof Callback2) {
                    ((Callback2)cb).folderChanged(curFolder);
                }
            }
        }
        if(fileToSelectOnSetCurrentNode != null) {
            fileTable.setSelection(fileToSelectOnSetCurrentNode);
            fileToSelectOnSetCurrentNode = null;
        }
    }

    void refreshFileTable() {
        fileTable.setShowFolders(btnShowFolders.getModel().isSelected());
        fileTable.setShowHidden(btnShowHidden.getModel().isSelected());
        fileTable.setCurrentFolder(fsm, getCurrentFolder());
    }

    TreeTableNode resolvePath(String path) throws IllegalArgumentException {
        Object obj = fsm.getFile(path);
        fileToSelectOnSetCurrentNode = null;
        if(obj != null) {
            if(fsm.isFile(obj)) {
                fileToSelectOnSetCurrentNode = obj;
                obj = fsm.getParent(obj);
            }
            FileSystemTreeModel.FolderNode node = model.getNodeForFolder(obj);
            if(node != null) {
                return node;
            }
        }
        throw new IllegalArgumentException("Could not resolve: " + path);
    }

    void showFolderMRU() {
        final PopupWindow popup = new PopupWindow(this);
        final ListBox<String> listBox = new ListBox<String>(folderMRU);
        popup.setTheme("fileselector-folderMRUpopup");
        popup.add(listBox);
        if(popup.openPopup()) {
            popup.setInnerSize(getInnerWidth()*2/3, getInnerHeight()*2/3);
            popup.setPosition(btnFolderMRU.getX() - popup.getWidth(), btnFolderMRU.getY());
            listBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
                public void callback(CallbackReason reason) {
                    if(reason.actionRequested()) {
                        popup.closePopup();
                        int idx = listBox.getSelected();
                        if(idx >= 0) {
                            gotoFolderFromMRU(idx);
                        }
                    }
                }
            });
        }
    }

    void showFilesMRU() {
        final PopupWindow popup = new PopupWindow(this);
        final DialogLayout layout = new DialogLayout();
        final ListBox<String> listBox = new ListBox<String>(filesMRU);
        final Button popupBtnOk = new Button();
        final Button popupBtnCancel = new Button();
        popupBtnOk.setTheme("buttonOk");
        popupBtnCancel.setTheme("buttonCancel");
        popup.setTheme("fileselector-filesMRUpopup");
        popup.add(layout);
        layout.add(listBox);
        layout.add(popupBtnOk);
        layout.add(popupBtnCancel);

        DialogLayout.Group hBtnGroup = layout.createSequentialGroup()
                .addGap().addWidget(popupBtnOk).addWidget(popupBtnCancel);
        DialogLayout.Group vBtnGroup = layout.createParallelGroup()
                .addWidget(popupBtnOk).addWidget(popupBtnCancel);
        layout.setHorizontalGroup(layout.createParallelGroup().addWidget(listBox).addGroup(hBtnGroup));
        layout.setVerticalGroup(layout.createSequentialGroup().addWidget(listBox).addGroup(vBtnGroup));
        
        if(popup.openPopup()) {
            popup.setInnerSize(getInnerWidth()*2/3, getInnerHeight()*2/3);
            popup.setPosition(getInnerX() + (getInnerWidth() - popup.getWidth())/2, btnFilesMRU.getY() - popup.getHeight());

            final Runnable okCB = new Runnable() {
                public void run() {
                    int idx = listBox.getSelected();
                    if(idx >= 0) {
                        Object obj = fsm.getFile(filesMRU.getEntry(idx));
                        if(obj != null) {
                            popup.closePopup();
                            fireAcceptCallback(new FileTable.Entry[] {
                                new FileTable.Entry(fsm, obj, fsm.getParent(obj) == null)
                            });
                        } else {
                            filesMRU.removeEntry(idx);
                        }
                    }
                }
            };
            popupBtnOk.addCallback(okCB);
            popupBtnCancel.addCallback(new Runnable() {
                public void run() {
                    popup.closePopup();
                }
            });
            listBox.addCallback(new CallbackWithReason<ListBox.CallbackReason>() {
                public void callback(CallbackReason reason) {
                    if(reason.actionRequested()) {
                        okCB.run();
                    }
                }
            });
        }
    }

    private void addToMRU(FileTable.Entry[] selection) {
        for(FileTable.Entry entry : selection) {
            filesMRU.addEntry(entry.getPath());
        }
        folderMRU.addEntry(fsm.getPath(getCurrentFolder()));
    }

    boolean gotoFolderFromMRU(int idx) {
        if(idx >= folderMRU.getNumEntries()) {
            return false;
        }
        String path = folderMRU.getEntry(idx);
        try {
            TreeTableNode node = resolvePath(path);
            setCurrentNode(node);
            return true;
        } catch(IllegalArgumentException ex) {
            folderMRU.removeEntry(idx);
            return false;
        }
    }

    static class FileFiltersModel extends SimpleListModel<String> {
        private final ArrayList<NamedFileFilter> filters = new ArrayList<NamedFileFilter>();
        public NamedFileFilter getFileFilter(int index) {
            return filters.get(index);
        }
        public String getEntry(int index) {
            NamedFileFilter filter = getFileFilter(index);
            return filter.getDisplayName();
        }
        public int getNumEntries() {
            return filters.size();
        }
        public void addFileFilter(NamedFileFilter filter) {
            int index = filters.size();
            filters.add(filter);
            fireEntriesInserted(index, index);
        }
        public void removeFileFilter(NamedFileFilter filter) {
            int idx = filters.indexOf(filter);
            if(idx >= 0) {
                filters.remove(idx);
                fireEntriesDeleted(idx, idx);
            }
        }
        public int findFilter(NamedFileFilter filter) {
            return filters.indexOf(filter);
        }
        void removeAll() {
            filters.clear();
            fireAllChanged();
        }
    }

    /**
     * A file object comparator which delegates to a String comprataor to sort based
     * on the name of the file objects.
     */
    public static class NameSorter implements Comparator<Object> {
        private final FileSystemModel fsm;
        private final Comparator<String> nameComparator;

        /**
         * Creates a new comparator which uses {@code NaturalSortComparator.stringComparator} to sort the names
         * @param fsm the file system model
         */
        public NameSorter(FileSystemModel fsm) {
            this.fsm = fsm;
            this.nameComparator = NaturalSortComparator.stringComparator;
        }

        /**
         * Creates a new comparator which uses the specified String comparator to sort the names
         * @param fsm the file system model
         * @param nameComparator the name comparator
         */
        public NameSorter(FileSystemModel fsm, Comparator<String> nameComparator) {
            this.fsm = fsm;
            this.nameComparator = nameComparator;
        }

        public int compare(Object o1, Object o2) {
            return nameComparator.compare(fsm.getName(o1), fsm.getName(o2));
        }
    }
}
