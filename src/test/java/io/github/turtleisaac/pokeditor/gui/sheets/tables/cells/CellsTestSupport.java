package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Shared fixtures for the cell renderer/editor tests.
 * <p>
 * Deliberately contains no PokEditor-Core types: everything under test here is a plain Swing
 * component, so a test failure can only mean the component is wrong.
 */
final class CellsTestSupport
{
    static
    {
        // must be set before any AWT class initialises a GraphicsEnvironment
        System.setProperty("java.awt.headless", "true");
    }

    private CellsTestSupport() {}

    /** A live JTable, because every renderer here reads selection/background state off one. */
    static JTable table()
    {
        return new JTable(new DefaultTableModel(4, 4));
    }

    /**
     * The domain of values a renderer can actually be handed by a painting JTable: legal
     * indices, indices past the end of the name list (hacked ROM, or a paste of a raw number),
     * negatives, nulls, and values whose type is not what the column nominally holds.
     * A renderer runs inside paint() once per visible cell - if any of these throws, the whole
     * sheet stops painting and the exception storm is invisible to the user.
     */
    static List<Object> hostileValues()
    {
        return Arrays.asList(
                0, 1, 5, 17, 20, 25, 255, 65535,
                -1, -20, Integer.MAX_VALUE, Integer.MIN_VALUE,
                null,
                "0", "5", "20", "-1",
                "", " ", "not a number", "12abc",
                3.5d, 7L, Boolean.TRUE, new Object());
    }

    /** Same idea, for components whose column nominally holds a boolean. */
    static List<Object> hostileBooleanValues()
    {
        return Arrays.asList(Boolean.TRUE, Boolean.FALSE, null, "true", "", 0, 1, new Object());
    }

    /**
     * Runs {@code call} over every value and reports, rather than propagates, each failure -
     * so one test names every value the component cannot survive instead of stopping at the
     * first one.
     */
    static List<String> failuresOver(List<Object> values, Function<Object, Component> call)
    {
        List<String> failures = new ArrayList<>();
        for (Object value : values)
        {
            try
            {
                if (call.apply(value) == null)
                    failures.add(describe(value) + " -> returned null instead of a component");
            }
            catch (Throwable t)
            {
                failures.add(describe(value) + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        }
        return failures;
    }

    static String describe(Object value)
    {
        if (value == null)
            return "null";
        return value.getClass().getSimpleName() + "(" + value + ")";
    }

    /** {@code count} distinct display names, so a round trip cannot pass by coincidence. */
    static String[] names(int count)
    {
        String[] items = new String[count];
        items[0] = "-----";
        for (int i = 1; i < count; i++)
            items[i] = "name-" + i;
        return items;
    }

    /** The text a combo box actually shows for row {@code index} of its model. */
    static String displayedTextAt(JComboBox<?> comboBox, int index)
    {
        return String.valueOf(comboBox.getItemAt(index));
    }

    /**
     * Simulates a user picking the option whose visible text is {@code text}: finds it by what
     * it says, not by the index the test already knows.
     */
    static int selectByDisplayedText(JComboBox<?> comboBox, String text)
    {
        int found = -1;
        for (int i = 0; i < comboBox.getItemCount(); i++)
        {
            if (displayedTextAt(comboBox, i).equals(text))
            {
                if (found != -1)
                    throw new IllegalArgumentException("ambiguous fixture: '" + text + "' appears at " + found + " and " + i);
                found = i;
            }
        }
        if (found == -1)
            throw new IllegalArgumentException("no option displays '" + text + "'");
        comboBox.setSelectedIndex(found);
        return found;
    }
}
