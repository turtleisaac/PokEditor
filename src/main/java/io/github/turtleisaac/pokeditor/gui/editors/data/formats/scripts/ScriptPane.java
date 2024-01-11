package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ScriptPane extends JTextPane
{
    private ScriptDocument scriptDocument;
    private JTextPane lineNumberPane;
    private JScrollPane scrollPane;

    public ScriptPane()
    {
        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.isMetaDown() || e.isControlDown())
                {
                    int offset = viewToModel2D(getMousePosition());
                    highlight(offset);
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                try {
                    scriptDocument.setSyntaxAttributes();
                }
                catch(BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @Override
    public void setStyledDocument(StyledDocument doc)
    {
        super.setStyledDocument(doc);
    }

    public void setScriptDocument(ScriptDocument scriptDocument)
    {
        this.scriptDocument = scriptDocument;
        super.setStyledDocument(scriptDocument);
    }

    public ScriptDocument getScriptDocument()
    {
        return scriptDocument;
    }

    @Override
    public JToolTip createToolTip()
    {
        return super.createToolTip();
    }

    @Override
    public String getToolTipText(MouseEvent event)
    {
        if (scriptDocument == null)
            return null;

        int offset = viewToModel2D(event.getPoint());

        ScriptDocument.ElementRange elementRange = scriptDocument.getScriptElementList().find(offset);

        if (elementRange != null)
        {
            return elementRange.getToolTipText();
        }
        return null;
    }

    private void highlight(int offset)
    {
        ScriptDocument.ElementRange elementRange = scriptDocument.getScriptElementList().find(offset);
        if (elementRange != null && elementRange.getElementType() == ScriptDocument.ElementType.LABEL)
        {
            String text = null;
            try {
                text = scriptDocument.getText(0, scriptDocument.getLength());
            }
            catch(BadLocationException ex) {
                throw new RuntimeException(ex);
            }

            scriptDocument.setCharacterAttributes(elementRange.getMin(), elementRange.getMaxExclusive() - elementRange.getMin() + 1, scriptDocument.getStyle(ScriptDocument.GOTO_LABEL), true);
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e)
    {
        int modifiers = e.getModifiersEx();

        if (e.getClickCount() != 0)
        {
            getHighlighter().removeAllHighlights();
        }

        if ((modifiers & (MouseEvent.CTRL_DOWN_MASK | MouseEvent.META_DOWN_MASK)) != 0 && (modifiers & MouseEvent.SHIFT_DOWN_MASK) == 0)
        {
            int offset = viewToModel2D(e.getPoint());

            if (e.getClickCount() == 0)
            {
                highlight(offset);
                return;
            }

            ScriptDocument.ElementRange elementRange = scriptDocument.getScriptElementList().find(offset);
            if (elementRange != null && elementRange.getElementType() == ScriptDocument.ElementType.LABEL)
            {
                String text = null;
                try {
                    text = scriptDocument.getText(0, scriptDocument.getLength());
                }
                catch(BadLocationException ex) {
                    throw new RuntimeException(ex);
                }
                String labelName = text.substring(elementRange.getMin(), elementRange.getMaxExclusive());
                int definitionOffset = text.indexOf(labelName + ":");

//                JPopupMenu popupMenu = new JPopupMenu();
//                popupMenu.setPopupSize(500, 500);
//                popupMenu.show(this, 0, e.getY());

                if ((modifiers & MouseEvent.MOUSE_CLICKED) != 0 && e.getClickCount() == 1)
                {
                    gotoStartOfLine(this, getLineAtOffset(this, definitionOffset));
                    centerLineInScrollPane(this);
                    try {
                        scriptDocument.setSyntaxAttributes();

                        DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                        getHighlighter().addHighlight(definitionOffset, definitionOffset + labelName.length(), highlightPainter);
                    }
                    catch(BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        super.processMouseEvent(e);
    }

    public void setLineNumberPane(JTextPane lineNumberPane) throws BadLocationException
    {
        this.lineNumberPane = lineNumberPane;
        lineNumberPane.setFont(Font.getFont("Monospaced"));
        lineNumberPane.setEnabled(false);
        lineNumberPane.setEditable(false);
        Document doc = lineNumberPane.getDocument();
        doc.remove(0, doc.getLength());
    }

    public void setScrollPane(JScrollPane scrollPane)
    {
        this.scrollPane = scrollPane;
    }

    public JTextPane getLineNumberPane()
    {
        return lineNumberPane;
    }

    /*
     *  Position the caret at the start of a line.
     */
    public static void gotoStartOfLine(JTextComponent component, int line)
    {
        Element root = component.getDocument().getDefaultRootElement();
        line = Math.max(line, 1);
        line = Math.min(line, root.getElementCount());
        int startOfLineOffset = root.getElement( line - 1 ).getStartOffset();
        component.setCaretPosition( startOfLineOffset );
    }

    /*
     *  Return the line number at the Caret position.
     */
    public static int getLineAtCaret(JTextComponent component)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex( caretPosition ) + 1;
    }

    /*
     *  Return the line number at the Caret position.
     */
    public static int getLineAtOffset(JTextComponent component, int offset)
    {
//        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

//        for (int i = 0; i < root.getElementCount(); i++)
//        {
//            Element line = root.getElement(i);
//            if (line.getStartOffset() <= offset && line.getEndOffset() >= offset)
//            {
//
//            }
//        }

        return root.getElementIndex(offset) + 1;

//        return root.getElementIndex( caretPosition ) + 1;
    }

    /*
     *  Attempt to center the line containing the caret at the center of the
     *  scroll pane.
     *
     *  @param component the text component in the sroll pane
     */
    public static void centerLineInScrollPane(JTextComponent component)
    {
        Container container = SwingUtilities.getAncestorOfClass(JViewport.class, component);

        if (container == null) return;

        try
        {
            Rectangle r = component.modelToView(component.getCaretPosition());
            JViewport viewport = (JViewport)container;
            int extentHeight = viewport.getExtentSize().height;
            int viewHeight = viewport.getViewSize().height;

            int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
            y = Math.min(y, viewHeight - extentHeight);

            viewport.setViewPosition(new Point(0, y));
        }
        catch(BadLocationException ble) {}
    }
}
