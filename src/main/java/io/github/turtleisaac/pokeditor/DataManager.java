package io.github.turtleisaac.pokeditor;

import com.google.inject.*;
import com.google.inject.util.Types;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterParser;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterParser;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionParser;
import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.items.ItemParser;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetParser;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveParser;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankParser;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerParser;

import java.lang.reflect.ParameterizedType;

public class DataManager
{
    private static final Injector injector = Guice.createInjector(
            new PersonalModule(),
            new LearnsetsModule(),
            new EvolutionsModule(),
            new TrainersModule(),
            new MovesModule(),
            new SinnohEncountersModule(),
            new JohtoEncountersModule(),
            new ItemsModule(),
            new TextBankModule());

    public static <E extends GenericFileData> GenericParser<E> getParser(Class<E> eClass)
    {
        ParameterizedType type = Types.newParameterizedType(GenericParser.class, eClass);
        return (GenericParser<E>) injector.getInstance(Key.get(TypeLiteral.get(type)));
    }

    static class PersonalModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<PersonalData>>() {})
                    .to(PersonalParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class LearnsetsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<LearnsetData>>() {})
                    .to(LearnsetParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class EvolutionsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<EvolutionData>>() {})
                    .to(EvolutionParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class TrainersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<TrainerData>>() {})
                    .to(TrainerParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class MovesModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<MoveData>>() {})
                    .to(MoveParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class SinnohEncountersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<SinnohEncounterData>>() {})
                    .to(SinnohEncounterParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class JohtoEncountersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<JohtoEncounterData>>() {})
                    .to(JohtoEncounterParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class ItemsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<ItemData>>() {})
                    .to(ItemParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class TextBankModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<TextBankData>>() {})
                    .to(TextBankParser.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
