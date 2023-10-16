/*
 * Created by JFormDesigner on Wed Dec 23 14:00:53 EST 2020
 */

package io.github.turtleisaac.pokeditor;

import javax.swing.*;

import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import io.github.turtleisaac.nds4j.ui.ProgramType;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.nds4j.ui.exceptions.ToolCreationException;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.console.ConsoleWindow;

import java.util.function.Supplier;

/**
 * @author turtleisaac
 */
public class Main
{
    public static final String versionNumber = "v3snapshot";
    public static final boolean programFilesModifiedThisVersion = true;
    private static ConsoleWindow console;

    private static final String[] mainMenuJokes= new String[] {"Even GUIer than before!", "Now with extra dip!", "Constructing additional pylons.", "Comes with a large soda.", "This is a bucket.", "It's dangerous to go alone, take this!", "Oh no, not again.", "Our Princess is in another castle!", "Hey! Listen!", "It was super effective!", "Do a barrel roll!", "Stop right there, criminal scum!", "Are you a boy or a girl?", "You have died of dysentery.", "All your base are belong to us.", "Somebody set up us the bomb.", "For great justice.", "Main screen turn on.", "You have no chance to survive make your time.", "Evolving recombination vectors.", "Online interactions not rated by the ESRB.", "Konohagakure Hiden Taijutsu Ōgi: Sennen Goroshi!", "A RED Spy is in the base!", "Medic!", "There is no possible way a bee should be able to fly.", "I'll be back.", "Just keep swimming, just keep swimming.", "I need your clothes, your boots, and your motorcycle.", "I am Groot.", "Sixty-nine!", "Would you like to play Global Thermonuclear War?", "This is Sparta!", "What does the fox say?", "Yer a wizard, 'arry.","I've got a bad feeling about this.","With great power comes great responsibility.","It's over Anakin, I have the high ground!","Pizza the Hutt!","Hello there!","Not the bees!","Where's my super suit?","Wilsoooooooon!","One million dollars.","Are you the keymaster?","There is no PokEditor, there is only Zuul.","You shall not pass!","I am the captain now.","You sunk my Battleship!","Oompa, Loompa, doom-pa-dee-do.","Pubert.","Who lives in a pineapple under the sea?","Is mayonnaise an instrument?","Is this PokEditor? No this is Patrick.", "We are not cavemen, we have technology!","FUUTUUURE!","Take a bite out of the silver sandwich.","Come with me and eat that horse!","Ohana means family.","There's a snake in my boot.","We toys can see EVERYTHING.","I love Kung FUUUUUUUUUUUUUUuuuuuuuuuuuuu!","IT'S SO FLUFFY!","I commit crimes with both direction and MAGNITUDE!", "Has anyone seen my LAUNCH box?", "Live long and prosper.", "Sharks with frickin' laser beams attached to their heads!", "When Mr. Bigglesworth gets upset, people die!","Why make trillions, when we can make... billions?", "Go Go Gadget PokEditor!", "The gum made for you from dead Pikachu.", "TRA LA LAAAA!", "Never underestimate the power of Captain Underpants.", "Ooh Eeh Ooh Ah Aah Ting Tang Walla Walla Bing Bang.", "WHAT ARE YA DOIN' IN MY SWAMP??!","Dead men tell no tales.","I speak for the trees.", "DOOOOOOOODGE!!!","I'm insane, from Earth.","The following is a fan-based parody.","NOTICE ME SENPAI!","Hypebola Mine Chamber.","Hyperglycemic Crime Chamber.","Hypebonics Rhyme Chamber.","That's what makes it hyper-sonic.","ALL HAIL PRINCESS TRUNKS!","Presented by Hetap.","Team Three Star!","KI! KOU! HOU!","It means God, now bow.","Who's on first.","Bear Left, Right Frog.","Oh man! It's even got a cool name!","Houston, we have a problem.","Here's looking at you, kid.","I am designated as Android 16.","Yamcha's Here! Yamcha's DEAD!", "I am hilarious, and you will quote everything I say.","Data not found.","PLACEHOLDER TEXT.","I am error.","It's a cookbook!","IT'S OVER 9000!","It's all coarse, and rough, and irritating", "I am the Senate.", "There's always a bigger fish","Now this is pod racing!","You don’t want to sell me death sticks.","I can’t watch anymore.","Unlimited Rice Pudding!","Wibbley Wobbley Timey Whimey.", "Blink and you’re dead.","It isn’t rocket science, it’s just quantum physics!","Big flashy things have my name written all over them.","Always take a banana to a party.","EXTERMINATE!","Bow ties are cool.","Let’s go and poke it with a stick.","Snow White and the Seven Keys to Doomsday.","I am the Bad Wolf.","Time and Relative Dimension In Space.","Totally and Radically Driving In Style.","John Smith.","Mesa eyeball stuck in the sleeve.","Who wants Babahoohas?","Humongous hungolomghononoloughongous.","Even bigger bonkhonagahoogs.","Bear will arrive sooner than thought.", "BEAR IS APPROACHING AT ALARMING SPEEDS.","BEAR IS GO FAST LOSING TRACK OF BEAR.","BEAR HAS REACHED MACH ONE.","WE HAVE LOST VISUAL ON BEAR!","https://youtu.be/Uj1ykZWtPYI", "Bested only by Route 201.","Now with cup holders.","About 90.","Did you read the faqs?","F is for fire that burns down the whole town!","U is for uranium... bombs!","N is for no survivors!","I'll make you an offer you can't refuse.","Dobby is free.","When in doubt, go to the library.","I solemnly swear I am up to no good.","I am a wizard, not a baboon brandishing a stick.","Slugulus Eructo!","Avada Kedavra!","Swish and flick!","Conglaturations!","Rock music approaches at high velocity.","I do not know what this Yamcha is... but it sounds disappointing.","It's all downhill from here.","I AM THE HYPE!","Hey Vegeta, are we there yet?","I'll use Rock Smash.","I've got a Master Ball with your name on it","Super Kami Guru allows this.","Look Vegeta! It's a Pokémon!","Vegeta Jr. NO!!","Mahogany.","apt get moo","Aw... crap baskets.","The Adobe Flash plugin has crashed. Reload the page to try again.","Row row row your boat.","Dramatic finish!","I am a super sandwich!","Muffin button.","Catch it. Catch it with your teeth.","1st rule of Popo's training.","I'll tell you where they're not: safe.","Can we go to the Bug Planet?","As mysterious as the dark side of the Moon.","Could you speak up? I'm not wearing pants.","OH MY GOD HE'S SO F***ING COOL!","What's your power level?","KAKKAROT!","WE ARE BIOMEN!","We're gonna buy us a submarine!","Sensu Bean!","Slimy, yet satisfying.","BEEP BEEP!!","YOU HAVE SUMMONED THE ETERNAL DRAGON!","IT'S TIME TO D-D-D-D-D-D-D-D-D-D-D-D-DUEL!","You've activated my trap card!","The mitochondria is the powerhouse of the cell.","Excelsior!","We are number one.","The snack that smiles back!","Just do it!","We have the meats.","I'm lovin' it.","America runs on Dunkin.","It's finger lickin' good.","Eat Fresh.","Got any grapes?","E-I-E-I-O","More than meets the eye.","Robots in disguise.","What's up, doc?","Rabbit season.","Duck season.","SYNTAX ERROR.","1337.","Trees on the planet Malchior 7 are 300 feet tall and breath fire!","Meow Mix Meow Mix please deliver.","I love democracy.","Do it.","Have you heard the tragedy of Darth Plagueis the Wise?","Coconut Gun.","Scotty beam me up!","You can't break the laws of physics.","Do you want to know what death tastes like?","Son of a gum-chewing funk monster!","You can make cupcakes out of anything!","Super Luxurious Omnidirectional Whatchamajigger.","Super Hydraulic Instantaneous Transporter.","City Morgue!","OH YEAH!","YOU'RE FIREEEEEEEEEEEEEEEEEEEEEEEeeeeeD!","Kupkake-inator!","I'll try spinning, that's a good trick!","Gotta catch 'em all!","Is your refrigerator running?","Got milk?","Only you can stop forest fires.","My train is swimming in the piano again.","And I would've gotten away with it too!","This is what we call a Pro Gamer move.","Welcome to the world of Pokémon!","Bill Nye the Science Guy!","Funnier than 24.","To infinity, and beyond!","Who you callin' pinhead?","Did you know that Venomoth is impossible to balance?","Zoo-Wee-Mama!","Ploopy.","Loded Diaper.","At least it isn't an acronym.","Process finished with exit code 130.","Your seat cushion may be used as a flotation device.","Did you know that Jay likes Moemon?","Jaydeer.","Do not open until 2099.","Programmed on a Mac.","Socks high, see ya bye!","We are the music men, and we've come to play.","Coming soon-ish.", "https://www.youtube.com/watch?v=asjQNZn7vng", "Brace for impact.", "And this is to go even further beyond!", "What a useless transformation.", "FIND ME IN THE ALPS!", "I Saw A Bird. It Was Pretty. Kick Its Ass.", "Look, Vegeta! More bald people.","Oh, it's just a space duck.","'Twas never a matter of if: only when.'","One star, two star, all as big as my head!","Jay in your house. With milk.","These aren't the droids you're looking for.","That's rough buddy.","The true heir of Salazar Slytherin."};

