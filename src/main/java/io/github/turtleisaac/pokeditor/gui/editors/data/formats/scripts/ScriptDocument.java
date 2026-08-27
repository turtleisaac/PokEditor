package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.formats.scripts.*;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.ScriptDataProducer;
import io.github.turtleisaac.variabletracker.ScriptVariable;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptDocument extends DefaultStyledDocument
{
    private int lineCount = 1;

    private static final int FONT_SIZE = 14;

    private final ScriptElementList scriptElementList;
    private final StyleContext context;

    private JTextPane lineNumberPane;

    private List<ScriptVariable> variableList;

    /**
     * Re-lexing and re-parsing the whole file with ANTLR (and issuing one setCharacterAttributes
     * per token) is far too expensive to do synchronously on every keystroke, so the work is
     * coalesced behind this timer.
     */
    private final Timer syntaxTimer;

    private static final int SYNTAX_UPDATE_DELAY_MS = 250;

    public ScriptDocument(ScriptPane pane)
    {
        super(StyleContext.getDefaultStyleContext());
        context = (StyleContext) getAttributeContext();
        scriptElementList = new ScriptElementList();
        addStylesToDocument(this);

        syntaxTimer = new Timer(SYNTAX_UPDATE_DELAY_MS, e -> {
            try {
                setSyntaxAttributes();
                updateLineNumbers();
            }
            catch(BadLocationException ex) {
                ex.printStackTrace();
            }
            catch(RuntimeException ex) {
                // a syntax highlighter must never take the editor down over bad syntax, and
                // half-finished text is bad syntax by definition - it is what typing looks like.
                // ANTLR's error recovery can match a rule against zero tokens (the 14 characters
                // "script(script(" are enough), which produces a zero width ElementRange and an
                // unchecked throw. That escaped this handler and reached the EDT, where a user
                // running a double-clicked jar would never even see the stack trace.
                ex.printStackTrace();
            }
        });
        syntaxTimer.setRepeats(false);

        setLineNumberPane(pane.getLineNumberPane());
    }

    private void scheduleSyntaxUpdate()
    {
        if (syntaxTimer != null)
            syntaxTimer.restart();
    }

    public void setVariableList(List<ScriptVariable> variableList)
    {
        this.variableList = variableList;
    }

    public FieldScriptData getScriptData() throws BadLocationException, ScriptDataProducer.ScriptCompilationException
    {
        ScriptDataProducer visitor = new ScriptDataProducer();

        return visitor.produceScriptData(replaceVariableNamesWithNumbers(getText(0, getLength())));
    }

    /**
     * Variable names are a display-only convenience - the compiler's parameter resolver only
     * understands the raw values, so they have to be turned back into their hex IDs before the
     * text is compiled.
     */
    public String replaceVariableNamesWithNumbers(String text)
    {
        if (variableList == null || variableList.isEmpty())
            return text;

        String result = text;
        for (ScriptVariable variable : variableList)
        {
            String name = variable.getVariableName();
            if (name == null || name.isBlank())
                continue;

            result = result.replaceAll("\\b" + Pattern.quote(name) + "\\b",
                    Matcher.quoteReplacement("0x" + Integer.toHexString(variable.getVariableID())));
        }

        return result;
    }

    /**
     * The inverse of {@link #replaceVariableNamesWithNumbers(String)} - applied when building
     * the text shown to the user, so the shared model keeps holding Integers.
     */
    public String replaceVariableNumbersWithNames(String text)
    {
        if (variableList == null || variableList.isEmpty())
            return text;

        String result = text;
        for (ScriptVariable variable : variableList)
        {
            String name = variable.getVariableName();
            if (name == null || name.isBlank())
                continue;

            String hex = Integer.toHexString(variable.getVariableID());
            result = result.replaceAll("\\b0[xX]0*" + hex + "\\b", Matcher.quoteReplacement(name));
        }

        return result;
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
        super.insertString(offs, str, a);
        // the element ranges describe the text as it was before this edit, and the re-highlight
        // is debounced by a quarter of a second. leaving them in place means the pane answers
        // hovers and ctrl-clicks from offsets that no longer exist - after a delete, find(0)
        // could hand back a 13 character range over a 2 character document, and reading its
        // text threw straight onto the EDT. no ranges at all is the honest answer until the
        // visitor has run again; every caller of find() already handles null.
        //
        // only when the text actually moved, though: an insert of nothing leaves every offset
        // valid, and discarding the ranges anyway would blank the tooltips and ctrl-click
        // targets for a quarter of a second in response to an edit that changed nothing.
        if (str != null && !str.isEmpty())
            scriptElementList.clear();
        scheduleSyntaxUpdate();
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);
        // see insertString: stale ranges outlive the text they describe for the debounce window,
        // but a removal of zero characters moves nothing and must leave them alone
        if (len > 0)
            scriptElementList.clear();
        scheduleSyntaxUpdate();
    }

    protected void setSyntaxAttributes() throws BadLocationException
    {
        ScriptFileLexer lexer = new ScriptFileLexer(CharStreams.fromString(getText(0, getLength())));
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScriptFileParser parser = new ScriptFileParser(tokens);
        parser.removeErrorListeners();

        ScriptFileSyntaxVisitor visitor = new ScriptFileSyntaxVisitor();
        visitor.visitScript_file(parser.script_file());
    }

    private void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = context.getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontSize(def, FONT_SIZE);
        StyleConstants.setFontFamily(regular, "Monospaced");
