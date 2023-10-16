package io.github.turtleisaac.pokeditor.gui;

import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.ui.PanelManager;
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
import java.util.*;
import java.util.List;

public class PokeditorManager extends PanelManager
{
    private static final Dimension dimension = new Dimension(1200, 714);

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
