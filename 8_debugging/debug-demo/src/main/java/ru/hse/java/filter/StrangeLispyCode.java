package ru.hse.java.filter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

public final class StrangeLispyCode {
    private StrangeLispyCode() {
    }

    @Nonnull
    @SafeVarargs
    public static <T> List<T> cons(@Nonnull T first, @Nonnull T... rest) {
        return Stream.concat(Stream.of(first), Stream.of(rest)).toList();
    }
}
