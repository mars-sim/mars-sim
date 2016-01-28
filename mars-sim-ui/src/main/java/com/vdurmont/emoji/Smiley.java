package com.vdurmont.emoji;

/**
Copyright(c) 2012 Samuel de Tomas.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.WindowConstants;
import javax.swing.table.TableModel;

/**
* Single cell customization example in JTable.
* Because JTables have not to be so serious!
*
* @author: Samuel de Tomas
* @version: 2012
* @see: www.mendrugox.net
*
*/
public class Smiley extends JFrame
{
/* Sizes & positions */
private static final int CELLSIZE = 17;
private static final int N_ROWS = 17;
private static final int N_COLS = 17;
private static final int PANEL_X = N_COLS*CELLSIZE + CELLSIZE;
private static final int PANEL_Y = N_ROWS*CELLSIZE + CELLSIZE;
private static final int PAD_SIZE = 6;
private static final int EYE_HEIGHT = 2*CELLSIZE/3;
private static final int EYE_ROW = 5;
private static final int EYE_COL = 6;
private static final int EYE_DIST = 4;
private static final int EYE_BR_SIZE = 3;
private static final int MOUTH_HEIGHT = CELLSIZE/2;
private static final int MOUTH_LEN = 5;
private static final int MOUTH_X = 9;
private static final int MOUTH_Y = 6;    
private static final int FACE_TOP_RIGHT_X = 2;
private static final int FACE_TOP_RIGHT_Y = 3;
private static final int FACE_SIZE = 11;
private static final int BANNER_X = 15;
private static final int BANNER_Y = 4;
private static final int BANNER_HEIGHT = CELLSIZE*2;
private static final int BANNER_FNT_SIZE = 12;

/* Colors & texts */
private static final Color BG_COLOR = Color.black;
private static final Color FACE_COLOR = Color.black;
private static final Color FACE_BR_COLOR = Color.lightGray;
private static final Color EYE_COLOR = Color.yellow;
private static final Color EYE_BR_COLOR = Color.orange;
private static final Color MOUTH_COLOR = Color.yellow;
private static final Color BANNER_COLOR = Color.darkGray;
private static final Color BANNER_TEXT_COLOR = Color.white;
private static final String TITLE = "JTable Smiley Example";
private static final String SMILE = "Smile!";
private static final String SERIOUS = "Be serious!";
private static final String BANNER = "MENDRUGOX";
private static final String BANNER_FNT = "Arial"; 

/* Variables */
private JTable table = null; /* The JTable */
private JPanel panel; /* Panel to host the JTable */
private JButton smileyButton; /* The button to make it smile */

/**
* This is the key class. We will replace the default cell renderer in all
* columns to use this and get all the properties from a JLabel.
*/
public class JLabelCellRenderer extends JLabel implements TableCellRenderer
{
    @Override
    public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row,int col)
    {
        JLabel label = (JLabel) value;
        setBackground(label.getBackground());
        setForeground(label.getForeground());
        setText(label.getText());
        setHorizontalAlignment(label.getHorizontalAlignment());
        setOpaque(label.isOpaque());
        setBorder(label.getBorder());
        setMinimumSize(label.getMinimumSize());
        setMaximumSize(label.getMaximumSize());
        setPreferredSize(label.getPreferredSize());
        setFont(label.getFont());
        return this;
    }
}

/* This will be the button handler */
private void changeState(ActionEvent evt)
{
    if (smileyButton.getText().equals(SMILE))
    {
        happyState();
    }
    else
    {
        seriousState();
    }
}

/* This method will set the containers appropriately for our purpose */
private void createContainer()
{
    panel = new JPanel();
    smileyButton = new JButton();
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    getContentPane().setLayout(new GridBagLayout());
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.insets = new Insets(PAD_SIZE, PAD_SIZE, PAD_SIZE, PAD_SIZE);
    getContentPane().add(panel, gbc);
    smileyButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            changeState(evt);
        }
    });
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(PAD_SIZE, PAD_SIZE, PAD_SIZE, PAD_SIZE);
    getContentPane().add(smileyButton, gbc);
    setTitle(TITLE);
    panel.setPreferredSize(new Dimension(PANEL_X,PANEL_Y));
    setResizable(false);        
    pack();
	setLocationRelativeTo(null);
}

/* This method adds our table to the panel */
private void addTableToContainer()
{
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    panel.add(table,gbc);
}

/* JTable creation setting useful properties */
private JTable createTable()
{
    table = new JTable();
    String[] columnNames = new String[N_COLS];
    JLabel[][] data = new JLabel[N_ROWS][N_COLS];
    for (int i = 0; i < N_COLS; i++)
    {
        columnNames[i] = ""; /* We don't care about headers in the example*/
    }
    for (int i = 0; i < N_ROWS; i++) /* Background cells */
    {
        for (int j = 0; j < N_COLS; j++)
        {
            JLabel cell = new JLabel("");
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder());
            cell.setBackground(BG_COLOR);
            data[i][j] = cell;
        }
    }
    /* We can customize the table model. */
    table.setModel(
            new DefaultTableModel(data, columnNames)
            {
                /* We don't want it to be editable */
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            }
    );
    for (int i = 0; i < N_COLS; i++)
    {
        TableColumn column = table.getColumnModel().getColumn(i);
        column.setMinWidth(CELLSIZE);
        column.setPreferredWidth(CELLSIZE);
        column.setCellRenderer(new JLabelCellRenderer());
    }
    table.setRowHeight(CELLSIZE);
    /* We force the table to use our preferred components' size */
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    /* We don't want headers */
    table.setTableHeader(null);
    /* Background in black, opaque and no grid */
    table.setBackground(BG_COLOR);
    table.setShowGrid(false);
    /* It's opaque by default but it's good to know this property */
    table.setOpaque(true);        
    return table;
}

