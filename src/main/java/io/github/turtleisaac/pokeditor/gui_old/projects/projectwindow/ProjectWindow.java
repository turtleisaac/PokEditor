///*
// * Created by JFormDesigner on Wed Dec 30 13:51:19 EST 2020
// */
//
//package io.github.turtleisaac.pokeditor.gui.projects.projectwindow;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.io.*;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.attribute.FileTime;
//import java.security.GeneralSecurityException;
//import java.util.*;
//import java.util.List;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//import javax.swing.*;
//import javax.swing.border.*;
//import javax.swing.table.DefaultTableCellRenderer;
//import javax.swing.table.DefaultTableModel;
//import javax.swing.tree.DefaultMutableTreeNode;
//
//import com.jackhack96.dspre.nitro.rom.ROMUtils;
//import com.jackhack96.jNdstool.main.JNdstool;
//import com.jidesoft.swing.ComboBoxSearchable;
//import io.github.turtleisaac.pokeditor.editors.items.ItemTableEntry;
//import io.github.turtleisaac.pokeditor.editors.items.ItemTableParser;
//import io.github.turtleisaac.pokeditor.editors.text.TextBank;
//import io.github.turtleisaac.pokeditor.framework.narctowl.Narctowl;
//import io.github.turtleisaac.pokeditor.editors.personal.gen4.PersonalEditor;
//import io.github.turtleisaac.pokeditor.editors.personal.gen4.PersonalReturnGen4;
//import io.github.turtleisaac.pokeditor.editors.spritepositions.SpriteDataProcessor;
//import io.github.turtleisaac.pokeditor.editors.text.TextEditor;
//import io.github.turtleisaac.pokeditor.gui.JCheckboxTree;
//import io.github.turtleisaac.pokeditor.gui.MyFilter;
//import io.github.turtleisaac.pokeditor.gui.editors.encounters.johto.*;
//import io.github.turtleisaac.pokeditor.gui.editors.encounters.sinnoh.SinnohEncounterPanel;
//import io.github.turtleisaac.pokeditor.PokEditor;
//import io.github.turtleisaac.pokeditor.gui.projects.projectwindow.console.ConsoleWindow;
//import io.github.turtleisaac.pokeditor.gui.editors.sprites.pokemon.PokemonSpritePanel;
//import io.github.turtleisaac.pokeditor.gui.editors.sprites.trainers.*;
//import io.github.turtleisaac.pokeditor.gui.editors.trainers.TrainerPanel;
//import io.github.turtleisaac.pokeditor.gui.projects.projectwindow.sheets.RomApplier;
//import io.github.turtleisaac.pokeditor.gui.projects.projectwindow.sheets.SheetApplier;
//import io.github.turtleisaac.pokeditor.gui.projects.projectwindow.sheets.SheetsSetup;
//import io.github.turtleisaac.pokeditor.gui.tutorial.TutorialFrame;
//import io.github.turtleisaac.pokeditor.project.Game;
//import io.github.turtleisaac.pokeditor.project.Project;
//import io.github.turtleisaac.pokeditor.utilities.RandomizerUtils;
//import io.github.turtleisaac.pokeditor.utilities.TableLocator;
//import io.github.turtleisaac.pokeditor.utilities.TablePointers;
//import net.miginfocom.swing.*;
//import org.apache.commons.io.FileDeleteStrategy;
//import org.apache.commons.io.FileUtils;
//import sun.swing.DefaultLookup;
//import io.github.turtleisaac.pokeditor.sheets.GoogleSheetsAPI;
//
///**
// * @author turtleisaac
// */
//public class ProjectWindow extends JFrame
//{
//    private final JFrame mainMenu;
//
//    private Project project;
//    private GoogleSheetsAPI api;
//    private String projectPath;
//    private Game baseRom;
//
//    private String sheetName;
//    private String sheetType;
//    private List<List<Object>> sheetData;
//    private List<List<Color>> colorData;
//
//    private ConsoleWindow console;
//    private TutorialFrame tutorial;
//
//    private HashMap<JPanel, JFrame> poppedEditors;
//
//    private static String[] itemNames;
//    private static ArrayList<ItemTableEntry> itemTableData;
//
//    public ProjectWindow(String xmlPath, JFrame mainMenu, ConsoleWindow console) throws IOException
//    {
//        project= Project.readFromXml(xmlPath);
//        projectPath= project.getProjectPath().toString();
//        boolean projectUpdate = false;
//
//        if (!PokEditor.versionNumber.equals(project.getProgramVersion()))
//        {
//            int continueChoice;
//            if (project.getProgramVersion() != null)
//            {
//                continueChoice = JOptionPane.showConfirmDialog(this, "This PokEditor project was last opened using PokEditor " + project.getProgramVersion() + ". You are using PokEditor " + PokEditor.versionNumber + ". Do you wish to continue?", project.getName() + " (PokEditor)", JOptionPane.YES_NO_OPTION);
//            }
//            else
//            {
//                continueChoice = JOptionPane.showConfirmDialog(this, "This PokEditor project was last opened using a version of PokEditor older than your current program version, which is PokEditor " + PokEditor.versionNumber + ". Do you wish to continue?", project.getName() + " (PokEditor)", JOptionPane.YES_NO_OPTION);
//            }
//
//            if (continueChoice != JOptionPane.YES_OPTION) {
//                System.exit(1);
//            } else {
//                project.setProgramVersion(PokEditor.versionNumber);
//                projectUpdate = true;
//            }
//        }
//
//        File resourceDir = new File(projectPath + File.separator + "Program Files");
//        if(!resourceDir.exists())
//        {
//            unpackProgramFilesDir(resourceDir);
//        }
//        else if (projectUpdate && PokEditor.programFilesModifiedThisVersion)
//        {
//            int unpackChoice = JOptionPane.showConfirmDialog(this, "If you have made any changes to the Program Files folder in your project directory, please make a backup of them now. After pressing \"Ok\", this folder will be deleted and replaced with a new version.", project.getName() + " (PokEditor)", JOptionPane.OK_CANCEL_OPTION);
//            if (unpackChoice == JOptionPane.OK_OPTION)
//            {
//                cleanupDirs(resourceDir);
//                unpackProgramFilesDir(resourceDir);
//            }
//            else
//            {
//                System.exit(1);
//            }
//        }
//
//        initComponents();
//
//        //TODO RE-ENABLE THESE FEATURES ONCE THEY ARE PROGRAMMED
//        tabbedPane1.remove(starterPanel);
//        tabbedPane1.remove(openingPanel);
//        tabbedPane1.remove(trainerSpritePanel);
//        tabbedPane1.remove(overworldSpritePanel);
//
//        //TODO RE-ENABLE THESE FEATURES AFTER DEV BUILD DISTRIBUTION
////        tabbedPane1.remove(johtoEncounterPanel);
////        tabbedPane1.remove(sinnohEncounterPanel);
////        tabbedPane1.remove(mainPanel);
////        tabbedPane1.remove(trainerPanel1);
//
////        menuBar.remove(sheetsMenu);
////        menuBar.remove(toolsMenu);
////        menuBar.remove(debugMenu);
////        menuBar.remove(helpMenu);
//
//        this.mainMenu= mainMenu;
//        this.console= console;
//        this.tutorial = new TutorialFrame();
//        tutorial.setSize(800, 800);
//        tutorial.setLocationRelativeTo(this);
//        tutorial.setVisible(false);
//
//        baseRom= project.getBaseRom();
//        setTitle(project.getName() + " (PokEditor)");
//        menuBar.setVisible(true);
//        openProjectButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/open_file.png")));
//        exportRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/save_rom.png")));
//        projectInfoButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/give_pokedex.png")));
//
//        if(new File(project.getProjectPath().toString() + File.separator + "api.ser").exists())
//        {
//            try
//            {
//                ObjectInputStream in= new ObjectInputStream(new FileInputStream(projectPath + "/api.ser"));
//                String link= (String) in.readObject();
//
//                if(new File(project.getProjectPath().toString() + File.separator + "tokens").exists())
//                    api= new GoogleSheetsAPI(link, projectPath, false);
//                else
//                    api= new GoogleSheetsAPI(link, projectPath, true);
//
//                if(!api.isLocal())
//                {
//                    linkTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                    linkTextField.setEnabled(true);
//                    linkTextField.setText(link);
//                    linkTextField.setForeground(new Color(27, 148, 255, 255));
//                    sheetPreviewTable.setEnabled(false);
//                }
//                else
//                {
//                    linkTextField.setText("You are using local sheet storage.");
//                    linkTextField.setForeground(new Color(255, 171, 27, 255));
//                    sheetPreviewTable.setEnabled(true);
//                }
//
//
//                sheetChooserComboBox.setEnabled(true);
//                applyToRomButton.setEnabled(true);
//                applyToSheetButton.setEnabled(true);
//                sheetRefreshChangesButton.setEnabled(true);
//                sheetUploadChangesButton.setEnabled(true);
//                setSheetChooserComboBox(api.getSheetNames());
//            } catch (ClassNotFoundException | IOException | GeneralSecurityException e)
//            {
//                JOptionPane.showMessageDialog(this,"Unable to load stored API credentials. Please re-open project.\nIf this continues to be unsucessful, re-do the API configuration.","Error",JOptionPane.ERROR_MESSAGE);
//                e.printStackTrace();
//
//                api= null;
//                linkTextField.setEnabled(false);
//                sheetChooserComboBox.setEnabled(false);
//                applyToRomButton.setEnabled(false);
//                applyToSheetButton.setEnabled(false);
//                sheetPreviewTable.setEnabled(false);
//                sheetRefreshChangesButton.setEnabled(false);
//                sheetUploadChangesButton.setEnabled(false);
//            }
//        }
//
//        switch(baseRom)
//        {
//            case Diamond:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/diamond.png")));
//                break;
//
//            case Pearl:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/pearl.png")));
//                break;
//
//            case Platinum:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/platinum.png")));
//                break;
//
//            case HeartGold:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/heartgold.png")));
//                break;
//
//            case SoulSilver:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/soulsilver.png")));
//                break;
//
//            case Black:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/black.png")));
//                break;
//
//            case White:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/white.png")));
//                break;
//
//            case Black2:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/black2.png")));
//                break;
//
//            case White2:
//                baseRomButton.setIcon(new ImageIcon(ProjectWindow.class.getResource("/icons/white2.png")));
//                break;
//        }
//
//        setPreferredSize(new Dimension(950, 1000));
//
//        pack();
//        toFront();
//        setVisible(true);
//
//        clearDirs(new File(project.getProjectPath() + File.separator + "temp"));
//
//        ComboBoxSearchable sheetComboBoxSearchable= new ComboBoxSearchable(sheetChooserComboBox);
//        trainerPanel1.setProject(project);
//        trainerPanel1.setApi(api);
//
////        Application.getApplication().setDefaultMenuBar(menuBar);
//        pokemonSpritePanel.setProject(project);
//
//        initializeItemsTable();
//
//        switch (baseRom)
//        {
//            case Diamond:
//            case Pearl:
//            case Platinum:
//                tabbedPane1.remove(johtoEncounterPanel);
//                sinnohEncounterPanel.setProject(project,api);
//                break;
//
//            case HeartGold:
//            case SoulSilver:
//                tabbedPane1.remove(johtoEncounterPanel); // TODO get rid of this line once editor is done
//                tabbedPane1.remove(sinnohEncounterPanel);
//                break;
//        }
//
//        if (projectUpdate) {
//            Project.saveProject(project, xmlPath);
//            JOptionPane.showMessageDialog(this, "Project update complete!", project.getName() + " (PokEditor)", JOptionPane.INFORMATION_MESSAGE);
//        }
//
//        poppedEditors = new HashMap<>();
//    }
//
//    private void unpackProgramFilesDir(File resourceDir) throws IOException
//    {
//        InputStream in= ProjectWindow.class.getResourceAsStream("/Program Files.zip");
//        byte[] buffer= new byte[1024];
//        ZipInputStream zipIn= new ZipInputStream(in);
//        ZipEntry zipEntry= zipIn.getNextEntry();
//
//        while(zipEntry != null)
//        {
//            if(!zipEntry.getName().contains("__"))
//            {
//                File outputFile= newFile(resourceDir.getParentFile(),zipEntry);
//                if(zipEntry.isDirectory())
//                {
//                    if(!outputFile.isDirectory() && !outputFile.mkdirs())
//                    {
//                        throw new IOException("Failed to create directory " + outputFile);
//                    }
//                }
//                else
//                {
//                    // fix for Windows-created archives
//                    File parent = outputFile.getParentFile();
//                    if(!parent.isDirectory() && !parent.mkdirs())
//                    {
//                        throw new IOException("Failed to create directory " + parent);
//                    }
//
//                    // write file content
//                    if(!outputFile.getName().contains("._"))
//                    {
//                        System.out.println("Entry: " + zipEntry.getName());
//                        System.out.println("    " + outputFile.getName());
//                        FileOutputStream outputStream= new FileOutputStream(outputFile);
//                        int len;
//                        while ((len = zipIn.read(buffer)) > 0)
//                        {
//                            outputStream.write(buffer, 0, len);
//                        }
//                        outputStream.close();
//                    }
//                }
//            }
//            zipEntry= zipIn.getNextEntry();
//        }
//        zipIn.closeEntry();
//        zipIn.close();
//    }
//
//    private void sheetsSetupButtonActionPerformed(ActionEvent e)
//    {
//        switch(JOptionPane.showOptionDialog(this, "PokEditor is an open source tool developed by Turtleisaac. By choosing Online, I authorize PokEditor to gain full viewing and editing access to any Google Sheet document that I provide it the link for. By choosing Offline, I opt to instead use local sheet editing and storage, and accept that I may encounter issues using PokEditor, as it was written with Google Sheets integration in-mind. (Note: Online is currently broken, use offline).", "Confirmation", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Online", "Offline"}, "Offline"))
//        {
//            case 1: //no - offline - local
//                try
//                {
//                    GoogleSheetsAPI api = new GoogleSheetsAPI(null, projectPath, true);
//                    setApi(api);
//
//                    RomApplier editApplier = new RomApplier(project, projectPath, api, this, false, itemTableData, true);
//                    editApplier.getCheckboxTree().checkRoot();
//                    editApplier.applyButtonActionPerformed(null);
//                    editApplier.dispose();
//
//
//                    trainerPanel1.setApi(this.api);
//                    if(Project.isPlatinum(project))
//                    {
//                        sinnohEncounterPanel.setApi(this.api);
//                    }
//
//                    String[] sheetNames= new String[0];
//
//                    try
//                    {
//                        sheetNames= api.getSheetNames();
//                        setSheetChooserComboBox(sheetNames);
//                    }
//                    catch (IOException ignored)
//                    {
//                    }
//
//
//                    sheetChooserComboBox.setSelectedIndex(indexOf("Personal",sheetNames));
//                }
//                catch(GeneralSecurityException generalSecurityException) {
//                    generalSecurityException.printStackTrace();
//                }
//                catch(IOException exception) {
//                    exception.printStackTrace();
//                }
//
//                break;
//
//            case 0: //yes - online
//                SheetsSetup setup= new SheetsSetup(project.getBaseRom(), this);
//                try
//                {
//                    Thread.sleep(1);
//                }
//                catch(InterruptedException exception)
//                {
//                    exception.printStackTrace();
//                }
//
//                setEnabled(false);
//                setup.toFront();
//
//                break;
//        }
//
//
//
//    }
//
//    private void thisWindowClosing(WindowEvent e)
//    {
//        if(e != null)
//        {
//            switch(JOptionPane.showConfirmDialog(this,"Any local changes that have not been uploaded or applied to your ROM will be lost. Are you sure you want to exit?","Warning",JOptionPane.YES_NO_OPTION))
//            {
//                case 0: //yes
//                    dispose();
//
//                    clearAllDirs();
//
//                    String backupPath= project.getProjectPath().getAbsolutePath() + File.separator + "backups";
//                    File backupDir= new File(backupPath);
//                    String dirPath= projectPath + File.separator + project.getName();
//
//                    if(!backupDir.exists())
//                    {
//                        if(!backupDir.mkdir())
//                        {
//                            System.err.println("Path: " + backupPath);
//                            throw new RuntimeException("Unable to create directory. Check write perms");
//                        }
//                    }
//
//
//                    File[] backups= Objects.requireNonNull(backupDir.listFiles());
//
//                    if(backups.length >= 10)
//                    {
//                        if(JOptionPane.showConfirmDialog(this,"There are already 10 or more backup ROMs. Would you like to delete the oldest one?") == 0)
//                        {
//                            File earliestFile;
//                            if(!backups[0].getName().equals("original.nds"))
//                            {
//                                earliestFile= backups[0];
//                            }
//                            else
//                            {
//                                earliestFile= backups[1];
//                            }
//
//
//                            for(int i= 1; i < backups.length; i++)
//                            {
//                                try
//                                {
//                                    FileTime creationTime= (FileTime) Files.getAttribute(Paths.get(backups[i].getAbsolutePath()),"creationTime");
//                                    FileTime earliestCreationTime= (FileTime) Files.getAttribute(Paths.get(earliestFile.getAbsolutePath()),"creationTime");
//
//                                    if(creationTime.compareTo(earliestCreationTime) < 0 && !backups[i].getName().equals("original.nds"))
//                                    {
//                                        earliestFile= backups[i];
//                                    }
//                                }
//                                catch (IOException exception)
//                                {
//                                    exception.printStackTrace();
//                                }
//                            }
//
//
//                            if(!earliestFile.delete())
//                            {
//                                System.err.println("Unable to delete oldest backup");
//                            }
//                        }
//                    }
//
//                    String date= java.time.LocalDate.now().toString();
//                    String time= java.time.LocalTime.now().toString().replaceAll(":",".");
//                    System.out.println("Time: " + time);
//
//                    cleanupDirs(new File(dirPath));
//                    exportRom(dirPath,backupPath + File.separator + date + " " + time.substring(0,time.lastIndexOf(".")) + ".nds",false);
//
//
//                    boolean framesActive= false;
//                    for(Frame frame : Frame.getFrames())
//                    {
//                        if(frame.isVisible())
//                        {
//                            framesActive= true;
//                            break;
//                        }
//                    }
//
//
//
//                    if(framesActive)
//                    {
//                        switch(JOptionPane.showConfirmDialog(this,"Do you want to return to the main menu?","PokEditor",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE))
//                        {
//                            case 0: //yes
//                                mainMenu.setEnabled(true);
//                                mainMenu.setVisible(true);
//                                mainMenu.setLocationRelativeTo(this);
//                                break;
//
//                            case 1: //no
//                                setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//                                break;
//                        }
//                    }
//                    else
//                    {
//                        mainMenu.setEnabled(true);
//                        mainMenu.setVisible(true);
//                        mainMenu.setLocationRelativeTo(this);
//                    }
//                    break;
//
//                case 1: //no
//                    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//                    break;
//            }
//        }
//        else
//        {
//            dispose();
//            mainMenu.setEnabled(true);
//            mainMenu.setVisible(true);
//            mainMenu.setLocationRelativeTo(this);
//        }
//
//    }
//
//    private void applyToSheetButtonActionPerformed(ActionEvent e) {
//        if(JOptionPane.showConfirmDialog(this,"Any changes that you have made to the sheet that do not exist in the ROM will be lost. Continue?","Alert",JOptionPane.YES_NO_OPTION) == 0)
//        {
//            RomApplier editApplier= new RomApplier(project, projectPath, api, this, true, itemTableData, true);
//            editApplier.setLocationRelativeTo(this);
//            setEnabled(false);
//            try
//            {
//                Thread.sleep(100);
//            }
//            catch (InterruptedException | IllegalMonitorStateException exception)
//            {
//                exception.printStackTrace();
//            }
//
//            editApplier.toFront();
//        }
//    }
//
//    private void applyToRomButtonActionPerformed(ActionEvent e)
//    {
//        if(JOptionPane.showConfirmDialog(this,"Any changes that you have made to the ROM that do not exist in the sheets you apply will be lost. Continue?","Alert",JOptionPane.YES_NO_OPTION) == 0)
//        {
//            SheetApplier editApplier= new SheetApplier(project, projectPath, api, this, itemTableData);
//            editApplier.setLocationRelativeTo(this);
//            setEnabled(false);
//            editApplier.toFront();
//        }
//    }
//
//    private void setSheetChooserComboBox(String... arr)
//    {
//        for(String str : arr)
//        {
//            sheetChooserComboBox.addItem(str);
//        }
//    }
//
//    public void sheetChooserComboBoxActionPerformed(ActionEvent e)
//    {
//        if(!api.isLocal() || new File(projectPath + File.separator + "local").exists())
//        {
//            sheetName= (String) sheetChooserComboBox.getSelectedItem();
//            List<List<Object>> values;
//
//            try
//            {
//                values= api.getSpecifiedSheet(sheetName);
//            }
//            catch(IOException exception)
//            {
//                JOptionPane.showMessageDialog(this,"Download failed","Error",JOptionPane.ERROR_MESSAGE);
//                values= null;
//            }
//
//            try
//            {
//                sheetType= api.getPokeditorSheetType(sheetChooserComboBox.getSelectedIndex());
//            }
//            catch (IOException exception)
//            {
//                exception.printStackTrace();
//            }
//            catch (NullPointerException exception)
//            {
//                sheetType= null;
//            }
//
//            sheetData= values;
//
//
//            Object[] header;
//
//            if(sheetType != null || contains(baseRom.sheetList,sheetName))
//            {
//                header= values.remove(0).toArray(new Object[0]);
//            }
//            else
//            {
//                header= new Object[values.get(0).size()];
//            }
//
//
//            Object[][] table= new Object[values.size()][];
//
//
//            for(int i= 0; i < values.size(); i++)
//            {
//                table[i]= values.get(i).toArray(new Object[0]);
//            }
//            int numColumns= table[1].length;
//
//            DefaultTableModel model= new DefaultTableModel(table,header);
//            sheetPreviewTable.setModel(model);
//            sheetPreviewTable.setGridColor(Color.black);
//            sheetPreviewTable.setShowVerticalLines(true);
//            sheetPreviewTable.setShowHorizontalLines(true);
//            sheetPreviewTable.getTableHeader().setReorderingAllowed(false);
//
//            if(numColumns >= 9)
//                sheetPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//            else
//                sheetPreviewTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//
////        try
////        {
////            colorData= api.getSpecifiedSheetColors(sheetChooserComboBox.getSelectedIndex());
////        }
////        catch (IOException exception)
////        {
////            colorData= null;
////            exception.printStackTrace();
////        }
//        }
//    }
//
//    private void sheetRefreshChangesButtonActionPerformed(ActionEvent e)
//    {
//        switch(JOptionPane.showConfirmDialog(this,"Any changes you have made to this sheet will be lost when refreshing from the stored version. Continue?","Warning",JOptionPane.YES_NO_OPTION))
//        {
//            case 0: //yes
//                sheetChooserComboBoxActionPerformed(e);
//                break;
//
//            case 1: //no
//                break;
//        }
//    }
//
//    private void sheetUploadChangesButtonActionPerformed(ActionEvent e)
//    {
//        if(!api.isLocal())
//        {
//            switch(JOptionPane.showConfirmDialog(this,"Any changes you have made to this sheet online will be lost when uploading the current local version. Continue?","Warning",JOptionPane.YES_NO_OPTION))
//            {
//                case 0: //yes
//                    try
//                    {
//                        api.updateSheet((String) sheetChooserComboBox.getSelectedItem(),getTableData());
//                    }
//                    catch (IOException exception)
//                    {
//                        JOptionPane.showMessageDialog(this,"Upload failed","Error",JOptionPane.ERROR_MESSAGE);
//                    }
//
//                    break;
//
//                case 1: //no
//                    break;
//            }
//        }
//        else
//        {
//            JOptionPane.showMessageDialog(this, "Changes have been saved.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
//        }
//    }
//
//    private void consoleItemActionPerformed(ActionEvent e)
//    {
//        console.setLocationRelativeTo(this);
//        console.setVisible(true);
//    }
//
//    private void narctowlItemActionPerformed(ActionEvent e)
//    {
//        PokEditor pokeditor= new PokEditor();
//        pokeditor.setVisible(false);
//
//        JFrame frame= new JFrame();
//        frame.setPreferredSize(new Dimension(500,250));
//        frame.setTitle("Narctowl");
//        frame.setLocationRelativeTo(this);
//        frame.setContentPane(pokeditor.getNarctowlPanel());
//        frame.setVisible(true);
//        frame.pack();
//    }
//
//    private void blzCoderItemActionPerformed(ActionEvent e)
//    {
//        PokEditor pokeditor= new PokEditor();
//        pokeditor.setVisible(false);
//
//        JFrame frame= new JFrame();
//        frame.setPreferredSize(new Dimension(500,150));
//        frame.setTitle("BLZ Coder");
//        frame.setLocationRelativeTo(this);
//        frame.setContentPane(pokeditor.getCompressionPanel());
//        frame.setVisible(true);
//        frame.pack();
//    }
//
//    private void linkTextFieldMouseClicked(MouseEvent e)
//    {
//        if(!api.isLocal())
//        {
//            try
//            {
//                Desktop.getDesktop().browse(new URI(linkTextField.getText()));
//            } catch (URISyntaxException | IOException exception)
//            {
//                exception.printStackTrace();
//            }
//        }
//    }
//
//    private void exportRomButtonActionPerformed(ActionEvent e)
//    {
////        if(e.getSource() == exportRomButton)
////            JOptionPane.showMessageDialog(this,"Make sure that you have applied the data that you want applied to your ROM with the \"Apply to ROM\" button.","Warning",JOptionPane.WARNING_MESSAGE);
//
//        JFileChooser fc= new JFileChooser(project.getProjectPath());
//        fc.addChoosableFileFilter(new MyFilter("Nintendo DS ROM",".nds"));
//        fc.setAcceptAllFileFilterUsed(false);
//
//        clearAllDirs();
//
////        fc.setDialogTitle("Export ROM");
//        int returnVal = fc.showOpenDialog(this);
//
//        if (returnVal == JFileChooser.APPROVE_OPTION)
//        {
//            String dirPath= projectPath + File.separator + project.getName();
//            cleanupDirs(new File(dirPath));
//            exportRom(dirPath,fc.getSelectedFile().toString(),true);
//        }
//    }
//
//    private void exportRom(String dirPath, String outputPath, boolean display)
//    {
//        if(!outputPath.endsWith(".nds"))
//        {
//            outputPath+= ".nds";
//        }
//
//        try
//        {
//            JNdstool.main("-b",dirPath,outputPath);
//        }
//        catch (IOException e)
//        {
//            System.err.println("ROM Output Error:");
//            e.printStackTrace();
//            return;
//        }
//
//        if(display)
//            JOptionPane.showMessageDialog(this,"Success!","PokEditor",JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    private void openProjectButtonActionPerformed(ActionEvent e)
//    {
//        JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//        fc.setDialogTitle("Open Project");
//        fc.setAcceptAllFileFilterUsed(false);
//
//        fc.addChoosableFileFilter(new MyFilter("PokEditor Projects",".pokeditor"));
//        fc.addChoosableFileFilter(new MyFilter("DS Pokemon Rom Editor Projects",".dspre"));
//
//        if (e.getSource() == openProjectButton) {
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                try
//                {
//                    ProjectWindow projectWindow= new ProjectWindow(fc.getSelectedFile().getAbsolutePath(), mainMenu,console);
//                    projectWindow.setLocationRelativeTo(this);
//                }
//                catch (IOException ignored)
//                {
//                }
//
//            }
//        }
//    }
//
//    private void baseRomButtonActionPerformed(ActionEvent e)
//    {
//        long arm9Size = new File(new File(project.getDataPath()).getParentFile().getAbsolutePath() + File.separator + "arm9.bin").length();
//        String message = "Game Code: " + project.getBaseRomGameCode() + "\n" +
//                "Game Title: " + Project.parseBaseRom(project.getBaseRomGameCode()) + "\n" +
//                "Language: " + project.getLanguage() + "\n" +
//                "Arm9 Size: " + arm9Size + " (0x" + Long.toHexString(arm9Size) + ") bytes\n" +
//                "Support Level: Full\n" +
//                "Generation: 4\n" +
//                "Filesystem Type: " + (Project.isHGSS(project) ? "Numerical" : "Lexicographical");
//        JOptionPane.showMessageDialog(this,message,"Base ROM Info",JOptionPane.INFORMATION_MESSAGE, baseRomButton.getIcon());
//    }
//
//    private void projectInfoButtonActionPerformed(ActionEvent e)
//    {
//        String message = "Project Name: " + project.getName() + "\n" +
//                "Project Path: " + project.getProjectPath() + "\n" +
//                "Data Path: " + project.getDataPath() + "\n" +
//                "Origin Program: " + project.getProgram() + "\n" +
//                "Primary Sprite Mode: " + (Project.isPrimary(project) ? "Enabled" : "Disabled") + "\n" +
//                "ROM Mode: " + Project.parseBaseRom(project.getBaseRomGameCode());
//        Icon icon = new ImageIcon(ProjectWindow.class.getResource("/icons/give_pokedex.png"));
//        JOptionPane.showMessageDialog(this,message,"Project Info",JOptionPane.INFORMATION_MESSAGE, icon);
//    }
//
//    private void exportRomItemActionPerformed(ActionEvent e) {
//        exportRomButtonActionPerformed(e);
//    }
//
//    private void openProjectItemActionPerformed(ActionEvent e) {
//        openProjectButtonActionPerformed(e);
//    }
//
//    private void aboutItemActionPerformed(ActionEvent e)
//    {
//        // TODO add your code here
//        JOptionPane.showMessageDialog(this,"Not implemented yet.","Error",JOptionPane.ERROR_MESSAGE);
//    }
//
//    private void tutorialItemActionPerformed(ActionEvent e) {
//        // TODO add your code here
//        tutorial.setVisible(true);
//    }
//
//    private void randomizerItemActionPerformed(ActionEvent e)
//    {
//        JFrame randomFrame= new JFrame("Close Window to Apply");
//        DefaultMutableTreeNode features= new DefaultMutableTreeNode("Features");
//        features.add(new DefaultMutableTreeNode("Randomize evolutions"));
//        features.add(new DefaultMutableTreeNode("Randomized evolutions every level"));
//        features.add(new DefaultMutableTreeNode("Randomize encounters"));
//        features.add(new DefaultMutableTreeNode("Randomize trainer teams"));
//        JCheckboxTree checkboxTree= new JCheckboxTree(features);
//        JScrollPane scrollPane= new JScrollPane(checkboxTree);
//        randomFrame.setContentPane(scrollPane);
//        randomFrame.setLocationRelativeTo(this);
//        randomFrame.setPreferredSize(new Dimension(400,500));
//        randomFrame.pack();
//        randomFrame.setVisible(true);
//        randomFrame.toFront();
//
//        ProjectWindow projectWindow= this;
//
//        randomFrame.addWindowListener(new WindowAdapter()
//        {
//            @Override
//            public void windowClosing(WindowEvent w)
//            {
//                randomFrame.dispose();
//
//                if(JOptionPane.showConfirmDialog(projectWindow,"Apply randomization?","Randomizer",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE) == 0)
//                {
//                    String selectedString= Arrays.toString(checkboxTree.getCheckedPaths());
//                    selectedString= selectedString.substring(0, selectedString.length()-1);
//
//                    String[] selected= selectedString.split(", ");
//                    System.out.println(Arrays.toString(selected));
//
//                    RandomizerUtils random;
//                    boolean success;
//
//                    if(selectedContains(selected,"Features"))
//                    {
//                        System.out.println("Randomizing");
//                        random= new RandomizerUtils(project);
//                        success= true;
//                    }
//                    else
//                    {
//                        JOptionPane.showMessageDialog(projectWindow,"No randomization features were selected","Randomizer",JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//
//
//                    if(selectedContains(selected,"Randomize evolutions")) //evolutions
//                    {
//                        try
//                        {
//                            JOptionPane.showMessageDialog(projectWindow,"Pokémon with trade evolutions will now evolve at level 38\nPokémon that evolve when traded when holding an item now evolve from leveling up while holding that item");
//                            random.randomizeEvolutions();
//                        }
//                        catch (IOException exception)
//                        {
//                            System.err.println("Evolution Randomization Error:");
//                            exception.printStackTrace();
//                            success= false;
//                        }
//                    }
//
//                    if(selectedContains(selected,"Randomized evolutions every level")) //evolutions every level
//                    {
//                        try
//                        {
//                            random.randomizeEvolutionsEveryLevel();
//                        }
//                        catch (IOException exception)
//                        {
//                            System.err.println("Evolution Randomization Error:");
//                            exception.printStackTrace();
//                            success= false;
//                        }
//                    }
//
//                    if(selectedContains(selected,"Randomize encounters")) //encounters
//                    {
//                        try
//                        {
//                            random.randomizeEncounters();
//                        }
//                        catch (IOException exception)
//                        {
//                            System.err.println("Encounter Randomization Error:");
//                            exception.printStackTrace();
//                            success= false;
//                        }
//                    }
//
//                    if(selectedContains(selected,"Randomize trainer teams")) //trainer teams
//                    {
//                        try
//                        {
//                            random.randomizeTrainerPokemon();
//                        }
//                        catch (IOException exception)
//                        {
//                            System.err.println("Trainer Randomization Error:");
//                            exception.printStackTrace();
//                            success= false;
//                        }
//                    }
//
//
//                    if(success)
//                    {
//                        exportRomButtonActionPerformed(e);
//                    }
//                    else
//                    {
//                        System.err.println("Randomization Error");
//                    }
//                }
//            }
//        });
//    }
//
//    private void fileRandomizerItemActionPerformed(ActionEvent e)
//    {
//        // TODO add your code here
//        JOptionPane.showMessageDialog(this,"Not implemented yet.","Error",JOptionPane.ERROR_MESSAGE);
//    }
//
//    private void importRomItemActionPerformed(ActionEvent e)
//    {
//
//        if(JOptionPane.showConfirmDialog(this,"Proceeding will wipe all data in the currently used ROM that is not contained on the PokEditor sheets. This includes sprites, scripts, asm, or any misc. hex edits.\n\nAre you sure that you want to continue?","Warning",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE) == 0)
//        {
//            String defaultDir;
//
//            if(new File(projectPath + File.separator + "backups").exists())
//                defaultDir= projectPath + File.separator + "backups";
//            else
//                defaultDir= System.getProperty("user.dir");
//
//            JFileChooser fc= new JFileChooser(defaultDir);
//            fc.addChoosableFileFilter(new MyFilter("Nintendo DS ROM",".nds"));
//            fc.setAcceptAllFileFilterUsed(true);
//
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION)
//            {
//                String gameCode = "";
//
//                try
//                {
//                    gameCode= ROMUtils.getROMCode(fc.getSelectedFile().getAbsolutePath());
//                }
//                catch (IOException exception)
//                {
//                    exception.printStackTrace();
//                    JOptionPane.showMessageDialog(this,"An unexpected error occurred while checking the ROM data","Error",JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                if(Project.parseBaseRom(gameCode) != baseRom)
//                {
//                    JOptionPane.showMessageDialog(this,"The selected ROM is not the same base as the ROM used for this project.","Error",JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                String dirPath= projectPath + File.separator + project.getName();
//                clearDirs(new File(dirPath));
//
//                try
//                {
//                    JNdstool.main("-x", fc.getSelectedFile().getAbsolutePath(),dirPath);
//                }
//                catch (IOException exception)
//                {
//                    exception.printStackTrace();
//                    JOptionPane.showMessageDialog(this,"An unexpected error occurred while unpacking this ROM","Error",JOptionPane.ERROR_MESSAGE);
//                    return;
//                }
//
//                String backupPath= project.getProjectPath().getAbsolutePath() + File.separator + "backups";
//                File backupDir= new File(backupPath);
//
//                if(!backupDir.exists())
//                {
//                    if(!backupDir.mkdir())
//                    {
//                        System.err.println("Path: " + backupPath);
//                        throw new RuntimeException("Unable to create directory. Check write perms");
//                    }
//                }
//
//                try
//                {
//                    Files.copy(Paths.get(fc.getSelectedFile().getAbsolutePath()),Paths.get(backupPath + File.separator + "original.nds"));
//                }
//                catch (IOException exception)
//                {
//                    exception.printStackTrace();
//                }
//
//                JOptionPane.showMessageDialog(this,"The project will now close. After reopening it, the ROM re-base will be complete.","PokEditor",JOptionPane.INFORMATION_MESSAGE);
//                thisWindowClosing(null);
//            }
//        }
//
//
//    }
//
//    private void createUIComponents() {
//        // TODO: add custom component creation code here
//    }
//
//    private void integrationSetupItemActionPerformed(ActionEvent e)
//    {
//        sheetsSetupButtonActionPerformed(e);
//    }
//
//    private void migrationItemActionPerformed(ActionEvent e)
//    {
//        // TODO add your code here
//        int choice= JOptionPane.showConfirmDialog(this,"This utility will transfer all of your existing data stored on Google Sheets to a new set of sheets. Your existing set of sheets will remain, and PokEditor v2 will automatically link itself with the new sheets. Continue?","Sheet Migration Utility",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
//
//        if(choice == 0)
//        {
//
//        }
//    }
//
//    private void applyToSheetItemActionPerformed(ActionEvent e) {
//        applyToSheetButtonActionPerformed(e);
//    }
//
//    private void applyToRomItemActionPerformed(ActionEvent e) {
//        applyToRomButtonActionPerformed(e);
//    }
//
//    private void sheetRefreshChangesItemActionPerformed(ActionEvent e) {
//        sheetRefreshChangesButtonActionPerformed(e);
//    }
//
//    private void sheetUploadChangesItemActionPerformed(ActionEvent e) {
//        sheetUploadChangesButtonActionPerformed(e);
//    }
//
//    private void dspreCompatibilityItem(ActionEvent e) {
//        JOptionPane.showMessageDialog(this, "In order to use a PokEditor project in DSPRE, go into your project directory and into the folder within it that has a name matching the project. In this folder is the unpacked ROM data:\n1. Rename \"arm9ovltable.bin\" to \"y9.bin\".\n2. Rename \"arm7ovltable.bin\" to \"y7.bin\".\n3. Add a new directory called \"unpacked\".\nNow you can open the unpacked ROM folder within the PokEditor project in DSPRE.\nFor changing back to PokEditor, just do the inverse.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
//    }
//
//    private void popEditors(EditorTypes editorType)
//    {
////        System.out.println("Popping");
//        PoppedEditorFrame newFrame = new PoppedEditorFrame(this, editorType);
//        String title;
//        switch(editorType)
//        {
//            case TRAINER:
//                tabbedPane1.remove(trainerPanel1);
//                newFrame.setContentPane(trainerPanel1);
//                editorMenu.remove(popTrainersMenuItem);
//                title = "Trainer Editor";
//                break;
//            case ENCOUNTER:
//                if (Project.isPlatinum(project))
//                {
//                    tabbedPane1.remove(sinnohEncounterPanel);
//                    newFrame.setContentPane(sinnohEncounterPanel);
//                    newFrame.setPreferredSize(new Dimension(884,920));
//                }
//                else if (Project.isHGSS(project))
//                {
//                    //TODO uncomment this once the HGSS encounter editor is done
//                    tabbedPane1.remove(johtoEncounterPanel);
//                    newFrame.setContentPane(johtoEncounterPanel);
//                }
//                editorMenu.remove(popEncountersMenuItem);
//                title = "Encounter Editor";
//                break;
//            case POKEMON_SPRITE:
//                tabbedPane1.remove(pokemonSpritePanel);
//                newFrame.setContentPane(pokemonSpritePanel);
//                editorMenu.remove(popPokemonSpritesMenuItem);
//                title = "Pokémon Sprite Editor";
//                break;
//
//            default:
//                title = "";
//        }
//        newFrame.setTitle(project.getName() + " (PokEditor) - " + title);
//        newFrame.pack();
//        newFrame.setVisible(true);
//        System.out.println(newFrame.getWidth() + ", " + newFrame.getHeight());
//    }
//
//    public void unpopEditors(EditorTypes editorType)
//    {
////        System.out.println("Unpopping");
//        String title;
//        switch(editorType)
//        {
//            case TRAINER:
//                title = "Trainer Editor";
//                tabbedPane1.addTab(title, trainerPanel1);
//                editorMenu.add(popTrainersMenuItem);
//                break;
//            case ENCOUNTER:
//                title = "Encounter Editor";
//                if (Project.isPlatinum(project))
//                {
//                    tabbedPane1.addTab(title, sinnohEncounterPanel);
//                }
//                else if (Project.isHGSS(project))
//                {
//                    //TODO uncomment this once the HGSS encounter editor is done
//                    tabbedPane1.addTab(title, johtoEncounterPanel);
//                }
//                editorMenu.add(popEncountersMenuItem);
//                break;
//            case POKEMON_SPRITE:
//                title = "Pokémon Sprite Editor";
//                tabbedPane1.addTab(title, pokemonSpritePanel);
//                editorMenu.add(popPokemonSpritesMenuItem);
//                break;
//
//            default:
//        }
//    }
//
//    private void popTrainersMenuItem(ActionEvent e) {
//        popEditors(EditorTypes.TRAINER);
//    }
//
//    private void popEncountersMenuItem(ActionEvent e) {
//        popEditors(EditorTypes.ENCOUNTER);
//    }
//
//    private void popPokemonSpritesMenuItem(ActionEvent e) {
//        popEditors(EditorTypes.POKEMON_SPRITE);
//    }
//
//    enum EditorTypes {
//        TRAINER,
//        ENCOUNTER,
//        POKEMON_SPRITE
//    }
//
//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        menuBar = new JMenuBar();
//        fileMenu = new JMenu();
//        openProjectItem = new JMenuItem();
//        openRecentItem = new JMenuItem();
//        importRomItem = new JMenuItem();
//        exportRomItem = new JMenuItem();
//        sheetsMenu = new JMenu();
//        integrationSetupItem = new JMenuItem();
//        migrationItem = new JMenuItem();
//        applyToSheetItem = new JMenuItem();
//        applyToRomItem = new JMenuItem();
//        sheetRefreshChangesItem = new JMenuItem();
//        sheetUploadChangesItem = new JMenuItem();
//        editorMenu = new JMenu();
//        popTrainersMenuItem = new JMenuItem();
//        popEncountersMenuItem = new JMenuItem();
//        popPokemonSpritesMenuItem = new JMenuItem();
//        toolsMenu = new JMenu();
//        randomizerItem = new JMenuItem();
//        narctowlItem = new JMenuItem();
//        blzCoderItem = new JMenuItem();
//        fileRandomizerItem = new JMenuItem();
//        debugMenu = new JMenu();
//        consoleItem = new JMenuItem();
//        helpMenu = new JMenu();
//        aboutItem = new JMenuItem();
//        tutorialItem = new JMenuItem();
//        dspreCompatibilityItem = new JMenuItem();
//        separator2 = new JSeparator();
//        tabbedPane1 = new JTabbedPane();
//        mainPanel = new JPanel();
//        sheetsSetupButton = new JButton();
//        linkTextField = new JLabel();
//        sheetChooserComboBox = new JComboBox();
//        applyToSheetButton = new JButton();
//        applyToRomButton = new JButton();
//        sheetPreviewScrollPane = new JScrollPane();
//        sheetPreviewTable = new JTable();
//        sheetRefreshChangesButton = new JButton();
//        sheetUploadChangesButton = new JButton();
//        warningLabel = new JLabel();
//        trainerPanel1 = new TrainerPanel(this);
//        sinnohEncounterPanel = new SinnohEncounterPanel();
//        johtoEncounterPanel = new JohtoEncounterPanel();
//        pokemonSpritePanel = new PokemonSpritePanel();
//        starterPanel = new JPanel();
//        label1 = new JLabel();
//        openingPanel = new JPanel();
//        label3 = new JLabel();
//        trainerSpritePanel = new TrainerSpritePanel();
//        overworldSpritePanel = new JPanel();
//        jtbMain = new JToolBar();
//        openProjectButton = new JButton();
//        exportRomButton = new JButton();
//        toolBarHorizontalSeparator = new JPanel(null);
//        baseRomButton = new JButton();
//        projectInfoButton = new JButton();
//
//        //======== this ========
//        setTitle("PokEditor");
//        setMinimumSize(new Dimension(600, 500));
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
//            "[853,grow,fill]",
//            // rows
//            "[]" +
//            "[63,grow,top]"));
//
//        //======== menuBar ========
//        {
//
//            //======== fileMenu ========
//            {
//                fileMenu.setText("File");
//
//                //---- openProjectItem ----
//                openProjectItem.setText("Open Project");
//                openProjectItem.addActionListener(e -> openProjectItemActionPerformed(e));
//                fileMenu.add(openProjectItem);
//
//                //---- openRecentItem ----
//                openRecentItem.setText("Open Recent");
//                fileMenu.add(openRecentItem);
//                fileMenu.addSeparator();
//
//                //---- importRomItem ----
//                importRomItem.setText("Import ROM");
//                importRomItem.setToolTipText("Re-bases this project on a different ROM of the same game");
//                importRomItem.addActionListener(e -> importRomItemActionPerformed(e));
//                fileMenu.add(importRomItem);
//
//                //---- exportRomItem ----
//                exportRomItem.setText("Export ROM");
//                exportRomItem.addActionListener(e -> exportRomItemActionPerformed(e));
//                fileMenu.add(exportRomItem);
//            }
//            menuBar.add(fileMenu);
//
//            //======== sheetsMenu ========
//            {
//                sheetsMenu.setText("Sheets");
//
//                //---- integrationSetupItem ----
//                integrationSetupItem.setText("Integration Setup");
//                integrationSetupItem.addActionListener(e -> integrationSetupItemActionPerformed(e));
//                sheetsMenu.add(integrationSetupItem);
//
//                //---- migrationItem ----
//                migrationItem.setText("Migration Utility");
//                migrationItem.addActionListener(e -> migrationItemActionPerformed(e));
//                sheetsMenu.add(migrationItem);
//
//                //---- applyToSheetItem ----
//                applyToSheetItem.setText("Apply ROM to Sheets");
//                applyToSheetItem.addActionListener(e -> applyToSheetItemActionPerformed(e));
//                sheetsMenu.add(applyToSheetItem);
//
//                //---- applyToRomItem ----
//                applyToRomItem.setText("Apply Sheets to ROM");
//                applyToRomItem.addActionListener(e -> applyToRomItemActionPerformed(e));
//                sheetsMenu.add(applyToRomItem);
//
//                //---- sheetRefreshChangesItem ----
//                sheetRefreshChangesItem.setText("Refresh Sheet from Google Sheets");
//                sheetRefreshChangesItem.addActionListener(e -> sheetRefreshChangesItemActionPerformed(e));
//                sheetsMenu.add(sheetRefreshChangesItem);
//
//                //---- sheetUploadChangesItem ----
//                sheetUploadChangesItem.setText("Upload Changes to Google Sheets");
//                sheetUploadChangesItem.addActionListener(e -> sheetUploadChangesItemActionPerformed(e));
//                sheetsMenu.add(sheetUploadChangesItem);
//            }
//            menuBar.add(sheetsMenu);
//
//            //======== editorMenu ========
//            {
//                editorMenu.setText("Editor Pop");
//
//                //---- popTrainersMenuItem ----
//                popTrainersMenuItem.setText("Pop Trainer Editor");
//                popTrainersMenuItem.addActionListener(e -> popTrainersMenuItem(e));
//                editorMenu.add(popTrainersMenuItem);
//
//                //---- popEncountersMenuItem ----
//                popEncountersMenuItem.setText("Pop Encounter Editor");
//                popEncountersMenuItem.addActionListener(e -> popEncountersMenuItem(e));
//                editorMenu.add(popEncountersMenuItem);
//
//                //---- popPokemonSpritesMenuItem ----
//                popPokemonSpritesMenuItem.setText("Pop Pok\u00e9mon Sprite Editor");
//                popPokemonSpritesMenuItem.addActionListener(e -> popPokemonSpritesMenuItem(e));
//                editorMenu.add(popPokemonSpritesMenuItem);
//            }
//            menuBar.add(editorMenu);
//
//            //======== toolsMenu ========
//            {
//                toolsMenu.setText("Tools");
//
//                //---- randomizerItem ----
//                randomizerItem.setText("Randomizer");
//                randomizerItem.addActionListener(e -> randomizerItemActionPerformed(e));
//                toolsMenu.add(randomizerItem);
//
//                //---- narctowlItem ----
//                narctowlItem.setText("Narctowl");
//                narctowlItem.addActionListener(e -> narctowlItemActionPerformed(e));
//                toolsMenu.add(narctowlItem);
//
//                //---- blzCoderItem ----
//                blzCoderItem.setText("BLZ Coder");
//                blzCoderItem.addActionListener(e -> blzCoderItemActionPerformed(e));
//                toolsMenu.add(blzCoderItem);
//
//                //---- fileRandomizerItem ----
//                fileRandomizerItem.setText("File Randomizer");
//                fileRandomizerItem.addActionListener(e -> fileRandomizerItemActionPerformed(e));
//                toolsMenu.add(fileRandomizerItem);
//            }
//            menuBar.add(toolsMenu);
//
//            //======== debugMenu ========
//            {
//                debugMenu.setText("Debug");
//
//                //---- consoleItem ----
//                consoleItem.setText("Console");
//                consoleItem.addActionListener(e -> consoleItemActionPerformed(e));
//                debugMenu.add(consoleItem);
//            }
//            menuBar.add(debugMenu);
//
//            //======== helpMenu ========
//            {
//                helpMenu.setText("Help");
//
//                //---- aboutItem ----
//                aboutItem.setText("About");
//                aboutItem.addActionListener(e -> aboutItemActionPerformed(e));
//                helpMenu.add(aboutItem);
//
//                //---- tutorialItem ----
//                tutorialItem.setText("Tutorial");
//                tutorialItem.addActionListener(e -> tutorialItemActionPerformed(e));
//                helpMenu.add(tutorialItem);
//                helpMenu.addSeparator();
//
//                //---- dspreCompatibilityItem ----
//                dspreCompatibilityItem.setText("DSPRE Compatibility");
//                dspreCompatibilityItem.addActionListener(e -> dspreCompatibilityItem(e));
//                helpMenu.add(dspreCompatibilityItem);
//            }
//            menuBar.add(helpMenu);
//        }
//        setJMenuBar(menuBar);
//
//        //---- separator2 ----
//        separator2.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
//        contentPane.add(separator2, "cell 0 0,grow");
//
//        //======== tabbedPane1 ========
//        {
//
//            //======== mainPanel ========
//            {
//                mainPanel.setLayout(new MigLayout(
//                    "hidemode 3",
//                    // columns
//                    "[grow,fill]" +
//                    "[grow,fill]",
//                    // rows
//                    "[54,fill]" +
//                    "[fill]" +
//                    "[fill]" +
//                    "[fill]" +
//                    "[grow,fill]" +
//                    "[fill]" +
//                    "[bottom]"));
//
//                //---- sheetsSetupButton ----
//                sheetsSetupButton.setText("Sheets Setup");
//                sheetsSetupButton.setToolTipText("Click this to configure a link with the Google Sheets API or a local sheet repository.");
//                sheetsSetupButton.addActionListener(e -> sheetsSetupButtonActionPerformed(e));
//                mainPanel.add(sheetsSetupButton, "cell 0 0 2 1,grow");
//
//                //---- linkTextField ----
//                linkTextField.setEnabled(false);
//                linkTextField.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        linkTextFieldMouseClicked(e);
//                    }
//                });
//                mainPanel.add(linkTextField, "cell 0 1 2 1,alignx center,growx 0");
//
//                //---- sheetChooserComboBox ----
//                sheetChooserComboBox.setEnabled(false);
//                sheetChooserComboBox.addActionListener(e -> sheetChooserComboBoxActionPerformed(e));
//                mainPanel.add(sheetChooserComboBox, "cell 0 2 2 1");
//
//                //---- applyToSheetButton ----
//                applyToSheetButton.setText("Apply ROM to Sheets");
//                applyToSheetButton.setEnabled(false);
//                applyToSheetButton.setToolTipText("Data from game files is copied into sheets.");
//                applyToSheetButton.addActionListener(e -> applyToSheetButtonActionPerformed(e));
//                mainPanel.add(applyToSheetButton, "cell 0 3");
//
//                //---- applyToRomButton ----
//                applyToRomButton.setText("Apply Sheets to ROM");
//                applyToRomButton.setEnabled(false);
//                applyToRomButton.setToolTipText("Data from sheets is applied to game files.");
//                applyToRomButton.addActionListener(e -> applyToRomButtonActionPerformed(e));
//                mainPanel.add(applyToRomButton, "cell 1 3");
//
//                //======== sheetPreviewScrollPane ========
//                {
//                    sheetPreviewScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
//                    sheetPreviewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//
//                    //---- sheetPreviewTable ----
//                    sheetPreviewTable.setEnabled(false);
//                    sheetPreviewTable.setGridColor(Color.black);
//                    sheetPreviewTable.setMaximumSize(new Dimension(2147483647, 2147483647));
//                    sheetPreviewTable.setRowSelectionAllowed(false);
//                    sheetPreviewTable.setCellSelectionEnabled(true);
//                    //sheetPreviewTable.setDefaultRenderer(Object.class, new PaintTableCellRenderer());
//                    sheetPreviewTable.setIntercellSpacing(new Dimension(0,0));
//                    sheetPreviewTable.setRowMargin(0);
//                    sheetPreviewTable.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
//                    sheetPreviewScrollPane.setViewportView(sheetPreviewTable);
//                }
//                mainPanel.add(sheetPreviewScrollPane, "cell 0 4 2 1,grow");
//
//                //---- sheetRefreshChangesButton ----
//                sheetRefreshChangesButton.setText("Reload Sheets");
//                sheetRefreshChangesButton.setEnabled(false);
//                sheetRefreshChangesButton.setToolTipText("Reloads sheets with the last saved data.");
//                sheetRefreshChangesButton.addActionListener(e -> sheetRefreshChangesButtonActionPerformed(e));
//                mainPanel.add(sheetRefreshChangesButton, "cell 0 5,growx");
//
//                //---- sheetUploadChangesButton ----
//                sheetUploadChangesButton.setText("Save Sheet Changes");
//                sheetUploadChangesButton.setEnabled(false);
//                sheetUploadChangesButton.setToolTipText("Saves local changes made in the preview window");
//                sheetUploadChangesButton.addActionListener(e -> sheetUploadChangesButtonActionPerformed(e));
//                mainPanel.add(sheetUploadChangesButton, "cell 1 5,growx");
//
//                //---- warningLabel ----
//                warningLabel.setText("No warnings to display");
//                mainPanel.add(warningLabel, "cell 0 6 2 1,alignx center,growx 0");
//            }
//            tabbedPane1.addTab("Main", mainPanel);
//            tabbedPane1.addTab("Trainer Editor", trainerPanel1);
//            tabbedPane1.addTab("Encounter Editor", sinnohEncounterPanel);
//            tabbedPane1.addTab("Encounter Editor", johtoEncounterPanel);
//            tabbedPane1.addTab("Pok\u00e9mon Sprites", pokemonSpritePanel);
//
//            //======== starterPanel ========
//            {
//                starterPanel.setLayout(new MigLayout(
//                    "hidemode 3",
//                    // columns
//                    "[fill]" +
//                    "[fill]",
//                    // rows
//                    "[]" +
//                    "[]" +
//                    "[]"));
//
//                //---- label1 ----
//                label1.setText("You were expecting a starter editor, but it was me, Dio!");
//                starterPanel.add(label1, "cell 0 0");
//            }
//            tabbedPane1.addTab("Starters", starterPanel);
//            tabbedPane1.setEnabledAt(5, false);
//
//            //======== openingPanel ========
//            {
//                openingPanel.setLayout(new MigLayout(
//                    "hidemode 3",
//                    // columns
//                    "[fill]" +
//                    "[fill]",
//                    // rows
//                    "[]" +
//                    "[]" +
//                    "[]"));
//
//                //---- label3 ----
//                label3.setText("You were expecting an opening cutscene editor, but it was me, Dio!");
//                openingPanel.add(label3, "cell 0 0");
//            }
//            tabbedPane1.addTab("Cutscenes", openingPanel);
//            tabbedPane1.setEnabledAt(6, false);
//            tabbedPane1.addTab("Trainer Sprites", trainerSpritePanel);
//            tabbedPane1.setEnabledAt(7, false);
//
//            //======== overworldSpritePanel ========
//            {
//                overworldSpritePanel.setLayout(new MigLayout(
//                    "hidemode 3",
//                    // columns
//                    "[fill]" +
//                    "[fill]",
//                    // rows
//                    "[]" +
//                    "[]" +
//                    "[]"));
//            }
//            tabbedPane1.addTab("Overworld Sprites", overworldSpritePanel);
//            tabbedPane1.setEnabledAt(8, false);
//        }
//        contentPane.add(tabbedPane1, "cell 0 1");
//
//        //======== jtbMain ========
//        {
//            jtbMain.setBorderPainted(false);
//            jtbMain.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
//            jtbMain.setFloatable(false);
//
//            //---- openProjectButton ----
//            openProjectButton.setMaximumSize(new Dimension(40, 40));
//            openProjectButton.setMinimumSize(new Dimension(40, 40));
//            openProjectButton.setPreferredSize(new Dimension(40, 40));
//            openProjectButton.setIcon(UIManager.getIcon("Tree.openIcon"));
//            openProjectButton.setToolTipText("Open Project");
//            openProjectButton.addActionListener(e -> openProjectButtonActionPerformed(e));
//            jtbMain.add(openProjectButton);
//            jtbMain.addSeparator();
//
//            //---- exportRomButton ----
//            exportRomButton.setPreferredSize(new Dimension(40, 40));
//            exportRomButton.setMaximumSize(new Dimension(40, 40));
//            exportRomButton.setMinimumSize(new Dimension(40, 40));
//            exportRomButton.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
//            exportRomButton.setToolTipText("Export ROM");
//            exportRomButton.addActionListener(e -> exportRomButtonActionPerformed(e));
//            jtbMain.add(exportRomButton);
//            jtbMain.addSeparator();
//            jtbMain.add(toolBarHorizontalSeparator);
//            jtbMain.addSeparator();
//
//            //---- baseRomButton ----
//            baseRomButton.setMaximumSize(new Dimension(110, 40));
//            baseRomButton.setMinimumSize(new Dimension(110, 40));
//            baseRomButton.setPreferredSize(new Dimension(110, 40));
//            baseRomButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
//            baseRomButton.setText("ROM Info");
//            baseRomButton.setToolTipText("ROM Info");
//            baseRomButton.addActionListener(e -> baseRomButtonActionPerformed(e));
//            jtbMain.add(baseRomButton);
//            jtbMain.addSeparator();
//
//            //---- projectInfoButton ----
//            projectInfoButton.setText("Project Info");
//            projectInfoButton.setMaximumSize(new Dimension(110, 40));
//            projectInfoButton.setMinimumSize(new Dimension(110, 40));
//            projectInfoButton.setPreferredSize(new Dimension(110, 40));
//            projectInfoButton.setToolTipText("Project Info");
//            projectInfoButton.addActionListener(e -> projectInfoButtonActionPerformed(e));
//            jtbMain.add(projectInfoButton);
//        }
//        contentPane.add(jtbMain, "north");
//        pack();
//        setLocationRelativeTo(getOwner());
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }
//
//    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
//    // Generated using JFormDesigner non-commercial license
//    private JMenuBar menuBar;
//    private JMenu fileMenu;
//    private JMenuItem openProjectItem;
//    private JMenuItem openRecentItem;
//    private JMenuItem importRomItem;
//    private JMenuItem exportRomItem;
//    private JMenu sheetsMenu;
//    private JMenuItem integrationSetupItem;
//    private JMenuItem migrationItem;
//    private JMenuItem applyToSheetItem;
//    private JMenuItem applyToRomItem;
//    private JMenuItem sheetRefreshChangesItem;
//    private JMenuItem sheetUploadChangesItem;
//    private JMenu editorMenu;
//    private JMenuItem popTrainersMenuItem;
//    private JMenuItem popEncountersMenuItem;
//    private JMenuItem popPokemonSpritesMenuItem;
//    private JMenu toolsMenu;
//    private JMenuItem randomizerItem;
//    private JMenuItem narctowlItem;
//    private JMenuItem blzCoderItem;
//    private JMenuItem fileRandomizerItem;
//    private JMenu debugMenu;
//    private JMenuItem consoleItem;
//    private JMenu helpMenu;
//    private JMenuItem aboutItem;
//    private JMenuItem tutorialItem;
//    private JMenuItem dspreCompatibilityItem;
//    private JSeparator separator2;
//    private JTabbedPane tabbedPane1;
//    private JPanel mainPanel;
//    private JButton sheetsSetupButton;
//    private JLabel linkTextField;
//    private JComboBox sheetChooserComboBox;
//    private JButton applyToSheetButton;
//    private JButton applyToRomButton;
//    private JScrollPane sheetPreviewScrollPane;
//    private JTable sheetPreviewTable;
//    private JButton sheetRefreshChangesButton;
//    private JButton sheetUploadChangesButton;
//    private JLabel warningLabel;
//    private TrainerPanel trainerPanel1;
//    private SinnohEncounterPanel sinnohEncounterPanel;
//    private JohtoEncounterPanel johtoEncounterPanel;
//    private PokemonSpritePanel pokemonSpritePanel;
//    private JPanel starterPanel;
//    private JLabel label1;
//    private JPanel openingPanel;
//    private JLabel label3;
//    private TrainerSpritePanel trainerSpritePanel;
//    private JPanel overworldSpritePanel;
//    private JToolBar jtbMain;
//    private JButton openProjectButton;
//    private JButton exportRomButton;
//    private JPanel toolBarHorizontalSeparator;
//    private JButton baseRomButton;
//    private JButton projectInfoButton;
//    // JFormDesigner - End of variables declaration  //GEN-END:variables
//
//
//
//    private boolean isDP(String gameCode)
//    {
//        return isPearl(gameCode) || isDiamond(gameCode);
//    }
//
//    private boolean isDiamond(String gameCode)
//    {
//        return gameCode.startsWith("ADA");
//    }
//
//    private boolean isPearl(String gameCode)
//    {
//        return gameCode.startsWith("APA");
//    }
//
//    private boolean isPlatinum(String gameCode)
//    {
//        return gameCode.startsWith("CPU");
//    }
//
//
//
//    private boolean isHGSS(String gameCode)
//    {
//        return isHeartGold(gameCode) || isSoulSilver(gameCode);
//    }
//
//    private boolean isHeartGold(String gameCode)
//    {
//        return gameCode.startsWith("IPK");
//    }
//
//    private boolean isSoulSilver(String gameCode)
//    {
//        return gameCode.startsWith("IPG");
//    }
//
//    public TutorialFrame getTutorial()
//    {
//        return tutorial;
//    }
//
//
//
//    public Project getProject()
//    {
//        return project;
//    }
//
//    public void setApi(GoogleSheetsAPI api)
//    {
//        this.api= api;
//        sheetChooserComboBox.setEnabled(true);
//        if(!api.isLocal())
//        {
//            linkTextField.setEnabled(true);
//            linkTextField.setText(api.getSPREADSHEET_LINK());
//            linkTextField.setForeground(new Color(27, 148, 255, 255));
//            sheetPreviewTable.setEnabled(false);
//            linkTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        }
//        else
//        {
//            linkTextField.setText("You are using local sheet storage.");
//            linkTextField.setForeground(new Color(255, 171, 27, 255));
//        }
//        applyToRomButton.setEnabled(true);
//        applyToSheetButton.setEnabled(true);
//        sheetRefreshChangesButton.setEnabled(true);
//        sheetUploadChangesButton.setEnabled(true);
//
//        toFront();
//        setEnabled(true);
//
//        try
//        {
//            ObjectOutputStream out= new ObjectOutputStream(new FileOutputStream(projectPath + "/api.ser"));
//            out.writeObject(api.getSPREADSHEET_LINK());
//            out.close();
//        }
//        catch (IOException e)
//        {
//            System.err.println("Attempt failed: " + projectPath + "/api.ser");
//            JOptionPane.showMessageDialog(this,"Unable to store API and credentials","Error",JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
//            new File(projectPath + "/api.ser").delete();
//        }
//
////        try
////        {
////            api.pokeditorSheetMetadataUpdater(baseRom.sheetList);
////        }
////        catch (IOException ignored)
////        {
////        }
//
//        if(!api.isLocal())
//        {
//            if (JOptionPane.showConfirmDialog(this, "Would you like to update the sheets using data from your ROM? (This will overwrite existing data in the sheets, and you can choose which data you want to apply specifically)", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == 0)
//            {
//                RomApplier editApplier = new RomApplier(project, projectPath, api, this, true, itemTableData, true);
//                editApplier.setLocationRelativeTo(this);
//            }
//        }
//
//        String[] sheetNames= new String[0];
//
//        try
//        {
//            sheetNames= api.getSheetNames();
//            setSheetChooserComboBox(sheetNames);
//        }
//        catch (IOException ignored)
//        {
//        }
//
//
//        sheetChooserComboBox.setSelectedIndex(indexOf("Personal",sheetNames));
//    }
//
//    private void initialLinkDataUpdate() throws IOException
//    {
//        System.out.println("Performing initial sheet updates");
//        Narctowl narctowl= new Narctowl(true);
//        String dataPath= projectPath + File.separator + project.getName() + "/data";
//
//        switch(baseRom)
//        {
//            case Diamond:
//            case Pearl:
//
//
//                break;
//
//            case Platinum:
//
//                //Personal and TM Learnsets
//                PersonalEditor editor= new PersonalEditor(dataPath, project);
//                narctowl.unpack(dataPath + "/poketool/personal/pl_personal.narc",dataPath + "/poketool/personal/pl_personal");
//                new File(dataPath + "/poketool/personal/pl_personal").deleteOnExit();
//                PersonalReturnGen4 personalReturn= editor.personalToSheet("/poketool/personal/pl_personal");
//                api.updateSheet("Personal",personalReturn.getPersonalData());
//                api.updateSheet("TM Learnsets",personalReturn.getTMData());
//
//                //Level-Up Learnsets
//                break;
//
//            case HeartGold:
//
//                break;
//
//            case SoulSilver:
//
//                break;
//
//            case Black:
//
//                break;
//
//            case White:
//
//                break;
//
//            case Black2:
//
//                break;
//
//            case White2:
//
//                break;
//        }
//    }
//
//    private void initializeItemsTable()
//    {
//        //TODO finish
//        TableLocator tableLocator= new TableLocator(project);
//        TablePointers.TablePointer itemTablePointer;
//        boolean success= true;
//        try
//        {
//            itemTablePointer = TablePointers.getPointers().get(project.getBaseRomGameCode()).get("items");
//        }
//        catch (Exception e)
//        {
//            System.err.println("Error loading items table");
//            JOptionPane.showMessageDialog(this, "Error loading items table", "Error", JOptionPane.ERROR_MESSAGE);
//            success= false;
//            itemTablePointer= null;
//            e.printStackTrace();
//        }
//
//        try
//        {
//            switch(project.getBaseRom())
//            {
//                case Platinum:
//                    itemNames = TextEditor.getBank(project, TextBank.PLAT_ITEM_NAMES);
//                    break;
//
//                case HeartGold:
//                case SoulSilver:
//                    itemNames = TextEditor.getBank(project, TextBank.HGSS_ITEM_NAMES);
//                    break;
//            }
//        }
//        catch(IOException e)
//        {
//            System.err.println("Error loading item name text bank");
//            JOptionPane.showMessageDialog(this, "Error loading item name text bank", "Error", JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace();
//            success= false;
//        }
//
//        byte[] itemTable;
//        if (success)
//        {
//            itemTable= tableLocator.obtainTableArr(itemTablePointer, itemNames.length, 8);
//            itemTableData = ItemTableParser.parseTable(itemTable, itemNames.length);
//        }
//    }
//
//    public static ArrayList<ItemTableEntry> getItemTableData()
//    {
//        return itemTableData;
//    }
//
//    private List<List<Object>> getTableData()
//    {
//        List<List<Object>> ret= new ArrayList<>();
//
//        List<Object> header= new ArrayList<>();
//        for(int col= 0; col < sheetPreviewTable.getColumnCount(); col++)
//        {
//            header.add(sheetPreviewTable.getColumnName(col));
//        }
//        ret.add(header);
//
//        for(int row= 0; row < sheetPreviewTable.getRowCount(); row++)
//        {
//            List<Object> sub= new ArrayList<>();
//            for(int col= 0; col < sheetPreviewTable.getColumnCount(); col++)
//            {
//                sub.add(sheetPreviewTable.getValueAt(row,col));
//            }
//            ret.add(sub);
//        }
//
//        return ret;
//    }
//
//    private boolean contains(String[] arr, String str)
//    {
////        System.out.println("Comparing " + str + " against " + Arrays.toString(arr));
//        for(String s : arr)
//        {
////            System.out.println("Comparing " + str + " to " + s);
//            if(s.equals(str))
//                return true;
//        }
//        return false;
//    }
//
//    private int indexOf(String str, String[] arr)
//    {
//        for(int i= 0; i < arr.length; i++)
//        {
//            if(arr[i].equals(str))
//                return i;
//        }
//        return -1;
//    }
//
//    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
//    {
//        File destFile = new File(destinationDir, zipEntry.getName());
//
//        String destDirPath = destinationDir.getCanonicalPath();
//        String destFilePath = destFile.getCanonicalPath();
//
//        if (!destFilePath.startsWith(destDirPath + File.separator)) {
//            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
//        }
//
//        return destFile;
//    }
//
//    public String getProjectPath()
//    {
//        return projectPath;
//    }
//
//    public GoogleSheetsAPI getApi()
//    {
//        return api;
//    }
//
//    public void clearAllDirs()
//    {
//        clearAllDirs(pokemonSpritePanel.toDelete);
//        clearAllDirs(trainerPanel1.toDelete);
//        clearAllDirs(SpriteDataProcessor.toDelete);
//        clearAllDirs(sinnohEncounterPanel.toDelete);
////        clearAllDirs(johtoEncounterPanel.toDelete);
//
//        TextEditor.cleanup(project);
//
//        pokemonSpritePanel.toDelete= new ArrayList<>();
//        trainerPanel1.toDelete= new ArrayList<>();
//        SpriteDataProcessor.toDelete= new ArrayList<>();
//        sinnohEncounterPanel.toDelete= new ArrayList<>();
//        //TODO fix johto clear and implement trainer text clear (a057)
////        johtoEncounterPanel.toDelete= new ArrayList<>();
//
//    }
//
//    public static void clearAllDirs(List<File> folders)
//    {
//        if(folders != null)
//        {
//            for(File folder : folders)
//            {
//                clearDirs(folder);
//            }
//        }
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
//    public static void cleanupDirs(File folder)
//    {
//        if(folder != null)
//        {
//            File[] subFiles= folder.listFiles();
//            if(subFiles != null)
//            {
//                if(folder.exists())
//                {
//                    for(File f : Objects.requireNonNull(folder.listFiles()))
//                    {
//                        if(f.isDirectory())
//                            cleanupDirs(f);
//                        else if(f.isHidden())
//                        {
//                            if(!f.delete())
//                            {
//                                System.err.println("File \"" + f.getAbsolutePath() + "\" could not be deleted.");
//                                if(System.getProperty("os.name").toLowerCase().contains("win"))
//                                {
//                                    System.err.println("\tAttempting to forcefully delete");
//                                    try
//                                    {
//                                        FileDeleteStrategy.FORCE.delete(f);
//                                    }
//                                    catch(IOException e)
//                                    {
//                                        System.err.println("\tForceful deletion failed");
//                                    }
//                                }
//                            }
//                        }
//                    }
//
////                    if(!folder.delete())
////                    {
////                        System.err.println("File \"" + folder.getAbsolutePath() + "\" could not be deleted.");
////                    }
//
//                    folder.delete();
//                }
//            }
//        }
//    }
//
//    private boolean selectedContains(String[] arr, String str)
//    {
//        for(String s : arr)
//        {
//            if(s.toLowerCase().contains(str.toLowerCase()))
//                return true;
//        }
//        return false;
//    }
//
//    public TrainerPanel getTrainerPanel1()
//    {
//        return trainerPanel1;
//    }
//
//    public class PaintTableCellRenderer extends DefaultTableCellRenderer
//    {
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
//        {
//            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//
//            table.setShowVerticalLines(true);
//            table.setShowHorizontalLines(true);
//            table.setShowGrid(true);
//
//            Color fg;
//            Color bg;
//
//            table.getTableHeader().setBackground(colorData.get(0).get(0));
//            table.getTableHeader().setForeground(Color.BLACK);
//            table.getTableHeader().setBorder(BorderFactory.createLineBorder(Color.BLACK));
//
//            if(colorData != null)
//            {
//                if(row < colorData.size() && column < colorData.get(row).size())
//                {
//                    fg= Color.BLACK;
//                    bg= colorData.get(row+1).get(column);
//
//                    setBackground(colorData.get(row).get(column));
//                    setForeground(Color.BLACK);
//                }
//                else
//                    return this;
//            }
//            else
//            {
//                fg= Color.BLACK;
//                bg= table.getBackground();
//
//                setBackground(table.getBackground());
//                setForeground(Color.BLACK);
//            }
//
//            if (isSelected) {
//                super.setForeground(fg == null ? Color.WHITE
//                        : fg);
//                super.setBackground(bg == null ? Color.BLUE
//                        : bg);
//            } else {
//                Color background= bg != null
//                        ? bg
//                        : table.getBackground();
//                if (background == null || background instanceof javax.swing.plaf.UIResource) {
//                    Color alternateColor = DefaultLookup.getColor(this, ui, "Table.alternateRowColor");
//                    if (alternateColor != null && row % 2 != 0) {
//                        background = alternateColor;
//                    }
//                }
//                super.setForeground(fg != null
//                        ? fg
//                        : table.getForeground());
//                super.setBackground(background);
//            }
//
//            if(!isSelected)
//            {
//                setBorder(BorderFactory.createLineBorder(Color.BLACK,1));
//            }
//            else
//            {
//                setBorder(BorderFactory.createLineBorder(Color.BLUE,2));
//            }
//
//            return this;
//        }
//    }
//}
