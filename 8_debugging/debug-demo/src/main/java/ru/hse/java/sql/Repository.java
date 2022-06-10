package ru.hse.java.sql;

import ru.hse.java.filter.Filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Repository<T> extends AutoCloseable {
    @Nonnull
    Repository<T> add(@Nonnull T element);

    @Nonnull
    List<T> query(@Nullable Filter<T> filter, int limit);

    @Override
    void close() throws RepositoryException;
}
