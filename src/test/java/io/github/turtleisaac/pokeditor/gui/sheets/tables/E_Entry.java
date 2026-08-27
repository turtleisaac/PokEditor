package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.nio.ByteBuffer;

/**
 * A minimal {@link GenericFileData} double: a fixed-width vector of ints and nothing else.
 * <p>
 * It is deliberately free of validation and of any repeated/variable-length structure, so that
 * a property which fails while using it can only be the fault of the code under test, never of
 * the format. {@link #save()} and {@link #setData(BytesDataContainer)} are exact inverses of one
 * another by construction, which is what lets a copy/paste round trip be asserted as an identity.
 */
public class E_Entry implements GenericFileData
{
    /** an arbitrary stable key; this double never goes near a real narc */
    static final GameFiles E_FILE = GameFiles.PERSONAL;

    private final int[] cells;

    private int saveCalls;
    private int setDataCalls;

    public E_Entry(int width)
    {
        this.cells = new int[width];
    }

    public int width()
    {
        return cells.length;
    }

    public int get(int idx)
    {
        return cells[idx];
    }

    public void set(int idx, int value)
    {
        cells[idx] = value;
    }

    /** a defensive copy of the whole payload, for before/after purity comparisons */
    public int[] snapshot()
    {
        return cells.clone();
    }

    public int saveCalls()
    {
        return saveCalls;
    }

    public int setDataCalls()
    {
        return setDataCalls;
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        setDataCalls++;
        ByteBuffer buf = ByteBuffer.wrap(files.get(E_FILE, null));
        int count = buf.getInt();
        for (int i = 0; i < count && i < cells.length; i++)
            cells[i] = buf.getInt();
    }

    @Override
    public BytesDataContainer save()
    {
        saveCalls++;
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES * (1 + cells.length));
        buf.putInt(cells.length);
        for (int value : cells)
            buf.putInt(value);
        return new BytesDataContainer(E_FILE, null, buf.array());
    }
}
