package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.MultiLineTableHeaderRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
        setBackground(Color.GREEN);

        MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration<?> enumK = getColumnModel().getColumns();
        while (enumK.hasMoreElements())
        {
            ((TableColumn) enumK.nextElement()).setHeaderRenderer(renderer);
        }


        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        setDragEnabled(false);

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
//                    c.setForeground(getSelectionForeground());
                    c.setBackground(getSelectionBackground());
                } else {
                    c.setForeground(getForeground());
                    if (row % 2 == 0)
                        setBackground(table.getBackground());
                    else
                        setBackground(new Color(248, 221, 231));
                }

                return c;
            }
        });

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    }
}
