package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A stand-in for a real sheet format which carries none of PokEditor-Core's semantics.
 * <p>
 * It deliberately has the two shapes every sheet in this editor has to cope with: a couple of
 * plain scalar fields, and a <em>variable-length</em> list of fixed-width groups. The second
 * shape is the interesting one - it is what {@code LearnsetData} and {@code EvolutionData} are,
 * and it is the shape which lets a table model be tempted into growing its own data while
 * merely rendering a cell which lies past the end of the list.
 * <p>
 * Nothing here validates or interprets anything: the point of the double is that a failure of
 * the contract can only ever be the model's fault, never the format's.
 */
public class FakeEntry implements GenericFileData
{
    /** an arbitrary key - this double never goes near a real narc, it just needs a stable one */
    static final GameFiles FAKE_FILE = GameFiles.PERSONAL;

    /** how many ints one repeated group holds; mirrors the {move, level} pair of a learnset */
    public static final int GROUP_WIDTH = 2;

    private int alpha;
    private int beta;
    private final List<int[]> repeated = new ArrayList<>();

    public FakeEntry()
    {
    }

    public FakeEntry(int alpha, int beta)
    {
        this.alpha = alpha;
        this.beta = beta;
    }

    public int getAlpha()
    {
        return alpha;
    }

    public void setAlpha(int alpha)
    {
        this.alpha = alpha;
    }

    public int getBeta()
    {
        return beta;
    }

    public void setBeta(int beta)
    {
        this.beta = beta;
    }

    /** the live list - a model is expected to read it without lengthening it */
    public List<int[]> getRepeated()
    {
        return repeated;
    }

    public FakeEntry withGroups(int groupCount)
    {
        for (int i = 0; i < groupCount; i++)
        {
            repeated.add(new int[GROUP_WIDTH]);
        }
        return this;
    }

    @Override
    public void setData(BytesDataContainer files)
    {
        ByteBuffer buf = ByteBuffer.wrap(files.get(FAKE_FILE, null));
        alpha = buf.getInt();
        beta = buf.getInt();
        int groupCount = buf.getInt();
        repeated.clear();
        for (int i = 0; i < groupCount; i++)
        {
            int[] group = new int[GROUP_WIDTH];
            for (int j = 0; j < GROUP_WIDTH; j++)
                group[j] = buf.getInt();
            repeated.add(group);
        }
    }

    @Override
    public BytesDataContainer save()
    {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES * (3 + repeated.size() * GROUP_WIDTH));
        buf.putInt(alpha);
        buf.putInt(beta);
        buf.putInt(repeated.size());
        for (int[] group : repeated)
        {
            for (int value : group)
                buf.putInt(value);
        }
        return new BytesDataContainer(FAKE_FILE, null, buf.array());
    }
}
