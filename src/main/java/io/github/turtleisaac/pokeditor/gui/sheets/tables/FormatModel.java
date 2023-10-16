package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public abstract class FormatModel<E> extends AbstractTableModel
{
    private final List<E> data;
    private final List<TextBankData> textBankData;

    private final String[] columnNames;

    public FormatModel(String[] columnNames, List<E> data, List<TextBankData> textBankData)
    {
        this.columnNames = columnNames;
        this.data = data;
        this.textBankData = textBankData;
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
        return columnNames.length;
    }

    public List<E> getData()
    {
        return data;
    }

    public List<TextBankData> getTextBankData()
    {
        return textBankData;
    }
}
