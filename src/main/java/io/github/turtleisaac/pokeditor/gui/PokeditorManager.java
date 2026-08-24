package io.github.turtleisaac.pokeditor.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.framework.GenericNtrFile;
import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.nds4j.ui.PanelManager;
import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.*;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditorPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.DefaultSheetPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static io.github.turtleisaac.pokeditor.DataManager.getData;

public class PokeditorManager extends PanelManager
{
    private static final Dimension dimension = new Dimension(1200, 714);

    public static final FlatSVGIcon sheetExportIcon;
    public static final FlatSVGIcon sheetImportIcon;
    public static final FlatSVGIcon rowRemoveIcon;
    public static final FlatSVGIcon rowInsertIcon;
    public static final FlatSVGIcon searchIcon;
    public static final FlatSVGIcon clipboardIcon;
    public static final FlatSVGIcon copyIcon;

    public static final Color[] typeColors = new Color[]{new Color(201, 201, 201),
            new Color(173, 96, 94),
            new Color(165, 218, 218),
            new Color(184, 121, 240),
            new Color(200, 164, 117),
            new Color(184, 146, 48),
            new Color(157, 195, 132),
            new Color(139, 125, 190),
            new Color(153, 153, 153),
            new Color(230, 199, 255),
            new Color(189, 75, 49),
            new Color(88, 132, 225),
            new Color(120, 166, 90),
            new Color(249, 218, 120),
            new Color(223, 151, 143),
            new Color(70, 185, 185),
            new Color(98, 98, 246),
            new Color(102, 102, 102),
    };

    public static final Color[] darkModeTypeColors = new Color[]{new Color(116, 116, 116),
            new Color(130, 72, 71),
            new Color(101, 133, 133),
            new Color(86, 57, 112),
            new Color(157, 129, 92),
            new Color(99, 79, 26),
            new Color(123, 152, 103),
            new Color(77, 69, 105),
            new Color(68, 68, 68),
            new Color(192, 166, 212), //
            new Color(61, 24, 16), //
            new Color(55, 82, 140), //
            new Color(59, 81, 44),
            new Color(163, 144, 79),
            new Color(180, 122, 116),
            new Color(38, 100, 100),
            new Color(30, 30, 76),
            new Color(17, 17, 17),
    };

