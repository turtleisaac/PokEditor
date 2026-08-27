/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.field;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import io.github.turtleisaac.nds4j.ui.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.FieldScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.LevelScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.ScriptDataProducer;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditor;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditorPanel;
import io.github.turtleisaac.pokeditor.gui.editors.data.EditorDataModel;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.*;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptDocument;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import io.github.turtleisaac.variabletracker.ScriptVariable;
import io.github.turtleisaac.variabletracker.gui.flags.FlagTracker;
import io.github.turtleisaac.variabletracker.gui.variables.VariableTracker;
import net.miginfocom.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author turtleisaac
 */
public class FieldScriptEditor extends DefaultDataEditor<GenericScriptData, FieldScriptEditor.FieldScriptContents>
{
    private DefaultListModel<GenericScriptData.ScriptComponent> levelScriptDataListModel = new DefaultListModel<>();
    private DefaultListModel<String> labelDisplayListModel = new DefaultListModel<>();
    private DefaultListModel<String> actionDisplayListModel = new DefaultListModel<>();
    private DefaultListModel<String> scriptDisplayListModel = new DefaultListModel<>();

    private boolean editMode;
    private GenericScriptData.ScriptComponent selected;

    private JFrame variableTrackerFrame;
    private VariableTracker variableTracker;

    private JFrame flagTrackerFrame;
    private FlagTracker flagTracker;

    public FieldScriptEditor(List<GenericScriptData> data, List<TextBankData> textBankData) {
        super(new FieldScriptModel(data, textBankData));
        editMode = false;
        initComponents();
//        FieldScriptEditorKit editorKit = new FieldScriptEditorKit();
        ScriptDocument document = new ScriptDocument(textPane1);
        textPane1.setScriptDocument(document); // NOT setDocument - that leaves scriptDocument null
        textPane1.setBackground(new Color(58, 56, 77));
        textPane1.setScrollPane(scrollPane1);
        textPane1.setForeground(Color.WHITE);

        levelScriptTypeComboBox.setSelectedIndex(0);
        paddingCheckbox.setSelected(true);

        levelScriptList.setModel(levelScriptDataListModel);
        levelScriptList.setSelectedIndex(-1);
        levelScriptListValueChanged(null);
        clearInputFields();
        valueField.addChangeListener(e -> paramFieldTextChange());
        scriptNoField.addChangeListener(e -> paramFieldTextChange());
        variableField.addChangeListener(e -> paramFieldTextChange());
        paddingCheckbox.addActionListener(e -> commitLevelScriptChanges());

        try
        {
            JTextPane numberPane = new JTextPane();
//            numberPane.setBackground(textPane1.getBackground());
//            numberPane.setForeground(textPane1.getForeground());
            textPane1.setLineNumberPane(numberPane);
            scrollPane1.setRowHeaderView(numberPane);
        }
        catch(BadLocationException e) {
            throw new RuntimeException(e);
        }

        setIcons();
        setupVariableTracker();
        setupFlagTracker();

        textPane1.getScriptDocument().setVariableList(variableTracker.getVariableList());
        installDocumentDirtyListener(textPane1.getScriptDocument());

        // establishes the non-edit-mode button states - addButton is declared disabled by the
        // generated code and nothing else ever turned it back on, which made the level script
        // editor completely read-only
        editMode = false;
        toggleEditModeStates();
        levelScriptListValueChanged(null);
    }

    /**
     * Tracks whether the script text has been edited since it was last loaded or saved, so
     * switching entries can offer to save instead of silently throwing the edits away.
     */
    private boolean documentDirty;

    private void installDocumentDirtyListener(ScriptDocument document)
    {
        if (document == null)
            return;

        document.addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e) { documentDirty = true; }

            @Override
            public void removeUpdate(DocumentEvent e) { documentDirty = true; }

