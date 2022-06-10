package ru.hse.java.filter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static ru.hse.java.filter.Filter.NullRel.Type.IS_NOT_NULL;
import static ru.hse.java.filter.Filter.NullRel.Type.IS_NULL;
import static ru.hse.java.filter.Filter.ValueRel.Type.EQ;
import static ru.hse.java.filter.Filter.ValueRel.Type.NEQ;
import static ru.hse.java.filter.StrangeLispyCode.cons;

public sealed interface Filter<T> permits Filter.Leaf, Filter.Composite {
    @Nonnull
    @SuppressWarnings("unchecked")
    static <T> Filter<T> alwaysTrue() {
        return (True<T>) True.INSTANCE;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    static <T> Filter<T> alwaysFalse() {
        return (False<T>) False.INSTANCE;
    }

    @Nonnull
    static <T, V> Filter<T> isEqualTo(@Nonnull RecordComponent field, @Nullable V value) {
        return value == null
                ? new NullRel<>(field, IS_NULL)
                : new ValueRel<>(field, EQ, value);
    }

    @Nonnull
    static <T, V> Filter<T> isNotEqualTo(@Nonnull RecordComponent field, @Nullable V value) {
        return value == null
                ? new NullRel<>(field, IS_NOT_NULL)
                : new ValueRel<>(field, NEQ, value);
    }

    @Nonnull
    static <V> RecordComponent field(@Nonnull Class<V> clazz, @Nonnull String field) {
        return Arrays.stream(clazz.getRecordComponents())
                .filter(rc -> field.equals(rc.getName()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Field: '" + field + "' not found in " + clazz));
    }

    @Nonnull
    @SafeVarargs
    static <T> Filter<T> and(@Nonnull Filter<T> first, @Nonnull Filter<T>... rest) {
        return new And<>(cons(first, rest));
    }

    @Nonnull
    @SafeVarargs
    static <T> Filter<T> or(@Nonnull Filter<T> first, @Nonnull Filter<T>... rest) {
        return new Or<>(cons(first, rest));
    }

    @Nonnull
    static <T> Filter<T> not(@Nonnull Filter<T> e) {
        return e.negate();
    }

    /////////////////////////////////////

    @Nonnull
    List<Filter<T>> children();

    @Nonnull
    default Filter<T> and(@Nullable Filter<T> other) {
        return other == null ? this : and(this, other);
    }

    @Nonnull
    default Filter<T> or(@Nullable Filter<T> other) {
        return other == null ? this : or(this, other);
    }

    @Nonnull
    default Filter<T> negate() {
        return new Not<>(this);
    }

    <R> R visit(@Nonnull Visitor<T, R> visitor);

    //////////////////////////////////////

    abstract sealed class Leaf<T> implements Filter<T> permits True, False, NullRel, ValueRel {
        @Nonnull
        @Override
        public final List<Filter<T>> children() {
            return List.of();
        }

        @Nonnull
        public abstract String toString();

        @Override
        public abstract boolean equals(Object o);

        @Override
        public abstract int hashCode();
    }

    sealed interface Composite<T> extends Filter<T> permits And, Or, Not {
    }

    final class True<T> extends Leaf<T> {
        static final True<?> INSTANCE = new True<>();

        private True() {
        }

        @Nonnull
        @Override
        public Filter<T> negate() {
            return alwaysFalse();
        }

        @Nonnull
        @Override
        public String toString() {
            return "alwaysTrue()";
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitTrue(this);
        }
    }

    final class False<T> extends Leaf<T> {
        static final False<?> INSTANCE = new False<>();

        private False() {
        }

        @Nonnull
        @Override
        public Filter<T> negate() {
            return alwaysTrue();
        }

        @Nonnull
        @Override
        public String toString() {
            return "alwaysFalse()";
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return -1;
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitFalse(this);
        }
    }

    final class NullRel<T> extends Leaf<T> {
        private final RecordComponent field;
        private final Type type;

        public NullRel(@Nonnull RecordComponent field, @Nonnull Type type) {
            this.field = requireNonNull(field, "field");
            this.type = requireNonNull(type, "type");
        }

        @Nonnull
        public RecordComponent getField() {
            return field;
        }

        @Nonnull
        public Type getType() {
            return type;
        }

        @Nonnull
        @Override
        public Filter<T> negate() {
            return new NullRel<>(this.field, this.type.negate());
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof NullRel other
                    && this.type.equals(other.type)
                    && this.field.equals(other.field);
        }

        @Override
        public int hashCode() {
            return hash(this.type, this.field);
        }

        @Nonnull
        @Override
        public String toString() {
            return format("%s.%s %s", field.getDeclaringRecord().getCanonicalName(), field.getName(), type);
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitNullRel(this);
        }

        public enum Type {
            IS_NULL {
                @Override
                Type negate() {
                    return IS_NOT_NULL;
                }
            },
            IS_NOT_NULL {
                @Override
                Type negate() {
                    return IS_NULL;
                }
            };

            abstract Type negate();
        }
    }

    final class ValueRel<T, V> extends Leaf<T> {
        private final RecordComponent field;
        private final Type type;
        private final V value;

        public ValueRel(@Nonnull RecordComponent field, @Nonnull Type type, @Nonnull V value) {
            this.field = requireNonNull(field);
            this.type = requireNonNull(type);
            this.value = requireNonNull(value);
        }

        @Nonnull
        public RecordComponent getField() {
            return field;
        }

        @Nonnull
        public Type getType() {
            return type;
        }

        @Nonnull
        public V getValue() {
            return value;
        }

        @Nonnull
        @Override
        public Filter<T> negate() {
            return new ValueRel<>(this.field, this.type.negate(), this.value);
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitValueRel(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ValueRel other
                    && this.type.equals(other.type)
                    && this.field.equals(other.field)
                    && this.value.equals(other.value);
        }

        @Override
        public int hashCode() {
            return hash(this.type, this.field, this.value);
        }

        @Nonnull
        @Override
        public String toString() {
            return format("%s.%s %s %s", field.getDeclaringRecord().getCanonicalName(), field.getName(), type, value);
        }

        public enum Type {
            EQ {
                @Override
                public Type negate() {
                    return NEQ;
                }
            },
            NEQ {
                @Override
                public Type negate() {
                    return EQ;
                }
            },
            ;

            public abstract Type negate();
        }
    }

    record And<T>(List<Filter<T>> children) implements Composite<T> {
        @Nonnull
        @Override
        public Filter<T> negate() {
            // NOT(x AND y AND z) <=> (NOT(x)) OR (NOT(y)) OR (NOT(z))
            return new Or<>(children.stream().map(Filter::negate).toList());
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitAnd(this);
        }

        @Nonnull
        @Override
        public String toString() {
            return children.stream().map(c -> "(" + c + ")").collect(joining(" and "));
        }
    }

    record Or<T>(List<Filter<T>> children) implements Composite<T> {
        @Nonnull
        @Override
        public Filter<T> negate() {
            // NOT(x OR y OR z) <=> (NOT(x)) AND (NOT(y)) AND (NOT(z))
            return new And<>(children.stream().map(Filter::negate).toList());
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitOr(this);
        }

        @Nonnull
        @Override
        public String toString() {
            return children.stream().map(c -> "(" + c + ")").collect(joining(" or "));
        }
    }

    record Not<T>(Filter<T> delegate) implements Composite<T> {
        @Nonnull
        @Override
        public List<Filter<T>> children() {
            return List.of(delegate);
        }

        @Nonnull
        @Override
        public Filter<T> negate() {
            return delegate;
        }

        @Override
        public <R> R visit(@Nonnull Visitor<T, R> visitor) {
            return visitor.visitNot(this);
        }

        @Nonnull
        @Override
        public String toString() {
            return "not(" + delegate + ")";
        }
    }

    interface Visitor<T, R> {
        R visitTrue(True<T> t);

        R visitFalse(False<T> f);

        R visitNullRel(NullRel<T> rel);

        R visitValueRel(ValueRel<T, ?> rel);

        R visitAnd(And<T> and);

        R visitOr(Or<T> or);

        R visitNot(Not<T> not);
    }
}
