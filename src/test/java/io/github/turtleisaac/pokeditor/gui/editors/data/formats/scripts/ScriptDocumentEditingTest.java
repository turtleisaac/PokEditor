package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.Fixture;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.text.BadLocationException;
import java.util.List;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptDocumentHighlightingTest.assertRangesDoNotCross;
import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptDocumentHighlightingTest.assertRangesLieInsideDocument;
import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Edits are where a highlighter's offset arithmetic breaks, because every stored offset was
 * computed against the text as it was before the edit. The first group of tests walks the
 * boundaries (offset 0, offset getLength(), empty edits, deleting everything); the second checks
 * that the range set never survives an edit in a state that describes offsets the document no
 * longer has.
 */
class ScriptDocumentEditingTest
{
    private static final String SCRIPT = "WaitTime 5 0\nEnd\nEnd\n";

    private ScriptDocument documentContaining(String text) throws BadLocationException
    {
        ScriptDocument document = newQuiescedDocument();
        document.insertString(0, text, null);
        document.setSyntaxAttributes();
        return document;
    }

    private void assertStillConsistent(ScriptDocument document) throws BadLocationException
    {
        document.setSyntaxAttributes();
        assertRangesLieInsideDocument(document);
        assertRangesDoNotCross(document);
    }

    @Test
    @DisplayName("inserting at offset 0 keeps the text and the ranges consistent")
    void insertAtStart() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.insertString(0, "End\n", null)).doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo("End\n" + SCRIPT);
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("inserting at getLength() keeps the text and the ranges consistent")
    void insertAtEnd() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.insertString(document.getLength(), "End\n", null))
                .doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT + "End\n");
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("inserting in the middle keeps the text and the ranges consistent")
    void insertInTheMiddle() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);
        int middle = SCRIPT.length() / 2;

        assertThatCode(() -> document.insertString(middle, "End\n", null)).doesNotThrowAnyException();

        assertThat(textOf(document))
                .isEqualTo(SCRIPT.substring(0, middle) + "End\n" + SCRIPT.substring(middle));
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("inserting an empty string is a no-op")
    void insertEmptyString() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);
        List<Interval> before = intervalsOf(document);

        assertThatCode(() -> document.insertString(0, "", null)).doesNotThrowAnyException();
        assertThatCode(() -> document.insertString(document.getLength(), "", null))
                .doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT);
        assertThat(intervalsOf(document)).isEqualTo(before);
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("inserting a null string is a no-op")
    void insertNullString() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.insertString(0, null, null)).doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT);
    }

    @Test
    @DisplayName("removing zero characters is a no-op, at either end")
    void removeNothing() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);
        List<Interval> before = intervalsOf(document);

        assertThatCode(() -> document.remove(0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> document.remove(document.getLength(), 0)).doesNotThrowAnyException();
        assertThatCode(() -> document.remove(document.getLength() / 2, 0)).doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT);
        assertThat(intervalsOf(document)).isEqualTo(before);
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("removing the first character keeps the text and the ranges consistent")
    void removeFirstCharacter() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.remove(0, 1)).doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT.substring(1));
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("removing the last character keeps the text and the ranges consistent")
    void removeLastCharacter() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.remove(document.getLength() - 1, 1)).doesNotThrowAnyException();

        assertThat(textOf(document)).isEqualTo(SCRIPT.substring(0, SCRIPT.length() - 1));
        assertStillConsistent(document);
    }

    @Test
    @DisplayName("removing the entire document leaves an empty document with no ranges")
    void removeEverything() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);

        assertThatCode(() -> document.remove(0, document.getLength())).doesNotThrowAnyException();

        assertThat(document.getLength()).isZero();
        assertThat(textOf(document)).isEmpty();
        assertStillConsistent(document);
        assertThat(intervalsOf(document)).isEmpty();
    }

    @Test
    @DisplayName("a long sequence of boundary edits never throws and never corrupts the text")
    void manyBoundaryEditsInSequence() throws BadLocationException
    {
        ScriptDocument document = documentContaining(SCRIPT);
        StringBuilder expected = new StringBuilder(SCRIPT);

        for (int i = 0; i < 40; i++)
        {
            document.insertString(0, "A", null);
            expected.insert(0, "A");
            document.insertString(document.getLength(), "B\n", null);
            expected.append("B\n");
            int middle = document.getLength() / 2;
            document.insertString(middle, "C", null);
            expected.insert(middle, "C");
            document.remove(0, 1);
            expected.deleteCharAt(0);
            document.remove(document.getLength() - 1, 1);
            expected.deleteCharAt(expected.length() - 1);

            document.setSyntaxAttributes();
            assertThat(textOf(document)).isEqualTo(expected.toString());
            assertRangesLieInsideDocument(document);
            assertRangesDoNotCross(document);
        }
    }

    @Test
    @DisplayName("after text is deleted, no range still points past the end of the document")
    void rangesNeverOutliveTheTextTheyDescribe() throws BadLocationException
    {
        // Between an edit and the debounced re-highlight, ScriptPane still answers hovers and
        // ctrl-clicks from this range set: it feeds getMin()/getMaxExclusive() straight into
        // setCharacterAttributes and into String.substring on the current text. A range left
        // pointing at offsets the document no longer has is a crash in that window, not a
        // cosmetic staleness.
        ScriptDocument document = documentContaining(SCRIPT);
        assertThat(intervalsOf(document)).isNotEmpty();

        document.remove(0, document.getLength() - 2);

        assertRangesLieInsideDocument(document);
    }

    @Test
    @DisplayName("after text is deleted, an element found by lookup can still be read as text")
    void lookupAfterDeletionYieldsAUsableElement() throws BadLocationException
    {
        // This is the concrete failure the previous property guards against, spelled out: the
        // element the pane would act on for a click at offset 0.
        ScriptDocument document = documentContaining(SCRIPT);
        document.remove(0, document.getLength() - 2);

        ScriptDocument.ElementRange found = document.getScriptElementList().find(0);
        if (found == null)
            return; // nothing to act on is a perfectly correct answer

        String text = textOf(document);
        assertThat(found.getMaxExclusive())
                .as("lookup returned %s for a %d character document; ScriptPane would call "
                                + "text.substring(%d, %d) on it",
                        Interval.of(found), text.length(), found.getMin(), found.getMaxExclusive())
                .isLessThanOrEqualTo(text.length());
    }

    @Test
    @DisplayName("an edit eventually causes the document to re-highlight on its own")
    void editsTriggerADebouncedRehighlight() throws Exception
    {
        // The debounce exists so the file is not re-lexed twice per keystroke on the EDT. It still
        // has to actually fire, or the colours simply stop tracking the text.
        Fixture fixture = newLiveFixture();
        ScriptDocument document = fixture.document();

        document.insertString(0, SCRIPT, null);

        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline && intervalsOf(document).isEmpty())
            Thread.sleep(20);

        assertThat(intervalsOf(document))
                .as("the debounced highlighting pass never ran")
                .isNotEmpty();
        assertRangesLieInsideDocument(document);
        assertRangesDoNotCross(document);
    }

    @Test
    @DisplayName("the debounced pass also brings the line number gutter back in step")
    void debouncedPassUpdatesTheGutter() throws Exception
    {
        Fixture fixture = newLiveFixture();
        ScriptDocument document = fixture.document();
        String text = "End\nEnd\nEnd\n";

        document.insertString(0, text, null);

        int expected = lineCountOf(text);
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline && gutterEntries(fixture.gutter()).size() != expected)
            Thread.sleep(20);

        assertThat(gutterEntries(fixture.gutter())).hasSize(expected);
    }
}
