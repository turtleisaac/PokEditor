package io.github.turtleisaac.pokeditor.framework;

public class InvalidStringException extends RuntimeException
{
    public InvalidStringException(String error) {
        super(error);
    }
}
