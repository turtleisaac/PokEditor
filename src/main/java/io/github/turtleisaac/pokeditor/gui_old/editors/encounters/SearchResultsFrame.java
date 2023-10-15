///*
// * Created by JFormDesigner on Sun Sep 12 14:52:43 CDT 2021
// */
//
//package io.github.turtleisaac.pokeditor.gui.editors.encounters;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.util.ArrayList;
//import javax.swing.*;
//
//import io.github.turtleisaac.pokeditor.gui.editors.encounters.johto.JohtoEncounterPanel;
//import io.github.turtleisaac.pokeditor.gui.editors.encounters.sinnoh.SinnohEncounterPanel;
//import net.miginfocom.swing.*;
//
///**
// * @author turtleisaac
// */
//public class SearchResultsFrame extends JFrame
//{
//    private boolean sinnohAccess;
//    private SinnohEncounterPanel sinnohPanel;
//    private JohtoEncounterPanel johtoPanel;
//
//
//    public SearchResultsFrame(boolean sinnohAccess, JPanel encounterPanel, ArrayList<String> searchResults)
//    {
//        initComponents();
//
//        this.sinnohAccess= sinnohAccess;
//
//        if(sinnohAccess)
//        {
//            sinnohPanel= (SinnohEncounterPanel) encounterPanel;
//        }
//        else
//        {
//            johtoPanel= (JohtoEncounterPanel) encounterPanel;
//        }
//
//        searchResultsList.setModel(new AbstractListModel<String>()
//        {
//            final String[] values= searchResults.toArray(new String[0]);
//            @Override
//            public int getSize() { return values.length; }
//            @Override
//            public String getElementAt(int i) { return values[i]; }
//        });
//    }
//
//    private void goToSelectionButtonActionPerformed(ActionEvent e)
//    {
//        int targetFile= Integer.parseInt(searchResultsList.getSelectedValue().split(":")[0]);
//        dispose();
//
//        if(sinnohAccess)
//        {
//            sinnohPanel.changeSelectedEncounterData(targetFile);
//        }
//        else
//        {
//            // TODO add code for Johto encounter editor
//        }
//    }
//
//    private void initComponents()
//    {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        scrollPane1 = new JScrollPane();
//        searchResultsList = new JList<>();
//        goToSelectionButton = new JButton();
//
//        //======== this ========
//        setTitle("Search Results");
//        Container contentPane = getContentPane();
//        contentPane.setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[grow,fill]",
//            // rows
//            "[grow]" +
//            "[]"));
//
//        //======== scrollPane1 ========
//        {
//
//            //---- searchResultsList ----
//            searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//            searchResultsList.setModel(new AbstractListModel<String>() {
//                String[] values = {
//                    "Default1",
//                    "Default2",
//                    "Default3"
//                };
//                @Override
//                public int getSize() { return values.length; }
//                @Override
//                public String getElementAt(int i) { return values[i]; }
//            });
//            scrollPane1.setViewportView(searchResultsList);
//        }
//        contentPane.add(scrollPane1, "cell 0 0,grow");
//
//        //---- goToSelectionButton ----
//        goToSelectionButton.setText("Jump to Selection");
//        goToSelectionButton.addActionListener(e -> goToSelectionButtonActionPerformed(e));
//        contentPane.add(goToSelectionButton, "cell 0 1");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    // Generated using JFormDesigner non-commercial license
//    private JScrollPane scrollPane1;
//    private JList<String> searchResultsList;
//    private JButton goToSelectionButton;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}
