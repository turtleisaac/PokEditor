package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The smallest fixtures which will let a real sheet model be constructed at all.
 * <p>
 * Everything here is deliberately trivial - an empty, correctly terminated learnset, a
 * default-constructed personal entry, a text bank of placeholder strings. The suite is testing
 * the table models, so a fixture which relied on PokEditor-Core parsing a real narc correctly
 * would just move the thing under test somewhere the tests cannot see it.
 */
final class SheetFixtures
{
    private SheetFixtures()
    {
    }

    /** an empty learnset: just the 0xFFFF terminator, which is what an unused slot looks like */
    static LearnsetData emptyLearnset()
    {
        return learnset();
    }

    /**
     * @param moveAndLevelPairs alternating move id and level
     */
    static LearnsetData learnset(int... moveAndLevelPairs)
    {
        int entryCount = moveAndLevelPairs.length / 2;
        ByteBuffer buf = ByteBuffer.allocate((entryCount + 1) * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < entryCount; i++)
        {
            int move = moveAndLevelPairs[i * 2];
            int level = moveAndLevelPairs[i * 2 + 1];
            buf.putShort((short) (((level & 0x7F) << 9) | (move & 0x1FF)));
        }
        buf.putShort((short) 0xFFFF);
        return new LearnsetData(new BytesDataContainer(GameFiles.LEVEL_UP_LEARNSETS, null, buf.array()));
    }

    static List<LearnsetData> learnsets(int rows, int entriesPerRow)
    {
        List<LearnsetData> data = new ArrayList<>();
        for (int row = 0; row < rows; row++)
        {
            int[] pairs = new int[entriesPerRow * 2];
            for (int i = 0; i < entriesPerRow; i++)
            {
                pairs[i * 2] = i + 1;      // move
                pairs[i * 2 + 1] = i + 5;  // level
            }
            data.add(learnset(pairs));
        }
        return data;
    }

    static List<LearnsetData> emptyLearnsets(int rows)
    {
        List<LearnsetData> data = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            data.add(emptyLearnset());
        return data;
    }

    /** an evolution file holding {@code entriesPerRow} zeroed-out evolution records */
    static EvolutionData evolutions(int entryCount)
    {
        ByteBuffer buf = ByteBuffer.allocate(Math.max(1, entryCount) * 6).order(ByteOrder.LITTLE_ENDIAN);
        return new EvolutionData(new BytesDataContainer(GameFiles.EVOLUTIONS, null, buf.array()));
    }

    static List<EvolutionData> evolutionRows(int rows, int entriesPerRow)
    {
        List<EvolutionData> data = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            data.add(evolutions(entriesPerRow));
        return data;
    }

    static List<PersonalData> personals(int rows)
    {
        List<PersonalData> data = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            data.add(new PersonalData());
        return data;
    }

    static List<MoveData> moves(int rows)
    {
        List<MoveData> data = new ArrayList<>();
        for (int row = 0; row < rows; row++)
            data.add(new MoveData());
        return data;
    }

    /**
     * A list of text banks long enough to be indexed by every {@link TextFiles} constant the
     * sheets reach for, each holding {@code rows} placeholder strings.
     */
    static List<TextBankData> textBanks(int rows)
    {
        int highestBank = 0;
        for (TextFiles file : TextFiles.values())
        {
            try
            {
                highestBank = Math.max(highestBank, file.getValue());
            }
            catch (IllegalStateException noRomLoaded)
            {
                // this bank's index is only known once a base ROM has been picked; the sheets
                // under test do not reach for it, so a fixture does not have to cover it
            }
        }

        List<TextBankData> banks = new ArrayList<>();
        for (int bank = 0; bank <= highestBank; bank++)
        {
            List<TextBankData.Message> messages = new ArrayList<>();
            for (int i = 0; i < rows; i++)
                messages.add(new TextBankData.Message("Entry " + i));
            banks.add(new TextBankData(messages));
        }
        return banks;
    }
}
