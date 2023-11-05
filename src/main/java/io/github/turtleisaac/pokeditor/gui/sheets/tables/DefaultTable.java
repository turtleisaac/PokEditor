package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.formdev.flatlaf.util.SystemInfo;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.BitfieldComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public abstract class DefaultTable<E extends GenericFileData> extends JTable
{
    private final CellTypes[] cellTypes;
    private final int[] widths;
    private final List<TextBankData> textData;

    private final FormatModel<E> formatModel;

    private final CellTypes.CustomCellFunctionSupplier customCellSupplier;

    public DefaultTable(CellTypes[] cellTypes, FormatModel<E> model, List<TextBankData> textData, int[] widths, CellTypes.CustomCellFunctionSupplier customCellSupplier)
    {
        super(model);
        this.formatModel = model;

        cellTypes = Arrays.copyOfRange(cellTypes, getNumFrozenColumns(), cellTypes.length);
        widths = Arrays.copyOfRange(widths, getNumFrozenColumns(), widths.length);

        this.cellTypes = cellTypes;
        this.widths = widths;
        this.textData = textData;
        this.customCellSupplier = customCellSupplier;
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

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        PasteAction action = new PasteAction(this);

        KeyStroke stroke;
        if (!SystemInfo.isMacOS) {
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        } else {
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.META_MASK, false);
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0), action);
        registerKeyboardAction(action, "Paste", stroke, JComponent.WHEN_FOCUSED);
//        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                System.out.println("moo");
//                editingCanceled(null);
//            }
//        });

//                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

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

    public abstract Queue<String[]> obtainTextSources(List<TextBankData> textData);

    public FormatModel<E> getFormatModel()
    {
        return formatModel;
    }

    public abstract Class<E> getDataClass();

    public abstract int getNumFrozenColumns();

    public void loadCellRenderers(Queue<String[]> textSources)
    {
        TableCellEditor customEditor = null;
        TableCellRenderer customRenderer = null;

        for (int i = 0; i < getColumnCount(); i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            if (c == CellTypes.CHECKBOX)
            {
                col.setCellRenderer(new CheckBoxRenderer());
                col.setCellEditor(new CheckBoxEditor());
            }
            else if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX || c == CellTypes.BITFIELD_COMBO_BOX)
            {
                String[] text = getTextFromSource(textSources);

                if (c != CellTypes.BITFIELD_COMBO_BOX) //normal and colored
                    col.setCellEditor(new ComboBoxCellEditor(text));
                else // bitfield combo box
                    col.setCellEditor(new BitfieldComboBoxEditor(text));

                if (c == CellTypes.COMBO_BOX)
                    col.setCellRenderer(new IndexedStringCellRenderer(text));
                else if (c == CellTypes.COLORED_COMBO_BOX)
                    col.setCellRenderer(new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(text, PokeditorManager.typeColors));
                else
                    col.setCellRenderer(new BitfieldStringCellRenderer(text));
            }
            else if (c == CellTypes.INTEGER)
            {
                col.setCellEditor(new NumberOnlyCellEditor());
            }
            else if (c == CellTypes.CUSTOM)
            {
                if (customEditor == null || customRenderer == null)
                {
                    String[] speciesNames = getTextFromSource(textSources);
                    String[] itemNames = getTextFromSource(textSources);
                    String[] moveNames = getTextFromSource(textSources);

                    customEditor = customCellSupplier.getEditor(speciesNames, itemNames, moveNames);
                    customRenderer = customCellSupplier.getRenderer(speciesNames, itemNames, moveNames);
                }

                if (customEditor != null)
                    col.setCellEditor(customEditor);

                if (customRenderer != null)
                    col.setCellRenderer(customRenderer);
            }
        }
    }

    private String[] getTextFromSource(Queue<String[]> textSources)
    {
        String[] text = textSources.remove();
        if (text == null)
            text = new String[] {""};

        return text;
    }

    public void resetIndexedCellRendererText()
    {
        Queue<String[]> textSources = obtainTextSources(textData);
        for (int i = 0; i < cellTypes.length; i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);

            if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX || c == CellTypes.BITFIELD_COMBO_BOX)
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

    public static class PasteAction extends AbstractAction {

        private final DefaultTable<? extends GenericFileData> table;

        public PasteAction(DefaultTable<? extends GenericFileData> table)
        {
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int[] rows = table.getSelectedRows();
            System.out.println("rows: " + Arrays.toString(rows));
            int[] cols = table.getSelectedColumns();

            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor))
            {
                try
                {
                    String value = (String) cb.getData(DataFlavor.stringFlavor);
                    String[] lines = value.split("\n");
                    String[][] pastedCells = new String[lines.length][];
                    int idx = 0;
                    for (String line : lines) {
                        pastedCells[idx++] = line.split("\t");
                    }

                    int numVerticalCopies = (int) Math.round(((double) rows.length) / pastedCells.length);
                    int numHorizontalCopies = (int) Math.round(((double) cols.length) / pastedCells[0].length);

                    for (int verticalCopyIdx = 0; verticalCopyIdx < numVerticalCopies; verticalCopyIdx++)
                    {
                        for (int horizontalCopyIdx = 0; horizontalCopyIdx < numHorizontalCopies; horizontalCopyIdx++)
                        {
                            for (int rowIdx = 0; rowIdx < pastedCells.length; rowIdx++)
                            {
                                for (int colIdx = 0; colIdx < pastedCells[0].length; colIdx++)
                                {
                                    int destRow = rows[0] + verticalCopyIdx * pastedCells.length + rowIdx;
                                    int destCol = cols[0] + horizontalCopyIdx * pastedCells[0].length + colIdx;

//                                    System.out.println("Setting value at (" + destRow + "," + destCol + ") to: " + pastedCells[rowIdx][colIdx]);
                                    table.setValueAt(pastedCells[rowIdx][colIdx], destRow, destCol);
                                    table.getFormatModel().fireTableCellUpdated(destRow, destCol);
                                }
                            }
                        }
                    }

//                    for (int userSelectedRow : rows)
//                    {
//                        for (int rowIdx = 0; rowIdx < pastedCells.length; rowIdx++)
//                        {
//                            for (int colIdx = 0; colIdx < pastedCells[0].length; colIdx++)
//                            {
//                                table.setValueAt(pastedCells[rowIdx][colIdx], userSelectedRow + rowIdx, cols[0] + colIdx);
//                                table.getFormatModel().fireTableCellUpdated(userSelectedRow + rowIdx, cols[0] + colIdx);
//                            }
//                        }
//                    }

//                    table.setValueAt(value, row, col);
                }
                catch (UnsupportedFlavorException | IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
}
