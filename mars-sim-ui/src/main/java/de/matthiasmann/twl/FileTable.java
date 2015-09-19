/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.model.AbstractTableModel;
import de.matthiasmann.twl.model.DefaultTableSelectionModel;
import de.matthiasmann.twl.model.FileSystemModel;
import de.matthiasmann.twl.model.FileSystemModel.FileFilter;
import de.matthiasmann.twl.model.SortOrder;
import de.matthiasmann.twl.model.TableSelectionModel;
import de.matthiasmann.twl.model.TableSingleSelectionModel;
import de.matthiasmann.twl.utils.CallbackSupport;
import de.matthiasmann.twl.utils.NaturalSortComparator;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * A table showing the content of a folder.
 * Uses FileSystemModel.
 * Supports sorting and filtering.
 *
 * @author Matthias Mann
 */
public class FileTable extends Table {

    public enum SortColumn {
        NAME(NameComparator.instance),
        TYPE(ExtensionComparator.instance),
        SIZE(SizeComparator.instance),
        LAST_MODIFIED(LastModifiedComparator.instance);

        final Comparator<Entry> comparator;
        SortColumn(Comparator<Entry> comparator) {
            this.comparator = comparator;
        }
    }

    public interface Callback {
        public void selectionChanged();
        public void sortingChanged();
    }

    private final FileTableModel fileTableModel;
    private final Runnable selectionChangedListener;
    private TableSelectionModel fileTableSelectionModel;
    private TableSearchWindow tableSearchWindow;
    private SortColumn sortColumn = SortColumn.NAME;
    private SortOrder sortOrder = SortOrder.ASCENDING;

    private boolean allowMultiSelection;
    private FileFilter fileFilter = null;
    private boolean showFolders = true;
    private boolean showHidden = false;

    private FileSystemModel fsm;
    private Object currentFolder;

    private Callback[] fileTableCallbacks;

    public FileTable() {
        fileTableModel = new FileTableModel();
        setModel(fileTableModel);

        selectionChangedListener = new Runnable() {
            public void run() {
                selectionChanged();
            }
        };
    }

    public void addCallback(Callback callback) {
        fileTableCallbacks = CallbackSupport.addCallbackToList(fileTableCallbacks, callback, Callback.class);
    }

    public void removeCallback(Callback callback) {
        fileTableCallbacks = CallbackSupport.removeCallbackFromList(fileTableCallbacks, callback);
    }

    public boolean getShowFolders() {
        return showFolders;
    }

    public void setShowFolders(boolean showFolders) {
        if(this.showFolders != showFolders) {
            this.showFolders = showFolders;
            refreshFileTable();
        }
    }

