package ru.hse.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;

public record Range(int min, int max) implements Iterable<Integer> {
    public Range {
        Preconditions.checkArgument(max >= min);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public long size() {
        return (long) max - (long) min;
    }

    public boolean contains(Range other) {
        if (other.isEmpty()) {
            return true;
        }
        return contains(other.min) && contains(other.max - 1);
    }

    public boolean contains(int n) {
        return n >= min && n < max;
    }

    @Nonnull
    @Override
    public Iterator<Integer> iterator() {
        return new Itr();
    }

    private final class Itr implements Iterator<Integer> {
        private int current = min;

        @Override
        public boolean hasNext() {
            return current < max;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current++;
        }
    }
}
