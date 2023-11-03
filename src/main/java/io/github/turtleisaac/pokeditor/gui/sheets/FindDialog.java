/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.pokeditor.gui.sheets;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class FindDialog extends JFrame {
    public FindDialog(DefaultSheetPanel<?> parent) {
        super();
        initComponents();
        setPreferredSize(dialogPane.getPreferredSize());
        setMinimumSize(dialogPane.getPreferredSize());
        setMaximumSize(dialogPane.getPreferredSize());
        setVisible(true);
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        findLabel = new JLabel();
        findTextField = new JTextField();
        panel1 = new JPanel();
        label1 = new JLabel();
        textField1 = new JTextField();
        radioButton1 = new JRadioButton();
        matchCaseCheckbox = new JCheckBox();
        matchEntireContentsCheckbox = new JCheckBox();
        buttonBar = new JPanel();
        findButton = new JButton();
        doneButton = new JButton();

        //======== this ========
        setTitle(bundle.getString("FindDialog.this.title"));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new MigLayout(
                    "insets dialog,hidemode 3",
                    // columns
                    "[fill]" +
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[grow]"));

                //---- findLabel ----
                findLabel.setText(bundle.getString("FindDialog.findLabel.text"));
                findLabel.setLabelFor(findTextField);
                contentPanel.add(findLabel, "cell 0 0");
                contentPanel.add(findTextField, "cell 1 0");

                //======== panel1 ========
                {
                    panel1.setLayout(new MigLayout(
                        "hidemode 3",
                        // columns
                        "[grow,fill]" +
                        "[fill]",
                        // rows
                        "[]" +
                        "[]" +
                        "[]"));

                    //---- label1 ----
                    label1.setText(bundle.getString("FindDialog.label1.text"));
                    panel1.add(label1, "cell 0 0,alignx left,growx 0");
                    panel1.add(textField1, "cell 0 0");
                    panel1.add(radioButton1, "cell 1 0");

                    //---- matchCaseCheckbox ----
                    matchCaseCheckbox.setText(bundle.getString("FindDialog.matchCaseCheckbox.text"));
                    panel1.add(matchCaseCheckbox, "cell 0 1");

                    //---- matchEntireContentsCheckbox ----
                    matchEntireContentsCheckbox.setText(bundle.getString("FindDialog.matchEntireContentsCheckbox.text"));
                    panel1.add(matchEntireContentsCheckbox, "cell 0 2");
                }
                contentPanel.add(panel1, "cell 0 1 2 1");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setLayout(new MigLayout(
                    "insets dialog,alignx right",
                    // columns
                    "[fill]" +
                    "[button,fill]",
                    // rows
                    null));

                //---- findButton ----
                findButton.setText(bundle.getString("FindDialog.findButton.text"));
                buttonBar.add(findButton, "cell 0 0");

                //---- doneButton ----
                doneButton.setText(bundle.getString("FindDialog.doneButton.text"));
                buttonBar.add(doneButton, "cell 1 0");
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel findLabel;
    private JTextField findTextField;
    private JPanel panel1;
    private JLabel label1;
    private JTextField textField1;
    private JRadioButton radioButton1;
    private JCheckBox matchCaseCheckbox;
    private JCheckBox matchEntireContentsCheckbox;
    private JPanel buttonBar;
    private JButton findButton;
    private JButton doneButton;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
