/*
 * Created by JFormDesigner on Sun Nov 21 15:30:19 CST 2021
 */

package io.github.turtleisaac.pokeditor.gui_old.tutorial;

import java.awt.*;
import javax.swing.*;
import net.miginfocom.swing.*;

/**
 * @author turtleisaac
 */
public class TutorialFrame extends JFrame {
    public TutorialFrame() {
        initComponents();
        setPreferredSize(new Dimension(800, 800));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        tutorialTabbedPane = new JTabbedPane();
        introPanel = new JPanel();
        introLabel1 = new JLabel();
        introLabel2 = new JLabel();
        introLabel3 = new JLabel();
        introLabel4 = new JLabel();
        introScrollPane = new JScrollPane();
        introTextArea = new JTextArea();
        introLabel5 = new JLabel();
        introLabel6 = new JLabel();
        introLabel7 = new JLabel();
        introLabel8 = new JLabel();
        introLabel9 = new JLabel();
        introLabel10 = new JLabel();
        mainPanel = new JPanel();
        label9 = new JLabel();
        textArea1 = new JTextArea();
        label10 = new JLabel();
        textArea2 = new JTextArea();
        label11 = new JLabel();
        textArea3 = new JTextArea();
        label12 = new JLabel();
        textArea4 = new JTextArea();
        label13 = new JLabel();
        textArea5 = new JTextArea();
        panel1 = new JPanel();
        label14 = new JLabel();
        textArea6 = new JTextArea();
        label15 = new JLabel();
        textArea7 = new JTextArea();
        label16 = new JLabel();
        textArea8 = new JTextArea();
        label17 = new JLabel();
        textArea9 = new JTextArea();
        label18 = new JLabel();
        textArea10 = new JTextArea();
        trainerPanel = new JPanel();
        label19 = new JLabel();
        encounterPanel = new JPanel();
        label21 = new JLabel();
        pokemonSpritesPanel = new JPanel();
        label20 = new JLabel();

        //======== this ========
        setTitle("Tutorial");
        var contentPane = getContentPane();
        contentPane.setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[fill]" +
            "[fill]",
            // rows
            "[grow,fill]" +
            "[]" +
            "[]"));

        //======== tutorialTabbedPane ========
        {
            tutorialTabbedPane.setTabPlacement(SwingConstants.LEFT);

            //======== introPanel ========
            {
                introPanel.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- introLabel1 ----
                introLabel1.setText("Welcome to PokEditor v2!");
                introLabel1.setFont(introLabel1.getFont().deriveFont(introLabel1.getFont().getStyle() | Font.BOLD, introLabel1.getFont().getSize() + 2f));
                introPanel.add(introLabel1, "cell 0 0");

                //---- introLabel2 ----
                introLabel2.setText("PokEditor v2 is a clean, advanced, and modern tool for hacking the 4th Generation of Pok\u00e9mon games.");
                introPanel.add(introLabel2, "cell 0 1");

                //---- introLabel3 ----
                introLabel3.setText("What Does PokEditor v2 Do?");
                introLabel3.setFont(introLabel3.getFont().deriveFont(introLabel3.getFont().getStyle() | Font.BOLD, introLabel3.getFont().getSize() + 2f));
                introPanel.add(introLabel3, "cell 0 2");

                //---- introLabel4 ----
                introLabel4.setText("PokEditor lets you edit various types of data in these games such as:");
                introPanel.add(introLabel4, "cell 0 3");

                //======== introScrollPane ========
                {
                    introScrollPane.setBorder(null);

                    //---- introTextArea ----
                    introTextArea.setText("-Species Personal Data (stats, types, abilities, etc...)\n-Species TM Learnsets\n-Species Level-Up Learnsets\n-Species Evolutions\n-Species Baby Forms\n-Moves\n-Items\n-Encounter Data\n-Trainers\n-Sprites\nAnd more....");
                    introTextArea.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                    introTextArea.setEditable(false);
                    introTextArea.setBackground(new Color(60, 63, 65));
                    introTextArea.setBorder(null);
                    introScrollPane.setViewportView(introTextArea);
                }
                introPanel.add(introScrollPane, "cell 0 4");

                //---- introLabel5 ----
                introLabel5.setText("What Games Does PokEditor v2 Support?");
                introLabel5.setFont(introLabel5.getFont().deriveFont(introLabel5.getFont().getStyle() | Font.BOLD, introLabel5.getFont().getSize() + 2f));
                introPanel.add(introLabel5, "cell 0 5");

                //---- introLabel6 ----
                introLabel6.setText("Pok\u00e9mon Platinum, HeartGold, and SoulSilver are the titles supported by PokEditor v2.");
                introPanel.add(introLabel6, "cell 0 6");

                //---- introLabel7 ----
                introLabel7.setText("Who made PokEditor v2?");
                introLabel7.setFont(introLabel7.getFont().deriveFont(introLabel7.getFont().getStyle() | Font.BOLD, introLabel7.getFont().getSize() + 2f));
                introPanel.add(introLabel7, "cell 0 7");

                //---- introLabel8 ----
                introLabel8.setText("PokEditor v2 was written by Turtleisaac, but it wouldn't have been possible without the efforts of many others.");
                introPanel.add(introLabel8, "cell 0 8");

                //---- introLabel9 ----
                introLabel9.setText("Special Thanks To (In no particular order):");
                introLabel9.setFont(introLabel9.getFont().deriveFont(introLabel9.getFont().getStyle() | Font.BOLD, introLabel9.getFont().getSize() + 2f));
                introPanel.add(introLabel9, "cell 0 9");

                //---- introLabel10 ----
                introLabel10.setText("JackHack96, Jay-San, BagBoy, Nomura, AdAstra, Mikelan, Vendor, FrankieD, Hello007, PlatinumMaster, and more");
                introPanel.add(introLabel10, "cell 0 10");
            }
            tutorialTabbedPane.addTab("Intro", introPanel);

            //======== mainPanel ========
            {
                mainPanel.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[grow,fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[268]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label9 ----
                label9.setText("Getting Started (ONLINE ONLY) (CURRENTLY BROKEN)");
                label9.setFont(label9.getFont().deriveFont(label9.getFont().getStyle() | Font.BOLD, label9.getFont().getSize() + 2f));
                mainPanel.add(label9, "cell 0 0");

                //---- textArea1 ----
                textArea1.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea1.setBorder(null);
                textArea1.setBackground(new Color(60, 63, 65));
                textArea1.setText("To get started in PokEditor v2, the first thing you are going to want to do is link your project with a Google Sheets spreadsheet utilizing the Google Sheets API. In order to do this, click the \"Google Sheets API Integration\" button in the \"Main\" tab of PokEditor v2. There will be a warning that pops up in a new window, so please read that and agree before proceeding.\n\nNow, follow the instructions on the API Integration Setup window that appears. If you haven't done so already, you are going to have to make a COPY of the provided template spreadsheet in your own Google Drive by going to \"File\" -> \"Make a copy.\" After you have done that, go to \"Share\" and set it so that \"Anyone with the link can edit.\" From here, just copy the share link it gives to you and paste it into the text field in the API Integration Setup window. Now you need to press \"Connect,\" and a webpage will open in your default web browser. Google flags PokEditor v2 as an  unverified program, so you have to press \"Advanced\" in the bottom left of the window and then press \"Continue to Quickstart (Unsafe).\" Now just give PokEditor v2 access to your sheet, and you can return to the API Integration Setup window and hit \"Finish.\"\n\nFrom here, a popup will appear that asks you if you want to update your sheets with the data from your game. See \"Apply ROM to Sheet\" for more info.");
                textArea1.setWrapStyleWord(true);
                textArea1.setLineWrap(true);
                mainPanel.add(textArea1, "cell 0 1");

                //---- label10 ----
                label10.setText("Apply ROM to Sheet");
                label10.setFont(label10.getFont().deriveFont(label10.getFont().getStyle() | Font.BOLD, label10.getFont().getSize() + 2f));
                mainPanel.add(label10, "cell 0 2");

                //---- textArea2 ----
                textArea2.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea2.setBorder(null);
                textArea2.setBackground(new Color(60, 63, 65));
                textArea2.setText("This function allows you to copy data from your ROM to your PokEditor sheets. What this will do is, for the sheets/ data you will select, copy the data in your ROM to to Google Sheets. This is so any changes you have possibly already made in another tool will sync to Google Sheets. If you have already been making edits to a PokEditor sheet and that data isn't present in your ROM, uploading that ROM data will overwrite whatever is present in the edited sheet, so be careful.");
                textArea2.setWrapStyleWord(true);
                textArea2.setLineWrap(true);
                mainPanel.add(textArea2, "cell 0 3");

                //---- label11 ----
                label11.setText("Apply Sheet to ROM");
                label11.setFont(label11.getFont().deriveFont(label11.getFont().getStyle() | Font.BOLD, label11.getFont().getSize() + 2f));
                mainPanel.add(label11, "cell 0 4");

                //---- textArea3 ----
                textArea3.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea3.setBorder(null);
                textArea3.setBackground(new Color(60, 63, 65));
                textArea3.setText("This function allows you to copy data from your PokEditor sheets to your local files. What this will do is, for the sheets/ data you will select, apply the changes you have made to the game. However, this isn't enough to produce an edited game file that you can now play. See \"Export ROM\" for more info.");
                textArea3.setWrapStyleWord(true);
                textArea3.setLineWrap(true);
                mainPanel.add(textArea3, "cell 0 5");

                //---- label12 ----
                label12.setText("Export ROM");
                label12.setFont(label12.getFont().deriveFont(label12.getFont().getStyle() | Font.BOLD, label12.getFont().getSize() + 2f));
                mainPanel.add(label12, "cell 0 6");

                //---- textArea4 ----
                textArea4.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea4.setBorder(null);
                textArea4.setBackground(new Color(60, 63, 65));
                textArea4.setText("This function allows you to export your changes as .nds file you can now play, use in another tool of your choice, or do whatever else you want to with.");
                textArea4.setWrapStyleWord(true);
                textArea4.setLineWrap(true);
                mainPanel.add(textArea4, "cell 0 7");

                //---- label13 ----
                label13.setText("The Sheet Preview Window");
                label13.setFont(label13.getFont().deriveFont(label13.getFont().getStyle() | Font.BOLD, label13.getFont().getSize() + 2f));
                mainPanel.add(label13, "cell 0 8");

                //---- textArea5 ----
                textArea5.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea5.setBorder(null);
                textArea5.setBackground(new Color(60, 63, 65));
                textArea5.setText("The Sheet Preview Window is exactly as its name implies. It allows you to PREVIEW your sheets from the PokEditor v2 GUI, but NOT to edit them. The PokEditor Google Sheets templates have been optimized and filled with features such as data validation, error correction, dropdown menus, and more, so they are BY FAR the best way to edit the majority of data with PokEditor v2.");
                textArea5.setWrapStyleWord(true);
                textArea5.setLineWrap(true);
                mainPanel.add(textArea5, "cell 0 9");
            }
            tutorialTabbedPane.addTab("Main - Online", mainPanel);

            //======== panel1 ========
            {
                panel1.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[grow,fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label14 ----
                label14.setText("Getting Started (OFFLINE ONLY)");
                label14.setFont(label14.getFont().deriveFont(label14.getFont().getStyle() | Font.BOLD, label14.getFont().getSize() + 2f));
                panel1.add(label14, "cell 0 0");

                //---- textArea6 ----
                textArea6.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea6.setBorder(null);
                textArea6.setBackground(new Color(60, 63, 65));
                textArea6.setText("To get started in PokEditor v2, the first thing you are going to want to do is set up your local sheet storage. In order to do this, click the \"Sheets Setup\" button in the \"Main\" tab of PokEditor v2. There will be a warning that pops up in a new window, so please read that and press \"No\".\n\nAfter this, PokEditor v2 will generate all of the spreadsheets that you need in order to edit data. You may have to restart PokEditor v2 after this step in order for the preview window to populate with data.");
                textArea6.setWrapStyleWord(true);
                textArea6.setLineWrap(true);
                panel1.add(textArea6, "cell 0 1");

                //---- label15 ----
                label15.setText("Apply ROM to Sheet");
                label15.setFont(label15.getFont().deriveFont(label15.getFont().getStyle() | Font.BOLD, label15.getFont().getSize() + 2f));
                panel1.add(label15, "cell 0 2");

                //---- textArea7 ----
                textArea7.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea7.setBorder(null);
                textArea7.setBackground(new Color(60, 63, 65));
                textArea7.setText("This function allows you to copy data from your ROM to your PokEditor sheets. What this will do is, for the sheets/ data you will select, copy the data in your ROM to your local sheets. This is so any changes you have possibly already made in another tool will be saved to your sheets. If you have already been making edits to a PokEditor sheet and that data isn't present in your ROM, uploading that ROM data will overwrite whatever is present in the edited sheet, so be careful.");
                textArea7.setWrapStyleWord(true);
                textArea7.setLineWrap(true);
                panel1.add(textArea7, "cell 0 3");

                //---- label16 ----
                label16.setText("Apply Sheet to ROM");
                label16.setFont(label16.getFont().deriveFont(label16.getFont().getStyle() | Font.BOLD, label16.getFont().getSize() + 2f));
                panel1.add(label16, "cell 0 4");

                //---- textArea8 ----
                textArea8.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea8.setBorder(null);
                textArea8.setBackground(new Color(60, 63, 65));
                textArea8.setText("This function allows you to copy data from your PokEditor sheets to your local files. What this will do is, for the sheets/ data you will select, apply the changes you have made to the game. However, this isn't enough to produce an edited game file that you can now play. See \"Export ROM\" for more info.");
                textArea8.setWrapStyleWord(true);
                textArea8.setLineWrap(true);
                panel1.add(textArea8, "cell 0 5");

                //---- label17 ----
                label17.setText("Export ROM");
                label17.setFont(label17.getFont().deriveFont(label17.getFont().getStyle() | Font.BOLD, label17.getFont().getSize() + 2f));
                panel1.add(label17, "cell 0 6");

                //---- textArea9 ----
                textArea9.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea9.setBorder(null);
                textArea9.setBackground(new Color(60, 63, 65));
                textArea9.setText("This function allows you to export your changes as .nds file you can now play, use in another tool of your choice, or do whatever else you want to with.");
                textArea9.setWrapStyleWord(true);
                textArea9.setLineWrap(true);
                panel1.add(textArea9, "cell 0 7");

                //---- label18 ----
                label18.setText("The Sheet Preview Window");
                label18.setFont(label18.getFont().deriveFont(label18.getFont().getStyle() | Font.BOLD, label18.getFont().getSize() + 2f));
                panel1.add(label18, "cell 0 8");

                //---- textArea10 ----
                textArea10.setFont(new Font(".AppleSystemUIFont", Font.PLAIN, 13));
                textArea10.setBorder(null);
                textArea10.setBackground(new Color(60, 63, 65));
                textArea10.setText("The Sheet Preview Window is exactly as its name implies - it allows you to PREVIEW your sheets from the PokEditor v2 GUI. You can also make edits here, but you will be missing out on a lot of features such as error correction, data validation, dropdown menus, and more. If an editor has a dedicated tab within PokEditor v2, you 100% should be using that to make edits instead of editing the sheet manually.");
                textArea10.setWrapStyleWord(true);
                textArea10.setLineWrap(true);
                panel1.add(textArea10, "cell 0 9");
            }
            tutorialTabbedPane.addTab("Main - Offline", panel1);

            //======== trainerPanel ========
            {
                trainerPanel.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label19 ----
                label19.setText("Tutorial coming soon");
                label19.setFont(label19.getFont().deriveFont(label19.getFont().getStyle() | Font.BOLD, label19.getFont().getSize() + 2f));
                trainerPanel.add(label19, "cell 0 0");
            }
            tutorialTabbedPane.addTab("Trainers", trainerPanel);

            //======== encounterPanel ========
            {
                encounterPanel.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label21 ----
                label21.setText("Tutorial coming soon");
                label21.setFont(label21.getFont().deriveFont(label21.getFont().getStyle() | Font.BOLD, label21.getFont().getSize() + 2f));
                encounterPanel.add(label21, "cell 0 0");
            }
            tutorialTabbedPane.addTab("Encounters", encounterPanel);

            //======== pokemonSpritesPanel ========
            {
                pokemonSpritesPanel.setLayout(new MigLayout(
                    "hidemode 3",
                    // columns
                    "[fill]" +
                    "[fill]",
                    // rows
                    "[]" +
                    "[]" +
                    "[]"));

                //---- label20 ----
                label20.setText("Tutorial coming soon");
                label20.setFont(label20.getFont().deriveFont(label20.getFont().getStyle() | Font.BOLD, label20.getFont().getSize() + 2f));
                pokemonSpritesPanel.add(label20, "cell 0 0");
            }
            tutorialTabbedPane.addTab("Sprites", pokemonSpritesPanel);

            tutorialTabbedPane.setSelectedIndex(0);
        }
        contentPane.add(tutorialTabbedPane, "cell 0 0");
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JTabbedPane tutorialTabbedPane;
    private JPanel introPanel;
    private JLabel introLabel1;
    private JLabel introLabel2;
    private JLabel introLabel3;
    private JLabel introLabel4;
    private JScrollPane introScrollPane;
    private JTextArea introTextArea;
    private JLabel introLabel5;
    private JLabel introLabel6;
    private JLabel introLabel7;
    private JLabel introLabel8;
    private JLabel introLabel9;
    private JLabel introLabel10;
    private JPanel mainPanel;
    private JLabel label9;
    private JTextArea textArea1;
    private JLabel label10;
    private JTextArea textArea2;
    private JLabel label11;
    private JTextArea textArea3;
    private JLabel label12;
    private JTextArea textArea4;
    private JLabel label13;
    private JTextArea textArea5;
    private JPanel panel1;
    private JLabel label14;
    private JTextArea textArea6;
    private JLabel label15;
    private JTextArea textArea7;
    private JLabel label16;
    private JTextArea textArea8;
    private JLabel label17;
    private JTextArea textArea9;
    private JLabel label18;
    private JTextArea textArea10;
    private JPanel trainerPanel;
    private JLabel label19;
    private JPanel encounterPanel;
    private JLabel label21;
    private JPanel pokemonSpritesPanel;
    private JLabel label20;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
