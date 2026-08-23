package io.github.turtleisaac.pokeditor.gui;

import io.github.turtleisaac.pokeditor.gui.EditorComboBox.ComboBoxItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.DefaultComboBoxModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link EditorComboBox}.
 *
 * <p>THEORY. A combo box built from a list of names is an order-preserving bijection between the
 * index range {@code [0, n)} and the supplied names: index i must display name i, and the selection
 * accessors must be mutual inverses of each other ({@code getSelectedIndex()} and
 * {@code getSelectedItem()} always describe the same element). Every editor in this application
 * converts a user-visible name back into a numeric id through exactly this mapping, so an
 * off-by-one, a silent de-duplication or a dropped entry writes the wrong id into the ROM.
 */
public class EditorComboBoxTest
{
    private static final String[] NAMES = {"Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon"};

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("the model is exactly the supplied list, in order, with duplicates kept")
    void modelMirrorsTheSuppliedList()
    {
        String[] items = {"Alpha", "Beta", "Alpha", "", "Gamma"};
        EditorComboBox box = new EditorComboBox(items);

        // Multiplicity and order are part of the mapping: entry i of the ROM's name table is at
        // index i. De-duplicating "Alpha" or dropping the empty name would shift every later id.
        assertThat(box.getItemCount()).isEqualTo(items.length);
        for (int i = 0; i < items.length; i++)
            assertThat(box.getItemAt(i).toString()).as("index %d", i).isEqualTo(items[i]);
    }

    @Test
    @DisplayName("index and value are mutual inverses across the whole range, ends included")
    void indexAndValueAreMutualInverses()
    {
        EditorComboBox box = new EditorComboBox(NAMES);

        for (int i = 0; i < NAMES.length; i++)
        {
            box.setSelectedIndex(i);
            // index -> value -> index must be the identity on [0, n), including the two ends,
            // which is where an off-by-one in a name/id map first shows up.
            assertThat(box.getSelectedIndex()).as("round trip through index %d", i).isEqualTo(i);
            assertThat(box.getSelectedItem()).isSameAs(box.getItemAt(i));
            assertThat(box.getSelectedItem().toString()).isEqualTo(NAMES[i]);
        }

        for (int i = 0; i < NAMES.length; i++)
        {
            box.setSelectedItem(box.getItemAt(i));
            // value -> index -> value is the other half of the bijection.
            assertThat(box.getSelectedIndex()).as("round trip through item %d", i).isEqualTo(i);
        }
    }

    @Test
    @DisplayName("a non-empty list starts with its first entry selected")
    void constructionSelectsTheFirstEntry()
    {
        // JComboBox(E[]) is specified to select the first item; editors rely on the component
        // never starting in an indeterminate state.
        EditorComboBox box = new EditorComboBox(NAMES);
        assertThat(box.getSelectedIndex()).isZero();
        assertThat(box.getSelectedItem()).isSameAs(box.getItemAt(0));
    }

    @Test
    @DisplayName("an out-of-range index is rejected and leaves the selection intact")
    void outOfRangeSelectionIsRejectedWithoutCorruptingState()
    {
        EditorComboBox box = new EditorComboBox(NAMES);
        box.setSelectedIndex(2);

        // JComboBox.setSelectedIndex is specified to throw IllegalArgumentException outside
        // [-1, itemCount); a clamp would silently write a neighbouring id instead.
        assertThatThrownBy(() -> box.setSelectedIndex(NAMES.length)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> box.setSelectedIndex(-2)).isInstanceOf(IllegalArgumentException.class);

        // Failure atomicity: a rejected command leaves the component readable and unchanged.
        assertThat(box.getSelectedIndex()).isEqualTo(2);
        assertThat(box.getSelectedItem()).isSameAs(box.getItemAt(2));
    }

    @Test
    @DisplayName("index -1 is the documented 'no selection' sentinel")
    void minusOneClearsTheSelection()
    {
        EditorComboBox box = new EditorComboBox(NAMES);
        box.setSelectedIndex(-1);
        // -1 <-> null is the documented sentinel pair; both accessors must agree on it.
        assertThat(box.getSelectedIndex()).isEqualTo(-1);
        assertThat(box.getSelectedItem()).isNull();
    }

