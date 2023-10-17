package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public abstract class FormatModel<E extends GenericFileData> extends AbstractTableModel
{
    private final List<E> data;
    private final List<TextBankData> textBankData;
    private final int numFrozenColumns;

    private final String[] columnNames;

    public FormatModel(String[] columnNameKeys, List<E> data, List<TextBankData> textBankData, int numFrozenColumns)
    {
        this.data = data;
        this.numFrozenColumns = numFrozenColumns;
        this.textBankData = textBankData;
        this.columnNames = new String[columnNameKeys.length];

        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);

        int idx = 0;
        for (String key : Arrays.copyOfRange(columnNameKeys, numFrozenColumns, columnNameKeys.length))
            columnNames[idx++] = bundle.getString(key);
    }

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnIndex != 0;
    }

    @Override
    public int getRowCount()
    {
        return data.size();
    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length - numFrozenColumns;
    }

    public List<E> getData()
    {
        return data;
    }

    public List<TextBankData> getTextBankData()
    {
        return textBankData;
    }

    public abstract FormatModel<E> getFrozenColumnModel();
}
