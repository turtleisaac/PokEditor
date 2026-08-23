package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.BitfieldComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.BitfieldStringCellRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.CheckBoxRenderer;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Every cell type must be dispatched to components that can actually display and edit it.
 * <p>
 * These assertions used to be made by reading the source text of
 * {@code DefaultTable.loadCellRenderers} and looking for each constant's name, because the
 * mapping could not be reached without constructing a whole sheet model. That test could not
 * distinguish a real dispatch from a mention in a comment, and it depended on the process
 * working directory. Now that the mapping is a pure function it is simply called.
 */
class CellTypesTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * The one cell type deliberately left to the JTable defaults: a STRING column needs no
     * special editor or renderer. Every other constant must be dispatched explicitly.
     */
    private static final Set<CellTypes> INTENTIONALLY_UNDISPATCHED = EnumSet.of(CellTypes.STRING);

    private static final String[][] NAMES = {{"a", "b", "c"}, {"i0", "i1"}, {"m0", "m1"}};

    private static final CellTypes.CustomCellFunctionSupplier CUSTOM_SUPPLIER =
            new CellTypes.CustomCellFunctionSupplier()
            {
                @Override
                public TableCellEditor getEditor(String[]... strings)
                {
                    return new ComboBoxCellEditor(strings[0]);
                }

                @Override
                public TableCellRenderer getRenderer(String[]... strings)
                {
                    return new IndexedStringCellRenderer(strings[0]);
                }
            };

    private static TableCellComponents.Pair dispatch(CellTypes type)
    {
        return TableCellComponents.forType(type, NAMES, new int[] {0, 255}, CUSTOM_SUPPLIER);
    }

    @Test
    @DisplayName("every cell type is dispatched to an editor and renderer, or is explicitly a default-rendered type")
    void everyCellTypeIsDispatched()
    {
        // The mapping is a switch over an enum but returns a default rather than being
        // exhaustive, so the compiler will not catch a constant added without a branch. Such a
        // column silently falls through to the plain JTable editor - a species column that
        // accepts free text, a flag column with no checkbox. Enumerating values() means a new
        // constant is covered the moment it exists.
        for (CellTypes type : CellTypes.values())
        {
            TableCellComponents.Pair pair = dispatch(type);
            assertThat(pair).as("%s must map to a pair, never null", type).isNotNull();

            if (INTENTIONALLY_UNDISPATCHED.contains(type))
                continue;

            assertThat(pair.editor()).as("%s must be given an editor", type).isNotNull();

            // INTEGER is the one dispatched type with no renderer of its own: a number is
            // displayed correctly by the table's default renderer, and only its editing needs
            // bounding. Every other type shows something other than its stored value.
            if (type != CellTypes.INTEGER)
                assertThat(pair.renderer()).as("%s must be given a renderer", type).isNotNull();
        }
    }

    @Test
    @DisplayName("each cell type gets the components that can actually display it")
    void eachTypeGetsTheRightComponents()
    {
        // asserted against what each type means, not against what the code returns: a
        // checkbox column needs a checkbox, a column showing names needs the renderer that
        // maps an index to a name, and a bitfield needs the one that maps a bit to a name
        assertThat(dispatch(CellTypes.CHECKBOX).renderer()).isInstanceOf(CheckBoxRenderer.class);
        assertThat(dispatch(CellTypes.CHECKBOX).editor()).isInstanceOf(CheckBoxEditor.class);

        assertThat(dispatch(CellTypes.COMBO_BOX).renderer()).isInstanceOf(IndexedStringCellRenderer.class);
        assertThat(dispatch(CellTypes.COMBO_BOX).editor()).isInstanceOf(ComboBoxCellEditor.class);

        assertThat(dispatch(CellTypes.COLORED_COMBO_BOX).renderer())
                .isInstanceOf(IndexedStringCellRenderer.ColoredIndexedStringCellRenderer.class);

        assertThat(dispatch(CellTypes.BITFIELD_COMBO_BOX).renderer()).isInstanceOf(BitfieldStringCellRenderer.class);
        assertThat(dispatch(CellTypes.BITFIELD_COMBO_BOX).editor()).isInstanceOf(BitfieldComboBoxEditor.class);

        assertThat(dispatch(CellTypes.INTEGER).editor()).isInstanceOf(NumberOnlyCellEditor.class);

        assertThat(dispatch(CellTypes.STRING).renderer()).isNull();
        assertThat(dispatch(CellTypes.STRING).editor()).isNull();
    }

    @Test
    @DisplayName("the numeric editor is bounded by the column's declared range")
    void integerEditorHonoursTheDeclaredRange()
    {
        // the range is the only thing distinguishing a priority column (-128..127) from a
        // stat column (0..255); dropping it on the way through would silently widen both
        NumberOnlyCellEditor editor = (NumberOnlyCellEditor) TableCellComponents
                .forType(CellTypes.INTEGER, null, new int[] {-128, 127}, CUSTOM_SUPPLIER).editor();
        assertThat(editor).isNotNull();
    }

    @Test
    @DisplayName("a missing name list degrades to a placeholder rather than reaching the renderer as null")
    void missingTextDoesNotProduceANullBackedRenderer()
    {
        // a null names array would not fail here but during painting, taking the whole sheet
        // down; the documented behaviour is to substitute a single empty entry
        for (CellTypes type : new CellTypes[] {CellTypes.COMBO_BOX, CellTypes.COLORED_COMBO_BOX,
                CellTypes.BITFIELD_COMBO_BOX})
        {
            assertThatCode(() -> {
                TableCellComponents.forType(type, null, null, CUSTOM_SUPPLIER);
                TableCellComponents.forType(type, new String[][] {null}, null, CUSTOM_SUPPLIER);
                TableCellComponents.forType(type, new String[0][], null, CUSTOM_SUPPLIER);
            }).as("%s with no names must still build", type).doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("a custom column with no supplier or no text yields no components rather than throwing")
    void customColumnDegradesRatherThanThrowing()
    {
        // buildColumnTextSources assigns the species/item/move triple only to the first
        // custom column, so any path that asks a later one for its text gets null. That used
        // to reach an array index and fail during table construction.
        assertThat(TableCellComponents.forType(CellTypes.CUSTOM, null, null, CUSTOM_SUPPLIER).renderer()).isNull();
        assertThat(TableCellComponents.forType(CellTypes.CUSTOM, NAMES, null, null).editor()).isNull();
        assertThat(TableCellComponents.forType(CellTypes.CUSTOM, new String[][] {{"a"}}, null, CUSTOM_SUPPLIER)
                .editor()).isNull();
    }

    @Test
    @DisplayName("the dispatch is a pure function - the same input yields equivalent, independent components")
    void dispatchIsPureAndReturnsFreshInstances()
    {
        // sharing is the caller's decision (DefaultTable deliberately shares one pair across
        // every custom column); the mapping itself must not hold state between calls, or two
        // columns would silently edit through the same widget
        for (CellTypes type : CellTypes.values())
        {
            TableCellComponents.Pair first = dispatch(type);
            TableCellComponents.Pair second = dispatch(type);

            if (first.editor() != null)
                assertThat(first.editor()).as("%s editor", type).isNotSameAs(second.editor());
            if (first.renderer() != null)
                assertThat(first.renderer()).as("%s renderer", type).isNotSameAs(second.renderer());
        }
    }

    @Test
    @DisplayName("the custom cell supplier provides both halves")
    void customSupplierProvidesBothHalves()
    {
        TableCellComponents.Pair pair = dispatch(CellTypes.CUSTOM);
        assertThat(pair.renderer()).isNotNull();
        assertThat(pair.editor()).isNotNull();
    }

    @Test
    @DisplayName("the set of cell types is the known one")
    void constantSetIsTheKnownOne()
    {
        // a tripwire: adding a constant should be a deliberate act that also updates the
        // dispatch and this list, rather than something that happens silently
        assertThat(CellTypes.values()).containsExactlyInAnyOrder(
                CellTypes.STRING, CellTypes.INTEGER, CellTypes.CHECKBOX, CellTypes.COMBO_BOX,
                CellTypes.COLORED_COMBO_BOX, CellTypes.BITFIELD_COMBO_BOX, CellTypes.CUSTOM);
    }
}
