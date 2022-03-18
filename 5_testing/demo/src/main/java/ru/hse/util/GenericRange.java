package ru.hse.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.UnaryOperator;

public record GenericRange<X extends Comparable<? super X>>
        (X min, X max, UnaryOperator<X> next)
        implements Iterable<X> {
    
    public GenericRange {
        Preconditions.checkArgument(max.compareTo(min) >= 0);
        Objects.requireNonNull(next, "generator must be supplied");
    }

    public boolean isEmpty() {
        return max.compareTo(min) == 0;
    }

    @Nonnull
    @Override
    public Iterator<X> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<X> {
        private X current = min;

        @Override
        public boolean hasNext() {
            return current.compareTo(max) < 0;
        }

        @Override
        public X next() {
            X result = current;
            current = next.apply(current);
            return result;
        }
    }
}
