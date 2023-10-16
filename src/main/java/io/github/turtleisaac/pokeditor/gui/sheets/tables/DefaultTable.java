package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.EditorComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.SpinnerCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class DefaultTable<E extends GenericFileData> extends JTable
{
    private final Class<?>[] classes;
    private final List<TextBankData> textData;
    private final int[] widths;

    public DefaultTable(Class<?>[] classes, int[] widths, List<E> data, List<TextBankData> textData, TextFiles[] textSources, TableModel model)
    {
        super(model);

        this.classes = classes;
        this.textData = textData;
        this.widths = widths;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setShowGrid(true);
//        setGridColor(Color.black);

//        setBackground(Color.WHITE);
//        setForeground(Color.black);

        Queue<TextFiles> textSourceQueue = new LinkedList<>(Arrays.asList(textSources));

        loadCellRenderers(textSourceQueue);

        moo();
    }

    public void loadCellRenderers(Queue<TextFiles> textSources)
    {
        for (int i = 0; i < classes.length; i++)
        {
            Class<?> c = classes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            if (c == JCheckBox.class)
            {
                col.setCellRenderer(new CheckBoxRenderer());
//                col.setCellRenderer(new CheckBoxRenderer());
            }
            else if (c == JComboBox.class)
            {
                TextFiles bank = textSources.remove();
                String[] text;
                if (bank != null)
                    text = textData.get(bank.getValue()).getStringList().toArray(String[]::new);
                else
                    text = new String[] {""};
                col.setCellEditor(new EditorComboBoxEditor(text));
                col.setCellRenderer(new EditorComboBoxRenderer(text, PokeditorManager.typeColors));
            }
            else if (c == JSpinner.class)
            {
                col.setCellEditor(new SpinnerCellEditor());
            }
            else if (c == JButton.class)
            {
                col.setCellRenderer(new ButtonRenderer());
            }
        }
    }

    private void moo()
    {
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