    public static void main(String[] args) throws ToolCreationException
    {
        Tool tool = Tool.create();
        tool.setType(ProgramType.PROJECT)
                .setName("PokEditor")
                .setVersion("3.0.0-SNAPSHOT")
//                .setFlavorText("Did you know that Jay likes Moemon?")
                .setFlavorText(mainMenuJokes[(int) (Math.random()*(mainMenuJokes.length))])
                .setAuthor("Developed by Turtleisaac")
                .addLookAndFeel(new FlatDarkPurpleIJTheme())
                .addLookAndFeel(new FlatArcOrangeIJTheme())
//                .addLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel())
                .addGame("Pokémon Platinum", "CPU")
                .addGame("Pokémon HeartGold","IPK")
                .addGame("Pokémon SoulSilver","IPG")
                .addPanelManager(() -> new PokeditorManager(tool))
                .init();



//        PokEditor pokeditor= new PokEditor();
//        pokeditor.jokeLabel.setText();
//        System.out.println(mainMenuJokes.length);

        /**
         * Update checker
         */

//        try
//        {
//            URL latestRelease= new URL("https://api.github.com/repos/turtleisaac/PokEditor-v2/releases/latest");
//            BufferedReader reader= new BufferedReader(new InputStreamReader(latestRelease.openStream()));
//            String line;
//
//            System.out.println("Checking for updates");
//            while((line= reader.readLine()) != null)
//            {
//                line= line.trim();
//                String original= line;
//                line= line.substring(line.indexOf("\"name\""),line.indexOf("\"draft\""));
//                if(line.contains("name"))
//                {
//                    String newVersion= line.substring(line.indexOf(":")+2,line.length()-2);
//                    String newVersionNumber= newVersion.split(" ")[1];
//
//                    if(checkNewer(versionNumber,newVersionNumber))
//                    {
//                        line= original;
//                        line= line.substring(line.indexOf("\"browser_download_url\""));
//                        line= line.substring(line.indexOf(":")+2,line.indexOf("}"));
//                        String downloadLink= line.substring(0,line.lastIndexOf("\""));
//
//                        line= original;
//                        String updateNotes= line.substring(line.indexOf("body")+7, line.lastIndexOf("\""));
////                        updateNotes= updateNotes.replace("* ","");
//                        updateNotes= updateNotes.replace("\\n","\n");
//                        updateNotes= updateNotes.replace("\\r","\r");
//
//                        System.out.println("Download link: " + downloadLink);
//                        System.out.println("Update notes:\n" + updateNotes);
//
//
//                        if(JOptionPane.showConfirmDialog(null,newVersion + " is now available. Would you like to update?\n" + updateNotes,"PokEditor",JOptionPane.YES_NO_OPTION,JOptionPane.INFORMATION_MESSAGE) == 0)
//                        {
//                            updater(downloadLink);
//                        }
//                        break;
//                    }
//                }
//            }
//        } catch (IOException ignored)
//        {
//        }

//        pokeditor.changeThemeButton.addActionListener(e -> pokeditor.changeThemeButtonActionPerformed(e));
    }

//    public PokEditor() {
//        initComponents();
//        titleLabel.setText("PokEditor " + versionNumber);
//        console= new ConsoleWindow();
//    }
//
//    public void setConfig(Config config)
//    {
//        this.config= config;
//    }
//
//    private void changeThemeButtonActionPerformed(ActionEvent e)
//    {
//        Config newConfig= Config.copyOf(config);
//
//        newConfig.setTheme(newConfig.getTheme() == Theme.Darcula ? Theme.Light : Theme.Darcula);
//
//        try
//        {
//            ObjectOutputStream configOut= new ObjectOutputStream(new FileOutputStream(path + "pokeditor_config"));
//            configOut.writeObject(newConfig);
//        }
//        catch (IOException a)
//        {
//            a.printStackTrace();
//        }
//
//        frame.dispose();
//
//        Point location= frame.getLocation();
//        String xLocation= "" + location.getX();
//        String yLocation= "" + location.getY();
//
//        main(new String[] {xLocation, yLocation});
//    }
//
//    private void newProjectButtonActionPerformed(ActionEvent e)
//    {
//        NewProject project= new NewProject(frame,console);
//    }
//
//    private void openProjectButtonActionPerformed(ActionEvent e)
//    {
//        JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//        fc.setDialogTitle("Open Project");
//        fc.setAcceptAllFileFilterUsed(false);
//
//        fc.addChoosableFileFilter(new MyFilter("PokEditor Projects",".pokeditor"));
////        fc.addChoosableFileFilter(new MyFilter("DS Pokemon Rom Editor Projects",".dspre"));
//
//        if (e.getSource() == openProjectButton) {
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                try
//                {
//                    ProjectWindow projectWindow= new ProjectWindow(fc.getSelectedFile().getAbsolutePath(), frame,console);
//                    projectWindow.setLocationRelativeTo(this);
//                    frame.setVisible(false);
//                }
//                catch (IOException ignored)
//                {
//                }
//
//            }
//        }
//    }
//
//    private void selectNarcButtonActionPerformed(ActionEvent e)
//    {
//        JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//        fc.setAcceptAllFileFilterUsed(true);
//        fc.setDialogTitle("Select Narc");
//
//        fc.setFileFilter(new MyFilter("Nitro Archive (Narc)",".narc",".bin"));
//
//        if (e.getSource() == selectNarcButton) {
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                selectNarcTextField.setText(fc.getSelectedFile().getAbsolutePath());
//            }
//        }
//    }
//
//    private void unpackNarcButtonActionPerformed(ActionEvent e)
//    {
//        try
//        {
//            Narctowl narctowl= new Narctowl(true);
//
//            String narc= selectNarcTextField.getText();
//            String path= narc.substring(0,narc.lastIndexOf(File.separator));
//            narc= narc.substring(narc.lastIndexOf(File.separator)+1);
//            narc= narc.substring(0,narc.lastIndexOf(".") != -1 ? narc.lastIndexOf(".") : narc.length());
//            System.out.println(narc);
//
//            File directory= new File(path + File.separator + narc + File.separator);
//            directory.mkdir();
//
//            JFileChooser fc= new JFileChooser(directory);
//            fc.setDialogTitle("Choose Output Directory");
//            System.out.println(fc.getCurrentDirectory().toString());
//            fc.setAcceptAllFileFilterUsed(false);
//            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//            if (e.getSource() == unpackNarcButton) {
//                int returnVal = fc.showOpenDialog(this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    boolean makeFolder = !fc.getSelectedFile().getAbsolutePath().equals(directory.getAbsolutePath());
//                    narctowl.unpack(selectNarcTextField.getText(), fc.getSelectedFile().getAbsolutePath(), makeFolder);
//                    JOptionPane.showMessageDialog(this,"Success!","Narctowl",JOptionPane.INFORMATION_MESSAGE);
//                }
//
//            }
//
//            directory.delete();
//        }
//        catch (IOException a)
//        {
//            JOptionPane.showMessageDialog(this,"Error","Narctowl",JOptionPane.ERROR_MESSAGE);
//            JOptionPane.showMessageDialog(this, a.toString(), "Stack Trace", JOptionPane.ERROR_MESSAGE);
//        }
//
//    }
//
//    private void selectFolderButtonActionPerformed(ActionEvent e)
//    {
//        JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//        fc.setDialogTitle("Choose unpacked Narc Directory");
//        fc.setAcceptAllFileFilterUsed(false);
//        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//
//        if (e.getSource() == selectFolderButton) {
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                selectFolderTextField.setText(fc.getSelectedFile().getAbsolutePath());
//            }
//        }
//    }
//
//    private void packNarcButtonActionPerformed(ActionEvent e)
//    {
//        try
//        {
//            Narctowl narctowl= new Narctowl(true);
//
//            JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//            fc.setDialogTitle("Select Narc");
//            fc.setAcceptAllFileFilterUsed(true);
//            fc.setFileFilter(new MyFilter("Nitro Archive (Narc)",".narc",".bin"));
//
//            if (e.getSource() == packNarcButton) {
//                int returnVal = fc.showOpenDialog(this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    narctowl.pack(selectFolderTextField.getText(), "",fc.getSelectedFile().getAbsolutePath());
//                }
//                JOptionPane.showMessageDialog(this,"Success!","Narctowl",JOptionPane.INFORMATION_MESSAGE);
//            }
//        }
//        catch (IOException a)
//        {
//            JOptionPane.showMessageDialog(this,"Error","Narctowl",JOptionPane.ERROR_MESSAGE);
//            JOptionPane.showMessageDialog(this, a.toString(), "Stack Trace", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void compressionSelectFileButtonActionPerformed(ActionEvent e)
//    {
//        JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//        fc.setDialogTitle("Choose File");
//        fc.setAcceptAllFileFilterUsed(true);
//
//        if (e.getSource() == compressionSelectFileButton) {
//            int returnVal = fc.showOpenDialog(this);
//
//            if (returnVal == JFileChooser.APPROVE_OPTION) {
//                compressionSelectFileTextField.setText(fc.getSelectedFile().getAbsolutePath());
//            }
//        }
//    }
//
//    private void decompressButtonActionPerformed(ActionEvent e)
//    {
//        try
//        {
//            BLZCoder blzCoder= new BLZCoder(null);
//            Buffer buffer;
//            BinaryWriter writer;
//
//            JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//            fc.setDialogTitle("Choose File");
//            fc.setAcceptAllFileFilterUsed(true);
//
//            if (e.getSource() == decompressButton) {
//                int returnVal = fc.showOpenDialog(this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    buffer= new Buffer(compressionSelectFileTextField.getText());
//
//                    byte[] data= buffer.readRemainder();
//
//                    byte[] decompressed= blzCoder.BLZ_DecodePub(data,"file");
//
//                    writer= new BinaryWriter(fc.getSelectedFile().getAbsolutePath());
//                    writer.write(decompressed);
//                    JOptionPane.showMessageDialog(this,"Success!","BLZ Coder",JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        }
//        catch (IOException a)
//        {
//            JOptionPane.showMessageDialog(this,"Error","BLZ Coder",JOptionPane.ERROR_MESSAGE);
//            JOptionPane.showMessageDialog(this, a.toString(), "Stack Trace", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void compressButtonActionPerformed(ActionEvent e)
//    {
//        try
//        {
//            BLZCoder blzCoder= new BLZCoder(null);
//            Buffer buffer;
//            BinaryWriter writer;
//
//            JFileChooser fc= new JFileChooser(System.getProperty("user.dir"));
//            fc.setAcceptAllFileFilterUsed(true);
//            fc.setDialogTitle("Choose File");
//
//            if (e.getSource() == compressButton) {
//                int returnVal = fc.showOpenDialog(this);
//
//                if (returnVal == JFileChooser.APPROVE_OPTION) {
//                    buffer= new Buffer(compressionSelectFileTextField.getText());
//
//                    boolean arm9= JOptionPane.showConfirmDialog(this,"Is this an arm9?","BLZ Coder",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == 1;
//
//                    boolean specialCompress= JOptionPane.showConfirmDialog(this,"Use best compression algorithm?","BLZ Coder",JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == 1;
//
//                    byte[] data= buffer.readRemainder();
//
//                    byte[] compressed= blzCoder.BLZ_EncodePub(data,arm9,specialCompress,"file");
//
//                    writer= new BinaryWriter(fc.getSelectedFile().getAbsolutePath());
//                    writer.write(compressed);
//                    JOptionPane.showMessageDialog(this,"Success!","BLZ Coder",JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        }
//        catch (IOException a)
//        {
//            JOptionPane.showMessageDialog(this,"Error","BLZ Coder",JOptionPane.ERROR_MESSAGE);
//            JOptionPane.showMessageDialog(this, a.toString(), "Stack Trace", JOptionPane.ERROR_MESSAGE);
//        }
//    }



//    private void initComponents() {
//        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
//        // Generated using JFormDesigner non-commercial license
//        mainPanel = new JPanel();
//        tabbedPane1 = new JTabbedPane();
//        projectsPanel = new JPanel();
//        projectsSubPanel = new JPanel();
//        titleLabel = new JLabel();
//        jokeLabel = new JLabel();
//        newProjectButton = new JButton();
//        openProjectButton = new JButton();
//        vSpacer4 = new JPanel(null);
//        changeThemeButton = new JButton();
//        narctowlPanel = new JPanel();
//        vSpacer3 = new JPanel(null);
//        selectNarcButton = new JButton();
//        selectNarcTextField = new JTextField();
//        selectNarcLabel = new JLabel();
//        unpackNarcButton = new JButton();
//        vSpacer1 = new JPanel(null);
//        narctowlSeparator = new JSeparator();
//        vSpacer2 = new JPanel(null);
//        selectFolderButton = new JButton();
//        selectFolderTextField = new JTextField();
//        selectFolderLabel = new JLabel();
//        packNarcButton = new JButton();
//        compressionPanel = new JPanel();
//        vSpacer5 = new JPanel(null);
//        compressionSelectFileButton = new JButton();
//        compressionSelectFileTextField = new JTextField();
//        compressionSelectFileLabel = new JLabel();
//        decompressButton = new JButton();
//        compressButton = new JButton();
//        authorLabel = new JLabel();
//
//        //======== mainPanel ========
//        {
//            mainPanel.setLayout(new MigLayout(
//                "hidemode 3",
//                // columns
//                "[467,grow,fill]",
//                // rows
//                "[171]" +
//                "[190,grow,bottom]" +
//                "[]" +
//                "[]" +
//                "[]" +
//                "[]" +
//                "[]" +
//                "[21,center]" +
//                "[]" +
//                "[]" +
//                "[0]" +
//                "[]"));
//
//            //======== tabbedPane1 ========
//            {
//
//                //======== projectsPanel ========
//                {
//                    projectsPanel.setLayout(new MigLayout(
//                        "hidemode 3",
//                        // columns
//                        "[fill]" +
//                        "[fill]",
//                        // rows
//                        "[]" +
//                        "[]" +
//                        "[]" +
//                        "[grow]" +
//                        "[]"));
//
//                    //======== projectsSubPanel ========
//                    {
//                        projectsSubPanel.setLayout(new MigLayout(
//                            "hidemode 3",
//                            // columns
//                            "[395,fill]",
//                            // rows
//                            "[grow]" +
//                            "[]" +
//                            "[]"));
//
//                        //---- titleLabel ----
//                        titleLabel.setText("PokEditor v2.0 - DEV BUILD");
//                        titleLabel.setFont(new Font(".SF NS Text", Font.PLAIN, 18));
//                        projectsSubPanel.add(titleLabel, "cell 0 1,alignx center,growx 0");
//
//                        //---- jokeLabel ----
//                        jokeLabel.setText("Even GUIer than Before!");
//                        jokeLabel.setFont(new Font(".SF NS Text", Font.PLAIN, 12));
//                        projectsSubPanel.add(jokeLabel, "cell 0 2,alignx center,growx 0");
//                    }
//                    projectsPanel.add(projectsSubPanel, "cell 0 0");
//
//                    //---- newProjectButton ----
//                    newProjectButton.setText("New Project");
//                    newProjectButton.addActionListener(e -> newProjectButtonActionPerformed(e));
//                    projectsPanel.add(newProjectButton, "cell 0 1");
//
//                    //---- openProjectButton ----
//                    openProjectButton.setText("Open Project");
//                    openProjectButton.addActionListener(e -> openProjectButtonActionPerformed(e));
//                    projectsPanel.add(openProjectButton, "cell 0 2");
//                    projectsPanel.add(vSpacer4, "cell 0 3,growy");
//
//                    //---- changeThemeButton ----
//                    changeThemeButton.setText("Change Theme");
//                    changeThemeButton.addActionListener(e -> changeThemeButtonActionPerformed(e));
//                    projectsPanel.add(changeThemeButton, "cell 0 4,alignx center,growx 0");
//                }
//                tabbedPane1.addTab("PokEditor", projectsPanel);
//
//                //======== narctowlPanel ========
//                {
//                    narctowlPanel.setLayout(new MigLayout(
//                        "hidemode 3",
//                        // columns
//                        "[fill]" +
//                        "[grow,fill]",
//                        // rows
//                        "[grow]" +
//                        "[]" +
//                        "[]" +
//                        "[]" +
//                        "[grow]" +
//                        "[]" +
//                        "[grow]" +
//                        "[]" +
//                        "[]" +
//                        "[]"));
//                    narctowlPanel.add(vSpacer3, "cell 0 0,growy");
//
//                    //---- selectNarcButton ----
//                    selectNarcButton.setText("Select Narc");
//                    selectNarcButton.addActionListener(e -> selectNarcButtonActionPerformed(e));
//                    narctowlPanel.add(selectNarcButton, "cell 0 1");
//                    narctowlPanel.add(selectNarcTextField, "cell 1 1");
//                    narctowlPanel.add(selectNarcLabel, "cell 0 2,alignx center,growx 0");
//
//                    //---- unpackNarcButton ----
//                    unpackNarcButton.setText("Unpack");
//                    unpackNarcButton.addActionListener(e -> unpackNarcButtonActionPerformed(e));
//                    narctowlPanel.add(unpackNarcButton, "cell 1 3,alignx right,growx 0");
//                    narctowlPanel.add(vSpacer1, "cell 0 4,growy");
//                    narctowlPanel.add(narctowlSeparator, "cell 0 5 2 1,aligny center,growy 0");
//                    narctowlPanel.add(vSpacer2, "cell 0 6,growy");
//
//                    //---- selectFolderButton ----
//                    selectFolderButton.setText("Select Folder");
//                    selectFolderButton.addActionListener(e -> selectFolderButtonActionPerformed(e));
//                    narctowlPanel.add(selectFolderButton, "cell 0 7");
//                    narctowlPanel.add(selectFolderTextField, "cell 1 7");
//                    narctowlPanel.add(selectFolderLabel, "cell 0 8,alignx center,growx 0");
//
//                    //---- packNarcButton ----
//                    packNarcButton.setText("Pack");
//                    packNarcButton.addActionListener(e -> packNarcButtonActionPerformed(e));
//                    narctowlPanel.add(packNarcButton, "cell 1 9,alignx right,growx 0");
//                }
//                tabbedPane1.addTab("Narc Pack/Unpack", narctowlPanel);
//
//                //======== compressionPanel ========
//                {
//                    compressionPanel.setLayout(new MigLayout(
//                        "hidemode 3",
//                        // columns
//                        "[fill]" +
//                        "[grow,fill]",
//                        // rows
//                        "[grow]" +
//                        "[]" +
//                        "[]" +
//                        "[]" +
//                        "[grow]" +
//                        "[]" +
//                        "[grow]" +
//                        "[]" +
//                        "[]" +
//                        "[]"));
//                    compressionPanel.add(vSpacer5, "cell 0 0,growy");
//
//                    //---- compressionSelectFileButton ----
//                    compressionSelectFileButton.setText("Select File");
//                    compressionSelectFileButton.addActionListener(e -> compressionSelectFileButtonActionPerformed(e));
//                    compressionPanel.add(compressionSelectFileButton, "cell 0 1");
//                    compressionPanel.add(compressionSelectFileTextField, "cell 1 1");
//                    compressionPanel.add(compressionSelectFileLabel, "cell 0 2,alignx center,growx 0");
//
//                    //---- decompressButton ----
//                    decompressButton.setText("Decompress");
//                    decompressButton.addActionListener(e -> decompressButtonActionPerformed(e));
//                    compressionPanel.add(decompressButton, "cell 1 3");
//
//                    //---- compressButton ----
//                    compressButton.setText("Compress");
//                    compressButton.addActionListener(e -> compressButtonActionPerformed(e));
//                    compressionPanel.add(compressButton, "cell 1 3");
//                }
//                tabbedPane1.addTab("Compression", compressionPanel);
//            }
//            mainPanel.add(tabbedPane1, "cell 0 0,growx");
//
//            //---- authorLabel ----
//            authorLabel.setText("Copyright (c) Turtleisaac");
//            authorLabel.setFont(new Font(".SF NS Text", Font.PLAIN, 10));
//            mainPanel.add(authorLabel, "cell 0 1,alignx center,growx 0");
//        }
//        // JFormDesigner - End of component initialization  //GEN-END:initComponents
//    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel projectsPanel;
    private JPanel projectsSubPanel;
    private JLabel titleLabel;
    private JLabel jokeLabel;
    private JButton newProjectButton;
    private JButton openProjectButton;
    private JPanel vSpacer4;
    private JButton changeThemeButton;
    private JPanel narctowlPanel;
    private JPanel vSpacer3;
    private JButton selectNarcButton;
    private JTextField selectNarcTextField;
    private JLabel selectNarcLabel;
    private JButton unpackNarcButton;
    private JPanel vSpacer1;
    private JSeparator narctowlSeparator;
    private JPanel vSpacer2;
    private JButton selectFolderButton;
    private JTextField selectFolderTextField;
    private JLabel selectFolderLabel;
    private JButton packNarcButton;
    private JPanel compressionPanel;
    private JPanel vSpacer5;
    private JButton compressionSelectFileButton;
    private JTextField compressionSelectFileTextField;
    private JLabel compressionSelectFileLabel;
    private JButton decompressButton;
    private JButton compressButton;
    private JLabel authorLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    public JPanel getNarctowlPanel()
    {
        return narctowlPanel;
    }

