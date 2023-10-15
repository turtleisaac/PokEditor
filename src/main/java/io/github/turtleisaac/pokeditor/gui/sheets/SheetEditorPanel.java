///*
// * Created by JFormDesigner on Wed Dec 15 17:55:23 CST 2021
// */
//
//package io.github.turtleisaac.pokeditor.gui.sheets;
//
//import javax.swing.*;
//
//import io.github.turtleisaac.pokeditor.gui.sheets.tables.PersonalTable;
//import net.miginfocom.swing.*;
//
///**
// * @author turtleisaac
// */
//public class SheetEditorPanel extends JPanel {
//    public SheetEditorPanel(Sheet... sheets) {
//        initComponents();
//
//        for(Sheet s : sheets)
//        {
//            if(s.getSheetType() != null)
//            {
//                switch(s.getSheetType())
//                {
//                    case Personal:
//                        tabbedPane1.addTab();
//                }
//            }
//        }
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        tabbedPane1 = new JTabbedPane();
//
//        //======== this ========
//        setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[grow,fill]",
//            // rows
//            "[grow,fill]"));
//        add(tabbedPane1, "");
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    // Generated using JFormDesigner non-commercial license
//    private JTabbedPane tabbedPane1;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}
