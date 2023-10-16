package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
//import io.github.turtleisaac.pokeditor.gui.sheets.SheetEditorPanel;

import javax.swing.*;
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

    static class PersonalModel extends FormatModel<PersonalData>
    {
        static final Class<?>[] personalClasses = new Class<?>[] {Integer.class, String.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class, JCheckBox.class};
        static final String[] personalNames = new String[] {"ID", "Name", "HP", "Atk", "Def", "Speed", "Sp. Atk", "Sp. Def", "Type 1", "Type 2", "Catch Rate", "Exp Drop", "HP EV", "Atk EV", "Def EV", "Speed EV", "Sp. Atk EV", "Sp. Def EV", "Uncommon Held Item", "Rare Held Item", "Gender Ratio", "Hatch Mult.", "Base Happiness", "Growth Rate", "Egg Group 1", "Egg Group 2", "Ability 1", "Ability 2", "Run Chance", "Color", "Flip"};

        public PersonalModel(List<PersonalData> data, List<TextBankData> textBankData)
        {
            super(personalNames, data, textBankData);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            PersonalData entry = getData().get(rowIndex);
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());

            if (columnIndex != 1 && columnIndex != 30 && aValue instanceof String)
                aValue = Integer.parseInt(((String) aValue).trim());

            switch (columnIndex) {
                case 0 -> {}
                case 1 -> {
                    TextBankData.Message message = speciesNames.get(rowIndex);
                    message.setText((String) aValue);
                }
                case 2 -> entry.setHp((Integer) aValue);
                case 3 -> entry.setAtk((Integer) aValue);
                case 4 -> entry.setDef((Integer) aValue);
                case 5 -> entry.setSpeed((Integer) aValue);
                case 6 -> entry.setSpAtk((Integer) aValue);
                case 7 -> entry.setSpDef((Integer) aValue);
                case 8 -> entry.setType1((Integer) aValue);
                case 9 -> entry.setType2((Integer) aValue);
                case 10 -> entry.setCatchRate((Integer) aValue);
                case 11 -> entry.setBaseExp((Integer) aValue);
                case 12 -> entry.setHpEvYield((Integer) aValue);
                case 13 -> entry.setAtkEvYield((Integer) aValue);
                case 14 -> entry.setDefEvYield((Integer) aValue);
                case 15 -> entry.setSpeedEvYield((Integer) aValue);
                case 16 -> entry.setSpAtkEvYield((Integer) aValue);
                case 17 -> entry.setSpDefEvYield((Integer) aValue);
                case 18 -> entry.setUncommonItem((Integer) aValue);
                case 19 -> entry.setRareItem((Integer) aValue);
                case 20 -> entry.setGenderRatio((Integer) aValue);
                case 21 -> entry.setHatchMultiplier((Integer) aValue);
                case 22 -> entry.setBaseHappiness((Integer) aValue);
                case 23, 24, 25 -> {}
                case 26 -> entry.setAbility1((Integer) aValue);
                case 27 -> entry.setAbility2((Integer) aValue);
                case 28 -> entry.setRunChance((Integer) aValue);
                case 29 -> entry.setDexColor((Integer) aValue);
                case 30 -> entry.setFlip((Boolean) aValue);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            PersonalData entry = getData().get(rowIndex);

            switch(columnIndex) {
                case 0 -> {
                    return rowIndex;
                }
                case 1 -> {
                    if(rowIndex < speciesNames.size())
                        return speciesNames.get(rowIndex).getText();
                    else
                        return "";
                }
                case 2 -> {
                    return entry.getHp();
                }
                case 3 -> {
                    return entry.getAtk();
                }
                case 4 -> {
                    return entry.getDef();
                }
                case 5 -> {
                    return entry.getSpeed();
                }
                case 6 -> {
                    return entry.getSpAtk();
                }
                case 7 -> {
                    return entry.getSpDef();
                }
                case 8 -> {
                    return entry.getType1();
                }
                case 9 -> {
                    return entry.getType2();
                }
                case 10 -> {
                    return entry.getCatchRate();
                }
                case 11 -> {
                    return entry.getBaseExp();
                }
                case 12 -> {
                    return entry.getHpEvYield();
                }
                case 13 -> {
                    return entry.getAtkEvYield();
                }
                case 14 -> {
                    return entry.getDefEvYield();
                }
                case 15 -> {
                    return entry.getSpeedEvYield();
                }
                case 16 -> {
                    return entry.getSpAtkEvYield();
                }
                case 17 -> {
                    return entry.getSpDefEvYield();
                }
                case 18 -> {
                    return entry.getUncommonItem();
                }
                case 19 -> {
                    return entry.getRareItem();
                }
                case 20 -> {
                    return entry.getGenderRatio();
                }
                case 21 -> {
                    return entry.getHatchMultiplier();
                }
                case 22 -> {
                    return entry.getBaseHappiness();
                }
                case 23, 24, 25 -> {
                    return 0;
                }
                case 26 -> {
                    return entry.getAbility1();
                }
                case 27 -> {
                    return entry.getAbility2();
                }
                case 28 -> {
                    return entry.getRunChance();
                }
                case 29 -> {
                    return entry.getDexColor();
                }
                case 30 -> {
                    return entry.isFlip();
                }
            }

            return null;
        }
    }
}
