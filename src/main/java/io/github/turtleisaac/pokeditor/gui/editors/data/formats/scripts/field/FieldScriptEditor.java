/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.field;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.LevelScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.ScriptDataProducer;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.*;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditor;
import io.github.turtleisaac.pokeditor.gui.editors.data.EditorDataModel;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.*;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptDocument;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
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
    private DefaultListModel<GenericScriptData.ScriptComponent> labelDisplayListModel = new DefaultListModel<>();
    private DefaultListModel<GenericScriptData.ScriptComponent> actionDisplayListModel = new DefaultListModel<>();
    private DefaultListModel<GenericScriptData.ScriptComponent> scriptDisplayListModel = new DefaultListModel<>();

    private boolean editMode;
    private GenericScriptData.ScriptComponent selected;

    public FieldScriptEditor(List<GenericScriptData> data, List<TextBankData> textBankData) {
        super(new FieldScriptModel(data, textBankData));
        editMode = false;
        initComponents();
//        FieldScriptEditorKit editorKit = new FieldScriptEditorKit();
        StyledDocument document = new ScriptDocument(textPane1);
        textPane1.setDocument(document);
        textPane1.setBackground(new Color(58, 56, 77));
        textPane1.setScrollPane(scrollPane1);
        textPane1.setForeground(Color.WHITE);

        levelScriptTypeComboBox.setSelectedIndex(0);
        paddingCheckbox.setSelected(true);

        levelScriptList.setModel(levelScriptDataListModel);
        levelScriptList.setSelectedIndex(-1);
        levelScriptListValueChanged(null);
        clearInputFields();
//        valueField.addChangeListener(e -> paramFieldTextChange());
//        scriptNoField.addChangeListener(e -> paramFieldTextChange());
//        variableField.addChangeListener(e -> paramFieldTextChange());
        removeButton.setEnabled(false);


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
        super.selectedIndexedChanged(idx, e);
        EditorDataModel<FieldScriptContents> model = getModel();
        GenericScriptData data = (GenericScriptData) model.getValueFor(idx, null);
//        errorsList.removeAll();

        if (data instanceof ScriptData scriptData)
        {
            remove(levelScriptPanel);
            add(fieldScriptPanel, "cell 1 0");

            labelDisplayListModel = new DefaultListModel<>();
            scriptDisplayListModel = new DefaultListModel<>();
            actionDisplayListModel = new DefaultListModel<>();

            for (GenericScriptData.ScriptComponent component : scriptData)
            {
                if (component instanceof GenericScriptData.ScriptLabel label)
                {
                    if (label.getScriptID() == -1)
                        labelDisplayListModel.addElement(component);
                    else
                        scriptDisplayListModel.addElement(component);
                }
                else if (component instanceof ScriptData.ActionLabel actionLabel)
                {
                    actionDisplayListModel.addElement(actionLabel);
                }
            }


            ScriptDocument document = new ScriptDocument(textPane1);
            textPane1.setScriptDocument(document);

            displayOnlyScriptsRadioButton.setSelected(true);
            labelDisplayList.setModel(scriptDisplayListModel);

            try {
                document.insertString(0, scriptData.toString(), document.getStyle("regular"));
            } catch (BadLocationException ble) {
                System.err.println("Couldn't insert initial text into text pane.");
            }
            scrollPane1.getVerticalScrollBar().setValue(0);
        }
        else if (data instanceof LevelScriptData)
        {
            remove(fieldScriptPanel);
            add(levelScriptPanel, "cell 1 0");

            levelScriptDataListModel = new DefaultListModel<>();
            levelScriptDataListModel.addAll(data);
            levelScriptList.setModel(levelScriptDataListModel);
        }

        updateUI();


//        errorsList.setModel(listModel);
    }

    private void saveScriptChangesButtonPressed(ActionEvent e) {
        if (textPane1.getDocument() instanceof ScriptDocument scriptDocument)
        {
            try
            {
                ScriptData data = scriptDocument.getScriptData();
                System.currentTimeMillis();
            }
            catch(BadLocationException ex) {
                throw new RuntimeException(ex);
            }
            catch(ScriptDataProducer.ScriptCompilationException ex) {
                DefaultListModel<String> errorListModel = new DefaultListModel<>();
                for (Throwable throwable : ex.getSuppressed())
                {
                    errorListModel.addElement(throwable.getMessage());
                    System.err.println(throwable.getMessage());
                }
                errorsList.setModel(errorListModel);
            }
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
        GenericScriptData.ScriptComponent built = addTriggerToList();

        if (built != null)
            levelScriptDataListModel.remove(levelScriptList.getSelectedIndex());

        int count = -1;
        for (LevelScriptData.LevelScriptTrigger lst : Arrays.stream(levelScriptDataListModel.toArray()).map(s -> (LevelScriptData.LevelScriptTrigger) s).toList()) {
            if (!lst.equals(built)) {
                count++;
            }
        }

        levelScriptList.setSelectedIndex(count + 1);
        editMode = false;
        toggleEditModeStates();
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
        addTriggerToList();
    }

    private void removeButtonActionPerformed(ActionEvent e) {
        if (levelScriptList.getSelectedIndex() != -1)
            levelScriptDataListModel.remove(levelScriptList.getSelectedIndex());
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
        textPane1.getHighlighter().removeAllHighlights();

        if (!labelDisplayList.isSelectionEmpty())
        {
            ScriptDocument document = textPane1.getScriptDocument();
            String text = null;
            try {
                text = document.getText(0, document.getLength());

                String toFind = labelDisplayList.getSelectedValue().getName() + ":";
                int index = text.indexOf(toFind);

                ScriptPane.gotoStartOfLine(textPane1, ScriptPane.getLineAtOffset(textPane1, index));

                DefaultHighlighter.DefaultHighlightPainter highlightPainter =
                        new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
                textPane1.getHighlighter().addHighlight(index, index + toFind.length(),
                        highlightPainter);
            }
            catch (BadLocationException ex) {
                throw new RuntimeException(ex);
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


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        fieldScriptPanel = new JPanel();
        scrollPane1 = new JScrollPane();
        textPane1 = new ScriptPane();
        panel1 = new JPanel();
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
                    "[]unrel" +
                    "[]" +
                    "[]"));

                //---- labelJumpListLabel ----
                labelJumpListLabel.setText(bundle.getString("FieldScriptEditor.labelJumpListLabel.text"));
                labelJumpListLabel.setFont(labelJumpListLabel.getFont().deriveFont(labelJumpListLabel.getFont().getSize() + 5f));
                panel1.add(labelJumpListLabel, "cell 0 0");

                //---- displayOnlyScriptsRadioButton ----
                displayOnlyScriptsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyScriptsRadioButton.text"));
                displayOnlyScriptsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyScriptsRadioButton, "cell 0 1,alignx left,growx 0");

                //---- displayOnlyLabelsRadioButton ----
                displayOnlyLabelsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyLabelsRadioButton.text"));
                displayOnlyLabelsRadioButton.setSelected(true);
                displayOnlyLabelsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyLabelsRadioButton, "cell 0 1,alignx left,growx 0");

                //---- displayOnlyActionLabelsRadioButton ----
                displayOnlyActionLabelsRadioButton.setText(bundle.getString("FieldScriptEditor.displayOnlyActionLabelsRadioButton.text"));
                displayOnlyActionLabelsRadioButton.addActionListener(e -> labelListDisplayControlButtonPressed(e));
                panel1.add(displayOnlyActionLabelsRadioButton, "cell 0 1");

                //======== scriptsScrollPane ========
                {

                    //---- labelDisplayList ----
                    labelDisplayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    labelDisplayList.addListSelectionListener(e -> labelDisplayListSelectionChanged(e));
                    scriptsScrollPane.setViewportView(labelDisplayList);
                }
                panel1.add(scriptsScrollPane, "cell 0 2");

                //---- errorsLabel ----
                errorsLabel.setText(bundle.getString("FieldScriptEditor.errorsLabel.text"));
                errorsLabel.setFont(errorsLabel.getFont().deriveFont(errorsLabel.getFont().getSize() + 5f));
                panel1.add(errorsLabel, "cell 0 3");

                //======== errorsScrollPane ========
                {
                    errorsScrollPane.setViewportView(errorsList);
                }
                panel1.add(errorsScrollPane, "cell 0 4");
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

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JPanel fieldScriptPanel;
    private JScrollPane scrollPane1;
    private ScriptPane textPane1;
    private JPanel panel1;
    private JLabel labelJumpListLabel;
    private JRadioButton displayOnlyScriptsRadioButton;
    private JRadioButton displayOnlyLabelsRadioButton;
    private JRadioButton displayOnlyActionLabelsRadioButton;
    private JScrollPane scriptsScrollPane;
    private JList<GenericScriptData.ScriptComponent> labelDisplayList;
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
    private JList<ScriptData.ScriptComponent> levelScriptList;
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
