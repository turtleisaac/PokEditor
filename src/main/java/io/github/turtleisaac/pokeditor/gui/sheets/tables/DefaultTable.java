package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public abstract class DefaultTable<E extends GenericFileData> extends JTable
{
    private final CellTypes[] cellTypes;
    private final int[] widths;
    private final List<TextBankData> textData;

    public DefaultTable(CellTypes[] cellTypes, FormatModel<E> model, List<TextBankData> textData, int[] widths)
    {
        super(model);

        cellTypes = Arrays.copyOfRange(cellTypes, getNumFrozenColumns(), cellTypes.length);
        widths = Arrays.copyOfRange(widths, getNumFrozenColumns(), widths.length);

        this.cellTypes = cellTypes;
        this.widths = widths;
        this.textData = textData;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setRowMargin(1);
        getColumnModel().setColumnMargin(1);
        setShowGrid(true);
        setGridColor(Color.black);
        setShowHorizontalLines(true);
        setShowVerticalLines(true);

//        setBackground(Color.WHITE);
//        setForeground(Color.black);

        loadCellRenderers(obtainTextSources(textData));

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

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                System.out.println("moo");
//                editingCanceled(null);
//            }
//        });

                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

//        addKeyListener(new KeyAdapter()
//        {
//            @Override
//            public void keyPressed(KeyEvent e)
//            {
////                super.keyPressed(e);
//                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
//                    clearSelection();
//            }
//        });
    }

    abstract Queue<String[]> obtainTextSources(List<TextBankData> textData);

    public abstract Class<E> getDataClass();

    public abstract int getNumFrozenColumns();

    public void loadCellRenderers(Queue<String[]> textSources)
    {
        for (int i = 0; i < getColumnCount(); i++)
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

    public void resetIndexedCellRendererText()
    {
        Queue<String[]> textSources = obtainTextSources(textData);
        for (int i = 0; i < cellTypes.length; i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);

            if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX)
            {
                String[] text = textSources.remove();
                if (text == null)
                    text = new String[] {""};
                ((ComboBoxCellEditor) col.getCellEditor()).setItems(text);
                ((IndexedStringCellRenderer) col.getCellRenderer()).setItems(text);
            }
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

    public static String[] loadStringsFromKeys(String... keys)
    {
        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);
        String[] result = new String[keys.length];
        int idx = 0;
        for (String s : keys) {
            result[idx++] = bundle.getString(s);
        }
        return result;
    }
}
