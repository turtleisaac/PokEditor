package io.github.turtleisaac.pokeditor.framework;

/**
 * A deliberately tiny compiled payload used by {@link JarClassLoaderTest}.
 *
 * <p>Its compiled bytes are copied into throwaway jars at test time. The class name is also
 * rewritten in-place inside those bytes (an equal-length substitution, so the constant pool stays
 * valid) to synthesise classes that exist ONLY inside a jar and not on the test classpath - which
 * is what makes an isolation/delegation test meaningful. Keep the class name exactly 11 characters
 * long and keep this class trivial.
 */
public class F_Payload_A
{
    public static String id()
    {
        return "payload";
    }
}
