/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.general.starters;

import java.util.*;
import javax.swing.*;
import io.github.turtleisaac.pokeditor.gui.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class StarterEditor extends JPanel {
    public StarterEditor() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        starterLabel1 = new JLabel();
        starterLabel2 = new JLabel();
        starterLabel3 = new JLabel();
        starterComboBox1 = new EditorComboBox();
        starterComboBox2 = new EditorComboBox();
        starterComboBox3 = new EditorComboBox();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]" +
            "[grow,fill]" +
            "[grow,fill]",
            // rows
            "[]" +
            "[grow]"));

        //---- starterLabel1 ----
        starterLabel1.setText(bundle.getString("StarterEditorPanel.starterLabel1.text"));
        starterLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        add(starterLabel1, "cell 0 0,alignx center,growx 0");

        //---- starterLabel2 ----
        starterLabel2.setText(bundle.getString("StarterEditorPanel.starterLabel2.text"));
        starterLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        add(starterLabel2, "cell 1 0,alignx center,growx 0");

        //---- starterLabel3 ----
        starterLabel3.setText(bundle.getString("StarterEditorPanel.starterLabel3.text"));
        starterLabel3.setHorizontalAlignment(SwingConstants.CENTER);
        add(starterLabel3, "cell 2 0,alignx center,growx 0");
        add(starterComboBox1, "cell 0 1,aligny top,growy 0");
        add(starterComboBox2, "cell 1 1,aligny top,growy 0");
        add(starterComboBox3, "cell 2 1,aligny top,growy 0");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JLabel starterLabel1;
    private JLabel starterLabel2;
    private JLabel starterLabel3;
    private EditorComboBox starterComboBox1;
    private EditorComboBox starterComboBox2;
    private EditorComboBox starterComboBox3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
