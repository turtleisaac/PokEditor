package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A concrete {@link DefaultTable} whose text sources are <em>self-identifying</em>: the array at
 * queue position k is {@code {"src<k>#0", "src<k>#1", "src<k>#2"}}. Because every element names
 * its own position, the array a column ends up holding is enough on its own to recover which
 * queue position that column was served from - which is exactly what a consumption bijection is
 * a statement about.
 */
public class E_ProbeTable extends DefaultTable<E_Entry, E_LayoutModel.Column>
{
    /**
     * How many source arrays the next {@code obtainTextSources} call hands out. It has to be
     * static because {@code DefaultTable}'s constructor calls {@code obtainTextSources} before any
     * field of this subclass has been assigned. Tests set it from their own reference mapping and
     * reset it in {@code @BeforeEach}.
     */
    public static int sourceCount = 0;

    /**
     * When non-negative, the element handed out at this queue position is {@code null} instead of
     * a real array. {@code DefaultTable.getTextFromSource} documents a {@code new String[]{""}}
     * fallback for exactly this case, and that fallback is only meaningful if a null element can
     * actually reach it.
     */
    public static int nullPosition = -1;

    /** deliberately has no initializer: a field initializer would run after the super constructor
     *  and would therefore discard the queue issued during construction */
    private List<E_CountingQueue> issued;

    public E_ProbeTable(E_LayoutModel model, int[] widths, CellTypes.CustomCellFunctionSupplier supplier)
    {
        super(model, new ArrayList<>(), widths, supplier);
    }

    /** the array which occupies queue position {@code k} */
    public static String[] source(int k)
    {
        return new String[] {"src" + k + "#0", "src" + k + "#1", "src" + k + "#2"};
    }

    /** the queue position an array of the form produced by {@link #source(int)} came from */
    public static int positionOf(String[] array)
    {
        if (array == null || array.length == 0 || array[0] == null || !array[0].startsWith("src"))
            return -1;
        return Integer.parseInt(array[0].substring("src".length(), array[0].indexOf('#')));
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        if (issued == null)
            issued = new ArrayList<>();
        Queue<String[]> contents = new LinkedList<>();
        for (int k = 0; k < sourceCount; k++)
            contents.add(k == nullPosition ? null : source(k));
        E_CountingQueue queue = new E_CountingQueue(contents);
        issued.add(queue);
        return queue;
    }

    /** every queue this table has handed out, in the order it handed them out */
    public List<E_CountingQueue> issuedQueues()
    {
        return issued == null ? new ArrayList<>() : issued;
    }

    public E_CountingQueue lastQueue()
    {
        List<E_CountingQueue> queues = issuedQueues();
        return queues.get(queues.size() - 1);
    }

    @Override
    public Class<E_Entry> getDataClass()
    {
        return E_Entry.class;
    }
}