    @Test
    @DisplayName("an empty combo box is constructible and reads as unselected")
    void emptyComboBoxIsWellDefined()
    {
        assertThatCode(EditorComboBox::new).doesNotThrowAnyException();

        EditorComboBox empty = new EditorComboBox();
        // With an empty index range there is no valid index, so the sentinel is the only possible
        // answer, and reading it must not throw.
        assertThat(empty.getItemCount()).isZero();
        assertThat(empty.getSelectedIndex()).isEqualTo(-1);
        assertThat(empty.getSelectedItem()).isNull();

        EditorComboBox fromEmptyArray = new EditorComboBox(new String[0]);
        assertThat(fromEmptyArray.getItemCount()).isZero();
        assertThat(fromEmptyArray.getSelectedIndex()).isEqualTo(-1);
        assertThat(fromEmptyArray.getSelectedItem()).isNull();
    }

    @Test
    @DisplayName("all four constructors produce the same mapping for the same names")
    void allConstructorsAgreeOnTheMapping()
    {
        ComboBoxItem[] items = new ComboBoxItem[NAMES.length];
        for (int i = 0; i < NAMES.length; i++)
            items[i] = new ComboBoxItem(NAMES[i]);

        EditorComboBox fromStrings = new EditorComboBox(NAMES);
        EditorComboBox fromItems = new EditorComboBox(items);
        EditorComboBox fromModel = new EditorComboBox(new DefaultComboBoxModel<>(items));

        // The constructors are different spellings of the same function from names to indices;
        // they must not disagree, or the id a user picks depends on which overload built the box.
        for (int i = 0; i < NAMES.length; i++)
        {
            assertThat(fromItems.getItemAt(i).toString()).as("index %d", i).isEqualTo(fromStrings.getItemAt(i).toString());
            assertThat(fromModel.getItemAt(i).toString()).as("index %d", i).isEqualTo(fromStrings.getItemAt(i).toString());
        }
        assertThat(fromItems.getItemCount()).isEqualTo(NAMES.length);
        assertThat(fromModel.getItemCount()).isEqualTo(NAMES.length);
    }

    @Test
    @DisplayName("selection stays self-consistent when set by value")
    void selectionByValueKeepsIndexAndItemConsistent()
    {
        EditorComboBox box = new EditorComboBox(NAMES);

        // Selecting by value is how a table cell editor restores the current row's entry: it has
        // the displayed name, not the item instance. Whatever the model does with a value equal to
        // one of its entries, the two accessors must keep describing the same element - a non-null
        // selected item with index -1 says "the selection is not in the list", which no caller can
        // act on and which silently writes the wrong id back.
        box.setSelectedItem(new ComboBoxItem("Venusaur"));

        int index = box.getSelectedIndex();
        Object selected = box.getSelectedItem();
        if (selected == null)
            assertThat(index).isEqualTo(-1);
        else
        {
            assertThat(index).as("selected item <%s> must be locatable in the model", selected).isNotNegative();
            assertThat(box.getItemAt(index).toString()).isEqualTo(selected.toString());
        }
    }

    @Test
    @DisplayName("ComboBoxItem renders exactly the text it was built from")
    void comboBoxItemRendersItsValue()
    {
        // The rendered text is the only thing the user matches against, so it must be the identity
        // on the string it was constructed with.
        assertThat(new ComboBoxItem("Thunderbolt").toString()).isEqualTo("Thunderbolt");
        assertThat(new ComboBoxItem("").toString()).isEmpty();

        // The int constructor is the decimal rendering of the number, over the whole int range.
        for (int value : new int[] {0, 1, 9, 10, 151, -1, Integer.MAX_VALUE, Integer.MIN_VALUE})
            assertThat(new ComboBoxItem(value).toString()).as("value %d", value).isEqualTo(Integer.toString(value));
    }

    @Test
    @DisplayName("renaming an item is visible through the model at the same index")
    void renamingAnItemKeepsItsIndex()
    {
        EditorComboBox box = new EditorComboBox(NAMES);
        box.getItemAt(1).setName("Renamed");

        // A mutator/accessor round trip: the new name must be readable, and renaming must not
        // move the entry, since the index is the id.
        assertThat(box.getItemAt(1).toString()).isEqualTo("Renamed");
        assertThat(box.getItemCount()).isEqualTo(NAMES.length);
        assertThat(box.getItemAt(0).toString()).isEqualTo(NAMES[0]);
        assertThat(box.getItemAt(2).toString()).isEqualTo(NAMES[2]);
    }
}
