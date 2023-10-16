package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.Sheet;
//import io.github.turtleisaac.pokeditor.gui.sheets.SheetEditorPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.editors.EditorComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.ButtonRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.CheckBoxRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.EditorComboBoxRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.SpinnerRenderer;
import io.github.turtleisaac.pokeditor.gui_old.EditorComboBox;
import io.github.turtleisaac.pokeditor.sheets.JohtoSheets;
import io.github.turtleisaac.pokeditor.sheets.SinnohSheets;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.*;
import java.util.List;

public class PersonalTable extends DefaultTable<PersonalData>
{

    public static final int[] columnWidths = new int[] {40, 100, 65, 65, 65, 65, 65, 65, 100, 100, 65, 65, 65, 65, 65, 65, 65, 65, 140, 140, 65, 65, 70, 120, 120, 120, 140, 140, 65, 65, 65};
    public static final TextFiles[] textSources = new TextFiles[] {TextFiles.TYPE_NAMES, TextFiles.TYPE_NAMES, TextFiles.ITEM_NAMES, TextFiles.ITEM_NAMES, null, null, null, TextFiles.ABILITY_NAMES, TextFiles.ABILITY_NAMES};

    public PersonalTable(List<PersonalData> data, List<TextBankData> textData)
    {
        super(PersonalModel.personalClasses, columnWidths, data, textData, textSources, new PersonalModel(data, textData));
    }

//    public static void main(String[] args)
//    {
//        FlatDarculaLaf.install();
////        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
////        if (defaults.get("Table.alternateRowColor") == null)
////            defaults.put("Table.alternateRowColor", new Color(208, 108, 108));
//
//        Object[][] data = new Object[500][];
//        for(int i= 0; i < data.length; i++)
//        {
//            data[i] = generateTestRow();
//        }
//
//        Sheet s = new Sheet()
//        {
//            @Override
//            public Object[][] getData()
//            {
//                return data;
//            }
//
//            @Override
//            public SinnohSheets getSheetType()
//            {
//                return SinnohSheets.Personal;
//            }
//
//            @Override
//            public JohtoSheets getAltSheetType()
//            {
//                return null;
//            }
//        };
//
////        SheetEditorPanel frame = new SheetEditorPanel(s);
////        frame.setPreferredSize(new Dimension(1500, 800));
//    }


//    private static Object[] generateTestRow()
//    {
//
//        ArrayList<Object> row = new ArrayList<>();
//        for(Class<?> c : personalClasses)
//        {
//            if(c == JCheckBox.class)
//            {
//                row.add(true);
//            }
//            else if(c == JComboBox.class)
//            {
//                row.add("Moo1");
//            }
//            else if(c == JSpinner.class)
//            {
//                row.add(1);
//            }
//            else if(c == JButton.class)
//            {
//                row.add(true);
//            }
//            else if(c == String.class)
//            {
//                row.add("species");
//            }
//            else if(c == Integer.class)
//            {
//                row.add(0);
//            }
//        }
//
//        return row.toArray(new Object[] {});
//    }

