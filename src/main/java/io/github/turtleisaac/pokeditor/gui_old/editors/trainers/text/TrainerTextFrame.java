///*
// * Created by JFormDesigner on Tue Nov 23 17:50:50 CST 2021
// */
//
//package io.github.turtleisaac.pokeditor.gui.editors.trainers.text;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.stream.Collectors;
//import javax.swing.*;
//
//import com.jidesoft.swing.ComboBoxSearchable;
//import io.github.turtleisaac.pokeditor.editors.text.TextBank;
//import io.github.turtleisaac.pokeditor.editors.text.TextEditor;
//import io.github.turtleisaac.pokeditor.editors.trainers.gen4.TrainerText;
//import io.github.turtleisaac.pokeditor.gui.editors.trainers.TrainerPanel;
//import io.github.turtleisaac.pokeditor.project.Project;
//import net.miginfocom.swing.*;
//import org.apache.commons.io.FileUtils;
//
///**
// * @author turtleisaac
// */
//public class TrainerTextFrame extends JFrame {
//    private TrainerPanel trainerPanel;
//    private int trainerId;
//
//    private Project project;
//
//    private TextBank textBank;
//    private ArrayList<String> messages;
//
//    private DefaultListModel<String> listModel;
//
//    private ArrayList<TrainerText> trainerTextsCopy;
//
//    private int firstTextIndex;
//    private int numOriginalEntries;
//
//    private ArrayList<TrainerText> thisTrainerTexts;
//    private HashMap<TrainerText, TrainerTextEntryPanel> trainerTextEntryPanels;
//
//    public static final ArrayList<Integer> activationConditionToId = new ArrayList<>(Arrays.asList(0, 0xD, 0xE, 1, 2, 0xB, 0xC, 0xF, 0x10, 0x14, 0x11, 3, 4, 5, 6, 0x12, 7, 8, 9, 0xA, 0x13));
//
//    protected static final ArrayList<String> activationConditions = new ArrayList<>(Arrays.asList(
//            "0. Pre-Battle Overworld",
//            "1. In-Battle Any Pokemon Damaged & Still Alive",
//            "2. In-Battle Any Pokemon Half HP & Still Alive",
//            "3. In-Battle Player Win",
//            "4. Post-Battle Trainer Defeat Overworld",
//            "5. Pre-Battle Overworld Jogger Night (Unused?)",
//            "6. Pre-Battle Overworld Policeman Day (Unused?)",
//            "7. In-Battle Last Pokemon",
//            "8. In-Battle Last Pokemon Critical HP & Still Alive",
//            "9. In-Battle Player Lose",
//            "10. Pre-Battle Vs. Seeker Overworld",
//            "11. (DOUBLE) Pre-Battle Trainer#1 Overworld",
//            "12. (DOUBLE) In-Battle Trainer#1 Defeat",
//            "13. (DOUBLE) Post-Battle Trainer#1 Defeat Overworld",
//            "14. (DOUBLE) Trainer#1 Player One Party Member Overworld",
//            "15. (DOUBLE) Pre-Battle Trainer#1 Vs. Seeker Overworld",
//            "16. (DOUBLE) Pre-Battle Trainer#2 Overworld",
//            "17. (DOUBLE) In-Battle Trainer#2 Defeat",
//            "18. (DOUBLE) Post-Battle Trainer#2 Defeat Overworld",
//            "19. (DOUBLE) Trainer#2 Player One Party Member Overworld",
//            "20. (DOUBLE) Pre-Battle Trainer#2 Vs. Seeker Overworld"
//    ));
//
//    public TrainerTextFrame(TrainerPanel trainerPanel, Project project, ArrayList<TrainerText> trainerTexts, int trainerId, String trainerName) throws IOException
//    {
//        initComponents();
//        setVisible(true);
//
//        this.trainerPanel = trainerPanel;
//        this.project = project;
//        this.trainerId = trainerId;
//
//        setTitle("Trainer Text Editor - " + trainerName);
//        entryTabbedPane.setMaximumSize(entryTabbedPane.getSize());
//
//        textBank = TextBank.getBankID(TextBank.TextBankTypes.TRAINER_TEXT, project.getBaseRom());
//        loadData(trainerTexts);
//    }
//
//    private void loadData(ArrayList<TrainerText> trainerTexts) throws IOException
//    {
//        trainerTextsCopy = new ArrayList<>(trainerPanel.getTrainerTexts());
//        this.firstTextIndex = -1;
//
//        messages = new ArrayList<>(Arrays.asList(TextEditor.getBank(project, textBank)));
//
//        ArrayList<TrainerText> newTrainerTexts = new ArrayList<>();
//        for(int i = 0; i < trainerTextsCopy.size(); i++)
//        {
//            TrainerText text = trainerTextsCopy.get(i);
//            int textId = text.getTextId();
//            int trainerId = text.getTrainerId();
//            int condition = text.getCondition();
//            String trainerMessage = messages.get(textId);
//            newTrainerTexts.add(new TrainerText()
//            {
//                @Override
//                public int getTextId()
//                {
//                    return textId;
//                }
//
//                @Override
//                public int getTrainerId()
//                {
//                    return trainerId;
//                }
//
//                @Override
//                public int getCondition()
//                {
//                    return condition;
//                }
//
//                @Override
//                public String getText()
//                {
//                    return trainerMessage;
//                }
//            });
//        }
//        trainerTextsCopy = new ArrayList<>(newTrainerTexts);
//
//        thisTrainerTexts = new ArrayList<>();
//        for (int i = 0; i < trainerTextsCopy.size(); i++)
//        {
//            TrainerText text = trainerTextsCopy.get(i);
//            if (trainerId == text.getTrainerId())
//            {
//                if (firstTextIndex == -1) {
//                    firstTextIndex = i;
//                }
//                thisTrainerTexts.add(text);
//            }
//        }
//
//        numOriginalEntries = thisTrainerTexts.size();
//
//        trainerTextEntryPanels = new HashMap<>();
//
//        if (thisTrainerTexts.size() == 0)
//        {
//            if (JOptionPane.showConfirmDialog(this, "This trainer has no text entries. Do you want to initialize one?", "Trainer Text Editor", JOptionPane.YES_NO_OPTION) == 0) //yes
//            {
//                addActionPerformed(null);
//                firstTextIndex = trainerTextsCopy.size();
//            }
//            else //no
//            {
//                thisWindowClosed(null);
//            }
//            return;
//        }
//
//        int i = 0;
//        for(TrainerText trainerText1 : thisTrainerTexts)
//        {
//            TrainerTextEntryPanel panel = new TrainerTextEntryPanel(this, trainerText1);
//            trainerTextEntryPanels.put(trainerText1, panel);
//            entryTabbedPane.addTab("" + i++, panel);
//            trainerTextsCopy.remove(trainerText1);
//        }
//    }
//
//    public String getMessage(int idx)
//    {
//        return messages.get(idx);
//    }
//
//    public void setMessage(int idx, String message)
//    {
//        messages.set(idx, message);
//    }
//
//    private void createUIComponents() {
//        // TODO: add custom component creation code here
//    }
//
//    private void thisWindowClosed(WindowEvent e) {
//        trainerPanel.setEnabled(true);
//        dispose();
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
//    private void addActionPerformed(ActionEvent e) {
//        int lastEntry = messages.size();
//        TrainerText text = new TrainerText()
//        {
//            @Override
//            public int getTextId()
//            {
//                return lastEntry;
//            }
//
//            @Override
//            public int getTrainerId()
//            {
//                return trainerId;
//            }
//
//            @Override
//            public int getCondition()
//            {
//                return -1;
//            }
//
//            @Override
//            public String getText()
//            {
//                return "";
//            }
//        };
//
//        messages.add("");
//        thisTrainerTexts.add(text);
//        TrainerTextEntryPanel panel = new TrainerTextEntryPanel(this, text);
//        trainerTextEntryPanels.put(text, panel);
//        entryTabbedPane.addTab("" + entryTabbedPane.getTabCount(), panel);
//    }
//
//    private void removeActionPerformed(ActionEvent e) {
//        if (entryTabbedPane.getSelectedIndex() != -1)
//        {
//            int selected = entryTabbedPane.getSelectedIndex();
//            TrainerText textToRemove = thisTrainerTexts.remove(selected);
//            trainerTextEntryPanels.remove(textToRemove);
//            entryTabbedPane.remove(selected);
//
//            for (int i = 0; i < entryTabbedPane.getTabCount(); i++)
//            {
//                entryTabbedPane.setTitleAt(i, "" + i);
//            }
//        }
//        else
//        {
//            JOptionPane.showMessageDialog(this, "You can't remove an entry that doesn't exist.", "Action Prohibited", JOptionPane.INFORMATION_MESSAGE);
//        }
//    }
//
//    private void saveActionPerformed(ActionEvent e) {
//        StringBuilder conditionsErrorMessageBuilder = new StringBuilder("Error(s):\n");
//        boolean entryErrorOccurred = false;
//        for(int i = 0; i < thisTrainerTexts.size() - 1; i++)
//        {
//            for(int j = i+1; j < thisTrainerTexts.size(); j++)
//            {
//                TrainerTextEntryPanel panel1 = trainerTextEntryPanels.get(thisTrainerTexts.get(i));
//                TrainerTextEntryPanel panel2 = trainerTextEntryPanels.get(thisTrainerTexts.get(j));
//                if (panel1.getActivationCondition() == panel2.getActivationCondition())
//                {
//                    entryErrorOccurred = true;
//                    conditionsErrorMessageBuilder.append("\"")
//                            .append(activationConditions.get(activationConditionToId.indexOf(panel1.getActivationCondition())))
//                            .append("\" (")
//                            .append(i)
//                            .append(") has same condition value as index ")
//                            .append(j)
//                            .append("\n");
//                }
//                else if (panel1.getActivationCondition() == -1)
//                {
//                    entryErrorOccurred = true;
//                    conditionsErrorMessageBuilder.append("\"")
//                            .append(activationConditions.get(activationConditionToId.indexOf(panel1.getActivationCondition())))
//                            .append("\" (")
//                            .append(i)
//                            .append(") does not have a condition value set\n");
//                }
//            }
//        }
//
//        if (entryErrorOccurred)
//        {
//            JOptionPane.showMessageDialog(this, conditionsErrorMessageBuilder.toString(), "Trainer Text Activation Condition Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        for (int i = 0; i < thisTrainerTexts.size(); i++)
//        {
//            TrainerText oldTextEntry = thisTrainerTexts.get(i);
//            TrainerTextEntryPanel panel = trainerTextEntryPanels.get(oldTextEntry);
//            int textId = oldTextEntry.getTextId();
//            int trainerId = oldTextEntry.getTrainerId();
//            int condition = panel.getActivationCondition();
//            String newText = panel.getText();
//            TrainerText text = new TrainerText()
//            {
//                @Override
//                public int getTextId()
//                {
//                    return textId;
//                }
//
//                @Override
//                public int getTrainerId()
//                {
//                    return trainerId;
//                }
//
//                @Override
//                public int getCondition()
//                {
//                    return condition;
//                }
//
//                @Override
//                public String getText()
//                {
//                    return newText;
//                }
//            };
//            messages.set(textId, newText);
//            trainerTextsCopy.add(text);
//        }
//
//        trainerTextsCopy = trainerTextsCopy.stream().sorted(new Comparator<TrainerText>()
//        {
//            @Override
//            public int compare(TrainerText o1, TrainerText o2)
//            {
//                int trainerDiff = o1.getTrainerId() - o2.getTrainerId();
//
//                if (trainerDiff != 0) {
//                    return trainerDiff;
//                }
//
//                return TrainerTextFrame.activationConditionToId.indexOf(o1.getCondition()) - TrainerTextFrame.activationConditionToId.indexOf(o2.getCondition());
//            }
//        }).collect(Collectors.toCollection(ArrayList::new));
//
//        trainerPanel.setTrainerTexts(trainerTextsCopy);
//
//        messages = new ArrayList<>();
//        for (TrainerText text : trainerTextsCopy) {
//            messages.add(text.getText());
//        }
//
//        try {
//            TextEditor.writeBank(project, messages, textBank, false);
//        }
//        catch (IOException exception) {
//            exception.printStackTrace();
//            JOptionPane.showMessageDialog(this, "An error in text writing occurred. Check the command-line output for more information.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//
//
//        int selected = entryTabbedPane.getSelectedIndex();
//        if (selected != -1)
//        {
//            reloadActionPerformed(null);
//            entryTabbedPane.setSelectedIndex(selected);
//        }
//    }
//
//    private void reloadActionPerformed(ActionEvent e) {
//        entryTabbedPane.removeAll();
//        try {
//            loadData(trainerPanel.getTrainerTexts());
//        }
//        catch (IOException exception) {
//            exception.printStackTrace();
//            JOptionPane.showMessageDialog(this, "An error in trainer text initialization occurred. Check the command-line output for more information.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        saveTrainerButton = new JButton();
//        addButton = new JButton();
//        removeButton = new JButton();
//        reloadButton = new JButton();
//        entryTabbedPane = new JTabbedPane();
//
//        //======== this ========
//        setMinimumSize(new Dimension(600, 250));
//        setTitle("Trainer Text Editor");
//        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//                thisWindowClosed(e);
//            }
//        });
//        var contentPane = getContentPane();
//        contentPane.setLayout(new MigLayout(
//            "hidemode 3",
//            // columns
//            "[grow,center]" +
//            "[grow,center]" +
//            "[grow,center]" +
//            "[grow,center]",
//            // rows
//            "[]" +
//            "[grow,fill]"));
//
//        //---- saveTrainerButton ----
//        saveTrainerButton.setText("Save Trainer");
//        saveTrainerButton.addActionListener(e -> saveActionPerformed(e));
//        contentPane.add(saveTrainerButton, "cell 0 0,growx");
//
//        //---- addButton ----
//        addButton.setText("Add");
//        addButton.addActionListener(e -> addActionPerformed(e));
//        contentPane.add(addButton, "cell 1 0,growx");
//
//        //---- removeButton ----
//        removeButton.setText("Remove");
//        removeButton.addActionListener(e -> removeActionPerformed(e));
//        contentPane.add(removeButton, "cell 2 0,growx");
//
//        //---- reloadButton ----
//        reloadButton.setText("Reload");
//        reloadButton.addActionListener(e -> reloadActionPerformed(e));
//        contentPane.add(reloadButton, "cell 3 0,growx");
//
//        //======== entryTabbedPane ========
//        {
//            entryTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
//        }
//        contentPane.add(entryTabbedPane, "cell 0 1 4 1,grow");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    // Generated using JFormDesigner non-commercial license
//    private JButton saveTrainerButton;
//    private JButton addButton;
//    private JButton removeButton;
//    private JButton reloadButton;
//    private JTabbedPane entryTabbedPane;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//}