    public boolean getShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        if(this.showHidden != showHidden) {
            this.showHidden = showHidden;
            refreshFileTable();
        }
    }

    public void setFileFilter(FileFilter filter) {
        // always refresh, filter parameters could have been changed
        fileFilter = filter;
        refreshFileTable();
    }

    public FileFilter getFileFilter() {
        return fileFilter;
    }

    public Entry[] getSelection() {
        return fileTableModel.getEntries(fileTableSelectionModel.getSelection());
    }

    public void setSelection(Object ... files) {
        fileTableSelectionModel.clearSelection();
        for(Object file : files) {
            int idx = fileTableModel.findFile(file);
            if(idx >= 0) {
                fileTableSelectionModel.addSelection(idx, idx);
            }
        }
    }

    public boolean setSelection(Object file) {
        fileTableSelectionModel.clearSelection();
        int idx = fileTableModel.findFile(file);
        if(idx >= 0) {
            fileTableSelectionModel.addSelection(idx, idx);
            scrollToRow(idx);
            return true;
        }
        return false;
    }
    
    public void clearSelection() {
        fileTableSelectionModel.clearSelection();
    }

    public void setSortColumn(SortColumn column) {
        if(column == null) {
            throw new NullPointerException("column");
        }
        if(sortColumn != column) {
            sortColumn = column;
            sortingChanged();
        }
    }

    public void setSortOrder(SortOrder order) {
        if(order == null) {
            throw new NullPointerException("order");
        }
        if(sortOrder != order) {
            sortOrder = order;
            sortingChanged();
        }
    }

    public boolean getAllowMultiSelection() {
        return allowMultiSelection;
    }

    public void setAllowMultiSelection(boolean allowMultiSelection) {
        this.allowMultiSelection = allowMultiSelection;
        if(fileTableSelectionModel != null) {
            fileTableSelectionModel.removeSelectionChangeListener(selectionChangedListener);
        }
        if(tableSearchWindow != null) {
            tableSearchWindow.setModel(null, 0);
        }
        if(allowMultiSelection) {
            fileTableSelectionModel = new DefaultTableSelectionModel();
        } else {
            fileTableSelectionModel = new TableSingleSelectionModel();
        }
        fileTableSelectionModel.addSelectionChangeListener(selectionChangedListener);
        tableSearchWindow = new TableSearchWindow(this, fileTableSelectionModel);
        tableSearchWindow.setModel(fileTableModel, 0);
        setSelectionManager(new TableRowSelectionManager(fileTableSelectionModel));
        setKeyboardSearchHandler(tableSearchWindow);
        selectionChanged();
    }

    public FileSystemModel getFileSystemModel() {
        return fsm;
    }

    public final Object getCurrentFolder() {
        return currentFolder;
    }

    public final boolean isRoot() {
        return currentFolder == null;
    }

    public void setCurrentFolder(FileSystemModel fsm, Object folder) {
        this.fsm = fsm;
        this.currentFolder = folder;
        refreshFileTable();
    }

    public void refreshFileTable() {
        Object[] objs = collectObjects();
        if(objs != null) {
            int lastFileIdx = objs.length;
            Entry[] entries = new Entry[lastFileIdx];
            int numFolders = 0;
            boolean isRoot = isRoot();
            for(int i=0 ; i<objs.length ; i++) {
                Entry e = new Entry(fsm, objs[i], isRoot);
                if(e.isFolder) {
                    entries[numFolders++] = e;
                } else {
                    entries[--lastFileIdx] = e;
                }
            }
            Arrays.sort(entries, 0, numFolders, NameComparator.instance);
            sortFilesAndUpdateModel(entries, numFolders);
        } else {
            sortFilesAndUpdateModel(EMPTY, 0);
        }
        if(tableSearchWindow != null) {
            tableSearchWindow.cancelSearch();
        }
    }

    protected void selectionChanged() {
        if(fileTableCallbacks != null) {
            for(Callback cb : fileTableCallbacks) {
                cb.selectionChanged();
            }
        }
    }

    protected void sortingChanged() {
        setSortArrows();
        sortFilesAndUpdateModel();
        if(fileTableCallbacks != null) {
            for(Callback cb : fileTableCallbacks) {
                cb.sortingChanged();
            }
        }
    }

    private Object[] collectObjects() {
        if(fsm == null) {
            return null;
        }
        if(isRoot()) {
            return fsm.listRoots();
        }
        FileFilter filter = fileFilter;
        if(filter != null || !getShowFolders() || !getShowHidden()) {
            filter = new FileFilterWrapper(filter, getShowFolders(), getShowHidden());
        }
        return fsm.listFolder(currentFolder, filter);
    }
    
    private void sortFilesAndUpdateModel(Entry[] entries, int numFolders) {
        StateSnapshot snapshot = makeSnapshot();
        Arrays.sort(entries, numFolders, entries.length,
                sortOrder.map(sortColumn.comparator));
        fileTableModel.setData(entries, numFolders);
        restoreSnapshot(snapshot);
    }

    @Override
    protected void columnHeaderClicked(int column) {
        super.columnHeaderClicked(column);

        SortColumn thisColumn = SortColumn.values()[column];
        if(sortColumn == thisColumn) {
            setSortOrder(sortOrder.invert());
        } else {
            setSortColumn(thisColumn);
        }
    }

    @Override
    protected void updateColumnHeaderNumbers() {
        super.updateColumnHeaderNumbers();
        setSortArrows();
    }

    protected void setSortArrows() {
        setColumnSortOrderAnimationState(sortColumn.ordinal(), sortOrder);
    }

    private void sortFilesAndUpdateModel() {
        sortFilesAndUpdateModel(fileTableModel.entries, fileTableModel.numFolders);
    }

    private StateSnapshot makeSnapshot() {
        return new StateSnapshot(
                fileTableModel.getEntry(fileTableSelectionModel.getLeadIndex()),
                fileTableModel.getEntry(fileTableSelectionModel.getAnchorIndex()),
                fileTableModel.getEntries(fileTableSelectionModel.getSelection()));
    }

    private void restoreSnapshot(StateSnapshot snapshot) {
        for(Entry e : snapshot.selected) {
            int idx = fileTableModel.findEntry(e);
            if(idx >= 0) {
                fileTableSelectionModel.addSelection(idx, idx);
            }
        }
        int leadIndex = fileTableModel.findEntry(snapshot.leadEntry);
        int anchorIndex = fileTableModel.findEntry(snapshot.anchorEntry);
        fileTableSelectionModel.setLeadIndex(leadIndex);
        fileTableSelectionModel.setAnchorIndex(anchorIndex);
        scrollToRow(Math.max(0, leadIndex));
    }

    static Entry[] EMPTY = new Entry[0];

    public static final class Entry {
        public final FileSystemModel fsm;
        public final Object obj;
        public final String name;
        public final boolean isFolder;
        public final long size;
        /** last modified date - can be null */
        public final Date lastModified;

        public Entry(FileSystemModel fsm, Object obj, boolean isRoot) {
            this.fsm = fsm;
            this.obj = obj;
            this.name = fsm.getName(obj);
            if(isRoot) {
                // don't call getLastModified on roots - causes bad performance
                // on windows when a DVD/CD/Floppy has no media inside
                this.isFolder = true;
                this.lastModified = null;
            } else {
                this.isFolder = fsm.isFolder(obj);
                this.lastModified = new Date(fsm.getLastModified(obj));
            }
            if(isFolder) {
                this.size = 0;
            } else {
                this.size = fsm.getSize(obj);
            }
        }

        public String getExtension() {
            int idx = name.lastIndexOf('.');
            if(idx >= 0) {
                return name.substring(idx+1);
            } else {
                return "";
            }
        }

        public String getPath() {
            return fsm.getPath(obj);
        }

        @Override
        public boolean equals(Object o) {
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            final Entry that = (Entry)o;
            return (this.fsm == that.fsm) && fsm.equals(this.obj, that.obj);
        }

        @Override
        public int hashCode() {
            return (obj != null) ? obj.hashCode() : 203;
        }
    }

    static class FileTableModel extends AbstractTableModel {
        private final DateFormat dateFormat = DateFormat.getDateInstance();
        
        Entry[] entries = EMPTY;
        int numFolders;

        public void setData(Entry[] entries, int numFolders) {
            fireRowsDeleted(0, getNumRows());
            this.entries = entries;
            this.numFolders = numFolders;
            fireRowsInserted(0, getNumRows());
        }

        static String COLUMN_HEADER[] = {"File name", "Type", "Size", "Last modified"};

        public String getColumnHeaderText(int column) {
            return COLUMN_HEADER[column];
        }

        public int getNumColumns() {
            return COLUMN_HEADER.length;
        }

        public Object getCell(int row, int column) {
            Entry e = entries[row];
            if(e.isFolder) {
                switch(column) {
                case 0: return "["+e.name+"]";
                case 1: return "Folder";
                case 2: return "";
                case 3: return formatDate(e.lastModified);
                default: return "??";
                }
            } else {
                switch(column) {
                case 0: return e.name;
                case 1: {
                    String ext = e.getExtension();
                    return (ext.length() == 0) ? "File" : ext+"-file";
                }
                case 2: return formatFileSize(e.size);
                case 3: return formatDate(e.lastModified);
                default: return "??";
                }
            }
        }

        @Override
        public Object getTooltipContent(int row, int column) {
            Entry e = entries[row];
            StringBuilder sb = new StringBuilder(e.name);
            if(!e.isFolder) {
                sb.append("\nSize: ").append(formatFileSize(e.size));
            }
            if(e.lastModified != null) {
                sb.append("\nLast modified: ").append(formatDate(e.lastModified));
            }
            return sb.toString();
        }

        public int getNumRows() {
            return entries.length;
        }

        Entry getEntry(int row) {
            if(row >= 0 && row < entries.length) {
                return entries[row];
            } else {
                return null;
            }
        }

        int findEntry(Entry entry) {
            for(int i=0 ; i<entries.length ; i++) {
                if(entries[i].equals(entry)) {
                    return i;
                }
            }
            return -1;
        }

        int findFile(Object file) {
            for(int i=0 ; i<entries.length ; i++) {
                Entry e = entries[i];
                if(e.fsm.equals(e.obj, file)) {
                    return i;
                }
            }
            return -1;
        }

        Entry[] getEntries(int[] selection) {
            final int count = selection.length;
            if(count == 0) {
                return EMPTY;
            }
            Entry[] result = new Entry[count];
            for(int i=0 ; i<count ; i++) {
                result[i] = entries[selection[i]];
            }
            return result;
        }

        static String SIZE_UNITS[] = {" MB", " KB", " B"};
        static long SIZE_FACTORS[] = {1024*1024, 1024, 1};

        private String formatFileSize(long size) {
            if(size <= 0) {
                return "0 B";
            } else {
                for(int i=0 ;; ++i) {
                    if(size >= SIZE_FACTORS[i]) {
                        long value = (size*10) / SIZE_FACTORS[i];
                        return Long.toString(value / 10) + '.' +
                                Character.forDigit((int)(value % 10), 10) +
                                SIZE_UNITS[i];
                    }
                }
            }
        }

        private String formatDate(Date date) {
            if(date == null) {
                return "";
            }
            return dateFormat.format(date);
        }
    }

    static class StateSnapshot {
        final Entry leadEntry;
        final Entry anchorEntry;
        final Entry[] selected;

        StateSnapshot(Entry leadEntry, Entry anchorEntry, Entry[] selected) {
            this.leadEntry = leadEntry;
            this.anchorEntry = anchorEntry;
            this.selected = selected;
        }
    }

    static class NameComparator implements Comparator<Entry> {
        static final NameComparator instance = new NameComparator();
        public int compare(Entry o1, Entry o2) {
            return NaturalSortComparator.naturalCompare(o1.name, o2.name);
        }
    }

    static class ExtensionComparator implements Comparator<Entry> {
        static final ExtensionComparator instance = new ExtensionComparator();
        public int compare(Entry o1, Entry o2) {
            return NaturalSortComparator.naturalCompare(o1.getExtension(), o2.getExtension());
        }
    }

    static class SizeComparator implements Comparator<Entry> {
        static final SizeComparator instance = new SizeComparator();
        public int compare(Entry o1, Entry o2) {
            return Long.signum(o1.size - o2.size);
        }
    }

    static class LastModifiedComparator implements Comparator<Entry> {
        static final LastModifiedComparator instance = new LastModifiedComparator();
        public int compare(Entry o1, Entry o2) {
            Date lm1 = o1.lastModified;
            Date lm2 = o2.lastModified;
            if(lm1 != null && lm2 != null) {
                return lm1.compareTo(lm2);
            }
            if(lm1 != null) {
                return 1;
            }
            if(lm2 != null) {
                return -1;
            }
            return 0;
        }
    }

    private static class FileFilterWrapper implements FileFilter {
        private final FileFilter base;
        private final boolean showFolder;
        private final boolean showHidden;
        public FileFilterWrapper(FileFilter base, boolean showFolder, boolean showHidden) {
            this.base = base;
            this.showFolder = showFolder;
            this.showHidden = showHidden;
        }
        public boolean accept(FileSystemModel fsm, Object file) {
            if(showHidden || !fsm.isHidden(file)) {
                if(fsm.isFolder(file)) {
                    return showFolder;
                }
                return (base == null) || base.accept(fsm, file);
            }
            return false;
        }
    }

}