    @Override
    public void setValues(List<PersonalData> data, List<TextBankData> textData)
    {
        DefaultTableModel model = (DefaultTableModel) getModel();
        TextBankData speciesNames = textData.get(TextFiles.SPECIES_NAMES.getValue());

        for (int row = 0; row < data.size(); row++)
        {
            PersonalData entry = data.get(row);
            int col = 0;
            model.setValueAt(row, row, col++);
            String speciesName;
            if (row < speciesNames.size())
                speciesName = speciesNames.get(row).getText();
            else
                speciesName = "";
            model.setValueAt(speciesName, row, col++);
            model.setValueAt(entry.getHp(), row, col++);
            model.setValueAt(entry.getAtk(), row, col++);
            model.setValueAt(entry.getDef(), row, col++);
            model.setValueAt(entry.getSpeed(), row, col++);
            model.setValueAt(entry.getSpAtk(), row, col++);
            model.setValueAt(entry.getSpDef(), row, col++);
            model.setValueAt(entry.getType1(), row, col++);
            model.setValueAt(entry.getType2(), row, col++);
            model.setValueAt(entry.getCatchRate(), row, col++);
            model.setValueAt(entry.getBaseExp(), row, col++);
            model.setValueAt(entry.getHpEvYield(), row, col++);
            model.setValueAt(entry.getAtkEvYield(), row, col++);
            model.setValueAt(entry.getDefEvYield(), row, col++);
            model.setValueAt(entry.getSpeedEvYield(), row, col++);
            model.setValueAt(entry.getSpAtkEvYield(), row, col++);
            model.setValueAt(entry.getSpDefEvYield(), row, col++);
            model.setValueAt(entry.getUncommonItem(), row, col++);
            model.setValueAt(entry.getRareItem(), row, col++);
            model.setValueAt(entry.getGenderRatio(), row, col++);
            model.setValueAt(entry.getHatchMultiplier(), row, col++);
            model.setValueAt(entry.getBaseHappiness(), row, col++);
            col++;
            col++;
            col++;
            model.setValueAt(entry.getAbility1(), row, col++);
            model.setValueAt(entry.getAbility2(), row, col++);
            model.setValueAt(entry.getRunChance(), row, col++);
            model.setValueAt(entry.getDexColor(), row, col++);
            model.setValueAt(entry.isFlip(), row, col);
        }
    }

    static class PersonalModel extends AbstractTableModel
    {
        static final Class<?>[] personalClasses = new Class<?>[] {Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class, JCheckBox.class};
        static final String[] personalNames = new String[] {"ID", "Name", "HP", "Atk", "Def", "Speed", "Sp. Atk", "Sp. Def", "Type 1", "Type 2", "Catch Rate", "Exp Drop", "HP EV", "Atk EV", "Def EV", "Speed EV", "Sp. Atk EV", "Sp. Def EV", "Uncommon Held Item", "Rare Held Item", "Gender Ratio", "Hatch Mult.", "Base Happiness", "Growth Rate", "Egg Group 1", "Egg Group 2", "Ability 1", "Ability 2", "Run Chance", "Color", "Flip"};

        private final List<PersonalData> data;
        private final List<TextBankData> textBankData;

        public PersonalModel(List<PersonalData> data, List<TextBankData> textBankData)
        {
            this.data = data;
            this.textBankData = textBankData;
        }

        @Override
        public String getColumnName(int column)
        {
            return personalNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            if (columnIndex == 0)
                return false;
            return super.isCellEditable(rowIndex, columnIndex);
        }

        @Override
        public int getRowCount()
        {
            return data.size();
        }

        @Override
        public int getColumnCount()
        {
            return personalNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData speciesNames = textBankData.get(TextFiles.SPECIES_NAMES.getValue());
            PersonalData entry = data.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return rowIndex;
                case 1:
                    if (rowIndex < speciesNames.size())
                        return speciesNames.get(rowIndex).getText();
                    else
                        return "";
                case 2:
                    return entry.getHp();
                case 3:
                    return entry.getAtk();
                case 4:
                    return entry.getDef();
                case 5:
                    return entry.getSpeed();
                case 6:
                    return entry.getSpAtk();
                case 7:
                    return entry.getSpDef();
                case 8:
                    return entry.getType1();
                case 9:
                    return entry.getType2();
                case 10:
                    return entry.getCatchRate();
                case 11:
                    return entry.getBaseExp();
                case 12:
                    return entry.getHpEvYield();
                case 13:
                    return entry.getAtkEvYield();
                case 14:
                    return entry.getDefEvYield();
                case 15:
                    return entry.getSpeedEvYield();
                case 16:
                    return entry.getSpAtkEvYield();
                case 17:
                    return entry.getSpDefEvYield();
                case 18:
                    return entry.getUncommonItem();
                case 19:
                    return entry.getRareItem();
                case 20:
                    return entry.getGenderRatio();
                case 21:
                    return entry.getHatchMultiplier();
                case 22:
                    return entry.getBaseHappiness();
                case 23:
                case 24:
                case 25:
                    return 0;
                case 26:
                    return entry.getAbility1();
                case 27:
                    return entry.getAbility2();
                case 28:
                    return entry.getRunChance();
                case 29:
                    return entry.getDexColor();
                case 30:
                    return entry.isFlip();
            }

            return null;
        }
    }
}
