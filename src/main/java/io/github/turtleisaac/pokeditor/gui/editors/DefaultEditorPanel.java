/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui_old.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class DefaultEditorPanel<G extends GenericFileData, E extends Enum<E>> extends JPanel {
    private final DefaultEditor<G, E> editor;

    public DefaultEditorPanel(PokeditorManager manager, DefaultEditor<G, E> editor) {
        initComponents();
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
//        exportSheetButton.setIcon(PokeditorManager.sheetExportIcon);
//        importSheetButton.setIcon(PokeditorManager.sheetImportIcon);
        addButton.setIcon(PokeditorManager.rowInsertIcon);
        deleteButton.setIcon(PokeditorManager.rowRemoveIcon);
//        findButton.setIcon(PokeditorManager.searchIcon);
//        copyModeButton.setIcon(PokeditorManager.clipboardIcon);
    }

    private void selectedEntryChanged(ActionEvent e) {
        editor.selectedIndexedChanged(entrySelectorComboBox.getSelectedIndex(), e);
    }

    private void saveButtonPressed(ActionEvent e) {
        // TODO add your code here
    }

    private void reloadDataButtonPressed(ActionEvent e) {
        // TODO add your code here
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
            saveButton.setText(bundle.getString("DefaultEditorPanel.saveButton.text"));
            saveButton.addActionListener(e -> saveButtonPressed(e));
            toolBar1.add(saveButton);
            toolBar1.addSeparator();

            //---- resetChangesButton ----
            resetChangesButton.setText(bundle.getString("DefaultEditorPanel.resetChangesButton.text"));
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
    private JPanel contentPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
