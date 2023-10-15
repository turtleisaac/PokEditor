package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.github.turtleisaac.pokeditor.gui.sheets.Sheet;
//import io.github.turtleisaac.pokeditor.gui.sheets.SheetEditorPanel;
import io.github.turtleisaac.pokeditor.sheets.JohtoSheets;
import io.github.turtleisaac.pokeditor.sheets.SinnohSheets;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PersonalTable extends DefaultTable
{
    public static final Class<?>[] personalClasses = new Class<?>[] {Integer.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class, Integer.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, JComboBox.class, Integer.class, Integer.class};
    public static final String[] personalNames = new String[] {"ID", "Name", "HP", "Atk", "Def", "Speed", "Sp. Atk", "Sp. Def", "Type 1", "Type 2", "Catch Rate", "Exp Drop", "HP EV", "Speed EV", "Atk EV", "Def EV", "Sp. Atk EV", "Sp. Def EV", "Uncommon Held Item", "Rare Held Item", "Gender Ratio", "Hatch Mult.", "Base Happiness", "Growth Rate", "Egg Group 1", "Egg Group 2", "Ability 1", "Ability 2", "Run Chance", "DO NOT TOUCH"};
    public static final int[] columnWidths = new int[] {40, 100, 65, 65, 65, 65, 65, 65, 80, 80, 65, 65, 65, 65, 65, 65, 65, 65, 120, 120, 65, 65, 70, 100, 100, 100, 100, 100, 65, 65};

    public PersonalTable(Object[][] data)
    {
        super(personalClasses, personalNames, columnWidths, data);
    }


    public static void main(String[] args)
    {
        FlatDarculaLaf.install();
//        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
//        if (defaults.get("Table.alternateRowColor") == null)
//            defaults.put("Table.alternateRowColor", new Color(208, 108, 108));

        Object[][] data = new Object[500][];
        for(int i= 0; i < data.length; i++)
        {
            data[i] = generateTestRow();
        }

        Sheet s = new Sheet()
        {
            @Override
            public Object[][] getData()
            {
                return data;
            }

            @Override
            public SinnohSheets getSheetType()
            {
                return SinnohSheets.Personal;
            }

            @Override
            public JohtoSheets getAltSheetType()
            {
                return null;
            }
        };

//        SheetEditorPanel frame = new SheetEditorPanel(s);
//        frame.setPreferredSize(new Dimension(1500, 800));
    }


    private static Object[] generateTestRow()
    {

        ArrayList<Object> row = new ArrayList<>();
        for(Class<?> c : personalClasses)
        {
            if(c == JCheckBox.class)
            {
                row.add(true);
            }
            else if(c == JComboBox.class)
            {
                row.add("Moo1");
            }
            else if(c == JSpinner.class)
            {
                row.add(1);
            }
            else if(c == JButton.class)
            {
                row.add(true);
            }
            else if(c == String.class)
            {
                row.add("species");
            }
            else if(c == Integer.class)
            {
                row.add(0);
            }
        }

        return row.toArray(new Object[] {});
    }
}
