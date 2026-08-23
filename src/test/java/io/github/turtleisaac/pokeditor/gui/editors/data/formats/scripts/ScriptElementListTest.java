package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.newQuiescedDocument;
import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.intervalsOf;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ScriptDocument.ScriptElementList} is the lookup structure behind every tooltip and every
 * ctrl-click: the pane asks it which element sits under a caret offset. Its whole job is to answer
 * that question, so the properties worth pinning down are "the answer contains the offset asked
 * about" and "the answer is the most specific element there".
 */
class ScriptElementListTest
{
    private ScriptDocument document;
    private ScriptDocument.ScriptElementList list;

    @BeforeEach
    void setUp()
    {
        document = newQuiescedDocument();
        list = new ScriptDocument.ScriptElementList();
    }

    private ScriptDocument.ElementRange range(int min, int maxExclusive)
    {
        return document.new ElementRange(min, maxExclusive, min + ".." + maxExclusive);
    }

    @Test
    @DisplayName("an empty list finds nothing")
    void emptyListFindsNothing()
    {
        assertThat(list.find(0)).isNull();
        assertThat(list.find(17)).isNull();
    }

    @Test
    @DisplayName("find returns null for an offset no range covers")
    void findsNothingOutsideEveryRange()
    {
        list.add(range(5, 10));
        list.add(range(20, 25));

        assertThat(list.find(4)).as("before the first range").isNull();
        assertThat(list.find(10)).as("one past the end of the first range").isNull();
        assertThat(list.find(15)).as("in the gap").isNull();
        assertThat(list.find(25)).as("one past the end of the last range").isNull();
    }

    @Test
    @DisplayName("find never returns a range that does not contain the offset asked about")
    void findReturnsOnlyContainingRanges()
    {
        list.add(range(0, 4));
        list.add(range(4, 9));
        list.add(range(9, 30));
        list.add(range(12, 15));

        for (int offset = -2; offset < 35; offset++)
        {
            ScriptDocument.ElementRange found = list.find(offset);
            if (found != null)
            {
                assertThat(found.contains(offset))
                        .as("find(%d) returned [%d,%d)", offset, found.getMin(), found.getMaxExclusive())
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("every offset covered by some range is found")
    void everyCoveredOffsetIsFound()
    {
        list.add(range(3, 7));
        list.add(range(11, 12));

        for (int offset : new int[]{3, 4, 5, 6, 11})
        {
            assertThat(list.find(offset)).as("offset %d is covered", offset).isNotNull();
        }
    }

    @Test
    @DisplayName("when ranges nest, find returns the innermost one")
    void findReturnsTheInnermostRange()
    {
        // A command's tooltip covers the whole line; a variable parameter inside it has its own,
        // more specific tooltip. Hovering the parameter must show the parameter's tooltip, not the
        // command's, otherwise the more specific information is unreachable.
        ScriptDocument.ElementRange command = range(0, 17);
        ScriptDocument.ElementRange parameter = range(9, 14);
        list.add(command);
        list.add(parameter);

        assertThat(list.find(9)).isSameAs(parameter);
        assertThat(list.find(13)).isSameAs(parameter);
        assertThat(list.find(0)).isSameAs(command);
        assertThat(list.find(8)).isSameAs(command);
        assertThat(list.find(14)).isSameAs(command);
    }

    @Test
    @DisplayName("the innermost range wins regardless of the order the ranges were added in")
    void innermostWinsWhicheverOrderRangesArrive()
    {
        ScriptDocument.ElementRange command = range(0, 17);
        ScriptDocument.ElementRange parameter = range(9, 14);
        list.add(parameter);
        list.add(command);

        assertThat(list.find(11)).isSameAs(parameter);
    }

    @Test
    @DisplayName("adding one range stores exactly one range")
    void addStoresExactlyOneRange()
    {
        // Storing a range once per enclosing range makes the list grow with nesting depth and
        // makes the same element answer twice, which is a leak in a structure rebuilt on a timer.
        list.add(range(0, 20));
        assertThat(intervalsOf(list)).as("after one add").hasSize(1);

        list.add(range(0, 10));
        assertThat(intervalsOf(list)).as("after two adds").hasSize(2);

        list.add(range(2, 5));
        assertThat(intervalsOf(list)).as("after three adds").hasSize(3);
    }

    @Test
    @DisplayName("no range is stored twice")
    void noDuplicateRanges()
    {
        list.add(range(0, 20));
        list.add(range(0, 10));
        list.add(range(2, 5));

        assertThat(intervalsOf(list)).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("disjoint ranges are stored one for one")
    void disjointRangesAreStoredOneForOne()
    {
        list.add(range(0, 4));
        list.add(range(4, 9));
        list.add(range(9, 13));

        assertThat(intervalsOf(list)).hasSize(3);
    }

    @Test
    @DisplayName("clear empties the list so nothing can be found afterwards")
    void clearRemovesEverything()
    {
        list.add(range(0, 4));
        list.add(range(4, 9));

        list.clear();

        assertThat(intervalsOf(list)).isEmpty();
        assertThat(list.find(0)).isNull();
        assertThat(list.find(5)).isNull();
    }
}
