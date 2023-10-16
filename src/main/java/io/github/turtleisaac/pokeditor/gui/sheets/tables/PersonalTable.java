package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.google.inject.Inject;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;

import java.util.*;

public class PersonalTable extends DefaultTable<PersonalData>
{
    public static final int[] columnWidths = new int[] {40, 100, 65, 65, 65, 65, 65, 65, 100, 100, 65, 65, 65, 65, 65, 65, 65, 65, 140, 140, 65, 65, 70, 120, 120, 120, 140, 140, 65, 65, 65};
    private static final String[] growthRateKeys = new String[] {"growthRate.mediumFast", "growthRate.erratic",  "growthRate.fluctuating", "growthRate.mediumSlow", "growthRate.fast", "growthRate.slow", "growthRate.mediumFast", "growthRate.mediumFast"};
    private static final String[] eggGroupKeys = new String[] {"eggGroup.null", "eggGroup.monster", "eggGroup.water1", "eggGroup.bug", "eggGroup.flying", "eggGroup.field", "eggGroup.fairy", "eggGroup.grass", "eggGroup.humanLike", "eggGroup.water3", "eggGroup.mineral", "eggGroup.amorphous", "eggGroup.water2", "eggGroup.ditto", "eggGroup.dragon", "eggGroup.undiscovered"};

    public PersonalTable(List<PersonalData> data, List<TextBankData> textData)
    {
        super(PersonalModel.personalClasses, new PersonalModel(data, textData), textData, columnWidths);
    }

    @Override
    Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();
        String[] temp = textData.get(TextFiles.TYPE_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);

        temp = textData.get(TextFiles.ITEM_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);

        textSources.add(DefaultTable.loadStringsFromKeys(growthRateKeys));

        temp = DefaultTable.loadStringsFromKeys(eggGroupKeys);
        textSources.add(temp);
        textSources.add(temp);

        temp = textData.get(TextFiles.ABILITY_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);
        textSources.add(temp);

        return textSources;
    }

    @Override
    public Class<PersonalData> getDataClass()
    {
        return PersonalData.class;
    }

    static class PersonalModel extends FormatModel<PersonalData>
    {
        static final CellTypes[] personalClasses = new CellTypes[] {CellTypes.INTEGER, CellTypes.STRING, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.COLORED_COMBO_BOX, CellTypes.COLORED_COMBO_BOX, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.COMBO_BOX, CellTypes.COMBO_BOX, CellTypes.COMBO_BOX, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.CHECKBOX};
        private static final String[] columnKeys = new String[] {"id", "name", "hp", "atk", "def", "speed", "spAtk", "spDef", "personal.type1", "personal.type2", "catchRate", "expDrop", "hpEvYield", "atkEvYield", "defEvYield", "speedEvYield", "spAtkEvYield", "spDefEvYield", "uncommonHeldItem", "rareHeldItem", "genderRatio", "hatchMultiplier", "baseHappiness", "growthRate", "personal.eggGroup1", "personal.eggGroup2", "personal.ability1", "personal.ability2", "runChance", "color", "flip"};

        public PersonalModel(List<PersonalData> data, List<TextBankData> textBankData)
        {
            super(columnKeys, data, textBankData);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            PersonalData entry = getData().get(rowIndex);
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());

            if (aValue instanceof String)
            {
                if (columnIndex != 1 && columnIndex != 30)
                    aValue = Integer.parseInt(((String) aValue).trim());
                else if (columnIndex == 30)
                    aValue = Boolean.parseBoolean(((String) aValue).trim());
            }


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
                case 23 -> entry.setExpRate((Integer) aValue);
                case 24 -> entry.setEggGroup1((Integer) aValue);
                case 25 -> entry.setEggGroup2((Integer) aValue);
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
                case 23 -> {
                    return entry.getExpRate();
                }
                case 24-> {
                    return entry.getEggGroup1();
                }
                case 25 -> {
                    return entry.getEggGroup2();
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
