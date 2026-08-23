package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;

import java.util.List;

/**
 * {@link FakeModel} with one thing changed: its read path lengthens the entry it is reading
 * from, exactly the way {@code LearnsetsTable.LearnsetsModel.getValueFor} once did.
 * <pre>
 *     while (entryIdx &gt;= learnset.size()) { learnset.add(new LearnsetEntry()); }
 * </pre>
 * That loop sat in a method reached from {@code getValueAt}, which Swing calls for every
 * visible cell on every repaint - so scrolling a sheet permanently appended entries to files
 * the user had never touched, and those entries serialised as a real move rather than as the
 * terminator, growing again on every reopen.
 * <p>
 * This double exists purely so the suite can prove it catches that shape: a contract which
 * cannot fail is not a contract. See the meta-tests in {@code FormatModelContractTest}.
 */
public class MutatingReadModel extends FakeModel
{
    public MutatingReadModel(List<FakeEntry> data, List<TextBankData> textBankData)
    {
        super(data, textBankData);
    }

    @Override
    public Object getValueFor(int entryIdx, FakeColumn property)
    {
        if (property.idx >= 0)
        {
            FakeEntry entry = getData().get(entryIdx);
            while (currentGroup() >= entry.getRepeated().size())
                entry.getRepeated().add(new int[FakeEntry.GROUP_WIDTH]);
        }
        return super.getValueFor(entryIdx, property);
    }
}
