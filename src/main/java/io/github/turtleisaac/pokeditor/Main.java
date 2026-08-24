package io.github.turtleisaac.pokeditor;

import com.formdev.flatlaf.intellijthemes.*;
import io.github.turtleisaac.nds4j.ui.ProgramType;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.ConsoleWindow;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.InputStream;
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
        installUncaughtExceptionHandler();

        // The jokes file is decoration for the start screen, and it is not in the repository -
        // Main has referenced it since before this branch, so a clean checkout dereferences null
        // here and the application cannot start at all. Nothing cosmetic should be able to do
        // that, whether the file is restored later or not.
        String[] mainMenuJokes;
        try (InputStream jokesStream = Main.class.getResourceAsStream(jokesPath))
        {
            mainMenuJokes = jokesStream == null
                    ? new String[] {""}
                    : new String(jokesStream.readAllBytes(), StandardCharsets.UTF_8).split("\n");
        }

//        Locale.setDefault(Locale.CHINA);
        Tool tool = Tool.create();
        tool.setType(ProgramType.PROJECT)
                .setName("PokEditor")
                .setVersion("3.2.0")
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

    /**
     * Without this, every exception which escapes onto the EDT (including the validation
     * failures thrown by the data classes' setters) is only ever printed to a command line
     * that a user running a double-clicked jar never sees.
     */
    private static void installUncaughtExceptionHandler()
    {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();

            String message = throwable.getMessage();
            if (message == null || message.isBlank())
                message = throwable.getClass().getSimpleName();

            final String finalMessage = message;
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                    "An unexpected error occurred:\n" + finalMessage + "\n\nSee the command-line for details.",
                    "PokEditor", JOptionPane.ERROR_MESSAGE));
        });
    }
}