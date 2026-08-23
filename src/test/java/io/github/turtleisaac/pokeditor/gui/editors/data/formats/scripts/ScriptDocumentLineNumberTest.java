package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The gutter's contract is small and total: it shows one entry per line of the document, the first
 * of which is 1. It used to start at 0 and to be filled with a hardcoded 2000 entries, so a short
 * file was numbered past its own end and a long one ran out of numbers.
 */
class ScriptDocumentLineNumberTest
{
    /** Rebuilds the gutter for the document's current text through the public entry point. */
    private static List<String> gutterFor(String text)
    {
        Fixture fixture = newQuiescedFixture();
        try
        {
            fixture.document().insertString(0, text, null);
        }
        catch (BadLocationException e)
        {
            throw new AssertionError(e);
        }
        fixture.document().setLineNumberPane(fixture.gutter());
        return gutterEntries(fixture.gutter());
    }

    static Stream<String> documents()
    {
        return Stream.of(
                "",
                "End",
                "End\n",
                "End\nEnd",
                "End\nEnd\n",
                "\n",
                "\n\n\n",
                "a\nb\nc\nd\ne"
        );
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("documents")
    @DisplayName("there is exactly one gutter entry per line of the document")
    void oneEntryPerLine(String text)
    {
        // Lines are counted independently of Swing here: no newline is one line, and each newline
        // opens another (a trailing newline opens a final empty line, which an editor still shows).
        assertThat(gutterFor(text)).hasSize(lineCountOf(text));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("documents")
    @DisplayName("gutter entries are the consecutive integers 1..lineCount")
    void entriesAreOneBasedAndConsecutive(String text)
    {
        List<String> expected = new ArrayList<>();
        for (int line = 1; line <= lineCountOf(text); line++)
            expected.add(String.valueOf(line));

        assertThat(gutterFor(text)).isEqualTo(expected);
    }

    @Test
    @DisplayName("the first line of a document is line 1, never line 0")
    void firstLineIsOne()
    {
        // The whole point of a gutter is that the number beside a line is the number the user can
        // quote to someone else. Zero-based numbering makes every one of those references wrong.
        assertThat(gutterFor("")).first().isEqualTo("1");
        assertThat(gutterFor("End\n")).first().isEqualTo("1");
        assertThat(gutterFor("a\nb\nc\n")).first().isEqualTo("1");
    }

    @Test
    @DisplayName("an empty document still shows a single line numbered 1")
    void emptyDocumentHasOneLine()
    {
        assertThat(gutterFor("")).containsExactly("1");
    }

    @Test
    @DisplayName("a one line document shows exactly one number")
    void oneLineDocument()
    {
        assertThat(gutterFor("WaitTime 5 0")).containsExactly("1");
    }

    @Test
    @DisplayName("a two line document shows exactly two numbers")
    void twoLineDocument()
    {
        assertThat(gutterFor("WaitTime 5 0\nEnd")).containsExactly("1", "2");
    }

    @Test
    @DisplayName("the gutter is not truncated for documents of several thousand lines")
    void severalThousandLines()
    {
        // The old gutter stopped at a hardcoded 2000 entries, so everything below line 2000 in a
        // real script file was unnumbered.
        int lines = 5_000;
        String text = "WaitTime 5 0\n".repeat(lines - 1) + "End";

        List<String> entries = gutterFor(text);

        assertThat(entries).hasSize(lines);
        assertThat(entries.get(0)).isEqualTo("1");
        assertThat(entries.get(1_999)).isEqualTo("2000");
        assertThat(entries.get(2_000)).isEqualTo("2001");
        assertThat(entries.get(lines - 1)).isEqualTo(String.valueOf(lines));
    }

    @Test
    @DisplayName("the gutter never shows more numbers than the document has lines")
    void gutterIsNotPaddedBeyondTheDocument()
    {
        // Padding a short file out to a fixed length is what the hardcoded 2000 did.
        assertThat(gutterFor("End\n")).hasSize(2).doesNotContain("3", "2000");
    }

    @Test
    @DisplayName("the gutter tracks the document as lines are added and removed")
    void gutterFollowsEdits() throws BadLocationException
    {
        Fixture fixture = newQuiescedFixture();
        ScriptDocument document = fixture.document();

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 30; i++)
        {
            text.append("End\n");
            document.insertString(document.getLength(), "End\n", null);
            document.setLineNumberPane(fixture.gutter());
            assertThat(gutterEntries(fixture.gutter()))
                    .as("after appending line %d", i + 1)
                    .hasSize(lineCountOf(text.toString()));
        }

        while (document.getLength() > 0)
        {
            document.remove(document.getLength() - 1, 1);
            text.deleteCharAt(text.length() - 1);
            document.setLineNumberPane(fixture.gutter());
            assertThat(gutterEntries(fixture.gutter()))
                    .as("after deleting back to %d characters", document.getLength())
                    .hasSize(lineCountOf(text.toString()));
        }

        assertThat(gutterEntries(fixture.gutter())).containsExactly("1");
    }

    @Test
    @DisplayName("a document with no gutter attached highlights and edits without complaint")
    void noGutterAttached() throws BadLocationException
    {
        // ScriptPane.getLineNumberPane() is null until a gutter is installed, and the document is
        // constructed from it, so the null case is on the normal startup path.
        ScriptPane pane = new ScriptPane();
        ScriptDocument document = new ScriptDocument(pane);
        disarmDebounce(document);

        document.insertString(0, "End\nEnd\n", null);
        document.setSyntaxAttributes();

        assertThat(textOf(document)).isEqualTo("End\nEnd\n");
    }
}
