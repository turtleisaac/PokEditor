package io.github.turtleisaac.pokeditor.framework;

public class SheetExceptionFactory
{
    /**
     * generates and returns a SheetException for use when a sheet has an invalid string value in a cell
     * @param classifier the kind of value which is incorrect (species, move, item, etc...)
     * @param invalid the string which was entered that is incorrect
     * @param lineIdentifier a string which identifies the line the error occurred on, usually the species or move name
     * @param line the line the error occurred on
     * @return a SheetException
     */
    public static SheetException generateInvalidNameSheetException(Class editor, String classifier, String invalid, String lineIdentifierClassifier, String lineIdentifier, int line)
    {
        SheetException e = new SheetException(editor.getSimpleName() + ": You have an invalid " + classifier + " name entered, \"" + invalid + "\", for " + classifier + " \"" + lineIdentifier + "\" on line #" + line);
        StackTraceElement[] arr = e.getStackTrace();
        System.arraycopy(arr, 1, arr, 0, arr.length - 1);
        e.setStackTrace(arr);
        return e;
    }

    /**
     * generates and returns a SheetException for use when a sheet has a missing value
     * @param classifier the kind of value for which the paired data is missing (species, move, item, level, etc...)
     * @param missingClassifier the kind of value which is missing
     * @param pairedData the string paired with the data which was missing (for example, if a move was missing a level in a learnset, it would be the move name)
     * @param lineIdentifierClassifier the kind of value which the line identifier is (for example, a species or move name)
     * @param lineIdentifier a string which identifies the line the error occurred on, usually the species or move name
     * @param line the line the error occurred on
     * @return a SheetException
     */
    public static SheetException generateMissingValueSheetException(Class editor, String classifier, String missingClassifier, String pairedData, String lineIdentifierClassifier, String lineIdentifier, int line)
    {
        SheetException e = new SheetException(editor.getSimpleName() + ": You are missing a defined " + missingClassifier + " for " + classifier + " \"" + pairedData + "\" for " + lineIdentifierClassifier + " \"" + lineIdentifier + "\" on line #" + line);
        StackTraceElement[] arr = e.getStackTrace();
        System.arraycopy(arr, 1, arr, 0, arr.length - 1);
        e.setStackTrace(arr);
        return e;
    }
}
