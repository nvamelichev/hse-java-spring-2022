package ru.hse.java.sql.somedb;

import com.google.common.base.CaseFormat;
import ru.hse.java.filter.Filter;
import ru.hse.java.sql.Repository;
import ru.hse.java.sql.RepositoryException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class SomeDbRepository<T> implements Repository<T> {
    private final String tableName;

    public SomeDbRepository(@Nonnull String tableName) {
        this.tableName = requireNonNull(tableName, "tableName");
    }

    @Nonnull
    @Override
    public Repository<T> add(@Nonnull T element) {
        System.out.printf("INSERT INTO %s VALUES (%s);%n",
                quoteTableName(),
                Arrays.stream(element.getClass().getRecordComponents())
                        .map(rc -> quoteValue(fieldValue(element, rc)))
                        .collect(joining(", ")));

        // TODO really execute INSERT query
        return this;
    }

    private String quoteTableName() {
        return tableName;
    }

    @Nonnull
    @Override
    public List<T> query(@Nullable Filter<T> filter, int limit) {
        System.out.printf("""
                        SELECT * FROM %s
                        WHERE %s
                        LIMIT %d;
                        %n""",
                quoteTableName(), toSql(filter == null ? Filter.alwaysTrue() : filter), limit);

        // TODO really execute the query, not just print it
        throw new UnsupportedOperationException();
    }

    private String toSql(@Nonnull Filter<T> filter) {
        return filter.visit(new Filter.Visitor<>() {
            @Override
            public String visitTrue(Filter.True<T> t) {
                return "(1 = 1)";
            }

            @Override
            public String visitFalse(Filter.False<T> f) {
                return "(1 = 0)";
            }

            @Override
            public String visitNullRel(Filter.NullRel<T> rel) {
                return switch (rel.getType()) {
                    case IS_NULL -> quoteFieldName(rel.getField()) + " IS NULL";
                    case IS_NOT_NULL -> quoteFieldName(rel.getField()) + " IS NOT NULL";
                };
            }

            @Override
            public String visitValueRel(Filter.ValueRel<T, ?> rel) {
                return switch (rel.getType()) {
                    case EQ -> quoteFieldName(rel.getField()) + " = " + quoteValue(rel.getValue());
                    case NEQ -> quoteFieldName(rel.getField()) + " <> " + quoteValue(rel.getValue());
                };
            }

            @Override
            public String visitAnd(Filter.And<T> and) {
                return and.children().stream()
                        .map(c -> "(" + c.visit(this) + ")")
                        .collect(joining(" AND "));
            }

            @Override
            public String visitOr(Filter.Or<T> or) {
                return or.children().stream()
                        .map(c -> "(" + c.visit(this) + ")")
                        .collect(joining(" OR "));
            }

            @Override
            public String visitNot(Filter.Not<T> not) {
                return "NOT(" + not.delegate().visit(this) + ")";
            }
        });
    }

    private static String quoteFieldName(RecordComponent rel) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, rel.getName());
    }

    private static String quoteValue(@Nullable Object value) {
        if (value == null) {
            return "NULL";
        } else if (String.class.equals(value.getClass())) {
            return "'" + value + "'";
        } else if (Enum.class.isAssignableFrom(value.getClass())) {
            return "'" + ((Enum<?>) value).name() + "'";
        } else if (Number.class.isAssignableFrom(value.getClass())) {
            return value.toString();
        } else if (Boolean.class.equals(value.getClass())) {
            return ((Boolean) value) ? "TRUE" : "FALSE";
        } else if (Record.class.isAssignableFrom(value.getClass())) {
            RecordComponent[] cmps = value.getClass().getRecordComponents();
            if (cmps.length != 1) {
                throw new IllegalArgumentException("Only one-field Record wrappers are supported, but got " + cmps.length + " fields");
            }
            return quoteValue(fieldValue(value, cmps[0]));
        } else {
            throw new IllegalArgumentException("Don't know how to handle value: " + value);
        }
    }

    private static Object fieldValue(@Nonnull Object entity, @Nonnull RecordComponent field) {
        try {
            return field.getAccessor().invoke(entity);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access record accessor: " + e.getMessage(), e);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Could not call record accessor: " + ite.getMessage(), ite);
        }
    }

    @Override
    public void close() throws RepositoryException {
        // NOOP for now...
    }
}
