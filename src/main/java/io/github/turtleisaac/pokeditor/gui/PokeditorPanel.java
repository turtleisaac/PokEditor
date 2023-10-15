/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui;

import java.awt.*;
import javax.swing.*;

import io.github.turtleisaac.NdsCommonBase.Tool;
import io.github.turtleisaac.NdsCommonBase.ToolPanel;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.gamedata.TextBanks;
import io.github.turtleisaac.pokeditor.gamedata.Game;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class PokeditorPanel extends ToolPanel
{
    private static final Dimension dimension = new Dimension(1200, 714);

    private NintendoDsRom rom;
    private Game baseRom;

    public PokeditorPanel(Tool tool)
    {
        super(tool);

        rom = tool.getRom();
        baseRom = Game.parseBaseRom(rom.getGameCode());
        GameFiles.initialize(baseRom);
        TextBanks.initialize(baseRom);

        initComponents();
        setPreferredSize(dimension);
        setMinimumSize(dimension);
        GenericParser<PersonalData> parser = DataManager.getParser(PersonalData.class);
        System.out.println(parser.getRequirements());


//        tabbedPane1.addTab("Personal", new DefaultSheetPanel(new PersonalTable(s.getData())));
//        tabbedPane1.addTab("Evolutions", new DefaultSheetPanel(new PersonalTable(s.getData())));
//        tabbedPane1.addTab("Moves", new DefaultSheetPanel(new PersonalTable(s.getData())));
        tabbedPane1.addTab("Trainer Editor", new JPanel());
        tabbedPane1.addTab("Encounter Editor", new JPanel());
        tabbedPane1.addTab("Pokémon Sprite Editor", new JPanel());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        tabbedPane1 = new JTabbedPane();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow]"));
        add(tabbedPane1, "grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    @Override
    public void doForwardsButtonAction()
    {

    }

    @Override
    public void doBackButtonAction()
    {

    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JTabbedPane tabbedPane1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
