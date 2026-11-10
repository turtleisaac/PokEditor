package io.github.turtleisaac.pokeditor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest
{
    @ParameterizedTest
    @ValueSource(strings = {
            "C:\\Program Files\\PokeHub\\PokeHub.exe",
            "C:\\Program Files\\PokeHub\\PokeHub.App.exe",
            "C:\\Program Files\\PokeHub\\pokehub.mcp.EXE",
            "D:\\tools\\pokehub.exe",
            "\\\\server\\share\\PokeHub.App.exe",
            "/Applications/PokeHub.app/Contents/MacOS/PokeHub",
            "/usr/local/bin/PokeHub.App",
            "/opt/pokehub/PokeHub.Mcp",
            "PokeHub",
            "PokeHub.App",
            "PokeHub.Mcp"
    })
    @DisplayName("matches PokeHub, PokeHub.App, and PokeHub.Mcp by executable base name")
    void matchesPokeHubExecutables(String command)
    {
        assertThat(Main.isPokeHub(command))
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "C:\\Windows\\System32\\notepad.exe",
            "C:\\PokeHub\\notepad.exe",
            "/Applications/PokeHub.app/Contents/MacOS/java",
            "/usr/bin/java",
            "PokeHubEditor",
            "NotPokeHub",
            "PokeHub.Mcp.Worker",
            ""
    })
    @DisplayName("rejects other executables, including ones stored inside a PokeHub directory")
    void rejectsOtherCommands(String command)
    {
        assertThat(Main.isPokeHub(command))
                .isFalse();
    }

    @Test
    @DisplayName("a missing command is not PokeHub")
    void rejectsNull()
    {
        assertThat(Main.isPokeHub(null))
                .isFalse();
    }
}
