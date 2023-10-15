/*
 * Created by JFormDesigner on Wed Dec 15 18:04:50 CST 2021
 */

package io.github.turtleisaac.pokeditor.gui.sheets;

import javax.swing.*;

import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class DefaultSheetPanel extends JPanel
{
    public DefaultSheetPanel(JTable table) {
        initComponents();
        table1 = table;
        scrollPane1.setViewportView(table1);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        table1 = new JTable();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]"));

        //======== scrollPane1 ========
        {
            scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane1.setViewportView(table1);
        }
        add(scrollPane1, "cell 0 0,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private JTable table1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
