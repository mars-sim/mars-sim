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
package de.matthiasmann.twl.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple table model which stores each row as Object[]
 *
 * @author Matthias Mann
 */
public class SimpleTableModel extends AbstractTableModel {

    private final String[] columnHeaders;
    private final ArrayList<Object[]> rows;

    public SimpleTableModel(String[] columnHeaders) {
        if(columnHeaders.length < 1) {
            throw new IllegalArgumentException("must have atleast one column");
        }
        this.columnHeaders = columnHeaders.clone();
        this.rows = new ArrayList<Object[]>();
    }

    public int getNumColumns() {
        return columnHeaders.length;
    }

    public String getColumnHeaderText(int column) {
        return columnHeaders[column];
    }

    public void setColumnHeaderText(int column, String text) {
        if(text == null) {
            throw new NullPointerException("text");
        }
        columnHeaders[column] = text;
        fireColumnHeaderChanged(column);
    }

    public int getNumRows() {
        return rows.size();
    }

    public Object getCell(int row, int column) {
        return rows.get(row)[column];
    }

    public void setCell(int row, int column, Object data) {
        rows.get(row)[column] = data;
        fireCellChanged(row, column);
    }

    public void addRow(Object ... data) {
        insertRow(rows.size(), data);
    }

    public void addRows(Collection<Object[]> rows) {
        insertRows(this.rows.size(), rows);
    }

    public void insertRow(int index, Object ... data) {
        rows.add(index, createRowData(data));
        fireRowsInserted(index, 1);
    }

    public void insertRows(int index, Collection<Object[]> rows) {
        if(!rows.isEmpty()) {
            ArrayList<Object[]> rowData = new ArrayList<Object[]>();
            for(Object[] row : rows) {
                rowData.add(createRowData(row));
            }
            this.rows.addAll(index, rowData);
            fireRowsInserted(index, rowData.size());
        }
    }

    public void deleteRow(int index) {
        rows.remove(index);
        fireRowsDeleted(index, 1);
    }

    public void deleteRows(int index, int count) {
        int numRows = rows.size();
        if(index < 0 || count < 0 || index >= numRows || count > (numRows - index)) {
            throw new IndexOutOfBoundsException("index="+index+" count="+count+" numRows="+numRows);
        }
        if(count > 0) {
            // delete backwards to not copy the rows around which will be deleted anyway
            for(int i=count ; i-- > 0 ; ) {
                rows.remove(index + i);
            }
            fireRowsDeleted(index, count);
        }
    }
    
    private Object[] createRowData(Object[] data) {
        Object[] rowData = new Object[getNumColumns()];
        System.arraycopy(data, 0, rowData, 0, Math.min(rowData.length, data.length));
        return rowData;
    }
}