    public JPanel getCompressionPanel()
    {
        return compressionPanel;
    }

//    public static class FolderFilter extends FileFilter{
//
//        @Override
//        public boolean accept(File f)
//        {
//            return f.isDirectory();
//        }
//
//        @Override
//        public String getDescription()
//        {
//            return "Directory";
//        }
//    }
//
//    private static int indexOf(String str, String... arr)
//    {
//        for(int i= 0; i < arr.length; i++)
//        {
//            if(arr[i].equals(str))
//                return i;
//        }
//        return -1;
//    }
//
//    private static void updater(String link) throws IOException
//    {
//        InputStream in= PokEditor.class.getResourceAsStream("/Updater.zip");
//        byte[] buffer= new byte[1024];
//        ZipInputStream zipIn= new ZipInputStream(in);
//        ZipEntry zipEntry= zipIn.getNextEntry();
//
//        while(zipEntry != null)
//        {
//            if(!zipEntry.getName().contains("__"))
//            {
//                File outputFile= ProjectWindow.newFile(new File(path),zipEntry);
//
//                if(!outputFile.getName().contains("._"))
//                {
//                    System.out.println("Entry: " + zipEntry.getName());
//                    System.out.println("    " + outputFile.getName());
//                    FileOutputStream outputStream= new FileOutputStream(outputFile);
//                    int len;
//                    while ((len = zipIn.read(buffer)) > 0)
//                    {
//                        outputStream.write(buffer, 0, len);
//                    }
//                    outputStream.close();
//                }
//            }
//            zipEntry= zipIn.getNextEntry();
//        }
//        zipIn.closeEntry();
//        zipIn.close();
//
//        ProcessBuilder pb = new ProcessBuilder(path, "java -jar", "Updater.jar",link);
//        pb.directory(new File(path));
//        Process p = pb.start();
//        System.exit(0);
//    }
//
//    private static boolean checkNewer(String localVer, String onlineVer)
//    {
//        localVer= localVer.replace("v","");
//        onlineVer= onlineVer.replace("v","");
//
//        String[] local= localVer.split("\\.");
//        String[] online= onlineVer.split("\\.");
//
//        System.out.println("Local: " + localVer);
//        System.out.println("Online: " + onlineVer);
//
//        int max= Math.max(local.length, online.length);
//
//        for(int i= 0; i < max; i++)
//        {
//            int localNum;
//            int onlineNum;
//
//            try
//            { localNum= Integer.parseInt(local[i]); }
//            catch (ArrayIndexOutOfBoundsException e)
//            { return true; }
//
//            try
//            { onlineNum= Integer.parseInt(online[i]); }
//            catch (ArrayIndexOutOfBoundsException e)
//            { return false; }
//
//            if(onlineNum > localNum)
//            { return true; }
//            else if(localNum > onlineNum)
//            { return false; }
//        }
//        return false;
//    }
}
