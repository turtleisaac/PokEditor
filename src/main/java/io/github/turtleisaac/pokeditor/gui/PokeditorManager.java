package io.github.turtleisaac.pokeditor.gui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.ui.PanelManager;
import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.Game;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.DefaultSheetPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.PersonalTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
            sheetExportIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/icons/svg/table-export.svg"));
            sheetImportIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/icons/svg/table-import.svg"));
            rowRemoveIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/icons/svg/row-remove.svg"));
            rowInsertIcon = new FlatSVGIcon(PokeditorManager.class.getResourceAsStream("/icons/svg/row-insert-bottom.svg"));

            sheetExportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            sheetImportIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowRemoveIcon.setColorFilter(ThemeUtils.iconColorFilter);
            rowInsertIcon.setColorFilter(ThemeUtils.iconColorFilter);
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<GameFiles, DefaultSheetPanel> sheetPanels;
    private JPanel placeholder;

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
        placeholder = new JPanel();
        placeholder.setName("Test panel");

        GenericParser<PersonalData> parser = DataManager.getParser(PersonalData.class);
        List<PersonalData> data = parser.generateDataList(Collections.singletonMap(GameFiles.PERSONAL, new Narc(rom.getFileByName(GameFiles.PERSONAL.getPath()))));

        GenericParser<TextBankData> textParser = DataManager.getParser(TextBankData.class);
        List<TextBankData> textData = textParser.generateDataList(Collections.singletonMap(GameFiles.TEXT, new Narc(rom.getFileByName(GameFiles.TEXT.getPath()))));

        PersonalTable personalTable = new PersonalTable(data, textData);
        DefaultSheetPanel personalPanel = new DefaultSheetPanel(personalTable);
        personalPanel.setName("Personal");

        sheetPanels.put(GameFiles.PERSONAL, personalPanel);
    }

    @Override
    public List<JPanel> getPanels()
    {
        placeholder.setPreferredSize(dimension);
        placeholder.setMinimumSize(dimension);
        List<JPanel> panels = new ArrayList<>(sheetPanels.values());
        panels.add(placeholder);
        return panels;
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return false;
    }

    @Override
    public void doForwardsButtonAction(ActionEvent actionEvent)
    {

    }

    @Override
    public void doBackButtonAction(ActionEvent actionEvent)
    {

    }

    @Override
    public void doInfoButtonAction(ActionEvent actionEvent)
    {

    }
}
