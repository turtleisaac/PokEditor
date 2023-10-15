///*
// * Created by JFormDesigner on Mon Jan 04 13:44:01 EST 2021
// */
//
//package io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.sheets;
//
//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*;
//
//import io.github.turtleisaac.pokeditor.project.Game;
//import net.miginfocom.swing.*;
//
///**
// * @author Truck
// */
//public class EditApplierInfo extends JFrame
//{
//    private Game baseRom;
//    private String[] editors;
//
//    public EditApplierInfo(Game baseRom)
//    {
//        initComponents();
//        this.baseRom= baseRom;
//        editors= baseRom.editorList;
//
//        for(String editor : editors)
//            sheetListComboBox.addItem(editor);
//
//        setPreferredSize(new Dimension(300,300));
//        setVisible(true);
//        pack();
//
//    }
//
//    private void sheetListComboBoxActionPerformed(ActionEvent e)
//    {
//        switch(baseRom)
//        {
//            case Diamond:
//            case Pearl:
//            case Platinum:
//                switch ((String) sheetListComboBox.getSelectedItem())
//                {
//                    case "Personal":
//                        editorInfoTextArea.setText("This editor applies both the Personal sheet and the TM Learnsets sheet to your rom.");
//                        break;
//
//                    case "Level-Up Learnsets":
//                        editorInfoTextArea.setText("This editor applies the Level-Up Learnsets sheet to your rom.");
//                        break;
//
//                    case "Evolutions":
//                        editorInfoTextArea.setText("This editor applies the Evolutions sheet to your rom.");
//                        break;
//
//                    case "Tutors":
//                        editorInfoTextArea.setText("This editor applies both the Tutor Move List sheet and the Tutor Move Compatibility sheet to your rom.");
//                        break;
//
//                    case "Babies":
//                        editorInfoTextArea.setText("This editor applies the Baby Forms sheet to your rom.");
//                        break;
//
//                    case "Moves":
//                        editorInfoTextArea.setText("This editor applies the Moves sheet to your rom.");
//                        break;
//
//                    case "Encounters":
//                        editorInfoTextArea.setText("This editor applies the Evolutions sheet to your rom.");
//                        break;
//
//                    case "Items":
//                        editorInfoTextArea.setText("This editor applies the Items sheet to your rom.");
//                        break;
//
//                }
//
//                break;
//
//
//
//            case HeartGold:
//            case SoulSilver:
//
//                break;
//        }
//
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        sheetListComboBox = new JComboBox();
//        scrollPane1 = new JScrollPane();
//        editorInfoTextArea = new JTextArea();
//
//        //======== this ========
//        setTitle("Info");
//        Container contentPane = getContentPane();
//        contentPane.setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[grow,fill]",
//            // rows
//            "[]" +
//            "[grow]"));
//
//        //---- sheetListComboBox ----
//        sheetListComboBox.addActionListener(e -> sheetListComboBoxActionPerformed(e));
//        contentPane.add(sheetListComboBox, "cell 0 0");
//
//        //======== scrollPane1 ========
//        {
//            scrollPane1.setViewportView(editorInfoTextArea);
//        }
//        contentPane.add(scrollPane1, "cell 0 1,grow");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    private JComboBox sheetListComboBox;
//    private JScrollPane scrollPane1;
//    private JTextArea editorInfoTextArea;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}
