package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.DefaultSheetCellRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EvolutionsTable extends DefaultTable<EvolutionData, EvolutionsTable.EvolutionsColumn>
{
    public static final int[] columnWidths = new int[] {40, 100, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140};
    private static final String[] evolutionMethodKeys = new String[] {"evolutionMethod.none", "evolutionMethod.happiness", "evolutionMethod.happinessDay", "evolutionMethod.happinessNight", "evolutionMethod.reachLevel", "evolutionMethod.trade", "evolutionMethod.tradeItem", "evolutionMethod.useItem", "evolutionMethod.levelAttackGreaterThanDefense", "evolutionMethod.levelAttackEqualToDefense", "evolutionMethod.levelAttackLessThanDefense", "evolutionMethod.levelPIDLessThan5", "evolutionMethod.levelPIDGreaterThan5", "evolutionMethod.levelNincada1", "evolutionMethod.levelNincada2", "evolutionMethod.beauty", "evolutionMethod.useItemMale", "evolutionMethod.useItemFemale", "evolutionMethod.heldItemLevelUpDay", "evolutionMethod.heldItemLevelUpNight", "evolutionMethod.moveKnown", "evolutionMethod.speciesInParty", "evolutionMethod.levelMale", "evolutionMethod.levelFemale", "evolutionMethod.levelElectricField", "evolutionMethod.levelMossyRock", "evolutionMethod.levelIcyRock"};

    public EvolutionsTable(List<EvolutionData> data, List<TextBankData> textData)
    {
        super(new EvolutionsModel(data, textData), textData, columnWidths, new EvolutionRequirementCellSupplier());
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();

        String[] evoMethodKeys = DefaultTable.loadStringsFromKeys(evolutionMethodKeys);

        String[] speciesNames = textData.get(TextFiles.SPECIES_NAMES.getValue()).getStringList().toArray(String[]::new);
        String[] itemNames = textData.get(TextFiles.ITEM_NAMES.getValue()).getStringList().toArray(String[]::new);
        String[] moveNames = textData.get(TextFiles.MOVE_NAMES.getValue()).getStringList().toArray(String[]::new);

        textSources.add(evoMethodKeys);
        textSources.add(speciesNames);
        textSources.add(itemNames);
        textSources.add(moveNames);

        for (int i = 0; i < 6; i++)
        {
            textSources.add(evoMethodKeys);
        }

        return textSources;
    }

    @Override
    public Class<EvolutionData> getDataClass()
    {
        return EvolutionData.class;
    }

    static class EvolutionsModel extends FormatModel<EvolutionData, EvolutionsColumn>
    {
        public EvolutionsModel(List<EvolutionData> data, List<TextBankData> textBankData)
        {
            super(data, textBankData);
        }

        @Override
        public int getNumFrozenColumns()
        {
            return 2;
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return EvolutionsColumn.getColumn(columnIndex).key;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0)
            {
                EvolutionsColumn c = EvolutionsColumn.getColumn(columnIndex % EvolutionsColumn.NUMBER_OF_COLUMNS.idx);
                c.repetition = columnIndex;
                setValueFor(aValue, rowIndex, c);
            }
            else {
                setValueFor(aValue, rowIndex, EvolutionsColumn.getColumn(columnIndex));
            }
        }

        @Override
        public void setValueFor(Object aValue, int rowIndex, EvolutionsColumn property)
        {
            EvolutionData species = getData().get(rowIndex);

            aValue = prepareObjectForWriting(aValue, property.cellType);

            if (property.idx >= 0)
            {
                int entryIdx = property.repetition / EvolutionsColumn.NUMBER_OF_COLUMNS.idx;
                while (entryIdx > species.size())
                {
                    species.add(new EvolutionData.EvolutionEntry());
                }
                EvolutionData.EvolutionEntry entry = species.get(entryIdx);

                switch (property) {
                    case METHOD -> entry.setMethod((Integer) aValue);
                    case REQUIREMENT -> entry.setRequirement((Integer) aValue);
                    case RESULT_SPECIES -> entry.setResultSpecies((Integer) aValue);
                }
            }
        }

        @Override
        public int getColumnCount()
        {
            return EvolutionData.MAX_NUM_ENTRIES * EvolutionsColumn.NUMBER_OF_COLUMNS.idx;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0) {
                EvolutionsColumn c = EvolutionsColumn.getColumn(columnIndex % EvolutionsColumn.NUMBER_OF_COLUMNS.idx);
                c.repetition = columnIndex;
                return getValueFor(rowIndex, c);
            }
            return getValueFor(rowIndex, EvolutionsColumn.getColumn(columnIndex));
        }

        @Override
        public Object getValueFor(int rowIndex, EvolutionsColumn property)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            EvolutionData species = getData().get(rowIndex);

            if (property.idx >= 0)
            {
                int entryIdx = property.repetition / EvolutionsColumn.NUMBER_OF_COLUMNS.idx;
                while (entryIdx > species.size())
                {
                    species.add(new EvolutionData.EvolutionEntry());
                }
                EvolutionData.EvolutionEntry entry = species.get(entryIdx);

                switch (property) {
                    case METHOD -> {
                        return entry.getMethod();
                    }
                    case REQUIREMENT -> {
                        return entry.getRequirement();
                    }
                    case RESULT_SPECIES -> {
                        return entry.getResultSpecies();
                    }
                }
            }
            else if (property == EvolutionsColumn.ID)
            {
                return rowIndex;
            }
            else if (property == EvolutionsColumn.NAME)
            {
                if(rowIndex < speciesNames.size())
                    return speciesNames.get(rowIndex).getText();
                else
                    return "";
            }

            return null;
        }

        @Override
        protected CellTypes getCellType(int columnIndex)
        {
            if (columnIndex >= 0)
            {
                return EvolutionsColumn.getColumn(columnIndex % EvolutionsColumn.NUMBER_OF_COLUMNS.idx).cellType;
            }

            return EvolutionsColumn.getColumn(columnIndex).cellType;
        }

        @Override
        public FormatModel<EvolutionData, EvolutionsColumn> getFrozenColumnModel()
        {
            return new EvolutionsTable.EvolutionsModel(getData(), getTextBankData()) {
                @Override
                public int getColumnCount()
                {
                    return super.getNumFrozenColumns();
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    return super.getValueAt(rowIndex, columnIndex - super.getNumFrozenColumns());
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                {
                    super.setValueAt(aValue, rowIndex, columnIndex - super.getNumFrozenColumns());
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            };
        }
    }

    static class EvolutionRequirementCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        ComboBoxCellEditor speciesRequirementEditor;
        ComboBoxCellEditor itemRequirementEditor;
        ComboBoxCellEditor moveRequirementEditor;
        NumberOnlyCellEditor intValueRequirementEditor;

        private TableCellEditor current;

        public EvolutionRequirementCellEditor(String[] speciesNames, String[] itemNames, String[] moveNames)
        {
            speciesRequirementEditor = new ComboBoxCellEditor(speciesNames);
            itemRequirementEditor = new ComboBoxCellEditor(itemNames);
            moveRequirementEditor = new ComboBoxCellEditor(moveNames);
            intValueRequirementEditor = new NumberOnlyCellEditor();

            current = null;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            Object priorCol = table.getValueAt(row, column - 1);

            if (priorCol instanceof String)
            {
                priorCol = Integer.parseInt((String) priorCol);
            }

            if (column % EvolutionsColumn.NUMBER_OF_COLUMNS.idx == 1)
            {
                switch ((Integer) priorCol) {
                    case 6, 7, 16, 17, 18, 19 -> { // items
                        current = itemRequirementEditor;
                        return itemRequirementEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                    case 20 -> { // move known
                        current = moveRequirementEditor;
                        return moveRequirementEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                    case 21 -> { // species in party
                        current = speciesRequirementEditor;
                        return speciesRequirementEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                    case 4, 8, 9, 10, 11, 12, 13, 14, 22, 23 -> { // levels
                        current = intValueRequirementEditor;
                        return intValueRequirementEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                    }
                }
            }
            else if (column % EvolutionsColumn.NUMBER_OF_COLUMNS.idx == 2) {
                priorCol = table.getValueAt(row, column - 2);
                if (priorCol instanceof String)
                {
                    priorCol = Integer.parseInt((String) priorCol);
                }

                if ((Integer) priorCol != 0) {
                    current = speciesRequirementEditor;
                    return speciesRequirementEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
                }

            }

            return null;
        }

        @Override
        public Object getCellEditorValue()
        {
            return current.getCellEditorValue();
        }
    }

    static class EvolutionRequirementCellRenderer extends DefaultSheetCellRenderer
    {
        IndexedStringCellRenderer speciesRequirementRenderer;
        IndexedStringCellRenderer itemRequirementRenderer;
        IndexedStringCellRenderer moveRequirementRenderer;

        public EvolutionRequirementCellRenderer(String[] speciesNames, String[] itemNames, String[] moveNames)
        {
            speciesRequirementRenderer = new IndexedStringCellRenderer(speciesNames);
            itemRequirementRenderer = new IndexedStringCellRenderer(itemNames);
            moveRequirementRenderer = new IndexedStringCellRenderer(moveNames);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Object priorCol = table.getValueAt(row, column - 1);

            if (priorCol instanceof String)
            {
                priorCol = Integer.parseInt((String) priorCol);
            }

            TableCellRenderer defaultRenderer = table.getDefaultRenderer(Object.class);
            Component defaultRenderedCell = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (column % EvolutionsColumn.NUMBER_OF_COLUMNS.idx == 1)
            {
                switch ((Integer) priorCol) {
                    case 6, 7, 16, 17, 18, 19 -> { // items
                        return itemRequirementRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                    case 20 -> { // move known
                        return moveRequirementRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                    case 21 -> { // species in party
                        return speciesRequirementRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                    case 4, 8, 9, 10, 11, 12, 13, 14, 22, 23 -> { // levels
                        if (defaultRenderedCell instanceof DefaultTableCellRenderer c)
                        {
                            c.setEnabled(true);
                            return c;
                        }
                    }
                }
            }
            else if (column % EvolutionsColumn.NUMBER_OF_COLUMNS.idx == 2)
            {
                priorCol = table.getValueAt(row, column - 2);
                if (priorCol instanceof String)
                {
                    priorCol = Integer.parseInt((String) priorCol);
                }

                if ((Integer) priorCol != 0) {
                    return speciesRequirementRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            }

            if (defaultRenderedCell instanceof DefaultTableCellRenderer c)
            {
                c.setText("N/A");
                c.setEnabled(false);
                return c;
            }

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setEnabled(false);
            return this;
        }
    }

    static class EvolutionRequirementCellSupplier implements CellTypes.CustomCellFunctionSupplier
    {

        @Override
        public TableCellEditor getEditor(String[]... strings)
        {
            return new EvolutionRequirementCellEditor(strings[0], strings[1], strings[2]);
        }

        @Override
        public TableCellRenderer getRenderer(String[]... strings)
        {
            return new EvolutionRequirementCellRenderer(strings[0], strings[1], strings[2]);
        }
    }

    enum EvolutionsColumn {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        METHOD(0, "evolutionMethod", CellTypes.COMBO_BOX),
        REQUIREMENT(1, "evolutionRequirement", CellTypes.CUSTOM),
        RESULT_SPECIES(2, "evolutionResultSpecies", CellTypes.CUSTOM),
        NUMBER_OF_COLUMNS(3, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;
        int repetition;

        EvolutionsColumn(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static EvolutionsColumn getColumn(int idx)
        {
            for (EvolutionsColumn column : EvolutionsColumn.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }
}
