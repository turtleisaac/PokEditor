package io.github.turtleisaac.pokeditor.gui.editors.data;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.E_Entry;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * {@code DefaultDataEditor} is the base every non-sheet editor extends. It is a tiny class, and
 * almost all of it is contract rather than behaviour:
 *
 * <ul>
 *   <li><b>A selection sentinel.</b> {@code selectedIndex} starts at {@code -1}, which is not an
 *       index - it is the encoding of "nothing is selected". The distinction matters because every
 *       other method in the class indexes the entry list with it: a class which started at 0 would
 *       address entry 0 (a real, editable Pokemon/script/sprite) before the user had chosen
 *       anything, and a copy or a paste would silently hit the wrong entry.</li>
 *   <li><b>Copy and paste are inverses.</b> {@code applyCopiedEntry(writeSelectedEntryForCopy())}
 *       is the identity on the selected entry - that is what makes it a copy rather than a
 *       transform - and it is a no-op on every other entry.</li>
 *   <li><b>Totality.</b> {@code getEnabledToolbarButtons()} is iterated by the panel without a null
 *       check, and {@code getPanel()} documents null as its own "not attached yet" answer.</li>
 * </ul>
 *
 * {@code DefaultDataEditorPanel} needs a {@code PokeditorManager} and therefore a real ROM, so it
 * is out of reach here; {@code EditorDataModel} is an interface, which is what makes the editor
 * itself fully constructible headlessly.
 */
public class DataEditorContractTest
{
    private static final int ENTRY_WIDTH = 3;
    private static final int ENTRY_COUNT = 4;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static List<E_Entry> entries()
    {
        List<E_Entry> list = new ArrayList<>();
        for (int index = 0; index < ENTRY_COUNT; index++)
        {
            E_Entry entry = new E_Entry(ENTRY_WIDTH);
            for (int cell = 0; cell < ENTRY_WIDTH; cell++)
                entry.set(cell, 100 * (index + 1) + cell);
            list.add(entry);
        }
        return list;
    }

    private static List<String> payloads(List<E_Entry> data)
    {
        List<String> snapshot = new ArrayList<>();
        for (E_Entry entry : data)
            snapshot.add(Arrays.toString(entry.snapshot()));
        return snapshot;
    }

    // ---------------------------------------------------------------- selection state machine