            @Override
            public void changedUpdate(DocumentEvent e) { /* attribute-only change (syntax highlighting) */ }
        });
    }

    private void setupVariableTracker()
    {
        variableTrackerFrame = new JFrame("Variable Tracker");
        variableTracker = new VariableTracker() {
            @Override
            public void postUpdateVariableTableAction()
            {
                replaceVariableNumbersWithNames();
            }
        };

        JMenuItem renameVariableMenuItem = new JMenuItem("Rename");
        renameVariableMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ScriptVariable variable = variableTracker.getSelectedVariable();
                if (variable == null)
                {
                    JOptionPane.showMessageDialog(variableTracker, "Select a variable to rename first.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                String oldName = variable.getVariableName();
                String newName = JOptionPane.showInputDialog(variableTracker, "Enter the new name for this variable");

                if (newName == null) // the user cancelled
                    return;

                newName = newName.trim();
                if (newName.isEmpty())
                {
                    JOptionPane.showMessageDialog(variableTracker, "A variable name cannot be blank.\nAction aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (newName.equalsIgnoreCase(oldName))
                {
                    return;
                }

                for (ScriptVariable other : variableTracker.getVariableList())
                {
                    if (variable != other && newName.equalsIgnoreCase(other.getVariableName()))
                    {
                        JOptionPane.showMessageDialog(variableTracker, "The specified variable name is already in use: \"" + other.getVariableName() + "\".\nAction aborted.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // NOTE: the model only ever holds Integers - variable names exist purely in the
                // displayed text - so a rename only needs the display refreshed
                variable.setVariableName(newName);
                variableTracker.fireTableDataChanged();
                replaceVariableNumbersWithNames();
            }
        });

        variableTracker.addDeveloperDefinedPopupMenuItem(renameVariableMenuItem);
        variableTrackerFrame.setContentPane(variableTracker);
        variableTrackerFrame.setJMenuBar(variableTracker.getMenuBar());
    }

    private void setupFlagTracker()
    {
        flagTrackerFrame = new JFrame("Flag Tracker");
        flagTracker = new FlagTracker();

//        JMenuItem copyNameMenuItem = new JMenuItem("Copy variable name");
//        copyNameMenuItem.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                StringSelection selection = new StringSelection(variableTracker.getSelectedVariable().getVariableName());
//                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//                clipboard.setContents(selection, selection);
//            }
//        });
//
//        variableTracker.addDeveloperDefinedPopupMenuItem(copyNameMenuItem);
        flagTrackerFrame.setContentPane(flagTracker);
//        flagTrackerFrame.setJMenuBar(variableTracker.getMenuBar());
    }

    /**
     * Refreshes the DISPLAYED script text so variable IDs read as their friendly names.
     * <p>
     * This deliberately does not touch the model: it used to walk every field script and swap
     * Integer parameters >= 0x4000 for Strings, which the command writer's parameter resolver
     * cannot understand - saving then threw "An invalid parameter was provided (NAME)" and
     * aborted the entire ROM write, for every script rather than just the open one. The
     * substitution now lives purely in the display path, and {@link ScriptDocument#getScriptData()}
     * reverses it before the text is compiled.
     */
    private void replaceVariableNumbersWithNames()
    {
        ScriptDocument document = textPane1.getScriptDocument();
        if (document == null)
            return;

        document.setVariableList(variableTracker.getVariableList());

        if (documentDirty) // never clobber unsaved edits just to relabel variables
            return;

        int idx = getSelectedIndex();
        if (idx < 0)
            return;

        Object entry = getModel().getValueFor(idx, null);
        if (entry instanceof FieldScriptData scriptData)
            loadScriptText(document, scriptData);
    }

    /**
     * Replaces the contents of the document with the provided script, applying variable names
     * for display only.
     */
    private void loadScriptText(ScriptDocument document, FieldScriptData scriptData)
    {
        try {
            document.remove(0, document.getLength());
            document.insertString(0, document.replaceVariableNumbersWithNames(scriptData.toString()), document.getStyle("regular"));
        }
        catch (BadLocationException ble) {
            System.err.println("Couldn't insert text into text pane.");
            ble.printStackTrace();
        }
        documentDirty = false;
    }

    private void setIcons()
    {
        addButton.setIcon(PokeditorManager.rowInsertIcon);
        removeButton.setIcon(PokeditorManager.rowRemoveIcon);
        confirmButton.setIcon(ThemeUtils.validIcon);
        discardButton.setIcon(ThemeUtils.reloadIcon);
    }

    @Override
    public void selectedIndexedChanged(int idx, ActionEvent e)
    {
        int previousIndex = getSelectedIndex();

        if (idx != previousIndex && !confirmDiscardUnsavedScript())
        {
            // the user backed out - put the selector back where it was
            if (getPanel() != null && previousIndex >= 0)
                getPanel().setSelectedEntryIndex(previousIndex);
            return;
        }

        super.selectedIndexedChanged(idx, e);

        if (idx < 0)
            return;

        EditorDataModel<FieldScriptContents> model = getModel();
        GenericScriptData data = (GenericScriptData) model.getValueFor(idx, null);

        errorsList.setModel(new DefaultListModel<>()); // stale errors from another script

        if (data instanceof FieldScriptData scriptData)
        {
            remove(levelScriptPanel);
            add(fieldScriptPanel, "cell 1 0");

            ScriptDocument document = new ScriptDocument(textPane1);
            document.setVariableList(variableTracker.getVariableList());
            textPane1.setScriptDocument(document);
            installDocumentDirtyListener(document);

            resetDisplayedFieldScriptData(scriptData);

            loadScriptText(document, scriptData);
            scrollPane1.getVerticalScrollBar().setValue(0);
        }
        else if (data instanceof LevelScriptData levelScriptData)
        {
            remove(fieldScriptPanel);
            add(levelScriptPanel, "cell 1 0");

            levelScriptDataListModel = new DefaultListModel<>();
            levelScriptDataListModel.addAll(data);
            levelScriptList.setModel(levelScriptDataListModel);
            paddingCheckbox.setSelected(levelScriptData.isHasPadding());

            editMode = false;
            toggleEditModeStates();
            levelScriptListValueChanged(null);
        }

        updateUI();
    }

    /**
     * @return true if it is safe to throw away the current script text (either it is unchanged,
     * or the user chose to save/discard it); false if the user cancelled
     */
    private boolean confirmDiscardUnsavedScript()
    {
        if (!documentDirty)
            return true;

        int result = JOptionPane.showConfirmDialog(this,
                "This script has unsaved changes.\nWould you like to save them first?",
                "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        switch (result)
        {
            case JOptionPane.YES_OPTION -> {
                return saveScriptChanges();
            }
            case JOptionPane.NO_OPTION -> {
                documentDirty = false;
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Rebuilds the selected level script from the on-screen trigger list and pushes it through
     * the model - without this every Add/Remove/Confirm and the padding checkbox were purely
     * decorative, because the list model is a throwaway copy.
     */
    private void commitLevelScriptChanges()
    {
        int idx = getSelectedIndex();
        if (idx < 0)
            return;

        EditorDataModel<FieldScriptContents> model = getModel();
        Object entry = model.getValueFor(idx, null);
        if (!(entry instanceof LevelScriptData levelScriptData))
            return;

        levelScriptData.clear();
        for (int i = 0; i < levelScriptDataListModel.getSize(); i++)
        {
            levelScriptData.add(levelScriptDataListModel.get(i));
        }
        levelScriptData.setHasPadding(paddingCheckbox.isSelected());

        model.setValueFor(levelScriptData, idx, null);
    }

    @Override
    public void addNewEntry()
    {
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        String message = bundle.getString("FieldScriptEditor.newEntryDialog.text");
        String fieldScript = bundle.getString("FieldScriptEditor.newEntryDialog.option1.text");
        String levelScript = bundle.getString("FieldScriptEditor.newEntryDialog.option2.text");

        Object selection = JOptionPane.showInputDialog(this, message, "PokEditor", JOptionPane.INFORMATION_MESSAGE, null, new Object[] {fieldScript, levelScript}, fieldScript);

        if (selection == null) // the user cancelled
            return;

        EditorDataModel<FieldScriptContents> model = getModel();

        if (model instanceof FormatModel<?, ?> formatModel)
        {
            List<GenericFileData> data = (List<GenericFileData>) formatModel.getData();
            int newIndex = data.size();
            if (selection.equals(fieldScript))
            {
                data.add(new FieldScriptData());
            }
            else if (selection.equals(levelScript))
            {
                data.add(new LevelScriptData());
            }
            else
                return;

            // the selector is only populated in DefaultDataEditorPanel's constructor, so without
            // this the new entry is written to the NARC but is unreachable
            if (getPanel() != null)
                getPanel().entryAdded(newIndex);

            selectedIndexedChanged(newIndex, null);
            updateUI();
        }

    }

    @Override
    public void deleteCurrentEntry()
    {
        int idx = getSelectedIndex();
        if (idx < 0)
        {
            JOptionPane.showMessageDialog(this, "Select the entry you would like to delete first.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!(getModel() instanceof FormatModel<?, ?> formatModel))
            return;

        List<?> data = formatModel.getData();
        if (idx >= data.size())
            return;

        if (JOptionPane.showConfirmDialog(this,
                "Delete script entry " + idx + "?\nEvery script after it will shift down by one, which will break any\nscript reference which points at them.",
                "PokEditor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            return;

        documentDirty = false;
        data.remove(idx);

        if (getPanel() != null)
            getPanel().entryRemoved(idx);

        int neighbour = Math.min(idx, data.size() - 1);
        selectedIndexedChanged(neighbour, null);
        updateUI();
    }

    private void resetDisplayedFieldScriptData(FieldScriptData scriptData)
    {
        labelDisplayListModel = new DefaultListModel<>();
        scriptDisplayListModel = new DefaultListModel<>();
        actionDisplayListModel = new DefaultListModel<>();

        int scriptCount = 1;
        for (GenericScriptData.ScriptComponent component : scriptData)
        {
            if (component instanceof GenericScriptData.ScriptLabel label)
            {
                if (label.getScriptID() == -1)
                {
                    String str = component.toString();
                    if (str.contains(" "))
                        str = str.split(" ")[1];
                    labelDisplayListModel.addElement(str);
                }
                else
                    scriptDisplayListModel.addElement("Script " + scriptCount++);
            }
            else if (component instanceof FieldScriptData.ActionLabel actionLabel)
            {
                actionDisplayListModel.addElement(actionLabel.toString());
            }
        }

        displayOnlyScriptsRadioButton.setSelected(true);
        labelDisplayList.setModel(scriptDisplayListModel);

        updateUI();
    }

    private void saveScriptChangesButtonPressed(ActionEvent e) {
        saveScriptChanges();
    }

    /**
     * @return true if the script compiled and was written back into the model
     */
    private boolean saveScriptChanges()
    {
        if (!(textPane1.getDocument() instanceof ScriptDocument scriptDocument))
            return false;

        try
        {
            FieldScriptData data = scriptDocument.getScriptData();

            EditorDataModel<FieldScriptContents> model = getModel();
            model.setValueFor(data, getSelectedIndex(), null);

            resetDisplayedFieldScriptData(data);
            errorsList.setModel(new DefaultListModel<>());
            documentDirty = false;

            // only claim success once the write has actually happened
            JOptionPane.showMessageDialog(this, "Script file saved!", "Field Script Editor", JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        catch(BadLocationException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "The script could not be read from the editor:\n" + ex.getMessage(), "Field Script Editor", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch(ScriptDataProducer.ScriptCompilationException ex) {
            DefaultListModel<String> errorListModel = new DefaultListModel<>();
            for (Throwable throwable : ex.getSuppressed())
            {
                errorListModel.addElement(throwable.getMessage());
                System.err.println(throwable.getMessage());
            }
            errorsList.setModel(errorListModel);
            return false;
        }
    }

    private void startEditingExistingLevelScriptTrigger()
    {
        if (levelScriptList.getSelectedIndex() != -1)
        {
            editMode = true;
            toggleEditModeStates();

            selected = levelScriptList.getSelectedValue();
            scriptNoField.requestFocus();

            if (!(selected instanceof LevelScriptData.LevelScriptTrigger selectedTrigger))
                return;

            scriptNoField.setValue(selectedTrigger.getScriptTriggered());
            levelScriptTypeComboBox.setSelectedIndex(selectedTrigger.getTriggerType()-1);
            if (selectedTrigger.getTriggerType() == LevelScriptData.VARIABLE_VALUE) {
                LevelScriptData.VariableValueTrigger selectedTrigger1 = (LevelScriptData.VariableValueTrigger) selectedTrigger;

                variableField.setEnabled(true);
                valueField.setEnabled(true);
                variableField.setValue(selectedTrigger1.getVariableToWatch());
                valueField.setValue(selectedTrigger1.getExpectedValue());
            }
            else
            {
                variableField.setValue(0);
                valueField.setValue(0);
                variableField.setEnabled(false);
                valueField.setEnabled(false);
            }
        }
        else
        {
            editMode = false;
            toggleEditModeStates();
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void levelScriptListValueChanged(ListSelectionEvent e) {
        removeButton.setEnabled(!levelScriptList.isSelectionEmpty());
    }

    private boolean allFieldsNotNull()
    {
        return scriptNoField.getValue() != null && variableField.getValue() != null && valueField.getValue() != null;
    }

    private boolean anyFieldEmpty()
    {
        return false;
    }

    private void paramFieldTextChange() {
        if (allFieldsNotNull())
        {
            if (((Integer) scriptNoField.getValue()) != -1 && levelScriptTypeComboBox.getSelectedIndex() + 1 != LevelScriptData.VARIABLE_VALUE)
            {
                if (!addButton.isEnabled() && !editMode)
                {
                    addButton.setEnabled(true);
                }
            }
            else if (anyFieldEmpty())
            {
                if (addButton.isEnabled())
                {
                    addButton.setEnabled(false);
                }
            }
            else //all are filled
            {
                if (!addButton.isEnabled() && !editMode)
                {
                    addButton.setEnabled(true);
                }
            }
            SwingUtilities.updateComponentTreeUI(this);
        }

    }

    /**
     * Called from BOTH mousePressed and mouseReleased - isPopupTrigger() is only ever true on
     * the release on Windows, which made the trigger context menu unreachable there.
     */
    private void levelScriptListMousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem editItem = new JMenuItem("Edit selected trigger");
            JMenuItem removeItem = new JMenuItem("Remove selected trigger");
            editItem.addActionListener(e1 -> startEditingExistingLevelScriptTrigger());
            removeItem.addActionListener(this::removeButtonActionPerformed);

            if (levelScriptDataListModel.isEmpty()) {
                menu.setEnabled(false);
                editItem.setEnabled(false);
                removeItem.setEnabled(false);
            }

            menu.add(editItem);
            menu.add(removeItem);
            menu.show(levelScriptList, e.getX(), e.getY());
        }
    }

    private LevelScriptData.LevelScriptTrigger addTriggerToList() {
        try
        {
            LevelScriptData.LevelScriptTrigger built = buildTriggerFromFields();

            if (levelScriptDataListModel.contains(built))
            {
                if(!editMode) {
                    throw new RuntimeException("Duplicate trigger");
                } else {
                    built = null;
                }
            }
            else
            {
                levelScriptDataListModel.addElement(built);
            }

            clearInputFields();
            return built;
        }
        catch (RuntimeException ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Level Script Editor Error", JOptionPane.ERROR_MESSAGE);
//            ex.printStackTrace();
        }

        return null;
    }

    private LevelScriptData.LevelScriptTrigger buildTriggerFromFields() {
        int triggerType = levelScriptTypeComboBox.getSelectedIndex() + 1;
        Integer scriptID = null, variableID = null, varExpectedValue = null;
        ArrayList<String> errorFields = new ArrayList<>();

        scriptID = (Integer) scriptNoField.getValue();

        if (scriptID == -1)
            errorFields.add("Script ID");

        if (triggerType == LevelScriptData.VARIABLE_VALUE) {
            variableID = (Integer) variableField.getValue();
            varExpectedValue = (Integer) valueField.getValue();

            if (variableID == -1)
                errorFields.add("Variable To Watch");
            if (varExpectedValue == -1)
                errorFields.add("Expected Value");
        }

        if (!errorFields.isEmpty())
            throw new RuntimeException("The following errors exist with this level script: " + errorFields);

        if (triggerType == LevelScriptData.VARIABLE_VALUE) {
            return new LevelScriptData.VariableValueTrigger(scriptID, variableID, varExpectedValue);
        } else {
            return new LevelScriptData.MapScreenLoadTrigger(triggerType, scriptID);
        }
    }

    private void confirmButtonActionPerformed(ActionEvent e) {
        // capture the index BEFORE addTriggerToList() - clicking empty space below the last row
        // clears the selection while edit mode stays on, and remove(-1) throws while leaving a
        // duplicate trigger appended
        int editedIndex = levelScriptList.getSelectedIndex();

        if (editedIndex == -1)
        {
            JOptionPane.showMessageDialog(this, "The trigger being edited is no longer selected.\nAction has been aborted.", "Level Script Editor", JOptionPane.ERROR_MESSAGE);
            clearInputFields();
            editMode = false;
            toggleEditModeStates();
            return;
        }

        GenericScriptData.ScriptComponent built = addTriggerToList();

        if (built != null)
            levelScriptDataListModel.remove(editedIndex);

        int count = -1;
        for (LevelScriptData.LevelScriptTrigger lst : Arrays.stream(levelScriptDataListModel.toArray()).map(s -> (LevelScriptData.LevelScriptTrigger) s).toList()) {
            if (!lst.equals(built)) {
                count++;
            }
        }

        levelScriptList.setSelectedIndex(count + 1);
        editMode = false;
        toggleEditModeStates();
        commitLevelScriptChanges();
    }

    private void clearInputFields() {
        valueField.setValue(0);
        variableField.setValue(0);
        scriptNoField.setValue(0);
    }

    private void discardButtonActionPerformed(ActionEvent e) {
        clearInputFields();
        editMode = false;
        toggleEditModeStates();
    }

    private void toggleEditModeStates()
    {
        if (editMode)
        {
            removeButton.setEnabled(false);
            addButton.setEnabled(false);
            confirmButton.setEnabled(true);
            discardButton.setEnabled(true);
            confirmButton.setVisible(true);
            discardButton.setVisible(true);
            paddingCheckbox.setEnabled(false);
        }
        else
        {
            addButton.setEnabled(true);
            removeButton.setEnabled(true);
            confirmButton.setEnabled(false);
            discardButton.setEnabled(false);
            confirmButton.setVisible(false);
            discardButton.setVisible(false);
            paddingCheckbox.setEnabled(true);
        }
    }

    private void addButtonActionPerformed(ActionEvent e) {
        if (addTriggerToList() != null)
            commitLevelScriptChanges();
    }

    private void removeButtonActionPerformed(ActionEvent e) {
        int idx = levelScriptList.getSelectedIndex();
        if (idx != -1)
        {
            levelScriptDataListModel.remove(idx);
            commitLevelScriptChanges();
        }
    }

    void changeFieldVisibility(boolean setting) {
        variableLabel.setVisible(setting);
        variableField.setVisible(setting);
        valueLabel.setVisible(setting);
        valueField.setVisible(setting);
        variableField.setEnabled(setting);
        valueField.setEnabled(setting);
    }

    private void levelScriptTypeSelectionChanged(ActionEvent e) {
        changeFieldVisibility(levelScriptTypeComboBox.getSelectedIndex() + 1 == LevelScriptData.VARIABLE_VALUE);
    }

    private void labelDisplayListSelectionChanged(ListSelectionEvent e) {
        if (e != null && e.getValueIsAdjusting())
            return;

        textPane1.getHighlighter().removeAllHighlights();

        if (!labelDisplayList.isSelectionEmpty())
        {
            ScriptDocument document = textPane1.getScriptDocument();
            String text = null;
            try {
                text = document.getText(0, document.getLength());

                String toFind;
                if (labelDisplayList.getModel().equals(scriptDisplayListModel))
                {
                    toFind = "script(" + (labelDisplayList.getSelectedIndex()+1) + ")";
                }
                else
                    toFind = labelDisplayList.getSelectedValue() + ":";

                int index = text.indexOf(toFind);

                if (index < 0)
                {
                    // the jump lists are only rebuilt on entry switch and successful save, so
                    // they go stale as soon as the user edits the script
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                ScriptPane.gotoStartOfLine(textPane1, ScriptPane.getLineAtOffset(textPane1, index));

                DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                textPane1.getHighlighter().addHighlight(index, index + toFind.length(),
                        highlightPainter);
            }
            catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void labelListDisplayControlButtonPressed(ActionEvent e) {
        if (!((JRadioButton) e.getSource()).isSelected())
            return;

        if (displayOnlyLabelsRadioButton.isSelected())
        {
            labelDisplayList.setModel(labelDisplayListModel);
        }
        else if (displayOnlyActionLabelsRadioButton.isSelected())
        {
            labelDisplayList.setModel(actionDisplayListModel);
        }
        else // only scripts
        {
            labelDisplayList.setModel(scriptDisplayListModel);
        }
    }

    private void variableTrackerButtonPressed(ActionEvent e) {
        variableTrackerFrame.setVisible(true);
        variableTrackerFrame.pack();
    }

    private void flagTrackerButtonPressed(ActionEvent e) {
        flagTrackerFrame.setVisible(true);
        flagTrackerFrame.pack();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        fieldScriptPanel = new JPanel();
        scrollPane1 = new JScrollPane();
        textPane1 = new ScriptPane();
        panel1 = new JPanel();
        variableTrackerButton = new JButton();
        flagTrackerButton = new JButton();
        labelJumpListLabel = new JLabel();
        displayOnlyScriptsRadioButton = new JRadioButton();
        displayOnlyLabelsRadioButton = new JRadioButton();
        displayOnlyActionLabelsRadioButton = new JRadioButton();
        scriptsScrollPane = new JScrollPane();
        labelDisplayList = new JList<>();
        errorsLabel = new JLabel();
        errorsScrollPane = new JScrollPane();
        errorsList = new JList<>();
        saveFieldScriptButton = new JButton();
        levelScriptPanel = new JPanel();
        panel2 = new JPanel();
        configLabel = new JLabel();
        levelScriptTypeComboBox = new JComboBox<>();
        scriptLabel = new JLabel();
        scriptNoField = new JSpinner();
        variableLabel = new JLabel();
        variableField = new HexadecimalSpinner();
        valueLabel = new JLabel();
        valueField = new JSpinner();
        confirmButton = new JButton();
        discardButton = new JButton();
        paddingCheckbox = new JCheckBox();
        addButton = new JButton();
        removeButton = new JButton();
        scrollPane3 = new JScrollPane();
        levelScriptList = new JList<>();
        separator2 = new JSeparator();
        labelListDisplayControlButtonGroup = new ButtonGroup();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3,alignx center",
            // columns
            "[fill]" +
            "[grow,fill]",
            // rows
            "[]"));

        //======== fieldScriptPanel ========
        {
            fieldScriptPanel.setLayout(new MigLayout(
                "insets 0,hidemode 3",
                // columns
                "[fill]" +
                "[grow,fill]" +
                "[grow,fill]",
                // rows
                "[]" +
                "[]"));

            //======== scrollPane1 ========
            {
                scrollPane1.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

                //---- textPane1 ----
                textPane1.setToolTipText("moo");
                scrollPane1.setViewportView(textPane1);
            }
            fieldScriptPanel.add(scrollPane1, "cell 0 0,grow,width 500:500:1000,height 500:500:500");

            //======== panel1 ========
            {
                panel1.setLayout(new MigLayout(
                    "insets 0 0 0 10,hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]unrel" +
                    "[]" +
                    "[]"));

                //---- variableTrackerButton ----
                variableTrackerButton.setText(bundle.getString("FieldScriptEditor.variableTrackerButton.text"));
                variableTrackerButton.addActionListener(e -> variableTrackerButtonPressed(e));
                panel1.add(variableTrackerButton, "cell 0 0");

                //---- flagTrackerButton ----
                flagTrackerButton.setText(bundle.getString("FieldScriptEditor.flagTrackerButton.text"));
                flagTrackerButton.addActionListener(e -> flagTrackerButtonPressed(e));
                panel1.add(flagTrackerButton, "cell 0 0");

                //---- labelJumpListLabel ----
                labelJumpListLabel.setText(bundle.getString("FieldScriptEditor.labelJumpListLabel.text"));
                labelJumpListLabel.setFont(labelJumpListLabel.getFont().deriveFont(labelJumpListLabel.getFont().getSize() + 5f));
                panel1.add(labelJumpListLabel, "cell 0 1");

                //---- displayOnlyScriptsRadioButton ----
                displayOnlyScriptsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyScriptsRadioButton.text"));
                displayOnlyScriptsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyScriptsRadioButton, "cell 0 2,alignx left,growx 0");

                //---- displayOnlyLabelsRadioButton ----
                displayOnlyLabelsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyLabelsRadioButton.text"));
                displayOnlyLabelsRadioButton.setSelected(true);
                displayOnlyLabelsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyLabelsRadioButton, "cell 0 2,alignx left,growx 0");

                //---- displayOnlyActionLabelsRadioButton ----
                displayOnlyActionLabelsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyActionLabelsRadioButton.text"));
                displayOnlyActionLabelsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyActionLabelsRadioButton, "cell 0 2");

                //======== scriptsScrollPane ========
                {

                    //---- labelDisplayList ----
                    labelDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    labelDisplayList.addListSelectionListener(e -> labelDisplayListSelectionChanged(e));
                    scriptsScrollPane.setViewportView(labelDisplayList);
                }
                panel1.add(scriptsScrollPane, "cell 0 3");

                //---- errorsLabel ----
                errorsLabel.setText(bundle.getString("FieldScriptEditor.errorsLabel.text"));
                errorsLabel.setFont(errorsLabel.getFont().deriveFont(errorsLabel.getFont().getSize() + 5f));
                panel1.add(errorsLabel, "cell 0 4");

                //======== errorsScrollPane ========
                {
                    errorsScrollPane.setViewportView(errorsList);
                }
                panel1.add(errorsScrollPane, "cell 0 5");
            }
            fieldScriptPanel.add(panel1, "cell 2 0,grow");

            //---- saveFieldScriptButton ----
            saveFieldScriptButton.setText(bundle.getString("FieldScriptEditor.saveFieldScriptButton.text"));
            saveFieldScriptButton.addActionListener(e -> saveScriptChangesButtonPressed(e));
            fieldScriptPanel.add(saveFieldScriptButton, "cell 0 1");
        }
        add(fieldScriptPanel, "cell 1 0");

        //======== levelScriptPanel ========
        {
            levelScriptPanel.setLayout(new MigLayout(
                "insets null 200 null null,hidemode 3,alignx center",
                // columns
                "[left]ind" +
                "[grow,fill]",
                // rows
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[]" +
                "[grow]" +
                "[]" +
                "[]" +
                "[]"));

            //======== panel2 ========
            {
                panel2.setLayout(new MigLayout(
                    "insets 0,hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[grow]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- configLabel ----
                configLabel.setText(bundle.getString("FieldScriptEditor.configLabel.text"));
                configLabel.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 16));
                panel2.add(configLabel, "cell 0 0");

                //---- levelScriptTypeComboBox ----
                levelScriptTypeComboBox.setModel(new DefaultComboBoxModel<>(new String[] {
                    "Variable Value",
                    "Map Change",
                    "Screen Reset",
                    "Load Game"
                }));
                levelScriptTypeComboBox.addActionListener(e -> levelScriptTypeSelectionChanged(e));
                panel2.add(levelScriptTypeComboBox, "cell 0 0");

                //---- scriptLabel ----
                scriptLabel.setText(bundle.getString("FieldScriptEditor.scriptLabel.text"));
                panel2.add(scriptLabel, "cell 0 2");

                //---- scriptNoField ----
                scriptNoField.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
                panel2.add(scriptNoField, "cell 0 3,aligny top,grow 100 0");

                //---- variableLabel ----
                variableLabel.setText(bundle.getString("FieldScriptEditor.variableLabel.text"));
                panel2.add(variableLabel, "cell 0 4,aligny top,growy 0");
                panel2.add(variableField, "cell 0 5,aligny top,grow 100 0");

                //---- valueLabel ----
                valueLabel.setText(bundle.getString("FieldScriptEditor.valueLabel.text"));
                panel2.add(valueLabel, "cell 0 6,aligny top,growy 0");

                //---- valueField ----
                valueField.setModel(new SpinnerNumberModel(0, 0, 65535, 1));
                panel2.add(valueField, "cell 0 7,aligny top,grow 100 0");

                //---- confirmButton ----
                confirmButton.setText(bundle.getString("FieldScriptEditor.confirmButton.text"));
                confirmButton.setIcon(new ImageIcon(getClass().getResource("/pokeditor/icons/tick.png")));
                confirmButton.setEnabled(false);
                confirmButton.addActionListener(e -> confirmButtonActionPerformed(e));
                panel2.add(confirmButton, "cell 0 8");

                //---- discardButton ----
                discardButton.setText(bundle.getString("FieldScriptEditor.discardButton.text"));
                discardButton.setIcon(new ImageIcon(getClass().getResource("/pokeditor/icons/cross.png")));
                discardButton.setEnabled(false);
                discardButton.addActionListener(e -> discardButtonActionPerformed(e));
                panel2.add(discardButton, "cell 0 8");

                //---- paddingCheckbox ----
                paddingCheckbox.setText(bundle.getString("FieldScriptEditor.paddingCheckbox.text"));
                panel2.add(paddingCheckbox, "cell 0 9");

                //---- addButton ----
                addButton.setText(bundle.getString("FieldScriptEditor.addButton.text"));
                addButton.setIcon(null);
                addButton.setEnabled(false);
                addButton.addActionListener(e -> addButtonActionPerformed(e));
                panel2.add(addButton, "cell 0 10");

                //---- removeButton ----
                removeButton.setText(bundle.getString("FieldScriptEditor.removeButton.text"));
                removeButton.setIcon(null);
                removeButton.setEnabled(false);
                removeButton.addActionListener(e -> removeButtonActionPerformed(e));
                panel2.add(removeButton, "cell 0 10");
            }
            levelScriptPanel.add(panel2, "cell 0 0 1 11");

            //======== scrollPane3 ========
            {

                //---- levelScriptList ----
                levelScriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                levelScriptList.addListSelectionListener(e -> levelScriptListValueChanged(e));
                levelScriptList.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        levelScriptListMousePressed(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        levelScriptListMousePressed(e);
                    }
                });
                scrollPane3.setViewportView(levelScriptList);
            }
            levelScriptPanel.add(scrollPane3, "cell 1 0 1 11,grow");
            levelScriptPanel.add(separator2, "cell 0 1");
        }

        //---- labelListDisplayControlButtonGroup ----
        labelListDisplayControlButtonGroup.add(displayOnlyScriptsRadioButton);
        labelListDisplayControlButtonGroup.add(displayOnlyLabelsRadioButton);
        labelListDisplayControlButtonGroup.add(displayOnlyActionLabelsRadioButton);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    @Override
    public Class<GenericScriptData> getDataClass()
    {
        return GenericScriptData.class;
    }

    @Override
    public Set<DefaultDataEditorPanel.DataEditorButtons> getEnabledToolbarButtons()
    {
        return Set.of(DefaultDataEditorPanel.DataEditorButtons.ADD_ENTRY,
                DefaultDataEditorPanel.DataEditorButtons.DELETE_ENTRY,
                DefaultDataEditorPanel.DataEditorButtons.EXPORT_FILE,
                DefaultDataEditorPanel.DataEditorButtons.IMPORT_FILE
        );
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JPanel fieldScriptPanel;
    private JScrollPane scrollPane1;
    private ScriptPane textPane1;
    private JPanel panel1;
    private JButton variableTrackerButton;
    private JButton flagTrackerButton;
    private JLabel labelJumpListLabel;
    private JRadioButton displayOnlyScriptsRadioButton;
    private JRadioButton displayOnlyLabelsRadioButton;
    private JRadioButton displayOnlyActionLabelsRadioButton;
    private JScrollPane scriptsScrollPane;
    private JList<String> labelDisplayList;
    private JLabel errorsLabel;
    private JScrollPane errorsScrollPane;
    private JList<String> errorsList;
    private JButton saveFieldScriptButton;
    private JPanel levelScriptPanel;
    private JPanel panel2;
    private JLabel configLabel;
    private JComboBox<String> levelScriptTypeComboBox;
    private JLabel scriptLabel;
    private JSpinner scriptNoField;
    private JLabel variableLabel;
    private HexadecimalSpinner variableField;
    private JLabel valueLabel;
    private JSpinner valueField;
    private JButton confirmButton;
    private JButton discardButton;
    private JCheckBox paddingCheckbox;
    private JButton addButton;
    private JButton removeButton;
    private JScrollPane scrollPane3;
    private JList<GenericScriptData.ScriptComponent> levelScriptList;
    private JSeparator separator2;
    private ButtonGroup labelListDisplayControlButtonGroup;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    static class FieldScriptModel extends FormatModel<GenericScriptData, FieldScriptContents>
    {

        public FieldScriptModel(List<GenericScriptData> data, List<TextBankData> textBankData)
        {
            super(data, textBankData);
        }

        @Override
        public Object getValueFor(int entryIdx, FieldScriptContents property)
        {
            GenericScriptData entry = getData().get(entryIdx);

//            switch (property) {
//                case FEMALE_BACK -> {
//                    return entry.getFemaleBack();
//                }
//                case MALE_BACK -> {
//                    return entry.getMaleBack();
//                }
//                case FEMALE_FRONT -> {
//                    return entry.getFemaleFront();
//                }
//                case MALE_FRONT -> {
//                    return entry.getMaleFront();
//                }
//                case PALETTE -> {
//                    return entry.getPalette();
//                }
//                case SHINY_PALETTE -> {
//                    return entry.getShinyPalette();
//                }
//                case PARTY_ICON -> {
//                    return entry.getPartyIcon();
//                }
//                case GLOBAL_FRONT_Y -> {
//                    return entry.getGlobalFrontYOffset();
//                }
//                case FEMALE_BACK_Y -> {
//                    return entry.getFemaleBackOffset();
//                }
//                case MALE_BACK_Y -> {
//                    return entry.getMaleBackOffset();
//                }
//                case FEMALE_FRONT_Y -> {
//                    return entry.getFemaleFrontOffset();
//                }
//                case MALE_FRONT_Y -> {
//                    return entry.getMaleFrontOffset();
//                }
//                case MOVEMENT -> {
//                    return entry.getMovement();
//                }
//                case SHADOW_X -> {
//                    return entry.getShadowXOffset();
//                }
//                case SHADOW_SIZE -> {
//                    return entry.getShadowSize();
//                }
//                case PARTY_ICON_PALETTE -> {
//                    return entry.getPartyIconPaletteIndex();
//                }
//            }

            return entry;
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, FieldScriptContents property)
        {
            if (entryIdx < 0 || !(aValue instanceof GenericScriptData scriptData))
                return;

            getData().set(entryIdx, scriptData);
            DataManager.markDirty(GenericScriptData.class);

//            GenericScriptData entry = getData().get(entryIdx);
//
//            switch (property) {
//                case FEMALE_BACK -> entry.setFemaleBack((IndexedImage) aValue);
//                case MALE_BACK -> entry.setMaleBack((IndexedImage) aValue);
//                case FEMALE_FRONT -> entry.setFemaleFront((IndexedImage) aValue);
//                case MALE_FRONT -> entry.setMaleFront((IndexedImage) aValue);
//                case PALETTE -> entry.setPalette((Palette) aValue);
//                case SHINY_PALETTE -> entry.setShinyPalette((Palette) aValue);
//                case PARTY_ICON -> entry.setPartyIcon((IndexedImage) aValue);
//                case GLOBAL_FRONT_Y -> entry.setGlobalFrontYOffset((Integer) aValue);
//                case FEMALE_BACK_Y -> entry.setFemaleBackOffset((Integer) aValue);
//                case MALE_BACK_Y -> entry.setMaleBackOffset((Integer) aValue);
//                case FEMALE_FRONT_Y -> entry.setFemaleFrontOffset((Integer) aValue);
//                case MALE_FRONT_Y -> entry.setMaleFrontOffset((Integer) aValue);
//                case MOVEMENT -> entry.setMovement((Integer) aValue);
//                case SHADOW_X -> entry.setShadowXOffset((Integer) aValue);
//                case SHADOW_SIZE -> entry.setShadowSize((Integer) aValue);
//                case PARTY_ICON_PALETTE -> entry.setPartyIconPaletteIndex((Integer) aValue);
//            }
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return null;
        }

//        @Override
//        public String getEntryName(int entryIdx)
//        {
//            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
//            if (entryIdx < speciesNames.size())
//                return speciesNames.get(entryIdx).getText();
//            return super.getEntryName(entryIdx);
//        }

        @Override
        public FormatModel<GenericScriptData, FieldScriptContents> getFrozenColumnModel()
        {
            return null;
        }

        @Override
        public int getColumnCount()
        {
            return 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }
    }

    enum FieldScriptContents
    {

    }
}
