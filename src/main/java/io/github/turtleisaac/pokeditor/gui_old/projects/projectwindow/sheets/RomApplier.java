///*
// * Created by JFormDesigner on Mon Jan 04 12:19:53 EST 2021
// */
//
//package io.github.turtleisaac.pokeditor.gui.projects.projectwindow.sheets;
//
//import java.awt.event.*;
//import io.github.turtleisaac.pokeditor.editors.babies.BabyFormEditor;
//import io.github.turtleisaac.pokeditor.editors.encounters.johto.JohtoEncounterEditor;
//import io.github.turtleisaac.pokeditor.editors.encounters.johto.JohtoEncounterReturn;
//import io.github.turtleisaac.pokeditor.editors.encounters.sinnoh.SinnohEncounterEditor;
//import io.github.turtleisaac.pokeditor.editors.encounters.sinnoh.SinnohEncounterReturn;
//import io.github.turtleisaac.pokeditor.editors.evolutions.gen4.EvolutionEditor;
//import io.github.turtleisaac.pokeditor.editors.items.ItemEditorGen4;
//import io.github.turtleisaac.pokeditor.editors.items.ItemTableEntry;
//import io.github.turtleisaac.pokeditor.editors.learnsets.LearnsetEditor;
//import io.github.turtleisaac.pokeditor.editors.moves.gen4.MoveEditorGen4;
//import io.github.turtleisaac.pokeditor.framework.narctowl.Narctowl;
//import io.github.turtleisaac.pokeditor.editors.personal.gen4.PersonalEditor;
//import io.github.turtleisaac.pokeditor.editors.personal.gen4.PersonalReturnGen4;
//import io.github.turtleisaac.pokeditor.editors.trainers.gen4.TrainerEditorGen4;
//import io.github.turtleisaac.pokeditor.editors.trainers.gen4.TrainerReturnGen4;
//import io.github.turtleisaac.pokeditor.gui.JCheckboxTree;
//import io.github.turtleisaac.pokeditor.project.Game;
//import io.github.turtleisaac.pokeditor.project.Project;
//import net.miginfocom.swing.MigLayout;
//import org.apache.commons.io.FileDeleteStrategy;
//import org.apache.commons.io.FileUtils;
//import io.github.turtleisaac.pokeditor.sheets.GoogleSheetsAPI;
//
//import javax.swing.*;
//import javax.swing.tree.DefaultMutableTreeNode;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Objects;
//
///**
// * @author turtleisaac
// * This class is for applying rom data to sheets
// */
//public class RomApplier extends JFrame
//{
//    private Project project;
//    private Game baseRom;
//    private String[] editors;
//    private String projectPath;
//
//    private GoogleSheetsAPI api;
//    private JFrame parent;
//    private ArrayList<ItemTableEntry> itemTableData;
//    private boolean displayEndMessage;
//
//    public RomApplier(Project project, String projectPath, GoogleSheetsAPI api, JFrame parent, boolean visible, ArrayList<ItemTableEntry> itemTableData, boolean display)
//    {
//        initComponents();
//        DefaultMutableTreeNode root= new DefaultMutableTreeNode("Sheets");
//        this.project= project;
//        baseRom= project.getBaseRom();
//        editors= baseRom.editorList;
//        this.projectPath = projectPath;
//        this.api= api;
//        this.parent= parent;
//        this.itemTableData = itemTableData;
//        this.displayEndMessage = display;
//
//        for(String str : baseRom.sheetList)
//        {
//            DefaultMutableTreeNode child= new DefaultMutableTreeNode(str);
//            root.add(child);
//        }
//
//
//        editorTree= new JCheckboxTree(root);
//        editorTree.setVisible(true);
//        setPreferredSize(new Dimension(300,350));
//        scrollPane1.setViewportView(editorTree);
//
//        if(visible)
//        {
//            pack();
//            setVisible(true);
//            toFront();
//        }
//        else
//        {
//            setVisible(false);
//        }
//    }
//
//    private void optionsButtonActionPerformed(ActionEvent e)
//    {
//        // TODO add your code here
//    }
//
//    public JCheckboxTree getCheckboxTree()
//    {
//        return editorTree;
//    }
//
//
//    private ArrayList<File> toDelete;
//
//    public void applyButtonActionPerformed(ActionEvent e)
//    {
//        // TODO add your code here
//        toDelete= new ArrayList<>();
//        String selectedString= Arrays.toString(editorTree.getCheckedPaths());
//        selectedString= selectedString.substring(0, selectedString.length()-1);
//
//        String[] selected= selectedString.split(", ");
//        try
//        {
//            api.updateSheet("Formatting (DO NOT TOUCH)",FormatGenerator.updateFormatSheet(project));
//
//            Narctowl narctowl= new Narctowl(true);
//            String dataPath= projectPath + File.separator + project.getName() + File.separator + "data";
//
//            String unpackedFolderPath = projectPath + File.separator + "temp" + File.separator;
//
//
//            switch (baseRom)
//            {
//                case Platinum:
//                    if(contains(selected, "Personal") || contains(selected, "TM Learnsets"))
//                    {
//                        PersonalEditor editor= new PersonalEditor(dataPath, project);
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "personal" + File.separator + "pl_personal.narc",unpackedFolderPath + "pl_personal");
//                        PersonalReturnGen4 personalReturn= editor.personalToSheet(unpackedFolderPath + "pl_personal");
//                        toDelete.add(new File(unpackedFolderPath + "pl_personal"));
//
//                        if(contains(selected, "Personal"))
//                            api.updateSheet("Personal",personalReturn.getPersonalData());
//                        if(contains(selected, "TM Learnsets"))
//                            api.updateSheet("TM Learnsets",personalReturn.getTMData());
//                    }
//
//                    if(contains(selected, "Level-Up Learnsets"))
//                    {
//                        LearnsetEditor editor= new LearnsetEditor(dataPath, project);
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "personal" + File.separator + "wotbl.narc",unpackedFolderPath + "wotbl");
//                        toDelete.add(new File(unpackedFolderPath + "wotbl"));
//                        api.updateSheet("Level-Up Learnsets",editor.learnsetToSheet(unpackedFolderPath + "wotbl"));
//                    }
//
//                    if(contains(selected,"Evolutions"))
//                    {
//                        EvolutionEditor editor= new EvolutionEditor(project, dataPath, baseRom);
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "personal" + File.separator + "evo.narc",unpackedFolderPath + "evo");
//                        toDelete.add(new File(unpackedFolderPath + "evo"));
//                        api.updateSheet("Evolutions",editor.evolutionsToSheet(unpackedFolderPath + "evo",false, false));
//                    }
//
//                    if(contains(selected,"Tutor Move"))
//                    {
//                        JOptionPane.showMessageDialog(this,"Tutor Move editors not implemented yet","Error",JOptionPane.ERROR_MESSAGE);
//                    }
//
//                    if(contains(selected,"Baby Forms"))
//                    {
//                        BabyFormEditor editor= new BabyFormEditor(dataPath,project);
//                        api.updateSheet("Baby Forms",editor.babyFormsToSheet(File.separator + "poketool" + File.separator + "personal" + File.separator + "pms.narc"));
//                    }
//
//                    if(contains(selected,"Moves"))
//                    {
//                        MoveEditorGen4 editor= new MoveEditorGen4(project,dataPath);
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "waza" + File.separator + "pl_waza_tbl.narc",unpackedFolderPath + "pl_waza_tbl");
//                        toDelete.add(new File(unpackedFolderPath + "pl_waza_tbl"));
//                        api.updateSheet("Moves",editor.movesToSheet(unpackedFolderPath + "pl_waza_tbl"));
//                    }
//
//                    if(contains(selected,"Items"))
//                    {
//                        ItemEditorGen4 editor= new ItemEditorGen4(dataPath,project,itemTableData);
//                        narctowl.unpack(dataPath + File.separator + "itemtool" + File.separator + "itemdata" + File.separator + "pl_item_data.narc",unpackedFolderPath + "pl_item_data");
//                        toDelete.add(new File(unpackedFolderPath + "pl_item_data"));
//                        api.updateSheet("Items",editor.itemsToSheet(unpackedFolderPath + "pl_item_data"));
//                    }
//
//                    if(contains(selected,"Encounters"))
//                    {
//                        SinnohEncounterEditor editor= new SinnohEncounterEditor(project,dataPath);
//                        narctowl.unpack(dataPath + File.separator + "fielddata" + File.separator + "encountdata" + File.separator + "pl_enc_data.narc",unpackedFolderPath + "pl_enc_data");
//                        SinnohEncounterReturn encounterReturn= editor.encountersToSheet(unpackedFolderPath + "pl_enc_data");
//                        toDelete.add(new File(unpackedFolderPath + "pl_enc_data"));
//
//                        if(contains(selected,"Field"))
//                            api.updateSheet("Field Encounters",encounterReturn.getField());
//                        if(contains(selected,"Water"))
//                            api.updateSheet("Water Encounters",encounterReturn.getWater());
//                        if(contains(selected,"Swarm"))
//                            api.updateSheet("Swarm/ Day/ Night Encounters",encounterReturn.getSwarm());
//                        if(contains(selected,"Radar"))
//                            api.updateSheet("Poke Radar Encounters",encounterReturn.getRadar());
//                        if(contains(selected,"Mode"))
//                            api.updateSheet("Dual-Slot Mode Encounters",encounterReturn.getDualSlot());
//                        if(contains(selected,"Forms"))
//                            api.updateSheet("Alt Form Encounters",encounterReturn.getFormProbabilityTable());
//                    }
//
//                    if(contains(selected, "Trainer"))
//                    {
//                        TrainerEditorGen4 editor= new TrainerEditorGen4(project,dataPath,baseRom);
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "trainer" + File.separator + "trdata.narc",unpackedFolderPath + "trdata");
//                        narctowl.unpack(dataPath + File.separator + "poketool" + File.separator + "trainer" + File.separator + "trpoke.narc",unpackedFolderPath + "trpoke");
//                        toDelete.add(new File(unpackedFolderPath + "trdata"));
//                        toDelete.add(new File(unpackedFolderPath + "trpoke"));
//                        TrainerReturnGen4 trainerReturn= editor.trainersToSheets(unpackedFolderPath + "trdata",unpackedFolderPath + "trpoke");
//                        if(contains(selected,"Data"))
//                            api.updateSheet("Trainer Data",trainerReturn.getTrainerData());
//                        if(contains(selected,"Pokemon"))
//                            api.updateSheet("Trainer Pokemon",trainerReturn.getTrainerPokemon());
//                    }
//                    break;
//
//
//
//                case HeartGold:
//                case SoulSilver:
//                    if(contains(selected, "Personal") || contains(selected, "TM Learnsets"))
//                    {
//                        PersonalEditor editor= new PersonalEditor(dataPath, project);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "0" + File.separator + "2",unpackedFolderPath + "personal");
//                        PersonalReturnGen4 personalReturn= editor.personalToSheet(unpackedFolderPath + "personal");
//                        toDelete.add(new File(unpackedFolderPath + "personal"));
//                        if(contains(selected, "Personal"))
//                            api.updateSheet("Personal",personalReturn.getPersonalData());
//                        if(contains(selected, "TM Learnsets"))
//                            api.updateSheet("TM Learnsets",personalReturn.getTMData());
//                    }
//
//                    if(contains(selected, "Level-Up Learnsets"))
//                    {
//                        LearnsetEditor editor= new LearnsetEditor(dataPath, project);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "3" + File.separator + "3",unpackedFolderPath + "wotbl");
//                        toDelete.add(new File(unpackedFolderPath + "wotbl"));
//                        api.updateSheet("Level-Up Learnsets",editor.learnsetToSheet(unpackedFolderPath + "wotbl"));
//                    }
//
//                    if(contains(selected,"Evolutions"))
//                    {
//                        EvolutionEditor editor= new EvolutionEditor(project, dataPath, baseRom);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "3" + File.separator + "4",unpackedFolderPath + "evo");
//                        toDelete.add(new File(unpackedFolderPath + "evo"));
//                        api.updateSheet("Evolutions",editor.evolutionsToSheet(unpackedFolderPath + "evo",false, false));
//                    }
//
//                    if(contains(selected,"Baby Forms"))
//                    {
//                        BabyFormEditor editor= new BabyFormEditor(dataPath,project);
//                        api.updateSheet("Baby Forms",editor.babyFormsToSheet(File.separator + "poketool" + File.separator + "personal" + File.separator + "pms.narc"));
//                    }
//
//                    if(contains(selected,"Moves"))
//                    {
//                        MoveEditorGen4 editor= new MoveEditorGen4(project,dataPath);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "1" + File.separator + "1",unpackedFolderPath + "waza_tbl");
//                        toDelete.add(new File(unpackedFolderPath + "waza_tbl"));
//                        api.updateSheet("Moves",editor.movesToSheet(unpackedFolderPath + "waza_tbl"));
//                    }
//
//                    if(contains(selected,"Items"))
//                    {
//                        ItemEditorGen4 editor= new ItemEditorGen4(dataPath,project,itemTableData);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "1" + File.separator + "7",unpackedFolderPath + "item_data");
//                        toDelete.add(new File(unpackedFolderPath + "item_data"));
//                        api.updateSheet("Items",editor.itemsToSheet(unpackedFolderPath + "item_data"));
//                    }
//
//                    if(contains(selected,"Encounters"))
//                    {
//                        JohtoEncounterEditor editor= new JohtoEncounterEditor(project, dataPath);
//                        JohtoEncounterReturn encounterReturn;
//
//                        if(baseRom == Game.HeartGold) //HG
//                        {
//                            narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "3" + File.separator + "7",unpackedFolderPath + "enc_data");
//                            encounterReturn= editor.encountersToSheet(unpackedFolderPath + "enc_data");
//                            toDelete.add(new File(unpackedFolderPath + "enc_data"));
//
//                        }
//                        else //SS
//                        {
//                            narctowl.unpack(dataPath + File.separator + "a" + File.separator + "1" + File.separator + "3" + File.separator + "6",unpackedFolderPath + "enc_data");
//                            encounterReturn= editor.encountersToSheet(unpackedFolderPath + "enc_data");
//                            toDelete.add(new File(unpackedFolderPath + "enc_data"));
//                        }
//
//                        if(contains(selected,"Field"))
//                            api.updateSheet("Field Encounters",encounterReturn.getField());
//                        if(contains(selected,"Water"))
//                            api.updateSheet("Water Encounters",encounterReturn.getWater());
//                        if(contains(selected,"Rock Smash"))
//                            api.updateSheet("Rock Smash Encounters",encounterReturn.getSmash());
//                        if(contains(selected,"Mass-Outbreak"))
//                            api.updateSheet("Mass-Outbreak Encounters",encounterReturn.getOutbreak());
//                        if(contains(selected,"Sound"))
//                            api.updateSheet("Sound Encounters",encounterReturn.getSound());
//                    }
//
//                    if(contains(selected, "Trainer"))
//                    {
//                        TrainerEditorGen4 editor= new TrainerEditorGen4(project,dataPath,baseRom);
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "5" + File.separator + "5",unpackedFolderPath + "trdata");
//                        narctowl.unpack(dataPath + File.separator + "a" + File.separator + "0" + File.separator + "5" + File.separator + "6",unpackedFolderPath + "trpoke");
//                        toDelete.add(new File(unpackedFolderPath + "trdata"));
//                        toDelete.add(new File(unpackedFolderPath + "trpoke"));
//                        TrainerReturnGen4 trainerReturn= editor.trainersToSheets(unpackedFolderPath + "trdata",unpackedFolderPath + "trpoke");
//                        if(contains(selected,"Data"))
//                            api.updateSheet("Trainer Data",trainerReturn.getTrainerData());
//                        if(contains(selected,"Pokemon"))
//                            api.updateSheet("Trainer Pokemon",trainerReturn.getTrainerPokemon());
//                    }
//                    break;
//
//
//                default:
//                    throw new IOException("Invalid game");
//            }
//
//
//
//        }
//        catch (IOException exception)
//        {
//            exception.printStackTrace();
//            JOptionPane.showMessageDialog(this,"An error has occurred. Please check the cmd window for further details.","Error",JOptionPane.ERROR_MESSAGE);
//            dispose();
//            parent.toFront();
//            parent.setEnabled(true);
//        }
//
//        for(File file : toDelete)
//        {
//            clearDirs(file);
//        }
//
//        if (editorTree.getCheckedPaths().length == 0)
//            JOptionPane.showMessageDialog(this,"You haven't selected anything to apply!","Error",JOptionPane.ERROR_MESSAGE);
//        else if (!contains(selected,"Tutor Move"))
//        {
//            if(displayEndMessage)
//            {
//                JOptionPane.showMessageDialog(this,"ROM applied to sheets!","Success",JOptionPane.INFORMATION_MESSAGE);
//            }
//            System.out.println("ROM applied to sheets");
//            dispose();
//            if (parent != null)
//            {
//                parent.toFront();
//                parent.setEnabled(true);
//            }
//        }
//    }
//
//    private void unpack(String inputNarc, String outputDir) throws IOException
//    {
//        Narctowl narctowl= new Narctowl(true);
//        narctowl.unpack(inputNarc,outputDir);
//        toDelete.add(new File(outputDir));
//    }
//
//    private void infoButtonActionPerformed(ActionEvent e)
//    {
//        JOptionPane.showMessageDialog(this,"This feature lets you take data from your ROM and upload it to your Google Sheets document. If you have previously made edits to your ROM using another tool, this is how you can make those edits appear in the Google Sheet. Uploading any data will overwrite the existing data in the sheet, so be careful.","Info",JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    private void thisWindowClosing(WindowEvent e)
//    {
//        parent.toFront();
//        parent.setEnabled(true);
//        dispose();
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        scrollPane1 = new JScrollPane();
//        editorTree = new JCheckboxTree();
//        optionsButton = new JButton();
//        infoButton = new JButton();
//        applyButton = new JButton();
//
//        //======== this ========
//        setTitle("Apply ROM to Sheet");
//        setMinimumSize(new Dimension(50, 50));
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                thisWindowClosing(e);
//            }
//        });
//        var contentPane = getContentPane();
//        contentPane.setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[fill]" +
//            "[fill]" +
//            "[grow,fill]",
//            // rows
//            "[grow,fill]" +
//            "[]"));
//
//        //======== scrollPane1 ========
//        {
//            scrollPane1.setViewportView(editorTree);
//        }
//        contentPane.add(scrollPane1, "cell 0 0 3 1");
//
//        //---- optionsButton ----
//        optionsButton.setText("Options");
//        optionsButton.addActionListener(e -> optionsButtonActionPerformed(e));
//        contentPane.add(optionsButton, "cell 0 1");
//
//        //---- infoButton ----
//        infoButton.setText("Info");
//        infoButton.addActionListener(e -> infoButtonActionPerformed(e));
//        contentPane.add(infoButton, "cell 1 1");
//
//        //---- applyButton ----
//        applyButton.setText("Apply");
//        applyButton.addActionListener(e -> applyButtonActionPerformed(e));
//        contentPane.add(applyButton, "cell 2 1");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    // Generated using JFormDesigner non-commercial license
//    private JScrollPane scrollPane1;
//    private JCheckboxTree editorTree;
//    private JButton optionsButton;
//    private JButton infoButton;
//    private JButton applyButton;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//
//    private boolean contains(String[] arr, String str)
//    {
//        for(String s : arr)
//        {
//            if(s.toLowerCase().contains(str.toLowerCase()))
//                return true;
//        }
//        return false;
//    }
//
//    private static void clearDirs(File folder)
//    {
//        try
//        {
//            FileUtils.deleteDirectory(folder);
//        }
//        catch(IOException e)
//        {
//            System.err.println("\tFailed to delete folder: " + folder.getAbsolutePath());
//            e.printStackTrace();
//        }
//
//        if(folder.exists())
//        {
//            System.err.println("\tFolder wasn't deleted?");
//        }
//    }
//
//    public static void makeVolatile(File folder)
//    {
//        folder.deleteOnExit();
//        for(File f : Objects.requireNonNull(folder.listFiles()))
//        {
//            if(f.isDirectory())
//                makeVolatile(f);
//            else
//                f.deleteOnExit();
//        }
//    }
//}