//        StyleConstants.setTabSet(regular, tabSet);
        StyleConstants.setLeftIndent(regular, 100);
//        StyleConstants.setFirstLineIndent(regular, -100);

        Style command = doc.addStyle(COMMAND, regular);
        StyleConstants.setForeground(command, BLUE);

        Style s = doc.addStyle(INCORRECT, command);
        StyleConstants.setBackground(s, RED);

        s = doc.addStyle(UNKNOWN_COMMAND, command);
        StyleConstants.setForeground(s, RED);

        s = doc.addStyle(ACTION_COMMAND, regular);
        StyleConstants.setForeground(s, PURPLE);

        s = doc.addStyle(PARAMETER, regular);
        StyleConstants.setForeground(s, YELLOW);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle(LABEL, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, ORANGE);

        s = doc.addStyle(ACTION_OR_TABLE_LABEL, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, PINK);

        s = doc.addStyle(SCRIPT, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, GREEN);

        s = doc.addStyle(GOTO_LABEL, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setUnderline(s, true);
    }

    public ScriptElementList getScriptElementList()
    {
        return scriptElementList;
    }

    public void setLineNumberPane(JTextPane lineNumberPane)
    {
        this.lineNumberPane = lineNumberPane;
        if (lineNumberPane != null)
        {
            StyledDocument document = new DefaultStyledDocument();
            lineNumberPane.setStyledDocument(document);

            Style def = context.getStyle(StyleContext.DEFAULT_STYLE);

            StyleConstants.setFontSize(def, FONT_SIZE);
            StyleConstants.setFontFamily(def, "Monospaced");

            updateLineNumbers();
        }
    }

    /**
     * Rebuilds the line number gutter so it matches this document's actual line count.
     * Script lines are numbered from 1 (they used to start at 0, and the gutter was hardcoded
     * to 2000 entries regardless of the file).
     */
    private void updateLineNumbers()
    {
        if (lineNumberPane == null)
            return;

        int lineCount = Math.max(1, getDefaultRootElement().getElementCount());

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= lineCount; i++)
        {
            builder.append(i).append("\n");
        }

        Document numberDoc = lineNumberPane.getDocument();
        try {
            numberDoc.remove(0, numberDoc.getLength());
            numberDoc.insertString(0, builder.toString(), null);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    static final Color ORANGE = new Color(185, 125, 25);
    static final Color BLUE = new Color(25, 148, 185);
    static final Color YELLOW = new Color(180, 185, 25);
    static final Color GREEN = new Color(75, 194, 94);
    static final Color RED = new Color(250, 92, 48);
    static final Color PURPLE = new Color(196, 82, 252);
    static final Color PINK = new Color(239, 163, 255);

    static final String UNKNOWN_COMMAND = "unknown_command";
    static final String COMMAND = "command";
    static final String INCORRECT = "incorrect";
    static final String PARAMETER = "parameter";
    static final String LABEL = "label";
    static final String SCRIPT = "script";
    static final String GOTO_LABEL = "goto_label";
    static final String ACTION_COMMAND = "action_command";
    static final String ACTION_OR_TABLE_LABEL = "action_label";

    class ScriptFileSyntaxVisitor extends ScriptFileBaseVisitor<Void>
    {
        private List<Integer> scriptNumbers;
        private List<String> labelNames;
        private List<String> actionNames;

        private boolean invalid = false;

        public boolean wasSuccessful()
        {
            return !invalid;
        }

        /** the arguments of one candidate range, so the guard below sees them before construction */
        private record RangeSpec(int min, int maxExclusive, String toolTipText, ElementType elementType)
        {
            RangeSpec(int min, int maxExclusive, String toolTipText)
            {
                this(min, maxExclusive, toolTipText, null);
            }
        }

        /**
         * Records a range, unless it covers no characters.
         * <p>
         * ANTLR's error recovery can match a rule against zero tokens while the file is being
         * typed - "script(script(" is enough - and the context it hands back then reports a stop
         * index one before its start. ElementRange rightly refuses to be empty, but that refusal
         * is an unchecked throw from inside the highlighter, so half-typed text used to take the
         * whole visitor down. There is nothing to highlight in zero characters, so skip it.
         */
        private void addRange(RangeSpec spec)
        {
            if (spec.maxExclusive() <= spec.min())
                return;

            scriptElementList.add(spec.elementType() == null
                    ? new ElementRange(spec.min(), spec.maxExclusive(), spec.toolTipText())
                    : new ElementRange(spec.min(), spec.maxExclusive(), spec.toolTipText(), spec.elementType()));
        }

        @Override
        public Void visitScript_file(ScriptFileParser.Script_fileContext ctx)
        {
            scriptElementList.clear();
            scriptNumbers = new ArrayList<>();
            labelNames = new ArrayList<>();
            actionNames = new ArrayList<>();
            return super.visitScript_file(ctx);
        }

        @Override
        public Void visitLabel_definition(ScriptFileParser.Label_definitionContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(LABEL), true);
            return super.visitLabel_definition(ctx);
        }

        @Override
        public Void visitLabel(ScriptFileParser.LabelContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            if (!(ctx.parent instanceof ScriptFileParser.Label_definitionContext))
            {
                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, null, ElementType.LABEL));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(LABEL), true);
            }
            else if (labelNames.contains(ctx.getText()))
            {
                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "A label with this name already exists"));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(INCORRECT), true);
                this.invalid = true;
            }
            else
            {
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(LABEL), true);
                labelNames.add(ctx.getText());
            }

            return super.visitLabel(ctx);
        }

        @Override
        public Void visitAction_definition(ScriptFileParser.Action_definitionContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
            return super.visitAction_definition(ctx);
        }

        @Override
        public Void visitTable_definition(ScriptFileParser.Table_definitionContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
            return super.visitTable_definition(ctx);
        }

        @Override
        public Void visitTable_entry(ScriptFileParser.Table_entryContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_COMMAND), true);
            return super.visitTable_entry(ctx);
        }

        @Override
        public Void visitEnd_table(ScriptFileParser.End_tableContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
            return super.visitEnd_table(ctx);
        }

        @Override
        public Void visitAction(ScriptFileParser.ActionContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            if (!(ctx.parent instanceof ScriptFileParser.Action_definitionContext))
            {
                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, null, ElementType.LABEL));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
            }
            else if (actionNames.contains(ctx.getText()))
            {
                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "An action with this name already exists"));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(INCORRECT), true);
                this.invalid = true;
            }
            else
            {
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
                actionNames.add(ctx.getText());
            }

            return super.visitAction(ctx);
        }

        @Override
        public Void visitScript_definition(ScriptFileParser.Script_definitionContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            boolean invalid = false;
            for (ParseTree child : ctx.children)
            {
                if (child instanceof TerminalNodeImpl terminalNode)
                {
                    if (terminalNode.symbol.getType() == ScriptFileLexer.NUMBER)
                    {
                        if (terminalNode.symbol.getStartIndex() == -1)
                        {
                            addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "You are missing a script number here"));
                            invalid = true;
                            this.invalid = true;
                        }
                        else
                        {
                            int scriptID = Integer.parseInt(terminalNode.getText());
                            if (scriptID == 0)
                            {
                                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "You can't use index 0 for a script"));
                                invalid = true;
                                this.invalid = true;
                            }
                            else if (scriptNumbers.contains(scriptID))
                            {
                                addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "This script ID number is already in use"));
                                invalid = true;
                                this.invalid = true;
                            }
                            else
                            {
                                scriptNumbers.add(scriptID);
                            }
                        }

                        break;
                    }
                }
            }

            if (!invalid)
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(SCRIPT), true);
            else
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(INCORRECT), true);

            return super.visitScript_definition(ctx);
        }

        @Override
        public Void visitCommand(ScriptFileParser.CommandContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            String toolTipText;

            String name = "";

            CommandMacro commandMacro = null;
            for (ParseTree child : ctx.children)
            {
                if (child instanceof TerminalNodeImpl terminalNode)
                {
                    if (terminalNode.symbol.getType() == ScriptFileLexer.NAME)
                    {
                        name = terminalNode.getText();
                        for (CommandMacro macro : FieldScriptParser.commandMacros)
                        {
                            if (macro.getName().equals(terminalNode.getText()))
                            {
                                commandMacro = macro;
                                break;
                            }
                        }
                    }
                }
            }


            if (commandMacro != null)
            {
                int paramCount = 0;
                for (ParseTree child : ctx.children)
                {
                    if (child instanceof ScriptFileParser.ParametersContext parametersContext)
                    {
                        if (parametersContext.children != null)
                        {
                            for (ParseTree parameter : parametersContext.children)
                            {
                                if (parameter instanceof ScriptFileParser.ParameterContext)
                                {
                                    paramCount++;
                                }
                            }
                        }
                    }
                }

                String[] parameters = commandMacro.getParameters();
                int actualCount = parameters != null ? parameters.length : 0;
                if (paramCount == actualCount)
                {
                    setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(COMMAND), true);
                    addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, commandMacro.toString(), ElementType.COMMAND));
                    return super.visitCommand(ctx);
                }
                else
                {
                    this.invalid = true;
                    StringBuilder text = new StringBuilder();
                    if (paramCount > actualCount)
                    {
                        text.append("You have too many parameters for this command");
                    }
                    else
                    {
                        text.append("You have too few parameters for this command");
                    }
                    text.append("\n").append(commandMacro);

                    toolTipText = text.toString();

                    setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(INCORRECT), true);
                    addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, toolTipText, ElementType.COMMAND));
                    return null;
                }
            }
            else
            {
                toolTipText = String.format("'%s' is not a valid command", name);
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(UNKNOWN_COMMAND), true);
                this.invalid = true;
            }

            addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, toolTipText));

            return null;
        }

        @Override
        public Void visitParameters(ScriptFileParser.ParametersContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(PARAMETER), false);
            return super.visitParameters(ctx);
        }

        @Override
        public Void visitParameter(ScriptFileParser.ParameterContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
//            int len = stopExclusive - ctx.start.getStartIndex();

            if (variableList != null)
            {
                for (ScriptVariable variable : variableList)
                {
                    if (variable.getVariableName().equalsIgnoreCase(ctx.getText().trim()))
                    {
                        addRange(new RangeSpec(ctx.start.getStartIndex(), stopExclusive, "0x" + Integer.toHexString(variable.getVariableID()).toUpperCase()));
                        break;
                    }
                }
            }

            return super.visitParameter(ctx);
        }

        @Override
        public Void visitAction_command(ScriptFileParser.Action_commandContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_COMMAND), false);

            return super.visitAction_command(ctx);
        }

        @Override
        public Void visitAction_parameters(ScriptFileParser.Action_parametersContext ctx)
        {
            int stopExclusive = ctx.stop.getStopIndex() + 1;
            int len = stopExclusive - ctx.start.getStartIndex();

            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(PARAMETER), false);
            return super.visitAction_parameters(ctx);
        }
    }

    public enum ElementType {
        LABEL,
        COMMAND,
        IGNORED
    }

    public class ElementRange
    {
        private final int min;
        private final int maxExclusive;
        private String toolTipText;
        private final ElementType elementType;

        public ElementRange(int min, int maxExclusive, String toolTipText)
        {
            if (maxExclusive <= min)
            {
                throw new RuntimeException(String.format("maxExclusive (%d) must be greater than min (%d)", maxExclusive, min));
            }
            this.min = min;
            this.maxExclusive = maxExclusive;
            this.toolTipText = toolTipText;
            this.elementType = ElementType.IGNORED;
        }

        public ElementRange(int min, int maxExclusive, String toolTipText, ElementType elementType)
        {
            if (maxExclusive <= min)
            {
                throw new RuntimeException(String.format("maxExclusive (%d) must be greater than min (%d)", maxExclusive, min));
            }
            this.min = min;
            this.maxExclusive = maxExclusive;
            this.toolTipText = toolTipText;
            this.elementType = elementType;
        }

        public int getLength()
        {
            return maxExclusive - min;
        }

        public boolean contains(int value)
        {
            return value >= min && value < maxExclusive;
        }

        public boolean contains(ElementRange range)
        {
            return contains(range.min) && (contains(range.maxExclusive-1) || range.maxExclusive == maxExclusive);
        }

        public String getToolTipText()
        {
            return toolTipText;
        }

        public ElementType getElementType()
        {
            return elementType;
        }

        public int getMin()
        {
            return min;
        }

        public int getMaxExclusive()
        {
            return maxExclusive;
        }

        @Override
        public String toString()
        {
            try {
                // the range is half open, so its length is exactly maxExclusive - min. the
                // extra +1 read one character too many: it never returned the range's own
                // text, and at the end of the document it returned the implicit trailing
                // newline instead of failing, which is why it went unnoticed.
                return getText(min, maxExclusive - min);
            }
            catch(BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ScriptElementList
    {
        private ArrayList<ElementRange> elementRanges = new ArrayList<>();

        public void add(ElementRange newRange)
        {
            // a range nested inside a wider one goes to the front, so that find(offset) - which
            // scans in order and returns the first match - answers with the innermost element
            // rather than whichever enclosing one happens to come first.
            //
            // the insertion has to happen once, not once per enclosing range. it used to sit
            // inside the loop, so a range nested two deep was stored twice and one nested three
            // deep three times; the list grew with nesting depth and find() could return a
            // duplicate. today the visitor only nests two levels, which is why this stayed
            // invisible, but the list is public API.
            boolean nested = false;
            for (ElementRange existingRange : elementRanges)
            {
                if (existingRange.contains(newRange) && existingRange.getLength() != newRange.getLength())
                {
                    nested = true;
                    break;
                }
            }

            if (nested)
            {
                elementRanges.add(0, newRange);
            }
            else
            {
                elementRanges.add(newRange);
            }
        }

        public void clear()
        {
            elementRanges.clear();
        }

        public ElementRange find(int offset)
        {
            for (ElementRange range : elementRanges)
            {
                if (range.contains(offset))
                {
                    return range;
                }
            }

            return null;
        }
    }
}
