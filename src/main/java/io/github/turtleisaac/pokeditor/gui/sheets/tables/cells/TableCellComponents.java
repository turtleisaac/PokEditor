package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.BitfieldComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.BitfieldStringCellRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.CheckBoxRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Decides which renderer and editor a cell type gets.
 * <p>
 * This used to live inside {@code DefaultTable.loadCellRenderers}, welded to the loop that
 * installs the results on a {@code TableColumn}. That made the mapping unreachable from a
 * test without building an entire sheet model, so the only way to check that every
 * {@link CellTypes} constant was handled was to read the method's source text and look for
 * its name - a test that depended on the working directory and could not tell a real
 * dispatch from a mention in a comment.
 * <p>
 * The pairing is a pure function of the cell type and the text it needs, so it belongs on
 * its own where it can simply be called.
 */
public final class TableCellComponents
{
    private TableCellComponents() {}

    /**
     * A renderer and editor for one column. Either half may be null, meaning "leave the
     * table's default in place for this column".
     */
    public record Pair(TableCellRenderer renderer, TableCellEditor editor)
    {
        static final Pair NONE = new Pair(null, null);
    }

    /**
     * @param type the cell type the column declares
     * @param text the externally supplied names this column needs, or null when it needs
     *             none. A combo box column takes one list; a {@link CellTypes#CUSTOM} column
     *             takes three, in the order species, item, move.
     * @param valueRange the inclusive {min, max} the column stores, used to bound the
     *                   numeric editor
     * @param customSupplier builds the pair for a {@link CellTypes#CUSTOM} column
     * @return the pair for this column, never null
     */
    public static Pair forType(CellTypes type, String[][] text, int[] valueRange,
                               CellTypes.CustomCellFunctionSupplier customSupplier)
    {
        switch (type)
        {
            case CHECKBOX:
                return new Pair(new CheckBoxRenderer(), new CheckBoxEditor());

            case COMBO_BOX:
            {
                String[] names = firstList(text);
                return new Pair(new IndexedStringCellRenderer(names), new ComboBoxCellEditor(names));
            }

            case COLORED_COMBO_BOX:
            {
                String[] names = firstList(text);
                return new Pair(
                        new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(names, PokeditorManager.typeColors),
                        new ComboBoxCellEditor(names));
            }

            case BITFIELD_COMBO_BOX:
            {
                String[] names = firstList(text);
                return new Pair(new BitfieldStringCellRenderer(names), new BitfieldComboBoxEditor(names));
            }

            case INTEGER:
            {
                int[] range = valueRange == null ? new int[] {0, 255} : valueRange;
                return new Pair(null, new NumberOnlyCellEditor(range[0], range[1]));
            }

            case CUSTOM:
            {
                if (customSupplier == null || text == null || text.length < 3)
                    return Pair.NONE;
                return new Pair(customSupplier.getRenderer(text[0], text[1], text[2]),
                        customSupplier.getEditor(text[0], text[1], text[2]));
            }

            case STRING:
            default:
                // rendered by the table's own default renderer and edited as plain text
                return Pair.NONE;
        }
    }

    /**
     * A column that names its values is unusable without the names, but a missing list must
     * not be allowed to reach the renderer as null and fail during painting.
     */
    private static String[] firstList(String[][] text)
    {
        if (text == null || text.length == 0 || text[0] == null)
            return new String[] {""};
        return text[0];
    }
}
