package io.github.turtleisaac.variabletracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.formdev.flatlaf.FlatDarkLaf;
import io.github.turtleisaac.variabletracker.gui.variables.VariableTracker;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    private static final String sourceData = "[ {\n" +
            "  \"variableID\" : 16416,\n" +
            "  \"variableName\" : \"VAR_TUTORIAL_PROGRESS\",\n" +
            "  \"variableDescription\" : \"\",\n" +
            "  \"temp\" : false,\n" +
            "  \"variableValues\" : [ {\n" +
            "    \"value\" : 0,\n" +
            "    \"valueName\" : \"INCOMPLETE\",\n" +
            "    \"valueDescription\" : \"\"\n" +
            "  } ]\n" +
            "}, {\n" +
            "  \"variableID\" : 16417,\n" +
            "  \"variableName\" : \"VAR_THING_2\",\n" +
            "  \"variableDescription\" : \"\",\n" +
            "  \"temp\" : false,\n" +
            "  \"variableValues\" : [ {\n" +
            "    \"value\" : 0,\n" +
            "    \"valueName\" : \"INCOMPLETE\",\n" +
            "    \"valueDescription\" : \"\"\n" +
            "  }, {\n" +
            "    \"value\" : 1,\n" +
            "    \"valueName\" : \"FIRST_STEP\",\n" +
            "    \"valueDescription\" : \"\"\n" +
            "  }, {\n" +
            "    \"value\" : 2,\n" +
            "    \"valueName\" : \"COMPLETE\",\n" +
            "    \"valueDescription\" : \"\"\n" +
            "  } ]\n" +
            "}, {\n" +
            "  \"variableID\" : 16418,\n" +
            "  \"variableName\" : \"VAR_THING_3\",\n" +
            "  \"variableDescription\" : \"\",\n" +
            "  \"temp\" : false,\n" +
            "  \"variableValues\" : [ ]\n" +
            "} ]";

    public static void main(String[] args)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<ScriptVariable> variableList = null;

        try {
            variableList = new ArrayList<>(Arrays.asList(objectMapper.readValue(sourceData, ScriptVariable[].class)));
        }
        catch(JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        System.setProperty( "apple.laf.useScreenMenuBar", "true" );

        FlatDarkLaf.install();
        JFrame frame = new JFrame("Variable Tracker");
        VariableTracker variableTracker = new VariableTracker(variableList);
        frame.setContentPane(variableTracker);
        frame.setJMenuBar(variableTracker.getMenuBar());
//        frame.add(variableTracker.getMenuBar1())
        frame.setVisible(true);
        frame.pack();
    }

    public static <T> T fromJSON(final TypeReference<T> type,
                                 final String jsonPacket) {
        T data = null;

        try {
            data = new ObjectMapper().readValue(jsonPacket, type);
        } catch (Exception e) {
            // Handle the problem
        }
        return data;
    }
}
