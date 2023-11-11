package io.github.turtleisaac.pokeditor.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.framework.GenericNtrFile;
import io.github.turtleisaac.nds4j.images.IndexedImage;
import io.github.turtleisaac.nds4j.images.Palette;
import io.github.turtleisaac.nds4j.ui.PanelManager;
import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.Game;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.editors.DefaultEditor;
import io.github.turtleisaac.pokeditor.gui.editors.DefaultEditorPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.DefaultSheetPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

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

    static {
        try {
            sheetExportIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/table-export.svg"));
            sheetImportIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/table-import.svg"));
            rowRemoveIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/row-remove.svg"));
            rowInsertIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/row-insert-bottom.svg"));
            searchIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/list-search.svg"));
            clipboardIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/clipboard-copy.svg"));
            copyIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/pokeditor/icons/svg/copy.svg"));

            sheetExportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            sheetImportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowRemoveIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowInsertIcon.setColorFilter(ThemeUtils.iconColorFilter);
            searchIcon.setColorFilter(ThemeUtils.iconColorFilter);
            clipboardIcon.setColorFilter(ThemeUtils.iconColorFilter);
            copyIcon.setColorFilter(ThemeUtils.iconColorFilter);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<Class<? extends GenericFileData>, DefaultSheetPanel<? extends GenericFileData, ? extends Enum<?>>> sheetPanels;
    private List<JPanel> panels;

    private NintendoDsRom rom;
    private Game baseRom;

    public PokeditorManager(Tool tool)
    {
        super(tool, "PokEditor");

        rom = tool.getRom();
        baseRom = Game.parseBaseRom(rom.getGameCode());
        GameFiles.initialize(baseRom);
        TextFiles.initialize(baseRom);

        sheetPanels = new HashMap<>();
        panels = new ArrayList<>();
//        JPanel placeholder = new JPanel();
//        placeholder.setName("Test panel");
//
//        placeholder.setPreferredSize(dimension);
//        placeholder.setMinimumSize(dimension);

        DefaultEditorPanel<PokemonSpriteData, ?> battleSpriteEditor = DataManager.createPokemonSpriteEditor(this, rom);
        battleSpriteEditor.setName("Battle Sprites");
        battleSpriteEditor.setPreferredSize(battleSpriteEditor.getPreferredSize());

        DefaultSheetPanel<PersonalData, ?> personalPanel = DataManager.createPersonalSheet(this, rom);
        personalPanel.setName("Personal Sheet");
        sheetPanels.put(PersonalData.class, personalPanel);
//        panels.add(personalPanel);

        DefaultSheetPanel<EvolutionData, ?> evolutionsPanel = DataManager.createEvolutionSheet(this, rom);
        evolutionsPanel.setName("Evolutions Sheet");
        sheetPanels.put(EvolutionData.class, evolutionsPanel);
//        panels.add(evolutionsPanel);

        DefaultSheetPanel<LearnsetData, ?> learnsetsPanel = DataManager.createLearnsetSheet(this, rom);
        learnsetsPanel.setName("Learnsets Sheet");
        sheetPanels.put(LearnsetData.class, learnsetsPanel);
//        panels.add(evolutionsPanel);

        DefaultSheetPanel<MoveData, ?> movesPanel = DataManager.createMoveSheet(this, rom);
        movesPanel.setName("Moves Sheet");
        sheetPanels.put(MoveData.class, movesPanel);

        JPanel fieldPanel = new JPanel();
        fieldPanel.setName("Field");
        JPanel waterPanel = new JPanel();
        waterPanel.setName("Water");
        PanelGroup encounters = new PanelGroup("Encounters", fieldPanel, waterPanel);

        PanelGroup pokemonGroup = new PanelGroup("Pokémon Editing", personalPanel, learnsetsPanel, evolutionsPanel);
        panels.add(pokemonGroup);
        panels.add(battleSpriteEditor);
        panels.add(movesPanel);
        panels.add(encounters);
//        panels.add(placeholder);
    }

    public <E extends GenericFileData> void saveData(Class<E> dataClass)
    {
        DataManager.saveData(rom, dataClass);
        DataManager.saveData(rom, TextBankData.class);

        if (!wipeAndWriteUnpacked())
            throw new RuntimeException("An error occurred while deleting or writing a file, please check write permissions");
    }

    public <E extends GenericFileData> void resetData(Class<E> dataClass)
    {
        DataManager.resetData(rom, dataClass);
    }

    public void resetAllIndexedCellRendererText()
    {
        for (DefaultSheetPanel<?, ?> panel : sheetPanels.values())
        {
            panel.getTable().resetIndexedCellRendererText();
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
            try
            {
                BufferedWriter writer = new BufferedWriter(new FileWriter(path));
                for (String[] row : data) {
                    for (String s : row)
                        writer.write(s + ",");
                    writer.write("\n");
                }

                writer.close();
            }
            catch(IOException e) {
                throw new RuntimeException(e);
            }

        }
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
        //todo
        return false;
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
