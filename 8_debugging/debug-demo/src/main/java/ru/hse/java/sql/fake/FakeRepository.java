package ru.hse.java.sql.fake;

import ru.hse.java.filter.Filter;
import ru.hse.java.sql.Repository;
import ru.hse.java.sql.RepositoryException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public final class FakeRepository<T> implements Repository<T> {
    private static final int MAX_LIMIT = 10_000;
    private static final Predicate<?> ALWAYS_TRUE_PREDICATE = __ -> true;
    private static final Predicate<?> ALWAYS_FALSE_PREDICATE = __ -> false;

    private final List<T> all = new ArrayList<>();

    @Nonnull
    @Override
    public Repository<T> add(@Nonnull T element) {
        all.add(requireNonNull(element, "element"));
        return this;
    }

    @Nonnull
    @Override
    public List<T> query(@Nullable Filter<T> filter, int limit) {
        if (limit <= 0 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Bad limit: " + limit);
        }

        return all.stream()
                .filter(toPredicate(filter))
                .limit(limit)
                .toList();
    }

    @SuppressWarnings("unchecked")
    public Predicate<T> toPredicate(@Nullable Filter<T> filter) {
        if (filter == null) {
            return (Predicate<T>) ALWAYS_TRUE_PREDICATE;
        }

        return filter.visit(new Filter.Visitor<>() {
            @Override
            public Predicate<T> visitTrue(Filter.True<T> t) {
                return (Predicate<T>) ALWAYS_TRUE_PREDICATE;
            }

            @Override
            public Predicate<T> visitFalse(Filter.False<T> f) {
                return (Predicate<T>) ALWAYS_FALSE_PREDICATE;
            }

            @Override
            public Predicate<T> visitNullRel(Filter.NullRel<T> rel) {
                return switch (rel.getType()) {
                    case IS_NULL -> t -> fieldValue(t, rel.getField()) == null;
                    case IS_NOT_NULL -> t -> fieldValue(t, rel.getField()) != null;
                };
            }

            @Override
            public Predicate<T> visitValueRel(Filter.ValueRel<T, ?> rel) {
                return switch (rel.getType()) {
                    case EQ -> t -> eq(fieldValue(t, rel.getField()), rel.getValue());
                    case NEQ -> t -> !eq(fieldValue(t, rel.getField()), rel.getValue());
                };
            }

            @Override
            public Predicate<T> visitAnd(Filter.And<T> and) {
                return and.children().stream()
                        .map(c -> c.visit(this))
                        .reduce((Predicate<T>) ALWAYS_TRUE_PREDICATE, Predicate::and);
            }

            @Override
            public Predicate<T> visitOr(Filter.Or<T> or) {
                return or.children().stream()
                        .map(c -> c.visit(this))
                        .reduce((Predicate<T>) ALWAYS_FALSE_PREDICATE, Predicate::or);
            }

            @Override
            public Predicate<T> visitNot(Filter.Not<T> not) {
                return not.delegate().visit(this).negate();
            }
        });
    }

    private Object fieldValue(@Nonnull T entity, @Nonnull RecordComponent field) {
        try {
            return field.getAccessor().invoke(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access record accessor: " + e.getMessage(), e);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Could not call record accessor: " + ite.getMessage(), ite);
        }
    }

    private static boolean eq(@Nullable Object o1, @Nullable Object o2) {
        return Objects.equals(o1, o2);
    }

    @Override
    public void close() throws RepositoryException {
        // intentionally NOOP
    }
}
