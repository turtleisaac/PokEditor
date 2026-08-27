package io.github.turtleisaac.pokeditor.gui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.LookAndFeel;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the one class this application must be able to resolve but never names.
 * <p>
 * jide-oss predates the module system. {@code LookAndFeelFactory} decides which style to
 * install by asking {@code lnf instanceof com.sun.java.swing.plaf.windows.WindowsLookAndFeel},
 * and it reaches that question on the branch taken for every look and feel it does not
 * recognise - which includes FlatLaf, the one this application sets. Every JIDE component runs
 * it, because {@code JidePopup.updateUI()} calls {@code installJideExtension()} and
 * {@code updateUI()} runs from the JComponent constructor. This application gets there whenever
 * someone types into a combo box in a sheet: {@link EditorComboBox} installs a
 * {@code ComboBoxSearchable}, and its search popup is a {@code JidePopup}.
 * <p>
 * An {@code instanceof} must <em>resolve</em> its class before it can answer false, so an
 * absent class is not a quiet no - it is a {@code NoClassDefFoundError} thrown out of a
 * constructor. No JDK ships that package on Linux or macOS, which is what the system-scoped
 * {@code WinLaF.jar} at the repository root exists to supply. Nothing references it by name, so
 * without this test it can be dropped as an obvious piece of dead weight and the failure only
 * appears when a user types a move name.
 * <p>
 * <b>What this test cannot cover.</b> On Windows the JDK <em>does</em> ship the package, inside
 * {@code java.desktop}, which does not export it. Parent-first delegation finds that copy and
 * shadows {@code WinLaF.jar}, so resolution fails there with {@code IllegalAccessError} no
 * matter what is on the classpath; the fix is the {@code Add-Exports} manifest entry written by
 * the {@code dist} profile. A test running inside one JVM cannot check the manifest of a jar
 * that profile has not built yet, so that half is verified by the profile configuration and
 * documented in {@code TECH_DEBT.md} rather than asserted here.
 */
class JideLookAndFeelResolutionTest
{
    private static final String WINDOWS_LAF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    @Test
    @DisplayName("the look and feel class JIDE resolves is on the application's classpath")
    void windowsLookAndFeelIsResolvable() throws Exception
    {
        // the loader JIDE's own classes are defined by, so this asks the question the way the
        // failing instanceof asks it rather than the way a test happens to be launched
        ClassLoader loader = com.jidesoft.swing.ComboBoxSearchable.class.getClassLoader();

        Class<?> laf = Class.forName(WINDOWS_LAF, false, loader);

        assertThat(LookAndFeel.class).as(
                        "%s must be a LookAndFeel - JIDE tests the installed look and feel against it "
                                + "with instanceof, which is a link error rather than a false answer if the "
                                + "class on the classpath is not related to it", WINDOWS_LAF)
                .isAssignableFrom(laf);
    }
}
