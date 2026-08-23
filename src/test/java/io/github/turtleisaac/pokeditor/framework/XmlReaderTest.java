package io.github.turtleisaac.pokeditor.framework;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Property-based tests for {@link XmlReader}.
 *
 * <p>THEORY. A reader for a documented format is a partial function from byte sequences to values,
 * and it owes its callers two things:
 * <ul>
 *   <li><b>Totality of failure.</b> For every input outside the accepted language the reader must
 *       fail through its declared channel - here {@code throws IOException} - carrying a message.
 *       A {@code NullPointerException}, an index-out-of-bounds or a {@code StackOverflowError}
 *       escaping from a parser means the input drove the implementation off its own rails, and
 *       tells the caller nothing about which input was bad.</li>
 *   <li><b>Fidelity.</b> For every document in the accepted language, the values read back must be
 *       the values the document states. The expectations below are written from the XML 1.0
 *       grammar (an element is {@code <Name>content</Name>}; end tags are spelled with a forward
 *       slash, XML 1.0 s.3.1), not from the implementation.</li>
 * </ul>
 */
public class XmlReaderTest
{
    @TempDir
    Path tempDir;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private Path write(String name, String content) throws IOException
    {
        Path path = tempDir.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    /** The hostile-input battery. Keys are case names so a failure names the offending input. */
    private Map<String, byte[]> hostileInputs()
    {
        Map<String, byte[]> cases = new LinkedHashMap<>();
        cases.put("empty", "".getBytes(StandardCharsets.UTF_8));
        cases.put("whitespace-only", "   \n \t \n".getBytes(StandardCharsets.UTF_8));
        cases.put("blank-line-where-root-belongs", "<?xml version=\"1.0\"?>\n\n<a>1</a>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("unclosed-root", "<?xml version=\"1.0\"?>\n<root>\n  <a>1</a>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("mismatched-tags", "<?xml version=\"1.0\"?>\n<root>\n  <a>1</b>\n</root>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("unescaped-ampersand", "<?xml version=\"1.0\"?>\n<root>\n  <a>Tom & Jerry</a>\n</root>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("duplicate-attribute", "<?xml version=\"1.0\"?>\n<root>\n  <a x=\"1\" x=\"2\">v</a>\n</root>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("truncated-mid-tag", "<?xml version=\"1.0\"?>\n<root>\n  <a>1</a>\n  <b>2</".getBytes(StandardCharsets.UTF_8));
        cases.put("no-markup-at-all", "just some prose\nspread over\nthree lines\n".getBytes(StandardCharsets.UTF_8));
        cases.put("unexpected-root", "<?xml version=\"1.0\"?>\n<totally-different>\n  <a>1</a>\n</totally-different>\n".getBytes(StandardCharsets.UTF_8));
        cases.put("deeply-nested-10000", deeplyNested(10_000).getBytes(StandardCharsets.UTF_8));
        cases.put("binary-garbage", new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0xC3, 0x28, (byte) 0x80,
                (byte) 0xA0, 0x00, 0x01, 0x02, (byte) 0xF8, (byte) 0x88, 0x0A, (byte) 0xED, (byte) 0xA0, (byte) 0x80});
        return cases;
    }

    private static String deeplyNested(int depth)
    {
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>\n");
        for (int i = 0; i < depth; i++)
            sb.append("<n").append(i).append(">\n");
        sb.append("  <leaf>value</leaf>\n");
        for (int i = depth - 1; i >= 0; i--)
            sb.append("</n").append(i).append(">\n");
        return sb.toString();
    }

    @Test
    @DisplayName("hostile input never escapes as a raw runtime failure")
    void hostileInputFailsDiagnosably() throws IOException
    {
        SoftAssertions soft = new SoftAssertions();
        int caseIndex = 0;
        for (Map.Entry<String, byte[]> entry : hostileInputs().entrySet())
        {
            Path path = tempDir.resolve("hostile" + (caseIndex++) + ".xml");
            Files.write(path, entry.getValue());

            Throwable thrown = catchThrowable(() -> new XmlReader(path).readFile());
            if (thrown == null)
                continue; // accepting an input is a fidelity question, checked separately

            // The reader declares IOException; anything else is an implementation detail leaking
            // out. NPE / index-out-of-bounds / StackOverflowError in particular identify no input.
            soft.assertThat(thrown)
                    .as("case '%s' must fail through a declared, diagnosable channel", entry.getKey())
                    .isNotInstanceOf(NullPointerException.class)
                    .isNotInstanceOf(IndexOutOfBoundsException.class)
                    .isNotInstanceOf(StackOverflowError.class);
            soft.assertThat(thrown.getMessage())
                    .as("case '%s' must carry a diagnostic message", entry.getKey())
                    .isNotNull()
                    .isNotEmpty();
        }
        soft.assertAll();
    }

