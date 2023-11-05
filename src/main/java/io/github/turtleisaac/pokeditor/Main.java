/*
 * Created by JFormDesigner on Wed Dec 23 14:00:53 EST 2020
 */

package io.github.turtleisaac.pokeditor;

import javax.swing.*;

import com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme;
import com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme;
import io.github.turtleisaac.nds4j.ui.ProgramType;
import io.github.turtleisaac.nds4j.ui.Tool;
import io.github.turtleisaac.nds4j.ui.exceptions.ToolCreationException;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.console.ConsoleWindow;

import java.util.Locale;
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
//        Locale.setDefault(Locale.FRANCE);
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
    }
}
