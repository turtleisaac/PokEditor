package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A {@link java.util.Queue} which records how many elements have been drawn out of it.
 * <p>
 * The queue returned by {@code DefaultTable.obtainTextSources} is <em>positional</em>: element k
 * is the name list intended for the k-th text-consuming column. A positional protocol is only
 * well defined if every element is consumed exactly once, so the number of removals is as much a
 * part of the contract as the values themselves - one removal too few (or too many) shifts every
 * later element onto the wrong column.
 */
public class E_CountingQueue extends LinkedList<String[]>
{
    private int removals;

    public E_CountingQueue(Collection<String[]> initial)
    {
        super(initial);
    }

    @Override
    public String[] remove()
    {
        removals++;
        return super.remove();
    }

    @Override
    public String[] poll()
    {
        removals++;
        return super.poll();
    }

    /** how many elements have been drawn out of this queue */
    public int removals()
    {
        return removals;
    }
}
