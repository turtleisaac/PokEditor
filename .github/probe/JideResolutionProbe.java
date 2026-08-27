/**
 * Runs the look-and-feel resolution that jide-oss performs inside every JIDE component, against
 * a built jar, and exits non-zero if it fails.
 *
 * LookAndFeelFactory decides which style to install by asking
 * "lnf instanceof com.sun.java.swing.plaf.windows.WindowsLookAndFeel". An instanceof must
 * resolve its class before it can answer false, so an absent or inaccessible class is a link
 * error thrown out of a constructor rather than a quiet no. The application reaches this
 * whenever someone types into a combo box in a sheet: EditorComboBox installs a
 * ComboBoxSearchable, its search popup is a JidePopup, and JidePopup.updateUI() - which runs
 * from the JComponent constructor - calls installJideExtension().
 *
 * The look and feel is set to FlatLaf first because that is the one the application installs,
 * and because it is the case that matters: JIDE recognises Metal and Aqua and answers from an
 * earlier branch, never reaching the instanceof. A probe left on the default look and feel
 * passes whether or not the class is present, which is no check at all.
 */
public class JideResolutionProbe
{
    public static void main(String[] args)
    {
        System.setProperty("java.awt.headless", "true");
        try {
            javax.swing.UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");
            com.jidesoft.plaf.LookAndFeelFactory.installJideExtension();
        }
        catch (Throwable t) {
            System.out.println("FAIL: " + t.getClass().getName() + ": " + t.getMessage());
            System.exit(1);
        }
        System.out.println("OK: WindowsLookAndFeel resolved and the JIDE extension installed");
    }
}
