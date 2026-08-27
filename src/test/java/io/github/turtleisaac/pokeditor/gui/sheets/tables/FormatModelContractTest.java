package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates {@link FormatModelContract} itself, against two models built for the purpose:
 * one which is correct, and one which carries the learnsets bug. The first shows the contract
 * is satisfiable; the second shows it is not vacuous.
 */
class FormatModelContractTest
{
    private static final int ROWS = 4;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    static List<TextBankData> names(int count)
    {
        List<TextBankData.Message> messages = new ArrayList<>();
        for (int i = 0; i < count; i++)
            messages.add(new TextBankData.Message("Entry " + i));
        List<TextBankData> banks = new ArrayList<>();
        banks.add(new TextBankData(messages));
        return banks;
    }

    private static FakeModel populatedModel()
    {
        return new FakeModel(FakeModel.populatedEntries(ROWS), names(ROWS));
    }

    // ------------------------------------------------------------ the double is coherent

    @Test
    @DisplayName("the stand-in format reloads exactly the state it saved, so a snapshot of it means something")
    void fakeEntryRoundTrips()
    {
        FakeEntry original = new FakeEntry(7, 9).withGroups(3);
        original.getRepeated().get(1)[0] = 42;
        original.getRepeated().get(2)[1] = 13;

        FakeEntry reloaded = new FakeEntry();
        reloaded.setData(original.save());

        assertThat(reloaded.getAlpha()).isEqualTo(7);
        assertThat(reloaded.getBeta()).isEqualTo(9);
        assertThat(reloaded.getRepeated()).hasSize(3);
        assertThat(reloaded.getRepeated().get(1)[0]).isEqualTo(42);
        assertThat(reloaded.getRepeated().get(2)[1]).isEqualTo(13);
    }

    // ------------------------------------------------------------ the contract holds on a correct model

    @Test
    @DisplayName("reading every cell of a correct model leaves its data untouched")
    void readsArePure()
    {
        FormatModelContract.assertReadsArePure(populatedModel());
    }

    @Test
    @DisplayName("reading every cell of a correct model whose entries are empty still leaves it untouched")
    void readsArePureOverAnEmptyTail()
    {
        // the control for the meta-test below: the grid is far wider than the entries are long,
        // so every cell past the first is a cell a buggy model would be tempted to materialise
        FormatModelContract.assertReadsArePure(new FakeModel(FakeModel.emptyEntries(ROWS), names(ROWS)));
    }

    @Test
    @DisplayName("a cell of a correct model reports the same value however much of the sheet is read around it")
    void readsAreIdempotent()
    {
        FormatModelContract.assertReadsAreIdempotent(populatedModel());
    }

    @Test
    @DisplayName("writing a cell of a correct model changes that cell and no other")
    void writesAreLocal()
    {
        FormatModelContract.assertWritesAreLocal(populatedModel(), 1, 0, 300);
        FormatModelContract.assertWritesAreLocal(populatedModel(), 2, 5, 99);
        FormatModelContract.assertWritesAreLocal(populatedModel(), 0, 7, 1);
        FormatModelContract.assertWritesAreLocal(populatedModel(), 3, -1, "Renamed");
    }

    @Test
    @DisplayName("a value written into a correct model is the value that model then reports")
    void writesRoundTrip()
    {
        FormatModelContract.assertWriteRoundTrips(populatedModel(), 0, 0, 511);
        FormatModelContract.assertWriteRoundTrips(populatedModel(), 3, 6, 0);
        FormatModelContract.assertWriteRoundTrips(populatedModel(), 2, 3, "64"); // as a cell editor delivers it
        FormatModelContract.assertWriteRoundTrips(populatedModel(), 1, -1, "Renamed");
    }

    @Test
    @DisplayName("every cell a correct model declares can be read without throwing")
    void everyCellIsReadable()
    {
        FormatModelContract.assertEveryCellIsReadable(populatedModel());
        FormatModelContract.assertEveryCellIsReadable(new FakeModel(FakeModel.emptyEntries(ROWS), names(ROWS)));
    }

    @Test
    @DisplayName("a correct model can store both bounds of every range it advertises, and refuses everything outside them")
    void valueRangesAreHonest()
    {
        FormatModelContract.assertValueRangesAreHonest(populatedModel());
    }

    // ------------------------------------------------------------ the contract has teeth

    @Test
    @DisplayName("META: the purity property fails on a model whose read grows the entry it is reading")
    void purityCatchesAGrowingRead()
    {
        MutatingReadModel buggy = new MutatingReadModel(FakeModel.emptyEntries(ROWS), names(ROWS));

        assertThatThrownBy(() -> FormatModelContract.assertReadsArePure(buggy))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("MutatingReadModel cell (row 0, column 0)")
                .hasMessageContaining("changed the data underneath it");
    }

    @Test
    @DisplayName("META: the growing read really does corrupt the data, not merely trip the fingerprint")
    void theGrowingReadActuallyLengthensTheEntry()
    {
        List<FakeEntry> entries = FakeModel.emptyEntries(ROWS);
        MutatingReadModel buggy = new MutatingReadModel(entries, names(ROWS));

        assertThat(entries.get(0).getRepeated()).isEmpty();
        for (int col = 0; col < buggy.getColumnCount(); col++)
            buggy.getValueAt(0, col); // one repaint of one row

        assertThat(entries.get(0).getRepeated())
                .as("merely painting row 0 appended entries which the user never typed")
                .hasSize(FakeModel.MAX_GROUPS);
    }

    @Test
    @DisplayName("META: an otherwise identical model without the growing read passes the same property")
    void theOnlyDifferenceIsTheGrowingRead()
    {
        // same fixture, same grid, same column enum - so the failure above can only be the read path
        FormatModelContract.assertReadsArePure(new FakeModel(FakeModel.emptyEntries(ROWS), names(ROWS)));
    }
}
