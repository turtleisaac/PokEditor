package io.github.turtleisaac.variabletracker;

import java.util.ArrayList;
import java.util.List;

public class ScriptVariable
{
    private int variableID = -1;
    private String variableName = "";
    private String variableDescription = "";
    private boolean temp = false;

    private List<VariableValue> variableValues = new ArrayList<>();

    public ScriptVariable() {}

    public ScriptVariable(int variableID)
    {
        this.variableID = variableID;
    }

    public ScriptVariable(String variableName, int variableID)
    {
        this.variableName = variableName;
        this.variableID = variableID;
    }

    public int getVariableID()
    {
        return variableID;
    }

    public void setVariableID(int variableID)
    {
        this.variableID = variableID;
    }

    public String getVariableName()
    {
        return variableName;
    }

    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    public String getVariableDescription()
    {
        return variableDescription;
    }

    public void setVariableDescription(String variableDescription)
    {
        this.variableDescription = variableDescription;
    }

    public VariableValue createVariableValue()
    {
        return new VariableValue();
    }

    public VariableValue createVariableValue(int value)
    {
        return new VariableValue(value);
    }

    public List<VariableValue> getVariableValues()
    {
        return variableValues;
    }

    public void setVariableValues(List<VariableValue> variableValues)
    {
        this.variableValues = variableValues;
    }

    public boolean isNotTemp()
    {
        return !temp;
    }

    public void setTemp(boolean temp)
    {
        this.temp = temp;
    }

    @Override
    public String toString()
    {
        if (variableName.isEmpty())
        {
            return String.format("0x%s", Integer.toHexString(variableID).toUpperCase());
        }
        return String.format("%s (0x%s)", variableName, Integer.toHexString(variableID).toUpperCase()).trim();
    }

    public static class VariableValue
    {
        private int value = -1;
        private String valueName = "";
        private String valueDescription = "";

        public VariableValue() {}

        public VariableValue(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return value;
        }

        public void setValue(int value)
        {
            this.value = value;
        }

        public String getValueName()
        {
            return valueName;
        }

        public void setValueName(String valueName)
        {
            this.valueName = valueName;
        }

        public String getValueDescription()
        {
            return valueDescription;
        }

        public void setValueDescription(String valueDescription)
        {
            this.valueDescription = valueDescription;
        }

//        @Override
//        public String toString()
//        {
//            String varName = variableName.isEmpty() ? "0x" + Integer.toHexString(variableID).toUpperCase() : variableName;
//            String valName = valueName.isEmpty() ? String.valueOf(value) : valueName;
//            return String.format("%s.%s (%d)", varName, valName, value);
//        }
    }
}
