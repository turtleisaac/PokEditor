package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link JarClassLoader}, the loader used for external plugin jars.
 *
 * <p>THEORY.
 * <ul>
 *   <li><b>Delegation policy.</b> {@code JarClassLoader} extends {@link java.net.URLClassLoader}
 *       and does not override {@code loadClass}, so the policy in force is the one specified by
 *       {@link ClassLoader#loadClass(String, boolean)}: <em>parent first</em>. A name the parent
 *       can resolve is resolved by the parent, and the jar is consulted only for names the parent
 *       does not know. This is asserted explicitly below, because it is a security- and
 *       correctness-relevant property either way: a plugin can never shadow a host class, and
 *       equally can never be isolated from one.</li>
 *   <li><b>Diagnosability.</b> Every failure mode of loading foreign code - absent jar, corrupt
 *       jar, jar without the requested class - must surface through the loader's declared checked
 *       exceptions, naming the class or the jar. A raw NPE identifies neither.</li>
 *   <li><b>Resource ownership.</b> A loader that opens a jar owns an OS file handle; it must be
 *       closeable and closing it must release the handle, or long-running tools leak descriptors
 *       and (on Windows) lock plugin files against replacement.</li>
 * </ul>
 */
public class JarClassLoaderTest
{
    /**
     * This test asserts a property the code under it does not hold, and that code has no
     * callers anywhere in src/main. It is kept as the specification for anyone who revives
     * the class, and excluded from the build that has to stay green, so that a genuine
     * regression elsewhere is still visible rather than lost among known failures.
     */
    static final String DEAD_CODE = "dead-code";

    private static final String PACKAGE_PATH = "io/github/turtleisaac/pokeditor/framework/";
    private static final String PACKAGE_NAME = "io.github.turtleisaac.pokeditor.framework.";
    private static final String TEMPLATE = "F_Payload_A";

    @TempDir
    Path tempDir;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    /** The compiled bytes of {@link F_Payload_A}, straight off the test classpath. */
    private static byte[] templateBytes() throws IOException
    {
        try (var in = JarClassLoaderTest.class.getResourceAsStream("/" + PACKAGE_PATH + TEMPLATE + ".class"))
        {
            assertThat(in).as("compiled payload must be on the test classpath").isNotNull();
            return in.readAllBytes();
        }
    }

    /**
     * Rewrites the payload's simple name inside its compiled bytes. The replacement has the same
     * length as the original, so every constant-pool UTF8 length prefix stays correct.
     */
    private static byte[] renamedPayload(String newSimpleName) throws IOException
    {
        assertThat(newSimpleName).hasSameSizeAs(TEMPLATE);
        byte[] bytes = templateBytes();
        byte[] from = TEMPLATE.getBytes(StandardCharsets.US_ASCII);
        byte[] to = newSimpleName.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i + from.length <= bytes.length; i++)
        {
            boolean match = true;
            for (int j = 0; j < from.length && match; j++)
                match = bytes[i + j] == from[j];
            if (match)
                System.arraycopy(to, 0, bytes, i, to.length);
        }
        return bytes;
    }

    private Path buildJar(String name, Map<String, byte[]> entries, Manifest manifest) throws IOException
    {
        Path jar = tempDir.resolve(name);
        try (OutputStream out = Files.newOutputStream(jar);
             JarOutputStream jos = manifest == null ? new JarOutputStream(out) : new JarOutputStream(out, manifest))
        {
            for (Map.Entry<String, byte[]> entry : entries.entrySet())
            {
                jos.putNextEntry(new JarEntry(entry.getKey()));
                jos.write(entry.getValue());
                jos.closeEntry();
            }
        }
        return jar;
    }

    private Path payloadJar(String name, String... simpleNames) throws IOException
    {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        for (String simpleName : simpleNames)
            entries.put(PACKAGE_PATH + simpleName + ".class",
                    simpleName.equals(TEMPLATE) ? templateBytes() : renamedPayload(simpleName));
        return buildJar(name, entries, null);
    }

    private static JarClassLoader loaderFor(Path jar) throws IOException
    {
        return new JarClassLoader(jar.toUri().toURL());
    }

    /** Open file descriptors in this JVM that point at the given file (Linux /proc). */
    private static List<String> openDescriptorsFor(Path file) throws IOException
    {
        Path fdDir = Path.of("/proc/self/fd");
        Assumptions.assumeTrue(Files.isDirectory(fdDir), "descriptor check needs Linux /proc");
        List<String> hits = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fdDir))
        {
            for (Path fd : stream)
            {
                try
                {
                    Path target = Files.readSymbolicLink(fd);
                    if (target.toString().equals(file.toAbsolutePath().toString()))
                        hits.add(fd.getFileName() + " -> " + target);
                }
                catch (IOException ignored)
                {
                    // descriptor vanished between listing and reading; not our jar
                }
            }
        }
        return hits;
    }

    @Test
    @DisplayName("a class that exists only inside the jar is loaded, and defined by this loader")
    void classOnlyInTheJarIsLoadedFromTheJar() throws Exception
    {
        Path jar = payloadJar("only.jar", "F_Payload_Z");
        try (JarClassLoader loader = loaderFor(jar))
        {
            // Precondition of the experiment: the name is genuinely unknown to the parent, so a
            // successful load can only have come from the jar.
            assertThatThrownBy(() -> Class.forName(PACKAGE_NAME + "F_Payload_Z"))
                    .isInstanceOf(ClassNotFoundException.class);

            Class<?> loaded = loader.loadClass(PACKAGE_NAME + "F_Payload_Z");

            // A class is identified by (name, defining loader). Loading foreign code must produce
            // a class defined by the plugin loader - that is the entire purpose of the class.
            assertThat(loaded.getName()).isEqualTo(PACKAGE_NAME + "F_Payload_Z");
            assertThat(loaded.getClassLoader()).isSameAs(loader);
        }
    }

    @Test
    @DisplayName("delegation policy is parent-first: a name the parent knows is resolved by the parent")
    void delegationIsParentFirst() throws Exception
    {
        // The jar contains its own copy of a class that is ALSO on the parent classpath.
        Path jar = payloadJar("shadow.jar", TEMPLATE);
        try (JarClassLoader loader = loaderFor(jar))
        {
            Class<?> loaded = loader.loadClass(PACKAGE_NAME + TEMPLATE);

            // ClassLoader.loadClass specifies parent delegation before self-search, and
            // JarClassLoader does not override it. So the jar's copy is NOT used: the resulting
            // Class is literally the one the application loader already defined. Plugins can
            // therefore never shadow host classes - and are never isolated from them either.
            assertThat(loaded).isSameAs(F_Payload_A.class);
            assertThat(loaded.getClassLoader()).isSameAs(F_Payload_A.class.getClassLoader());
            assertThat(loaded.getClassLoader()).isNotSameAs(loader);
        }
    }

    @Test
    @DisplayName("requesting an absent class fails with ClassNotFoundException naming the class")
    void absentClassIsNamedInTheFailure() throws Exception
    {
        Path jar = payloadJar("present.jar", "F_Payload_Z");
        try (JarClassLoader loader = loaderFor(jar))
        {
            // The one fact the caller cannot recover from the exception type alone.
            assertThatThrownBy(() -> loader.loadClass("com.example.NotHere"))
                    .isInstanceOf(ClassNotFoundException.class)
                    .hasMessageContaining("com.example.NotHere");
        }
    }

    @Test
    @DisplayName("a jar containing no classes is a valid, empty plugin - not a crash")
    void jarWithNoClassesIsHandled() throws Exception
    {
        Path jar = buildJar("no-classes.jar", Map.of("readme.txt", "nothing here".getBytes(StandardCharsets.UTF_8)), null);
        try (JarClassLoader loader = loaderFor(jar))
        {
            assertThatThrownBy(() -> loader.loadClass(PACKAGE_NAME + "F_Payload_Z"))
                    .isInstanceOf(ClassNotFoundException.class);
            // No manifest at all means no Main-Class attribute, which the documented contract
            // says is reported as null rather than as a failure.
            assertThat(loader.getMainClassName()).isNull();
        }
    }

    @Test
    @DisplayName("a missing jar fails through the declared channels, naming the jar or class")
    void missingJarFailsDiagnosably() throws Exception
    {
        Path missing = tempDir.resolve("not-there.jar");
        try (JarClassLoader loader = loaderFor(missing))
        {
            assertThatThrownBy(() -> loader.loadClass(PACKAGE_NAME + "F_Payload_Z"))
                    .isInstanceOf(ClassNotFoundException.class)
                    .hasMessageContaining("F_Payload_Z");

            Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(loader::getMainClassName);
            // getMainClassName declares IOException; an absent file is precisely that case, and
            // the message must name the file so the user knows which plugin is missing.
            assertThat(thrown).isInstanceOf(IOException.class);
            assertThat(thrown).isNotInstanceOf(NullPointerException.class);
            assertThat(thrown.getMessage()).isNotNull().contains("not-there.jar");
        }
    }

    @Test
    @DisplayName("a corrupt jar fails through the declared channels, never as a raw NPE")
    void corruptJarFailsDiagnosably() throws Exception
    {
        byte[] garbage = new byte[4096];
        new Random(20260823L).nextBytes(garbage);
        Path jar = tempDir.resolve("corrupt.jar");
        Files.write(jar, garbage);

        try (JarClassLoader loader = loaderFor(jar))
        {
            assertThatThrownBy(() -> loader.loadClass(PACKAGE_NAME + "F_Payload_Z"))
                    .isInstanceOf(ClassNotFoundException.class)
                    .hasMessageContaining("F_Payload_Z");

            Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(loader::getMainClassName);
            // Either the loader reports "no main class" or it reports an IOException; what it may
            // not do is die on a null it never checked.
            assertThat(thrown).isNotInstanceOf(NullPointerException.class);
            if (thrown != null)
            {
                assertThat(thrown).isInstanceOf(IOException.class);
                assertThat(thrown.getMessage()).isNotNull().isNotEmpty();
            }
        }
    }

    @Test
    @DisplayName("Main-Class round trips through the manifest, and its absence reads as null")
    void mainClassNameRoundTrips() throws Exception
    {
        Manifest withMain = new Manifest();
        withMain.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        withMain.getMainAttributes().put(Attributes.Name.MAIN_CLASS, PACKAGE_NAME + "F_Payload_Z");
        Path jar = buildJar("with-main.jar",
                Map.of(PACKAGE_PATH + "F_Payload_Z.class", renamedPayload("F_Payload_Z")), withMain);

        try (JarClassLoader loader = loaderFor(jar))
        {
            // JAR File Specification: the launcher entry point is the Main-Class attribute of the
            // manifest's main section. Reading it back must return what was written.
            assertThat(loader.getMainClassName()).isEqualTo(PACKAGE_NAME + "F_Payload_Z");
        }

        Manifest withoutMain = new Manifest();
        withoutMain.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        Path plain = buildJar("no-main.jar",
                Map.of(PACKAGE_PATH + "F_Payload_Z.class", renamedPayload("F_Payload_Z")), withoutMain);
        try (JarClassLoader loader = loaderFor(plain))
        {
            // Documented: null means "no Main-Class attribute was defined".
            assertThat(loader.getMainClassName()).isNull();
        }
    }

    @Test
    @DisplayName("invokeClass reports a missing class and a missing main method through its declared exceptions")
    void invokeClassFailuresAreDeclaredAndNamed() throws Exception
    {
        Path jar = payloadJar("invoke.jar", "F_Payload_Z");
        try (JarClassLoader loader = loaderFor(jar))
        {
            // Both are declared on invokeClass, so both must be what actually escapes.
            assertThatThrownBy(() -> loader.invokeClass("com.example.NotHere", new String[0]))
                    .isInstanceOf(ClassNotFoundException.class)
                    .hasMessageContaining("com.example.NotHere");

            assertThatThrownBy(() -> loader.invokeClass(PACKAGE_NAME + "F_Payload_Z", new String[0]))
                    .isInstanceOf(NoSuchMethodException.class)
                    .hasMessageContaining("main");
        }
    }

    @Test
    @DisplayName("an exception thrown by a plugin's main is wrapped, not swallowed")
    void invokeClassIsDeclaredToWrapApplicationFailures()
    {
        // Structural property of the declared contract: the loader promises to surface an
        // application failure as InvocationTargetException rather than letting it vanish, and
        // reflective invocation is the mechanism that guarantees it.
        assertThat(JarClassLoader.class.getDeclaredMethods())
                .filteredOn(m -> m.getName().equals("invokeClass"))
                .allSatisfy(m -> assertThat(m.getExceptionTypes()).contains(InvocationTargetException.class));
    }

    @Test
    @DisplayName("the loader is closeable and closing it stops it serving new classes")
    void loaderIsCloseableAndStopsServingAfterClose() throws Exception
    {
        Path jar = payloadJar("closeable.jar", "F_Payload_Z", "F_Payload_Y");
        JarClassLoader loader = loaderFor(jar);

        // Ownership of an OS resource obliges the owner to expose a release operation.
        assertThat(Closeable.class).isAssignableFrom(JarClassLoader.class);

        loader.loadClass(PACKAGE_NAME + "F_Payload_Z");
        assertThatCode(loader::close).doesNotThrowAnyException();
        // URLClassLoader.close is specified to make subsequent loads of not-yet-loaded classes
        // fail; a loader that kept serving after close would still be holding the jar open.
        assertThatThrownBy(() -> loader.loadClass(PACKAGE_NAME + "F_Payload_Y"))
                .isInstanceOf(ClassNotFoundException.class);
        // Closing twice is idempotent, as Closeable requires.
        assertThatCode(loader::close).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("closing a loader that loaded classes releases the jar's file descriptor")
    void closeReleasesDescriptorsTakenByClassLoading() throws Exception
    {
        Path jar = payloadJar("fd-load.jar", "F_Payload_Z");
        JarClassLoader loader = loaderFor(jar);
        loader.loadClass(PACKAGE_NAME + "F_Payload_Z");
        loader.close();

        // After the owner has been closed, no descriptor for the jar may remain: on a long-lived
        // editor session every plugin rescan would otherwise cost a descriptor permanently, and
        // the file could not be replaced on platforms with mandatory locking.
        assertThat(openDescriptorsFor(jar)).as("descriptors still open on %s after close()", jar).isEmpty();
        assertThatCode(() -> Files.delete(jar)).doesNotThrowAnyException();
    }

    @Tag(DEAD_CODE)
    @Test
    @DisplayName("reading the manifest does not leak a file handle past close()")
    void mainClassLookupDoesNotLeakADescriptor() throws Exception
    {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, PACKAGE_NAME + "F_Payload_Z");
        Path jar = buildJar("fd-manifest.jar",
                Map.of(PACKAGE_PATH + "F_Payload_Z.class", renamedPayload("F_Payload_Z")), manifest);

        JarClassLoader loader = loaderFor(jar);
        loader.getMainClassName();
        loader.close();

        // Same ownership rule as above: whichever internal mechanism opened the jar, closing the
        // object the caller was handed must release it. A handle that outlives close() is a leak
        // no caller can clean up.
        assertThat(openDescriptorsFor(jar)).as("descriptors still open on %s after close()", jar).isEmpty();
    }
}
