/*
 * Created by JFormDesigner
 */

package io.github.turtleisaac.variabletracker.gui.flags;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class FlagTracker extends JPanel {
    public FlagTracker() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("variable_tracker.gui");
        toolBar1 = new JToolBar();
        label1 = new JLabel();
        textField1 = new JTextField();
        button2 = new JButton();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        vSpacer1 = new JPanel(null);
        menuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        editMenu = new JMenu();
        menu3 = new JMenu();
        menuItem1 = new JMenuItem();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[]" +
            "[grow,fill]" +
            "[]"));

        //======== toolBar1 ========
        {
            toolBar1.setFloatable(false);

            //---- label1 ----
            label1.setText(bundle.getString("VariableTracker.searchLabel.text"));
            toolBar1.add(label1);
            toolBar1.add(textField1);
            toolBar1.addSeparator();

            //---- button2 ----
            button2.setText(bundle.getString("VariableTracker.optionsButton.text"));
            toolBar1.add(button2);
        }
        add(toolBar1, "north");

        //======== scrollPane1 ========
        {

            //---- table1 ----
            table1.setModel(new DefaultTableModel(
                new Object[][] {
                    {null, null, null},
                    {null, null, null},
                },
                new String[] {
                    "Flag", "Name", "Description"
                }
            ));
            scrollPane1.setViewportView(table1);
        }
        add(scrollPane1, "cell 0 1");
        add(vSpacer1, "cell 0 2");

        //======== menuBar1 ========
        {

            //======== fileMenu ========
            {
                fileMenu.setText(bundle.getString("VariableTracker.fileMenu.text"));

                //---- openMenuItem ----
                openMenuItem.setText(bundle.getString("VariableTracker.openMenuItem.text"));
                fileMenu.add(openMenuItem);

                //---- saveMenuItem ----
                saveMenuItem.setText(bundle.getString("VariableTracker.saveMenuItem.text"));
                fileMenu.add(saveMenuItem);
            }
            menuBar1.add(fileMenu);

            //======== editMenu ========
            {
                editMenu.setText(bundle.getString("VariableTracker.editMenu.text"));
            }
            menuBar1.add(editMenu);

            //======== menu3 ========
            {
                menu3.setText(bundle.getString("VariableTracker.helpMenu.text"));

                //---- menuItem1 ----
                menuItem1.setText(bundle.getString("VariableTracker.infoMenuItem.text"));
                menu3.add(menuItem1);
            }
            menuBar1.add(menu3);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner non-commercial license
    private JToolBar toolBar1;
    private JLabel label1;
    private JTextField textField1;
    private JButton button2;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JPanel vSpacer1;
    private JMenuBar menuBar1;
    private JMenu fileMenu;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenu editMenu;
    private JMenu menu3;
    private JMenuItem menuItem1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
