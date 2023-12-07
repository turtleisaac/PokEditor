package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.formats.scripts.*;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.CommandMacro;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import javax.swing.text.*;
import java.awt.*;

public class ScriptDocument extends DefaultStyledDocument
{
    static final Color ORANGE = new Color(185, 125, 25);
    static final Color BLUE = new Color(25, 148, 185);
    static final Color YELLOW = new Color(180, 185, 25);
    static final Color GREEN = new Color(75, 194, 94);
    static final Color RED = new Color(250, 92, 48);

    public ScriptDocument()
    {
        addStylesToDocument(this);
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
        super.insertString(offs, str, a);

        ScriptFileLexer lexer = new ScriptFileLexer(CharStreams.fromString(getText(0, getLength())));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScriptFileParser parser = new ScriptFileParser(tokens);

        ScriptFileVisitor visitor = new ScriptFileVisitor();
        visitor.visitScript_file(parser.script_file());
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException
    {
        super.remove(offs, len);

        ScriptFileLexer lexer = new ScriptFileLexer(CharStreams.fromString(getText(0, getLength())));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ScriptFileParser parser = new ScriptFileParser(tokens);

        ScriptFileVisitor visitor = new ScriptFileVisitor();
        visitor.visitScript_file(parser.script_file());
    }

    protected void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        StyleConstants.setFontSize(def, 14);
        StyleConstants.setFontFamily(def, "SansSerif");

        Style command = doc.addStyle(COMMAND, regular);
        StyleConstants.setForeground(command, BLUE);
        StyleConstants.setItalic(command, true);

        Style s = doc.addStyle(INCORRECT, command);
        StyleConstants.setBackground(s, RED);

        s = doc.addStyle(UNKNOWN_COMMAND, command);
        StyleConstants.setForeground(s, RED);

        s = doc.addStyle(PARAMETER, regular);
        StyleConstants.setForeground(s, YELLOW);

        s = doc.addStyle(LABEL, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, ORANGE);

        s = doc.addStyle(SCRIPT, regular);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, GREEN);
    }

    static final String UNKNOWN_COMMAND = "unknown_command";
    static final String COMMAND = "command";
    static final String INCORRECT = "incorrect";
    static final String PARAMETER = "parameter";
    static final String LABEL = "label";
    static final String SCRIPT = "script";

    class ScriptFileVisitor extends ScriptFileBaseVisitor<Void>
    {
        @Override
        public Void visitLabel_definition(ScriptFileParser.Label_definitionContext ctx)
        {
            int len = ctx.stop.getStopIndex() - ctx.start.getStartIndex();
            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(LABEL), true);
            return super.visitLabel_definition(ctx);
        }

        @Override
        public Void visitScript_definition(ScriptFileParser.Script_definitionContext ctx)
        {
            int len = ctx.stop.getStopIndex() - ctx.start.getStartIndex();
            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(SCRIPT), true);
            return super.visitScript_definition(ctx);
        }

        @Override
        public Void visitCommand(ScriptFileParser.CommandContext ctx)
        {
            int len = ctx.stop.getStopIndex() - ctx.start.getStartIndex();
            CommandMacro commandMacro = null;
            for (CommandMacro macro : ScriptParser.commandMacros)
            {
                if (macro.getName().equals(ctx.getChild(0).getText()))
                {
                    commandMacro = macro;
                    break;
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
                    return super.visitCommand(ctx);
                }
                else
                {
                    setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(INCORRECT), true);
                }
            }
            else
            {
                setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(UNKNOWN_COMMAND), true);
            }

            return null;
        }

        @Override
        public Void visitParameters(ScriptFileParser.ParametersContext ctx)
        {
            int len = ctx.stop.getStopIndex() - ctx.start.getStartIndex() + 1;
            setCharacterAttributes(ctx.start.getStartIndex(), len, getStyle(PARAMETER), false);
            return super.visitParameters(ctx);
        }
    }
}
