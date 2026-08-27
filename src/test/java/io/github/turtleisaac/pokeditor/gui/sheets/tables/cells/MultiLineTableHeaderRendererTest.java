package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.MultiLineTableHeaderRenderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.failuresOver;
import static io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellsTestSupport.table;
import static org.assertj.core.api.Assertions.assertThat;

class MultiLineTableHeaderRendererTest
{
    private static final List<Object> HEADERS = Arrays.asList(
            "Power", "", null, "   ",
            "Reflected By Magic Coat",
            "a very long column heading that will certainly have to wrap more than once ".repeat(8),
            "<html><b>not markup</b>", "&amp; < > \" '", "line\nbreak", "\t", 42);

    private final JTable table = table();
    private final MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();

    @Test
    @DisplayName("renders a component for any header text, including empty, null and markup-like strings")
    void totalOverEveryHeaderText()
    {
        // The header is painted before any row is. A header string it cannot handle costs the
        // user the entire sheet, not just the column label.
        List<String> failures = failuresOver(HEADERS,
                value -> renderer.getTableCellRendererComponent(table, value, false, false, 0, 0));

        assertThat(failures).as("header values this renderer cannot render").isEmpty();
    }

    @Test
    @DisplayName("shows header text literally rather than interpreting it as markup")
    void headerTextIsNotInterpretedAsMarkup()
    {
        // A JTextArea header is chosen precisely so a column name containing < or & shows as
        // typed. Rendering it as HTML would silently swallow part of a column's name.
        String raw = "<html><b>not markup</b>";
        JTextArea area = (JTextArea) renderer.getTableCellRendererComponent(table, raw, false, false, 0, 0);

        assertThat(area.getText()).isEqualTo(raw);
    }

    @Test
    @DisplayName("wraps long header text instead of rendering it as a single unreadable line")
    void longHeaderTextWraps()
    {
        // The reason this renderer exists at all: sheet columns are narrow and their names are not.
        JTextArea area = (JTextArea) renderer.getTableCellRendererComponent(table, "Reflected By Magic Coat", false, false, 0, 0);

        assertThat(area.getLineWrap()).as("line wrap").isTrue();
        assertThat(area.getWrapStyleWord()).as("word wrap").isTrue();
        assertThat(area.isEditable()).as("a header must not be editable").isFalse();
    }

    @Test
    @DisplayName("sizes itself to the width of the column it is rendering")
    void sizedToItsColumn()
    {
        // A header sized to some other column's width either clips its own name or overlaps its
        // neighbour, which is how a column ends up looking like it has no name at all.
        table.getColumnModel().getColumn(1).setWidth(37);
        Component c = renderer.getTableCellRendererComponent(table, "Power", false, false, 0, 1);

        assertThat(c.getWidth()).isEqualTo(37);
    }
}
