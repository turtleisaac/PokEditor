/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.field;

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.ScriptData;
import io.github.turtleisaac.pokeditor.formats.scripts.antlr4.ScriptDataProducer;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
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
    public FieldScriptEditor(List<GenericScriptData> data, List<TextBankData> textBankData) {
        super(new FieldScriptModel(data, textBankData));
        initComponents();
//        FieldScriptEditorKit editorKit = new FieldScriptEditorKit();
        StyledDocument document = new ScriptDocument(textPane1);
        textPane1.setDocument(document);
        textPane1.setBackground(new Color(58, 56, 77));
        textPane1.setScrollPane(scrollPane1);
        textPane1.setForeground(Color.WHITE);


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

    }

    @Override
    public void selectedIndexedChanged(int idx, ActionEvent e)
    {
        super.selectedIndexedChanged(idx, e);
        EditorDataModel<FieldScriptContents> model = getModel();
        GenericScriptData data = (GenericScriptData) model.getValueFor(idx, null);
//        list1.removeAll();

        DefaultListModel<ScriptData.ScriptComponent> listModel = new DefaultListModel<>();

        StyledDocument document = new ScriptDocument(textPane1);
        textPane1.setStyledDocument(document);

        if (data instanceof ScriptData scriptData) {
            listModel.addAll(data);

            try {
                document.insertString(0, scriptData.toString(), document.getStyle("regular"));
            } catch (BadLocationException ble) {
                System.err.println("Couldn't insert initial text into text pane.");
            }
        }
        scrollPane1.getVerticalScrollBar().setValue(0);
//        list1.setModel(listModel);
    }

    private void button1(ActionEvent e) {
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
                list1.setModel(errorListModel);
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        scrollPane1 = new JScrollPane();
        textPane1 = new ScriptPane();
        panel1 = new JPanel();
        label1 = new JLabel();
        scrollPane2 = new JScrollPane();
        list1 = new JList<>();
        button1 = new JButton();
        panel2 = new JPanel();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[fill]" +
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
        add(scrollPane1, "cell 1 0,grow,width 500:500:1000,height 500:500:500");

        //======== panel1 ========
        {
            panel1.setLayout(new MigLayout(
                "insets 0 0 0 10,hidemode 3",
                // columns
                "[grow,fill]",
                // rows
                "[]" +
                "[]"));

            //---- label1 ----
            label1.setText(bundle.getString("FieldScriptEditor.label1.text"));
            label1.setFont(label1.getFont().deriveFont(label1.getFont().getSize() + 5f));
            panel1.add(label1, "cell 0 0");

            //======== scrollPane2 ========
            {
                scrollPane2.setViewportView(list1);
            }
            panel1.add(scrollPane2, "cell 0 1");
        }
        add(panel1, "cell 3 0,grow");

        //---- button1 ----
        button1.setText(bundle.getString("FieldScriptEditor.button1.text"));
        button1.addActionListener(e -> button1(e));
        add(button1, "cell 1 1");

        //======== panel2 ========
        {
            panel2.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                "[fill]",
                // rows
                "[]" +
                "[]" +
                "[]"));
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    @Override
    public Class<GenericScriptData> getDataClass()
    {
        return GenericScriptData.class;
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private ScriptPane textPane1;
    private JPanel panel1;
    private JLabel label1;
    private JScrollPane scrollPane2;
    private JList<String> list1;
    private JButton button1;
    private JPanel panel2;
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