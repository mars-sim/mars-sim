package com.vdurmont.emoji;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import javafx.scene.image.Image;

public class IconsInTableCell extends JPanel {

    private static final String[] COLUMN_NAMES = { "Icons" };
    private MyTableModel tableModel;
    private JTable table;
    private JFrame frame = new JFrame();

    public IconsInTableCell() {
        super(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tableModel = new MyTableModel();
        table = new JTable(tableModel);
        table.setDefaultEditor(Icon.class, new IconEditor());
        table.setDefaultRenderer(Icon.class, new IconRenderer());
        table.setRowHeight(60);
        add(new JScrollPane(table), BorderLayout.CENTER);
        tableModel.add(new TableEntry());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.pack();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new IconsInTableCell();
            }
        });
    }

    private enum Icon {
        Delete, Sort;
    }

    private class IconPanel extends JPanel implements MouseListener {

        private JLabel icon1;
        private JLabel icon2, icon3;

        IconPanel() {
            super(new GridLayout(0, 1));
            setOpaque(true);

            //icon1 = new JLabel(new ImageIcon(ImageIO.read(new File("../../icons/laugh16.jpg"))));
			//icon2 = new JLabel(new ImageIcon(ImageIO.read(new File("../../icons/cry16.jpg"))));
			//icon3 = new JLabel(new ImageIcon(ImageIO.read(new File("../../icons/neutral16.jpg"))));
			icon1 = new JLabel(new ImageIcon(this.getClass().getResource("/icons/laugh16.png")));
			icon2 = new JLabel(new ImageIcon(this.getClass().getResource("/icons/cry16.png")));
			icon3 = new JLabel(new ImageIcon(this.getClass().getResource("/icons/neutral16.png")));

            icon1.addMouseListener(this);
            icon2.addMouseListener(this);
            icon3.addMouseListener(this);
            
            this.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            add(icon1, c);
            //add(new JLabel("TEXT"), c);
            add(icon2, c);
            add(icon3, c);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getSource() == icon1)
                System.out.println("icon1");
            else if (e.getSource() == icon2)
                System.out.println("icon2");
            else if (e.getSource() == icon3)
                System.out.println("icon3");
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    private class TableEntry {

        private Icon theIcons;

        TableEntry() {
        }

        TableEntry(Icon aIcons) {
            theIcons = aIcons;
        }

        public Icon getIcons() {
            return theIcons;
        }

        public void setIcons(Icon aIcon) {
            theIcons = aIcon;
        }
    }

    private class MyTableModel extends AbstractTableModel {

        private Vector<Object> theEntries;

        MyTableModel() {
            theEntries = new Vector<Object>();
        }

        @SuppressWarnings("unchecked")
        public void add(TableEntry anEntry) {
            int index = theEntries.size();
            theEntries.add(anEntry);
            fireTableRowsInserted(index, index);
        }

        public void remove(int aRowIndex) {
            if (aRowIndex < 0 || aRowIndex >= theEntries.size()) {
                return;
            }
            theEntries.removeElementAt(aRowIndex);
            fireTableRowsDeleted(aRowIndex, aRowIndex);
        }

        public int getRowCount() {
            return theEntries.size();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Icon.class;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            TableEntry entry = (TableEntry) theEntries.elementAt(rowIndex);
            entry.setIcons((Icon) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TableEntry entry = (TableEntry) theEntries.elementAt(rowIndex);
            switch (columnIndex) {
            case 0:
                return entry.getIcons();
            }
            return null;
        }
    }

    private class IconEditor extends AbstractCellEditor implements TableCellEditor {

        private IconPanel theIconPanel;

        IconEditor() {
            theIconPanel = new IconPanel();
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (isSelected) {
                theIconPanel.setBackground(table.getSelectionBackground());
            } else {
                theIconPanel.setBackground(table.getBackground());
            }
            return theIconPanel;
        }
    }

    private class IconRenderer extends IconPanel implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
}