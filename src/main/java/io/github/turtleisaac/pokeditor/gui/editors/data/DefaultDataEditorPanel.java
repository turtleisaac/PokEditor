/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
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
    }

    private void setIcons()
    {
        saveButton.setIcon(ThemeUtils.saveIcon);
        resetChangesButton.setIcon(ThemeUtils.reloadIcon);
        addButton.setIcon(PokeditorManager.rowInsertIcon);
        deleteButton.setIcon(PokeditorManager.rowRemoveIcon);
        copyEntryButton.setIcon(PokeditorManager.copyIcon);
        pasteEntryButton.setIcon(PokeditorManager.clipboardIcon);
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
        // TODO add your code here
    }

    private void deleteEntryButtonPressed(ActionEvent e) {
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
        deleteButton = new JButton();
        copyEntryButton = new JButton();
        pasteEntryButton = new JButton();
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
            toolBar1.addSeparator();

            //---- deleteButton ----
            deleteButton.setText(bundle.getString("DefaultEditorPanel.deleteButton.text"));
            deleteButton.addActionListener(e -> deleteEntryButtonPressed(e));
            toolBar1.add(deleteButton);
            toolBar1.addSeparator();

            //---- copyEntryButton ----
            copyEntryButton.setText(bundle.getString("DefaultEditorPanel.copyEntryButton.text"));
            toolBar1.add(copyEntryButton);
            toolBar1.addSeparator();

            //---- pasteEntryButton ----
            pasteEntryButton.setText(bundle.getString("DefaultEditorPanel.pasteEntryButton.text"));
            toolBar1.add(pasteEntryButton);
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
    private JButton deleteButton;
    private JButton copyEntryButton;
    private JButton pasteEntryButton;
    private JPanel contentPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}