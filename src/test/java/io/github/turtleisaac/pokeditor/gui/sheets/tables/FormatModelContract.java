package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * The properties every {@link FormatModel} has to have by virtue of being a table model at all,
 * expressed so that they can be pointed at any model - the real sheets, or a test double.
 * <p>
 * None of these assertions encode what any particular sheet currently returns. They are all
 * statements about the <em>relationship</em> between reading a cell, writing a cell and the
 * data underneath, which is what a spreadsheet is; a model which fails one of them is broken
 * regardless of what the numbers in it happen to be.
 * <p>
 * Every failure message names the model, the cell, and the values involved, because a red
 * assertion on a 4x100 grid which does not say <em>which</em> cell moved costs more time than
 * it saves.
 */
public final class FormatModelContract
{
    /** how many times the whole grid is swept when checking that reading changes nothing */
    private static final int READ_PASSES = 3;

    /** how many range violations are spelled out before the rest are merely counted */
    private static final int MAX_LISTED_VIOLATIONS = 12;

    /** guards the reflective fingerprint against a pathological object graph */
    private static final int MAX_FINGERPRINT_DEPTH = 8;

    private FormatModelContract()
    {
    }

    // ------------------------------------------------------------------ properties

    /**
     * <b>Observing a model must not change it.</b>
     * <p>
     * Swing calls {@code getValueAt} for every visible cell on every repaint, and again for
     * every cell when a sheet is exported. If any of those reads writes to the data underneath,
     * then simply looking at a file corrupts it, silently, and the corruption compounds every
     * time the file is opened. That is precisely what the learnsets sheet did when its read
     * path padded the learnset out to the column being painted.
     * <p>
     * The whole grid is read several times over, including the frozen columns (which live at
     * negative indices), and the model's data is fingerprinted before and after <em>each</em>
     * cell so the offending cell can be named.
     */
    public static void assertReadsArePure(FormatModel<?, ?> model)
    {
        String pristine = fingerprintOf(model);

        for (int pass = 0; pass < READ_PASSES; pass++)
        {
            for (int row = 0; row < model.getRowCount(); row++)
            {
                for (int col = firstColumn(model); col < model.getColumnCount(); col++)
                {
                    Object value = model.getValueAt(row, col);
                    String now = fingerprintOf(model);
                    if (!now.equals(pristine))
                    {
                        fail("reading %s (sweep %d) returned <%s> and changed the data underneath it."
                                        + " A read reached from painting must never mutate what it observes.%n%s",
                                cell(model, row, col), pass, value, difference(pristine, now));
                    }
                }
            }
        }
    }

