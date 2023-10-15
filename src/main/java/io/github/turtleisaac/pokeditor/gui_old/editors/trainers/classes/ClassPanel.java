/*
 * Created by JFormDesigner on Fri Jan 29 16:17:22 EST 2021
 */

package io.github.turtleisaac.pokeditor.gui_old.editors.trainers.classes;

import javax.swing.*;
import javax.swing.border.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class ClassPanel extends JPanel {
    public ClassPanel() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel1 = new JPanel();
        label1 = new JLabel();
        slider1 = new JSlider();
        label2 = new JLabel();
        panel2 = new JPanel();
        label3 = new JLabel();
        spinner1 = new JSpinner();
        label4 = new JLabel();
        spinner2 = new JSpinner();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[]" +
            "[]" +
            "[]"));

        //======== panel1 ========
        {
            panel1.setBorder(new TitledBorder("Gender"));
            panel1.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                "[grow,fill]" +
                "[fill]",
                // rows
                "[]"));

            //---- label1 ----
            label1.setText("Female");
            panel1.add(label1, "cell 0 0,align center center,grow 0 0");

            //---- slider1 ----
            slider1.setMaximum(1);
            slider1.setMajorTickSpacing(1);
            slider1.setSnapToTicks(true);
            slider1.setValue(0);
            panel1.add(slider1, "cell 1 0");

            //---- label2 ----
            label2.setText("Male");
            panel1.add(label2, "cell 2 0,align center center,grow 0 0");
        }
        add(panel1, "cell 0 0");

        //======== panel2 ========
        {
            panel2.setBorder(new TitledBorder("Misc"));
            panel2.setLayout(new MigLayout(
                "hidemode 3",
                // columns
                "[fill]" +
                "[grow,fill]" +
                "[fill]" +
                "[grow,fill]",
                // rows
                "[]"));

            //---- label3 ----
            label3.setText("Prize Money");
            panel2.add(label3, "cell 0 0");
            panel2.add(spinner1, "cell 1 0");

            //---- label4 ----
            label4.setText("Song");
            panel2.add(label4, "cell 2 0");
            panel2.add(spinner2, "cell 3 0");
        }
        add(panel2, "cell 0 1");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel1;
    private JLabel label1;
    private JSlider slider1;
    private JLabel label2;
    private JPanel panel2;
    private JLabel label3;
    private JSpinner spinner1;
    private JLabel label4;
    private JSpinner spinner2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
