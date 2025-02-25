package no.spk.tidsserie.tidsperiode;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class AarstallProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        return IntStream.range(1997, 2099).mapToObj(aar -> Arguments.of(new Aarstall(aar)));
    }

}
