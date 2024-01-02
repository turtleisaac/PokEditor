package io.github.turtleisaac.pokeditor;

import com.formdev.flatlaf.intellijthemes.*;
import io.github.turtleisaac.nds4j.ui.ProgramType;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.ConsoleWindow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author turtleisaac
 */
public class Main
{
    private static ConsoleWindow console;
    private static final String jokesPath = "/pokeditor/jokes.txt";

    public static void main(String[] args) throws IOException
    {
        String[] mainMenuJokes = new String(Main.class.getResourceAsStream(jokesPath).readAllBytes(), StandardCharsets.UTF_8).split("\n");

//        Locale.setDefault(Locale.CHINA);
        Tool tool = Tool.create();
        tool.setType(ProgramType.PROJECT)
                .setName("PokEditor")
                .setVersion("3.1.1")
//                .setFlavorText("Did you know that Jay likes Moemon?")
                .setFlavorText(mainMenuJokes[(int) (Math.random()*(mainMenuJokes.length))])
                .setAuthor("Developed by Turtleisaac")
                .setGitEnabled(true)
                .addLookAndFeel(new FlatArcOrangeIJTheme())
                .addLookAndFeel(new FlatDarkPurpleIJTheme())
//                .addLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel())
                .addGame("Pokémon Platinum", "CPU")
                .addGame("Pokémon HeartGold","IPK")
                .addGame("Pokémon SoulSilver","IPG")
                .addPanelManager(() -> new PokeditorManager(tool))
                .init();
    }
}