package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The text sources a sheet needs arrive as a <em>positional</em> queue: element k is the name list
 * intended for the k-th text-consuming column, in column order. A positional protocol of that kind
 * is a statement about a function
 *
 * <pre>    pos : {columns needing text} -> {0 .. n-1}</pre>
 *
 * and it is only well defined if that function is
 * <ul>
 *   <li><b>total and injective over the supply</b> - every element is consumed by exactly one
 *       column, so no column is served an element intended for another; and</li>
 *   <li><b>the same function for every walker</b> - the code which installs the editors and the
 *       code which later refreshes them must derive the identical mapping, otherwise a refresh
 *       silently re-labels columns.</li>
 * </ul>
 *
 * Those two clauses are what these tests assert. The reference mapping used here is derived in
 * {@link #referencePositions(CellTypes[])} from the <em>specification</em> of the protocol (one
 * element per combo-box column; one shared triple for the custom columns, which is what a single
 * shared custom editor means) and never from observing the production walk. The independent
 * confirmation that this is the intended protocol is {@code EvolutionsTable.obtainTextSources},
 * which supplies exactly {@code referenceSourceCount} elements for its own layout.
 * <p>
 * The historical failure this guards against: one walker consumed three elements for a
 * {@code CellTypes.CUSTOM} column while the other consumed none, so every combo-box column after
 * the custom column was handed the previous column's names - wrong Pokemon/move/item names in the
 * sheet, with nothing thrown and nothing logged.
 */
public class TextSourceSymmetryTest
{
    /**
     * A deliberately awkward layout: combo boxes before and after a custom column which sits in
     * the <em>middle</em>, all three combo-box flavours present, a second custom column that must
     * share with the first, and checkbox/integer/string columns interleaved to prove that
     * non-consuming columns do not perturb the mapping.
     */
    private static final CellTypes[] AWKWARD = {
            CellTypes.CHECKBOX,             // 0 - consumes nothing
            CellTypes.COMBO_BOX,            // 1 - consumes 1
            CellTypes.INTEGER,              // 2 - consumes nothing
            CellTypes.COLORED_COMBO_BOX,    // 3 - consumes 1
            CellTypes.CUSTOM,               // 4 - consumes 3 (species, item, move)
            CellTypes.STRING,               // 5 - consumes nothing
            CellTypes.COMBO_BOX,            // 6 - consumes 1
            CellTypes.BITFIELD_COMBO_BOX,   // 7 - consumes 1
            CellTypes.CUSTOM,               // 8 - shares column 4's editor, consumes nothing
            CellTypes.COMBO_BOX             // 9 - consumes 1
    };

    private static final int FROZEN = 2;
    private static final int ROWS = 4;

    private E_CustomCellSupplier supplier;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    void resetStatics()
    {
        E_ProbeTable.sourceCount = 0;
        E_ProbeTable.nullPosition = -1;
        supplier = new E_CustomCellSupplier();
    }

    // ---------------------------------------------------------------- reference mapping

    /**
     * The specification of the positional queue, restated independently of the production code:
     * walking the columns in ascending order, a combo-box column of any flavour claims the next
     * single element, and the custom columns collectively claim one triple (they share a single
     * editor/renderer instance, so there is exactly one triple for the whole table however many
     * custom columns there are).
     *
     * @return column index -> the queue position at which that column's block starts
     */
    private static Map<Integer, Integer> referencePositions(CellTypes[] layout)
    {
        Map<Integer, Integer> positions = new LinkedHashMap<>();
        int next = 0;
        boolean customClaimed = false;
        for (int column = 0; column < layout.length; column++)
        {
            CellTypes type = layout[column];
            if (type == CellTypes.COMBO_BOX || type == CellTypes.COLORED_COMBO_BOX || type == CellTypes.BITFIELD_COMBO_BOX)
            {
                positions.put(column, next);
                next += 1;
            }
            else if (type == CellTypes.CUSTOM && !customClaimed)
            {
                customClaimed = true;
                positions.put(column, next);
                next += 3;
            }
        }
        return positions;
    }

    /** the total supply the reference mapping accounts for */
    private static int referenceSourceCount(CellTypes[] layout)
    {
        int total = 0;
        boolean customClaimed = false;
        for (CellTypes type : layout)
        {
            if (type == CellTypes.COMBO_BOX || type == CellTypes.COLORED_COMBO_BOX || type == CellTypes.BITFIELD_COMBO_BOX)
                total += 1;
            else if (type == CellTypes.CUSTOM && !customClaimed)
            {
                customClaimed = true;
                total += 3;
            }
        }
        return total;
    }