    /**
     * <b>Reading a cell twice gives the same answer, whatever is read in between.</b>
     * <p>
     * The repeated-column sheets identify which repetition a column refers to by writing into a
     * field before dispatching to the read. When that field lives somewhere shared - on the
     * column enum, say, where every model in the process sees the same one - then reading any
     * other cell first can change what a cell reports, and a repaint reads every cell. The
     * symptom is a sheet which shows one entry's move under another entry's level.
     */
    public static void assertReadsAreIdempotent(FormatModel<?, ?> model)
    {
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int col = firstColumn(model); col < model.getColumnCount(); col++)
            {
                Object first = model.getValueAt(row, col);
                Object again = model.getValueAt(row, col);
                assertThat(again)
                        .as("%s returned <%s> and then, with nothing in between, <%s>", cell(model, row, col), first, again)
                        .isEqualTo(first);

                sweep(model, row, col);

                Object afterSweep = model.getValueAt(row, col);
                assertThat(afterSweep)
                        .as("%s returned <%s>, but after the rest of the sheet was read - which is all a repaint does -"
                                        + " the same cell returned <%s>. Which cell a read refers to must not be carried in state"
                                        + " that other reads overwrite.",
                                cell(model, row, col), first, afterSweep)
                        .isEqualTo(first);
            }
        }
    }

    /**
     * <b>A write lands in one cell and nowhere else.</b>
     * <p>
     * This is the table analogue of writing one pixel: it catches an index translation which is
     * off by the frozen-column count, a repetition index computed from the wrong column, or a
     * write which reaches through into a neighbour. Those are the errors which make a user's
     * edit appear to work while quietly overwriting a different Pokemon's data.
     */
    public static void assertWritesAreLocal(FormatModel<?, ?> model, int row, int col, Object value)
    {
        Object[][] before = readGrid(model);

        model.setValueAt(value, row, col);

        Object landed = model.getValueAt(row, col);
        assertThat(sameCellValue(value, landed))
                .as("wrote <%s> into %s, which now reads back <%s>", value, cell(model, row, col), landed)
                .isTrue();

        Object[][] after = readGrid(model);
        int offset = -firstColumn(model);
        for (int r = 0; r < after.length; r++)
        {
            for (int c = firstColumn(model); c < model.getColumnCount(); c++)
            {
                if (r == row && c == col)
                    continue;
                Object was = before[r][c + offset];
                Object is = after[r][c + offset];
                assertThat(is)
                        .as("writing <%s> into %s also changed %s, which held <%s> and now holds <%s>",
                                value, cell(model, row, col), cell(model, r, c), was, is)
                        .isEqualTo(was);
            }
        }
    }

    /**
     * <b>What you put into a cell is what the cell then shows.</b>
     * <p>
     * A model which accepts a value and then renders something else has lost the user's edit;
     * a model which accepts it and renders it only until the next repaint has lost it more
     * subtly. The read is taken twice for that reason.
     */
    public static void assertWriteRoundTrips(FormatModel<?, ?> model, int row, int col, Object value)
    {
        model.setValueAt(value, row, col);

        Object read = model.getValueAt(row, col);
        assertThat(sameCellValue(value, read))
                .as("wrote <%s> into %s, read back <%s>", value, cell(model, row, col), read)
                .isTrue();

        Object reread = model.getValueAt(row, col);
        assertThat(sameCellValue(value, reread))
                .as("wrote <%s> into %s, which read back <%s> once and <%s> the second time",
                        value, cell(model, row, col), read, reread)
                .isTrue();
    }

    /**
     * <b>Every cell the model says exists can be rendered.</b>
     * <p>
     * {@code getRowCount()} and {@code getColumnCount()} are a promise to Swing, which will ask
     * for every one of those cells without being asked twice. A cell inside the declared grid
     * which throws is not a caught error, it is an exception on the paint thread: the sheet is
     * left half-drawn and the editor has to be killed.
     */
    public static void assertEveryCellIsReadable(FormatModel<?, ?> model)
    {
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int col = firstColumn(model); col < model.getColumnCount(); col++)
            {
                try
                {
                    model.getValueAt(row, col);
                }
                catch (Throwable t)
                {
                    fail("%s is inside the %d x %d grid the model declares, but reading it threw %s: %s",
                            cell(model, row, col), model.getRowCount(), model.getColumnCount(),
                            t.getClass().getName(), t.getMessage());
                }
            }
        }
    }

    /**
     * <b>The range a column advertises is the range that column really has.</b>
     * <p>
     * {@code getCellValueRange} is what the cell editors use to decide which keystrokes to
     * refuse, so it is a promise made to the user. If a bound cannot actually be stored, the
     * editor happily accepts a value the write path then rejects with a dialog; if a value
     * outside the range can be stored, it survives until save time and is truncated into a
     * different, plausible-looking value - a move that becomes a different move.
     * <p>
     * Checkbox and free-text columns have no numeric range to be honest about and are skipped.
     */
    public static void assertValueRangesAreHonest(FormatModel<?, ?> model)
    {
        List<String> violations = new ArrayList<>();

        for (int col = 0; col < model.getColumnCount(); col++)
        {
            int[] range = model.getCellValueRange(col);

            // a malformed range is not a violation to collect, it is a broken promise the cell
            // editors cannot even be built from, so it fails on the spot
            assertThat(range)
                    .as("%s declares no value range for column %d", name(model), col)
                    .isNotNull();
            assertThat(range.length)
                    .as("%s column %d must declare a range as {min, max}, but declared %d value(s)",
                            name(model), col, range.length)
                    .isEqualTo(2);
            assertThat(range[0])
                    .as("%s column %d declares the empty range {%d, %d}", name(model), col, range[0], range[1])
                    .isLessThanOrEqualTo(range[1]);

            if (!hasNumericRange(model, col))
                continue;

            for (int bound : new int[] {range[0], range[1]})
                collectBoundViolation(model, col, range, bound, violations);

            collectOutOfRangeViolation(model, col, range, range[1] + 1, violations);
            if (range[0] > Integer.MIN_VALUE)
                collectOutOfRangeViolation(model, col, range, range[0] - 1, violations);
        }

        if (!violations.isEmpty())
        {
            // more than the first offending column is listed, so that one red run says how far
            // the problem reaches instead of having to be re-run column by column
            List<String> listed = violations.subList(0, Math.min(violations.size(), MAX_LISTED_VIOLATIONS));
            String tail = violations.size() > listed.size()
                    ? String.format("%n  ... and %d more", violations.size() - listed.size())
                    : "";
            fail("%s does not hold the value ranges it advertises (%d violation(s)):%n  %s%s",
                    name(model), violations.size(), String.join(String.format("%n  "), listed), tail);
        }
    }

    // ------------------------------------------------------------------ internals

    /** the column has to be able to hold the bound it tells the cell editor it can hold */
    private static void collectBoundViolation(FormatModel<?, ?> model, int col, int[] range, int bound, List<String> violations)
    {
        try
        {
            model.setValueAt(bound, 0, col);
        }
        catch (RuntimeException e)
        {
            violations.add(String.format("%s advertises %d..%d, but writing the advertised bound <%d> was rejected: %s: %s",
                    cell(model, 0, col), range[0], range[1], bound, e.getClass().getName(), e.getMessage()));
            return;
        }

        Object read = model.getValueAt(0, col);
        if (!sameCellValue(bound, read))
        {
            violations.add(String.format("%s advertises %d..%d; the advertised bound <%d> was written and <%s> read back",
                    cell(model, 0, col), range[0], range[1], bound, read));
        }
    }

    /** a value the column cannot hold has to be refused where it is written, not at save time */
    private static void collectOutOfRangeViolation(FormatModel<?, ?> model, int col, int[] range, int outside, List<String> violations)
    {
        try
        {
            model.setValueAt(outside, 0, col);
        }
        catch (RuntimeException refused)
        {
            return; // refused loudly, which is the whole point
        }

        Object read = model.getValueAt(0, col);
        Integer numeric = asInt(read);
        if (numeric == null || numeric < range[0] || numeric > range[1])
        {
            violations.add(String.format("%s advertises %d..%d, but accepted <%d> without complaint and now reads back <%s>"
                            + " - a value the column cannot hold has to be refused where it is written, not stored until"
                            + " save silently truncates it into a different value",
                    cell(model, 0, col), range[0], range[1], outside, read));
        }
    }

    /** the leftmost column index of the grid, which is negative when the sheet has frozen columns */
    private static int firstColumn(FormatModel<?, ?> model)
    {
        return -model.getNumFrozenColumns();
    }

    private static void sweep(FormatModel<?, ?> model, int skipRow, int skipCol)
    {
        for (int row = 0; row < model.getRowCount(); row++)
        {
            for (int col = firstColumn(model); col < model.getColumnCount(); col++)
            {
                if (row == skipRow && col == skipCol)
                    continue;
                model.getValueAt(row, col);
            }
        }
    }

    private static Object[][] readGrid(FormatModel<?, ?> model)
    {
        int offset = -firstColumn(model);
        Object[][] grid = new Object[model.getRowCount()][model.getColumnCount() + offset];
        for (int row = 0; row < grid.length; row++)
        {
            for (int col = firstColumn(model); col < model.getColumnCount(); col++)
            {
                grid[row][col + offset] = model.getValueAt(row, col);
            }
        }
        return grid;
    }

    private static boolean hasNumericRange(FormatModel<?, ?> model, int col)
    {
        CellTypes type = model.getCellType(col);
        return type != null && type != CellTypes.CHECKBOX && type != CellTypes.STRING;
    }

    private static Integer asInt(Object value)
    {
        if (value instanceof Integer i)
            return i;
        if (value instanceof Number n)
            return n.intValue();
        if (value instanceof String s)
        {
            try
            {
                return Integer.valueOf(s.trim());
            }
            catch (NumberFormatException ignored)
            {
                return null;
            }
        }
        return null;
    }

    /**
     * Whether a value written into a cell and a value read back out of it are the same value.
     * A sheet is edited with text and stores numbers, so {@code "42"} and {@code 42} are the
     * same cell contents; anything else is a difference.
     */
    private static boolean sameCellValue(Object written, Object read)
    {
        if (Objects.equals(written, read))
            return true;
        if (written == null || read == null)
            return false;

        Integer a = asInt(written);
        Integer b = asInt(read);
        if (a != null && b != null)
            return a.equals(b);

        if (written instanceof Boolean || read instanceof Boolean)
            return String.valueOf(written).equalsIgnoreCase(String.valueOf(read));

        return false;
    }

    private static String name(FormatModel<?, ?> model)
    {
        String simple = model.getClass().getSimpleName();
        return simple.isEmpty() ? model.getClass().getName() : simple;
    }

    private static String cell(FormatModel<?, ?> model, int row, int col)
    {
        return String.format("%s cell (row %d, column %d)", name(model), row, col);
    }

    // ------------------------------------------------------ deep fingerprint

    /**
     * A textual fingerprint of everything the model is showing: the entry list, the length and
     * contents of every nested collection and array inside each entry, and the text banks the
     * frozen columns are drawn from.
     * <p>
     * It is built by reflection rather than by calling anything on the formats themselves, so
     * that the comparison cannot be defeated by a format whose {@code equals} is inherited from
     * {@code ArrayList} (and so ignores an appended default-valued entry's <em>meaning</em>) and
     * so that it does not depend on any format serialising correctly.
     */
    private static String fingerprintOf(FormatModel<?, ?> model)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("data=");
        fingerprint(model.getData(), sb, new IdentityHashMap<>(), 0);
        sb.append(" text=");
        fingerprint(model.getTextBankData(), sb, new IdentityHashMap<>(), 0);
        return sb.toString();
    }

    private static void fingerprint(Object value, StringBuilder sb, IdentityHashMap<Object, Object> seen, int depth)
    {
        if (value == null)
        {
            sb.append("null");
            return;
        }

        Class<?> type = value.getClass();
        if (type.isPrimitive() || value instanceof Number || value instanceof Boolean
                || value instanceof Character || value instanceof CharSequence || value instanceof Enum<?>)
        {
            sb.append(value);
            return;
        }

        if (depth > MAX_FINGERPRINT_DEPTH)
        {
            sb.append("...");
            return;
        }

        if (seen.put(value, value) != null)
        {
            sb.append("<cycle>");
            return;
        }

        try
        {
            if (type.isArray())
            {
                int length = Array.getLength(value);
                sb.append('[').append(length).append(':');
                for (int i = 0; i < length; i++)
                {
                    if (i > 0)
                        sb.append(',');
                    fingerprint(Array.get(value, i), sb, seen, depth + 1);
                }
                sb.append(']');
                return;
            }

            if (value instanceof Collection<?> collection)
            {
                // the size is the part which the learnsets bug moved, so it leads
                sb.append('(').append(collection.size()).append(':');
                boolean first = true;
                for (Object element : collection)
                {
                    if (!first)
                        sb.append(',');
                    first = false;
                    fingerprint(element, sb, seen, depth + 1);
                }
                sb.append(')');
                // a format may be a list and still carry fields of its own
                appendFields(value, sb, seen, depth);
                return;
            }

            if (value instanceof Map<?, ?> map)
            {
                sb.append('{').append(map.size()).append(':');
                for (Map.Entry<?, ?> entry : map.entrySet())
                {
                    fingerprint(entry.getKey(), sb, seen, depth + 1);
                    sb.append("->");
                    fingerprint(entry.getValue(), sb, seen, depth + 1);
                    sb.append(';');
                }
                sb.append('}');
                return;
            }

            sb.append(type.getSimpleName());
            appendFields(value, sb, seen, depth);
        }
        finally
        {
            seen.remove(value);
        }
    }

    private static void appendFields(Object value, StringBuilder sb, IdentityHashMap<Object, Object> seen, int depth)
    {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = value.getClass(); c != null && !c.getName().startsWith("java."); c = c.getSuperclass())
        {
            for (Field field : c.getDeclaredFields())
            {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic())
                    continue;
                fields.add(field);
            }
        }
        if (fields.isEmpty())
            return;

        fields.sort(Comparator.comparing(Field::getName));
        sb.append('<');
        for (Field field : fields)
        {
            sb.append(field.getName()).append('=');
            try
            {
                field.setAccessible(true);
                fingerprint(field.get(value), sb, seen, depth + 1);
            }
            catch (ReflectiveOperationException | RuntimeException e)
            {
                sb.append("<unreadable>");
            }
            sb.append(';');
        }
        sb.append('>');
    }

    /** the neighbourhood of the first character at which two fingerprints diverge */
    private static String difference(String before, String after)
    {
        int i = 0;
        while (i < before.length() && i < after.length() && before.charAt(i) == after.charAt(i))
            i++;
        int from = Math.max(0, i - 60);
        return String.format("first divergence at offset %d%n  before: ...%s%n  after:  ...%s",
                i, window(before, from), window(after, from));
    }

    private static String window(String s, int from)
    {
        int to = Math.min(s.length(), from + 200);
        return s.substring(Math.min(from, s.length()), to) + (to < s.length() ? "..." : "");
    }
}
