package io.github.turtleisaac.pokeditor.gui.sheets.tables.models;

import javax.swing.*;

public class PersonalTableModel extends EditorTableModel
{
    public static final Class<?>[] personalClasses = new Class<?>[] {Integer.class, String.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class, JSpinner.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JSpinner.class, JSpinner.class};
    public static final String[] personalNames = new String[] {"ID Number", "Name", "HP", "Attack", "Defense", "Speed", "Sp. Atk", "Sp. Def", "Type 1", "Type 2", "Catch Rate", "Exp Drop", "HP EV Yield", "Spe EV Yield", "Atk EV Yield", "Def EV Yield", "Sp. Atk EV Yield", "Sp. Def EV Yield", "Uncommon Held Item", "Rare Held Item", "Gender Ratio", "Hatch Mult.", "Base Happiness", "Growth Rate", "Egg Group 1", "Egg Group 2", "Ability 1", "Ability 2", "Run Chance", "DO NOT TOUCH"};

    public PersonalTableModel()
    {
        super(personalClasses, personalNames);
    }

    @Override
    public int getRowCount()
    {
        return 0;
    }

    @Override
    public int getColumnCount()
    {
        return COLUMN_NAMES.length;
    }
}