    // ---------------------------------------------------------------- fixture helpers

    private E_ProbeTable build(CellTypes[] layout, int supply)
    {
        E_ProbeTable.sourceCount = supply;
        E_LayoutModel model = E_LayoutModel.create(layout, FROZEN, ROWS);
        int[] widths = new int[FROZEN + layout.length];
        Arrays.fill(widths, 50);
        return new E_ProbeTable(model, widths, supplier);
    }

    private E_ProbeTable build(CellTypes[] layout)
    {
        return build(layout, referenceSourceCount(layout));
    }

    /**
     * The name list a combo-box column's renderer is currently holding. Read reflectively because
     * {@code IndexedStringCellRenderer.items} is package private to the renderers package; reading
     * the array itself (rather than a rendered string) is what lets the whole list, and not just
     * one entry of it, be compared.
     */
    private static String[] rendererItems(TableCellRenderer renderer)
    {
        try
        {
            Field field = IndexedStringCellRenderer.class.getDeclaredField("items");
            field.setAccessible(true);
            return (String[]) field.get(renderer);
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not read the renderer's name list", e);
        }
    }

    /** the name list a combo-box column's editor is currently offering, read off its combo model */
    private static String[] editorItems(TableCellEditor editor)
    {
        try
        {
            Field field = ComboBoxCellEditor.class.getDeclaredField("comboBox");
            field.setAccessible(true);
            JComboBox<?> box = (JComboBox<?>) field.get(editor);
            String[] items = new String[box.getItemCount()];
            for (int i = 0; i < items.length; i++)
                items[i] = String.valueOf(box.getItemAt(i));
            return items;
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not read the editor's name list", e);
        }
    }

    /** a snapshot of what every text-consuming column currently holds, keyed by column */
    private static Map<Integer, String[]> installedText(E_ProbeTable table, CellTypes[] layout)
    {
        Map<Integer, String[]> installed = new LinkedHashMap<>();
        for (int column = 0; column < layout.length; column++)
        {
            CellTypes type = layout[column];
            if (type == CellTypes.COMBO_BOX || type == CellTypes.COLORED_COMBO_BOX || type == CellTypes.BITFIELD_COMBO_BOX)
                installed.put(column, rendererItems(table.getColumnModel().getColumn(column).getCellRenderer()));
        }
        return installed;
    }

    // ---------------------------------------------------------------- properties

    @Test
    @DisplayName("every combo-box column holds the array at the queue position the reference mapping assigns it")
    void columnsHoldTheArrayTheirQueuePositionNames()
    {
        E_ProbeTable table = build(AWKWARD);
        Map<Integer, Integer> reference = referencePositions(AWKWARD);

        for (Map.Entry<Integer, Integer> entry : reference.entrySet())
        {
            int column = entry.getKey();
            int position = entry.getValue();
            if (AWKWARD[column] == CellTypes.CUSTOM)
                continue;

            // PROPERTY (positional protocol): the queue is consumed in column order, so the array
            // installed in column c must be the one supplied at position pos(c). The arrays are
            // self-identifying, so any other array here is proof of an off-by-N in the walk.
            assertThat(rendererItems(table.getColumnModel().getColumn(column).getCellRenderer()))
                    .as("renderer of column %d must hold queue element %d", column, position)
                    .containsExactly(E_ProbeTable.source(position));

            // PROPERTY (editor/renderer agreement): the editor offers the user a list of names and
            // the renderer turns the chosen index back into a name. If they disagree, choosing an
            // entry displays a different one, so both must be served the same queue position.
            assertThat(editorItems(table.getColumnModel().getColumn(column).getCellEditor()))
                    .as("editor of column %d must offer queue element %d", column, position)
                    .containsExactly(E_ProbeTable.source(position));
        }
    }

