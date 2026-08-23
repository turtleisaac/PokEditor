package io.github.turtleisaac.pokeditor.framework;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for {@link SheetExceptionFactory} and the exception types it produces.
 *
 * <p>THEORY. A diagnostic factory is an injection from a fault description into a message: the
 * message is the only artefact that survives to the user, so every argument the caller was asked
 * to supply must be recoverable from it. If a parameter cannot be observed in the output, the
 * caller was made to compute a value for nothing and the user is shown an incomplete diagnosis.
 *
 * <p>The factory methods are enumerated reflectively rather than by hand, so a factory method
 * added tomorrow is covered by these properties automatically.
 */
public class SheetExceptionFactoryTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static List<Method> factoryMethods()
    {
        List<Method> methods = new ArrayList<>();
        for (Method method : SheetExceptionFactory.class.getDeclaredMethods())
        {
            if (method.isSynthetic() || !Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers()))
                continue;
            if (!Throwable.class.isAssignableFrom(method.getReturnType()))
                continue;
            methods.add(method);
        }
        methods.sort((a, b) -> a.getName().compareTo(b.getName()));
        return methods;
    }

    /** A distinct, greppable argument value per parameter position, so a drop is attributable. */
    private static Object argumentFor(Class<?> type, int position)
    {
        if (type == Class.class)
            return SheetExceptionFactoryTest.class;
        if (type == String.class)
            return "MARKER" + position + "VALUE";
        if (type == int.class || type == Integer.class)
            return 4200 + position;
        if (type == long.class || type == Long.class)
            return 4200L + position;
        if (type == boolean.class || type == Boolean.class)
            return Boolean.TRUE;
        throw new IllegalStateException("no argument recipe for parameter type " + type
                + "; extend argumentFor() so the reflective coverage stays complete");
    }

    /** The text the caller must be able to find in the message for the argument at this position. */
    private static String expectedTextFor(Object argument)
    {
        if (argument instanceof Class<?> clazz)
            return clazz.getSimpleName();
        return String.valueOf(argument);
    }

    @Test
    @DisplayName("the reflective enumeration actually finds factory methods")
    void enumerationIsNotEmpty()
    {
        // Guards the tests below from passing vacuously if the class is renamed or emptied.
        assertThat(factoryMethods()).as("public static Throwable-returning factory methods").isNotEmpty();
    }

    @Test
    @DisplayName("every factory method yields a non-null unchecked SheetException with a message")
    void everyFactoryMethodIsTotal() throws Exception
    {
        SoftAssertions soft = new SoftAssertions();
        for (Method method : factoryMethods())
        {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++)
                arguments[i] = argumentFor(parameterTypes[i], i);

            Object produced = method.invoke(null, arguments);

            // Totality: a factory whose whole job is to build a diagnostic may never return null,
            // and an exception with no message is a dead end for whoever has to read the log.
            soft.assertThat(produced).as("%s must produce an exception", method.getName()).isNotNull();
            if (produced == null)
                continue;

            soft.assertThat(produced).as("%s return type", method.getName()).isInstanceOf(SheetException.class);
            // SheetException extends RuntimeException, so it is unchecked; callers rely on being
            // able to throw it from the Swing/table code paths that declare no checked exceptions.
            soft.assertThat(produced).as("%s must be unchecked", method.getName()).isInstanceOf(RuntimeException.class);
            soft.assertThat(((Throwable) produced).getMessage())
                    .as("%s message", method.getName())
                    .isNotNull()
                    .isNotEmpty();
        }
        soft.assertAll();
    }

    @Test
    @DisplayName("every argument a factory method demands appears in the message it produces")
    void everyArgumentIsRecoverableFromTheMessage() throws Exception
    {
        SoftAssertions soft = new SoftAssertions();
        for (Method method : factoryMethods())
        {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++)
                arguments[i] = argumentFor(parameterTypes[i], i);

            String message = ((Throwable) method.invoke(null, arguments)).getMessage();

            for (int i = 0; i < arguments.length; i++)
            {
                // An argument that never reaches the message is information the caller was forced
                // to gather and the user never gets to see - the offending value, the row number
                // or the kind of value are exactly what makes a spreadsheet error actionable.
                soft.assertThat(message)
                        .as("%s: parameter #%d (%s %s) must appear in the message <%s>",
                                method.getName(), i, parameterTypes[i].getSimpleName(),
                                method.getParameters()[i].getName(), message)
                        .contains(expectedTextFor(arguments[i]));
            }
        }
        soft.assertAll();
    }

    @Test
    @DisplayName("the message identifies the reporting editor and the line number distinctly")
    void messageIdentifiesEditorAndLine()
    {
        SheetException e = SheetExceptionFactory.generateInvalidNameSheetException(
                SheetExceptionFactoryTest.class, "move", "Hyprr Beam", "species", "Bulbasaur", 37);

        // The user is looking at a spreadsheet: without the editor name and the row number the
        // message cannot be acted on, regardless of how the rest of the sentence is worded.
        assertThat(e.getMessage()).contains("SheetExceptionFactoryTest");
        assertThat(e.getMessage()).contains("37");
        assertThat(e.getMessage()).contains("Hyprr Beam");
    }

    @Test
    @DisplayName("trimming the factory frame removes exactly one frame and fabricates none")
    void stackTraceTrimRemovesExactlyOneFrame()
    {
        SheetException produced = SheetExceptionFactory.generateMissingValueSheetException(
                SheetExceptionFactoryTest.class, "move", "level", "Tackle", "species", "Bulbasaur", 12);
        Throwable reference = new Throwable();

        StackTraceElement[] trimmed = produced.getStackTrace();
        StackTraceElement[] expected = reference.getStackTrace();

        // Both throwables are created in this method, so their traces share the same suffix; the
        // factory's own frame sits on top of the produced one. Deleting that frame must shorten
        // the trace by exactly one element - a trace that keeps its length is claiming a call that
        // never happened, and a duplicated frame misdirects whoever reads the log.
        assertThat(trimmed.length).as("frame count after trimming the factory frame").isEqualTo(expected.length);

        // The deepest frame of a thread's stack has no caller below it, so it cannot appear twice
        // in a row: a trace ending in two identical frames has had one fabricated by the trim.
        assertThat(trimmed[trimmed.length - 1])
                .as("the deepest frame must not be duplicated")
                .isNotEqualTo(trimmed[trimmed.length - 2]);

        // The frame that was removed must be the factory's own, and it must be the only one gone.
        assertThat(trimmed[0].getClassName()).isEqualTo(SheetExceptionFactoryTest.class.getName());
        assertThat(trimmed).noneMatch(frame -> frame.getClassName().equals(SheetExceptionFactory.class.getName()));
    }

    @Test
    @DisplayName("the sheet exception types are unchecked and preserve their message")
    void exceptionTypesArePlainCarriers()
    {
        // Identity law for a message carrier: what goes into the constructor comes out of
        // getMessage() unchanged.
        assertThat(new SheetException("boom").getMessage()).isEqualTo("boom");
        assertThat(new InvalidStringException("bad string").getMessage()).isEqualTo("bad string");

        // Both are unchecked, matching how the sheet/table code throws them from methods that
        // declare no checked exceptions.
        assertThat(RuntimeException.class).isAssignableFrom(SheetException.class);
        assertThat(RuntimeException.class).isAssignableFrom(InvalidStringException.class);
    }
}