    @Test
    @DisplayName("documents that violate XML well-formedness are rejected, not half-read")
    void notWellFormedDocumentsAreRejected() throws IOException
    {
        Map<String, String> cases = new LinkedHashMap<>();
        // XML 1.0 s.2.1: a well-formed document has exactly one root element whose start tag is
        // matched by an end tag. Returning a value for a document that never closes its root means
        // a truncated file is indistinguishable from a complete one - the caller silently loses
        // every element that was cut off.
        cases.put("unclosed-root", "<?xml version=\"1.0\"?>\n<root>\n  <a>1</a>\n");
        cases.put("truncated-mid-element", "<?xml version=\"1.0\"?>\n<root>\n  <a>1</a>\n  <b>2</");
        cases.put("no-markup-at-all", "just some prose\nspread over\nthree lines\n");

        SoftAssertions soft = new SoftAssertions();
        for (Map.Entry<String, String> entry : cases.entrySet())
        {
            Path path = write(entry.getKey() + ".xml", entry.getValue());
            Throwable thrown = catchThrowable(() -> new XmlReader(path).readFile());
            soft.assertThat(thrown).as("case '%s' must be rejected", entry.getKey()).isNotNull();
        }
        soft.assertAll();
    }

    @Test
    @DisplayName("a well-formed document round trips: every element maps to the text it contains")
    void wellFormedDocumentRoundTrips() throws IOException
    {
        // Written by hand against the XML 1.0 grammar so the expectation comes from the document,
        // not from the parser: a root element "settings" containing three child elements, each
        // holding character data. Child indentation is two spaces, the layout this reader slices
        // its element names out of.
        String document =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<settings>\n" +
                "  <romPath>/home/user/roms/heartgold.nds</romPath>\n" +
                "  <theme>Darcula</theme>\n" +
                "  <lastEditor>Learnsets</lastEditor>\n" +
                "</settings>\n";
        Path path = write("well-formed.xml", document);

        HashMap<String, String> values = new XmlReader(path).readFile();

        // Fidelity: the reader must reproduce the document's own element-to-content mapping.
        assertThat(values).containsEntry("romPath", "/home/user/roms/heartgold.nds");
        assertThat(values).containsEntry("theme", "Darcula");
        assertThat(values).containsEntry("lastEditor", "Learnsets");
    }

    @Test
    @DisplayName("an external entity declared in the DOCTYPE is never resolved")
    void externalEntitiesAreNotResolved() throws IOException
    {
        String canary = "CANARY_" + UUID.randomUUID().toString().replace("-", "") + "_SECRET";
        Path secret = write("secret.txt", canary + "\n");

        // Classic XXE payload (OWASP XML External Entity): if the reader expands SYSTEM entities,
        // the contents of an arbitrary local file end up in the parsed values, which is a file
        // disclosure vulnerability whenever the document is attacker-supplied.
        String document =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE settings [ <!ENTITY xxe SYSTEM \"file://" + secret.toAbsolutePath() + "\"> ]>\n" +
                "<settings>\n" +
                "  <theme>&xxe;</theme>\n" +
                "</settings>\n";
        Path path = write("xxe.xml", document);

        HashMap<String, String> values = null;
        Throwable thrown = null;
        try
        {
            values = new XmlReader(path).readFile();
        }
        catch (Throwable t)
        {
            thrown = t;
        }

        // Whatever the reader does with the document, the secret file's contents must not appear
        // in any value it hands back, nor in the diagnostics it throws.
        if (values != null)
        {
            assertThat(values.values()).as("no parsed value may contain the external file's contents")
                    .noneMatch(v -> v != null && v.contains(canary));
            assertThat(values.keySet()).noneMatch(k -> k != null && k.contains(canary));
        }
        if (thrown != null && thrown.getMessage() != null)
            assertThat(thrown.getMessage()).doesNotContain(canary);
    }

    @Test
    @DisplayName("a 10,000-deep document does not overflow the stack")
    void deepNestingDoesNotOverflowTheStack() throws IOException
    {
        Path path = write("deep.xml", deeplyNested(10_000));
        // Input-proportional recursion is a denial-of-service and an undiagnosable failure mode;
        // document depth is attacker-controlled, so it must not map onto Java stack depth.
        Throwable thrown = catchThrowable(() -> new XmlReader(path).readFile());
        assertThat(thrown).isNotInstanceOf(StackOverflowError.class);
    }

    @Test
    @DisplayName("a missing file fails with an IOException naming the file")
    void missingFileIsReportedWithItsName()
    {
        Path missing = tempDir.resolve("definitely-not-here.xml");
        // The declared failure channel, with the one piece of context the caller cannot recover
        // on its own if the message omits it.
        assertThatThrownBy(() -> new XmlReader(missing).readFile())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("definitely-not-here.xml");
    }
}