    @Test
    @DisplayName("selectedIndex starts at the no-selection sentinel, not at entry 0")
    void selectedIndexStartsAtTheSentinel()
    {
        E_Editor editor = new E_Editor(new E_FormatModel(entries()));

        // PROPERTY: "no entry chosen" is a distinct state from "entry 0 chosen", and the class has
        // to be able to represent it. -1 is the sentinel this class picks; the requirement is only
        // that the initial value is NOT a valid index into a non-empty entry list, because a valid
        // one would make the two states indistinguishable and let a copy/paste address a real entry
        // the user never selected.
        assertThat(editor.getSelectedIndex() >= 0 && editor.getSelectedIndex() < editor.getModel().getEntryCount())
                .as("the initial selection must not be a valid entry index")
                .isFalse();
        assertThat(editor.getSelectedIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("selection tracks the last change and is idempotent")
    void selectionTracksTheLastChange()
    {
        E_Editor editor = new E_Editor(new E_FormatModel(entries()));

        editor.selectedIndexedChanged(2, null);
        assertThat(editor.getSelectedIndex()).isEqualTo(2);

        // PROPERTY (idempotence): selecting what is already selected is a no-op - the state is the
        // selection itself, not a history of selections.
        editor.selectedIndexedChanged(2, null);
        assertThat(editor.getSelectedIndex()).isEqualTo(2);

        // PROPERTY (last-write-wins): the selection is a single value, so a sequence of changes
        // leaves exactly the last one in effect.
        editor.selectedIndexedChanged(0, null);
        editor.selectedIndexedChanged(3, null);
        assertThat(editor.getSelectedIndex()).isEqualTo(3);
    }

    // ---------------------------------------------------------------- panel attachment

    @Test
    @DisplayName("getPanel() is null until a panel is attached")
    void panelIsNullUntilAttached()
    {
        E_Editor editor = new E_Editor(new E_FormatModel(entries()));

        // PROPERTY: the Javadoc defines null as "this editor has not been added to a panel". A
        // freshly constructed editor has not been, so null is the only answer consistent with it.
        assertThat(editor.getPanel()).isNull();
    }

    @Test
    @DisplayName("getEnabledToolbarButtons() is never null")
    void enabledToolbarButtonsIsTotal()
    {
        E_Editor editor = new E_Editor(new E_FormatModel(entries()));

        // PROPERTY (totality of the contract): DefaultDataEditorPanel calls
        // enabledToolbarButtons.contains(..) six times in a row with no null check, so the return
        // value is required to be a set - possibly empty, never absent. "No buttons" is the empty
        // set; null is not a value in the codomain.
        Set<DefaultDataEditorPanel.DataEditorButtons> buttons = editor.getEnabledToolbarButtons();
        assertThat(buttons).isNotNull();
        assertThatCode(() -> buttons.contains(DefaultDataEditorPanel.DataEditorButtons.ADD_ENTRY))
                .doesNotThrowAnyException();
    }

    // ---------------------------------------------------------------- model type discrimination

    @Test
    @DisplayName("copying from a non-FormatModel editor yields null")
    void copyFromNonFormatModelYieldsNull()
    {
        E_Editor editor = new E_Editor(new E_PlainModel(ENTRY_COUNT));
        editor.selectedIndexedChanged(1, null);

        // PROPERTY: the copy path is defined only for models which expose a GenericFileData list.
        // For any other model there is nothing to serialise, so the answer is "no clipboard
        // content" - null - rather than a partially formed container.
        assertThat(editor.writeSelectedEntryForCopy()).isNull();
    }

    @Test
    @DisplayName("pasting into a non-FormatModel editor changes nothing")
    void pasteIntoNonFormatModelIsANoOp()
    {
        E_PlainModel model = new E_PlainModel(ENTRY_COUNT);
        E_Editor editor = new E_Editor(model);
        editor.selectedIndexedChanged(1, null);
        List<String> before = model.snapshot();

        // PROPERTY (all-or-nothing): the same discrimination has to govern both directions. If a
        // model cannot be copied FROM, it cannot be pasted INTO either, and the paste must leave it
        // exactly as it was - a partial write here would be a corruption the user cannot see.
        assertThatCode(() -> editor.applyCopiedEntry(new BytesDataContainer()))
                .doesNotThrowAnyException();
        assertThat(model.snapshot()).isEqualTo(before);
    }

    // ---------------------------------------------------------------- copy/paste round trip

    @Test
    @DisplayName("applyCopiedEntry(writeSelectedEntryForCopy()) is the identity on the selected entry")
    void copyPasteRoundTripIsTheIdentity()
    {
        List<E_Entry> data = entries();
        E_Editor editor = new E_Editor(new E_FormatModel(data));
        editor.selectedIndexedChanged(2, null);

        List<String> original = payloads(data);
        BytesDataContainer copied = editor.writeSelectedEntryForCopy();

        // scribble over the selected entry so that "restored" cannot be confused with "never touched"
        data.get(2).set(0, 999);
        assertThat(payloads(data)).isNotEqualTo(original);

        editor.applyCopiedEntry(copied);

        // PROPERTY (inverse pair): save and setData are inverses on the format, so copy followed by
        // paste into the same entry restores exactly the bytes that were read. Anything else means
        // the clipboard round trip is lossy, and the user's "copy this Pokemon" quietly edits it.
        assertThat(payloads(data))
                .as("copy then paste into the same entry must restore it exactly")
                .isEqualTo(original);
    }

    @Test
    @DisplayName("pasting writes the selected entry and no other")
    void pasteIsLocalToTheSelectedEntry()
    {
        List<E_Entry> data = entries();
        E_Editor editor = new E_Editor(new E_FormatModel(data));

        editor.selectedIndexedChanged(1, null);
        BytesDataContainer copied = editor.writeSelectedEntryForCopy();
        String sourcePayload = Arrays.toString(data.get(1).snapshot());

        List<String> before = payloads(data);
        editor.selectedIndexedChanged(3, null);
        editor.applyCopiedEntry(copied);

        // PROPERTY (locality): a paste is an assignment to one entry. Entry 3 must become equal to
        // entry 1, and entries 0, 1 and 2 must be fixed points of the operation - the same
        // injectivity requirement a cell write in a sheet has to satisfy.
        assertThat(Arrays.toString(data.get(3).snapshot()))
                .as("the pasted-into entry must equal the copied-from entry")
                .isEqualTo(sourcePayload);
        for (int index : new int[] {0, 1, 2})
        {
            assertThat(payloads(data).get(index))
                    .as("entry %d must be untouched by a paste into entry 3", index)
                    .isEqualTo(before.get(index));
        }
    }

    // ---------------------------------------------------------------- no selection

    @Test
    @DisplayName("copying with nothing selected yields no clipboard content rather than an index error")
    void copyWithNoSelectionYieldsNull()
    {
        List<E_Entry> data = entries();
        E_Editor editor = new E_Editor(new E_FormatModel(data));

        // PROPERTY: the sentinel exists precisely so that "nothing selected" can be handled. The
        // copy path already has a well-defined answer for "there is nothing to copy" - it returns
        // null when the model is the wrong kind - and "no entry is selected" is the same situation
        // reached by a different route, so it must produce the same answer. Instead the sentinel is
        // fed straight to getData().get(-1), which raises a raw IndexOutOfBoundsException at the
        // user from a toolbar button they are allowed to press at any time.
        assertThat(editor.writeSelectedEntryForCopy())
                .as("copying with no selection must yield no clipboard content")
                .isNull();
    }

    @Test
    @DisplayName("pasting with nothing selected changes nothing and raises nothing")
    void pasteWithNoSelectionIsANoOp()
    {
        List<E_Entry> data = entries();
        E_Editor editor = new E_Editor(new E_FormatModel(data));
        List<String> before = payloads(data);

        // PROPERTY: with no entry selected there is no destination, so the paste has nothing to do
        // and must do nothing. It must in particular not throw: applyCopiedEntry's own catch block
        // reports failures with a hard-coded JOptionPane.showMessageDialog, which in a headless JVM
        // throws HeadlessException from inside the catch - so the sentinel's IndexOutOfBoundsException
        // is not contained, it is merely exchanged for a different escaping exception. (That the
        // error path cannot be exercised at all without a display is itself a testability defect:
        // the dialog is hard-coded rather than delegated to an injectable error reporter.)
        assertThatCode(() -> editor.applyCopiedEntry(new BytesDataContainer()))
                .as("pasting with no selection must be a no-op")
                .doesNotThrowAnyException();
        assertThat(payloads(data)).isEqualTo(before);
    }

    // ---------------------------------------------------------------- model swap

    @Test
    @DisplayName("setModel replaces the model the copy path discriminates on")
    void setModelReplacesTheModel()
    {
        E_PlainModel plain = new E_PlainModel(ENTRY_COUNT);
        E_Editor editor = new E_Editor(plain);
        assertThat(editor.getModel()).isSameAs(plain);

        // PROPERTY: getModel/setModel are a plain get/set pair, so get after set returns what was
        // set. The copy path branches on the model's runtime type, so a stale model here would send
        // copies to a list the editor is no longer showing.
        E_FormatModel replacement = new E_FormatModel(entries());
        editor.setModel(replacement);
        assertThat(editor.getModel()).isSameAs(replacement);

        editor.selectedIndexedChanged(0, null);
        assertThat(editor.writeSelectedEntryForCopy())
                .as("after swapping in a FormatModel the copy path must use it")
                .isNotNull();
    }

    // ---------------------------------------------------------------- doubles

    /** the property enum a real editor's model is parameterised by; this double needs only one */
    public enum E_Property
    {
        VALUE
    }

    /** a concrete editor: the class under test is abstract in exactly two methods, both trivial */
    static class E_Editor extends DefaultDataEditor<E_Entry, E_Property>
    {
        E_Editor(EditorDataModel<E_Property> model)
        {
            super(model);
        }

        @Override
        public Class<E_Entry> getDataClass()
        {
            return E_Entry.class;
        }

        @Override
        public Set<DefaultDataEditorPanel.DataEditorButtons> getEnabledToolbarButtons()
        {
            return Set.of(DefaultDataEditorPanel.DataEditorButtons.COPY_ENTRY,
                    DefaultDataEditorPanel.DataEditorButtons.PASTE_ENTRY);
        }
    }

    /** an {@link EditorDataModel} which is deliberately NOT a {@code FormatModel} */
    static class E_PlainModel implements EditorDataModel<E_Property>
    {
        private final int[] values;

        E_PlainModel(int entryCount)
        {
            this.values = new int[entryCount];
            for (int index = 0; index < entryCount; index++)
                values[index] = index * 7;
        }

        List<String> snapshot()
        {
            List<String> flat = new ArrayList<>();
            for (int value : values)
                flat.add(String.valueOf(value));
            return flat;
        }

        @Override
        public Object getValueFor(int entryIdx, E_Property property)
        {
            return values[entryIdx];
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, E_Property property)
        {
            values[entryIdx] = Integer.parseInt(String.valueOf(aValue));
        }

        @Override
        public int getEntryCount()
        {
            return values.length;
        }

        @Override
        public String getEntryName(int entryIdx)
        {
            return "entry " + entryIdx;
        }
    }

    /** the minimum {@code FormatModel} the copy path needs: a list of GenericFileData entries */
    static class E_FormatModel extends FormatModel<E_Entry, E_Property>
    {
        E_FormatModel(List<E_Entry> data)
        {
            super(data, Collections.<TextBankData>emptyList());
        }

        @Override
        public int getColumnCount()
        {
            return 1;
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return "id";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return getData().get(rowIndex).get(0);
        }

        @Override
        public Object getValueFor(int entryIdx, E_Property property)
        {
            return getData().get(entryIdx).get(0);
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, E_Property property)
        {
            getData().get(entryIdx).set(0, Integer.parseInt(String.valueOf(aValue)));
        }

        @Override
        public FormatModel<E_Entry, E_Property> getFrozenColumnModel()
        {
            return null;
        }
    }
}
