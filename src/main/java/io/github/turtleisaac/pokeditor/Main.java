package io.github.turtleisaac.pokeditor;

import javax.swing.*;

import com.formdev.flatlaf.intellijthemes.*;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme;
import io.github.turtleisaac.nds4j.ui.ProgramType;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.nds4j.ui.exceptions.ToolCreationException;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.console.ConsoleWindow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * @author turtleisaac
 */
public class Main
{
    public static final String versionNumber = "v3snapshot";
    public static final boolean programFilesModifiedThisVersion = true;
    private static ConsoleWindow console;
    private static final String jokesPath = "/pokeditor/jokes.txt";

    public static void main(String[] args) throws IOException
    {
        String[] mainMenuJokes = new String(Main.class.getResourceAsStream(jokesPath).readAllBytes(), StandardCharsets.UTF_8).split("\n");

//        Locale.setDefault(Locale.CHINA);
        Tool tool = Tool.create();
        tool.setType(ProgramType.PROJECT)
                .setName("PokEditor")
                .setVersion("3.0.0-SNAPSHOT")
//                .setFlavorText("Did you know that Jay likes Moemon?")
                .setFlavorText(mainMenuJokes[(int) (Math.random()*(mainMenuJokes.length))])
                .setAuthor("Developed by Turtleisaac")
                .addLookAndFeel(new FlatDarkPurpleIJTheme())
                .addLookAndFeel(new FlatArcOrangeIJTheme())
//                .addLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel())
                .addGame("Pokémon Platinum", "CPU")
                .addGame("Pokémon HeartGold","IPK")
                .addGame("Pokémon SoulSilver","IPG")
                .addPanelManager(() -> new PokeditorManager(tool))
                .init();
    }
}