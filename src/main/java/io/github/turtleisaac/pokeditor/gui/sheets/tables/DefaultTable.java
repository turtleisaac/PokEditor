package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class DefaultTable<E extends GenericFileData> extends JTable
{
    private final CellTypes[] cellTypes;
    private final int[] widths;

    public DefaultTable(CellTypes[] cellTypes, TableModel model, List<TextBankData> textData, int[] widths)
    {
        super(model);

        this.cellTypes = cellTypes;
        this.widths = widths;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setShowGrid(true);
//        setGridColor(Color.black);

//        setBackground(Color.WHITE);
//        setForeground(Color.black);

        loadCellRenderers(obtainTextSources(textData));

        moo();
    }

    abstract Queue<String[]> obtainTextSources(List<TextBankData> textData);

    public void loadCellRenderers(Queue<String[]> textSources)
    {
        for (int i = 0; i < cellTypes.length; i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            if (c == CellTypes.CHECKBOX)
            {
                col.setCellRenderer(new CheckBoxRenderer());
            }
            else if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX)
            {
                String[] text = textSources.remove();
                if (text == null)
                    text = new String[] {""};
                col.setCellEditor(new ComboBoxCellEditor(text));

                if (c == CellTypes.COMBO_BOX)
                    col.setCellRenderer(new IndexedStringCellRenderer(text));
                else
                    col.setCellRenderer(new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(text, PokeditorManager.typeColors));
            }
            else if (c == CellTypes.INTEGER)
            {
                col.setCellEditor(new NumberOnlyCellEditor());
            }
//            else if (c == JButton.class)
//            {
//                col.setCellRenderer(new ButtonRenderer());
//            }
        }
    }

    public String[][] exportClean()
    {
        String[][] output = new String[getModel().getRowCount()][getColumnCount()];
        for (int colIdx = 0; colIdx < output[0].length; colIdx++)
        {
            TableColumn column = getColumnModel().getColumn(colIdx);
            TableCellRenderer renderer = column.getCellRenderer();
            if (renderer != null && !(renderer instanceof CheckBoxRenderer))
            {
                for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
                {
                    output[rowIdx][colIdx] = ((DefaultTableCellRenderer) prepareRenderer(renderer, rowIdx, colIdx)).getText();
                }
            }
            else
            {
                for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
                {
                    output[rowIdx][colIdx] = String.valueOf(getValueAt(rowIdx, colIdx));
                }
            }

        }

        return output;
    }

    public String[][] exportEditable()
    {
        String[][] output = new String[getModel().getRowCount()][getColumnCount()];
        for (int colIdx = 0; colIdx < output[0].length; colIdx++)
        {
            for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
            {
                output[rowIdx][colIdx] = String.valueOf(getValueAt(rowIdx, colIdx));
            }
        }

        return output;
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

    public static String[] loadStringsFromKeys(String... keys)
    {
        ResourceBundle bundle = ResourceBundle.getBundle("sheet_strings");
        String[] result = new String[keys.length];
        int idx = 0;
        for (String s : keys) {
            result[idx++] = bundle.getString(s);
        }
        return result;
    }
}
