package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.EditorComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class DefaultTable extends JTable
{
    private final Class<?>[] classes;
    private final String[] names;

    public DefaultTable(Class<?>[] classes, String[] names, int[] widths, Object[][] data)
    {
        super();

        this.classes = classes;
        this.names = names;
        DefaultTableModel model= (DefaultTableModel) getModel();
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setShowGrid(true);
        setGridColor(Color.black);
//        setBackground(Color.WHITE);
//        setForeground(Color.black);

        for(int col= 0; col < data[0].length; col++)
        {
            ArrayList<Object> column = new ArrayList<>();
            for(Object[] datum : data)
            {
                column.add(datum[col]);
            }
            model.addColumn(names[col], column.toArray());
        }

        for(int i= 0; i < classes.length; i++)
        {
            Class<?> c = classes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            if(c == JCheckBox.class)
            {
                col.setCellRenderer(new CheckBoxRenderer());
                col.setCellRenderer(new CheckBoxRenderer());
            }
            else if(c == JComboBox.class)
            {
                col.setCellEditor(new EditorComboBoxEditor(new String[] {"Moo1", "Moo2", "Moo3"}));
                col.setCellRenderer(new EditorComboBoxRenderer(new String[] {"Moo1", "Moo2", "Moo3"}));
            }
            else if(c == JSpinner.class)
            {
                col.setCellRenderer(new SpinnerRenderer());
            }
            else if(c == JButton.class)
            {
                col.setCellRenderer(new ButtonRenderer());
            }
        }

        for(int row= 0; row < data.length; row++)
        {
            for(int col= 0; col < data[row].length; col++)
            {
                model.setValueAt(data[row][col], row, col);
            }
        }

        MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration<?> enumK = getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
        }


        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setDragEnabled(false);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected)
                    c.setForeground(Color.black);
                return c;
            }
        });

        setRowSelectionAllowed(false);
    }
}
