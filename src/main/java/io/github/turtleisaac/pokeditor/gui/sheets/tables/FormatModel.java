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
    private final String[] columnNames;

    private boolean copyPasteModeEnabled;

    public FormatModel(List<E> data, List<TextBankData> textBankData)
    {
        this.data = data;
        this.textBankData = textBankData;
        this.columnNames = new String[getColumnCount() + getNumFrozenColumns()];

        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);

        int lastValid = 0;
        for (int idx = 0; idx < columnNames.length; idx++)
        {
            int adjusted = idx-getNumFrozenColumns();
            String columnNameKey = getColumnNameKey(adjusted);
            if (columnNameKey == null)
            {
                columnNames[idx] = bundle.getString(getColumnNameKey(adjusted % (lastValid + 1)));
            }
            else {
                columnNames[idx] = bundle.getString(columnNameKey);
                lastValid = adjusted;
            }

        }

        copyPasteModeEnabled = false;
    }

    public abstract int getNumFrozenColumns();

    public abstract String getColumnNameKey(int columnIndex);

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column + getNumFrozenColumns()];
    }

    public void toggleCopyPasteMode(boolean state)
    {
        copyPasteModeEnabled = state;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return !copyPasteModeEnabled;
    }

    @Override
    public int getRowCount()
    {
        return data.size();
    }

    public List<E> getData()
    {
        return data;
    }

    public List<TextBankData> getTextBankData()
    {
        return textBankData;
    }

    protected CellTypes getCellType(int columnIndex)
    {
        return CellTypes.STRING;
    }

    public abstract FormatModel<E> getFrozenColumnModel();

    public Object prepareObjectForWriting(Object aValue, int columnIndex)
    {
        if (aValue instanceof String)
        {
            CellTypes cellType = getCellType(columnIndex);
            if (cellType == CellTypes.CHECKBOX)
                aValue = Boolean.parseBoolean(((String) aValue).trim());
            else if (cellType != CellTypes.STRING)
                aValue = Integer.parseInt(((String) aValue).trim());
        }
        return aValue;
    }
}
