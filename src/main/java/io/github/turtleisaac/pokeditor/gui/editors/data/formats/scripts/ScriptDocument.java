package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.formats.scripts.*;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.ScriptDataProducer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptDocument extends DefaultStyledDocument
{
    private int lineCount = 1;

    private final ScriptElementList scriptElementList;
    private final StyleContext context;

    private JTextPane lineNumberPane;

    public ScriptDocument(ScriptPane pane)
    {
        super(StyleContext.getDefaultStyleContext());
        context = (StyleContext) getAttributeContext();
        scriptElementList = new ScriptElementList();
        addStylesToDocument(this);
        pane.insertComponent(new JButton("test"));
        setLineNumberPane(pane.getLineNumberPane());
    }

    public ScriptData getScriptData() throws BadLocationException, ScriptDataProducer.ScriptCompilationException
    {
        ScriptDataProducer visitor = new ScriptDataProducer();

        return visitor.produceScriptData(getText(0, getLength()));
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
        super.insertString(offs, str, a);
        setSyntaxAttributes();
        int count = getDefaultRootElement().getElementCount();

        if (lineNumberPane != null)
        {
            Document numberDoc = lineNumberPane.getDocument();
            numberDoc.remove(0, numberDoc.getLength());

            for (int i = 0; i < count; i++)
            {
                numberDoc.insertString(numberDoc.getLength(), i  + "\n", null);
            }
        }

    }

    @Override
    public void remove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);
        setSyntaxAttributes();
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
        StyleConstants.setFontSize(def, 14);
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
                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, null, ElementType.LABEL));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(LABEL), true);
            }
            else if (labelNames.contains(ctx.getText()))
            {
                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, "A label with this name already exists"));
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
                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, null, ElementType.LABEL));
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(ACTION_OR_TABLE_LABEL), true);
            }
            else if (actionNames.contains(ctx.getText()))
            {
                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, "An action with this name already exists"));
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
                            scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, "You are missing a script number here"));
                            invalid = true;
                            this.invalid = true;
                        }
                        else
                        {
                            int scriptID = Integer.parseInt(terminalNode.getText());
                            if (scriptID == 0)
                            {
                                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, "You can't use index 0 for a script"));
                                invalid = true;
                                this.invalid = true;
                            }
                            else if (scriptNumbers.contains(scriptID))
                            {
                                scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, "This script ID number is already in use"));
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
                        for (CommandMacro macro : ScriptParser.commandMacros)
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
                    scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, commandMacro.toString(), ElementType.COMMAND));
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
                    scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, toolTipText, ElementType.COMMAND));
                    return null;
                }
            }
            else
            {
                toolTipText = String.format("'%s' is not a valid command", name);
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(UNKNOWN_COMMAND), true);
                this.invalid = true;
            }

            scriptElementList.add(new ElementRange(ctx.start.getStartIndex(), stopExclusive, toolTipText));

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
            return value >= min && value < maxExclusive - 1;
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
                return getText(min, maxExclusive - min + 1);
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
            boolean found = false;
            for (ElementRange existingRange : elementRanges)
            {
                if (existingRange.contains(newRange) && existingRange.getLength() != newRange.getLength())
                {
                    elementRanges.remove(existingRange);
                    elementRanges.add(newRange);
                    found = true;
                }
            }

            if (!found)
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
