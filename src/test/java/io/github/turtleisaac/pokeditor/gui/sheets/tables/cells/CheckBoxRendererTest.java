package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.CheckBoxRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

class CheckBoxRendererTest
{
    private final JTable table = table();
    private final CheckBoxRenderer renderer = new CheckBoxRenderer();

    @Test
    @DisplayName("renders a component for every value a painting table could hand it, without throwing")
    void totalOverEveryValue()
    {
        // Same paint()-time contract as every other renderer. A boolean column is not immune:
        // a pasted cell, a freshly inserted row, or a format whose flag is absent all deliver
        // something that is not a Boolean, and one of them stops the sheet painting for good.
        List<String> failures = failuresOver(hostileBooleanValues(),
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 1, 0));

        assertThat(failures).as("values this checkbox renderer cannot render").isEmpty();
    }

    @Test
    @DisplayName("shows a checkbox whose selection is the boolean it was given")
    void selectionReflectsValue()
    {
        // The user reads the tick, not the model. If the tick does not track the value, every
        // TM compatibility cell in the sheet lies about what is stored.
        assertThat(checkBoxSelectedFor(Boolean.TRUE)).as("rendered state for true").isTrue();
        assertThat(checkBoxSelectedFor(Boolean.FALSE)).as("rendered state for false").isFalse();
        // and the component must not latch: back and forth, repeatedly
        assertThat(checkBoxSelectedFor(Boolean.TRUE)).isTrue();
        assertThat(checkBoxSelectedFor(Boolean.FALSE)).isFalse();
    }

    private boolean checkBoxSelectedFor(Object value)
    {
        Component c = renderer.getTableCellRendererComponent(table, value, false, false, 1, 0);
        JCheckBox box = findCheckBox(c);
        assertThat(box).as("rendered component must contain a checkbox").isNotNull();
        return box.isSelected();
    }

    private static JCheckBox findCheckBox(Component c)
    {
        if (c instanceof JCheckBox box)
            return box;
        if (c instanceof Container container)
        {
            for (Component child : container.getComponents())
            {
                JCheckBox found = findCheckBox(child);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
}
