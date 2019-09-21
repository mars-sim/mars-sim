/**
 * Mars Simulation Project
 * ZebraJTable.java
 * @version 3.1.0 2017-09-20
 * @author Manny Kung
 */
// see http://nadeausoftware.com/articles/2008/01/java_tip_how_add_zebra_background_stripes_jtable

package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


/**
 * A WebTable that draws a zebra striped background.
 */
@SuppressWarnings("serial")
public class ZebraJTable extends JTable {

    private Color rowColors[] = new Color[2];
    private boolean drawStripes = false;

    public ZebraJTable(){
    }
    public ZebraJTable(JTable table) {// javax.swing.table.TableModel dataModel ) {
        super(table.getModel());
    }
    public ZebraJTable( int numRows, int numColumns )
    {
        super( numRows, numColumns );
    }
    public ZebraJTable( Object[][] rowData, Object[] columnNames )
    {
        super( rowData, columnNames );
    }
    public ZebraJTable(TableModel dataModel )
    {
        super( dataModel );
    }
    public ZebraJTable(TableModel dataModel,
        TableColumnModel columnModel )
    {
        super( dataModel, columnModel );
    }
    public ZebraJTable(TableModel dataModel,
        TableColumnModel columnModel,
        ListSelectionModel selectionModel )
    {
        super( dataModel, columnModel, selectionModel );
    }
    
//    public ZebraJTable( java.util.Vector<?> rowData,
//        java.util.Vector<?> columnNames )
//    {
//        super( rowData, columnNames );
//    }

    /** Add stripes between cells and behind non-opaque cells. */
    public void paintComponent(Graphics g )
    {
        if ( !(drawStripes = isOpaque( )) )
        {
            super.paintComponent( g );
            return;
        }

        // Paint zebra background stripes
        updateZebraColors( );
        final java.awt.Insets insets = getInsets( );
        final int w   = getWidth( )  - insets.left - insets.right;
        final int h   = getHeight( ) - insets.top  - insets.bottom;
        final int x   = insets.left;
        int y         = insets.top;
        int rowHeight = 16; // A default for empty tables
        final int nItems = getRowCount( );
        for ( int i = 0; i < nItems; i++, y+=rowHeight )
        {
            rowHeight = getRowHeight( i );
            g.setColor( rowColors[i&1] );
            g.fillRect( x, y, w, rowHeight );
        }
        // Use last row height for remainder of table area
        final int nRows = nItems + (insets.top + h - y) / rowHeight;
        for ( int i = nItems; i < nRows; i++, y+=rowHeight )
        {
            g.setColor( rowColors[i&1] );
            g.fillRect( x, y, w, rowHeight );
        }
        final int remainder = insets.top + h - y;
        if ( remainder > 0 )
        {
            g.setColor( rowColors[nRows&1] );
            g.fillRect( x, y, w, remainder );
        }

        // Paint component
        setOpaque( false );
        super.paintComponent( g );
        setOpaque( true );
    }

    /** Add background stripes behind rendered cells. */
    public Component prepareRenderer(
        TableCellRenderer renderer, int row, int col )
    {
        final Component c = super.prepareRenderer( renderer, row, col );
        if ( drawStripes && !isCellSelected( row, col ) )
            c.setBackground( rowColors[row&1] );
        return c;
    }

    /** Add background stripes behind edited cells. */
    public Component prepareEditor(
        TableCellEditor editor, int row, int col )
    {
        final java.awt.Component c = super.prepareEditor( editor, row, col );
        if ( drawStripes && !isCellSelected( row, col ) )
            c.setBackground( rowColors[row&1] );
        return c;
    }

    /** Force the table to fill the viewport's height. */
    public boolean getScrollableTracksViewportHeight( )
    {
        final Component p = getParent( );
        if ( !(p instanceof JViewport) )
            return false;
        return ((JViewport)p).getHeight() > getPreferredSize().height;
    }

//    /** Compute zebra background stripe colors. */
//    private void updateZebraColors( )
//    {
//        if ( (rowColors[0] = getBackground( )) == null )
//        {
//            rowColors[0] = rowColors[1] = Color.white;
//            return;
//        }
//        final Color sel = getSelectionBackground( );
//        if ( sel == null )
//        {
//            rowColors[1] = rowColors[0];
//            return;
//        }
//        final float[] bgHSB = Color.RGBtoHSB(
//            rowColors[0].getRed( ), rowColors[0].getGreen( ),
//            rowColors[0].getBlue( ), null );
//        final float[] selHSB  = java.awt.Color.RGBtoHSB(
//            sel.getRed( ), sel.getGreen( ), sel.getBlue( ), null );
//        rowColors[1] = java.awt.Color.getHSBColor(
//            (selHSB[1]==0.0||selHSB[2]==0.0) ? bgHSB[0] : selHSB[0],
//            0.1f * selHSB[1] + 0.9f * bgHSB[1],
//            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.05f : -0.05f) );
//    }
    
    private void updateZebraColors( )
    {
        if ( (rowColors[1] = getBackground( )) == null )
        {
            rowColors[1] = rowColors[0] = Color.white;
            return;
        }
        final Color sel = getSelectionBackground( );
        if ( sel == null )
        {
            rowColors[0] = rowColors[1];
            return;
        }
        final float[] bgHSB = Color.RGBtoHSB(
            rowColors[1].getRed( ), rowColors[1].getGreen( ),
            rowColors[1].getBlue( ), null );
        final float[] selHSB  = Color.RGBtoHSB(
            sel.getRed( ), sel.getGreen( ), sel.getBlue( ), null );
        rowColors[0] = Color.getHSBColor(
            (selHSB[1]==0.0||selHSB[2]==0.0) ? bgHSB[0] : selHSB[0],
            0.1f * selHSB[1] + 0.9f * bgHSB[1],
//            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.05f : -0.05f) 
            // Match the slightly darkened background color of a zebra row
            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.0005f : -0.0005f) 
            );
    }
    
    public String getToolTipText(MouseEvent e) {
		//TooltipManager.setTooltip (radiationLabel, Msg.getString("TabPanelRadiation.tooltip"), TooltipWay.down);
		return super.getToolTipText();
    }

    public int[] getSelectedRows() {
    	return super.getSelectedRows();
    }
}