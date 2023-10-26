package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.CheckBoxRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.IndexedStringCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class EvolutionsTable extends DefaultTable<EvolutionData>
{
    private static final int NUM_FROZEN_COLUMNS = 2;
    public static final int[] columnWidths = new int[] {40, 100, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140, 200, 140, 140};
    private static final String[] evolutionMethodKeys = new String[] {"evolutionMethod.none", "evolutionMethod.happiness", "evolutionMethod.happinessDay", "evolutionMethod.happinessNight", "evolutionMethod.reachLevel", "evolutionMethod.trade", "evolutionMethod.tradeItem", "evolutionMethod.useItem", "evolutionMethod.levelAttackGreaterThanDefense", "evolutionMethod.levelAttackEqualToDefense", "evolutionMethod.levelAttackLessThanDefense", "evolutionMethod.levelPIDLessThan5", "evolutionMethod.levelPIDGreaterThan5", "evolutionMethod.levelNincada1", "evolutionMethod.levelNincada2", "evolutionMethod.beauty", "evolutionMethod.useItemMale", "evolutionMethod.useItemFemale", "evolutionMethod.heldItemLevelUpDay", "evolutionMethod.heldItemLevelUpNight", "evolutionMethod.moveKnown", "evolutionMethod.speciesInParty", "evolutionMethod.levelMale", "evolutionMethod.levelFemale", "evolutionMethod.levelElectricField", "evolutionMethod.levelMossyRock", "evolutionMethod.levelIcyRock"};

    public EvolutionsTable(List<EvolutionData> data, List<TextBankData> textData)
    {
        super(EvolutionsTable.EvolutionsModel.evolutionsClasses, new EvolutionsTable.EvolutionsModel(data, textData), textData, columnWidths, new EvolutionRequirementCellSupplier());
    }

    @Override
    Queue<String[]> obtainTextSources(List<TextBankData> textData)
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

    @Override
    public int getNumFrozenColumns()
    {
        return NUM_FROZEN_COLUMNS;
    }

    static class EvolutionsModel extends FormatModel<EvolutionData>
    {
        static final CellTypes[] evolutionsClasses = new CellTypes[] {CellTypes.INTEGER, CellTypes.STRING, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, CellTypes.COMBO_BOX, CellTypes.CUSTOM, CellTypes.CUSTOM, };
        private static final String[] columnKeys = new String[] {"id", "name", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies", "evolutionMethod", "evolutionRequirement", "evolutionResultSpecies"};

        public EvolutionsModel(List<EvolutionData> data, List<TextBankData> textBankData)
        {
            super(columnKeys, data, textBankData, NUM_FROZEN_COLUMNS);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            EvolutionData species = getData().get(rowIndex);

            if (aValue instanceof String)
            {
                if (columnIndex != -1)
                    aValue = Integer.parseInt(((String) aValue).trim());
            }

            if (columnIndex >= 0)
            {
                int entryIdx = columnIndex / 3;
                while (entryIdx > species.size())
                {
                    species.add(new EvolutionData.EvolutionEntry());
                }
                EvolutionData.EvolutionEntry entry = species.get(entryIdx);

                switch (columnIndex % 3) {
                    case 0 -> entry.setMethod((Integer) aValue);
                    case 1 -> entry.setRequirement((Integer) aValue);
                    case 2 -> entry.setResultSpecies((Integer) aValue);
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            EvolutionData species = getData().get(rowIndex);

            if (columnIndex >= 0)
            {
                int entryIdx = columnIndex / 3;
                while (entryIdx > species.size())
                {
                    species.add(new EvolutionData.EvolutionEntry());
                }
                EvolutionData.EvolutionEntry entry = species.get(entryIdx);

                switch (columnIndex % 3) {
                    case 0 -> {
                        return entry.getMethod();
                    }
                    case 1 -> {
                        return entry.getRequirement();
                    }
                    case 2 -> {
                        return entry.getResultSpecies();
                    }
                }
            }
            else if (columnIndex == -2)
            {
                return rowIndex;
            }
            else if (columnIndex == -1)
            {
                if(rowIndex < speciesNames.size())
                    return speciesNames.get(rowIndex).getText();
                else
                    return "";
            }

            return null;
        }

        @Override
        public FormatModel<EvolutionData> getFrozenColumnModel()
        {
            return new EvolutionsTable.EvolutionsModel(getData(), getTextBankData()) {
                @Override
                public int getColumnCount()
                {
                    return NUM_FROZEN_COLUMNS;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    return super.getValueAt(rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                {
                    super.setValueAt(aValue, rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
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

            if (column % 3 == 1)
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
            else if (column % 3 == 2) {
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

    static class EvolutionRequirementCellRenderer extends DefaultTableCellRenderer
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

            if (defaultRenderedCell instanceof DefaultTableCellRenderer c)
            {
                c.setText(String.valueOf(value));
                c.setForeground(Color.black);
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else if (table.getSelectedRow() == row) {
                    c.setBackground(table.getSelectionBackground());
                }
                else {
                    if (row % 2 == 0)
                        c.setBackground(table.getBackground());
                    else
                        c.setBackground(new Color(248, 221, 231));
                }
                c.validate();
                c.repaint();
            }

            if (column % 3 == 1)
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
            else if (column % 3 == 2)
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
}
