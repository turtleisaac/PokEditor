package io.github.turtleisaac.pokeditor.gui.sheets;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Lends the running JVM a working system clipboard for the duration of one action.
 * <p>
 * The paste action reads {@code Toolkit.getDefaultToolkit().getSystemClipboard()} directly, and
 * a headless toolkit has no clipboard to give - it throws. Rather than change production code to
 * open a seam, this swaps in a toolkit which delegates everything to the real one except the
 * clipboard, which becomes an ordinary in-process {@link Clipboard}. The swap lasts only as long
 * as the action being tested and is always undone.
 */
final class SystemClipboardStub
{
    private SystemClipboardStub() {}

    /**
     * Runs {@code action} with {@code contents} on the system clipboard.
     *
     * @param contents the clipboard text, in the tab and newline separated form a spreadsheet copy produces
     */
    static void withClipboardContents(String contents, Runnable action)
    {
        // force the focus manager and the real toolkit into existence first: parts of AWT
        // require the platform toolkit's own type, and can only be initialised while it is installed
        Toolkit real = Toolkit.getDefaultToolkit();
        KeyboardFocusManager.getCurrentKeyboardFocusManager();

        Field toolkitField = toolkitField();
        DelegatingToolkit stub = new DelegatingToolkit(real);
        stub.clipboard.setContents(new StringSelection(contents), null);

        try {
            set(toolkitField, stub);
            action.run();
        }
        finally {
            set(toolkitField, real);
        }
    }

    private static Field toolkitField()
    {
        try {
            Field field = Toolkit.class.getDeclaredField("toolkit");
            field.setAccessible(true);
            return field;
        }
        catch (NoSuchFieldException | RuntimeException e) {
            throw new AssertionError("cannot reach java.awt.Toolkit's default instance to lend the test a clipboard; "
                    + "the surefire configuration must pass --add-opens java.desktop/java.awt=ALL-UNNAMED", e);
        }
    }

    private static void set(Field field, Toolkit value)
    {
        try {
            field.set(null, value);
        }
        catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /** Everything is the real toolkit's answer except {@link #getSystemClipboard()}. */
    private static final class DelegatingToolkit extends Toolkit
    {
        private final Toolkit delegate;
        private final Clipboard clipboard = new Clipboard("test system clipboard");

        private DelegatingToolkit(Toolkit delegate) { this.delegate = delegate; }

        @Override public Clipboard getSystemClipboard() { return clipboard; }

        @Override public Dimension getScreenSize() { return delegate.getScreenSize(); }
        @Override public int getScreenResolution() { return delegate.getScreenResolution(); }
        @Override public ColorModel getColorModel() { return delegate.getColorModel(); }
        @SuppressWarnings("deprecation") @Override public String[] getFontList() { return delegate.getFontList(); }
        @SuppressWarnings("deprecation") @Override public FontMetrics getFontMetrics(Font font) { return delegate.getFontMetrics(font); }
        @Override public void sync() { delegate.sync(); }
        @Override public Image getImage(String filename) { return delegate.getImage(filename); }
        @Override public Image getImage(URL url) { return delegate.getImage(url); }
        @Override public Image createImage(String filename) { return delegate.createImage(filename); }
        @Override public Image createImage(URL url) { return delegate.createImage(url); }
        @Override public Image createImage(ImageProducer producer) { return delegate.createImage(producer); }
        @Override public Image createImage(byte[] data, int offset, int length) { return delegate.createImage(data, offset, length); }
        @Override public boolean prepareImage(Image image, int w, int h, ImageObserver o) { return delegate.prepareImage(image, w, h, o); }
        @Override public int checkImage(Image image, int w, int h, ImageObserver o) { return delegate.checkImage(image, w, h, o); }
        @Override public PrintJob getPrintJob(Frame frame, String title, Properties props) { return delegate.getPrintJob(frame, title, props); }
        @Override public void beep() { delegate.beep(); }
        @Override protected EventQueue getSystemEventQueueImpl() { return delegate.getSystemEventQueue(); }
        @Override public boolean isModalityTypeSupported(Dialog.ModalityType type) { return delegate.isModalityTypeSupported(type); }
        @Override public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType type) { return delegate.isModalExclusionTypeSupported(type); }
        @SuppressWarnings("deprecation") @Override public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) { return delegate.mapInputMethodHighlight(highlight); }
    }
}
