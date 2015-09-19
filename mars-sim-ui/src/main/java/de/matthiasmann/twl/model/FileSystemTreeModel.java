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
package de.matthiasmann.twl.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * A tree model which displays the folders of a FileSystemModel
 *
 * @author Matthias Mann
 */
public class FileSystemTreeModel extends AbstractTreeTableModel {

    private final FileSystemModel fsm;
    private final boolean includeLastModified;

    protected Comparator<Object> sorter;

    public FileSystemTreeModel(FileSystemModel fsm, boolean includeLastModified) {
        this.fsm = fsm;
        this.includeLastModified = includeLastModified;

        insertRoots();
    }

    public FileSystemTreeModel(FileSystemModel fsm) {
        this(fsm, false);
    }

    public int getNumColumns() {
        return includeLastModified ? 2 : 1;
    }

    public String getColumnHeaderText(int column) {
        switch(column) {
            case 0:
                return "Folder";
            case 1:
                return "Last modified";
            default:
                return "";
        }
    }

    public FileSystemModel getFileSystemModel() {
        return fsm;
    }

    /**
     * Removes all nodes from the tree and creates the root nodes
     */
    public void insertRoots() {
        removeAllChildren();

        for(Object root : fsm.listRoots()) {
            insertChild(new FolderNode(this, fsm, root), getNumChildren());
        }
    }

    public FolderNode getNodeForFolder(Object obj) {
        Object parent = fsm.getParent(obj);
        TreeTableNode parentNode;
        if(parent == null) {
            parentNode = this;
        } else {
            parentNode = getNodeForFolder(parent);
        }
        if(parentNode != null) {
            for(int i=0 ; i<parentNode.getNumChildren() ; i++) {
                FolderNode node = (FolderNode)parentNode.getChild(i);
                if(fsm.equals(node.folder, obj)) {
                    return node;
                }
            }
        }
        return null;
    }

    public Comparator<Object> getSorter() {
        return sorter;
    }

    /**
     * Sets the sorter used for sorting folders (the root nodes are not sorted).
     *
     * Will call insertRoots() when the sorter is changed.
     *
     * @see #insertRoots()
     * @param sorter The new sorter - can be null
     */
    public void setSorter(Comparator<Object> sorter) {
        if(this.sorter != sorter) {
            this.sorter = sorter;
            insertRoots();
        }
    }

    static final FolderNode[] NO_CHILDREN = new FolderNode[0];
    
    public static class FolderNode implements TreeTableNode {
        private final TreeTableNode parent;
        private final FileSystemModel fsm;
        final Object folder;
        FolderNode[] children;

        protected FolderNode(TreeTableNode parent, FileSystemModel fsm, Object folder) {
            this.parent = parent;
            this.fsm = fsm;
            this.folder = folder;
        }

        public Object getFolder() {
            return folder;
        }

        public Object getData(int column) {
            switch(column) {
                case 0:
                    return fsm.getName(folder);
                case 1:
                    return getlastModified();
                default:
                    return null;
            }
        }

        public Object getTooltipContent(int column) {
            StringBuilder sb = new StringBuilder(fsm.getPath(folder));
            Date lastModified = getlastModified();
            if(lastModified != null) {
                sb.append("\nLast modified: ").append(lastModified);
            }
            return sb.toString();
        }

        public TreeTableNode getChild(int idx) {
            return children[idx];
        }

        public int getChildIndex(TreeTableNode child) {
            for(int i=0,n=children.length ; i<n ; i++) {
                if(children[i] == child) {
                    return i;
                }
            }
            return -1;
        }

        public int getNumChildren() {
            if(children == null) {
                collectChilds();
            }
            return children.length;
        }

        public TreeTableNode getParent() {
            return parent;
        }

        public boolean isLeaf() {
            return false;
        }

        public FileSystemTreeModel getTreeModel() {
            TreeTableNode node = this.parent;
            TreeTableNode nodeParent;
            while((nodeParent = node.getParent()) != null) {
                node = nodeParent;
            }
            return (FileSystemTreeModel)node;
        }

        private void collectChilds() {
            children = NO_CHILDREN;
            try {
                Object[] subFolder = fsm.listFolder(folder, FolderFilter.instance);
                if(subFolder != null && subFolder.length > 0) {
                    Comparator<Object> sorter = getTreeModel().sorter;
                    if(sorter != null) {
                        Arrays.sort(subFolder, sorter);
                    }
                    FolderNode[] newChildren = new FolderNode[subFolder.length];
                    for(int i=0 ; i<subFolder.length ; i++) {
                        newChildren[i] = new FolderNode(this, fsm, subFolder[i]);
                    }
                    children = newChildren;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private Date getlastModified() {
            if(parent instanceof FileSystemTreeModel) {
                // don't call getLastModified on roots - causes bad performance
                // on windows when a DVD/CD/Floppy has no media inside
                return null;
            }
            return new Date(fsm.getLastModified(folder));
        }
    }

    public static final class FolderFilter implements FileSystemModel.FileFilter {
        public static final FolderFilter instance = new FolderFilter();
        
        public boolean accept(FileSystemModel model, Object file) {
            return model.isFolder(file);
        }
    };
}
