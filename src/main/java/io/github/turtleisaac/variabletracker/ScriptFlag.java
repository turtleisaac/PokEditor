package io.github.turtleisaac.variabletracker;

public class ScriptFlag
{
    private int flagID;
    private String flagName;
    private String flagDescription;

    public ScriptFlag(int flagID)
    {
        this.flagID = flagID;
    }

    public ScriptFlag(int flagID, String flagName)
    {
        this.flagID = flagID;
        this.flagName = flagName;
    }

    public int getFlagID()
    {
        return flagID;
    }

    public void setFlagID(int flagID)
    {
        this.flagID = flagID;
    }

    public String getFlagName()
    {
        return flagName;
    }

    public void setFlagName(String flagName)
    {
        this.flagName = flagName;
    }

    public String getFlagDescription()
    {
        return flagDescription;
    }

    public void setFlagDescription(String flagDescription)
    {
        this.flagDescription = flagDescription;
    }
}
