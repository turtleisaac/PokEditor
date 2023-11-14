/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.editors.general;

import java.util.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class GeneralEditorPanel extends JPanel {
    public GeneralEditorPanel() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        toolBar1 = new JToolBar();
        saveChangesButton = new JButton();
        resetChangesButton = new JButton();
        contentPanel = new JPanel();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow]"));

        //======== toolBar1 ========
        {
            toolBar1.setFloatable(false);

            //---- saveChangesButton ----
            saveChangesButton.setText(bundle.getString("DefaultSheetPanel.saveSheetButton.text"));
            toolBar1.add(saveChangesButton);
            toolBar1.addSeparator();

            //---- resetChangesButton ----
            resetChangesButton.setText(bundle.getString("DefaultSheetPanel.reloadSheetButton.text"));
            toolBar1.add(resetChangesButton);
        }
        add(toolBar1, "north");

        //======== contentPanel ========
        {
            contentPanel.setLayout(new MigLayout(
                "fill,insets 0,hidemode 3",
                // columns
                "[fill]",
                // rows
                "[grow,fill]"));
        }
        add(contentPanel, "cell 0 0,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JToolBar toolBar1;
    private JButton saveChangesButton;
    private JButton resetChangesButton;
    private JPanel contentPanel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
