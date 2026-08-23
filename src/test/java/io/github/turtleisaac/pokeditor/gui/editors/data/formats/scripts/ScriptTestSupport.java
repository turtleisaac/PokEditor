package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared fixtures for the script editor tests.
 *
 * <p>Two seams are needed and neither exists as public API, so both are reached by reflection
 * rather than by widening visibility in src/main:</p>
 * <ul>
 *     <li>{@code ScriptDocument.syntaxTimer} - the 250ms debounce that re-highlights after an
 *         edit. It fires on the EDT, so unless it is neutered every assertion about the range
 *         set races a background re-lex of the same document.</li>
 *     <li>{@code ScriptElementList.elementRanges} - the range set itself. The only public
 *         accessor is {@code find(int)}, which cannot show duplicates, ordering or ranges that
 *         point outside the document.</li>
 * </ul>
 */
final class ScriptTestSupport
{
    static
    {
        // Swing is only constructible in this environment when AWT is told there is no display.
        System.setProperty("java.awt.headless", "true");
    }

    private ScriptTestSupport() {}

    /** A document plus the panes it is wired to, so tests can reach the line-number gutter. */
    record Fixture(ScriptPane pane, JTextPane gutter, ScriptDocument document) {}

    /**
     * Builds a document whose debounce timer has been disarmed. Every test that asserts something
     * about the range set must use this: the timer would otherwise re-run the visitor on the EDT
     * concurrently with the test thread, and the two runs share one un-synchronized ArrayList.
     */
    static Fixture newQuiescedFixture()
    {
        Fixture fixture = newLiveFixture();
        disarmDebounce(fixture.document());
        return fixture;
    }

    /** Builds a document with the real debounce timer still running. */
    static Fixture newLiveFixture()
    {
        try
        {
            ScriptPane pane = new ScriptPane();
            JTextPane gutter = new JTextPane();
            pane.setLineNumberPane(gutter);
            ScriptDocument document = new ScriptDocument(pane);
            return new Fixture(pane, gutter, document);
        }
        catch (BadLocationException e)
        {
            throw new AssertionError("failed to build a script document fixture", e);
        }
    }

    static ScriptDocument newQuiescedDocument()
    {
        return newQuiescedFixture().document();
    }

    static void disarmDebounce(ScriptDocument document)
    {
        try
        {
            Field field = ScriptDocument.class.getDeclaredField("syntaxTimer");
            field.setAccessible(true);
            Timer timer = (Timer) field.get(document);
            timer.stop();
            for (ActionListener listener : timer.getActionListeners())
                timer.removeActionListener(listener);
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not disarm the syntax debounce timer", e);
        }
    }

    /** The document's whole text, which is what every offset in a range is an index into. */
    static String textOf(ScriptDocument document)
    {
        try
        {
            return document.getText(0, document.getLength());
        }
        catch (BadLocationException e)
        {
            throw new AssertionError("getText(0, getLength()) must always be legal", e);
        }
    }

    /** Snapshot of the range set. Reflection: {@code elementRanges} has no accessor. */
    @SuppressWarnings("unchecked")
    static List<ScriptDocument.ElementRange> rangesOf(ScriptDocument document)
    {
        return rangesOf(document.getScriptElementList());
    }

    @SuppressWarnings("unchecked")
    static List<ScriptDocument.ElementRange> rangesOf(ScriptDocument.ScriptElementList list)
    {
        try
        {
            Field field = ScriptDocument.ScriptElementList.class.getDeclaredField("elementRanges");
            field.setAccessible(true);
            return new ArrayList<>((List<ScriptDocument.ElementRange>) field.get(list));
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not read the element range set", e);
        }
    }

    /** Half-open interval, so failure messages read as intervals rather than as object ids. */
    record Interval(int min, int maxExclusive)
    {
        static Interval of(ScriptDocument.ElementRange range)
        {
            return new Interval(range.getMin(), range.getMaxExclusive());
        }

        boolean disjointFrom(Interval other)
        {
            return maxExclusive <= other.min || other.maxExclusive <= min;
        }

        boolean encloses(Interval other)
        {
            return min <= other.min && other.maxExclusive <= maxExclusive;
        }

        int length()
        {
            return maxExclusive - min;
        }

        @Override
        public String toString()
        {
            return "[" + min + "," + maxExclusive + ")";
        }
    }

    static List<Interval> intervalsOf(ScriptDocument document)
    {
        return toIntervals(rangesOf(document));
    }

    static List<Interval> intervalsOf(ScriptDocument.ScriptElementList list)
    {
        return toIntervals(rangesOf(list));
    }

    private static List<Interval> toIntervals(List<ScriptDocument.ElementRange> ranges)
    {
        List<Interval> intervals = new ArrayList<>();
        for (ScriptDocument.ElementRange range : ranges)
            intervals.add(Interval.of(range));
        return intervals;
    }

    /** Invokes {@code ScriptPane.highlight(int)}, which is private but is the styling path
     *  a ctrl-click takes. Driving it through a real MouseEvent would need a laid-out window. */
    static void paneHighlight(ScriptPane pane, int offset)
    {
        try
        {
            Method method = ScriptPane.class.getDeclaredMethod("highlight", int.class);
            method.setAccessible(true);
            method.invoke(pane, offset);
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("could not invoke ScriptPane.highlight", e);
        }
    }

    /** The line numbers currently rendered in the gutter, in order. */
    static List<String> gutterEntries(JTextPane gutter)
    {
        try
        {
            String text = gutter.getDocument().getText(0, gutter.getDocument().getLength());
            List<String> entries = new ArrayList<>();
            for (String line : text.split("\n", -1))
            {
                if (!line.isEmpty())
                    entries.add(line);
            }
            return entries;
        }
        catch (BadLocationException e)
        {
            throw new AssertionError("could not read the line number gutter", e);
        }
    }

    /**
     * The number of lines a text has, defined independently of Swing: a document with no newline
     * is one line, and every newline starts one more (a trailing newline therefore opens a final
     * empty line, which an editor still numbers).
     */
    static int lineCountOf(String text)
    {
        int lines = 1;
        for (int i = 0; i < text.length(); i++)
        {
            if (text.charAt(i) == '\n')
                lines++;
        }
        return lines;
    }
}