    @Test
    @DisplayName("the custom column is handed the triple starting at its reference position, in order")
    void customColumnReceivesItsTripleInOrder()
    {
        E_ProbeTable table = build(AWKWARD);
        int position = referencePositions(AWKWARD).get(4);

        // PROPERTY: a custom column claims three consecutive positions (species, item, move) and
        // must receive them in that order - the supplier's three parameters are not interchangeable,
        // so a rotation of them is as wrong as reading the wrong block entirely.
        assertThat(supplier.rendererCalls()).hasSize(1);
        assertThat(supplier.rendererCalls().get(0).length).isEqualTo(3);
        for (int slot = 0; slot < 3; slot++)
        {
            assertThat(supplier.rendererCalls().get(0)[slot])
                    .as("custom renderer argument %d must be queue element %d", slot, position + slot)
                    .containsExactly(E_ProbeTable.source(position + slot));
            assertThat(supplier.editorCalls().get(0)[slot])
                    .as("custom editor argument %d must be queue element %d", slot, position + slot)
                    .containsExactly(E_ProbeTable.source(position + slot));
        }

        // and the installed renderer really is the one built from that triple
        assertThat(table.getColumnModel().getColumn(4).getCellRenderer())
                .isInstanceOf(E_CustomCellSupplier.E_CustomRenderer.class);
    }

    @Test
    @DisplayName("two custom columns share one editor and one renderer, and claim only one triple")
    void customColumnsShareOneInstanceAndOneTriple()
    {
        E_ProbeTable table = build(AWKWARD);

        // PROPERTY: the supplier is consulted once per table, not once per custom column - that is
        // what the `customEditor == null` guard means. Consulting it twice would also draw a second
        // triple out of the queue and shift every later column's names by three.
        assertThat(supplier.editorCalls()).as("the custom supplier must be consulted exactly once").hasSize(1);
        assertThat(supplier.rendererCalls()).as("the custom supplier must be consulted exactly once").hasSize(1);

        assertThat(table.getColumnModel().getColumn(8).getCellEditor())
                .as("both custom columns must share one editor instance")
                .isSameAs(table.getColumnModel().getColumn(4).getCellEditor());
        assertThat(table.getColumnModel().getColumn(8).getCellRenderer())
                .as("both custom columns must share one renderer instance")
                .isSameAs(table.getColumnModel().getColumn(4).getCellRenderer());
    }

    @Test
    @DisplayName("the supplied queue is consumed exactly once over, with nothing left and nothing over-drawn")
    void queueIsConsumedExactlyOnce()
    {
        E_ProbeTable table = build(AWKWARD);
        int expected = referenceSourceCount(AWKWARD);

        // PROPERTY (totality): every element the concrete table supplies is claimed by exactly one
        // column. An element left over means some column read a neighbour's names; an extra removal
        // means the walk ran past the end of the supply.
        assertThat(table.lastQueue().removals())
                .as("the walk must draw exactly %d elements for this layout", expected)
                .isEqualTo(expected);
        assertThat(table.lastQueue())
                .as("no supplied name list may go unclaimed")
                .isEmpty();
    }

    @Test
    @DisplayName("resetIndexedCellRendererText is idempotent: every column keeps the array it already had")
    void resetIsIdempotent()
    {
        E_ProbeTable table = build(AWKWARD);
        Map<Integer, String[]> before = installedText(table, AWKWARD);

        table.resetIndexedCellRendererText();
        Map<Integer, String[]> after = installedText(table, AWKWARD);

        // PROPERTY (agreement of walkers): the text banks are unchanged between the two walks, so
        // the mapping from queue position to column must be the same function both times. This is
        // the direct expression of the desync bug: if the two walkers disagree at any column, the
        // refresh silently re-labels that column and every one after it.
        assertThat(after.keySet()).isEqualTo(before.keySet());
        for (int column : before.keySet())
        {
            assertThat(after.get(column))
                    .as("column %d must hold the same name list after a reset as before it", column)
                    .containsExactly(before.get(column));
        }
    }

    @Test
    @DisplayName("both walks draw the same number of elements from the queue")
    void bothWalksConsumeTheSameAmount()
    {
        E_ProbeTable table = build(AWKWARD);
        table.resetIndexedCellRendererText();

        assertThat(table.issuedQueues()).hasSize(2);

        // PROPERTY: the consumption count is a function of the column layout alone. Since the
        // layout does not change between the two walks, the two counts must be equal - a walker
        // which skips the custom column's triple (or claims one it should share) shows up here as
        // a difference of exactly three.
        assertThat(table.issuedQueues().get(1).removals())
                .as("the refresh walk must consume exactly as much as the install walk")
                .isEqualTo(table.issuedQueues().get(0).removals());
        assertThat(table.issuedQueues().get(1))
                .as("the refresh walk must leave nothing unclaimed either")
                .isEmpty();
    }

