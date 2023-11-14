package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.DefaultSheetCellRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.MultiLineTableHeaderRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Enumeration;

public class FrozenColumnTable<E extends GenericFileData> extends JTable
{
    public FrozenColumnTable(TableModel model)
    {
        super(model);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setRowMargin(1);
        getColumnModel().setColumnMargin(1);
        setShowGrid(true);
        setGridColor(Color.black);
        setShowHorizontalLines(true);
        setShowVerticalLines(true);

        TableColumn col = getColumnModel().getColumn(0);
        col.setWidth(35);
        col.setPreferredWidth(35);


//        for (int i = 0; i < getColumnCount(); i++)
//        {
//            CellTypes c = cellTypes[i];
//            TableColumn col = getColumnModel().getColumn(i);
//            col.setWidth(widths[i]);
//            col.setPreferredWidth(widths[i]);
//        }

        MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration<?> enumK = getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
        }

//        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        getTableHeader().setReorderingAllowed(false);
        setDragEnabled(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setDefaultRenderer(Object.class, new DefaultSheetCellRenderer());
    }

    public JTable getCornerTableHeader()
    {
        TableModel model = new DefaultTableModel() {
            @Override
            public int getColumnCount()
            {
                return getColumnModel().getColumnCount();
            }

            @Override
            public int getRowCount()
            {
                return 1;
            }

            @Override
            public Object getValueAt(int row, int column)
            {
                return getModel().getColumnName(column - getColumnModel().getColumnCount());
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                return false;
            }
        };

        JTable corner = new JTable(model);
        corner.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        corner.setRowMargin(1);
        corner.getColumnModel().setColumnMargin(1);
        corner.setShowGrid(true);
        corner.setShowHorizontalLines(true);
        corner.setShowVerticalLines(true);
        corner.setRowSelectionAllowed(false);
        corner.getColumnModel().setColumnSelectionAllowed(false);
        corner.getTableHeader().setReorderingAllowed(false);
        corner.setDragEnabled(false);

        corner.setRowHeight(39);

        MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration<?> enumK = corner.getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            TableColumn c = ((TableColumn) enumK.nextElement());
            c.setCellRenderer(renderer);
        }
        corner.getColumnModel().getColumn(0).setWidth(21);
        corner.getColumnModel().getColumn(0).setPreferredWidth(21);

        return corner;
    }
}