    static {
        try {
            sheetExportIcon = loadIcon("/pokeditor/icons/svg/table-export.svg");
            sheetImportIcon = loadIcon("/pokeditor/icons/svg/table-import.svg");
            rowRemoveIcon = loadIcon("/pokeditor/icons/svg/row-remove.svg");
            rowInsertIcon = loadIcon("/pokeditor/icons/svg/row-insert-bottom.svg");
            searchIcon = loadIcon("/pokeditor/icons/svg/list-search.svg");
            clipboardIcon = loadIcon("/pokeditor/icons/svg/clipboard-copy.svg");
            copyIcon = loadIcon("/pokeditor/icons/svg/copy.svg");

            sheetExportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            sheetImportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowRemoveIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowInsertIcon.setColorFilter(ThemeUtils.iconColorFilter);
            searchIcon.setColorFilter(ThemeUtils.iconColorFilter);
            clipboardIcon.setColorFilter(ThemeUtils.iconColorFilter);
            copyIcon.setColorFilter(ThemeUtils.iconColorFilter);

            /*
                this code determines if the label foreground color is closer to/further from black/white, then uses that
                to select which set of colors to use for the types on the sheets (lighter or darker)
                (the idea being a darker theme tends to have a lighter text foreground color)
             */
            Color c = UIManager.getColor("Label.foreground");
            double diff = (double) (c.getRed() + c.getBlue() + c.getGreen()) / 3;
            if (Math.max(diff, 255 - diff) == diff)
            {
                System.arraycopy(darkModeTypeColors, 0, typeColors, 0, typeColors.length);
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static FlatSVGIcon loadIcon(String path) throws IOException
    {
        try (InputStream stream = PokeditorManager.class.getResourceAsStream(path))
        {
            return new FlatSVGIcon(stream);
        }
    }

    private List<JPanel> panels;

    private NintendoDsRom rom;
    private Game baseRom;
    private boolean gitEnabled;

    public PokeditorManager(Tool tool)
    {
        super(tool, "PokEditor");

        rom = tool.getRom();
        Game.BaseRomInfo baseRomInfo = Game.parseBaseRom(rom.getGameCode());
        baseRom = baseRomInfo.game();
        gitEnabled = tool.isGitEnabled();
        GameFiles.initialize(baseRom);
        TextFiles.initialize(baseRom);
        GameCodeBinaries.initialize(baseRom);
        // The region is passed explicitly rather than read back off the Game enum, which used to
        // carry it as mutable per-constant state shared across every ROM opened in the process.
        Tables.initialize(baseRom, baseRomInfo.region());

        DataManager.codeBinarySetup(rom);

//        sheetPanels = new HashMap<>();
        panels = new ArrayList<>();
//        JPanel placeholder = new JPanel();
//        placeholder.setName("Test panel");
//
//        placeholder.setPreferredSize(dimension);
//        placeholder.setMinimumSize(dimension);

        DefaultDataEditorPanel<PokemonSpriteData, ?> battleSpriteEditor = DataManager.createPokemonSpriteEditor(this, rom);
        battleSpriteEditor.setName("Pokemon Sprites");
        battleSpriteEditor.setPreferredSize(battleSpriteEditor.getPreferredSize());

        DefaultSheetPanel<PersonalData, ?> personalPanel = DataManager.createPersonalSheet(this, rom);
        personalPanel.setName("Personal Sheet");

        DefaultSheetPanel<PersonalData, ?> tmCompatibilityPanel = DataManager.createTmCompatibilitySheet(this, rom);
        tmCompatibilityPanel.setName("TMs Sheet");
//        panels.add(personalPanel);

        DefaultSheetPanel<EvolutionData, ?> evolutionsPanel = DataManager.createEvolutionSheet(this, rom);
        evolutionsPanel.setName("Evolutions Sheet");
//        panels.add(evolutionsPanel);

        DefaultSheetPanel<LearnsetData, ?> learnsetsPanel = DataManager.createLearnsetSheet(this, rom);
        learnsetsPanel.setName("Learnsets Sheet");
//        panels.add(evolutionsPanel);

        DefaultSheetPanel<MoveData, ?> movesPanel = DataManager.createMoveSheet(this, rom);
        movesPanel.setName("Moves Sheet");

        // The field script editor is not in a working state and is not built. It is left out
        // rather than shown and broken: it is the one editor that can compile a script back into
        // the ROM, so a half-working version of it damages a project rather than merely
        // disappointing. Constructing it is also what pulls in VariableTracker, which is not
        // published anywhere, so a clean checkout cannot resolve it.
        //
        // To bring it back: restore the construction below and the panels.add further down.


//        JPanel fieldPanel = new JPanel();
//        fieldPanel.setName("Field");
//        JPanel waterPanel = new JPanel();
//        waterPanel.setName("Water");
//        PanelGroup encounters = new PanelGroup("Encounters", fieldPanel, waterPanel);

        PanelGroup pokemonGroup = new PanelGroup("Pokémon Editing", personalPanel, tmCompatibilityPanel, learnsetsPanel, evolutionsPanel, battleSpriteEditor);
        panels.add(pokemonGroup);
//        panels.add(battleSpriteEditor);
        panels.add(movesPanel);
//        panels.add(encounters);
//        panels.add(fieldScriptEditor); // disabled - see above
//        panels.add(placeholder);
    }

    public <E extends GenericFileData> void saveData(Class<E> dataClass)
    {
        // Both confirmations happen before anything is prepared.
        //
        // processDataList is not side effect free: PersonalParser writes the TM/HM table straight
        // into the shared arm9 buffer, and PokemonSpriteParser does the same. Preparing first and
        // asking afterwards therefore left arm9 already modified when the user said no - and the
        // next confirmed save of any sheet wrote that cancelled edit to disk. Reloading could not
        // undo it either, because the reload re-reads the TM table out of the arm9 it just
        // mutated. Asking first removes the window entirely.
        if (!DataManager.isLoaded(dataClass))
        {
            JOptionPane.showMessageDialog(null, "A fatal error occurred while attempting to save.", "Abort", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // the files a save will write, taken from the parser's own requirements rather than from
        // prepared output - the two are the same set for every parser in Core
        List<GameFiles> gameFiles = new ArrayList<>(DataManager.filesWrittenBy(dataClass));
        if (DataManager.isLoaded(TextBankData.class))
        {
            for (GameFiles gameFile : DataManager.filesWrittenBy(TextBankData.class))
            {
                if (!gameFiles.contains(gameFile))
                    gameFiles.add(gameFile);
            }
        }

        StringBuilder stringBuilder = new StringBuilder("This operation will write the following files:\n");
        for (GameFiles gameFile : gameFiles)
        {
            stringBuilder.append(gameFile.name()).append(": ");
            stringBuilder.append(gameFile.getPath()).append("\n");
        }
        stringBuilder.append("\nAre you sure you want to proceed?");

        if (JOptionPane.showConfirmDialog(null, stringBuilder.toString(), "PokEditor", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
        {
            JOptionPane.showMessageDialog(null, "Cancelled.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String message = null;
        if (gitEnabled)
        {
            message = JOptionPane.showInputDialog(null, "Enter commit message:", "Save & Commit Changes", JOptionPane.INFORMATION_MESSAGE);
            if (message == null)
            {
                JOptionPane.showMessageDialog(null, "Save aborted", "Abort", JOptionPane.ERROR_MESSAGE);
                return;
            }
            else if (message.isEmpty())
            {
                message = null;
            }
        }

        // Past this point the user has committed to the save. Serialising can still fail on a
        // value the data refuses, which leaves arm9 partly written - that is a failure rather
        // than a cancellation, and it behaved the same way before.
        Map<GameFiles, Narc> preparedData = DataManager.prepareData(rom, dataClass);
        if (preparedData == null)
        {
            JOptionPane.showMessageDialog(null, "A fatal error occurred while attempting to save.", "Abort", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<GameFiles, Narc> preparedTextData = DataManager.prepareData(rom, TextBankData.class);

        // only now is anything actually modified
        DataManager.commitData(rom, preparedData);
        DataManager.commitData(rom, preparedTextData);
        DataManager.saveCodeBinaries(rom, List.of(GameCodeBinaries.ARM9));

        for (GameFiles gameFile : gameFiles)
        {
            writeModifiedFile(gameFile.getPath());
        }

        // TM/HM move reassignments are applied to the in-memory arm9 above, so it has to be
        // written out too -- otherwise reopening the project rebuilds arm9 from the unchanged
        // file on disk and the edit silently disappears.
        writeModifiedArm9();

        if (gitEnabled)
        {
            commit(message);
        }

        DataManager.markClean(dataClass);
        DataManager.markClean(TextBankData.class);

        JOptionPane.showMessageDialog(null, "Success! (If any error popups came before this message, then disregard).", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
    }

    public void markSheetDirty(Class<? extends GenericFileData> dataClass)
    {
        DataManager.markDirty(dataClass);
        DataManager.markDirty(TextBankData.class);
    }

    public <E extends GenericFileData> void resetData(Class<E> dataClass)
    {
        DataManager.resetData(rom, dataClass);
    }

    public void resetAllIndexedCellRendererText()
    {
        for (JPanel panel : panels)
        {
            if (panel instanceof DefaultSheetPanel<?,?> sheetPanel)
            {
                sheetPanel.getTable().resetIndexedCellRendererText();
            }
            else if (panel instanceof PanelGroup panelGroup)
            {
                for (JPanel groupPanel : panelGroup.getPanels())
                {
                    if (groupPanel instanceof DefaultSheetPanel<?,?> sheetPanel)
                    {
                        sheetPanel.getTable().resetIndexedCellRendererText();
                    }
                }
            }
        }
    }

    public void writeSheet(String[][] data)
    {
        JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
        fc.setDialogTitle("Export Sheet");
        fc.setAcceptAllFileFilterUsed(false);

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.addChoosableFileFilter(new FileFilter()
        {
            @Override
            public boolean accept(File f)
            {
                return f.isDirectory() || f.getName().endsWith(".csv");
            }

            @Override
            public String getDescription()
            {
                return "CSV file (*.csv)";
            }
        });

        int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();
            String path = selected.getAbsolutePath();
            if (!path.endsWith(".csv"))
                path = path + ".csv";
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(path), StandardCharsets.UTF_8))
            {
                for (String[] row : data) {
                    String[] quoted = new String[row.length];
                    for (int i = 0; i < row.length; i++)
                        quoted[i] = quoteCsvField(row[i]);
                    writer.write(String.join(",", quoted));
                    writer.write("\n");
                }
            }
            catch(IOException e) {
                JOptionPane.showMessageDialog(null, "A fatal error occurred while writing the sheet to disk. See command-line for details.", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Quotes a single CSV field as per RFC 4180 - fields containing a comma, a double quote,
     * or a line break are wrapped in double quotes with any embedded quotes doubled.
     */
    private static String quoteCsvField(String field)
    {
        if (field == null)
            return "";

        if (field.indexOf(',') >= 0 || field.indexOf('"') >= 0 || field.indexOf('\r') >= 0 || field.indexOf('\n') >= 0)
            return '"' + field.replace("\"", "\"\"") + '"';

        return field;
    }

    private static JFileChooser prepareImageChooser(String title, boolean allowPalette)
    {
        String lastPath = Tool.preferences.get("pokeditor.imagePath", null);
        if (lastPath == null) {
            lastPath = System.getProperty("user.dir");
        }

        JFileChooser fc = new JFileChooser(lastPath);
        fc.setDialogTitle(title);
        fc.setAcceptAllFileFilterUsed(false);

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fc.addChoosableFileFilter(pngFilter);
        fc.addChoosableFileFilter(ncgrFilter);
        if (allowPalette)
            fc.addChoosableFileFilter(nclrFilter);
        return fc;
    }

    public static void writeIndexedImage(IndexedImage image)
    {
        JFileChooser fc = prepareImageChooser("Export Sprite", true);
        int returnVal = fc.showSaveDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();

            String extension;
            if (fc.getFileFilter().equals(pngFilter))
                extension = "png";
            else if (fc.getFileFilter().equals(ncgrFilter))
                extension = "NCGR";
            else
                extension = "NCLR";

            String path = selected.getAbsolutePath();
            if (!path.endsWith("." + extension))
                path = path + "." + extension;

            Tool.preferences.put("pokeditor.imagePath", selected.getParentFile().getAbsolutePath());

            try
            {
                if (extension.equals("png"))
                    image.saveToIndexedPngFile(path);
                else if (extension.equals("NCGR"))
                    image.saveToFile(path);
                else
                    image.getPalette().saveToFile(path);
            }
            catch(IOException e) {
                JOptionPane.showMessageDialog(null, "A fatal error occurred while writing an indexed PNG to disk. See command-line for details.", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e);
            }

        }
    }

    public static GenericNtrFile readIndexedImage(boolean allowPalette)
    {
        JFileChooser fc = prepareImageChooser("Import Sprite", allowPalette);
        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selected = fc.getSelectedFile();

            String path = selected.getAbsolutePath();

            Tool.preferences.put("pokeditor.imagePath", selected.getParentFile().getAbsolutePath());

            try
            {
                if (fc.getFileFilter().equals(pngFilter))
                    return IndexedImage.fromIndexedPngFile(path);
                else if (fc.getFileFilter().equals(ncgrFilter))
                    return IndexedImage.fromFile(path, 0, 0, 1, 1, true); //todo revisit if implementing DP support
                else
                    return Palette.fromFile(path, 4);
            }
            catch(IOException e) {
                JOptionPane.showMessageDialog(null, "A fatal error occurred while writing an indexed PNG to disk. See command-line for details.", "Error", JOptionPane.ERROR_MESSAGE);
                throw new RuntimeException(e);
            }
        }

        return new GenericNtrFile();
    }

    @Override
    public List<JPanel> getPanels()
    {
        return panels;
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return DataManager.hasUnsavedChanges();
    }

    @Override
    public void doInfoButtonAction(ActionEvent actionEvent)
    {
        //todo
        JOptionPane.showMessageDialog(null, "Not yet implemented", "Sorry", JOptionPane.ERROR_MESSAGE);
    }

    private static final FileFilter pngFilter = new FileFilter() {
        @Override
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(".png");
        }

        @Override
        public String getDescription()
        {
            return "Portable Network Graphics file (*.png)";
        }
    };

    private static final FileFilter ncgrFilter = new FileFilter()
    {
        @Override
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(".NCGR");
        }

        @Override
        public String getDescription()
        {
            return "Nitro Character Graphics Resource (*.NCGR)";
        }
    };

    private static final FileFilter nclrFilter = new FileFilter()
    {
        @Override
        public boolean accept(File f)
        {
            return f.isDirectory() || f.getName().endsWith(".NCLR");
        }

        @Override
        public String getDescription()
        {
            return "Nitro Color Resource (*.NCLR)";
        }
    };
}