    @Test
    @DisplayName("repeated resets are a fixed point")
    void repeatedResetsAreAFixedPoint()
    {
        E_ProbeTable table = build(AWKWARD);
        Map<Integer, String[]> reference = installedText(table, AWKWARD);

        for (int iteration = 1; iteration <= 3; iteration++)
        {
            table.resetIndexedCellRendererText();
            Map<Integer, String[]> current = installedText(table, AWKWARD);
            for (int column : reference.keySet())
            {
                // PROPERTY (idempotence, f(f(x)) = f(x)): refreshing text from unchanged banks is
                // an idempotent operation. If it were not, the sheet's labels would drift further
                // every time the user edited a text bank.
                assertThat(current.get(column))
                        .as("column %d after reset #%d", column, iteration)
                        .containsExactly(reference.get(column));
            }
        }
    }

    @Test
    @DisplayName("a reset does not disturb the custom columns")
    void resetLeavesCustomColumnsAlone()
    {
        E_ProbeTable table = build(AWKWARD);
        TableCellRenderer customRendererBefore = table.getColumnModel().getColumn(4).getCellRenderer();
        TableCellEditor customEditorBefore = table.getColumnModel().getColumn(4).getCellEditor();

        table.resetIndexedCellRendererText();

        // PROPERTY (locality): a refresh of indexed text touches only the columns whose text it
        // refreshes. Silently replacing the shared custom instance would break the sharing the
        // previous property established.
        assertThat(table.getColumnModel().getColumn(4).getCellRenderer()).isSameAs(customRendererBefore);
        assertThat(table.getColumnModel().getColumn(4).getCellEditor()).isSameAs(customEditorBefore);
        assertThat(table.getColumnModel().getColumn(8).getCellRenderer()).isSameAs(customRendererBefore);
    }

    @Test
    @DisplayName("a layout with no custom column still maps every combo column to its own position")
    void layoutWithoutCustomColumnStillAgrees()
    {
        CellTypes[] plain = {
                CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX,
                CellTypes.CHECKBOX, CellTypes.BITFIELD_COMBO_BOX
        };
        E_ProbeTable table = build(plain);
        Map<Integer, Integer> reference = referencePositions(plain);

        // PROPERTY: the mapping is defined by the column types alone, so removing the custom column
        // from the layout must simply remove its block from the numbering - nothing else moves.
        for (Map.Entry<Integer, Integer> entry : reference.entrySet())
        {
            assertThat(rendererItems(table.getColumnModel().getColumn(entry.getKey()).getCellRenderer()))
                    .as("column %d must hold queue element %d", entry.getKey(), entry.getValue())
                    .containsExactly(E_ProbeTable.source(entry.getValue()));
        }
        assertThat(table.lastQueue()).isEmpty();
    }

    @Test
    @DisplayName("a null element in the queue degrades to the documented empty fallback, never to null")
    void nullElementFallsBackToTheDocumentedEmptyList()
    {
        int position = referencePositions(AWKWARD).get(1);
        E_ProbeTable.nullPosition = position;
        E_ProbeTable table = build(AWKWARD);

        // PROPERTY (no silent nulls): a name list is dereferenced on every paint, so the absence of
        // a source has to become a well-formed empty list at the point of installation rather than
        // a null which only fails later, on the painting thread, with no indication of the cause.
        // `getTextFromSource` documents `new String[]{""}` as that fallback.
        String[] installed = rendererItems(table.getColumnModel().getColumn(1).getCellRenderer());
        assertThat(installed).as("a missing name list must not be installed as null").isNotNull();
        assertThat(installed).as("the documented fallback for a missing name list").containsExactly("");

        String[] offered = editorItems(table.getColumnModel().getColumn(1).getCellEditor());
        assertThat(offered).as("the editor must receive the same fallback").containsExactly("");
    }

    @Test
    @DisplayName("a queue shorter than the layout needs must fail with a diagnosable message")
    void shortQueueFailsDiagnosably()
    {
        int needed = referenceSourceCount(AWKWARD);

        // PROPERTY (diagnosability): a supply/demand mismatch is a programming error in a concrete
        // table, and the only way it is ever noticed is the message it produces. A throw carrying no
        // message tells a maintainer neither which table nor which column ran out, so the failure is
        // indistinguishable from any other empty-collection bug in the process.
        assertThatThrownBy(() -> build(AWKWARD, needed - 1))
                .as("running out of text sources must say so")
                .isInstanceOf(RuntimeException.class)
                .satisfies(thrown -> assertThat(thrown.getMessage())
                        .as("the failure must carry a message naming the exhausted text-source queue")
                        .isNotNull()
                        .isNotBlank());
    }
}
