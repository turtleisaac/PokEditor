///*
// * Created by JFormDesigner on Thu Dec 31 14:18:05 EST 2020
// */
//
//package io.github.turtleisaac.pokeditor.gui.projects.projectwindow.sheets;
//
//import java.awt.event.*;
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.text.DefaultFormatter;
//import javax.swing.text.DefaultFormatterFactory;
//
//import io.github.turtleisaac.pokeditor.gui.projects.projectwindow.ProjectWindow;
//import io.github.turtleisaac.pokeditor.project.Game;
//import io.github.turtleisaac.pokeditor.sheets.GoogleSheetsAPI;
//import net.miginfocom.swing.*;
//
//import java.awt.*;
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.security.GeneralSecurityException;
//import java.util.Objects;
//
///**
// * @author turtleisaac
// */
//public class SheetsSetup extends JFrame {
//
//    private JFrame frame;
//    private final ProjectWindow parent;
//    GoogleSheetsAPI api;
//
//    public SheetsSetup(Game game, ProjectWindow projectWindow)
//    {
//        initComponents();
//        parent= projectWindow;
//
//        sheetsLinkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        sheetsLinkLabel.setForeground(new Color(27, 148, 255, 255));
//
//        Dimension minimum= new Dimension();
//        minimum.setSize(1100,300);
//
//        setPreferredSize(minimum);
//        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//        pack();
//        setVisible(true);
//        toFront();
//
//        switch(game)
//        {
//            case Diamond:
//                sheetsLinkLabel.setText("https://docs.google.com/spreadsheets/d/1adZg_ehcfHcOnyQaggqOCRPjH0AgjTXfy4lN0QuwrTA/edit?usp=sharing");
//                break;
//
//            case Pearl:
//                sheetsLinkLabel.setText("https://docs.google.com/spreadsheets/d/1I03U9nJoo-3jm2hVPKmCWA84C9JesuE7dQ9QnKEGBMI/edit?usp=sharing");
//                break;
//
//            case Platinum:
//                sheetsLinkLabel.setText("https://docs.google.com/spreadsheets/d/10TABGoNr9t_7gjyTaTt3ZioaUzAUOHf8Tnl2EJSWicU/edit?usp=sharing");
//                break;
//
//            case HeartGold:
//                sheetsLinkLabel.setText("https://docs.google.com/spreadsheets/d/1YtxLNls6qRIHmtEh2ESvCIoOYO1xIZJmawffKjKuJRs/edit?usp=sharing");
//                break;
//
//            case SoulSilver:
//                sheetsLinkLabel.setText("https://docs.google.com/spreadsheets/d/1H-Is3X4uhmFL45f3Xk0UYF2_JkbmNovsDmUP-MzF_jA/edit?usp=sharing");
//                break;
//
//            case Black:
//                sheetsLinkLabel.setText("This template has not been made yet.");
//                break;
//
//            case White:
//                sheetsLinkLabel.setText("This template has not been made yet.");
//                break;
//
//            case Black2:
//                sheetsLinkLabel.setText("This template has not been made yet.");
//                break;
//
//            case White2:
//                sheetsLinkLabel.setText("This template has not been made yet.");
//                break;
//        }
//
//        sheetsLinkLabel.setForeground(new Color(27, 148, 255, 255));
//
//        DefaultFormatter formatter= new DefaultFormatter();
//        formatter.setCommitsOnValidEdit(true);
//        DefaultFormatterFactory factory= new DefaultFormatterFactory(formatter);
//        enterLinkTextField.setText("");
//        enterLinkTextField.setFormatterFactory(factory);
//
//        enterLinkTextField.getDocument().addDocumentListener(new DocumentListener()
//        {
//            @Override
//            public void insertUpdate(DocumentEvent e)
//            {
//                String text= enterLinkTextField.getText();
//
//                if(!text.contains("https://"))
//                    text= "https://" + text;
//
//                if(text.contains("https://docs.google.com/spreadsheets/d/"))
//                {
//                    linkStatusLabel.setText("Valid link!");
//                    linkStatusLabel.setForeground(new Color(45, 253, 0, 255));
//
//                    try
//                    {
//                        String[] link= text.split("/");
//                        spreadsheetIdLabel.setText(link[5]);
//                        spreadsheetIdLabel.setForeground(new Color(45, 253, 0, 255));
//                        connectButton.setEnabled(true);
//                    }
//                    catch (NullPointerException | ArrayIndexOutOfBoundsException ignored)
//                    {
//
//                    }
//                }
//                else
//                {
//                    linkStatusLabel.setText("Invalid link!");
//                    linkStatusLabel.setForeground(new Color(253, 0, 0, 255));
//                    spreadsheetIdLabel.setText("Spreadsheet ID can't be determined without valid link!");
//                    spreadsheetIdLabel.setForeground(new Color(253, 0, 0, 255));
//                }
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e)
//            {
//                String text= enterLinkTextField.getText();
//
//                if(!text.contains("https://"))
//                    text= "https://" + text;
//
//                if(text.contains("https://docs.google.com/spreadsheets/d/"))
//                {
//                    linkStatusLabel.setText("Valid link!");
//                    linkStatusLabel.setForeground(new Color(45, 253, 0, 255));
//
//                    try
//                    {
//                        String[] link= text.split("/");
//                        spreadsheetIdLabel.setText(link[5]);
//                        spreadsheetIdLabel.setForeground(new Color(45, 253, 0, 255));
//                        connectButton.setEnabled(true);
//                    }
//                    catch (NullPointerException | ArrayIndexOutOfBoundsException ignored)
//                    {
//
//                    }
//                }
//                else
//                {
//                    linkStatusLabel.setText("Invalid link!");
//                    linkStatusLabel.setForeground(new Color(253, 0, 0, 255));
//                    spreadsheetIdLabel.setText("Spreadsheet ID can't be determined without valid link!");
//                    spreadsheetIdLabel.setForeground(new Color(253, 0, 0, 255));
//                }
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e)
//            {
//                String text= enterLinkTextField.getText();
//
//                if(!text.contains("https://"))
//                    text= "https://" + text;
//
//                if(text.contains("https://docs.google.com/spreadsheets/d/"))
//                {
//                    linkStatusLabel.setText("Valid link!");
//                    linkStatusLabel.setForeground(new Color(45, 253, 0, 255));
//
//                    try
//                    {
//                        String[] link= text.split("/");
//                        spreadsheetIdLabel.setText(link[5]);
//                        spreadsheetIdLabel.setForeground(new Color(45, 253, 0, 255));
//                        connectButton.setEnabled(true);
//                    }
//                    catch (NullPointerException | ArrayIndexOutOfBoundsException ignored)
//                    {
//
//                    }
//                }
//                else
//                {
//                    linkStatusLabel.setText("Invalid link!");
//                    linkStatusLabel.setForeground(new Color(253, 0, 0, 255));
//                    spreadsheetIdLabel.setText("Spreadsheet ID can't be determined without valid link!");
//                    spreadsheetIdLabel.setForeground(new Color(253, 0, 0, 255));
//                }
//            }
//        });
//    }
//
//    private void connectButtonActionPerformed(ActionEvent e)
//    {
//        try
//        {
//            if(new File(parent.getProject().getProjectPath().toString() + "/tokens").exists())
//                clearDirectory(new File(parent.getProject().getProjectPath().toString() + "/tokens"));
//            api= new GoogleSheetsAPI(enterLinkTextField.getText(), parent.getProject().getProjectPath().toString(), false);
//            connectStatusLabel.setText("Connection Success!");
//            connectStatusLabel.setForeground(new Color(45, 253, 0, 255));
//            finishButton.setEnabled(true);
//
//        }
//        catch (IOException | GeneralSecurityException exception)
//        {
//            connectStatusLabel.setText("Connection Error!");
//            connectStatusLabel.setForeground(new Color(253, 0, 0, 255));
//        }
//    }
//
//    public GoogleSheetsAPI getApi()
//    {
//        return api;
//    }
//
//    private void finishButtonActionPerformed(ActionEvent e)
//    {
//        dispose();
//        parent.setApi(api);
//        try
//        {
//            parent.getTrainerPanel1().setApi(api);
//        } catch (IOException exception)
//        {
//            exception.printStackTrace();
//        }
//
//    }
//
//    private void sheetsLinkLabelMouseClicked(MouseEvent e)
//    {
//        try
//        {
//            Desktop.getDesktop().browse(new URI(sheetsLinkLabel.getText()));
//        } catch (URISyntaxException uriSyntaxException)
//        {
//            uriSyntaxException.printStackTrace();
//        } catch (IOException exception)
//        {
//            exception.printStackTrace();
//        }
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
//        copyInstructionsLabel = new JLabel();
//        sheetsLinkLabel = new JLabel();
//        separator2 = new JSeparator();
//        enterLinkInstructionsLabel = new JLabel();
//        enterLinkTextField = new JFormattedTextField();
//        linkStatusLabel = new JLabel();
//        spreadsheetIdLabel = new JLabel();
//        separator3 = new JSeparator();
//        connectButton = new JButton();
//        connectStatusLabel = new JLabel();
//        separator1 = new JSeparator();
//        finishButton = new JButton();
//
//        //======== this ========
//        setTitle("Google Sheets API Setup");
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent e) {
//                thisWindowClosing(e);
//            }
//        });
//        Container contentPane = getContentPane();
//        contentPane.setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[grow,fill]" +
//            "[grow,fill]",
//            // rows
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]" +
//            "[]"));
//
//        //---- copyInstructionsLabel ----
//        copyInstructionsLabel.setText("Make a copy of the following sheet in your Google Drive:");
//        contentPane.add(copyInstructionsLabel, "cell 0 0");
//
//        //---- sheetsLinkLabel ----
//        sheetsLinkLabel.setText("Link");
//        sheetsLinkLabel.setBackground(new Color(69, 73, 74));
//        sheetsLinkLabel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                sheetsLinkLabelMouseClicked(e);
//            }
//        });
//        contentPane.add(sheetsLinkLabel, "cell 1 0");
//        contentPane.add(separator2, "cell 0 1 2 1");
//
//        //---- enterLinkInstructionsLabel ----
//        enterLinkInstructionsLabel.setText("Enter the link to your copy of the sheet below (make sure to set it so that anyone with the link can edit):");
//        contentPane.add(enterLinkInstructionsLabel, "cell 0 2 2 1,alignx center,growx 0");
//        contentPane.add(enterLinkTextField, "cell 0 3 2 1,growx");
//
//        //---- linkStatusLabel ----
//        linkStatusLabel.setText("Link has not been entered yet");
//        contentPane.add(linkStatusLabel, "cell 0 4 2 1,alignx center,growx 0");
//
//        //---- spreadsheetIdLabel ----
//        spreadsheetIdLabel.setText("Spreadsheet ID can't be determined yet");
//        contentPane.add(spreadsheetIdLabel, "cell 0 5 2 1,alignx center,growx 0");
//        contentPane.add(separator3, "cell 0 6 2 1");
//
//        //---- connectButton ----
//        connectButton.setText("Connect");
//        connectButton.setEnabled(false);
//        connectButton.addActionListener(e -> connectButtonActionPerformed(e));
//        contentPane.add(connectButton, "cell 0 7 2 1,alignx center,growx 0");
//
//        //---- connectStatusLabel ----
//        connectStatusLabel.setText("Connection has not been attempted yet");
//        contentPane.add(connectStatusLabel, "cell 0 8 2 1,alignx center,growx 0");
//        contentPane.add(separator1, "cell 0 9 2 1");
//
//        //---- finishButton ----
//        finishButton.setText("Finish");
//        finishButton.setEnabled(false);
//        finishButton.addActionListener(e -> finishButtonActionPerformed(e));
//        contentPane.add(finishButton, "cell 0 10 2 1,growx");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    private JLabel copyInstructionsLabel;
//    private JLabel sheetsLinkLabel;
//    private JSeparator separator2;
//    private JLabel enterLinkInstructionsLabel;
//    private JFormattedTextField enterLinkTextField;
//    private JLabel linkStatusLabel;
//    private JLabel spreadsheetIdLabel;
//    private JSeparator separator3;
//    private JButton connectButton;
//    private JLabel connectStatusLabel;
//    private JSeparator separator1;
//    private JButton finishButton;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//
//    private void clearDirectory(File directory)
//    {
//        for(File subfile : Objects.requireNonNull(directory.listFiles()))
//        {
//            if(subfile.isDirectory())
//            {
//                clearDirectory(subfile);
//            }
//            else
//            {
//                subfile.delete();
//            }
//        }
//        directory.delete();
//    }
//}