/* Time to paint the face */
private void drawFace()
{
    int rows = FACE_TOP_RIGHT_X + FACE_SIZE;
    int cols = FACE_TOP_RIGHT_Y + FACE_SIZE;
    for (int i = FACE_TOP_RIGHT_X; i < rows; i++)
    {
        for (int j = FACE_TOP_RIGHT_Y; j < cols; j++)
        {
            JLabel jl = (JLabel) table.getModel().getValueAt(i,j);
            if ((i == FACE_TOP_RIGHT_X) || (i == (rows-1)))
            {
                jl.setBackground(FACE_BR_COLOR);
            }
            else if ((j == FACE_TOP_RIGHT_Y) || (j == (cols-1)))
            {
                jl.setBackground(FACE_BR_COLOR);
            }
            else
            {
                jl.setBackground(FACE_COLOR);
                jl.setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }
}

/* Now is the turn for the eyes */
private void drawEyes()
{
    TableModel model = table.getModel();
    table.setRowHeight(EYE_ROW, EYE_HEIGHT);
    JLabel eye = (JLabel) model.getValueAt(EYE_ROW,EYE_COL);
    eye.setBorder(BorderFactory.createLineBorder(EYE_BR_COLOR,EYE_BR_SIZE));
    eye.setBackground(EYE_COLOR);
    eye.setOpaque(true);
    eye = (JLabel) model.getValueAt(EYE_ROW,EYE_COL + EYE_DIST);
    eye.setBorder(BorderFactory.createLineBorder(EYE_BR_COLOR,EYE_BR_SIZE));
    eye.setBackground(EYE_COLOR);
    eye.setOpaque(true);
}

private void setAllButSmile()
{
    drawFace();
    drawEyes();
    table.setRowHeight(MOUTH_X-1, MOUTH_HEIGHT);
    table.setRowHeight(MOUTH_X, MOUTH_HEIGHT);
}

/* This method paints the serious mouth */
private void setSerious()
{
    setAllButSmile();
    TableModel model = table.getModel();
    for (int i = 0; i < MOUTH_LEN; i++)
    {
        JLabel mouth = (JLabel) model.getValueAt(MOUTH_X, MOUTH_Y+i);
        mouth.setBorder(BorderFactory.createEmptyBorder());
        mouth.setBackground(MOUTH_COLOR);
        mouth.setOpaque(true);
    }
}

/* This method paints the smile */
private void setHappy()
{
    setAllButSmile();
    TableModel model = table.getModel();
    for (int i = 0; i < MOUTH_LEN-2; i++)
    {
        JLabel mouth = (JLabel) model.getValueAt(MOUTH_X, MOUTH_Y+i+1);
        mouth.setBorder(BorderFactory.createEmptyBorder());
        mouth.setBackground(MOUTH_COLOR);
        mouth.setOpaque(true);
    }
    JLabel edge1 = (JLabel) model.getValueAt(MOUTH_X-1,MOUTH_Y);
    edge1.setBorder(BorderFactory.createEmptyBorder());
    edge1.setBackground(MOUTH_COLOR);
    edge1.setOpaque(true);
    JLabel edge2 = (JLabel) model.getValueAt(MOUTH_X-1,MOUTH_Y+MOUTH_LEN-1);
    edge2.setBorder(BorderFactory.createEmptyBorder());
    edge2.setBackground(MOUTH_COLOR);
    edge2.setOpaque(true);
}

/* This method paints the banner */
private void drawMendrugoxBanner()
{
    table.setRowHeight(BANNER_X, BANNER_HEIGHT);
    JLabel[] mendrugox = new JLabel[BANNER.length()];
    for (int i = 0; i < BANNER.length(); i++)
    {
        JLabel letterCell = new JLabel("" + BANNER.charAt(i));
        letterCell.setBackground(BANNER_COLOR);
        letterCell.setForeground(BANNER_TEXT_COLOR);
        letterCell.setOpaque(true);
        letterCell.setHorizontalAlignment(JLabel.CENTER);
        letterCell.setFont(new Font(BANNER_FNT,Font.BOLD,BANNER_FNT_SIZE));
        mendrugox[i] = letterCell;
        table.getModel().setValueAt(letterCell, BANNER_X,BANNER_Y+i);

    }
}

/* This method erases the banner */
private void clearMendrugoxBanner()
{
    table.setRowHeight(BANNER_X, BANNER_HEIGHT);
    for (int i = 0; i < BANNER.length(); i++)
    {
        TableModel model = table.getModel();
        JLabel letter = (JLabel) model.getValueAt(BANNER_X,BANNER_Y+i);
        letter.setText("");
        letter.setBackground(BG_COLOR);
        letter.setOpaque(true);
    }
}

private void seriousState()
{
    setSerious();
    clearMendrugoxBanner();
    smileyButton.setText(SMILE);
}

private void happyState()
{
    setHappy();
    drawMendrugoxBanner();
    smileyButton.setText(SERIOUS);
}

public Smiley()
{
    createContainer();
    createTable();
    addTableToContainer();
    seriousState(); /* The default state, you need to make it laugh */
}

public static void main(String args[])
{
    try
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new Smiley().setVisible(true);
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
        });
    }
    catch (Exception ex)
    {
        System.err.println(ex);
    }
}
}
