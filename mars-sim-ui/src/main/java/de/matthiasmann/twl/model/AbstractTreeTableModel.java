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

import de.matthiasmann.twl.utils.CallbackSupport;
import java.util.ArrayList;

/**
 *
 * @author Matthias Mann
 */
public abstract class AbstractTreeTableModel extends AbstractTableColumnHeaderModel implements TreeTableModel {

    private final ArrayList<TreeTableNode> children;
    private ChangeListener[] callbacks;

    public AbstractTreeTableModel() {
        this.children = new ArrayList<TreeTableNode>();
    }

    public void addChangeListener(ChangeListener listener) {
        callbacks = CallbackSupport.addCallbackToList(callbacks, listener, ChangeListener.class);
    }

    public void removeChangeListener(ChangeListener listener) {
        callbacks = CallbackSupport.removeCallbackFromList(callbacks, listener);
    }

    public Object getData(int column) {
        return null;
    }

    public Object getTooltipContent(int column) {
        return null;
    }

    public final TreeTableNode getParent() {
        return null;
    }

    public boolean isLeaf() {
        return false;
    }

    public int getNumChildren() {
        return children.size();
    }

    public TreeTableNode getChild(int idx) {
        return children.get(idx);
    }

    public int getChildIndex(TreeTableNode child) {
        for(int i=0,n=children.size() ; i<n ; i++) {
            if(children.get(i) == child) {
                return i;
            }
        }
        return -1;
    }

    protected void insertChild(TreeTableNode node, int idx) {
        assert getChildIndex(node) < 0;
        assert node.getParent() == this;
        children.add(idx, node);
        fireNodesAdded(this, idx, 1);
    }
    
    protected void removeChild(int idx) {
        children.remove(idx);
        fireNodesRemoved(this, idx, 1);
    }

    protected void removeAllChildren() {
        int count = children.size();
        if(count > 0) {
            children.clear();
            fireNodesRemoved(this, 0, count);
        }
    }

    protected void fireNodesAdded(TreeTableNode parent, int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.nodesAdded(parent, idx, count);
            }
        }
    }

    protected void fireNodesRemoved(TreeTableNode parent, int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.nodesRemoved(parent, idx, count);
            }
        }
    }

    protected void fireNodesChanged(TreeTableNode parent, int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.nodesChanged(parent, idx, count);
            }
        }
    }

    protected void fireColumnInserted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnInserted(idx, count);
            }
        }
    }

    protected void fireColumnDeleted(int idx, int count) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnDeleted(idx, count);
            }
        }
    }

    protected void fireColumnHeaderChanged(int column) {
        if(callbacks != null) {
            for(ChangeListener cl : callbacks) {
                cl.columnHeaderChanged(column);
            }
        }
    }
}
