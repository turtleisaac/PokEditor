/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.formats.BytesDataContainer;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.EditorComboBox.ComboBoxItem;
import io.github.turtleisaac.pokeditor.gui.EditorComboBox;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class DefaultDataEditorPanel<G extends GenericFileData, E extends Enum<E>> extends JPanel {
    private final DefaultDataEditor<G, E> editor;
    private final PokeditorManager manager;

    private BytesDataContainer copiedEntry;

    public DefaultDataEditorPanel(PokeditorManager manager, DefaultDataEditor<G, E> editor) {
        initComponents();
        this.manager = manager;
        this.editor = editor;
        contentPanel.add(editor);
        setIcons();

        EditorDataModel<?> model = editor.getModel();
        for (int i = 0; i < model.getEntryCount(); i++) {
            entrySelectorComboBox.addItem(new ComboBoxItem(model.getEntryName(i)));
        }

        Set<DataEditorButtons> enabledToolbarButtons = editor.getEnabledToolbarButtons();

        if (!enabledToolbarButtons.contains(DataEditorButtons.ADD_ENTRY))
        {
            toolBar1.remove(addButton);
            toolBar1.remove(separator3);
        }
        if (!enabledToolbarButtons.contains(DataEditorButtons.DELETE_ENTRY))
        {
            toolBar1.remove(deleteButton);
            toolBar1.remove(separator6);
        }
        if (!enabledToolbarButtons.contains(DataEditorButtons.COPY_ENTRY))
        {
            toolBar1.remove(copyEntryButton);
            toolBar1.remove(separator5);
        }
        if (!enabledToolbarButtons.contains(DataEditorButtons.PASTE_ENTRY))
        {
            toolBar1.remove(pasteEntryButton);
            toolBar1.remove(separator7);
        }
        if (!enabledToolbarButtons.contains(DataEditorButtons.EXPORT_FILE))
        {
            toolBar1.remove(exportFileButton);
            toolBar1.remove(separator8);
        }
        if (!enabledToolbarButtons.contains(DataEditorButtons.IMPORT_FILE))
        {
            toolBar1.remove(importFileButton);
            toolBar1.remove(separator3);
        }
    }

    private void setIcons()
    {
        saveButton.setIcon(ThemeUtils.saveIcon);
        resetChangesButton.setIcon(ThemeUtils.reloadIcon);
        addButton.setIcon(PokeditorManager.rowInsertIcon);
        deleteButton.setIcon(PokeditorManager.rowRemoveIcon);
        copyEntryButton.setIcon(PokeditorManager.copyIcon);
        pasteEntryButton.setIcon(PokeditorManager.clipboardIcon);
        importFileButton.setIcon(ThemeUtils.fileImportIcon);
        exportFileButton.setIcon(ThemeUtils.fileExportIcon);
    }

    private void selectedEntryChanged(ActionEvent e) {
        editor.selectedIndexedChanged(entrySelectorComboBox.getSelectedIndex(), e);
    }

    private void saveButtonPressed(ActionEvent e) {
        manager.saveData(editor.getDataClass());
    }

    private void reloadDataButtonPressed(ActionEvent e) {
        manager.resetData(editor.getDataClass());
        editor.selectedIndexedChanged(entrySelectorComboBox.getSelectedIndex(), e);
    }

    private void addEntryButtonPressed(ActionEvent e) {
        editor.addNewEntry();
    }

    private void deleteEntryButtonPressed(ActionEvent e) {
        editor.deleteCurrentEntry();
    }

    private void copyEntryButtonPressed(ActionEvent e) {
        copiedEntry = editor.writeSelectedEntryForCopy();
        pasteEntryButton.setEnabled(copiedEntry != null);
    }

    private void pasteEntryButtonPressed(ActionEvent e) {
        editor.applyCopiedEntry(copiedEntry);
        selectedEntryChanged(null);
    }

    private void exportFileButtonPressed(ActionEvent e) {
        // TODO add your code here
    }

    private void importFileButtonPressed(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        separator4 = new JSeparator();
        toolBar1 = new JToolBar();
        entrySelectorComboBox = new EditorComboBox();
        saveButton = new JButton();
        resetChangesButton = new JButton();
        addButton = new JButton();
        separator3 = new JToolBar.Separator();
        deleteButton = new JButton();
        separator6 = new JToolBar.Separator();
        copyEntryButton = new JButton();
        separator5 = new JToolBar.Separator();
        pasteEntryButton = new JButton();
        separator7 = new JToolBar.Separator();
        exportFileButton = new JButton();
        separator8 = new JToolBar.Separator();
        importFileButton = new JButton();
        contentPanel = new JPanel();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[]" +
            "[grow]"));
        add(separator4, "cell 0 0");

        //======== toolBar1 ========
        {
            toolBar1.setFloatable(false);

            //---- entrySelectorComboBox ----
            entrySelectorComboBox.addActionListener(e -> selectedEntryChanged(e));
            toolBar1.add(entrySelectorComboBox);

            //---- saveButton ----
            saveButton.setText(bundle.getString("DefaultSheetPanel.saveSheetButton.text"));
            saveButton.addActionListener(e -> saveButtonPressed(e));
            toolBar1.add(saveButton);
            toolBar1.addSeparator();

            //---- resetChangesButton ----
            resetChangesButton.setText(bundle.getString("DefaultSheetPanel.reloadSheetButton.text"));
            resetChangesButton.addActionListener(e -> reloadDataButtonPressed(e));
            toolBar1.add(resetChangesButton);
            toolBar1.addSeparator();

            //---- addButton ----
            addButton.setText(bundle.getString("DefaultEditorPanel.addButton.text"));
            addButton.addActionListener(e -> addEntryButtonPressed(e));
            toolBar1.add(addButton);
            toolBar1.add(separator3);
            separator3.setOrientation(toolBar1.getOrientation() == JToolBar.VERTICAL
                ? JSeparator.HORIZONTAL : JSeparator.VERTICAL);

            //---- deleteButton ----
            deleteButton.setText(bundle.getString("DefaultEditorPanel.deleteButton.text"));
            deleteButton.addActionListener(e -> deleteEntryButtonPressed(e));
            toolBar1.add(deleteButton);
            toolBar1.add(separator6);
            separator6.setOrientation(toolBar1.getOrientation() == JToolBar.VERTICAL
                ? JSeparator.HORIZONTAL : JSeparator.VERTICAL);

            //---- copyEntryButton ----
            copyEntryButton.setText(bundle.getString("DefaultEditorPanel.copyEntryButton.text"));
            copyEntryButton.addActionListener(e -> copyEntryButtonPressed(e));
            toolBar1.add(copyEntryButton);
            toolBar1.add(separator5);
            separator5.setOrientation(toolBar1.getOrientation() == JToolBar.VERTICAL
                ? JSeparator.HORIZONTAL : JSeparator.VERTICAL);

            //---- pasteEntryButton ----
            pasteEntryButton.setText(bundle.getString("DefaultEditorPanel.pasteEntryButton.text"));
            pasteEntryButton.setEnabled(false);
            pasteEntryButton.addActionListener(e -> pasteEntryButtonPressed(e));
            toolBar1.add(pasteEntryButton);
            toolBar1.add(separator7);
            separator7.setOrientation(toolBar1.getOrientation() == JToolBar.VERTICAL
                ? JSeparator.HORIZONTAL : JSeparator.VERTICAL);

            //---- exportFileButton ----
            exportFileButton.setText(bundle.getString("DefaultEditorPanel.exportFileButton.text"));
            exportFileButton.addActionListener(e -> exportFileButtonPressed(e));
            toolBar1.add(exportFileButton);
            toolBar1.add(separator8);
            separator8.setOrientation(toolBar1.getOrientation() == JToolBar.VERTICAL
                ? JSeparator.HORIZONTAL : JSeparator.VERTICAL);

            //---- importFileButton ----
            importFileButton.setText(bundle.getString("DefaultEditorPanel.importFileButton.text"));
            importFileButton.addActionListener(e -> importFileButtonPressed(e));
            toolBar1.add(importFileButton);
        }
        add(toolBar1, "north");

        //======== contentPanel ========
        {
            contentPanel.setBorder(null);
            contentPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        }
        add(contentPanel, "cell 0 1,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JSeparator separator4;
    private JToolBar toolBar1;
    private EditorComboBox entrySelectorComboBox;
    private JButton saveButton;
    private JButton resetChangesButton;
    private JButton addButton;
    private JToolBar.Separator separator3;
    private JButton deleteButton;
    private JToolBar.Separator separator6;
    private JButton copyEntryButton;
    private JToolBar.Separator separator5;
    private JButton pasteEntryButton;
    private JToolBar.Separator separator7;
    private JButton exportFileButton;
    private JToolBar.Separator separator8;
    private JButton importFileButton;
    private JPanel contentPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on

    public enum DataEditorButtons
    {
        ADD_ENTRY,
        DELETE_ENTRY,
        COPY_ENTRY,
        PASTE_ENTRY,
        IMPORT_FILE,
        EXPORT_FILE
    }
}
